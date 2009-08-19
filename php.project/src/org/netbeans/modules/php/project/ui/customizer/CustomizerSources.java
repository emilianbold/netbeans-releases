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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.PhpVersion;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServerController;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.Utils.EncodingModel;
import org.netbeans.modules.php.project.ui.Utils.EncodingRenderer;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerSources extends JPanel implements SourcesFolderProvider, HelpCtx.Provider {
    private static final long serialVersionUID = -58846454454544071L;

    private static final String DEFAULT_WEB_ROOT = NbBundle.getMessage(CustomizerSources.class, "LBL_DefaultWebRoot");
    private final CopyFilesVisual copyFilesVisual;
    private final boolean originalCopySrcFiles;
    final Category category;
    final PhpProjectProperties properties;
    String originalEncoding;
    boolean notified;
    boolean visible;
    private String originalCopySrcTarget;

    public CustomizerSources(final Category category, final PhpProjectProperties properties) {
        initComponents();

        this.category = category;
        this.properties = properties;

        initEncoding();
        initProjectAndSources();
        webRootTextField.setText(getWebRoot());
        originalCopySrcFiles = initCopyFiles();
        initPhpVersion();
        initTags();

        final LocalServer copyTarget = initCopyTarget();
        originalCopySrcTarget = copyTarget.getSrcRoot();

        copyFilesVisual = new CopyFilesVisual(this, LocalServer.PENDING_LOCAL_SERVER);
        copyFilesVisual.setCopyFiles(originalCopySrcFiles);
        copyFilesVisual.setState(false);
        copyFilesPanel.add(BorderLayout.NORTH, copyFilesVisual);

        PhpEnvironment.get().readDocumentRoots(new PhpEnvironment.ReadDocumentRootsNotifier() {
            public void finished(final List<DocumentRoot> documentRoots) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initCopyTargets(documentRoots, copyTarget);
                    }
                });
            }
        }, getSourcesFolderName());

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
        copyFilesVisual.addChangeListener(defaultChangeListener);
        webRootTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
        ItemListener defaultItemListener = new DefaultItemListener();
        phpVersionComboBox.addItemListener(defaultItemListener);
        shortTagsCheckBox.addItemListener(defaultItemListener);
        aspTagsCheckBox.addItemListener(defaultItemListener);

        // check init values
        validateFields();
    }

    @Override
    public void addNotify() {
        visible = true;
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        visible = false;
        super.removeNotify();
    }

    private void initEncoding() {
        originalEncoding = ProjectPropertiesSupport.getEncoding(properties.getProject());
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

    private void initProjectAndSources() {
        PhpProject project = properties.getProject();

        // load project path
        FileObject projectFolder = project.getProjectDirectory();
        String projectPath = FileUtil.getFileDisplayName(projectFolder);
        projectFolderTextField.setText(projectPath);

        // sources
        sourceFolderTextField.setText(FileUtil.getFileDisplayName(ProjectPropertiesSupport.getSourcesDirectory(properties.getProject())));
        // tests
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        if (testDirectory != null) {
            testFolderTextField.setText(FileUtil.getFileDisplayName(testDirectory));
        }
    }

    private void initPhpVersion() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(PhpVersion.values());
        PhpVersion phpVersion = ProjectPropertiesSupport.getPhpVersion(properties.getProject());
        assert phpVersion != null;
        comboBoxModel.setSelectedItem(phpVersion);
        phpVersionComboBox.setModel(comboBoxModel);
    }

    private void initTags() {
        shortTagsCheckBox.setSelected(ProjectPropertiesSupport.areShortTagsEnabled(properties.getProject()));
        aspTagsCheckBox.setSelected(ProjectPropertiesSupport.areAspTagsEnabled(properties.getProject()));
    }

    private boolean initCopyFiles() {
        return ProjectPropertiesSupport.isCopySourcesEnabled(properties.getProject());
    }

    private LocalServer initCopyTarget() {
        // copy target, if any
        File copyTarget = ProjectPropertiesSupport.getCopySourcesTarget(properties.getProject());
        if (copyTarget == null) {
            return LocalServer.getEmpty();
        }
        FileObject resolvedFO = FileUtil.toFileObject(copyTarget);
        if (resolvedFO == null) {
            // target directory doesn't exist?!
            return new LocalServer(copyTarget.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    void initCopyTargets(final List<DocumentRoot> roots, LocalServer initialLocalServer) {
        assert initialLocalServer != null;
        int size = roots.size() + 1;
        List<LocalServer> localServers = new ArrayList<LocalServer>(size);
        localServers.add(initialLocalServer);
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot());
            localServers.add(ls);
        }
        copyFilesVisual.setLocalServerModel(new LocalServer.ComboBoxModel(localServers.toArray(new LocalServer[localServers.size()])));
        copyFilesVisual.selectLocalServer(initialLocalServer);
        copyFilesVisual.setState(true);
        validateFields();
    }

    public String getSourcesFolderName() {
        return getSourcesFolder().getName();
    }

    public File getSourcesFolder() {
        return FileUtil.normalizeFile(new File(projectFolderTextField.getText()));
    }

    void validateFields() {
        if (!visible) {
            // #160249
            category.setValid(true);
            return;
        }
        if (!copyFilesVisual.getState()) {
            // document roots not read yet
            category.setValid(false);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);

        String err = null;

        // sources
        File srcDir = getSrcDir();
        if (!srcDir.isDirectory()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalSources"));
            category.setValid(false);
            return;
        }

        File webRootDir = getWebRootDir();
        if (!webRootDir.exists()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalWebRoot"));
            category.setValid(false);
            return;
        }
        // copy files
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
        properties.setCopySrcFiles(String.valueOf(isCopyFiles));
        properties.setCopySrcTarget(copyTargetDir == null ? "" : copyTargetDir.getAbsolutePath()); // NOI18N
        String webRoot = PropertyUtils.relativizeFile(srcDir, webRootDir);
        assert webRoot != null && !webRoot.startsWith("../") : "WebRoot must be underneath Sources";
        properties.setWebRoot(webRoot);
        properties.setShortTags(String.valueOf(shortTagsCheckBox.isSelected()));
        properties.setAspTags(String.valueOf(aspTagsCheckBox.isSelected()));
        properties.setPhpVersion(((PhpVersion) phpVersionComboBox.getSelectedItem()).name());
    }

    private File getSrcDir() {
        return new File(sourceFolderTextField.getText()); // file already normalized
    }

    private File getWebRootDir() {
        String webRoot = webRootTextField.getText();
        if (isDefaultWebRoot(webRoot)) {
            return getSrcDir();
        }
        return FileUtil.normalizeFile(new File(getSrcDir(), webRoot));
    }

    private String getWebRoot() {
        String webRoot = properties.getWebRoot();
        if (isDefaultWebRoot(webRoot)) {
            return DEFAULT_WEB_ROOT;
        }
        return webRoot;
    }

    private static boolean isDefaultWebRoot(String webRoot) {
        return webRoot == null || webRoot.trim().length() == 0 || webRoot.equals(".") || DEFAULT_WEB_ROOT.equals(webRoot); // NOI18N
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
        return originalCopySrcFiles && originalCopySrcTarget.equals(copyTargetDir); // #133109
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
        sourceFolderTextField = new javax.swing.JTextField();
        testFolderLabel = new javax.swing.JLabel();
        testFolderTextField = new javax.swing.JTextField();
        webRootLabel = new javax.swing.JLabel();
        webRootTextField = new javax.swing.JTextField();
        webRootButton = new javax.swing.JButton();
        copyFilesPanel = new javax.swing.JPanel();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();
        phpVersionLabel = new javax.swing.JLabel();
        phpVersionComboBox = new javax.swing.JComboBox();
        phpVersionInfoLabel = new javax.swing.JLabel();
        shortTagsCheckBox = new javax.swing.JCheckBox();
        aspTagsCheckBox = new javax.swing.JCheckBox();

        projectFolderLabel.setLabelFor(projectFolderTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, bundle.getString("LBL_ProjectFolder")); // NOI18N

        projectFolderTextField.setEditable(false);

        sourceFolderLabel.setLabelFor(sourceFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, bundle.getString("LBL_SourceFolder")); // NOI18N

        sourceFolderTextField.setEditable(false);

        testFolderLabel.setLabelFor(testFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(testFolderLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.testFolderLabel.text")); // NOI18N

        testFolderTextField.setEditable(false);

        webRootLabel.setLabelFor(webRootTextField);
        org.openide.awt.Mnemonics.setLocalizedText(webRootLabel, bundle.getString("LBL_WebRoot")); // NOI18N

        webRootTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(webRootButton, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_BrowseWebRoot")); // NOI18N
        webRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webRootButtonActionPerformed(evt);
            }
        });

        copyFilesPanel.setLayout(new java.awt.BorderLayout());

        encodingLabel.setLabelFor(encodingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Encoding")); // NOI18N

        phpVersionLabel.setLabelFor(phpVersionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(phpVersionLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(phpVersionInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionInfoLabel.text")); // NOI18N
        phpVersionInfoLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(shortTagsCheckBox, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ShortTagsEnabled")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(aspTagsCheckBox, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_AspTagsEnabled")); // NOI18N

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
                            .add(webRootLabel)
                            .add(encodingLabel)
                            .add(phpVersionLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                            .add(sourceFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                            .add(testFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(webRootTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(webRootButton))
                            .add(encodingComboBox, 0, 280, Short.MAX_VALUE)
                            .add(phpVersionComboBox, 0, 280, Short.MAX_VALUE)
                            .add(phpVersionInfoLabel)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, copyFilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .add(aspTagsCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(testFolderLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(shortTagsCheckBox)
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
                    .add(sourceFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(testFolderLabel)
                    .add(testFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(webRootLabel)
                    .add(webRootTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(webRootButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(encodingLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phpVersionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(phpVersionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(phpVersionInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(shortTagsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(aspTagsCheckBox))
        );

        projectFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.projectFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        projectFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.projectFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSN_ProjectFolder")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSD_ProjectFolder")); // NOI18N
        sourceFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        sourceFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        sourceFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        sourceFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        testFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.testFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        testFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.testFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        testFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.testFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        testFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.testFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        webRootLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootLabel.AccessibleContext.accessibleName")); // NOI18N
        webRootLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootLabel.AccessibleContext.accessibleDescription")); // NOI18N
        webRootTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootTextField.AccessibleContext.accessibleName")); // NOI18N
        webRootTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootTextField.AccessibleContext.accessibleDescription")); // NOI18N
        webRootButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSN_Browse")); // NOI18N
        webRootButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSD_BrowseWebRoot")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.copyFilesPanel.AccessibleContext.accessibleName")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.copyFilesPanel.AccessibleContext.accessibleDescription")); // NOI18N
        encodingLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.encodingLabel.AccessibleContext.accessibleName")); // NOI18N
        encodingLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.encodingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        encodingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSN_Encoding")); // NOI18N
        encodingComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "ACSD_Encoding")); // NOI18N
        shortTagsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        shortTagsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        aspTagsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.aspTagsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        aspTagsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.aspTagsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void webRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webRootButtonActionPerformed
        String selected = Utils.browseSourceFolder(properties.getProject(), webRootTextField.getText());
        if (isDefaultWebRoot(selected)) {
            selected = DEFAULT_WEB_ROOT;
        }
        webRootTextField.setText(selected);
    }//GEN-LAST:event_webRootButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox aspTagsCheckBox;
    private javax.swing.JPanel copyFilesPanel;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JComboBox phpVersionComboBox;
    private javax.swing.JLabel phpVersionInfoLabel;
    private javax.swing.JLabel phpVersionLabel;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderTextField;
    private javax.swing.JCheckBox shortTagsCheckBox;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JTextField sourceFolderTextField;
    private javax.swing.JLabel testFolderLabel;
    private javax.swing.JTextField testFolderTextField;
    private javax.swing.JButton webRootButton;
    private javax.swing.JLabel webRootLabel;
    private javax.swing.JTextField webRootTextField;
    // End of variables declaration//GEN-END:variables

    private class DefaultChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            validateFields();
        }
    }

    private class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            validateFields();
        }
        public void removeUpdate(DocumentEvent e) {
            validateFields();
        }
        public void changedUpdate(DocumentEvent e) {
            validateFields();
        }
    }

    private class DefaultItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            validateFields();
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerSources.class);
    }

    public void addChangeListener(ChangeListener listener) {
        throw new IllegalStateException();
    }

    public void removeChangeListener(ChangeListener listener) {
        throw new IllegalStateException();
    }
}
