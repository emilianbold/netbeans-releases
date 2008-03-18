/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.framework;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.hibernate.wizards.Util;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.openide.util.NbBundle;

/**
 * This panel allows setting up a Hibernate session during the 
 * New Project creation. This panel by default setups a default session
 * using Java DB connection. 
 * 
 * @author  Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateConfigurationPanel extends javax.swing.JPanel implements DocumentListener, ItemListener{

    private final String JAVADB_DIALECT_CODE = "Derby"; //NOI18N
    private HibernateWebModuleExtender webModuleExtender;
    private ExtenderController controller;
    private boolean forNewProjectWizard = false;

    /** Creates new form HibernateConfigurationPanel */
    public HibernateConfigurationPanel(HibernateWebModuleExtender webModuleExtender, 
            ExtenderController controller, boolean forNewProjectWizard) {
        this.webModuleExtender = webModuleExtender;
        this.controller = controller;
        this.forNewProjectWizard = forNewProjectWizard;
        initComponents();
        
        hibernateSessionNameTextField.getDocument().addDocumentListener(this);
        dialectComboBox.addItemListener(this);
        driverClassTextField.getDocument().addDocumentListener(this);
        connectionURLComboBox.addItemListener(this);
        usernameTextField.getDocument().addDocumentListener(this);
        passwordTextField.getDocument().addDocumentListener(this);
        
        fillDBDialectCombo();
        setDefaults();
    }

    private void fillDBDialectCombo() {
        dialectComboBox.setModel(new DefaultComboBoxModel(Util.getDialectCodes()));
    }

    private void setDefaults() {
        dialectComboBox.setSelectedItem(JAVADB_DIALECT_CODE);
        driverClassTextField.setText(Util.getSelectedDriver(JAVADB_DIALECT_CODE));
        connectionURLComboBox.setSelectedItem(Util.getSelectedURLConnection(JAVADB_DIALECT_CODE));
        usernameTextField.setText("travel"); //NOI18N
        passwordTextField.setText("travel"); //NOI18N
    }

    private void updateDriverClassAndConnectionURL() {
        String dialectCode = (String) dialectComboBox.getSelectedItem();
        driverClassTextField.setText(Util.getSelectedDriver(dialectCode));
        connectionURLComboBox.setSelectedItem(Util.getSelectedURLConnection(dialectCode));
        if (JAVADB_DIALECT_CODE.equals(dialectCode)) {
            usernameTextField.setText("travel"); //NOI18N
            passwordTextField.setText("travel"); //NOI18N
        } else {
            usernameTextField.setText(""); //NOI18N
            passwordTextField.setText(""); //NOI18N
        }
    }

    public boolean isPanelValid() {
        if(forNewProjectWizard) { // Validate only in case of New Project Wizard.
            if(connectionURLComboBox.getSelectedItem() != null &&
                    connectionURLComboBox.getSelectedItem().toString().trim().equals("")) {
                controller.setErrorMessage(NbBundle.getMessage(HibernateConfigurationPanel.class,"MSG_connectionUrlEmpty"));
                return false;
            }
            if(usernameTextField.getText().trim().equals("")) {
                controller.setErrorMessage(NbBundle.getMessage(HibernateConfigurationPanel.class,"MSG_usernameEmpty"));
                return false;
            }
        }
        return true;
    }

    public String getSessionName() {
        return hibernateSessionNameTextField.getText().trim();
    }
    
    public void setSessionName(String newSessionName) {
        hibernateSessionNameTextField.setText(newSessionName);
    }

    public String getSelectedDialect() {
        if (dialectComboBox.getSelectedItem() != null) {
            return Util.getSelectedDialect(dialectComboBox.getSelectedItem().toString());
        }
        return null;
    }

    public void setDialect(String dialectName) {
        dialectComboBox.setSelectedItem(Util.getDailectCode(dialectName));
    }
    
    public String getSelectedDriver() {
        return driverClassTextField.getText();
    }
    
    public void setDriver(String driver) {
        driverClassTextField.setText(driver);
    }

    public String getSelectedURL() {
        if (connectionURLComboBox.getSelectedItem() != null) {
            return connectionURLComboBox.getSelectedItem().toString();
        }
        return null;

    }
    
    public void setConnectionURL(String url) {
        connectionURLComboBox.setSelectedItem(url);
    }

    public String getUserName() {
        return usernameTextField.getText().trim();
    }
    
    public void setUserName(String username) {
        usernameTextField.setText(username);
    }

    public String getPassword() {
        return passwordTextField.getText().trim();
    }
    
    public void setPassword(String password) {
        passwordTextField.setText(password);
    }
    
    @Override
    public void disable() {
        super.disable();
        for(Component component : this.getComponents()) {
            component.setEnabled(false);
        }
    }
    
//    @Override
//    public void enable() {
//        
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hibernateSessionNameLabel = new javax.swing.JLabel();
        hibernateSessionNameTextField = new javax.swing.JTextField();
        databaseDialectNameLabel = new javax.swing.JLabel();
        driverClassLabel = new javax.swing.JLabel();
        connectionURLLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        dialectComboBox = new javax.swing.JComboBox();
        connectionURLComboBox = new javax.swing.JComboBox();
        passwordTextField = new javax.swing.JTextField();
        usernameTextField = new javax.swing.JTextField();
        driverClassTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        hibernateSessionNameLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.hibernateSessionNameLabel.text")); // NOI18N

        hibernateSessionNameTextField.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.hibernateSessionNameTextField.text")); // NOI18N

        databaseDialectNameLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.databaseDialectNameLabel.text")); // NOI18N

        driverClassLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.driverClassLabel.text")); // NOI18N

        connectionURLLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.connectionURLLabel.text")); // NOI18N

        usernameLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.usernameLabel.text")); // NOI18N

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.passwordLabel.text")); // NOI18N

        dialectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialectComboBoxActionPerformed(evt);
            }
        });

        connectionURLComboBox.setEditable(true);

        passwordTextField.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.passwordTextField.text")); // NOI18N

        usernameTextField.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.usernameTextField.text")); // NOI18N

        driverClassTextField.setEditable(false);
        driverClassTextField.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.driverClassTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 65, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 98, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 41, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hibernateSessionNameLabel)
                    .add(databaseDialectNameLabel)
                    .add(driverClassLabel)
                    .add(connectionURLLabel)
                    .add(usernameLabel)
                    .add(passwordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(passwordTextField)
                                    .add(usernameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, driverClassTextField)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, connectionURLComboBox, 0, 262, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, hibernateSessionNameTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, dialectComboBox, 0, 186, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(hibernateSessionNameLabel)
                            .add(hibernateSessionNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(dialectComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(databaseDialectNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(driverClassLabel)
                            .add(driverClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(connectionURLComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(connectionURLLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(usernameLabel)
                                    .add(usernameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(passwordLabel)
                                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(80, 80, 80)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void dialectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialectComboBoxActionPerformed
        updateDriverClassAndConnectionURL();
}//GEN-LAST:event_dialectComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionURLComboBox;
    private javax.swing.JLabel connectionURLLabel;
    private javax.swing.JLabel databaseDialectNameLabel;
    private javax.swing.JComboBox dialectComboBox;
    private javax.swing.JLabel driverClassLabel;
    private javax.swing.JTextField driverClassTextField;
    private javax.swing.JLabel hibernateSessionNameLabel;
    private javax.swing.JTextField hibernateSessionNameTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField passwordTextField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

    
    public void insertUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void removeUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void itemStateChanged(ItemEvent e) {
        webModuleExtender.fireChangeEvent();
    }
}
