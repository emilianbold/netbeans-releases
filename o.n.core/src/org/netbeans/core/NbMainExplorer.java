/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core;

import java.awt.*;
import java.beans.*;
import java.io.ObjectInput;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import java.util.prefs.Preferences;
import javax.swing.event.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableTopComponent;
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
    private List<Node> prevRoots;

    /** assignes to each node one top component holding explorer panel
    * (Node, ExplorerTab) */
    private Map<Node, ExplorerTab> rootsToTCs;

    /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
    private transient RootsListener rootsListener;

    /** Minimal initial height of this top component */
    public static final int MIN_HEIGHT = 150;
    /** Default width of main explorer */
    public static final int DEFAULT_WIDTH = 350;

    /** Mapping module tabs to their root node classes */
    private static Map<Node, ModuleTab> moduleTabs;

    /** Default constructor */
    public NbMainExplorer () {
//	System.out.println("NbMainExplorer.<init>");
        // listening on changes of roots
        rootsListener = new RootsListener();
        NbPlaces p = NbPlaces.getDefault();
        p.addChangeListener(WeakListeners.change (rootsListener, p));

        refreshRoots();
    }

    public HelpCtx getHelpCtx () {
        return ExplorerUtils.getHelpCtx (getActivatedNodes (),
                                         new HelpCtx (NbMainExplorer.class));
    }

    /** Finds module tab in mapping of module tabs to their root node classes.
     * If it is not found it is added when parameter tc is not null. When parameter
     * tc is null new ModuleTab is created using default constructor. */
    private static synchronized ModuleTab findModuleTab (Node root, ModuleTab tc) {
	System.out.println("NbMainExplorer.findModuleTab "+root);
        if (moduleTabs == null) {
            moduleTabs = new WeakHashMap<Node, ModuleTab>(5);
        }
        ModuleTab tab = moduleTabs.get(root);
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

    /** Refreshes current state of main explorer's top components, so they
    * will reflect new nodes. Called when content of "roots" nodes is changed.
    */
    final void refreshRoots() {
        List<Node> curRoots = getRoots ();
        // first of all we have to close top components for
        // the roots that are no longer present in the roots content
        if (prevRoots != null) {
            HashSet<Node> toRemove = new HashSet<Node>(prevRoots);
            toRemove.removeAll(curRoots);
            // ^^^ toRemove now contains only roots that are used no more
            for (Map.Entry<Node, ExplorerTab> me: rootsToTCs.entrySet()) {
                Node r = me.getKey();
                if (toRemove.contains(r)) {
                    // close top component asociated with this root context
                    me.getValue().close();
                }
            }
        } else {
            // initialize previous roots list
            prevRoots();
        }

        // create and open top components for newly added roots
        for (Node r : curRoots) {
            ExplorerTab tc = getRootPanel(r);
            if (tc == null) {
                // newly added root -> create new TC and open it
                tc = createTC(r, false);
                tc.open();
            }
        }

        // save roots for use during future changes
        prevRoots = curRoots;
    }

    //Temporary solution for bugfix #9352. There is currently
    //no way how to select given tab other than focused in split container.
    //It requires better solution.
    //Method changed from private to public so it can be used in DefaultCreator.

    /** @return List of "root" nodes which has following structure:<br>
    * First goes repository, than root nodes added by modules and at last
    * runtime root node */
    public static List<Node> getRoots () {
        NbPlaces places = NbPlaces.getDefault();
        // build the list of roots
        LinkedList<Node> result = new LinkedList<Node>();

        //repository goes first
/*
        #47032:  Netbeans hangs for 30 seconds during startup - so commented out
        Moreover there isn't any ExlorerTab dedicated to show this repository root.
        result.add(RepositoryNodeFactory.getDefault().repository(DataFilter.ALL));
*/

        // roots added by modules (javadoc etc...)
        result.addAll(Arrays.asList(places.roots()));
        // runtime
        result.add(places.environment());

        return result;
    }

    /** Creates a top component dedicated to exploration of
    * specified node, which will serve as root context */
    private ExplorerTab createTC (Node rc, boolean deserialize) {
        // switch according to the type of the root context
        MainTab panel = null;
        NbPlaces places = NbPlaces.getDefault();

        if (rc.equals(places.environment())) {
            // default tabs
            if (deserialize) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("runtime"); // NOI18N
                if (tc != null) {
                    if (tc instanceof MainTab) {
                        panel = (MainTab) tc;
                    } else {
                        //Incorrect settings file?
                        IllegalStateException exc = new IllegalStateException
                        ("Incorrect settings file. Unexpected class returned." // NOI18N
                        + " Expected:" + MainTab.class.getName() // NOI18N
                        + " Returned:" + tc.getClass().getName()); // NOI18N
                        Logger.getLogger(NbMainExplorer.class.getName()).log(Level.WARNING, null, exc);
                        panel = MainTab.getDefaultMainTab();
                    }
                } else {
                    panel = MainTab.getDefaultMainTab();
                }
            } else {
                panel = MainTab.getDefaultMainTab();
            }
            panel.setRootContext(rc, false);
        } else {
            // tabs added by modules
            //We cannot use findTopComponent here because we do not know unique
            //TC ID ie. proper deserialization of such TC will not work.
            panel = NbMainExplorer.findModuleTab(rc, null);
            panel.setRootContext(rc);
        }


        rootsToTCs().put(rc, panel);
        return panel;
    }

    /** Safe accessor for root context - top component map. */
    private Map<Node,ExplorerTab> rootsToTCs () {
        if (rootsToTCs == null) {
            rootsToTCs = new HashMap<Node,ExplorerTab>(7);
        }
        return rootsToTCs;
    }

    /** Safe accessor for list of previous root nodes */
    private List<Node> prevRoots () {
        if (prevRoots == null) {
            prevRoots = new LinkedList<Node>();
        }
        return prevRoots;
    }

    /** Deserialize this top component, sets as default.
    * Provided provided here only for backward compatibility
    * with older serialization protocol */
    @Override
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
        return rootsToTCs().get(root);
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

    /** Shared instance of NbMainExplorer */
    private static NbMainExplorer explorer;


    /** Common explorer top component which composites bean tree view
    * to view given context. */
    public static class ExplorerTab extends org.netbeans.beaninfo.ExplorerPanel
        implements /*DeferredPerformer.DeferredCommand,*/ TopComponent.Cloneable {
        static final long serialVersionUID =-8202452314155464024L;
        /** confirmDelete property name */
        private static final String PROP_CONFIRM_DELETE = "confirmDelete"; // NOI18N
        /** composited view */
        protected TreeView view;
        /** listeners to the root context and IDE settings */
        private PropertyChangeListener weakRcL;
        private NodeListener weakNRcL;

        private NodeListener rcListener;
        /** validity flag */
        private boolean valid = true;
        private boolean rootVis = true;

        /** Used by ModuleTab to set persistence type according
         * root context node persistence ability. */
        protected int persistenceType = TopComponent.PERSISTENCE_ALWAYS;

        public ExplorerTab () {
            super();
            // complete initialization of composited explorer actions

            getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), getConfirmDelete()));

            getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (PROP_CONFIRM_DELETE.equals(evt.getKey())) {
                        getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), getConfirmDelete()));
                    }
                }
            });
        }

        private static Preferences getPreferences() {
            return NbPreferences.root().node("/org/netbeans/core");  //NOI18N
        }

        /** Getter for ConfirmDelete
         * @param true if the user should asked for confirmation of object delete, false otherwise */
        private static boolean getConfirmDelete() {
            return getPreferences().getBoolean(PROP_CONFIRM_DELETE, true);//NOI18N
        }

        /** Overriden to explicitely set persistence type of ExplorerTab
         * to PERSISTENCE_ALWAYS
         */
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ALWAYS;
        }

        /** Initialize visual content of component */
        @Override
        protected void componentShowing () {
            super.componentShowing ();

            if (view == null) {
                view = initGui ();
                view.setRootVisible(rootVis);

                view.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NbMainExplorer.class).getString("ACSN_ExplorerBeanTree"));
                view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NbMainExplorer.class).getString("ACSD_ExplorerBeanTree"));
            }
        }

        /** Performs superclass addNotify code, then delegates to
         * componentShowing if component is used outside window system.
         * Needed for proper initialization.
         */
        @Override
        public void addNotify () {
            super.addNotify();
            if (WindowManager.getDefault().findMode(this) != null) {
                return;
            }
            componentShowing();
        }

        /** Transfer focus to view. */
        @SuppressWarnings("deprecation")
        @Override
        public void requestFocus () {
            super.requestFocus();
            if (view != null) {
                view.requestFocus();
            }
        }

        /** Transfer focus to view. */
        @SuppressWarnings("deprecation")
        @Override
        public boolean requestFocusInWindow () {
            super.requestFocusInWindow();
            if (view != null) {
                return view.requestFocusInWindow();
            } else {
                return false;
            }
        }

        /** Initializes gui of this component. Subclasses can override
        * this method to install their own gui.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView v = new BeanTreeView();
            v.setUseSubstringInQuickSearch(true);
            v.setDragSource (true);
            setLayout(new BorderLayout());
            add (v);
            return v;
        }

        /** Ensures that component is valid before opening */
        @Override
        public void open() {
            setValidRootContext();

            super.open();
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

        public void setRootContext(Node rc, boolean rootVisible) {
            rootVis = rootVisible;
            if (view != null) {
                view.setRootVisible(rootVisible);
            }
            setRootContext(rc);
        }

        // #16375. Not to try to serialize explored nodes which aren't
        // serializable (getHandle returns null).
        /** Adjusts this component persistence according
         * root context node persistence ability. */
        public void adjustComponentPersistence() {
            Node.Handle handle = getExplorerManager().getRootContext().getHandle();
            if(handle == null) {
                // Not persistent.
                persistenceType = TopComponent.PERSISTENCE_NEVER;
            } else {
                // Persistent.
                persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED;
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
        @Override
        public void setName(String name) {
            super.setName(name);
            if (view != null) {
                view.getAccessibleContext().setAccessibleName(name);
            }
        }

        /* Updated accessible description of the tree view */
        @Override
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
                Exceptions.printStackTrace(pve);
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
                // root context node change
                final Node n = (Node)source;
                if (Node.PROP_DISPLAY_NAME.equals(propName) ||
                        Node.PROP_NAME.equals(propName)) {
                    // Fix #39275 start - posted to awt thread.
                    Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                setName(n.getDisplayName());
                            }
                        });
                    // fix #39275 end
                } else if (Node.PROP_ICON.equals(propName)) {
                    // Fix #39275 start - posted to awt thread.
                    Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
                            }
                        });
                    // fix #39275 end
                } else if (Node.PROP_SHORT_DESCRIPTION.equals(propName)) {
                    setToolTipText(n.getShortDescription());
                }
            }

            @SuppressWarnings("deprecation") public void nodeDestroyed(org.openide.nodes.NodeEvent nodeEvent) {
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

        /** Creator/accessor method of Runtime tab singleton. Instance is properly
         * deserialized by winsys.
         */
        public static MainTab findEnvironmentTab () {
            return (MainTab)getExplorer().createTC(
                NbPlaces.getDefault().environment(), true
            );
        }

        /** Creator/accessor method used ONLY by winsys for first time instantiation
         * of Runtime tab. Use <code>findEnvironmentTab</code> to properly deserialize
         * singleton instance.
         */
        public static MainTab createEnvironmentTab () {
            return (MainTab)getExplorer().createTC(
            NbPlaces.getDefault().environment(), false
            );
        }

        /** Overriden to explicitely set persistence type of MainTab
         * to PERSISTENCE_ALWAYS */
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ALWAYS;
        }

        @Override
        protected String preferredID () {
            return "runtime"; //NOI18N
        }

        @Override
        public HelpCtx getHelpCtx () {
            return ExplorerUtils.getHelpCtx (getExplorerManager ().getSelectedNodes (),
                    new HelpCtx (EnvironmentNode.class));
	}

        /** Deserialization of RepositoryTab */
        @Override
        public Object readResolve() throws java.io.ObjectStreamException {
            if (DEFAULT == null) {
                DEFAULT = this;
            }
            getDefaultMainTab().scheduleValidation();
            return getDefaultMainTab();
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        @Override
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

        /** Overrides superclass' version, remembers last activated
        * main tab */
        @Override
        protected void componentActivated () {
            super.componentActivated();
            lastActivated = this;
        }

        /** Registers root context in main explorer in addition to superclass'
        * version */
        @Override
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
    /** Special class for tabs added by modules to the main explorer */
    public static class ModuleTab extends MainTab {
        static final long serialVersionUID =8089827754534653731L;

        public ModuleTab() {
//	    System.out.println("NbMainExplorer.ModuleTab");
        }


        @Override
        public void setRootContext(Node root) {
            super.setRootContext(root);
            adjustComponentPersistence();
        }

        /** Overriden to explicitely set persistence type of ModuleTab
         * to selected type */
        @Override
        public int getPersistenceType() {
            return persistenceType;
        }

        /** Throws deserialized root context and sets proper node found
        * in roots set as new root context for this top component.
        * The reason for such construction is to keep the uniquennes of
        * root context node after deserialization. */
        @Override
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
        @Override
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

    /** Listener on roots, listens to changes of roots content */
    private static final class RootsListener extends Object implements ChangeListener {

        RootsListener() {}

        public void stateChanged(ChangeEvent e) {
            NbMainExplorer.getExplorer().refreshRoots();
        }
    } // end of RootsListener inner class

    public static void main (String[] args) throws Exception {
        NbMainExplorer e = new NbMainExplorer ();
        e.open ();
    }
}
