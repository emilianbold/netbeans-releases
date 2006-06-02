/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that holds a list of listeners of some type.
 * Replacement of  EventListListener, that solves performance issue #20715
 * @author  rm111737
 */
class ListenerList<T> {
    private final List<T> listenerList;
    private List<T> copy = null;

    ListenerList() {
        listenerList = new ArrayList<T>();
    }

    /**
     * Adds the listener .
     **/
    public synchronized boolean add(T listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        copy = null;

        return listenerList.add(listener);
    }

    /**
     * Removes the listener .
     **/
    public synchronized boolean remove(T listener) {
        copy = null;

        return listenerList.remove(listener);
    }

    /**
     * Passes back the event listener list
     */
    public synchronized List<T> getAllListeners() {
        if (copy == null) {
            copy = new ArrayList<T>(listenerList);
        }
        return copy;
    }
    
    public synchronized boolean hasListeners() {
        return !listenerList.isEmpty();
    }
    
}
