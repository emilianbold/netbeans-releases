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

package org.netbeans.spi.viewmodel;

import java.beans.PropertyEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


/**
 * Allows to filter content of some existing {@link TableModel}.
 *
 * @author   Jan Jancura
 */
public interface TableModelFilter {
    
    
    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}.
     *
     * @param   original the original table model
     * @param   node a object returned from {@TreeModel#getChildren} for this row
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
        Object node, 
        String columnID
    ) throws ComputingException, UnknownTypeException;
    
    /**
     * Filters original isReadOnly value from given table model.
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public abstract boolean isReadOnly (
        TableModel original,
        Object node, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}.
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public abstract void setValueAt (
        TableModel original,
        Object node, 
        String columnID, 
        Object value
    ) throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addTreeModelListener (TreeModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeTreeModelListener (TreeModelListener l);
}
