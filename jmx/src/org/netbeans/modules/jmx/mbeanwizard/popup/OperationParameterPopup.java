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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationParameterTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationParameterPopupTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.MBeanOperationParameter;



/**
 * Class implementing the parameter popup window
 *
 */
public class OperationParameterPopup extends AbstractPopup{
    
    
    private FireEvent wiz = null;
    private MBeanOperationTableModel methodModel = null;
    private int editedRow = 0;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup; here the wizard
     * @param textField the text field to fill with the popup information
     * @param result the intermediate structure which storespopup information
     * @param wiz the parent-window's wizard panel
     */
    public OperationParameterPopup(JPanel ancestorPanel, 
            MBeanOperationTableModel model,
            JTextField textField, int editedRow, FireEvent wiz) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        this.methodModel = model;
        this.textFieldToFill = textField;
        this.editedRow = editedRow;
        this.wiz = wiz;
        
        setLayout(new BorderLayout());
        initComponents();
        
        readSettings();
        
        //setDimensions(NbBundle.getMessage(OperationParameterPopup.class,"LBL_OperationParameter_Popup"));// NOI18N
        setDimensions(bundle.getString("LBL_OperationParameter_Popup"));// NOI18N
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationParameterTableModel();
        popupTable = new OperationParameterPopupTable(popupTableModel);
        popupTable.setName("ParamPopupTable");// NOI18N
    }
    
    protected void initComponents() {
        initJTable();
        /*
        addJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationParameter_addParam"));// NOI18N
        removeJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationParameter_remParam"));// NOI18N
        closeJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationParameter_close"));// NOI18N
     
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_REMOVE_PARAMETER"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_REMOVE_PARAMETER_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_ADD_PARAMETER"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_ADD_PARAMETER_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_CLOSE_PARAMETER"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_CLOSE_PARAMETER_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_PARAMETER_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationParameterPopup.class,"ACCESS_PARAMETER_TABLE_DESCRIPTION"));// NOI18N
        */
        
        addJButton = instanciatePopupButton(bundle.getString("LBL_OperationParameter_addParam"));// NOI18N
        removeJButton = instanciatePopupButton(bundle.getString("LBL_OperationParameter_remParam"));// NOI18N
        closeJButton = instanciatePopupButton(bundle.getString("LBL_OperationParameter_close"));// NOI18N
     
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REMOVE_PARAMETER"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REMOVE_PARAMETER_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ADD_PARAMETER"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ADD_PARAMETER_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_CLOSE_PARAMETER"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_CLOSE_PARAMETER_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_PARAMETER_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PARAMETER_TABLE_DESCRIPTION"));// NOI18N
        
        
        addJButton.setName("addParamJButton");// NOI18N
        removeJButton.setName("remParamJButton");// NOI18N
        closeJButton.setName("closeJButton");// NOI18N
        //cancelJButton.setName("cancelJButton");// NOI18N 
                
        //remove button should first be disabled
        removeJButton.setEnabled(false);
        
        addJButton.addActionListener(new AddTableRowListener(popupTable,
                popupTableModel,removeJButton));
        removeJButton.addActionListener(new RemTableRowListener(popupTable,
                popupTableModel,removeJButton));
        
        //TODO factorise the listeners; this is a copy paste of 
        //ClosePopupButtonListener a little modified
        //closeJButton.addActionListener(
        //new ClosePopupButtonListener(this,textFieldToFill));
        final OperationParameterPopup opParamPopup = this;
        closeJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                
                textFieldToFill.setText(opParamPopup.storeSettings());
                opParamPopup.dispose();
                
                wiz.event();
            }
        });
        
        definePanels(getUsedButtons(),
                popupTable, 
                bundle.getString("LBL_OP_PARAMETERS_POPUP_TABLE")); // NOI18N
    }
    
    protected JButton[] getUsedButtons() {
        return new JButton[] {addJButton,
                removeJButton,
                closeJButton
        };
    }
    
    protected void readSettings() {
        if(methodModel.size() != 0) {
            //gets the current parameter list from the current operation
            List<MBeanOperationParameter> opParam = 
                    methodModel.getOperation(editedRow).getParametersList();
            
            for (int i = 0; i < opParam.size(); i++) {
                popupTableModel.addRow();
                //copy the current parameter from the panel model to the popup
                //model
                ((OperationParameterTableModel)
                                popupTableModel).setParameter(i, opParam.get(i));
            }
            removeJButton.setEnabled(popupTableModel.getRowCount() > 0);
        }
    }
    
    /**
     * Method which stores the information from the popup window in the 
     * intermediate structure
     * and returns a String containing all couples parameter types and names 
     * seperated by a ','
     * @return String contains the couples parameter types and names of the 
     * popup window
     */
    public String storeSettings() {
        
        //stores all values from the table in the model even with 
        //keyboard navigation
        popupTable.editingStopped(new ChangeEvent(this));
        
        String paramString = "";// NOI18N
        String paramName = "";// NOI18N
        String paramType = "";// NOI18N
        String paramDescr = "";// NOI18N
        List<MBeanOperationParameter> mbop = 
                new ArrayList<MBeanOperationParameter>();
        
        for (int i = 0 ; i < popupTableModel.size(); i++) {
            //get the current parameter in the popup
            MBeanOperationParameter popupParam = ((OperationParameterTableModel)
                popupTableModel).getParameter(i);
            paramName = popupParam.getParamName();
            paramType = popupParam.getParamType();
            paramDescr = popupParam.getParamDescription();
            
            
            if ((paramName != "") && (paramType != ""))// NOI18N
                paramString += paramType + " " + paramName;// NOI18N
            
            if (i < popupTableModel.size() -1)
                paramString += ",";// NOI18N
            
            // fills the arraylist with the exceptions to store
            mbop.add(popupParam);
        }
        
        //copy back the parameters from the popup to the panel model
        methodModel.getOperation(editedRow).setParametersList(mbop);
        
        return paramString;
    }
}
