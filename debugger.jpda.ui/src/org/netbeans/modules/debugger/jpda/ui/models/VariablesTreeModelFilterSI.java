/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilter;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.debugger.jpda.ui.FixedWatch;


/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public class VariablesTreeModelFilterSI implements TreeModelFilter, 
NodeModel, TableModel, NodeActionsProvider {

    public static final String INHERITED =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable";
    public static final String STATIC =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable";
    private static final Set ignore = new HashSet (Arrays.asList (new String[] {
        "java.lang.String",
        "java.lang.StringBuffer",
        "java.lang.Character",
        "java.lang.Integer",
        "java.lang.Float",
        "java.lang.Byte",
        "java.lang.Boolean",
        "java.lang.Double",
        "java.lang.Long",
        "java.lang.Short",

        "java.lang.ref.WeakReference",
        
        "java.util.ArrayList",
        "java.util.HashSet",
        "java.util.LinkedHashSet",
        "java.util.LinkedList",
        "java.util.Stack",
        "java.util.TreeSet",
        "java.util.Vector",
        "java.util.Hashtable",
        "java.util.Hashtable$Entry",
        "java.util.HashMap",
        "java.util.HashMap$Entry",
        "java.util.IdentityHashMap",
        "java.util.AbstractMap$SimpleEntry",
        "java.util.TreeMap",
        "java.util.TreeMap$Entry",
        "java.util.WeakHashMap",
        "java.util.LinkedHashMap",
        "java.util.LinkedHashMap$Entry",
        "java.beans.PropertyChangeSupport"
    }));
    private ContextProvider lookupProvider;
    
    
    public VariablesTreeModelFilterSI (ContextProvider lookupProvider) {
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
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        parent = switchParentIfFixedWatch(parent);
        if (parent instanceof ObjectVariable) {
            ObjectVariable variable = (ObjectVariable) parent;
            if (ignore.contains (variable.getType ()))
                return original.getChildren (parent, from, to);
            int tto = Math.min (to, original.getChildrenCount (parent));
            List l = new ArrayList (Arrays.asList (
                original.getChildren (parent, from, tto)
            ));
            if ( (l.size () < to) && 
                 (variable.getAllStaticFields (0, 0).length > 0)
            ) l.add (new Object[] {"static", parent});
            if ( (l.size () < to) && 
                 (variable.getInheritedFields (0, 0).length > 0)
            ) l.add (new Object[] {"inherited", parent});
            return l.toArray ();
        } else
        if (parent instanceof Object[]) {
            Object[] os1 = (Object[]) parent;
            if (os1.length != 2) return original.getChildren (parent, from, to);
            
            if ("static".equals (os1 [0])) 
                return ((ObjectVariable) os1 [1]).getAllStaticFields (0, 0);
            if ("inherited".equals (os1 [0])) 
                return ((ObjectVariable) os1 [1]).getInheritedFields (0, 0);
        }
        return original.getChildren (parent, from, to);
    }
    
    /** 
     * Returns number of filtered children for given variable.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  number of filtered children for given variable
     */
    public int getChildrenCount (
        TreeModel   original, 
        Object      parent
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        parent = switchParentIfFixedWatch(parent);
        if (parent instanceof ObjectVariable) {
            ObjectVariable variable = (ObjectVariable) parent;
            if (ignore.contains (variable.getType ()))
                return original.getChildrenCount (parent);
            int i = original.getChildrenCount (parent);
            if (variable.getAllStaticFields (0, 0).length > 0) i++;
            if (variable.getInheritedFields (0, 0).length > 0) i++;
            return i;
        } else
        if (parent instanceof Object[]) {
            Object[] os1 = (Object[]) parent;
            if (os1.length != 2) return original.getChildrenCount (parent);
            
            if ("static".equals (os1 [0])) 
                return ((ObjectVariable) os1 [1]).getAllStaticFields (0, 0).
                    length;
            if ("inherited".equals (os1 [0])) 
                return ((ObjectVariable) os1 [1]).getInheritedFields (0, 0).
                    length;
        }
        return original.getChildrenCount (parent);
    }

    private Object switchParentIfFixedWatch(Object parent) {
        if (parent instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) parent;
            if (fw.getVariable() instanceof ObjectVariable) {
                return fw.getVariable();
            }
        }
        return parent;
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
        if (! (node instanceof Object[])) return original.isLeaf (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) return original.isLeaf (node);
        if ( (!"static".equals (os [0])) &&
             (!"inherited".equals (os [0]))) return original.isLeaf (node);
        return false;
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    
    // NodeModelFilter
    
    public String getDisplayName (Object node) 
    throws ComputingException, UnknownTypeException {
        if (!(node instanceof Object[])) throw new UnknownTypeException (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) throw new UnknownTypeException (node);
        if ("static".equals (os [0])) return "Static";
        if ("inherited".equals (os [0])) return "Inherited";
        throw new UnknownTypeException (node);
    }
    
    public String getIconBase (Object node) 
    throws ComputingException, UnknownTypeException {
        if (! (node instanceof Object[])) throw new UnknownTypeException (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) throw new UnknownTypeException (node);
        if ("static".equals (os [0])) return STATIC;
        if ("inherited".equals (os [0])) return INHERITED;
        throw new UnknownTypeException (node);
    }
    
    public String getShortDescription (Object node) 
    throws ComputingException, UnknownTypeException {
        if (! (node instanceof Object[])) throw new UnknownTypeException (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) throw new UnknownTypeException (node);
        if ("static".equals (os [0])) return null;
        if ("inherited".equals (os [0])) return null;
        throw new UnknownTypeException (node);
    }
    
    
    // NodeActionsProviderFilter
    
    public Action[] getActions (
        Object node
    ) throws UnknownTypeException {
        if (! (node instanceof Object[])) throw new UnknownTypeException (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) throw new UnknownTypeException (node);
        if ("static".equals (os [0])) return new Action [0];
        if ("inherited".equals (os [0])) return new Action [0];
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (
        Object node
    ) throws UnknownTypeException {
        if (!(node instanceof Object[])) throw new UnknownTypeException (node);
        Object[] os = (Object[]) node;
        if (os.length != 2) throw new UnknownTypeException (node);
        if ("static".equals (os [0])) return;
        if ("inherited".equals (os [0])) return;
        throw new UnknownTypeException (node);
    }
    
    
    // TableModelFilter
    
    public Object getValueAt (
        Object row, 
        String columnID
    ) throws ComputingException, UnknownTypeException {
        if (!(row instanceof Object[])) throw new UnknownTypeException (row);
        Object[] os = (Object[]) row;
        if (os.length != 2) throw new UnknownTypeException (row);
        if ("static".equals (os [0])) return "";
        if ("inherited".equals (os [0])) return "";
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        if (!(row instanceof Object[])) throw new UnknownTypeException (row);
        Object[] os = (Object[]) row;
        if (os.length != 2) throw new UnknownTypeException (row);
        if ("static".equals (os [0])) return true;
        if ("inherited".equals (os [0])) return true;
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (
        Object row, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        if (!(row instanceof Object[])) throw new UnknownTypeException (row);
        Object[] os = (Object[]) row;
        if (os.length != 2) throw new UnknownTypeException (row);
        if ("static".equals (os [0])) return;
        if ("inherited".equals (os [0])) return;
        throw new UnknownTypeException (row);
    }
    
    
    // helper methods ..........................................................
}
