/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class LocalsTreeModel implements TreeModel {

    
    private static boolean      verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('l') >= 0);
    
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector              listeners = new Vector ();
    
    
    public LocalsTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public Object[] getChildren (Object o, int from, int to) 
    throws NoInformationException, UnknownTypeException {
        try {
            if (o.equals (ROOT)) {
                Object[] os = getLocalVariables (from, to);
                return os;
            } else
//            if (o instanceof SuperVariable) {
//                SuperVariable mv = (SuperVariable) o;
//                return getSuperFields (mv, true, from, to);
//            } else
            if (o instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable mv = (AbstractVariable) o;
                Object[] avs = mv.getFields (from, to);
//                if (mv.getInnerValue () instanceof ArrayReference) {
//                    ArrayReference ar = (ArrayReference) mv.getInnerValue ();
//                    if (ar.length () > 50) {
//                        Object[] a2 = new Object [avs.length + 1];
//                        System.arraycopy (avs, 0, a2, 0, avs.length);
//                        a2 [avs.length] = "More";
//                        avs = a2;
//                    }
//                }
                return avs;
            } else
            throw new UnknownTypeException (o);
        } catch (VMDisconnectedException ex) {
            return new Object [0];
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
    public int getChildrenCount (Object node) throws UnknownTypeException,
    NoInformationException {
        try {
            if (node.equals (ROOT)) {
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) 
                    throw new NoInformationException ("No current thread");
                StackFrame sf = frame.getStackFrame ();
                if (sf == null) 
                    throw new NoInformationException ("No current thread");
                try {
                    int i = 0;
                    try {
                        i = sf.visibleVariables ().size ();
                    } catch (AbsentInformationException ex) {
                        i++;
                    }
                    if (sf.thisObject () != null) i++;
                    return i;
//                } catch (AbsentInformationException ex) {
//                    throw new NoInformationException ("compiled without -g");
                } catch (NativeMethodException ex) {
                    throw new NoInformationException ("native method");
                } catch (InvalidStackFrameException ex) {
                    throw new NoInformationException ("thread is running");
                } catch (VMDisconnectedException ex) {
                }
                return 0;
            } else
//            if (node instanceof SuperVariable) {
//                SuperVariable mv = (SuperVariable) node;
//                return getSuperFields (mv, true, 0, 0).length;
//            } else
            if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable mv = (AbstractVariable) node;
//                int i = 0;
//                if (mv.getInnerValue () instanceof ArrayReference) {
//                    ArrayReference ar = (ArrayReference) mv.getInnerValue ();
//                    if (ar.length () > 50) 
//                        i++;
//                }
                return mv.getFields (0, 0).length;
            } else
            throw new UnknownTypeException (node);
        } catch (VMDisconnectedException ex) {
        }
        return 0;
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o.equals (ROOT))
            return false;
        if (o instanceof AbstractVariable)
            return !(((AbstractVariable) o).getInnerValue () instanceof ObjectReference);
        if (o.equals ("More")) // NOI18N
            return true;
        if (o.equals ("NoInfo")) // NOI18N
            return true;
        throw new UnknownTypeException (o);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
        if (listener == null)
            listener = new Listener (this, debugger);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
        if (listeners.size () == 0) {
            listener.destroy ();
            listener = null;
        }
    }
    
    void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    void fireNodeChanged (Object n) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (n);
    }

    
    // private methods .........................................................
    
    Object[] getLocalVariables (
        int from, 
        int to
    ) throws NoInformationException {
        synchronized (debugger.LOCK) {
            CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.
                getCurrentCallStackFrame ();
            if (callStackFrame == null) 
                throw new NoInformationException ("No current thread");
            StackFrame stackFrame = callStackFrame.getStackFrame ();
            if (stackFrame == null) 
                throw new NoInformationException ("No current thread");
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
                    throw new NoInformationException ("compiled without -g");
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
                    result [0] = getThis (thisR, "");
                System.arraycopy (avs, 0, result, 1, avs.length);
                return result;
            }            
        } // synchronized
    }
    
    AbstractVariable[] getLocalVariables (
        final CallStackFrameImpl    callStackFrame, 
        final StackFrame            stackFrame,
        final int                   from,
        final int                   to
    ) throws NoInformationException, AbsentInformationException {
        try {
            String className = stackFrame.location ().declaringType ().name ();
            List l = stackFrame.visibleVariables ();
            int i, k = to - from, j = from;
            AbstractVariable[] locals = new AbstractVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (j++);
                locals [i] = getLocal (lv, callStackFrame, className);
            }
            return locals;
        } catch (NativeMethodException ex) {
            throw new NoInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new NoInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new AbstractVariable [0];
        }
    }
    
    ThisVariable getThis (ObjectReference thisR, String parentID) {
        return new ThisVariable (this, thisR, parentID);
    }
    
    private Local getLocal (LocalVariable lv, CallStackFrameImpl frame, String className) {
        Value v = frame.getStackFrame ().getValue (lv);
        if (v instanceof ObjectReference)
            return new ObjectLocalVariable (
                this, 
                v, 
                className, 
                lv, 
                JPDADebuggerImpl.getGenericSignature (lv), 
                frame
            );
        return new Local (this, v, className, lv, frame);
    }
    
    public Variable getVariable (Value v) {
        if (v instanceof ObjectReference)
            return new ObjectVariable (
                this,
                (ObjectReference) v,
                null
            );
        return new AbstractVariable (this, v, null);
    }
    
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference model;
        
        private Listener (
            LocalsTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference (tm);
            debugger.addPropertyChangeListener (this);
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel old task " + task);
                task = null;
            }
        }
        
        private LocalsTreeModel getModel () {
            LocalsTreeModel tm = (LocalsTreeModel) model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( ( (e.getPropertyName () == 
                     JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                   //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == JPDADebugger.PROP_STATE)
                 ) && (debugger.getState () == JPDADebugger.STATE_STOPPED)
            ) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
                final LocalsTreeModel ltm = getModel ();
                if (ltm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("LTM cancel old task " + task);
                    task = null;
                }
                task = RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        if (debugger.getState () != JPDADebugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println("LTM cancel started task " + task);
                            return;
                        }
                        if (verbose)
                            System.out.println("LTM do task " + task);
                        ltm.fireTreeChanged ();
                    }
                }, 500);
                if (verbose)
                    System.out.println("LTM  create task " + task);
            } else
            if ( (e.getPropertyName () == JPDADebugger.PROP_STATE) &&
                 (debugger.getState () != JPDADebugger.STATE_STOPPED) &&
                 (task != null)
            ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel task " + task);
                task = null;
            }
        }
    }
}
