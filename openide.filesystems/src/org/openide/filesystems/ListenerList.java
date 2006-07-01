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
