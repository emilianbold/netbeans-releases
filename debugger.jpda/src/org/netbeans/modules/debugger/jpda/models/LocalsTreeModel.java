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
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerEngine;
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
                return getLocalVariables (true);
            } else
            if (o instanceof SuperVariable) {
                SuperVariable mv = (SuperVariable) o;
                return getSuperFields (mv, true);
            } else
            if (o instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable mv = (AbstractVariable) o;
                return getFields (mv, true);
            } else
            throw new UnknownTypeException (o);
        } catch (VMDisconnectedException ex) {
            return new Object [0];
        }
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o.equals (ROOT)) {
            return false;
        } else
        if (o instanceof AbstractVariable) {
            return !(((AbstractVariable) o).getInnerValue () instanceof ObjectReference);
        } else
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
    
    AbstractVariable[] getLocalVariables (boolean includeThis)
    throws NoInformationException {
        CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
            getCurrentCallStackFrame ();
        if (frame == null) return new AbstractVariable [0];
        return getLocalVariables (
            frame,
            includeThis
        );
    }
    
    AbstractVariable[] getLocalVariables (
        CallStackFrameImpl frame, 
        boolean includeThis
    ) throws NoInformationException {
        if (frame == null) throw new NoInformationException ("No current thread");
        StackFrame sf = frame.getStackFrame ();
        if (sf == null) throw new NoInformationException ("No current thread");
        try {
            ObjectReference thisR = sf.thisObject ();
            String className = sf.location ().declaringType ().name ();
            List l = sf.visibleVariables ();
            int k = l.size (), j = 0, i = 0;
            if ((thisR != null) && includeThis) k++;
            
            AbstractVariable[] locals = new AbstractVariable [k];
            if ((thisR != null) && includeThis)
                locals [i++] = getThis (thisR, "");
            for (; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (j++);
                locals [i] = getLocal (lv, frame, className);
            }
            return locals;
        } catch (AbsentInformationException ex) {
            throw new NoInformationException ("compiled without -g");
        } catch (NativeMethodException ex) {
            throw new NoInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new NoInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new AbstractVariable [0];
        }
    }
    
    AbstractVariable[] getSuperFields (
        SuperVariable mv,
        boolean includeSuper
    ) {
        ObjectReference or = (ObjectReference) mv.getInnerValue ();
        ReferenceType rt = mv.getSuperClass ();
        return getFields (
            or, 
            rt, 
            true,
            ((AbstractVariable) mv).getID ()
        );
    }
    
    AbstractVariable[] getFields (
        AbstractVariable mv,
        boolean includeSuper
    ) {// ThisVariable & FieldVariable
        if (!(mv.getInnerValue () instanceof ObjectReference)) 
            return new AbstractVariable [0];
        ObjectReference or = (ObjectReference) mv.getInnerValue ();
        ReferenceType rt = or.referenceType ();
        if (or instanceof ArrayReference) 
            return getFieldsOfArray (
                (ArrayReference) or, 
                ((ArrayType) rt).componentTypeName (),
                mv.getID ()
            );
        else 
            return getFields (or, rt, includeSuper, mv.getID ());
    }
    
    private AbstractVariable[] getFields (
        ObjectReference or, 
        ReferenceType rt,
        boolean includeSuper,
        String parentID
    ) {
    // ThisVariable & FieldVariable & SuperVariable
        List l = rt.fields ();
        ArrayList ch = new ArrayList ();
        ClassType superRt = null;
       // String className = rt.name ();
//        if ( includeSuper &&
//             (rt instanceof ClassType)
//        ) {
//            superRt = ((ClassType) rt).superclass ();
//            if (superRt != null)
//                ch.add (getSuper (superRt, or, parentID));
//        }
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            Field f = (Field) l.get (i);
            if ( f.isStatic () || f.isSynthetic ())
                continue;
            ch.add (getField (f, or, parentID));
        }
        return (AbstractVariable[]) ch.toArray (new AbstractVariable [ch.size ()]);
    }
    
    FieldVariable[] getAllStaticFields (
        AbstractVariable av
    ) {
        Value v = av.getInnerValue ();
        if ( (v == null) || !(v instanceof ObjectReference)) return new FieldVariable [0];
        ObjectReference or = (ObjectReference) v;
        ReferenceType rt = or.referenceType ();
        List l = rt.allFields ();
        ArrayList ch = new ArrayList ();
        //String className = rt.name ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            Field f = (Field) l.get (i);
            if (f.isStatic ())
                ch.add (getField (f, or, av.getID ()));
        }
        return (FieldVariable[]) ch.toArray (new FieldVariable [ch.size ()]);
    }
    
    FieldVariable[] getInheritedFields (
        AbstractVariable av
    ) {
        Value v = av.getInnerValue ();
        if ( (v == null) || !(v instanceof ObjectReference)) return new FieldVariable [0];
        ObjectReference or = (ObjectReference) v;
        ReferenceType rt = or.referenceType ();
        List l = rt.allFields ();
        Set s = new HashSet (rt.fields ());
        ArrayList ch = new ArrayList ();
       // String className = rt.name ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            Field f = (Field) l.get (i);
            if (f.isStatic ())
                continue;
            if (s.contains (f))
                continue;
            ch.add (getField (f, or, av.getID ()));
        }
        return (FieldVariable[]) ch.toArray (new FieldVariable [ch.size ()]);
    }
    
    AbstractVariable[] getFieldsOfArray (
        ArrayReference ar, 
        String componentType,
        String parentID
    ) {
    // ThisVariable & FieldVariable & SuperVariable
        List l = ar.getValues ();
        int i, k = l.size ();
        AbstractVariable[] ch = new AbstractVariable [k];
        String className = ar.referenceType ().name ();
        for (i = 0; i < k; i++) {
            Value v = (Value) l.get (i);
            ch [i] = (v instanceof ObjectReference) ?
                new ObjectArrayFieldVariable (
                    this, (ObjectReference) v, className, componentType, i, parentID
                ) :
                new ArrayFieldVariable (
                    this, v, className, componentType, i, parentID
                );
        }
        return ch;
    }
    
    ThisVariable getThis (ObjectReference thisR, String parentID) {
        return new ThisVariable (this, thisR, parentID);
    }
    
    private Local getLocal (LocalVariable lv, CallStackFrameImpl frame, String className) {
        Value v = frame.getStackFrame ().getValue (lv);
        if (v instanceof ObjectReference)
            return new ObjectLocalVariable (
                this, 
                (ObjectReference) v, 
                className, 
                lv, 
                debugger.getGenericSignature (lv), 
                frame
            );
        else
            return new Local (this, v, className, lv, frame);
    }
    
    public Variable getVariable (Value v) {
        if (v instanceof ObjectReference)
            return new ObjectVariable (
                this,
                (ObjectReference) v,
                null
            );
        else
            return new AbstractVariable (
                this,
                v,
                null
            );
    }
    
    SuperVariable getSuper (
        ClassType superRt, 
        ObjectReference or,
        String parentID
    ) {
        return new SuperVariable (this, or, superRt, parentID);
    }

    FieldVariable getField (
        Field f, 
        ObjectReference or, 
        String parentID//,
        //String className
    ) {
        Value v = or.getValue (f);
        if ( (v == null) || (v instanceof ObjectReference))
            return new ObjectFieldVariable (
                this,
                (ObjectReference) v,
                //f.declaringType (), //className,
                f,
                parentID,
                debugger.getGenericSignature(f),
                or
            );
        else
            return new FieldVariable (this, v, f, parentID, or);
    }
    
    private boolean isLeafChanged (Value v1, Value v2) {
        return (v1 instanceof ObjectReference) != 
               (v2 instanceof ObjectReference);
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
                     debugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                   //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == debugger.PROP_STATE)
                 ) && (debugger.getState () == debugger.STATE_STOPPED)
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
                        if (debugger.getState () != debugger.STATE_STOPPED) {
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
            if ( (e.getPropertyName () == debugger.PROP_STATE) &&
                 (debugger.getState () != debugger.STATE_STOPPED) &&
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
