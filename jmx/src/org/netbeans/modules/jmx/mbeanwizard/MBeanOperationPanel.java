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
package org.netbeans.modules.jmx.mbeanwizard;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;

import org.openide.WizardDescriptor;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.event.*;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationTable;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * MBean Attribute and Operation Panel: Manages the components responsible for 
 * MBean Attribute and Operation description
 *
 */
public class MBeanOperationPanel extends JPanel implements ListSelectionListener {
    private boolean DEBUG = false;
    
    protected OperationWizardPanel wiz;
    protected ResourceBundle bundle; 
    
    protected OperationTable operationTable;
    protected MBeanOperationTableModel operationModel;
    protected TableColumnModel opColumnModel;
    protected JButton opRemoveJButton;
    
    protected JPanel labelPanel;
    protected JLabel tableLabel;
    
    /**
     * Panel constructor: Fills a wizard descriptor with the user data
     * @param <code>wiz</code> the wizard panel
     */
    public MBeanOperationPanel(OperationWizardPanel wiz) {
        super(new BorderLayout(0,5));
        this.wiz = wiz;
        bundle = NbBundle.getBundle(MBeanOperationPanel.class);
        initComponents();
        //String str = NbBundle.getMessage(MBeanOperationPanel.class,"LBL_Operation_Panel");// NOI18N
        String str = bundle.getString("LBL_Operation_Panel");// NOI18N
        setName(str);
        wiz.setErrorMsg(null);
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    protected void initJTables() {
        
        operationModel = new MBeanOperationTableModel();
        operationTable = new OperationTable(this, getModel(), wiz);
        operationTable.setName("methodTable");// NOI18N
        
        // Accessibility
        //operationTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_OPERATIONS_TABLE"));// NOI18N
        //operationTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_OPERATIONS_TABLE_DESCRIPTION"));// NOI18N
        operationTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_OPERATIONS_TABLE"));// NOI18N
        operationTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_OPERATIONS_TABLE_DESCRIPTION"));// NOI18N
    }
    
    /**
     * Returns the table model of the operation table
     * @return <code>MBeanOperationTableModel</code> the operation table
     */
    public MBeanOperationTableModel getModel() {
        return operationModel;
    }
    
    protected void initComponents() {
        
        initJTables();
        
        opColumnModel = operationTable.getColumnModel();
        
        affectOperationTableComponents(opColumnModel);
    }
    
    protected void affectOperationTableComponents(TableColumnModel columnModel) {
        
        // defines the scrollpane which contains the operation JTable
        JScrollPane methodJTableScrollPane = new JScrollPane(operationTable);
        
        // defines the method add and remove button
        JButton methAddJButton = new JButton();
        Mnemonics.setLocalizedText(methAddJButton,
                //NbBundle.getMessage(MBeanOperationPanel.class,"BUTTON_add_method"));//NOI18N
                bundle.getString("BUTTON_add_method"));// NOI18N
        opRemoveJButton = new JButton();
        Mnemonics.setLocalizedText(opRemoveJButton,
                //NbBundle.getMessage(MBeanOperationPanel.class,"BUTTON_rem_method"));//NOI18N
                bundle.getString("BUTTON_rem_method"));// NOI18N
        methAddJButton.setName("methAddJButton");// NOI18N
        opRemoveJButton.setName("methRemoveJButton");// NOI18N
        
        // remove button should be first disabled
        opRemoveJButton.setEnabled(false);
        
        methAddJButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                getModel().addRow();
                if (getModel().size() != 0)
                    opRemoveJButton.setEnabled(true);
                
                // to verify that there are not two same operation names, 
                // an event is fired to run dynamically the test
                wiz.event();
            }
        });
        
        opRemoveJButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                final int selectedRow = operationTable.getSelectedRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                //removes the row in the table model
                getModel().remRow(selectedRow,operationTable);
                
                //selects the next row in the table
                getModel().selectNextRow(selectedRow, operationTable);
                
                //disables the remove button if the model is empty
                if (getModel().size() == 0)
                    opRemoveJButton.setEnabled(false);
                
                // to verify that there are not two same operation names
                wiz.event();
            }
        });
        
        JPanel methodJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel firstInternalMethodPanel = new JPanel();
        firstInternalMethodPanel.setLayout(new BorderLayout());
        methodJPanel.add(methAddJButton);
        methodJPanel.add(opRemoveJButton);
        
        firstInternalMethodPanel.add(methodJTableScrollPane, 
                BorderLayout.CENTER);
        firstInternalMethodPanel.add(methodJPanel, BorderLayout.SOUTH);
        
        tableLabel = new JLabel();
        Mnemonics.setLocalizedText(tableLabel,
                     bundle.getString("LBL_OpTable"));//NOI18N
        tableLabel.setLabelFor(operationTable);
        
        // in order to be able to get a label on multiple lines for the wrapper
        // attribute panel, this panel defines a panel. Here, only oner label is
        // added to the panel, but there will be another one in the subclass
        labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(tableLabel, BorderLayout.NORTH);
        
        add(labelPanel, BorderLayout.NORTH);
        add(firstInternalMethodPanel, BorderLayout.CENTER);
        
        //Accessibility
        //methAddJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_ADD_OPERATION"));// NOI18N
        //methAddJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_ADD_OPERATION_DESCRIPTION"));// NOI18N
        //opRemoveJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_REMOVE_OPERATION"));// NOI18N
        //opRemoveJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_REMOVE_OPERATION_DESCRIPTION"));// NOI18N
        
        methAddJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ADD_OPERATION"));// NOI18N
        methAddJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ADD_OPERATION_DESCRIPTION"));// NOI18N
        opRemoveJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REMOVE_OPERATION"));// NOI18N
        opRemoveJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REMOVE_OPERATION_DESCRIPTION"));// NOI18N
        
    }
    
    protected boolean OperationAlreadyContained() {
        //for each operation, construction of the concat operation name 
        //+ all parameter types
        ArrayList operations = new ArrayList(getModel().size());
        for (int i=0; i < getModel().size(); i++) {
            //the current operation
            MBeanOperation oper = getModel().getOperation(i);
            String operationName = oper.getName();
            //for this operation, get all his parameter types concat
            String operationParameter = (String)
                                        oper.getFullSimpleSignature();
            String operation = operationName.concat(operationParameter);
            operations.add(operation);
        }
        
        // for each operation constructed, verification if there is another one
        // which has same name and
        // parameter types; the order of the parameter types matters
        for (int i=0; i < operations.size(); i++) {
            int count = 0;
            String currentValue = ((String)operations.get(i));
            for(int j=0; j < operations.size(); j++) {
                String compareValue = ((String)operations.get(j));
                if (compareValue.equals(currentValue))
                    count ++;
                if (count >= 2)
                    return true;
            }
        }
        return false;
    }
    
    
    /**
     * Inner static class which defines the wizard descriptor and fills it with 
     * user information
     */
    public static class OperationWizardPanel extends GenericWizardPanel
            implements FinishablePanel, FireEvent {
        private MBeanOperationPanel panel = null;
        protected WizardDescriptor wiz = null;
        
        public WizardDescriptor getWiz() {
            return wiz;
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return boolean true if the panel can be the last one 
         * and enables the finish button
         */
        public boolean isFinishPanel() {
            return true;
        }
        
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel 
         * is sufficient to go to the next step
         */
        public boolean isValid() {
            
            boolean attrValid = true;
            boolean opValid = true;
            String msg = null;
            
            if (getPanel() != null) {
                if (getPanel().OperationAlreadyContained()) {
                    opValid = false;
                    //msg = NbBundle.getMessage(MBeanOperationPanel.class,"LBL_State_Same_Operation");// NOI18N
                    msg = getPanel().bundle.getString("LBL_State_Same_Operation");// NOI18N
                }
            }
            setErrorMsg(msg);
            
            return opValid;
        }
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (wiz != null) {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        message);    //NOI18N
            }
        }
        
        /**
         * Method which fires an event to notify that there was a change in 
         * the data
         */
        public void event() {
            fireChangeEvent();
        }
        
        /**
         * Method returning the corresponding panel; here 
         * the MBeanAttrAndMethodPanel
         * @return Component the panel
         */
        public Component getComponent() { return getPanel(); }
        
        private MBeanOperationPanel getPanel() {
            if (panel == null) {
                panel = new MBeanOperationPanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         *        attribute table
         */
        public void readSettings(Object settings) {
            wiz = (WizardDescriptor) settings;
            
            getPanel().getModel().clear();
            
            String nbAddedOpStr = (String)wiz.getProperty(WizardConstants.PROP_METHOD_NB);
            
            int nbAddedOp = 0;
            
            if (nbAddedOpStr != null)
                nbAddedOp = new Integer(nbAddedOpStr);
            
            for (int i=0; i < nbAddedOp; i++) {
                //init parameters
                List<MBeanOperationParameter> params = new ArrayList();
                String[] paramsStr = ((String)
                wiz.getProperty(WizardConstants.PROP_METHOD_PARAM+ i)).trim().split(
                        WizardConstants.PARAMETER_SEPARATOR);
                //test if no parameters
                if (!(paramsStr.length == 1 && paramsStr[0].equals(""))) {// NOI18N
                    for (int j=0; j < paramsStr.length; j++) {
                        String[] paramStr = paramsStr[j].trim().split(" ");// NOI18N
                        String desc = (String)
                        wiz.getProperty(WizardConstants.PROP_METHOD_PARAM + i +
                                WizardConstants.DESC + j);
                        params.add(
                                new MBeanOperationParameter(paramStr[1], paramStr[0], desc));
                    }
                }
                
                //init exceptions
                List<MBeanOperationException> exceptions = new ArrayList();
                String[] exceptsStr = ((String)
                wiz.getProperty(WizardConstants.PROP_METHOD_EXCEP+ i)).trim().split(
                        WizardConstants.PARAMETER_SEPARATOR);
                //test if no exceptions
                if (!(exceptsStr.length == 1 && exceptsStr[0].equals(""))) {// NOI18N
                    for (int j=0; j < exceptsStr.length; j++) {
                        String desc = (String)
                        wiz.getProperty(WizardConstants.PROP_METHOD_EXCEP + i +
                                WizardConstants.DESC + j);
                        exceptions.add(
                                new MBeanOperationException(exceptsStr[j], desc));
                    }
                }
                
                getPanel().getModel().addRow(
                        new MBeanOperation(
                        (String)wiz.getProperty(WizardConstants.PROP_METHOD_NAME + i),
                        (String)wiz.getProperty(WizardConstants.PROP_METHOD_TYPE + i),
                        params,
                        exceptions,
                        (String)wiz.getProperty(WizardConstants.PROP_METHOD_DESCR + i)));
                
            }
        
            setErrorMsg(null);
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            getPanel().operationTable.editingStopped(new ChangeEvent(this));
            
            //read the contents of the operation table
            MBeanOperationTableModel methModel = getPanel().getModel();
            
            int nbMeths = methModel.size();
            
            wiz.putProperty(WizardConstants.PROP_METHOD_NB, 
                    new Integer(nbMeths).toString());
            
            for (int i = 0 ; i < nbMeths ; i++) {
                
                // the current operation (number i)
                MBeanOperation op = getPanel().getModel().
                        getOperation(i);
                
                wiz.putProperty(WizardConstants.PROP_METHOD_NAME + i,
                        op.getName());
                
                wiz.putProperty(WizardConstants.PROP_METHOD_TYPE + i,
                        op.getReturnTypeName());
                
                wiz.putProperty(WizardConstants.PROP_METHOD_PARAM + i,
                        op.getSignature());
                
                for (int j = 0; j < op.getParametersList().size(); j++) {
                    MBeanOperationParameter param = op.getParameter(j);
                    wiz.putProperty(WizardConstants.PROP_METHOD_PARAM + i + 
                            WizardConstants.DESC + j,
                            param.getParamDescription());
                }
                
                wiz.putProperty(WizardConstants.PROP_METHOD_EXCEP + i,
                        op.getExceptionClasses());
                
                for (int j = 0; j < op.getExceptionsList().size(); j++) {
                    MBeanOperationException excep = op.getException(j);
                    wiz.putProperty(WizardConstants.PROP_METHOD_EXCEP + i + 
                            WizardConstants.DESC + j,
                            excep.getExceptionDescription());
                }
                wiz.putProperty(WizardConstants.PROP_METHOD_DESCR + i,
                        op.getDescription());
            }
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");// NOI18N
        }
    }
    
    /**
     * Fires that a value changed
     * @param evt A list selection event
     */
    public void valueChanged(ListSelectionEvent evt) {
    }
    
}
