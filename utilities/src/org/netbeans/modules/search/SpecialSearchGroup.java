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

package org.netbeans.modules.search;

import java.util.Collection;
import java.util.Iterator;
import org.openide.loaders.DataObject;
import org.openidex.search.DataObjectSearchGroup;
import org.openidex.search.SearchType;

/**
 *
 * @author  Marian Petras
 */
final class SpecialSearchGroup extends DataObjectSearchGroup {

    final BasicSearchCriteria basicCriteria;
    final boolean hasExtraSearchTypes;
    private final SearchScope searchScope;
    private SearchTask listeningSearchTask;
    
    SpecialSearchGroup(BasicSearchCriteria basicCriteria,
                       Collection<SearchType> extraSearchTypes,
                       SearchScope searchScope) {
        super();
        this.basicCriteria = basicCriteria;
        hasExtraSearchTypes = !extraSearchTypes.isEmpty();
        this.searchScope = searchScope;
        
        if ((basicCriteria == null) && !hasExtraSearchTypes) {
            assert false;
            throw new IllegalArgumentException();
        }
        
        if (hasExtraSearchTypes) {
            for (SearchType searchType : extraSearchTypes) {
                add(searchType);
            }
        }
    }
    
    @Override
    public void doSearch() {
        for (Iterator j = searchScope.getSearchInfo().objectsToSearch(); j.hasNext(); ) {
            if (stopped) return;
            processSearchObject(/*DataObject*/ j.next());
        }
    }

    /**
     * Provides search on one search object instance. The object is added to
     * set of searched objects and passed to all search types encapsulated by
     * this search group. In the case the object passes all search types is added
     * to the result set and fired an event <code>PROP_FOUND</code> about successful
     * match to interested property change listeners. 
     *
     * @param searchObject object to provide actuall test on it. The actual instance
     * has to be of type returned by all <code>SearchKey.getSearchObjectType</code>
     * returned by <code>SearchType</code> of this <code>SearchGroup</code>
     */
    @Override
    protected void processSearchObject(Object searchObject) {
        if (!hasExtraSearchTypes) {
            assert basicCriteria != null;
            DataObject dataObj = (DataObject) searchObject;
            if (basicCriteria.matches(dataObj)) {
                notifyMatchingObjectFound(dataObj);
            }
            return;
        }
        
        if ((basicCriteria == null) || basicCriteria.matches((DataObject) searchObject)) {
            super.processSearchObject(searchObject);
        }
    }

    @Override
    protected void firePropertyChange(String name,
                                      Object oldValue,
                                      Object newValue) {
        notifyMatchingObjectFound((DataObject) newValue);
    }
    
    private void notifyMatchingObjectFound(DataObject obj) {
        if (listeningSearchTask != null) {
            listeningSearchTask.matchingObjectFound(obj);
        } else {
            assert false;
        }
    }
    
    void setListeningSearchTask(SearchTask searchTask) {
        listeningSearchTask = searchTask;
    }
    
}
