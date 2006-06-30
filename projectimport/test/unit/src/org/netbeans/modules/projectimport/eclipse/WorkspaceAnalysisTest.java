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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Tests importing of complex project (still without workspace provided). This
 * test should check all features if project analyzer.
 *
 * @author mkrauskopf
 */
public class WorkspaceAnalysisTest extends ProjectImporterTestCase {

    public WorkspaceAnalysisTest(String name) {
        super(name);
    }

    public void testComplexAloneProjectFor_3_1_M6() throws Exception {
        File workspaceDir = extractToWorkDir("workspace-test-3.1M6.zip");
        Workspace workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        assertNotNull("Unable to load workspace", workspace);
        printMessage("Checking " + workspace.getDirectory());
        assertFalse("Workspace shouldn't be emtpy", workspace.getProjects().isEmpty());
        printMessage("Project in the workspace: " + workspace.getProjects());
        
        // Below information are just known. Get familiar with tested zips
        // (which could be created by the helper script createWorkspace.sh)
        String[] ws31M6ProjectNames = {"p1", "p2", "p3"};
        String[] p1RequiredProjects  = {"/p2", "/p3"};
        
        boolean p1Tested = false;
        Collection/*<String>*/ p1ReqProjectsNames =
                new ArrayList(Arrays.asList(p1RequiredProjects));
        Collection/*<String>*/ wsProjectNames =
                new ArrayList(Arrays.asList(ws31M6ProjectNames));
        Collection/*<EclipseProject>*/ gainedP1ReqProjects = null;
        
        for (Iterator it = workspace.getProjects().iterator(); it.hasNext(); ) {
            EclipseProject project = (EclipseProject) it.next();
            /* Test p1 project and its dependencies. */
            if ("p1".equals(project.getName())) {
                SingleProjectAnalysisTest.doBasicProjectTest(project); // for p1
                gainedP1ReqProjects = project.getProjectsEntries();
                assertEquals("Incorrect project count for p1",
                        p1RequiredProjects.length, gainedP1ReqProjects.size());
                printCollection("projects", gainedP1ReqProjects);
                p1Tested = true;
            }
            wsProjectNames.remove(project.getName());
        }
        assertTrue("\"p1\" project wasn't found in the workspace.", p1Tested);
        assertTrue("All project should be processed.", wsProjectNames.isEmpty());
        for (Iterator it = gainedP1ReqProjects.iterator(); it.hasNext(); ) {
            p1ReqProjectsNames.remove(((ClassPathEntry)it.next()).getRawPath());
        }
        assertTrue("\"p1\" project depends on unknown projects: " + p1ReqProjectsNames,
                p1ReqProjectsNames.isEmpty());
    }
    
    public void test_73542() throws Exception {
        File workspaceDir = extractToWorkDir("workspace_73542-3.1.2.zip");
        Workspace workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        assertNotNull("Unable to load workspace", workspace);
    }
    
}
