/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Marian Petras
 */
class CompoundSearchInfo implements SearchInfo {
    
    /** */
    private final SearchInfo[] elements;
    
    /**
     * Creates a new instance of CompoundSearchInfo
     *
     * @param  elements  elements of this <code>SearchInfo</code>
     * @exception  java.lang.IllegalArgumentException
     *             if the argument was <code>null</code>
     */
    CompoundSearchInfo(SearchInfo[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        this.elements = elements.length != 0 ? elements
                                             : null;
    }

    /**
     */
    public boolean canSearch() {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].canSearch()) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public Iterator objectsToSearch() {
        List searchableElements = new ArrayList(elements.length);
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].canSearch()) {
                searchableElements.add(elements[i]);
            }
        }
        return new CompoundSearchIterator(
            (SearchInfo[])
            searchableElements.toArray(
                    new SearchInfo[searchableElements.size()]));
    }
    
}
