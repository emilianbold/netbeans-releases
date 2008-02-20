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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class EjbJarProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private EjbJarProject project;
    
    public EjbJarProjectOperations(EjbJarProject project) {
        this.project = project;
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
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "catalog.xml", files); //NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        
        FileObject metaInf = project.getEjbModule().getMetaInf();
        if (metaInf != null) {
            files.add(metaInf);
        }
        
        SourceRoots src = project.getSourceRoots();
        FileObject[] srcRoots = src.getRoots();
        
        for (int cntr = 0; cntr < srcRoots.length; cntr++) {
            files.add(srcRoots[cntr]);
        }
        
        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(EjbJarProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject srcDir = project.getAntProjectHelper().resolveFileObject(prop);
            if (projectDirectory != srcDir && !files.contains(srcDir)) {
                files.add(srcDir);
            }
        }
  
        SourceRoots test = project.getTestSourceRoots();
        FileObject[] testRoots = test.getRoots();
        
        for (int cntr = 0; cntr < testRoots.length; cntr++) {
            files.add(testRoots[cntr]);
        }
        
        File resourceDir = project.getEjbModule().getResourceDirectory();
        if (resourceDir != null) {
            FileObject resourceFO = FileUtil.toFileObject(resourceDir);
            if (resourceFO != null) {
                files.add(resourceFO);
            }
        }
        
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        EjbJarActionProvider ap = project.getLookup().lookup(EjbJarActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        //nothing.
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        fixOtherReferences(originalPath);
        
        project.setName(nueName);
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, final String newName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        
	final String oldProjectName = project.getName();

        project.setName(newName);
        project.getReferenceHelper().fixReferences(originalPath);        
        fixOtherReferences(originalPath);
	
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
		AntProjectHelper helper = project.getAntProjectHelper();
		EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

		String jarName = projectProps.get(EjbJarProjectProperties.JAR_NAME);
		String oldName = jarName.substring(0, jarName.length() - 4);
		if (jarName.endsWith(".jar") && oldName.equals(oldProjectName)) { //NOI18N
                    projectProps.put(EjbJarProjectProperties.JAR_NAME, newName + ".jar"); //NOI18N
                }

		helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
            }
        });
    }
    
    private void fixOtherReferences(final File originalPath) {
        final String property = EjbJarProjectProperties.META_INF;
        final File projectDir = FileUtil.toFile(project.getProjectDirectory());
                
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties props = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String path = props.getProperty(property);
                if (path == null) {
                    return;
                }

                if (path.startsWith(originalPath.getAbsolutePath())) {
                    String relative = PropertyUtils.relativizeFile(originalPath, new File(path));
                    String fixedPath = new File(projectDir, relative).getAbsolutePath();
                    props.setProperty(property, fixedPath);
                    project.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                }
            }
        });
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

}
