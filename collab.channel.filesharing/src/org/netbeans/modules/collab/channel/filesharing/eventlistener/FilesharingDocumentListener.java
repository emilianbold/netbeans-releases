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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import org.openide.*;

import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeInsert;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeRemove;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.DocumentEventContext;
import org.netbeans.modules.collab.core.Debug;


/**
 * Document Listener attached to each shared file being handled
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class FilesharingDocumentListener extends CollabDocumentListener {
    private boolean skipUpdate;

    /**
     * constructor for CollabDocumentListener
     *
     * @param document
     * @param fileHandler
     */
    public FilesharingDocumentListener(Document document, EventNotifier eventNotifier) {
        super(document, eventNotifier);
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
        Debug.log("CollabFileHandlerSupport", "FilesharingDocumentListener, insertUpdate: ");

        if (isSkipUpdate()) {
            return;
        }

        int offset = event.getOffset();
        String text = null;

        try {
            text = document.getText(offset, event.getLength());
        } catch (Exception e) {
            remove(e);
        }

        EventContext evContext = new DocumentEventContext(
                DocumentChangeInsert.getEventID(), document, offset, text.length(), text
            );
        CollabEvent ce = new DocumentChangeInsert(evContext);

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
        Debug.log("CollabFileHandlerSupport", "FilesharingDocumentListener, removeUpdate: ");

        if (isSkipUpdate()) {
            return;
        }

        EventContext evContext = new DocumentEventContext(
                DocumentChangeRemove.getEventID(), document, event.getOffset(), event.getLength(), null
            );
        CollabEvent ce = new DocumentChangeRemove(evContext);

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

    public void setSkipUpdate(boolean skip) {
        this.skipUpdate = skip;
    }

    public boolean isSkipUpdate() {
        return this.skipUpdate;
    }
}
