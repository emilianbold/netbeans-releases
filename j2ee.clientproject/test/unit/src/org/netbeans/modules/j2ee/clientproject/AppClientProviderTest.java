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

package org.netbeans.modules.j2ee.clientproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Andrei Badea
 */
public class AppClientProviderTest extends NbTestCase {
    
    private static final String APPLICATION_CLIENT_XML = "application-client.xml";
    
    public AppClientProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        AntProjectHelper helper = ((AppClientProject) project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_CLIENT_XML);
        assertNotNull(appXmlFO);
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(appXmlFO, provider.findDeploymentConfigurationFile(APPLICATION_CLIENT_XML));
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_CLIENT_XML)),
                provider.getDeploymentConfigurationFile(APPLICATION_CLIENT_XML));
        
        J2eeModule j2eeModule = (J2eeModule)project.getLookup().lookup(J2eeModule.class);
        assertNotNull(j2eeModule.getDeploymentDescriptor(J2eeModule.CLIENT_XML));
    }
    
    public void testThatProjectWithoutDDCanBeOpened() throws Exception {
        File prjDirOrigF = new File(getDataDir().getAbsolutePath(), "projects/ApplicationClient1");
        File prjDirF = TestUtil.copyFolder(getWorkDir(), prjDirOrigF);
        TestUtil.deleteRec(new File(new File(prjDirF, "src"), "conf"));
        
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        File someConfFile = provider.getDeploymentConfigurationFile("does-not-matter.xml");
        assertNotNull("J2eeModuleProvider.getDeploymentConfigurationFile() cannot return null", someConfFile);
        File expected = new File(prjDirF + File.separator + "src" +
                File.separator + "conf" + File.separator + "does-not-matter.xml");
        assertEquals("expected path", expected, someConfFile);
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                AppClientProvider.needConfigurationFolder(AppClientProjectProperties.J2EE_1_3));
        assertTrue("1.4 needs configuration folder",
                AppClientProvider.needConfigurationFolder(AppClientProjectProperties.J2EE_1_4));
        assertFalse("5.0 does not need configuration folder",
                AppClientProvider.needConfigurationFolder(AppClientProjectProperties.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder("5.0"));
        assertFalse("Anything else does not need configuration folder",
                AppClientProvider.needConfigurationFolder("6.0.hmmm?"));
        assertFalse("Even null does not need configuration folder",
                AppClientProvider.needConfigurationFolder(null));
    }
    
}
