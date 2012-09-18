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
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public final class ClientSideProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectWizardIterator.class.getName());

    private final Wizard wizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizardDescriptor;


    private ClientSideProjectWizardIterator(Wizard wizard) {
        assert wizard != null;
        this.wizard = wizard;
    }

    @TemplateRegistration(folder="Project/ClientSide",
            displayName="#ClientSideProjectWizardIterator.newProject.displayName",
            description="../resources/NewClientSideProjectDescription.html",
            iconBase=ClientSideProject.PROJECT_ICON,
            position=100)
    @NbBundle.Messages("ClientSideProjectWizardIterator.newProject.displayName=HTML5 Application")
    public static ClientSideProjectWizardIterator newProject() {
        return new ClientSideProjectWizardIterator(new NewProjectWizard());
    }

    @TemplateRegistration(folder="Project/ClientSide",
            displayName="#ClientSideProjectWizardIterator.existingProject.displayName",
            description="../resources/ExistingClientSideProjectDescription.html",
            iconBase=ClientSideProject.PROJECT_ICON,
            position=200)
    @NbBundle.Messages("ClientSideProjectWizardIterator.existingProject.displayName=HTML5 Application with Existing Sources")
    public static ClientSideProjectWizardIterator existingProject() {
        return new ClientSideProjectWizardIterator(new ExistingProjectWizard());
    }

    @NbBundle.Messages("ClientSideProjectWizardIterator.progress.creatingProject=Creating project")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.ClientSideProjectWizardIterator_progress_creatingProject());
        Set<FileObject> files = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(Wizard.PROJECT_DIRECTORY));
        String name = (String) wizardDescriptor.getProperty(Wizard.NAME);
        if (!dirF.isDirectory() && !dirF.mkdirs()) {
            throw new IOException("Cannot create project directory"); //NOI18N
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        AntProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(dir, name);
        // Always open top dir as a project:
        files.add(dir);

        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        FileObject siteRoot = wizard.instantiate(files, handle, wizardDescriptor, project);

        // index file
        FileObject indexFile = siteRoot.getFileObject("index", "html"); // NOI18N
        if (indexFile != null) {
            files.add(indexFile);
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();
        return files;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("never implemented - use progress one"); //NOI18N
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        index = 0;
        panels = wizard.createPanels();
        // Make sure list of steps is accurate.
        String[] steps = wizard.createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps[i] != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        wizardDescriptor.putProperty(Wizard.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(Wizard.NAME, null);
        wizard.uninitialize(wizardDescriptor);
        panels = null;
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "ClientSideProjectWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.ClientSideProjectWizardIterator_name(Integer.valueOf(index + 1), Integer.valueOf(panels.length));
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    //~ Inner classes

    public interface Wizard {
        String PROJECT_DIRECTORY = "PROJECT_DIRECTORY"; // NOI18N
        String NAME = "NAME"; // NOI18N

        WizardDescriptor.Panel<WizardDescriptor>[] createPanels();
        String[] createSteps();
        /** @return site root */
        FileObject instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException;
        void uninitialize(WizardDescriptor wizardDescriptor);
    }

    public static final class NewProjectWizard implements Wizard {

        public static final String SITE_TEMPLATE = "SITE_TEMPLATE"; // NOI18N
        public static final String LIBRARIES_FOLDER = "LIBRARIES_FOLDER"; // NOI18N


        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings("unchecked")
            WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                new NewClientSideProjectPanel(),
                new SiteTemplateWizardPanel(),
                new JavaScriptLibrarySelectionPanel(),
            };
            return panels;
        }

        @NbBundle.Messages({
            "NewProjectWizard.step.createProject=Name and Location",
            "NewProjectWizard.step.chooseSite=Site Template",
            "NewProjectWizard.step.selectJsLibrary=JavaScript Files"
        })
        @Override
        public String[] createSteps() {
            return new String[] {
                Bundle.NewProjectWizard_step_createProject(),
                Bundle.NewProjectWizard_step_chooseSite(),
                Bundle.NewProjectWizard_step_selectJsLibrary(),
            };
        }

        @Override
        public FileObject instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException {
            AntProjectHelper projectHelper = project.getProjectHelper();
            // site template
            SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(SITE_TEMPLATE);
            if (siteTemplate != null) {
                // any site template selected
                applySiteTemplate(projectHelper, siteTemplate, handle);
            }

            // get application dir:
            FileObject siteRootDir = ClientSideProjectUtilities.getSiteRootFolder(projectHelper);
            if (siteRootDir == null) {
                ClientSideProjectUtilities.initializeProject(projectHelper);
                siteRootDir = ClientSideProjectUtilities.getSiteRootFolder(projectHelper);
                assert siteRootDir != null;
             }

            // js libs
            FileObject jsLibs = (FileObject) wizardDescriptor.getProperty(LIBRARIES_FOLDER);
            if (jsLibs != null) {
                // move all downloaded libraries
                for (FileObject child : jsLibs.getChildren()) {
                    FileUtil.moveFile(child, siteRootDir, child.getName());
                }
                jsLibs.delete();
            }

            // index file (#216293)
            File[] htmlFiles = FileUtil.toFile(siteRootDir).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // accept html or xhtml files
                    return pathname.isFile()
                            && pathname.getName().toLowerCase().endsWith("html"); // NOI18N
                }
            });
            if (htmlFiles != null && htmlFiles.length == 0) {
                createIndexFile(siteRootDir);
            }
            return siteRootDir;
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(SITE_TEMPLATE, null);
            wizardDescriptor.putProperty(LIBRARIES_FOLDER, null);
        }

        @NbBundle.Messages({
            "# {0} - template name",
            "ClientSideProjectWizardIterator.error.applyingSiteTemplate=Cannot apply template \"{0}\"."
        })
        private void applySiteTemplate(AntProjectHelper helper, SiteTemplateImplementation siteTemplate, final ProgressHandle handle) {
            assert !EventQueue.isDispatchThread();
            final String templateName = siteTemplate.getName();
            try {
                siteTemplate.apply(helper, handle);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                errorOccured(Bundle.ClientSideProjectWizardIterator_error_applyingSiteTemplate(templateName));
            }
        }

        private void errorOccured(String message) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }

        private void createIndexFile(FileObject siteRoot) throws IOException {
            FileObject indexTemplate = FileUtil.getConfigFile("Templates/Other/html.html"); // NOI18N
            DataFolder dataFolder = DataFolder.findFolder(siteRoot);
            DataObject dataIndex = DataObject.find(indexTemplate);
            dataIndex.createFromTemplate(dataFolder, "index"); // NOI18N
        }

    }

    public static final class ExistingProjectWizard implements Wizard {

        public static final String SITE_ROOT = "SITE_ROOT"; // NOI18N
        public static final String CONFIG_ROOT = "CONFIG_ROOT"; // NOI18N
        public static final String TEST_ROOT = "TEST_ROOT"; // NOI18N

        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings("unchecked")
            WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                new ExistingClientSideProjectPanel(),
            };
            return panels;
        }

        @NbBundle.Messages("ExistingProjectWizard.step.createProject=Name and Location")
        @Override
        public String[] createSteps() {
            return new String[] {
                Bundle.ExistingProjectWizard_step_createProject(),
            };
        }

        @Override
        public FileObject instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException {
            File siteRoot = (File) wizardDescriptor.getProperty(SITE_ROOT);
            ReferenceHelper referenceHelper = project.getReferenceHelper();
            ClientSideProjectUtilities.initializeProject(project.getProjectHelper(),
                    referenceHelper.createForeignFileReference(siteRoot, null),
                    getDir(wizardDescriptor, TEST_ROOT, ClientSideProjectConstants.DEFAULT_TEST_FOLDER, referenceHelper),
                    getDir(wizardDescriptor, CONFIG_ROOT, ClientSideProjectConstants.DEFAULT_CONFIG_FOLDER, referenceHelper),
                    false);
            return FileUtil.toFileObject(siteRoot);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(SITE_ROOT, null);
            wizardDescriptor.putProperty(CONFIG_ROOT, null);
            wizardDescriptor.putProperty(TEST_ROOT, null);
        }

        private String getDir(WizardDescriptor wizardDescriptor, String property, String defaultDir, ReferenceHelper referenceHelper) {
            File dir = (File) wizardDescriptor.getProperty(property);
            if (dir != null) {
                return referenceHelper.createForeignFileReference(dir, null);
            }
            return defaultDir;
        }

    }

}
