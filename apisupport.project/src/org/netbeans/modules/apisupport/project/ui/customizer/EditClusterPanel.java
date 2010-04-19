/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.awt.Dialog;
import java.io.File;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizerJavadoc;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizerSources;
import org.netbeans.modules.apisupport.project.universe.JavadocRootsSupport;
import org.netbeans.modules.apisupport.project.universe.SourceRootsSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Richard Michalsky
 */
public final class EditClusterPanel extends javax.swing.JPanel implements DocumentListener {

    private NbPlatformCustomizerSources sourcesPanel;
    private NbPlatformCustomizerJavadoc javadocPanel;
    private JButton okButton;

    /**
     * Displays Add Cluster dialog and lets user select path to external cluster.
     *
     * @param prj Project into which the path will be stored. Returned path is relative to the project dir if possible.
     * @return Info for newly added cluster or null if user cancelled the dialog.
     */
    static ClusterInfo showAddDialog(Project prj) {
        EditClusterPanel panel = new EditClusterPanel();
        panel.prjDir = FileUtil.toFile(prj.getProjectDirectory());
        panel.prj = prj;
        SourceRootsSupport srs = new SourceRootsSupport(new URL[0], null);
        panel.sourcesPanel.setSourceRootsProvider(srs);
        JavadocRootsSupport jrs = new JavadocRootsSupport(new URL[0], null);
        panel.javadocPanel.setJavadocRootsProvider(jrs);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(EditClusterPanel.class, "CTL_AddCluster_Title"), // NOI18N
                true,
                new Object[] { panel.okButton, NotifyDescriptor.CANCEL_OPTION },
                panel.okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(EditClusterPanel.class),
                null);
        descriptor.setClosingOptions(null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        panel.updateDialog();
        dlg.setVisible(true);
        ClusterInfo retVal = null;
        if (descriptor.getValue() == panel.okButton) {
            retVal = ClusterInfo.createExternal(panel.getAbsoluteClusterPath(),
                    srs.getSourceRoots(), jrs.getJavadocRoots(), true);

        }
        dlg.dispose();
        return retVal;
    }

    /**
     * Shows Edit Cluster dialog for existing external cluster.
     * Browse button is disabled, user can only change src and javadoc.
     *
     * @param ci Original cluster info 
     * @return Updated cluster info or null if user cancelled the dialog
     */
    static ClusterInfo showEditDialog(ClusterInfo ci, Project prj) {
        EditClusterPanel panel = new EditClusterPanel();
        panel.prjDir = FileUtil.toFile(prj.getProjectDirectory());
        panel.prj = prj;
        SourceRootsSupport srs = new SourceRootsSupport(
                ci.getSourceRoots() == null ? new URL[0] : ci.getSourceRoots(), null);
        panel.sourcesPanel.setSourceRootsProvider(srs);
        JavadocRootsSupport jrs = new JavadocRootsSupport(
                ci.getJavadocRoots() == null ? new URL[0] : ci.getJavadocRoots(), null);
        panel.javadocPanel.setJavadocRootsProvider(jrs);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(EditClusterPanel.class, "CTL_EditCluster_Title"), // NOI18N
                true,
                new Object[] { panel.okButton, NotifyDescriptor.CANCEL_OPTION },
                panel.okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(EditClusterPanel.class),
                null);
        descriptor.setClosingOptions(null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        panel.clusterDirText.setText(ci.getClusterDir().toString());
        panel.updateDialog();
        panel.browseButton.setEnabled(false);
        dlg.setVisible(true);
        ClusterInfo retVal = null;
        if (descriptor.getValue() == panel.okButton) {
            retVal = ClusterInfo.createExternal(panel.getAbsoluteClusterPath(), 
                    srs.getSourceRoots(), jrs.getJavadocRoots(), true);
        }
        dlg.dispose();
        return retVal;
    }
    private File prjDir;
    private Project prj;

    /** Creates new form EditClusterPanel */
    public EditClusterPanel() {
        initComponents();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton,
            NbBundle.getMessage(EditClusterPanel.class, "CTL_OK"));
    }

    private File getAbsoluteClusterPath() {
        String maybeRelPath = clusterDirText.getText();
        return PropertyUtils.resolveFile(prjDir, maybeRelPath);
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
        clusterDirText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        sourcesPanelContainer = new javax.swing.JPanel();
        sourcesPanel = new NbPlatformCustomizerSources();
        sourcesPanelContainer.add(sourcesPanel);
        javadocPanelContainer = new javax.swing.JPanel();
        javadocPanel = new NbPlatformCustomizerJavadoc();
        javadocPanelContainer.add(javadocPanel);

        jLabel1.setLabelFor(clusterDirText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.jLabel1.text")); // NOI18N

        clusterDirText.setEditable(false);
        clusterDirText.setText(org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "MSG_BrowseForCluster")); // NOI18N
        clusterDirText.getDocument().addDocumentListener(this);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(EditClusterPanel.class, "EditClusterPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        sourcesPanelContainer.setLayout(new java.awt.CardLayout());

        javadocPanelContainer.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(clusterDirText, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(jLabel1))
                .addContainerGap())
            .addComponent(javadocPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
            .addComponent(sourcesPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseButton)
                    .addComponent(clusterDirText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(sourcesPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javadocPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser(ModuleUISettings.getDefault().getLastUsedClusterLocation());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            AGAIN: for (;;) {
                if (! file.exists() || file.isFile() || ! ClusterUtils.isValidCluster(file)) {
                    if (Clusterize.clusterize(prj, file)) {
                        continue AGAIN;
                    }
                } else {
                    ModuleUISettings.getDefault().setLastUsedClusterLocation(file.getParentFile().getAbsolutePath());
                    String relPath = PropertyUtils.relativizeFile(prjDir, file);
                    clusterDirText.setText(relPath);
                }
                break;
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    public @Override void changedUpdate(DocumentEvent e) {
        updateDialog();
    }

    public @Override void insertUpdate(DocumentEvent e) {
        updateDialog();
    }

    public @Override void removeUpdate(DocumentEvent e) {
        updateDialog();
    }

    private void updateDialog() {
        okButton.setEnabled((getAbsoluteClusterPath()).exists());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField clusterDirText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel javadocPanelContainer;
    private javax.swing.JPanel sourcesPanelContainer;
    // End of variables declaration//GEN-END:variables


}
