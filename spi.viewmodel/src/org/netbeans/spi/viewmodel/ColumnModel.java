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
public abstract class ColumnModel implements Model {


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
     * Returns the character, that indicates a mnemonic key
     * for this column name. Can be <code>null</code>.
     *
     * @return the mnemonic key or <code>null</code>.
     * @since 1.11
     */
    public Character getDisplayedMnemonic() {
        return null;
    }
    
    /**
     * Returns type of column items.
     *
     * @return type of column items
     */
    public abstract Class getType ();
    
    /**
     * Returns ID of column this column should be installed before or 
     * <code>null</code>. Defines default order of columns only. 
     * This default order can be changed by user, and 
     * {@link #getCurrentOrderNumber} and {@link #setCurrentOrderNumber} are
     * used for sorting after that.
     *
     * @return ID of column this column should be installed before or 
     * <code>null</code>
     * @deprecated Not used. See {@link #getCurrentOrderNumber}.
     */
    public String getPreviuosColumnID () {
        return null;
    }
    
    /**
     * Returns ID of column this column should be installed after or 
     * <code>null</code>. Defines default order of columns only. 
     * This default order can be changed by user, and 
     * {@link #getCurrentOrderNumber} and {@link #setCurrentOrderNumber} are
     * used for sorting after that.
     *
     * @return ID of column next to this one or <code>null</code>
     * @deprecated Not used. See {@link #getCurrentOrderNumber}.
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
     * True if column can be sorted. Default implementation returns 
     * <code>true</code>.
     *
     * @return true if column can be sorted
     */
    public boolean isSortable () {
        return true;
    }
    
    /**
     * True if column should be visible. Default implementation 
     * returns <code>true</code>.
     *
     * @return <code>true</code> if column should be visible
     */
    public boolean isVisible () {
        return true;
    }
    
    /**
     * Set true if column is to be visible. Default implementation does nothing.
     *
     * @param visible set true if column is to be visible
     */
    public void setVisible (boolean visible) {}
    
    /**
     * True if column is sorted.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column is sorted.
     */
    public boolean isSorted () {
        return false;
    }
    
    /**
     * Set true if column is to be sorted. Default implementation does nothing.
     *
     * @param sorted set true if column is to be sorted
     */
    public void setSorted (boolean sorted) {}
    
    /**
     * True if column should be sorted in descending order.
     * Default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if column should be sorted
     *         in descending order
     */
    public boolean isSortedDescending () {
        return false;
    }
    
    /**
     * Set true if column is to be sorted in descending order.
     * Default implementation does nothing.
     *
     * @param sortedDescending set true if column is to be sorted
     *        in descending order
     */
    public void setSortedDescending (boolean sortedDescending) {}
    
    /**
     * Should return current order number of this column. Default value is 
     * <code>-1</code>.
     *
     * @return current order number of this column or <code>-1</code>
     */
    public int getCurrentOrderNumber () {
        return -1;
    }
    
    /**
     * Is called when current order number of this column is changed.
     * Default implementation does nothing.
     *
     * @param newOrderNumber new order number
     */
    public void setCurrentOrderNumber (int newOrderNumber) {}
    
    /**
     * Return column width of this column.
     *
     * @return column width of this column
     */
    public int getColumnWidth () {
        return 20;
    }
    
    /**
     * Is called when column width of this column is changed.
     * Default implementation does nothing.
     *
     * @param newColumnWidth a new column width
     */
    public void setColumnWidth (int newColumnWidth) {}
    
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
