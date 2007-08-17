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

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrameImpl;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariableImpl;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/*
 * LocalsTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class LocalsTreeModel implements TreeModel, TreeExpansionModel, PropertyChangeListener {
        
    /** Nest array elements when array length is bigger then this. */
    private static final int ARRAY_CHILDREN_NESTED_LENGTH = 100;
    
    private GdbDebugger     debugger;
    private Listener            listener;
    private Vector              listeners = new Vector();
    private Map                 cachedLocals = new WeakHashMap();
    private Map                 cachedArrayChildren = new WeakHashMap();
    private Set                 expandedNodes = new WeakSet();
    private Set                 collapsedNodes = new WeakSet();
        
    public LocalsTreeModel(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
    }    
    
    public Object getRoot() {
        return ROOT;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireTableValueChangedChanged(evt.getSource(), null);
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
                LocalVariable[] lv = callStackFrame.getLocalVariables();
                return lv.length;
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
        
        /*
         *`Temporary fix.
         * AbstractVariable cannot be casted to Field, so we use LocalVariableImpl
         * to specify children (see getLocalVariables() in CallStackFrameImpl).
         * The problem is that LocalVariableImpl does not have fields, so
         * this solution works only for 1-level structures.
         */
        if (o instanceof LocalVariableImpl) {
            return true;
        } else if (o instanceof LocalVariable) {
            return true;
        } else if (o.equals("NoInfo")) { // NOI18N
            return true;
        } else if (o.equals("No current thread")) { // NOI18N
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
        if (listeners.size() == 0) {
            listener.destroy();
            listener = null;
        }
    }
    
    void fireTreeChanged() {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
    private void fireTableValueChangedChanged(Object node, String propertyName) {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.TableValueChanged(this, node, propertyName));
        }
    }
    
    
    // private methods .........................................................
    
    private Object[] getLocalVariables(int from, int to) {
        synchronized (debugger.LOCK) {
            CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.getCurrentCallStackFrame();
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
        
        private GdbDebugger debugger;
        private WeakReference model;
        
        public Listener(LocalsTreeModel tm, GdbDebugger debugger) {
            this.debugger = debugger;
            model = new WeakReference(tm);
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
            LocalsTreeModel tm = (LocalsTreeModel) model.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange(PropertyChangeEvent e) {
            if (((e.getPropertyName() == debugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                    (e.getPropertyName() == debugger.PROP_STATE)) && (debugger.getState() == debugger.STATE_STOPPED)) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
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
                        if (debugger.getState() != debugger.STATE_STOPPED) {
                            return;
                        }
                        debugger.waitForTypeCompletionCompletion();
                        ltm.fireTreeChanged();
                    }
                }, 500);
            } else if ((e.getPropertyName() == debugger.PROP_STATE) && (debugger.getState() != debugger.STATE_STOPPED) && (task != null)) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel();
                task = null;
            } else if (e.getPropertyName().equals(debugger.PROP_LOCALS_VIEW_UPDATE)) {
                final LocalsTreeModel ltm = getModel();
                if (ltm == null) {
                    return;
                }
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (debugger.getState() == debugger.STATE_STOPPED) {
                            ltm.fireTreeChanged();
                        }
                    }
                });
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
}
