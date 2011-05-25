/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class NewServerPanel extends javax.swing.JPanel {

    private static ResourceBundle bundle = NbBundle.getBundle(NewServerPanel.class);
    private static Logger logger = Logger.getLogger(NewServerPanel.class.getCanonicalName());
    private Properties cloneProperties = new Properties();

    public String getServerName() {
        return tfServerName.getText();
    }
    public String getClassPath() {
        return tfClasspath.getText();
    }
    public final static String COHERENCE_CLASSPATH_KEY = "coherence.classpath";

    // Listener
    private class ButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println("Event " + e);
            System.out.println("Server name :" + getServerName());
            if (e.getActionCommand().equalsIgnoreCase("OK")) {
                Properties serverProp = new Properties();

                String coherenceHome = NbPreferences.forModule(CoherenceLocationPanel.class).get(CoherenceLocationPanel.COHERENCE_HOME_DIR_PROPERTY, null);
                if (coherenceHome != null && coherenceHome.trim().length() > 0) {
                    String coherenceCP = getClassPath();
                    cloneProperties.setProperty(COHERENCE_CLASSPATH_KEY, coherenceCP);
                }

                Enumeration keys = cloneProperties.propertyNames();
                String key = null;
                String value = null;
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    value = cloneProperties.getProperty(key);
                    serverProp.setProperty(key, value);
                    logger.log(Level.INFO, "*** APH-I1 : ButtonActionListener() Saving " + key + " = " + value);
                }

                serverProp.setProperty(ServerPropertyFileManager.SERVERNAME_KEY, getServerName());

                // Save File
                try {
                    ServerPropertyFileManager.saveProperties(serverProp);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "*** APH-I2 : Failed to Save Server Properties " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
//                    AllServersNotifier.changed();
                }
            }
        }
    }

    public ButtonActionListener getButtonActionListener() {
        return new ButtonActionListener();
    }

    /** Creates new form NewServerPanel */
    public NewServerPanel() {
        initComponents();
        buildClasspath();
    }

    public NewServerPanel(Properties cloneProperties) {
        this();
        this.cloneProperties = cloneProperties;
        tfServerName.setText(cloneProperties.getProperty(ServerPropertyFileManager.SERVERNAME_KEY, ""));
        tfClasspath.setText(cloneProperties.getProperty(COHERENCE_CLASSPATH_KEY));
        parseClasspath();
        buildClasspath();
    }

    private void parseClasspath() {
        String classpath = tfClasspath.getText();
        if (classpath != null && classpath.trim().length()>0) {
            if (classpath.contains(COHERENCE_HIBERNATE_JAR)) cbHibernateJar.setSelected(true);
            if (classpath.contains(COHERENCE_JPA_JAR)) cbJPAJar.setSelected(true);
            if (classpath.contains(COHERENCE_LOADBALANCER_JAR)) cbLoadbalancerJar.setSelected(true);
            if (classpath.contains(COHERENCE_TRANSACTION_JAR)) cbTransactionJar.setSelected(true);
            if (classpath.contains(COHERENCE_TX_JAR)) cbTxJar.setSelected(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tfServerName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfClasspath = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        cbHibernateJar = new javax.swing.JCheckBox();
        cbJPAJar = new javax.swing.JCheckBox();
        cbLoadbalancerJar = new javax.swing.JCheckBox();
        cbTransactionJar = new javax.swing.JCheckBox();
        cbTxJar = new javax.swing.JCheckBox();

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(tfServerName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.lServerName.text")); // NOI18N
        jLabel1.setName("lServerName"); // NOI18N

        tfServerName.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.tfServerName.text")); // NOI18N
        tfServerName.setToolTipText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.tfServerName.toolTipText")); // NOI18N
        tfServerName.setName("tfServerName"); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.jLabel2.text")); // NOI18N

        jLabel3.setLabelFor(tfClasspath);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.jLabel3.text")); // NOI18N

        tfClasspath.setEditable(false);
        tfClasspath.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.tfClasspath.text")); // NOI18N
        tfClasspath.setToolTipText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.tfClasspath.toolTipText")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.jPanel1.border.title"))); // NOI18N

        cbHibernateJar.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.cbHibernateJar.text")); // NOI18N
        cbHibernateJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbHibernateJarActionPerformed(evt);
            }
        });

        cbJPAJar.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.cbJPAJar.text")); // NOI18N
        cbJPAJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbJPAJarActionPerformed(evt);
            }
        });

        cbLoadbalancerJar.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.cbLoadbalancerJar.text")); // NOI18N
        cbLoadbalancerJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLoadbalancerJarActionPerformed(evt);
            }
        });

        cbTransactionJar.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.cbTransactionJar.text")); // NOI18N
        cbTransactionJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTransactionJarActionPerformed(evt);
            }
        });

        cbTxJar.setText(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.cbTxJar.text")); // NOI18N
        cbTxJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTxJarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbHibernateJar)
                    .addComponent(cbJPAJar)
                    .addComponent(cbLoadbalancerJar)
                    .addComponent(cbTransactionJar)
                    .addComponent(cbTxJar))
                .addContainerGap(339, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbHibernateJar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbJPAJar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbLoadbalancerJar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbTransactionJar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbTxJar))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                    .addComponent(tfClasspath, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                    .addComponent(tfServerName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfServerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(9, 9, 9)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfClasspath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewServerPanel.class, "NewServerPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbHibernateJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbHibernateJarActionPerformed
        buildClasspath();
    }//GEN-LAST:event_cbHibernateJarActionPerformed

    private void cbJPAJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbJPAJarActionPerformed
        buildClasspath();
    }//GEN-LAST:event_cbJPAJarActionPerformed

    private void cbLoadbalancerJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLoadbalancerJarActionPerformed
        buildClasspath();
    }//GEN-LAST:event_cbLoadbalancerJarActionPerformed

    private void cbTransactionJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTransactionJarActionPerformed
        buildClasspath();
    }//GEN-LAST:event_cbTransactionJarActionPerformed

    private void cbTxJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTxJarActionPerformed
        buildClasspath();
    }//GEN-LAST:event_cbTxJarActionPerformed

    public final static String COHERENCE_JAR = "/lib/coherence.jar";
    public final static String COHERENCE_JPA_JAR = "/lib/coherence-jpa.jar";
    public final static String COHERENCE_HIBERNATE_JAR = "/lib/coherence-hibernate.jar";
    public final static String COHERENCE_LOADBALANCER_JAR = "/lib/coherence-loadbalancer.jar";
    public final static String COHERENCE_TRANSACTION_JAR = "/lib/coherence-transaction.jar";
    public final static String COHERENCE_TX_JAR = "/lib/coherence-tx.jar";
    private void buildClasspath() {
        String coherenceHome = NbPreferences.forModule(CoherenceLocationPanel.class).get(CoherenceLocationPanel.COHERENCE_HOME_DIR_PROPERTY, null);
        if (coherenceHome != null && coherenceHome.trim().length() > 0) {
            StringBuilder classpathSB = new StringBuilder(coherenceHome.concat(COHERENCE_JAR).concat(File.pathSeparator));
            if (cbHibernateJar.isSelected()) classpathSB.append(coherenceHome.concat(COHERENCE_HIBERNATE_JAR).concat(File.pathSeparator));
            if (cbJPAJar.isSelected()) classpathSB.append(coherenceHome.concat(COHERENCE_JPA_JAR).concat(File.pathSeparator));
            if (cbLoadbalancerJar.isSelected()) classpathSB.append(coherenceHome.concat(COHERENCE_LOADBALANCER_JAR).concat(File.pathSeparator));
            if (cbTransactionJar.isSelected()) classpathSB.append(coherenceHome.concat(COHERENCE_TRANSACTION_JAR).concat(File.pathSeparator));
            if (cbTxJar.isSelected()) classpathSB.append(coherenceHome.concat(COHERENCE_TX_JAR).concat(File.pathSeparator));
            tfClasspath.setText(classpathSB.toString());
        } else {
            cbHibernateJar.setEnabled(false);
            cbJPAJar.setEnabled(false);
            cbLoadbalancerJar.setEnabled(false);
            cbTransactionJar.setEnabled(false);
            cbTxJar.setEnabled(false);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbHibernateJar;
    private javax.swing.JCheckBox cbJPAJar;
    private javax.swing.JCheckBox cbLoadbalancerJar;
    private javax.swing.JCheckBox cbTransactionJar;
    private javax.swing.JCheckBox cbTxJar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField tfClasspath;
    private javax.swing.JTextField tfServerName;
    // End of variables declaration//GEN-END:variables
}
