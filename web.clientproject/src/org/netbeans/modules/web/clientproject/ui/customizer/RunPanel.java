/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.modules.web.clientproject.ClientSideConfigurationProvider;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.webserver.WebServer;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author david
 */
public class RunPanel extends javax.swing.JPanel implements DocumentListener, ItemListener {

    private ClientSideProject project;
    private ComboBoxModel model;
    private ProjectCustomizer.Category category;
    
    /**
     * Creates new form RunPanel
     */
    public RunPanel(ProjectCustomizer.Category category, ClientSideProject p) {
        this.category = category;
        this.project = p;
        initComponents();
        
        final ClientSideConfigurationProvider configProvider = project.getProjectConfigurations();
        jConfigurationComboBox.setRenderer(new ConfigRenderer(jConfigurationComboBox.getRenderer()));
        jConfigurationComboBox.setModel(new DefaultComboBoxModel(configProvider.getConfigurations().toArray()));
        jConfigurationComboBox.setSelectedItem(configProvider.getActiveConfiguration());
        updateConfigurationCustomizer();
        
        jFileToRunTextField.setText(project.getStartFile());
        model = new DefaultComboBoxModel(new String[]{NbBundle.getMessage(RunPanel.class, "EMBEDDED_LIGHTWEIGHT"), 
            NbBundle.getMessage(RunPanel.class, "EXTERNAL")});
        jServerComboBox.setModel(model);
        jServerComboBox.addItemListener(this);
        jServerComboBox.setSelectedIndex(getServer());
        //jServerComboBox.setSelectedIndex(cfg.isUseServer() ? 1 : 0);
        jWebRootTextField.getDocument().addDocumentListener(this);
        jWebRootTextField.setText(project.getWebContextRoot());
        jProjectURLTextField.setText(project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL));
        jProjectURLTextField.getDocument().addDocumentListener(this);
        updateWebRootEnablement();
        jProjectURLDescriptionLabel.setText(
                NbBundle.getMessage(RunPanel.class, "URL_DESCRIPTION", FileUtil.getFileDisplayName(project.getSiteRootFolder())));
        
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditableProperties ep = project.getProjectProperties();
                ep.setProperty(ClientSideProjectConstants.PROJECT_START_FILE, jFileToRunTextField.getText());
                if (isEmbeddedServer()) {
                    ep.setProperty(ClientSideProjectConstants.PROJECT_WEB_ROOT, jWebRootTextField.getText());
                } else {
                    ep.setProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL, jProjectURLTextField.getText());
                }
                ep.setProperty(ClientSideProjectConstants.PROJECT_SERVER, isEmbeddedServer() ? "internal" : "external"); //NOI18N
                project.getProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                try {
                    configProvider.setActiveConfiguration(
                            (ClientProjectConfigurationImplementation)jConfigurationComboBox.getSelectedItem());
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
    }

    private void updateConfigurationCustomizer() {
        jConfigurationPlaceholder.removeAll();
        final ClientProjectConfigurationImplementation selectedConfiguration = 
                (ClientProjectConfigurationImplementation)jConfigurationComboBox.getSelectedItem();
        if (selectedConfiguration != null) {
            ProjectConfigurationCustomizer customizerPanel = selectedConfiguration.getProjectConfigurationCustomizer();
            if (customizerPanel != null) {
                jConfigurationPlaceholder.add(customizerPanel.createPanel(), BorderLayout.CENTER);
            }
        }
        validate();
        repaint();
    }
    
    private int getServer() {
        return project.isUsingEmbeddedServer() ? 0 : 1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jFileToRunTextField = new javax.swing.JTextField();
        jBrowseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jServerComboBox = new javax.swing.JComboBox();
        jWebRootLabel = new javax.swing.JLabel();
        jWebRootTextField = new javax.swing.JTextField();
        jWebRootExampleLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jConfigurationComboBox = new javax.swing.JComboBox();
        jProjectURLLabel = new javax.swing.JLabel();
        jProjectURLTextField = new javax.swing.JTextField();
        jConfigurationPlaceholder = new javax.swing.JPanel();
        jProjectURLDescriptionLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel1.text")); // NOI18N

        jFileToRunTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jFileToRunTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBrowseButton, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jBrowseButton.text")); // NOI18N
        jBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jWebRootLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootLabel.text")); // NOI18N

        jWebRootTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootTextField.text")); // NOI18N

        jWebRootExampleLabel.setFont(jWebRootExampleLabel.getFont().deriveFont(jWebRootExampleLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(jWebRootExampleLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootExampleLabel.text")); // NOI18N
        jWebRootExampleLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jLabel3.text")); // NOI18N

        jConfigurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConfigurationComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jProjectURLLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLLabel.text")); // NOI18N

        jProjectURLTextField.setText(org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLTextField.text")); // NOI18N

        jConfigurationPlaceholder.setLayout(new java.awt.BorderLayout());

        jProjectURLDescriptionLabel.setFont(jProjectURLDescriptionLabel.getFont().deriveFont(jProjectURLDescriptionLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(jProjectURLDescriptionLabel, org.openide.util.NbBundle.getMessage(RunPanel.class, "RunPanel.jProjectURLDescriptionLabel.text")); // NOI18N
        jProjectURLDescriptionLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jWebRootLabel)
                    .addComponent(jProjectURLLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jWebRootExampleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jFileToRunTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBrowseButton))
                    .addComponent(jConfigurationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jServerComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProjectURLTextField)
                    .addComponent(jWebRootTextField)
                    .addComponent(jConfigurationPlaceholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProjectURLDescriptionLabel)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jConfigurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jConfigurationPlaceholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jFileToRunTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBrowseButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jServerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jProjectURLLabel)
                    .addComponent(jProjectURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProjectURLDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jWebRootLabel)
                    .addComponent(jWebRootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jWebRootExampleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(org.openide.util.NbBundle.getMessage(RunPanel.class, "HTML_DOCUMENTS"), "html"));
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        File file = new File(FileUtil.toFile(project.getSiteRootFolder()), jFileToRunTextField.getText());
        if (file.exists()) {
            chooser.setSelectedFile(file);
        } else {
            chooser.setCurrentDirectory(FileUtil.toFile(project.getSiteRootFolder()));
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            FileObject fo = FileUtil.toFileObject(selected);
            if (fo != null) {
                String rel = FileUtil.getRelativePath(project.getSiteRootFolder(), fo);
                if (rel != null) {
                    jFileToRunTextField.setText(rel);
                } else {
                    DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                        org.openide.util.NbBundle.getMessage(RunPanel.class, "WARNING")));
                }
            }
        }
        
    }//GEN-LAST:event_jBrowseButtonActionPerformed

    private void jConfigurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConfigurationComboBoxActionPerformed
        updateConfigurationCustomizer();
    }//GEN-LAST:event_jConfigurationComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBrowseButton;
    private javax.swing.JComboBox jConfigurationComboBox;
    private javax.swing.JPanel jConfigurationPlaceholder;
    private javax.swing.JTextField jFileToRunTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jProjectURLDescriptionLabel;
    private javax.swing.JLabel jProjectURLLabel;
    private javax.swing.JTextField jProjectURLTextField;
    private javax.swing.JComboBox jServerComboBox;
    private javax.swing.JLabel jWebRootExampleLabel;
    private javax.swing.JLabel jWebRootLabel;
    private javax.swing.JTextField jWebRootTextField;
    // End of variables declaration//GEN-END:variables

    private void updateWebRooExample() {
        if (!jWebRootTextField.isVisible()) {
            return;
        }
        if (!jWebRootTextField.isEnabled()) {
            jWebRootExampleLabel.setText(" "); //NOI18N
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append(WebServer.getWebserver().getPort());
        String ctx = jWebRootTextField.getText();
        if (ctx.trim().length() == 0) {
            s.append("/"); //NOI18N
        } else {
            if (!ctx.startsWith("/")) { //NOI18N
                s.append("/"); //NOI18N
            }
            s.append(ctx);
        }
        jWebRootExampleLabel.setText(NbBundle.getMessage(RunPanel.class, "RunPanel.jWebRootExampleLabel.text", s.toString()));
    }
    
    private boolean isEmbeddedServer() {
        return jServerComboBox.getSelectedIndex() == 0;
    }
    
    private void updateWebRootEnablement() {
        jWebRootTextField.setVisible(isEmbeddedServer());
        jWebRootLabel.setVisible(isEmbeddedServer());
        jWebRootExampleLabel.setVisible(isEmbeddedServer());
        jProjectURLLabel.setVisible(!isEmbeddedServer());
        jProjectURLTextField.setVisible(!isEmbeddedServer());
        jProjectURLDescriptionLabel.setVisible(!isEmbeddedServer());
        updateWebRooExample();
        validateProjectURL();
    }
    
    private void validateProjectURL() {
        if (!jProjectURLTextField.isVisible()) {
            category.setValid(true);
            category.setErrorMessage(null);
            return;
        }
        category.setValid(jProjectURLTextField.getText().length() > 0);
        if (!category.isValid()) {
            category.setErrorMessage(org.openide.util.NbBundle.getMessage(RunPanel.class, "ERROR_URL_MISSING"));
        } else {
            category.setErrorMessage(null);
        }
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        updateWebRooExample();
        validateProjectURL();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateWebRooExample();
        validateProjectURL();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        updateWebRootEnablement();
    }
    
    private static class ConfigRenderer implements ListCellRenderer {
        
        private ListCellRenderer original;

        public ConfigRenderer(ListCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProjectConfiguration) {
                value = ((ProjectConfiguration) value).getDisplayName();
            }
            return original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        
    }

}
