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

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
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
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class NewProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    @StaticResource
    private static final String NODEJS_PROJECT_ICON = "org/netbeans/modules/javascript/nodejs/ui/resources/nodejs-badge.png"; // NOI18N
    private static final String DEFAULT_SOURCE_FOLDER = "src"; // NOI18N

    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizardDescriptor;


    private NewProjectWizardIterator() {
        baseWizard = CreateProjectUtils.createBaseWizardPanel("NodeJsApplication"); // NOI18N
        toolsWizard = CreateProjectUtils.createToolsWizardPanel();
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newProject.displayName",
            description = "../resources/NewNodeJsProjectDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 150)
    @NbBundle.Messages("NewProjectWizardIterator.newProject.displayName=Node.js Application")
    public static NewProjectWizardIterator newNodeJsProject() {
        return new NewProjectWizardIterator();
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
                .setPlatformProvider(NodeJsPlatformProvider.IDENT);
        Project project = ClientSideProjectGenerator.createProject(createProperties);
        FileObject sources = projectDirectory.getFileObject(DEFAULT_SOURCE_FOLDER);
        assert sources != null;
        // create main.js file
        FileObject mainFile = createMainFile(sources);
        files.add(mainFile);

        // tools
        CreateProjectUtils.instantiateTools(project, wizardDescriptor);

        // enable running as node.js
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        nodeJsSupport.getPreferences().setRunEnabled(true);
        nodeJsSupport.getPreferences().setStartFile(FileUtil.toFile(mainFile).getAbsolutePath());
        nodeJsSupport.firePropertyChanged(NodeJsPlatformProvider.PROP_RUN_CONFIGURATION, null, NodeJsRunPanel.IDENTIFIER);

        handle.finish();
        return files;
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wizardDescriptor = wizard;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initializeInternal();
            }
        });
    }

    void initializeInternal() {
        assert EventQueue.isDispatchThread();
        index = 0;
        panels = new WizardDescriptor.Panel[] {
            baseWizard.first(),
            toolsWizard.first(),
        };
        // Make sure list of steps is accurate.
        List<String> steps = Arrays.asList(
                baseWizard.second(),
                toolsWizard.second()
        );

        // XXX should be lazy
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i;
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
                // name
                jc.setName(steps.get(i));
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        wizardDescriptor.putProperty("NewProjectWizard_Title", Bundle.NewProjectWizardIterator_newProject_displayName()); // NOI18N
        return panels[index];
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "NewProjectWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.NewProjectWizardIterator_name(index + 1, panels.length);
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
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    private FileObject createMainFile(FileObject sources) throws IOException {
        assert sources != null;
        FileObject template = FileUtil.getConfigFile("Templates/Other/javascript.js"); // NOI18N
        DataFolder dataFolder = DataFolder.findFolder(sources);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, "main").getPrimaryFile(); // NOI18N
    }

}
