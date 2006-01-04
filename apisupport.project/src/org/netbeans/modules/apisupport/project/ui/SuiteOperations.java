/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;

/**
 * @author Martin Krauskopf
 */
public final class SuiteOperations implements DeleteOperationImplementation {
    
    private final SuiteProject suite;
    private final FileObject projectDir;
    
    public SuiteOperations(final SuiteProject suite) {
        this.suite = suite;
        this.projectDir = suite.getProjectDirectory();
    }
    
    public void notifyDeleting() throws IOException {
        FileObject buildXML = projectDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        ActionUtils.runTarget(buildXML, new String[] { ActionProvider.COMMAND_CLEAN }, null).waitFinished();
        
        SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
        for (Iterator it = spp.getSubprojects().iterator(); it.hasNext();) {
            NbModuleProject suiteComponent = (NbModuleProject) it.next();
            SuiteUtils.removeModuleFromSuite(suiteComponent);
        }
    }
    
    public void notifyDeleted() throws IOException {
        suite.getHelper().notifyDeleted();
    }
    
    public List/*<FileObject>*/ getMetadataFiles() {
        List/*<FileObject>*/ files = new ArrayList();
        addFile(GeneratedFilesHelper.BUILD_XML_PATH, files); // NOI18N
        addFile("nbproject", files); // NOI18N
        addFile(".cvsignore", files); // NOI18N
        return files;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        return Collections.EMPTY_LIST;
    }
    
    private void addFile(String fileName, List/*<FileObject>*/ result) {
        FileObject file = projectDir.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }
    
}
