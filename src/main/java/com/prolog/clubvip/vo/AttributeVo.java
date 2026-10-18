package com.prolog.clubvip.vo;

import java.io.Serializable;

/** One characteristic of a product already named by its category: "Talla: M", "Sabor: Naranja". */
public class AttributeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return label + ": " + value;
    }
}
