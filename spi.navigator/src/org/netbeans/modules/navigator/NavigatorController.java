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

package org.netbeans.modules.navigator;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.nodes.NodeListener;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

/**
 * Listen to user action and handles navigator behaviour.
 *
 * @author Dafe Simonek
 */
public final class NavigatorController implements LookupListener, ActionListener,
                                                    PropertyChangeListener, NodeListener, Runnable {

    /** Time in ms to wait before propagating current node changes further
     * into navigator UI */
    /* package private for tests */
    static final int COALESCE_TIME = 100;

    /** Asociation with navigator UI, which we control */
    private NavigatorTC navigatorTC;

    /** holds currently scheduled/running task for set data context of selected node */
    private RequestProcessor.Task nodeSetterTask;
    private final Object NODE_SETTER_LOCK = new Object();

    private final Object CUR_NODES_LOCK = new Object();

    /** template for finding current nodes in actions global context */
    private static final Lookup.Template<Node> CUR_NODES =
            new Lookup.Template<Node>(Node.class);
    /** template for finding nav hints in actions global context */
    private static final Lookup.Template<NavigatorLookupHint> CUR_HINTS =
            new Lookup.Template<NavigatorLookupHint>(NavigatorLookupHint.class);

    /** current nodes (lookup result) to listen on when we are active */
    private Lookup.Result<Node> curNodesRes;
    /** current navigator hints (lookup result) to listen on when we are active */
    private Lookup.Result<NavigatorLookupHint> curHintsRes;

    /** current nodes to show content for */
    private Collection<? extends Node> curNodes = Collections.emptyList();
    /** Lookup that is passed to clients */
    private final ClientsLookup clientsLookup;
    /** Lookup that wraps lookup of active panel */
    private final Lookup panelLookup;
    /** Lookup result that track nodes (for activated nodes propagation) */
    private Lookup.Result<Node> panelLookupNodesResult;
    /** Listener for panel lookup content changes */
    private final LookupListener panelLookupListener;

    /** A TopComponent which was active in winsys before navigator */
    private Reference<TopComponent> lastActivatedRef;

    /** Listen to possible destroy of asociated curNodes */
    private List<NodeListener> weakNodesL = Collections.emptyList();

    /** boolean flag to indicate whether updateContext is currently running */
    private boolean inUpdate;

    private static final Logger LOG = Logger.getLogger(NavigatorController.class.getName());

    /** Creates a new instance of NavigatorController */
    public NavigatorController(NavigatorTC navigatorTC) {
        this.navigatorTC = navigatorTC;
        clientsLookup = new ClientsLookup();
        panelLookup = Lookups.proxy(new PanelLookupWrapper());
        panelLookupListener = new PanelLookupListener();
    }

    /** Starts listening to selected nodes and active component */
    public void navigatorTCOpened() {
        LOG.fine("Entering navigatorTCOpened");
        curNodesRes = Utilities.actionsGlobalContext().lookup(CUR_NODES);
        curNodesRes.addLookupListener(this);
        curHintsRes = Utilities.actionsGlobalContext().lookup(CUR_HINTS);
        curHintsRes.addLookupListener(this);
        navigatorTC.getPanelSelector().addActionListener(this);
        TopComponent.getRegistry().addPropertyChangeListener(this);
        panelLookupNodesResult = panelLookup.lookup(CUR_NODES);
        panelLookupNodesResult.addLookupListener(panelLookupListener);

        updateContext();
    }

    /** Stops listening to selected nodes and active component */
    public void navigatorTCClosed() {
        LOG.fine("Entering navigatorTCClosed");
        curNodesRes.removeLookupListener(this);
        curHintsRes.removeLookupListener(this);
        navigatorTC.getPanelSelector().removeActionListener(this);
        TopComponent.getRegistry().removePropertyChangeListener(this);
        panelLookupNodesResult.removeLookupListener(panelLookupListener);
        curNodesRes = null;
        curHintsRes = null;
        synchronized (CUR_NODES_LOCK) {
            curNodes = Collections.emptyList();
        }
        weakNodesL = Collections.emptyList();
        // #113764: mem leak fix - update lookup - force ClientsLookup to free its delegates
        clientsLookup.lookup(Object.class);
        // #104145: panelDeactivated called if needed
        NavigatorPanel selPanel = navigatorTC.getSelectedPanel();
        if (selPanel != null) {
            selPanel.panelDeactivated();
        }
        lastActivatedRef = null;
        navigatorTC.setPanels(null, null);
        panelLookupNodesResult = null;
        LOG.fine("navigatorTCClosed: activated nodes: " + navigatorTC.getActivatedNodes());
        if (navigatorTC.getActivatedNodes() != null) {
            LOG.fine("navigatorTCClosed: clearing act nodes...");
            navigatorTC.setActivatedNodes(new Node[0]);
        }
    }

    /** Returns lookup that delegates to lookup of currently active
     * navigator panel
     */
    public Lookup getPanelLookup () {
        return panelLookup;
    }

    /** Reacts on user selecting some new Navigator panel in panel selector
     * combo box - shows the panel user has selected.
     */
    public void actionPerformed (ActionEvent e) {
        int index = navigatorTC.getPanelSelector().getSelectedIndex();
        if (index == -1) {
            // combo box cleared, nothing to activate
            return;
        }
        NavigatorPanel newPanel = navigatorTC.getPanels().get(index);
        activatePanel(newPanel);
    }

    /** Activates given panel. Throws IllegalArgumentException if panel is
     * not available for activation.
     */
    public void activatePanel (NavigatorPanel panel) {
        if (!navigatorTC.getPanels().contains(panel)) {
            throw new IllegalArgumentException("Panel is not available for activation: " + panel); //NOI18N
        }
        NavigatorPanel oldPanel = navigatorTC.getSelectedPanel();
        if (!panel.equals(oldPanel)) {
            if (oldPanel != null) {
                oldPanel.panelDeactivated();
            }
            panel.panelActivated(clientsLookup);
            navigatorTC.setSelectedPanel(panel);
            // selected panel changed, update selPanelLookup to listen correctly
            panelLookup.lookup(Object.class);
            cacheLastSelPanel(panel);
        }
    }

    /** Invokes navigator data context change upon current nodes change or
     * current navigator hints change,
     * performs coalescing of fast coming changes.
     */
    public void resultChanged(LookupEvent ev) {
        if (!navigatorTC.equals(WindowManager.getDefault().getRegistry().getActivated())
                // #117089: allow node change when we are empty
                || (curNodes == null || curNodes.isEmpty())) {
            ActNodeSetter nodeSetter = new ActNodeSetter();
            synchronized (NODE_SETTER_LOCK) {
                if (nodeSetterTask != null) {
                    nodeSetterTask.cancel();
                }
                // wait some time before propagating the change further
                nodeSetterTask = RequestProcessor.getDefault().post(nodeSetter, COALESCE_TIME);
                nodeSetterTask.addTaskListener(nodeSetter);
            }
        }
    }

    /** @return True when update show be performed, false otherwise. Update
     * isn't needed when current nodes are null and no navigator lookup hints
     * in lookup.
     */
    private boolean shouldUpdate () {
        return TopComponent.getRegistry().getCurrentNodes() != null ||
               Utilities.actionsGlobalContext().lookup(NavigatorLookupHint.class) != null;
    }

    private void updateContext () {
        updateContext(false);
    }


    /** Important worker method, sets navigator content (available panels)
     * according to providers found in current lookup context.
     *
     * @force if true that update is forced even if it means clearing navigator content
     */
    private void updateContext (boolean force) {
        LOG.fine("updateContext entered, force: " + force);
        // #105327: don't allow reentrancy, may happen due to listening to node changes
        if (inUpdate) {
            LOG.fine("Exit because inUpdate already, force: " + force);
            return;
        }
        inUpdate = true;

        // #67599,108066: Some updates runs delayed, so it's possible that
        // navigator was already closed, that's why the check
        if (curNodesRes == null) {
            inUpdate = false;
            LOG.fine("Exit because curNodesRes is null, force: " + force);
            return;
        }

        // #80155: don't empty navigator for Properties window and similar
        // which don't define activated nodes
        Collection<? extends Node> nodes = curNodesRes.allInstances();
        if (nodes.isEmpty() && !shouldUpdate() && !force) {
            inUpdate = false;
            LOG.fine("Exit because act nodes empty, force: " + force);
            return;
        }

        synchronized (CUR_NODES_LOCK) {
            // detach node listeners
            Iterator<? extends NodeListener> curL = weakNodesL.iterator();
            for (Iterator<? extends Node> curNode = curNodes.iterator(); curNode.hasNext(); ) {
                curNode.next().removeNodeListener(curL.next());
            }
            weakNodesL = new ArrayList<NodeListener> (nodes.size());

            // #63165: curNode has to be modified only in updateContext
            // body, to prevent situation when curNode is null in getLookup
            curNodes = nodes;
            LOG.fine("new CurNodes size " + curNodes.size());

            // #104229: listen to node destroy and update navigator correctly
            NodeListener weakNodeL = null;
            for (Node curNode : curNodes) {
                weakNodeL = WeakListeners.create(NodeListener.class, this, curNode);
                weakNodesL.add(weakNodeL);
                curNode.addNodeListener(weakNodeL);
            }
        }

        List<NavigatorPanel> providers = obtainProviders(nodes);
        List oldProviders = navigatorTC.getPanels();

        final boolean areNewProviders = providers != null && !providers.isEmpty();

        // navigator remains empty, do nothing
        if (oldProviders == null && providers == null) {
            inUpdate = false;
            LOG.fine("Exit because nav remain empty, force: " + force);
            return;
        }

        NavigatorPanel selPanel = navigatorTC.getSelectedPanel();

        // don't call panelActivated/panelDeactivated if the same provider is
        // still available, it's client's responsibility to listen to
        // context changes while active
        if (oldProviders != null && oldProviders.contains(selPanel) &&
            providers != null && providers.contains(selPanel)) {
            // trigger resultChanged() call on client side
            clientsLookup.lookup(Node.class);
            // #93123: refresh providers list if needed
            if (!oldProviders.equals(providers)) {
                // we must disable combo-box listener to not receive unwanted events
                // during combo box content change
                navigatorTC.getPanelSelector().removeActionListener(this);
                navigatorTC.setPanels(providers, null);
                navigatorTC.setSelectedPanel(selPanel);
                navigatorTC.getPanelSelector().addActionListener(this);
            }
            // #100122: update activated nodes of Navigator TC
            updateActNodesAndTitle();

            LOG.fine("Exit because same provider and panel, notified. Force: " + force);
            inUpdate = false;
            return;
        }

        if (selPanel != null) {
            // #61334: don't deactivate previous providers if there are no new ones
            if (!areNewProviders && !force && null != providers) {
                inUpdate = false;
                LOG.fine("Exit because no new providers, force: " + force);
                return;
            }
            selPanel.panelDeactivated();
        }

        // #67849: curNode's lookup cleanup, held through ClientsLookup delegates
        clientsLookup.lookup(Node.class);

        NavigatorPanel newSel = null;
        if (areNewProviders) {
            newSel = getLastSelPanel(providers);
            if (newSel == null) {
                newSel = providers.get(0);
            }
            newSel.panelActivated(clientsLookup);
        }
        // we must disable combo-box listener to not receive unwanted events
        // during combo box content change
        navigatorTC.getPanelSelector().removeActionListener(this);
        navigatorTC.setPanels(providers, newSel);
        // selected panel changed, update selPanelLookup to listen correctly
        panelLookup.lookup(Object.class);
        navigatorTC.getPanelSelector().addActionListener(this);

        updateActNodesAndTitle();

        LOG.fine("Normal exit, change to new provider, force: " + force);
        inUpdate = false;
    }

    /** Updates activated nodes of Navigator TopComponent and updates its
     * display name to reflect activated nodes */
    private void updateActNodesAndTitle () {
        LOG.fine("updateActNodesAndTitle called...");
        Node[] actNodes = obtainActivatedNodes();
        navigatorTC.setActivatedNodes(actNodes);
        updateTCTitle(actNodes);
    }

    /** Sets navigator title according to active context */
    private void updateTCTitle (Node[] nodes) {
        String newTitle;
        if (nodes != null && nodes.length > 0) {
            newTitle = NbBundle.getMessage(
                    NavigatorTC.class, "FMT_Navigator", nodes[0].getDisplayName()  //NOI18N
            );
        } else {
            newTitle = NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator");  //NOI18N
        }
        navigatorTC.setDisplayName(newTitle);
    }

    /** Searches and return a list of providers which are suitable for given
     * node context. Both Node lookup registered clients and xml layer registered
     * clients are returned.
     *
     * @node Nodes collection context, may be empty.
     */
    /* package private for tests */ List<NavigatorPanel> obtainProviders (Collection<? extends Node> nodes) {
        // obtain policy for panels if there is one
        Lookup globalContext = Utilities.actionsGlobalContext();
        NavigatorLookupPanelsPolicy panelsPolicy = globalContext.lookup(NavigatorLookupPanelsPolicy.class);

        List<NavigatorPanel> result = null;

        // search in global lookup first, they had preference
        Collection<? extends NavigatorLookupHint> lkpHints = globalContext.lookupAll(NavigatorLookupHint.class);
        for (NavigatorLookupHint curHint : lkpHints) {
            Collection<? extends NavigatorPanel> providers = ProviderRegistry.getInstance().getProviders(curHint.getContentType());
            if (providers != null && !providers.isEmpty()) {
                if (result == null) {
                        result = new ArrayList<NavigatorPanel>(providers.size() * lkpHints.size());
                }
                for( NavigatorPanel np : providers ) {
                    if( !result.contains( np ) )
                        result.add( np );
                }
            }
        }

        // #100457: exclude Node/DataObject providers if requested
        if (panelsPolicy != null &&
                panelsPolicy.getPanelsPolicy() == NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY) {
            return result;
        }

        // search based on Node/DataObject's primary file mime type
        List<NavigatorPanel> fileResult = null;
        for (Node node : nodes) {
            DataObject dObj = node.getLookup().lookup(DataObject.class);
            // #64871: Follow DataShadows to their original
            while (dObj instanceof DataShadow) {
                dObj = ((DataShadow)dObj).getOriginal();
            }
            if (dObj == null) {
                fileResult = null;
                break;
            }

            FileObject fo = dObj.getPrimaryFile();
            // #65589: be no friend with virtual files
            if (fo.isVirtual()) {
                fileResult = null;
                break;
            }

            String contentType = fo.getMIMEType();
            Collection<? extends NavigatorPanel> providers = ProviderRegistry.getInstance().getProviders(contentType);
            if (providers == null || providers.isEmpty()) {
                fileResult = null;
                break;
            }
            LOG.fine("File mime type providers size: " + providers.size());
            if (fileResult == null) {
                fileResult = new ArrayList<NavigatorPanel>(providers.size());
                fileResult.addAll(providers);
            } else {
                fileResult.retainAll(providers);
            }
        }

        if (result != null) {
            if (fileResult != null) {
                for (NavigatorPanel np : fileResult) {
                    if( !result.contains( np ) )
                        result.add( np );
                }
            }
        } else {
            result = fileResult;
        }

        return result;
    }

    /** Builds and returns activated nodes array for Navigator TopComponent.
     */
    private Node[] obtainActivatedNodes () {
        Lookup selLookup = getSelectedPanelLookup();
        if (selLookup == null) {
            // set Navigator's active node to be the same as the content
            // it is showing if no lookup from selected panel
            return curNodes.toArray(new Node[0]);
        } else {
            return selLookup.lookupAll(Node.class).toArray(new Node[0]);
        }
    }

    /** Retrieves and returns UndoRedo support from selected panel if panel
     * offers UndoRedo (implements NavigatorPanelWithUndo).
     */
    UndoRedo getUndoRedo () {
        NavigatorPanel panel = navigatorTC.getSelectedPanel();
        if (panel == null || !(panel instanceof NavigatorPanelWithUndo)) {
            return UndoRedo.NONE;
        }
        return ((NavigatorPanelWithUndo)panel).getUndoRedo();
    }

    /** Installs user actions handling for NavigatorTC top component */
    public void installActions () {
        // ESC key handling - return focus to previous focus owner
        KeyStroke returnKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        //JComponent contentArea = navigatorTC.getContentArea();
        navigatorTC.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(returnKey, "return"); //NOI18N
        navigatorTC.getActionMap().put("return", new ESCHandler()); //NOI18N
    }

    /***** PropertyChangeListener implementation *******/

    /** Stores last TopComponent activated before NavigatorTC. Used to handle
     * ESC key functionality */
    public void propertyChange(PropertyChangeEvent evt) {
        // careful here, note that prop changes coming here both from
        // TopComponent.Registry and currently asociated Node

        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            TopComponent tc = TopComponent.getRegistry().getActivated();
            if (tc != null && tc != navigatorTC) {
                lastActivatedRef = new WeakReference<TopComponent>(tc);
            }
        } else if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
            // force update context if some tc was closed
            // invokeLater to let node change perform before calling update
            LOG.fine("Component closed, invoking update through invokeLater...");
            // #124061 - force navigator cleanup in special situation
            TopComponent tc = TopComponent.getRegistry().getActivated();
            if (tc == navigatorTC) {
                LOG.fine("navigator active, clearing its activated nodes");
                navigatorTC.setActivatedNodes(new Node[0]);
            }

            EventQueue.invokeLater(this);
        }
    }

    /****** NodeListener implementation *****/

    public void nodeDestroyed(NodeEvent ev) {
        LOG.fine("Node destroyed reaction...");
        // #121944: don't react on node destroy when we are active
        if (navigatorTC.equals(WindowManager.getDefault().getRegistry().getActivated())) {
            LOG.fine("NavigatorTC active, skipping node destroyed reaction.");
            return;
        }
        LOG.fine("invokeLater on updateContext from node destroyed reaction...");
        // #122257: update content later to fight possible deadlocks
        EventQueue.invokeLater(this);
    }

    public void childrenAdded(NodeMemberEvent ev) {
        // no operation
    }

    public void childrenRemoved(NodeMemberEvent ev) {
        // no operation
    }

    public void childrenReordered(NodeReorderEvent ev) {
        // no operation
    }

    /** Runnable implementation - forces update */
    public void run() {
        updateContext(true);
    }

    /** Remembers given panel for current context type */
    private void cacheLastSelPanel (NavigatorPanel panel) {
        String mime = findMimeForContext();
        if (mime != null) {
            String className = panel.getClass().getName();
            NbPreferences.forModule(NavigatorController.class).put(mime, className);
            LOG.fine("cached " + className + "for mime " + mime);
        }
    }

    /** Finds last selected panel for current context type */
    private NavigatorPanel getLastSelPanel (List<NavigatorPanel> panels) {
        String mime = findMimeForContext();
        if (mime == null) {
            return null;
        }
        String className = NbPreferences.forModule(NavigatorController.class).get(mime, null);
        if (className == null) {
            return null;
        }
        LOG.fine("found cached " + className + "for mime " + mime);
        for (NavigatorPanel curPanel : panels) {
            if (className.equals(curPanel.getClass().getName())) {
                LOG.fine("returning cached " + className + "for mime " + mime);
                return curPanel;
            }
        }
        return null;
    }

    /** Returns current context type or null if not available */
    private String findMimeForContext () {
        // try hints first, they have preference
        if (curHintsRes != null) {
            Collection<? extends NavigatorLookupHint> hints = curHintsRes.allInstances();
            if (!hints.isEmpty()) {
                return hints.iterator().next().getContentType();
            }
        }
        FileObject fob = getClientsLookup().lookup(FileObject.class);
        if (fob != null) {
            return fob.getMIMEType();
        }

        return null;
    }

    /** Handles ESC key request - returns focus to previously focused top component
     */
    private class ESCHandler extends AbstractAction {
        public void actionPerformed (ActionEvent evt) {
            Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
            // move focus away only from navigator AWT children,
            // but not combo box to preserve its ESC functionality
            if (lastActivatedRef == null ||
                focusOwner == null ||
                !SwingUtilities.isDescendingFrom(focusOwner, navigatorTC) ||
                focusOwner instanceof JComboBox) {
                return;
            }
            TopComponent prevFocusedTc = lastActivatedRef.get();
            if (prevFocusedTc != null) {
                prevFocusedTc.requestActive();
            }
        }
    } // end of ESCHandler

    /** Returns lookup of selected panel or null */
    private Lookup getSelectedPanelLookup () {
        NavigatorPanel selPanel = navigatorTC.getSelectedPanel();
        if (selPanel != null) {
            Lookup panelLkp = selPanel.getLookup();
            if (panelLkp != null) {
                return panelLkp;
            }
        }
        return null;
    }

    /** Lookup delegating to lookup of currently selected panel.
     * If no panel is selected or panels' lookup is null, then acts as
     * dummy empty lookup.
     */
    private final class PanelLookupWrapper implements Lookup.Provider {

        public Lookup getLookup () {
            Lookup selLookup = getSelectedPanelLookup();
            return selLookup != null ? selLookup : Lookup.EMPTY;
        }

    } // end of PanelLookupWrapper

    /** Listens to changes of lookup content of panel's lookup
     * (NavigatorPanel.getLookup()) and updates activated nodes.
     */
    private final class PanelLookupListener implements LookupListener, Runnable {

        public void resultChanged(LookupEvent ev) {
            if (SwingUtilities.isEventDispatchThread()) {
                run();
            } else {
                SwingUtilities.invokeLater(this);
            }
        }

        public void run() {
            updateActNodesAndTitle();
        }

    } // end of PanelLookupListener

    /** Task to set given node (as data context). Used to be able to coalesce
     * data context changes if selected nodes changes too fast.
     * Listens to own finish for cleanup */
    private class ActNodeSetter implements Runnable, TaskListener {

        public void run() {
            // technique to share one runnable impl between RP and Swing,
            // to save one inner class
            if (RequestProcessor.getDefault().isRequestProcessorThread()) {
                LOG.fine("invokeLater on updateContext from ActNodeSetter");
                SwingUtilities.invokeLater(this);
            } else {
                // AWT thread
                LOG.fine("Calling updateContext from ActNodeSetter");
                updateContext();
            }
        }

        public void taskFinished(Task task) {
            synchronized (NODE_SETTER_LOCK) {
                if (task == nodeSetterTask) {
                    nodeSetterTask = null;
                }
            }
        }

    } // end of ActNodeSetter


    /** accessor for tests */
    ClientsLookup getClientsLookup () {
        return clientsLookup;
    }

    /** Lookup that holds context for clients, for NavigatorPanel implementors.
     * It's proxy lookup that delegates to lookups of current nodes */
    /* package private for tests */ class ClientsLookup extends ProxyLookup {

        @Override
        protected void beforeLookup(Template<?> template) {
            super.beforeLookup(template);

            Lookup[] curNodesLookups;

            synchronized (CUR_NODES_LOCK) {
                curNodesLookups = new Lookup[curNodes.size()];
                int i = 0;
                for (Iterator<? extends Node> it = curNodes.iterator(); it.hasNext(); i++) {
                    curNodesLookups[i] = it.next().getLookup();
                }
            }

            setLookups(curNodesLookups);
        }

        /** for tests */
        Lookup[] obtainLookups () {
            return getLookups();
        }

    } // end of ClientsLookup

}
