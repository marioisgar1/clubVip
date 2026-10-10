package com.prolog.clubvip.view;

/**
 * Implemented by page beans that own the shared appointment / booking dialogs, so the dialog
 * beans can ask the page to reload its data after an action (start, close, book, ...).
 */
public interface Refreshable {

    void refresh();
}
