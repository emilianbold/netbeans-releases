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

package org.netbeans.core;

import java.awt.*;
import java.beans.*;
import java.io.ObjectInput;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.core.IDESettings;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.loaders.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Main explorer - the class remains here for backward compatibility
* with older serialization protocol. Its responsibilty is also
* to listen to the changes of "roots" nodes and open / close 
* explorer's top components properly.
*
* @author Ian Formanek, David Simonek, Jaroslav Tulach
*/
public final class NbMainExplorer extends CloneableTopComponent {

    static final long serialVersionUID=6021472310669753679L;
    //  static final long serialVersionUID=-9070275145808944151L;

    /** holds list of roots (Node) */
    private List prevRoots;

    /** assignes to each node one top component holding explorer panel
    * (Node, ExplorerTab) */
    private Map rootsToTCs;

    /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
    private transient RootsListener rootsListener;

    /** Minimal initial height of this top component */
    public static final int MIN_HEIGHT = 150;
    /** Default width of main explorer */
    public static final int DEFAULT_WIDTH = 350;
    
    /** Mapping module tabs to their root node classes */
    private static Map moduleTabs;

    /** Default constructor */
    public NbMainExplorer () {
        // listening on changes of roots
        rootsListener = new RootsListener();
        NbPlaces p = NbPlaces.getDefault();
        p.addChangeListener(WeakListeners.change (rootsListener, p));

        refreshRoots();
    }

    public HelpCtx getHelpCtx () {
        return ExplorerPanel.getHelpCtx (getActivatedNodes (),
                                         new HelpCtx (NbMainExplorer.class));
    }
    
    /** Finds module tab in mapping of module tabs to their root node classes.
     * If it is not found it is added when parameter tc is not null. When parameter
     * tc is null new ModuleTab is created using default constructor. */
    private static synchronized ModuleTab findModuleTab (Node root, ModuleTab tc) {
        if (moduleTabs == null) {
            moduleTabs = new WeakHashMap(5);
        }
        ModuleTab tab = (ModuleTab) moduleTabs.get(root);
        if (tab != null) {
            return tab;
        } else {
            if (tc != null) {
                moduleTabs.put(root, tc);
                return tc;
            } else {
                ModuleTab newTC = new ModuleTab();
                moduleTabs.put(root, newTC);
                return newTC;
            }
        }
    }
    
    /** Overriden to open all top components of main explorer and
    * close this top component, as this top component exists only because of 
    * backward serialization compatibility.
    * Performed with delay, when WS is in consistent state. */
    public void open (Workspace workspace) {
        doOpen(workspace);
    }

    private void doOpen(Workspace workspace) {
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
        openRoots(WindowManager.getDefault().getCurrentWorkspace());
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
        
        //Bugfix #9352 20 Feb 2001 by Marek Slama
        //requestFocus called directly on mode - it sets
        //deferred request so that requestFocus is performed
        //on correct workspace when component is shown.
        //Delayed call of requestFocus on ExplorerTab
        //was performed on incorrect workspace.
        /*final ExplorerTab localActivated = toBeActivated;
        SwingUtilities.invokeLater(new Runnable () {
                                       public void run () {
        System.out.println("++*** localActivated:" + localActivated);
                                           if (localActivated != null) {
        System.out.println("++*** Call of localActivated.requestFocus()");
                                               localActivated.requestFocus();
                                           }
                                       }
                                   });*/
        
        //Bugfix #9815: added check if toBeActivated is null before
        //request focus is called.
        //Bugfix #17956: Make sure that findMode is called after top component
        //is added to mode.
        if (SwingUtilities.isEventDispatchThread()) {
            if (toBeActivated != null) {
                Mode mode = workspace.findMode(toBeActivated);
                if (mode != null) {
                    toBeActivated.focusView();
                }
            }
        } else {
            if (toBeActivated != null) {
                final ExplorerTab localActivated = toBeActivated;
                final Workspace localWorkspace = workspace;
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        Mode mode = localWorkspace.findMode(localActivated);
                        if (mode != null) {
                            localActivated.focusView();
                        }
                    }
                });
            }
        }
        //End of bugfix #9815
        //End of bugfix #9352
    }

    /** Refreshes current state of main explorer's top components, so they
    * will reflect new nodes. Called when content of "roots" nodes is changed.
    */
    final void refreshRoots () {
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
    }

    /** Helper method - closes given top component on all workspaces
    * where it is opened */
    private static void closeEverywhere (TopComponent tc) {
        Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            if (tc.isOpened(workspaces[i])) {
                tc.close(workspaces[i]);
            }
        }
    }

    /** Utility method - returns list of workspaces where at least one from
    * given list of top components is opened. */
    private static List whereOpened (TopComponent[] tcs) {
        Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
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

    //Temporary solution for bugfix #9352. There is currently
    //no way how to select given tab other than focused in split container.
    //It requires better solution.
    //Method changed from private to public so it can be used in DefaultCreator.
    
    /** @return List of "root" nodes which has following structure:<br>
    * First goes repository, than root nodes added by modules and at last
    * runtime root node */
    public static List getRoots () {
        NbPlaces places = NbPlaces.getDefault();
        // build the list of roots
        LinkedList result = new LinkedList();
        // repository goes first
        result.add(RepositoryNodeFactory.getDefault().repository(DataFilter.ALL));
        // roots added by modules (javadoc etc...)
        result.addAll(Arrays.asList(places.roots()));
        // runtime
        result.add(places.environment());

        return result;
    }

    /** Creates a top component dedicated to exploration of
    * specified node, which will serve as root context */
    private ExplorerTab createTC (Node rc) {
        // switch according to the type of the root context
        MainTab panel = null;
        NbPlaces places = NbPlaces.getDefault();

        if (rc.equals(RepositoryNodeFactory.getDefault().repository(DataFilter.ALL))) {
            panel = RepositoryTab.getDefaultRepositoryTab();
        } else if (rc.equals(places.environment())) {
            // default tabs
            panel = MainTab.getDefaultMainTab();
        } else {
            // tabs added by modules
            panel = NbMainExplorer.findModuleTab(rc, null);
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

    //Temporary solution for bugfix #9352. There is currently
    //no way how to select given tab other than focused in split container.
    //It requires better solution.
    //Method changed from package to public so it can be used in DefaultCreator.
    
    /** Finds the right panel for given node.
    * @return the panel or null if no such panel exists
    */
    public final ExplorerTab getRootPanel (Node root) {
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
        Mode result = workspace.findMode("explorer"); // NOI18N
        if (result == null) {
            // create explorer mode on current workspace
            String displayName = NbBundle.getBundle(NbMainExplorer.class).
                                 getString("CTL_ExplorerTitle");
            result = workspace.createMode(
                         "explorer", // NOI18N
                         displayName,
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
        implements /*DeferredPerformer.DeferredCommand,*/ TopComponent.Cloneable {
        static final long serialVersionUID =-8202452314155464024L;
        /** composited view */
        protected TreeView view;
        /** mini status bar */
        private MiniStatusBar miniStatusBar;
        //private MouseListener statusBarPopupInvoker;
        /** listeners to the root context and IDE settings */
        private PropertyChangeListener weakRcL, weakIdeL;
        private NodeListener weakNRcL;
        private IDESettings ideSettings;

        private NodeListener rcListener;
        /** validity flag */
        private boolean valid = true;

        public ExplorerTab () {
            super();
            // complete initialization of composited explorer actions
            ideSettings = (IDESettings)IDESettings.findObject(IDESettings.class, true);
            setConfirmDelete(ideSettings.getConfirmDelete ());
            
            // attach listener to the changes of IDE settings
            weakIdeL = WeakListeners.propertyChange(rcListener(), ideSettings);
            
            // enhancement 9940, add MiniStatusBarListener a status bar's state
            ideSettings.addPropertyChangeListener (new MiniStatusBarStateListener ());
        }

        /** Initialize visual content of component */
        protected void componentShowing () {
            super.componentShowing ();
            
            if (view == null) {
                view = initGui ();
                
                view.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NbMainExplorer.class).getString("ACSN_ExplorerBeanTree"));
                view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NbMainExplorer.class).getString("ACSD_ExplorerBeanTree"));
            }
        }

        /** Performs superclass addNotify code, then delegates to
         * componentShowing if component is used outside window system.
         * Needed for proper initialization.
         */
        public void addNotify () {
            super.addNotify();
            Workspace curWs = WindowManager.getDefault().getCurrentWorkspace();
            if ((curWs != null) && curWs.findMode(this) != null) {
                return;
            }
            componentShowing();
        }
        
        /** Transfer focus to view. */
        public void requestFocus () {
            super.requestFocus();
            if (view != null) {
                view.requestFocus();
            }
        }
        
        /** Initializes gui of this component. Subclasses can override
        * this method to install their own gui.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView view = new BeanTreeView();
            setLayout(new BorderLayout());
            
            add (view);
            
            // add mini status bar at the bottom of this explorer panel
            add (getMiniStatusBar(), BorderLayout.SOUTH);
            getMiniStatusBar ().setVisible (MiniStatusBar.isMiniStatusBarEnabled ());

            return view;
        }

        private void removeMiniStatusBar () {
            if (miniStatusBar!=null) {
                getMiniStatusBar ().setExplorerManager (null);
                getMiniStatusBar ().setVisible (false);
                revalidate ();
            }
        }
        
        private MiniStatusBar getMiniStatusBar () {
            if (miniStatusBar==null) {
                miniStatusBar = new MiniStatusBar (getExplorerManager ());
            }
            return miniStatusBar;
        }
        
        /** Listensm on the changes in IDESettings about the mini status bar.
         */
        private class MiniStatusBarStateListener implements PropertyChangeListener {
            
            MiniStatusBarStateListener() {}
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (IDESettings.PROP_MINI_STATUS_BAR_STATE.equals (evt.getPropertyName ())) {
                    if (evt.getNewValue ().equals(evt.getOldValue ()))
                        // no change
                        return ;
                    
                    // mini status bar will be hidden
                    if (((Boolean)evt.getOldValue ()).booleanValue ()) {
                        removeMiniStatusBar ();
                    }
                    
                    // mini status bar will be showed
                    if (((Boolean)evt.getNewValue ()).booleanValue ()) {
                        getMiniStatusBar ().setExplorerManager (getExplorerManager ());
                        getMiniStatusBar ().setVisible (true);
                    }
                }
            }
        }
        
        void focusView() {
            if (view != null) {
                view.requestFocus();
            }
        }

        /** Ensures that component is valid before opening */
        public void open (Workspace workspace) {
            setValidRootContext();
            
            super.open(workspace);
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

        // #16375. Not to try to serialize explored nodes which aren't
        // serializable (getHandle returns null).
        /** Adjusts this component persistence according
         * root context node persistence ability. */
        public void adjustComponentPersistence() {
            Node.Handle handle = getExplorerManager().getRootContext().getHandle();
            if(handle == null) {
                // Not persistent.
                putClientProperty("PersistenceType", "Never"); // NOI18N
            } else {
                // Persistent.
                putClientProperty("PersistenceType", "OnlyOpened"); // NOI18N
            }
        }

        public Node getRootContext () {
            return getExplorerManager().getRootContext();
        }
        
        /** Deserialization of ExploreTab, if subclass overwrites this method it
            MUST call scheduleValidation() */
        public Object readResolve() throws java.io.ObjectStreamException {
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            //Bugfix #17622, call of scheduleValidation() moved from
            //readExternal().
            scheduleValidation();
            return this;
        }

        private void setValidRootContext() {
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
            // set explorer manager to mini status bar
            if (miniStatusBar!=null)
                miniStatusBar.setExplorerManager (getExplorerManager ());
            // update TC's attributes
            setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
            setToolTipText(rc.getShortDescription());
            // bugfix #15136
            setName(rc.getDisplayName());
            updateTitle();

            if (weakRcL == null) {
                weakRcL = WeakListeners.propertyChange(rcListener(), rc);
            }
            else {
                rc.removePropertyChangeListener(weakRcL);
            }
            rc.addPropertyChangeListener(weakRcL);
            
            if (weakNRcL == null) {
                weakNRcL = org.openide.nodes.NodeOp.weakNodeListener (rcListener(), rc);
            }
            else {
                rc.removeNodeListener(weakNRcL);
            }
            rc.addNodeListener(weakNRcL);
        }
        
        // put a request for later validation
        // we must do this here, because of ExplorerManager's deserialization.
        // Root context of ExplorerManager is validated AFTER all other
        // deserialization, so we must wait for it
        protected final void scheduleValidation() {
            valid = false;
            setValidRootContext();
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

        public TopComponent cloneComponent() {
            ExplorerTab nue = new ExplorerTab();
            nue.getExplorerManager().setRootContext(getExplorerManager().getRootContext());
            try {
                nue.getExplorerManager().setSelectedNodes(getExplorerManager().getSelectedNodes());
            } catch (PropertyVetoException pve) {
                ErrorManager.getDefault().notify(pve);
            }
            return nue;
        }
        
        /** Multi - purpose listener, listens to: <br>
        * 1) Changes of name, icon, short description of root context.
        * 2) Changes of IDE settings, namely delete confirmation settings */
        private final class RootContextListener extends Object implements NodeListener {
            
            RootContextListener() {}
            
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
            
            public void nodeDestroyed(org.openide.nodes.NodeEvent nodeEvent) {
                ExplorerTab.this.setCloseOperation(TopComponent.CLOSE_EACH);
                ExplorerTab.this.close();
            }            
            
            public void childrenRemoved(org.openide.nodes.NodeMemberEvent e) {}
            public void childrenReordered(org.openide.nodes.NodeReorderEvent e) {}
            public void childrenAdded(org.openide.nodes.NodeMemberEvent e) {}
            
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
        
        private static MainTab DEFAULT;

        public static synchronized MainTab getDefaultMainTab() {
            if (DEFAULT == null) {
                DEFAULT = new MainTab();
                // put a request for later validation
                // we must do this here, because of ExplorerManager's deserialization.
                // Root context of ExplorerManager is validated AFTER all other
                // deserialization, so we must wait for it
                DEFAULT.scheduleValidation();
            }
            
            return DEFAULT;
        }
        
        /** Creator/accessor method for proper initialization of
         * environment (runtime) top component from xml settings file.
         */
        public static MainTab createEnvironmentTab () {
            return (MainTab)getExplorer().createTC(
                NbPlaces.getDefault().environment()
            );
        }

        /** Deserialization of RepositoryTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            if (DEFAULT == null) {
                DEFAULT = this;
            }
            getDefaultMainTab().scheduleValidation();
            return getDefaultMainTab();
        }
        
        public void open (Workspace workspace) {
            Workspace realWorkspace = (workspace == null)
                                      ? WindowManager.getDefault().getCurrentWorkspace()
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

        /** data object which should be selected in EQ; synch array when accessing */
        private static final DataObject[] needToSelect = new DataObject[1];

        private static RepositoryTab DEFAULT;
        
        /** attaches itself to as a listener.
        */
        public RepositoryTab () {
            DataLoaderPool pool = (DataLoaderPool)Lookup.getDefault ().lookup (DataLoaderPool.class);
            pool.addOperationListener (
                (OperationListener)WeakListeners.create (OperationListener.class, this, pool)
            );
        }
        
        public static synchronized RepositoryTab getDefaultRepositoryTab() {
            if (DEFAULT == null) {
                DEFAULT = new RepositoryTab();
                // put a request for later validation
                // we must do this here, because of ExplorerManager's deserialization.
                // Root context of ExplorerManager is validated AFTER all other
                // deserialization, so we must wait for it
                DEFAULT.scheduleValidation();
            }
            
            return DEFAULT;
        }
        
        /** Creator/accessor method for proper initialization of
         * filesystems top component from xml settings file.
         */
        public static RepositoryTab createRepositoryTab () {
            return (RepositoryTab)getExplorer().createTC(
                RepositoryNodeFactory.getDefault().repository(DataFilter.ALL)
            );
        }

        /** Deserialization of RepositoryTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            if (DEFAULT == null) {
                DEFAULT = this;
            }
            getDefaultRepositoryTab().scheduleValidation();
            return getDefaultRepositoryTab();
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
            postTask (ev.getObject ());
        }
        /** New instance of an object has been created.
         * @param ev event describing the action
         */
        public void operationCreateFromTemplate (OperationEvent.Copy ev) {
            postTask (ev.getObject ());
        }

        private void postTask(DataObject obj) {
            try {
                // bugfix #32180, don't select node on hidden filesystem
                if (obj.getPrimaryFile ().getFileSystem ().isHidden ()) {
                    // ignore it
                    return ;
                }
            } catch (org.openide.filesystems.FileStateInvalidException fsie) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, fsie);
                // don't post task
                return ;
            }
            
            synchronized (needToSelect) {
                needToSelect[0] = obj;
            }
            EventQueue.invokeLater(new Runnable() {
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
        
        /** Setups the environment to select the right node.
        */
        public void doSelectNode (DataObject obj) {
            selectNode (obj, null);
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

            // insert parent as well
            if (obj != null)
                stack.push (obj);
            
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
            
            // bugfix #32781, check if the data object is valid before call getNodeDelegate()
            if (obj.isValid ()) {
                Node n = node.getChildren ().findChild (obj.getNodeDelegate ().getName ());
                if (n != null) return n;
            }

            Node[] arr = node.getChildren ().getNodes ();
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
            return null;
        }
    }

    /** Special class for projects tab in main explorer */
    public static class ProjectsTab extends RepositoryTab {
        static final long serialVersionUID =-8178367548546385799L;
        
        /** Must have *default* constructor - deserialization from 3.2 supposes it */
        private ProjectsTab() {
        }
        
        /** Old (3.2) deserialization of the ProjectTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            throw new java.io.InvalidObjectException(
                "Old [NB3.2 ProjectsTab] component is not supported anymore."); // NOI18N
        }

    } // end of ProjectsTab inner class

    /** Special class for tabs added by modules to the main explorer */
    public static class ModuleTab extends MainTab {
        static final long serialVersionUID =8089827754534653731L;
        
        public ModuleTab() {
        }
                
        
        public void setRootContext(Node root) {
            super.setRootContext(root);
            adjustComponentPersistence();
        }
        
        /** Throws deserialized root context and sets proper node found
        * in roots set as new root context for this top component.
        * The reason for such construction is to keep the uniquennes of
        * root context node after deserialization. */
        protected void validateRootContext () {
            // find proper node
            Class nodeClass = getExplorerManager().getRootContext().getClass();
            Node[] roots = NbPlaces.getDefault().roots();
            for (int i = 0; i < roots.length; i++) {
                if (nodeClass.equals(roots[i].getClass())) {
                    setRootContext(roots[i]);
                    registerRootContext(roots[i]);
                    break;
                }
            }
        }
        
        /** Deserialization of ModuleTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            Node root = getExplorerManager().getRootContext();
            
            ModuleTab tc = NbMainExplorer.findModuleTab(root, this);
            if(tc == null) {
                throw new java.io.InvalidObjectException(
                    "Cannot deserialize ModuleTab for node " + root); // NOI18N
            }
            
            tc.scheduleValidation();
            return tc;
        }

    } // end of ModuleTab inner class

    /** Top component for project ang global settings. */
    public static class SettingsTab extends ExplorerTab {
        static final long serialVersionUID =9087127908986061114L;

        /** Overrides superclass version - put tree view and property
        * sheet to the split panel.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView view = new BeanTreeView();
            SplittedPanel split = new SplittedPanel();
            PropertySheetView propertyView = new PropertySheetView();
            propertyView.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NbMainExplorer.class).getString("ACSN_ExplorerPropertySheetView"));
            propertyView.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NbMainExplorer.class).getString("ACSD_ExplorerPropertySheetView"));
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
        
        /** Get context help for a NbMainExplorer.SettingsTab.
        * Looks at the manager's node selection if no node is selected
        * returns root context node's help context.
        * @return the help context
        */
        public HelpCtx getHelpCtx () {
            HelpCtx hlp = super.getHelpCtx ();
            if(hlp==null || hlp.equals(new HelpCtx(ExplorerPanel.class))) {
                return getExplorerManager ().getRootContext ().getHelpCtx ();
            }
            return hlp;
        }
    }

    /** Listener on roots, listens to changes of roots content */
    private final class RootsListener extends Object implements ChangeListener {
        
        RootsListener() {}
        
        public void stateChanged(ChangeEvent e) {
            NbMainExplorer.getExplorer().doOpen(null);
        }
    } // end of RootsListener inner class

    public static void main (String[] args) throws Exception {
        NbMainExplorer e = new NbMainExplorer ();
        e.open ();
    }
}
