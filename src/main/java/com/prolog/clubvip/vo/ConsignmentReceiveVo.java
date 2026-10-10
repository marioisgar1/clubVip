package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * What actually arrived (consignment/receive/{shipmentId}); a missing line counts as fully received.
 */
public class ConsignmentReceiveVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ConsignmentLineVo> items = new ArrayList<>();

    private String comment;

    public List<ConsignmentLineVo> getItems() {
        return items;
    }

    public void setItems(List<ConsignmentLineVo> items) {
        this.items = items;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
