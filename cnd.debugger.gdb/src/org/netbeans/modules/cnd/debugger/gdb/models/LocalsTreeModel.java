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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/*
 * LocalsTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class LocalsTreeModel implements TreeModel, TreeExpansionModel, PropertyChangeListener {
        
    private GdbDebugger debugger;
    private Listener listener;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private Set<Object> expandedNodes = new WeakSet<Object>();
    private Set<Object> collapsedNodes = new WeakSet<Object>();
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N
        
    public LocalsTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        if (debugger != null) {
            debugger.addPropertyChangeListener(GdbDebugger.PROP_LOCALS_REFRESH, this);
        }
    }    
    
    public Object getRoot() {
        return ROOT;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireTableValueChanged(evt.getNewValue(), null);
    }
    
    public Object[] getChildren(Object o, int from, int to) throws UnknownTypeException {
        Object[] ch = getChildrenImpl(o, from, to);
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] instanceof Customizer) {
                ((Customizer) ch[i]).addPropertyChangeListener(this);
            }
        }
        return ch;
    }
    
    public Object[] getChildrenImpl(Object o, int from, int to) throws UnknownTypeException {
        if (o.equals(ROOT)) {            
            return getLocalVariables(from, to);
        } else if (o instanceof AbstractVariable) {
            AbstractVariable abstractVariable = (AbstractVariable) o;
            return abstractVariable.getFields(from, to);
        } else {
            return new Object[0];
        }
    }
    
    /**
     * Returns number of children for given node.
     *
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  0 if no children are visible
     */
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node.equals(ROOT)) {
            CallStackFrame callStackFrame = debugger.getCurrentCallStackFrame();
            if (callStackFrame == null) {
                return 1;
            } else {
                return callStackFrame.getLocalVariables().length;
            }
        } else if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) node;
                return abstractVariable.getFieldsCount();
        }
        return 0;
    }
    
    public boolean isLeaf(Object o) throws UnknownTypeException {
        if (o.equals(ROOT)) {
            return false;
        }
        if (o instanceof AbstractVariable) {
            int i = ((AbstractVariable) o).getFieldsCount();
            return i == 0;
        }
        
//        /*
//         *`Temporary fix.
//         * AbstractVariable cannot be casted to Field, so we use LocalVariableImpl
//         * to specify children (see getLocalVariables() in CallStackFrame).
//         * The problem is that LocalVariableImpl does not have fields, so
//         * this solution works only for 1-level structures.
//         */
//        if (o instanceof LocalVariableImpl) {
//            return true;
//        } else
        if (o instanceof LocalVariable) {
            return true;
        } else if (o.equals("NoInfo")) { // NOI18N
            return true;
        } else if (o.equals("No current thread")) { // NOI18N
            return true;
        } else if (o instanceof AbstractVariable.ErrorField) {
            return true;
        }
        throw new UnknownTypeException(o);
    }
    
    /**
     * Save ModelListener to be able to push updates
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
        if (listener == null) {
            listener = new Listener(this, debugger);
        }
    }
    
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
        if (listeners.isEmpty()) {
            listener.destroy();
            listener = null;
        }
    }
    
    void fireTreeChanged() {
        log.fine("LTM.fireTreeChanged:");
        for (ModelListener l : listeners) {
            l.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
    private void fireTableValueChanged(Object node, String propertyName) {
        for (ModelListener l : listeners) {
            l.modelChanged(new ModelEvent.TableValueChanged(this, node, propertyName));
        }
    }
    
    
    // private methods .........................................................
    
    private Object[] getLocalVariables(int from, int to) {
        synchronized (debugger.LOCK) {
            CallStackFrame callStackFrame = debugger.getCurrentCallStackFrame();
            if (callStackFrame == null) {
                return new String [] {"No current thread"}; // NOI18N
            }
            return callStackFrame.getLocalVariables();
        } // synchronized
    }
    
    GdbDebugger getDebugger() {
        return debugger;
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private final GdbDebugger debugger;
        private final WeakReference<LocalsTreeModel> model;
        
        public Listener(LocalsTreeModel tm, GdbDebugger debugger) {
            this.debugger = debugger;
            model = new WeakReference<LocalsTreeModel>(tm);
            debugger.addPropertyChangeListener(this);
        }
        
        void destroy() {
            debugger.removePropertyChangeListener(this);
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
        
        private LocalsTreeModel getModel() {
            LocalsTreeModel tm = model.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange(PropertyChangeEvent e) {
            if ((e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                    e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_THREAD)) &&
                    (debugger.getState() == GdbDebugger.State.STOPPED)) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stopped
                log.fine("LTM.propertyChange: Change for " + e.getPropertyName());
                final LocalsTreeModel ltm = getModel();
                if (ltm == null) {
                    return;
                }
                if (task != null) {
                    // cancel old task
                    task.cancel();
                    task = null;
                }
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (debugger.getState() == GdbDebugger.State.STOPPED) {
                            ltm.fireTreeChanged();
                        }
                    }
                }, 500);
            } else if ((e.getPropertyName().equals(GdbDebugger.PROP_STATE)) &&
                    (debugger.getState() != GdbDebugger.State.STOPPED) &&
                    task != null) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel();
                task = null;
//            } else {
//                final LocalsTreeModel ltm = getModel();
//                if (ltm == null) {
//                    return;
//                }
//                task = RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        if (debugger.getState().equals(GdbDebugger.STATE_STOPPED)) {
//                            ltm.fireTreeChanged();
//                        }
//                    }
//                });
            }
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
        return false;
    }
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public void nodeExpanded(Object node) {
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
}
