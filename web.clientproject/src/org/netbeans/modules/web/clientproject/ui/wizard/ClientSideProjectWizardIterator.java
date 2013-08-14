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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import org.netbeans.modules.web.clientproject.api.jslibs.JavaScriptLibraries;
import org.netbeans.modules.web.clientproject.spi.ClientProjectExtender;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation.ProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class ClientSideProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectWizardIterator.class.getName());

    private final Wizard wizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor.Panel<WizardDescriptor>[] extenderPanels;
    private WizardDescriptor.Panel<WizardDescriptor>[] initPanels;
    private Collection<? extends ClientProjectExtender> extenders;
    private WizardDescriptor wizardDescriptor;
    private boolean withExtenders;


    private ClientSideProjectWizardIterator(Wizard wizard) {
        this(wizard, false);
    }

    private ClientSideProjectWizardIterator(Wizard wizard, boolean withExtenders) {
        assert wizard != null;
        this.wizard = wizard;
        this.withExtenders = withExtenders;
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
    
    public static ClientSideProjectWizardIterator newProjectWithExtender() {
        return new ClientSideProjectWizardIterator(new NewProjectWizard(true), true);
    }

    @NbBundle.Messages({
        "ClientSideProjectWizardIterator.progress.creatingProject=Creating project",
        "ClientSideProjectWizardIterator.error.noSiteRoot=<html>Site Root folder cannot be created.<br><br>Use <i>Resolve Project Problems...</i> action to repair the project."
    })
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.ClientSideProjectWizardIterator_progress_creatingProject());
        Set<FileObject> files = new LinkedHashSet<FileObject>();
        File projectDirectory = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(Wizard.PROJECT_DIRECTORY));
        String name = (String) wizardDescriptor.getProperty(Wizard.NAME);
        if (!projectDirectory.isDirectory() && !projectDirectory.mkdirs()) {
            throw new IOException("Cannot create project directory"); //NOI18N
        }
        FileObject dir = FileUtil.toFileObject(projectDirectory);
        AntProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(dir, name);
        // Always open top dir as a project:
        files.add(dir);

        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        FileObject siteRoot = wizard.instantiate(files, handle, wizardDescriptor, project);

        // #221550
        if (siteRoot != null) {
            // index file
            FileObject indexFile = siteRoot.getFileObject("index", "html"); // NOI18N
            if (indexFile != null) {
                files.add(indexFile);
            }
        } else {
            errorOccured(Bundle.ClientSideProjectWizardIterator_error_noSiteRoot());
        }

        File parent = projectDirectory.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();

        SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(NewProjectWizard.SITE_TEMPLATE);
        String libraryNames = (String)wizardDescriptor.getProperty(NewProjectWizard.LIBRARY_NAMES);
        boolean newProjWizard = wizard instanceof NewProjectWizard;
        ClientSideProjectUtilities.logUsage(ClientSideProjectWizardIterator.class, "USG_PROJECT_HTML5_CREATE", // NOI18N
                new Object[] { newProjWizard ? "NEW" : "EXISTING", // NOI18N
                siteTemplate != null ? siteTemplate.getId() : "NONE", // NOI18N
                libraryNames == null ? "" : libraryNames,
                !newProjWizard && siteRoot != null ? (FileUtil.isParentOf(dir, siteRoot) ? "YES" : "NO") : "" // NOI18N
                });

        return files;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("never implemented - use progress one"); //NOI18N
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        index = 0;
        if (withExtenders) {
            extenders = Lookup.getDefault().lookupAll(ClientProjectExtender.class);
        } else {
            extenders = Collections.EMPTY_LIST;
        }
        panels = wizard.createPanels();
        
        // Make sure list of steps is accurate.
        LinkedList<String> steps = new LinkedList<String>();
        steps.addAll(Arrays.asList(wizard.createSteps()));

        //Compute steps from extenders
        ArrayList<Panel<? extends WizardDescriptor>> extenderPanelsCol = new ArrayList<Panel<? extends WizardDescriptor>>();
        ArrayList<Panel<? extends WizardDescriptor>> initPanelsCol = new ArrayList<Panel<? extends WizardDescriptor>>();
        for (ClientProjectExtender extender: extenders) {
            extender.initialize(wizardDescriptor);
            for (Panel<WizardDescriptor> panel: extender.createWizardPanels()) {
                extenderPanelsCol.add(panel);
                steps.add(panel.getComponent().getName());
            }
            int i =0;
            for (Panel<WizardDescriptor> panel: extender.createInitPanels()) {
                initPanelsCol.add(panel);
                steps.add(i++,panel.getComponent().getName());
            }
            
        }

        extenderPanels = extenderPanelsCol.toArray(new Panel[0]);
        initPanels = initPanelsCol.toArray(new Panel[0]);

        int i = 0;
        // XXX should be lazy
        //Extenders
        for (; i < initPanels.length; i++) {
            Component c = initPanels[i].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray());
                // name
                jc.setName(steps.get(i));
            }
        }

        // XXX should be lazy
        //Regular panels
        for (; i < panels.length + initPanels.length; i++) {
            Component c = panels[i-initPanels.length].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
                // name
                jc.setName(steps.get(i));
            }
        }

        // XXX should be lazy
        //Extenders
        for (; i < extenderPanels.length + panels.length + initPanels.length; i++) {
            Component c = extenderPanels[i-panels.length-initPanels.length].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray());
                // name
                jc.setName(steps.get(i));
            }
        }

    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        wizardDescriptor.putProperty(Wizard.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(Wizard.NAME, null);
        wizard.uninitialize(wizardDescriptor);
        panels = null;
        extenders = null;
        extenderPanels = null;
        initPanels = null;
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
        return index < panels.length + extenderPanels.length + initPanels.length -1;
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
        setTitle();
        if (index < initPanels.length) {
            return initPanels[index];
        }
       if (index < initPanels.length + panels.length) {
            return panels[index - initPanels.length];
       }
       return extenderPanels[index - initPanels.length - panels.length];
    }

    private void setTitle() {
        if (wizardDescriptor != null) {
            // wizard title
            String title = null;
            if (wizard instanceof NewProjectWizard) {
                title = Bundle.ClientSideProjectWizardIterator_newProject_displayName();
            } else if (wizard instanceof ExistingProjectWizard) {
                title = Bundle.ClientSideProjectWizardIterator_existingProject_displayName();
            } else {
                assert false : "Unknown project wizard type: " + wizard.getClass().getName();
            }
            if (title != null) {
                wizardDescriptor.putProperty("NewProjectWizard_Title", title); // NOI18N
            }
        }
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

    static void errorOccured(final String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
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
        public static final String LIBRARIES_PATH = "LIBRARIES_PATH";
        public static final String LIBRARY_NAMES = "LIBRARY_NAMES"; // NOI18N
        public static final String SITE_ROOT = "SITE_ROOT"; // NOI18N
        
        private boolean withExtenders;

        public NewProjectWizard(boolean withExtenders) {
            this.withExtenders = withExtenders;
        }
        
        public NewProjectWizard() {
            this(false);
        }
        
        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings({"rawtypes", "unchecked"})
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
            String customSiteRoot = (String) wizardDescriptor.getProperty(SITE_ROOT);
            // site template
            SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(SITE_TEMPLATE);
            ProjectProperties projectProperties = new ProjectProperties()
                    .setSiteRootFolder(customSiteRoot!=null?customSiteRoot:ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER)
                    .setTestFolder(ClientSideProjectConstants.DEFAULT_TEST_FOLDER)
                    .setConfigFolder(ClientSideProjectConstants.DEFAULT_CONFIG_FOLDER);
            if (siteTemplate != null) {
                // configure
                siteTemplate.configure(projectProperties);
                // init project
                initProject(project, projectProperties, wizardDescriptor);
                // any site template selected
                applySiteTemplate(projectHelper.getProjectDirectory(), projectProperties, siteTemplate, handle);
            } else {
                // init standard project
                initProject(project, projectProperties, wizardDescriptor);
            }

            FileObject siteRootDir = project.getSiteRootFolder();
            if (siteRootDir == null) {
                // #221550
                return null;
            }

            // js libs
            FileObject jsLibs = (FileObject) wizardDescriptor.getProperty(LIBRARIES_FOLDER);
            if (jsLibs != null) {
                // move all downloaded libraries
                FileUtilities.moveContent(jsLibs, siteRootDir);
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

            // apply extenders
            if (withExtenders) {
                for (ClientProjectExtender extender : Lookup.getDefault().lookupAll(ClientProjectExtender.class)) {
                    extender.apply(project.getProjectDirectory(), siteRootDir, (String) wizardDescriptor.getProperty(LIBRARIES_PATH));
                }
            }

            return siteRootDir;
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            // cleanup js libs
            FileObject jsLibs = (FileObject) wizardDescriptor.getProperty(LIBRARIES_FOLDER);
            if (jsLibs != null && jsLibs.isValid()) {
                try {
                    jsLibs.delete();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            wizardDescriptor.putProperty(SITE_TEMPLATE, null);
            wizardDescriptor.putProperty(LIBRARIES_FOLDER, null);
        }

        private void initProject(ClientSideProject project, ProjectProperties properties, WizardDescriptor wizardDescriptor) throws IOException {
            ClientSideProjectUtilities.initializeProject(project,
                    properties.getSiteRootFolder(),
                    properties.getTestFolder(),
                    properties.getConfigFolder());
            // #231326
            String librariesPath = (String) wizardDescriptor.getProperty(LIBRARIES_PATH);
            if (librariesPath != null) {
                JavaScriptLibraries.setJsLibFolder(project, librariesPath);
            }
        }

        @NbBundle.Messages({
            "# {0} - template name",
            "ClientSideProjectWizardIterator.error.applyingSiteTemplate=Cannot apply template \"{0}\"."
        })
        private void applySiteTemplate(FileObject projectDir, ProjectProperties projectProperties, SiteTemplateImplementation siteTemplate, final ProgressHandle handle) {
            assert !EventQueue.isDispatchThread();
            final String templateName = siteTemplate.getName();
            try {
                siteTemplate.apply(projectDir, projectProperties, handle);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                errorOccured(Bundle.ClientSideProjectWizardIterator_error_applyingSiteTemplate(templateName));
            }
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
            @SuppressWarnings({"unchecked", "rawtypes"})
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
            File projectDir = FileUtil.toFile(project.getProjectDirectory());
            File siteRoot = (File) wizardDescriptor.getProperty(SITE_ROOT);
            // #218736
            String testFolder;
            String configFolder;
            if (projectDir.equals(siteRoot)) {
                testFolder = null;
                configFolder = null;
            } else {
                testFolder = getExistingDir(wizardDescriptor, TEST_ROOT, ClientSideProjectConstants.DEFAULT_TEST_FOLDER);
                configFolder = getExistingDir(wizardDescriptor, CONFIG_ROOT, ClientSideProjectConstants.DEFAULT_CONFIG_FOLDER);
            }
            ClientSideProjectUtilities.initializeProject(project, siteRoot.getAbsolutePath(), testFolder, configFolder);
            return FileUtil.toFileObject(siteRoot);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(SITE_ROOT, null);
            wizardDescriptor.putProperty(CONFIG_ROOT, null);
            wizardDescriptor.putProperty(TEST_ROOT, null);
        }

        private String getExistingDir(WizardDescriptor wizardDescriptor, String property, String defaultDir) throws IOException {
            File dir = (File) wizardDescriptor.getProperty(property);
            if (dir != null) {
                // dir set
                return dir.getAbsolutePath();
            }
            return defaultDir;
        }

    }

}
