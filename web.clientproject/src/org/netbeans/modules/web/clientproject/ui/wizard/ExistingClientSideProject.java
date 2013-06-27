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

    // @GuardedBy("EDT")
    boolean fireChanges = true;
    // @GuardedBy("EDT")
    String lastSiteRoot = ""; // NOI18N
    // @GuardedBy("EDT")
    String lastProjectName = ""; // NOI18N
    volatile String configDir = null;
    volatile String testDir = null;


    public ExistingClientSideProject() {
        initComponents();
        initSiteRoot();
        initProjectName();
        initProjectDirectory();
    }

    private void initSiteRoot() {
        siteRootTextField.getDocument().addDocumentListener(new DefaultDocumentListener(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                fireChanges = false;
                updateProjectName();
                updateProjectDirectory();
                lastSiteRoot = getSiteRoot();
                detectClientSideProject(lastSiteRoot);
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

    public String getSiteRoot() {
        return siteRootTextField.getText().trim();
    }

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getProjectDirectory() {
        return projectDirectoryTextField.getText().trim();
    }

    public String getConfigDir() {
        return configDir;
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
        String error = validateSiteRoot();
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
        "ExistingClientSideProject.error.siteRoot.empty=Site root must be selected.",
        "ExistingClientSideProject.error.siteRoot.invalid=Site root is not a valid path.",
        "ExistingClientSideProject.error.siteRoot.nbproject=Site root is already NetBeans project (maybe only in memory)."
    })
    private String validateSiteRoot() {
        String siteRoot = getSiteRoot();
        if (siteRoot.isEmpty()) {
            return Bundle.ExistingClientSideProject_error_siteRoot_empty();
        }
        File siteRootDir = FileUtil.normalizeFile(new File(siteRoot).getAbsoluteFile());
        if (!siteRootDir.isDirectory()) {
            return Bundle.ExistingClientSideProject_error_siteRoot_invalid();
        } else if (ClientSideProjectUtilities.isProject(siteRootDir)) {
            return Bundle.ExistingClientSideProject_error_siteRoot_nbproject();
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
        File siteRoot = FileUtil.normalizeFile(new File(getSiteRoot()).getAbsoluteFile());
        if (projDir.isDirectory()) {
            if (projDir.equals(siteRoot)) {
                // same as site root, do nothing
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
        projectNameTextField.setText(new File(getSiteRoot()).getName());
    }

    void updateProjectDirectory() {
        assert EventQueue.isDispatchThread();
        String projectDirectory = getProjectDirectory();
        if (!lastSiteRoot.isEmpty() && projectDirectory.equals(lastSiteRoot)) {
            // project directory in site root => do nothing
            return;
        }
        projectDirectoryTextField.setText(getSiteRoot());
    }

    void updateProjectDirectoryName() {
        String projectDirectory = getProjectDirectory();
        if (projectDirectory.equals(getSiteRoot())) {
            // project directory in site root => do nothing
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

    void detectClientSideProject(String siteRoot) {
        ClientSideProjectDetector detector = new ClientSideProjectDetector(new File(siteRoot));
        if (detector.detected()) {
            projectNameTextField.setText(detector.getName());
            projectDirectoryTextField.setText(detector.getProjectDirPath());
            configDir = detector.getConfigDirPath();
            testDir = detector.getTestDirPath();
        } else {
            resetDetectedValues();
        }
    }

    void resetDetectedValues() {
        configDir = null;
        testDir = null;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        siteRootLabel = new JLabel();
        siteRootTextField = new JTextField();
        siteRootBrowseButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        projectDirectoryLabel = new JLabel();
        projectDirectoryTextField = new JTextField();
        projectDirectoryBrowseButton = new JButton();

        siteRootLabel.setLabelFor(siteRootTextField);
        Mnemonics.setLocalizedText(siteRootLabel, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.siteRootLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(siteRootBrowseButton, NbBundle.getMessage(ExistingClientSideProject.class, "ExistingClientSideProject.siteRootBrowseButton.text")); // NOI18N
        siteRootBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                siteRootBrowseButtonActionPerformed(evt);
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
                    .addComponent(siteRootLabel)
                    .addComponent(projectNameLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(siteRootTextField)
                            .addComponent(projectDirectoryTextField))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(siteRootBrowseButton, Alignment.TRAILING)
                            .addComponent(projectDirectoryBrowseButton, Alignment.TRAILING)))
                    .addComponent(projectNameTextField)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {projectDirectoryBrowseButton, siteRootBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(siteRootLabel)
                    .addComponent(siteRootTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(siteRootBrowseButton))
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

    @NbBundle.Messages("ExistingClientSideProject.siteRoot.dialog.title=Select Site Root")
    private void siteRootBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_siteRootBrowseButtonActionPerformed
        File siteRoot = browseFile(".siteRoot", Bundle.ExistingClientSideProject_siteRoot_dialog_title(), //NOI18N
                getSiteRoot());
        if (siteRoot != null) {
            siteRootTextField.setText(FileUtil.normalizeFile(siteRoot).getAbsolutePath());
        }
    }//GEN-LAST:event_siteRootBrowseButtonActionPerformed

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
    private JButton projectDirectoryBrowseButton;
    private JLabel projectDirectoryLabel;
    private JTextField projectDirectoryTextField;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;
    private JButton siteRootBrowseButton;
    private JLabel siteRootLabel;
    private JTextField siteRootTextField;
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
