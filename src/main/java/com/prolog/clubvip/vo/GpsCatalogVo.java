package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Brand or model of the GPS vehicles (backend gpsVehicle/getBrands, getModels); brandId only on a model. */
public class GpsCatalogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer brandId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getBrandId() { return brandId; }
    public void setBrandId(Integer brandId) { this.brandId = brandId; }
}
