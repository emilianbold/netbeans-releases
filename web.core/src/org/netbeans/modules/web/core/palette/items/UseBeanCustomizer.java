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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette.items;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 */
public class UseBeanCustomizer extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    PageInfo.BeanData[] beanData = null;

    UseBean useBean;
    JTextComponent target;
            
    /** Creates new form TABLECustomizerPanel */
    public UseBeanCustomizer(UseBean useBean, JTextComponent target) {
        this.useBean = useBean;
        this.target = target;
        
        initComponents();
        jComboBox2.setModel(new DefaultComboBoxModel(UseBean.scopes));
        jComboBox2.setSelectedIndex(useBean.getScopeIndex());
        jTextField2.getDocument().addDocumentListener(new DocumentListener(){

            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
        jTextField1.getDocument().addDocumentListener(new DocumentListener(){

            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
        
    }
    
    public boolean showDialog(PageInfo.BeanData[] beanData) {
        this.beanData = beanData;
        dialogOK = false;
        
        String displayName = "";
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-UseBean"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(UseBeanCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            collectInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        validateInput();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void collectInput() {

        useBean.setBean(jTextField2.getText());
        
        String clazz = jTextField1.getText();
        useBean.setClazz(clazz);
        
        int scopeIndex = jComboBox2.getSelectedIndex();
        useBean.setScopeIndex(scopeIndex);
        
    }
    
    private void validateInput()
    {
        if(jTextField2.getText().equals(""))
        {
            String msg = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle").getString("Error_Empty_ID"); // NOI18N
            descriptor.setValid(false);
            errorMessage.setText(msg);
            return;
        }
        if(JSPPaletteUtilities.idExists(jTextField2.getText(), beanData))
        {   
            String msg = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle").getString("Error_not_uniq_ID"); // NOI18N
            descriptor.setValid(false);
            errorMessage.setText(msg);
            return;
        }
        if(jTextField1.getText().equals(""))
        {
            String msg = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle").getString("Error_Empty_class"); // NOI18N
            descriptor.setValid(false);
            errorMessage.setText(msg);
            return;
        }
        if(!JSPPaletteUtilities.typeExists(target, jTextField1.getText()))
        {
            String msg = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle").getString("Error_No_Such_class"); // NOI18N
            descriptor.setValid(false);
            errorMessage.setText(msg);
            return;
        }
 
        descriptor.setValid(true);
        errorMessage.setText("");
       
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        errorMessage = new javax.swing.JLabel();

        jFileChooser1.setCurrentDirectory(null);

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jComboBox2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_Scope")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_Scope")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_Scope")); // NOI18N

        jTextField1.setColumns(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel1.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_ID")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_ID")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_ID")); // NOI18N

        jLabel2.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_Class")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_Class")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_Class")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jComboBox2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField2, gridBagConstraints);

        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        add(jLabel3, new java.awt.GridBagConstraints());

        errorMessage.setForeground(new java.awt.Color(255, 0, 0));
        errorMessage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        errorMessage.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 12, 12, 12);
        add(errorMessage, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessage;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
    
}
