package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ConsignmentDetailVo;
import com.prolog.clubvip.vo.ConsignmentLineVo;
import com.prolog.clubvip.vo.ConsignmentPaymentRequestVo;
import com.prolog.clubvip.vo.ConsignmentPaymentVo;
import com.prolog.clubvip.vo.ConsignmentReceiptVo;
import com.prolog.clubvip.vo.ConsignmentProductVo;
import com.prolog.clubvip.vo.ConsignmentReceiveVo;
import com.prolog.clubvip.vo.ConsignmentSaleLineVo;
import com.prolog.clubvip.vo.ConsignmentShipmentItemVo;
import com.prolog.clubvip.vo.ConsignmentShipmentRequestVo;
import com.prolog.clubvip.vo.ConsignmentShipmentVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * File of one consignment relation, shown inside consignment.xhtml (?id= opens it directly): the
 * big numbers, pieces at the store, shipments, and the send / receive / reject / cancel actions.
 * consignment/getAgreement/{id} · getAvailableProducts/{id} · sendShipment/{id} · getShipment/{id}
 * · receive/{id} · rejectShipment/{id} · cancelShipment/{id}
 */
@Named("consignmentFileMb")
@ViewScoped
public class ConsignmentFileMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type PRODUCT_LIST = new TypeToken<List<ConsignmentProductVo>>() { }.getType();
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private ConsignmentDetailVo detail;

    // send dialog
    private String productSearch;
    private List<ConsignmentProductVo> productResults = new ArrayList<>();
    private List<CartLine> cart = new ArrayList<>();
    private String notes;

    // receive / view dialog
    private ConsignmentShipmentVo shipment;
    /** Ventas: pending lines the store ticked to pay. */
    private List<ConsignmentSaleLineVo> selectedSales = new ArrayList<>();
    private String payNotes;
    /** Receipt read in the browser (data URL), its name and type (hidden fields filled by JS). */
    private String receiptBase64;
    private String receiptName;
    private String receiptContentType;
    private ConsignmentReceiptVo receipt;
    private ConsignmentPaymentVo viewingPayment;
    private ConsignmentPaymentVo rejectingPayment;
    private String rejectPaymentReason;
    private String receiveComment;
    private String rejectReason;

    /** One product of the shipment being built. */
    public static class CartLine implements Serializable {
        private static final long serialVersionUID = 1L;
        private final ConsignmentProductVo product;
        private Integer quantity = 1;

        CartLine(ConsignmentProductVo product) {
            this.product = product;
        }

        public ConsignmentProductVo getProduct() {
            return product;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    @PostConstruct
    public void init() {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (id != null && id.matches("\\d+")) {
            open(Integer.valueOf(id));
        }
    }

    // ------------------------------------------------------------------ navigation

    public void open(Integer agreementId) {
        try {
            detail = RestClient.get("consignment/getAgreement/" + agreementId, ConsignmentDetailVo.class, userId(), locale());
        } catch (RestException e) {
            detail = null;
            errors(e);
        }
    }

    public void reload() {
        if (detail != null && detail.getAgreement() != null) {
            open(detail.getAgreement().getId());
        }
    }

    public void close() {
        detail = null;
    }

    // ------------------------------------------------------------------ send (supplier)

    public void prepareSend() {
        productSearch = null;
        cart = new ArrayList<>();
        notes = null;
        searchProducts();
    }

    public void searchProducts() {
        try {
            String q = productSearch == null || productSearch.isBlank() ? "" : "?q=" + RestClient.encode(productSearch.trim());
            List<ConsignmentProductVo> list = RestClient.get("consignment/getAvailableProducts/" + agreementId() + q,
                    PRODUCT_LIST, userId(), locale());
            productResults = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            productResults = new ArrayList<>();
            errors(e);
        }
    }

    /** Enter in the search box (a barcode scanner ends with Enter): an exact barcode or single result goes in. */
    public void searchAndAdd() {
        searchProducts();
        String code = productSearch == null ? "" : productSearch.trim();
        ConsignmentProductVo match = null;
        for (ConsignmentProductVo p : productResults) {
            if (code.equals(p.getBarcode())) {
                match = p;
                break;
            }
        }
        if (match == null && productResults.size() == 1) {
            match = productResults.get(0);
        }
        if (match != null) {
            add(match);
            productSearch = null;
        }
    }

    public void add(ConsignmentProductVo p) {
        for (CartLine line : cart) {
            if (line.getProduct().getProductId().equals(p.getProductId())) {
                if (line.getQuantity() < p.getAvailable()) {
                    line.setQuantity(line.getQuantity() + 1);
                } else {
                    error(sessionMb.text("cons.send.noMore"));
                }
                return;
            }
        }
        cart.add(new CartLine(p));
    }

    public void remove(CartLine line) {
        cart.remove(line);
    }

    public int getCartPieces() {
        int n = 0;
        for (CartLine line : cart) {
            n += line.getQuantity() != null ? line.getQuantity() : 0;
        }
        return n;
    }

    public BigDecimal getCartValue() {
        BigDecimal v = BigDecimal.ZERO;
        for (CartLine line : cart) {
            if (line.getQuantity() != null && line.getProduct().getPrice() != null) {
                v = v.add(line.getProduct().getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return v;
    }

    /** What the supplier gets if everything sells: value × (100 − commission) %. */
    public BigDecimal getCartSupplierValue() {
        BigDecimal pct = detail != null && detail.getAgreement() != null ? detail.getAgreement().getSupplierPercent() : null;
        return pct == null ? null : getCartValue().multiply(pct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public void send() {
        if (cart.isEmpty()) {
            errorKey("cons.send.empty");
            return;
        }
        ConsignmentShipmentRequestVo request = new ConsignmentShipmentRequestVo();
        for (CartLine line : cart) {
            ConsignmentLineVo l = new ConsignmentLineVo();
            l.setProductId(line.getProduct().getProductId());
            l.setQuantity(line.getQuantity());
            request.getItems().add(l);
        }
        request.setNotes(notes);
        try {
            ConsignmentShipmentVo sent = RestClient.post("consignment/sendShipment/" + agreementId(), request,
                    ConsignmentShipmentVo.class, userId(), locale());
            info("cons.msg.sent");
            reload();
            PrimeFaces.current().executeScript("PF('consSendDlg').hide()");
            if (sent != null) {
                shipment = sent;
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ receive / view (both sides)

    /** From the hub notice: opens the relation and, on top, the shipment ready to receive. */
    public void receiveFrom(ConsignmentShipmentVo s) {
        open(s.getAgreementId());
        if (detail != null) {
            openShipment(s);
        }
    }

    public void openShipment(ConsignmentShipmentVo s) {
        try {
            shipment = RestClient.get("consignment/getShipment/" + s.getId(), ConsignmentShipmentVo.class, userId(), locale());
            if (shipment != null && Boolean.TRUE.equals(shipment.getCanReceive())) {
                // Everything comes checked as received; only what is missing gets lowered.
                for (ConsignmentShipmentItemVo i : shipment.getItems()) {
                    i.setQuantityReceived(i.getQuantitySent());
                }
            }
            receiveComment = null;
            rejectReason = null;
        } catch (RestException e) {
            shipment = null;
            errors(e);
        }
    }

    public int getReceivingPieces() {
        int n = 0;
        if (shipment != null) {
            for (ConsignmentShipmentItemVo i : shipment.getItems()) {
                n += i.getQuantityReceived() != null ? i.getQuantityReceived() : 0;
            }
        }
        return n;
    }

    public void receive() {
        if (shipment == null) {
            return;
        }
        ConsignmentReceiveVo vo = new ConsignmentReceiveVo();
        for (ConsignmentShipmentItemVo i : shipment.getItems()) {
            ConsignmentLineVo l = new ConsignmentLineVo();
            l.setItemId(i.getId());
            l.setQuantity(i.getQuantityReceived() != null ? i.getQuantityReceived() : 0);
            vo.getItems().add(l);
        }
        vo.setComment(receiveComment);
        try {
            RestClient.post("consignment/receive/" + shipment.getId(), vo, null, userId(), locale());
            info("cons.msg.received");
            PrimeFaces.current().executeScript("PF('consShipmentDlg').hide()");
            reload();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void rejectShipment() {
        if (shipment == null) {
            return;
        }
        try {
            String reason = rejectReason == null || rejectReason.isBlank() ? ""
                    : "?reason=" + RestClient.encode(rejectReason.trim());
            RestClient.post("consignment/rejectShipment/" + shipment.getId() + reason, null, null, userId(), locale());
            info("cons.msg.shipmentRejected");
            PrimeFaces.current().executeScript("PF('consShipmentRejectDlg').hide();PF('consShipmentDlg').hide()");
            reload();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void cancelShipment(ConsignmentShipmentVo s) {
        try {
            RestClient.post("consignment/cancelShipment/" + s.getId(), null, null, userId(), locale());
            info("cons.msg.shipmentCancelled");
            reload();
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ ventas y cobranza
    //  La tienda elige qué pendientes paga, ve la suma, adjunta el comprobante y paga (REPORTED);
    //  el proveedor ve el comprobante y cierra el pago (CONFIRMED) o lo rechaza (regresan a pendiente).

    /** Store: pending lines ticked to pay. */
    public BigDecimal getSelectedTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (ConsignmentSaleLineVo l : selectedSales) {
            if (isPending(l) && l.getNetAmount() != null) {
                sum = sum.add(l.getNetAmount());
            }
        }
        return sum;
    }

    public int getSelectedCount() {
        int n = 0;
        for (ConsignmentSaleLineVo l : selectedSales) {
            if (isPending(l)) {
                n++;
            }
        }
        return n;
    }

    /** Store: the line is ticked to pay. */
    public boolean isSelected(ConsignmentSaleLineVo l) {
        if (l == null) {
            return false;
        }
        for (ConsignmentSaleLineVo x : selectedSales) {
            if (x.getSaleItemId() != null && x.getSaleItemId().equals(l.getSaleItemId())) {
                return true;
            }
        }
        return false;
    }

    /** Store: the checkbox of a pending line. */
    public void toggleLine(ConsignmentSaleLineVo l) {
        if (l == null || !isPending(l)) {
            return;
        }
        if (isSelected(l)) {
            selectedSales.removeIf(x -> x.getSaleItemId() != null && x.getSaleItemId().equals(l.getSaleItemId()));
        } else {
            selectedSales.add(l);
        }
    }

    public boolean isPending(ConsignmentSaleLineVo l) {
        return l != null && (l.getPaymentStatus() == null || "PENDING".equals(l.getPaymentStatus()));
    }

    /** Store: every pending line is ticked (the button then offers to clear the selection). */
    public boolean isAllPendingSelected() {
        return detail != null && detail.getPendingCount() > 0 && getSelectedCount() >= detail.getPendingCount();
    }

    /** Store: "Seleccionar todo lo pendiente" / "Quitar selección". */
    public void toggleAllPending() {
        if (isAllPendingSelected()) {
            selectedSales = new ArrayList<>();
        } else {
            selectAllPending();
        }
    }

    /** Store: tick every pending line (the usual "pay everything I owe"). */
    public void selectAllPending() {
        selectedSales = new ArrayList<>();
        if (detail != null) {
            for (ConsignmentSaleLineVo l : detail.getSales()) {
                if (isPending(l)) {
                    selectedSales.add(l);
                }
            }
        }
    }

    /** Store: opens the pay dialog with what is ticked (the button is disabled while nothing is). */
    public void preparePay() {
        if (getSelectedCount() == 0) {
            errorKey("cons.msg.selectPending");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        payNotes = null;
        receiptBase64 = null;
        receiptName = null;
        receiptContentType = null;
    }

    /** Store: reports the payment of the ticked pending lines with its receipt. */
    public void reportPayment() {
        List<Integer> ids = new ArrayList<>();
        for (ConsignmentSaleLineVo l : selectedSales) {
            if (isPending(l)) {
                ids.add(l.getSaleItemId());
            }
        }
        if (ids.isEmpty()) {
            errorKey("cons.msg.selectPending");
            return;
        }
        if (receiptBase64 == null || receiptBase64.isBlank()) {
            errorKey("cons.pay.receiptMissing");
            return;
        }
        ConsignmentPaymentRequestVo request = new ConsignmentPaymentRequestVo();
        request.setSaleItemIds(ids);
        request.setNotes(payNotes);
        request.setReceiptBase64(receiptBase64);
        request.setReceiptName(receiptName);
        request.setReceiptContentType(receiptContentType);
        if (refreshWith(RestCall.of(() -> RestClient.post("consignment/reportPayment/" + agreementId(), request,
                ConsignmentDetailVo.class, userId(), locale())))) {
            info("cons.msg.paymentReported");
            PrimeFaces.current().executeScript("PF('consPayDlg').hide()");
        }
    }

    /** Supplier: the money arrived, the payment is closed. */
    public void confirmPayment(ConsignmentPaymentVo p) {
        if (p != null && refreshWith(RestCall.of(() -> RestClient.post("consignment/confirmPayment/" + p.getId(), null,
                ConsignmentDetailVo.class, userId(), locale())))) {
            info("cons.msg.paymentConfirmed");
        }
    }

    public void prepareReject(ConsignmentPaymentVo p) {
        rejectingPayment = p;
        rejectPaymentReason = null;
    }

    /** Supplier: did not receive it; its sales go back to pending. */
    public void rejectPayment() {
        if (rejectingPayment == null) {
            return;
        }
        String reason = rejectPaymentReason == null || rejectPaymentReason.isBlank() ? ""
                : "?reason=" + RestClient.encode(rejectPaymentReason.trim());
        Integer id = rejectingPayment.getId();
        if (refreshWith(RestCall.of(() -> RestClient.post("consignment/rejectPayment/" + id + reason, null,
                ConsignmentDetailVo.class, userId(), locale())))) {
            info("cons.msg.paymentRejected");
            PrimeFaces.current().executeScript("PF('consRejectPayDlg').hide()");
        }
    }

    /** Both: loads the receipt of a payment for the viewer dialog. */
    public void viewReceipt(ConsignmentPaymentVo p) {
        receipt = null;
        viewingPayment = p;
        if (p == null) {
            return;
        }
        try {
            receipt = RestClient.get("consignment/getPaymentReceipt/" + p.getId(), ConsignmentReceiptVo.class, userId(), locale());
        } catch (RestException e) {
            errors(e);
        }
    }

    public boolean isReceiptImage() {
        return receipt != null && receipt.getContentType() != null && receipt.getContentType().startsWith("image/");
    }

    /** data: URL of the receipt (images inline in the viewer, PDF in an embedded frame / download link). */
    public String getReceiptDataUrl() {
        return receipt == null || receipt.getBase64() == null ? ""
                : "data:" + receipt.getContentType() + ";base64," + receipt.getBase64();
    }

    public org.primefaces.model.StreamedContent getReceiptFile() {
        if (receipt == null || receipt.getBase64() == null) {
            return null;
        }
        byte[] bytes = java.util.Base64.getDecoder().decode(receipt.getBase64());
        return org.primefaces.model.DefaultStreamedContent.builder()
                .name(receipt.getName() != null ? receipt.getName() : "comprobante")
                .contentType(receipt.getContentType())
                .stream(() -> new java.io.ByteArrayInputStream(bytes))
                .build();
    }

    public String paymentStatusLabel(String status) {
        return status != null ? sessionMb.text("cons.payStatus." + status) : "";
    }

    public String paymentStatusClass(String status) {
        if ("CONFIRMED".equals(status)) {
            return "badge badge-success";
        }
        if ("REJECTED".equals(status)) {
            return "badge badge-danger";
        }
        return "badge badge-info";
    }

    public String lineStatusLabel(ConsignmentSaleLineVo l) {
        String st = l != null && l.getPaymentStatus() != null ? l.getPaymentStatus() : "PENDING";
        return sessionMb.text("cons.lineStatus." + st);
    }

    public String lineStatusClass(ConsignmentSaleLineVo l) {
        String st = l != null && l.getPaymentStatus() != null ? l.getPaymentStatus() : "PENDING";
        return "CONFIRMED".equals(st) ? "badge badge-success" : "REPORTED".equals(st) ? "badge badge-info" : "badge badge-warning";
    }

    /** Runs a call that answers the refreshed file; true when it worked. */
    private boolean refreshWith(RestCall call) {
        try {
            ConsignmentDetailVo updated = call.run();
            if (updated != null) {
                detail = updated;
            } else {
                reload();
            }
            selectedSales = new ArrayList<>();
            return true;
        } catch (RestException e) {
            errors(e);
            return false;
        }
    }

    @FunctionalInterface
    private interface RestCall {
        ConsignmentDetailVo run() throws RestException;

        static RestCall of(RestCall c) {
            return c;
        }
    }

    public List<ConsignmentSaleLineVo> getSelectedSales() {
        return selectedSales;
    }

    public void setSelectedSales(List<ConsignmentSaleLineVo> selectedSales) {
        this.selectedSales = selectedSales != null ? selectedSales : new ArrayList<>();
    }

    public String getPayNotes() {
        return payNotes;
    }

    public void setPayNotes(String payNotes) {
        this.payNotes = payNotes;
    }

    public String getReceiptBase64() {
        return receiptBase64;
    }

    public void setReceiptBase64(String receiptBase64) {
        this.receiptBase64 = receiptBase64;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getReceiptContentType() {
        return receiptContentType;
    }

    public void setReceiptContentType(String receiptContentType) {
        this.receiptContentType = receiptContentType;
    }

    public ConsignmentReceiptVo getReceipt() {
        return receipt;
    }

    public ConsignmentPaymentVo getViewingPayment() {
        return viewingPayment;
    }

    public ConsignmentPaymentVo getRejectingPayment() {
        return rejectingPayment;
    }

    public String getRejectPaymentReason() {
        return rejectPaymentReason;
    }

    public void setRejectPaymentReason(String rejectPaymentReason) {
        this.rejectPaymentReason = rejectPaymentReason;
    }

    // ------------------------------------------------------------------ view helpers

    public boolean isSupplier() {
        return detail != null && detail.getAgreement() != null && "SUPPLIER".equals(detail.getAgreement().getRole());
    }

    public boolean isCanSend() {
        return isSupplier() && "ACTIVE".equals(detail.getAgreement().getAgreementStatus());
    }

    public String shipmentStatusLabel(String s) {
        return sessionMb.text("cons.ship.status." + s);
    }

    public String shipmentStatusClass(String s) {
        if ("SENT".equals(s)) {
            return "badge badge-warning";
        }
        if ("RECEIVED".equals(s)) {
            return "badge badge-success";
        }
        if ("RECEIVED_DIFF".equals(s)) {
            return "badge badge-info";
        }
        if ("REJECTED".equals(s)) {
            return "badge badge-danger";
        }
        return "badge badge-muted";
    }

    /** "→ Envío" when the pieces go to the store, "← Devolución" when they come back. */
    public String directionLabel(String d) {
        return sessionMb.text("cons.ship.dir." + d);
    }

    /** "yyyy-MM-dd HH:mm" from the backend to "dd/MM/yyyy HH:mm". */
    public String stamp(String s) {
        if (s == null || s.length() < 16) {
            return s;
        }
        return s.substring(8, 10) + "/" + s.substring(5, 7) + "/" + s.substring(0, 4) + " " + s.substring(11, 16);
    }

    public String variant(String size, String color) {
        String a = size == null ? "" : size.trim();
        String b = color == null ? "" : color.trim();
        return a.isEmpty() ? b : (b.isEmpty() ? a : a + " · " + b);
    }

    private Integer agreementId() {
        return detail != null && detail.getAgreement() != null ? detail.getAgreement().getId() : null;
    }

    public ConsignmentDetailVo getDetail() {
        return detail;
    }

    public String getProductSearch() {
        return productSearch;
    }

    public void setProductSearch(String productSearch) {
        this.productSearch = productSearch;
    }

    public List<ConsignmentProductVo> getProductResults() {
        return productResults;
    }

    public List<CartLine> getCart() {
        return cart;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ConsignmentShipmentVo getShipment() {
        return shipment;
    }

    public String getReceiveComment() {
        return receiveComment;
    }

    public void setReceiveComment(String receiveComment) {
        this.receiveComment = receiveComment;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
