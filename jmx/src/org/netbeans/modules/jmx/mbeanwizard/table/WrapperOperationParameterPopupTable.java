/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.table;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;

/**
 * Class responsible for the parameter table in the operation parameter popup  
 * 
 */
public class WrapperOperationParameterPopupTable extends OperationParameterPopupTable {
    
    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    /**
     * Constructor
     * @param model the table model of this table
     */
    public WrapperOperationParameterPopupTable(AbstractTableModel model) {
        super(model);
    }
    
    /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        if ((column == 0) || (column == 1)) {
            return new TextFieldRenderer(new JTextField(),true,false);
        }
        
        return super.getCellRenderer(row,column);
    }
    
    public boolean isCellEditable(int row, int col) {
        return (col == 2);
    }
}
