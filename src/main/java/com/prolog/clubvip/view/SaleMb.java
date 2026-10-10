package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CreditInstallmentVo;
import com.prolog.clubvip.vo.CreditPlanVo;
import com.prolog.clubvip.vo.CustomerVo;
import com.prolog.clubvip.vo.PaymentMethodVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.SaleItemVo;
import com.prolog.clubvip.vo.SaleLocationVo;
import com.prolog.clubvip.vo.SaleRequestVo;
import com.prolog.clubvip.vo.SaleVo;
import com.prolog.clubvip.vo.MemberLookupVo;
import com.prolog.clubvip.vo.PointsAwardVo;
import com.prolog.clubvip.vo.PointsMovementVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Point of sale (SALE_NEW): a USB reader types the barcode and presses Enter, the line lands in
 * the cart; products can also be searched by description. Prices are haggled per line, the
 * discount is shown, and a credit payment method opens the instalment setup (customer by phone,
 * down payment, schedule previewed by {@code credit/previewSchedule} and editable before selling).
 *
 * <p>sale/scan/{barcode} · product/getProducts · paymentMethod/getPaymentMethods ·
 * saleLocation/getSaleLocations · customer/getCustomerByPhone/{phone} · customer/saveUpdateCustomer ·
 * credit/previewSchedule · sale/registerSale
 */
@Named("saleMb")
@ViewScoped
public class SaleMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type PAYMENT_LIST = new TypeToken<List<PaymentMethodVo>>() { }.getType();
    private static final Type LOCATION_LIST = new TypeToken<List<SaleLocationVo>>() { }.getType();
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final Type INSTALLMENT_LIST = new TypeToken<List<CreditInstallmentVo>>() { }.getType();
    private static final int MAX_SEARCH_RESULTS = 25;

    public static final String FREQ_WEEKLY = "WEEKLY";
    public static final String FREQ_BIWEEKLY = "BIWEEKLY";
    public static final String FREQ_MONTHLY = "MONTHLY";

    // --- cart ---
    private String barcode;
    private String search;
    private List<ProductVo> searchResults = new ArrayList<>();
    private List<ProductVo> products;
    private List<SaleItemVo> cart = new ArrayList<>();

    // --- checkout ---
    private List<PaymentMethodVo> paymentMethods = new ArrayList<>();
    private List<SaleLocationVo> saleLocations = new ArrayList<>();
    private Integer paymentMethodId;
    private Integer saleLocationId;

    // --- credit setup ---
    private String customerPhone;
    private CustomerVo customer;
    private boolean customerNotFound;
    private CustomerVo newCustomer = new CustomerVo();
    private BigDecimal downPayment = BigDecimal.ZERO;
    private Integer installmentCount = 4;
    private String frequency = FREQ_BIWEEKLY;
    private java.util.Date firstDueDate;
    private List<CreditInstallmentVo> schedule = new ArrayList<>();

    // --- Rewards member card (points for this sale) ---
    private String memberNumber;
    private MemberLookupVo member;
    private PointsMovementVo lastPoints;
    private String lastPointsError;

    // --- result ---
    private SaleVo lastSale;
    private String whatsappLink;

    @PostConstruct
    public void init() {
        refresh();
        firstDueDate = toDate(nextDue(today(), frequency));
    }

    public void refresh() {
        try {
            List<PaymentMethodVo> list = RestClient.get("paymentMethod/getPaymentMethods", PAYMENT_LIST, userId(), locale());
            paymentMethods = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            paymentMethods = new ArrayList<>();
            errors(e);
        }
        try {
            List<SaleLocationVo> list = RestClient.get("saleLocation/getSaleLocations", LOCATION_LIST, userId(), locale());
            saleLocations = list != null ? list : new ArrayList<>();
            if (saleLocationId == null && saleLocations.size() == 1) {
                saleLocationId = saleLocations.get(0).getId();
            }
        } catch (RestException e) {
            saleLocations = new ArrayList<>();
            errors(e);
        }
    }

    // ------------------------------------------------------------------ cart

    /** GET sale/scan/{barcode}: the product goes straight into the cart (readers press Enter). */
    public void scan() {
        if (barcode == null || barcode.isBlank()) {
            return;
        }
        String code = barcode.trim();
        barcode = null;
        if (isMemberCard(code)) {
            // The cashier scanned the Rewards card in the product field: it is the member, not a product.
            memberNumber = code;
            lookupMember();
            return;
        }
        try {
            ProductVo p = RestClient.get("sale/scan/" + RestClient.encode(code), ProductVo.class, userId(), locale());
            if (p == null || p.getId() == null) {
                errorKey("pos.sale.notFound");
                return;
            }
            addProduct(p);
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ Rewards member

    private static boolean isMemberCard(String code) {
        return code != null && code.trim().toUpperCase().matches("^CV\\d{9}$");
    }

    /** GET points/lookupMember/{number}: who the member is and its points here. */
    public void lookupMember() {
        if (memberNumber == null || memberNumber.isBlank()) {
            return;
        }
        try {
            member = RestClient.get("points/lookupMember/" + RestClient.encode(memberNumber.trim().toUpperCase()),
                    MemberLookupVo.class, userId(), locale());
            memberNumber = member != null ? member.getMemberNumber() : null;
        } catch (RestException e) {
            member = null;
            errors(e);
        }
    }

    public void clearMember() {
        member = null;
        memberNumber = null;
    }

    /** Points the cart total will give (shown before selling). */
    public int getPointsPreview() {
        if (member == null || member.getPesosPerPoint() == null || member.getPesosPerPoint().signum() <= 0) {
            return 0;
        }
        return getTotal().divide(member.getPesosPerPoint(), 0, java.math.RoundingMode.FLOOR).intValue();
    }

    /** Local search over the (lightweight) product list by name, short description, barcode or brand. */
    public void searchProducts() {
        searchResults = new ArrayList<>();
        String q = search != null ? search.trim().toLowerCase(Locale.ROOT) : "";
        if (q.length() < 2) {
            return;
        }
        if (products == null) {
            try {
                List<ProductVo> list = RestClient.get("product/getProducts?includeImages=false", PRODUCT_LIST, userId(), locale());
                products = list != null ? list : new ArrayList<>();
            } catch (RestException e) {
                products = new ArrayList<>();
                errors(e);
            }
        }
        for (ProductVo p : products) {
            if (contains(p.getName(), q) || contains(p.getShortDescription(), q) || contains(p.getBarcode(), q)
                    || contains(p.getBrandName(), q) || contains(p.getModelName(), q)) {
                searchResults.add(p);
                if (searchResults.size() >= MAX_SEARCH_RESULTS) {
                    break;
                }
            }
        }
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    public void clearSearch() {
        search = null;
        searchResults = new ArrayList<>();
    }

    /** Adds one unit; a product already in the cart just grows its quantity. */
    public void addProduct(ProductVo p) {
        if (p == null || p.getId() == null) {
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
    }

    public void removeFromCart(SaleItemVo item) {
        cart.remove(item);
    }

    public void clearCart() {
        cart = new ArrayList<>();
        barcode = null;
        clearSearch();
        resetCredit();
    }

    public BigDecimal getSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleItemVo item : cart) {
            sum = sum.add(item.getComputedLineTotal());
        }
        return sum;
    }

    public BigDecimal getTotal() {
        return getSubtotal();
    }

    public BigDecimal getTotalDiscount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleItemVo item : cart) {
            sum = sum.add(item.getComputedDiscount());
        }
        return sum;
    }

    public int getItemCount() {
        int n = 0;
        for (SaleItemVo item : cart) {
            n += nz(item.getQuantity());
        }
        return n;
    }

    // ------------------------------------------------------------------ credit setup

    public PaymentMethodVo getSelectedMethod() {
        if (paymentMethodId == null) {
            return null;
        }
        for (PaymentMethodVo m : paymentMethods) {
            if (paymentMethodId.equals(m.getId())) {
                return m;
            }
        }
        return null;
    }

    /** True when the chosen payment method is the credit one: the sale needs a customer and a plan. */
    public boolean isCreditSelected() {
        PaymentMethodVo m = getSelectedMethod();
        return m != null && Boolean.TRUE.equals(m.getCredit());
    }

    public void onPaymentMethodChange() {
        if (!isCreditSelected()) {
            resetCredit();
        }
    }

    private void resetCredit() {
        customerPhone = null;
        customer = null;
        customerNotFound = false;
        newCustomer = new CustomerVo();
        downPayment = BigDecimal.ZERO;
        installmentCount = 4;
        frequency = FREQ_BIWEEKLY;
        firstDueDate = toDate(nextDue(today(), frequency));
        schedule = new ArrayList<>();
    }

    /** GET customer/getCustomerByPhone/{phone}: 204 means she is new, so the creation fields open. */
    public void findCustomer() {
        customer = null;
        customerNotFound = false;
        schedule = new ArrayList<>();
        String digits = customerPhone != null ? customerPhone.replaceAll("[^0-9]", "") : "";
        if (digits.length() < 10) {
            errorKey("pos.sale.credit.phoneRequired");
            return;
        }
        try {
            CustomerVo found = RestClient.get("customer/getCustomerByPhone/" + RestClient.encode(digits),
                    CustomerVo.class, userId(), locale());
            if (found != null && found.getId() != null) {
                customer = found;
            } else {
                customerNotFound = true;
                newCustomer = new CustomerVo();
                newCustomer.setPhone(digits);
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    /** POST customer/saveUpdateCustomer: on-the-spot registration at the counter. */
    public void createCustomer() {
        if (newCustomer == null) {
            return;
        }
        String first = trim(newCustomer.getFirstName());
        String last = trim(newCustomer.getLastName());
        if (first.isEmpty()) {
            errorKey("pos.sale.credit.nameRequired");
            return;
        }
        newCustomer.setFullName((first + " " + last).trim());
        if (newCustomer.getPhone() == null || newCustomer.getPhone().isBlank()) {
            newCustomer.setPhone(customerPhone != null ? customerPhone.replaceAll("[^0-9]", "") : null);
        }
        try {
            CustomerVo saved = RestClient.post("customer/saveUpdateCustomer", newCustomer, CustomerVo.class, userId(), locale());
            customer = saved != null ? saved : newCustomer;
            customerNotFound = false;
            info("pos.sale.credit.customerCreated");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void changeCustomer() {
        customer = null;
        customerNotFound = false;
        schedule = new ArrayList<>();
    }

    public BigDecimal getFinancedAmount() {
        BigDecimal financed = getTotal().subtract(downPayment != null ? downPayment : BigDecimal.ZERO);
        return financed.signum() > 0 ? financed : BigDecimal.ZERO;
    }

    public void onFrequencyChange() {
        firstDueDate = toDate(nextDue(today(), frequency));
        schedule = new ArrayList<>();
    }

    /** GET credit/previewSchedule: the proposed dates and amounts, editable afterwards. */
    public void previewSchedule() {
        schedule = new ArrayList<>();
        BigDecimal financed = getFinancedAmount();
        if (financed.signum() <= 0) {
            errorKey("pos.sale.credit.nothingToFinance");
            return;
        }
        if (installmentCount == null || installmentCount < 1) {
            errorKey("pos.sale.credit.installmentsRequired");
            return;
        }
        if (firstDueDate == null) {
            firstDueDate = toDate(nextDue(today(), frequency));
        }
        try {
            List<CreditInstallmentVo> list = RestClient.get("credit/previewSchedule?amount=" + financed.toPlainString()
                    + "&installments=" + installmentCount + "&frequency=" + RestClient.encode(frequency)
                    + "&firstDueDate=" + isoDay(firstDueDate), INSTALLMENT_LIST, userId(), locale());
            schedule = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    public BigDecimal getScheduleTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CreditInstallmentVo i : schedule) {
            sum = sum.add(i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO);
        }
        return sum;
    }

    /** The edited instalments must still add up to the financed amount (the backend re-checks it). */
    public boolean isScheduleBalanced() {
        return !schedule.isEmpty() && getScheduleTotal().compareTo(getFinancedAmount()) == 0;
    }

    public String frequencyLabel(String code) {
        if (code == null) {
            return "";
        }
        return sessionMb.text("pos.credit.frequency." + code);
    }

    // ------------------------------------------------------------------ register

    /** POST sale/registerSale with the cart (and the credit plan when the method is credit). */
    public void registerSale() {
        if (cart.isEmpty()) {
            errorKey("pos.sale.emptyCart");
            return;
        }
        if (paymentMethodId == null) {
            errorKey("pos.sale.pickPayment");
            return;
        }
        if (saleLocationId == null) {
            errorKey("pos.sale.pickLocation");
            return;
        }
        for (SaleItemVo c : cart) {
            if (nz(c.getQuantity()) < 1 || c.getUnitPrice() == null || c.getUnitPrice().signum() < 0) {
                errorKey("pos.sale.invalidLine");
                return;
            }
        }
        SaleRequestVo payload = new SaleRequestVo();
        payload.setPaymentMethodId(paymentMethodId);
        payload.setSaleLocationId(saleLocationId);
        List<SaleItemVo> items = new ArrayList<>();
        for (SaleItemVo c : cart) {
            SaleItemVo line = new SaleItemVo();
            line.setProductId(c.getProductId());
            line.setQuantity(c.getQuantity());
            line.setListPrice(c.getListPrice());
            line.setUnitPrice(c.getUnitPrice());
            items.add(line);
        }
        payload.setItems(items);

        if (isCreditSelected()) {
            if (customer == null || customer.getId() == null) {
                errorKey("pos.sale.credit.customerRequired");
                return;
            }
            if (schedule.isEmpty()) {
                errorKey("pos.sale.credit.scheduleRequired");
                return;
            }
            if (!isScheduleBalanced()) {
                errorKey("pos.sale.credit.scheduleUnbalanced");
                return;
            }
            payload.setCustomerId(customer.getId());
            CreditPlanVo plan = new CreditPlanVo();
            plan.setCustomerId(customer.getId());
            plan.setDownPayment(downPayment != null ? downPayment : BigDecimal.ZERO);
            plan.setFinancedAmount(getFinancedAmount());
            plan.setTotalAmount(getTotal());
            plan.setFrequency(frequency);
            plan.setInstallmentCount(schedule.size());
            List<CreditInstallmentVo> lines = new ArrayList<>();
            for (int i = 0; i < schedule.size(); i++) {
                CreditInstallmentVo src = schedule.get(i);
                CreditInstallmentVo line = new CreditInstallmentVo();
                line.setInstallmentNumber(i + 1);
                line.setDueDate(src.getDueDate());
                line.setAmount(src.getAmount());
                lines.add(line);
            }
            plan.setFirstDueDate(lines.get(0).getDueDate());
            plan.setInstallments(lines);
            payload.setCredit(plan);
        }

        // Plain days must travel as yyyy-MM-dd (the backend's @JsonFormat), not as Gson's default date text.
        JsonElement body = new GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJsonTree(payload);
        try {
            SaleVo saved = RestClient.post("sale/registerSale", body, SaleVo.class, userId(), locale());
            lastSale = saved != null ? saved : new SaleVo();
            whatsappLink = buildWhatsappLink(lastSale);
            awardPoints(lastSale);
            clearCart();
            paymentMethodId = null;
            products = null; // stock changed: reload on the next search
            info("pos.sale.registered");
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Rewards: the member of the card gets the points of the sale; a failure never undoes the sale. */
    private void awardPoints(SaleVo sale) {
        lastPoints = null;
        lastPointsError = null;
        if (member == null || sale == null || sale.getId() == null) {
            return;
        }
        PointsAwardVo award = new PointsAwardVo();
        award.setMemberNumber(member.getMemberNumber());
        award.setSaleId(sale.getId());
        try {
            lastPoints = RestClient.post("points/award", award, PointsMovementVo.class, userId(), locale());
        } catch (RestException e) {
            lastPointsError = e.getMessages() != null && !e.getMessages().isEmpty() ? e.getMessages().get(0) : null;
        }
        clearMember();
    }

    public void closeResult() {
        lastSale = null;
        whatsappLink = null;
        lastPoints = null;
        lastPointsError = null;
    }

    public boolean isHasStockWarnings() {
        return lastSale != null && lastSale.getStockWarnings() != null && !lastSale.getStockWarnings().isEmpty();
    }

    // ------------------------------------------------------------------ whatsapp

    /** wa.me link to the buyer with the itemised ticket and every payment commitment (credit sales only). */
    private String buildWhatsappLink(SaleVo sale) {
        if (sale == null || sale.getCredit() == null) {
            return null;
        }
        CreditPlanVo plan = sale.getCredit();
        String phone = plan.getCustomerPhone() != null ? plan.getCustomerPhone()
                : (customer != null ? customer.getPhone() : null);
        String digits = waDigits(phone);
        if (digits.isEmpty()) {
            return null;
        }
        return WhatsAppLink.withText(digits, buildCreditMessage(sale, plan));
    }

    String buildCreditMessage(SaleVo sale, CreditPlanVo plan) {
        StringBuilder sb = new StringBuilder();
        String name = plan.getCustomerName() != null ? plan.getCustomerName()
                : (customer != null ? customer.getFullName() : "");
        sb.append(t("pos.sale.wa.greeting", name)).append("\n\n");
        sb.append(t("pos.sale.wa.header", sale.getFolio() != null ? sale.getFolio() : "", sessionMb.getBusinessName())).append('\n');
        List<SaleItemVo> items = plan.getItems() != null && !plan.getItems().isEmpty() ? plan.getItems() : sale.getItems();
        if (items != null && !items.isEmpty()) {
            sb.append('\n').append(t("pos.sale.wa.products")).append('\n');
            for (SaleItemVo item : items) {
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal() : item.getComputedLineTotal();
                sb.append("- ");
                if (qty > 1) {
                    sb.append(qty).append(" x ");
                }
                sb.append(item.getDescription() != null ? item.getDescription() : "").append("  ")
                        .append(sessionMb.fmtMoney(lineTotal)).append('\n');
            }
        }
        BigDecimal total = plan.getTotalAmount() != null ? plan.getTotalAmount() : sale.getTotal();
        sb.append('\n').append(t("pos.sale.wa.total", sessionMb.fmtMoney(total))).append('\n');
        if (plan.getDownPayment() != null && plan.getDownPayment().signum() > 0) {
            sb.append(t("pos.sale.wa.downPayment", sessionMb.fmtMoney(plan.getDownPayment()))).append('\n');
            sb.append(t("pos.sale.wa.financed", sessionMb.fmtMoney(plan.getFinancedAmount()))).append('\n');
        }
        sb.append('\n').append(t("pos.sale.wa.schedule", frequencyLabel(plan.getFrequency()))).append('\n');
        if (plan.getInstallments() != null) {
            for (CreditInstallmentVo i : plan.getInstallments()) {
                sb.append(i.getInstallmentNumber()).append(". ").append(fmtDay(i.getDueDate()))
                        .append(" - ").append(sessionMb.fmtMoney(i.getAmount())).append('\n');
            }
        }
        BigDecimal balance = plan.getBalance() != null ? plan.getBalance() : plan.getFinancedAmount();
        sb.append('\n').append(t("pos.sale.wa.balance", sessionMb.fmtMoney(balance))).append('\n');
        sb.append('\n').append(t("pos.sale.wa.footer"));
        return sb.toString();
    }

    /** i18n text with {0}, {1}... replaced (no MessageFormat: the texts carry apostrophes freely). */
    private String t(String key, String... args) {
        String text = sessionMb.text(key);
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", args[i] != null ? args[i] : "");
        }
        return text;
    }

    // ------------------------------------------------------------------ helpers

    /** One period after a day: 7 days, 15 days ("quincena") or one month. */
    static LocalDate nextDue(LocalDate from, String frequency) {
        if (FREQ_WEEKLY.equals(frequency)) {
            return from.plusDays(7);
        }
        if (FREQ_MONTHLY.equals(frequency)) {
            return from.plusMonths(1);
        }
        return from.plusDays(15);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public List<String> getFrequencies() {
        List<String> list = new ArrayList<>();
        list.add(FREQ_WEEKLY);
        list.add(FREQ_BIWEEKLY);
        list.add(FREQ_MONTHLY);
        return list;
    }

    // ------------------------------------------------------------------ getters / setters

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<ProductVo> getSearchResults() {
        return searchResults;
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

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public CustomerVo getCustomer() {
        return customer;
    }

    public boolean isCustomerNotFound() {
        return customerNotFound;
    }

    public CustomerVo getNewCustomer() {
        return newCustomer;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public java.util.Date getFirstDueDate() {
        return firstDueDate;
    }

    public void setFirstDueDate(java.util.Date firstDueDate) {
        this.firstDueDate = firstDueDate;
    }

    public List<CreditInstallmentVo> getSchedule() {
        return schedule;
    }

    public SaleVo getLastSale() {
        return lastSale;
    }

    public String getWhatsappLink() {
        return whatsappLink;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public MemberLookupVo getMember() {
        return member;
    }

    public void setMember(MemberLookupVo member) {
        this.member = member;
    }

    public PointsMovementVo getLastPoints() {
        return lastPoints;
    }

    public void setLastPoints(PointsMovementVo lastPoints) {
        this.lastPoints = lastPoints;
    }

    public String getLastPointsError() {
        return lastPointsError;
    }

    public void setLastPointsError(String lastPointsError) {
        this.lastPointsError = lastPointsError;
    }
}
