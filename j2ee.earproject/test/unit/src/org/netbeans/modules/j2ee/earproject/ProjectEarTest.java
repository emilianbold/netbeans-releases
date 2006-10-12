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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class ProjectEarTest extends NbTestCase {
    
    private String serverID;
    
    public ProjectEarTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testModuleAddition() throws Exception {
        // testing project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, ejbName, null, null, null, null);
        FileObject earDirFO = FileUtil.toFileObject(earDirF);
        FileObject ejbProjectFO = earDirFO.getFileObject("testEA-ejb");
        assertNotNull(ejbProjectFO);
        
        File earDirAnotherF = new File(getWorkDir(), "testEA-another");
        NewEarProjectWizardIteratorTest.generateEARProject(earDirAnotherF, name, j2eeLevel,
                serverID, null, null, null, null, null, null);
        FileObject earDirAnotherFO = FileUtil.toFileObject(earDirAnotherF);
        EjbJarProject createdEjbJarProject = (EjbJarProject) ProjectManager.getDefault().findProject(ejbProjectFO);
        assertNotNull("ejb project found", createdEjbJarProject);
        Ear ear = Ear.getEar(earDirAnotherFO);
        assertNotNull("have Ear instance", ear);
        if (ear != null) {
            ear.addEjbJarModule(createdEjbJarProject.getAPIEjbJar());
        }
        
        EarProject earProject = (EarProject) ProjectManager.getDefault().findProject(earDirAnotherFO);
        Application app = DDProvider.getDefault().getDDRoot(
                earProject.getAppModule().getDeploymentDescriptor());
        assertSame("ejb added modules", 1, app.getModule().length);
        
    }
    
}
