package com.prolog.clubvip.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.optionconfig.legend.Legend;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ProfitByVenueVo;
import com.prolog.clubvip.vo.ProfitDayVo;
import com.prolog.clubvip.vo.ProfitMovementVo;
import com.prolog.clubvip.vo.SalesProfitVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Real profit (REPORT_SALES_PROFIT) laid out as an income statement: income, cost of goods, variable
 * selling costs (card fees, staff commissions), operating expenses (salaries, venues, running
 * expenses) and the net result, with financial ratios, the ledger of every movement (date and time),
 * the result per day, the cost of each employee, the expenses and the breakdown by venue.
 * report/salesProfit?from=&to=
 */
@Named("reportProfitMb")
@ViewScoped
public class ReportProfitMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter STAMP_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter WEEKDAY_FMT = DateTimeFormatter.ofPattern("EEE dd/MM");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** One line of the income statement. kind: section, item, less, subtotal, total. */
    public static class StatementLine implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String label;
        private final BigDecimal amount;
        private final BigDecimal percent;
        private final String kind;
        private final String hint;

        StatementLine(String label, BigDecimal amount, BigDecimal percent, String kind, String hint) {
            this.label = label;
            this.amount = amount;
            this.percent = percent;
            this.kind = kind;
            this.hint = hint;
        }

        public String getLabel() { return label; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getPercent() { return percent; }
        public String getKind() { return kind; }
        public String getHint() { return hint; }
        public boolean isNegative() { return amount != null && amount.signum() < 0; }
    }

    /** One bar of "where each peso went": share of the income. */
    public static class ShareBar implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String label;
        private final BigDecimal amount;
        private final BigDecimal percent;
        private final String styleClass;

        ShareBar(String label, BigDecimal amount, BigDecimal percent, String styleClass) {
            this.label = label;
            this.amount = amount;
            this.percent = percent;
            this.styleClass = styleClass;
        }

        public String getLabel() { return label; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getPercent() { return percent; }
        public String getStyleClass() { return styleClass; }
        /** Bar width, 0..100. */
        public String getWidth() {
            BigDecimal w = percent == null ? BigDecimal.ZERO : percent.max(BigDecimal.ZERO).min(HUNDRED);
            return w.setScale(1, RoundingMode.HALF_UP).toPlainString();
        }
    }

    private LocalDate from;
    private LocalDate to;
    private String preset = "month";
    private SalesProfitVo report;
    private BarChartModel venueChart;
    private BarChartModel dayChart;
    private List<StatementLine> statement = new ArrayList<>();
    private List<ShareBar> shares = new ArrayList<>();
    private String movementType = "";
    private List<ProfitMovementVo> filteredMovements;

    @PostConstruct
    public void init() {
        applyPreset("month");
    }

    /** Quick ranges: today, week (Mon..today), month, lastMonth, year. */
    public void applyPreset(String code) {
        LocalDate today = today();
        preset = code;
        switch (code) {
            case "today":
                from = today;
                to = today;
                break;
            case "week":
                from = today.with(DayOfWeek.MONDAY);
                to = today;
                break;
            case "lastMonth":
                from = today.minusMonths(1).withDayOfMonth(1);
                to = from.withDayOfMonth(from.lengthOfMonth());
                break;
            case "year":
                from = today.withDayOfYear(1);
                to = today;
                break;
            default:
                from = today.withDayOfMonth(1);
                to = today;
                break;
        }
        load();
    }

    /** The "Generar" button: a hand-picked range. */
    public void generate() {
        preset = "";
        load();
    }

    private void load() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        try {
            report = RestClient.get("report/salesProfit?from=" + (from != null ? from.format(FMT) : "")
                    + "&to=" + (to != null ? to.format(FMT) : ""), SalesProfitVo.class, userId(), locale());
        } catch (RestException e) {
            report = null;
            errors(e);
        }
        filteredMovements = null;
        buildStatement();
        buildShares();
        buildVenueChart();
        buildDayChart();
    }

    // ------------------------------------------------------------------ income statement

    private void buildStatement() {
        statement = new ArrayList<>();
        if (report == null) {
            return;
        }
        SalesProfitVo r = report;
        BigDecimal posGross = nz(r.getPosRevenue()).add(nz(r.getDiscountsGiven()));
        BigDecimal servicesGross = nz(r.getVisitServices()).add(nz(r.getVisitDiscounts()));
        BigDecimal discounts = nz(r.getDiscountsGiven()).add(nz(r.getVisitDiscounts()));

        section("profit.st.income");
        item("profit.st.posSales", posGross, null, true);
        item("profit.st.services", servicesGross, null, r.getVisitsCount() > 0);
        item("profit.st.visitProducts", r.getVisitProducts(), null, nz(r.getVisitProducts()).signum() != 0);
        item("profit.st.consignment", r.getConsignmentRevenue(), "profit.st.consignment.hint", r.getConsignmentCount() > 0);
        if (discounts.signum() != 0) {
            line("profit.st.grossSales", r.getGrossSales(), "subtotal-soft", null);
            less("profit.st.discounts", discounts, "profit.st.discounts.hint");
        }
        line("profit.st.netIncome", r.getRevenue(), "subtotal", null);

        section("profit.st.costOfSales");
        less("profit.st.cogs", r.getCogs(), "profit.st.cogs.hint");
        line("profit.st.grossProfit", r.getGrossProfit(), "subtotal", null);

        section("profit.st.variable");
        less("profit.st.cardFees", r.getCardCommissions(), "profit.st.cardFees.hint");
        less("profit.st.consignmentFees", r.getConsignmentCommissions(), "profit.st.consignmentFees.hint");
        less("profit.st.staffServices", r.getServiceStaffCommissions(), null);
        less("profit.st.staffSales", r.getSaleStaffCommissions(), null);
        line("profit.st.contribution", r.getContributionMargin(), "subtotal", "profit.st.contribution.hint");

        section("profit.st.operating");
        less("profit.st.salaries", r.getSalaries(), "profit.st.salaries.hint");
        less("profit.st.venues", r.getVenueCost(), "profit.st.venues.hint");
        Map<String, BigDecimal> byCategory = r.getBusinessExpensesByCategory();
        if (byCategory != null) {
            for (Map.Entry<String, BigDecimal> e : byCategory.entrySet()) {
                if (e.getValue() != null && e.getValue().signum() != 0) {
                    statement.add(new StatementLine(categoryLabel(e.getKey()), e.getValue().negate(),
                            percentOf(e.getValue().negate()), "less", null));
                }
            }
        }
        line("profit.st.totalOperating", nz(r.getOperatingExpenses()).negate(), "subtotal-soft", null);

        line("profit.st.netProfit", r.getRealProfit(), "total", "profit.st.netProfit.hint");
    }

    private void section(String key) {
        statement.add(new StatementLine(sessionMb.text(key), null, null, "section", null));
    }

    private void item(String key, BigDecimal amount, String hintKey, boolean show) {
        if (show) {
            statement.add(new StatementLine(sessionMb.text(key), nz(amount), percentOf(amount), "item",
                    hintKey != null ? sessionMb.text(hintKey) : null));
        }
    }

    /** A cost: shown in parentheses as a negative; skipped when zero to keep the statement short. */
    private void less(String key, BigDecimal amount, String hintKey) {
        if (amount == null || amount.signum() == 0) {
            return;
        }
        statement.add(new StatementLine(sessionMb.text(key), amount.negate(), percentOf(amount.negate()), "less",
                hintKey != null ? sessionMb.text(hintKey) : null));
    }

    private void line(String key, BigDecimal amount, String kind, String hintKey) {
        statement.add(new StatementLine(sessionMb.text(key), nz(amount), percentOf(amount), kind,
                hintKey != null ? sessionMb.text(hintKey) : null));
    }

    private void buildShares() {
        shares = new ArrayList<>();
        if (report == null || nz(report.getRevenue()).signum() <= 0) {
            return;
        }
        share("profit.share.cogs", report.getCogs(), "s-cogs");
        share("profit.share.fees", report.getCardCommissions(), "s-fees");
        share("profit.share.consignment", report.getConsignmentCommissions(), "s-cons");
        share("profit.share.staff", report.getStaffCommissions(), "s-staff");
        share("profit.share.salaries", report.getSalaries(), "s-salary");
        share("profit.share.venues", report.getVenueCost(), "s-venue");
        share("profit.share.expenses", report.getBusinessExpenses(), "s-exp");
        BigDecimal left = nz(report.getRealProfit());
        shares.add(new ShareBar(sessionMb.text(left.signum() >= 0 ? "profit.share.profit" : "profit.share.loss"),
                left, percentOf(left.abs()), left.signum() >= 0 ? "s-profit" : "s-loss"));
    }

    private void share(String key, BigDecimal amount, String css) {
        if (amount != null && amount.signum() != 0) {
            shares.add(new ShareBar(sessionMb.text(key), amount, percentOf(amount), css));
        }
    }

    // ------------------------------------------------------------------ ratios

    /** amount / revenue x 100, one decimal; null without income. */
    public BigDecimal percentOf(BigDecimal amount) {
        if (report == null || amount == null || nz(report.getRevenue()).signum() == 0) {
            return null;
        }
        return amount.multiply(HUNDRED).divide(report.getRevenue(), 1, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrossMargin() {
        return report != null ? percentOf(report.getGrossProfit()) : null;
    }

    public BigDecimal getContributionPercent() {
        return report != null ? percentOf(report.getContributionMargin()) : null;
    }

    public BigDecimal getNetMargin() {
        return report != null ? percentOf(report.getRealProfit()) : null;
    }

    /** Kept for the old KPI: net profit before running expenses over revenue. */
    public BigDecimal getMarginPercent() {
        BigDecimal p = report != null ? percentOf(report.getNetProfit()) : null;
        return p != null ? p : BigDecimal.ZERO;
    }

    public BigDecimal getPayrollPercent() {
        return report != null ? percentOf(nz(report.getSalaries()).add(nz(report.getStaffCommissions()))) : null;
    }

    public BigDecimal getAverageTicket() {
        if (report == null || report.getSalesCount() == 0) {
            return null;
        }
        return nz(report.getRevenue()).divide(BigDecimal.valueOf(report.getSalesCount()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Income needed to cover the operating expenses at the current contribution margin:
     * fixed costs / (contribution / revenue). Null when the margin is not positive.
     */
    public BigDecimal getBreakEven() {
        if (report == null || nz(report.getOperatingExpenses()).signum() == 0) {
            return null;
        }
        if (nz(report.getRevenue()).signum() <= 0 || nz(report.getContributionMargin()).signum() <= 0) {
            return null;
        }
        return nz(report.getOperatingExpenses()).multiply(report.getRevenue())
                .divide(report.getContributionMargin(), 2, RoundingMode.HALF_UP);
    }

    /** How far the income is above (positive) or below the break-even point, in % of it. */
    public BigDecimal getBreakEvenCoverage() {
        BigDecimal be = getBreakEven();
        if (be == null || be.signum() == 0) {
            return null;
        }
        return report.getRevenue().subtract(be).multiply(HUNDRED).divide(be, 1, RoundingMode.HALF_UP);
    }

    /** No operating expenses in the range: any sale with a positive margin is already profit. */
    public boolean isNoFixedCosts() {
        return report != null && nz(report.getOperatingExpenses()).signum() == 0;
    }

    public BigDecimal getTotalPayroll() {
        return report == null ? BigDecimal.ZERO : nz(report.getSalaries()).add(nz(report.getStaffCommissions()));
    }

    public BigDecimal getTotalCosts() {
        return report == null ? BigDecimal.ZERO
                : nz(report.getRevenue()).subtract(nz(report.getRealProfit()));
    }

    public String getPeriodLabel() {
        if (from == null || to == null) {
            return "";
        }
        return from.equals(to) ? from.format(DAY_FMT) : from.format(DAY_FMT) + " – " + to.format(DAY_FMT);
    }

    // ------------------------------------------------------------------ ledger

    public List<ProfitMovementVo> getMovements() {
        if (report == null || report.getMovements() == null) {
            return new ArrayList<>();
        }
        if (movementType == null || movementType.isEmpty()) {
            return report.getMovements();
        }
        List<ProfitMovementVo> list = new ArrayList<>();
        for (ProfitMovementVo m : report.getMovements()) {
            boolean income = "SALE".equals(m.getType()) || "EXCHANGE".equals(m.getType()) || "VISIT".equals(m.getType())
                    || "CONSIGNMENT".equals(m.getType());
            if (movementType.equals(m.getType()) || ("INCOME".equals(movementType) && income)
                    || ("OUTCOME".equals(movementType) && !income)) {
                list.add(m);
            }
        }
        return list;
    }

    public void changeMovementType() {
        filteredMovements = null;
    }

    /** Sum of one column over the movements on screen: income, cogs, fees, staff, expense, net. */
    public BigDecimal movementTotal(String column) {
        BigDecimal sum = BigDecimal.ZERO;
        List<ProfitMovementVo> list = filteredMovements != null ? filteredMovements : getMovements();
        for (ProfitMovementVo m : list) {
            switch (column) {
                case "income": sum = sum.add(nz(m.getIncome())); break;
                case "cogs": sum = sum.add(nz(m.getCogs())); break;
                case "fees": sum = sum.add(nz(m.getFees())); break;
                case "staff": sum = sum.add(nz(m.getStaffCommission())); break;
                case "cons": sum = sum.add(nz(m.getConsignmentCommission())); break;
                case "expense": sum = sum.add(nz(m.getExpense())); break;
                default: sum = sum.add(nz(m.getNet())); break;
            }
        }
        return sum;
    }

    public String stamp(ProfitMovementVo m) {
        if (m == null || m.getDateMillis() == null) {
            return "";
        }
        return (m.isTimed() ? STAMP_FMT : DAY_FMT).format(zoned(m.getDateMillis()));
    }

    public String dayLabel(Long millis) {
        return millis != null ? DAY_FMT.format(zoned(millis)) : "";
    }

    public String weekdayLabel(Long millis) {
        return millis != null ? WEEKDAY_FMT.withLocale(locale() != null ? locale() : Locale.getDefault())
                .format(zoned(millis)) : "";
    }

    public String typeLabel(String type) {
        return type != null ? sessionMb.text("profit.type." + type) : "";
    }

    public String typeIcon(String type) {
        if (type == null) {
            return "pi pi-circle";
        }
        switch (type) {
            case "SALE": return "pi pi-shopping-cart";
            case "EXCHANGE": return "pi pi-sync";
            case "VISIT": return "pi pi-calendar";
            case "CONSIGNMENT": return "pi pi-box";
            case "VENUE": return "pi pi-map-marker";
            case "SALARY": return "pi pi-id-card";
            default: return "pi pi-wallet";
        }
    }

    /** What the movement was, in words, for the "Concepto" column. */
    public String concept(ProfitMovementVo m) {
        if (m == null) {
            return "";
        }
        switch (m.getType() != null ? m.getType() : "") {
            case "EXPENSE": {
                String cat = categoryLabel(m.getCategory());
                return m.getConcept() != null && !m.getConcept().isBlank() ? cat + " · " + m.getConcept() : cat;
            }
            case "VENUE": {
                List<String> parts = new ArrayList<>();
                if (m.getConcept() != null && !m.getConcept().isBlank()) parts.add(m.getConcept());
                if (m.getReference() != null) parts.add(m.getReference());
                parts.add(daysText(m.getDays()));
                return String.join(" · ", parts);
            }
            case "SALARY":
                return sessionMb.text("profit.salaryAccrued") + " · " + daysText(m.getDays());
            default:
                return m.getConcept() != null ? m.getConcept() : "";
        }
    }

    private String daysText(Integer days) {
        int d = days != null ? days : 0;
        return d + " " + sessionMb.text(d == 1 ? "pos.report.day" : "pos.report.days");
    }

    public String categoryLabel(String code) {
        return code != null ? sessionMb.text("biz.expense.cat." + code) : "";
    }

    public String schemeLabel(String scheme) {
        return scheme != null ? sessionMb.text("rel.payScheme." + scheme) : "—";
    }

    /** Some of her products sold at partner stores in the range: show the consignment columns. */
    public boolean isHasConsignment() {
        return report != null && report.getConsignmentCount() > 0;
    }

    public boolean isHasPayroll() {
        return report != null && report.getPayroll() != null && !report.getPayroll().isEmpty();
    }

    public boolean isHasDays() {
        return report != null && report.getDays() != null && !report.getDays().isEmpty();
    }

    public boolean isHasExpenses() {
        return report != null && (nz(report.getBusinessExpenses()).signum() != 0 || nz(report.getVenueCost()).signum() != 0);
    }

    public List<ProfitMovementVo> getExpenseMovements() {
        List<ProfitMovementVo> list = new ArrayList<>();
        if (report != null && report.getMovements() != null) {
            for (ProfitMovementVo m : report.getMovements()) {
                if ("EXPENSE".equals(m.getType()) || "VENUE".equals(m.getType())) {
                    list.add(m);
                }
            }
        }
        return list;
    }

    /** Running-expense categories with money in the range, biggest first, with their share of all expenses. */
    public List<ShareBar> getExpenseCategories() {
        List<ShareBar> list = new ArrayList<>();
        if (report == null) {
            return list;
        }
        BigDecimal total = nz(report.getBusinessExpenses()).add(nz(report.getVenueCost()));
        if (total.signum() == 0) {
            return list;
        }
        if (nz(report.getVenueCost()).signum() != 0) {
            list.add(new ShareBar(sessionMb.text("profit.st.venues"), report.getVenueCost(),
                    report.getVenueCost().multiply(HUNDRED).divide(total, 1, RoundingMode.HALF_UP), "s-venue"));
        }
        if (report.getBusinessExpensesByCategory() != null) {
            for (Map.Entry<String, BigDecimal> e : report.getBusinessExpensesByCategory().entrySet()) {
                if (e.getValue() != null && e.getValue().signum() != 0) {
                    list.add(new ShareBar(categoryLabel(e.getKey()), e.getValue(),
                            e.getValue().multiply(HUNDRED).divide(total, 1, RoundingMode.HALF_UP), "s-exp"));
                }
            }
        }
        list.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return list;
    }

    // ------------------------------------------------------------------ charts

    /** Income bars per day with the net result as a line. */
    private void buildDayChart() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet income = new BarChartDataSet();
        income.setLabel(sessionMb.text("profit.col.income"));
        LineChartDataSet net = new LineChartDataSet();
        net.setLabel(sessionMb.text("profit.col.net"));
        List<Object> incomeValues = new ArrayList<>();
        List<Object> netValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (isHasDays()) {
            for (ProfitDayVo d : report.getDays()) {
                labels.add(weekdayLabel(d.getDayMillis()));
                incomeValues.add(nz(d.getIncome()));
                netValues.add(nz(d.getNet()));
            }
        }
        income.setData(incomeValues);
        income.setBackgroundColor("#CEF187");
        income.setBorderColor("#5B7720");
        income.setBorderWidth(1);
        net.setData(netValues);
        net.setBorderColor("#5B7720");
        net.setBackgroundColor("#5B7720");
        net.setFill(false);
        net.setTension(0.3);
        data.addChartDataSet(net);
        data.addChartDataSet(income);
        data.setLabels(labels);
        model.setData(data);
        model.setOptions(chartOptions());
        dayChart = model;
    }

    /** Revenue and net profit per venue, lime bars for revenue and deep green bars for profit. */
    private void buildVenueChart() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet revenue = new BarChartDataSet();
        revenue.setLabel(sessionMb.text("pos.report.revenue"));
        BarChartDataSet profit = new BarChartDataSet();
        profit.setLabel(sessionMb.text("pos.report.netProfit"));
        List<Object> revenueValues = new ArrayList<>();
        List<Object> profitValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (report != null && report.getVenues() != null) {
            for (ProfitByVenueVo v : report.getVenues()) {
                labels.add(venueTitle(v));
                revenueValues.add(v.getRevenue() != null ? v.getRevenue() : BigDecimal.ZERO);
                profitValues.add(v.getNetProfit() != null ? v.getNetProfit() : BigDecimal.ZERO);
            }
        }
        revenue.setData(revenueValues);
        revenue.setBackgroundColor("#CEF187");
        revenue.setBorderColor("#5B7720");
        revenue.setBorderWidth(1);
        profit.setData(profitValues);
        profit.setBackgroundColor("#5B7720");
        profit.setBorderColor("#4A6219");
        profit.setBorderWidth(1);
        data.addChartDataSet(revenue);
        data.addChartDataSet(profit);
        data.setLabels(labels);
        model.setData(data);
        model.setOptions(chartOptions());
        venueChart = model;
    }

    private BarChartOptions chartOptions() {
        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes y = new CartesianLinearAxes();
        y.setBeginAtZero(true);
        scales.addYAxesData(y);
        options.setScales(scales);
        Legend legend = new Legend();
        legend.setDisplay(true);
        options.setLegend(legend);
        options.setMaintainAspectRatio(false);
        return options;
    }

    public boolean isHasVenues() {
        return report != null && report.getVenues() != null && !report.getVenues().isEmpty();
    }

    /** Headline of a breakdown row: what she called the expense, or the plain location. */
    public String venueTitle(ProfitByVenueVo venue) {
        if (venue == null) {
            return "";
        }
        if (venue.getDescription() != null && !venue.getDescription().trim().isEmpty()) {
            return venue.getDescription().trim();
        }
        return venue.getLocation() != null && !venue.getLocation().trim().isEmpty()
                ? venue.getLocation() : sessionMb.text("pos.report.noLocation");
    }

    /** "Plaza del Valle · 13/03/2026 - 17/03/2026 · 5 days", trimmed of whatever is missing. */
    public String venueSubtitle(ProfitByVenueVo venue) {
        if (venue == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        boolean titled = venue.getDescription() != null && !venue.getDescription().trim().isEmpty();
        if (titled && venue.getLocation() != null && !venue.getLocation().trim().isEmpty()) {
            parts.add(venue.getLocation());
        }
        if (venue.getStartMillis() != null && venue.getEndMillis() != null) {
            parts.add(dayLabel(venue.getStartMillis()) + " - " + dayLabel(venue.getEndMillis()));
        }
        if (venue.getDays() != null && venue.getDays() > 0) {
            parts.add(daysText(venue.getDays()));
        }
        return String.join(" · ", parts);
    }

    private static java.time.ZonedDateTime zoned(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of(Constants.TIMEZONE));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
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

    public String getPreset() {
        return preset;
    }

    public SalesProfitVo getReport() {
        return report;
    }

    public BarChartModel getVenueChart() {
        return venueChart;
    }

    public BarChartModel getDayChart() {
        return dayChart;
    }

    public List<StatementLine> getStatement() {
        return statement;
    }

    public List<ShareBar> getShares() {
        return shares;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public List<ProfitMovementVo> getFilteredMovements() {
        return filteredMovements;
    }

    public void setFilteredMovements(List<ProfitMovementVo> filteredMovements) {
        this.filteredMovements = filteredMovements;
    }
}
