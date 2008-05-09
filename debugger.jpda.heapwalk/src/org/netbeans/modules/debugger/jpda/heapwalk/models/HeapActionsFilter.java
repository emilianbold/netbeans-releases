/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.heapwalk.models;

import org.netbeans.lib.profiler.heap.Instance;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);

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
                final InstancesView instances = openInstances(true);
                final Reference<ObjectVariable> varRef = new WeakReference(var);
                final Reference<JPDADebugger> debuggerRef = new WeakReference(debugger);
                InstancesView.HeapFragmentWalkerProvider provider =
                        new InstancesView.HeapFragmentWalkerProvider() {
                    public synchronized HeapFragmentWalker getHeapFragmentWalker() {
                        HeapFragmentWalker hfw = instances.getCurrentFragmentWalker();
                        HeapImpl heap = (hfw != null) ? (HeapImpl) hfw.getHeapFragment() : null;
                        JPDADebugger debugger = debuggerRef.get();
                        if (heap == null || debugger != null && heap.getDebugger() != debugger) {
                            heap = new HeapImpl(debugger);
                            hfw = new DebuggerHeapFragmentWalker(heap);
                        }
                        ObjectVariable var = varRef.get();
                        if (var != null) {
                            Instance instance = InstanceImpl.createInstance(heap, var);
                            hfw.getInstancesController().showInstance(instance);
                        }
                        return hfw;
                    }
                };
                instances.setHeapFragmentWalkerProvider(provider);
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
