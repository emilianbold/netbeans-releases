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

package org.netbeans.spi.debugger.jpda;

import javax.swing.Action;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * This filter allows to change nodes in Locals View and Watches View for
 * some concrete variable types. For example itallows to define "logical" view
 * for java.util.Hashtable.
 *
 * Instances of this filter should be registerred in: "
 * "META-INF\debugger\netbeans-JPDADebuggerEngine\org.netbeans.spi.debugger.jpda.VariablesFilter"
 * file.
 *
 * @author   Jan Jancura
 */
public abstract class VariablesFilter {
    

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedTypes ();

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedAncestors ();
    
    /** 
     * Returns filtered children for given variable on given indexes.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public abstract Object[] getChildren (
        TreeModel      original,
        Variable       variable, 
        int            from, 
        int            to
    ) throws NoInformationException, ComputingException, UnknownTypeException;
    
    /**
     * Returns true if variable is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (
        TreeModel      original,
        Variable       variable
    ) throws UnknownTypeException;
    
    
    // NodeModelFilter
    
    /**
     * Returns filterred display name for given variable.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Variable variable) 
    throws ComputingException, UnknownTypeException;
    
    /**
     * Returns filterred icon for given variable.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Variable variable) 
    throws ComputingException, UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given variable.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Variable variable) 
    throws ComputingException, UnknownTypeException;
    
    
    // NodeActionsProviderFilter
    
    /**
     * Returns set of actions for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  set of actions for given variable
     */
    public abstract Action[] getActions (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    /**
     * Performs default action for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  default action for given variable
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    
    // TableModelFilter
    
    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and for variable <code>variable</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and variables are defined by values returned from 
     * {@TreeModel#getChildren}.
     *
     * @param   original the original table model
     * @param   variable a variable returned from {@TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException if there is no TableModel defined for given
     *          parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    public abstract Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws ComputingException, UnknownTypeException;
    
    /**
     * Filters original isReadOnly value from given table model.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public abstract boolean isReadOnly (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * for variable <code>variable</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and variable are defined by values returned from 
     * {@TreeModel#getChildren}.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public abstract void setValueAt (
        TableModel original, 
        Variable variable, 
        String columnID, 
        Object value
    ) throws UnknownTypeException;
}
