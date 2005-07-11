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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ProjectOperationsImplementation.DeleteOperationImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Jan Lahoda
 */
public class FreeformProjectOperations implements DeleteOperationImplementation {
    
    private FreeformProject project;
    
    public FreeformProjectOperations(FreeformProject project) {
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
        
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element foldersEl = Util.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        List/*<Element>*/ folders = Util.findSubElements(foldersEl);
        List/*<FileObject>*/ result = new ArrayList/*<FileObject>*/();

        for (Iterator i = folders.iterator(); i.hasNext(); ) {
            Element el = (Element) i.next();
            
            if ("source-folder".equals(el.getLocalName()) && FreeformProjectType.NS_GENERAL.equals(el.getNamespaceURI())) {
                addFile(el, result);
            }
        }
        
        addFile(project.getProjectDirectory(), "build.xml", result); // NOI18N
        
        return result;
    }
    
    private void addFile(Element folder, List/*<FileObject>*/ result) {
        Element location = Util.findElement(folder, "location", FreeformProjectType.NS_GENERAL);
        
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
    
    public void performClean() throws IOException {
        //TODO: invoke clean action if bound.
    }
    
    public void notifyDeleted() throws IOException {
        project.helper().notifyDeleted();
    }
    
}
