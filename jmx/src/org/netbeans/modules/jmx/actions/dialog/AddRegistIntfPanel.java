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
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask MBean to instantiate and register.
 * @author  tl156378
 */
public class AddRegistIntfPanel extends javax.swing.JPanel {
    
    /** class to add registration of MBean */
    private JavaSource currentClass;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
     
    /** 
     * Creates new form Panel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public AddRegistIntfPanel(Node node) throws IOException {
        bundle = NbBundle.getBundle(AddRegistIntfPanel.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        
        currentClass = JavaModelHelper.getSource(fo);
        String className = JavaModelHelper.getSimpleName(currentClass);
        // init tags
        
        initComponents();
        
        //init labels
        Mnemonics.setLocalizedText(keepRefCheckBox,
                bundle.getString("LBL_Keep_References")); // NOI18N
        
        //for accessibility
        keepRefCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REGISTRATION_KEEP")); // NOI18N
        keepRefCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REGISTRATION_KEEP_DESCRIPTION")); // NOI18N
        
        infoTextArea.setText(className + " " + // NOI18N
                bundle.getString("LBL_MBeanRegistration_Informations")); // NOI18N
        
        //for functionnals tests
        keepRefCheckBox.setName("keepRefCheckBox"); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    private boolean isAcceptable() {
        return true;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if specified operations are correct.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_Action_AddMBeanRegistrationIntf.Title"); // NOI18N
        
        btnOK = new JButton(bundle.getString("LBL_OK")); //NOI18N
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
                        new HelpCtx("jmx_mbean_update_registration"), // NOI18N
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    /**
     * Returns the MBean class to add operations.
     * @return <CODE>JavaSource</CODE> the MBean class
     */
    public JavaSource getMBeanClass() {
        return currentClass;
    }
    
    /**
     * Returns the MBean have to keep references of preRegister method parameters.
     * @return <CODE>boolean</CODE> true if it is selected.
     */
    public boolean getKeepRefSelected() {
        return keepRefCheckBox.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        northPanel = new javax.swing.JPanel();
        infoTextArea = new javax.swing.JTextArea();
        keepRefCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.GridBagLayout());

        infoTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        infoTextArea.setEditable(false);
        infoTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        infoTextArea.setBorder(null);
        infoTextArea.setFocusable(false);
        infoTextArea.setSelectionColor(javax.swing.UIManager.getDefaults().getColor("textText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 12);
        northPanel.add(infoTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        northPanel.add(keepRefCheckBox, gridBagConstraints);

        add(northPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JCheckBox keepRefCheckBox;
    private javax.swing.JPanel northPanel;
    // End of variables declaration//GEN-END:variables
    
}
