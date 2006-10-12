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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ProjectEarProviderTest extends NbTestCase {
    
    private String serverID;
    
    public ProjectEarProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testFindEar() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel, serverID);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        Project createdEjbJarProject = ProjectManager.getDefault().findProject(earDirFO);
        assertNotNull("Ear found", Ear.getEar(earDirFO));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf")));
        assertNotNull("Ear found", Ear.getEar(earDirFO.getFileObject("src/conf/application.xml")));
        assertNull("not Ear for parent", Ear.getEar(earDirFO.getParent()));
    }
    
}
