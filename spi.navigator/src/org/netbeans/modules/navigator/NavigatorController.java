/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;

/**
 *
 * @author Dafe Simonek
 */
final class NavigatorController implements LookupListener, ActionListener, Lookup.Provider {
    
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
    private static final Lookup.Template CUR_NODES = new Lookup.Template(Node.class);
    /** template for finding nav hints in actions global context */
    private static final Lookup.Template CUR_HINTS = new Lookup.Template(NavigatorLookupHint.class);
    
    /** current nodes (lookup result) to listen on when we are active */
    private Lookup.Result curNodes;
    /** current navigator hints (lookup result) to listen on when we are active */
    private Lookup.Result curHints;     
    
    /** current node to show content for */
    private Node curNode;
    /** Previous activated node */
    private WeakReference oldNodeRef;
    /** Lookup that is passed to clients */
    private final Lookup clientsLookup;
    /** Lookup that wraps lookup of active panel */
    private final Lookup panelLookup; 
    
    /** Creates a new instance of NavigatorController */
    public NavigatorController(NavigatorTC navigatorTC) {
        this.navigatorTC = navigatorTC;
        clientsLookup = Lookups.proxy(this);
        panelLookup = Lookups.proxy(new PanelLookupWrapper());
    }
    
    /** Starts listening to selected nodes */
    public void navigatorTCOpened() {
        curNodes = Utilities.actionsGlobalContext().lookup(CUR_NODES);
        curNodes.addLookupListener(this);
        curHints = Utilities.actionsGlobalContext().lookup(CUR_HINTS);
        curHints.addLookupListener(this);
        
        navigatorTC.getPanelSelector().addActionListener(this);
        curNode = obtainFirstCurNode();
        setContext(curNode);
    }
    
    /** Stops listening to selected nodes */
    public void navigatorTCClosed() {
        curNodes.removeLookupListener(this);
        curHints.removeLookupListener(this);
        navigatorTC.getPanelSelector().removeActionListener(this);
        curNodes = null;
        curHints = null;
        curNode = null;
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
        NavigatorPanel newPanel = (NavigatorPanel)navigatorTC.getPanels().get(index);
        NavigatorPanel oldPanel = navigatorTC.getSelectedPanel();
        if (!newPanel.equals(oldPanel)) {
            if (oldPanel != null) {
                oldPanel.panelDeactivated();
            }
            newPanel.panelActivated(clientsLookup);
            navigatorTC.setSelectedPanel(newPanel);
        }
    }
    
    /** Invokes navigator data context change upon current nodes change or
     * current navigator hints change,
     * performs coalescing of fast coming changes.
     */
    public void resultChanged(LookupEvent ev) {
        if (!navigatorTC.equals(WindowManager.getDefault().getRegistry().getActivated())) {
            curNode = obtainFirstCurNode();
            
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
    
    /** Important worker method, sets navigator content (available panels)
     * according to providers found in given context.
     *
     * @param node Node context, may be also null
     */
    private void setContext (Node node) {
        Node oldNode = oldNodeRef != null ? (Node)oldNodeRef.get() : null;
        // same node, do nothing...
        
        //commented out because of activated nodes problems when using NavigatorLookupHint-s.
//        if ((oldNode != null) && (oldNode.equals(node))) {
//            return;
//        }
        
        oldNodeRef = node != null ? new WeakReference(node) : null;
        
        List providers = obtainProviders(node);
        List oldProviders = navigatorTC.getPanels();

        // set Navigator's active node to be the same as the content
        // it is showing
        navigatorTC.setActivatedNodes(node == null ? new Node[0] : new Node[] { node });

        updateTCTitle(providers != null && !providers.isEmpty() ? node : null);
        
        // navigator remains empty, do nothing
        if (oldProviders == null && providers == null) {
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
            return;
        }
        
        if (selPanel != null) {
            selPanel.panelDeactivated();
        }
        if (providers != null && providers.size() > 0) {
            NavigatorPanel newSel = (NavigatorPanel)providers.get(0);
            newSel.panelActivated(clientsLookup);
        }
        // we must disable combo-box listener to not receive unwanted events
        // during combo box content change
        navigatorTC.getPanelSelector().removeActionListener(this);
        navigatorTC.setPanels(providers);
        navigatorTC.getPanelSelector().addActionListener(this);
    }

    /** Sets navigator title according to active context */
    private void updateTCTitle (Node node) {
        String newTitle;
        if (node != null) {
            newTitle = NbBundle.getMessage(
                    NavigatorTC.class, "FMT_Navigator", node.getDisplayName()  //NOI18N
            );
        } else {
            newTitle = NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator");  //NOI18N
        }
        navigatorTC.setName(newTitle);
    }
    
    /** Searches and return a list of providers which are suitable for given
     * node context. Both Node lookup registered clients and xml layer registered
     * clients are returned.
     *
     * @node Node context, may be also null.
     */
    private List obtainProviders (Node node) {
        List result = null; 
        // search in global lookup first, they had preference
        // XXX - TBD - lookup for all instances, not only one
        NavigatorLookupHint lkpHint =
                (NavigatorLookupHint)Utilities.actionsGlobalContext().lookup(NavigatorLookupHint.class);
        if (lkpHint != null) {
            List providers = ProviderRegistry.getInstance().getProviders(lkpHint.getContentType());
            if (providers != null && !providers.isEmpty()) {
                result = new ArrayList(providers.size());
                result.addAll(providers);
            }
        }
        
        // search in declarative layers
        if (node != null) {
            DataObject dObj = (DataObject)node.getLookup().lookup(DataObject.class);
            if (dObj != null) {
                String contentType = dObj.getPrimaryFile().getMIMEType();
                List providers = ProviderRegistry.getInstance().getProviders(contentType);
                if (providers != null && !providers.isEmpty()) {
                    if (result == null) {
                        result = new ArrayList(providers.size());
                    }
                    result.addAll(providers);
                }
            }
        }
        
        return result;
    }
    
    /** Impl of Lookup.Provider to provide Lookup suitable for clients of
     * Navigator API. Delegates to lookup of current node.
     *
     * Public only due to impl reasons, please treate as private.
     */ 
    public Lookup getLookup () {
        return curNode.getLookup();
    }

    /** Lookup delegating to lookup of currently selected panel.
     * If no panel is selected or panels' lookup is null, then acts as
     * dummy empty lookup.
     */ 
    private class PanelLookupWrapper implements Lookup.Provider {
        
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
                Collection nodeList = curNodes.allInstances();
                Node curNode = nodeList.isEmpty() ? null : (Node)nodeList.iterator().next();
                setContext(curNode);
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
