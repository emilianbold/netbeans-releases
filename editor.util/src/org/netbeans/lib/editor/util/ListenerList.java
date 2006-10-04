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
import java.util.EventListener;
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
    private transient T[] listeners;
    
    public ListenerList() {
        listeners = emptyTArray();
    }
    
    /**
     * Returns array of the listeners.
     * <br/>
     * The listener added as the last one is at index 0
     * so for firing in the order of how the listeners were added use:
     * <pre>
     *      MyListener[] listeners = listenerList.getListeners();
     *      for (int i = listeners.length - 1; i >= 0; i--) {
     *          listeners[i].notify(evt);
     *      }
     * </pre>
     * 
     * <p>
     * <b>Note</b>: Absolutely NO modification of
     * the data contained in the returned array should be made.
     * </p>
     * 
     * @return non-null array of listeners contained in this listener list.
     */
    public T[] getListeners() {
        return listeners;
    }
    
    /**
     * Returns the total number of listeners for this listener list.
     */
    public int getListenerCount() {
        return listeners.length;
    }
    
    /**
     * Adds the given listener to this listener list.
     * 
     * @param listener the listener to be added. If null is passed it is ignored (nothing gets added).
     */
    public synchronized void add(T listener) {
        if (listener == null)
            return;

        T[] tmp;
        if (listeners == EMPTY_LISTENER_ARRAY) {
            tmp = allocateTArray(1);
        } else { // non-empty
            tmp = allocateTArray(listeners.length + 1);
            System.arraycopy(listeners, 0, tmp, 1, listeners.length);
        }
        tmp[0] = listener;
        listeners = tmp;
    }
    
    /**
     * Removes the given listener from this listener list.
     * <br/>
     * The existing listeners are compared by "==" to the given listener
     * from the last existing listener till the first one.
     * 
     * @param listener the listener to be removed. If null is passed it is ignored (nothing gets removed).
     */
    public synchronized void remove(T listener) {
        if (listener == null)
            return;

        // Search from 0 - suppose that later added will sooner be removed
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == listener) {
                if (listeners.length > 1) {
                    T[] tmp = allocateTArray(listeners.length - 1);
                    System.arraycopy(listeners, 0, tmp, 0, i);
                    System.arraycopy(listeners, i + 1, tmp, i, tmp.length - i);
                    listeners = tmp;
                } else { // Only one listener was in the array
                    listeners = emptyTArray();
                }
            }
        }
    }
    
    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        T[] listeners = this.listeners; // Ignore possible mods during this method
        s.defaultWriteObject();
        
        // Write in opposite order of adding 
        for (int i = 0; i < listeners.length; i++) {
            T l = listeners[i];
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
        @SuppressWarnings("unchecked")
        T[] lArr = (T[])lList.toArray(new EventListener[lList.size()]);
        this.listeners = lArr;
    }
    
    public String toString() {
        return ArrayUtilities.toString(listeners);
    }
    
    @SuppressWarnings("unchecked")
    private T[] allocateTArray(int length) {
        return (T[])new EventListener[length];
    }
    
    @SuppressWarnings("unchecked")
    private T[] emptyTArray() {
        return (T[])EMPTY_LISTENER_ARRAY;
    }

}
