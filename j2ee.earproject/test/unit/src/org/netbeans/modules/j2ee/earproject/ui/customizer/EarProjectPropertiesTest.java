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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.EarProjectTest;
import org.netbeans.modules.j2ee.earproject.EarProjectType;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class EarProjectPropertiesTest extends NbTestCase {
    
    private static final String CAR_REFERENCE_EXPECTED_KEY = "reference.testEA-app-client.j2ee-module-car";
    private static final String CAR_REFERENCE_EXPECTED_VALUE = "${project.testEA-app-client}/dist/testEA-app-client.jar";
    private static final String EJB_REFERENCE_EXPECTED_KEY = "reference.testEA-ejb.dist-ear";
    private static final String EJB_REFERENCE_EXPECTED_VALUE = "${project.testEA-ejb}/dist/testEA-ejb.jar";
    private static final String WEB_REFERENCE_EXPECTED_KEY = "reference.testEA-web.dist-ear";
    private static final String WEB_REFERENCE_EXPECTED_VALUE = "${project.testEA-web}/dist/testEA-web.war";
    private String serverID;
    private EarProject earProject;
    private EarProjectProperties earProjectProperties;
    
    public EarProjectPropertiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
        
        // create project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String carName = "testEA-app-client";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, null, ejbName, carName, null, null, null);
        FileObject prjDirFO = FileUtil.toFileObject(earDirF);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(prjDirFO);
        
        // verify ejb reference
        EditableProperties ep = TestUtil.loadProjectProperties(prjDirFO);
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertEquals("ejb reference should be set properly", EJB_REFERENCE_EXPECTED_VALUE, ejbReferenceValue);
        
        // verify car reference
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should be set properly", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        
        // get ear project from lookup
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        earProject = p.getLookup().lookup(EarProject.class);
        assertNotNull("project should be created", earProject);
        
        // create ear project properties and verify them
        AntProjectHelper aph = project.getAntProjectHelper();
        AuxiliaryConfiguration aux = aph.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(aph, aux, aph.getStandardPropertyEvaluator());
        earProjectProperties = new EarProjectProperties(earProject, refHelper, new EarProjectType());
        assertNotNull("ear project properties should be created", earProjectProperties);
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
        File earDirF = new File(getWorkDir(), "testEA-1");
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
        File earDirF = new File(getWorkDir(), "testEA-2");
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
    
    // see #97185 & #95604
    public void testResolveProjectDependencies() throws Exception {
        
        int countBefore = earProjectProperties.getJarContentAdditional().size();
        
        List<VisualClassPathItem> modules = new ArrayList<VisualClassPathItem>();
        modules.addAll(earProjectProperties.getJarContentAdditional());
        modules.remove(getEjbProject());
        earProjectProperties.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules);
        earProjectProperties.store();
        
        EditableProperties ep = TestUtil.loadProjectProperties(earProject.getProjectDirectory());
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertNull("ejb reference should not exist", ejbReferenceValue);
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should exist", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        assertEquals("wrong count of project references", countBefore - 1, earProjectProperties.getJarContentAdditional().size());
        assertEquals("wrong count of project references", countBefore - 1, earProjectProperties.getReferenceHelper().getRawReferences().length);
        
        // remove all entries
        modules.clear();
        earProjectProperties.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules);
        assertEquals("wrong count of project references", 0, earProjectProperties.getJarContentAdditional().size());
        
        // add new project/module
        modules.add(getWebProject());
        earProjectProperties.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules);
        
        earProjectProperties.store();
        
        ep = TestUtil.loadProjectProperties(earProject.getProjectDirectory());
        ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertNull("ejb reference should not exist", ejbReferenceValue);
        carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertNull("car reference should not exist", carReferenceValue);
        String webReferenceValue = ep.getProperty(WEB_REFERENCE_EXPECTED_KEY);
        assertEquals("web reference should exist", WEB_REFERENCE_EXPECTED_VALUE, webReferenceValue);
        assertEquals("wrong count of project references", 1, earProjectProperties.getJarContentAdditional().size());
        assertEquals("wrong count of project references", 1, earProjectProperties.getReferenceHelper().getRawReferences().length);
    }
    
    private VisualClassPathItem getEjbProject() {
        List<VisualClassPathItem> list = earProjectProperties.getJarContentAdditional();
        for (VisualClassPathItem vcpi : list) {
            if (vcpi.getRaw().indexOf(EJB_REFERENCE_EXPECTED_KEY) != -1
                    && EJB_REFERENCE_EXPECTED_VALUE.endsWith(vcpi.getEvaluated())) {
                return vcpi;
            }
        }
        return null;
    }
    
    private VisualClassPathItem getWebProject() throws IOException {
        List<AntArtifact> artifactList = new ArrayList<AntArtifact>();
        AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                createWebProject(),
                EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE);
        if (null != artifacts) {
            artifactList.addAll(Arrays.asList(artifacts));
        }
        assertEquals("size should be exactly 1", 1, artifactList.size());
        
        // create the vcpis
        List<VisualClassPathItem> newVCPIs = new ArrayList<VisualClassPathItem>();
        for (AntArtifact art : artifactList) {
            VisualClassPathItem vcpi = VisualClassPathItem.createArtifact(art);
            vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
            return vcpi;
        }
        fail("web reference should exist");
        return null;
    }
    
    private Project createWebProject() throws IOException {
        String warName = "testEA-web";
        File projectDir = FileUtil.toFile(earProject.getProjectDirectory());
        File webAppDir = new File(projectDir, warName);
        if (webAppDir.exists()) {
            webAppDir.delete();
        }
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(FileUtil.normalizeFile(webAppDir));
        createData.setName(warName);
        createData.setServerInstanceID(this.serverID);
        createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
        createData.setJavaEEVersion(EarProjectGenerator.checkJ2eeVersion(J2eeModule.JAVA_EE_5, serverID, J2eeModule.WAR));
        createData.setContextPath("/" + warName);
        AntProjectHelper webHelper = WebProjectUtilities.createProject(createData);

        FileObject webAppDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(webAppDir));
        Project webProject = ProjectManager.getDefault().findProject(webAppDirFO);
        assertNotNull("web project should exist", webProject);
        
        return webProject;
    }
}
