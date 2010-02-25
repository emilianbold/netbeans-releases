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

package org.netbeans.modules.maven.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.validation.adapters.DialogDescriptorAdapter;
import org.netbeans.api.validation.adapters.NotificationLineSupportAdapter;
import org.netbeans.modules.maven.MavenValidators;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.DialogDescriptor;
import org.openide.LifecycleManager;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class RenameProjectPanel extends javax.swing.JPanel {
    private final NbMavenProjectImpl project;
    private ValidationGroup vg;
    private NotificationLineSupport nls;

    RenameProjectPanel(NbMavenProjectImpl prj) {
        initComponents();
        txtFolder.putClientProperty(ValidationListener.CLIENT_PROP_NAME, NbBundle.getMessage(RenameProjectPanel.class, "NAME_Folder"));
        txtArtifactId.putClientProperty(ValidationListener.CLIENT_PROP_NAME, NbBundle.getMessage(RenameProjectPanel.class, "NAME_Artifact"));
        this.project = prj;
        final String folder = project.getProjectDirectory().getNameExt();
        txtFolder.setText(folder);
        //load values..
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                MavenProject prj = project.getOriginalMavenProject();
                final String dn = prj.getName();
                final String artId = prj.getArtifactId();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        txtArtifactId.setText(artId);
                        txtDisplayName.setText(dn);
                        lblRename.setText(NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.lblRename.text2", dn));
                    }
                });
            }
        });
    }

    void createValidations(DialogDescriptor dd) {
        nls = dd.createNotificationLineSupport();
        vg = ValidationGroup.create(new NotificationLineSupportAdapter(nls), new DialogDescriptorAdapter(dd));
        vg.add(txtFolder,
                new OptionalValidator(cbFolder,
                    Validators.merge(true,
                        Validators.REQUIRE_NON_EMPTY_STRING,
                        Validators.REQUIRE_VALID_FILENAME,
                        new FileNameExists(FileUtil.toFile(project.getProjectDirectory().getParent()))
                    )
                ));
        vg.add(txtArtifactId,
                new OptionalValidator(cbArtifactId,
                        MavenValidators.createArtifactIdValidators()
                ));
        checkEnablement();
    }


    private void checkEnablement() {
        txtArtifactId.setEnabled(cbArtifactId.isSelected());
        txtDisplayName.setEnabled(cbDisplayName.isSelected());
        txtFolder.setEnabled(cbFolder.isSelected());
        taWarning.setText(cbFolder.isSelected() ? NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.taWarning.text") : "");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblRename = new javax.swing.JLabel();
        cbDisplayName = new javax.swing.JCheckBox();
        txtDisplayName = new javax.swing.JTextField();
        cbArtifactId = new javax.swing.JCheckBox();
        txtArtifactId = new javax.swing.JTextField();
        cbFolder = new javax.swing.JCheckBox();
        txtFolder = new javax.swing.JTextField();
        taWarning = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(lblRename, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.lblRename.text")); // NOI18N

        cbDisplayName.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbDisplayName, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbDisplayName.text")); // NOI18N
        cbDisplayName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDisplayNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbArtifactId, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbArtifactId.text")); // NOI18N
        cbArtifactId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbArtifactIdActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbFolder, org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.cbFolder.text")); // NOI18N
        cbFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFolderActionPerformed(evt);
            }
        });

        taWarning.setColumns(20);
        taWarning.setEditable(false);
        taWarning.setForeground(UIManager.getColor("nb.errorForeground"));
        taWarning.setLineWrap(true);
        taWarning.setRows(5);
        taWarning.setText(org.openide.util.NbBundle.getMessage(RenameProjectPanel.class, "RenameProjectPanel.taWarning.text")); // NOI18N
        taWarning.setWrapStyleWord(true);
        taWarning.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblRename)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(taWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 395, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(cbArtifactId)
                                    .add(cbDisplayName)
                                    .add(cbFolder))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(txtDisplayName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                    .add(txtFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblRename)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbDisplayName)
                    .add(txtDisplayName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbArtifactId)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbFolder)
                    .add(txtFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(taWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbDisplayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDisplayNameActionPerformed
        vg.validateAll();
        checkEnablement();
    }//GEN-LAST:event_cbDisplayNameActionPerformed

    private void cbArtifactIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbArtifactIdActionPerformed
        vg.validateAll();
        checkEnablement();
    }//GEN-LAST:event_cbArtifactIdActionPerformed

    private void cbFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFolderActionPerformed
        vg.validateAll();
        checkEnablement();
    }//GEN-LAST:event_cbFolderActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbArtifactId;
    private javax.swing.JCheckBox cbDisplayName;
    private javax.swing.JCheckBox cbFolder;
    private javax.swing.JLabel lblRename;
    private javax.swing.JTextArea taWarning;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JTextField txtFolder;
    // End of variables declaration//GEN-END:variables

    void renameProject() {
        final boolean artId = cbArtifactId.isSelected();
        final boolean dname = cbDisplayName.isSelected();
        final boolean folder = cbFolder.isSelected();
        final String newArtId = txtArtifactId.getText().trim();
        final String newDname = txtDisplayName.getText().trim();
        final String newFolder = txtFolder.getText().trim();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                List<ModelOperation<POMModel>> opers = new ArrayList<ModelOperation<POMModel>>();
                if (artId) {
                    opers.add(new ArtIdOperation(newArtId));
                }
                if (dname) {
                    opers.add(new DNameOperation(newDname));
                }
                FileObject pomFO = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                Utilities.performPOMModelOperations(pomFO, opers);
                if (folder) {
                    final ProgressHandle handle = ProgressHandleFactory.createHandle("Rename Project" + dname);
                    //#76559
                    handle.start(MAX_WORK);
                    try {
                        checkParentProject(project.getProjectDirectory().getNameExt(), newFolder);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        doMoveProject(handle, project, newFolder, project.getProjectDirectory().getParent());
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        handle.finish();
                    }
                }
            }
        });
    }


    private class ArtIdOperation implements ModelOperation<POMModel> {
        private final String artifactId;
        ArtIdOperation(String art) {
            artifactId = art;
        }
        @Override
        public void performOperation(POMModel model) {
            model.getProject().setArtifactId(artifactId);
        }
    }

    private class DNameOperation implements ModelOperation<POMModel> {
        private final String name;
        DNameOperation(String nm) {
            name = nm;
        }
        @Override
        public void performOperation(POMModel model) {
            model.getProject().setName(name);
        }
    }

    private void checkParentProject(final String newName, final String oldName) throws IOException {
        FileObject fo = project.getProjectDirectory().getParent();
        Project possibleParent = ProjectManager.getDefault().findProject(fo);
        if (possibleParent != null) {
            final NbMavenProjectImpl par = possibleParent.getLookup().lookup(NbMavenProjectImpl.class);
            if (par != null) {
                FileObject pomFO = par.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        List<String> modules = model.getProject().getModules();
                        if (modules != null && modules.contains(oldName)) {
                            //delete/add module from/to parent..
                            model.getProject().removeModule(oldName);
                            model.getProject().addModule(newName);
                        }
                    }
                };
                Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
            }
        }

    }




//--- copied from org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation

    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int    MAX_WORK = 100;

    /*package private for tests*/ static void doMoveProject(ProgressHandle handle, Project project, String nueFolderName, FileObject newTarget) throws Exception {
        boolean originalOK = true;
        Project main    = OpenProjects.getDefault().getMainProject();
        boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
	FileObject target = null;

        try {

            int totalWork = MAX_WORK;
            double currentWorkDone = 0;

            handle.progress((int) currentWorkDone);

            close(project);

            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));

            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toMoveList = new ArrayList<FileObject>();
            for (FileObject child : projectDirectory.getChildren()) {
                if (child.isValid()) {
                    toMoveList.add(child);
                }
            }

            double workPerFileAndOperation = (totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK) / toMoveList.size()) / 2;

            target = newTarget.createFolder(nueFolderName);

            for (FileObject toCopy : toMoveList) {
                doCopy(project, toCopy, target);

                int lastWorkDone = (int) currentWorkDone;

                currentWorkDone += workPerFileAndOperation;

                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }

            originalOK = false;

            for (FileObject toCopy : toMoveList) {
                doDelete(project, toCopy);

                int lastWorkDone = (int) currentWorkDone;

                currentWorkDone += workPerFileAndOperation;

                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }

            if (projectDirectory.getChildren().length == 0) {
                projectDirectory.delete();
            }

            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);

            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));

            assert nue != null;

            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));

            ProjectManager.getDefault().saveProject(nue);

            open(nue, wasMain);

            handle.progress(totalWork);
        } catch (Exception e) {
            if (originalOK) {
                open(project, wasMain);
            } else {
		assert target != null;

		//#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
		ProjectManager.getDefault().clearNonProjectCache();
		Project nue = ProjectManager.getDefault().findProject(target);

		assert nue != null;

                open(nue, wasMain);
            }
//            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(RenameProjectPanel.class, errorKey, e.getLocalizedMessage()));
            throw e;
        }
    }

    private static boolean doDelete(Project original, FileObject toDelete) throws IOException {
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(toDelete).getProjectDirectory())) {
            return false;
        }

        if (toDelete.isFolder()) {
            boolean delete = true;

            for (FileObject kid : toDelete.getChildren()) {
                delete &= doDelete(original, kid);
            }

            if (delete) {
                //#83958
                DataFolder.findFolder(toDelete).delete();
            }

            return delete;
        } else {
            assert toDelete.isData();
            try {
                //#83958
                DataObject dobj = DataObject.find(toDelete);
                dobj.delete();
            } catch (DataObjectNotFoundException ex) {
                //In case of MultiDataObjects the file may be laready deleted
                if (toDelete.isValid()) {
                    toDelete.delete();
                }
            }
            return true;
        }
    }

    private static void doCopy(Project original, FileObject from, FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return ;
        }

        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(from).getProjectDirectory())) {
            return ;
        }

        //#109580
        if (SharabilityQuery.getSharability(FileUtil.toFile(from)) == SharabilityQuery.NOT_SHARABLE) {
            return;
        }

        if (from.isFolder()) {
            FileObject copy = toParent.createFolder(from.getNameExt());
            for (FileObject kid : from.getChildren()) {
                doCopy(original, kid, copy);
            }
        } else {
            assert from.isData();
            FileObject target = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
    }

    private static void close(final Project prj) {
        LifecycleManager.getDefault().saveAll();
        OpenProjects.getDefault().close(new Project[] {prj});
    }

    private static void open(final Project prj, final boolean setAsMain) {
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        if (setAsMain) {
            OpenProjects.getDefault().setMainProject(prj);
        }
    }

    private class OptionalValidator implements Validator<String> {
        private final JCheckBox checkbox;
        private final Validator<String> delegate;

        public OptionalValidator(JCheckBox cb, Validator<String> validator) {
            checkbox = cb;
            delegate = validator;
        }
        
        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (checkbox.isSelected()) {
                return delegate.validate(problems, compName, model);
            }
            return true;
        }
    }

    private class FileNameExists implements Validator<String> {
        private final File parent;

        public FileNameExists(File parent) {
            assert parent.isDirectory() && parent.exists();
            this.parent = parent;
}

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            File newDir = new File(parent, model);
            if (newDir.exists()) {
                problems.add("Folder with name '" + model + "' already exists.", Severity.FATAL);
                return false;
            }
            return true;
        }
    }
}
