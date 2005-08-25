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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class EarProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private EarProject project;
    
    public EarProjectOperations(EarProject project) {
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
        
        FileObject src = project.getSourceDirectory();
        if (src != null)
            files.add(src);
        
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        EarActionProvider ap = (EarActionProvider) project.getLookup().lookup(EarActionProvider.class);
        
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
        if (project == original) { //TODO: this is illegal
            //do nothing for the original project.
            return ;
        }
        
        project.setName(nueName);
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (project == original) { //TODO: this is illegal
            //do nothing for the original project.
            return ;
        }
        
        project.setName(nueName);
    }
        
}
