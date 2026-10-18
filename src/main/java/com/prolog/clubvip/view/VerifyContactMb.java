package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ContactVerificationVo;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * "Confirma tus datos": the screen a new business owner sees before the home, until the e-mail,
 * the business phone and the contact phone are confirmed with the codes Brevo sent them.
 * Per channel: type the code, ask for another one, or fix a mistyped value.
 */
@Named("verifyContactMb")
@ViewScoped
public class VerifyContactMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionMb sessionMb;

    private ContactVerificationVo status = new ContactVerificationVo();
    /** Typed code per channel. */
    private Map<String, String> codes = new HashMap<>();
    /** New value per channel while "Cambiar" is open. */
    private Map<String, String> newValues = new HashMap<>();
    private String editing;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            ContactVerificationVo vo = RestClient.get("verification/status", ContactVerificationVo.class,
                    sessionMb.getUserId(), sessionMb.getLocale());
            if (vo != null) {
                status = vo;
            }
        } catch (RestException e) {
            addErrors(e.getMessages());
        }
        syncSession();
    }

    /** The session copy of the pending list drives the filter, so it follows every confirmation. */
    private void syncSession() {
        LoginResponseVo user = sessionMb.getUser();
        if (user != null && status != null) {
            user.setPendingVerifications(status.getPending());
        }
    }

    public void send(String channel) {
        call("verification/send", channel, null, null, "verify.sent");
    }

    public void confirm(String channel) {
        String code = codes.get(channel);
        if (code == null || code.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, sessionMb.text("verify.code.required"), null));
            return;
        }
        if (call("verification/confirm", channel, code, null, "verify.confirmed")) {
            codes.remove(channel);
        }
    }

    public void startEdit(String channel) {
        editing = channel;
        newValues.put(channel, targetOf(channel));
    }

    public void cancelEdit() {
        editing = null;
    }

    public void change(String channel) {
        if (call("verification/change", channel, null, newValues.get(channel), "verify.changed")) {
            editing = null;
            codes.remove(channel);
        }
    }

    private boolean call(String path, String channel, String code, String value, String okKey) {
        Map<String, String> body = new HashMap<>();
        body.put("channel", channel);
        if (code != null) {
            body.put("code", code);
        }
        if (value != null) {
            body.put("value", value);
        }
        try {
            ContactVerificationVo vo = RestClient.post(path, body, ContactVerificationVo.class,
                    sessionMb.getUserId(), sessionMb.getLocale());
            if (vo != null) {
                status = vo;
            }
            syncSession();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, sessionMb.text(okKey), null));
            return true;
        } catch (RestException e) {
            addErrors(e.getMessages());
            return false;
        }
    }

    private void addErrors(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    private String targetOf(String channel) {
        for (ContactVerificationVo.Item it : status.getItems()) {
            if (channel.equals(it.getChannel())) {
                return it.getTarget();
            }
        }
        return null;
    }

    /** Everything confirmed: on to the home of the profile. */
    public String goHome() {
        LoginResponseVo user = sessionMb.getUser();
        if (user == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        return LoginMb.homeFor(user) + "?faces-redirect=true";
    }

    /** "Confirmar después": in for this session; the screen comes back on the next login. */
    public String postpone() {
        LoginResponseVo user = sessionMb.getUser();
        if (user == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        user.setVerificationPostponed(true);
        return LoginMb.homeFor(user) + "?faces-redirect=true";
    }

    /** What happens on "Enviar código" for this kind of contact (spam tip for e-mail). */
    public String channelHelp(String channel) {
        return sessionMb.text("verify.help." + channel);
    }

    public boolean isAllDone() {
        return status.getPending() == null || status.getPending().isEmpty();
    }

    public int getDoneCount() {
        int n = 0;
        for (ContactVerificationVo.Item it : status.getItems()) {
            if (Boolean.TRUE.equals(it.getVerified())) {
                n++;
            }
        }
        return n;
    }

    public String channelLabel(String channel) {
        return sessionMb.text("verify.channel." + channel);
    }

    public String channelIcon(String channel) {
        return "EMAIL".equals(channel) ? "pi pi-envelope" : "pi pi-mobile";
    }

    public boolean isEditing(String channel) {
        return channel != null && channel.equals(editing);
    }

    public ContactVerificationVo getStatus() {
        return status;
    }

    public Map<String, String> getCodes() {
        return codes;
    }

    public Map<String, String> getNewValues() {
        return newValues;
    }
}
