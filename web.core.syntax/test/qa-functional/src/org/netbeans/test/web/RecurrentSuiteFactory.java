/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
            
            if (projects != null) {
                for(int i = 0; i < projects.length; i++) {
                    Project project = (Project) ProjectSupport.openProject(projects[i]);
                    ProjectInformation projectInfo =  ProjectUtils.getInformation(project);
                    // find recursively all test.*[.jsp|.jspx|.jspf|.html] files in
                    // the web/ folder
                    FileObject prjDir = project.getProjectDirectory();
                    FileObject webDir = prjDir.getFileObject("web");
                    Enumeration fileObjs = webDir.getChildren(true);
                    
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
}
