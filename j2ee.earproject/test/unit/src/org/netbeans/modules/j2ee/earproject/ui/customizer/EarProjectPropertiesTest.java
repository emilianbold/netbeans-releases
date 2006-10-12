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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.EarProjectTest;
import org.netbeans.modules.j2ee.earproject.EarProjectType;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class EarProjectPropertiesTest extends NbTestCase {
    
    private String serverID;
    
    public EarProjectPropertiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testPropertiesWithoutDD() throws Exception { // see #73751
        File proj = new File(getWorkDir(), "EARProject");
        AntProjectHelper aph = EarProjectGenerator.createProject(proj,
                "test-project", J2eeModule.JAVA_EE_5, serverID, "1.5");
        FileObject prjDirFO = aph.getProjectDirectory();
        // simulateing #73751
        prjDirFO.getFileObject("src/conf/application.xml").delete();
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        AuxiliaryConfiguration aux = aph.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(aph, aux, aph.getStandardPropertyEvaluator());
        EarProjectProperties epp = new EarProjectProperties((EarProject) p, refHelper, new EarProjectType());
        assertNotNull("non-null application modules", epp.getApplicationModules());
    }
    
    public void testPathInEARChanging() throws Exception { // see #76008
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, ejbName, null, null, null, null);
        EarProject earProject = (EarProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(earDirF));
        Application app = DDProvider.getDefault().getDDRoot(
                earProject.getAppModule().getDeploymentDescriptor());
        assertEquals("ejb path", "testEA-ejb.jar", app.getModule(0).getEjb());
        
        // simulate change through customizer
        EarProjectProperties epp = earProject.getProjectProperties();
        List<VisualClassPathItem> vcpis = epp.getJarContentAdditional();
        vcpis.get(0).setPathInEAR("otherPath");
        epp.updateContentDependency(
                new HashSet<VisualClassPathItem>(vcpis),
                new HashSet<VisualClassPathItem>(vcpis));
        
        assertEquals("ejb path", "otherPath/testEA-ejb.jar", app.getModule(0).getEjb());
    }
    
    public void testSetACPrivateProperties() throws Exception { // #81964
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, null, acName, null, null, null);
        EarProject earProject = (EarProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(earDirF));
        earProject.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH).delete();
        EarProjectTest.openProject(earProject);
        assertNotNull("private properties successfully regenerated", earProject.getAntProjectHelper().getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(EarProjectProperties.APPCLIENT_WA_COPY_CLIENT_JAR_FROM));
    }
}
