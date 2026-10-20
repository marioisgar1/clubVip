package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Vehicle with GPS (backend gpsVehicle/*); copy of the backend VO. */
public class GpsVehicleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String plates;
    private String imei;
    private Integer brandId;
    private String brandName;
    private Integer modelId;
    private String modelName;
    private Integer modelYear;
    private Integer ownerUserId;
    private Integer profileId;
    private String profileName;
    private String ownerName;
    private String ownerEmail;
    private Integer ownerBusinessId;
    private String ownerBusinessName;
    private Boolean hasPhoto;
    private String photoBase64;
    private Boolean removePhoto;
    private Integer status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPlates() { return plates; }
    public void setPlates(String plates) { this.plates = plates; }
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public Integer getBrandId() { return brandId; }
    public void setBrandId(Integer brandId) { this.brandId = brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public Integer getModelId() { return modelId; }
    public void setModelId(Integer modelId) { this.modelId = modelId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    /** "Nissan NP300 2022" for the list; empty when none is set. */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        for (Object part : new Object[] {brandName, modelName, modelYear}) {
            if (part != null && !String.valueOf(part).isBlank()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(part);
            }
        }
        return sb.toString();
    }

    public Integer getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Integer ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public Integer getOwnerBusinessId() { return ownerBusinessId; }
    public void setOwnerBusinessId(Integer ownerBusinessId) { this.ownerBusinessId = ownerBusinessId; }
    public String getOwnerBusinessName() { return ownerBusinessName; }
    public void setOwnerBusinessName(String ownerBusinessName) { this.ownerBusinessName = ownerBusinessName; }
    public Boolean getHasPhoto() { return hasPhoto; }
    public void setHasPhoto(Boolean hasPhoto) { this.hasPhoto = hasPhoto; }
    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    public Boolean getRemovePhoto() { return removePhoto; }
    public void setRemovePhoto(Boolean removePhoto) { this.removePhoto = removePhoto; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
}
