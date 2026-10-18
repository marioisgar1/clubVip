package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Mirror of the backend's ContactVerificationVo: one item per contact to confirm. */
public class ContactVerificationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Item> items = new ArrayList<>();
    private List<String> pending = new ArrayList<>();
    private Integer resendAfterSeconds;

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private String channel;
        private String target;
        private Boolean verified;
        private Boolean codeSent;
        private Integer sendsLeftThisHour;

        public String getChannel() {
            return channel;
        }

        public String getTarget() {
            return target;
        }

        public Boolean getVerified() {
            return verified;
        }

        public Boolean getCodeSent() {
            return codeSent;
        }

        public Integer getSendsLeftThisHour() {
            return sendsLeftThisHour;
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public List<String> getPending() {
        return pending;
    }

    public Integer getResendAfterSeconds() {
        return resendAfterSeconds;
    }
}
