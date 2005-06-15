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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationExceptionTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;
import org.netbeans.modules.jmx.runtime.ManagementDialogs;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationExceptionPopupTable;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.mbeanwizard.mbeanstructure.MBeanOperationException;


/**
 * Class implementing the exception popup window
 * 
 */
public class OperationExceptionPopup extends AbstractPopup{
    
    private MBeanMethodTableModel methodModel;
    private int editedRow;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup; here the wizard
     * @param methodModel the table model from the operation table in the wizard
     * @param textField the textfield to fill with the popup information
     * @param editedRow the current edited row in the wizard table
     */
    public OperationExceptionPopup(JPanel ancestorPanel, 
            MBeanMethodTableModel methodModel,
            JTextField textField, int editedRow) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        this.textFieldToFill = textField;
        this.methodModel = methodModel;
        this.editedRow = editedRow;
        
        setLayout(new BorderLayout());
        initJTable();
        initComponents();
        
        readSettings();
        
        setDimensions(NbBundle.getMessage(OperationExceptionPopup.class,
                "LBL_OperationException_Popup"));
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationExceptionTableModel();
        popupTable = new OperationExceptionPopupTable(popupTableModel);
        popupTable.setName("ExcepPopupTable");
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
        
        addJButton.addActionListener(new AddTableRowListener(
                popupTable,popupTableModel,removeJButton));
        removeJButton.addActionListener(new RemTableRowListener(
                popupTable,popupTableModel,removeJButton));
        
        closeJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (existsSameException()) {
                     ManagementDialogs.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(
                             OperationExceptionPopup.class, 
                             "ERR_InheritanceConflict"), 
                             NotifyDescriptor.ERROR_MESSAGE));
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
        
        if(methodModel.size() != 0) {
            //get the exception list of the current operation
            ArrayList<MBeanOperationException> panelExcepts =
                    (ArrayList<MBeanOperationException>)
            methodModel.getOperation(editedRow).getOperationExceptionList();
            
            for (int i = 0; i < panelExcepts.size(); i++) {
                 
                popupTableModel.addRow();
                //copy the exceptions from the panel model to the popup model
                ((OperationExceptionTableModel)
                                popupTableModel).setException(i, panelExcepts.get(i)); 
            }
            removeJButton.setEnabled(popupTableModel.getRowCount() > 0);
        }
    }
    
    /**
     * Inherited method from superclass
     */ 
    public String storeSettings() {
        
        //stores all values from the table in the model even with keyboard navigation
        popupTable.editingStopped(new ChangeEvent(this));
        
        String excepString = "";
        String excepName = "";
        String excepDescription = "";
        // create a new list of exceptions to fill
        ArrayList<MBeanOperationException> mboe = 
                new ArrayList<MBeanOperationException>();
        
        for (int i = 0 ; i < popupTableModel.size(); i++) {
            //get the current exception in the popup model
            MBeanOperationException popupEx = ((OperationExceptionTableModel)
                popupTableModel).getException(i);
            excepName = popupEx.getExceptionClass();
            excepDescription = popupEx.getExceptionDescription();
            
            if (excepName != "")
                excepString += excepName;
            
            if (i < popupTableModel.size() -1)
                excepString += ",";

            // fills the arraylist with the exceptions to store
            mboe.add(popupEx);
        }
        
        //copy back the exceptions from the popup to the panel model
        methodModel.getOperation(editedRow).setOperationExceptionList(mboe);
        
        return excepString;
    }
    
    private boolean existsSameException() {
        // prevents from having two or more exception with the same name
        String excepName = "";
        String excepString = "";
        
        for (int i = 0 ; i < popupTableModel.size() ; i++) {
            //excepName = (String)popupTableModel.getValueAt(i,
            //        OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            excepName = ((OperationExceptionTableModel)
                popupTableModel).getException(i).getExceptionClass();
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