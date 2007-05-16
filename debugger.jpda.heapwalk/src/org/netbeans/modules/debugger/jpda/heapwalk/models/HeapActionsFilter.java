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

package org.netbeans.modules.debugger.jpda.heapwalk.models;

import com.sun.tools.profiler.heap.Instance;

import javax.swing.Action;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.InstanceImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.views.DebuggerHeapFragmentWalker;
import org.netbeans.modules.debugger.jpda.heapwalk.views.InstancesView;

import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Heap Walker actions on ObjectVariable
 * 
 * @author Martin Entlicher
 */
public class HeapActionsFilter implements NodeActionsProviderFilter {
    
    private JPDADebugger debugger;
    
    /** Creates a new instance of HeapActionsFilter */
    public HeapActionsFilter(ContextProvider contextProvider) {
        debugger = (JPDADebugger) contextProvider.
            lookupFirst (null, JPDADebugger.class);

    }
    
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        if (node instanceof ObjectVariable && debugger.canGetInstanceInfo()) {
            int index;
            for (index = 0; index < actions.length; index++) {
                if (actions[index] == null)
                    break;
            }
            Action[] newActions = new Action[actions.length + 1];
            System.arraycopy(actions, 0, newActions, 0, index);
            newActions[index] = HEAP_REFERENCES_ACTION;
            if (index < actions.length) {
                System.arraycopy(actions, index, newActions, index + 1, actions.length - index);
            }
            actions = newActions;
        }
        return actions;
    }

    private final Action HEAP_REFERENCES_ACTION = Models.createAction (
        NbBundle.getBundle(HeapActionsFilter.class).getString("CTL_References_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                ObjectVariable var = (ObjectVariable) node;
                return var.getUniqueID() != 0L;
            }
            public void perform (Object[] nodes) {
                ObjectVariable var = (ObjectVariable) nodes[0];
                if (var.getUniqueID() == 0L) return ;
                InstancesView instances = openInstances(true);
                HeapFragmentWalker hfw = instances.getCurrentFragmentWalker();
                HeapImpl heap = (hfw != null) ? (HeapImpl) hfw.getHeapFragment() : null;
                if (heap == null || heap.getDebugger() != debugger) {
                    heap = new HeapImpl(debugger);
                    hfw = new DebuggerHeapFragmentWalker(heap);
                    instances.setHeapFragmentWalker(hfw);
                }
                Instance instance = InstanceImpl.createInstance(heap, var);
                hfw.getInstancesController().showInstance(instance);
            }
            
            private InstancesView openInstances (boolean activate) {
                TopComponent view = WindowManager.getDefault().findTopComponent("dbgInstances");
                if (view == null) {
                    throw new IllegalArgumentException("dbgInstances");
                }
                view.open();
                if (activate) {
                    view.requestActive();
                }
                return (InstancesView) view;
            }
    
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
}
