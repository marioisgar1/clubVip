package com.prolog.clubvip.vo;

/**
 * Body of {@code POST /sale/registerSale}, as documented in ENDPOINTS.md:
 * {@code {paymentMethodId, saleLocationId, customerId?, items:[{productId, quantity, listPrice,
 * unitPrice}], credit?}}. Leonisa posts the very same JSON as a {@link SaleVo}, so this type is
 * that VO under the documented name: the read-only fields (folio, totals, status...) are simply
 * ignored on the way in.
 */
public class SaleRequestVo extends SaleVo {

    private static final long serialVersionUID = 1L;
}
