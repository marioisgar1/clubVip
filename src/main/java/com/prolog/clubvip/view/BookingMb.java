package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentItemVo;
import com.prolog.clubvip.vo.AppointmentRequestVo;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.AvailabilityVo;
import com.prolog.clubvip.vo.BusinessCustomerVo;
import com.prolog.clubvip.vo.CustomerVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.ServiceVo;
import com.prolog.clubvip.vo.TimeSlotVo;

import org.primefaces.PrimeFaces;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Counter booking dialog (/WEB-INF/includes/bookingDialog.xhtml): customer by phone (or quick
 * walk-in creation), service, employee (or anyone), date, real availability slots, reserved
 * products, pre-consultation notes and payment mode. Submits appointment/book as MANUAL.
 */
@Named("bookingMb")
@ViewScoped
public class BookingMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type SERVICE_LIST = new TypeToken<List<ServiceVo>>() { }.getType();
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();

    // customer
    private String phone;
    private String customerName;
    private Integer customerId;
    private boolean lookedUp;
    private boolean customerFound;

    // service / employee / date / slot
    private List<ServiceVo> services = new ArrayList<>();
    private Integer serviceId;
    private List<EmployeeVo> employees = new ArrayList<>();
    private Integer employeeId; // null = anyone
    private LocalDate date;
    private AvailabilityVo availability;
    private String slotKey; // "HH:mm|employeeId"

    // products
    private List<ProductVo> products = new ArrayList<>();
    private Map<Integer, Integer> quantities = new LinkedHashMap<>();

    private String preNotes;
    private String paymentMode = "AT_BUSINESS";

    // ------------------------------------------------------------------ open

    /** Empty booking form. */
    public void openNew() {
        reset();
        loadCatalogs();
    }

    /** Booking form with the date preselected (from the weekly agenda). */
    public void openForDate(LocalDate day) {
        openNew();
        if (day != null && !day.isBefore(today())) {
            date = day;
        }
    }

    /** Booking form prefilled with a known customer (from the customer page). */
    public void openForCustomer(BusinessCustomerVo customer) {
        openNew();
        if (customer != null) {
            customerId = customer.getCustomerId();
            customerName = customer.getFullName();
            phone = customer.getPhone();
            lookedUp = true;
            customerFound = true;
        }
    }

    private void reset() {
        phone = null;
        customerName = null;
        customerId = null;
        lookedUp = false;
        customerFound = false;
        serviceId = null;
        employees = new ArrayList<>();
        employeeId = null;
        date = today();
        availability = null;
        slotKey = null;
        quantities = new LinkedHashMap<>();
        preNotes = null;
        paymentMode = "AT_BUSINESS";
    }

    private void loadCatalogs() {
        try {
            List<ServiceVo> list = RestClient.get("service/getServices", SERVICE_LIST, userId(), locale());
            services = new ArrayList<>();
            if (list != null) {
                for (ServiceVo s : list) {
                    if (!Boolean.FALSE.equals(s.getActiveForBooking())) {
                        services.add(s);
                    }
                }
            }
        } catch (RestException e) {
            services = new ArrayList<>();
            errors(e);
        }
        try {
            List<ProductVo> list = RestClient.get("product/getProductsByBusiness/" + businessId() + "?includePhotos=false", PRODUCT_LIST,
                    userId(), locale());
            products = list != null ? list : new ArrayList<>();
            for (ProductVo p : products) {
                quantities.put(p.getId(), 0);
            }
        } catch (RestException e) {
            products = new ArrayList<>();
            errors(e);
        }
    }

    // ------------------------------------------------------------------ customer

    /** GET customer/getCustomerByPhone/{phone}: 204 means a walk-in to be created with the typed name. */
    public void lookupCustomer() {
        customerId = null;
        customerFound = false;
        lookedUp = false;
        if (phone == null || phone.trim().isEmpty()) {
            return;
        }
        try {
            CustomerVo c = RestClient.get("customer/getCustomerByPhone/" + RestClient.encode(phone.trim()),
                    CustomerVo.class, userId(), locale());
            lookedUp = true;
            if (c != null && c.getId() != null) {
                customerId = c.getId();
                customerName = c.getFullName();
                customerFound = true;
            } else {
                customerName = null;
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ service / employee / slots

    public void onServiceChange() {
        employeeId = null;
        employees = new ArrayList<>();
        availability = null;
        slotKey = null;
        if (serviceId == null) {
            return;
        }
        try {
            List<EmployeeVo> list = RestClient.get("employee/getEmployeesByBusiness/" + businessId()
                    + "?serviceId=" + serviceId, EMPLOYEE_LIST, userId(), locale());
            employees = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        loadAvailability();
    }

    public void loadAvailability() {
        availability = null;
        slotKey = null;
        if (serviceId == null || date == null) {
            return;
        }
        try {
            StringBuilder path = new StringBuilder("appointment/getAvailability?businessId=").append(businessId())
                    .append("&serviceId=").append(serviceId).append("&date=").append(date);
            if (employeeId != null) {
                path.append("&employeeId=").append(employeeId);
            }
            availability = RestClient.get(path.toString(), AvailabilityVo.class, userId(), locale());
        } catch (RestException e) {
            errors(e);
        }
    }

    public List<TimeSlotVo> getSlots() {
        return availability != null && availability.getSlots() != null ? availability.getSlots() : new ArrayList<>();
    }

    public String slotKey(TimeSlotVo slot) {
        return slot.getStartTime() + "|" + (slot.getEmployeeId() != null ? slot.getEmployeeId() : "");
    }

    public ServiceVo getSelectedService() {
        if (serviceId == null) {
            return null;
        }
        for (ServiceVo s : services) {
            if (serviceId.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    public void plus(ProductVo p) {
        quantities.put(p.getId(), nz(quantities.get(p.getId())) + 1);
    }

    public void minus(ProductVo p) {
        int q = nz(quantities.get(p.getId())) - 1;
        quantities.put(p.getId(), Math.max(0, q));
    }

    public int qty(ProductVo p) {
        return nz(quantities.get(p.getId()));
    }

    public int getReservedCount() {
        int n = 0;
        for (Integer q : quantities.values()) {
            n += nz(q);
        }
        return n;
    }

    // ------------------------------------------------------------------ submit

    /** POST appointment/book. */
    public void book(Refreshable owner) {
        if (customerId == null && (phone == null || phone.isBlank() || customerName == null || customerName.isBlank())) {
            errorKey("op.msg.selectCustomer");
            return;
        }
        if (serviceId == null) {
            errorKey("op.msg.selectService");
            return;
        }
        if (slotKey == null || slotKey.isBlank()) {
            errorKey("op.msg.selectSlot");
            return;
        }
        String[] parts = slotKey.split("\\|", -1);
        AppointmentRequestVo req = new AppointmentRequestVo();
        req.setBusinessId(businessId());
        req.setServiceId(serviceId);
        req.setEmployeeId(parts.length > 1 && !parts[1].isEmpty() ? Integer.valueOf(parts[1]) : employeeId);
        req.setDate(date.toString());
        req.setStartTime(parts[0]);
        req.setPreNotes(preNotes);
        req.setPaymentMode(paymentMode);
        if (customerId != null) {
            req.setCustomerId(customerId);
        } else {
            req.setCustomerPhone(phone.trim());
            req.setCustomerName(customerName.trim());
        }
        List<AppointmentItemVo> items = new ArrayList<>();
        for (ProductVo p : products) {
            int q = nz(quantities.get(p.getId()));
            if (q > 0) {
                AppointmentItemVo it = new AppointmentItemVo();
                it.setProductId(p.getId());
                it.setProductName(p.getName());
                it.setQuantity(q);
                it.setUnitPrice(p.getPrice());
                items.add(it);
            }
        }
        req.setItems(items);
        try {
            RestClient.post("appointment/book", req, AppointmentVo.class, userId(), locale());
            info("op.msg.booked");
            if (owner != null) {
                owner.refresh();
            }
            PrimeFaces.current().executeScript("PF('bookingDlg').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getMinDate() {
        return today();
    }

    public LocalDate getMaxDate() {
        return today().plusDays(30);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public boolean isLookedUp() {
        return lookedUp;
    }

    public boolean isCustomerFound() {
        return customerFound;
    }

    public List<ServiceVo> getServices() {
        return services;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public List<EmployeeVo> getEmployees() {
        return employees;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AvailabilityVo getAvailability() {
        return availability;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }

    public List<ProductVo> getProducts() {
        return products;
    }

    public String getPreNotes() {
        return preNotes;
    }

    public void setPreNotes(String preNotes) {
        this.preNotes = preNotes;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}
