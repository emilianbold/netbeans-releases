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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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
