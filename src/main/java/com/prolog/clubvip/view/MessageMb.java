package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.MessageLogVo;
import com.prolog.clubvip.vo.MessageTemplateVo;

import org.primefaces.PrimeFaces;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/** WhatsApp templates of the business (with platform defaults) and the sent-messages log. */
@Named("messageMb")
@ViewScoped
public class MessageMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type TEMPLATE_LIST = new TypeToken<List<MessageTemplateVo>>() { }.getType();
    private static final Type LOG_LIST = new TypeToken<List<MessageLogVo>>() { }.getType();
    public static final String[] TYPES = {"AUTO_REPLY", "REMINDER", "BIRTHDAY", "REACTIVATION", "REVIEW_REQUEST", "GENERIC"};

    private List<MessageTemplateVo> templates = new ArrayList<>();
    private MessageTemplateVo selected = new MessageTemplateVo();
    private List<MessageLogVo> log = new ArrayList<>();
    private LocalDate from;
    private LocalDate to;

    @PostConstruct
    public void init() {
        to = today();
        from = to.withDayOfMonth(1);
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<MessageTemplateVo> list = RestClient.get("message/getTemplates", TEMPLATE_LIST, userId(), locale());
            templates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            templates = new ArrayList<>();
            errors(e);
        }
        if (sessionMb.isAdmin()) {
            loadLog();
        }
    }

    public void loadLog() {
        try {
            List<MessageLogVo> list = RestClient.get("message/getLog?from=" + from + "&to=" + to, LOG_LIST,
                    userId(), locale());
            log = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            log = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new MessageTemplateVo();
        selected.setTemplateType("GENERIC");
    }

    /** Editing a platform default creates a copy for the business (id cleared). */
    public void prepareEdit(MessageTemplateVo t) {
        selected = new MessageTemplateVo();
        selected.setId(isOwn(t) ? t.getId() : null);
        selected.setTemplateType(t.getTemplateType());
        selected.setName(t.getName());
        selected.setBody(t.getBody());
    }

    public void save() {
        try {
            RestClient.post("message/saveUpdateTemplate", selected, MessageTemplateVo.class, userId(), locale());
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('templateDlg').hide()");
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(MessageTemplateVo t) {
        if (t == null || t.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("message/deleteTemplates/" + t.getId(), userId(), locale());
        if (r.isOk()) {
            info("msg.deleted");
            refresh();
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    /** Own template = belongs to this business (platform defaults have no business). */
    public boolean isOwn(MessageTemplateVo t) {
        return t != null && t.getBusinessId() != null && t.getBusinessId().equals(businessId());
    }

    public String[] getTypes() {
        return TYPES;
    }

    public List<MessageTemplateVo> getTemplates() {
        return templates;
    }

    public MessageTemplateVo getSelected() {
        return selected;
    }

    public List<MessageLogVo> getLog() {
        return log;
    }

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
}
