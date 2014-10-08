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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.api.util.ValidationUtilities;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Project location for existing project.
 */
public class ExistingClientSideProject extends JPanel {

    private static final long serialVersionUID = -4683211573157747L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final boolean library;

    // @GuardedBy("EDT")
    boolean fireChanges = true;
    // @GuardedBy("EDT")
    String lastFolder = ""; // NOI18N
    // @GuardedBy("EDT")
    String lastProjectName = ""; // NOI18N
    volatile String testDir = null;


    public ExistingClientSideProject(boolean library) {
        this.library = library;

        initComponents();
        initFolder();
        initProjectName();
        initProjectDirectory();
    }

    private void initFolder() {
        Mnemonics.setLocalizedText(folderLabel, getFolderLabel());
        folderTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectName();
                updateProjectDirectory();
                lastFolder = getFolder();
                detectClientSideProject(lastFolder);
                fireChanges = true;
            }
        }));
    }

    private void initProjectName() {
        projectNameTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectDirectoryName();
                lastProjectName = getProjectName();
                fireChanges = true;
            }
        }));
    }

    private void initProjectDirectory() {
        projectDirectoryTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
    }

    public String getFolder() {
        return folderTextField.getText().trim();
    }

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getProjectDirectory() {
        return projectDirectoryTextField.getText().trim();
    }

    public String getTestDir() {
        return testDir;
    }

    public final void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public final void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getErrorMessage() {
        String error = validateFolder();
        if (error != null) {
            return error;
        }
        error = validateProjectName();
        if (error != null) {
            return error;
        }
        error = validateProjectDirectory();
        if (error != null) {
            return error;
        }
        return null;
    }

    @NbBundle.Messages({
        "# label with mnemonic",
        "ExistingClientSideProject.folder.label.project=&Site Root:",
        "# label with mnemonic",
        "ExistingClientSideProject.folder.label.library=&Source Folder:",
    })
    private String getFolderLabel() {
        if (library) {
            return Bundle.ExistingClientSideProject_folder_label_library();
        }
        return Bundle.ExistingClientSideProject_folder_label_project();
    }

    @NbBundle.Messages({
        "ExistingClientSideProject.folder.name.project=Site Root",
        "ExistingClientSideProject.folder.name.library=Source folder",
    })
    private String getFolderName() {
        if (library) {
            return Bundle.ExistingClientSideProject_folder_name_library();
        }
        return Bundle.ExistingClientSideProject_folder_name_project();
    }

    @NbBundle.Messages({
        "# {0} - folder name",
        "ExistingClientSideProject.error.folder.empty={0} must be selected.",
        "# {0} - folder name",
        "ExistingClientSideProject.error.folder.invalid={0} is not a valid path.",
        "# {0} - folder name",
        "ExistingClientSideProject.error.folder.nbproject={0} is already NetBeans project (maybe only in memory).",
    })
    private String validateFolder() {
        String folder = getFolder();
        if (folder.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_folder_empty(getFolderName());
        }
        File folderDir = FileUtil.normalizeFile(new File(folder).getAbsoluteFile());
        if (!folderDir.isDirectory()) {
            return Bundle.ExistingClientSideProject_error_folder_invalid(getFolderName());
        } else if (ClientSideProjectUtilities.isProject(folderDir)) {
            return Bundle.ExistingClientSideProject_error_folder_nbproject(getFolderName());
        }
        return null;
    }

    @NbBundle.Messages("ExistingClientSideProject.error.name.empty=Project name must be provided.")
    private String validateProjectName() {
        String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_name_empty();
        }
        return null;
    }

    @NbBundle.Messages({
        "ExistingClientSideProject.error.projectDirectory.invalid=Project directory is not a valid path.",
        "ExistingClientSideProject.error.projectDirectory.empty=Project directory must be selected.",
        "ExistingClientSideProject.error.projectDirectory.alreadyProject=Project directory is already NetBeans project (maybe only in memory).",
        "ExistingClientSideProject.error.projectDirectory.notWritable=Project directory cannot be created."
    })
    private String validateProjectDirectory() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_projectDirectory_empty();
        }
        File projDir = FileUtil.normalizeFile(new File(projectDirectory).getAbsoluteFile());
        if (ClientSideProjectUtilities.isProject(projDir)) {
            return Bundle.ExistingClientSideProject_error_projectDirectory_alreadyProject();
        }
        File folder = FileUtil.normalizeFile(new File(getFolder()).getAbsoluteFile());
        if (projDir.isDirectory()) {
            if (projDir.equals(folder)) {
                // same as folder, do nothing
                return null;
            }
            // XXX ideally warn about possibly non-empty directory
        } else {
            // not existing directory
            if (!ValidationUtilities.isValidFilename(projDir)) {
                return Bundle.ExistingClientSideProject_error_projectDirectory_invalid();
            }
            File existingParent = projDir;
            while (existingParent != null && !existingParent.exists()) {
                existingParent = existingParent.getParentFile();
            }
            if (existingParent == null || !existingParent.canWrite()) {
                return Bundle.ExistingClientSideProject_error_projectDirectory_notWritable();
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void updateProjectName() {
        projectNameTextField.setText(new File(getFolder()).getName());
    }

    void updateProjectDirectory() {
        assert EventQueue.isDispatchThread();
        String projectDirectory = getProjectDirectory();
        if (!lastFolder.isEmpty() && projectDirectory.equals(lastFolder)) {
            // project directory is folder => do nothing
            return;
        }
        projectDirectoryTextField.setText(getFolder());
    }

    void updateProjectDirectoryName() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.equals(getFolder())) {
            // project directory is folder => do nothing
            return;
        }
        if (!lastProjectName.isEmpty()
                && !projectDirectory.equals(lastProjectName)
                && projectDirectory.endsWith(lastProjectName)) {
            // yes, project directory follows project name
            String newProjDir = projectDirectory.substring(0, projectDirectory.length() - lastProjectName.length()) + getProjectName();
            projectDirectoryTextField.setText(newProjDir);
        }
    }

    void detectClientSideProject(String folder) {
        if (library) {
            // noop
            return;
        }
        ClientSideProjectDetector detector = new ClientSideProjectDetector(new File(folder));
        if (detector.detected()) {
            projectNameTextField.setText(detector.getName());
            projectDirectoryTextField.setText(detector.getProjectDirPath());
            testDir = detector.getTestDirPath();
        } else {
            resetDetectedValues();
        }
    }

    void resetDetectedValues() {
        testDir = null;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        folderLabel = new JLabel();
        folderTextField = new JTextField();
        folderBrowseButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectDirectoryLabel = new JLabel();
        projectDirectoryTextField = new JTextField();
        projectDirectoryBrowseButton = new JButton();

        folderLabel.setLabelFor(folderTextField);
        Mnemonics.setLocalizedText(folderLabel, "FOLDER:"); // NOI18N

        Mnemonics.setLocalizedText(folderBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.folderBrowseButton.text")); // NOI18N
        folderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                folderBrowseButtonActionPerformed(evt);
            }
        });

        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectNameLabel.text")); // NOI18N

        projectDirectoryLabel.setLabelFor(projectDirectoryTextField);
        Mnemonics.setLocalizedText(projectDirectoryLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectDirectoryLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(projectDirectoryBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.projectDirectoryBrowseButton.text")); // NOI18N
        projectDirectoryBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectDirectoryBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(projectDirectoryLabel)
                    .addComponent(folderLabel)
                    .addComponent(projectNameLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(folderTextField)
                            .addComponent(projectDirectoryTextField))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(folderBrowseButton, Alignment.TRAILING)
                            .addComponent(projectDirectoryBrowseButton, Alignment.TRAILING)))
                    .addComponent(projectNameTextField)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {folderBrowseButton, projectDirectoryBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(folderBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(projectNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectNameLabel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(projectDirectoryLabel)
                    .addComponent(projectDirectoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectDirectoryBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({
        "# {0} - folder name",
        "ExistingClientSideProject.folder.dialog.title=Select {0}",
    })
    private void folderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_folderBrowseButtonActionPerformed
        File folder = browseFile(".folder", Bundle.ExistingClientSideProject_folder_dialog_title(getFolderName()), //NOI18N
                getFolder());
        if (folder != null) {
            folderTextField.setText(FileUtil.normalizeFile(folder).getAbsolutePath());
        }
    }//GEN-LAST:event_folderBrowseButtonActionPerformed

    @NbBundle.Messages("ExistingClientSideProject.projectDirectory.dialog.title=Select Project Directory")
    private void projectDirectoryBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectDirectoryBrowseButtonActionPerformed
        File projectDirectory = browseFile(".projectDirectory", Bundle.ExistingClientSideProject_projectDirectory_dialog_title(), // NOI18N
                getProjectDirectory());
        if (projectDirectory != null) {
            File result = FileUtil.normalizeFile(projectDirectory);
            File[] children = result.listFiles();
            if (children != null && children.length > 0) {
                // non-empty dir is not allowed, preselect directory with project name
                result = new File(result, getProjectName());
            }
            projectDirectoryTextField.setText(result.getAbsolutePath());
        }
    }//GEN-LAST:event_projectDirectoryBrowseButtonActionPerformed

    private File browseFile(String dirKey, String title, String currentDirectory) {
        File workDir = null;
        if (currentDirectory != null && !currentDirectory.isEmpty()) {
            File currDir = new File(currentDirectory);
            if (currDir.isDirectory()) {
                workDir = currDir;
            }
        }
        FileChooserBuilder builder = new FileChooserBuilder(NewClientSideProject.class.getName() + dirKey)
                .setTitle(title)
                .setDirectoriesOnly(true);
        if (workDir != null) {
            builder.setDefaultWorkingDirectory(workDir)
                    .forceUseOfDefaultWorkingDirectory(true);
        }
        return builder.showOpenDialog();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton folderBrowseButton;
    private JLabel folderLabel;
    private JTextField folderTextField;
    private JButton projectDirectoryBrowseButton;
    private JLabel projectDirectoryLabel;
    private JTextField projectDirectoryTextField;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        private final Runnable task;


        public DefaultDocumentListener() {
            this(null);
        }

        public DefaultDocumentListener(Runnable task) {
            this.task = task;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            assert EventQueue.isDispatchThread();
            if (task != null) {
                task.run();
            }
            if (fireChanges) {
                fireChange();
            }
        }

    }

}
