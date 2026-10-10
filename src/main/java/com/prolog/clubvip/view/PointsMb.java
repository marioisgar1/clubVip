package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.MemberLookupVo;
import com.prolog.clubvip.vo.PointsAwardVo;
import com.prolog.clubvip.vo.PointsMovementVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Rewards Club VIP at the business (pages/points.xhtml): read a member card, give points for a
 * purchase amount (services, sales outside the point of sale) and review the movements.
 * points/lookupMember/{n} · points/award · points/getMovements
 */
@Named("pointsMb")
@ViewScoped
public class PointsMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type MOVEMENT_LIST = new TypeToken<List<PointsMovementVo>>() { }.getType();

    private String memberNumber;
    private MemberLookupVo member;
    private BigDecimal amount;
    private String note;
    private List<PointsMovementVo> movements = new ArrayList<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<PointsMovementVo> list = RestClient.get("points/getMovements", MOVEMENT_LIST, userId(), locale());
            movements = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void lookup() {
        if (memberNumber == null || memberNumber.isBlank()) {
            return;
        }
        try {
            member = RestClient.get("points/lookupMember/" + RestClient.encode(memberNumber.trim().toUpperCase()),
                    MemberLookupVo.class, userId(), locale());
        } catch (RestException e) {
            member = null;
            errors(e);
        }
    }

    public void clear() {
        member = null;
        memberNumber = null;
        amount = null;
        note = null;
    }

    public int getPreview() {
        if (member == null || amount == null || member.getPesosPerPoint() == null || member.getPesosPerPoint().signum() <= 0) {
            return 0;
        }
        return amount.divide(member.getPesosPerPoint(), 0, java.math.RoundingMode.FLOOR).intValue();
    }

    public void award() {
        if (member == null) {
            return;
        }
        PointsAwardVo vo = new PointsAwardVo();
        vo.setMemberNumber(member.getMemberNumber());
        vo.setAmount(amount);
        vo.setNote(note);
        try {
            PointsMovementVo m = RestClient.post("points/award", vo, PointsMovementVo.class, userId(), locale());
            addMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, sessionMb.text("rw.award.done")
                    .replace("{points}", String.valueOf(m.getPoints())).replace("{name}", m.getCustomerName())
                    .replace("{balance}", String.valueOf(m.getBalance())));
            clear();
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    public String typeLabel(String type) {
        return sessionMb.text("rw.type." + type);
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public MemberLookupVo getMember() {
        return member;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<PointsMovementVo> getMovements() {
        return movements;
    }
}
