/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.remote.options;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
//@OptionsPanelController.Keywords(keywords={"svn", "subversion", "#SvnOptionsPanel.kw1", "#SvnOptionsPanel.kw2", "#SvnOptionsPanel.kw3",
//    "#SvnOptionsPanel.kw4", "#SvnOptionsPanel.kw5"}, location="Team", tabTitle="#CTL_OptionsPanel.title")
@Messages({
    "CTL_OptionsPanel.title=Versioning",
    "SvnOptionsPanel.kw1=versioning",
    "SvnOptionsPanel.kw2=preferred client",
    "SvnOptionsPanel.kw3=connection settings",
    "SvnOptionsPanel.kw4=status labels",
    "SvnOptionsPanel.kw5=locking settings"
})
public class SvnOptionsPanel extends javax.swing.JPanel {
    private String[] keywords;

    /** Creates new form SvnOptionsPanel */
    public SvnOptionsPanel() {
        initComponents();
        panelCLI.setVisible(true);
        jLabel5.setText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel5.unix.text"));
        Document doc = textPaneClient.getDocument();
        if (doc instanceof HTMLDocument) { // Issue 185505
            HTMLDocument htmlDoc = (HTMLDocument)doc;
            Font font = UIManager.getFont("Label.font"); // NOI18N
            String bodyRule = "body { font-family: " + font.getFamily() + "; " // NOI18N
                + "color: " + SvnUtils.getColorString(textPaneClient.getForeground()) + "; " //NOI18N
                + "font-size: " + font.getSize() + "pt; }"; // NOI18N
            htmlDoc.getStyleSheet().addRule(bodyRule);
        }
        textPaneClient.setOpaque(false);
        textPaneClient.setBackground(new Color(0,0,0,0)); // windows and nimbus workaround see issue 145826
    }

    Collection<String> getKeywords () {
        if (keywords == null) {
            keywords = new String[] {
                "SVN", //NOI18N
                "SUBVERSION", //NOI18N
                Bundle.SvnOptionsPanel_kw1().toUpperCase(Locale.getDefault()),
                Bundle.SvnOptionsPanel_kw2().toUpperCase(Locale.getDefault()),
                Bundle.SvnOptionsPanel_kw3().toUpperCase(Locale.getDefault()),
                Bundle.SvnOptionsPanel_kw4().toUpperCase(Locale.getDefault()),
                Bundle.SvnOptionsPanel_kw5().toUpperCase(Locale.getDefault())
            };
        }
        return Collections.unmodifiableList(Arrays.asList(keywords));
    }

    void fileSystemChanged(FileSystem fileSystem) {
        boolean enabled = fileSystem != null;
        addButton.setEnabled(enabled);
        annotationTextField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        cbAutoLockFiles.setEnabled(enabled);
        cbDetermineBranches.setEnabled(enabled);
        cbGetRemoteLocks.setEnabled(enabled);
        cbOpenOutputWindow.setEnabled(enabled);
        excludeNewFiles.setEnabled(enabled);
        executablePathTextField.setEnabled(enabled);
        manageConnSettingsButton.setEnabled(enabled);
        manageLabelsButton.setEnabled(enabled);
        prefixRepositoryPath.setEnabled(enabled);
        textPaneClient.setEnabled(enabled);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(manageConnSettingsButton, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.manageConnSettingsButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel4.text")); // NOI18N

        jLabel2.setLabelFor(annotationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel2.text")); // NOI18N

        annotationTextField.setText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.annotationTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageLabelsButton, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.manageLabelsButton.text")); // NOI18N

        jScrollPane2.setBorder(null);

        jLabel6.setLabelFor(manageConnSettingsButton);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel6.TTtext")); // NOI18N

        jLabel7.setLabelFor(manageLabelsButton);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel8.text")); // NOI18N

        cbOpenOutputWindow.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbOpenOutputWindow, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbOpenOutputWindow.text")); // NOI18N
        cbOpenOutputWindow.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.cbOpenOutput.text")); // NOI18N
        cbOpenOutputWindow.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(excludeNewFiles, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.excludeNewFiles.text")); // NOI18N
        excludeNewFiles.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.excludeNewFiles.toolTipText")); // NOI18N
        excludeNewFiles.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(prefixRepositoryPath, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.prefixRepositoryPath.text")); // NOI18N
        prefixRepositoryPath.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.prefixRepositoryPath.toolTipText")); // NOI18N
        prefixRepositoryPath.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbGetRemoteLocks, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbGetRemoteLocks.text")); // NOI18N
        cbGetRemoteLocks.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbGetRemoteLocks.TTtext")); // NOI18N
        cbGetRemoteLocks.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(cbAutoLockFiles, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbAutoLockFiles.text")); // NOI18N
        cbAutoLockFiles.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbAutoLockFiles.toolTipText")); // NOI18N
        cbAutoLockFiles.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.browseButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel5.windows.text")); // NOI18N

        jLabel1.setLabelFor(executablePathTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout panelCLILayout = new javax.swing.GroupLayout(panelCLI);
        panelCLI.setLayout(panelCLILayout);
        panelCLILayout.setHorizontalGroup(
            panelCLILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCLILayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCLILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelCLILayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelCLILayout.createSequentialGroup()
                        .addComponent(executablePathTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))))
        );
        panelCLILayout.setVerticalGroup(
            panelCLILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(panelCLILayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCLILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseButton)
                    .addComponent(executablePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5))
        );

        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.browseButton.text")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.browseButton.text")); // NOI18N
        executablePathTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.executablePathTextField.text")); // NOI18N
        executablePathTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.executablePathTextField.text")); // NOI18N

        textPaneClient.setEditable(false);
        textPaneClient.setBackground(jLabel1.getBackground());
        textPaneClient.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        textPaneClient.setContentType(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.textPaneClient.contentType")); // NOI18N
        textPaneClient.setText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "OptionsPanel.textPaneClient.text")); // NOI18N

        cbDetermineBranches.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbDetermineBranches, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbDetermineBranches.text")); // NOI18N
        cbDetermineBranches.setToolTipText(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.cbDetermineBranches.text")); // NOI18N
        cbDetermineBranches.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbDetermineBranchesStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel10.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCLI, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manageLabelsButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textPaneClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manageConnSettingsButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(annotationTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbDetermineBranches)
                            .addComponent(cbOpenOutputWindow)
                            .addComponent(excludeNewFiles)
                            .addComponent(prefixRepositoryPath)
                            .addComponent(cbAutoLockFiles)
                            .addComponent(cbGetRemoteLocks))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbBuildHost, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cbBuildHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCLI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPaneClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(manageConnSettingsButton))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(annotationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbDetermineBranches)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(manageLabelsButton)
                    .addComponent(jLabel7))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbGetRemoteLocks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAutoLockFiles)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbOpenOutputWindow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeNewFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prefixRepositoryPath)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        manageConnSettingsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.manageConnSettingsButton.text")); // NOI18N
        manageConnSettingsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.manageConnSettingsButton.text")); // NOI18N
        annotationTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.annotationTextField.text")); // NOI18N
        annotationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.annotationTextField.text")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.addButton.text")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.addButton.text")); // NOI18N
        manageLabelsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSN_SvnOptionsPanel.manageLabelsButton.text")); // NOI18N
        manageLabelsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.manageLabelsButton.text")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel6.AccessibleContext.accessibleName")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel6.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel7.AccessibleContext.accessibleName")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "SvnOptionsPanel.jLabel7.AccessibleContext.accessibleDescription")); // NOI18N
        cbOpenOutputWindow.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnOptionsPanel.class, "ACSD_SvnOptionsPanel.cbOpenOutput.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbDetermineBranchesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbDetermineBranchesStateChanged
        boolean selected = cbDetermineBranches.isSelected();
        jLabel7.setEnabled(selected);
        manageLabelsButton.setEnabled(selected);
    }//GEN-LAST:event_cbDetermineBranchesStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton addButton = new javax.swing.JButton();
    final javax.swing.JTextField annotationTextField = new javax.swing.JTextField();
    final javax.swing.JButton browseButton = new javax.swing.JButton();
    final javax.swing.JCheckBox cbAutoLockFiles = new javax.swing.JCheckBox();
    final javax.swing.JComboBox cbBuildHost = new javax.swing.JComboBox();
    final javax.swing.JCheckBox cbDetermineBranches = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox cbGetRemoteLocks = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox cbOpenOutputWindow = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox excludeNewFiles = new javax.swing.JCheckBox();
    final javax.swing.JTextField executablePathTextField = new javax.swing.JTextField();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    final javax.swing.JButton manageConnSettingsButton = new javax.swing.JButton();
    final javax.swing.JButton manageLabelsButton = new javax.swing.JButton();
    final javax.swing.JPanel panelCLI = new javax.swing.JPanel();
    final javax.swing.JCheckBox prefixRepositoryPath = new javax.swing.JCheckBox();
    final javax.swing.JTextPane textPaneClient = new javax.swing.JTextPane();
    // End of variables declaration//GEN-END:variables
}
