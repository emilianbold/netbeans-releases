/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MakeProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private MakeProject project;
    
    public MakeProjectOperations(MakeProject project) {
        this.project = project;
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List/*<FileObject>*/ result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List/*<FileObject>*/ getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, ((MakeConfigurationDescriptor)pdp.getConfigurationDescriptor()).getProjectMakefileName(), files); // NOI18N
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        String projectMakefileName = ((MakeConfigurationDescriptor)pdp.getConfigurationDescriptor()).getProjectMakefileName();
        
        List/*<FileObject>*/ files = new ArrayList();
        FileObject[] children = projectDirectory.getChildren();
        List metadataFiles = getMetadataFiles();
        for (int i = 0; i < children.length; i++) {
            if (metadataFiles.indexOf(children[i]) < 0)
                files.add(children[i]);
        }
        return files;
    }
    
    public void notifyDeleting() throws IOException {
//        J2SEActionProvider ap = (J2SEActionProvider) project.getLookup().lookup(J2SEActionProvider.class);
//        
//        assert ap != null;
//        
//        Lookup context = Lookups.fixed(new Object[0]);
//        Properties p = new Properties();
//        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
//        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
//        
//        assert targetNames != null;
//        assert targetNames.length > 0;
//        
//        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        pdp.getConfigurationDescriptor().save();
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        
        // Update all external relative paths
        String originalFilePath = originalPath.getPath();
        String newFilePath = FileUtil.toFile(project.getProjectDirectory()).getPath();
        if (!originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = IpeUtils.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
            pdp.setRelativeOffset(fromNewToOriginal);
        }
        
//      fixDistJarProperty (nueName);
//      project.getReferenceHelper().fixReferences(originalPath);
        
        project.setName(nueName);
    }
    
    public void notifyMoving() throws IOException {
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        pdp.getConfigurationDescriptor().save();
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }                
        // Update all external relative paths
        String originalFilePath = originalPath.getPath();
        String newFilePath = FileUtil.toFile(project.getProjectDirectory()).getPath();
        if (!originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = IpeUtils.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
            pdp.setRelativeOffset(fromNewToOriginal);
        }
//      fixDistJarProperty (nueName);
        project.setName(nueName);        
//	project.getReferenceHelper().fixReferences(originalPath);
    }
    
    private static boolean isParent(File folder, File fo) {
        if (folder.equals(fo))
            return false;
        
        while (fo != null) {
            if (fo.equals(folder))
                return true;
            
            fo = fo.getParentFile();
        }
        
        return false;
    }
    
//    private void fixDistJarProperty (final String newName) {
//        ProjectManager.mutex().writeAccess(new Runnable () {
//            public void run () {
//                ProjectInformation pi = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
//                String oldDistJar = pi == null ? null : "${dist.dir}/"+PropertyUtils.getUsablePropertyName(pi.getDisplayName())+".jar"; //NOI18N
//                EditableProperties ep = project.getUpdateHelper().getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                String propValue = ep.getProperty("dist.jar");  //NOI18N
//                if (oldDistJar != null && oldDistJar.equals (propValue)) {
//                    ep.put ("dist.jar","${dist.dir}/"+PropertyUtils.getUsablePropertyName(newName)+".jar"); //NOI18N
//                    project.getUpdateHelper().putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
//                }
//            }
//        });
//    }
    
}
