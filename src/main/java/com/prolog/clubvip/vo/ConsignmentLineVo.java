package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Line of a shipment request (productId + quantity) or of a reception (itemId + quantity received).
 */
public class ConsignmentLineVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;

    private Integer itemId;

    private Integer quantity;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
