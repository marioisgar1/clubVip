package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One line of the profit ledger (report/salesProfit, {@code movements}): a ticket, a charged visit, a
 * running expense, a venue expense or the salary accrued by an employee, with what it added to or
 * took from the profit. {@code net} is signed: income minus every cost of the line.
 */
public class ProfitMovementVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** When it happened; for date-only lines (expenses, salaries) the start of that day. */
    private Long dateMillis;
    /** False for date-only lines, which carry no meaningful hour. */
    private boolean timed;
    /** SALE, EXCHANGE, VISIT, EXPENSE, VENUE or SALARY. */
    private String type;
    /** Expense category code (RENT, ELECTRICITY, ...) on EXPENSE lines. */
    private String category;
    /** Ticket folio, CITA-{visit}, or null. */
    private String reference;
    /** What it was: services charged, location, expense description, ... */
    private String concept;
    /** Seller, employee who attended, or the employee the salary belongs to. */
    private String person;
    private String customer;
    private String paymentMethod;
    /** Days charged on VENUE and SALARY lines. */
    private Integer days;
    /** Net income of the line (after discounts). */
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal cogs = BigDecimal.ZERO;
    /** Card / platform commission. */
    private BigDecimal fees = BigDecimal.ZERO;
    private BigDecimal staffCommission = BigDecimal.ZERO;
    /** Expense, venue cost or salary of the line. */
    private BigDecimal expense = BigDecimal.ZERO;
    private BigDecimal net = BigDecimal.ZERO;

    /** Consignación: comisión de la tienda aliada */
    private BigDecimal consignmentCommission = BigDecimal.ZERO;

    public Long getDateMillis() { return dateMillis; }
    public void setDateMillis(Long dateMillis) { this.dateMillis = dateMillis; }
    public boolean isTimed() { return timed; }
    public void setTimed(boolean timed) { this.timed = timed; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }
    public String getPerson() { return person; }
    public void setPerson(String person) { this.person = person; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getCogs() { return cogs; }
    public void setCogs(BigDecimal cogs) { this.cogs = cogs; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public BigDecimal getStaffCommission() { return staffCommission; }
    public void setStaffCommission(BigDecimal staffCommission) { this.staffCommission = staffCommission; }
    public BigDecimal getExpense() { return expense; }
    public void setExpense(BigDecimal expense) { this.expense = expense; }
    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }

    public BigDecimal getConsignmentCommission() {
        return consignmentCommission;
    }

    public void setConsignmentCommission(BigDecimal consignmentCommission) {
        this.consignmentCommission = consignmentCommission;
    }
}
