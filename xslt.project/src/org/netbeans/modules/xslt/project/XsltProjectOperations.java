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
package org.netbeans.modules.xslt.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltProjectOperations  implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private XsltproProject project;
            
    public XsltProjectOperations(XsltproProject project) {
        this.project = project;
    }

    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "catalog.xml", files); //NOI18N
        addFile(projectDirectory, projectDirectory.getName(), files); //NOI18N
        addFile(projectDirectory, org.netbeans.modules.xml.retriever.XMLCatalogProvider.TYPE_RETRIEVED , files); //NOI18N
        
        return files;
    }

    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
 
        files.add(project.getSourceDirectory());
        
        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(IcanproProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject srcDir = project.getAntProjectHelper().resolveFileObject(prop);
            if (projectDirectory != srcDir && !files.contains(srcDir)) {
                files.add(srcDir);
            }
        }
     
        return files;
    }

    public void notifyDeleting() throws IOException {
       XsltproActionProvider ap = project.getLookup().lookup(XsltproActionProvider.class);
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
       
    public void notifyCopying() throws IOException {
        // do nothing.
        // This does copy the old distribution file over though, which is
        // probably OK because "ant clean" will clean it up.
    }

    public void notifyCopied(Project original, File originalPath, String newName) throws IOException {
        if (original == null) {
            // do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        String oldName = project.getName();
        project.setName(newName);
    }

    public void notifyMoving() throws IOException {
        notifyDeleting();
    }

    public void notifyMoved(Project original, File originalPath, String newName) throws IOException {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        String oldName = project.getName();
        project.setName(newName);
        project.getReferenceHelper().fixReferences(originalPath);
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
}
