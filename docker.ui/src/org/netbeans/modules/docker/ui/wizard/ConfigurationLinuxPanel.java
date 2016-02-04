/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class ConfigurationLinuxPanel extends javax.swing.JPanel implements Configuration {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form DockerConnectionLinux
     */
    public ConfigurationLinuxPanel() {
        initComponents();

        DefaultDocumentListener listener = new DefaultDocumentListener();
        nameTextField.getDocument().addDocumentListener(listener);
        socketTextField.getDocument().addDocumentListener(listener);
        urlTextField.getDocument().addDocumentListener(listener);
        certTextField.getDocument().addDocumentListener(listener);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public String getDisplayName() {
        return UiUtils.getValue(nameTextField);
    }

    @Override
    public void setDisplayName(String displayName) {
        nameTextField.setText(displayName);
    }

    @Override
    public boolean isSocketSelected() {
        return socketRadioButton.isSelected();
    }

    @Override
    public void setSocketSelected(boolean socketSelected) {
        socketRadioButton.setSelected(socketSelected);
    }

    @Override
    public File getSocket() {
        String value = UiUtils.getValue(socketTextField);
        if (value != null) {
            return new File(value);
        }
        return null;
    }

    @Override
    public void setSocket(File socket) {
        if (socket != null) {
            socketTextField.setText(socket.getAbsolutePath());
        }
    }

    @Override
    public String getUrl() {
        return UiUtils.getValue(urlTextField);
    }

    @Override
    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    @Override
    public String getCertPath() {
        return UiUtils.getValue(certTextField);
    }

    @Override
    public void setCertPath(String path) {
        certTextField.setText(path);
    }

    private class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        switchButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        certDirectoryLabel = new javax.swing.JLabel();
        certTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        socketRadioButton = new javax.swing.JRadioButton();
        socketLabel = new javax.swing.JLabel();
        socketBrowseButton = new javax.swing.JButton();
        socketTextField = new javax.swing.JTextField();
        urlRadioButton = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.nameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.urlLabel.text")); // NOI18N

        urlTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(certDirectoryLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.certDirectoryLabel.text")); // NOI18N

        certTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.browseButton.text")); // NOI18N
        browseButton.setEnabled(false);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        switchButtonGroup.add(socketRadioButton);
        socketRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(socketRadioButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketRadioButton.text")); // NOI18N

        socketLabel.setLabelFor(socketTextField);
        org.openide.awt.Mnemonics.setLocalizedText(socketLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(socketBrowseButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketBrowseButton.text")); // NOI18N
        socketBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                socketBrowseButtonActionPerformed(evt);
            }
        });

        switchButtonGroup.add(urlRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(urlRadioButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.urlRadioButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameTextField))
            .addGroup(layout.createSequentialGroup()
                .addComponent(socketRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(urlRadioButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(socketLabel)
                    .addComponent(urlLabel)
                    .addComponent(certDirectoryLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(socketTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(socketBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(certTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(urlTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(socketRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(socketLabel)
                    .addComponent(socketTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(socketBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(urlRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certDirectoryLabel)
                    .addComponent(certTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        String text = UiUtils.getValue(certTextField);
        if (text != null) {
            chooser.setSelectedFile(new File(text));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            certTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void socketBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_socketBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileHidingEnabled(false);
        String text = UiUtils.getValue(socketTextField);
        if (text != null) {
            chooser.setSelectedFile(new File(text));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            socketTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_socketBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel certDirectoryLabel;
    private javax.swing.JTextField certTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton socketBrowseButton;
    private javax.swing.JLabel socketLabel;
    private javax.swing.JRadioButton socketRadioButton;
    private javax.swing.JTextField socketTextField;
    private javax.swing.ButtonGroup switchButtonGroup;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JRadioButton urlRadioButton;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
