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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;

import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class WatchesModel implements TreeModel {

    
    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('w') >= 0);

    private static boolean      USE_CACHE = false;
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector              listeners = new Vector ();
    private LookupProvider      lookupProvider;
    WeakHashMap watchToExpression = new WeakHashMap();

    
    public WatchesModel (LookupProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }

    /**
     *
     * @return watches contained in this group of watches
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException, NoInformationException {
        if (parent == ROOT) {
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            JPDAWatch[] jws = new JPDAWatch [k];
            for (i = 0; i < k; i++) {
                Object expr = watchToExpression.get(ws[i].getExpression());
                if (expr == null) {
                    try {
                        expr = Expression.parse(ws[i].getExpression(), Expression.LANGUAGE_JAVA_1_5);
                    } catch (ParseException e) {
                        expr = e.getMessage();
                    }
                    watchToExpression.put(ws[i].getExpression(), expr);
                }
                if (expr instanceof String) {
                    jws [i] = new JPDAWatchImpl (this, ws[i], (String) expr);
                } else {
                    jws [i] = evaluate (ws[i], (Expression) expr);
                }
            }
            if (listener == null)
                listener = new Listener (this, debugger);
            return jws;
        }
        if (parent instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildren (parent, from, to);
        }
        return getLocalsTreeModel ().getChildren (parent, from, to);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof JPDAWatchImpl) 
            return ((JPDAWatchImpl) node).isPrimitive ();
        return getLocalsTreeModel ().isLeaf (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    void fireNodeChanged (Watch b) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (b);
    }
    
    
    // other methods ...........................................................
    
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

    JPDAWatch evaluate (Watch w, Expression expr) {
        Value v = null;
        String exception = null;
        try {
            v = debugger.evaluateIn(expr);
        } catch (InvalidExpressionException e) {
            exception = e.getMessage ();
        }
        JPDAWatch wi;
        if (exception != null)
            wi = new JPDAWatchImpl (this, w, exception);
        else
            if (v instanceof ObjectReference)
                wi = new JPDAObjectWatchImpl (this, w, (ObjectReference) v);
            else
                wi = new JPDAWatchImpl (this, w, v);
        return wi;
    }

/*
    JPDAWatch evaluate (Watch w) {
        Value v = null;
        String exception = null;
        try {
            v = debugger.evaluateIn (w.getExpression ());
        } catch (InvalidExpressionException e) {
            e.printStackTrace ();
            exception = e.getMessage ();
        }
        JPDAWatch wi;
        if (exception != null)
            wi = new JPDAWatchImpl (this, w, exception);
        else
            if (v instanceof ObjectReference)
                wi = new JPDAObjectWatchImpl (this, w, (ObjectReference) v);
            else
                wi = new JPDAWatchImpl (this, w, v);
        return wi;
    }
*/

    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference model;
        private WeakReference debugger;
        
        private Listener (
            WatchesModel tm,
            JPDADebuggerImpl debugger
        ) {
            model = new WeakReference (tm);
            this.debugger = new WeakReference (debugger);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            debugger.addPropertyChangeListener (this);
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].addPropertyChangeListener (this);
        }
        
        private WatchesModel getModel () {
            WatchesModel m = (WatchesModel) model.get ();
            if (m == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_WATCHES,
                    this
                );
                JPDADebugger d = (JPDADebugger) debugger.get ();
                if (d != null)
                    d.removePropertyChangeListener (this);
                
                Watch[] ws = DebuggerManager.getDebuggerManager ().
                    getWatches ();
                int i, k = ws.length;
                for (i = 0; i < k; i++)
                    ws [i].removePropertyChangeListener (this);

                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("WM cancel old task " + task);
                    task = null;
                }
            }
            return m;
        }
        
        public void watchAdded (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.addPropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        public void watchRemoved (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.removePropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent evt) {
            final WatchesModel m = getModel ();
            if (m == null) return;
            
            if (evt.getSource () instanceof Watch) {
                m.fireTreeChanged ();
            }

            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("WM cancel old task " + task);
                task = null;
            }
            
            task = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    if (verbose)
                        System.out.println("WM do task " + task);
                    m.fireTreeChanged ();
                }
            }, 500);
            if (verbose)
                System.out.println("WM  create task " + task);
        }
    }
}
