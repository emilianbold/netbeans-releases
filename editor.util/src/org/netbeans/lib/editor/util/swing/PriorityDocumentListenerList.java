/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.util.swing;

import java.util.EventListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.PriorityListenerList;

/**
 * Priority listener list that acts as DocumentListener itself
 * firing all added document listeners according to their priority.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class PriorityDocumentListenerList extends PriorityListenerList implements DocumentListener {
    
    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void insertUpdate(DocumentEvent evt) {
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                ((DocumentListener)listeners[i]).insertUpdate(evt);
            }
        }
    }

    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void removeUpdate(DocumentEvent evt) {
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                ((DocumentListener)listeners[i]).removeUpdate(evt);
            }
        }
    }

    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void changedUpdate(DocumentEvent evt) {
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                ((DocumentListener)listeners[i]).changedUpdate(evt);
            }
        }
    }

}
