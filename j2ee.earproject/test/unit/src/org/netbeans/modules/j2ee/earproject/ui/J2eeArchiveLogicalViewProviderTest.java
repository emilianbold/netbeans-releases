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

package org.netbeans.modules.j2ee.earproject.ui;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectTest;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.J2eeArchiveLogicalViewProvider.ArchiveLogicalViewRootNode;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of {@link J2eeArchiveLogicalViewProvider}.
 *
 * @author Martin Krauskopf
 */
public class J2eeArchiveLogicalViewProviderTest extends NbTestCase {
    
    private String serverInstanceID;
    
    public J2eeArchiveLogicalViewProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        serverInstanceID = TestUtil.registerSunAppServer(this);
    }
    
    public void testProjectFiles() throws Exception {
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = "1.4";
        String jarName = "testEA-ejb";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeLevel,
                serverInstanceID, null, null, jarName, null, null, null);
        Project earProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        EarProjectTest.openProject((EarProject) earProject);
        J2eeArchiveLogicalViewProvider.ArchiveLogicalViewRootNode rootNode = (ArchiveLogicalViewRootNode)
                ((LogicalViewProvider) earProject.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        Set<FileObject> expected = new HashSet<FileObject>(Arrays.asList(
                new FileObject[] {
            earProject.getProjectDirectory().getFileObject("nbproject"),
            earProject.getProjectDirectory().getFileObject("build.xml"),
            earProject.getProjectDirectory().getFileObject("src")
        }
        ));
        assertEquals("right project files", expected, rootNode.getProjectFiles());
    }
    
}
