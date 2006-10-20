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

package org.netbeans.modules.debugger.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class BreakpointsTreeModel implements TreeModel {
    
    private Listener listener;
    private Vector listeners = new Vector ();
    
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to)
    throws UnknownTypeException {
        if (parent == ROOT) {
            Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                getBreakpoints ();
            ArrayList l = new ArrayList();
            int i, k = bs.length;
            for (i = 0; i < k; i++) {
                String gn = bs[i].getGroupName();
                if (gn.equals("")) {
                    l.add (bs [i]);
                } else {
                    if (!l.contains(gn)) {
                        l.add(gn);
                    }
                }
            }
            if (listener == null)
                listener = new Listener (this);
            if (to == 0)
                return l.toArray ();
            to = Math.min(l.size(), to);
            from = Math.min(l.size(), from);
            return l.subList (from, to).toArray ();
        } else
        if (parent instanceof String) {
            String groupName = (String) parent;
            Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                getBreakpoints ();
            ArrayList l = new ArrayList();
            int i, k = bs.length;
            for (i = 0; i < k; i++)
                if (bs [i].getGroupName ().equals (groupName))
                    l.add (bs [i]);
            if (listener == null)
                listener = new Listener (this);
            if (to == 0)
                return l.toArray ();
            to = Math.min(l.size(), to);
            from = Math.min(l.size(), from);
            return l.subList (from, to).toArray ();
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
        if (node == ROOT) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return getChildren (node, 0, 0).length;
        } else
        if (node instanceof String) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return getChildren (node, 0, 0).length;
        } else
        throw new UnknownTypeException (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof Breakpoint) return true;
        if (node instanceof String) return false;
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    private void fireTreeChanged (ModelEvent me) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (me);
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference model;
        
        public Listener (
            BreakpointsTreeModel tm
        ) {
            model = new WeakReference (tm);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_BREAKPOINTS,
                this
            );
            Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                getBreakpoints ();
            int i, k = bs.length;
            for (i = 0; i < k; i++)
                bs [i].addPropertyChangeListener (this);
        }
        
        private BreakpointsTreeModel getModel () {
            BreakpointsTreeModel m = (BreakpointsTreeModel) model.get ();
            if (m == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_BREAKPOINTS,
                    this
                );
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    bs [i].removePropertyChangeListener (this);
            }
            return m;
        }
        
        public void breakpointAdded (Breakpoint breakpoint) {
            BreakpointsTreeModel m = getModel ();
            if (m == null) return;
            breakpoint.addPropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        public void breakpointRemoved (Breakpoint breakpoint) {
            BreakpointsTreeModel m = getModel ();
            if (m == null) return;
            breakpoint.removePropertyChangeListener (this);
            m.fireTreeChanged ();
        }
    
        public void propertyChange (PropertyChangeEvent evt) {
            BreakpointsTreeModel m = getModel ();
            if (m == null) return;
            if (! (evt.getSource () instanceof Breakpoint))
                return;
            if (evt.getPropertyName () == Breakpoint.PROP_GROUP_NAME) {
                m.fireTreeChanged ();
            } else {
                m.fireTreeChanged (new ModelEvent.NodeChanged(
                        m, evt.getSource ()));
            }
        }
    }
    
}
