/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EjbJarProviderTest extends NbTestCase {
    
    private Project project;
    private AntProjectHelper helper;
    
    public EjbJarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that null is silently returned for files in the configuration files directory
     * (meta.inf) when this directory does not exist.
     */
    public void testMetaInfBasedPathsAreNulLWhenMetaInfIsNullIssue65888() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/BrokenEJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf does not exist
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        assertNull(helper.resolveFileObject(metaInf));
        
        // ensure meta.inf-related files are silently returned as null
        
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        assertNull(provider.findDeploymentConfigurationFile("ejb-jar.xml"));
        assertNull(provider.getDeploymentConfigurationFile("ejb-jar.xml"));
        
        J2eeModule j2eeModule = (J2eeModule) project.getLookup().lookup(J2eeModule.class);
        assertNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBJAR_XML));
        assertNull(j2eeModule.getDeploymentDescriptor(J2eeModule.EJBSERVICES_XML));
        
        EjbJarImplementation ejbJar = (EjbJarImplementation) project.getLookup().lookup(EjbJarImplementation.class);
        assertNull(ejbJar.getMetaInf());
        assertNull(ejbJar.getDeploymentDescriptor());
    }
}
