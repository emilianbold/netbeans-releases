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
 * Defines model for one table view column. Can be used together with 
 * {@link TreeModel} for tree table view representation.
 *
 * @author   Jan Jancura
 */
public abstract class ColumnModel {
    
    
    /**
     * Returns unique ID of this column.
     *
     * @return unique ID of this column
     */
    public abstract String getID ();
    
    /** 
     * Returns display name of this column.
     *
     * @return display name of this column
     */
    public abstract String getDisplayName ();
    
    /**
     * Returns type of column items.
     *
     * @return type of column items
     */
    public abstract Class getType ();
    
    /**
     * Returns ID of column previous to this one. Default implementation returns 
     * <code>null</code> - first column or unsorted.
     *
     * @return ID of column previous to this one or <code>null</code>
     */
    public String getPreviuosColumnID () {
        return null;
    }
    
    /**
     * Returns ID of column next to this one. Default implementation returns 
     * <code>null</code> - last column or unsorted.
     *
     * @return ID of column next to this one or <code>null</code>
     */
    public String getNextColumnID () {
        return null;
    }
    
    /**
     * Returns tooltip for given column. Default implementation returns 
     * <code>null</code> - do not use tooltip.
     *
     * @return  tooltip for given node or <code>null</code>
     */
    public String getShortDescription () {
        return null;
    }
    
    /**
     * True if column should be visible by default. Default implementation 
     * returns <code>true</code>.
     *
     * @return <code>true</code> if column should be visible by default
     */
    public boolean initiallyVisible () {
        return true;
    }
    
    /**
     * True if column can be sorted. Default implementation returns 
     * <code>true</code>.
     *
     * @return true if column can be sorted
     */
    public boolean isSortable () {
        return true;
    }
    
    /**
     * True if column should be sorted by default.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column should be sorted by default
     */
    public boolean initiallySorted () {
        return false;
    }
    
    /**
     * True if column should be sorted by default in descending order.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column should be sorted by default 
     *         in descending order
     */
    public boolean initiallySortedDescending () {
        return false;
    }
    
    /**
     * Returns {@link java.beans.PropertyEditor} to be used for 
     * this column. Default implementation returns <code>null</code> - 
     * means use default PropertyEditor.
     *
     * @return {@link java.beans.PropertyEditor} to be used for 
     *         this column
     */
    public PropertyEditor getPropertyEditor () {
        return null;
    }
    
    /**
     * Rerturns {@link javax.swing.table.TableCellEditor} to be used for 
     * this column.
     *
     * @return {@link javax.swing.table.TableCellEditor} to be used for 
     *         this column
     */
//    public TableCellEditor getTableCellEditor () {
//        return null;
//    }
    
    /**
     * Rerturns {@link javax.swing.table.TableCellRenderer} to be used for 
     * this column.
     *
     * @return {@link javax.swing.table.TableCellRenderer} to be used for 
     *         this column
     */
//    public TableCellRenderer getTableCellRenderer () {
//        return null;
//    }
}
