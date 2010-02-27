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

package org.netbeans.modules.cnd.discovery.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class to create a project for well known project.
 * Original file was written by Andrey Gubichev for make2netbeans module.
 * This file was adapted to create OpenSolaris projects.
 * @author Alexander Simon
 */
public class ProjectCreator {
    private static final String Sun = "SunStudio"; // NOI18N
    private static final String GNU = "GNU"; // NOI18N
    //default makefile name
    private static final String MAKEFILE_NAME = "Makefile"; // NOI18N
    //project type
    private static final String TYPE = "org.netbeans.modules.cnd.makeproject"; // NOI18N
    //configuration namespace
    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/make-project/1"; // NOI18N
    private String workingDir;
    private String buildCommand;
    private String cleanCommand;
    private String output;
    private String makefileName;
    private List<File> sourceFiles;
    private List<String> requiredProjects;
    private String makefilePath;
    private String projectFolder;
    private String baseDir;
    private String buildScript;
    private Folder rootFolder;
    private DiscoveryDescriptor discovery;

    /** Creates a new instance of ProjectCreator */
    public ProjectCreator(DiscoveryDescriptor discovery) {
        makefileName = MAKEFILE_NAME;
        this.discovery = discovery;
    }

    /**
     * initialize
     * @param newProjectFolder project folder
     * @param newWorkingDir working directory (for build and clean commands)
     * @param newMakefilePath path to existing makefile
     */
    public void init(String newProjectFolder, String newWorkingDir, String newMakefilePath, String buildScript) {
        this.projectFolder = newProjectFolder;
        this.workingDir = newWorkingDir;
        this.makefilePath = newMakefilePath;
        this.buildScript = buildScript;
    }

    /**
     *
     * @param com new build command
     */
    public void setBuildCommand(String com) {
        buildCommand = com;
    }

    /**
     *
     * @param com new clean command
     */
    public void setCleanCommand(String com) {
        cleanCommand = com;
    }

    /**
     *
     * @param out new output
     */
    public void setOutput(String out) {
        output = out;
    }

    /**
     *
     * @param p list of source files to be added to the project
     */
    public void setSourceFiles(List<File> p) {
        sourceFiles = p;
    }

    public void setRequiredProjects(List<String> p) {
        requiredProjects = p;
    }

    public List<String> getProjectLevelInludes(){
        return Collections.<String>emptyList();
    }

    public List<String> getProjectLevelMacros(){
        return Collections.<String>emptyList();
    }
    
    /**
     * Create project
     * @param name project name
     * @return the helper object permitting it to be further customized
     * @throws java.io.IOException see createProject(File, String, String, Configuration[], Iterator, Iterator)
     */
    public AntProjectHelper createProject(String name, String displayName, 
            Set<String> folders, Set<String> libs) throws IOException {
        File dirF = new File(projectFolder);
        if (dirF != null) {
            dirF = CndFileUtils.normalizeFile(dirF);
        }
        String target = "Default"; // NOI18N

        // TODO: create localhost based project
        MakeConfiguration extConf = new MakeConfiguration(dirF.getPath(), target, MakeConfiguration.TYPE_MAKEFILE, HostInfoUtils.LOCALHOST);
        String workingDirRel = CndPathUtilitities.toRelativePath(dirF.getPath(), CndPathUtilitities.naturalize(workingDir));
        workingDirRel = CndPathUtilitities.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        if (displayName.indexOf(".lib.")>0 || displayName.indexOf(".cmd.")>0) { // NOI18N
            extConf.getMakefileConfiguration().getBuildCommand().setValue("bldenv -d ../../../../"+buildScript+" 'dmake all'"); // NOI18N
            extConf.getMakefileConfiguration().getCleanCommand().setValue("bldenv -d ../../../../"+buildScript+" 'dmake clobber'"); // NOI18N
        } else {
            extConf.getMakefileConfiguration().getBuildCommand().setValue("bldenv -d ../../../"+buildScript+" 'dmake all'"); // NOI18N
            extConf.getMakefileConfiguration().getCleanCommand().setValue("bldenv -d ../../../"+buildScript+" 'dmake clobber'"); // NOI18N
        }
        extConf.getMakefileConfiguration().getOutput().setValue(output);
        
        if (requiredProjects != null) {
            for(String sub : requiredProjects) {
                extConf.getRequiredProjectsConfiguration().add(new LibraryItem.ProjectItem(new MakeArtifact(
                        sub, //String projectLocation
                        0, // int configurationType
                        "Default", // String configurationName // NOI18N
                        true, // boolean active
                        false, // boolean build
                        sub, // String workingDirectory
                        "${MAKE}  -f "+sub+"-Makefile.mk CONF=Default", // String buildCommand // NOI18N
                        "${MAKE}  -f "+sub+"-Makefile.mk CONF=Default clean", // String cleanCommand // NOI18N
                        "" //String output
                        )));
            }
        }
        
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            extConf.getCompilerSet().setCompilerSetName(new StringConfiguration(null, ProjectCreator.Sun));
        } else {
            extConf.getCompilerSet().setCompilerSetName(new StringConfiguration(null, ProjectCreator.GNU));
        }
        
        extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(getProjectLevelInludes());
        extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(getProjectLevelInludes());
        extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(getProjectLevelMacros());
        extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(getProjectLevelMacros());
        Iterator<String> importantItemsIterator = null;
        if (makefilePath != null && makefilePath.length() > 0) {
            List<String> importantItems = new ArrayList<String>();
            makefilePath = CndPathUtilitities.toRelativePath(dirF.getPath(), CndPathUtilitities.naturalize(makefilePath));
            makefilePath = CndPathUtilitities.normalize(makefilePath);
            importantItems.add(makefilePath);
            importantItemsIterator = importantItems.iterator();
        }

        Iterator<File> it = null;
        if (sourceFiles != null) {
            it = sourceFiles.iterator();
        }
        AntProjectHelper h1 = null;
        makefileName = name + "-" + makefileName + ".mk"; // NOI18N
        h1 = createProject(dirF, displayName, makefileName, new MakeConfiguration[]{extConf},
                it, importantItemsIterator, folders, libs);
        return h1;
    }

    /**
     * Create a new  Make project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public AntProjectHelper createProject(File dir, String displayName, String makefileName, Configuration[] confs,
                            Iterator<File> sourceFiles, Iterator<String> importantItems,
                            Set<String> folders, Set<String> libs) throws IOException {
        FileObject dirFO = createProjectDir(dir);
        AntProjectHelper h = createProject(dirFO, displayName, makefileName, confs, sourceFiles, importantItems);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        boolean successful = applyDiscovery(p, displayName, folders, libs);
        ProjectManager.getDefault().saveProject(p);
        if (!successful) {
            removeProjectDir(dir);
            return null;
        }
        ProjectManager.getDefault().clearNonProjectCache();
        return h;
    }

    //Create a project with specified project folder, makefile, name, source files and important items
    private AntProjectHelper createProject(FileObject dirFO, String displayName, String makefileName,
            Configuration[] confs, Iterator<File> sourceFiles, Iterator<String> importantItems) throws IOException {
        //Create a helper object
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(displayName));
        data.appendChild(nameEl);
        Element nativeProjectType = doc.createElementNS(PROJECT_CONFIGURATION_NAMESPACE, "make-project-type"); // NOI18N
        nativeProjectType.appendChild(doc.createTextNode("" + 0));
        data.appendChild(nativeProjectType);
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        // Create new project descriptor with default configurations and save it to disk
        MakeConfigurationDescriptor projectDescriptor = new MakeConfigurationDescriptor(FileUtil.toFile(dirFO).getPath());
        projectDescriptor.setState(State.READY);
        projectDescriptor.setProjectMakefileName(makefileName);
        projectDescriptor.init(confs);
        baseDir = projectDescriptor.getBaseDir();
        projectDescriptor.initLogicalFolders(null, false, null, importantItems, null);
        rootFolder = projectDescriptor.getLogicalFolders();
        //projectDescriptor.addSourceRootRaw(workingDir);
        projectDescriptor.addSourceRootRaw(CndPathUtilitities.toRelativePath(baseDir, workingDir));
        
        addFiles(sourceFiles);
        
        projectDescriptor.save();
        
        // create Makefile
        copyURLFile("/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile", projectDescriptor.getBaseDir() + File.separator + projectDescriptor.getProjectMakefileName()); // NOI18N
        return h;
    }

    private boolean applyDiscovery(Project project, String displayName, 
            Set<String> folders, Set<String> libs) throws IOException{
        discovery.setProject(project);
        new DiscoveryProjectGenerator(discovery).process();
        createAdditionalRequiredProjects(project, displayName, folders, libs);
        return true;
    }
    
    //add source files from filelist
    private void addFiles(Iterator<File> filelist) {
        if (filelist == null) {
            return;
        }
        while (filelist.hasNext()) {
            File f = filelist.next();
            String path = CndPathUtilitities.toRelativePath(workingDir, f.getPath());
            StringTokenizer tok = new StringTokenizer(path, File.separator);
            String relativePath = CndPathUtilitities.toRelativePath(baseDir, f.getPath());
            addFile(rootFolder, tok, f.getPath(), relativePath);
        }
    }

    //auxiliary function for addFiles(Iterator)
    private void addFile(Folder fld, StringTokenizer tok, String path, String relativePath) {
        if (tok.countTokens() == 1) {
            String t = path.substring(0, path.lastIndexOf(File.separator));
            String name = t.substring(t.lastIndexOf(File.separator) + 1);
            Folder top = null;
            top = fld.findFolderByName(name);
            if (top == null) {
                top = new Folder(fld.getConfigurationDescriptor(), fld, name, name, true);
                fld.addFolder(top, true);
            }
            fld = top;
        }
        while (tok.hasMoreElements()) {
            String part = tok.nextToken();
            if (part.contains(":") || part.equals("..")) { // NOI18N
                continue;
            }
            if (part.contains(".")) { // NOI18N
                fld.addItem(new Item(relativePath));
                continue;
            }
            Folder top = null;
            top = fld.findFolderByName(part);
            if (top == null) {
                top = new Folder(fld.getConfigurationDescriptor(), fld, part, part, true);
                fld.addFolder(top, true);
            }
            fld = top;
        }
    }

    //copy from one file with specified URL to another
    private void copyURLFile(String resource, String toFile) throws IOException {
        String fromURL = "nbresloc:"+resource; // NOI18N
        InputStream is = null;
        try {
            URL url = new URL(fromURL);
            is = url.openStream();
        } catch (Exception e) {
            is = MakeConfigurationDescriptor.class.getResourceAsStream(resource);
        }
        if (is != null) {
            FileOutputStream os = new FileOutputStream(toFile);
            FileUtil.copy(is, os);
            is.close();
            os.close();
        }
    }

    //create project directory
    //return FileObject created with specified File dir
    /*package-local*/ static FileObject createProjectDir(File dir) throws IOException {
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO == null && !dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem(dir);
            if (!dir.mkdirs()) {
                throw new IOException("Can not create project folder."); // NOI18N
            }
            refreshFileSystem(dir);
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }

    //refresh file system
    /*package-local*/ static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

    private void createAdditionalRequiredProjects(Project project, String displayName, 
            Set<String> folders, Set<String> libraries){
        if (displayName.indexOf(".sources")>0 || // NOI18N
            displayName.indexOf(".libraries")>0 || // NOI18N
            displayName.indexOf(".uts")>0 || // NOI18N
            displayName.indexOf(".commands")>0) { // NOI18N
            return;
        }
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration conf = makeConfigurationDescriptor.getActiveConfiguration();
        String name = displayName;
        if (displayName.indexOf('.')>0){
            name = displayName.substring(displayName.lastIndexOf('.')+1);
        }
        Set<String> libs = new HashSet<String>();
        if (name.equals("libc")){ // NOI18N
            libraries.remove("c"); // NOI18N
        } else {
            libraries.add("c"); // NOI18N
        }
        for(String lib : libraries){
           lib = "lib"+lib; // NOI18N
           String folder = null;
           for(String f: folders){
               if (f.endsWith("/"+lib)) { // NOI18N
                   folder = f;
                   break;
               }
           }
           if (folder == null) {
               //System.out.println("Not found "+lib+" for "+displayName); // NOI18N
               continue;
           }
           String[] prj = workingDir.substring(workingDir.indexOf("/usr/src/")+9).split("/"); // NOI18N
           String[] lbr = folder.split("/"); // NOI18N
           int start = 0;
           for(int i = 0; i < prj.length; i++){
               if(!prj[i].equals(lbr[i])){
                   break;
               }
               start++;
           }
            StringBuilder res = new StringBuilder();
            for (int i = start; i < prj.length; i++) {
                res.append("../"); // NOI18N
            }
            for (int i = start; i < lbr.length; i++) {
                if (res.length() == 0 || res.charAt(res.length() - 1) != '/') { // NOI18N
                    res.append('/'); // NOI18N
                }
                res.append(lbr[i]);
            }
            libs.add(res.toString());
        }
        for (String sub:libs){
            //System.out.println("Add Required Project "+sub+" in "+displayName); // NOI18N
            makeConfigurationDescriptor.setModified();
            conf.getRequiredProjectsConfiguration().add(new LibraryItem.ProjectItem(new MakeArtifact(sub, //String projectLocation
                    0, // int configurationType
                    "Default", // String configurationName // NOI18N
                    true, // boolean active
                    false, // boolean build
                    sub, // String workingDirectory
                    "${MAKE}  -f " + sub + "-Makefile.mk CONF=Default", // String buildCommand // NOI18N
                    "${MAKE}  -f " + sub + "-Makefile.mk CONF=Default clean", // String cleanCommand // NOI18N
                    "" //String output
                    )));
        }
        makeConfigurationDescriptor.save();
    }

    private void removeProjectDir(File dir) {
        if (dir.exists() && dir.canRead() && dir.isDirectory()) {
            for(File file : dir.listFiles()){
                if (file.isDirectory()){
                    removeProjectDir(file);
                }
            }
            for(File file : dir.listFiles()){
                file.delete();
            }
            dir.delete();
        }
    }

}
