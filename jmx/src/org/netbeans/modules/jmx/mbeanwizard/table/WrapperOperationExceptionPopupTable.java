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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.table;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;

/**
 * Class responsible for the exception table in the operation exception popup
 *
 */
public class WrapperOperationExceptionPopupTable extends OperationExceptionPopupTable {

    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    /**
     * Constructor
     * @param model the table model of this table
     */
    public WrapperOperationExceptionPopupTable(AbstractTableModel model) {
        super(model);
    }
    
     public TableCellRenderer getCellRenderer(int row, int column) {
         if (column == 0)
             return new TextFieldRenderer(new JTextField(), true, false);
         else
             return super.getCellRenderer(row, column);
     } 
    
    public boolean isCellEditable(int row, int col) {
        return (col == 1);
    }

}
