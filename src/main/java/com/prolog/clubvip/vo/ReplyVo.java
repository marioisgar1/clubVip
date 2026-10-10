package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Business reply to a review.
 */
public class ReplyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reply;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
