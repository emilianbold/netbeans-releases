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
 * Manages lifecycle and presentation of fixed watches. Should be registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Jan Jancura, Maros Sandor
 */
public class FixedWatchesManager implements TreeModelFilter, 
NodeActionsProvider, NodeActionsProviderFilter, TableModel, NodeModel {
            
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch";
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
    { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
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
    private ContextProvider  contextProvider; // not used at the moment

    
    public FixedWatchesManager (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    private static String loc(String key) {
        return NbBundle.getBundle(FixedWatchesManager.class).getString(key);
    }
    
    // NodeActionsProvider .....................................................

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

    
    // NodeActionsProviderFilter ...............................................
    
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
            if (fixedWatches == null || fixedWatches.size
                () == 0) 
                return original.getChildren (parent, from, to);

            int fixedSize = fixedWatches.size();
            int originalFrom = from - fixedSize;
            int originalTo = to - fixedSize;
            if (originalFrom < 0) originalFrom = 0;

            Object [] children;
            if (originalTo > originalFrom) {
                children = original.getChildren
                    (parent, originalFrom, originalTo);
            } else {
                children = new Object [0];
            }
            Object [] allChildren = new Object [children.length + fixedSize];

            fixedWatches.toArray (allChildren);
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
    ) throws UnknownTypeException {
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

    public boolean isLeaf (TreeModel original, Object node) 
    throws UnknownTypeException {
        if (node instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) node;
            if (fw.getVariable () == null) 
                return true;
            return original.isLeaf (fw.getVariable ());
        }
        return original.isLeaf (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
        HashSet newListeners = (listeners == null) ? 
            new HashSet () : (HashSet) listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    public void removeTreeModelListener (TreeModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone ();
        newListeners.remove (l);
        listeners = newListeners;
    }
    
    
    // TableModel ..............................................................
    
    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof FixedWatch)
            return getOriginalModel ().getValueAt (
                ((FixedWatch) row).getVariable (),
                columnID
            );
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof FixedWatch)
            return getOriginalModel ().isReadOnly (
                ((FixedWatch) row).getVariable (),
                columnID
            );
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        if (row instanceof FixedWatch) {
            getOriginalModel ().setValueAt (
                ((FixedWatch) row).getVariable (),
                columnID,
                value
            );
            return;
        }
        throw new UnknownTypeException (row);
    }
    
    
    // NodeModel ...............................................................
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o instanceof FixedWatch)
            return ((FixedWatch) o).getName();
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) o;
            return fw.getName () + " = (" + fw.getType () + ") " + 
                fw.getValue ();
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof FixedWatch)
            return FIXED_WATCH;
        throw new UnknownTypeException (node);
    }
    
    
    // other methods ...........................................................
    
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

    private void fireModelChanged () {
        if (listeners == null) return;
        for (Iterator i = listeners.iterator (); i.hasNext ();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeChanged();;
        }
    }
    
    private TableModel original;
    private TableModel getOriginalModel () {
        if (original == null)
            original = Models.createCompoundTableModel (
                Models.createCompoundTableModel (
                    contextProvider.lookup ("WatchesView", TableModel.class)
                ),
                contextProvider.lookup ("WatchesView", TableModelFilter.class)
            );
        return original;
    }
}
