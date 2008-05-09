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

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 * Model describing the tree column of the Process Execution View.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessExecutionTreeModel implements TreeModel {
    
    private BpelDebugger myDebugger;
    private ProcessExecutionModel myPem;
    
    private Listener myListener;
    private Vector myListeners = new Vector();
    
    /**
     * Creates a new instance of ProcessExecutionTreeModel.
     *
     * @param lookupProvider debugger context
     */
    public ProcessExecutionTreeModel(
            final ContextProvider contextProvider) {
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
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
        
        if (myPem == null) {
            if (object.equals(ROOT)) {
                return new Object[] {
                    new Dummy()
                };
            }
            
            if (object instanceof Dummy) {
                return new Object[0];
            }
            
            throw new UnknownTypeException(object);
        }
        
        if (object.equals(ROOT)) {
            final Object root = getPemRoot();
            
            return root != null ? new Object[] {root} : new Object[0];
        } 
        
        if (object instanceof PsmEntity) {
            return ((PsmEntity) object).getChildren();
        } 
        
        if (object instanceof PemEntity) {
            return getPemEntityChildren((PemEntity) object);
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public int getChildrenCount(
            final Object object) throws UnknownTypeException {
        
        if (myPem == null) {
            if (object.equals(ROOT)) {
                return 1;
            }
            
            if (object instanceof Dummy) {
                return 0;
            }
            
            throw new UnknownTypeException(object);
        }
        
        if (object.equals(ROOT)) {
            return getPemRoot() != null ? 1 : 0;
        }
        
        if (object instanceof PsmEntity) {
            return ((PsmEntity) object).getChildrenCount();
        }
        
        if (object instanceof PemEntity) {
            return getPemEntityChildrenCount((PemEntity) object);
        }
        
        throw new UnknownTypeException(object);
    }

    /**{@inheritDoc}*/
    public boolean isLeaf(
            final Object object) throws UnknownTypeException {
        
        if (myPem == null) {
            if (object.equals(ROOT)) {
                return false;
            }
            
            if (object instanceof Dummy) {
                return true;
            }
            
            throw new UnknownTypeException(object);
        }
        
        if (object.equals(ROOT)) {
            return false;
        }
        
        if (object instanceof PsmEntity) {
            return !((PsmEntity)object).hasChildren();
        }
        
        if (object instanceof PemEntity) {
            return isPemEntityLeaf((PemEntity) object);
        }
        
        throw new UnknownTypeException(object);
    }

    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        myListeners.add(listener);
        
        if ((myListener == null) && (myDebugger != null)) {
            myListener = new Listener(this, myDebugger);
        }
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        myListeners.remove(listener);
        
        if ((myListeners.size() == 0) && (myListener != null)) {
            myListener.destroy();
            myListener = null;
        }
    }
    
    // Package methods /////////////////////////////////////////////////////////
    void setProcessExecutionModel(
            final ProcessExecutionModel pem) {
        myPem = pem;
    }

    void fireTreeChanged() {
        final Vector clone = (Vector) myListeners.clone();
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(
                    new ModelEvent.TreeChanged(this));
        }
    }

    // Private methods /////////////////////////////////////////////////////////
    private Object getPemRoot() {
        if (myPem != null) {
            return myPem.getRoot();
        } else {
            return null;
        }
    }
    
    private Object[] getPemEntityChildren(
            final PemEntity pemEntity) {
        final PsmEntity psmEntity = pemEntity.getPsmEntity();
        
        if (!pemEntity.hasChildren()) {
            return psmEntity.getChildren();
        }
        
        if (psmEntity.isLoop()) {
            return pemEntity.getChildren();
        }
        
        final Object[] children = new Object[psmEntity.getChildrenCount()];
        int i = 0;
        for (PsmEntity psmChild : psmEntity.getChildren()) {
            final PemEntity[] pemChildren = pemEntity.getChildren(psmChild);
            
            if (pemChildren.length > 0) {
                children[i++] = pemChildren[0];
            } else {
                children[i++] = psmChild;
            }
        }
        
        return children;
    }
    
    private int getPemEntityChildrenCount(
            final PemEntity pemEntity) {
        final PsmEntity psmEntity = pemEntity.getPsmEntity();
        
        if (!pemEntity.hasChildren()) {
            return psmEntity.getChildrenCount();
        }
        
        if (psmEntity.isLoop()) {
            return pemEntity.getChildrenCount();
        }
        
        return psmEntity.getChildrenCount();
    }
    
    private boolean isPemEntityLeaf(
            final PemEntity pemEntity) {
        return !pemEntity.hasChildren() && 
                !pemEntity.getPsmEntity().hasChildren();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes 
    private static class Listener implements
            ProcessExecutionModel.Listener, PropertyChangeListener {
        private BpelDebugger myDebugger;
        private WeakReference<ProcessExecutionTreeModel> myModel;
        private ProcessExecutionModel myPem;
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task myTask;

        private Listener (
                final ProcessExecutionTreeModel model,
                final BpelDebugger debugger) {
            myDebugger = debugger;
            myModel = new WeakReference<ProcessExecutionTreeModel>(model);
            myDebugger.addPropertyChangeListener(this);
            
            init();
        }
        
        private void init() {
            final ProcessExecutionTreeModel model = getModel();
            if (model == null) {
                return;
            }
                
            if (myPem != null) {
                myPem.removeListener(this);
                myPem = null;
            }
            
            final ProcessInstance processInstance = 
                    myDebugger.getCurrentProcessInstance();
            if (processInstance != null) {
                myPem = processInstance.getProcessExecutionModel();
                if (myPem != null) {
                    myPem.addListener(this);
                }
            }
            
            model.setProcessExecutionModel(myPem);
        }
        
        private void destroy() {
            myDebugger.removePropertyChangeListener(this);
            if (myPem != null) {
                myPem.removeListener(this);
                myPem = null;
            }
            
            if (myTask != null) {
                // cancel old task
                myTask.cancel();
                myTask = null;
            }
        }
        
        private ProcessExecutionTreeModel getModel() {
            final ProcessExecutionTreeModel model = myModel.get();
            
            if (model == null) {
                destroy();
            }
            
            return model;
        }
        
        /**{@iheritDoc}*/
        public void propertyChange(final PropertyChangeEvent e) {
            if (BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE.equals(
                    e.getPropertyName())) {
                if (myTask != null) {
                    // cancel old task
                    myTask.cancel();
                    myTask = null;
                }
                
                init();
                
                modelUpdated();
            } else if (BpelDebugger.PROP_STATE.equals(e.getPropertyName())) {
                if (myDebugger.getState() == BpelDebugger.STATE_DISCONNECTED) {
                    destroy();
                    modelUpdated();
                }
            }
        }
        
        /**{@inheritDoc}*/
        public void modelUpdated() {
            final ProcessExecutionTreeModel model = getModel();
            
            if (model == null) {
                return;
            }
            
            if (myTask != null) {
                // cancel old task
                myTask.cancel();
                myTask = null;
            }
            
            myTask = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    model.fireTreeChanged ();
                }
            }, 500);
        }
    }
    
    static class Dummy {
        // Empty, stub class
    }
}
