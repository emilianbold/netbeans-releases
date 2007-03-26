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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple equivalent of {@link java.beans.PropertyChangeSupport} for
 * {@link ChangeListener}s. This class is not serializable.
 *
 * @since 7.8
 * @author Andrei Badea
 */
public final class ChangeSupport {

    // not private because used in unit tests
    final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private final Object source;

    /**
     * Creates a new <code>ChangeSupport</code>
     *
     * @param  source the instance to be given as the source for events.
     */
    public ChangeSupport(Object source) {
        this.source = source;
    }

    /**
     * Adds a <code>ChangeListener</code> to the listener list. The same
     * listener object may be added more than once, and will be called
     * as many times as it is added. If <code>listener</code> is null,
     * no exception is thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be added.
     */
    public void addChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    /**
     * Removes a <code>ChangeListener</code> from the listener list.
     * If <code>listener</code> was added more than once,
     * it will be notified one less time after being removed.
     * If <code>listener</code> is null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param  listener the <code>ChangeListener</code> to be removed.
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    /**
     * Fires a change event to all registered listeners.
     */
    public void fireChange() {
        if (listeners.isEmpty()) {
            return;
        }
        fireChange(new ChangeEvent(source));
    }

    /**
     * Fires the specified <code>ChangeEvent</code> to all registered
     * listeners. If <code>event</code> is null, no exception is thrown
     * and no action is taken.
     *
     * @param  event the <code>ChangeEvent</code> to be fired.
     */
    private void fireChange(ChangeEvent event) {
        assert event != null;
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    /**
     * Checks if there are any listeners registered to this<code>ChangeSupport</code>.
     *
     * @return true if there are one or more listeners for the given property,
     *         false otherwise.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
