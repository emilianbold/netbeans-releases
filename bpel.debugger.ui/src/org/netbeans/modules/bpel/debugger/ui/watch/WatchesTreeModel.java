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

package org.netbeans.modules.bpel.debugger.ui.watch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.variable.HelperTreeModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 * Tree model for BPEL variables.
 * 
 * @author Sun Microsystems
 * @author Sun Microsystems
 */
public class WatchesTreeModel implements TreeModel {
    public static final String VIEW_NAME = "WatchesView";

    private BpelDebugger myDebugger;
    private Listener myListener;
    private Vector myListeners = new Vector();
    private ContextProvider myContextProvider;
    private HelperTreeModel myHelperTreeModel;

    private Map<Watch, BpelWatchImpl> myWatchToValue =
            new WeakHashMap<Watch, BpelWatchImpl>();
    
    /**
     * Creates a new instance of LocalsTreeModel.
     *
     * @param lookupProvider debugger context
     */
    public WatchesTreeModel(ContextProvider contextProvider) {
        myContextProvider = contextProvider;
        myDebugger = 
            (BpelDebugger) contextProvider.lookupFirst(null, BpelDebugger.class);
    }

    
    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object object, int from, int to) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            return getWatches(from, to);
        } else if (object instanceof BpelWatchImpl) {
            BpelWatchImpl bpelWatch = (BpelWatchImpl)object;
            if (bpelWatch.getValue() != null) {
                return getHelperTreeModel().getChildren(bpelWatch.getValue(), from, to);
            } else {
                return new Object[0];
            }
        } else {
            throw new UnknownTypeException(object);
        }
    }

    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount(Object object) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            if (myListener == null) {
                myListener = new Listener(this, myDebugger);
            }
            return getWatchCount();
        } else if (object instanceof BpelWatchImpl) {
            BpelWatchImpl bpelWatch = (BpelWatchImpl)object;
            if (bpelWatch.getValue() != null) {
                return getHelperTreeModel().getChildrenCount(bpelWatch.getValue());
            } else {
                return 0;
            }
        } else {
            throw new UnknownTypeException(object);
        }
    }

    public boolean isLeaf(Object object) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            return false;
        } else if (object instanceof BpelWatchImpl) {
            return getChildrenCount(object) == 0;
        } else {
            throw new UnknownTypeException(object);
        }
    }

    public void addModelListener(ModelListener l) {
        myListeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        myListeners.remove(l);
    }
    
    private Object[] getWatches(int from, int to) {
        Watch[] nbWatches = DebuggerManager.getDebuggerManager().getWatches();
        BpelWatchImpl[] bpelWatches = new BpelWatchImpl[to - from];
        int j = 0;
        for (int i = from; i < to; i++) {
            BpelWatchImpl bpelWatch = myWatchToValue.get(nbWatches[i]);
            if (bpelWatch == null) {
                bpelWatch = new BpelWatchImpl(myDebugger, nbWatches[i]);
                myWatchToValue.put(nbWatches[i], bpelWatch);
            }
            bpelWatches[j++] = bpelWatch;
        }

        if (myListener == null) {
            myListener = new Listener(this, myDebugger);
        }

        return bpelWatches;
    }
    
    private HelperTreeModel getHelperTreeModel() {
        if (myHelperTreeModel == null) {
            List models = myContextProvider.lookup(VIEW_NAME, TreeModel.class);
            for (Object model : models) {
                if (model instanceof HelperTreeModel) {
                    myHelperTreeModel = (HelperTreeModel)model;
                    break;
                }
            }
        }
        return myHelperTreeModel;
    }

    private void fireWatchesChanged() {
        Vector v = (Vector)myListeners.clone();
        int i, k = v.size();
        ModelEvent event = new ModelEvent.NodeChanged(
                this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (i = 0; i < k; i++) {
            ((ModelListener)v.get (i)).modelChanged (event);
        }
    }

    private void fireTreeChanged () {
        synchronized (myWatchToValue) {
            myWatchToValue.clear();
        }
        Vector v = (Vector) myListeners.clone();
        int i, k = v.size();
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(event);
        }
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        //TODO:ugly hack to fix #82191 - see comments
        //
        fireTreeChanged();
//        synchronized(myWatchToValue) {
//            for (Object w : myWatchToValue.keySet()) {
//                if (node.equals(myWatchToValue.get(w))) {
//                    myWatchToValue.remove(w);
//                    break;
//                }
//            }
//        }
//        fireTableValueChangedComputed(node, propertyName);
    }
    
    void fireTableValueChangedComputed (Object node, String propertyName) {
        Vector v = (Vector)myListeners.clone ();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(
                    new ModelEvent.TableValueChanged (this, node, propertyName));
        }
    }
    
    // private methods .........................................................

    private BpelDebugger getDebugger() {
        return myDebugger;
    }

    private int getWatchCount() {
        return DebuggerManager.getDebuggerManager().
                getWatches().length;
    }


    // innerclasses ............................................................

    private static class Listener
            extends DebuggerManagerAdapter 
            implements PropertyChangeListener
    {
        private WeakReference myDebugger;
        private WeakReference myModel;

        private Listener(WatchesTreeModel tm, BpelDebugger debugger) {
            myModel = new WeakReference(tm);
            myDebugger = new WeakReference(debugger);
            
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                    DebuggerManager.PROP_WATCHES, this);
            debugger.addPropertyChangeListener(this);
            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws[i].addPropertyChangeListener(this);
        }

        private WatchesTreeModel getModel() {
            WatchesTreeModel m = (WatchesTreeModel)myModel.get();
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
        
        public void watchRemoved (Watch watch) {
            WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            watch.removePropertyChangeListener(this);
            //TODO:shouldn't we remove evaluated watch first?
            m.fireWatchesChanged();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if (DebuggerManager.PROP_WATCHES.equals(propName)) {
                return ;
            }
            
            final WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            
            if (m.myDebugger.getState() == BpelDebugger.STATE_DISCONNECTED) {
                destroy();
                return;
            }
            
            if (evt.getSource() instanceof Watch) {
                Object node;
                synchronized (m.myWatchToValue) {
                    node = m.myWatchToValue.get(evt.getSource());
                }
                if (node != null) {
                    m.fireTableValueChangedChanged(node, null);
                    return ;
                }
            }
            
            if (propName == BpelDebugger.PROP_CURRENT_POSITION) {
                final WatchesTreeModel wtm = getModel();
                if (wtm == null) {
                    return;
                }
                
                if (task != null) {
                    // cancel old task
                    task.cancel();
                    task = null;
                }
                
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        wtm.fireTreeChanged();
                    }
                }, 500);
            }
        }
        
        void destroy() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener (
                    DebuggerManager.PROP_WATCHES, this);
            
            BpelDebugger d = (BpelDebugger)myDebugger.get();
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
