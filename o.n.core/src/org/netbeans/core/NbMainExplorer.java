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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.*;
import java.text.MessageFormat;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarToggleButton;
import org.openide.loaders.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.WellKnownModeNames;
import org.netbeans.core.windows.DeferredPerformer;
import org.netbeans.core.windows.WindowManagerImpl;

/** Main explorer - the class remains here for backward compatibility
* with older serialization protocol. Its responsibilty is also
* to listen to the changes of "roots" nodes and open / close 
* explorer's top components properly.
*
* @author Ian Formanek, David Simonek, Jaroslav Tulach
*/
public final class NbMainExplorer extends CloneableTopComponent
    implements DeferredPerformer.DeferredCommand {

    static final long serialVersionUID=6021472310669753679L;
    //  static final long serialVersionUID=-9070275145808944151L;

    /** The message formatter for Explorer title */
    private static MessageFormat formatExplorerTitle;

    /** holds list of roots (Node) */
    private List prevRoots;

    /** assignes to each node one top component holding explorer panel
    * (Node, ExplorerTab) */
    private Map rootsToTCs;

    /** currently selected node */
    private Node currentRoot;

    /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
    private transient RootsListener rootsListener;
    /** weak roots listener */
    private transient PropertyChangeListener weakRootsL;
    /** true if listener to ide setiings properly initialized */
    private transient boolean listenerInitialized;

    /** Minimal initial height of this top component */
    public static final int MIN_HEIGHT = 150;
    /** Default width of main explorer */
    public static final int DEFAULT_WIDTH = 350;

    /** Default constructor */
    public NbMainExplorer () {
        // listening on changes of roots
        rootsListener = new RootsListener();
        weakRootsL = WeakListener.propertyChange(rootsListener, TopManager.getDefault());
        TopManager.getDefault().addPropertyChangeListener(weakRootsL);
    }

    public HelpCtx getHelpCtx () {
        return ExplorerPanel.getHelpCtx (getActivatedNodes (),
                                         new HelpCtx (NbMainExplorer.class));
    }

    /** Overriden to open all top components of main explorer and
    * close this top component, as this top component exists only because of 
    * backward serialization compatibility.
    * Performed with delay, when WS is in consistent state. */
    public void open (Workspace workspace) {
        WindowManagerImpl.deferredPerformer().putRequest(
            this, new DeferredPerformer.DeferredContext(workspace, true)
        );
    }

    /** Implementation of DeferredPerformer.DeferredCommand.
    * Serves both for refresh roots and old explorer open requests */
    public void performCommand (DeferredPerformer.DeferredContext context) {
        Workspace workspace = (Workspace)context.getData(); 
        if (workspace == null) {
            // refresh roots request
            refreshRoots ();
        } else {
            // old explorer open request
            super.open(workspace);
            close(workspace);
            // now open new main explorer top components
            NbMainExplorer singleton = NbMainExplorer.getExplorer();
            singleton.openRoots(workspace);
        }
    }

    /** Open all main explorer's top components on current workspace */
    public void openRoots () {
        openRoots(TopManager.getDefault().getWindowManager().getCurrentWorkspace());
    }

    /** Open all main explorer's top components on given workspace */
    public void openRoots (Workspace workspace) {
        // save the tab we should activate
        ExplorerTab toBeActivated = MainTab.lastActivated;
        // perform open operation
        refreshRoots();
        Node[] rootsArray = (Node[])getRoots().toArray(new Node[0]);
        TopComponent tc = null;
        for (int i = 0; i < rootsArray.length; i++) {
            tc = getRootPanel(rootsArray[i]);
            if (tc != null) {
                tc.open(workspace);
            }
        }
        // set focus to saved last activated tab or repository tab
        if (toBeActivated == null) {
            toBeActivated = getRootPanel(rootsArray[0]);
        }
        final ExplorerTab localActivated = toBeActivated;
        SwingUtilities.invokeLater(new Runnable () {
                                       public void run () {
                                           if (localActivated != null) {
                                               localActivated.requestFocus();
                                           }
                                       }
                                   });
    }

    /** Refreshes current state of main explorer's top components, so they
    * will reflect new nodes. Called when content of "roots" nodes is changed.
    */
    final void refreshRoots () {
        // attach listener to the ide settings if possible
        if (!listenerInitialized) {
            IDESettings ideS = (IDESettings)IDESettings.findObject(IDESettings.class);
            if (ideS != null) {
                ideS.addPropertyChangeListener(weakRootsL);
                listenerInitialized = true;
            }
        }

        List curRoots = getRoots ();
        // first of all we have to close top components for
        // the roots that are no longer present in the roots content
        if (prevRoots != null) {
            HashSet toRemove = new HashSet(prevRoots);
            toRemove.removeAll(curRoots);
            // ^^^ toRemove now contains only roots that are used no more
            for (Iterator it = rootsToTCs.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry me = (Map.Entry)it.next();
                Node r = (Node)me.getKey();
                if (toRemove.contains(r)) {
                    // close top component asociated with this root context
                    // on all workspaces
                    closeEverywhere((TopComponent)me.getValue());
                }
            }
        } else {
            // initialize previous roots list
            prevRoots();
        }

        // create and open top components for newly added roots
        List workspaces = whereOpened(
                              (TopComponent[])rootsToTCs().values().toArray(new TopComponent[0])
                          );
        for (Iterator iter = curRoots.iterator(); iter.hasNext(); ) {
            Node r = (Node)iter.next();
            ExplorerTab tc = getRootPanel(r);
            if (tc == null) {
                // newly added root -> create new TC and open it on every
                // workspace where some top compoents from main explorer
                // are already opened
                tc = createTC(r);
                for (Iterator iter2 = workspaces.iterator(); iter2.hasNext(); ) {
                    tc.open((Workspace)iter2.next());
                }
            }
        }
        // save roots for use during future changes
        prevRoots = curRoots;

        // now select the right component
        // PENDING
        /*ExplorerTab tab = getRootPanel (currentRoot);
        if (tab == null) {
          // root not found
          currentRoot = (Node)roots.get (0);
          tabs.setSelectedIndex (0);
    } else {
          tabs.setSelectedComponent (tab);
    }*/
    }

    /** Helper method - closes given top component on all workspaces
    * where it is opened */
    private static void closeEverywhere (TopComponent tc) {
        Workspace[] workspaces =
            TopManager.getDefault().getWindowManager().getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            if (tc.isOpened(workspaces[i])) {
                tc.close(workspaces[i]);
            }
        }
    }

    /** Utility method - returns list of workspaces where at least one from
    * given list of top components is opened. */
    private static List whereOpened (TopComponent[] tcs) {
        Workspace[] workspaces =
            TopManager.getDefault().getWindowManager().getWorkspaces();
        ArrayList result = new ArrayList(workspaces.length);
        for (int i = 0; i < workspaces.length; i++) {
            for (int j = 0; j < tcs.length; j++) {
                if (tcs[j].isOpened(workspaces[i])) {
                    result.add(workspaces[i]);
                    break;
                }
            }
        }
        return result;
    }

    /** @return List of "root" nodes which has following structure:<br>
    * First goes repository, than root nodes added by modules and at last
    * runtime root node */
    private static List getRoots () {
        Places.Nodes ns = TopManager.getDefault().getPlaces().nodes();
        // build the list of roots
        LinkedList result = new LinkedList();
        // repository goes first
        result.add(ns.repository());
        // projects tab (only if projects module is installed)
        if (NbProjectOperation.hasProjectDesktop()) {
            result.add(NbProjectOperation.getProjectDesktop());
        }
        // roots added by modules (javadoc etc...)
        result.addAll(Arrays.asList(ns.roots()));
        // runtime
        result.add(ns.environment());

        return result;
    }

    /** Creates a top component dedicated to exploration of
    * specified node, which will serve as root context */
    private ExplorerTab createTC (Node rc) {
        // switch according to the type of the root context
        MainTab panel = null;
        Places.Nodes ns = TopManager.getDefault().getPlaces().nodes();
        if (rc.equals(NbProjectOperation.getProjectDesktop())) {
            // projects tab
            panel = new ProjectsTab();
        } else if (rc.equals(ns.repository())) {
            panel = new RepositoryTab ();
        } else if (rc.equals(ns.environment())) {
            // default tabs
            panel = new MainTab();
        } else {
            // tabs added by modules
            panel = new ModuleTab();
        }
        panel.setRootContext(rc);
        rootsToTCs().put(rc, panel);
        return panel;
    }

    /** Safe accessor for root context - top component map. */
    private Map rootsToTCs () {
        if (rootsToTCs == null) {
            rootsToTCs = new HashMap(7);
        }
        return rootsToTCs;
    }

    /** Safe accessor for list of previous root nodes */
    private List prevRoots () {
        if (prevRoots == null) {
            prevRoots = new LinkedList();
        }
        return prevRoots;
    }

    /** Deserialize this top component, sets as default.
    * Provided provided here only for backward compatibility
    * with older serialization protocol */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        //System.out.println("READING old main explorer..."); // NOI18N
        // read explorer panels (and managers)
        int cnt = in.readInt ();
        for (int i = 0; i < cnt; i++) {
            in.readObject();
        }
        in.readObject();
        // read property sheet switcher state...
        in.readBoolean ();
        in.readBoolean ();
        in.readInt();
        in.readInt();
    }

    /** Finds the right panel for given node.
    * @return the panel or null if no such panel exists
    */
    final ExplorerTab getRootPanel (Node root) {
        return (ExplorerTab)rootsToTCs().get(root);
    }


    // -------------------------------------------------------------------------
    // Static methods

    /** Static method to obtains the shared instance of NbMainExplorer
    * @return the shared instance of NbMainExplorer
    */
    public static NbMainExplorer getExplorer () {
        if (explorer == null) {
            explorer = new NbMainExplorer ();
        }
        return explorer;
    }

    /** @return The mode for main explorer on given workspace.
    * Creates explorer mode if no such mode exists on given workspace */
    private static Mode explorerMode (Workspace workspace) {
        Mode result = workspace.findMode(WellKnownModeNames.EXPLORER);
        if (result == null) {
            // create explorer mode on current workspace
            String displayName = NbBundle.getBundle(NbMainExplorer.class).
                                 getString("CTL_ExplorerTitle");
            result = workspace.createMode(
                         WellKnownModeNames.EXPLORER, displayName,
                         NbMainExplorer.class.getResource(
                             "/org/netbeans/core/resources/frames/explorer.gif" // NOI18N
                         )
                     );
        }
        return result;
    }

    /** Shared instance of NbMainExplorer */
    private static NbMainExplorer explorer;


    /** Common explorer top component which composites bean tree view
    * to view given context. */
    public static class ExplorerTab extends ExplorerPanel
        implements DeferredPerformer.DeferredCommand {
        static final long serialVersionUID =-8202452314155464024L;
        /** composited view */
        private TreeView view;
        /** listeners to the root context and IDE settings */
        private PropertyChangeListener rcListener, weakRcL, weakIdeL;
        /** validity flag */
        private boolean valid = true;

        public ExplorerTab () {
            super();
            view = initGui();
            // complete initialization of composited explorer actions
            IDESettings ideS = (IDESettings)IDESettings.findObject(IDESettings.class);
            setConfirmDelete(ideS.getConfirmDelete());
            // attach listener to the changes of IDE settings
            weakIdeL = WeakListener.propertyChange(rcListener(), ideS);
        }

        /** Initializes gui of this component. Subclasses can override
        * this method to install their own gui.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView view = new BeanTreeView();
            setLayout(new BorderLayout());
            add(view);
            return view;
        }

        /** Request focus also for asociated view */
        public void requestFocus () {
            super.requestFocus();
            view.requestFocus();
        }

        /** Ensures that component is valid before opening */
        public void open (Workspace workspace) {
            performCommand(null);
            super.open(workspace);
        }

        /** Sets new root context to view. Name, icon, tooltip
        * of this top component will be updated properly */
        public void setRootContext (Node rc) {
            // remove old listener, if possible
            if (weakRcL != null) {
                getExplorerManager().getRootContext().
                removePropertyChangeListener(weakRcL);
            }
            getExplorerManager().setRootContext(rc);
            initializeWithRootContext(rc);
        }

        public Node getRootContext () {
            return getExplorerManager().getRootContext();
        }

        /** Overrides superclass version - adds request for initialization
        * of the icon and other attributes, also re-attaches listener to the
        * root context */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            super.readExternal(oi);
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            valid = false;
            WindowManagerImpl.deferredPerformer().putRequest(this, null);
        }

        /** Implementation of DeferredPerformer.DeferredCommand
        * Performs initialization of component's attributes
        * after deserialization (component's name, icon etc, 
        * according to the root context) */
        public void performCommand (DeferredPerformer.DeferredContext context) {
            if (!valid) {
                valid = true;
                validateRootContext();
            }
        }

        /** Validates root context of this top component after deserialization.
        * It is guaranteed that this method is called at a time when
        * getExplorerManager().getRootContext() call will return valid result.
        * Subclasses can override this method and peform further validation
        * or even set new root context instead of deserialized one.<br>
        * Default implementation just initializes top component with standard
        * deserialized root context. */
        protected void validateRootContext () {
            initializeWithRootContext(getExplorerManager().getRootContext());
        }

        private PropertyChangeListener rcListener () {
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
            setName(rc.getDisplayName());
            updateTitle();
            // attach listener
            if (weakRcL == null) {
                weakRcL = WeakListener.propertyChange(rcListener(), rc);
            }
            rc.addPropertyChangeListener(weakRcL);
        }

        /** Multi - purpose listener, listens to: <br>
        * 1) Changes of name, icon, short description of root context.
        * 2) Changes of IDE settings, namely delete confirmation settings */
        private final class RootContextListener extends Object
            implements PropertyChangeListener {
            public void propertyChange (PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                Object source = evt.getSource();
                if (source instanceof IDESettings) {
                    // possible change in confirm delete settings
                    setConfirmDelete(((IDESettings)source).getConfirmDelete());
                    return;
                }
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
        } // end of RootContextListener inner class

    } // end of ExplorerTab inner class

    /** Tab of main explorer. Tries to dock itself to main explorer mode
    * before opening, if it's not docked already.
    * Also deserialization is enhanced in contrast to superclass */
    public static class MainTab extends ExplorerTab {
        static final long serialVersionUID =4233454980309064344L;

        /** Holds main tab which was last activated.
        * Used during decision which tab should receive focus
        * when opening all tabs at once using NbMainExplorer.openRoots()
        */
        private static MainTab lastActivated;

        public void open (Workspace workspace) {
            Workspace realWorkspace = (workspace == null)
                                      ? TopManager.getDefault().getWindowManager().getCurrentWorkspace()
                                      : workspace;
            Mode ourMode = realWorkspace.findMode(this);
            if (ourMode == null) {
                explorerMode(realWorkspace).dockInto(this);
            }
            super.open(workspace);
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

        /** Overrides superclass' version, remembers last activated
        * main tab */
        protected void componentActivated () {
            super.componentActivated();
            lastActivated = this;
        }

        /** Registers root context in main explorer in addition to superclass'
        * version */
        protected void validateRootContext () {
            super.validateRootContext();
            registerRootContext(getExplorerManager().getRootContext());
        }

        /* Add given root context and this top component
        * to the map of main explorer's top components and nodes */
        protected void registerRootContext (Node rc) {
            NbMainExplorer explorer = NbMainExplorer.getExplorer();
            explorer.prevRoots().add(rc);
            explorer.rootsToTCs().put(rc, this);
        }

    } // end of MainTab inner class

    /** Repository tab implements operation listener and
    * if createFromTemplate is performed it selects the 
    * created node.
    */
    public static class RepositoryTab extends MainTab
        implements OperationListener {
        static final long serialVersionUID =4233454980309064344L;

        /** previous task */
        private RequestProcessor.Task previousTask;

        /** attaches itself to as a listener.
        */
        public RepositoryTab () {
            DataLoaderPool pool = TopManager.getDefault ().getLoaderPool ();
            pool.addOperationListener (
                WeakListener.operation (this, pool)
            );
        }

        /** Object has been recognized by
         * {@link DataLoaderPool#findDataObject}.
         * This allows listeners
         * to attach additional cookies, etc.
         *
         * @param ev event describing the action
         */
        public void operationPostCreate (OperationEvent ev) {
        }
        /** Object has been successfully copied.
         * @param ev event describing the action
         */
        public void operationCopy (OperationEvent.Copy ev) {
        }
        /** Object has been successfully moved.
         * @param ev event describing the action
         */
        public void operationMove (OperationEvent.Move ev) {
        }
        /** Object has been successfully deleted.
         * @param ev event describing the action
         */
        public void operationDelete (OperationEvent ev) {
        }
        /** Object has been successfully renamed.
         * @param ev event describing the action
         */
        public void operationRename (OperationEvent.Rename ev) {
        }
        /** A shadow of a data object has been created.
         * @param ev event describing the action
         */
        public void operationCreateShadow (OperationEvent.Copy ev) {
        }
        /** New instance of an object has been created.
         * @param ev event describing the action
         */
        public void operationCreateFromTemplate (final OperationEvent.Copy ev) {
            RequestProcessor.Task t = previousTask;
            if (t != null) {
                t.cancel ();
            }

            previousTask = RequestProcessor.postRequest (new Runnable () {
                               public void run () {
                                   previousTask = null;

                                   doSelectNode (ev.getObject ());
                               }
                           }, 100);
        }

        /** Setups the environment to select the right node.
        */
        public void doSelectNode (DataObject obj) {
            if (selectNode (obj, null)) {
                requestFocus ();
            }
        }


        /** Finds a node for given data object.
        * @param stop the folder to stop at or null
        */
        protected boolean selectNode (DataObject obj, DataFolder stop) {
            Stack stack = new Stack ();

            while (obj != null && obj != stop) {
                stack.push (obj);
                obj = obj.getFolder ();
            }

            Node current = getExplorerManager ().getRootContext ();
            while (!stack.isEmpty ()) {
                Node n = findDataObject (current, (DataObject)stack.pop ());
                if (n == null) {
                    // no node to select found
                    return false;
                }
                current = n;
            }

            try {
                getExplorerManager ().setSelectedNodes (new Node[] { current });
            } catch (PropertyVetoException e) {
                // you are out of luck!
                throw new InternalError ();
            }

            return true;
        }

        /** Finds a data object in given node.
        * @param node the node to search in
        * @param obj the object to look for
        */
        private static Node findDataObject (Node node, DataObject obj) {
            Node n = node.getChildren ().findChild (obj.getNodeDelegate ().getName ());
            if (n != null) return n;

            Node[] arr = node.getChildren ().getNodes ();
            for (int i = 0; i < arr.length; i++) {
                if (obj == arr[i].getCookie (DataObject.class)) {
                    return arr[i];
                }
            }

            return null;
        }
    }

    /** Special class for projects tab in main explorer */
    public static class ProjectsTab extends RepositoryTab {
        static final long serialVersionUID =-8178367548546385799L;

        /** Exchanges deserialized root context to projects root context
        * to keep the uniquennes. */
        protected void validateRootContext () {
            Node projectsRc = NbProjectOperation.getProjectDesktop();
            setRootContext(projectsRc);
            registerRootContext(projectsRc);
        }

        public void doSelectNode (DataObject obj) {
            DataFolder root = (DataFolder)getRootContext ().getCookie (DataFolder.class);

            if (selectNode (obj, root)) {
                requestFocus ();
            }
        }

    } // end of ProjectsTab inner class

    /** Special class for tabs added by modules to the main explorer */
    public static class ModuleTab extends MainTab {
        static final long serialVersionUID =8089827754534653731L;

        /** Throws deserialized root context and sets proper node found
        * in roots set as new root context for this top component.
        * The reason for such construction is to keep the uniquennes of
        * root context node after deserialization. */
        protected void validateRootContext () {
            // find proper node
            Class nodeClass = getExplorerManager().getRootContext().getClass();
            Node[] roots = TopManager.getDefault().getPlaces().nodes().roots();
            for (int i = 0; i < roots.length; i++) {
                if (nodeClass.equals(roots[i].getClass())) {
                    setRootContext(roots[i]);
                    registerRootContext(roots[i]);
                    break;
                }
            }
        }

    } // end of ModuleTab inner class

    /** Top component for project ang global settings. */
    public static class SettingsTab extends ExplorerTab {
        static final long serialVersionUID =9087127908986061114L;

        /** Overrides superclass version - put tree view and property
        * sheet to the splitted panel.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView view = new BeanTreeView();
            SplittedPanel split = new SplittedPanel();
            PropertySheetView propertyView = new PropertySheetView();
            split.add(view, SplittedPanel.ADD_LEFT);
            split.add(propertyView, SplittedPanel.ADD_RIGHT);
            // add to the panel
            setLayout(new BorderLayout());
            add(split, BorderLayout.CENTER);

            return view;
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

    }

    /** Listener on roots, listens to changes of roots content */
    private final class RootsListener extends Object
        implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            if (TopManager.PROP_PLACES.equals(evt.getPropertyName())) {
                // possible change in list of roots
                // defer refresh request if window system is in inconsistent state
                WindowManagerImpl.deferredPerformer().putRequest(
                    NbMainExplorer.getExplorer(), 
                    new DeferredPerformer.DeferredContext(null, true)
                );
            }
        }
    } // end of RootsListener inner class

    public static void main (String[] args) throws Exception {
        NbMainExplorer e = new NbMainExplorer ();
        e.open ();
    }
}

/*
* Log
*  54   Gandalf   1.53        1/19/00  Petr Nejedly    Commented out debug 
*       messages
*  53   Gandalf   1.52        1/17/00  David Simonek   renaming of tabs now 
*       react also on NAME node chaanges, not only display name
*  52   Gandalf   1.51        1/13/00  Jaroslav Tulach I18N
*  51   Gandalf   1.50        1/11/00  David Simonek   projects tab now second 
*       tab in main explorer
*  50   Gandalf   1.49        1/9/00   David Simonek   modified initialization 
*       of the WindowManagerImpl
*  49   Gandalf   1.48        1/5/00   Jaroslav Tulach Newly created objects are
*       selected in explorer
*  48   Gandalf   1.47        12/23/99 David Simonek   special tabs for projects
*       and module tabs
*  47   Gandalf   1.46        12/21/99 David Simonek   minor fixes
*  46   Gandalf   1.45        12/17/99 David Simonek   #4886
*  45   Gandalf   1.44        12/3/99  David Simonek   
*  44   Gandalf   1.43        11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  43   Gandalf   1.42        11/5/99  Jesse Glick     Context help jumbo patch.
*  42   Gandalf   1.41        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  41   Gandalf   1.40        10/25/99 Ian Formanek    Fixed title of Main 
*       Explorer - now displays selected node instead of explored context
*  40   Gandalf   1.39        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  39   Gandalf   1.38        10/7/99  David Simonek   request focus related 
*       bugs repaired
*  38   Gandalf   1.37        9/22/99  Jaroslav Tulach Solving class cast 
*       exception.  
*  37   Gandalf   1.36        9/20/99  Jaroslav Tulach #1603
*  36   Gandalf   1.35        9/15/99  David Simonek   cut/copy/delete actions 
*       bugfix
*  35   Gandalf   1.34        8/29/99  Ian Formanek    Removed obsoleted import
*  34   Gandalf   1.33        8/20/99  Ian Formanek    Reverted last 2 changes
*  33   Gandalf   1.32        8/20/99  Ian Formanek    Fixed bug with explorer 
*       when starting clean IDE
*  32   Gandalf   1.31        8/19/99  David Simonek   cut/copy/paste/delete 
*       actions enabling hopefully fixed
*  31   Gandalf   1.30        8/18/99  David Simonek   bugfix #3463, #3461  
*  30   Gandalf   1.29        8/17/99  David Simonek   commentaries removed
*  29   Gandalf   1.28        8/13/99  Jaroslav Tulach New Main Explorer
*  28   Gandalf   1.27        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  27   Gandalf   1.26        8/3/99   Jaroslav Tulach Getting better and 
*       better.
*  26   Gandalf   1.25        8/3/99   Jaroslav Tulach Serialization of 
*       NbMainExplorer improved again.
*  25   Gandalf   1.24        8/2/99   Jaroslav Tulach 
*  24   Gandalf   1.23        8/1/99   Jaroslav Tulach MainExplorer now listens 
*       to changes in root elements.
*  23   Gandalf   1.22        7/30/99  David Simonek   
*  22   Gandalf   1.21        7/30/99  David Simonek   serialization fixes
*  21   Gandalf   1.20        7/28/99  David Simonek   canClose updates
*  20   Gandalf   1.19        7/21/99  David Simonek   properties switcher fixed
*  19   Gandalf   1.18        7/19/99  Jesse Glick     Context help.
*  18   Gandalf   1.17        7/16/99  Ian Formanek    Fixed bug #1800 - You can
*       drag off the explorer toolbar. 
*  17   Gandalf   1.16        7/15/99  Ian Formanek    Swapped Global and 
*       Project settings tabs
*  16   Gandalf   1.15        7/13/99  Ian Formanek    New MainExplorer tabs 
*       (usability&intuitiveness discussion results)
*  15   Gandalf   1.14        7/12/99  Jesse Glick     Context help.
*  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
*  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   Gandalf   1.11        5/30/99  Ian Formanek    Fixed bug 1647 - Open, 
*       Compile, Rename, Execute and  etc. actions in popup menu in explorer are
*       sometimes disabled.  Fixed bug 1971 - If the tab is switched from 
*       Desktop to Repository with some nodes already selected, the actions in 
*       popupmenu might not be correctly enabled.  Fixed bug 1616 - Property 
*       sheet button in explorer has no tooltip.
*  11   Gandalf   1.10        5/15/99  David Simonek   switchable sheet 
*       serialized properly.....finally
*  10   Gandalf   1.9         5/14/99  David Simonek   serialization of 
*       switchable sheet state
*  9    Gandalf   1.8         5/11/99  David Simonek   changes to made window 
*       system correctly serializable
*  8    Gandalf   1.7         3/25/99  David Simonek   another small changes in 
*       window system
*  7    Gandalf   1.6         3/25/99  David Simonek   changes in window system,
*       initial positions, bugfixes
*  6    Gandalf   1.5         3/18/99  Ian Formanek    The title now updates 
*       when tab is switched
*  5    Gandalf   1.4         3/16/99  Ian Formanek    SINGLE mode removed, as 
*       it is there by default
*  4    Gandalf   1.3         3/16/99  Ian Formanek    Title improved
*  3    Gandalf   1.2         3/16/99  Ian Formanek    Added listening to icon 
*       and displayName changes on roots, support for ExplorerActions 
*       (Cut/Copy/...)
*  2    Gandalf   1.1         3/15/99  Ian Formanek    Added formatting of 
*       title, updating activatedNodes
*  1    Gandalf   1.0         3/14/99  Ian Formanek    
* $
*/
