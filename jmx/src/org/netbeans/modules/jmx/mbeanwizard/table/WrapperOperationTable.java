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
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.CheckBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.JCheckBoxCellEditor;
import javax.swing.JTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.MBeanOperationPanel.OperationWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.listener.OperationTextFieldKeyListener;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperOperationTableModel;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationExceptionPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationParameterPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationExceptionPopup;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationParameterPopup;
import org.netbeans.modules.jmx.mbeanwizard.renderer.EmptyRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationParameterPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationExceptionPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.popup.WrapperOperationExceptionPopup;
import org.netbeans.modules.jmx.mbeanwizard.popup.WrapperOperationParameterPopup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;

/**
 *
 * @author an156382
 */
public class WrapperOperationTable extends OperationTable {
    
    private static final int SELWIDTH = 55;
    
    final JTable table;
    final OperationWizardPanel wiz;
    private MBeanWrapperOperationTableModel model;
    
    /** Creates a new instance of WrapperOperationTable */
    public WrapperOperationTable(JPanel ancestorPanel, 
            AbstractTableModel model, OperationWizardPanel wiz) {
        super(ancestorPanel,model,wiz);
        this.table = this;
        this.model = (MBeanWrapperOperationTableModel) model;
        this.wiz = wiz;
        ajustSelectionColumnWidth();
    }
    
    private void ajustSelectionColumnWidth() {
        TableColumnModel colModel = this.getColumnModel();
        TableColumn tc = colModel.getColumn(MBeanWrapperAttributeTableModel.IDX_ATTR_SELECTION);
        tc.setMaxWidth(SELWIDTH);
        tc.setMinWidth(SELWIDTH);
        tc.setPreferredWidth(SELWIDTH);
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
         
         int firstEditableRow = ((MBeanWrapperOperationTableModel)getModel()).getFirstEditableRow();
         
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
         } else if (column == 1) { //operation name
             JTextField nameField = new JTextField();
             String o = (String)getModel().getValueAt(row,column);
             nameField.setText(o);
             nameField.addKeyListener(new OperationTextFieldKeyListener());
             return new JTextFieldCellEditor(nameField, this);
         } else if (column == 2) { //operation return type
             if (row < firstEditableRow) {
                 JTextField typeField = new JTextField();
                 String o = (String)getModel().getValueAt(row,column);
                 typeField.setText(o);
                 return new DefaultCellEditor(typeField);
             } else {
                 JComboBox jcb = WizardHelpers.instanciateRetTypeJComboBox();
                 jcb.setName("methTypeBox");// NOI18N
                 return new JComboBoxCellEditor(jcb, this);
             }
         } else if (column == 3) { //parameter panel
             final JTextField paramField = new JTextField();
             paramField.setEditable(false);
             paramField.setName("methParamTextField");// NOI18N
             JButton paramButton =
                     new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
             paramButton.setName("methAddParamButton");// NOI18N
             paramButton.setMargin(new java.awt.Insets(2,2,2,2));
             final int editedRow = row;
             
             int nbParam = model.getWrapperOperation(row).getParametersSize();
             final boolean nonEditableParams = ((row < firstEditableRow) && (nbParam > 0));
             
             paramButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     OperationParameterPopup methParamPopup;
                     if (nonEditableParams)
                         methParamPopup = new WrapperOperationParameterPopup(
                             ancestorPanel, model, paramField,
                             editedRow, wiz);
                     else
                         methParamPopup = new OperationParameterPopup(
                             ancestorPanel, model, paramField,
                             editedRow, wiz); 
                 }
             });
             
             boolean stateBut = (row >= firstEditableRow) ||
                     ((row < firstEditableRow) && selection && (nbParam > 0));
             paramButton.setEnabled(stateBut);
             
             JPanel panel = new JPanel(new BorderLayout());
             panel.setName("methParamColJPanel");// NOI18N
             panel.add(paramField, BorderLayout.CENTER);
             panel.add(paramButton, BorderLayout.EAST);
             
             OperationParameterPanelEditor operationParamEditor = new
                     OperationParameterPanelEditor(model, panel,
                     paramField, editedRow);
             
             //TODO Edit OperationParameterPanelEditor
             //return new OperationParameterPanelEditor(panel,
             //paramField, paramButton);
             return operationParamEditor;
         } else if (column == 4) { //operation exceptions
             final JTextField excepField = new JTextField();
             excepField.setEditable(false);
             excepField.setName("methExcepTextField");// NOI18N
             JButton excepButton = new JButton(
                     WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
             excepButton.setName("methAddExcepJButton");// NOI18N
             excepButton.setMargin(new java.awt.Insets(2,2,2,2));
             final int editedRow = row;
             
             int nbExcep = model.getWrapperOperation(row).getExceptionsSize();
             final boolean nonEditableExcept = ((row < firstEditableRow) && (nbExcep > 0));
             excepButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     OperationExceptionPopup opExceptionPopup;
                     if (nonEditableExcept)
                         opExceptionPopup = new WrapperOperationExceptionPopup(
                             ancestorPanel, model, excepField,
                             editedRow);
                     else
                         opExceptionPopup = new OperationExceptionPopup(
                             ancestorPanel, model, excepField,
                             editedRow);
                 }
             });
             
             boolean stateBut = (row >= firstEditableRow) ||
                     ((row < firstEditableRow) && selection && (nbExcep > 0));
             excepButton.setEnabled(stateBut);
             
             JPanel panel = new JPanel(new BorderLayout());
             panel.add(excepField, BorderLayout.CENTER);
             panel.add(excepButton, BorderLayout.EAST);
             
             OperationExceptionPanelEditor methExcepEditor = new
                     OperationExceptionPanelEditor(model,panel,
                     excepField, row);
             
             //TODO Edit OperationExceptionPanelEditor
             //return new OperationExceptionPanelEditor(panel,
             //excepField, excepButton);
             return methExcepEditor;
         } else if (column == 5) {
             JTextField descrField = new JTextField();
             String o = (String)getModel().getValueAt(row,column);
             descrField.setText(o);
             descrField.setEnabled(true);
             return new JTextFieldCellEditor(descrField, this);
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
        
        int editableRow = model.getFirstEditableRow();
        boolean selection = (Boolean)getModel().getValueAt(row,0);
        
        if (column == 0) { //selection
            if (row < editableRow) {
                JCheckBox cb = new JCheckBox();
                cb.setEnabled(true);
                return new CheckBoxRenderer(cb);
            } else
                return new EmptyRenderer(new JTextField());
        } else if (column == 1) { //operation Name
            boolean ok = (row < editableRow);
            //if (ok)
            //return new WrapperTextFieldRenderer(new JTextField(),true,false);
            return new TextFieldRenderer(new JTextField(),true,!ok);
            //else
            //    return new TextFieldRenderer(new JTextField(),true,true);
        } else if (column == 2) {
            if (row < editableRow)
                //return new WrapperTextFieldRenderer(new JTextField(),true,false);
                return new TextFieldRenderer(new JTextField(),true,false);
            else {
                JComboBox jcb = WizardHelpers.instanciateRetTypeJComboBox();
                return new ComboBoxRenderer(jcb, true, true);
            }
        } else if (column == 3) {
            JTextField paramField = new JTextField();
            paramField.setEditable(false);
            paramField.setName("methParamTextField");// NOI18N
            JButton paramButton =
                    new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
            paramButton.setMargin(new java.awt.Insets(2,2,2,2));
            int nbParam = model.getWrapperOperation(row).getParametersSize();
            boolean stateBut = (row >= editableRow) ||
                    ((row < editableRow) && selection && (nbParam > 0));
            paramButton.setEnabled(stateBut);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(paramField, BorderLayout.CENTER);
            panel.add(paramButton, BorderLayout.EAST);
            return new OperationParameterPanelRenderer(panel, paramField);
        } else if (column == 4) {
            JTextField excepField = new JTextField();
            excepField.setEditable(false);
            excepField.setName("methExcepTextField");// NOI18N
            JButton excepButton =
                    new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
            excepButton.setMargin(new java.awt.Insets(2,2,2,2));
            
            int nbExcep = model.getWrapperOperation(row).getExceptionsSize();
            boolean stateBut = (row >= editableRow) ||
                    ((row < editableRow) && selection && (nbExcep > 0));
            excepButton.setEnabled(stateBut);
             
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(excepField, BorderLayout.CENTER);
            panel.add(excepButton, BorderLayout.EAST);
            return new OperationExceptionPanelRenderer(panel, excepField);
        } else if (column == 5) {
            JTextField txt = new JTextField();
            
            return new TextFieldRenderer(txt, true, selection);
        } else
            return super.getCellRenderer(row, column);
    }
    
   public boolean isCellEditable(int row, int col) {
        
        int editableRow = ((MBeanWrapperOperationTableModel)getModel()).getFirstEditableRow();
        boolean isChecked = (Boolean)getModel().getValueAt(row,0);
        
        if (row < editableRow) {
            if (isChecked)
                return ((col == 0) || (col == 3) || (col == 4) || (col == 5));
            else
                return (col ==0);
        }
        else
            return (col != 0);
    }
}
