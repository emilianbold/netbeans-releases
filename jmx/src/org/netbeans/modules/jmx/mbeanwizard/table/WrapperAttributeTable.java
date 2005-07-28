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
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttributePanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.renderer.CheckBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JCheckBoxCellEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.DefaultCellEditor;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperAttribute;
import org.netbeans.modules.jmx.mbeanwizard.listener.AttributeTextFieldKeyListener;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.EmptyRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
        
/**
 *
 * @author an156382
 */
public class WrapperAttributeTable extends AttributeTable{

    /** Creates a new instance of WrapperAttributeTable */
    public WrapperAttributeTable(AbstractTableModel model, AttributesWizardPanel wiz) {
        super(model, wiz);
        //ajustColumnWidth();
    }
    
    /* ColumnWidth ??
    private void ajustColumnWidth() {
        TableColumnModel colModel = this.getColumnModel();
        TableColumn tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_SELECTION);
        tc.setMaxWidth(55);
        tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_NAME +1);
        tc.setMaxWidth(95);
        tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_TYPE +1);
        tc.setMaxWidth(95);
        tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_ACCESS +1);
        tc.setMaxWidth(105);
        tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_DESCRIPTION +1);
        tc.setMaxWidth(getDescriptionColumnWidth());
    }
    
    public int getDescriptionColumnWidth() {
        return ((int)this.getPreferredScrollableViewportSize().getWidth() - 55 - 95 - 95 - 105);
    }
    */
    
    public MBeanWrapperAttributeTableModel getModel() {
        return (MBeanWrapperAttributeTableModel)super.getModel();
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
         
         int editableRow = ((MBeanWrapperAttributeTableModel)getModel()).getFirstEditableRow();
         boolean selection = (Boolean)getModel().getValueAt(row,0);
         
         if (column == 0) { //selection
             final JCheckBox selBox = new JCheckBox();
             selBox.setSelected((Boolean)getModel().getValueAt(row,column));
             selBox.setHorizontalAlignment(SwingConstants.CENTER);
             selBox.setEnabled(true);
             selBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     getModel().setValueAt(selBox.isSelected(), row, column);
                     ((AbstractJMXTableModel)getModel()).fireTableDataChanged();
                     wiz.event();
                 }
             });
             //return new DefaultCellEditor(selBox);
             return new JCheckBoxCellEditor(selBox,this);
         } else {
             if (column == 1) { //attribute name
                 JTextField nameField = new JTextField();
                 String o = (String)getModel().getValueAt(row,column);
                 nameField.setText(o);
                 //nameField.setEditable(false);
                 //nameField.setEnabled(false);
                 nameField.addKeyListener(new AttributeTextFieldKeyListener());
                 return new JTextFieldCellEditor(nameField, this);
             } else {
                 if (column == 2) { //attribute type
                     if (row < editableRow) {
                         JTextField typeField = new JTextField();
                         String o = (String)getModel().getValueAt(row,column);
                         typeField.setText(o);
                         //typeField.setEditable(false);
                         //typeField.setEnabled(false);
                         return new DefaultCellEditor(typeField);
                     } else {
                         JComboBox jcb = WizardHelpers.instanciateTypeJComboBox();
                         return new JComboBoxCellEditor(jcb, this);
                     }
                 } else {
                     if (column == 3) { //access mode
                         
                         JComboBox jcb = new JComboBox();
                         // fills an MBean Attribute with the information in the model
                         MBeanWrapperAttribute mba = ((MBeanWrapperAttributeTableModel) getModel()).getWrapperAttribute(row);
                        /** test to fill the access JComboBox **/
                         if (row < editableRow) {
                             if (mba.isOriginalReadable())
                                 jcb.addItem(WizardConstants.ATTR_ACCESS_READ_ONLY);
                             if (mba.isOriginalWritable())
                                 jcb.addItem(WizardConstants.ATTR_ACCESS_WRITE_ONLY);
                             if (mba.isOriginalReadable() && mba.isOriginalWritable())
                                 jcb.addItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
                         } else {
                             jcb = WizardHelpers.instanciateAccessJComboBox();
                         }
                         jcb.setEditable(false);
                         jcb.setEnabled(true);
                         return new JComboBoxCellEditor(jcb, this);
                     } else {
                         if (column == 4) { //attribute description
                             JTextField descrField = new JTextField();
                             String o = (String)getModel().getValueAt(row,column);
                             descrField.setText(o);
                             descrField.setEnabled(true);
                             return new JTextFieldCellEditor(descrField, this);
                         }
                     }
                 }
             }
         }
         return super.getCellEditor(row,column-1);
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
        
        int editableRow = ((MBeanWrapperAttributeTableModel)getModel()).getFirstEditableRow();
        
        if (column == 0) { //selection
            if (row < editableRow) {
                JCheckBox cb = new JCheckBox();
                cb.setEnabled(true);
                return new CheckBoxRenderer(cb);
            } else
                return new EmptyRenderer(new JTextField());
        } else {
            if (column == 1) { //attribute Name
                boolean ok = (row < editableRow);
                //if (ok)
                    //return new WrapperTextFieldRenderer(new JTextField(),true,false);
                    return new TextFieldRenderer(new JTextField(),true,!ok);
                //else
                //    return new TextFieldRenderer(new JTextField(),true,true);
            } else {
                if (column == 2) {
                    if (row < editableRow)
                        //return new WrapperTextFieldRenderer(new JTextField(),true,false);
                        return new TextFieldRenderer(new JTextField(),true,false);
                    else {
                        JComboBox jcb = WizardHelpers.instanciateTypeJComboBox();
                        return new ComboBoxRenderer(jcb, true, true);
                    }
                } else {
                    if (column == 3) { //attribute access
                        JComboBox jcb = new JComboBox();
                        // fills an MBean Attribute with the information in the model
                        MBeanWrapperAttribute mba = ((MBeanWrapperAttributeTableModel) getModel()).getWrapperAttribute(row);
                        /** test to fill the access JComboBox **/
                        if (mba.isOriginalReadable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_ONLY);
                        if (mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_WRITE_ONLY);
                        if (mba.isOriginalReadable() && mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
                        return new ComboBoxRenderer(jcb, true, false); 
                    } else {
                        if (column == 4) { //attribute description
                            JTextField txt = new JTextField();
                            boolean selection = (Boolean)getModel().getValueAt(row,0);
                            //return new WrapperDescriptionTextFieldRenderer(
                            return new TextFieldRenderer(txt, true, selection);
                            }
                        }
                    }
                }
            }
        return super.getCellRenderer(row, column-1);
    }
    
    public boolean isCellEditable(int row, int col) {
        
        int editableRow = ((MBeanWrapperAttributeTableModel)getModel()).getFirstEditableRow();
        boolean isChecked = (Boolean)getModel().getValueAt(row,0);
        
        if (row < editableRow) {
            if (isChecked)
                return ((col == 0) || (col == 3) || (col == 4));
            else
                return (col ==0);
        }
        else
            return (col != 0);
    }
    
    /**
     * Returns the wizard panel of the parent dialog
     * @return FireEvent
     */
    public FireEvent getWiz() {
        
        return this.wiz;
    }
}