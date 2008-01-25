/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.subversion.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class CheckoutCompleted implements ActionListener {

    private final File workingFolder;
    private final boolean openProject;
    private String[] checkedOutFolders;

    private CheckoutCompletedPanel panel;
    private Dialog dialog;
    private Project projectToBeOpened;

    public CheckoutCompleted(File workingFolder, String[] checkedOutFolders, boolean openProject) {
        this.openProject = openProject;
        this.checkedOutFolders = checkedOutFolders;
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(SvnProgressSupport support) {

        List<Project> checkedOutProjects = new LinkedList<Project>();
        File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
        // checkout creates new folders and cache must be aware of them
        SvnUtils.refreshParents(normalizedWorkingFolder);
        FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
        if (fo != null) {
            for (int i = 0; i < checkedOutFolders.length; i++) {
                if (support != null && support.isCanceled()) {
                    return;
                }
                String module = checkedOutFolders[i];
                if (".".equals(module)) {                   // NOI18N
                    checkedOutProjects = ProjectUtilities.scanForProjects(fo);
                    break;
                } else {
                    FileObject subfolder = fo.getFileObject(module);
                    if (subfolder != null) {
                        checkedOutProjects.addAll(ProjectUtilities.scanForProjects(subfolder));
                    }
                }
            }
        }

        panel = new CheckoutCompletedPanel();
        panel.openButton.addActionListener(this);
        panel.createButton.addActionListener(this);
        panel.closeButton.addActionListener(this);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.againCheckBox.setVisible(openProject == false);
        String title = NbBundle.getMessage(CheckoutAction.class, "BK3008"); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        descriptor.setModal(true);

        // move buttons from dialog to descriptor
        panel.remove(panel.openButton);
        panel.remove(panel.createButton);
        panel.remove(panel.closeButton);

        Object[] options = null;
        if (checkedOutProjects.size() > 1) {
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3009", new Integer(checkedOutProjects.size()));   // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[]{panel.openButton, panel.closeButton};
        } else if (checkedOutProjects.size() == 1) {
            Project project = (Project) checkedOutProjects.iterator().next();
            projectToBeOpened = project;
            ProjectInformation projectInformation = ProjectUtils.getInformation(project);
            String projectName = projectInformation.getDisplayName();
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3011", projectName);                              // NOI18N
            panel.jLabel1.setText(msg);
            panel.openButton.setText(NbBundle.getMessage(CheckoutAction.class, "BK3012"));                              // NOI18N
            options = new Object[]{panel.openButton, panel.closeButton};
        } else {
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3010");                                           // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[]{panel.createButton, panel.closeButton};
        }

        descriptor.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
        descriptor.setOptions(options);
        descriptor.setClosingOptions(options);
        descriptor.setHelpCtx(new HelpCtx(CheckoutCompletedPanel.class));
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CheckoutAction.class, "ACSD_CheckoutCompleted_Dialog")); // NOI18N
        if (support != null && support.isCanceled()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                dialog.setVisible(true);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        dialog.setVisible(false);
        if (panel.openButton.equals(src)) {
            // show project chooser
            if (projectToBeOpened == null) {
                JFileChooser chooser = ProjectChooser.projectChooser();
                chooser.setCurrentDirectory(workingFolder);
                chooser.setMultiSelectionEnabled(true);
                chooser.showOpenDialog(dialog);
                File[] projectDirs = chooser.getSelectedFiles();
                for (int i = 0; i < projectDirs.length; i++) {
                    File projectDir = projectDirs[i];
                    FileObject projectFolder = FileUtil.toFileObject(projectDir);
                    if (projectFolder != null) {
                        try {
                            Project p = ProjectManager.getDefault().findProject(projectFolder);
                            if (p != null) {
                                openProject(p);
                            }
                        } catch (IOException e1) {                            
                            Subversion.LOG.log(Level.SEVERE, NbBundle.getMessage(CheckoutAction.class, "BK1014", projectFolder), e1);  // NOI18N
                        }
                    }
                }
            } else {
                if (projectToBeOpened == null) {
                    return;
                }
                openProject(projectToBeOpened);
            }
        } else if (panel.createButton.equals(src)) {
            ProjectUtilities.newProjectWizard(workingFolder);
        }
    }

    private void openProject(Project p) {
        Project[] projects = new Project[]{p};
        OpenProjects.getDefault().open(projects, false);

        // set as main project and expand
        OpenProjects.getDefault().setMainProject(p);        
        ProjectUtilities.selectAndExpandProject(p);
    }
}
