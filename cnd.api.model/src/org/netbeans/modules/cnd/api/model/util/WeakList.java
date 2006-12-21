/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.api.model.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A list that keeps weak references to its elements
 * @author Vladimir Kvashin
 */
public class WeakList<T> implements Iterable<T> {
    
    private List<WeakReference<T>> list = new ArrayList<WeakReference<T>>();
    
    /**
     * Adds a weak reference to the given element to this list
     */
    public synchronized void add(T element) {
        list.add(new WeakReference<T>(element));
    }

    /**
     * Adds all weak references frim the given iterator to this list
     */
    public synchronized void addAll(Iterator<T> elements) {
	while( elements.hasNext() ) {
	    list.add(new WeakReference<T>(elements.next()));
	}
    }
    
    /*
     * Removes all references to the given element from this list
     */
    public synchronized void remove(T element) {
	for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
	    WeakReference<T> ref = it.next();
            if( ref.get() == element ) {
                it.remove();
            }
        }
    }
    
    /** Removes all elements */
    public synchronized void clear() {
	list.clear();
    }
    
    /** 
     * Returns an iterator of non-null references.
     * NB: it iterates over a snapshot made at the moment of the call
     */
    public synchronized Iterator<T> iterator() {
        List<T> result = new ArrayList<T>();
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
            WeakReference<T> ref = it.next();
            T element = ref.get();
            if( element != null ) {
                result.add(element);
            }
        }    
        return result.iterator();
    }
}
