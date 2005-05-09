/*
 * OperationParameterPopupTableopupTable.java
 *
 * Created on March 30, 2005, 11:01 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.table;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author an156382
 */
public class OperationParameterPopupTable extends JTable {
    
    /** Creates a new instance of AttributeTable */
    public OperationParameterPopupTable(AbstractTableModel model) {
        super(model);
        
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(250, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        if (column == 0) { //parameter name
            final JTextField nameField = new JTextField();
            String o = ((String)getModel().getValueAt(row,column));
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
            if (column == 1) { //parameter type
                JComboBox typeBox = instanciateTypeJComboBox();
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                return new JComboBoxCellEditor(typeBox, this);
            } else {
                if (column == 2) { //parameter description
                    JTextField descrField = new JTextField();
                    String o = ((String)getModel().getValueAt(row,column));
                    descrField.setText(o);
                    return new JTextFieldCellEditor(descrField, this);
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
                return null;
        
            if (column == 1) {
                JComboBox typeBox = instanciateTypeJComboBox();
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                return new ComboBoxRenderer(typeBox);
            } 
        
        return super.getCellRenderer(row,column);
    }
    
    
    /**************************************************/
    /**************** Helper methods ******************/
    /**************************************************/
    
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
}
