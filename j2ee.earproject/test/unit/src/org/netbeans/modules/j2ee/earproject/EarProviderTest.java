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
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Andrei Badea
 */
public class EarProviderTest extends NbTestCase {

    
    private static final String APPLICATION_XML = "application.xml";
    
    
    public EarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EnterpriseApplication1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        AntProjectHelper helper = ((EarProject) project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer application-client.xml exists
        FileObject appXmlFO = metaInfFO.getFileObject(APPLICATION_XML);
        assertNotNull(appXmlFO);
        
        // ensure deployment descriptor file is returned
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(appXmlFO, provider.findDeploymentConfigurationFile(APPLICATION_XML));
        assertEquals(FileUtil.toFile(metaInfFO.getFileObject(APPLICATION_XML)),
                provider.getDeploymentConfigurationFile(APPLICATION_XML));
        
        J2eeModule j2eeModule = (J2eeModule)project.getLookup().lookup(J2eeModule.class);
        assertNotNull(j2eeModule.getDeploymentDescriptor(J2eeModule.APP_XML));
    }
    
}
