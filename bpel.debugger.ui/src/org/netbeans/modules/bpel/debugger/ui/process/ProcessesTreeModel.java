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


package org.netbeans.modules.bpel.debugger.ui.process;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 * Represents a process instance table that shows all known
 * process instances and their current known state.
 * This is built to plug into the SOA debugger UI module
 * process view mechanism as seemlessly as possible.
 * 
 * @author Sun Microsystems
 * @author Sun Microsystems
 */
public class ProcessesTreeModel implements TreeModel {

    private BpelDebugger myDebugger;
    private Listener myListener;
    private Vector<ModelListener> myListeners = new Vector<ModelListener>();
    
    /**
     * Creates a new instance of ProcessInstancesTreeModel.
     *
     * @param lookupProvider debugger context
     */
    public ProcessesTreeModel(
            final ContextProvider lookupProvider) {
        
        myDebugger = (BpelDebugger)
                lookupProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    public Object getRoot() {
        return ROOT;
    }
    
    public Object[] getChildren(
            final Object object, 
            final int from, 
            final int to) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return filter(getProcesses(), from, to);
        }
        
        if (object instanceof BpelProcess) {
            return filter(getProcessInstances((BpelProcess) object), from, to);
        }
        
        if (object instanceof ProcessInstance) {
            return new Object[] {
                new CorrelationSetsWrapper((ProcessInstance) object),
            };
        }
        
        if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
            return filter(((ProcessesTreeModel.CorrelationSetsWrapper) object).
                    getProcessInstance().getCorrelationSets(), from, to);
        }
        
        if (object instanceof CorrelationSet) {
            return filter(((CorrelationSet) object).getProperties(), from, to);
        }
        
        if (object instanceof CorrelationSet.Property) {
            return new Object[0];
        }
        
        throw new UnknownTypeException(object);
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
    public int getChildrenCount(
            final Object object) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return getProcesses().length;
        }
        
        if (object instanceof BpelProcess) {
            return getProcessInstances((BpelProcess) object).length;
        }
        
        if (object instanceof ProcessInstance) {
            return 1;
        }
        
        if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
            return ((ProcessesTreeModel.CorrelationSetsWrapper) object).
                    getCorrelationSets().length;
        }
        
        if (object instanceof CorrelationSet) {
            return ((CorrelationSet) object).getProperties().length;
        }
        
        if (object instanceof CorrelationSet.Property) {
            return 0;
        }
        
        throw new UnknownTypeException (object);
    }
    
    public boolean isLeaf(
            final Object object) throws UnknownTypeException {
        return getChildrenCount(object) == 0;
    }

    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
        
        if ((myListener == null) && (myDebugger != null)) {
            myListener = new Listener (this, myDebugger);
        }
    }

    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
        
        if (myListeners.size() == 0) {
            myListener.destroy();
            myListener = null;
        }
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void runInDispatch(
            final Runnable runner) {
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(runner);
        } else {
            runner.run();
        }
    }
    
    private void fireTreeChanged() {
        final Runnable runner = new Runnable() {
            public void run() {
                @SuppressWarnings("unchecked")
                final Vector<ModelListener> clone = 
                        (Vector<ModelListener>) myListeners.clone();
                
                for (int i = 0; i < clone.size(); i++) {
                    clone.get(i).modelChanged(
                            new ModelEvent.TreeChanged(this));
                }
            }
        };
        
        runInDispatch(runner);
    }
    
    private BpelProcess[] getProcesses() {
        
        if (myDebugger != null) {
            return myDebugger.getProcessInstancesModel().getProcesses();
        }
        
        return new BpelProcess[0];
    }
    
    private ProcessInstance[] getProcessInstances(
            final BpelProcess process) {
        
        if (myDebugger != null) {
            return myDebugger.
                    getProcessInstancesModel().getProcessInstances(process);
        }
        
        return new ProcessInstance[0];
    }
    
    private Object[] filter(
            final Object[] array, 
            final int from, 
            final int to) {
        final Object[] filtered = new Object[to - from];
        
        System.arraycopy(array, from, filtered, 0, to - from);
        
        return filtered;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class Listener 
            implements ProcessInstancesModel.Listener, PropertyChangeListener {
        
        private BpelDebugger myDebugger;
        private WeakReference<ProcessesTreeModel> myModel;
        
        private RequestProcessor.Task task;
        
        private Listener(
                final ProcessesTreeModel treeModel, 
                final BpelDebugger debugger) {
            
            myDebugger = debugger;
            myModel = new WeakReference<ProcessesTreeModel>(treeModel);
            
            debugger.addPropertyChangeListener(this);
            debugger.getProcessInstancesModel().addListener(this);
        }
        
        void destroy() {
            myDebugger.getProcessInstancesModel().removeListener(this);
            myDebugger.removePropertyChangeListener(this);
            
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
        
        private ProcessesTreeModel getModel() {
            final ProcessesTreeModel tm = myModel.get();
            
            if (tm == null) {
                destroy();
            }
            
            return tm;
        }
        
        public void propertyChange (
                final PropertyChangeEvent e) {
            
            if (BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE.equals(
                    e.getPropertyName())) {
                asyncFireTreeChanged();
            } else if (BpelDebugger.PROP_STATE.equals(e.getPropertyName())) {
                if (myDebugger.getState() == BpelDebugger.STATE_DISCONNECTED) {
                    destroy();
                    asyncFireTreeChanged();
                } else if (myDebugger.getState() == 
                        BpelDebugger.STATE_RUNNING) {
                    asyncFireTreeChanged();
                }
            }
        }
        
        public void processInstanceRemoved(
                final ProcessInstance processInstance) {
            
            asyncFireTreeChanged();
        }
        
        public void processInstanceAdded(
                final ProcessInstance processInstance) {
            
            asyncFireTreeChanged();
        }
        
        public void processInstanceStateChanged(
                final ProcessInstance processInstance, 
                final int oldState, 
                final int newState) {
                
            asyncFireTreeChanged();
        }
        
        private void asyncFireTreeChanged() {
            final ProcessesTreeModel model = getModel();
            if (model == null) {
                return;
            }
            
            if (task != null) {
                task.cancel();
                task = null;
            }
            
            task = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    model.fireTreeChanged();
                }
            }, 500);
        }
    }
    
    private static class ProcessInstanceWrapper {
        private ProcessInstance myProcessInstance;
        
        public ProcessInstanceWrapper(final ProcessInstance instance) {
            myProcessInstance = instance;
        }
        
        public ProcessInstance getProcessInstance() {
            return myProcessInstance;
        }
    }
    
    static class CorrelationSetsWrapper extends ProcessInstanceWrapper {
        public CorrelationSetsWrapper(final ProcessInstance instance) {
            super(instance);
        }
        
        public CorrelationSet[] getCorrelationSets() {
            return getProcessInstance().getCorrelationSets();
        }
    }
    
    static class FaultsWrapper extends ProcessInstanceWrapper {
        public FaultsWrapper(final ProcessInstance instance) {
            super(instance);
        }
    }
    
    static class WaitingMessagesWrapper extends ProcessInstanceWrapper {
        public WaitingMessagesWrapper(final ProcessInstance instance) {
            super(instance);
        }
    }
}
