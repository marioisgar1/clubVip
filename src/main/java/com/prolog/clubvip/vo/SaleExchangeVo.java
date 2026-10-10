package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload of an exchange: which lines of {@code originSaleId} come back and which products
 * the customer takes instead. The credit for the returned goods is the price actually
 * charged on the original sale; the new goods are charged at today's price.
 */
public class SaleExchangeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer originSaleId;

    /** Where the exchange is being made. Defaults to the original sale's location. */
    private Integer saleLocationId;

    /** Only needed when the customer has to pay a difference; defaults to the original one. */
    private Integer paymentMethodId;

    private List<SaleExchangeReturnItemVo> returnedItems = new ArrayList<>();

    private List<SaleItemVo> newItems = new ArrayList<>();

    public Integer getOriginSaleId() {
        return originSaleId;
    }

    public void setOriginSaleId(Integer originSaleId) {
        this.originSaleId = originSaleId;
    }

    public Integer getSaleLocationId() {
        return saleLocationId;
    }

    public void setSaleLocationId(Integer saleLocationId) {
        this.saleLocationId = saleLocationId;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public List<SaleExchangeReturnItemVo> getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(List<SaleExchangeReturnItemVo> returnedItems) {
        this.returnedItems = returnedItems;
    }

    public List<SaleItemVo> getNewItems() {
        return newItems;
    }

    public void setNewItems(List<SaleItemVo> newItems) {
        this.newItems = newItems;
    }
}
