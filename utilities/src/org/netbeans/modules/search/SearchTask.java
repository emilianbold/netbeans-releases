/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.Task;
import org.openide.util.WeakListener;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Task performing search.
 *
 * @author  Peter Zavadsky
 */
public class SearchTask extends Task {

    /** Root nodes to search on. */
    private Node[] nodes;
    /** ResultModel result model. */
    private ResultModel resultModel;
    /** Properties listener which listens on last search type in the
     * search chain. */
    private PropertyChangeListener propListener;
    /** <code>SearchGroup</code> to search on. */
    private SearchGroup searchGroup;
    
    
    /** Creates new <code>SearchTask</code>.
     * @param nodes search starting points
     * @param searchGroup search group to search on.
     * @param na who could be notified
     */
    public SearchTask(Node[] nodes, SearchGroup searchGroup, ResultModel resultModel) {
        super(Task.EMPTY);

        this.nodes = nodes;
        this.searchGroup = searchGroup;
        this.resultModel = resultModel;
    }

    
    /** Runs the search task. */
    public void run() {
        try {
            // Set of search types to be used able to search on the same object type.
            if(searchGroup == null)
                return;

            searchGroup.addPropertyChangeListener(WeakListener.propertyChange(
                propListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(SearchGroup.PROP_FOUND.equals(evt.getPropertyName())) {
                            resultModel.acceptFoundObjects(new Object[] {evt.getNewValue()});
                        }
                    }
                }, searchGroup)
            );

            searchGroup.setSearchRootNodes(nodes);
            searchGroup.search();
        } finally {
            // Notifies search task has finished.
            notifyFinished();
        }
    }
    
    /** Stops this search task. */
    public void stop() {
        if(searchGroup != null) {
            searchGroup.stopSearch();
        }
    }

}
