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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.search.res.Res;
import org.netbeans.modules.search.scanners.RepositoryScanner;
import org.netbeans.modules.search.types.DetailHandler;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openidex.search.DetailCookie;
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

        root = new ResultRootNode();
        criteria = model;
    }

    
    /** Accept nodes. Some nodes were found by engine. */
    public synchronized boolean acceptNodes(Node[] nodes) {
        root.addNodes(nodes);
        
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
        return root == null ? 0 : root.getNumberOfFoundNodes();
    }

    /** Whether found nodes are sorted. */
    public boolean isSorted() {
        return root == null ? false : root.isSorted();
    }

    /** Sort or unsort found nodes. (Display name is used for sorting.)
     * A new root node is created. Should not be called until search is finished.
     * @return the new root node with (un)sorted subnodes.
     */
    public Node sortNodes(boolean sort) {
        boolean sorted = root.isSorted();
        if(sort == sorted)
            return root;

        root.setDisplayName(getRootDisplayName());

        root.sort(sort);
        
        return root;
    }


    /** Search task finished. Notify all listeners. */
    public void taskFinished(final org.openide.util.Task task) {
        done = true;
        root.setDisplayName(getRootDisplayName());
        fireChange();
    }

    /** Gets display name for root node. Utilitty method. */
    private String getRootDisplayName() {
        if (!isDone()) {
            return Res.text("SEARCHING___"); // NOI18N
        }

        int found = getFound();

        return getRootDisplayNameHelp(found);
    }
    
    /** Gets display name based on number of found nodes.
     * @param found number of found nodes. */
    private static String getRootDisplayNameHelp(int found) {
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
    private static class ResultRootNode extends AbstractNode implements PropertyChangeListener {

        /** Maps keys to nodes. The keys are names of fileobject for which nodes are found or
         * if there was not such one the node itself. The nodes are <code>FoundNode</code>'s. */
        private final Map keys = new Hashtable();

        /** Comparator used for sorting children nodes. */
        private final Comparator comparator;
        
        /** Whether this node has sorted children. */
        private boolean sorted = false;

        
        /** Creates a new node with no content. */
        public ResultRootNode() {
            super(new ResultRootChildren());

            this.comparator = new Comparator() {
                public boolean equals(Object o) {
                    if(this == o)
                        return true;
                    
                    return false;
                }
                
                public int compare(Object o1, Object o2) {
                    if(o1 == o2)
                        return 0;
                    
                    if(o1 == null)
                        return 1;
                    
                    if(o2 == null)
                        return -1;
                    
                    Node node1 = (Node)keys.get(o1);
                    Node node2 = (Node)keys.get(o2);
                    
                    if(node1 == node2)
                        return 0;
                    
                    if(node1 == null)
                        return 1;
                    
                    if(node2 == null)
                        return -1;

                    int result = node1.getDisplayName().compareTo(node2.getDisplayName());

                    // Can't return that two different nodes are equal even their names are same.
                    return result == 0 ? -1 : result;
                }

            };
            
            // displayed name indicates search in progress
            setDisplayName(Res.text("SEARCHING___")); // NOI18N
        }


        /** Adds found nodes. */
        public void addNodes(Node[] nodes) {
            for(int i = 0; i < nodes.length; i++) {
                if(nodes[i] == null || keys.values().contains(nodes[i]))
                    continue;
                
                if(nodes[i] instanceof RepositoryScanner.FoundNode) {
                    String keyName = ((RepositoryScanner.FoundNode)nodes[i]).getOriginalFileObjectName();
                    if(keyName != null) {
                        keys.put(keyName, nodes[i]);
                        
                        continue;
                    }
                    
                    nodes[i].addPropertyChangeListener(this);
                }
                
                keys.put(nodes[i], nodes[i]);
            }
            
            updateChildren();
        }
        
        /** Gets node for strings key. */
        public Node getNodeForKey(String key) {
            if(!keys.containsKey(key))
                return null;
            
            FileObject fileObject = TopManager.getDefault().getRepository().findResource(key);
            
            if(fileObject == null) {
                keys.remove(key);
                setDisplayName(getRootDisplayNameHelp(getNumberOfFoundNodes()));

                return null;
            }
            
            try {
                DataObject dataObject = DataObject.find(fileObject);
                
                Node originalNode = dataObject.getNodeDelegate(); 

                Node oldNode = (Node)keys.get(key);
                oldNode.removePropertyChangeListener(this);
                
                // return new refreshed node with the original detail cookie.
                Node newFoundNode = new RepositoryScanner.FoundNode(originalNode, (DetailCookie)oldNode.getCookie(DetailCookie.class));
                newFoundNode.addPropertyChangeListener(this);
                
                keys.put(key, newFoundNode);
                
                return newFoundNode;
            } catch(DataObjectNotFoundException dnfe) {
                keys.remove(key);
                setDisplayName(getRootDisplayNameHelp(getNumberOfFoundNodes()));
                
                return null;
            }
        }
        
        /** Implements <code>PropertyChangeListener</code>. */
        public void propertyChange(PropertyChangeEvent evt) {
            if(RepositoryScanner.PROP_NODE_VALID.equals(evt.getPropertyName())) {
                String name = ((RepositoryScanner.FoundNode)evt.getOldValue()).getOriginalFileObjectName();
                
                if(name != null)
                    updateChild(name);
            } else if(RepositoryScanner.PROP_NODE_DESTROYED.equals(evt.getPropertyName())) {
                keys.values().remove(evt.getOldValue());
                setDisplayName(getRootDisplayNameHelp(getNumberOfFoundNodes()));
                
                String name = ((RepositoryScanner.FoundNode)evt.getOldValue()).getOriginalFileObjectName();
                
                if(name != null)
                    updateChild(name);
            }
        }
        
        /** Sorts/unsorts the children nodes. */
        public void sort(boolean sort) {
            Set newKeys;
            
            if(sort)
                newKeys = new TreeSet(comparator);
            else
                newKeys = new HashSet();
            
            newKeys.addAll(keys.keySet());
            
            updateChildren(newKeys);

            sorted = sort;
        }

        /** Getter for sorted property. */
        public boolean isSorted() {
            return sorted;
        }

        /** Gets number of found nodes. */
        public int getNumberOfFoundNodes() {
            return keys.size();
        }
        
        /** Updates one child. */
        private void updateChild(String key) {
            ((ResultRootChildren)getChildren()).update(key);
        }
        
        /** Updates all children. */
        private void updateChildren() {
            ((ResultRootChildren)getChildren()).update(keys.keySet());
        }
        
        /** Updates all children by new keys. */
        private void updateChildren(Set newKeys) {
            ((ResultRootChildren)getChildren()).update(newKeys);
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
    

    /** Children for result root node. */
    private static class ResultRootChildren extends Children.Keys {
        /** Overrides superclass method. */
        protected void addNotify() {
            setKeys(Collections.EMPTY_SET);
            RequestProcessor.postRequest(new Runnable() {
                 public void run() {
                     ResultRootNode root = (ResultRootNode)getNode();
                     
                     if(root != null)
                        root.updateChildren();
                 }
             });
        }

        /** Overrrides superclass method. */
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        /** Creates nodes. */
        protected Node[] createNodes(Object key) {
            if(key instanceof String) {
                ResultRootNode root = (ResultRootNode)getNode();
                
                if(root == null)
                    return new Node[0];
                
                Node node = root.getNodeForKey((String)key);
                
                return node == null ? new Node[0] : new Node[] {node};
            } else if(key instanceof Node)
                return new Node[] {(Node)key};
            else
                return new Node[0];
        }
        
        /** Updates key. */
        public void update(String key) {
            refreshKey(key);
        }
        
        /** Updates all keys from set. */
        public void update(Set keys) {
            setKeys(keys);
        }
        
    } // End of ResultRootChildren class.
    
}
