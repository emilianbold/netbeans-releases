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
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessExecutionTreeModel implements TreeModel {
    private BpelDebugger myDebugger;
    private ProcessExecutionModel myPem;
    private Listener myListener;
    private Vector myListeners = new Vector();
    private ContextProvider myContextProvider;

    
    /**
     * Creates a new instance of ProcessExecutionTreeModel.
     *
     * @param lookupProvider debugger context
     */
    public ProcessExecutionTreeModel(ContextProvider contextProvider) {
        myContextProvider = contextProvider;
        myDebugger = 
            (BpelDebugger) contextProvider.lookupFirst(null, BpelDebugger.class);
    }

    
    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object object, int from, int to) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            Object root = getTreeRoot();
            return root != null ? new Object[] {root} : new Object[0];
        } else if (object instanceof PsmEntity) {
            return ((PsmEntity)object).getChildren();
        } else if (object instanceof PemEntity) {
            return getPemEntityChildren((PemEntity)object, from, to);
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
            return getTreeRoot() != null ? 1 : 0;
        } else if (object instanceof PsmEntity) {
            return ((PsmEntity)object).getChildrenCount();
        } else if (object instanceof PemEntity) {
            return getPemEntityChildrenCount((PemEntity)object);
        } else {
            throw new UnknownTypeException(object);
        }
    }

    public boolean isLeaf(Object object) throws UnknownTypeException {
        if (object.equals(ROOT)) {
            return false;
        } else if (object instanceof PsmEntity) {
            return !((PsmEntity)object).hasChildren();
        } else if (object instanceof PemEntity) {
            return isPemEntityLeaf((PemEntity)object);
        } else {
            throw new UnknownTypeException(object);
        }
    }

    public void addModelListener(ModelListener l) {
        myListeners.add(l);
        if (myListener == null) {
            myListener = new Listener(this, getDebugger());
        }
    }

    public void removeModelListener(ModelListener l) {
        myListeners.remove(l);
        if (myListeners.size() == 0) {
            myListener.destroy();
            myListener = null;
        }
    }
    
    void setProcessExecutionModel(ProcessExecutionModel pem) {
        myPem = pem;
    }

    void fireTreeChanged() {
        Vector v = (Vector) myListeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.TreeChanged(this));
    }

    void fireTableValueChangedChanged(Object node, String propertyName) {
        Vector v = (Vector) myListeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.TableValueChanged(this, node,
                    propertyName));
    }

    // private methods .........................................................
    
    private BpelDebugger getDebugger() {
        return myDebugger;
    }
    
    private Object getTreeRoot() {
        if (myPem != null) {
            return myPem.getRoot();
        } else {
            return null;
        }
    }
    
    private Object[] getPemEntityChildren(PemEntity pemEntity, int from, int to) {
        PsmEntity psmEntity = pemEntity.getPsmEntity();
        if (!pemEntity.hasChildren()) {
            return psmEntity.getChildren();
        } else if (psmEntity.isLoop()) {
            return pemEntity.getChildren();
        } else {
            Object[] children = new Object[psmEntity.getChildrenCount()];
            int i = 0;
            for (PsmEntity psmChild : psmEntity.getChildren()) {
                PemEntity[] pemChildren = pemEntity.getChildren(psmChild);
                if (pemChildren.length > 0) {
                    children[i++] = pemChildren[0];
                } else {
                    children[i++] = psmChild;
                }
            }
            return children;
        }
    }
    
    private int getPemEntityChildrenCount(PemEntity pemEntity) {
        PsmEntity psmEntity = pemEntity.getPsmEntity();
        if (!pemEntity.hasChildren()) {
            return psmEntity.getChildrenCount();
        } else if (psmEntity.isLoop()) {
            return pemEntity.getChildrenCount();
        } else {
            return psmEntity.getChildrenCount();
        }
    }
    
    private boolean isPemEntityLeaf(PemEntity pemEntity) {
        return !pemEntity.hasChildren() && !pemEntity.getPsmEntity().hasChildren();
    }

    // innerclasses ............................................................

    private static class Listener implements
            ProcessExecutionModel.Listener,
            PropertyChangeListener
    {
        private BpelDebugger myDebugger;
        private WeakReference myModel;
        private ProcessExecutionModel myPem;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task myTask;
        

        private Listener (
                ProcessExecutionTreeModel tm,
                BpelDebugger debugger)
        {
            myDebugger = debugger;
            myModel = new WeakReference(tm);
            myDebugger.addPropertyChangeListener(this);
            init();
        }
        
        void destroy() {
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
        
        private void init() {
            ProcessExecutionTreeModel model = getModel();
            if (model == null) {
                return;
            }
                
            if (myPem != null) {
                myPem.removeListener(this);
                myPem = null;
            }
            
            ProcessInstance processInstance = myDebugger.getCurrentProcessInstance();
            if (processInstance != null) {
                myPem = processInstance.getProcessExecutionModel();
                if (myPem != null) {
                    myPem.addListener(this);
                }
            }
            
            model.setProcessExecutionModel(myPem);
        }
        
        private ProcessExecutionTreeModel getModel() {
            ProcessExecutionTreeModel tm =
                    (ProcessExecutionTreeModel)myModel.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE) {
                if (myTask != null) {
                    // cancel old task
                    myTask.cancel();
                    myTask = null;
                }

                init();
                asyncFireTreeChanged();
                
            } else if (e.getPropertyName() == BpelDebugger.PROP_STATE) {
                if (myDebugger.getState() == BpelDebugger.STATE_DISCONNECTED) {
                    destroy();
                    asyncFireTreeChanged();
                }
            }
        }

        public void modelUpdated() {
            asyncFireTreeChanged();
        }
        
        private void asyncFireTreeChanged() {
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
}
