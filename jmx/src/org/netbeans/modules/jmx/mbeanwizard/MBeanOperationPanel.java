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
package org.netbeans.modules.jmx.mbeanwizard;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.GenericWizardPanel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;

import org.openide.WizardDescriptor;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
public class MBeanOperationPanel extends JPanel implements DocumentListener,
        ListSelectionListener{
    private boolean DEBUG = false;
    
    protected OperationWizardPanel wiz;
    
    protected OperationTable methodTable;
    protected MBeanMethodTableModel methodModel;
    protected TableColumnModel methColumnModel;
    
    /**
     * Panel constructor: Fills a wizard descriptor with the user data
     * @param <code>wiz</code> the wizard panel
     */
    public MBeanOperationPanel(OperationWizardPanel wiz) {
        //super(new GridLayout(1,1));
        super(new BorderLayout());
        this.wiz = wiz;
        initComponents();
        String str = NbBundle.getMessage(MBeanOperationPanel.class,"LBL_Operation_Panel");// NOI18N
        setName(str);
        wiz.setErrorMsg(" ");// NOI18N
        
    }
    
    protected void initJTables() {
        
        methodModel = new MBeanMethodTableModel();
        methodTable = new OperationTable(this, methodModel, wiz);
        methodTable.setName("methodTable");// NOI18N
        
        // Accessibility
        methodTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_OPERATIONS_TABLE"));// NOI18N
        methodTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_OPERATIONS_TABLE_DESCRIPTION"));// NOI18N
        
    }
    
    /**
     * Returns the table model of the operation table
     * @return <code>MBeanMethodTableModel</code> the operation table
     */
    public MBeanMethodTableModel getOperationModel() {
        return methodModel;
    }
    
    protected void initComponents() {
        
        initJTables();
        
        methColumnModel = methodTable.getColumnModel();
        
        affectOperationTableComponents(methColumnModel);
    }
    
    protected void affectOperationTableComponents(TableColumnModel columnModel) {
        
        // defines the scrollpane which contains the operation JTable
        JScrollPane methodJTableScrollPane = new JScrollPane(methodTable);
        
        // defines the method add and remove button
        JButton methAddJButton = new JButton();
        Mnemonics.setLocalizedText(methAddJButton,
                NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"BUTTON_add_method"));//NOI18N
        final JButton methRemoveJButton = new JButton();
        Mnemonics.setLocalizedText(methRemoveJButton,
                NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"BUTTON_rem_method"));//NOI18N
        methAddJButton.setName("methAddJButton");// NOI18N
        methRemoveJButton.setName("methRemoveJButton");// NOI18N
        
        // remove button should be first disabled
        methRemoveJButton.setEnabled(false);
        
        methAddJButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                methodModel.addRow();
                if (methodModel.size() != 0)
                    methRemoveJButton.setEnabled(true);
                
                // to verify that there are not two same operation names, 
                // an event is fired to run dynamically the test
                wiz.event();
            }
        });
        
        methRemoveJButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                final int selectedRow = methodTable.getSelectedRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                //removes the row in the table model
                methodModel.remRow(selectedRow,methodTable);
                
                //selects the next row in the table
                methodModel.selectNextRow(selectedRow, methodTable);
                
                //disables the remove button if the model is empty
                if (methodModel.size() == 0)
                    methRemoveJButton.setEnabled(false);
                
                // to verify that there are not two same operation names
                wiz.event();
            }
        });
        
        JPanel methodJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel firstInternalMethodPanel = new JPanel();
        firstInternalMethodPanel.setLayout(new BorderLayout());
        methodJPanel.add(methAddJButton);
        methodJPanel.add(methRemoveJButton);
        
        firstInternalMethodPanel.add(methodJTableScrollPane, 
                BorderLayout.CENTER);
        firstInternalMethodPanel.add(methodJPanel, BorderLayout.SOUTH);
        
        JLabel tableLabel = new JLabel(NbBundle.getMessage(MBeanOperationPanel.class, "LBL_OpTable"));// NOI18N
        
        add(tableLabel, BorderLayout.NORTH);
        add(firstInternalMethodPanel, BorderLayout.CENTER);
        
        //Accessibility
        methAddJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_ADD_OPERATION"));// NOI18N
        methAddJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_ADD_OPERATION_DESCRIPTION"));// NOI18N
        methRemoveJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_REMOVE_OPERATION"));// NOI18N
        methRemoveJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"ACCESS_REMOVE_OPERATION_DESCRIPTION"));// NOI18N
        
    }
    
    private boolean OperationAlreadyContained() {
        //for each operation, construction of the concat operation name 
        //+ all parameter types
        ArrayList operations = new ArrayList(methodModel.size());
        for (int i=0; i < methodModel.size(); i++) {
            //the current operation
            MBeanOperation oper = methodModel.getOperation(i);
            String operationName = oper.getName();
            //for this operation, get all his parameter types concat
            String operationParameter = (String)
                                        oper.getSimpleSignature();
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
            String msg = WizardConstants.EMPTY_STRING;
            
            if (getPanel() != null) {
                if (getPanel().OperationAlreadyContained()) {
                    opValid = false;
                    msg = NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"LBL_State_Same_Operation");// NOI18N
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
                wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE,
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
            
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            getPanel().methodTable.editingStopped(new ChangeEvent(this));
            
            //read the contents of the operation table
            MBeanMethodTableModel methModel = getPanel().methodModel;
            
            int nbMeths = methModel.size();
            
            wiz.putProperty(WizardConstants.PROP_METHOD_NB, 
                    new Integer(nbMeths).toString());
            
            for (int i = 0 ; i < nbMeths ; i++) {
                
                // the current operation (number i)
                MBeanOperation op = getPanel().getOperationModel().
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
    
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void changedUpdate( DocumentEvent e ) {}
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void insertUpdate( DocumentEvent e )  {}
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void removeUpdate( DocumentEvent e )  {}
}