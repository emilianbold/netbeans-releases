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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 */
final class DatasourceComboBoxCustomizer extends javax.swing.JPanel {
    
    private final Color nbErrorForeground;
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    
    private final HashMap<String, Datasource> datasources;
    
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public DatasourceComboBoxCustomizer(Set<Datasource> datasources) {
        this.datasources = new HashMap<String, Datasource>();
        if (datasources != null) { // transform Set to Map for faster searching
            for (Iterator it = datasources.iterator(); it.hasNext();) {
                Datasource datasource = (Datasource) it.next();
                if (datasource.getJndiName() != null)
                    this.datasources.put(datasource.getJndiName(), datasource);
            }
        }
        initComponents();
        
        DatabaseExplorerUIs.connect(connCombo, ConnectionManager.getDefault());

        connCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
        
        jndiNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verify();
            }
        });
        
        Color errorColor = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (errorColor == null)
            errorColor = new Color(255, 0, 0);
        nbErrorForeground = errorColor;
        
        errorLabel.setForeground(nbErrorForeground);
    }
    
    public boolean showDialog() {
        
        descriptor = new DialogDescriptor
            (this, NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DatasourceCustomizer"), true,
             DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
             DialogDescriptor.DEFAULT_ALIGN,
             new HelpCtx("DatasourceUIHelper_DatasourceCustomizer"), // NOI18N
             new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean close = true;
                    if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                        boolean valid = handleConfirmation();
                        close = valid;
                        dialogOK = valid;
                    }
                    
                    if (close) {
                        dialog.dispose();
                    }
                 }
             });
        
        descriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        
        verify();

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private boolean handleConfirmation() {
        
        jndiName = jndiNameField.getText().trim();
        
        DatabaseConnection conn = (DatabaseConnection)connCombo.getSelectedItem();
        
        if (conn.getPassword() == null) {
            ConnectionManager.getDefault().showConnectionDialog(conn);
        }
        if (conn.getPassword() == null) {
            //user did not provide the password
            errorLabel.setText(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NoPassword"));
            return false;
        }
        url = conn.getDatabaseURL();
        username = conn.getUser();
        password = conn.getPassword();
        driverClassName = conn.getDriverClass();

        return true;
    }
    
    private boolean verify() {
        
        boolean isValid = verifyJndiName();
        if (isValid)
            isValid = verifyConnection();
        
        return isValid;
    }
    
    private boolean verifyJndiName() {
        
        boolean valid = true;
        
        String jndiName = jndiNameField.getText().trim();
        if (jndiName.length() == 0) {
            errorLabel.setText(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_JNDI_NAME_EMPTY"));
            valid = false;
        }
        else
        if (datasourceAlreadyExists(jndiName)) {
            errorLabel.setText(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_DS_EXISTS"));
            valid = false;
        }
        else {
            errorLabel.setText(""); // NOI18N
        }

        descriptor.setValid(valid);
        
        return valid;
    }
    
    private boolean verifyConnection() {
        
        boolean valid = true;
        
        if (!(connCombo.getSelectedItem() instanceof DatabaseConnection)) {
            errorLabel.setText(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NO_CONN_SELECTED"));
            valid = false;
        }
        else {
            errorLabel.setText(""); // NOI18N
        }

        descriptor.setValid(valid);
        
        return valid;
    }
    
    private boolean datasourceAlreadyExists(String jndiName) {
        return datasources.containsKey(jndiName);
    }
    
    String getJndiName() {
        return jndiName;
    }

    String getUrl() {
        return url;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getDriverClassName() {
        return driverClassName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jndiNameField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        connCombo = new javax.swing.JComboBox();
        warningLabel = new javax.swing.JLabel();

        setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setLabelFor(jndiNameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_JndiName"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_DbConn"));

        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_Warning"));
        warningLabel.setEnabled(false);
        warningLabel.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jndiNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(connCombo, 0, 359, Short.MAX_VALUE)))
                    .add(errorLabel)
                    .add(warningLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jndiNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 33, Short.MAX_VALUE)
                        .add(warningLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(errorLabel))
                    .add(layout.createSequentialGroup()
                        .add(connCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connCombo;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jndiNameField;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
    
}
