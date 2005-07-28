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
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.renderer.CheckBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.JCheckBoxCellEditor;
import javax.swing.JTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;

/**
 *
 * @author an156382
 */
public class WrapperOperationTable extends OperationTable {
    
    final JTable table;
    final AttributesWizardPanel wiz;
    
    /** Creates a new instance of WrapperOperationTable */
    public WrapperOperationTable(JPanel ancestorPanel, 
            AbstractTableModel model, AttributesWizardPanel wiz) {
        super(ancestorPanel,model,wiz);
        this.table = this;
        this.wiz = wiz;
    }
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
     public TableCellEditor getCellEditor(final int row, final int column) {
         
         if(row >= getRowCount())
             return null;
         
         
         
         if (column == 0) { //selection
             final JCheckBox selBox = new JCheckBox();
             selBox.setSelected((Boolean)getModel().getValueAt(row,column));
             selBox.setHorizontalAlignment(SwingConstants.CENTER);
             selBox.setEnabled(true);
             selBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     getModel().setValueAt(selBox.isSelected(), row, column);
                     ((AbstractJMXTableModel)getModel()).fireTableDataChanged();
                 }
             });
             //return new DefaultCellEditor(selBox);
             return new JCheckBoxCellEditor(selBox,this);
         } else {
             /*
             if (column == 1) { // operation name
                 final JTextField nameField = new JTextField();
                 String o = (String)getModel().getValueAt(row,column);
                 nameField.setText(o);
                 nameField.setEnabled(false);
                 nameField.setEditable(false);
                 nameField.addKeyListener(new OperationTextFieldKeyListener());
                 return new JTextFieldCellEditor(nameField, this); 
             } else {
                 if ((column == 2)|| (column == 3) || (column == 4)) { 
                     //operation return type
                     JTextField typeField = new JTextField();
                     String o = (String)getModel().getValueAt(row,column);
                     typeField.setText(o);
                     typeField.setEditable(false);
                     typeField.setEnabled(false);
                     return new JTextFieldCellEditor(typeField, this); 
                 } else {*/
                     if (column == 5) {
                         JTextField descrField = new JTextField();
                         String o = (String)getModel().getValueAt(row,column);
                         descrField.setText(o);
                         
                         boolean selection = (Boolean)getModel().getValueAt(row,0);
                         descrField.setEditable(selection);
                         descrField.setEnabled(selection);
                         
                         return new JTextFieldCellEditor(descrField, this);
                     }
                 }
         return super.getCellEditor(row,column);
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
        
        if (column == 0) {
            JCheckBox cb = new JCheckBox();
            cb.setSelected(true);
            cb.setEnabled(true);
            return new CheckBoxRenderer(cb);
        } else {
            if (column != 5) {
                JTextField txt = new JTextField();
                return new TextFieldRenderer(txt, false, false);
            } else {
                JTextField txt = new JTextField();
                boolean selection = (Boolean)getModel().getValueAt(row,0);
                txt.setEditable(selection);
                txt.setEnabled(selection);
                //return new WrapperTextFieldRenderer(txt, selection, selection);
                return new TextFieldRenderer(txt,selection,selection);
            }
        }
    }
    
    public boolean isCellEditable(int row, int col) {
        return ((col == 0) || (col == 5));
    }
}
