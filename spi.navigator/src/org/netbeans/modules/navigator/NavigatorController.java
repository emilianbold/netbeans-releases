/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.List;
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
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;

/**
 * Listen to user action and handles navigator behaviour. 
 * 
 * @author Dafe Simonek
 */
public final class NavigatorController implements LookupListener, ActionListener, Lookup.Provider,
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
    
    /** template for finding current nodes in actions global context */
    private static final Lookup.Template<Node> CUR_NODES = 
            new Lookup.Template<Node>(Node.class);
    /** template for finding nav hints in actions global context */
    private static final Lookup.Template<NavigatorLookupHint> CUR_HINTS = 
            new Lookup.Template<NavigatorLookupHint>(NavigatorLookupHint.class);
    
    /** current nodes (lookup result) to listen on when we are active */
    private Lookup.Result<Node> curNodes;
    /** current navigator hints (lookup result) to listen on when we are active */
    private Lookup.Result<NavigatorLookupHint> curHints;     
    
    /** current node to show content for */
    private Node curNode;
    /** Lookup that is passed to clients */
    private final Lookup clientsLookup;
    /** Lookup that wraps lookup of active panel */
    private final Lookup panelLookup; 
    /** Lookup result that track nodes (for activated nodes propagation) */
    private Lookup.Result<Node> panelLookupNodesResult;
    /** Listener for panel lookup content changes */
    private final LookupListener panelLookupListener;
    
    /** A TopComponent which was active in winsys before navigator */
    private Reference<TopComponent> lastActivatedRef;

    /** Listen to possible destroy of asociated curNode */
    private NodeListener weakNodeL;

    /** boolean flag to indicate whether updateContext is currently running */
    private boolean inUpdate;
    
    /** Creates a new instance of NavigatorController */
    public NavigatorController(NavigatorTC navigatorTC) {
        this.navigatorTC = navigatorTC;
        clientsLookup = Lookups.proxy(this);
        panelLookup = Lookups.proxy(new PanelLookupWrapper());
        panelLookupListener = new PanelLookupListener();
    }
    
    /** Starts listening to selected nodes and active component */
    public void navigatorTCOpened() {
        curNodes = Utilities.actionsGlobalContext().lookup(CUR_NODES);
        curNodes.addLookupListener(this);
        curHints = Utilities.actionsGlobalContext().lookup(CUR_HINTS);
        curHints.addLookupListener(this);
        navigatorTC.getPanelSelector().addActionListener(this);
        TopComponent.getRegistry().addPropertyChangeListener(this);
        panelLookupNodesResult = panelLookup.lookup(CUR_NODES);
        panelLookupNodesResult.addLookupListener(panelLookupListener);
        
        updateContext();
    }
    
    /** Stops listening to selected nodes and active component */
    public void navigatorTCClosed() {
        curNodes.removeLookupListener(this);
        curHints.removeLookupListener(this);
        navigatorTC.getPanelSelector().removeActionListener(this);
        TopComponent.getRegistry().removePropertyChangeListener(this);
        panelLookupNodesResult.removeLookupListener(panelLookupListener);
        curNodes = null;
        curHints = null;
        curNode = null;
        // #113764: mem leak fix - update lookup - force SimpleProxyLookup to free its delegate
        clientsLookup.lookup(Object.class);
        lastActivatedRef = null;
        navigatorTC.setPanels(null);
        panelLookupNodesResult = null;
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
        }
    }
    
    /** Invokes navigator data context change upon current nodes change or
     * current navigator hints change,
     * performs coalescing of fast coming changes.
     */
    public void resultChanged(LookupEvent ev) {
        if (!navigatorTC.equals(WindowManager.getDefault().getRegistry().getActivated())) {
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
    
    /** Returns first node of collection of nodes from active lookup context */
    private Node obtainFirstCurNode () {
        Collection nodeList = curNodes.allInstances();
        return nodeList.isEmpty() ? null : (Node)nodeList.iterator().next();
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
        // #105327: don't allow reentrancy, may happen due to listening to node changes
        if (inUpdate) {
            return;
        }
        inUpdate = true;
        
        // #67599,108066: Some updates runs delayed, so it's possible that
        // navigator was already closed, that's why the check
        if (curNodes == null) {
            inUpdate = false;
            return;
        }
        
        // #80155: don't empty navigator for Properties window and similar
        // which don't define activated nodes
        Node node = obtainFirstCurNode();
        if (node == null && !shouldUpdate() && !force) {
            inUpdate = false;
            return;
        }
        
        if (curNode != null && weakNodeL != null) {
            curNode.removeNodeListener(weakNodeL);
            weakNodeL = null;
        }
        
        // #63165: curNode has to be modified only in updateContext
        // body, to prevent situation when curNode is null in getLookup
        curNode = node;
        
        // #104229: listen to node destroy and update navigator correctly 
        if (curNode != null) {
            weakNodeL = WeakListeners.create(NodeListener.class, this, curNode);
            curNode.addNodeListener(weakNodeL);
        }
        
        List<NavigatorPanel> providers = obtainProviders(node);
        List oldProviders = navigatorTC.getPanels();

        final boolean areNewProviders = providers != null && !providers.isEmpty();
        
        // navigator remains empty, do nothing
        if (oldProviders == null && providers == null) {
            inUpdate = false;
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
                navigatorTC.setPanels(providers);
                navigatorTC.setSelectedPanel(selPanel);
                navigatorTC.getPanelSelector().addActionListener(this);
            }
            // #100122: update activated nodes of Navigator TC
            updateActNodesAndTitle();
            
            inUpdate = false;
            return;
        }
        
        if (selPanel != null) {
            // #61334: don't deactivate previous providers if there are no new ones
            if (!areNewProviders && !force) {
                inUpdate = false;
                return;
            }
            selPanel.panelDeactivated();
        }
        
        if (areNewProviders) {
            NavigatorPanel newSel = providers.get(0);
            newSel.panelActivated(clientsLookup);
        }
        // we must disable combo-box listener to not receive unwanted events
        // during combo box content change
        navigatorTC.getPanelSelector().removeActionListener(this);
        navigatorTC.setPanels(providers);
        navigatorTC.getPanelSelector().addActionListener(this);
        
        updateActNodesAndTitle();
        
        inUpdate = false;
    }

    /** Updates activated nodes of Navigator TopComponent and updates its
     * display name to reflect activated nodes */
    private void updateActNodesAndTitle () {
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
     * @node Node context, may be also null.
     */
    /* package private for tests */ List<NavigatorPanel> obtainProviders (Node node) {
        // obtain policy for panels if there is one
        Lookup globalContext = Utilities.actionsGlobalContext();
        NavigatorLookupPanelsPolicy panelsPolicy = globalContext.lookup(NavigatorLookupPanelsPolicy.class);
        
        List<NavigatorPanel> result = null; 
        
        // search in global lookup first, they had preference
        Collection<? extends NavigatorLookupHint> lkpHints =
                globalContext.lookupAll(NavigatorLookupHint.class);
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
        if (node != null) {
            DataObject dObj = node.getLookup().lookup(DataObject.class);
            // #64871: Follow DataShadows to their original
            while (dObj instanceof DataShadow) {
                dObj = ((DataShadow)dObj).getOriginal();
            }
            if (dObj != null) {
                FileObject fo = dObj.getPrimaryFile();
                // #65589: be no friend with virtual files
                if (!fo.isVirtual()) {
                String contentType = fo.getMIMEType();
                    Collection<? extends NavigatorPanel> providers = ProviderRegistry.getInstance().getProviders(contentType);
                    if (providers != null && !providers.isEmpty()) {
                        if (result == null) {
                            result = new ArrayList<NavigatorPanel>(providers.size());
                        }
                        for( NavigatorPanel np : providers ) {
                            if( !result.contains( np ) )
                                result.add( np );
                        }
                    }
                }
            }
        }
        
        return result;
    }

    /** Builds and returns activated nodes array for Navigator TopComponent.
     */
    private Node[] obtainActivatedNodes () {
        Collection<? extends Node> nodes = getPanelLookup().lookupAll(Node.class);
        if (nodes.isEmpty()) {
            // set Navigator's active node to be the same as the content it is showing
            return curNode == null ? new Node[0] : new Node[] { curNode };
        } else {
            return nodes.toArray(new Node[0]);
        }
    }
    
    /** Impl of Lookup.Provider to provide Lookup suitable for clients of
     * Navigator API. Delegates to lookup of current node.
     *
     * Public only due to impl reasons, please treate as private.
     */ 
    public Lookup getLookup () {
        // #63165: null check must be here, because curNode may be null sometimes, 
        // and as this lookup is given to clients, this method can be called 
        // anytime, so we can't avoid the situation where curNode is null
        if (curNode == null) {
            return Lookup.EMPTY;
        }
        return curNode.getLookup();
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
            EventQueue.invokeLater(this);
        }
    }
    
    /****** NodeListener implementation *****/
    
    public void nodeDestroyed(NodeEvent ev) {
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            EventQueue.invokeLater(this);
        }
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

    /** Lookup delegating to lookup of currently selected panel.
     * If no panel is selected or panels' lookup is null, then acts as
     * dummy empty lookup.
     */ 
    private final class PanelLookupWrapper implements Lookup.Provider {
        
        public Lookup getLookup () {
            NavigatorPanel selPanel = navigatorTC.getSelectedPanel();
            if (selPanel != null) {
                Lookup panelLkp = selPanel.getLookup();
                if (panelLkp != null) {
                    return panelLkp;
                }
            }
            return Lookup.EMPTY;
        }
        
    } // end of PanelLookupWrapper
    
    /** Listens to changes of lookup content of panel's lookup
     * (NavigatorPanel.getLookup()) and updates activated nodes.
     */ 
    private final class PanelLookupListener implements LookupListener {
        
        public void resultChanged(LookupEvent ev) {
            // #103981: update also display name of Navigator TopComp
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
                SwingUtilities.invokeLater(this);
            } else {
                // AWT thread
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

        
}
