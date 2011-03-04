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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileView;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

final class RemoteProjectImportVisual extends SettingsPanel implements DocumentListener, HelpCtx.Provider {
    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    
    private String localProjectName;
    private volatile boolean initialized = false;
    private final RemoteProjectImportWizardPanel controller;
    // null - all is fine
    // TRUE - check in progress
    // FALSE - check failed
    private volatile Boolean checkingRemote = null;
    private volatile String remoteCheckError = "";
    private ExecutionEnvironment remoteHost;
    /** Creates new form RemoteProjectImportVisual */
    public RemoteProjectImportVisual(RemoteProjectImportWizardPanel controller) {
        initComponents();
        this.controller = controller;
        localProjectName = "";
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(RemoteProjectImportVisual.this);
        projectLocationTextField.getDocument().addDocumentListener(RemoteProjectImportVisual.this);
        remoteProjectFolder.getDocument().addDocumentListener(new RemoteProjectListener());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.title");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        remoteProjectFolderLabel = new javax.swing.JLabel();
        remoteProjectFolder = new javax.swing.JTextField();
        browseRemoteButton = new javax.swing.JButton();
        localProjectPanel = new javax.swing.JPanel();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseLocalButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        remoteProjectFolderLabel.setLabelFor(remoteProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(remoteProjectFolderLabel, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.remoteProjectFolderLabel.text")); // NOI18N

        remoteProjectFolder.setColumns(20);
        remoteProjectFolder.setText(org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.remoteProjectFolder.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseRemoteButton, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.browseRemoteButton.text")); // NOI18N
        browseRemoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseRemoteButtonActionPerformed(evt);
            }
        });

        localProjectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.localProjectPanel.border.title"))); // NOI18N

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.projectNameLabel.text")); // NOI18N

        projectNameTextField.setColumns(20);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.projectLocationLabel.text")); // NOI18N

        projectLocationTextField.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(browseLocalButton, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.browseLocalButton.text")); // NOI18N
        browseLocalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocalButtonbrowseLocationAction(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.createdFolderLabel.text")); // NOI18N

        createdFolderTextField.setColumns(20);
        createdFolderTextField.setEditable(false);

        javax.swing.GroupLayout localProjectPanelLayout = new javax.swing.GroupLayout(localProjectPanel);
        localProjectPanel.setLayout(localProjectPanelLayout);
        localProjectPanelLayout.setHorizontalGroup(
            localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localProjectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(createdFolderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectLocationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, localProjectPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createdFolderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseLocalButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        localProjectPanelLayout.setVerticalGroup(
            localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localProjectPanelLayout.createSequentialGroup()
                .addGroup(localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectLocationLabel)
                    .addComponent(browseLocalButton))
                .addGap(5, 5, 5)
                .addGroup(localProjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFolderLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        localProjectPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {createdFolderTextField, projectLocationTextField, projectNameTextField});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(remoteProjectFolderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(remoteProjectFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseRemoteButton)
                        .addGap(30, 30, 30))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(localProjectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remoteProjectFolderLabel)
                    .addComponent(remoteProjectFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseRemoteButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localProjectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocalButtonbrowseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocalButtonbrowseLocationAction
        String path = this.projectLocationTextField.getText();
        FileChooser chooser = new FileChooser(
                NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_SelectProjectLocation"),
                null, JFileChooser.DIRECTORIES_ONLY, null, path, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(projectDir.getAbsolutePath());
        }
        controller.fireChangeEvent();
}//GEN-LAST:event_browseLocalButtonbrowseLocationAction

    private static Map<ExecutionEnvironment, String> lastUsedDirs = new HashMap<ExecutionEnvironment, String>();
    
    private void browseRemoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseRemoteButtonActionPerformed
        if (remoteHost != null) {
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {

                private volatile String homeDir;

                @Override
                public void doWork() {
                    if (ConnectionManager.getInstance().isConnectedTo(remoteHost)) {
                        homeDir = lastUsedDirs.get(remoteHost);
                        if (homeDir == null) {
                            homeDir = getRemoteProjectDir(remoteHost);
                        }
                    }
                }

                @Override
                public void doPostRunInEDT() {
                    if (ConnectionManager.getInstance().isConnectedTo(remoteHost)) {
                        String chooseRemoteFolder = chooseRemoteFolder(homeDir);
                        if (chooseRemoteFolder != null) {
                            remoteProjectFolder.setText(chooseRemoteFolder);
                            return;
                        }
                    }
                    controller.fireChangeEvent();
                }

            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.comment.title"); //NOI18N
            String msg = NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.comment.message", remoteHost.getDisplayName()); //NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
        }
        controller.fireChangeEvent();
    }//GEN-LAST:event_browseRemoteButtonActionPerformed

    private String chooseRemoteFolder(String homeDir) {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        JFileChooserEx fileChooser = (JFileChooserEx) RemoteFileUtil.createFileChooser(remoteHost,
            NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.title"),//NOI18N
            NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.open"), //NOI18N
            JFileChooser.DIRECTORIES_ONLY, null, homeDir, true);
        fileChooser.setFileView(new MyFileView(fileChooser));
        int ret = fileChooser.showOpenDialog(mainWindow);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        FileObject remoteProjectFO = fileChooser.getSelectedFileObject();
        if (remoteProjectFO == null) {
            return null;
        }
        lastUsedDirs.put(remoteHost, remoteProjectFO.getParent().getPath());
//        FileObject nbprojectFO = remoteProjectFO.getFileObject("nbproject"); // NOI18N
//        if (nbprojectFO == null) {
//            return null;
//        }
        return remoteProjectFO.getPath();
    }
    
    private static String getRemoteProjectDir(ExecutionEnvironment env) {
        try {
            // TODO: better to jump into ~/SunStudioProjects or ~/NetBeansProjects
            return HostInfoUtils.getHostInfo(env).getUserDir() + "/" + NbBundle.getMessage(RemoteProjectImportVisual.class, "DefaultProjectFolder"); 
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (CancellationException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        }
        return null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLocalButton;
    private javax.swing.JButton browseRemoteButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JPanel localProjectPanel;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JTextField remoteProjectFolder;
    private javax.swing.JLabel remoteProjectFolderLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    void store(WizardDescriptor settings) {
        String projectName = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        if (CndPathUtilitities.isPathAbsolute(folder)) {
            File file = CndFileUtils.createLocalFile(folder);
            file = FileUtil.normalizeFile(file);
            settings.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, file);
        }
        settings.putProperty(WizardConstants.PROPERTY_NAME, projectName);
    }

    @Override
    void read(WizardDescriptor settings) {
        initialized = false;
        String hostUID = (String) settings.getProperty(WizardConstants.PROPERTY_HOST_UID);
        remoteHost = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        File projectLocation = (File) settings.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER); // File - SIC! for projects always local
        String projectName = null;
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectName = projectLocation.getName();
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
//        if (projectName == null) {
//            if (name == null) {
//                String workingDir = (String) settings.getProperty(WizardConstants.PROPERTY_WORKING_DIR); //NOI18N
//                if (workingDir != null && workingDir.length() > 0
//                        && (templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)
//                        || templateName.equals(NewMakeProjectWizardIterator.FULL_REMOTE_PROJECT_NAME))) {
//                    name = CndPathUtilitities.getBaseName(workingDir);
//                } else {
//                    String sourcesPath = (String) settings.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH); // NOI18N
//                    if (sourcesPath != null && sourcesPath.length() > 0) {
//                        name = CndPathUtilitities.getBaseName(sourcesPath);
//                    }
//                }
//            }
//            int baseCount = 1;
//            String formater = name + "_{0}"; // NOI18N
//            while ((projectName = PanelProjectLocationVisual.validFreeProjectName(projectLocation, formater, baseCount)) == null) {
//                baseCount++;
//            }
//            settings.putProperty(NewMakeProjectWizardIterator.PROP_NAME_INDEX, Integer.valueOf(baseCount));
//        }
//        this.projectNameTextField.setText(projectName);
//        this.projectNameTextField.selectAll();
        initialized = true;
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        if (!initialized) {
            return false;
        }
        if (this.remoteProjectFolder.getText().trim().isEmpty()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(RemoteProjectImportVisual.class, "MSG_EmptyRemoteProjectName")); // NOI18N
            return false; // remote project is not specified
        }
        if (remoteHost == null || !ConnectionManager.getInstance().isConnectedTo(remoteHost)) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(RemoteProjectImportVisual.class, "MSG_RemoteHostIsNotConnected")); // NOI18N
            return false; // remote project is not specified
        }
        String remoteError = this.remoteCheckError;
        if (checkingRemote == Boolean.TRUE) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(RemoteProjectImportVisual.class, "MSG_CheckingRemoteProject")); // NOI18N
            return false; // checking remote project 
        } else if (checkingRemote == Boolean.FALSE) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, remoteError);
            return false; // checking remote project folder failed
        }
        if (!PanelProjectLocationVisual.isValidLocalProjectNameAndLocation(settings, projectNameTextField.getText(), projectLocationTextField.getText(), createdFolderTextField.getText())) {
            return false;
        }
        final File destFolder = PanelProjectLocationVisual.getCanonicalFile(CndFileUtils.createLocalFile(createdFolderTextField.getText()).getAbsoluteFile()); // project folder always local
        if (destFolder.exists()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(RemoteProjectImportVisual.class, "MSG_LocalProjectAlreadyExists", createdFolderTextField.getText().trim()));
            return false; // checking remote project folder failed 
        }
        return true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    private void update(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    /** Handles changes in the Project name and project directory
     */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            String projectName = projectNameTextField.getText().trim();
            String projectFolder = projectLocationTextField.getText().trim();
            while (projectFolder.endsWith("/")) { // NOI18N
                projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
            }
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
        }
        controller.fireChangeEvent(); // Notify that the panel changed
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return getHelpCtxImpl();
    }
    
    static HelpCtx getHelpCtxImpl() {
        return new HelpCtx("ImportRemoteProjectWizard"); // NOI18N
    }
    
    private class RemoteProjectListener implements DocumentListener, Runnable {
        private final RequestProcessor.Task task = new RequestProcessor("Checking remote project folder", 1).create(this); // NOI18N
        private String lastCheckError = "";// NOI18N
        private String remotePath = "";// NOI18N
        private Color defColor = remoteProjectFolder.getForeground();
        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            if (remoteHost != null) {
                checkingRemote = Boolean.TRUE;
                remotePath = remoteProjectFolder.getText().trim();
                remoteProjectFolder.setForeground(Color.red);
                task.schedule(10);
                controller.fireChangeEvent();
            }
            String baseName = CndPathUtilitities.getBaseName(remotePath);
            if (baseName != null && !baseName.isEmpty() && !baseName.endsWith("/")) { // NOI18N
                projectNameTextField.setText(baseName+"_shadow");// NOI18N
            }
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                String status = lastCheckError;
                checkingRemote = status.isEmpty() ? null : Boolean.FALSE;
                if (checkingRemote == null) {
                    remoteProjectFolder.setForeground(defColor);
                }
                remoteCheckError = status;
                controller.fireChangeEvent();
            } else {
                String curRemotePath = this.remotePath;
                try {
                    assert remoteHost != null;
                    ConnectionManager.getInstance().connectTo(remoteHost);
                    boolean fileExists = HostInfoUtils.fileExists(remoteHost, curRemotePath);
                    String msgError = "";
                    msgError = fileExists ? "" : NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.remotePathNotExists", curRemotePath);
                    if (fileExists) {
                        // check that this is project
                        String conf = curRemotePath + "/nbproject/configurations.xml"; // NOI18N
                        String projectXml = curRemotePath + "/nbproject/project.xml"; // NOI18N
                        fileExists = HostInfoUtils.fileExists(remoteHost, conf) && HostInfoUtils.fileExists(remoteHost, projectXml);
                        if (!fileExists) {
                            msgError = NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.remotePathNotProject", curRemotePath);
                        }
                    }
                    lastCheckError = msgError;
                } catch (Throwable ex) {
                    lastCheckError = NbBundle.getMessage(RemoteProjectImportVisual.class, "RemoteProjectImportVisual.remotePathNotExistsError", curRemotePath, ex.getMessage());
                }
                SwingUtilities.invokeLater(this);
            }
        }
    }
    
        private static final class MyFileView extends FileView implements Runnable {

        private final JFileChooser chooser;
        private final Map<File, Icon> knownProjectIcons = new HashMap<File, Icon>();
        private final RequestProcessor.Task task = new RequestProcessor("ProjectIconFileView").create(this);//NOI18N
        private File lookingForIcon;

        public MyFileView(JFileChooser chooser) {
            this.chooser = chooser;
        }

        @Override
        public Icon getIcon(File f) {
            if (f.isDirectory() && // #173958: do not call ProjectManager.isProject now, could block
                    !f.toString().matches("/[^/]+") && // Unix: /net, /proc, etc. //NOI18N
                    f.getParentFile() != null) { // do not consider drive roots
                synchronized (this) {
                    Icon icon = knownProjectIcons.get(f);
                    if (icon != null) {
                        return icon;
                    } else if (lookingForIcon == null) {
                        lookingForIcon = f;
                        task.schedule(20);
                        // Only calculate one at a time.
                        // When the view refreshes, the next unknown icon
                        // should trigger the task to be reloaded.
                    }
                }
            }
            return chooser.getFileSystemView().getSystemIcon(f);
        }

        @Override
        public void run() {
            String path = lookingForIcon.getAbsolutePath();
            String project = path + "/nbproject"; // NOI18N
            File projectDir = chooser.getFileSystemView().createFileObject(project);
            Icon icon = chooser.getFileSystemView().getSystemIcon(lookingForIcon);
            if (projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()) {
                String projectXml = path + "/nbproject/project.xml"; // NOI18N
                File projectFile = chooser.getFileSystemView().createFileObject(projectXml);
                if (projectFile.exists()) {
                    String conf = path + "/nbproject/configurations.xml"; // NOI18N
                    File configuration = chooser.getFileSystemView().createFileObject(conf);
                    if (configuration.exists()) {
                        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
                    }
                }
            }
            synchronized (this) {
                knownProjectIcons.put(lookingForIcon, icon);
                lookingForIcon = null;
            }
            chooser.repaint();
        }
    }

}
