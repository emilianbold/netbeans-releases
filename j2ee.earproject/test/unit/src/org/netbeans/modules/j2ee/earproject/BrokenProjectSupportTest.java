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
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class BrokenProjectSupportTest extends NbTestCase {
    
    private static final String CAR_REFERENCE_EXPECTED_KEY = "reference.testEA-app-client.j2ee-module-car";
    private static final String CAR_REFERENCE_EXPECTED_VALUE = "${project.testEA-app-client}/dist/testEA-app-client.jar";
    
    private String serverID;
    private EarProject earProject;
    private BrokenProjectSupport bps;
    
    public BrokenProjectSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
        
        // testing project
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        String carName = "testEA-app-client";
        
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeLevel,
                serverID, null, null, carName, null, null, null);
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        
        EditableProperties ep = TestUtil.loadProjectProperties(prjDirFO);
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("right initial car reference", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        // broke the project
        ep.setProperty(CAR_REFERENCE_EXPECTED_KEY, CAR_REFERENCE_EXPECTED_VALUE + "-broken");
        carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertFalse("broken car reference", CAR_REFERENCE_EXPECTED_VALUE.equals(carReferenceValue));
        TestUtil.storeProjectProperties(prjDirFO, ep);
        
        Project project = ProjectManager.getDefault().findProject(prjDirFO);
        this.earProject = (EarProject) project.getLookup().lookup(EarProject.class);
        assertNotNull("project successfully created", this.earProject);
        this.bps = (BrokenProjectSupport) this.earProject.getLookup().lookup(BrokenProjectSupport.class);
        assertNotNull("has BrokenProjectSupport in the lookup", bps);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        
        serverID = null;
        earProject = null;
        bps = null;
    }
    
    public void testAdjustReferences() {
        EditableProperties projectProps = earProject.getAntProjectHelper().
                getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertFalse("reference property is broken",
                CAR_REFERENCE_EXPECTED_VALUE.equals(projectProps.getProperty(CAR_REFERENCE_EXPECTED_KEY)));
        bps.adjustReferences();
        EditableProperties fixedProjectProps = earProject.getAntProjectHelper().
                getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("reference property was fixed", CAR_REFERENCE_EXPECTED_VALUE,
                fixedProjectProps.getProperty(CAR_REFERENCE_EXPECTED_KEY));
    }
}
