package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pieces the supplier sends to the store (consignment/sendShipment/{agreementId}).
 */
public class ConsignmentShipmentRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ConsignmentLineVo> items = new ArrayList<>();

    private String notes;

    public List<ConsignmentLineVo> getItems() {
        return items;
    }

    public void setItems(List<ConsignmentLineVo> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
