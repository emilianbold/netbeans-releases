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

package org.netbeans.modules.j2ee.metadata;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple equivalent of {@link java.beans.PropertyChangeSupport} for
 * {@link ChangeListener}'s.
 *
 * @author Andrei Badea
 */
public class ChangeSupport {

    // XXX move to j2ee/utilities or openide/util

    private final Object source;
    private List<ChangeListener> listeners = Collections.emptyList();

    public ChangeSupport(Object source) {
        this.source = source;
    }

    public synchronized void addChangeListener(ChangeListener listener) {
        List<ChangeListener> newListeners = new LinkedList<ChangeListener>(listeners);
        newListeners.add(listener);
        listeners = newListeners;
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        List<ChangeListener> newListeners = new LinkedList<ChangeListener>(listeners);
        newListeners.remove(listener);
        listeners = newListeners;
    }

    public void fireChange() {
        fireChange(new ChangeEvent(source));
    }

    public void fireChange(ChangeEvent event) {
        List<ChangeListener> listenersCopy;
        synchronized (this) {
            listenersCopy = listeners;
        }
        for (ChangeListener listener : listenersCopy) {
            listener.stateChanged(event);
        }
    }

    public synchronized int getListenerCount() {
        return listeners.size();
    }
}
