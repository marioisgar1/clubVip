package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BrandVo;
import com.prolog.clubvip.vo.FamilyVo;
import com.prolog.clubvip.vo.ProductReportVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Most consulted products (price lookups) (REPORT_MOST_CONSULTED): report/mostConsulted?from=&to=&familyId=&brandId=
 */
@Named("reportConsultedMb")
@ViewScoped
public class ReportConsultedMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Type LIST_TYPE = new TypeToken<List<ProductReportVo>>() { }.getType();
    private static final Type FAMILY_TYPE = new TypeToken<List<FamilyVo>>() { }.getType();
    private static final Type BRAND_TYPE = new TypeToken<List<BrandVo>>() { }.getType();

    private LocalDate from;
    private LocalDate to;
    private Integer familyId;
    private Integer brandId;

    private List<FamilyVo> families = new ArrayList<>();
    private List<BrandVo> brands = new ArrayList<>();
    private List<ProductReportVo> rows = new ArrayList<>();

    @PostConstruct
    public void init() {
        from = today().withDayOfMonth(1);
        to = today();
        try {
            List<FamilyVo> f = RestClient.get("family/getFamilies", FAMILY_TYPE, userId(), locale());
            families = f != null ? f : new ArrayList<>();
            List<BrandVo> b = RestClient.get("brand/getBrands", BRAND_TYPE, userId(), locale());
            brands = b != null ? b : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        generate();
    }

    public void generate() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        try {
            StringBuilder path = new StringBuilder("report/mostConsulted");
            path.append("?from=").append(from != null ? from.format(FMT) : "");
            path.append("&to=").append(to != null ? to.format(FMT) : "");
            if (familyId != null) {
                path.append("&familyId=").append(familyId);
            }
            if (brandId != null) {
                path.append("&brandId=").append(brandId);
            }
            List<ProductReportVo> list = RestClient.get(path.toString(), LIST_TYPE, userId(), locale());
            rows = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            rows = new ArrayList<>();
            errors(e);
        }
    }

    public long getTotalLookups() {
        long n = 0;
        for (ProductReportVo r : rows) {
            n += r.getValue() != null ? r.getValue() : 0;
        }
        return n;
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

    public Integer getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public List<FamilyVo> getFamilies() {
        return families;
    }

    public List<BrandVo> getBrands() {
        return brands;
    }

    public List<ProductReportVo> getRows() {
        return rows;
    }
}
