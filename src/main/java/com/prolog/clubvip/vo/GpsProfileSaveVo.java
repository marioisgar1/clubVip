package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Result of saving a profile / vehicle: how many vehicles the GPS service took (failed = it did not answer). */
public class GpsProfileSaveVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer profileId;
    private Integer vehicles;
    private Integer sent;
    private Integer failed;

    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }
    public Integer getVehicles() { return vehicles; }
    public void setVehicles(Integer vehicles) { this.vehicles = vehicles; }
    public Integer getSent() { return sent; }
    public void setSent(Integer sent) { this.sent = sent; }
    public Integer getFailed() { return failed; }
    public void setFailed(Integer failed) { this.failed = failed; }
}
