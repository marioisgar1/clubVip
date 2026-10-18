package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessCustomerVo;
import com.prolog.clubvip.vo.CustomerBehaviorVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Customer payment record (REPORT_BEHAVIOR): every customer of the business is listed on open
 * (nobody has to search blind without the phone at hand); the search narrows the list, pick her
 * and read how punctual she has been before granting more credit.
 * customer/getCustomers?search= · credit/getCustomerBehavior?customerId=
 */
@Named("reportBehaviorMb")
@ViewScoped
public class ReportBehaviorMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type CUSTOMER_LIST = new TypeToken<List<BusinessCustomerVo>>() { }.getType();
    private static final Gson GSON = new Gson();

    private String search;
    private List<BusinessCustomerVo> matches = new ArrayList<>();
    private boolean searched;
    private CustomerBehaviorVo selected;

    @PostConstruct
    public void init() {
        find();
    }

    /** Empty search lists every customer (customer/getCustomers without search). */
    public void find() {
        selected = null;
        searched = true;
        matches = new ArrayList<>();
        String text = search != null ? search.trim() : "";
        try {
            List<BusinessCustomerVo> list = RestClient.get(text.isEmpty() ? "customer/getCustomers"
                    : "customer/getCustomers?search=" + RestClient.encode(text), CUSTOMER_LIST, userId(), locale());
            matches = list != null ? list : new ArrayList<>();
            matches.sort(Comparator.comparing(c -> c.getFullName() != null ? c.getFullName() : "",
                    String.CASE_INSENSITIVE_ORDER));
            // A single match of a search is what the seller almost always wants: open it right away.
            if (!text.isEmpty() && matches.size() == 1) {
                select(matches.get(0));
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    /** GET credit/getCustomerBehavior?customerId=: accepts the object or a one-element list. */
    public void select(BusinessCustomerVo customer) {
        selected = null;
        if (customer == null || customer.getCustomerId() == null) {
            return;
        }
        try {
            JsonElement body = RestClient.get("credit/getCustomerBehavior?customerId=" + customer.getCustomerId(),
                    JsonElement.class, userId(), locale());
            if (body == null || body.isJsonNull()) {
                selected = emptyFor(customer);
            } else if (body.isJsonArray()) {
                selected = body.getAsJsonArray().size() > 0
                        ? GSON.fromJson(body.getAsJsonArray().get(0), CustomerBehaviorVo.class) : emptyFor(customer);
            } else {
                selected = GSON.fromJson(body, CustomerBehaviorVo.class);
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    private static CustomerBehaviorVo emptyFor(BusinessCustomerVo c) {
        CustomerBehaviorVo vo = new CustomerBehaviorVo();
        vo.setCustomerId(c.getCustomerId());
        vo.setCustomerName(c.getFullName());
        vo.setCustomerPhone(c.getPhone());
        return vo;
    }

    /** CSS chip for the rating, so the word carries a colour too. */
    public String ratingStyle(String rating) {
        String r = rating != null ? rating : CustomerBehaviorVo.RATING_NEW;
        switch (r) {
            case CustomerBehaviorVo.RATING_EXCELLENT:
                return "badge badge-success";
            case CustomerBehaviorVo.RATING_GOOD:
                return "badge badge-info";
            case CustomerBehaviorVo.RATING_FAIR:
                return "badge badge-warning";
            case CustomerBehaviorVo.RATING_RISKY:
                return "badge badge-danger";
            default:
                return "badge badge-muted";
        }
    }

    public String ratingLabel(String rating) {
        return sessionMb.text("pos.behavior.rating." + (rating != null ? rating : CustomerBehaviorVo.RATING_NEW));
    }

    // ------------------------------------------------------------------ getters / setters

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<BusinessCustomerVo> getMatches() {
        return matches;
    }

    public boolean isSearched() {
        return searched;
    }

    public CustomerBehaviorVo getSelected() {
        return selected;
    }
}
