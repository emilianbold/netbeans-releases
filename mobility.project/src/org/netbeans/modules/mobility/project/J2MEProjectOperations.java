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

package org.netbeans.modules.mobility.project;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Lahoda
 */
public class J2MEProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    final private J2MEProject project;
    final private AntProjectHelper helper;
    final private ReferenceHelper refHelper;
    
    public J2MEProjectOperations(J2MEProject project, AntProjectHelper helper, ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.refHelper = refHelper;
    }
    
    private static void addFile(final FileObject projectDirectory, final String fileName, final List<FileObject> result) {
        final FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        final FileObject projectDirectory = project.getProjectDirectory();
        final List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        final List<FileObject> files = new ArrayList<FileObject>();
        final String srcRoot = helper.getStandardPropertyEvaluator().getProperty("src.dir");
        if (srcRoot != null) {
            final FileObject src = helper.resolveFileObject(srcRoot);
            if (src != null) files.add(src);
        }
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        final J2MEActionProvider ap = project.getLookup().lookup(J2MEActionProvider.class);
        
        assert ap != null;
        
        final Properties p = new Properties();
        final String[] targetNames = ap.getTargetNames(J2MEActionProvider.COMMAND_CLEAN_ALL);
        final FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() {
        helper.notifyDeleted();
    }
    
    public void notifyCopying() {
        //nothing.
    }
    
    public void notifyCopied(final Project original, final File originalPath, final String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        
        project.setName(nueName);
        refHelper.fixReferences(originalPath);
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(final Project original, final File originalPath, final String nueName) {
        if (original == null) {
            helper.notifyDeleted();
            return ;
        }
        
        project.setName(nueName);
        refHelper.fixReferences(originalPath);
    }
}
