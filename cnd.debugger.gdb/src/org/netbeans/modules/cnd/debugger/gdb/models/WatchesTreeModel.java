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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/*
 * WatchesTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's Ant Debugger implementation)
 *
 */

public class WatchesTreeModel implements TreeModel, TreeExpansionModel {

    private GdbDebugger debugger;
    private ContextProvider lookupProvider;
    private LocalsTreeModel localsTreeModel;
    private Listener listener;
    private Vector listeners = new Vector();
    private Map watchToVariable = new WeakHashMap(); // <node (expression), GdbWatchVariable>
    private Set expandedNodes = new WeakSet();
    private Set collapsedNodes = new WeakSet();
    
    
    public WatchesTreeModel(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
    }

    LocalsTreeModel getLocalsTreeModel() {
        if (localsTreeModel == null) {
            localsTreeModel = (LocalsTreeModel) lookupProvider.lookupFirst("LocalsView", TreeModel.class);
        }
        return localsTreeModel;
    }
    
    /** 
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    public Object getRoot() {
        return ROOT;
    }
    
    /** 
     * Returns children for given parent on given indexes.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            // 1) ger Watches
            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            to = Math.min(ws.length, to);
            from = Math.min(ws.length, from);
            Watch[] fws = new Watch[to - from];
            System.arraycopy(ws, from, fws, 0, to - from);
            
            // 2) create GdbWatches for Watches
            int i, k = fws.length;
            GdbWatchVariable[] gws = new GdbWatchVariable[k];
            for (i = 0; i < k; i++) {
                GdbWatchVariable gw = (GdbWatchVariable) watchToVariable.get(fws[i]);
                if (gw == null) {
                    gw = new GdbWatchVariable(this, fws[i]);
                    watchToVariable.put(fws[i], gw);
                }
                gws[i] = gw;
            }
            
            if (listener == null) {
                listener = new Listener(this, debugger);
            }
            return gws;
        } else if (parent instanceof GdbWatchVariable) {
            return getLocalsTreeModel().getChildren(parent, from, to);
        }
        return getLocalsTreeModel().getChildren(parent, from, to);
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        if (node instanceof Watch) {
            return true;
        }
        if (node instanceof GdbWatchVariable) {
            return ((GdbWatchVariable) node).getFieldsCount() == 0;
        }
        throw new UnknownTypeException(node);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     * @since 1.1
     */
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return DebuggerManager.getDebuggerManager().getWatches().length;
        }
        throw new UnknownTypeException(node);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTreeChanged() {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged (event);
        }
    }
    
    private void fireWatchesChanged() {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        ModelEvent event = new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(event);
        }
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        synchronized (watchToVariable) {
            for (Iterator it = watchToVariable.keySet().iterator(); it.hasNext(); ) {
                Object w = it.next();
                if (node.equals(watchToVariable.get(w))) {
                    System.err.println("WTM.fireTableValueChanged:" + ((GdbWatchVariable) node).getName());
//                    ((GdbWatchVariable) node).setEvaluated(null);
                    break;
                }
            }
        }
        fireTableValueChangedComputed(node, propertyName);
    }
        
    void fireTableValueChangedComputed (Object node, String propertyName) {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.TableValueChanged(this, node, propertyName));
        }
    }
  
    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    public boolean isExpanded(Object node) throws UnknownTypeException {
        synchronized (this) {
            if (expandedNodes.contains(node)) {
                return true;
            }
            if (collapsedNodes.contains(node)) {
                return false;
            }
        }
        // Default behavior follows:
        if (node instanceof AbstractVariable) {
            return false;
        }
        throw new UnknownTypeException(node);
    }
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public void nodeExpanded(Object node) {
        if (node instanceof AbstractVariable) {
            AbstractVariable var = (AbstractVariable) node;
            if (var.expandChildren()) {
                fireTreeChanged();
            }
        }
        synchronized (this) {
            expandedNodes.add(node);
            collapsedNodes.remove(node);
        }
    }
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    public void nodeCollapsed(Object node) {
        synchronized (this) {
            collapsedNodes.add(node);
            expandedNodes.remove(node);
        }
    }
    
    private static class Listener extends DebuggerManagerAdapter implements PropertyChangeListener {
        
        private WeakReference model;
        private WeakReference debugger;
        
        private Listener(WatchesTreeModel tm, GdbDebugger debugger) {
            model = new WeakReference(tm);
            this.debugger = new WeakReference(debugger);
            DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            debugger.addPropertyChangeListener(this);
            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            int i, k = ws.length;
            for (i = 0; i < k; i++) {
                ws [i].addPropertyChangeListener(this);
            }
        }
        
        private WatchesTreeModel getModel() {
            WatchesTreeModel m = (WatchesTreeModel) model.get();
            if (m == null) {
                destroy();
            }
            return m;
        }
        
        public void watchAdded(Watch watch) {
            WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            watch.addPropertyChangeListener(this);
            m.fireWatchesChanged();
        }
        
        public void watchRemoved(Watch watch) {
            WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            Object o = m.watchToVariable.remove(watch); // FIXME - check that o is non-null
            watch.removePropertyChangeListener(this);
            m.fireWatchesChanged();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if (DebuggerManager.PROP_WATCHES.equals(propName)) {
                return;
            }
            final WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            if (m.debugger.getState() == GdbDebugger.STATE_EXITED) {
                destroy();
                return;
            }
            if (m.debugger.getState() == GdbDebugger.STATE_RUNNING) {
                return;
            }
            
            if (evt.getSource() instanceof Watch) {
                Object node;
                synchronized (m.watchToVariable) {
                    node = m.watchToVariable.get(evt.getSource());
                }
                if (node != null) {
                    m.fireTableValueChangedChanged(node, null);
                    return;
                }
            }
            
            if (task == null) {
                task = RequestProcessor.getDefault().create(new Runnable() {
                    public void run() {
                        m.fireTreeChanged();
                    }
                });
            }
            task.schedule(100);
        }
        
        private void destroy() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            GdbDebugger d = (GdbDebugger) debugger.get();
            if (d != null) {
                d.removePropertyChangeListener(this);
            }

            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            int i, k = ws.length;
            for (i = 0; i < k; i++) {
                ws [i].removePropertyChangeListener(this);
            }

            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
    }
    
}
