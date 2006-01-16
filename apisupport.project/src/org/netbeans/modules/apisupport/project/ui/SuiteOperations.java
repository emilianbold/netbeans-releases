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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.BasicBrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * @author Martin Krauskopf
 */
public final class SuiteOperations implements DeleteOperationImplementation,
        MoveOperationImplementation {
    
    private static final Map/*<String, Set<Project>>*/ TEMPORARY_CACHE = new HashMap();
    
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
    
    public void notifyMoving() throws IOException {
        Set/*<Project>*/ subprojects = SuiteUtils.getSubProjects(suite);
        if (!subprojects.isEmpty()) {
            // XXX using suite's name is probably weak. Consider another solution. E.g.
            // store some "private" property and than read it.
            TEMPORARY_CACHE.put(ProjectUtils.getInformation(suite).getName(), subprojects);
        }
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) { // called on the original project
            suite.getHelper().notifyDeleted();
        } else { // called on the new project
            String name = ProjectUtils.getInformation(suite).getName();
            Set/*<Project>*/ subprojects = (Set) TEMPORARY_CACHE.remove(name);
            if (subprojects != null) {
                for (Iterator it = subprojects.iterator(); it.hasNext();) {
                    NbModuleProject p = (NbModuleProject) it.next();
                    SuiteUtils.addModule(suite, p);
                }
            }
            boolean isRename = original.getProjectDirectory().getParent().equals(
                    suite.getProjectDirectory().getParent());
            if (isRename) {
                setDisplayName(nueName);
            }
        }
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
    
    private void setDisplayName(String nueName) throws IOException {
        final SuiteProperties sp = new SuiteProperties(suite, suite.getHelper(),
                suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        BasicBrandingModel branding = sp.getBrandingModel();
        if (branding.isBrandingEnabled()) {
            branding.setTitle(nueName);
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        sp.storeProperties();
                        ProjectManager.getDefault().saveProject(suite);
                        return null;
                    }
                });
            } catch (MutexException e) {
                throw (IOException) e.getException();
            }
        }
    }
    
}
