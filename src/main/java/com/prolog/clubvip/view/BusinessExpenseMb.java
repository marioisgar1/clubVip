package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessExpenseVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * "Mis gastos": the running costs of the business (supplies, power, internet, water, rent,
 * purchases) for a date range; the profit report subtracts them (BUSINESS_EXPENSE_VIEW).
 * businessExpense/getBusinessExpenses?from=&to= · businessExpense/saveUpdateBusinessExpense · businessExpense/deleteBusinessExpenses/{id}
 */
@Named("businessExpenseMb")
@ViewScoped
public class BusinessExpenseMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<BusinessExpenseVo>>() { }.getType();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final List<String> CATEGORIES = List.of("SUPPLIES", "ELECTRICITY", "INTERNET", "WATER", "RENT", "PURCHASES", "OTHER");

    private LocalDate from;
    private LocalDate to;
    private List<BusinessExpenseVo> expenses = new ArrayList<>();
    private List<BusinessExpenseVo> filtered;
    private BusinessExpenseVo selected = new BusinessExpenseVo();
    /** Dialog date (p:datePicker does not take a String); copied into the VO on save. */
    private LocalDate expenseDate;

    @PostConstruct
    public void init() {
        from = today().withDayOfMonth(1);
        to = today();
        refresh();
    }

    @Override
    public void refresh() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        try {
            List<BusinessExpenseVo> list = RestClient.get("businessExpense/getBusinessExpenses?from="
                    + (from != null ? from.format(FMT) : "") + "&to=" + (to != null ? to.format(FMT) : ""),
                    LIST_TYPE, userId(), locale());
            expenses = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            expenses = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new BusinessExpenseVo();
        expenseDate = today();
        selected.setCategory("SUPPLIES");
    }

    public void prepareEdit(BusinessExpenseVo vo) {
        selected = new BusinessExpenseVo();
        selected.setId(vo.getId());
        expenseDate = vo.getExpenseDate() != null ? LocalDate.parse(vo.getExpenseDate(), FMT) : today();
        selected.setCategory(vo.getCategory());
        selected.setDescription(vo.getDescription());
        selected.setAmount(vo.getAmount());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        selected.setExpenseDate(expenseDate != null ? expenseDate.format(FMT) : null);
        try {
            RestClient.post("businessExpense/saveUpdateBusinessExpense", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('businessExpenseDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(BusinessExpenseVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("businessExpense/deleteBusinessExpenses/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            refresh();
            info("msg.deleted");
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    /** Sum of the listed expenses. */
    public BigDecimal getTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BusinessExpenseVo e : expenses) {
            if (e.getAmount() != null) {
                sum = sum.add(e.getAmount());
            }
        }
        return sum;
    }

    /** Category -> amount for the listed expenses, biggest first. */
    public List<Map.Entry<String, BigDecimal>> getByCategory() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (BusinessExpenseVo e : expenses) {
            if (e.getAmount() == null) {
                continue;
            }
            String c = e.getCategory() != null ? e.getCategory() : "OTHER";
            map.merge(c, e.getAmount(), BigDecimal::add);
        }
        List<Map.Entry<String, BigDecimal>> rows = new ArrayList<>(map.entrySet());
        rows.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return rows;
    }

    public String categoryLabel(String category) {
        return sessionMb.text("biz.expense.cat." + (category != null ? category : "OTHER"));
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public List<BusinessExpenseVo> getExpenses() {
        return expenses;
    }

    public List<BusinessExpenseVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<BusinessExpenseVo> filtered) {
        this.filtered = filtered;
    }

    public BusinessExpenseVo getSelected() {
        return selected;
    }

    public void setSelected(BusinessExpenseVo selected) {
        this.selected = selected;
    }

    public List<String> getCategories() {
        return CATEGORIES;
    }
}
