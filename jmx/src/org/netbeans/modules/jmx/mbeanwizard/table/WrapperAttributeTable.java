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
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.renderer.CheckBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.WrapperDescriptionTextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.renderer.WrapperTextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JCheckBoxCellEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperAttribute;
import org.netbeans.modules.jmx.mbeanwizard.renderer.WrapperComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;

/**
 *
 * @author an156382
 */
public class WrapperAttributeTable extends AttributeTable{
    
    /** Creates a new instance of WrapperAttributeTable */
    public WrapperAttributeTable(AbstractTableModel model, AttributesWizardPanel wiz) {
        super(model, wiz);
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
         } else {/*
             if (column == 1) { //attribute name
                 JTextField nameField = new JTextField();
                 String o = (String)getModel().getValueAt(row,column);
                 nameField.setText(o);
                 nameField.setEditable(false);
                 nameField.setEnabled(false);
                 nameField.addKeyListener(new AttributeTextFieldKeyListener());
                 return new JTextFieldCellEditor(nameField, this);
             } else {
                 if (column == 2) { //attribute type
                     JTextField typeField = new JTextField();
                     String o = (String)getModel().getValueAt(row,column);
                     typeField.setText(o);
                     typeField.setEditable(false);
                     typeField.setEnabled(false);
                     return new DefaultCellEditor(typeField);
                 } else {*/
                     if (column == 3) { //access mode
                         JComboBox jcb = new JComboBox();
                         // fills an MBean Attribute with the information in the model
                         MBeanWrapperAttribute mba = ((MBeanWrapperAttributeTableModel) getModel()).getAttribute(row);
                        /** test to fill the access JComboBox **/
                        if (mba.isOriginalReadable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_ONLY);
                        if (mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_WRITE_ONLY);
                        if (mba.isOriginalReadable() && mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
                         boolean selection = (Boolean)getModel().getValueAt(row,0);
                         jcb.setEditable(false);
                         jcb.setEnabled(selection);
                         return new JComboBoxCellEditor(jcb, this);
                     } else {
                         if (column == 4) { //attribute description
                             JTextField descrField = new JTextField();
                             String o = (String)getModel().getValueAt(row,column);
                             descrField.setText(o);
                             boolean selection = (Boolean)getModel().getValueAt(row,0);
                             descrField.setEditable(selection);
                             descrField.setEnabled(selection);
                             return new JTextFieldCellEditor(descrField, this);
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
        
        if (column == 0) { //selection
            JCheckBox cb = new JCheckBox();
            cb.setSelected(true);
            cb.setEnabled(true);
            return new CheckBoxRenderer(cb);
        } else {
            if ((column == 1)||(column == 2)) { //attribute Name
                return new WrapperTextFieldRenderer(new JTextField(),false,false);
            } else {
                    if (column == 3) { //attribute access
                        JComboBox jcb = new JComboBox();
                        // fills an MBean Attribute with the information in the model
                        MBeanWrapperAttribute mba = ((MBeanWrapperAttributeTableModel) getModel()).getAttribute(row);
                        /** test to fill the access JComboBox **/
                        if (mba.isOriginalReadable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_ONLY);
                        if (mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_WRITE_ONLY);
                        if (mba.isOriginalReadable() && mba.isOriginalWritable())
                            jcb.addItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
                        boolean selection = (Boolean)getModel().getValueAt(row,0);
                        jcb.setEditable(false);
                        jcb.setEnabled(selection);
                        return new WrapperComboBoxRenderer(jcb, false, selection);
                    } else {
                        if (column == 4) { //attribute description
                            JTextField txt = new JTextField();
                            boolean selection = (Boolean)getModel().getValueAt(row,0);
                            txt.setEditable(selection);
                            txt.setEnabled(selection);
                            //return new WrapperTextFieldRenderer(txt, selection, selection);
                            return new WrapperDescriptionTextFieldRenderer(
                                    txt, selection, selection);
                        }
                    }
                }
            }
        return super.getCellRenderer(row, column-1);
    }
    
    public boolean isCellEditable(int row, int col) {
        return ((col == 0) || (col == 3) || (col == 4));
    }
}