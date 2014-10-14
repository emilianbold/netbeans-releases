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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.ui.wizard.ToolsPanel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Support class for "Tools" (Bower, NPM, Grunt).
 * @since 1.65
 */
public final class Tools {

    private static final Tools INSTANCE = new Tools();


    private Tools() {
    }

    public static Tools getInstance() {
        return INSTANCE;
    }

    @NbBundle.Messages("Tools.displayName=Tools")
    public String getDisplayName() {
        return Bundle.Tools_displayName();
    }

    /**
     * Create wizard panel for "Tools" (Bower, NPM, Grunt). All
     * these tools are enabled by default.
     * <p>
     * Currently, this panel is always finishable.
     * @return panel for "Tools" (Bower, NPM, Grunt).
     */
    public WizardDescriptor.FinishablePanel<WizardDescriptor> createWizardPanel() {
        return new ToolsPanel();
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
    public Set<FileObject> instantiate(Project project, WizardDescriptor wizardDescriptor) throws IOException {
        Set<FileObject> files = new HashSet<>();
        FileObject folder = findBestFolder(project);
        assert folder != null;
        if (isEnabled(wizardDescriptor, ToolsPanel.BOWER_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/bower.json")); // NOI18N
        }
        if (isEnabled(wizardDescriptor, ToolsPanel.NPM_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/package.json")); // NOI18N
        }
        if (isEnabled(wizardDescriptor, ToolsPanel.GRUNT_ENABLED)) {
            files.add(createFile(folder, "Templates/ClientSide/Gruntfile.js")); // NOI18N
        }
        return files;
    }

    private boolean isEnabled(WizardDescriptor wizardDescriptor, String support) {
        Boolean enabled = (Boolean) wizardDescriptor.getProperty(support);
        if (enabled == null) {
            // not set => set as enabled
            return true;
        }
        return enabled;
    }

    private FileObject findBestFolder(Project project) {
        // XXX ???
        /*Sources sources = ProjectUtils.getSources(project);
        for (SourceGroup sourceGroup : sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)) {
            return sourceGroup.getRootFolder();
        }*/
        return project.getProjectDirectory();
    }

    private FileObject createFile(FileObject root, String template) throws IOException {
        assert root != null;
        assert root.isFolder() : root;
        FileObject templateFile = FileUtil.getConfigFile(template);
        DataFolder dataFolder = DataFolder.findFolder(root);
        DataObject dataIndex = DataObject.find(templateFile);
        return dataIndex.createFromTemplate(dataFolder).getPrimaryFile();
    }

}
