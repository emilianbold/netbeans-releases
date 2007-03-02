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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Jan Lahoda
 */
public class FreeformProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private FreeformProject project;
    
    public FreeformProjectOperations(FreeformProject project) {
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
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        Element genldata = project.getPrimaryConfigurationData();
        Element foldersEl = Util.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        List<Element> folders = foldersEl != null ? Util.findSubElements(foldersEl) : Collections.<Element>emptyList();
        List<FileObject> result = new ArrayList<FileObject>();

        for (Element el : folders) {
            if ("source-folder".equals(el.getLocalName()) && FreeformProjectType.NS_GENERAL.equals(el.getNamespaceURI())) { // NOI18N
                addFile(el, result);
            }
        }
        
        addFile(project.getProjectDirectory(), "build.xml", result); // NOI18N
        
        return result;
    }
    
    private void addFile(Element folder, List<FileObject> result) {
        Element location = Util.findElement(folder, "location", FreeformProjectType.NS_GENERAL); // NOI18N
        
        if (location == null) {
            return ;
        }
        
        PropertyEvaluator evaluator = project.evaluator();
        String val = evaluator.evaluate(Util.findText(location));
        
        if (val == null) {
            return ;
        }
        
        File f = project.helper().resolveFile(val);
            
        if (f == null) {
            return ;
        }
        
        FileObject fo = FileUtil.toFileObject(f);
        
        if (fo != null && FileUtil.isParentOf(project.getProjectDirectory(), fo)) {
            result.add(fo);
        }
    }
    
    public void notifyDeleting() throws IOException {
        //TODO: invoke clean action if bound.
    }
    
    public void notifyDeleted() throws IOException {
        project.helper().notifyDeleted();
    }

    public void notifyCopying() throws IOException {
    }

    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            project.setName(nueName);
        }
    }

    public void notifyMoving() throws IOException {
    }

    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            project.setName(nueName);
        } else {
            project.helper().notifyDeleted();
        }
    }
    
}
