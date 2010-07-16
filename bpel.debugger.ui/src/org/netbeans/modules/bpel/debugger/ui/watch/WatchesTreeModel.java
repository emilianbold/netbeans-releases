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
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementValue;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Node;

/**
 * Tree model for BPEL variables.
 * 
 * @author Sun Microsystems
 */
public class WatchesTreeModel implements TreeModel, Constants {
    
    private ContextProvider myContextProvider;
    private BpelDebugger myDebugger;
    private Util myHelper;
    
    private Listener myListener;
    private Vector myListeners = new Vector();
    
    private Map<Watch, BpelWatch> myWatchToValue =
            new WeakHashMap<Watch, BpelWatch>();

    public static final Object ADD_NEW_WATCH = new Object();
    
    public WatchesTreeModel(
            final ContextProvider contextProvider) {
        
        myContextProvider = contextProvider;
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new Util(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public Object getRoot() {
        return ROOT;
    }
    
    /**{@inheritDoc}*/
    public Object[] getChildren(
            final Object object, 
            final int from, 
            final int to) throws UnknownTypeException {
        
        if (object.equals(ROOT)) {
            return getWatches(from, to);
        }
        
        if (object instanceof BpelWatch) {
            final BpelWatch bpelWatch = (BpelWatch) object;
            final Value value = bpelWatch.getValue();
            
            if (bpelWatch.getValue() != null) {
                if (value instanceof XmlElementValue) {
                    return myHelper.getChildren((XmlElementValue) value);
                }
                
                return new Object[0];
            }
            
            // If we did not get the value in an ordinary way, it could be 
            // a variable expression. Check if it starts with a "$", 
            // prepend if it not and try to fetch the value
            String expression = bpelWatch.getExpression();
            if (!expression.startsWith("$")) {
                expression = "$" + expression;
            }
            
            return myHelper.getChildren(expression);
        }
        
        if (object instanceof NamedValueHost) {
            return myHelper.getVariablesUtil().getChildren(object);
        }
        
        if (object instanceof Node) {
            return myHelper.getVariablesUtil().getChildren(object);
        }
        
        throw new UnknownTypeException(object);
    }

    /**{@inheritDoc}*/
    public int getChildrenCount(
            final Object object) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            if (myListener == null) {
                myListener = new Listener(this, myDebugger);
            }
            
            return getWatchCount();
        }
        
        if (object instanceof BpelWatch) {
            final BpelWatch bpelWatch = (BpelWatch) object;
            final Value value = bpelWatch.getValue();
            
            if (bpelWatch.getValue() != null) {
                if (value instanceof XmlElementValue) {
                    return myHelper.getChildren((XmlElementValue) value).length;
                }
                
                return 0;
            }
            
            String expression = bpelWatch.getExpression();
            if (!expression.startsWith("$")) {
                expression = "$" + expression;
            }
            
            return myHelper.getChildren(expression).length;
        }
        
        if (object instanceof NamedValueHost) {
            return myHelper.getVariablesUtil().getChildren(object).length;
        }
        
        if (object instanceof Node) {
            return myHelper.getVariablesUtil().getChildren(object).length;
        }
        
        throw new UnknownTypeException(object);
    }

    /**{@inheritDoc}*/
    public boolean isLeaf(
            final Object object) throws UnknownTypeException {
        return getChildrenCount(object) == 0;
    }

    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listeners) {
        myListeners.remove(listeners);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private Object[] getWatches(
            final int from, 
            final int to) {
        final Watch[] nbWatches = 
                DebuggerManager.getDebuggerManager().getWatches();
        final Object[] bpelWatches = new Object[to - from];
         
        int watchesTo = to;
        if (to == nbWatches.length + 1) {
            watchesTo = to - 1;
        }

        int j = 0;
        for (int i = from; i < watchesTo; i++) {
            BpelWatch bpelWatch = myWatchToValue.get(nbWatches[i]);
            
            if (bpelWatch == null) {
                bpelWatch = new BpelWatch(myDebugger, nbWatches[i]);
                myWatchToValue.put(nbWatches[i], bpelWatch);
            }
            
            bpelWatches[j++] = bpelWatch;
        }

        if (to == nbWatches.length + 1) {
            bpelWatches[j++] = ADD_NEW_WATCH;
        }
        
        if (myListener == null) {
            myListener = new Listener(this, myDebugger);
        }
        
        return bpelWatches;
    }
    
    private int getWatchCount() {
        return DebuggerManager.getDebuggerManager().
                getWatches().length + 1;
    }
    
    private void fireWatchesChanged() {
        final Vector clone = (Vector) myListeners.clone();
        final ModelEvent event = new ModelEvent.NodeChanged(
                this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged (event);
        }
    }
    
    private void fireTreeChanged() {
        synchronized (myWatchToValue) {
            myWatchToValue.clear();
        }
        
        final Vector clone = (Vector) myListeners.clone();
        final ModelEvent event = new ModelEvent.TreeChanged(this);
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(event);
        }
    }
    
    void fireTableValueChangedChanged(
            final Object node, 
            final String propertyName) {
        fireTreeChanged();
    }
    
    void fireTableValueChangedComputed(
            final Object node, 
            final String propertyName) {
        final Vector clone = (Vector) myListeners.clone ();
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(
                    new ModelEvent.TableValueChanged(this, node, propertyName));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class Listener extends DebuggerManagerAdapter 
            implements PropertyChangeListener {
        
        private WeakReference myDebugger;
        private WeakReference myModel;
        
        private RequestProcessor.Task task;
        
        private Listener(
                final WatchesTreeModel model, 
                final BpelDebugger debugger) {
            myModel = new WeakReference(model);
            myDebugger = new WeakReference(debugger);
            
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                    DebuggerManager.PROP_WATCHES, this);
            debugger.addPropertyChangeListener(this);
            
            final Watch[] ws = 
                    DebuggerManager.getDebuggerManager().getWatches();
            for (int i = 0; i < ws.length; i++) {
                ws[i].addPropertyChangeListener(this);
            }
        }
        
        private WatchesTreeModel getModel() {
            final WatchesTreeModel model = (WatchesTreeModel) myModel.get();
            
            if (model == null) {
                destroy();
            }
            
            return model;
        }
        
        @Override
        public void watchAdded(
                final Watch watch) {
            final WatchesTreeModel model = getModel();
            if (model == null) {
                return;
            }
            
            watch.addPropertyChangeListener(this);
            model.fireWatchesChanged();
        }
        
        @Override
        public void watchRemoved(
                final Watch watch) {
            
            final WatchesTreeModel model = getModel();
            if (model == null) {
                return;
            }
            
            watch.removePropertyChangeListener(this);
            //TODO:shouldn't we remove evaluated watch first?
            model.fireWatchesChanged();
        }
        
        @Override
        public void propertyChange(
                final PropertyChangeEvent event) {
            
            final String propName = event.getPropertyName();
            
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if (DebuggerManager.PROP_WATCHES.equals(propName)) {
                return ;
            }
            
            final WatchesTreeModel model = getModel();
            if (model == null) {
                return;
            }
            
            if (model.myDebugger.getState() == 
                    BpelDebugger.STATE_DISCONNECTED) {
                destroy();
                return;
            }
            
            if (event.getSource() instanceof Watch) {
                Object node;
                synchronized (model.myWatchToValue) {
                    node = model.myWatchToValue.get(event.getSource());
                }
                
                if (node != null) {
                    model.fireTableValueChangedChanged(node, null);
                    return ;
                }
            }
            
            if (BpelDebugger.PROP_CURRENT_POSITION.equals(propName)) {
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
            
            final BpelDebugger debugger = (BpelDebugger) myDebugger.get();
            if (debugger != null) {
                debugger.removePropertyChangeListener(this);
            }
            
            final Watch[] ws = 
                    DebuggerManager.getDebuggerManager().getWatches();
            for (int i = 0; i < ws.length; i++) {
                ws[i].removePropertyChangeListener(this);
            }
            
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }

    }
}
