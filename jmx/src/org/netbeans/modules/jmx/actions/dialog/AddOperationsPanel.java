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

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanOperation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask new operations to user.
 * @author tl156378
 */
public class AddOperationsPanel extends javax.swing.JPanel 
        implements FireEvent, ListSelectionListener {
    
    /** class to add registration of MBean */
    private JavaSource currentClass;
    
    private AddMBeanOperationTableModel operationModel;
    private AddOperationTable operationTable;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    
    /**
     * Returns all the specified operations by user.
     * @return <CODE>MBeanOperation[]</CODE> specified operations by user
     */
    public MBeanOperation[] getOperations() {
        MBeanOperation[] Operations = new MBeanOperation[
                operationModel.getRowCount() - operationModel.getFirstEditable()];
        for (int i = 0; i < Operations.length; i++)
            Operations[i] = operationModel.getOperation(
                    operationModel.getFirstEditable() + i);
        return Operations;
    }
     
    /** 
     * Creates new form AddOperationsPanel.
     * @param  node  node selected when the Add MBean Operations action was invoked.
     */
    public AddOperationsPanel(Node node) {
        bundle = NbBundle.getBundle(AddOperationsPanel.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        
        currentClass = JavaModelHelper.getSource(fo);
        
        // init tags
        initComponents();
        
        operationModel = new AddMBeanOperationTableModel();
        operationTable = new AddOperationTable(this,operationModel,this);
        operationTable.setName("operationTable"); // NOI18N
        //operationTable.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setViewportView(operationTable);
        operationTable.getSelectionModel().addListSelectionListener(this);
        opTableLabel.setLabelFor(operationTable);
        
        //discovery of existing Operations
        MBeanOperation[] existOperations = null;
        try {
            existOperations = JavaModelHelper.getOperations(currentClass);
        }catch(IOException ioex) {
            existOperations = new MBeanOperation[0];
        }

        for (int i = 0; i < existOperations.length; i++)
            operationModel.addOperation(existOperations[i]);
        operationModel.setFirstEditable(existOperations.length);
        
        removeButton.setEnabled(false);
        addButton.addActionListener(
                new AddTableRowListenerWithFireEvent(operationTable, operationModel,
                removeButton, this));
        removeButton.addActionListener(new RemTableRowListenerWithFireEvent(
                operationTable, operationModel, removeButton,this));
        
        // init labels
        Mnemonics.setLocalizedText(opTableLabel,
                bundle.getString("LBL_Operations")); // NOI18N
        Mnemonics.setLocalizedText(addButton,
                bundle.getString("LBL_Button_AddOperation")); // NOI18N
        Mnemonics.setLocalizedText(removeButton,
                bundle.getString("LBL_Button_RemoveOperation")); // NOI18N
        
        // for accessibility
        operationTable.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_OPERATIONS_TABLE")); // NOI18N
        operationTable.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OPERATIONS_TABLE_DESCRIPTION")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_ADD_OPERATION")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_ADD_OPERATION_DESCRIPTION")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REMOVE_OPERATION")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REMOVE_OPERATION_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    public void event() {
        removeButton.setEnabled(
                (operationModel.getRowCount() > operationModel.getFirstEditable()));
        btnOK.setEnabled(isAcceptable());
    }
    
    private boolean isAcceptable() {
        if (!(operationModel.getRowCount() > operationModel.getFirstEditable())) {
            stateLabel.setText(bundle.getString("LBL_NoOperation")); // NOI18N
            return false;
        } else if (operationNameAlreadyContained()) {
            stateLabel.setText(bundle.getString("LBL_State_Same_Operation")); // NOI18N
            return false;
        } else {
            stateLabel.setText(""); // NOI18N
            return true;
        }
    }
    
    private boolean operationNameAlreadyContained() {
        //for each operation, construction of the concat operation name
        //+ all parameter types
        ArrayList operations = new ArrayList(operationModel.size());
        for (int i=0; i < operationModel.size(); i++) {
            //the current operation
            MBeanOperation oper = operationModel.getOperation(i);
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
     * Displays a configuration dialog and updates MBean options
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if specified operations are correct.
     */
    public boolean configure() {
        
        // create and display the dialog:
        MessageFormat formAttribute = 
                new MessageFormat(bundle.getString("LBL_AddOperationsAction.Title")); // NOI18N
        String itfName = ""; // NOI18N
        try {
            itfName = JavaModelHelper.getManagementInterfaceSimpleName(currentClass);
        }catch(IOException ioe) {
            // 
        }
        Object[] args = {itfName};
        String title = formAttribute.format(args);
        
        btnOK = new JButton(bundle.getString("LBL_OK")); // NOI18N
        btnOK.setEnabled(isAcceptable());
        btnOK.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx("jmx_mbean_update_attributes_operations"), // NOI18N
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    /**
     * Returns the MBean class to add operations.
     * @return <CODE>JavaClass</CODE> the MBean class
     */
    public JavaSource getMBeanClass() {
        return currentClass;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int firstEditable = operationModel.getFirstEditable();
        if ((operationTable.getSelectedRow() != -1) &&
                (operationTable.getSelectedRow() < firstEditable))
            removeButton.setEnabled(false);
        else if ((operationTable.getSelectedRow() != -1) &&
                (operationTable.getSelectedRow() >= firstEditable))
            removeButton.setEnabled(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        stateLabel = new javax.swing.JLabel();
        opTableLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        leftPanel.add(removeButton, gridBagConstraints);

        addButton.setName("opAddJButton");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        leftPanel.add(addButton, gridBagConstraints);

        buttonsPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 12);
        add(buttonsPanel, gridBagConstraints);

        stateLabel.setForeground(java.awt.SystemColor.activeCaption);
        stateLabel.setMaximumSize(new java.awt.Dimension(0, 18));
        stateLabel.setMinimumSize(new java.awt.Dimension(0, 18));
        stateLabel.setName("stateLabel");
        stateLabel.setPreferredSize(new java.awt.Dimension(0, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(stateLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(opTableLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel opTableLabel;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel stateLabel;
    // End of variables declaration//GEN-END:variables
    
}
