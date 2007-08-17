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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.RequestProcessor;

/**
 * This tree model provides an array of CallStackFrame objects.
 *
 * @author Gordon Prieur (copied from Jan Jancura's and Martin Entlicher's JPDA implementation)
 */
public class CallStackTreeModel implements TreeModel {
    private GdbDebugger     debugger;
    private Collection          listeners = new HashSet();
    private Listener            listener;
    
   
    public CallStackTreeModel(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent.equals(ROOT)) {
            CallStackFrame[] sfs = debugger.getCallStackFrames(from, to);
	    return sfs;
        } else {
	    throw new UnknownTypeException(parent);
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
    public int getChildrenCount(Object parent) throws UnknownTypeException {
        if ( parent.equals(ROOT)) {
            return debugger.getStackDepth();
        } else {
	    throw new UnknownTypeException(parent);
	}
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot() {
        return ROOT;
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node.equals(ROOT)) {
	    return false;
	}
        if (node instanceof CallStackFrame) {
	    return true;
	}
        throw new UnknownTypeException(node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
            if (listener == null) {
                listener = new Listener(this, debugger);
            }
        }
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size() == 0) {
                listener.destroy();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        Object[] ls;
        synchronized (listeners) {
            ls = listeners.toArray();
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ((ModelListener) ls[i]).modelChanged(ev);
        }
    }
    
    
    /**
     * Listens on GdbDebugger on PROP_STATE
     */
    private static class Listener implements PropertyChangeListener {
        
        private GdbDebugger debugger;
        private WeakReference model;
        
        public Listener(CallStackTreeModel tm, GdbDebugger debugger) {
            this.debugger = debugger;
            model = new WeakReference(tm);
            debugger.addPropertyChangeListener(this);
        }
        
        private CallStackTreeModel getModel() {
            CallStackTreeModel tm = (CallStackTreeModel) model.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        void destroy() {
            debugger.removePropertyChangeListener(this);
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        // check also whether the current thread was resumed/suspended
        // the call stack needs to be refreshed after invokeMethod() which resumes the thread
        public synchronized void propertyChange(PropertyChangeEvent e) {
            boolean refresh = false;
            String propertyName = e.getPropertyName();
            if ((propertyName == debugger.PROP_STATE) && (debugger.getState() == debugger.STATE_STOPPED)) {
                refresh = true;
            }
            if (refresh) {
                synchronized (this) {
                    if (task == null) {
                        task = RequestProcessor.getDefault().create(new Refresher());
                    }
                    task.schedule(200);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                if (debugger.getState () == debugger.STATE_STOPPED) {
                    CallStackTreeModel tm = getModel();
                    if (tm != null) {
                        tm.fireTreeChanged();
                    }
                }
            }
        }
    }
}

