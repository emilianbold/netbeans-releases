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
package org.netbeans.modules.jmx.mbeanwizard.editor;
import org.netbeans.modules.jmx.mbeanwizard.listener.TableRemoveListener;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.Component;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
        
/**
 * Class implementing the behaviour for the editor of a Text field
 * 
 */
public class JTextFieldCellEditor extends DefaultCellEditor 
        implements FocusListener, TableRemoveListener {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
        private JTextField tf;
        private JTable table;
        private TableModel model;
        
        private int editedRow = 0;
        private int editedColumn = 0;
        
        /**
         * Constructor
         * @param tf the textfield
         * @param table the JTable containing the textfield
         */
        public JTextFieldCellEditor(JTextField tf, JTable table) {
            super(tf);
            this.tf = tf;
            this.table = table;
            this.model = table.getModel();
            ((AbstractJMXTableModel)this.model).addTableRemoveListener(this);
            tf.addFocusListener(this);
            super.setClickCountToStart(1);
        }

        /**
         * Overriden method; called eached time the component gets in the 
         * editor mode
         * @param table the JTable in which the component is in
         * @param value the object with the current value
         * @param isSelected boolean indicating whether the component is 
         * selected or not
         * @param row the selected row in the table
         * @param column the selected column in the table
         * @return Component the modified component
         */
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            tf.setText(model.getValueAt(row,column).toString());
            return tf;
        }
        
        /**
         * Method defining the behaviour of the component when he gets 
         * the focus
         * @param e a focusEvent
         */
        public void focusGained(FocusEvent e) {
            tf.selectAll();
            
            editedRow = table.getEditingRow();
            editedColumn = table.getEditingColumn();
        }
        
        /**
         * Method defining the behaviour of the component when he looses 
         * the focus
         * @param e a focusEvent
         */
        public void focusLost(FocusEvent e) {
            if (editedRow < ((AbstractJMXTableModel)table.getModel()).size()) 
                model.setValueAt(tf.getText(), editedRow, editedColumn);
            
            if (table instanceof AttributeTable) {
                ((AttributeTable)table).getWiz().event();
            } else {
                if (table instanceof OperationTable) {
                    ((OperationTable)table).getWiz().event();
                }
            }
            
        }
        
        /**
         * Method which sets the model right after a Table Model Event has 
         * been thrown
         * @param e the table model event
         */
        public void tableStateChanged(TableModelEvent e) {
            if (e.getFirstRow() < ((AbstractJMXTableModel)
                        table.getModel()).size()) {
                if (e.getColumn() != -1)
                    tf.setText(model.getValueAt(e.getFirstRow(),
                            e.getColumn()).toString());
            }
        }
}