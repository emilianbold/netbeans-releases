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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrameImpl;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariableImpl;
import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.openide.util.RequestProcessor;

/*
 * LocalsTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class LocalsTreeModel implements TreeModel, PropertyChangeListener {
        
    private static boolean      verbose = false;
    
    /** Nest array elements when array length is bigger then this. */
    private static final int ARRAY_CHILDREN_NESTED_LENGTH = 100;
    
    private GdbDebuggerImpl     debugger;
    private Listener            listener;
    private Vector              listeners = new Vector();
    private Map                 cachedLocals = new WeakHashMap();
    private Map                 cachedArrayChildren = new WeakHashMap();
        
    public LocalsTreeModel(ContextProvider lookupProvider) {
        debugger = (GdbDebuggerImpl) lookupProvider.lookupFirst(null, GdbDebugger.class);
        debugger.registerLocalsModel(this);
    }    
    
    public Object getRoot() {
        return ROOT;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireTableValueChangedChanged(evt.getSource(), null);
    }
    
    public Object[] getChildren(Object o, int from, int to)
    throws UnknownTypeException {
        Object[] ch = getChildrenImpl(o, from, to);
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] instanceof Customizer) {
                ((Customizer) ch[i]).addPropertyChangeListener(this);
            }
        }
        return ch;
    }
    
    public Object[] getChildrenImpl(Object o, int from, int to)
    throws UnknownTypeException {
        //NM try {
        if (o.equals(ROOT)) {
            Object[] os = getLocalVariables(from, to);            
            return os;
        }
        if (o instanceof AbstractVariable) {
            AbstractVariable abstractVariable = (AbstractVariable) o;
            return abstractVariable.getFields (from, to);
        }
        if (o instanceof LocalVariableImpl) {
            LocalVariableImpl localVariableImpl = (LocalVariableImpl) o;
            return localVariableImpl.getFields (from, to);
        }
            /*NM TEMPORARY COMMENTED OUT (needs AbstractVariable, ArrayChildrenNode)
            if (o instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) o;
                boolean isArray =
                        abstractVariable.getInnerValue () instanceof ArrayReference;
                if (isArray) {
                    to = abstractVariable.getFieldsCount ();
                    // We need to reset it for arrays, to get the full array
                }
                if (isArray && (to - from) > ARRAY_CHILDREN_NESTED_LENGTH) {
                    ArrayChildrenNode achn =
                            (ArrayChildrenNode) cachedArrayChildren.get(abstractVariable.getInnerValue ());
                    if (achn == null) {
                        achn = new ArrayChildrenNode(abstractVariable);
                        cachedArrayChildren.put(abstractVariable.getInnerValue (), achn);
                    } else {
                        achn.update(abstractVariable);
                    }
                    return achn.getChildren();
                } else {
                    return abstractVariable.getFields (from, to);
                }
            } else
            if (o instanceof ArrayChildrenNode) {
                return ((ArrayChildrenNode) o).getChildren();
            } else
                throw new UnknownTypeException(o);
            NM*/
        //NM } catch (VMDisconnectedException ex) {
             return new Object [0];
        //NM }
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
        //NM TEMPORARY CODE
        if (node.equals(ROOT)) {
            CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame();
            if (callStackFrame == null)
                return 1;
            LocalVariable[] lv = callStackFrame.visibleVariables();
            return lv.length;
        } else {
            if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) node;
                return abstractVariable.getFieldsCount();
            }
            if (node instanceof LocalVariableImpl) {
                LocalVariableImpl localVariableImpl = (LocalVariableImpl) node;
                return localVariableImpl.getFieldsCount();
            }
            return 0;
        }
        /*NM TEMPORARY COMMENTED OUT
        try {
            if (node.equals(ROOT)) {
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                        getCurrentCallStackFrame();
                if (frame == null)
                    return 1; //NM
                StackFrame sf = frame.getStackFrame();
                if (sf == null)
                    return 1;
                try {
                    int i = 0;
                    try {
                        i = sf.visibleVariables().size();
                    } catch (AbsentInformationException ex) {
                        i++;
                    }
                    if (sf.thisObject() != null) i++;
                    return i;
                } catch (NativeMethodException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InternalException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InvalidStackFrameException ex) {
                    return 1;//throw new NoInformationException ("thread is running");
                } catch (VMDisconnectedException ex) {
                }
                return 0;
            }
            if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) node;
                if (abstractVariable.getInnerValue() instanceof
                        ArrayReference
                        )
                    return Math.min(abstractVariable.getFieldsCount(), ARRAY_CHILDREN_NESTED_LENGTH);
                return abstractVariable.getFieldsCount();
            } else
                if (node instanceof ArrayChildrenNode) {
                    return ((ArrayChildrenNode) node).getChildren().length;
                } else
                    throw new UnknownTypeException(node);
        } catch (VMDisconnectedException ex) {
        }
        NM*/
    }
    
    public boolean isLeaf(Object o) throws UnknownTypeException {
        if (o.equals(ROOT))
            return false;
        if (o instanceof AbstractVariable) {
            //NM return !(((AbstractVariable) o).getInnerValue () instanceof ObjectReference);
            int i = ((AbstractVariable) o).getFieldsCount();
            if (i > 0) return false;
            return true;
        }
        /*
         *`Temporary fix.
         * AbstractVariable cannot be casted to Field, so we use LocalVariableImpl
         * to specify children (see getLocalVariables() in CallStackFrameImpl).
         * The problem is that LocalVariableImpl does not have fields, so
         * this solution works only for 1-level structures.
         */
        if (o instanceof LocalVariableImpl) {
            int i = ((LocalVariableImpl) o).getFieldsCount();
            if (i > 0) return false;
            return true;
        }
        if (o instanceof LocalVariable) {
            return true;
        }
        if (o.equals("NoInfo")) // NOI18N
            return true;
        if (o.equals("No current thread")) // NOI18N
            return true;
        throw new UnknownTypeException(o);
    }
    
    /**
     * Save ModelListener to be able to push updates
     */
    private ModelListener modelListener = null;
    public void addModelListener(ModelListener l) {
        modelListener = l;
        listeners.add(l);
        if (listener == null)
            listener = new Listener(this, debugger);
        debugger.registerLocalsListener(l);
        debugger.isLocalViewActive = true; // Perfromance Optimization
    }
    
    public void removeModelListener(ModelListener l) {
        debugger.isLocalViewActive = false; // Perfromance Optimization
        debugger.registerLocalsListener(null);
        modelListener = null; // DEBUG
        listeners.remove(l);
        if (listeners.size() == 0) {
            listener.destroy();
            listener = null;
        }
    }
    
    void fireTreeChanged() {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get(i)).modelChanged(
                    new ModelEvent.TreeChanged(this)
                    );
    }
    
    private void fireTableValueChangedChanged(Object node, String propertyName) {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get(i)).modelChanged(
                    new ModelEvent.TableValueChanged(this, node, propertyName)
                    );
    }
    
    
    // private methods .........................................................
    
    private Object[] getLocalVariables(
            int from,
            int to
            ) {
        synchronized (debugger.LOCK) {
            CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame();
            if (callStackFrame == null)
                return new String [] {"No current thread"}; // NOI18N
            /*NM TEMPORARY COMMENTED OUT
            StackFrame stackFrame = callStackFrame.getStackFrame ();
            if (stackFrame == null)
                return new String [] {"No current thread"};
            try {
                ObjectReference thisR = stackFrame.thisObject ();
                if (thisR == null) {
                    Object[] avs = null;
                    try {
                        return getLocalVariables (
                            callStackFrame,
                            stackFrame,
                            from,
                            to
                        );
                    } catch (AbsentInformationException ex) {
                        return new String [] {"NoInfo"};
                    }
                } else {
                    Object[] avs = null;
                    try {
                        avs = getLocalVariables (
                            callStackFrame,
                            stackFrame,
                            Math.max (from - 1, 0),
                            Math.max (to - 1, 0)
                        );
                    } catch (AbsentInformationException ex) {
                        avs = new Object[] {"NoInfo"};
                    }
                    Object[] result = new Object [avs.length + 1];
                    if (from < 1)
                        result [0] = new ThisVariable (debugger, thisR, "");
                    System.arraycopy (avs, 0, result, 1, avs.length);
                    return result;
                }
            } catch (InternalException ex) {
                return new String [] {ex.getMessage ()};
            }
            NM*/ return callStackFrame.getLocalVariables();// new String [] {"NoInfo"}; // callStackFrame.getLocalVariables();
        } // synchronized
    }
    
    LocalVariable[] getLocalVariables(
            final CallStackFrameImpl    callStackFrame,
            //final StackFrame            stackFrame,
            int                         from,
            int                         to
            ) /*NM throws AbsentInformationException */ {
        LocalVariable[] locals = callStackFrame.getLocalVariables();
        int n = locals.length;
        to = Math.min(n, to);
        from = Math.min(n, from);
        if (from != 0 || to != n) {
            LocalVariable[] subLocals
                    = new LocalVariable[to - from];
            for (int i = from; i < to; i++) {
                subLocals[i - from] = locals[i];
            }
            locals = subLocals;
        }
        return locals;
        /*NM TEMPORARY COMMENTED OUT
        try {
            String className = stackFrame.location ().declaringType ().name ();
            List l = stackFrame.visibleVariables ();
            to = Math.min(l.size(), to);
            from = Math.min(l.size(), from);
            int i, k = to - from, j = from;
            AbstractVariable[] locals = new AbstractVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (j++);
                locals [i] = getLocal (lv, callStackFrame, className);
            }
            return locals;
        } catch (NativeMethodException ex) {
            throw new AbsentInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new AbsentInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new AbstractVariable [0];
        }
        NM*/
    }
    /*NM TEMPORARY COMMENTED OUT
    private Local getLocal (LocalVariable lv, CallStackFrameImpl frame, String className) {
        Value v = frame.getStackFrame ().getValue (lv);
        Local local = (Local) cachedLocals.get(lv);
        if (local != null) {
            local.setInnerValue(v);
            local.setFrame(frame);
            local.setLocalVariable(lv);
        } else {
            if (v instanceof ObjectReference) {
                local = new ObjectLocalVariable (
                    debugger,
                    v,
                    className,
                    lv,
                    JPDADebuggerImpl.getGenericSignature (lv),
                    frame
                );
            } else {
                local = new Local (debugger, v, className, lv, frame);
            }
            cachedLocals.put(lv, local);
        }
        return local;
    }
    NM*/
    
    public Variable getVariable(/* Value */ String v) {
        /*NM TEMPORARY COMMENTED OUT
        if (v instanceof ObjectReference)
            return new AbstractVariable(
                    debugger,
                    (ObjectReference) v,
                    null
                    );
        NM*/
        return new AbstractVariable(debugger, v, null, null, null);
    }
    
    
    GdbDebuggerImpl getDebugger() {
        return debugger;
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private GdbDebugger debugger;
        private WeakReference model;
        
        public Listener(
                LocalsTreeModel tm,
                GdbDebugger debugger
                ) {
            this.debugger = debugger;
            model = new WeakReference(tm);
            debugger.addPropertyChangeListener(this);
        }
        
        void destroy() {
            debugger.removePropertyChangeListener(this);
            if (task != null) {
                // cancel old task
                task.cancel();
                if (verbose)
                    System.out.println("LTM cancel old task " + task); // NOI18N
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
            if ( ( (e.getPropertyName() ==
                    debugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                    //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                    (e.getPropertyName() == debugger.PROP_STATE)
                    ) && (debugger.getState() == debugger.STATE_STOPPED)
                    ) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
                final LocalsTreeModel ltm = getModel();
                if (ltm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel();
                    if (verbose)
                        System.out.println("LTM cancel old task " + task); // NOI18N
                    task = null;
                }
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (debugger.getState() != debugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println("LTM cancel started task " + task); // NOI18N
                            return;
                        }
                        if (verbose)
                            System.out.println("LTM do task " + task); // NOI18N
                        ltm.fireTreeChanged();
                    }
                }, 500);
                if (verbose)
                    System.out.println("LTM  create task " + task); // NOI18N
            } else
                if ( (e.getPropertyName() == debugger.PROP_STATE) &&
                    (debugger.getState() != debugger.STATE_STOPPED) &&
                    (task != null)
                    ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel();
                if (verbose)
                    System.out.println("LTM cancel task " + task); // NOI18N
                task = null;
                }
        }
    }
    
    /**
     * The hierarchical representation of nested array elements.
     * Used for arrays longer then {@link #ARRAY_CHILDREN_NESTED_LENGTH}.
     */
    /*NM TEMPORARY COMMENTED OUT
    private static final class ArrayChildrenNode {
     
        private AbstractVariable var;
        private int from = 0;
        private int length;
        private int maxIndexLog;
     
        public ArrayChildrenNode(AbstractVariable var) {
            this(var, 0, var.getFieldsCount(), -1);
        }
     
        private ArrayChildrenNode(AbstractVariable var, int from, int length,
                                  int maxIndex) {
            this.var = var;
            this.from = from;
            this.length = length;
            if (maxIndex < 0) {
                maxIndex = from + length - 1;
            }
            this.maxIndexLog = ArrayFieldVariable.log10(maxIndex);
        }
     
        private static int pow(int a, int b) {
            if (b == 0) return 1;
            int p = a;
            for (int i = 1; i < b; i++) {
                p *= a;
            }
            return p;
        }
     
        public Object[] getChildren() {
            if (length > ARRAY_CHILDREN_NESTED_LENGTH) {
                int depth = (int) Math.ceil(Math.log(length)/Math.log(ARRAY_CHILDREN_NESTED_LENGTH) - 1);
                int n = pow(ARRAY_CHILDREN_NESTED_LENGTH, depth);
                int numCh = (int) Math.ceil(length/((double) n));
     
                // We have 'numCh' children, each with 'n' sub-children (or possibly less for the last one)
                Object[] ch = new Object[numCh];
                for (int i = 0; i < numCh; i++) {
                    int chLength = n;
                    if (i == (numCh - 1)) {
                        chLength = length % n;
                        if (chLength == 0) chLength = n;
                    }
                    ch[i] = new ArrayChildrenNode(var, from + i*n, chLength, from + length - 1);
                }
                return ch;
            } else {
                return var.getFields(from, from + length);
            }
        }
     
        public void update(AbstractVariable var) {
            this.var = var;
        }
     
        // Overriden equals so that the nodes are not re-created when not necessary.
        public boolean equals(Object obj) {
            if (!(obj instanceof ArrayChildrenNode)) return false;
            ArrayChildrenNode achn = (ArrayChildrenNode) obj;
            return achn.var.equals(this.var) &&
                   achn.from == this.from &&
                   achn.length == this.length;
        }
     
        public int hashCode() {
            return var.hashCode() + from + length;
        }
     
        public String toString() {
            int num0 = maxIndexLog - ArrayFieldVariable.log10(from);
            String froms;
            if (num0 > 0) {
                froms = ArrayFieldVariable.zeros(2*num0) + from; // One space is roughly 1/2 of width of a number
            } else {
                froms = Integer.toString(from);
            }
            int last = from + length - 1;
            num0 = maxIndexLog - ArrayFieldVariable.log10(last);
            String lasts;
            if (num0 > 0) {
                lasts = ArrayFieldVariable.zeros(2*num0) + last; // One space is roughly 1/2 of width of a number
            } else {
                lasts = Integer.toString(last);
            }
            return "SubArray"+froms+"-"+lasts; // NOI18N
        }
     
    }
    NM*/
}
