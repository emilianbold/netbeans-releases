/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.favorites;


import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Stack;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Special class for projects tab in main explorer */
public class Tab extends TopComponent
implements OperationListener, Runnable, ExplorerManager.Provider {
    static final long serialVersionUID =-8178367548546385799L;

    /** data object which should be selected in EQ; synch array when accessing */
    private static final DataObject[] needToSelect = new DataObject[1];


    private static transient Tab DEFAULT;

    /** composited view */
    transient protected TreeView view;
    /** listeners to the root context and IDE settings */
    transient private PropertyChangeListener weakRcL;
    transient private NodeListener weakNRcL;

    transient private NodeListener rcListener;
    /** validity flag */
    transient private boolean valid = true;
    
    private ExplorerManager manager;
    
    /** Creates */
    private Tab() {
        this.manager = new ExplorerManager();
        
        ActionMap map = this.getActionMap ();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete (manager, true)); // or false

        // following line tells the top component which lookup should be associated with it
        associateLookup (ExplorerUtils.createLookup (manager, map));
        
        
        DataLoaderPool pool = (DataLoaderPool)Lookup.getDefault ().lookup (DataLoaderPool.class);
        pool.addOperationListener ((OperationListener)org.openide.util.WeakListeners.create (
            OperationListener.class, this, pool
        ));
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /** Return preferred ID */
    protected String preferredID () {
        return "favorites"; //NOI18N
    }
    
    /** Initialize visual content of component */
    protected void componentShowing () {
        super.componentShowing ();

        if (view == null) {
            view = initGui ();

            view.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(
                    Tab.class, "ACSN_ExplorerBeanTree"));
            view.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                    Tab.class, "ACSD_ExplorerBeanTree"));
        }
        
        run();
    }

    /** Initializes gui of this component. Subclasses can override
    * this method to install their own gui.
    * @return Tree view that will serve as main view for this explorer.
    */
    protected TreeView initGui () {
        TreeView view = new BeanTreeView();
        view.setRootVisible (true);
        setLayout(new BorderLayout());
        add (view);
        return view;
    }

    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
    /** Transfer focus to view. */
    public void requestFocus () {
        super.requestFocus();
        if (view != null) {
            view.requestFocus();
        }
    }

    /** Transfer focus to view. */
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        if (view != null) {
            return view.requestFocusInWindow();
        } else {
            return false;
        }
    }

    /** Sets new root context to view. Name, icon, tooltip
    * of this top component will be updated properly */
    public void setRootContext (Node rc) {
        Node oldRC = getExplorerManager().getRootContext();
        // remove old listener, if possible
        if (weakRcL != null) {
            oldRC.removePropertyChangeListener(weakRcL);
        }
        if (weakNRcL != null) {
            oldRC.removeNodeListener(weakNRcL);
        }
        getExplorerManager().setRootContext(rc);
        initializeWithRootContext(rc);
    }

    public Node getRootContext () {
        return getExplorerManager().getRootContext();
    }

    /** Implementation of DeferredPerformer.DeferredCommand
    * Performs initialization of component's attributes
    * after deserialization (component's name, icon etc, 
    * according to the root context) */
    public void run() {
        if (!valid) {
            valid = true;
            validateRootContext();
        }
    }

    // Bugfix #5891 04 Sep 2001 by Jiri Rechtacek
    // the title is derived from the root context
    // it isn't changed by a selected node in the tree
    /** Called when the explored context changes.
    * Overriden - we don't want title to change in this style.
    */
    protected void updateTitle () {
        // set name by the root context
        setName(getExplorerManager ().getRootContext().getDisplayName());
    }

    private NodeListener rcListener () {
        if (rcListener == null) {
            rcListener = new RootContextListener();
        }
        return rcListener;
    }

    /** Initialize this top component properly with information
    * obtained from specified root context node */
    private void initializeWithRootContext (Node rc) {
        // update TC's attributes
        setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
        setToolTipText(rc.getShortDescription());
        // bugfix #15136
        setName(rc.getDisplayName());
        updateTitle();
        // attach listener
        if (weakRcL == null) {
            weakRcL = org.openide.util.WeakListeners.propertyChange(
                rcListener(), rc
            );
        }
        rc.addPropertyChangeListener(weakRcL);

        if (weakNRcL == null) {
            weakNRcL = org.openide.nodes.NodeOp.weakNodeListener (
                rcListener(), rc
            );
        }
        rc.addNodeListener(weakNRcL);
    }

    // put a request for later validation
    // we must do this here, because of ExplorerManager's deserialization.
    // Root context of ExplorerManager is validated AFTER all other
    // deserialization, so we must wait for it
    protected final void scheduleValidation() {
        valid = false;
//            WindowManagerImpl.deferredPerformer().putRequest(this, null);
        SwingUtilities.invokeLater(this); // TEMP
    }

    /* Updated accessible name of the tree view */
    public void setName(String name) {
        super.setName(name);
        if (view != null) {
            view.getAccessibleContext().setAccessibleName(name);
        }
    }

    /* Updated accessible description of the tree view */
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        if (view != null) {
            view.getAccessibleContext().setAccessibleDescription(text);
        }
    }

    /** Multi - purpose listener, listens to: <br>
    * 1) Changes of name, icon, short description of root context.
    * 2) Changes of IDE settings, namely delete confirmation settings */
    private final class RootContextListener implements NodeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            Object source = evt.getSource();

            // root context node change
            Node n = (Node)source;
            if (Node.PROP_DISPLAY_NAME.equals(propName) ||
                    Node.PROP_NAME.equals(propName)) {
                setName(n.getDisplayName());
            } else if (Node.PROP_ICON.equals(propName)) {
                setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            } else if (Node.PROP_SHORT_DESCRIPTION.equals(propName)) {
                setToolTipText(n.getShortDescription());
            }
        }

        public void nodeDestroyed(org.openide.nodes.NodeEvent nodeEvent) {
            //Tab.this.setCloseOperation(TopComponent.CLOSE_EACH);
            Tab.this.close();
        }            

        public void childrenRemoved(org.openide.nodes.NodeMemberEvent e) {}
        public void childrenReordered(org.openide.nodes.NodeReorderEvent e) {}
        public void childrenAdded(org.openide.nodes.NodeMemberEvent e) {}

    } // end of RootContextListener inner class

    /** Gets default instance. Don't use directly, it reserved for deserialization routines only,
     * e.g. '.settings' file in xml layer, otherwise you can get non-deserialized instance. */
    public static synchronized Tab getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Tab();
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            DEFAULT.scheduleValidation();
        }

        return DEFAULT;
    }

    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized Tab findDefault() {
        if(DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("favorites"); // NOI18N
            if(DEFAULT == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find project component for its ID. Returned " + tc)); // NOI18N
                DEFAULT = new Tab();
                // XXX Look into getDefault method.
                DEFAULT.scheduleValidation();
            }
        }

        return DEFAULT;
    }

    /** Overriden to explicitely set persistence type of ProjectsTab
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    // ---- Imlementation of OperationListener

    public void operationPostCreate (OperationEvent ev) {}
    public void operationCopy (OperationEvent.Copy ev) {}
    public void operationMove (OperationEvent.Move ev) {}
    public void operationDelete (OperationEvent ev) {}
    public void operationRename (OperationEvent.Rename ev) {}
    /** A shadow of a data object has been created.
     * @param ev event describing the action
     */
    public void operationCreateShadow (OperationEvent.Copy ev) {
        postTask (ev.getObject ());
    }
    /** New instance of an object has been created.
     * @param ev event describing the action
     */
    public void operationCreateFromTemplate (OperationEvent.Copy ev) {
        postTask (ev.getObject ());
    }
    // ---- private implementation

    private void postTask(DataObject obj) {
        synchronized (needToSelect) {
            needToSelect[0] = obj;
        }
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                DataObject d;
                synchronized (needToSelect) {
                    d = needToSelect[0];
                    needToSelect[0] = null;
                }
                if (d != null && /* #14179 */d.isValid()) {
                    doSelectNode(d);
                }
            }
        });
    }

    /** Finds a node for given data object.
    */
    private static Node findClosestNode (DataObject obj, Node start, boolean useLogicalViews) {
        DataObject original = obj;

        Stack stack = new Stack ();
        while (obj != null) {
            stack.push (obj);
            obj = obj.getFolder ();
        }

        Node current = start;
        int topIdx = stack.size();
        int i = 0;
        while (i < topIdx) {
            Node n = findDataObject (current, (DataObject)stack.get (i));
            if (n != null) {
                current = n;
                topIdx = i;
                i = 0;
            } else {
                i++;
            }
        }
        if (!check(current, original) && useLogicalViews) {
            Node[] children = current.getChildren().getNodes();
            for (int j = 0; j < children.length; j++) {
                Node child = children[j];
                Node n = selectInLogicalViews(original, child);
                if (check(n, original)) {
                    current = n;
                    break;
                }
            }
        }
        return current;
    }

    private static Node selectInLogicalViews(DataObject original, Node start) {
        return start;
        /* Nothing for now.
        DataShadow shadow = (DataShadow)start.getCookie(DataShadow.class);
        if (shadow == null) {
            return findClosestNode(original, start, false);
        }
        return start;
         */
    }

    /** Selects a node for given data object.
    */
    private boolean selectNode (DataObject obj, Node root) {
        Node node = findClosestNode(obj, root, true);

        try {
            getExplorerManager ().setSelectedNodes (new Node[] { node });
        } catch (PropertyVetoException e) {
            // you are out of luck!
            throw new IllegalStateException();
        }
        return check(node, obj);
    }

    private static boolean check(Node node, DataObject obj) {
        DataObject dObj = (DataObject)node.getLookup().lookup(DataObject.class);
        if (obj == dObj) {
            return true;
        }
        if (dObj instanceof DataShadow && obj == ((DataShadow)dObj).getOriginal()) {
            return true;
        }
        return false;
    }

    /** Finds a data object in given node.
    * @param node the node to search in
    * @param obj the object to look for
    */
    private static Node findDataObject (Node node, DataObject obj) {

        Node[] arr = node.getChildren ().getNodes (true);
        for (int i = 0; i < arr.length; i++) {
            DataShadow ds = (DataShadow)arr[i].getCookie (DataShadow.class);
            if (ds != null && obj == ds.getOriginal ()) {
                return arr[i];
            }
            if (obj == arr[i].getCookie (DataFolder.class)) {
                return arr[i];
            }
            if (obj == arr[i].getCookie (DataObject.class)) {
                return arr[i];
            }
        }

        if (obj.isValid ()) {
            Node n = node.getChildren ().findChild (obj.getNodeDelegate ().getName ());
            if (n != null) return n;
        }

        return null;
    }

    /** Exchanges deserialized root context to projects root context
    * to keep the uniquennes. */
    protected void validateRootContext () {
        Node projectsRc = Favorites.getNode ();
        setRootContext(projectsRc);
    }
    
    

    protected boolean containsNode(DataObject obj) {
        Node node = findClosestNode(obj, getExplorerManager ().getRootContext (), true);
        return check(node, obj);
    }

    protected void doSelectNode (DataObject obj) {
        Node root = getExplorerManager ().getRootContext ();
        if (selectNode (obj, root)) {
            requestActive();
        }
    }

    /** Old (3.2) deserialization of the ProjectTab */
    public Object readResolve() throws java.io.ObjectStreamException {
        getDefault().scheduleValidation();
        return getDefault();
    }
    
} // end of Tab inner class
