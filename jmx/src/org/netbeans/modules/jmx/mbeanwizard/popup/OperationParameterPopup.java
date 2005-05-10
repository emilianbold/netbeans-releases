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
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel.AttributesWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationParameterTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.popup.ParamResultStructure;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationParameterPopupTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;



/**
 *
 * @author an156382
 */
public class OperationParameterPopup extends AbstractPopup{
    
    private ParamResultStructure result;
    private AttributesWizardPanel wiz = null;
    
    public OperationParameterPopup(JPanel ancestorPanel, JTextField textField, 
            ParamResultStructure result, AttributesWizardPanel wiz) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        this.textFieldToFill = textField;
        this.result = result;
        this.wiz = wiz;
        
        setLayout(new BorderLayout());
        initComponents();
        
        if (this.result.size() != 0)
            readSettings();
        
        setDimensions(NbBundle.getMessage(OperationParameterPopup.class,
                "LBL_OperationParameter_Popup"));
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationParameterTableModel();
        popupTable = new OperationParameterPopupTable(popupTableModel);
        popupTable.setName("ParamPopupTable");
    }
    
    protected void initComponents() {
        initJTable();
        
        addJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationParameter_addParam");
        removeJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationParameter_remParam");
        closeJButton = instanciatePopupButton(OperationExceptionPopup.class,
                "LBL_OperationParameter_close");
        
        addJButton.setName("addParamJButton");
        removeJButton.setName("remParamJButton");
        closeJButton.setName("closeJButton");
        
        //remove button should first be disabled
        removeJButton.setEnabled(false);
        
        addJButton.addActionListener(new AddTableRowListener(popupTable,popupTableModel,removeJButton));
        removeJButton.addActionListener(new RemTableRowListener(popupTable,popupTableModel,removeJButton));
        
        //TODO factorise the listeners; this is a copy paste of ClosePopupButtonListener a little modified
        //closeJButton.addActionListener(new ClosePopupButtonListener(this,textFieldToFill));
        final OperationParameterPopup opParamPopup = this;
        closeJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                
                textFieldToFill.setText(opParamPopup.storeSettings());
                opParamPopup.dispose();
                
                wiz.event();
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
            
            String tmp = (String)result.getResultValue(i, OperationParameterTableModel.IDX_OP_PARAM_NAME);
            popupTableModel.setValueAt(tmp,i,OperationParameterTableModel.IDX_OP_PARAM_NAME);
            
            tmp = (String)result.getResultValue(i, OperationParameterTableModel.IDX_OP_PARAM_TYPE);
            popupTableModel.setValueAt(tmp,i,OperationParameterTableModel.IDX_OP_PARAM_TYPE);
            
            tmp = (String)result.getResultValue(i, OperationParameterTableModel.IDX_OP_PARAM_DESCRIPTION);
            popupTableModel.setValueAt(tmp,i,OperationParameterTableModel.IDX_OP_PARAM_DESCRIPTION);
        }
        // if readsettings is called, then the table has at least one row
        // i.e the remButton must be enabled
        removeJButton.setEnabled(true);
    }
    
    public String storeSettings() {
        
        int nbParam = popupTableModel.size();
        
        String paramString = "";
        String paramName = "";
        String paramType = "";
        String paramDescription = "";
        String[] stringToAdd;
        result.empty();
        
        
        for (int i = 0 ; i < nbParam ; i++) {
            paramName = (String)popupTableModel.getValueAt(
                    i, OperationParameterTableModel.IDX_OP_PARAM_NAME);
            paramType = (String)popupTableModel.getValueAt(
                    i, OperationParameterTableModel.IDX_OP_PARAM_TYPE);
            paramDescription = (String)popupTableModel.getValueAt(
                    i, OperationParameterTableModel.IDX_OP_PARAM_DESCRIPTION);
            
            if (paramName != "") {
                paramString += paramType + " " + paramName;
                if (i < nbParam -1)
                    paramString += ", ";
                stringToAdd = new String[3];
                stringToAdd[0] = paramName;
                stringToAdd[1] = paramType;
                stringToAdd[2] = paramDescription;
                result.addLine(stringToAdd);
            }
        }
        return paramString;
    }
}
