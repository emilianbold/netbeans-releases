/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsRunPanel;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class NewProjectWizardIterator extends BaseWizardIterator {

    private static final String DEFAULT_SOURCE_FOLDER = ""; // NOI18N
    private static final String DEFAULT_SITE_ROOT_FOLDER = "public"; // NOI18N

    private final String displayName;
    private final boolean withSiteRoot;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;

    private NewProjectWizardIterator(String displayName, boolean withSiteRoot) {
        assert displayName != null;
        this.displayName = displayName;
        this.withSiteRoot = withSiteRoot;
        baseWizard = CreateProjectUtils.createBaseWizardPanel("NodeJsApplication"); // NOI18N
        toolsWizard = CreateProjectUtils.createToolsWizardPanel(new CreateProjectUtils.Tools()
                .setNpm(true));
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newNodeJsProject.displayName",
            description = "../resources/NewNodeJsProjectDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 150)
    @NbBundle.Messages("NewProjectWizardIterator.newNodeJsProject.displayName=Node.js Application")
    public static NewProjectWizardIterator newNodeJsProject() {
        return new NewProjectWizardIterator(Bundle.NewProjectWizardIterator_newNodeJsProject_displayName(), false);
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newHtml5ProjectWithNodeJs.displayName",
            description = "../resources/NewHtml5ProjectWithNodeJsDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 160)
    @NbBundle.Messages("NewProjectWizardIterator.newHtml5ProjectWithNodeJs.displayName=HTML5/JS Application with Node.js")
    public static NewProjectWizardIterator newHtml5ProjectWithNodeJs() {
        return new NewProjectWizardIterator(Bundle.NewProjectWizardIterator_newHtml5ProjectWithNodeJs_displayName(), true);
    }

    @Override
    String getWizardTitle() {
        return displayName;
    }

    @Override
    WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            baseWizard.first(),
            toolsWizard.first(),
        };
    }

    @Override
    String[] createSteps() {
        return new String[] {
            baseWizard.second(),
            toolsWizard.second(),
        };
    }

    @Override
    void uninitializeInternal() {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
    }

    @NbBundle.Messages("NewProjectWizardIterator.progress.creating=Creating project")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.NewProjectWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        File projectDir = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_DIRECTORY));
        if (!projectDir.isDirectory()
                && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory: " + projectDir);
        }
        FileObject projectDirectory = FileUtil.toFileObject(projectDir);
        assert projectDirectory != null : "FileObject must be found for " + projectDir;
        files.add(projectDirectory);

        CreateProjectProperties createProperties = new CreateProjectProperties()
                .setProjectDir(projectDirectory)
                .setProjectName((String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME))
                .setSourceFolder(DEFAULT_SOURCE_FOLDER)
                .setSiteRootFolder(withSiteRoot ? DEFAULT_SITE_ROOT_FOLDER : null)
                .setPlatformProvider(NodeJsPlatformProvider.IDENT);
        Project project = ClientSideProjectGenerator.createProject(createProperties);
        FileObject sources = projectDirectory.getFileObject(DEFAULT_SOURCE_FOLDER);
        assert sources != null;
        // create main.js file
        FileObject mainJsFile = createMainJsFile(sources);
        files.add(mainJsFile);
        // create index.html?
        if (withSiteRoot) {
            FileObject siteRoot = projectDirectory.getFileObject(DEFAULT_SITE_ROOT_FOLDER);
            assert siteRoot != null;
            FileObject indexHtmlFile = createIndexHtmlFile(siteRoot);
            files.add(indexHtmlFile);
        }

        // tools
        CreateProjectUtils.instantiateTools(project, toolsWizard.first());

        // set proper node.js start file
        NodeJsSupport.forProject(project).getPreferences().setStartFile(FileUtil.toFile(mainJsFile).getAbsolutePath());
        if (!withSiteRoot) {
            // set node.js run config only for server-side node.js project (since project URL is not known)
            SetupProject.setup(project);
        }

        handle.finish();
        return files;
    }

    private FileObject createMainJsFile(FileObject sources) throws IOException {
        assert sources != null;
        FileObject template = FileUtil.getConfigFile("Templates/Other/javascript.js"); // NOI18N
        DataFolder dataFolder = DataFolder.findFolder(sources);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, "main").getPrimaryFile(); // NOI18N
    }

    private FileObject createIndexHtmlFile(FileObject siteRoot) throws IOException {
        assert siteRoot != null;
        FileObject template = FileUtil.getConfigFile("Templates/Other/html.html"); // NOI18N
        DataFolder dataFolder = DataFolder.findFolder(siteRoot);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, "index").getPrimaryFile(); // NOI18N
    }

    //~ Inner classes

    private static final class SetupProject implements PropertyChangeListener {

        final OpenProjects openProjects = OpenProjects.getDefault();
        private final Project project;


        private SetupProject(Project project) {
            assert project != null;
            this.project = project;
        }

        public static void setup(Project project) {
            SetupProject setupProject = new SetupProject(project);
            setupProject.openProjects.addPropertyChangeListener(setupProject);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                if (Arrays.asList(openProjects.getOpenProjects()).contains(project)) {
                    openProjects.removePropertyChangeListener(this);
                    NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
                    nodeJsSupport.getPreferences().setRunEnabled(true);
                    nodeJsSupport.firePropertyChanged(NodeJsPlatformProvider.PROP_RUN_CONFIGURATION, null, NodeJsRunPanel.IDENTIFIER);
                }
            }
        }

    }

}
