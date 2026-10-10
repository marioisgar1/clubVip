package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessCustomerVo;
import com.prolog.clubvip.vo.CustomerFileVo;
import com.prolog.clubvip.vo.CustomerStatsVo;
import com.prolog.clubvip.vo.CustomerVo;
import com.prolog.clubvip.vo.MessageComposeVo;

import org.primefaces.PrimeFaces;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Customers of the business: KPIs, lists (all / inactive / birthdays / new), walk-in creation,
 * WhatsApp actions and the customer file (expediente).
 */
@Named("customerMb")
@ViewScoped
public class CustomerMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type CUSTOMER_LIST = new TypeToken<List<BusinessCustomerVo>>() { }.getType();

    private CustomerStatsVo stats = new CustomerStatsVo();
    private String search;
    private List<BusinessCustomerVo> customers = new ArrayList<>();
    private List<BusinessCustomerVo> inactive = new ArrayList<>();
    private List<BusinessCustomerVo> birthdays = new ArrayList<>();
    private List<BusinessCustomerVo> newCustomers = new ArrayList<>();
    private Integer month;
    private Integer day;
    private String promo;

    private CustomerVo newCustomer = new CustomerVo();
    private CustomerFileVo file;

    @PostConstruct
    public void init() {
        month = today().getMonthValue();
        day = today().getDayOfMonth();
        refresh();
    }

    @Override
    public void refresh() {
        try {
            CustomerStatsVo s = RestClient.get("report/customerStats", CustomerStatsVo.class, userId(), locale());
            stats = s != null ? s : new CustomerStatsVo();
        } catch (RestException e) {
            stats = new CustomerStatsVo();
            if (sessionMb.isAdmin()) {
                errors(e);
            }
        }
        searchCustomers();
        if (sessionMb.isAdmin()) {
            inactive = load("customer/getInactiveCustomers");
            newCustomers = load("customer/getNewCustomers");
            loadBirthdays();
        }
    }

    private List<BusinessCustomerVo> load(String path) {
        try {
            List<BusinessCustomerVo> list = RestClient.get(path, CUSTOMER_LIST, userId(), locale());
            return list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
            return new ArrayList<>();
        }
    }

    public void searchCustomers() {
        String q = search != null ? search.trim() : "";
        customers = load("customer/getCustomers?search=" + RestClient.encode(q));
    }

    public void loadBirthdays() {
        StringBuilder path = new StringBuilder("customer/getBirthdays?");
        if (month != null) {
            path.append("month=").append(month);
        }
        if (day != null) {
            path.append(month != null ? "&" : "").append("day=").append(day);
        }
        birthdays = load(path.toString());
    }

    // ------------------------------------------------------------------ whatsapp

    /** Compose a template for the customer, open wa.me and log it. BIRTHDAY carries the optional promo. */
    public void whatsapp(BusinessCustomerVo c, String templateType) {
        if (c == null || c.getCustomerId() == null) {
            return;
        }
        try {
            MessageComposeVo compose = WhatsappSender.compose(templateType, c.getCustomerId(), null,
                    "BIRTHDAY".equals(templateType) ? promo : null, userId(), locale());
            String link = compose != null && compose.getWaLink() != null ? compose.getWaLink() : c.getWaLink();
            if (link == null) {
                errorKey("op.msg.noPhoneCustomer");
                return;
            }
            openInNewTab(link);
            WhatsappSender.log(compose, null, userId(), locale());
            info("op.msg.messageSent");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ walk-in

    public void prepareNew() {
        newCustomer = new CustomerVo();
    }

    public void saveCustomer() {
        try {
            RestClient.post("customer/saveUpdateCustomer", newCustomer, CustomerVo.class, userId(), locale());
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('customerDlg').hide()");
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ file

    public void openFile(BusinessCustomerVo c) {
        file = null;
        if (c == null || c.getCustomerId() == null) {
            return;
        }
        try {
            file = RestClient.get("customer/getCustomer/" + c.getCustomerId(), CustomerFileVo.class, userId(), locale());
        } catch (RestException e) {
            errors(e);
        }
    }

    public String birthdayLabel(BusinessCustomerVo c) {
        if (c == null || c.getBirthDay() == null || c.getBirthMonth() == null) {
            return "-";
        }
        return String.format("%02d/%02d", c.getBirthDay(), c.getBirthMonth());
    }

    public String loyaltyLabel(BusinessCustomerVo c) {
        if (c == null || c.getLoyaltyRequired() == null || c.getLoyaltyRequired() == 0) {
            return "-";
        }
        return nz(c.getLoyaltyStamps()) + " / " + c.getLoyaltyRequired();
    }

    public int loyaltyPercent(BusinessCustomerVo c) {
        if (c == null || c.getLoyaltyRequired() == null || c.getLoyaltyRequired() == 0) {
            return 0;
        }
        return Math.min(100, nz(c.getLoyaltyStamps()) * 100 / c.getLoyaltyRequired());
    }

    public String stars(Integer rating) {
        int r = nz(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= r ? '★' : '☆');
        }
        return sb.toString();
    }

    /** Month names for f:selectItems (JSF maps: key = label, value = item value). */
    public Map<String, Integer> getMonths() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Month m : Month.values()) {
            String name = m.getDisplayName(TextStyle.FULL, locale());
            map.put(Character.toUpperCase(name.charAt(0)) + name.substring(1), m.getValue());
        }
        return map;
    }

    public List<Integer> getDays() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            list.add(i);
        }
        return list;
    }

    // ------------------------------------------------------------------ getters / setters

    public CustomerStatsVo getStats() {
        return stats;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<BusinessCustomerVo> getCustomers() {
        return customers;
    }

    public List<BusinessCustomerVo> getInactive() {
        return inactive;
    }

    public List<BusinessCustomerVo> getBirthdays() {
        return birthdays;
    }

    public List<BusinessCustomerVo> getNewCustomers() {
        return newCustomers;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public CustomerVo getNewCustomer() {
        return newCustomer;
    }

    public CustomerFileVo getFile() {
        return file;
    }
}
