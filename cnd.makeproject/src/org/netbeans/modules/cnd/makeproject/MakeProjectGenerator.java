/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 * Creates a MakeProject from scratch according to some initial configuration.
 */
public class MakeProjectGenerator {

    private MakeProjectGenerator() {
    }

    public static String getDefaultProjectFolder() {
        return ProjectChooser.getProjectsFolder().getPath();
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
            File projectNameFile = new File(projectFolder, projectName);
            if (!projectNameFile.exists()) {
                break;
            }
            baseCount++;
        }
        return projectName;
    }

    public static MakeProject createBlankProject(boolean open) throws IOException {
        String projectFolder = getDefaultProjectFolder();
        String projectName = getValidProjectName(projectFolder);
        String baseDir = projectFolder + File.separator + projectName;
        MakeConfiguration conf = new MakeConfiguration(baseDir, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/Bundle").getString("DefaultProjectName"), MakeConfiguration.TYPE_MAKEFILE); // FIXUP
        return MakeProjectGenerator.createBlankProject(projectName, projectFolder, new MakeConfiguration[]{conf}, open);
    }

    public static MakeProject createBlankProject(String projectName, String projectFolder, boolean open) throws IOException {
        return createBlankProject(projectName, projectFolder, new MakeConfiguration[0], open);
    }

    public static MakeProject createBlankProject(String projectName, String makefileName, String projectFolder, boolean open) throws IOException {
        return createBlankProject(projectName, makefileName, projectFolder, new MakeConfiguration[0], open);
    }

    public static MakeProject createBlankProject(String projectName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
        return createBlankProject(projectName, MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME, projectFolder, confs, open);
    }

    public static MakeProject createBlankProject(String projectName, String makefileName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
        File projectNameFile = new File(projectFolder, projectName);
        if (confs == null) {
            confs = new MakeConfiguration[0];
        }

        // work in a copy of confs
        MakeConfiguration[] copyConfs = new MakeConfiguration[confs.length];
        for (int i = 0; i < confs.length; i++) {
            copyConfs[i] = (MakeConfiguration) confs[i].clone();
            copyConfs[i].setBaseDir(projectNameFile.getPath());
            RunProfile profile = (RunProfile) copyConfs[i].getAuxObject(RunProfile.PROFILE_ID);
            profile.setBuildFirst(false);
        }

        FileObject dirFO = createProjectDir(projectNameFile);
        createProject(dirFO, projectName, makefileName, copyConfs, null, null, null, null, true, null);
        MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);

        if (open) {
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
    public static MakeProject createProject(File dir, String name, String makefileName, MakeConfiguration[] confs, Iterator<SourceFolderInfo> sourceFolders, String sourceFoldersFilter, Iterator<SourceFolderInfo> testFolders, Iterator<String> importantItems, String mainFile) throws IOException {
        FileObject dirFO = createProjectDir(dir);
        AntProjectHelper h = createProject(dirFO, name, makefileName, confs, sourceFolders, sourceFoldersFilter, testFolders, importantItems, false, mainFile); //NOI18N
        MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        //FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
        return p;
    }

    /*
    public static AntProjectHelper createProject(final File dir, final String name, final File sourceFolder, final File testFolder) throws IOException {
    System.out.println("createProject2 ");
    assert sourceFolder != null : "Source folder must be given";   //NOI18N
    final FileObject dirFO = createProjectDir (dir);
    // this constructor creates only java application type
    final AntProjectHelper h = createProject(dirFO, name, null, null, null, null, false, 0, null, null);
    final MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
    final ReferenceHelper refHelper = p.getReferenceHelper();
    try {
    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
    public Object run() throws Exception {
    String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
    EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
    props.put("src.dir",srcReference);          //NOI18N
    String testLoc;
    if (testFolder == null) {
    testLoc = NbBundle.getMessage (MakeProjectGenerator.class,"TXT_DefaultTestFolderName");
    File f = new File (dir,testLoc);    //NOI18N
    f.mkdirs();
    }
    else {
    if (!testFolder.exists()) {
    testFolder.mkdirs();
    }
    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
    testLoc = refHelper.createForeignFileReference(testFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
    props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
    }
    props.put("test.src.dir",testLoc);    //NOI18N
    h.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    ProjectManager.getDefault().saveProject (p);
    return null;
    }
    });
    } catch (MutexException me ) {
    ErrorManager.getDefault().notify (me);
    }
    return h;
    return null;
    }
     */
    private static AntProjectHelper createProject(FileObject dirFO, String name, String makefileName, Configuration[] confs, final Iterator<SourceFolderInfo> sourceFolders, final String sourceFoldersFilter, final Iterator<SourceFolderInfo> testFolders, final Iterator<String> importantItems, boolean saveNow, String mainFile) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, MakeProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        //Element minant = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        //minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        //data.appendChild(minant);
        Element nativeProjectType = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "make-project-type"); // NOI18N
        nativeProjectType.appendChild(doc.createTextNode("" + 0)); // NOI18N
        data.appendChild(nativeProjectType);
        h.putPrimaryConfigurationData(data, true);

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //ep.setProperty("make.configurations", "");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //ep.setProperty("application.args", ""); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        // Create new project descriptor with default configurations and save it to disk.
        final MakeConfigurationDescriptor projectDescriptor = new MakeConfigurationDescriptor(FileUtil.toFile(dirFO).getPath());
        projectDescriptor.setProjectMakefileName(makefileName);
        projectDescriptor.init(confs);
        projectDescriptor.setState(State.READY);

        Project project = projectDescriptor.getProject();
        // create main source file
        final String mainFilePath;
        if (mainFile != null) {
            mainFilePath = createMain(mainFile, dirFO);
        } else {
            mainFilePath = null;
        }
        if (sourceFoldersFilter != null && !MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN.equals(sourceFoldersFilter)) {
            projectDescriptor.setFolderVisibilityQuery(sourceFoldersFilter);
        }
        Runnable task = new Runnable() {

            @Override
            public void run() {
                projectDescriptor.initLogicalFolders(sourceFolders, sourceFolders == null, testFolders, importantItems, mainFilePath); // FIXUP: need a better check whether logical folder should be ccreated or not.
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
        // create Makefile
        copyURLFile("nbresloc:/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile", // NOI18N
                projectDescriptor.getBaseDir() + File.separator + projectDescriptor.getProjectMakefileName());
        return h;
    }

    private static void copyURLFile(String fromURL, String toFile) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(fromURL);
            is = url.openStream();
        } catch (Exception e) {
            // FIXUP
        }
        if (is != null) {
            FileOutputStream os = new FileOutputStream(toFile);
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
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        String line;

        while ((line = br.readLine()) != null) {
            bw.write(line + "\n"); // NOI18N
        }
        bw.flush();
    }

    private static FileObject createProjectDir(File dir) throws IOException {
        FileObject dirFO;
        if (!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            // refreshFileSystem (dir); // See 136445
            if (!dir.mkdirs()) {
                throw new IOException("Can not create project folder."); // NOI18N
            }
            // refreshFileSystem (dir); // See 136445
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }

    private static String createMain(String mainFile, FileObject srcFolder) throws IOException {
        String mainName = mainFile.substring(0, mainFile.indexOf('|'));
        String template = mainFile.substring(mainFile.indexOf('|') + 1);

        if (mainName.length() == 0) {
            return null;
        }

        FileObject mainTemplate = FileUtil.getConfigFile(template);

        if (mainTemplate == null) {
            return null; // Don't know the template
        }

        DataObject mt = DataObject.find(mainTemplate);
        DataFolder pDf = DataFolder.findFolder(srcFolder);
        mt.createFromTemplate(pDf, mainName);

        return mainName;
    }
//    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
//        File rootF = dir;
//        while (rootF.getParentFile() != null /*UNC*/&& rootF.getParentFile().exists()) {
//            rootF = rootF.getParentFile();
//    }
}


