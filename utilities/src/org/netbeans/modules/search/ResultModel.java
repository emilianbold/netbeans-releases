/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.awt.Image;
import java.beans.*;
import java.io.IOException;
import java.io.CharConversionException;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.WeakListeners;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;
import org.openide.util.Utilities;


/**
 * Holds search result data.
 * 
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public final class ResultModel extends Observable {

    /** maximum number of found objects */
    private static final int COUNT_LIMIT = 500;
    /** */
    private int size = 0;
    
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
        size++;
        setChanged();
        notifyObservers(object);
        limitReached = size >= COUNT_LIMIT;
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
