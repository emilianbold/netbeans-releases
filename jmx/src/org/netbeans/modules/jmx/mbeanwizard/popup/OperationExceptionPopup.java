/*
 * OperationExceptionPopup.java
 *
 * Created on April 4, 2005, 5:29 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationExceptionTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jmx.mbeanwizard.popup.ExceptionResultStructure;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;
import org.netbeans.modules.jmx.runtime.ManagementDialogs;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationExceptionPopupTable;
import javax.swing.JPanel;


/**
 *
 * @author an156382
 */
public class OperationExceptionPopup extends AbstractPopup{
    
    private MBeanMethodTableModel methodModel;
    private ExceptionResultStructure result;
    private int editedRow;
    
    public OperationExceptionPopup(JPanel ancestorPanel, MBeanMethodTableModel methodModel,
            JTextField textField, ExceptionResultStructure result, int editedRow) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        this.textFieldToFill = textField;
        this.result = result;
        this.methodModel = methodModel;
        this.editedRow = editedRow;
        
        setLayout(new BorderLayout());
        initJTable();
        initComponents();
        
        if (this.result.size() != 0)
            readSettings();
        
        setDimensions(NbBundle.getMessage(OperationExceptionPopup.class,
                "LBL_OperationException_Popup"));
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationExceptionTableModel();
        popupTable = new OperationExceptionPopupTable(popupTableModel);
    }
    
    protected void initComponents() {
        
        addJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationException_addException");
        removeJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationException_remException");
        closeJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationException_close");
        
        addJButton.setName("addExceptionJButton");
        removeJButton.setName("remExceptionJButton");
        closeJButton.setName("closeJButton");
        
        //remove button should first be remove
        removeJButton.setEnabled(false);
        
        addJButton.addActionListener(new AddTableRowListener(popupTable,popupTableModel,removeJButton));
        removeJButton.addActionListener(new RemTableRowListener(popupTable,popupTableModel,removeJButton));
        
        closeJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (existsSameException()) {
                     ManagementDialogs.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(OperationExceptionPopup.class, "ERR_InheritanceConflict"), NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    textFieldToFill.setText(storeSettings());
                    dispose();
                }
            }
        });
        
        definePanels(new JButton[] {addJButton,
                removeJButton,
                closeJButton
        },
                popupTable);
    }
    
    protected void readSettings() {
        
        for (int i = 0 ; i < result.size() ; i++) {
            
            popupTableModel.addRow();
            
            String tmp = (String)result.getResultValue(i, OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            popupTableModel.setValueAt(tmp,i,OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            
            tmp = (String)result.getResultValue(i, OperationExceptionTableModel.IDX_EXCEPTION_DESCR);
            popupTableModel.setValueAt(tmp,i,OperationExceptionTableModel.IDX_EXCEPTION_DESCR);
        }
        removeJButton.setEnabled(true);
    }
    
    public String storeSettings() {
        
        int nbParam = popupTableModel.size();
        
        String excepString = "";
        String excepName = "";
        String excepDescription = "";
        String[] stringToAdd;
        result.empty();
        
        for (int i = 0 ; i < nbParam ; i++) {
            excepName = (String)popupTableModel.getValueAt(i,
                    OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            excepDescription = (String)popupTableModel.getValueAt(i,
                    OperationExceptionTableModel.IDX_EXCEPTION_DESCR);
            
            if (excepName != "") {
                excepString += excepName;
                if (i < nbParam -1)
                    excepString += ",";
                
                stringToAdd = new String[2];
                stringToAdd[0] = excepName;
                stringToAdd[1] = excepDescription;
                result.addLine(stringToAdd);
            }
        }
        methodModel.setValueAt(excepString,editedRow,
                MBeanMethodTableModel.IDX_METH_EXCEPTION);
        
        return excepString;
    }
    
    private boolean existsSameException() {
        
        String excepName = "";
        String excepString = "";
        
        for (int i = 0 ; i < popupTableModel.size() ; i++) {
            excepName = (String)popupTableModel.getValueAt(i,
                    OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            
            if (excepString.contains(excepName))
                return true;
            else {
                if (excepName != "")
                    excepString += excepName;
            }
        }
        return false;
    }
}