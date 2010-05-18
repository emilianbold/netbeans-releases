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
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationExceptionTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import org.netbeans.modules.jmx.common.runtime.ManagementDialogs;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationExceptionPopupTable;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.MBeanOperationException;


/**
 * Class implementing the exception popup window
 * 
 */
public class OperationExceptionPopup extends AbstractPopup{
    
    protected MBeanOperationTableModel methodModel;
    private int editedRow;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup; here the wizard
     * @param methodModel the table model from the operation table in the wizard
     * @param textField the textfield to fill with the popup information
     * @param editedRow the current edited row in the wizard table
     */
    public OperationExceptionPopup(JPanel ancestorPanel, 
            MBeanOperationTableModel methodModel,
            JTextField textField, int editedRow) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        this.textFieldToFill = textField;
        this.methodModel = methodModel;
        this.editedRow = editedRow;
        
        setLayout(new BorderLayout());
        initJTable();
        initComponents();
        
        readSettings();
        
        //setDimensions(NbBundle.getMessage(OperationExceptionPopup.class,
          //      "LBL_OperationException_Popup"));// NOI18N
        setDimensions(bundle.getString("LBL_OperationException_Popup"));// NOI18N
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationExceptionTableModel();
        popupTable = new OperationExceptionPopupTable(popupTableModel);
        popupTable.setName("ExcepPopupTable");// NOI18N
    }
    
    protected void initComponents() {
        /*
        addJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationException_addException"));// NOI18N
        removeJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationException_remException"));// NOI18N
        closeJButton = instanciatePopupButton(NbBundle.getMessage(OperationExceptionPopup.class,"LBL_OperationException_close"));// NOI18N
        
        
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_REMOVE_EXCEPTION"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_REMOVE_EXCEPTION_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_ADD_EXCEPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_ADD_EXCEPTION_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_CLOSE_EXCEPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_CLOSE_EXCEPTION_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_EXCEPTION_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OperationExceptionPopup.class,"ACCESS_EXCEPTION_TABLE_DESCRIPTION"));// NOI18N
        */
        
        addJButton = instanciatePopupButton(bundle.getString("LBL_OperationException_addException"));// NOI18N
        removeJButton = instanciatePopupButton(bundle.getString("LBL_OperationException_remException"));// NOI18N
        closeJButton = instanciatePopupButton(bundle.getString("LBL_OperationException_close"));// NOI18N
        
        
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REMOVE_EXCEPTION"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REMOVE_EXCEPTION_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ADD_EXCEPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ADD_EXCEPTION_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_CLOSE_EXCEPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_CLOSE_EXCEPTION_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_EXCEPTION_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_EXCEPTION_TABLE_DESCRIPTION"));// NOI18N
        
        
        addJButton.setName("addExceptionJButton");// NOI18N
        removeJButton.setName("remExceptionJButton");// NOI18N
        closeJButton.setName("closeJButton");// NOI18N
        
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
                        //new NotifyDescriptor.Message(NbBundle.getMessage(OperationExceptionPopup.class, "ERR_InheritanceConflict"), // NOI18N
                          //   NotifyDescriptor.ERROR_MESSAGE));
                          new NotifyDescriptor.Message(bundle.getString("ERR_InheritanceConflict"), // NOI18N
                             NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    textFieldToFill.setText(storeSettings());
                    dispose();
                }
            }
        });
        
        definePanels(getUsedButtons(),
                popupTable, 
                bundle.getString("LBL_OP_EXCEPTIONS_POPUP_TABLE")); // NOI18N
    }
    
    protected JButton[] getUsedButtons() {
        return new JButton[] {addJButton,
                removeJButton,
                closeJButton
        };
    }
    
    protected void readSettings() {
        
        if(methodModel.size() != 0) {
            //get the exception list of the current operation
            List<MBeanOperationException> panelExcepts =
                    methodModel.getOperation(editedRow).getExceptionsList();
            
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
        
        String excepString = "";// NOI18N
        String excepName = "";// NOI18N
        String excepDescription = "";// NOI18N
        // create a new list of exceptions to fill
        ArrayList<MBeanOperationException> mboe = 
                new ArrayList<MBeanOperationException>();
        
        for (int i = 0 ; i < popupTableModel.size(); i++) {
            //get the current exception in the popup model
            MBeanOperationException popupEx = ((OperationExceptionTableModel)
                popupTableModel).getException(i);
            excepName = popupEx.getExceptionClass();
            excepDescription = popupEx.getExceptionDescription();
            
            if (excepName != "")// NOI18N
                excepString += excepName;
            
            if (i < popupTableModel.size() -1)
                excepString += ",";// NOI18N

            // fills the arraylist with the exceptions to store
            mboe.add(popupEx);
        }
        
        //copy back the exceptions from the popup to the panel model
        methodModel.getOperation(editedRow).setExceptionsList(mboe);
        
        return excepString;
    }
    
    private boolean existsSameException() {
        // prevents from having two or more exception with the same name
        String excepName = "";// NOI18N
        String excepString = "";// NOI18N
        
        for (int i = 0 ; i < popupTableModel.size() ; i++) {
            //excepName = (String)popupTableModel.getValueAt(i,
            //        OperationExceptionTableModel.IDX_EXCEPTION_NAME);
            excepName = ((OperationExceptionTableModel)
                popupTableModel).getException(i).getExceptionClass();
            if (excepString.contains(excepName))
                return true;
            else {
                if (excepName != "")// NOI18N
                    excepString += excepName;
            }
        }
        return false;
    }
}
