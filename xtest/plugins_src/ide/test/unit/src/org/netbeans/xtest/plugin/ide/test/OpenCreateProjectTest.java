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
 package org.netbeans.xtest.plugin.ide.testst.plugin.ide.test;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.OpenProjectList;

/** Test xtest properties which handle creating and opening of projects:
 * xtest.ide.create.project - if true java project XTestProject should be created;
 * xtest.ide.open.project - if set to e.g. ${xtest.data}/SampleProject, the project should be opened;
 * if projects.zip is present in ${xtest.data} folder, all projects in zip should be opened;
 */
public class OpenCreateProjectTest extends NbTestCase {
    
    /** Creates a new test.
     * @param name test name
     */
    public OpenCreateProjectTest(String name) {
        super(name);
    }
    
    /** Create suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new OpenCreateProjectTest("testCreateProject"));
        suite.addTest(new OpenCreateProjectTest("testOpenProject"));
        suite.addTest(new OpenCreateProjectTest("testOpenProjectsFromZip"));
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Test xtest.ide.create.project - if true java project XTestProject should be created. */
    public void testCreateProject() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("XTestProject")) {
                return;
            }
        }
        fail("Project XTestProject should be created because property xtest.ide.create.project is set");
    }
 
    /** Test xtest.ide.open.project - if set to e.g. ${xtest.data}/SampleProject, the project should be opened. */
     public void testOpenProject() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("SampleProject")) {
                return;
            }
        }
        fail("Project SampleProject should be opened because property xtest.ide.open.project is set to ${xtest.data}/SampleProject");
    }
    
    /** If projects.zip is present in ${xtest.data} folder, all projects in zip should be opened. */
    public void testOpenProjectsFromZip() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        boolean project1 = false;
        boolean project2 = false;
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("SampleProjectFromZip1")) {
                project1 = true;
            }
            if(ProjectUtils.getInformation(project).getName().equals("SampleProjectFromZip2")) {
                project2 = true;
            }
        }
        assertTrue("Projects SampleProjectFromZip1 and SampleProjectFromZip2 should be opened because projects.zip is present in xtest.data folder.", project1 && project2);
    }
}