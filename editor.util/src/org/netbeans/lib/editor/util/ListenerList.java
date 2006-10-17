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

package org.netbeans.lib.editor.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

/**
 * Listener list storing listeners of a single type.
 *
 * @author Miloslav Metelka
 * @since 1.11
 */

public final class ListenerList<T extends EventListener> implements Serializable {

    static final long serialVersionUID = 0L;
    
    /** A null array to be shared by all empty listener lists */
    private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];

    /* The array of listeners. */
    private transient List<T> listeners;
    
    public ListenerList() {
        listeners = new ArrayList<T>();
    }
    
    /**
     * Returns a list of listeners.
     * 
     * <p>The listeners are returned in exactly the same order
     * as they were added to this list. Use the following code
     * for firing events to all the listeners you get from this
     * method.
     * 
     * <pre>
     *      List<MyListener> listeners = listenerList.getListeners();
     *      for (MyListener l : listeners) {
     *          l.notify(evt);
     *      }
     * </pre>
     * 
     * @return A list of listeners contained in this listener list.
     */
    public synchronized List<T> getListeners() {
        return new ArrayList<T>(listeners);
    }
    
    /**
     * Returns the total number of listeners for this listener list.
     */
    public synchronized int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Adds the given listener to this listener list.
     * 
     * @param listener the listener to be added. If null is passed it is ignored (nothing gets added).
     */
    public synchronized void add(T listener) {
        if (listener == null)
            return;

        listeners.add(listener);
    }
    
    /**
     * Removes the given listener from this listener list.
     * 
     * @param listener the listener to be removed. If null is passed it is ignored (nothing gets removed).
     */
    public synchronized void remove(T listener) {
        if (listener == null)
            return;

        listeners.remove(listener);
    }
    
    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        
        // Write in opposite order of adding 
        for (Iterator<T> i = listeners.iterator(); i.hasNext(); ) {
            T l = i.next();
            // Save only the serializable listeners
            if (l instanceof Serializable) {
                s.writeObject(l);
            }
        }
        
        s.writeObject(null);
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        List<T> lList = new ArrayList<T>();
        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
            @SuppressWarnings("unchecked")
            T l = (T)listenerOrNull;
            lList.add(l);
        }
        this.listeners = lList;
    }
    
    public String toString() {
        return listeners.toString();
    }
    
}
