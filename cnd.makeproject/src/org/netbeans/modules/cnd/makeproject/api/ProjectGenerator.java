/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectGenerator;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileUtil;

public class ProjectGenerator {

    public static final class ProjectParameters {

        private final String projectName;
        private final File projectFolder;
        private String makefile;
        private MakeConfiguration[] configurations;
        private boolean openFlag;
        private Iterator<SourceFolderInfo> sourceFolders;
        private String sourceFoldersFilter;
        private Iterator<String> importantFileItems;
        private Iterator<SourceFolderInfo> testFolders;
        private String mainFile;
        private String hostUID;
        private boolean fullRemote;
        private CompilerSet cs;
        private boolean defaultToolchain;
        private String postCreationClassName;
        private String mainProject;
        private String subProjects;
        private Map<String, Object> templateParams;
        private String databaseConnection;

        /**
         *
         * @param projectFolderName name of the project's folder
         * @param projectParentFolderPath parent folder path (i.e. ~/NetbeansProjects)
         *          where project folder is to be created
         */
        public ProjectParameters(String projectFolderName, String projectParentFolderPath) {
            this(projectFolderName, CndFileUtils.createLocalFile(projectParentFolderPath, projectFolderName));
        }

        /**
         *
         * @param projectName name of the project
         * @param projectFolder project folder (i.e. ~/NetbeansProjects/projectName)
         */
        public ProjectParameters(String projectName, File projectFolder) {            
            this.projectName = projectName;
            this.projectFolder = FileUtil.normalizeFile(projectFolder);
            //this.makefile = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
            this.configurations = new MakeConfiguration[0];
            this.openFlag = false;
            this.sourceFolders = null;
            this.sourceFoldersFilter = null;
            this.testFolders = null; 
            this.importantFileItems = null; 
            this.mainFile = "";
            this.postCreationClassName = null;
            this.mainProject = null;
            this.templateParams = Collections.<String, Object>emptyMap();
        }

        public ProjectParameters setMakefileName(String makefile) {
            CndUtils.assertNotNull(makefile, "project makefile name should not be null"); //NOI18N
            this.makefile = makefile;
            return this;
        }

        public ProjectParameters setConfigurations(MakeConfiguration[] confs) {
            this.configurations = (confs == null) ? new MakeConfiguration[0] : confs;
            return this;
        }

        public ProjectParameters setConfiguration(MakeConfiguration conf) {
            this.configurations = new MakeConfiguration[] { conf };
            return this;
        }

        public ProjectParameters setOpenFlag(boolean open) {
            this.openFlag = open;
            return this;
        }

        public ProjectParameters setSourceFolders(Iterator<SourceFolderInfo> sourceFolders) {
            this.sourceFolders = sourceFolders;
            return this;
        }

        public ProjectParameters setSourceFoldersFilter(String sourceFoldersFilter) {
            this.sourceFoldersFilter = sourceFoldersFilter;
            return this;
        }

        public ProjectParameters setTestFolders(Iterator<SourceFolderInfo> testFolders) {
            this.testFolders = testFolders;
            return this;
        }

        public ProjectParameters setImportantFiles(Iterator<String> importantItems) {
            this.importantFileItems = importantItems;
            return this;
        }

        public ProjectParameters setMainFile(String mainFile) {
            this.mainFile = mainFile == null ? "" : mainFile;
            return this;
        }
        
        public ProjectParameters setHostToolchain(String hostUID, CompilerSet cs, boolean defaultCS) {
            this.hostUID = hostUID;
            this.cs = cs;
            this.defaultToolchain = defaultCS;
            return this;
        }

        public ProjectParameters setFullRemote(boolean fullRemote) {
            this.fullRemote = fullRemote;
            return this;
        }

        public boolean getFullRemote() {
            return fullRemote;
        }

        public RemoteProject.Mode getRemoteMode() {
            return fullRemote ? RemoteProject.Mode.REMOTE_SOURCES : RemoteProject.Mode.LOCAL_SOURCES;
        }

        public ProjectParameters setTemplateParams(Map<String, Object> params) {
            this.templateParams = params;
            return this;
        }

        public ProjectParameters setDatabaseConnection(String connection) {
            this.databaseConnection = connection;
            return this;
        }

        public File getProjectFolder() {
            return projectFolder;
        }

        public String getProjectName() {
            return projectName;
        }

        public MakeConfiguration[] getConfigurations() {
            return this.configurations;
        }

        public boolean getOpenFlag() {
            return this.openFlag;
        }

        public String getMakefileName() {
            return this.makefile;
        }

        public String getMainFile() {
            return this.mainFile;
        }

        public Iterator<SourceFolderInfo> getSourceFolders() {
            return this.sourceFolders;
        }

        public String getSourceFoldersFilter() {
            return this.sourceFoldersFilter;
        }

        public Iterator<SourceFolderInfo> getTestFolders() {
            return this.testFolders;
        }

        public Iterator<String> getImportantFiles() {
            return this.importantFileItems;
        }

        public String getHostUID() {
            return hostUID;
        }

        public void setHostUID(String hostUID) {
            this.hostUID = hostUID;
        }

        public CompilerSet getToolchain() {
            return cs;
        }

        public boolean isDefaultToolchain() {
            return defaultToolchain;
        }

        /**
         * @return the postCreationClassName
         */
        public String getPostCreationClassName() {
            return postCreationClassName;
        }

        /**
         * @param postCreationClassName the postCreationClassName to set
         */
        public void setPostCreationClassName(String postCreationClassName) {
            this.postCreationClassName = postCreationClassName;
        }

        /**
         * @return the mainProject
         */
        public String getMainProject() {
            return mainProject;
        }

        /**
         * @param mainProject the mainProject to set
         */
        public void setMainProject(String mainProject) {
            this.mainProject = mainProject;
        }

        /**
         * @return the subProjects
         */
        public String getSubProjects() {
            return subProjects;
        }

        /**
         * @param subProjects the subProjects to set
         */
        public void setSubProjects(String subProjects) {
            this.subProjects = subProjects;
        }

        public Map<String, Object> getTemplateParams() {
            return templateParams;
        }

        public String getDatabaseConnection() {
            return databaseConnection;
        }

        public boolean isMakefileProject() {
            return configurations[0].isMakefileConfiguration();
        }

    }
    
    public static String getDefaultProjectFolder() {
        return MakeProjectGenerator.getDefaultProjectFolder();
    }

    public static String getValidProjectName(String projectFolder) {
        return MakeProjectGenerator.getValidProjectName(projectFolder);
    }

    public static String getValidProjectName(String projectFolder, String suggestedProjectName) {
        return MakeProjectGenerator.getValidProjectName(projectFolder, suggestedProjectName);
    }

    public static Project createBlankProject(ProjectParameters prjParams) throws IOException {
        return MakeProjectGenerator.createBlankProject(prjParams);
    }

    public static Project createProject(ProjectParameters prjParams) throws IOException {
        MakeProject createdProject = MakeProjectGenerator.createProject(prjParams);
        ConfigurationDescriptorProvider.recordCreatedProjectMetrics(prjParams.getConfigurations());
        return createdProject;
    }

    /*
     * Used by Sun Studio
     */
    public static void createProjectFromTemplate(URL templateResourceURL, ProjectParameters prjParams) throws IOException {
        MakeSampleProjectGenerator.createProjectFromTemplate(templateResourceURL.openStream(), prjParams);
    }

    private ProjectGenerator() {
    }
}
