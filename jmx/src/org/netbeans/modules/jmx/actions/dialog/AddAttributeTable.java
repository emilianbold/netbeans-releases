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

package org.netbeans.modules.jmx.actions.dialog;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.WrapperTextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;

/**
 * Class responsible for the attribute table shown when you use Add Attributes...
 * popup action in the contextual management menu.
 * @author tl156378
 */
public class AddAttributeTable extends AttributeTable {
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the panel to notify for events
     */
    public AddAttributeTable(AbstractTableModel model, FireEvent wiz) {
        super(model,wiz);
    }
        
   /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        AddMBeanAttributeTableModel addAttrModel = 
                (AddMBeanAttributeTableModel) this.getModel();
        int firstEditable = addAttrModel.getFirstEditable();
        
        if(row < firstEditable) {
            switch (column) {
                case AddMBeanAttributeTableModel.IDX_ATTR_NAME :
                    return new WrapperTextFieldRenderer(new JTextField(), false);
                case AddMBeanAttributeTableModel.IDX_ATTR_TYPE :
                    JComboBox typeBox = WizardHelpers.instanciateTypeJComboBox();
                    return new ComboBoxRenderer(typeBox,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_ACCESS :
                    JComboBox accessBox = WizardHelpers.instanciateAccessJComboBox();
                    return new ComboBoxRenderer(accessBox,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_DESCRIPTION :
                    return new WrapperTextFieldRenderer(new JTextField(), false);
                default : 
                    return super.getCellRenderer(row,column);
            }
        }
            
        return super.getCellRenderer(row,column);
    }
}
