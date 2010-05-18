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

import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.listener.OperationTextFieldKeyListener;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationExceptionPopup;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationParameterPopup;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationParameterPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationExceptionPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationParameterPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationExceptionPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanAttributeTableModel;

/**
 * Class responsible for the operation table in the Method and Attribute Panel
 *
 */
public class OperationTable extends JTable {
    
    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    private JTextField nameField;
    private JTextField descriptionField;
    private JComboBox typeBox;
    
    protected JPanel ancestorPanel = null;
    private FireEvent wiz = null;
    
    private JDialog currentPopup = null;
    
    
    /**
     * Constructor
     * @param ancestorPanel the parent dialog's panel
     * @param model the table model for this table
     * @param wiz a wizard panel
     */
    public OperationTable(JPanel ancestorPanel, AbstractTableModel model,
            FireEvent wiz) {
        super(model);
        this.ancestorPanel = ancestorPanel;
        this.wiz = wiz;
        
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
    public TableCellEditor getCellEditor(int row, int column) {
        
        final MBeanOperationTableModel model = 
                (MBeanOperationTableModel)this.getModel();
        
        if(row >= getRowCount())
            return null;
        
        if (column == 0) { // operation name
            final JTextField nameField = new JTextField();
            String o = (String)getModel().getValueAt(row,column);
            nameField.setText(o);
            nameField.addKeyListener(new OperationTextFieldKeyListener());
            /* OLD
            nameField.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {}
                public void keyReleased(KeyEvent e) {}
                public void keyTyped(KeyEvent e) {
                    String txt = nameField.getText();
                    int selectionStart = nameField.getSelectionStart();
                    int selectionEnd = nameField.getSelectionEnd();
                    char typedKey = e.getKeyChar();
                    boolean acceptedKey = false;
                    if (selectionStart == 0) {
                        acceptedKey = Character.isJavaIdentifierStart(typedKey);
                    } else {
                        acceptedKey = Character.isJavaIdentifierPart(typedKey);
                    }
                    if (acceptedKey) {
                        if ((typedKey != KeyEvent.VK_BACK_SPACE) &&
                                (typedKey != KeyEvent.VK_DELETE)) {
                            txt = txt.substring(0, selectionStart) +
                                    typedKey +
                                    txt.substring(selectionEnd);
                        } else if (typedKey == KeyEvent.VK_DELETE) {
                            txt = txt.substring(0, selectionStart) +
                                    txt.substring(selectionEnd);
                        } else {
                            txt = txt.substring(0, selectionStart) +
                                    txt.substring(selectionEnd);
                        }
                    } else {
                        getToolkit().beep();
                    }
                    //txt = WizardHelpers.capitalizeFirstLetter(txt);
                    nameField.setText(txt);
                    if ((typedKey == KeyEvent.VK_BACK_SPACE) ||
                            (typedKey == KeyEvent.VK_DELETE))
                        nameField.setCaretPosition(selectionStart);
                    else {
                        if (acceptedKey) {
                            nameField.setCaretPosition(selectionStart + 1);
                        } else {
                            nameField.setCaretPosition(selectionStart);
                        }
                    }
                    
                    e.consume();
                }
            });
             */
            return new JTextFieldCellEditor(nameField, this);
        } else {
            if (column == 1) { //operation return type
                JComboBox typeBox = WizardHelpers.instanciateRetTypeJComboBox();
                typeBox.setName("methTypeBox");// NOI18N
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                return new JComboBoxCellEditor(typeBox, this);
            } else {
                if (column == 2) { //parameter panel
                    final JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    paramField.setName("methParamTextField");// NOI18N
                    JButton paramButton = 
                          new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    paramButton.setName("methAddParamButton");// NOI18N
                    paramButton.setMargin(new java.awt.Insets(2,2,2,2));
                    final int editedRow = row;
                    paramButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            OperationParameterPopup methParamPopup =
                                    new OperationParameterPopup(
                                    ancestorPanel, model, paramField, 
                                    editedRow, wiz);
                        }
                    });
                    
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
                } else {
                    if (column == 3) { //operation exceptions
                        final JTextField excepField = new JTextField();
                        excepField.setEditable(false);
                        excepField.setName("methExcepTextField");// NOI18N
                        JButton excepButton = new JButton(
                                WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                        excepButton.setName("methAddExcepJButton");// NOI18N
                        excepButton.setMargin(new java.awt.Insets(2,2,2,2));
                        final int editedRow = row;
                        excepButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                OperationExceptionPopup opExceptionPopup = 
                                        new OperationExceptionPopup(
                                        ancestorPanel, model, excepField, 
                                        editedRow);
                            }
                        });
                        
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
                    } else {
                        if (column == 4) {//operation description
                            JTextField descrField = new JTextField();
                            String o = 
                                    (String)getModel().getValueAt(row,column);
                            descrField.setText(o);
                            return new JTextFieldCellEditor(descrField, this);
                        }
                    }
                }
            }
            return super.getCellEditor(row,column);
        }
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
        
        if (column == 1) {
            JComboBox retTypeBox = WizardHelpers.instanciateRetTypeJComboBox(); 
            return new ComboBoxRenderer(retTypeBox,true,true);
        } else {
            if (column == 2) {
                JTextField paramField = new JTextField();
                paramField.setEditable(false);
                paramField.setName("methParamTextField");// NOI18N
                JButton paramButton = 
                        new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                paramButton.setMargin(new java.awt.Insets(2,2,2,2));
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(paramField, BorderLayout.CENTER);
                panel.add(paramButton, BorderLayout.EAST);
                return new OperationParameterPanelRenderer(panel, paramField);
            } else {
                if (column == 3) {
                    JTextField excepField = new JTextField();
                    excepField.setEditable(false);
                    excepField.setName("methExcepTextField");// NOI18N
                    JButton excepButton = 
                          new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    excepButton.setMargin(new java.awt.Insets(2,2,2,2));
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(excepField, BorderLayout.CENTER);
                    panel.add(excepButton, BorderLayout.EAST);
                    return 
                        new OperationExceptionPanelRenderer(panel, excepField);
                }
            }
        }
        return super.getCellRenderer(row,column);
    }
    
    /**
     * Returns the wizard panel of the parent dialog
     * @return FireEvent
     */
    public FireEvent getWiz() {
        
        return this.wiz;
    }
}


