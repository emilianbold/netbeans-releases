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

package org.netbeans.test.web;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import junit.framework.Test;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author ms113234
 */
public class RecurrentSuiteFactory {
    private static boolean debug = true;
    
    public static Test createSuite(Class clazz, File projectsDir, FileObjectFilter filter) {
        String clazzName = clazz.getName();
        NbTestSuite suite = new NbTestSuite(clazzName);
        try {
            //get list of projects to be used for testing
            File[] projects = projectsDir.listFiles(new FilenameFilter() {
                // filter out non-project folders
                public boolean accept(File dir, String fileName) {
                    return !fileName.equals("CVS");
                }
            });
            debug("RecurrentSuiteFactory");
            debug("Projects dir: " + projectsDir);
            if (projects != null) {
                for(int i = 0; i < projects.length; i++) {
                    debug("Prj Folder: " + projects[i].getName());
                    Project project = (Project) ProjectSupport.openProject(projects[i]);
                    // not a project
                    if (project == null) {
                        debug("WW: Not a project!!!");
                        continue;
                    }
                    ProjectInformation projectInfo =  ProjectUtils.getInformation(project);
                    // find recursively all test.*[.jsp|.jspx|.jspf|.html] files in
                    // the web/ folder
                    
                    // TODO check if the project is of current version and if necessery update it.
                    // enables transparent update, see: org.netbeans.modules.web.project.UpdateHelper
                    // System.setProperty("webproject.transparentUpdate",  "true");
                    
                    FileObject prjDir = project.getProjectDirectory();
                    Enumeration fileObjs = prjDir.getChildren(true);
                    
                    while (fileObjs.hasMoreElements()) {
                        FileObject fo = (FileObject) fileObjs.nextElement();
                        if (filter.accept(fo)) {
                            String testName = projectInfo.getName() + "_"
                                    + FileUtil.getRelativePath(prjDir, fo).replaceAll("[/.]", "_");
                            Constructor cnstr = clazz.getDeclaredConstructor(new Class[] {String.class, FileObject.class});
                            NbTestCase test = (NbTestCase) cnstr.newInstance(new Object[] {testName, fo});
                            suite.addTest(test);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return suite;
    }
    
    private static void debug(Object msg) {
        if (!debug) return;
        System.err.println("[debug] " + msg);
    }
}
