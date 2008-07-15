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

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServerController;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.Utils.EncodingModel;
import org.netbeans.modules.php.project.ui.Utils.EncodingRenderer;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerSources extends JPanel implements SourcesFolderProvider {
    private static final long serialVersionUID = -5803489817914071L;

    final Category category;
    final PhpProjectProperties properties;
    final PropertyEvaluator evaluator;
    String originalEncoding;
    boolean notified;
    private final LocalServerController localServerController;
    private final CopyFilesVisual copyFilesVisual;
    private final boolean originalCopySrcFiles;
    private final String originalCopySrcTarget;
    private final String originalSources;

    public CustomizerSources(final Category category, final PhpProjectProperties properties) {
        initComponents();

        this.category = category;
        this.properties = properties;
        evaluator = properties.getProject().getEvaluator();

        initEncoding();
        LocalServer sources = initSources();
        originalSources = sources.getSrcRoot();
        webRootTextField.setText(properties.getWebRoot());
        originalCopySrcFiles = initCopyFiles();
        LocalServer copyTarget = initCopyTarget();
        LocalServer[] copyTargets = getCopyTargets(copyTarget);
        originalCopySrcTarget = copyTarget.getSrcRoot();
        localServerController = LocalServerController.create(localServerComboBox, localServerButton,
                NbBundle.getMessage(CustomizerSources.class, "LBL_SelectSourceFolderTitle"), sources);
        localServerController.selectLocalServer(sources);

        copyFilesVisual = new CopyFilesVisual(this, copyTargets);
        copyFilesVisual.selectLocalServer(copyTarget);
        copyFilesVisual.setCopyFiles(originalCopySrcFiles);
        copyFilesPanel.add(BorderLayout.NORTH, copyFilesVisual);

        encodingComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Charset enc = (Charset) encodingComboBox.getSelectedItem();
                String encName;
                if (enc != null) {
                    encName = enc.name();
                } else {
                    encName = originalEncoding;
                }
                if (!notified && encName != null && !encName.equals(originalEncoding)) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(CustomizerSources.class, "MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE));
                    notified = true;
                }
                properties.setEncoding(encName);
            }
        });
        ChangeListener defaultChangeListener = new DefaultChangeListener();
        localServerController.addChangeListener(defaultChangeListener);
        copyFilesVisual.addChangeListener(defaultChangeListener);
        webRootTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
        // check init values
        validateFields(category);
    }

    private void initEncoding() {
        originalEncoding = evaluator.getProperty(PhpProjectProperties.SOURCE_ENCODING);
        if (originalEncoding == null) {
            originalEncoding = Charset.defaultCharset().name();
        }
        encodingComboBox.setRenderer(new EncodingRenderer());
        encodingComboBox.setModel(new EncodingModel(originalEncoding));
        final String lafid = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { // NOI18N
             encodingComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
             encodingComboBox.addItemListener(new ItemListener() {
                 public void itemStateChanged(ItemEvent e) {
                     JComboBox combo = (JComboBox) e.getSource();
                     combo.setPopupVisible(false);
                 }
             });
        }
    }

    private LocalServer initSources() {
        PhpProject project = properties.getProject();

        // load project path
        FileObject projectFolder = project.getProjectDirectory();
        String projectPath = FileUtil.getFileDisplayName(projectFolder);
        projectFolderTextField.setText(projectPath);

        // sources
        String src = evaluator.evaluate(properties.getSrcDir());
        File resolvedFile = PropertyUtils.resolveFile(FileUtil.toFile(projectFolder), src);
        FileObject resolvedFO = FileUtil.toFileObject(resolvedFile);
        if (resolvedFO == null) {
            // src directory doesn't exist?!
            return new LocalServer(resolvedFile.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    private boolean initCopyFiles() {
        return Boolean.valueOf(evaluator.evaluate(properties.getCopySrcFiles()));
    }

    private LocalServer initCopyTarget() {
        // copy target, if any
        String copyTarget = evaluator.evaluate(properties.getCopySrcTarget());
        if (copyTarget == null || copyTarget.length() == 0) {
            return new LocalServer(""); // NOI18N
        }
        File resolvedFile = FileUtil.normalizeFile(new File(copyTarget));
        FileObject resolvedFO = FileUtil.toFileObject(resolvedFile);
        if (resolvedFO == null) {
            // target directory doesn't exist?!
            return new LocalServer(resolvedFile.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    private LocalServer[] getCopyTargets(LocalServer initialLocalServer) {
        List<DocumentRoot> roots = PhpEnvironment.get().getDocumentRoots(getSourcesFolderName());

        int size = roots.size() + 1;
        List<LocalServer> localServers = new ArrayList<LocalServer>(size);
        localServers.add(initialLocalServer);
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot());
            localServers.add(ls);
        }
        return localServers.toArray(new LocalServer[size]);
    }

    public String getSourcesFolderName() {
        return getSourcesFolder().getNameExt();
    }

    public FileObject getSourcesFolder() {
        return FileUtil.toFileObject(new File(projectFolderTextField.getText()));
    }

    void validateFields(Category category) {
        category.setErrorMessage(null);
        category.setValid(true);

        // sources
        String err = LocalServerController.validateLocalServer(localServerController.getLocalServer(), "Sources", true, // NOI18N
                true);
        if (err != null) {
            category.setErrorMessage(err);
            category.setValid(false);
            return;
        }

        // copy files
        File srcDir = getSrcDir();
        File webRoot = getWebRoot();
        if (!webRoot.exists()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, 
                    "MSG_IllegalWebRoot"));//NOI18N
            return;
        }
        File copyTargetDir = getCopyTargetDir();
        boolean isCopyFiles = copyFilesVisual.isCopyFiles();
        if (isCopyFiles) {
            if (copyTargetDir == null) {
                // nothing selected
                category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalFolderName"));
                category.setValid(false);
                return;
            }
            err = LocalServerController.validateLocalServer(copyFilesVisual.getLocalServer(), "Folder", // NOI18N
                    allowNonEmptyDirectory(copyTargetDir.getAbsolutePath(), srcDir.getAbsolutePath()), true);
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
            // #131023
            err = Utils.validateSourcesAndCopyTarget(srcDir.getAbsolutePath(), copyTargetDir.getAbsolutePath());
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
        }

        // everything ok
        File projectDirectory = FileUtil.toFile(properties.getProject().getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        if (srcPath == null || srcPath.startsWith("../")) { // NOI18N
            // relative path, change to absolute
            srcPath = srcDir.getAbsolutePath();
        }
        properties.setSrcDir(srcPath);
        properties.setCopySrcFiles(String.valueOf(isCopyFiles));
        properties.setCopySrcTarget(copyTargetDir == null ? "" : copyTargetDir.getAbsolutePath()); // NOI18N
        properties.setWebRoot(webRootTextField.getText());
    }

    private File getSrcDir() {
        LocalServer localServer = localServerController.getLocalServer();
        return FileUtil.normalizeFile(new File(localServer.getSrcRoot()));
    }

    private File getWebRoot() {
        return new File(getSrcDir(), webRootTextField.getText());
    }

    private File getCopyTargetDir() {
        LocalServer localServer = copyFilesVisual.getLocalServer();
        // #132864
        String srcRoot = localServer.getSrcRoot();
        if (srcRoot == null || srcRoot.length() == 0) {
            return null;
        }
        return FileUtil.normalizeFile(new File(srcRoot));
    }

    private boolean allowNonEmptyDirectory(String copyTargetDir, String srcDir) {
        return originalCopySrcFiles && originalCopySrcTarget.equals(copyTargetDir)
                && originalSources.equals(srcDir); // #133109
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectFolderLabel = new javax.swing.JLabel();
        projectFolderTextField = new javax.swing.JTextField();
        sourceFolderLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();
        copyFilesPanel = new javax.swing.JPanel();
        localServerComboBox = new javax.swing.JComboBox();
        localServerButton = new javax.swing.JButton();
        webRootLabel = new javax.swing.JLabel();
        webRootButton = new javax.swing.JButton();
        webRootTextField = new javax.swing.JTextField();

        projectFolderLabel.setLabelFor(projectFolderTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, bundle.getString("LBL_ProjectFolder")); // NOI18N

        projectFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, bundle.getString("LBL_SourceFolder")); // NOI18N

        encodingLabel.setLabelFor(encodingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Encoding")); // NOI18N

        copyFilesPanel.setLayout(new java.awt.BorderLayout());

        localServerComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(localServerButton, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse")); // NOI18N

        webRootLabel.setLabelFor(webRootTextField);
        org.openide.awt.Mnemonics.setLocalizedText(webRootLabel, bundle.getString("LBL_WebRoot")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(webRootButton, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse")); // NOI18N
        webRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webRootButtonActionPerformed(evt);
            }
        });

        webRootTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectFolderLabel)
                            .add(sourceFolderLabel)
                            .add(webRootLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(webRootTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                                    .add(localServerComboBox, 0, 173, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, localServerButton)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, webRootButton)))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, copyFilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(encodingLabel)
                        .add(40, 40, 40)
                        .add(encodingComboBox, 0, 268, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectFolderLabel)
                    .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceFolderLabel)
                    .add(localServerButton)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(webRootLabel)
                    .add(webRootButton)
                    .add(webRootTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(encodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(encodingLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void webRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webRootButtonActionPerformed
        Utils.browseSourceFolder(properties.getProject(), webRootTextField);
    }//GEN-LAST:event_webRootButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel copyFilesPanel;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JButton localServerButton;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderTextField;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JButton webRootButton;
    private javax.swing.JLabel webRootLabel;
    private javax.swing.JTextField webRootTextField;
    // End of variables declaration//GEN-END:variables

    private class DefaultChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            validateFields(category);
        }
    }

    private class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            validateFields(category);
        }
        public void removeUpdate(DocumentEvent e) {
            validateFields(category);
        }
        public void changedUpdate(DocumentEvent e) {
            validateFields(category);
        }
    }
}
