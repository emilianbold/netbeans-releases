/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.beans.*;
import java.util.*;

import org.openide.ErrorManager;
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
    /** */
    private int size = 0;
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
    private List searchTypeList;

    /** Search group this result shows search results for. */
    private SearchGroup searchGroup;

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;

    /** Creates new <code>ResultModel</code>. */
    public ResultModel(List searchTypeList, SearchGroup searchGroup) {
        this.searchTypeList = searchTypeList;
        this.searchGroup = searchGroup;
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
            Iterator it = searchTypeList.iterator();
            while (it.hasNext()) {
                Object searchType = /*(SearchType)*/it.next();
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
     * @return  <code>true</code> if this result model can accept more objects,
     *          <code>false</code> if number of found objects reached the limit
     */
    synchronized boolean objectFound(Object object) {
        assert limitReached == false;
        
        if ((observer != null) && observer.objectFound(object)) {
            limitReached = (++size >= COUNT_LIMIT);
        }
        return !limitReached;
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

}
