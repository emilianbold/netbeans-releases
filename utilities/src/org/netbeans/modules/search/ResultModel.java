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
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ImageIcon;

import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.WeakListener;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Holds search result data.
 * 
 * @author  Petr Kuzel
 */
public class ResultModel implements TaskListener {

    /** ChangeEvent object being used to notify about the search task finish. */
    private final ChangeEvent EVENT;

    /** Property name of sorted proerty. */
    public final String PROP_SORTED = "sorted"; // NOI18N

    /** Node representing root of found nodes.
     * Its children hold all found nodes. */
    private ResultRootNode root;

    /** Search task. */
    private SearchTask searchTask = null;

    /** Search state field. */
    private boolean done = false;

    /** Set of listeners. */
    private HashSet listeners = new HashSet();

    /** Which search types creates were enabled for this model. */
    private List searchTypeList;

    /** Indicates if to use search displayer. */
    private boolean useDisp = false;
    
    /** Search displayer. */
    private SearchDisplayer disp = null;

    /** Porperty change support. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Search group this result shows search results for. */
    private SearchGroup searchGroup;

    /** Listener on search group to reflect dynamically changes on search objects. */
    private PropertyChangeListener propListener;
    

    /** Creates new <code>ResultModel</code>. */
    public ResultModel(List searchTypeList, SearchGroup searchGroup) {
        this.searchTypeList = searchTypeList;
        this.searchGroup = searchGroup;
        
        EVENT = new ChangeEvent(this);

        root = new ResultRootNode();
    }

    
    /** Accept nodes. Some nodes were found by engine. */
    public synchronized boolean acceptFoundObjects(Object[] foundObjects) {
        root.addFoundObjects(foundObjects);
        
/*        if(useDisp && disp != null) {
            disp.acceptNodes(nodes);
        }
 */ // PENDING important

        if(!done) {
            root.setDisplayName(MessageFormat.format(
                NbBundle.getBundle(ResultModel.class).getString("TXT_RootSearchedNodes"),
                new Object[] {Integer.toString(getFound())}
            ));
        }

        return true;
    }

    /** Getter for search group property. */
    public SearchGroup getSearchGroup() {
        return searchGroup;
    }

    /** Send search details to output window. */
    public void fillOutput() {
        if(useDisp) {
            disp.resetOutput();
        } else {
            disp = new SearchDisplayer();
            useDisp = true;
        }
        
        Node[] nodes = root.getChildren().getNodes();
        
        SearchType[] searchTypes = searchGroup.getSearchTypes();        
        
        List detailNodes = new ArrayList(nodes.length * searchTypes.length * 3);
        
        for(int i = 0; i < searchTypes.length; i++) {
            for(int j = 0; j < nodes.length; j++) {
                Node[] details = searchTypes[i].getDetails(nodes[j]);
                
                if(details != null)
                    detailNodes.addAll(Arrays.asList(details));
            }
        }
        
        disp.acceptNodes((Node[])detailNodes.toArray(new Node[detailNodes.size()]));
    }

    /** Is search engine still running? */
    public boolean isDone() {
        return done;
    }

    /** Sets search task. */
    public void setTask(SearchTask searchTask) {
        this.searchTask = searchTask;
        this.searchTask.addTaskListener(this);
    }

    /** Gets result root node.
     * @return root node of result. */
    public Node getRoot() {
        return root;
    }

    /** Gets all search types, all enabled not only customized ones. */
    public List getEnabledSearchTypes() {
        return searchTypeList;
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


    /** Search task finished. Notify all listeners. Implements <code>TasListener</code>
     * interface. */
    public void taskFinished(Task task) {
        done = true;
        root.setDisplayName(getRootDisplayName());
        fireChange();
        
        registerListening();
    }

    /** Registers listening on search group to reflect dynamically changes
     * made on search/found objects to reflect result of the original search. */
    private void registerListening() {
        searchGroup.addPropertyChangeListener(WeakListener.propertyChange(propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(SearchGroup.PROP_RESULT.equals(evt.getPropertyName())) {
                    if(evt.getNewValue() == null) {
                        // Old object to remove.
                        root.removeFoundObject(evt.getOldValue());
                    } else {
                        // New object to add.
                        root.addFoundObjects(new Object[] {evt.getNewValue()});
                    }
                }
            }
        }, searchGroup));
    }

    /** Gets display name for root node. Utilitty method. */
    private String getRootDisplayName() {
        if (!isDone()) {
            return NbBundle.getBundle(ResultModel.class).getString("TEXT_SEARCHING___");
        }

        int found = getFound();

        return getRootDisplayNameHelp(found);
    }
    
    /** Gets display name based on number of found nodes.
     * @param found number of found nodes. */
    private static String getRootDisplayNameHelp(int found) {
        if (found == 1) {
            return MessageFormat.format(NbBundle.getBundle(ResultModel.class).getString("TEXT_MSG_FOUND_A_NODE"), // NOI18N
                    new Object[] { new Integer(found) } );
        } else if (found > 1) {
            return MessageFormat.format(NbBundle.getBundle(ResultModel.class).getString("TEXT_MSG_FOUND_X_NODES"),
                    new Object[] { new Integer(found) } );
        } else { // <1
            return NbBundle.getBundle(ResultModel.class).getString("TEXT_MSG_NO_NODE_FOUND");
        }
    }

    /** Stops search task. */
    public void stop() {
        // PENDING important, stop search form here.
        if(searchTask != null)
            searchTask.stop();
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
    private class ResultRootNode extends AbstractNode { 

        /** Creates a new node with no content. */
        public ResultRootNode() {
            super(ResultModel.this.new ResultRootChildren());
            
            // displayed name indicates search in progress
            setDisplayName(NbBundle.getBundle(ResultModel.class).getString("TEXT_SEARCHING___"));
        }


        /** Adds founds objects to root node. */
        public void addFoundObjects(Object[] foundObjects) {
            ((ResultRootChildren)getChildren()).addFoundObjects(foundObjects);
             
             ResultModel.this.root.setDisplayName(ResultModel.this.getRootDisplayName());
        }
        
        /** Removes found node from root node. */
        public void removeFoundObject(Object foundObject) {
            ((ResultRootChildren)getChildren()).removeFoundObject(foundObject);
             
             ResultModel.this.root.setDisplayName(ResultModel.this.getRootDisplayName());
        }
        

        /** Sorts/unsorts the children nodes. */
        public void sort(boolean sort) {
            ((ResultRootChildren)getChildren()).sort(sort);
        }

        /** Getter for sorted property. */
        public boolean isSorted() {
            return ((ResultRootChildren)getChildren()).isSorted();
        }
        

        /** Gets number of found nodes. */
        public int getNumberOfFoundNodes() {
            return getChildren().getNodes().length;
        }
        
        
        /** Gets icon. Overrides superclass method.
         * @return universal search icon. */
        public Image getIcon(int type) {
            return new ImageIcon(getClass().getResource("/org/netbeans/modules/search/res/find.gif")).getImage();
        }

        /** Gets opened icon. Overrides superclass method. */
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
    } // End of ResultRoorNode class.
    

    /** Children for result root node. */
    private class ResultRootChildren extends Children.Keys {

        /** Keys as found objects for the search. */
        private Set keys;
        
        /** Comparator used for sorting children nodes. */
        private final Comparator comparator;

        /** Whether this node has sorted children. */
        private boolean sorted = false;
        
        
        /** Constructor. */
        public ResultRootChildren() {
            keys = searchGroup.getResultObjects();
            
            this.comparator = new Comparator() {
                
                public int compare(Object o1, Object o2) {
                    if(o1 == o2)
                        return 0;
                    
                    if(o1 == null)
                        return 1;
                    
                    if(o2 == null)
                        return -1;
                    
                    Node node1 = searchGroup.getNodeForFoundObject(o1);
                    Node node2 = searchGroup.getNodeForFoundObject(o2);
                    
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
        }
        
        
        /** Overrides superclass method. */
        protected void addNotify() {
            setKeys(Collections.EMPTY_SET);
            
            RequestProcessor.postRequest(new Runnable() {
                 public void run() {
                     setKeys(keys);
                 }
             });
        }

        /** Overrrides superclass method. */
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        /** Creates nodes. */
        protected Node[] createNodes(Object key) {
            return new Node[] { new FoundNode(key)};
        }
        
        public void addFoundObjects(Object[] foundObjects) {
            keys.addAll(Arrays.asList(foundObjects));
            setKeys(keys);
        }

        public void removeFoundObject(Object foundObject) {
            if(keys.remove(foundObject))
                setKeys(keys);
        }
        
        /** Sorts/unsorts the children nodes. */
        public void sort(boolean sort) {
            Set newKeys;
            
            if(sort)
                newKeys = new TreeSet(comparator);
            else
                newKeys = new HashSet();
            
            newKeys.addAll(keys);
            
            setKeys(newKeys);

            sorted = sort;
        }

        /** Getter for sorted property. */
        public boolean isSorted() {
            return sorted;
        }
        
    } // End of ResultRootChildren class.


    /** Node to show in result window. */
    class FoundNode extends FilterNode implements PropertyChangeListener {
        
        /** Original data object if there is any. */
        private DataObject originalDataObject;

        /** Original found object. */
        private Object foundObject;
        

        /** Constructs node. */
        public FoundNode(Object foundObject) {
            super(ResultModel.this.searchGroup.getNodeForFoundObject(foundObject));
            
            this.foundObject = foundObject;
            
            this.originalDataObject = (DataObject)getOriginal().getCookie(DataObject.class);

            if (originalDataObject == null)
                return; 

            this.originalDataObject.addPropertyChangeListener(this);

            FileObject fileFolder = originalDataObject.getPrimaryFile().getParent();
            if(fileFolder != null) {
                String packageName = fileFolder.getPackageName ('.');
                String hint;
                if(packageName.equals("")) // NOI18N
                    hint = NbBundle.getBundle(ResultModel.class).getString("HINT_result_default_package"); // NOI18N                    
                else
                    hint = MessageFormat.format(NbBundle.getBundle(ResultModel.class).getString("HINT_result_package"), // NOI18N
                         new Object[] { packageName });

                disableDelegation(DELEGATE_SET_SHORT_DESCRIPTION |
                                   DELEGATE_GET_SHORT_DESCRIPTION);
                setShortDescription (hint);
            }

        }


        /** Gets system actions for this node. Overrides superclass method. 
         * Adds <code>RemoveFromSearchAction<code>. */
        public SystemAction[] getActions() {
            List originalActions = new ArrayList(Arrays.asList(super.getActions()));

            int deleteIndex = originalActions.indexOf(SystemAction.get(DeleteAction.class));

            SystemAction removeFromSearch = SystemAction.get(RemoveFromSearchAction.class);

            if(deleteIndex != -1) {
                originalActions.add(deleteIndex, removeFromSearch);
            } else {
                originalActions.add(null);
                originalActions.add(removeFromSearch);
            }

            return (SystemAction[])originalActions.toArray(new SystemAction[originalActions.size()]);
        }

        /** Removes node from serach result window. <em>Note</em>: it doesn't delete the original. */
        public void removeFromSearch() { 
            if(originalDataObject != null)
                originalDataObject.removePropertyChangeListener(this);

            ResultModel.this.root.removeFoundObject(foundObject);
            
        }

        /** Destroys node. Overrides superclass method. */
        public void destroy() throws IOException {
            super.destroy();

            if(originalDataObject != null)
                originalDataObject.removePropertyChangeListener(this);

            ResultModel.this.root.removeFoundObject(foundObject);
        }


        /** Implements <code>PropertyChangeListener</code>. */
        public void propertyChange(PropertyChangeEvent evt) {
            if(DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                // data object might be deleted
                if (!originalDataObject.isValid()) { // link becomes invalid
                    if(originalDataObject != null)
                        originalDataObject.removePropertyChangeListener(this);

                    ResultModel.this.root.removeFoundObject(foundObject);
                }
            }
        }

    } // End of FoundNode class.
   
}
