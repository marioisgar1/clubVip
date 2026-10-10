package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.PaymentMethodVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.SaleExchangeResultVo;
import com.prolog.clubvip.vo.SaleExchangeReturnItemVo;
import com.prolog.clubvip.vo.SaleExchangeVo;
import com.prolog.clubvip.vo.SaleItemVo;
import com.prolog.clubvip.vo.SaleLocationVo;
import com.prolog.clubvip.vo.SaleVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Merchandise exchange (SALE_EXCHANGE), ported from Leonisa: find the original ticket, pick what
 * comes back, scan what the customer takes instead. Returned goods are credited at the price they
 * were really sold for, new ones charged at today's price, so only the difference is collected.
 * The original sale is never modified (the backend writes its own EXCHANGE document); when the
 * credit is worth more than the new goods nothing is refunded in cash and the screen says so.
 *
 * <p>sale/getExchangeableSales?from=&to= · sale/getSaleForExchange/{idOrFolio} · sale/scan/{barcode} ·
 * sale/registerExchange
 */
@Named("saleExchangeMb")
@ViewScoped
public class SaleExchangeMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type PAYMENT_LIST = new TypeToken<List<PaymentMethodVo>>() { }.getType();
    private static final Type LOCATION_LIST = new TypeToken<List<SaleLocationVo>>() { }.getType();
    private static final Type SALE_LIST = new TypeToken<List<SaleVo>>() { }.getType();
    /** Default span offered when searching by date; the backend enforces the real window. */
    private static final int DEFAULT_RANGE_DAYS = 30;

    /** Step 1a: ticket folio (V000123) or plain sale id. */
    private String folio;
    /** Step 1b: when the customer has no ticket, pick it from the sales of a date range. */
    private Date from;
    private Date to;
    private List<SaleVo> candidates = new ArrayList<>();
    private SaleVo originSale;

    /** Step 2: exchangeable lines of the original sale, with how many units come back. */
    private List<SaleExchangeReturnItemVo> returnItems = new ArrayList<>();

    /** Step 3: replacement products. */
    private String barcode;
    private List<SaleItemVo> cart = new ArrayList<>();

    private List<PaymentMethodVo> paymentMethods = new ArrayList<>();
    private List<SaleLocationVo> saleLocations = new ArrayList<>();
    private Integer paymentMethodId;
    private Integer saleLocationId;

    private SaleExchangeResultVo lastResult;

    @PostConstruct
    public void init() {
        from = toDate(today().minusDays(DEFAULT_RANGE_DAYS));
        to = toDate(today());
        refresh();
    }

    public void refresh() {
        try {
            List<PaymentMethodVo> all = RestClient.get("paymentMethod/getPaymentMethods", PAYMENT_LIST, userId(), locale());
            paymentMethods = new ArrayList<>();
            if (all != null) {
                for (PaymentMethodVo method : all) {
                    // An exchange has no instalment plan, so it cannot go on credit: the difference
                    // is settled on the spot. The backend rejects it too.
                    if (!Boolean.TRUE.equals(method.getCredit())) {
                        paymentMethods.add(method);
                    }
                }
            }
        } catch (RestException e) {
            errors(e);
        }
        try {
            List<SaleLocationVo> list = RestClient.get("saleLocation/getSaleLocations", LOCATION_LIST, userId(), locale());
            saleLocations = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    // --- step 1: find the original ticket -------------------------------------------------

    public void findSale() {
        if (folio == null || folio.isBlank()) {
            errorKey("pos.exchange.folioRequired");
            return;
        }
        loadSale(folio.trim());
    }

    public void searchByDate() {
        candidates = new ArrayList<>();
        try {
            List<SaleVo> list = RestClient.get("sale/getExchangeableSales?from=" + isoDay(from) + "&to=" + isoDay(to),
                    SALE_LIST, userId(), locale());
            candidates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void selectSale(SaleVo sale) {
        if (sale == null || sale.getId() == null) {
            return;
        }
        folio = sale.getFolio();
        loadSale(String.valueOf(sale.getId()));
    }

    private void loadSale(String idOrFolio) {
        reset();
        try {
            SaleVo sale = RestClient.get("sale/getSaleForExchange/" + RestClient.encode(idOrFolio), SaleVo.class, userId(), locale());
            if (sale == null || sale.getId() == null) {
                errorKey("pos.exchange.saleNotFound");
                return;
            }
            originSale = sale;
            saleLocationId = sale.getSaleLocationId();
            if (sale.getItems() != null) {
                for (SaleItemVo item : sale.getItems()) {
                    int returnable = nz(item.getReturnableQuantity());
                    if (returnable <= 0) {
                        continue;
                    }
                    SaleExchangeReturnItemVo line = new SaleExchangeReturnItemVo();
                    line.setSaleItemId(item.getId());
                    line.setQuantity(0);
                    line.setRestock(true);
                    line.setBarcode(item.getBarcode());
                    line.setDescription(item.getDescription());
                    line.setUnitPrice(item.getUnitPrice());
                    line.setReturnableQuantity(returnable);
                    returnItems.add(line);
                }
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Clears the whole operation, keeping the catalogs and the date search already loaded. */
    public void reset() {
        originSale = null;
        returnItems = new ArrayList<>();
        cart = new ArrayList<>();
        barcode = null;
        paymentMethodId = null;
        saleLocationId = null;
        lastResult = null;
    }

    // --- step 3: replacement products ------------------------------------------------------

    /** GET sale/scan/{barcode}: straight into the replacement cart. */
    public void scan() {
        if (barcode == null || barcode.isBlank()) {
            return;
        }
        String code = barcode.trim();
        barcode = null;
        try {
            ProductVo p = RestClient.get("sale/scan/" + RestClient.encode(code), ProductVo.class, userId(), locale());
            if (p == null || p.getId() == null) {
                errorKey("pos.sale.notFound");
                return;
            }
            for (SaleItemVo line : cart) {
                if (p.getId().equals(line.getProductId())) {
                    line.setQuantity(nz(line.getQuantity()) + 1);
                    return;
                }
            }
            SaleItemVo item = new SaleItemVo();
            item.setProductId(p.getId());
            item.setBarcode(p.getBarcode());
            item.setDescription(p.getName());
            item.setQuantity(1);
            BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            item.setListPrice(price);
            item.setUnitPrice(price);
            item.setLineTotal(price);
            cart.add(item);
        } catch (RestException e) {
            errors(e);
        }
    }

    public void removeFromCart(SaleItemVo item) {
        cart.remove(item);
    }

    // --- running balance -------------------------------------------------------------------

    public BigDecimal getCreditAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleExchangeReturnItemVo item : returnItems) {
            sum = sum.add(item.getComputedCredit());
        }
        return sum;
    }

    public BigDecimal getNewAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleItemVo item : cart) {
            sum = sum.add(item.getComputedLineTotal());
        }
        return sum;
    }

    /** Positive: the customer pays. Negative: credit left over, not refunded. */
    public BigDecimal getDifference() {
        return getNewAmount().subtract(getCreditAmount());
    }

    public BigDecimal getAmountToPay() {
        BigDecimal difference = getDifference();
        return difference.signum() > 0 ? difference : BigDecimal.ZERO;
    }

    public BigDecimal getCreditLeftOver() {
        BigDecimal difference = getDifference();
        return difference.signum() < 0 ? difference.negate() : BigDecimal.ZERO;
    }

    public boolean isCustomerPays() {
        return getDifference().signum() > 0;
    }

    public boolean isCreditLeft() {
        return getDifference().signum() < 0;
    }

    // --- register --------------------------------------------------------------------------

    public void registerExchange() {
        if (originSale == null) {
            errorKey("pos.exchange.findSaleFirst");
            return;
        }
        List<SaleExchangeReturnItemVo> returned = new ArrayList<>();
        for (SaleExchangeReturnItemVo item : returnItems) {
            if (item.getQuantity() != null && item.getQuantity() > 0) {
                SaleExchangeReturnItemVo line = new SaleExchangeReturnItemVo();
                line.setSaleItemId(item.getSaleItemId());
                line.setQuantity(item.getQuantity());
                line.setRestock(item.isRestock());
                line.setReason(item.getReason());
                returned.add(line);
            }
        }
        if (returned.isEmpty()) {
            errorKey("pos.exchange.pickReturned");
            return;
        }
        if (cart.isEmpty()) {
            errorKey("pos.exchange.pickNew");
            return;
        }
        if (saleLocationId == null) {
            errorKey("pos.sale.pickLocation");
            return;
        }
        if (isCustomerPays() && paymentMethodId == null) {
            errorKey("pos.sale.pickPayment");
            return;
        }

        SaleExchangeVo payload = new SaleExchangeVo();
        payload.setOriginSaleId(originSale.getId());
        payload.setSaleLocationId(saleLocationId);
        payload.setPaymentMethodId(paymentMethodId);
        payload.setReturnedItems(returned);
        List<SaleItemVo> newItems = new ArrayList<>();
        for (SaleItemVo c : cart) {
            SaleItemVo line = new SaleItemVo();
            line.setProductId(c.getProductId());
            line.setQuantity(c.getQuantity());
            line.setListPrice(c.getListPrice());
            line.setUnitPrice(c.getUnitPrice());
            newItems.add(line);
        }
        payload.setNewItems(newItems);

        try {
            SaleExchangeResultVo result = RestClient.post("sale/registerExchange", payload, SaleExchangeResultVo.class, userId(), locale());
            reset();
            folio = null;
            lastResult = result;
            if (!candidates.isEmpty()) {
                searchByDate(); // that ticket may have no units left now
            }
            info("pos.exchange.registered");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public List<SaleVo> getCandidates() {
        return candidates;
    }

    public SaleVo getOriginSale() {
        return originSale;
    }

    public List<SaleExchangeReturnItemVo> getReturnItems() {
        return returnItems;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<SaleItemVo> getCart() {
        return cart;
    }

    public List<PaymentMethodVo> getPaymentMethods() {
        return paymentMethods;
    }

    public List<SaleLocationVo> getSaleLocations() {
        return saleLocations;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Integer getSaleLocationId() {
        return saleLocationId;
    }

    public void setSaleLocationId(Integer saleLocationId) {
        this.saleLocationId = saleLocationId;
    }

    public SaleExchangeResultVo getLastResult() {
        return lastResult;
    }
}
