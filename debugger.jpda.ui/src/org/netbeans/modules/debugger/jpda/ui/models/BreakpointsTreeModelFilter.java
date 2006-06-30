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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * Filters content of some original tree of nodes (represented by 
 * {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public class BreakpointsTreeModelFilter implements TreeModelFilter {
    
    static Map MAX_LINES = new WeakHashMap();
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.show_hidden_breakpoints") != null;

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren (
        TreeModel   original, 
        Object      parent, 
        int         from, 
        int         to
    ) throws UnknownTypeException {
        if (to - from <= 0) return new Object[0]; 
        Object[] ch = original.getChildren (parent, 0, 0);
        List l = new ArrayList ();
        int i, k = ch.length, n = to - from;
        Map maxLines = new HashMap();
        for (i = 0; i < k; i++) {
            if ( (!verbose) &&
                 (ch [i] instanceof JPDABreakpoint) &&
                 ((JPDABreakpoint) ch [i]).isHidden ()
            ) continue;
            if (--from >= 0) continue;
            l.add (ch [i]);
            if (ch[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) ch[i];
                String fn = EditorContextBridge.getFileName(lb);
                int line = lb.getLineNumber();
                Integer mI = (Integer) maxLines.get(fn);
                if (mI != null) {
                    line = Math.max(line, mI.intValue());
                }
                mI = new Integer(line);
                maxLines.put(fn, mI);
            }
            if (--n == 0) break;
        }
        for (i = l.size() - 1; i >= 0; i--) {
            Object o = l.get(i);
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) o;
                MAX_LINES.put(lb, maxLines.get(EditorContextBridge.getFileName(lb)));
            }
        }
        return l.toArray();
    }
    
    /**
     * Returns number of filterred children for given node.
     * 
     * @param   original the original tree model
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (
        TreeModel original,
        Object node
    ) throws UnknownTypeException {
        return getChildren(original, node, 0, Integer.MAX_VALUE).length;
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf (
        TreeModel original, 
        Object node
    ) throws UnknownTypeException {
        return original. isLeaf (node);
    }

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
}
