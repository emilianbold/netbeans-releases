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


import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.search.res.Res;
import org.netbeans.modules.search.types.DetailHandler;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.TaskListener;
import org.openidex.search.SearchTask;
import org.openidex.search.SearchType;


/**
 * Holds search result data.
 * 
 * @author  Petr Kuzel
 */
public class ResultModel implements NodeAcceptor, TaskListener {
    
    /** ChangeEvent object being used to notify about the search task finish. */
    private final ChangeEvent EVENT;

    /** Property name of sorted proerty. */
    public final String PROP_SORTED = "sorted"; // NOI18N

    /** Node representing root of found nodes.
     * Its children hold all found nodes. */
    private ResultRootNode root;

    /** Whether the nodes are sorted. */
    private boolean sorted;

    /** Unsorted list of found nodes. */
    private ArrayList unsortedNodes;
    
    /** Sorted list of found nodes. */
    private ArrayList sortedNodes;

    /** Search task. */
    private SearchTask task = null;

    /** Search state field. */
    private boolean done = false;

    /** Set of listeners. */
    private HashSet listeners = new HashSet();

    /** Which criteria have produced this result. */
    private CriteriaModel criteria;

    /** Indicates if to use search displayer. */
    private boolean useDisp = false;
    
    /** Search displayer. */
    private SearchDisplayer disp = null;

    /** Porperty change support. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    /** Creates new <code>ResultModel</code>. */
    public ResultModel(CriteriaModel model) {
        EVENT = new ChangeEvent(this);

        sorted = false;
        unsortedNodes = new ArrayList(100);
        sortedNodes = new ArrayList(100);
        root = new ResultRootNode();
        criteria = model;
    }

    
    /** Accept nodes. Some nodes were found by engine. */
    public synchronized boolean acceptNodes(Node[] nodes) {

        root.getChildren().add(nodes);
        for (int i=0; i < nodes.length; i++) {
            unsortedNodes.add(nodes[i]);
            sortedNodes.add(nodes[i]);
        }

        if (useDisp && disp != null) {
            disp.acceptNodes(nodes);
        }

        return true;
    }

    /** Send search details to output window. */
    public void fillOutput () {
        if (useDisp) {
            disp.resetOutput();
        } else {
            disp = new SearchDisplayer();
            useDisp = true;
        }

        disp.acceptNodes(root.getChildren().getNodes());
    }

    /** Does used criteria allow filling output window?
     * Currently it checks for presence of DetailHandler.
     * @return true it it can be used. */
    synchronized boolean canFillOutput() {

        SearchType[] crs = getCriteriaModel().getCustomizedCriteria();

        for (int i=0; i < crs.length; i++) {

            Class[] detCls = crs[i].getDetailClasses();
            // We support just AND critera relation
            // so if one of them support a detail then
            // all search results (matched nodes) do.
            if (detCls == null) continue;
            for (int j=0; j < detCls.length; j++)
                if (DetailHandler.class.isAssignableFrom(detCls[j]))
                    return true;
        }
        return false;
    }

    /** Is search engine still running? */
    public boolean isDone() {
        return done;
    }

    /** Sets search task. */
    public void setTask (SearchTask task) {
        this.task = task;
        this.task.addTaskListener(this);
    }

    /** Gets result root node.
     * @return root node of result. */
    public Node getRoot() {
        return root;
    }

    /** Gets criteria model.
     * @return criteria model that produces these results. */
    public CriteriaModel getCriteriaModel() {
        return criteria;
    }

    /** Gets number of found nodes. */
    public int getFound() {
        return unsortedNodes.size();
    }

    /** Whether found nodes are sorted. */
    public boolean isSorted() {
        return sorted;
    }

    /** Sort or unsort found nodes. (Display name is used for sorting.)
     * A new root node is created. Should not be called until search is finished.
     * @return the new root node with (un)sorted subnodes.
     */
    public Node sortNodes(boolean sort) {
        if(sort == sorted)
            return root;

        root.setDisplayName(getRootDisplayName());
        
        // copy one array of nodes to another array of nodes which is
        // of type Node[] ...
        Object[] objects;
        Node[] sortedN;
        Node[] unsortedN;

        objects = sortedNodes.toArray();
        sortedN = new Node[objects.length];
        
        for (int i=0; i < objects.length; i++)
            sortedN[i] = (Node)objects[i];

        objects = unsortedNodes.toArray();
        unsortedN = new Node[objects.length];
        
        for (int i=0; i < objects.length; i++)
            unsortedN[i] = (Node)objects[i];

        if (sort) {
            root.getChildren().remove(unsortedN);
            root.getChildren().add(sortedN);
        }
        else {
            root.getChildren().remove(sortedN);
            root.getChildren().add(unsortedN);
        }

        sorted = sort;
        propertyChangeSupport.firePropertyChange(PROP_SORTED, !sorted, sorted);

        return root;
    }


    /** Search task finished. Notify all listeners. */
    public void taskFinished(final org.openide.util.Task task) {
        done = true;
        root.setDisplayName(getRootDisplayName());
        Collections.sort(sortedNodes, NodeNameComparator.getComparator());
        fireChange();
    }

    /** Gets display name for root node. Utilitty method. */
    private String getRootDisplayName() {
        if (!isDone()) {
            return Res.text("SEARCHING___"); // NOI18N
        }

        int found = getFound();

        if (found == 1) {
            return MessageFormat.format(Res.text("MSG_FOUND_A_NODE"), // NOI18N
                                        new Object[] { new Integer(found) } );
        } else if (found > 1) {
            return MessageFormat.format(Res.text("MSG_FOUND_X_NODES"), // NOI18N
                                        new Object[] { new Integer(found) } );
        } else { // <1
            return Res.text("MSG_NO_NODE_FOUND"); // NOI18N
        }
    }

    /** Stops serach task. */
    public void stop() {
        if (task != null) task.stop();
    }

    /** Adds change listsner. */
    public void addChangeListener(ChangeListener lis) {
        listeners.add(lis);
    }

    /** Removes change listener. */
    public void removeChangeListener(ChangeListener lis) {
        listeners.remove(lis);
    }

    /** Fires change event to all listeners. */
    private void fireChange() {
        Iterator it = listeners.iterator();

        while(it.hasNext()) {
            ChangeListener next = (ChangeListener) it.next();
            next.stateChanged(EVENT);
        }
    }

    /** Adds a <code>PropertyChangeListener</code> to the listener list.
     * @param l The listener to add. */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /** Removes a <code>PropertyChangeListener</code> from the listener list.
     * @param l The listener to remove. */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /** Search Result root node. May contain some statistic properties. */
    private static class ResultRootNode extends AbstractNode {
        /** Creates a new node with no content. */
        public ResultRootNode() {
            super(new Children.Array());

            // displayed name indicates search in progress
            setDisplayName(Res.text("SEARCHING___")); // NOI18N
        }

        /** Creates a new node with subnodes. */
        public ResultRootNode(Children ch, String dispName) {
            super(ch);
            setDisplayName(dispName);
        }

        
        /** Gets icon. Overrides superclass method.
         * @return universal search icon. */
        public Image getIcon(int type) {
            return Res.image("SEARCH"); // NOI18N
        }

        /** Gets opened icon. Overrides superclass method. */
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    } // End of ResultRoorNode class.
    

    /** Node name comparator, */
    private static class NodeNameComparator implements Comparator {
        /** Comparator. */
        private static Comparator comparator = null;

        /** Compare two nodes according to their display names.
         * Implements <code>Comparator</code> interface method. */
        public int compare(Object o1,Object o2) {
            return ((Node)o1).getDisplayName().compareTo(((Node)o2).getDisplayName());
        }

        /** Implements <code>Comparator</code> interface method. */
        public boolean equals(Object obj) {
            return obj instanceof NodeNameComparator;
        }

        /** Gets the instance pf comparator. 
         * @return the instance of <code>NodeNameComparator</code> */
        public static Comparator getComparator() {
            if (comparator == null)
                comparator = new NodeNameComparator();
            return comparator;
        }
    } // End of NodeNameComparator class.
    
}
