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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
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
    throws NoInformationException, UnknownTypeException {
        StackFrame[] ch = (StackFrame[]) model.getChildren (parent, from, to);
        int i, k = ch.length;
        CallStackFrameImpl[] r = new CallStackFrameImpl [k];
        for (i = 0; i < k; i++) {
            String id = "" + ch [i];
            try {
                id = ch [i].thread ().name () + ":" + i;
            } catch (InvalidStackFrameException e) {
                // sf was obsoleted -> use default id
            }
            // StackFrame of the same thread with the same index should 
            // be "equal"
            r [i] = new CallStackFrameImpl (
                ch [i], 
                this, 
                id,
                i
            );
        }
        return r;
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
        StackFrame sf = ((CallStackFrameImpl) node).getStackFrame();
        if (sf == null) return true;
        if (node instanceof CallStackFrameImpl)
            return model.isLeaf(sf);
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
        //listeners.add (l);
        model.addTreeModelListener (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
        //listeners.remove (l);
        model.removeTreeModelListener (l);
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

