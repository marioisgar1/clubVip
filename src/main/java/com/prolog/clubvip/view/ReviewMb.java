package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.RatingSummaryVo;
import com.prolog.clubvip.vo.ReplyVo;
import com.prolog.clubvip.vo.ReviewVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Customer reviews: rating summary, list (optionally only unanswered) and business replies.
 * Employees only see their own reviews (backend) and cannot reply.
 */
@Named("reviewMb")
@ViewScoped
public class ReviewMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type REVIEW_LIST = new TypeToken<List<ReviewVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private RatingSummaryVo summary = new RatingSummaryVo();
    private List<ReviewVo> reviews = new ArrayList<>();
    private List<ReviewVo> filtered;
    private boolean pendingOnly;
    private ReviewVo selected = new ReviewVo();
    private String replyText;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        if (isCanReply()) {
            try {
                RatingSummaryVo s = RestClient.get("review/getSummary", RatingSummaryVo.class, userId(), locale());
                summary = s != null ? s : new RatingSummaryVo();
            } catch (RestException e) {
                summary = new RatingSummaryVo();
                errors(e);
            }
        }
        loadReviews();
    }

    public void loadReviews() {
        try {
            String path = "review/getReviews" + (pendingOnly ? "?pending=true" : "");
            List<ReviewVo> list = RestClient.get(path, REVIEW_LIST, userId(), locale());
            reviews = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            reviews = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareReply(ReviewVo review) {
        selected = review;
        replyText = review.getReply();
    }

    public void sendReply() {
        if (selected == null || selected.getId() == null) {
            return;
        }
        ReplyVo payload = new ReplyVo();
        payload.setReply(replyText);
        RestResponse r = RestClient.post("review/reply/" + selected.getId(), payload, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.review.replied"));
            PrimeFaces.current().executeScript("PF('replyDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    /** Only business administrators (and operators) may reply. */
    public boolean isCanReply() {
        return !sessionMb.isEmployee();
    }

    // ------------------------------------------------------------------ presentation helpers

    public String stars(Integer rating) {
        int full = rating != null ? rating : 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? '★' : '☆');
        }
        return sb.toString();
    }

    public String getAverageStars() {
        BigDecimal avg = summary.getAverage() != null ? summary.getAverage() : BigDecimal.ZERO;
        return stars(avg.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    public BigDecimal getAverage() {
        return summary.getAverage() != null ? summary.getAverage() : BigDecimal.ZERO;
    }

    public int getCount() {
        return summary.getCount() != null ? summary.getCount() : 0;
    }

    public int getPendingReplies() {
        return summary.getPendingReplies() != null ? summary.getPendingReplies() : 0;
    }

    public int starCount(int stars) {
        Integer v;
        switch (stars) {
            case 5: v = summary.getFiveStars(); break;
            case 4: v = summary.getFourStars(); break;
            case 3: v = summary.getThreeStars(); break;
            case 2: v = summary.getTwoStars(); break;
            default: v = summary.getOneStar();
        }
        return v != null ? v : 0;
    }

    public int starPercent(int stars) {
        int total = getCount();
        return total > 0 ? starCount(stars) * 100 / total : 0;
    }

    public List<Integer> getStarLevels() {
        return List.of(5, 4, 3, 2, 1);
    }

    // ------------------------------------------------------------------ helpers

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            error(m);
        }
    }

    private void errors(RestResponse r) {
        for (String m : RestClient.extractErrors(r.getBody())) {
            error(m);
        }
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // ------------------------------------------------------------------ getters / setters

    public RatingSummaryVo getSummary() {
        return summary;
    }

    public List<ReviewVo> getReviews() {
        return reviews;
    }

    public List<ReviewVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<ReviewVo> filtered) {
        this.filtered = filtered;
    }

    public boolean isPendingOnly() {
        return pendingOnly;
    }

    public void setPendingOnly(boolean pendingOnly) {
        this.pendingOnly = pendingOnly;
    }

    public ReviewVo getSelected() {
        return selected;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
}
