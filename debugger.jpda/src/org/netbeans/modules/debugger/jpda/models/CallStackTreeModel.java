/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * This tree model delegates to BasicCallStackTreeModel and encapsulates 
 * StackFrame instances to CallStackFrameImpl.
 *
 * @author Jan Jancura
 */
public class CallStackTreeModel implements TreeModel {

    private ContextProvider             lookupProvider;
    private JPDADebuggerImpl            debugger;
    private BasicCallStackTreeModel     model;
    //private Vector listeners = new Vector ();
    //private Listener listener = new Listener ();
    
   
    public CallStackTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        model = new BasicCallStackTreeModel (lookupProvider);
        //model.addTreeModelListener (listener);
        this.lookupProvider = lookupProvider;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if ( parent.equals (ROOT) ||
             (parent instanceof ThreadReference) 
        ) {
            // 1) get ThreadReference
            ThreadReference threadRef = null;
            if (parent.equals (ROOT)) {
                JPDAThreadImpl ti = (JPDAThreadImpl) debugger.
                    getCurrentThread ();
                if (ti != null)
                    threadRef = ti.getThreadReference ();
            } else
                threadRef = (ThreadReference) parent;
            if (threadRef == null) 
                return new String [] {"No current thread"};

            // 2) get StackFrames
            Object[] res = (Object[]) model.getChildren (parent, from, to);
            if (res instanceof String[]) return res;
            StackFrame[] ch = (StackFrame[]) res;
            
            // 3) encapsulate them to CallStackFrameImpls
            int i, k = ch.length, j = from;
            CallStackFrameImpl[] r = new CallStackFrameImpl [k];
            String threadName = threadRef.name () + ":";
            for (i = 0; i < k; i++, j++) {
                String id = threadName + j;
                // StackFrame of the same thread with the same index should 
                // be "equal"
                r [i] = new CallStackFrameImpl (
                    threadRef,
                    ch [i], 
                    this, 
                    id,
                    j
                );
            }
            return r;
        } else
        throw new UnknownTypeException (parent);
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
    public int getChildrenCount (Object node) throws UnknownTypeException {
        return model.getChildrenCount (node);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return model.getRoot ();
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == BasicCallStackTreeModel.ROOT) 
            return model.isLeaf (node);
        if (node instanceof CallStackFrame) {
            CallStackFrame csf = (CallStackFrame) node;
            return model.isLeaf(csf);
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        //listeners.add (l);
        model.addModelListener (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        //listeners.remove (l);
        model.removeModelListener (l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
//    private void fireTreeNodeChanged (Object parent) {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
//    }
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    private LocalsTreeModel localsTreeModel;

    LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }
    
    
//    private class Listener implements TreeModelListener {
//        
//        public void treeNodeChanged (Object node) {
//            fireTreeNodeChanged (node);
//        }
//        
//        public void treeChanged () {
//            fireTreeChanged ();
//        }
//        
//    }
}

