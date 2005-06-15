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

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;
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
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationParameterPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationExceptionPanelEditor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationExceptionPopup;
import org.netbeans.modules.jmx.mbeanwizard.popup.OperationParameterPopup;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationParameterPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationExceptionPanelRenderer;

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
    
    private JPanel ancestorPanel = null;
    private AttributesWizardPanel wiz = null;
    
    private JDialog currentPopup = null;
    
    
    /**
     * Constructor
     * @param ancestorPanel the parent dialog's panel
     * @param model the table model for this table
     * @param wiz a wizard panel
     */
    public OperationTable(JPanel ancestorPanel, AbstractTableModel model,
            AttributesWizardPanel wiz) {
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
        
        final MBeanMethodTableModel model = 
                (MBeanMethodTableModel)this.getModel();
        
        if(row >= getRowCount())
            return null;
        
        if (column == 0) { // operation name
            final JTextField nameField = new JTextField();
            String o = (String)getModel().getValueAt(row,column);
            nameField.setText(o);
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
            return new JTextFieldCellEditor(nameField, this);
        } else {
            if (column == 1) { //operation return type
                JComboBox typeBox = WizardHelpers.instanciateRetTypeJComboBox();
                typeBox.setName("methTypeBox");
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                return new JComboBoxCellEditor(typeBox, this);
            } else {
                if (column == 2) { //parameter panel
                    final JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    paramField.setName("methParamTextField");
                    JButton paramButton = 
                          new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    paramButton.setName("methAddParamButton");
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
                    panel.setName("methParamColJPanel");
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
                        excepField.setName("methExcepTextField");
                        JButton excepButton = new JButton(
                                WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                        excepButton.setName("methAddExcepJButton");
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
            return new ComboBoxRenderer(retTypeBox);
        } else {
            if (column == 2) {
                JTextField paramField = new JTextField();
                paramField.setEditable(false);
                paramField.setName("methParamTextField");
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
                    excepField.setName("methExcepTextField");
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
     * @return AttributesWizardPanel
     */
    public AttributesWizardPanel getWiz() {
        
        return this.wiz;
    }
}


