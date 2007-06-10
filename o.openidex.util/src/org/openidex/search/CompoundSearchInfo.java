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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.loaders.DataObject;

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
    CompoundSearchInfo(SearchInfo... elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        this.elements = elements.length != 0 ? elements
                                             : null;
    }

    /**
     */
    public boolean canSearch() {
        if (elements != null) {
            for (SearchInfo element : elements) {
                if (element.canSearch()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    public Iterator<DataObject> objectsToSearch() {
        if (elements == null) {
            return Collections.<DataObject>emptyList().iterator();
        }
        
        List<SearchInfo> searchableElements = new ArrayList<SearchInfo>(elements.length);
        for (SearchInfo element : elements) {
            if (element.canSearch()) {
                searchableElements.add(element);
            }
        }
        return new CompoundSearchIterator(
            searchableElements.toArray(
                    new SearchInfo[searchableElements.size()]));
    }
    
}
