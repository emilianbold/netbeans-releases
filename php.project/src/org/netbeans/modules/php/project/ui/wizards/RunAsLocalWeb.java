/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsLocalWeb extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -53487456454387871L;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;
    private final SourcesFolderProvider sourcesFolderProvider;
    private final CopyFilesVisual copyFilesVisual;

    public RunAsLocalWeb(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider) {
        this(manager, sourcesFolderProvider, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_ConfigLocalWeb"));
    }

    private RunAsLocalWeb(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider, String displayName) {
        super(manager);
        this.displayName = displayName;
        this.sourcesFolderProvider = sourcesFolderProvider;
        initComponents();
        copyFilesVisual = new CopyFilesVisual(sourcesFolderProvider);
        copyFilesPanel.add(BorderLayout.NORTH, copyFilesVisual);

        labels = new JLabel[] {
            urlLabel,
            indexFileLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            indexFileTextField,
        };
        propertyNames = new String[] {
            RunConfigurationPanel.URL,
            RunConfigurationPanel.INDEX_FILE,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
        copyFilesVisual.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeSupport.fireChange();
            }
        });
        runAsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    @Override
    protected boolean isDefault() {
        return true;
    }

    @Override
    protected RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.LOCAL;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox getRunAsCombo() {
        return runAsCombo;
    }

    @Override
    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(getValue(propertyNames[i]));
        }
    }

    @Override
    protected void validateFields() {
        // validation is done in RunConfigurationPanel
        changeSupport.fireChange();
    }

    public void addRunAsLocalWebListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsLocalWebListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsLocalWeb.this.getDefaultValue(getPropName());
        }
    }

    public String getUrl() {
        return urlTextField.getText().trim();
    }

    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    public boolean isCopyFiles() {
        return copyFilesVisual.isCopyFiles();
    }

    public void setCopyFiles(boolean copyFiles) {
        copyFilesVisual.setCopyFiles(copyFiles);
    }

    public LocalServer getLocalServer() {
        return copyFilesVisual.getLocalServer();
    }

    public MutableComboBoxModel getLocalServerModel() {
        return copyFilesVisual.getLocalServerModel();
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
        copyFilesVisual.setLocalServerModel(localServers);
    }

    public void selectLocalServer(LocalServer localServer) {
        copyFilesVisual.selectLocalServer(localServer);
    }

    public String getIndexFile() {
        return indexFileTextField.getText().trim();
    }

    public void setIndexFile(String indexFile) {
        indexFileTextField.setText(indexFile);
    }

    public void hideIndexFile() {
        indexFileLabel.setVisible(false);
        indexFileTextField.setVisible(false);
        indexFileBrowseButton.setVisible(false);
    }

    public void setCopyFilesState(boolean state) {
        copyFilesVisual.setState(state);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel = new JLabel();
        urlTextField = new JTextField();
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        copyFilesPanel = new JPanel();

        setFocusTraversalPolicy(null);

        urlLabel.setLabelFor(urlTextField);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_ProjectUrl")); // NOI18N

        runAsLabel.setLabelFor(runAsCombo);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_RunAs")); // NOI18N

        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_IndexFile"));
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_BrowseIndex"));
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        copyFilesPanel.setLayout(new BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(runAsLabel)
                .addContainerGap())
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(copyFilesPanel, Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(urlLabel)
                            .addComponent(indexFileLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(runAsCombo, Alignment.TRAILING, 0, 220, Short.MAX_VALUE)
                            .addComponent(urlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(indexFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(indexFileBrowseButton)))))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(indexFileLabel)
                    .addComponent(indexFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexFileBrowseButton))
                .addGap(18, 18, 18)
                .addComponent(copyFilesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.copyFilesPanel.AccessibleContext.accessibleName")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.copyFilesPanel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void indexFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseFolderFile(PhpVisibilityQuery.getDefault(), sourcesFolderProvider.getSourcesFolder(), indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel copyFilesPanel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JComboBox runAsCombo;
    private JLabel runAsLabel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
