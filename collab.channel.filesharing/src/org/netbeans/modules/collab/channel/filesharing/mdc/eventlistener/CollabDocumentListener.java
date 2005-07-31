/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import org.openide.*;

import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.event.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.DocumentEventContext;


/**
 * Document Listener attached to each shared file being handled
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class CollabDocumentListener extends Object implements DocumentListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* Document */
    protected Document document = null;

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /**
     * constructor for CollabDocumentListener
     *
     * @param document
     * @param fileHandler
     */
    public CollabDocumentListener(Document document, EventNotifier eventNotifier) {
        super();
        this.document = document;
        this.eventNotifier = eventNotifier;
    }

    /**
     *
     * @param documentEvent
     */
    public void changedUpdate(DocumentEvent documentEvent) {
        // Ignore
    }

    /**
     * invoked during document insert
     *
     * @param event
     */
    public void insertUpdate(DocumentEvent event) {
        int offset = event.getOffset();
        String text = null;

        try {
            text = document.getText(offset, event.getLength());
        } catch (Exception e) {
            remove(e);
        }

        EventContext evContext = new DocumentEventContext(
                DocumentInsertUpdate.getEventID(), document, offset, text.length(), text
            );
        CollabEvent ce = new DocumentInsertUpdate(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * invoked during document remove
     *
     * @param event
     */
    public void removeUpdate(DocumentEvent event) {
        EventContext evContext = new DocumentEventContext(
                DocumentRemoveUpdate.getEventID(), document, event.getOffset(), event.getLength(), null
            );
        CollabEvent ce = new DocumentRemoveUpdate(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * invoked during listener removal
     * @param e
     */
    public void remove(Throwable e) {
        ErrorManager.getDefault().notify(
            ErrorManager.getDefault().annotate(e, "Removing document listener due to exception")
        ); // NOI18N
        remove();
    }

    /**
     *
     *
     */
    public void remove() {
        document.removeDocumentListener(this);
    }
}
