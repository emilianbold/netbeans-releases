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
public class ResultModel implements TaskListener {

    /** maximum number of found objects */
    private static final int COUNT_LIMIT = 500;

    /** For debug purposes. */
    private static final ErrorManager em = ErrorManager.getDefault()
            .getInstance("org.netbeans.modules.search");                //NOI18N

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
    
    /**
     * flag - did number of found objects reach the limit?
     *
     * @see  #COUNT_LIMIT
     */
    private boolean limitReached;

    /** Set of listeners. */
    private HashSet listeners = new HashSet();

    /** Which search types creates were enabled for this model. */
    private List searchTypeList;

    /** Indicates if to use search displayer. */
    private boolean useDisp = false;
    
    /** Search displayer. */
    private SearchDisplayer disp = null;

    /** Search group this result shows search results for. */
    private SearchGroup searchGroup;

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;

    /** Creates new <code>ResultModel</code>. */
    public ResultModel(List searchTypeList, SearchGroup searchGroup) {
        this.searchTypeList = searchTypeList;
        this.searchGroup = searchGroup;
        
        EVENT = new ChangeEvent(this);

        root = new ResultRootNode();
    }

    /**
     * Clean the allocated resources. Do not rely on GC there as we are often referenced from
     * various objects (some VisualizerNode realy loves us). So keep leak as small as possible.
     * */
    public void close() {
        if (searchTask != null) {
            searchTask.removeTaskListener(this);
            searchTask = null;
        }

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

        // kill expensive children structure, again GC should kick it out but it does not
        root.clear();

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
        root.addFoundObject(object);
        limitReached = getFound() >= COUNT_LIMIT;
        return !limitReached;
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
        limitReached = false;
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
        task.removeTaskListener(this);
        root.setDisplayName(getRootDisplayName());
        fireChange();
    }

    /** Gets display name for root node. Utilitty method. */
    private String getRootDisplayName() {
        if (!isDone()) {
            return NbBundle.getMessage(ResultModel.class,
                                       "TXT_RootSearchedNodes",         //NOI18N
                                       Integer.toString(getFound()));
        }

        int found = getFound();

        return getRootDisplayNameHelp(found, limitReached, finishMessage);
    }
    
    /** Gets display name based on number of found nodes.
     * @param found number of found nodes. */
    private static String getRootDisplayNameHelp(int found,
                                                 boolean limitReached,
                                                 String finishMessage) {
        String orig;
        if (found == 0) {
            orig = NbBundle.getMessage(ResultModel.class,
                                       "TEXT_MSG_NO_NODE_FOUND");       //NOI18N
        } else if (found == 1) {
            orig = NbBundle.getMessage(ResultModel.class,
                                       "TEXT_MSG_FOUND_A_NODE");        //NOI18N
        } else if (limitReached) {
            assert found == COUNT_LIMIT;
            orig = NbBundle.getMessage(ResultModel.class,
                                       "TEXT_MSG_FOUND_X_NODES_LIMIT",  //NOI18N
                                       new Integer(COUNT_LIMIT));
        } else {
            orig = NbBundle.getMessage(ResultModel.class,
                                       "TEXT_MSG_FOUND_X_NODES",        //NOI18N
                                       new Integer(found));
        }

        if (finishMessage != null) {
            return orig + " (" + finishMessage + ")";                   //NOI18N
        }

        return orig;
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

    
    /** Search Result root node. May contain some statistic properties. */
    private class ResultRootNode extends AbstractNode { 

        /** Creates a new node with no content. */
        public ResultRootNode() {
            super(ResultModel.this.new ResultRootChildren());
            
            // displayed name indicates search in progress
            setDisplayName(NbBundle.getBundle(ResultModel.class).getString("TEXT_SEARCHING___"));
        }

        /**
         * Clear children in fast batch manner, does not touch model.
         * Model should be cleaned by client. This approach eliminates costly
         * event driven cleanup.
         */
        public void clear() {
            ResultRootChildren children = (ResultRootChildren)getChildren();
            Enumeration en = children.nodes();
            while (en.hasMoreElements()) {
                FoundNode node = (FoundNode) en.nextElement();
                node.originalDataObject.removePropertyChangeListener(node);
            }
            children.dispose();
        }

        /** Adds a found object to this root node. */
        public void addFoundObject(Object foundObject) {
            ((ResultRootChildren) getChildren()).addFoundObject(foundObject);
             
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
            return ((ResultRootChildren) getChildren()).size();
        }
        
        
        /** Gets icon. Overrides superclass method.
         * @return universal search icon. */
        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/search/res/find.gif"); // NOI18N
        }

        /** Gets opened icon. Overrides superclass method. */
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
    } // End of ResultRoorNode class.
    

    /** Children for result root node. */
    private class ResultRootChildren extends Children.Keys implements Runnable {

        /** Keys as found objects for the search. Must us esyncronized access to avoid connurent modifications.  */
        private Set keys;
        
        /** Comparator used for sorting children nodes. */
        private final Comparator comparator;

        /** Whether this node has sorted children. */
        private boolean sorted = false;

        // once keys get bigger than BATCH_LEVEL start batching expensive setKeys call
        private final int BATCH_LEVEL = 61;
        private final int BATCH_INTERVAL_MS = 759;
        private volatile RequestProcessor.Task batchSetKeys;
        private volatile boolean active = false;
        private int size = 0;

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
        
        int size() {
            return size;
        }
        
        /** Overrides superclass method. */
        protected void addNotify() {
            setKeys(Collections.EMPTY_SET);
            active = true;

            RequestProcessor.getDefault().post(new Runnable() {
                 public void run() {
                     setKeys(keys);
                 }
             });
        }

        /** Overrrides superclass method. */
        protected void removeNotify() {
            active = false;
            setKeys(Collections.EMPTY_SET);
        }

        /** Explicit garbage collect request. */
        void dispose() {
            synchronized(keys) {
                keys = Collections.EMPTY_SET;
            }
            removeNotify();
        }

        /** Creates nodes. */
        protected Node[] createNodes(Object key) {
            return new Node[] { createFoundNode(key)};
        }
        
        public void addFoundObject(Object foundObject) {
            if ( em.isLoggable (ErrorManager.INFORMATIONAL) ) {
                em.log ("addFoundObject: " + foundObject);
                em.notify (/*ErrorManager.INFORMATIONAL, */new RuntimeException ("++ addFoundObject"));
            }

            synchronized(keys) {
                keys.add(foundObject);
                size++;
            }

            if (size < BATCH_LEVEL) {
                synchronized(keys) {
                    setKeys (keys); //??? -> sort (sorted);
                }
            } else {
                batchSetKeys();  // 2 times faster for nb_all/core search for "void" case
            }
        }

        // do not update keys too often it's rather heavyweight operation
        // batch all request that come in BATCH_INTERVAL_MS into one real update
        private void batchSetKeys() {
            if (batchSetKeys == null) {
                batchSetKeys = RequestProcessor.getDefault().post(this, BATCH_INTERVAL_MS);
            }
        }


        public void removeFoundObject(Object foundObject) {
            if ( em.isLoggable (ErrorManager.INFORMATIONAL) ) {
                em.log ("removeFoundObjects: " + foundObject);
                em.notify (/*ErrorManager.INFORMATIONAL, */new RuntimeException ("-- removeFoundObjects"));
            }

            boolean removed = false;
            synchronized(keys) {
                removed = keys.remove (foundObject);
            }
            if ( removed ) {
                sort (sorted);
            }
        }
        
        /** Sorts/unsorts the children nodes. */
        public void sort(boolean sort) {
            Set newKeys;
            
            if(sort)
                newKeys = new TreeSet(comparator);
            else
                newKeys = new HashSet();

            synchronized(keys) {
                newKeys.addAll(keys);
            }

            setKeys(newKeys);

            sorted = sort;
        }

        /** Getter for sorted property. */
        public boolean isSorted() {
            return sorted;
        }

        // called from random request processor thread
        public void run() {
            batchSetKeys = null;
            synchronized(keys) {
                if (active) setKeys(keys);
            }
        }


    } // End of ResultRootChildren class.


    /** Creates result node with carefully cafted children */
    public FoundNode createFoundNode(Object foundObject) {
        Node node = searchGroup.getNodeForFoundObject(foundObject);
        SearchType[] types = searchGroup.getSearchTypes();

        // TODO need faster hasDetails check, without creating (and discarding) actual detail nodes
        boolean hasDetails = false;
        for (int i = 0; i < types.length; i++) {
            SearchType searchType = types[i];
            Node[] details = searchType.getDetails(node);
            if ((details != null) && details.length>0) {
                hasDetails = true;
                break;
            }
        }

        if (hasDetails) {
            return new FoundNode(node, new DetailChildren(node), foundObject);
        } else {
            return new FoundNode(node, org.openide.nodes.Children.LEAF, foundObject);
        }
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

    /** Details for found top level (file) nodes. */
    final class DetailChildren extends Children.Array {

        private final Node parent;

        DetailChildren(Node parent) {
            this.parent = parent;
        }

        // TODO why I must subclass Children.Array? I tried to subclass Children directly
        // and returned createNodes result from getNodes() but it did not work.
        // Why I complain, *Children.Array* is very memory expensive structure
        // other implementation (Keys, ...) are even worse :-(

        protected void addNotify() {
            add(createNodes(parent));
        }

        protected void removeNotify() {
            remove(getNodes());
        }

        protected Node[] createNodes(Object key) {
            Node node = (Node) key;           // the parent
            SearchType[] types = searchGroup.getSearchTypes();
            ArrayList nodes = new ArrayList(5);
            for (int i = 0; i < types.length; i++) {
                SearchType searchType = types[i];
                Node[] details = searchType.getDetails(node);
                if ((details != null) && details.length>0) {
                    for (int j = 0; j < details.length; j++) {
                        Node detail = details[j];
                        nodes.add(detail);
                    }
                }
            }

            return (Node[]) nodes.toArray(new Node[nodes.size()]);
        }

    }

    /** Node to show in result window. */
    final class FoundNode extends FilterNode implements PropertyChangeListener {
        
        /** Original data object if there is any. */
        private DataObject originalDataObject;

        /** Original found object. */
        private Object foundObject;


        /** Use {@link ResultModel#createFoundNode} instead. */
        FoundNode(Node node, org.openide.nodes.Children kids, Object foundObject) {

            // cut off children, we do not need to show them and they eat memory
            super(node, kids);
            
            this.foundObject = foundObject;
            
            this.originalDataObject = (DataObject)getOriginal().getCookie(DataObject.class);

            if (originalDataObject == null)
                return; 

            this.originalDataObject.addPropertyChangeListener(this);

            FileObject fileFolder = originalDataObject.getPrimaryFile().getParent();
            if(fileFolder != null) {
                disableDelegation(DELEGATE_SET_SHORT_DESCRIPTION |
                                   DELEGATE_GET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION);
                setShortDescription ("");  // NOI18N
            }

        }


        public String getDisplayName() {
            FileObject fileFolder = originalDataObject.getPrimaryFile().getParent();
            if(fileFolder != null) {
                String hint = fileFolder.getPath();
                String orig = getOriginal().getDisplayName();
                return orig + " " + hint;
            } else {
                return getOriginal().getDisplayName();
            }
        }

        public String getHtmlDisplayName() {
            FileObject fileFolder = originalDataObject.getPrimaryFile().getParent();
            if(fileFolder != null) {
                String hint = FileUtil.getFileDisplayName(fileFolder);
                String orig = getOriginal().getDisplayName();
                try {
                    return "<html>" + orig + " <font color='!controlShadow'>" + XMLUtil.toElementContent(hint);  // NOI18N
                } catch (CharConversionException e) {
                    return null;
                }
            } else {
                return getOriginal().getHtmlDisplayName();
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

        /** Action performer. Removes node from search result window (and model). <em>Note</em>: it doesn't delete the original. */
        public void removeFromSearch() { 
            if(originalDataObject != null)
                originalDataObject.removePropertyChangeListener(this);

            ResultModel.this.root.removeFoundObject(foundObject);
        }

        /** Destroys node and it's file. Overrides superclass method. */
        public void destroy() throws IOException {
            super.destroy();

            if(originalDataObject != null)
                originalDataObject.removePropertyChangeListener(this);

            ResultModel.this.root.removeFoundObject(foundObject);
        }



        /** Implements <code>PropertyChangeListener</code> litening or originalDataObject. */
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

        /** Optimalized JavaNode.getIcon that starts parser! */
        public Image getIcon(int type) {
            if (originalDataObject.getPrimaryFile().hasExt("java") ) { // NOI18N
                // take icon from loader, it should be similar to dataobject's one
                try {
                    BeanInfo info = Utilities.getBeanInfo(originalDataObject.getLoader().getClass());
                    return info.getIcon(type);
                } catch (IntrospectionException e) {
                    return AbstractNode.EMPTY.getIcon(type);
                }
            } else {
                return super.getIcon(type);
            }
        }

        /** Optimalized JavaNode.getIcon that starts parser! */
        public Image getOpenedIcon(int type) {
            if (originalDataObject.getPrimaryFile().hasExt("java") ) { // NOI18N
                // take icon from loader, it should be similar to dataobject's one
                try {
                    BeanInfo info = Utilities.getBeanInfo(originalDataObject.getLoader().getClass());
                    return info.getIcon(type);
                } catch (IntrospectionException e) {
                    return AbstractNode.EMPTY.getIcon(type);
                }
            } else {
                return super.getOpenedIcon(type);
            }
        }
    } // End of FoundNode class.
   
}
