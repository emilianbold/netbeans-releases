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


package org.netbeans.modules.search;


import java.beans.*;
import java.util.*;
import org.netbeans.modules.search.types.FullTextType;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openidex.search.DataObjectSearchGroup;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Holds search result data.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public final class ResultModel {

    /** maximum number of found objects */
    private static final int COUNT_LIMIT = 500;
    /** maximum total number of detail entries for found objects */
    private static final int DETAILS_COUNT_LIMIT = 5000;
    /** */
    private static final String DEF_SEARCH_TYPES_PACKAGE
            = "org.netbeans.modules.search.types";                      //NOI18N
    private static final String FULLTEXT_SEARCH_TYPE
            = "FullTextType";                                           //NOI18N
    /** */
    private int size = 0;
    /** */
    private int totalDetailsCount = 0;
    /**
     */
    private ResultTreeChildren observer;
    
    /**
     * flag - did number of found objects reach the limit?
     *
     * @see  #COUNT_LIMIT
     */
    private boolean limitReached = false;

    /** Which search types creates were enabled for this model. */
    private List<SearchType> searchTypeList;

    /** Search group this result shows search results for. */
    private SearchGroup searchGroup;
    
    /**
     * is the {@code searchGroup} an instance of class
     * {@code DataObjectSearchGroup}?
     * 
     * @see  #searchGroup
     */
    private final boolean isDataObjectSearchGroup;
    /**
     * are all search types defined in the {@code SearchGroup} those
     * defined in the Utilities module?
     */
    final boolean defaultSearchTypesOnly;
    /** */
    final FullTextType fullTextSearchType;
    /** list of matching objects (usually {@code DataObject}s) */
    final Collection<MatchingObject> matchingObjects
            = new HashSet<MatchingObject>();

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;

    /** Creates new <code>ResultModel</code>. */
    public ResultModel(List<SearchType> searchTypeList,
                       SearchGroup searchGroup) {
        this.searchTypeList = searchTypeList;
        this.searchGroup = searchGroup;
        
        isDataObjectSearchGroup
                = (searchGroup.getClass() == DataObjectSearchGroup.class);
        boolean hasNonDefaultSearchType = false;
        FullTextType fullTextType = null;
        for (SearchType searchType : searchGroup.getSearchTypes()) {
            Class searchTypeClass = searchType.getClass();
            String searchTypeName = searchTypeClass.getName();
            if (searchTypeClass == FullTextType.class) {
                fullTextType = (FullTextType) searchType;
            } else if (!searchTypeName.startsWith(DEF_SEARCH_TYPES_PACKAGE)) {
                hasNonDefaultSearchType = true;
            }
            if (hasNonDefaultSearchType && (fullTextType != null)) {
                break;
            }
        }
        defaultSearchTypesOnly = !hasNonDefaultSearchType;
        fullTextSearchType = fullTextType;
    }
    
    /**
     * Sets an observer which will be notified whenever an object is found.
     *
     * @param  observer  observer or <code>null</code>
     */
    void setObserver(ResultTreeChildren observer) {
        this.observer = observer;
    }

    /**
     * Clean the allocated resources. Do not rely on GC there as we are often referenced from
     * various objects (some VisualizerNode realy loves us). So keep leak as small as possible.
     * */
    void close() {
        if (searchTypeList != null){
            for (SearchType searchType : searchTypeList) {
                /*
                 * HACK:
                 * GC should eliminate FullTextType details map but it does not,
                 * so we force cleaning of the map
                 */
                if (searchType instanceof                           //XXX - hack
                        org.netbeans.modules.search.types.FullTextType) {
                    ((org.netbeans.modules.search.types.FullTextType)searchType)
                    .destroy();
                }
            }
            searchTypeList.clear();
            searchTypeList = null;
        }

        // eliminate search group content
        // no other way then leaving it on GC, it should work because
        // search group is always recreated by a it's factory and
        // nobody keeps reference to it. 7th May 2004

        searchGroup = null;
    }
    
    /**
     * Notifies ths result model of a newly found matching object.
     *
     * @param  object  matching object
     * @return  {@code true} if this result model can accept more objects,
     *          {@code false} if number of found objects reached the limit
     */
    synchronized boolean objectFound(Object object) {
        MatchingObject matchingObject = new MatchingObject(object);
        if (matchingObjects.add(matchingObject) == false) {
            return true;
        }
        
        assert limitReached == false;
        assert observer != null;
        
        observer.objectFound(matchingObject);
        
        size++;
        totalDetailsCount += getDetailsCount(object);
        return size < COUNT_LIMIT && totalDetailsCount < DETAILS_COUNT_LIMIT;
    }
    
    /**
     * Returns number of detail nodes available to the given found object.
     *
     * @param  foundObject  object matching the search criteria
     * @return  number of detail items (represented by individual nodes)
     *          available for the given object (usually {@code DataObject})
     */
    private int getDetailsCount(Object foundObject) {
        if (defaultSearchTypesOnly) {
            return (fullTextSearchType != null)
                   ? fullTextSearchType.getDetailsCount(foundObject)
                   : 0;
        }
        
        int count = 0;
        for (SearchType searchType : searchGroup.getSearchTypes()) {
            if (searchType == fullTextSearchType) {
                count += fullTextSearchType.getDetailsCount(foundObject);
            } else {
                Node[] detailNodes = searchType.getDetails(foundObject);
                count += (detailNodes != null) ? detailNodes.length : 0;
            }
        }
        return count;
    }
    
    /**
     */
    int size() {
        return size;
    }

    /** Getter for search group property. */
    SearchGroup getSearchGroup() {
        return searchGroup;
    }
    
    /** Gets all search types, all enabled not only customized ones. */
    List getEnabledSearchTypes() {
        return searchTypeList;
    }
    
    /**
     * Returns search types that were used during the search.
     *
     * @return  array of <code>SearchType</code>s that each tested object was
     *          tested for compliance
     */
    SearchType[] getQueriedSearchTypes() {
        return searchGroup.getSearchTypes();
    }

    /**
     */
    boolean wasLimitReached() {
        return limitReached;
    }

    /** This exception stoped search */
    void searchException(RuntimeException ex) {
        ErrorManager.Annotation[] annotations = ErrorManager.getDefault().findAnnotations(ex);
        for (int i = 0; i < annotations.length; i++) {
            ErrorManager.Annotation annotation = annotations[i];
            if (annotation.getSeverity() == ErrorManager.USER) {
                finishMessage = annotation.getLocalizedMessage();
                if (finishMessage != null) return;
            }
        }
        finishMessage = ex.getLocalizedMessage();
    }
    
    /**
     */
    String getExceptionMsg() {
        return finishMessage;
    }
    
    /**
     * Data structure holding a reference to the found object and information
     * whether occurences in the found object should be found or not.
     */
    static final class MatchingObject {
        final Object object;
        MatchingObject(Object object) {
            if (object == null) {
                throw new IllegalArgumentException("null");             //NOI18N
            }
            this.object = object;
        }
        @Override
        public boolean equals(Object anotherObject) {
            return (anotherObject != null)
                   && (anotherObject.getClass() == MatchingObject.class)
                   && (((MatchingObject) anotherObject) == object);
        }
        @Override
        public int hashCode() {
            return object.hashCode() + 1;
        }
    }

}
