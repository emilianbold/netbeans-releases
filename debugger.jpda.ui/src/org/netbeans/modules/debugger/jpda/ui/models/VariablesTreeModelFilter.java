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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakSet;


/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public class VariablesTreeModelFilter implements TreeModelFilter, 
NodeModelFilter, TableModelFilter, NodeActionsProviderFilter {
    
    private ContextProvider lookupProvider;
    
    /** The set of nodes that alread evaluated their values. */
    static final Set evaluatedNodes = new WeakSet();
    
    /** The set of nodes by their models. */
    private final Map evaluatedNodesByModels = new WeakHashMap();
    
    
    public VariablesTreeModelFilter (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

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
        Object[] ch = null;
        if (ch == null) {
            VariablesFilter vf = getFilter (parent, true);
            if (vf == null) 
                ch = original.getChildren (parent, from, to);
            else
                ch = vf.getChildren (original, (Variable) parent, from, to);
        }
        synchronized (evaluatedNodes) {
            Set nodes = (Set) evaluatedNodesByModels.get(original);
            if (nodes == null) {
                original.addModelListener(new ValueChangeListener());
                //System.out.println("  ADDED LISTENER to "+original);
                nodes = new WeakSet();
                evaluatedNodesByModels.put(original, nodes);
            }
            for (int i = 0; i < ch.length; i++) {
                nodes.add(ch[i]);
            }
        }
        return ch;
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
        TreeModel   original, 
        Object      parent
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (parent, true);
        if (vf == null) 
            return original.getChildrenCount (parent);
        return vf.getChildrenCount (original, (Variable) parent);
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
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            return original.isLeaf (node);
        return vf.isLeaf (original, (Variable) node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
    
    
    // NodeModelFilter
    
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            return original.getDisplayName (node);
        return vf.getDisplayName (original, (Variable) node);
    }
    
    public String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            return original.getIconBase (node);
        return vf.getIconBase (original, (Variable) node);
    }
    
    public String getShortDescription (NodeModel original, Object node) 
    throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            return original.getShortDescription (node);
        return vf.getShortDescription (original, (Variable) node);
    }
    
    
    // NodeActionsProviderFilter
    
    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            return original.getActions (node);
        return vf.getActions (original, (Variable) node);
    }
    
    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true);
        if (vf == null) 
            original.performDefaultAction (node);
        else
            vf.performDefaultAction (original, (Variable) node);
    }
    
    
    // TableModelFilter
    
    public Object getValueAt (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        Object value;
        VariablesFilter vf = getFilter (row, false);
        if (vf == null) {
            value = original.getValueAt (row, columnID);
        } else {
            value = vf.getValueAt (original, (Variable) row, columnID);
        }
        synchronized (evaluatedNodes) {
            evaluatedNodes.add(row);
            Set nodes = (Set) evaluatedNodesByModels.get(original);
            if (nodes == null) {
                nodes = new WeakSet();
                evaluatedNodesByModels.put(original, nodes);
                original.addModelListener(new ValueChangeListener());
            }
            nodes.add(row);
        }
        return value;
    }
    
    public boolean isReadOnly (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, true);
        if (vf == null) 
            return original.isReadOnly (row, columnID);
        return vf.isReadOnly (original, (Variable) row, columnID);
    }
    
    public void setValueAt (
        TableModel original, 
        Object row, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, false);
        if (vf == null)
            original.setValueAt (row, columnID, value);
        else
            vf.setValueAt (original, (Variable) row, columnID, value);
    }
    
    
    // helper methods ..........................................................
    
    private HashMap typeToFilter;
    private HashMap ancestorToFilter;
    
    /**
     * @param lazy When <code>null</code>, get the correct filter immediately.
     *             Otherwise if the variable is not initialized, set this parameter
     *             to { true } and return <code>null</code>, when it is initialized,
     *             set this to { false } and return the filter (or <code>null</code>).
     * @return The filter or <code>null</code>.
     */
    private VariablesFilter getFilter (Object o, boolean checkEvaluated) {//, boolean[] lazy) {
        if (typeToFilter == null) {
            typeToFilter = new HashMap ();
            ancestorToFilter = new HashMap ();
            List l = lookupProvider.lookup (null, VariablesFilter.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                VariablesFilter f = (VariablesFilter) l.get (i);
                String[] types = f.getSupportedAncestors ();
                int j, jj = types.length;
                for (j = 0; j < jj; j++)
                    ancestorToFilter.put (types [j], f);
                types = f.getSupportedTypes ();
                jj = types.length;
                for (j = 0; j < jj; j++)
                    typeToFilter.put (types [j], f);
            }
        }
        
        if (typeToFilter.size() == 0) return null; // Optimization for corner case
        
        if (!(o instanceof Variable)) return null;
        
        Variable v = (Variable) o;
        
        if (checkEvaluated) {
            synchronized (evaluatedNodes) {
                //System.out.println("Var "+v+" is evaluated = "+evaluatedNodes.contains(v));
                if (!evaluatedNodes.contains(v)) {
                    return null;
                }
            }
        }
        
        String type = v.getType ();
        VariablesFilter vf = (VariablesFilter) typeToFilter.get (type);
        if (vf != null) return vf;
        
        if (!(o instanceof ObjectVariable)) return null;
        ObjectVariable ov = (ObjectVariable) o;
        ov = ov.getSuper ();
        while (ov != null) {
            type = ov.getType ();
            vf = (VariablesFilter) ancestorToFilter.get (type);
            if (vf != null) return vf;
            ov = ov.getSuper ();
        }
        return null;
    }

    private final class ValueChangeListener extends Object implements ModelListener {
        
        public void modelChanged(ModelEvent event) {
            Object node = null;
            if (event instanceof ModelEvent.NodeChanged) {
                node = ((ModelEvent.NodeChanged) event).getNode();
            } else if (event instanceof ModelEvent.TableValueChanged) {
                node = ((ModelEvent.TableValueChanged) event).getNode();
            } else if (event instanceof ModelEvent.TreeChanged) {
                Object model = ((ModelEvent.TreeChanged) event).getSource();
                synchronized (evaluatedNodes) {
                    Set nodes = (Set) evaluatedNodesByModels.remove(model);
                    if (nodes != null) {
                        for (Iterator it = nodes.iterator(); it.hasNext(); ) {
                            node = it.next();
                            evaluatedNodes.remove(node);
                        }
                    }
                }
                return ;
            } else {
                //System.out.println("\n\n  UNKNOWN EVENT  \n\n");
                return ;
            }
            synchronized (evaluatedNodes) {
                evaluatedNodes.remove(node);
            }
        }
        
    }
    
}
