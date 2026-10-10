package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shortage / surplus report built from CLOSED stock counts within a date range.
 */
public class StockDiffReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<StockDiffLineVo> lines = new ArrayList<>();

    private Integer shortageUnits = 0;

    private Integer surplusUnits = 0;

    private BigDecimal totalDifferenceValue = BigDecimal.ZERO;

    public List<StockDiffLineVo> getLines() {
        return lines;
    }

    public void setLines(List<StockDiffLineVo> lines) {
        this.lines = lines;
    }

    public Integer getShortageUnits() {
        return shortageUnits;
    }

    public void setShortageUnits(Integer shortageUnits) {
        this.shortageUnits = shortageUnits;
    }

    public Integer getSurplusUnits() {
        return surplusUnits;
    }

    public void setSurplusUnits(Integer surplusUnits) {
        this.surplusUnits = surplusUnits;
    }

    public BigDecimal getTotalDifferenceValue() {
        return totalDifferenceValue;
    }

    public void setTotalDifferenceValue(BigDecimal totalDifferenceValue) {
        this.totalDifferenceValue = totalDifferenceValue;
    }
}
