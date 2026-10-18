package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.LocationExpenseVo;
import com.prolog.clubvip.vo.SaleLocationVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Venue expenses: what a selling spell at a location cost (stand rent, fees) (EXPENSE_VIEW): locationExpense/getLocationExpenses · locationExpense/saveUpdateLocationExpense · locationExpense/deleteLocationExpenses/{id}
 */
@Named("locationExpenseMb")
@ViewScoped
public class LocationExpenseMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<LocationExpenseVo>>() { }.getType();
    private static final Type LOCATION_LIST_TYPE = new TypeToken<List<SaleLocationVo>>() { }.getType();

    private List<LocationExpenseVo> expenses = new ArrayList<>();
    private List<LocationExpenseVo> filtered;
    private LocationExpenseVo selected = new LocationExpenseVo();
    private List<SaleLocationVo> saleLocations = new ArrayList<>();
    /** Dialog dates (p:datePicker does not take a String); copied into the VO on save. */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private LocalDate startDate;
    private LocalDate endDate;

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<LocationExpenseVo> list = RestClient.get("locationExpense/getLocationExpenses", LIST_TYPE, userId(), locale());
            expenses = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            expenses = new ArrayList<>();
            errors(e);
        }
        try {
            List<SaleLocationVo> list = RestClient.get("saleLocation/getSaleLocations", LOCATION_LIST_TYPE, userId(), locale());
            saleLocations = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            saleLocations = new ArrayList<>();
        }
    }

    public void prepareNew() {
        selected = new LocationExpenseVo();
        startDate = today();
        endDate = today();
    }

    public void prepareEdit(LocationExpenseVo vo) {
        selected = new LocationExpenseVo();
        selected.setId(vo.getId());
        selected.setSaleLocationId(vo.getSaleLocationId());
        selected.setSaleLocationName(vo.getSaleLocationName());
        selected.setDescription(vo.getDescription());
        startDate = vo.getStartDate() != null ? LocalDate.parse(vo.getStartDate(), FMT) : today();
        endDate = vo.getEndDate() != null ? LocalDate.parse(vo.getEndDate(), FMT) : startDate;
        selected.setTotalCost(vo.getTotalCost());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        selected.setStartDate(startDate != null ? startDate.format(FMT) : null);
        selected.setEndDate(endDate != null ? endDate.format(FMT) : null);
        try {
            RestClient.post("locationExpense/saveUpdateLocationExpense", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('locationExpenseDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(LocationExpenseVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("locationExpense/deleteLocationExpenses/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            refresh();
            info("msg.deleted");
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<LocationExpenseVo> getExpenses() {
        return expenses;
    }

    public List<LocationExpenseVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<LocationExpenseVo> filtered) {
        this.filtered = filtered;
    }

    public LocationExpenseVo getSelected() {
        return selected;
    }

    public void setSelected(LocationExpenseVo selected) {
        this.selected = selected;
    }

    public List<SaleLocationVo> getSaleLocations() {
        return saleLocations;
    }
}
