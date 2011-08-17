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
package org.netbeans.modules.coherence.server.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.CoherenceServer;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for set up base Coherence server properties.
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 * @author Martin Fousek <mafous@netbeans.org>
 */
public class ServerLocationVisual extends javax.swing.JPanel {

    private static final String COHERENCE_LIB_BASE = File.separator + CoherenceServer.PLATFORM_LIB_DIR + File.separator;
    private static final String COHERENCE_JAR = COHERENCE_LIB_BASE + "coherence.jar"; //NOI18N
    private static final String COHERENCE_JPA_JAR = COHERENCE_LIB_BASE + "coherence-jpa.jar"; //NOI18N
    private static final String COHERENCE_HIBERNATE_JAR = COHERENCE_LIB_BASE + "coherence-hibernate.jar"; //NOI18N
    private static final String COHERENCE_LOADBALANCER_JAR = COHERENCE_LIB_BASE + "coherence-loadbalancer.jar"; //NOI18N
    private static final String COHERENCE_TRANSACTION_JAR = COHERENCE_LIB_BASE + "coherence-transaction.jar"; //NOI18N
    private static final String COHERENCE_TX_JAR = COHERENCE_LIB_BASE + "coherence-tx.jar"; //NOI18N

    private String classpath = "";

    private static JFileChooser fileChooser;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    public String getServerName() {
        return serverLocationTextField.getText();
    }

    private String validCoherenceServerDirectory(File directory) {
        boolean libDir, docDir, binDir;
        libDir = docDir = binDir = false;
        for (File file : directory.listFiles()) {
            if (file.getName().equals(CoherenceServer.PLATFORM_BIN_DIR)) {
                binDir = true;
            } else if (file.getName().equals(CoherenceServer.PLATFORM_LIB_DIR)) {
                libDir = true;
            } else if (file.getName().equals(CoherenceServer.PLATFORM_DOC_DIR)) {
                docDir = true;
            }
        }

        // one of mandatory directories wasn't found
        if (!libDir || !binDir || !docDir) {
            return NbBundle.getMessage(ServerLocationVisual.class, "LBL_NotValidCoherencePlatformDir"); //NOI18N
        }

        // inside library directory was not found Coherence.jar
        if (!new File(directory, COHERENCE_JAR).exists()) {
            return NbBundle.getMessage(ServerLocationVisual.class, "LBL_CoherenceJarNotFoundInPlatform"); //NOI18N
        }
        return null;
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        if (serverLocationTextField.getText().isEmpty()) {
            return false;
        } else {
            File file = new File(serverLocationTextField.getText());
            if (!file.exists()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotExists")); //NOI18N
                return false;
            } else if (!file.isDirectory()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotDir")); //NOI18N
                return false;
            } else if (!file.canRead()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotReadable")); //NOI18N
                return false;
            } else if (validCoherenceServerDirectory(file) != null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        validCoherenceServerDirectory(file));
                return false;
            }
        }

        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    /**
     * Creates new form ServerLocationVisual
     */
    public ServerLocationVisual() {
        initComponents();
        initListeners();
    }

    private void initListeners() {
        // document listener for serverLocationTextField
        serverLocationTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
        });

        // action listeners for checkboxes
        hibernateJarCheckBox.addActionListener(new CheckBoxActionListener());
        jpaJarCheckBox.addActionListener(new CheckBoxActionListener());
        transactionJarCheckBox.addActionListener(new CheckBoxActionListener());
        txJarCheckBox.addActionListener(new CheckBoxActionListener());
        loadbalancerJarCheckBox.addActionListener(new CheckBoxActionListener());
    }

    private class CheckBoxActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Adds a listener
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a registered listener
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Shows the filechooser set to currently selected directory or to the
     * default system root if the directory is invalid
     */
    private void showFileChooser() {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.exists() && f.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(ServerLocationVisual.class, "DESC_ServerLocationFileFilter"); //NOI18N
            }
        });
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // set the current directory
        File currentLocation = new File(serverLocationTextField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }

        // wait for the user to choose the directory and if he clicked the OK
        // button store the selected directory in the server location field
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            serverLocationTextField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }

    private void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public String getClasspath() {
        return classpath;
    }

    public String getServerLocation() {
        return serverLocationTextField.getText();
    }

    public void fillInCoherenceClasspath(boolean isCoherenceValid) {
        if (isCoherenceValid) {
            String location = serverLocationTextField.getText();
            if (location != null && location.trim().length() > 0) {
                StringBuilder classpathSB = new StringBuilder(location.concat(COHERENCE_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                if (hibernateJarCheckBox.isSelected()) {
                    classpathSB.append(location.concat(COHERENCE_HIBERNATE_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                }
                if (jpaJarCheckBox.isSelected()) {
                    classpathSB.append(location.concat(COHERENCE_JPA_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                }
                if (loadbalancerJarCheckBox.isSelected()) {
                    classpathSB.append(location.concat(COHERENCE_LOADBALANCER_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                }
                if (transactionJarCheckBox.isSelected()) {
                    classpathSB.append(location.concat(COHERENCE_TRANSACTION_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                }
                if (txJarCheckBox.isSelected()) {
                    classpathSB.append(location.concat(COHERENCE_TX_JAR).concat(CoherenceProperties.CLASSPATH_SEPARATOR));
                }
                classpath = classpathSB.toString();
                classpath = classpath.substring(0, classpath.length() - CoherenceProperties.CLASSPATH_SEPARATOR.length());
            }
        } else {
            classpath = "";
        }
    }

    public void setEnabledCheckboxes(boolean enabled) {
        hibernateJarCheckBox.setEnabled(enabled);
        jpaJarCheckBox.setEnabled(enabled);
        loadbalancerJarCheckBox.setEnabled(enabled);
        transactionJarCheckBox.setEnabled(enabled);
        txJarCheckBox.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLocationLabel = new javax.swing.JLabel();
        serverLocationTextField = new javax.swing.JTextField();
        serverPropertiesNoticeLabel = new javax.swing.JLabel();
        additionalClasspathPanel = new javax.swing.JPanel();
        hibernateJarCheckBox = new javax.swing.JCheckBox();
        jpaJarCheckBox = new javax.swing.JCheckBox();
        loadbalancerJarCheckBox = new javax.swing.JCheckBox();
        transactionJarCheckBox = new javax.swing.JCheckBox();
        txJarCheckBox = new javax.swing.JCheckBox();
        browseButton = new javax.swing.JButton();

        serverLocationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.serverLocationLabel.mnemonics").charAt(0));
        serverLocationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serverLocationLabel.setLabelFor(serverLocationTextField);
        serverLocationLabel.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverLocationLabel.text")); // NOI18N
        serverLocationLabel.setName("lServerName"); // NOI18N

        serverLocationTextField.setToolTipText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverLocationTextField.toolTipText")); // NOI18N
        serverLocationTextField.setName("serverLocationTextField"); // NOI18N

        serverPropertiesNoticeLabel.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverPropertiesNoticeLabel.text")); // NOI18N

        additionalClasspathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.additionalClasspathPanel.border.title"))); // NOI18N
        additionalClasspathPanel.setName(""); // NOI18N

        hibernateJarCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.hibernateJarCheckBox.mnemonics").charAt(0));
        hibernateJarCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.hibernateJarCheckBox.text")); // NOI18N

        jpaJarCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.jpaJarCheckBox.mnemonics").charAt(0));
        jpaJarCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.jpaJarCheckBox.text")); // NOI18N

        loadbalancerJarCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.loadbalancerJarCheckBox.mnemonics").charAt(0));
        loadbalancerJarCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.loadbalancerJarCheckBox.text")); // NOI18N

        transactionJarCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.transactionJarCheckBox.mnemonics").charAt(0));
        transactionJarCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.transactionJarCheckBox.text")); // NOI18N

        txJarCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.txJarCheckBox.mnemonics").charAt(0));
        txJarCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.txJarCheckBox.text")); // NOI18N

        javax.swing.GroupLayout additionalClasspathPanelLayout = new javax.swing.GroupLayout(additionalClasspathPanel);
        additionalClasspathPanel.setLayout(additionalClasspathPanelLayout);
        additionalClasspathPanelLayout.setHorizontalGroup(
            additionalClasspathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(additionalClasspathPanelLayout.createSequentialGroup()
                .addGroup(additionalClasspathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hibernateJarCheckBox)
                    .addComponent(jpaJarCheckBox)
                    .addComponent(loadbalancerJarCheckBox)
                    .addComponent(transactionJarCheckBox)
                    .addComponent(txJarCheckBox))
                .addContainerGap(226, Short.MAX_VALUE))
        );
        additionalClasspathPanelLayout.setVerticalGroup(
            additionalClasspathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(additionalClasspathPanelLayout.createSequentialGroup()
                .addComponent(hibernateJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpaJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadbalancerJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transactionJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txJarCheckBox))
        );

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.browseButtonLabel.mnemonics").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(serverPropertiesNoticeLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(additionalClasspathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLocationLabel)
                    .addComponent(browseButton)
                    .addComponent(serverLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(additionalClasspathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(serverPropertiesNoticeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        additionalClasspathPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.additionalClasspathPanel.border.title")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        showFileChooser();
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel additionalClasspathPanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox hibernateJarCheckBox;
    private javax.swing.JCheckBox jpaJarCheckBox;
    private javax.swing.JCheckBox loadbalancerJarCheckBox;
    private javax.swing.JLabel serverLocationLabel;
    private javax.swing.JTextField serverLocationTextField;
    private javax.swing.JLabel serverPropertiesNoticeLabel;
    private javax.swing.JCheckBox transactionJarCheckBox;
    private javax.swing.JCheckBox txJarCheckBox;
    // End of variables declaration//GEN-END:variables
}
