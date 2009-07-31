/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class J2SEProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private final J2SEProject project;
    private final J2SEActionProvider actionProvider;
    
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private String appArgs;
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private String workDir;
    
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private String libraryPath;
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private File libraryFile;
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private boolean libraryWithinProject;
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private String absolutesRelPath;
    //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
    private FileSystem configs;
    
    public J2SEProjectOperations(final J2SEProject project, final J2SEActionProvider actionProvider) {
        assert project != null;
        assert actionProvider != null;
        this.project = project;
        this.actionProvider = actionProvider;
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, J2SEProjectUtil.getBuildXmlName(project), files); // NOI18N
        addFile(projectDirectory, "xml-resources", files); //NOI18N
        addFile(projectDirectory, "catalog.xml", files); //NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        files.addAll(Arrays.asList(project.getSourceRoots().getRoots()));
        files.addAll(Arrays.asList(project.getTestSourceRoots().getRoots()));
        addFile(project.getProjectDirectory(), "manifest.mf", files); // NOI18N
        addFile(project.getProjectDirectory(), "master.jnlp", files); // NOI18N
        // add libraries folder if it is within project:
        AntProjectHelper helper = project.getAntProjectHelper();
        if (helper.getLibrariesLocation() != null) {
            File f = helper.resolveFile(helper.getLibrariesLocation());
            if (f != null && f.exists()) {
                FileObject libFolder = FileUtil.toFileObject(f).getParent();
                if (FileUtil.isParentOf(project.getProjectDirectory(), libFolder)) {
                    files.add(libFolder);
                }
            }
        }
        return files;
    }
    
    public void notifyDeleting() throws IOException {                
        Properties p = new Properties();
        String[] targetNames = this.actionProvider.getTargetNames(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY, p, false);
        FileObject buildXML = J2SEProjectUtil.getBuildXml(project);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        rememberLibraryLocation();
        readPrivateProperties();
        rememberConfigurations();
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        J2SEProjectOperations origOperations = original.getLookup().lookup(J2SEProjectOperations.class);
        fixLibraryLocation(origOperations);
        fixPrivateProperties(origOperations);
        fixDistJarProperty (nueName);
        project.getReferenceHelper().fixReferences(originalPath);        
        project.setName(nueName);
        restoreConfigurations(origOperations);
    }
    
    public void notifyMoving() throws IOException {
        if (!this.project.getUpdateHelper().requestUpdate()) {
            throw new IOException (NbBundle.getMessage(J2SEProjectOperations.class,
                "MSG_OldProjectMetadata"));
        }
        rememberLibraryLocation();
        readPrivateProperties ();
        rememberConfigurations();
        notifyDeleting();
    }
            
    public void notifyMoved(Project original, File originalPath, String nueName) {        
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }                
        J2SEProjectOperations origOperations = original.getLookup().lookup(J2SEProjectOperations.class);
        fixLibraryLocation(origOperations);
        fixPrivateProperties (origOperations);
        fixDistJarProperty (nueName);
        project.setName(nueName);        
	project.getReferenceHelper().fixReferences(originalPath);
        restoreConfigurations(origOperations);
    }

    private void fixLibraryLocation(J2SEProjectOperations original) throws IllegalArgumentException {
        String libPath = original.libraryPath;
        if (libPath != null) {
            if (!new File(libPath).isAbsolute()) {
                //relative path to libraries
                if (!original.libraryWithinProject) {
                    File file = original.libraryFile;
                    if (file == null) {
                        // could happen in some rare cases, but in that case the original project was already broken, don't fix.
                        return;
                    }
                    String relativized = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
                    if (relativized != null) {
                        project.getAntProjectHelper().setLibrariesLocation(relativized);
                    } else {
                        //cannot relativize, use absolute path
                        project.getAntProjectHelper().setLibrariesLocation(file.getAbsolutePath());
                    }
                } else {
                    //got copied over to new location.. the relative path is the same..
                }
            } else {

                //absolute path to libraries..
                if (original.libraryWithinProject) {
                    if (original.absolutesRelPath != null) {
                        project.getAntProjectHelper().setLibrariesLocation(PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), original.absolutesRelPath).getAbsolutePath());
                    }
                } else {
                    // absolute path to an external folder stays the same.
                }
            }
        }
    }
    
    private void readPrivateProperties () {
        ProjectManager.mutex().readAccess(new Runnable() {
            public void run () {
                appArgs = project.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(J2SEProjectProperties.APPLICATION_ARGS);
                workDir = project.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(J2SEProjectProperties.RUN_WORK_DIR);        
            }
        });
    }
    
    private void fixPrivateProperties (final J2SEProjectOperations original) {
        if (original != null && (original.appArgs != null || original.workDir != null)) {
            ProjectManager.mutex().writeAccess(new Runnable () {
                public void run () {
                    final EditableProperties ep = project.getUpdateHelper().getProperties (AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    if (original.appArgs != null) {
                        ep.put(J2SEProjectProperties.APPLICATION_ARGS, original.appArgs);
                    }
                    if (original.workDir != null) {
                        ep.put (J2SEProjectProperties.RUN_WORK_DIR, original.workDir);
                    }
                    project.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                }
            });
        }
    }
    
    private void fixDistJarProperty (final String newName) {
        ProjectManager.mutex().writeAccess(new Runnable () {
            public void run () {
                ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
                String oldDistJar = pi == null ? null : "${dist.dir}/"+PropertyUtils.getUsablePropertyName(pi.getDisplayName())+".jar"; //NOI18N
                EditableProperties ep = project.getUpdateHelper().getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String propValue = ep.getProperty("dist.jar");  //NOI18N
                if (oldDistJar != null && oldDistJar.equals (propValue)) {
                    ep.put ("dist.jar","${dist.dir}/"+PropertyUtils.getUsablePropertyName(newName)+".jar"); //NOI18N
                    project.getUpdateHelper().putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                }
            }
        });
    }

    private void rememberLibraryLocation() {
        libraryWithinProject = false;
        absolutesRelPath = null;
        libraryPath = project.getAntProjectHelper().getLibrariesLocation();
        if (libraryPath != null) {
            File prjRoot = FileUtil.toFile(project.getProjectDirectory());
            libraryFile = PropertyUtils.resolveFile(prjRoot, libraryPath);
            if (FileOwnerQuery.getOwner(libraryFile.toURI()) == project && 
                    libraryFile.getAbsolutePath().startsWith(prjRoot.getAbsolutePath())) {
                //do not update the relative path if within the project..
                libraryWithinProject = true;
                FileObject fo = FileUtil.toFileObject(libraryFile);
                if (new File(libraryPath).isAbsolute() && fo != null) {
                    // if absolte path within project, it will get moved/copied..
                    absolutesRelPath = FileUtil.getRelativePath(project.getProjectDirectory(), fo);
                }
            }
        }
    }
    
    private void rememberConfigurations () {
        FileObject fo = project.getProjectDirectory().getFileObject(J2SEConfigurationProvider.CONFIG_PROPS_PATH);
        if (fo != null) {
            //Has configurations
            try {
                FileSystem fs = FileUtil.createMemoryFileSystem();
                FileUtil.copyFile(fo, fs.getRoot(),fo.getName());
                fo = project.getProjectDirectory().getFileObject("nbproject/private/configs");      //NOI18N
                if (fo != null && fo.isFolder()) {
                    FileObject cfgs = fs.getRoot().createFolder("configs");                         //NOI18N
                    for (FileObject child : fo.getChildren()) {
                        FileUtil.copyFile(child, cfgs, child.getName());
                    }
                }
                configs = fs;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    private void restoreConfigurations (final J2SEProjectOperations original) {
        final FileSystem fs = original.configs;
        original.configs = null;
        if (fs != null) {
            try {
                FileObject fo = fs.getRoot().getFileObject("config.properties");        //NOI18N
                if (fo != null) {
                    FileObject privateFolder = FileUtil.createFolder(project.getProjectDirectory(), "nbproject/private");  //NOI18N
                    if (privateFolder != null) {
                        // #131857: SyncFailedException : check for file existence before FileUtil.copyFile
                        FileObject oldFile = privateFolder.getFileObject(fo.getName(), fo.getExt());
                        if (oldFile != null) {
                            oldFile.delete();
                        }

                        FileUtil.copyFile(fo, privateFolder, fo.getName());
                    }                
                }
                fo = fs.getRoot().getFileObject("configs");                             //NOI18N
                if (fo != null) {
                    FileObject configsFolder = FileUtil.createFolder(project.getProjectDirectory(), "nbproject/private/configs");  //NOI18N
                    if (configsFolder != null) {
                        for (FileObject child : fo.getChildren()) {
                            FileUtil.copyFile(child, configsFolder, child.getName());
                        }
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
}
