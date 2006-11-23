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
    private final long creationTime;
    
    /** */
    private int size = 0;
    /** */
    private int totalDetailsCount = 0;
    /**
     */
    private ResultTreeModel treeModel;
    /** */
    private ResultView resultView;
    
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
    final boolean fullTextOnly;
    /** */
    final FullTextType fullTextSearchType;
    /** */
    final String replaceString;
    /** */
    final boolean searchAndReplace;
    /** list of matching objects (usually {@code DataObject}s) */
    final List<MatchingObject> matchingObjects
            = new ArrayList<MatchingObject>();

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;

    /** Creates new <code>ResultModel</code>. */
    public ResultModel(List<SearchType> searchTypeList,
                       SearchGroup searchGroup,
                       String replaceString) {
        this.searchTypeList = searchTypeList;
        this.searchGroup = searchGroup;
        this.replaceString = replaceString;
        this.searchAndReplace = (replaceString != null);
        
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
        fullTextOnly = (searchAndReplace || defaultSearchTypesOnly)
                       && (fullTextSearchType != null);
        
        creationTime = System.currentTimeMillis();
    }
    
    /**
     */
    long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Sets an observer which will be notified whenever an object is found.
     *
     * @param  observer  observer or <code>null</code>
     */
    void setObserver(ResultTreeModel observer) {
        this.treeModel = observer;
    }
    
    /**
     * Sets an observer which will be notified whenever an object is found.
     *
     * @param  observer  observer or <code>null</code>
     */
    void setObserver(ResultView observer) {
        this.resultView = observer;
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
        MatchingObject matchingObject = new MatchingObject(this, object);
        if (matchingObjects.add(matchingObject) == false) {
            return true;
        }
        
        assert limitReached == false;
        assert treeModel != null;
        assert resultView != null;
        
        totalDetailsCount += getDetailsCount(matchingObject);
        
        treeModel.objectFound(matchingObject, size++);
        resultView.objectFound(matchingObject, totalDetailsCount);
        
        return size < COUNT_LIMIT && totalDetailsCount < DETAILS_COUNT_LIMIT;
    }
    
    /**
     */
    synchronized int getTotalDetailsCount() {
        return totalDetailsCount;
    }
    
    /**
     */
    synchronized MatchingObject[] getMatchingObjects() {
        return matchingObjects.toArray(
                                    new MatchingObject[matchingObjects.size()]);
    }
    
    /**
     */
    synchronized Object[] getFoundObjects() {
        Object[] foundObjects = new Object[matchingObjects.size()];
        int index = 0;
        for (MatchingObject matchingObj : matchingObjects) {
            foundObjects[index++] = matchingObj.object;
        }
        return foundObjects;
    }
    
    public void run() {
        
    }
    
    /**
     */
    boolean hasDetails() {
        return totalDetailsCount != 0;      //PENDING - synchronization?
    }
    
    /**
     * Performs a quick check whether
     * {@linkplain MatchingObject matching objects} contained in this model
     * can have details.
     * 
     * @return  {@code Boolean.TRUE} if all matching objects have details,
     *          {@code Boolean.FALSE} if no matching object has details,
     *          {@code null} if matching objects may have details
     *                         (if more time consuming check would be necessary)
     */
    Boolean canHaveDetails() {
        Boolean ret;
        if (fullTextSearchType != null) {
            ret = Boolean.TRUE;
        } else if (defaultSearchTypesOnly) {
            ret = Boolean.FALSE;
        } else {
            ret = null;
        }
        return ret;
    }
    
    /*
     * A cache exists for information about a single MatchingObject
     * to prevent from repetitive calls of time-consuming queries on
     * number of details and list of details. These calls are initiated
     * by the node renderer (class NodeRenderer).
     */
    
    private MatchingObject infoCacheMatchingObject;
    private Boolean infoCacheHasDetails;
    private int infoCacheDetailsCount;
    private Node[] infoCacheDetailNodes;
    private final Node[] EMPTY_NODES_ARRAY = new Node[0];
    
    /**
     */
    private void prepareCacheFor(MatchingObject matchingObject) {
        if (matchingObject != infoCacheMatchingObject) {
            infoCacheHasDetails = null;
            infoCacheDetailsCount = -1;
            infoCacheDetailNodes = null;
            infoCacheMatchingObject = matchingObject;
        }
    }
    
    /**
     */
    boolean hasDetails(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        if (infoCacheHasDetails != null) {
            return infoCacheHasDetails.booleanValue();
        }
        
        boolean hasDetails = hasDetailsReal(matchingObject);
        infoCacheHasDetails = Boolean.valueOf(hasDetails);
        
        assert (infoCacheHasDetails == Boolean.TRUE)
               || (infoCacheHasDetails == Boolean.FALSE);
        return hasDetails;
    }
    
    /**
     */
    private boolean hasDetailsReal(MatchingObject matchingObject) {
        boolean ret;
        if (fullTextSearchType != null) {
            ret = true;
        } else if (defaultSearchTypesOnly) {
            ret = false;
        } else {
            ret = false;
            final Object foundObject = matchingObject.object;
            for (SearchType searchType : searchGroup.getSearchTypes()) {
                Node[] detailNodes = searchType.getDetails(foundObject);
                if ((detailNodes != null) && (detailNodes.length != 0)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    
    /**
     */
    int getDetailsCount(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        if (infoCacheDetailsCount == -1) {
            infoCacheDetailsCount = getDetailsCountReal(matchingObject);
            if (infoCacheDetailsCount == 0) {
                infoCacheDetailNodes = EMPTY_NODES_ARRAY;
            }
        }
        
        assert infoCacheDetailsCount >= 0;
        return infoCacheDetailsCount;
    }
    
    /**
     * Returns number of detail nodes available to the given found object.
     *
     * @param  foundObject  object matching the search criteria
     * @return  number of detail items (represented by individual nodes)
     *          available for the given object (usually {@code DataObject})
     */
    private int getDetailsCountReal(MatchingObject matchingObject) {
        if (defaultSearchTypesOnly) {
            return (fullTextSearchType != null)
                   ? fullTextSearchType.getDetailsCount(matchingObject.object)
                   : 0;
        }
        
        int count = 0;
        final Object foundObject = matchingObject.object;
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
     * 
     * @return  non-empty array of detail nodes
     *          or {@code null} if there are no detail nodes
     */
    Node[] getDetails(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        Node[] detailNodes;
        if (infoCacheDetailNodes == null) {
            detailNodes = getDetailsReal(matchingObject);
            infoCacheDetailNodes = (detailNodes != null)
                                   ? detailNodes
                                   : EMPTY_NODES_ARRAY;
            infoCacheDetailsCount = infoCacheDetailNodes.length;
        } else {
            detailNodes = (infoCacheDetailNodes != EMPTY_NODES_ARRAY)
                          ? infoCacheDetailNodes
                          : null;
        }
        
        assert (infoCacheDetailNodes != null)
               && ((infoCacheDetailNodes == EMPTY_NODES_ARRAY)
                   || (infoCacheDetailNodes.length > 0));
        assert (detailNodes == null) || (detailNodes.length > 0);
        return detailNodes;
    }
    
    /**
     * 
     * @return  non-empty array of detail nodes
     *          or {@code null} if there are no detail nodes
     */
    private Node[] getDetailsReal(MatchingObject matchingObject) {
        if (defaultSearchTypesOnly) {
            return (fullTextSearchType != null)
                   ? fullTextSearchType.getDetails(matchingObject.object)
                   : null;
        }
        
        Node[] nodesTotal = null;
        final Object foundObject = matchingObject.object;
        for (SearchType searchType : searchGroup.getSearchTypes()) {
            Node[] detailNodes = searchType.getDetails(foundObject);
            if ((detailNodes == null) || (detailNodes.length == 0)) {
                continue;
            }
            if (nodesTotal == null) {
                nodesTotal = detailNodes;
            } else {
                Node[] oldNodesTotal = nodesTotal;
                nodesTotal = new Node[nodesTotal.length + detailNodes.length];
                System.arraycopy(oldNodesTotal, 0,
                                 nodesTotal, 0,
                                 oldNodesTotal.length);
                System.arraycopy(detailNodes, 0,
                                 nodesTotal, oldNodesTotal.length,
                                 detailNodes.length);
            }
        }
        return nodesTotal;
    }
    
    /**
     */
    synchronized int size() {
        return size;
    }
    
    /**
     */
    boolean isEmpty() {
        return size == 0;
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
