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
package org.netbeans.modules.cnd.makeproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.api.LogicalFolderItemsInfo;
import org.netbeans.modules.cnd.makeproject.api.LogicalFoldersInfo;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator.ProjectParameters;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectGenerator;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a MakeProject from scratch according to some initial configuration.
 */
public class MakeProjectGeneratorImpl {

    private static final String PROP_DBCONN = "dbconn"; // NOI18N

    private MakeProjectGeneratorImpl() {
    }

    public static String getDefaultProjectFolder() {
        return ProjectChooser.getProjectsFolder().getPath();
    }

    public static String getDefaultProjectFolder(ExecutionEnvironment env) {
        try {
            if (env.isLocal()) {
                return getDefaultProjectFolder();
            } else {
                return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName();  //NOI18N
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (CancellationException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        }
        return null;
    }

    public static String getValidProjectName(String projectFolder) {
        return getValidProjectName(projectFolder, "Project"); // NOI18N
    }

    public static String getValidProjectName(String projectFolder, String name) {
        int baseCount = 0;
        String projectName = null;
        while (true) {
            if (baseCount == 0) {
                projectName = name;
            } else {
                projectName = name + baseCount;
            }
            File projectNameFile = CndFileUtils.createLocalFile(projectFolder, projectName);
            if (!projectNameFile.exists()) {
                break;
            }
            baseCount++;
        }
        return projectName;
    }

    public static MakeProject createBlankProject(ProjectParameters prjParams) throws IOException {
        MakeConfiguration[] confs = prjParams.getConfigurations();
        String projectFolderPath = prjParams.getProjectFolderPath();

        // work in a copy of confs
        MakeConfiguration[] copyConfs = new MakeConfiguration[confs.length];
        for (int i = 0; i < confs.length; i++) {
            copyConfs[i] = confs[i].clone();
            copyConfs[i].setBaseFSPath(new FSPath(prjParams.getSourceFileSystem(), projectFolderPath));
            RunProfile profile = (RunProfile) copyConfs[i].getAuxObject(RunProfile.PROFILE_ID);
            profile.setBuildFirst(false);
        }

        FileObject dirFO = createProjectDir(prjParams);
        prjParams.setConfigurations(copyConfs);
        createProject(dirFO, prjParams, true);
        MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);

        if (prjParams.getOpenFlag()) {
            OpenProjects.getDefault().open(new Project[]{p}, false);
        }

        return p;
    }

    /**
     * Create a new empty Make project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static MakeProject createProject(ProjectParameters prjParams) throws IOException {
        FileObject dirFO = createProjectDir(prjParams);
        createProject(dirFO, prjParams, false); //NOI18N
        MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        if(prjParams.getDatabaseConnection() != null) {
            Preferences prefs = ProjectUtils.getPreferences(p, ProjectSupport.class, true);
            prefs.put(PROP_DBCONN, prjParams.getDatabaseConnection());
        }
        //FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
        return p;
    }

    private static MakeProjectHelper createProject(FileObject dirFO, final ProjectParameters prjParams, boolean saveNow) throws IOException {
        String name = prjParams.getProjectName();
        String makefileName = prjParams.getMakefileName();
        Configuration[] confs = prjParams.getConfigurations();
        if (prjParams.getCustomizerId() != null) {
            dirFO.createData("cndcustomizerid." + prjParams.getCustomizerId()); // NOI18N
        }
        final Iterator<SourceFolderInfo> sourceFolders = prjParams.getSourceFolders();
        final String sourceFoldersFilter = prjParams.getSourceFoldersFilter();
        final Iterator<SourceFolderInfo> testFolders = prjParams.getTestFolders();
        final Iterator<String> importantItems = prjParams.getImportantFiles();
        final Iterator<LogicalFolderItemsInfo> logicalFolderItems = prjParams.getLogicalFolderItems();
        final Iterator<LogicalFoldersInfo> logicalFolders = prjParams.getLogicalFolders();
        String mainFile = prjParams.getMainFile();
        MakeProjectHelper h = null;
        try {
            h = MakeProjectGenerator.createProject(dirFO, MakeProjectTypeImpl.TYPE);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        
        FileObject sourceBaseFO = dirFO;
        h.putPrimaryConfigurationData(data, true);

        //EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        //ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        // Create new project descriptor with default configurations and save it to disk.
        final MakeConfigurationDescriptor projectDescriptor = new MakeConfigurationDescriptor(dirFO, sourceBaseFO);
        if (makefileName != null) {
            projectDescriptor.setProjectMakefileName(makefileName);
        }
        projectDescriptor.init(confs);
        projectDescriptor.setState(State.READY);

        Project project = projectDescriptor.getProject();
        projectDescriptor.setProject(project);
        // create main source file
        final CreateMainParams mainFileParams = prepareMainIfNeeded(mainFile, dirFO, prjParams.getTemplateParams());
        if (sourceFoldersFilter != null && !MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN.equals(sourceFoldersFilter)) {
            projectDescriptor.setFolderVisibilityQuery(sourceFoldersFilter);
        }
        Runnable task = new Runnable() {

            @Override
            public void run() {
                projectDescriptor.initLogicalFolders(sourceFolders, sourceFolders == null, testFolders,
                        logicalFolders, logicalFolderItems, importantItems, mainFileParams.mainFilePath, false); // FIXUP: need a better check whether logical folder should be ccreated or not.
                
                projectDescriptor.save();
                // finish postponed activity when project metadata is ready
                mainFileParams.doPostProjectCreationWork();
                projectDescriptor.closed();
                projectDescriptor.clean();
            }
        };
        //if (project instanceof MakeProject && !saveNow) { // How can it not be an instance of MakeProject???
        //    MakeProject makeProject = (MakeProject) project;
        //    makeProject.addOpenedTask(task);
        //} else {
            task.run();
        //}
        if (!prjParams.isMakefileProject()) {
            FileObject baseDirFileObject = projectDescriptor.getBaseDirFileObject();
            FileObject createData = baseDirFileObject.createData(projectDescriptor.getProjectMakefileName());
            // create Makefile
            copyURLFile("nbresloc:/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile", // NOI18N
                    createData.getOutputStream());
        }
        return h;
    }

    private static void copyURLFile(String fromURL, OutputStream os) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(fromURL);
            is = url.openStream();
        } catch (Exception e) {
            // FIXUP
        }
        if (is != null) {
            copy(is, os);
        }
    }

    /**
     * Replacement for FileUtil.copy(). The problem with FU.c is that on Windows it terminates lines with
     * <CRLF> rather than <LF>. Now that we do remote development, this means that if a remote project is
     * created on Windows to be built by Sun Studio's dmake, then the <CRLF> breaks the build (this is
     * probably true with Solaris "make" as well).
     *
     * @param is The InputStream
     * @param os The Output Stream
     * @throws java.io.IOException
     */
    private static void copy(InputStream is, OutputStream os) throws IOException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            bw = new BufferedWriter(new OutputStreamWriter(os));
            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line + "\n"); // NOI18N
            }
            bw.flush();
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }

    private static FileObject createProjectDir(ProjectParameters prjParams) throws IOException {
        String projectFolderPath = prjParams.getProjectFolderPath();
        MakeSampleProjectGenerator.workAroundBug203507(projectFolderPath);
        FileObject dirFO = FileUtil.createFolder(prjParams.getSourceFileSystem().getRoot(), projectFolderPath);
        //File dir = prjParams.getProjectFolder();
        //if (!dir.exists()) {
        //    //Refresh before mkdir not to depend on window focus
        //    // refreshFileSystem (dir); // See 136445
        //    if (!dir.mkdirs()) {
        //        throw new IOException("Can not create project folder."); // NOI18N
        //    }
        //    // refreshFileSystem (dir); // See 136445
        //}
        //dirFO = CndFileUtils.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + prjParams.getProjectFolderPath(); // NOI18N
        assert dirFO.isValid() : "No such dir on disk: " + prjParams.getProjectFolderPath(); // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + prjParams.getProjectFolderPath(); // NOI18N
        return dirFO;
    }

    private static CreateMainParams prepareMainIfNeeded(String mainFile, FileObject srcFolder, Map<String, Object> templateParams) throws IOException {
        if (mainFile.length() == 0) {
            return new CreateMainParams(null, null);
        }
        String mainName = mainFile.substring(0, mainFile.indexOf('|'));
        String template = mainFile.substring(mainFile.indexOf('|') + 1);

        if (mainName.length() == 0) {
            return new CreateMainParams(null, null);
        }

        FileObject mainTemplate = FileUtil.getConfigFile(template);

        if (mainTemplate == null) {
            return new CreateMainParams(null, null); // Don't know the template
        }
        final String createdMainName;
         if (mainName.indexOf('\\') > 0 || mainName.indexOf('/') > 0) {
            String absPath = CndPathUtilitities.toAbsolutePath(srcFolder, mainName);
            absPath = FileSystemProvider.getCanonicalPath(srcFolder.getFileSystem(), absPath);
            srcFolder = FileUtil.createFolder(srcFolder, CndPathUtilitities.getDirName(mainName));
            createdMainName = CndPathUtilitities.getBaseName(absPath);
         } else {
            createdMainName = mainName;
         }

        final DataObject mt = DataObject.find(mainTemplate);
        final DataFolder pDf = DataFolder.findFolder(srcFolder);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(CreateFromTemplateHandler.FREE_FILE_EXTENSION, true);
        params.putAll(templateParams);

        // manipulation with file content should be postponed
        // project does not yet know about main file and can not provide
        // settings which can affect i.e. formatting of file
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mt.createFromTemplate(pDf, createdMainName, params);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        return new CreateMainParams(mainName, runnable);
    }

    private static final class CreateMainParams {
        final String mainFilePath;
        private final Runnable postProjectSaveWorker;

        public CreateMainParams(String mainName, Runnable postProjectSaveWorker) {
            this.mainFilePath = mainName;
            this.postProjectSaveWorker = postProjectSaveWorker;
        }

        private void doPostProjectCreationWork() {
            if (postProjectSaveWorker != null) {
                postProjectSaveWorker.run();
            }
        }

    }
}


