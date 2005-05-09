/*
 * OperationExceptionPopupTable.java
 *
 * Created on April 4, 2005, 5:15 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.table;

import org.netbeans.modules.jmx.WizardHelpers;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author an156382
 */
public class OperationExceptionPopupTable extends JTable {
    
    /** Creates a new instance of AttributeTable */
    public OperationExceptionPopupTable(AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(250, 70));
        this.setRowSelectionAllowed(true); 
        this.setColumnSelectionAllowed(false);
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        // here, one text field for the two columns of the popup is enough
        final JTextField genericField = new JTextField();
        String o = ((String)getModel().getValueAt(row,column));
        genericField.setText(o);
        genericField.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {}
                public void keyReleased(KeyEvent e) {}
                public void keyTyped(KeyEvent e) {
                    String txt = genericField.getText();
                    int selectionStart = genericField.getSelectionStart();
                    int selectionEnd = genericField.getSelectionEnd();
                    char typedKey = e.getKeyChar();
                    boolean acceptedKey = false;
                    if (selectionStart == 0) {
                        acceptedKey = Character.isJavaIdentifierStart(typedKey);
                    } else {
                        acceptedKey = Character.isJavaIdentifierPart(typedKey);
                    }
                    if ((!acceptedKey) && (typedKey == '.')) {
                        acceptedKey = true;
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
                    genericField.setText(txt);
                    if ((typedKey == KeyEvent.VK_BACK_SPACE) || 
                            (typedKey == KeyEvent.VK_DELETE))
                        genericField.setCaretPosition(selectionStart);
                    else {
                        if (acceptedKey) {
                            genericField.setCaretPosition(selectionStart + 1);
                        } else {
                            genericField.setCaretPosition(selectionStart);
                        }                            
                    }
                        
                    e.consume();
                }
            });
        return new JTextFieldCellEditor(genericField, this);
    }

}
