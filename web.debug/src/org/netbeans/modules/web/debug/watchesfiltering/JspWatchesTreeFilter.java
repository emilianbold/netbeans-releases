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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.TreeModelListener;
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
    
    private JPDADebugger    debugger;

    public JspWatchesTreeFilter(ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    private Map watch2JspElWatch = new HashMap();
    
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent == original.getRoot()) {
            Watch [] allWatches = DebuggerManager.getDebuggerManager().getWatches();
            Object [] result = original.getChildren(parent, from, to);
            
            for (int i = from; i < to; i++) {
                Watch w = allWatches[i];
                String expression = w.getExpression();
                if (isJSPexpression(expression)) {
                    JspElWatch jw = (JspElWatch) watch2JspElWatch.get(w);
                    if (jw == null ) {
                        jw = new JspElWatch(w, debugger);
                        watch2JspElWatch.put(w, jw);
                    }
                    result[i - from] = jw;
                }
            }
            return result;
        } else {
            return original.getChildren(parent, from, to);
        }
    }

    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return original.getChildrenCount(node);
    }

    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JspElWatch) return true;
        return original.isLeaf(node);
    }

    private boolean isJSPexpression(String expression) {
        return expression.startsWith("${") && expression.endsWith("}");
    }
    
    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
}
