package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Permission / menu item.
 */
public class PermissionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String code;

    private String name;

    private String description;

    private String menuGroup;

    private String outcomeWeb;

    private String outcomeApp;

    private String icon;

    /** COMMON | APPOINTMENTS | SALES */
    private String module;

    private Integer sortOrder;

    private Boolean isMenu;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsMenu() {
        return isMenu;
    }

    public void setIsMenu(Boolean isMenu) {
        this.isMenu = isMenu;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
