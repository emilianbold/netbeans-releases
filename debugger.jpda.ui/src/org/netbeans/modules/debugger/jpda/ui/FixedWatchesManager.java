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

import javax.swing.KeyStroke;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;

/**
 * Manages lifecycle and presentation of fixed watches. Should be 
 * registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Jan Jancura, Maros Sandor
 */
public class FixedWatchesManager implements TreeModelFilter, 
NodeActionsProviderFilter, NodeModelFilter {
            
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch";
    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle (FixedWatchesManager.class).getString 
            ("CTL_DeleteFixedWatch_Label"),
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
    { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private final Action CREATE_FIXED_WATCH_ACTION = Models.createAction (
        NbBundle.getBundle (FixedWatchesManager.class).getString 
            ("CTL_CreateFixedWatch_Label"),
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
        
        
    private Map             fixedWatches = new HashMap ();
    private HashSet         listeners;
    private ContextProvider contextProvider;

    
    public FixedWatchesManager (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }
    

    // TreeModelFilter .........................................................
    
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }

    public Object[] getChildren (
        TreeModel original, 
        Object parent, 
        int from, 
        int to
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            if (fixedWatches.size () == 0) 
                return original.getChildren (parent, from, to);

            int fixedSize = fixedWatches.size ();
            int originalFrom = from - fixedSize;
            int originalTo = to - fixedSize;
            if (originalFrom < 0) originalFrom = 0;

            Object[] children;
            if (originalTo > originalFrom) {
                children = original.getChildren
                    (parent, originalFrom, originalTo);
            } else {
                children = new Object [0];
            }
            Object [] allChildren = new Object [children.length + fixedSize];

            fixedWatches.keySet ().toArray (allChildren);
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
        return original.getChildren (parent, from, to);
    }

    public int getChildrenCount (
        TreeModel original, 
        Object parent
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            int chc = original.getChildrenCount (parent);
            return chc + fixedWatches.size ();
        }
        return original.getChildrenCount (parent);
    }

    public boolean isLeaf (TreeModel original, Object node) 
    throws UnknownTypeException {
        return original.isLeaf (node);
    }

    public void addModelListener (ModelListener l) {
        HashSet newListeners = (listeners == null) ? 
            new HashSet () : (HashSet) listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    public void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone ();
        newListeners.remove (l);
        listeners = newListeners;
    }

    
    // NodeActionsProviderFilter ...............................................

    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        List myActions = new ArrayList();
        if (fixedWatches.containsKey (node)) {
            return new Action[] {
                DELETE_ACTION
            };
        }
        if (node instanceof Variable) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
        if (node instanceof JPDAWatch) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
            return actions;
        myActions.addAll (Arrays.asList (actions));
        return (Action[]) myActions.toArray (new Action [myActions.size ()]);
    }
    
    
    // NodeModel ...............................................................
    
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node))
            return (String) fixedWatches.get (node);
        return original.getDisplayName (node);
    }
    
    public String getShortDescription (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node)) {
            Variable v = (Variable) node;
            return ((String) fixedWatches.get (node)) + 
                " = (" + v.getType () + ") " + 
                v.getValue ();
        }
        return original.getShortDescription (node);
    }
    
    public String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node))
            return FIXED_WATCH;
        return original.getIconBase (node);
    }
    
    
    // other methods ...........................................................
    
    private void createFixedWatch (Object node) {
        if (node instanceof JPDAWatch) {
            JPDAWatch jw = (JPDAWatch) node;
            addFixedWatch (jw.getExpression (), jw);
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
            addFixedWatch (name, variable);
        }
    }

    private void addFixedWatch (String name, Variable variable) {
        fixedWatches.put (variable, name);
        fireModelChanged ();
    }

    private void fireModelChanged () {
        if (listeners == null) return;
        for (Iterator i = listeners.iterator (); i.hasNext ();) {
            ModelListener listener = (ModelListener) i.next();
            listener.modelChanged(null);;
        }
    }
}
