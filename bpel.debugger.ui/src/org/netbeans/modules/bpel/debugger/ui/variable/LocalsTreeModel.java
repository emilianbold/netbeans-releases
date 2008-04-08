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

package org.netbeans.modules.bpel.debugger.ui.variable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;


/**
 * Tree model supporting the Local Variable view.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class LocalsTreeModel implements TreeModel {
    public static final String VIEW_NAME = "LocalsView";

    private BpelDebugger myDebugger;
    private VariablesUtil myHelper;
    
    private PositionListener myListener;
    private Vector<ModelListener> myListeners = new Vector<ModelListener>();
    
    /**
     * Creates a new instance of BpelVariableTreeModel.
     *
     * @param contextProvider debugger context
     */
    public LocalsTreeModel(
            final ContextProvider contextProvider) {
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new VariablesUtil(myDebugger);
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
        
        if (myDebugger.getCurrentPosition() == null) {
            if (ROOT.equals(object)) {
                return new Object[] {
                    new Dummy()
                };
            }
            
            if (object instanceof Dummy) {
                return new Object[0];
            }
        }
        
        return myHelper.getChildren(object);
    }

    /**{@inheritDoc}*/
    public int getChildrenCount(
            final Object object) throws UnknownTypeException {
        
        if (myDebugger.getCurrentPosition() == null) {
            if (ROOT.equals(object)) {
                return 1;
            }
            
            if (object instanceof Dummy) {
                return 0;
            }
        }
        
        return getChildren(object, 0, 0).length;
    }

    /**{@inheritDoc}*/
    public boolean isLeaf(
            final Object object) throws UnknownTypeException {
        
        if (myDebugger.getCurrentPosition() == null) {
            if (ROOT.equals(object)) {
                return false;
            }
            
            if (object instanceof Dummy) {
                return true;
            }
        }
        
        return getChildrenCount(object) == 0;
    }

    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
        
        if (myListener == null) {
            myListener = new PositionListener(this, myDebugger);
        }
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
        
        if (myListeners.size() == 0) {
            myListener.destroy();
            myListener = null;
        }
    }

    // Private /////////////////////////////////////////////////////////////////
    private void fireTreeChanged() {
        final Vector clone = (Vector) myListeners.clone();
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(
                    new ModelEvent.TreeChanged(this));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class PositionListener implements PropertyChangeListener {

        private BpelDebugger myDebugger;
        private WeakReference<LocalsTreeModel> myModel;

        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        private PositionListener(
                final LocalsTreeModel model, 
                final BpelDebugger debugger) {
            myDebugger = debugger;
            myModel = new WeakReference<LocalsTreeModel>(model);
            
            debugger.addPropertyChangeListener(this);
        }

        private void destroy() {
            myDebugger.removePropertyChangeListener(this);
            
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }

        private LocalsTreeModel getModel() {
            final LocalsTreeModel model = myModel.get();
            
            if (model == null) {
                destroy();
            }
            
            return model;
        }
        
        /**{@inheritDoc}*/
        public void propertyChange(
                final PropertyChangeEvent event) {
            
            if (BpelDebugger.PROP_CURRENT_POSITION.equals(
                    event.getPropertyName())) {
                final LocalsTreeModel model = getModel();
                
                if (model == null) {
                    return;
                }
                
                if (task != null) {
                    // cancel old task
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
    }
    
    static class Dummy {
        // Empty, stub class
        
        public String toString() {
            return "";
        }
    }
}
