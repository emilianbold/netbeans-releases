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
package org.netbeans.spi.java.classpath.support;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

/**
 * This class provides a base class for PathResource implementations
 * @since org.netbeans.api.java/1 1.4
 */
public abstract class PathResourceBase implements PathResourceImplementation {

    private ArrayList<PropertyChangeListener> pListeners;


    /**
     * Adds property change listener.
     * The listener is notified when the roots of the PathResource are changed.
     * @param listener
     */
    public synchronized final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            this.pListeners = new ArrayList<PropertyChangeListener> ();
        this.pListeners.add (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public synchronized final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            return;
        this.pListeners.remove (listener);
    }

    /**
     * Fires PropertyChangeEvent
     * @param propName name of property
     * @param oldValue old property value or null
     * @param newValue new property value or null
     */
    protected final void firePropertyChange (String propName, Object oldValue, Object newValue) {
        PropertyChangeListener[] _listeners;
        synchronized (this) {
            if (this.pListeners == null)
                return;
            _listeners = this.pListeners.toArray(new PropertyChangeListener[this.pListeners.size()]);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        for (PropertyChangeListener l : _listeners) {
            l.propertyChange (event);
        }
    }
}
