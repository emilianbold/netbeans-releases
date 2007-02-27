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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
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

    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector<ModelListener> listeners = new Vector<ModelListener>();
    private ContextProvider     lookupProvider;
    // Watch to Expression or Exception
    private Map<Watch, JPDAWatchEvaluating>  watchToValue = new WeakHashMap<Watch, JPDAWatchEvaluating>(); // <node (expression), JPDAWatch>

    
    public WatchesModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
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
    throws UnknownTypeException {
        if (parent == ROOT) {
            
            // 1) ger Watches
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            to = Math.min(ws.length, to);
            from = Math.min(ws.length, from);
            Watch[] fws = new Watch [to - from];
            System.arraycopy (ws, from, fws, 0, to - from);
            
            // 2) create JPDAWatches for Watches
            int i, k = fws.length;
            JPDAWatch[] jws = new JPDAWatch [k];
            for (i = 0; i < k; i++) {
                
                
                JPDAWatchEvaluating jw = watchToValue.get(fws[i]);
                if (jw == null) {
                    jw = new JPDAWatchEvaluating(this, fws[i], debugger);
                    watchToValue.put(fws[i], jw);
                }
                jws[i] = jw;
                
                // The actual expressions are computed on demand in JPDAWatchEvaluating
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
        if (node == ROOT) {
            if (listener == null)
                listener = new Listener (this, debugger);
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return DebuggerManager.getDebuggerManager ().getWatches ().length;
        }
        if (node instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildrenCount (node);
        }
        return getLocalsTreeModel ().getChildrenCount (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof JPDAWatchEvaluating) {
            JPDAWatchEvaluating jwe = (JPDAWatchEvaluating) node;
            JPDAWatch jw = jwe.getEvaluatedWatch();
            if (jw instanceof JPDAWatchImpl) {
                return ((JPDAWatchImpl) jw).isPrimitive ();
            }
        }
        return getLocalsTreeModel ().isLeaf (node);
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        synchronized (watchToValue) {
            for (Iterator<JPDAWatchEvaluating> it = watchToValue.values().iterator(); it.hasNext(); ) {
                it.next().setEvaluated(null);
            }
        }
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    private void fireWatchesChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        ((JPDAWatchEvaluating) node).setEvaluated(null);
        fireTableValueChangedComputed(node, propertyName);
    }
        
    void fireTableValueChangedComputed (Object node, String propertyName) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TableValueChanged (this, node, propertyName)
            );
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


    // innerclasses ............................................................
    
    private static class JPDAWatchEvaluating extends AbstractVariable
                                             implements JPDAWatch, Variable,
                                                        Refreshable, //.Lazy {
                                                        PropertyChangeListener {
        
        private WatchesModel model;
        private Watch w;
        private JPDADebuggerImpl debugger;
        private JPDAWatch evaluatedWatch;
        private Expression expression;
        private ParseException parseException;
        private boolean[] evaluating = new boolean[] { false };
        private PropertyChangeSupport propSupp = new PropertyChangeSupport(this);
        
        public JPDAWatchEvaluating(WatchesModel model, Watch w, JPDADebuggerImpl debugger) {
            super(debugger, null, "" + w);
            this.model = model;
            this.w = w;
            this.debugger = debugger;
            parseExpression(w.getExpression());
        }
        
        private void parseExpression(String exprStr) {
            try {
                expression = Expression.parse (
                    exprStr, 
                    Expression.LANGUAGE_JAVA_1_5
                );
                parseException = null;
            } catch (ParseException e) {
                setEvaluated(new JPDAWatchImpl(debugger, w, e, this));
                parseException = e;
            }
        }
        
        Expression getParsedExpression() throws ParseException {
            if (parseException != null) {
                throw parseException;
            }
            return expression;
        }

        
        public void setEvaluated(JPDAWatch evaluatedWatch) {
            synchronized (this) {
                this.evaluatedWatch = evaluatedWatch;
            }
            if (evaluatedWatch != null) {
                if (evaluatedWatch instanceof JPDAWatchImpl) {
                    setInnerValue(((JPDAWatchImpl) evaluatedWatch).getInnerValue());
                } else if (evaluatedWatch instanceof JPDAObjectWatchImpl) {
                    setInnerValue(((JPDAObjectWatchImpl) evaluatedWatch).getInnerValue());
                }
                //propSupp.firePropertyChange(PROP_INITIALIZED, null, Boolean.TRUE);
            } else {
                setInnerValue(null);
            }
            //model.fireTableValueChangedComputed(this, null);
        }
        
        synchronized JPDAWatch getEvaluatedWatch() {
            return evaluatedWatch;
        }
        
        public void expressionChanged() {
            setEvaluated(null);
            parseExpression(w.getExpression());
        }
        
        public synchronized String getExceptionDescription() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExceptionDescription();
            } else {
                return null;
            }
        }

        public synchronized String getExpression() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExpression();
            } else {
                return w.getExpression();
            }
        }

        public String getToStringValue() throws InvalidExpressionException {
            synchronized (this) {
                JPDAWatch evaluatedWatch = this.evaluatedWatch;
            }
            if (evaluatedWatch == null) {
                getValue();
            }
            return evaluatedWatch.getToStringValue();
        }

        public String getType() {
            synchronized (this) {
                JPDAWatch evaluatedWatch = this.evaluatedWatch;
            }
            if (evaluatedWatch == null) {
                getValue(); // To init the evaluatedWatch
            }
            return evaluatedWatch.getType();
        }

        public String getValue() {
            synchronized (evaluating) {
                if (evaluating[0]) {
                    try {
                        evaluating.wait();
                    } catch (InterruptedException iex) {
                        return null;
                    }
                }
                synchronized (this) {
                    if (evaluatedWatch != null) {
                        return evaluatedWatch.getValue();
                    }
                }
                evaluating[0] = true;
            }
            
            JPDAWatch jw = null;
            try {
                Expression expr = getParsedExpression();
                Value v = debugger.evaluateIn (expr);
                //if (v instanceof ObjectReference)
                //    jw = new JPDAObjectWatchImpl (debugger, w, (ObjectReference) v);
                JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, v, this);
                jwi.addPropertyChangeListener(this);
                jw = jwi;
            } catch (InvalidExpressionException e) {
                JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, e, this);
                jwi.addPropertyChangeListener(this);
                jw = jwi;
            } catch (ParseException e) {
                JPDAWatchImpl jwi = new JPDAWatchImpl (debugger, w, e, this);
                jwi.addPropertyChangeListener(this);
                jw = jwi;
            } finally {
                setEvaluated(jw);
                synchronized (evaluating) {
                    evaluating[0] = false;
                    evaluating.notifyAll();
                }
            }
            //System.out.println("    value = "+jw.getValue());
            return jw.getValue();
        }

        public synchronized void remove() {
            if (evaluatedWatch != null) {
                evaluatedWatch.remove();
            } else {
                w.remove ();
            }
        }

        public void setExpression(String expression) {
            w.setExpression (expression);
            expressionChanged();
        }

        public synchronized void setValue(String value) throws InvalidExpressionException {
            if (evaluatedWatch != null) {
                evaluatedWatch.setValue(value);
            } else {
                throw new InvalidExpressionException("Can not set value while evaluating.");
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            propSupp.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            propSupp.removePropertyChangeListener(l);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            model.fireTableValueChangedChanged (this, null);
        }
        
        /** Does wait for the value to be evaluated. */
        public void refresh() throws RefreshFailedException {
            synchronized (evaluating) {
                if (evaluating[0]) {
                    try {
                        evaluating.wait();
                    } catch (InterruptedException iex) {
                        throw new RefreshFailedException(iex.getLocalizedMessage());
                    }
                }
            }
        }
        
        /** Tells whether the variable is fully initialized and getValue()
         *  returns the value immediately. */
        public synchronized boolean isCurrent() {
            return evaluatedWatch != null;
        }

        public JPDAWatchEvaluating clone() {
            return new JPDAWatchEvaluating(model, w, debugger);
        }
        
    }
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference<WatchesModel> model;
        private WeakReference<JPDADebuggerImpl> debugger;
        
        private Listener (
            WatchesModel tm,
            JPDADebuggerImpl debugger
        ) {
            model = new WeakReference<WatchesModel>(tm);
            this.debugger = new WeakReference<JPDADebuggerImpl>(debugger);
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
            WatchesModel m = model.get ();
            if (m == null) destroy ();
            return m;
        }
        
        public void watchAdded (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.addPropertyChangeListener (this);
            m.fireWatchesChanged ();
        }
        
        public void watchRemoved (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.removePropertyChangeListener (this);
            m.fireWatchesChanged ();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if (DebuggerManager.PROP_WATCHES.equals(propName)) return ;
            final WatchesModel m = getModel ();
            if (m == null) return;
            if (m.debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
                destroy ();
                return;
            }
            if (m.debugger.getState () == JPDADebugger.STATE_RUNNING) {
                return ;
            }
            
            if (evt.getSource () instanceof Watch) {
                Object node;
                synchronized (m.watchToValue) {
                    node = m.watchToValue.get(evt.getSource());
                }
                if (node != null) {
                    m.fireTableValueChangedChanged(node, null);
                    return ;
                }
            }
            
            if (task == null) {
                task = RequestProcessor.getDefault ().create (new Runnable () {
                    public void run () {
                        if (verbose)
                            System.out.println("WM do task " + task);
                        m.fireTreeChanged ();
                    }
                });
                if (verbose)
                    System.out.println("WM  create task " + task);
            }
            task.schedule(100);
        }
        
        private void destroy () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            JPDADebugger d = debugger.get ();
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
    }
}
