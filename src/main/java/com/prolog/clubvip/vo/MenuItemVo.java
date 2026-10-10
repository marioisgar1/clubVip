package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * A single menu entry derived from a permission (sidebar in web / app).
 */
public class MenuItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private String menuGroup;

    private String outcomeWeb;

    private String outcomeApp;

    private String icon;

    private Integer sortOrder;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(String menuGroup) {
        this.menuGroup = menuGroup;
    }

    public String getOutcomeWeb() {
        return outcomeWeb;
    }

    public void setOutcomeWeb(String outcomeWeb) {
        this.outcomeWeb = outcomeWeb;
    }

    public String getOutcomeApp() {
        return outcomeApp;
    }

    public void setOutcomeApp(String outcomeApp) {
        this.outcomeApp = outcomeApp;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
