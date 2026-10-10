package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Number of businesses in one category.
 */
public class CategoryCountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryName;

    private Long count;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
