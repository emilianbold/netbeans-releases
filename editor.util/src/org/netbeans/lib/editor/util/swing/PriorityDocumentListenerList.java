/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

class PriorityDocumentListenerList extends PriorityListenerList<DocumentListener> implements DocumentListener {
    
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
