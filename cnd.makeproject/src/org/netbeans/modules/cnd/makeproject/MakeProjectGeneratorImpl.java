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

import org.netbeans.modules.cnd.makeproject.api.support.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator.ProjectParameters;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.loaders.CreateFromTemplateHandler;

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
            return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName();  //NOI18N
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
            OpenProjects.getDefault().setMainProject(p);
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
        MakeProjectHelper h = createProject(dirFO, prjParams, false); //NOI18N
        MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        p.setRemoteMode(prjParams.getRemoteMode());
        if (prjParams.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
            p.setRemoteFileSystemHost(ExecutionEnvironmentFactory.fromUniqueID(prjParams.getHostUID()));
        }
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
        if (prjParams.getFullRemote()) {
            RemoteSyncFactory factory = RemoteSyncFactory.fromID(RemoteProject.FULL_REMOTE_SYNC_ID);
            CndUtils.assertNotNull(factory, "Can not find sync factory for full remote"); //NOI18N
            for (Configuration conf : confs) {
                MakeConfiguration mk = (MakeConfiguration) conf;
                mk.setFixedRemoteSyncFactory(factory);
                mk.setRemoteMode(RemoteProject.Mode.REMOTE_SOURCES);
            }
        }
        final Iterator<SourceFolderInfo> sourceFolders = prjParams.getSourceFolders();
        final String sourceFoldersFilter = prjParams.getSourceFoldersFilter();
        final Iterator<SourceFolderInfo> testFolders = prjParams.getTestFolders();
        final Iterator<String> importantItems = prjParams.getImportantFiles();
        String mainFile = prjParams.getMainFile();
        MakeProjectHelper h = MakeProjectGenerator.createProject(dirFO, MakeProjectTypeImpl.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);

        FileObject sourceBaseFO;
        if (prjParams.getFullRemote()) {
            // mode
            Element fullRemoteNode = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProject.REMOTE_MODE);
            fullRemoteNode.appendChild(doc.createTextNode(prjParams.getRemoteMode().name()));
            data.appendChild(fullRemoteNode);
            // host
            Element rfsHostNode = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProject.REMOTE_FILESYSTEM_HOST);
            rfsHostNode.appendChild(doc.createTextNode(prjParams.getHostUID()));
            data.appendChild(rfsHostNode);
            // mount point
            String remoteProjectPath = prjParams.getFullRemoteNativeProjectPath();
            Element rfsBaseDir = doc.createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProject.REMOTE_FILESYSTEM_BASE_DIR);
            rfsBaseDir.appendChild(doc.createTextNode(remoteProjectPath));
            data.appendChild(rfsBaseDir);
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(prjParams.getHostUID());
            sourceBaseFO = FileSystemProvider.getFileObject(env, remoteProjectPath);
            if (sourceBaseFO == null) {
                throw new FileNotFoundException("File does not exist: " + env + ':' + remoteProjectPath); //NOI18N
            }
        } else {
            sourceBaseFO = dirFO;
        }

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
        final String mainFilePath;
        if (mainFile.length() > 0) {
            mainFilePath = createMain(mainFile, dirFO, prjParams.getTemplateParams());
        } else {
            mainFilePath = null;
        }
        if (sourceFoldersFilter != null && !MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN.equals(sourceFoldersFilter)) {
            projectDescriptor.setFolderVisibilityQuery(sourceFoldersFilter);
        }
        Runnable task = new Runnable() {

            @Override
            public void run() {
                projectDescriptor.initLogicalFolders(sourceFolders, sourceFolders == null, testFolders, importantItems, mainFilePath, prjParams.getFullRemote()); // FIXUP: need a better check whether logical folder should be ccreated or not.
                projectDescriptor.save();
                projectDescriptor.closed();
                projectDescriptor.clean();
            }
        };
        if (project instanceof MakeProject && !saveNow) { // How can it not be an instance of MakeProject???
            MakeProject makeProject = (MakeProject) project;
            makeProject.addOpenedTask(task);
        } else {
            task.run();
        }
        if (!prjParams.getFullRemote() && !prjParams.isMakefileProject()) {
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
            if (os != null) {
                os.close();
            }
        }
    }

    private static FileObject createProjectDir(ProjectParameters prjParams) throws IOException {
        FileObject dirFO = FileUtil.createFolder(prjParams.getSourceFileSystem().getRoot(), prjParams.getProjectFolderPath());
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

    private static String createMain(String mainFile, FileObject srcFolder, Map<String, Object> templateParams) throws IOException {
        String mainName = mainFile.substring(0, mainFile.indexOf('|'));
        String template = mainFile.substring(mainFile.indexOf('|') + 1);

        if (mainName.length() == 0) {
            return null;
        }

        FileObject mainTemplate = FileUtil.getConfigFile(template);

        if (mainTemplate == null) {
            return null; // Don't know the template
        }
        String createdMainName = mainName;
         if (mainName.indexOf('\\') > 0 || mainName.indexOf('/') > 0) {
            String absPath = CndPathUtilitities.toAbsolutePath(srcFolder, mainName);
            absPath = FileSystemProvider.getCanonicalPath(srcFolder.getFileSystem(), absPath);
            srcFolder = FileUtil.createFolder(srcFolder, CndPathUtilitities.getDirName(mainName));
            createdMainName = CndPathUtilitities.getBaseName(absPath);
         }

        DataObject mt = DataObject.find(mainTemplate);
        DataFolder pDf = DataFolder.findFolder(srcFolder);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CreateFromTemplateHandler.FREE_FILE_EXTENSION, true);
        params.putAll(templateParams);

        mt.createFromTemplate(pDf, createdMainName, params);

        return mainName;
    }

}


