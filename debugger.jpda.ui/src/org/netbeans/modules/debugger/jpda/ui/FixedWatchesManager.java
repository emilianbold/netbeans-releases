/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import org.netbeans.spi.viewmodel.*;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.*;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;

/**
 * Manages lifecycle and presentation of fixed watches. Should be registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Maros Sandor
 */
public class FixedWatchesManager implements TreeModelFilter, 
NodeActionsProvider, NodeActionsProviderFilter {
            
    private final Action DELETE_ACTION = Models.createAction (
        loc("CTL_DeleteFixedWatch_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    fixedWatches.remove (nodes [i]);
                fireModelChanged ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    private final Action CREATE_FIXED_WATCH_ACTION = Models.createAction (
        loc("CTL_CreateFixedWatch_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    createFixedWatch (nodes [i]);
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
        
        
    private List            fixedWatches;
    private HashSet         listeners;
    private ContextProvider  lookupProvider; // not used at the moment

    
    public FixedWatchesManager (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    private static String loc(String key) {
        return NbBundle.getBundle(FixedWatchesManager.class).getString(key);
    }

    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (!(node instanceof FixedWatch)) 
            throw new UnknownTypeException (node);
    }

    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node instanceof FixedWatch) {
            return new Action[] {
                DELETE_ACTION
            };
        }
        throw new UnknownTypeException(node);
    }

    public void performDefaultAction (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        Action [] actions = original.getActions(node);
        List myActions = new ArrayList();
        if (node instanceof Variable) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else if (node instanceof JPDAWatch) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else if (node instanceof FixedWatch) {
            myActions.add (DELETE_ACTION);
        } else {
            return actions;
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }
    
    private void createFixedWatch (Object node) {
        if (node instanceof JPDAWatch) {
            JPDAWatch jw = (JPDAWatch) node;
            createFixedWatch (jw.getExpression (), jw);
        } else {
            Variable variable = (Variable) node;
            String name = null;
            if (variable instanceof LocalVariable) {
                name = ((LocalVariable) variable).getName ();
            } else if (variable instanceof Field) {
                name = ((Field) variable).getName();
            } else if (variable instanceof This) {
                name = "this";
            } else if (variable instanceof ObjectVariable) {
                name = "object";
            } else {
                name = "unnamed";
            }
            createFixedWatch(name, variable);
        }
    }

    private void createFixedWatch (String name, Variable variable) {
        if (fixedWatches == null) fixedWatches = new ArrayList();
        FixedWatch fw = new FixedWatch(name, variable);
        fixedWatches.add(fw);
        fireModelChanged();
    }

    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }

    public Object[] getChildren (
        TreeModel original, 
        Object parent, 
        int from, 
        int to
    ) throws NoInformationException,ComputingException, UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            if (fixedWatches == null || fixedWatches.size() == 0) return original.getChildren(parent, from, to);

            int fixedSize = fixedWatches.size();
            int originalFrom = from - fixedSize;
            int originalTo = to - fixedSize;
            if (originalFrom < 0) originalFrom = 0;

            Object [] children;
            if (originalTo > originalFrom) {
                children = original.getChildren(parent, originalFrom, originalTo);
            } else {
                children = new Object[0];
            }
            Object [] allChildren = new Object[children.length + fixedSize];

            fixedWatches.toArray(allChildren);
            System.arraycopy (
                children, 
                0, 
                allChildren, 
                fixedSize,
                children.length
            );
            Object[] fallChildren = new Object [to - from];
            System.arraycopy (allChildren, from, fallChildren, 0, to - from);
            return fallChildren;
        }
        if (parent instanceof FixedWatch) {
            Variable v = ((FixedWatch) parent).getVariable ();
            return (v != null) ? 
                original.getChildren (v, from, to) : 
                new Object [0];
        }
        return original.getChildren (parent, from, to);
    }

    public int getChildrenCount (
        TreeModel original, 
        Object parent
    ) throws NoInformationException,ComputingException, UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            int chc = original.getChildrenCount (parent);
            if (fixedWatches == null) return chc;
            return chc + fixedWatches.size ();
        }
        if (parent instanceof FixedWatch) {
            Variable v = ((FixedWatch) parent).getVariable ();
            return (v != null) ? original.getChildrenCount (v) : 0;
        }
        return original.getChildrenCount (parent);
    }

    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) node;
            if (fw.getVariable() == null) return true;
            return original.isLeaf(fw.getVariable());
        }
        return original.isLeaf(node);
    }

    private void fireModelChanged() {
        if (listeners == null) return;
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeChanged();;
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        HashSet newListeners = (listeners == null) ? new HashSet() : (HashSet) listeners.clone();
        newListeners.add(l);
        listeners = newListeners;
    }

    public void removeTreeModelListener(TreeModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove(l);
        listeners = newListeners;
    }
}
