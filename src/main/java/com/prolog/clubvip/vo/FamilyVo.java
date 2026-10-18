package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Product family / category of the caller's business.
 */
public class FamilyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private Integer status;


    /** Name of each of the 4 product characteristics of this category and whether it is used. */
    private String attribute1Label;
    private Boolean attribute1Enabled;
    private String attribute2Label;
    private Boolean attribute2Enabled;
    private String attribute3Label;
    private Boolean attribute3Enabled;
    private String attribute4Label;
    private Boolean attribute4Enabled;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAttribute1Label() {
        return attribute1Label;
    }

    public void setAttribute1Label(String attribute1Label) {
        this.attribute1Label = attribute1Label;
    }

    public Boolean getAttribute1Enabled() {
        return attribute1Enabled;
    }

    public void setAttribute1Enabled(Boolean attribute1Enabled) {
        this.attribute1Enabled = attribute1Enabled;
    }

    public String getAttribute2Label() {
        return attribute2Label;
    }

    public void setAttribute2Label(String attribute2Label) {
        this.attribute2Label = attribute2Label;
    }

    public Boolean getAttribute2Enabled() {
        return attribute2Enabled;
    }

    public void setAttribute2Enabled(Boolean attribute2Enabled) {
        this.attribute2Enabled = attribute2Enabled;
    }

    public String getAttribute3Label() {
        return attribute3Label;
    }

    public void setAttribute3Label(String attribute3Label) {
        this.attribute3Label = attribute3Label;
    }

    public Boolean getAttribute3Enabled() {
        return attribute3Enabled;
    }

    public void setAttribute3Enabled(Boolean attribute3Enabled) {
        this.attribute3Enabled = attribute3Enabled;
    }

    public String getAttribute4Label() {
        return attribute4Label;
    }

    public void setAttribute4Label(String attribute4Label) {
        this.attribute4Label = attribute4Label;
    }

    public Boolean getAttribute4Enabled() {
        return attribute4Enabled;
    }

    public void setAttribute4Enabled(Boolean attribute4Enabled) {
        this.attribute4Enabled = attribute4Enabled;
    }
}
