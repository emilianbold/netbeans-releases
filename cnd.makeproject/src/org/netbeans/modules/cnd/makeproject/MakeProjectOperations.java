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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
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

    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);

        if (file != null) {
            result.add(file);
        }
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        addFile(projectDirectory, MakeConfiguration.NBPROJECT_FOLDER, files); // NOI18N
        addFile(projectDirectory, pdp.getConfigurationDescriptor().getProjectMakefileName(), files); // NOI18N

        return files;
    }

    @Override
    public List<FileObject> getDataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();

        List<FileObject> files = new ArrayList<FileObject>();
        FileObject[] children = projectDirectory.getChildren();
        List metadataFiles = getMetadataFiles();
        for (int i = 0; i < children.length; i++) {
            if (metadataFiles.indexOf(children[i]) < 0) {
                files.add(children[i]);
            }
        }
        if (files.isEmpty()) {
            // FIXUP: Don't return empty list. If the list is empty, the "Also Delete Sources" checkbox in the dialog is disabled and the project dir cannot be deleted.
            // IZ?????
            files.add(projectDirectory);
        }
        return files;
    }

    @Override
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

    @Override
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject instanceof NativeProjectProvider) {
            ((NativeProjectProvider) nativeProject).fireProjectDeleted();
        }

        // Notify configuration listeners (worka-round for http://www.netbeans.org/issues/show_bug.cgi?id=167259
        MakeProjectConfigurationProvider makeProjectConfigurationProvider = project.getLookup().lookup(MakeProjectConfigurationProvider.class);
        if (makeProjectConfigurationProvider != null) {
            makeProjectConfigurationProvider.propertyChange(null);
        }
    }

    @Override
    public void notifyCopying() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        pdp.getConfigurationDescriptor().save();
        // Also move private
        MakeSharabilityQuery makeSharabilityQuery = project.getLookup().lookup(MakeSharabilityQuery.class);
        makeSharabilityQuery.setPrivateShared(true);
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return;
        }

        // Update all external relative paths
        String originalFilePath = originalPath.getPath();
        String newFilePath = FileUtil.toFile(project.getProjectDirectory()).getPath();
        if (!originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = CndPathUtilitities.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = CndPathUtilitities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = CndPathUtilitities.normalize(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.setRelativeOffset(fromNewToOriginal);
        }

//      fixDistJarProperty (nueName);
//      project.getReferenceHelper().fixReferences(originalPath);

        project.setName(nueName);

        MakeSharabilityQuery makeSharabilityQuery = original.getLookup().lookup(MakeSharabilityQuery.class);
        makeSharabilityQuery.setPrivateShared(false);
    }

    @Override
    public void notifyMoving() throws IOException {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        pdp.getConfigurationDescriptor().save();
        notifyDeleting();
        // Also move private
        MakeSharabilityQuery makeSharabilityQuery = project.getLookup().lookup(MakeSharabilityQuery.class);
        makeSharabilityQuery.setPrivateShared(true);
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return;
        }
        // Update all external relative paths
        String originalFilePath = originalPath.getPath();
        String newFilePath = FileUtil.toFile(project.getProjectDirectory()).getPath();
        if (!originalFilePath.equals(newFilePath)) {
            //String fromOriginalToNew = CndPathUtilitities.getRelativePath(originalFilePath, newFilePath);
            String fromNewToOriginal = CndPathUtilitities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
            fromNewToOriginal = CndPathUtilitities.normalize(fromNewToOriginal);
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.setRelativeOffset(fromNewToOriginal);
        }
//      fixDistJarProperty (nueName);
        project.setName(nueName);
//	project.getReferenceHelper().fixReferences(originalPath);
    }

    private static boolean isParent(File folder, File fo) {
        if (folder.equals(fo)) {
            return false;
        }

        while (fo != null) {
            if (fo.equals(folder)) {
                return true;
            }

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
