package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Pre-appointment notes update.
 */
public class PreNotesVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String preNotes;

    public String getPreNotes() {
        return preNotes;
    }

    public void setPreNotes(String preNotes) {
        this.preNotes = preNotes;
    }
}
