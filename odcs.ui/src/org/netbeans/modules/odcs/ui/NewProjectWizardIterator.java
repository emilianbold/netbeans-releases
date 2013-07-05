/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui;

import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.odcs.ui.Bundle.*;
import org.netbeans.modules.odcs.ui.spi.VCSAccessor;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milan Kubec
 */
public class NewProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private transient int index;
    private File[] initialDirectories;

    public static final String PROP_PRJ_NAME = "projectTitle"; // NOI18N
    public static final String PROP_PRJ_DESC = "projectDescription"; // NOI18N
    public static final String PROP_PRJ_ACCESSIBILITY = "projectAccessibility"; //NOI18N
    public static final String PROP_PRJ_WIKI = "projectWikiStyle"; //NOI18N
    public static final String PROP_SCM_TYPE = "projectSCMType"; // NOI18N
    public static final String PROP_SCM_NAME = "projectSCMName"; // NOI18N
    public static final String PROP_SCM_URL = "projectSCMUrl"; // NOI18N
    public static final String PROP_SCM_LOCAL = "projectSCMLocal"; // NOI18N
    public static final String PROP_SCM_PREVIEW = "projectSCMPreview"; // NOI18N
    public static final String PROP_FOLDERS_TO_SHARE = "projectFoldersToShare"; // NOI18N

    public static final String PROP_EXC_ERR_MSG = "exceptionErrorMessage"; // NOI18N

    // special values when no features are created
    public static final String NO_REPO = "none"; // NOI18N
    public static final String NO_ISSUES = "none"; // NOI18N
    private ODCSServer server;

    private static final Logger logger = Logger.getLogger(NewProjectWizardIterator.class.getName());

    NewProjectWizardIterator(File[] initialDirs, ODCSServer server) {
        this.initialDirectories = initialDirs != null ? initialDirs : new File[]{};
        this.server = server;
    }

    @Messages({"NewProjectWizardIterator.NoGitClient=Git client is not available",
        "# {0} - detailed message",
        "NewProject.progress.projectCreationFailed=Project creation failed: {0}",
        "NewProject.progress.creatingProject=Creating project...",
        "NewProject.progress.creatingTaskRepository=Creating task repository...",
        "NewProject.progress.repositoryCheckout=Initializing local repository..."})
    @Override
    public Set<CreatedProjectInfo> instantiate(ProgressHandle handle) throws IOException {

        handle.start(3);

        String newPrjTitle = (String) wizard.getProperty(PROP_PRJ_NAME);
        String newPrjDesc = (String) wizard.getProperty(PROP_PRJ_DESC);
        String newPrjAccessibility = (String) wizard.getProperty(PROP_PRJ_ACCESSIBILITY);
        String newPrjWikiStyle = (String) wizard.getProperty(PROP_PRJ_WIKI);

        String newPrjScmType = (String) wizard.getProperty(PROP_SCM_TYPE);
        String newPrjScmLocal = (String) wizard.getProperty(PROP_SCM_LOCAL);

        List<SharedItem> sharedItems = (List<SharedItem>) wizard.getProperty(PROP_FOLDERS_TO_SHARE);

        VCSAccessor.RepositoryInitializer repoInitializer = VCSAccessor.getDefault().getRepositoryInitializer(newPrjScmType);
        if (repoInitializer == null) {
            // git unavailable
            ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG,
                    NewProjectWizardIterator_NoGitClient());
            throw new IOException("Git client is not available"); //NOI18N
        }

        // Create project
        ODCSProject project = null;
        try {
            handle.progress(NewProject_progress_creatingProject(), 1);

            logger.log(Level.FINE, "Creating ODCS Project - Title: {0}, Description: {1}, Accessibility: {2}, Wiki: {3}", //NOI18N
                    new Object[]{newPrjTitle, newPrjDesc, newPrjAccessibility, newPrjWikiStyle});
            project = server.createProject(newPrjTitle, newPrjDesc, newPrjAccessibility, newPrjWikiStyle);
        } catch (ODCSException ex) {
            String errorMessage = Utils.parseKnownMessage(ex);
            errorMessage = NewProject_progress_projectCreationFailed(errorMessage == null ? ex.getMessage() : errorMessage);
            ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG, errorMessage);
            throw new IOException(errorMessage, ex);
        }

        // Create feature - SCM repository
        boolean repoCreated = project != null;
        
        if (repoCreated) {
            handle.progress(NewProject_progress_creatingTaskRepository(), 2);
            int i = 30;
            while (!project.hasTasks() && i-- > 0) {
                try {
                    Thread.sleep(2000); // any other way, is there a blocking call in the API?
                } catch (InterruptedException ex) {
                }
                project = server.getProject(project.getId(), true);
            }
        }
        
        // After the repository is created it must be checked out
        if (repoCreated) {
            try {
                String repositoryUrl = null;
                Collection<ScmRepository> repositories = project.getRepositories();
                if (repositories != null && !repositories.isEmpty()) {
                    repositoryUrl = repositories.iterator().next().getUrl();
                }
                if (repositoryUrl != null) {
                    handle.progress(NewProject_progress_repositoryCheckout(), 3);
                    logger.log(Level.FINE, "Checking out repository - Repository URL: {0}, Local Folder: {1}, Service: Git", //NOI18N
                            new Object[]{repositoryUrl, newPrjScmLocal});
                    PasswordAuthentication passwdAuth = server.getPasswordAuthentication();
                    if (passwdAuth != null) {
                        final File localScmRoot = new File(newPrjScmLocal);
                        boolean inPlaceRepository = isCommonParent(sharedItems, newPrjScmLocal);
                        repoInitializer.initialize(localScmRoot, repositoryUrl, passwdAuth);
                        if (!inPlaceRepository) {
                            copySharedItems(sharedItems, localScmRoot);
                            // if shared items contain projects, those projects need to be closed and open from new location
                            File[] oldLoc = new File[sharedItems.size()];
                            File[] newLoc = new File[sharedItems.size()];
                            int i = 0;
                            for (SharedItem item : sharedItems) {
                                oldLoc[i] = item.getRoot();
                                newLoc[i] = new File(localScmRoot, item.getRoot().getName());
                                i++;
                            }
                            ProjectServices projects  = Lookup.getDefault().lookup(ProjectServices.class);
                            projects.reopenProjectsFromNewLocation(oldLoc, newLoc);
                        }
                    } else {
                        // user not logged in, do nothing
                    }
                }
            } catch (ODCSException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Set<CreatedProjectInfo> set = new HashSet<CreatedProjectInfo>();
        set.add(new CreatedProjectInfo(project, newPrjScmLocal));
//        // Open the project in Dashboard
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run () {
//                TeamUIUtils.activateTeamDashboard();
//                uiServer.getDashboard().addProject(projectHandle, true, true);
//            }
//        });

        handle.finish();

        return set;
        
    }

    /**
     * Copy all items (projects, folders) to be shared on Kenai to a new directory, the SCM root.
     *
     * @param sharedItems
     * @param localScmRoot
     */
    private void copySharedItems(List<SharedItem> sharedItems, File localScmRoot) throws IOException {
        localScmRoot.mkdirs();
        FileObject dest = FileUtil.toFileObject(localScmRoot);
        for (SharedItem item : sharedItems) {
            FileObject root = FileUtil.toFileObject(item.getRoot());
            copy(root, dest);
        }
    }

    /**
     * Copies the given file/folder into the given destination folder.
     *
     * @param src source file/folder to copy
     * @param dest destination folder
     */
    private void copy(FileObject src, FileObject destFolder) throws IOException {
        if (src.isFolder()) {
            FileObject srcCopy = destFolder.getFileObject(src.getNameExt());
            if (srcCopy == null) {
                srcCopy = destFolder.createFolder(src.getNameExt());
            }
            FileObject [] files = src.getChildren();
            for (FileObject file : files) {
                copy(file, srcCopy);
            }
        } else {
            src.copy(destFolder, src.getName(), src.getExt());
        }
    }

    static File getCommonParent(List<SharedItem> sharedItems) {
        File commonParent = null;
        if (sharedItems.size() > 0) {
            commonParent = sharedItems.get(0).getRoot().getParentFile();
            for (SharedItem item : sharedItems) {
                if (!commonParent.equals(item.getRoot().getParentFile())) {
                    commonParent = null;
                    break;
                }
            }
            if (commonParent != null && commonParent.list().length != sharedItems.size()) {
                commonParent = null;
            }
        }
        return commonParent;
    }

    static boolean isCommonParent(List<SharedItem> sharedItems, String newPrjScmLocal) {
        File commonParent = new File(newPrjScmLocal);
        for (SharedItem item : sharedItems) {
            if (!commonParent.equals(item.getRoot().getParentFile())) {
                return false;
            }
        }

        if (commonParent.list() == null || commonParent.list().length != sharedItems.size()) return false;
        return true;
    }

    @Override
    public Set<?> instantiate() throws IOException {
        assert false;
        return null;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.panels = getPanels();
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        // XXX set properties to null ???
    }

    @Override
    public Panel current() {
        return panels[index];
    }

    @Messages("NewProjectWizardIterator.name=New Team Project Wizard")
    @Override
    public String name() {
        return NewProjectWizardIterator_name();
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) { }

    @Override
    public void removeChangeListener(ChangeListener l) { }

    ODCSServer getServer () {
        return server;
    }

    public void setServer (ODCSServer server) {
        this.server = server;
    }

    // ----------

    public static class CreatedProjectInfo {

        public ODCSProject project;
        public String localRepoPath;

        public CreatedProjectInfo(ODCSProject prj, String pth) {
            project = prj;
            localRepoPath = pth;
        }

    }

    // ----------

    @Messages("NewProjectWizardIterator.GitRepository=Git Repository")
    private String getScmDisplayName () {
        return NewProjectWizardIterator_GitRepository();
    }

    @Messages("NewProjectWizardIterator.GitSourceCodeRepository=Git Source Code Repository")
    private String getScmDescription () {
        return NewProjectWizardIterator_GitSourceCodeRepository();
    }

    // ----------

    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    private WizardDescriptor.Panel[] createPanels() {
        List<SharedItem> initialItems = new ArrayList<SharedItem>();
        for (File d : initialDirectories) {
            initialItems.add(new SharedItem(d));
        }
        return new WizardDescriptor.Panel[]{
                    new NameWizardPanel(this, initialItems),
                    new SourceAndIssuesWizardPanel(this, initialItems),
                    new SummaryWizardPanel(this)
                };
    }

    public static class SharedItem {

        private final File      root;

        public SharedItem(File file) {
            this.root = file;
        }

        @Override
        public String toString() {
            return root.getName();
        }

        public File getRoot() {
            return root;
        }
    }

}
