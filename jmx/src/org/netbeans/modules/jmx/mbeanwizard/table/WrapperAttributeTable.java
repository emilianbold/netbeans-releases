/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
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

    private static final int SELWIDTH = 55;
    
    /** Creates a new instance of WrapperAttributeTable */
    public WrapperAttributeTable(AbstractTableModel model, AttributesWizardPanel wiz) {
        super(model, wiz);
        ajustSelectionColumnWidth();
    }
    
    private void ajustSelectionColumnWidth() {
        TableColumnModel colModel = this.getColumnModel();
        TableColumn tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_SELECTION);
        tc.setMaxWidth(SELWIDTH);
        tc.setMinWidth(SELWIDTH);
        tc.setPreferredWidth(SELWIDTH);
    }
    
    protected void ajustAccessColumnWidth() {
        TableColumnModel colModel = this.getColumnModel();
        TableColumn tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_ACCESS+1);
        tc.setMaxWidth(ACCESSWIDTH); 
        tc.setMinWidth(ACCESSWIDTH);
        tc.setPreferredWidth(ACCESSWIDTH);
    }
    
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
             selBox.setName("wrapperAttrSelBox");// NOI18N
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
                         jcb.setName("wrapperAttrAccessBox");// NOI18N
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
