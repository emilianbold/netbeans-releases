/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperOperationTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperOperationTableModel;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.WizardConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;


/**
 *
 * @author an156382
 */
public class MBeanWrapperOperationPanel extends MBeanOperationPanel 
    implements ListSelectionListener{
    
    private int orderNumber = Integer.MAX_VALUE;
    
    /**
     * Returns the table model of the operation table
     * @return <code>MBeanOperationTableModel</code> the operation table
     */
    public MBeanWrapperOperationTableModel getModel() {
        return (MBeanWrapperOperationTableModel) operationModel;
    }
    
    /** Creates a new instance of WrapperPanel */
    public MBeanWrapperOperationPanel(WrapperOperationsWizardPanel wiz) {
        super(wiz);
        initWrapperComponents();
        //String str = NbBundle.getMessage(MBeanWrapperOperationPanel.class,"LBL_Operation_Panel");// NOI18N
        String str = bundle.getString("LBL_Operation_Panel");// NOI18N
        setName(str);
    }
    
    protected void initJTables() {
        
        operationModel = new MBeanWrapperOperationTableModel();
        operationTable = new WrapperOperationTable(this,getModel(), wiz);
        operationTable.setName("wrapperOperationTable");// NOI18N
        operationTable.getSelectionModel().addListSelectionListener(this);
        
        // Accessibility
        //operationTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_OPERATIONS_TABLE"));// NOI18N
        //operationTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanOperationPanel.class,"ACCESS_WRAPPED_OPERATIONS_TABLE_DESCRIPTION"));// NOI18N
    
        operationTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_OPERATIONS_TABLE"));// NOI18N
        operationTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_WRAPPED_OPERATIONS_TABLE_DESCRIPTION"));// NOI18N
    
    }
    
    /**
     * Overriding super class method to force emty treatment
     * The reason is a Layout manager change that is incompatible
     * This method is therefore empty thus it is called by the constructor
     * of the super class
     * The real treatment of component initialization is in 
     * initWrapperComponents() which is called later
     */
    protected void initComponents() {
    }
    
    protected void initWrapperComponents() {
        
        initJTables();
        
        opColumnModel = operationTable.getColumnModel();
        affectOperationTableComponents(opColumnModel);
    }
    
    protected void affectOperationTableComponents(TableColumnModel columnModel) {
        super.affectOperationTableComponents(columnModel);
        Mnemonics.setLocalizedText(tableLabel,
                     bundle.getString("LBL_OpTable_FromExistingClass"));//NOI18N 
        tableLabel.setLabelFor(operationTable);
        
        // second label definition to get a label text on multiple lines; a new
        // label is added to the labelPanel
        JLabel complementaryLabel = new JLabel(bundle.getString("LBL_OpTable_FromExistingClass_suite"));//NOI18N
        labelPanel.add(complementaryLabel, java.awt.BorderLayout.SOUTH);
        
        opRemoveJButton.setName("wrapperOpRemoveJButton");
        
        /* New ActionListener for the remove button that overrides the one from
         * the super class: Now, to be able to remove a line, it must not be 
         * an introspected Operation
         */ 
        opRemoveJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int selectedRow = operationTable.getSelectedRow();
                int firstEditableRow = getModel().getFirstEditableRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                if (selectedRow >= firstEditableRow) { // remove allowed
                    try {
                        //attrRemoveJButton.setEnabled(true);
                        getModel().remRow(selectedRow, operationTable);
                        getModel().selectNextRow(selectedRow, operationTable);  
                    } catch (Exception ex) {
                        System.out.println("Exception here : ");// NOI18N
                        ex.printStackTrace();
                    }
                } else {
                    opRemoveJButton.setEnabled(false);
                }
                
                // if the model has no rows, disable the remove button
                if (getModel().size() == getModel().getFirstEditableRow())
                    opRemoveJButton.setEnabled(false);
                
                wiz.event();
            }
        });
    }
    
      protected boolean OperationAlreadyContained() {
        //for each operation, construction of the concat operation name 
        //+ all parameter types
        ArrayList operations = new ArrayList(getModel().size());
        for (int i=0; i < getModel().size(); i++) {
            //the current operation
            MBeanWrapperOperation oper = 
                    ((MBeanWrapperOperationTableModel) getModel()).getWrapperOperation(i);
            if (oper.isSelected()) {
                String operationName = oper.getName();
                //for this operation, get all his parameter types concat
                String operationParameter = (String)
                oper.getFullSimpleSignature();
                String operation = operationName.concat(operationParameter);
                operations.add(operation);
            }
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
    public static class WrapperOperationsWizardPanel extends OperationWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel {
        private MBeanWrapperOperationPanel panel = null;
        
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
            
            boolean opValid = true;
            String msg = WizardConstants.EMPTY_STRING;
           
            if (getPanel() != null) {
                if (getPanel().OperationAlreadyContained()) {
                    opValid = false;
                    //msg = NbBundle.getMessage(MBeanWrapperOperationPanel.class,"LBL_State_Same_Operation");// NOI18N
                    msg = getPanel().bundle.getString("LBL_State_Same_Operation");// NOI18N
                }
                setErrorMsg(msg);
            }
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
                        message);
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
        
        private MBeanWrapperOperationPanel getPanel() {
            if (panel == null) {
                panel = new MBeanWrapperOperationPanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         *        Operation table
         */
        public void readSettings(Object settings) {
            
            wiz = (WizardDescriptor) settings;
            
            // if the user loads the panel for the first time, perform introspection
            // else do nothing ...
            int oNumber = (Integer)wiz.getProperty(WizardConstants.PROP_USER_ORDER_NUMBER);
            if (oNumber != getPanel().orderNumber) { // the user loads the panel for the first time
                getPanel().getModel().clear();
                MBeanDO mbdo = null;
                try {
                    mbdo = JavaModelHelper.getMBeanLikeModel(
                            (JavaSource)wiz.getProperty(WizardConstants.PROP_MBEAN_EXISTING_CLASS));
                    
                    List<MBeanOperation> operations = mbdo.getOperations();
                    for (Iterator<MBeanOperation> it = operations.iterator(); it.hasNext();) {
                        ((MBeanWrapperOperationTableModel) getPanel().getModel()).addRow(it.next());
                    }
                    ((MBeanWrapperOperationTableModel) getPanel().getModel()).setFirstEditableRow(operations.size());
                    
                    event();
                } catch (Exception e) {e.printStackTrace();}
                
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
                            wiz.getProperty(WizardConstants.PROP_METHOD_PARAM + i +
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
                getPanel().orderNumber = oNumber;
            } 
            
            wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, "");// NOI18N
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
            
            //read the contents of the Operation table
            MBeanWrapperOperationTableModel opModel = 
                    (MBeanWrapperOperationTableModel)getPanel().getModel();
            
            int nbOp = opModel.size();
            int firstEditableRow = opModel.getFirstEditableRow();
            
            // counter for the Operation storage
            int j = 0;
            
            // two loops; one for the wrapped atributes and the other for the
            // Operations added by the user
            for (int i = firstEditableRow ; i < nbOp; i++) {
                
                // the current Operation (number i)
                MBeanWrapperOperation op = opModel.getWrapperOperation(i);
                
                wiz.putProperty(WizardConstants.PROP_METHOD_NAME + j,
                        op.getName());
                
                wiz.putProperty(WizardConstants.PROP_METHOD_TYPE + j,
                        op.getReturnTypeName());
                
                wiz.putProperty(WizardConstants.PROP_METHOD_PARAM + j,
                        op.getSignature());
                
                for (int k = 0; k < op.getParametersList().size(); k++) {
                    MBeanOperationParameter param = op.getParameter(k);
                    wiz.putProperty(WizardConstants.PROP_METHOD_PARAM + j + 
                            WizardConstants.DESC + k,
                            param.getParamDescription());
                }
                
                wiz.putProperty(WizardConstants.PROP_METHOD_EXCEP + j,
                        op.getExceptionClasses());
                
                for (int k = 0; k < op.getExceptionsList().size(); k++) {
                    MBeanOperationException excep = op.getException(k);
                    wiz.putProperty(WizardConstants.PROP_METHOD_EXCEP + j + 
                            WizardConstants.DESC + k,
                            excep.getExceptionDescription());
                }
                wiz.putProperty(WizardConstants.PROP_METHOD_DESCR + j,
                        op.getDescription());
                
                j++;
            }
            
            for (int i = 0 ; i < firstEditableRow; i++) {
                
                // the current Operation (number i)
                MBeanWrapperOperation op = opModel.getWrapperOperation(i);
                
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_NAME + i,
                        op.getName());
                
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_TYPE + i,
                        op.getReturnTypeName());
                
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_PARAM + i,
                        op.getSignature());
                
                for (int k = 0; k < op.getParametersList().size(); k++) {
                    MBeanOperationParameter param = op.getParameter(k);
                    wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_PARAM + i + 
                            WizardConstants.DESC + k,
                            param.getParamDescription());
                }
                
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_EXCEP + i,
                        op.getExceptionClasses());
                
                for (int k = 0; k < op.getExceptionsList().size(); k++) {
                    MBeanOperationException excep = op.getException(k);
                    wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_EXCEP + i + 
                            WizardConstants.DESC + k,
                            excep.getExceptionDescription());
                }
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_DESCR + i,
                        op.getDescription());
                
                wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_SELECT + i,
                        op.isSelected());
            }
            
            // sets the number of introspected Operations and the number of
            // added Operations
            wiz.putProperty(WizardConstants.PROP_INTRO_METHOD_NB, 
                    new Integer(firstEditableRow).toString());
            wiz.putProperty(WizardConstants.PROP_METHOD_NB, 
                    new Integer(nbOp - firstEditableRow).toString());
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_from_existing_app");// NOI18N
        }
    }
    
    public void valueChanged(ListSelectionEvent evt) {
        boolean enable = (operationTable.getSelectedRow() < 
                ((MBeanWrapperOperationTableModel)getModel()).getFirstEditableRow());
        opRemoveJButton.setEnabled(!enable);
    }    
}
