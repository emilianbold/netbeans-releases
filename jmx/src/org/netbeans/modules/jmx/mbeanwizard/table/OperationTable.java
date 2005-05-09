/*
 * MethodTable.java
 *
 * Created on March 24, 2005, 2:33 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.table;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.listener.DisplayPopupListener;
import org.netbeans.modules.jmx.mbeanwizard.popup.ExceptionResultStructure;
import org.netbeans.modules.jmx.mbeanwizard.popup.ParamResultStructure;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationParameterPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.OperationExceptionPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationPanelRenderer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;


/**
 *
 * @author an156382
 */
public class OperationTable extends JTable {
    
     private JTextField nameField;
     private JTextField descriptionField;
     private JComboBox typeBox;
     
     private ArrayList<ParamResultStructure> paramResultArrayList;
     private ArrayList<ExceptionResultStructure> excepResultArrayList;
     
     private JPanel ancestorPanel = null;
     private AttributesWizardPanel wiz = null;
     
     private JDialog currentPopup = null;
     
    
    /** Creates a new instance of AttributeTable */
    public OperationTable(JPanel ancestorPanel, AbstractTableModel model, 
            ArrayList<ParamResultStructure> paramResultArrayList,
            ArrayList<ExceptionResultStructure> excepResultArrayList,
            AttributesWizardPanel wiz) {
        super(model);
        this.paramResultArrayList = paramResultArrayList;
        this.excepResultArrayList = excepResultArrayList;
        this.ancestorPanel = ancestorPanel;
        this.wiz = wiz;
        
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));  
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        
        final MBeanMethodTableModel model = (MBeanMethodTableModel)this.getModel();
        
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
                JComboBox typeBox = instanciateRetTypeJComboBox();
                typeBox.setName("methTypeBox");
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                return new JComboBoxCellEditor(typeBox, this);
            } else {
                if (column == 2) { //parameter panel
                    JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    JButton paramButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    paramButton.setName("methAddParamButton");
                    paramButton.setMargin(new java.awt.Insets(2,2,2,2));
                    paramButton.addActionListener(new DisplayPopupListener(ancestorPanel, this, paramField,
                            paramResultArrayList, wiz)); 
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setName("methParamColJPanel");
                    panel.add(paramField, BorderLayout.CENTER);
                    panel.add(paramButton, BorderLayout.EAST);
                    
                    OperationParameterPanelEditor operationParamEditor = new 
                            OperationParameterPanelEditor(panel, paramField, paramButton);
                    
                    operationParamEditor.addCellEditorListener(new CellEditorListener() {
                        public void editingCanceled(javax.swing.event.ChangeEvent evt) {
                            updateTableModel();
                        }
                        public void editingStopped(javax.swing.event.ChangeEvent evt) {
                            updateTableModel();
                        }
                        private void updateTableModel() {
                            for (int i = 0 ; i < paramResultArrayList.size() ; i++ ) {
                                model.setValueAt(
                                        getMethParamResultStructure(i).getTypesAndNames(),
                                        i, MBeanMethodTableModel.IDX_METH_PARAM);
                            }
                        }
                    });
                   
                    //OperationParameterPanelEditor a editer
                    //return new OperationParameterPanelEditor(panel, paramField, paramButton);
                    return operationParamEditor;
                } else {
                    if (column == 3) { //operation exceptions
                        JTextField excepField = new JTextField();
                        excepField.setEditable(false);
                        JButton excepButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                        excepButton.setMargin(new java.awt.Insets(2,2,2,2));
                        excepButton.addActionListener(new DisplayPopupListener(ancestorPanel, this, model,
                                excepField, excepResultArrayList));

                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(excepField, BorderLayout.CENTER);
                        panel.add(excepButton, BorderLayout.EAST);
                        
                        OperationExceptionPanelEditor methExcepEditor = new
                                OperationExceptionPanelEditor(panel, excepField, excepButton);
                                
                        methExcepEditor.addCellEditorListener(new CellEditorListener() {
                            public void editingCanceled(javax.swing.event.ChangeEvent evt) {
                                updateTableModel();
                            }
                            public void editingStopped(javax.swing.event.ChangeEvent evt) {
                                updateTableModel();
                            }
                            private void updateTableModel() {
                                for (int i = 0 ; i < excepResultArrayList.size() ; i++ ) {
                                    model.setValueAt(
                                            getMethExcepResultStructure(i).getNames(),
                                            i, MBeanMethodTableModel.IDX_METH_EXCEPTION);
                                }
                            }
                        });

                        //OperationExceptionPanelEditor a editer
                        //return new OperationExceptionPanelEditor(panel, excepField, excepButton);
                        return methExcepEditor;
                    } else {
                        if (column == 4) {//operation description
                            JTextField descrField = new JTextField();
                            String o = (String)getModel().getValueAt(row,column);
                            descrField.setText(o);
                            return new JTextFieldCellEditor(descrField, this);
                        }
                    }
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
            if (column == 1) {
                JComboBox retTypeBox = instanciateRetTypeJComboBox();
                return new ComboBoxRenderer(retTypeBox);
            } else {
                if (column == 2) {
                    JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    JButton paramButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    paramButton.setMargin(new java.awt.Insets(2,2,2,2));
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(paramField, BorderLayout.CENTER);
                    panel.add(paramButton, BorderLayout.EAST);
                    return new OperationPanelRenderer(panel, paramField);
                } else {
                    if (column == 3) {
                        JTextField excepField = new JTextField();
                        excepField.setEditable(false);
                        JButton excepButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                        excepButton.setMargin(new java.awt.Insets(2,2,2,2));
                        
                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(excepField, BorderLayout.CENTER);
                        panel.add(excepButton, BorderLayout.EAST);
                        return new OperationPanelRenderer(panel, excepField);
                    }
                }
            }
        return super.getCellRenderer(row,column);
    }
    
    /*******************************************************************/
    /************************ Helper methods ***************************/
    /*******************************************************************/
    public void setCurrentPopup(JDialog d) {
         currentPopup =d;
    }
    
    public JDialog getCurrentPopup() {
        return currentPopup;
    }
    
    public AttributesWizardPanel getWiz() {
    
        return this.wiz;
    } 
    
    private JComboBox instanciateRetTypeJComboBox() {
        
      JComboBox retTypeJCombo = instanciateTypeJComboBox();
      retTypeJCombo.addItem(WizardConstants.VOID_RET_TYPE);
      retTypeJCombo.setSelectedItem(WizardConstants.VOID_RET_TYPE);
      
      return retTypeJCombo;
    }
    
    private JComboBox instanciateTypeJComboBox() {
        
        JComboBox typeCombo = new JComboBox();
        
        // the attribute's type combo box     
        typeCombo.addItem(WizardConstants.BOOLEAN_NAME);
        typeCombo.addItem(WizardConstants.BYTE_NAME);
        typeCombo.addItem(WizardConstants.CHAR_NAME);
        typeCombo.addItem(WizardConstants.DATE_OBJ_NAME);
        typeCombo.addItem(WizardConstants.INT_NAME);
        typeCombo.addItem(WizardConstants.LONG_NAME);
        typeCombo.addItem(WizardConstants.OBJECTNAME_NAME);        
        typeCombo.addItem(WizardConstants.STRING_OBJ_NAME);
        typeCombo.setSelectedItem(WizardConstants.STRING_OBJ_NAME);
        typeCombo.setEditable(true);
        
        return typeCombo;
    }
    
    public ParamResultStructure getMethParamResultStructure(int numOp) {
        
        return paramResultArrayList.get(numOp); 
    }
    
    public ExceptionResultStructure getMethExcepResultStructure(int numOp) {
        
        return excepResultArrayList.get(numOp); 
    }
}
    

