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
package org.netbeans.junit.ide;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.OpenProjectList;

/** Test of ProjectSupport class.
 * @author Jiri Skrivanek 
 */
public class ProjectSupportTest extends NbTestCase {
    
    /** Creates a new test. 
     * @param testName name of test
     */
    public ProjectSupportTest(String testName) {
        super(testName);
    }

    /** Set up. */
    protected void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Creates a new test suite.
     * @return returns a new suite.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ProjectSupportTest.class);
        return suite;
    }

    private static final String PROJECT_NAME = "SampleProject";
    private static File projectParentDir;
    
    /** Test createProject method. */
    public void testCreateProject() throws Exception {
        projectParentDir = this.getWorkDir();
        Project project = (Project)ProjectSupport.createProject(this.getWorkDir(), PROJECT_NAME);
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Created project not opened.", project, projects[0]);
    }
    
    /** Test closeProject method. */
    public void testCloseProject() {
        assertTrue("Should return true if succeeded.", ProjectSupport.closeProject(PROJECT_NAME));
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("None project should be opened.", 0, projects.length);
        assertFalse("Should return false if project doesn't exist.", ProjectSupport.closeProject("Dummy"));
    }
    
    /** Test openProject method. */
    public void testOpenProject() throws Exception {
        Project project = (Project)ProjectSupport.openProject(new File(projectParentDir, PROJECT_NAME));
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Opened project not opened.", project, projects[0]);
        assertNull("Should return null if project doesn't exist.", ProjectSupport.openProject(new File(projectParentDir, "Dummy")));
    }
    
    /** Test waitScanFinished method. */
    public void testWaitScanFinished() {
        // TODO - somehow check this functionality
        ProjectSupport.waitScanFinished();
    }
}
