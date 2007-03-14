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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathModifier;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
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
public class WebProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private WebProject project;
    
    public WebProjectOperations(WebProject project) {
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
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "catalog.xml", files); //NOI18N
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        List/*<FileObject>*/ files = new ArrayList();
        
        FileObject docRoot = project.getAPIWebModule().getDocumentBase();
        if (docRoot != null)
            files.add(docRoot);
        
        FileObject confDir = project.getWebModule().getConfDir();
        if (confDir != null)
            files.add(confDir);
        
        File resourceDir = project.getWebModule().getEnterpriseResourceDirectory();
        if (resourceDir != null) {
            FileObject resourceFO = FileUtil.toFileObject(resourceDir);
            if (resourceFO != null)
                files.add(resourceFO);
        }
        
        SourceRoots src = project.getSourceRoots();
        FileObject[] srcRoots = src.getRoots();

        for (int cntr = 0; cntr < srcRoots.length; cntr++) {
            files.add(srcRoots[cntr]);
        }

        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(WebProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject srcDir = project.getAntProjectHelper().resolveFileObject(prop);
            if (projectDirectory != srcDir && !files.contains(srcDir))
                files.add(srcDir);
        }
        
        SourceRoots test = project.getTestSourceRoots();
        FileObject[] testRoots = test.getRoots();
        
        for (int cntr = 0; cntr < testRoots.length; cntr++) {
            files.add(testRoots[cntr]);
        }

        return files;
    }
    
    public void notifyDeleting() throws IOException {
        WebActionProvider ap = (WebActionProvider) project.getLookup().lookup(WebActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
        
        WebProjectClassPathModifier cpMod = (WebProjectClassPathModifier) project.getLookup().lookup(WebProjectClassPathModifier.class);
        cpMod.notifyDeleting();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        //nothing.
    }
    
    public void notifyCopied(Project original, File originalPath, final String newName) {
        if (original == null) {
            //nothing for the original project
            return ;
        }
        
	final String oldProjectName = project.getName();
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        project.setName(newName);
        
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
		AntProjectHelper helper = project.getAntProjectHelper();
		EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

		String warName = (String) projectProps.get(WebProjectProperties.WAR_NAME);
		String warEarName = (String) projectProps.get(WebProjectProperties.WAR_EAR_NAME);
		String oldName = warName.substring(0, warName.length() - 4);
		if (warName.endsWith(".war") && oldName.equals(oldProjectName)) //NOI18N
		    projectProps.put(WebProjectProperties.WAR_NAME, PropertyUtils.getUsablePropertyName(newName) + ".war"); //NOI18N
		if (warEarName.endsWith(".war") && oldName.equals(oldProjectName)) //NOI18N
		    projectProps.put(WebProjectProperties.WAR_EAR_NAME, PropertyUtils.getUsablePropertyName(newName) + ".war"); //NOI18N
                
                ProjectWebModule wm = (ProjectWebModule) project.getLookup ().lookup(ProjectWebModule.class);
                if (wm != null) //should not be null
                    wm.setContextPath("/" + newName);
                
		helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
            }
        });
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

        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
		AntProjectHelper helper = project.getAntProjectHelper();
		EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
		EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

		String warName = (String) projectProps.get(WebProjectProperties.WAR_NAME);
		String warEarName = (String) projectProps.get(WebProjectProperties.WAR_EAR_NAME);
		String oldName = warName.substring(0, warName.length() - 4);
		if (warName.endsWith(".war") && oldName.equals(oldProjectName)) //NOI18N
		    projectProps.put(WebProjectProperties.WAR_NAME, newName + ".war"); //NOI18N
		if (warEarName.endsWith(".war") && oldName.equals(oldProjectName)) //NOI18N
		    projectProps.put(WebProjectProperties.WAR_EAR_NAME, newName + ".war"); //NOI18N

		ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
		String serverId = privateProps.getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
		String oldCP = wm.getContextPath(serverId);
		if (oldCP != null && oldName.equals(oldCP.substring(1)))
		    wm.setContextPath(serverId, "/" + newName); //NOI18N

		helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
            }
        });
    }

}
