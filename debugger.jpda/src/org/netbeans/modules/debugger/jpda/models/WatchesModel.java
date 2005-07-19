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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.*;

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
    private Vector              listeners = new Vector ();
    private ContextProvider     lookupProvider;
    // Watch to Expression or Exception
    private Map                 watchToValue = new WeakHashMap(); // <node (expression), JPDAWatch>

    
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
            Watch[] fws = new Watch [to - from];
            System.arraycopy (ws, from, fws, 0, to - from);
            
            // 2) create JPDAWatches for Watches
            int i, k = fws.length;
            JPDAWatch[] jws = new JPDAWatch [k];
            for (i = 0; i < k; i++) {
                
                
                JPDAWatch jw = (JPDAWatch) watchToValue.get(fws[i]);
                if (jw == null) {
                    jw = new JPDAWatchEvaluating(this, fws[i], debugger);
                }
                jws[i] = jw;
                watchToValue.put(fws[i], jw);
                
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
            return DebuggerManager.getDebuggerManager ().getWatches ().length;
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
            watchToValue.clear();
        }
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    private void fireWatchesChanged () {

        class NodeChildrenChanged extends ModelEvent.NodeChanged
                                  implements javax.naming.ldap.ExtendedResponse {
            private String id;
            public NodeChildrenChanged(Object source, Object node, String id) {
                super(source, node);
                this.id = id;
            }
            public byte[] getEncodedValue() {
                return null;
            }
            public String getID() {
                return id;
            }
        }
        
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new NodeChildrenChanged(this, ROOT, org.openide.nodes.Node.PROP_LEAF);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    void fireTableValueChangedChanged (Object node, String propertyName) {
        synchronized (watchToValue) {
            for (Iterator it = watchToValue.keySet().iterator(); it.hasNext(); ) {
                Object w = it.next();
                if (node.equals(watchToValue.get(w))) {
                    watchToValue.remove(w);
                    ((JPDAWatchEvaluating) node).setEvaluated(null);
                    break;
                }
            }
            //watchToValue.remove(node);
        }
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
                                             implements JPDAWatch, Variable {//.Lazy {
        
        private WatchesModel model;
        private Watch w;
        private JPDADebuggerImpl debugger;
        private JPDAWatch evaluatedWatch;
        private Expression expression;
        private boolean[] evaluating = new boolean[] { false };
        private PropertyChangeSupport propSupp = new PropertyChangeSupport(this);
        
        public JPDAWatchEvaluating(WatchesModel model, Watch w, JPDADebuggerImpl debugger) {
            super(model.getLocalsTreeModel(), null, "" + w);
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
            } catch (ParseException e) {
                setEvaluated(new JPDAWatchImpl(model, w, e));
            }
        }
        
        Expression getParsedExpression() {
            return expression;
        }

        
        public void setEvaluated(JPDAWatch evaluatedWatch) {
            this.evaluatedWatch = evaluatedWatch;
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
        
        JPDAWatch getEvaluatedWatch() {
            return evaluatedWatch;
        }
        
        public void expressionChanged() {
            setEvaluated(null);
            parseExpression(w.getExpression());
        }
        
        public String getExceptionDescription() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExceptionDescription();
            } else {
                return null;
            }
        }

        public String getExpression() {
            if (evaluatedWatch != null) {
                return evaluatedWatch.getExpression();
            } else {
                return w.getExpression();
            }
        }

        public String getToStringValue() throws InvalidExpressionException {
            if (evaluatedWatch == null) {
                getValue();
            }
            return evaluatedWatch.getToStringValue();
        }

        public String getType() {
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
                if (evaluatedWatch != null) {
                    return evaluatedWatch.getValue();
                }
                evaluating[0] = true;
            }
            
            JPDAWatch jw = null;
            try {
                Expression expr = getParsedExpression();
                Value v = debugger.evaluateIn (expr);
                if (v instanceof ObjectReference)
                    jw = new JPDAObjectWatchImpl (model, w, (ObjectReference) v);
                jw = new JPDAWatchImpl (model, w, v);
            } catch (InvalidExpressionException e) {
                jw = new JPDAWatchImpl (model, w, e);
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

        public void remove() {
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

        public void setValue(String value) throws InvalidExpressionException {
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
        
    }
    
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
            }
            task.schedule(100);

            if (verbose)
                System.out.println("WM  create task " + task);
        }
        
        private void destroy () {
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
    }
}
