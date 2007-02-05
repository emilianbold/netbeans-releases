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

package org.netbeans.modules.web.debug.watchesfiltering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import java.util.*;

/**
 * Tree model filter for JSP EL watches.
 *
 * @author Maros Sandor
 */
public class JspWatchesTreeFilter implements TreeModelFilter {
    
    private final JPDADebugger debugger;
    private final Map<Watch, JspElWatch> watch2JspElWatch = new HashMap<Watch, JspElWatch>();
    private DebuggerListener listener;

    public JspWatchesTreeFilter(ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent == original.getRoot()) {
            Watch [] allWatches = DebuggerManager.getDebuggerManager().getWatches();
            Object [] result = original.getChildren(parent, from, to);
            
            //original model returns array of JPDAWatch-es, thus we must create an Object array 
            //to allow merging with JspElWatch-es
            Object[] ch = new Object[result.length];
            System.arraycopy(result, 0, ch, 0, result.length);
            
            synchronized (watch2JspElWatch) {
                
                for (int i = from; i < allWatches.length; i++) {
                    Watch w = allWatches[i];
                    String expression = w.getExpression();
                    if (isJSPexpression(expression)) {
                        JspElWatch jw = (JspElWatch) watch2JspElWatch.get(w);
                        if (jw == null ) {
                            jw = new JspElWatch(w, debugger);
                            watch2JspElWatch.put(w, jw);
                        }
                        ch[i - from] = jw;
                    }
                }
            }
            
            if (listener == null) {
                listener = new DebuggerListener(this, debugger);
            }
            
            return ch;
        } else {
            return original.getChildren(parent, from, to);
        }
    }

    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        if (node == original.getRoot() && listener == null) {
            listener = new DebuggerListener(this, debugger);
        }
        return original.getChildrenCount(node);
    }

    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JspElWatch) return true;
        return original.isLeaf(node);
    }

    private boolean isJSPexpression(String expression) {
        return expression.startsWith("${") && expression.endsWith("}"); // NOI18N
    }
    
    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }
    
    void fireTreeChanged() {
        synchronized (watch2JspElWatch) {
            for (JspElWatch jspElWatch : watch2JspElWatch.values()) {
                jspElWatch.setUnevaluated();
            }
        }
    }
    
    private static class DebuggerListener implements PropertyChangeListener {
        
        WeakReference<JspWatchesTreeFilter> jspWatchesFilterRef;
        WeakReference<JPDADebugger> debuggerRef;
        
        DebuggerListener(JspWatchesTreeFilter jspWatchesFilter, JPDADebugger debugger) {
            jspWatchesFilterRef = new WeakReference<JspWatchesTreeFilter>(jspWatchesFilter);
            debuggerRef = new WeakReference<JPDADebugger>(debugger);
            debugger.addPropertyChangeListener(this);
        }

        public void propertyChange (PropertyChangeEvent evt) {
            
            if (debuggerRef.get().getState() == JPDADebugger.STATE_DISCONNECTED) {
                destroy();
                return;
            }
            if (debuggerRef.get().getState() == JPDADebugger.STATE_RUNNING) {
                return;
            }

            final JspWatchesTreeFilter jspWatchesFilter = getJspWatchesFilter();
            if (jspWatchesFilter != null) {
                jspWatchesFilter.fireTreeChanged();
            }
        }
        
        private JspWatchesTreeFilter getJspWatchesFilter() {
            JspWatchesTreeFilter jspWatchesFilter = jspWatchesFilterRef.get();
            if (jspWatchesFilter == null) {
                destroy();
            }
            return jspWatchesFilter;
        }
        
        private void destroy() {
            JPDADebugger debugger = debuggerRef.get();
            if (debugger != null) {
                debugger.removePropertyChangeListener(this);
            }
        }

    }

}
