/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectOperationsImplementation.DeleteOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class WebProjectOperations implements DeleteOperationImplementation {
    
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
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        
        FileObject docRoot = project.getAPIWebModule().getDocumentBase();
        if (docRoot != null)
            files.add(docRoot);
        
        FileObject confDir = project.getWebModule().getConfDir();
        if (confDir != null)
            files.add(confDir);
        
        SourceRoots src = project.getSourceRoots();
        FileObject[] srcRoots = src.getRoots();
        
        for (int cntr = 0; cntr < srcRoots.length; cntr++) {
            files.add(srcRoots[cntr]);
        }
        
        SourceRoots test = project.getTestSourceRoots();
        FileObject[] testRoots = test.getRoots();
        
        for (int cntr = 0; cntr < testRoots.length; cntr++) {
            files.add(testRoots[cntr]);
        }
        
//        addFile(projectDirectory, "manifest.mf", files); // NOI18N
        
        return files;
    }
    
    public void performClean() throws IOException {
        WebActionProvider ap = (WebActionProvider) project.getLookup().lookup(WebActionProvider.class);
        
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
//        ap.invokeAction(ActionProvider.COMMAND_CLEAN, context);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getAntProjectHelper().notifyDeleted();
    }
    
}
