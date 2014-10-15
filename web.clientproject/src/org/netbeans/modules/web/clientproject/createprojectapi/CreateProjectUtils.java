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
package org.netbeans.modules.web.clientproject.createprojectapi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ui.wizard.ClientSideProjectWizardIterator;
import org.netbeans.modules.web.clientproject.ui.wizard.NewClientSideProjectPanel;
import org.netbeans.modules.web.clientproject.ui.wizard.ToolsPanel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Support class for creating new HTML5 projects.
 * @since 1.65
 */
public final class CreateProjectUtils {

    /**
     * Constant for project directory.
     * @see #createBaseWizardPanel(String)
     */
    public static final String PROJECT_DIRECTORY = ClientSideProjectWizardIterator.Wizard.PROJECT_DIRECTORY;
    /**
     * Constant for project name.
     * @see #createBaseWizardPanel(String)
     */
    public static final String PROJECT_NAME = ClientSideProjectWizardIterator.Wizard.NAME;


    private CreateProjectUtils() {
    }

    /**
     * Create base wizard panel for new HTML5 projects. This panel contains the base information
     * about project, e.g. name, location etc. These properties are stored in the given {@link WizardDescriptor}.
     * @param projectNameTemplate default project name, e.g. "JsLibrary"
     * @return base wizard panel for new HTML5 projects together with its default display name
     * @see #PROJECT_DIRECTORY
     * @see #PROJECT_NAME
     */
    @NbBundle.Messages("CreateProjectUtils.nameLocation.displayName=Name and Location")
    public static Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> createBaseWizardPanel(String projectNameTemplate) {
        return Pair.<WizardDescriptor.FinishablePanel<WizardDescriptor>, String>of(new NewClientSideProjectPanel(projectNameTemplate),
                Bundle.CreateProjectUtils_nameLocation_displayName());
    }

    /**
     * Create wizard panel for "Tools" (Bower, NPM, Grunt). All
     * these tools are enabled by default.
     * <p>
     * Currently, this panel is always finishable.
     * @return panel for "Tools" (Bower, NPM, Grunt) together with its default display name
     */
    @NbBundle.Messages("CreateProjectUtils.tools.displayName=Tools")
    public static Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> createToolsWizardPanel() {
        return Pair.<WizardDescriptor.FinishablePanel<WizardDescriptor>, String>of(new ToolsPanel(), Bundle.CreateProjectUtils_tools_displayName());
    }

    /**
     * Instantiate "Tools" support. In other words, generate proper files.
     * <p>
     * This method is typically used in <code>WizardIterator</code>.
     * @param project project to be used
     * @param wizardDescriptor settings to be used
     * @return set of generated files; can be empty but never {@code null}
     * @throws IOException if any error occurs
     */
    public static Set<FileObject> instantiateTools(Project project, WizardDescriptor wizardDescriptor) throws IOException {
        Set<FileObject> files = new HashSet<>();
        FileObject folder = findBestToolsFolder(project);
        assert folder != null;
        if (isToolEnabled(wizardDescriptor, ToolsPanel.BOWER_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/bower.json")); // NOI18N
        }
        if (isToolEnabled(wizardDescriptor, ToolsPanel.NPM_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/package.json")); // NOI18N
        }
        if (isToolEnabled(wizardDescriptor, ToolsPanel.GRUNT_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/Gruntfile.js")); // NOI18N
        }
        return files;
    }

    private static boolean isToolEnabled(WizardDescriptor wizardDescriptor, String tool) {
        Boolean enabled = (Boolean) wizardDescriptor.getProperty(tool);
        if (enabled == null) {
            // not set => set as enabled
            return true;
        }
        return enabled;
    }

    private static FileObject findBestToolsFolder(Project project) {
        // XXX ???
        /*Sources sources = ProjectUtils.getSources(project);
        for (SourceGroup sourceGroup : sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)) {
            return sourceGroup.getRootFolder();
        }*/
        return project.getProjectDirectory();
    }

    private static FileObject createFile(FileObject root, String template) throws IOException {
        assert root != null;
        assert root.isFolder() : root;
        FileObject templateFile = FileUtil.getConfigFile(template);
        DataFolder dataFolder = DataFolder.findFolder(root);
        DataObject dataIndex = DataObject.find(templateFile);
        return dataIndex.createFromTemplate(dataFolder).getPrimaryFile();
    }

}
