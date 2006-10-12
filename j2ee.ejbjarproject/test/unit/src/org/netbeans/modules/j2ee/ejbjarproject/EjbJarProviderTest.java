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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EjbJarProviderTest extends TestBase {
    
    private static final String EJBJAR_XML = "ejb-jar.xml";
    
    private Project project;
    private AntProjectHelper helper;
    
    public EjbJarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer ejb-jar.xml and webservices.xml exist
        FileObject ejbJarXmlFO = metaInfFO.getFileObject(EJBJAR_XML);
        assertNotNull(ejbJarXmlFO);
        assertNotNull(metaInfFO.getFileObject("webservices.xml"));

        // ensure deployment descriptor files and beans are returned
        
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(ejbJarXmlFO, provider.findDeploymentConfigurationFile(EJBJAR_XML));
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(EJBJAR_XML)), provider.getDeploymentConfigurationFile(EJBJAR_XML));
        
        J2eeModule j2eeModule = (J2eeModule)project.getLookup().lookup(J2eeModule.class);
        assertNotNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBJAR_XML));
        assertNotNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBSERVICES_XML));
        
        EjbJarImplementation ejbJar = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
        assertEquals(metaInfFO, ejbJar.getMetaInf());
        assertEquals(ejbJarXmlFO, ejbJar.getDeploymentDescriptor());
    }
    
    /**
     * Tests that null is silently returned for files in the configuration files directory
     * (meta.inf) when this directory does not exist.
     */
    public void testMetaInfBasedPathsAreNullWhenMetaInfIsNullIssue65888() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/BrokenEJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf does not exist
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        assertNull(helper.resolveFileObject(metaInf));
        
        // ensure meta.inf-related files are silently returned as null
        
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        assertNull(provider.findDeploymentConfigurationFile(EJBJAR_XML));
        assertNotNull(provider.getDeploymentConfigurationFile(EJBJAR_XML));
        
        J2eeModule j2eeModule = (J2eeModule)project.getLookup().lookup(J2eeModule.class);
        assertNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBJAR_XML));
        assertNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBSERVICES_XML));
        
        EjbJarImplementation ejbJar = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
        assertNull(ejbJar.getMetaInf());
        assertNull(ejbJar.getDeploymentDescriptor());
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(EjbJarProjectProperties.J2EE_1_3));
        assertTrue("1.4 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(EjbJarProjectProperties.J2EE_1_4));
        assertFalse("5.0 does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(EjbJarProjectProperties.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder("5.0"));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder("6.0.hmmm?"));
        assertFalse("Even null does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(null));
    }
    
}
