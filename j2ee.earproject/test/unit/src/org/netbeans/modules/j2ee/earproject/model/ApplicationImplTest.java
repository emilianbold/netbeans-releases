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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ProjectEar;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test case for {@link ApplicationImpl}.
 * @author Tomas Mysik
 */
public class ApplicationImplTest extends NbTestCase {

    private static final String CAR_NAME = "testEA-app-client";
    private static final String CAR_REFERENCE_EXPECTED_KEY = "reference.testEA-app-client.j2ee-module-car";
    private static final String CAR_REFERENCE_EXPECTED_VALUE = "${project.testEA-app-client}/dist/testEA-app-client.jar";
    private static final String EJB_NAME = "testEA-ejb";
    private static final String EJB_REFERENCE_EXPECTED_KEY = "reference.testEA-ejb.dist-ear";
    private static final String EJB_REFERENCE_EXPECTED_VALUE = "${project.testEA-ejb}/dist/testEA-ejb.jar";
    private static final String WEB_NAME = "testEA-web";
    private static final String WEB_REFERENCE_EXPECTED_KEY = "reference.testEA-web.dist-ear";
    private static final String WEB_REFERENCE_EXPECTED_VALUE = "${project.testEA-web}/dist/testEA-web.war";
    private String serverID;
    private EarProject earProject;
    
    public ApplicationImplTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
        
        // create project
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = J2eeModule.JAVA_EE_5;
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeLevel,
                serverID, /* WEB_NAME // XXX because of pavel buzek's change in web project*/ null, EJB_NAME, CAR_NAME, null, null, null);
        FileObject prjDirFO = FileUtil.toFileObject(earDirF);
        
        // verify war reference
        EditableProperties ep = TestUtil.loadProjectProperties(prjDirFO);
        /*String webReferenceValue = ep.getProperty(WEB_REFERENCE_EXPECTED_KEY);
        assertEquals("war reference should be set properly", WEB_REFERENCE_EXPECTED_VALUE, webReferenceValue);*/
        
        // verify ejb reference
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertEquals("ejb reference should be set properly", EJB_REFERENCE_EXPECTED_VALUE, ejbReferenceValue);
        
        // verify car reference
        String carReferenceValue = ep.getProperty(CAR_REFERENCE_EXPECTED_KEY);
        assertEquals("car reference should be set properly", CAR_REFERENCE_EXPECTED_VALUE, carReferenceValue);
        
        // get ear project from lookup
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        earProject = p.getLookup().lookup(EarProject.class);
        assertNotNull("project should be created", earProject);
    }
    
    /**
     * <ul>
     * Test for:
     * <li>deployment descriptor <i>application.xml</i> should not be generated</li>
     * <li>Application should not be <code>null</code></li>
     * </ul>
     * @throws Exception if any error occurs.
     */
    public void testGetApplication() throws Exception {
        FileObject prjDirFO = earProject.getProjectDirectory();
        assertNull("application.xml should not exist", prjDirFO.getFileObject("src/conf/application.xml"));
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                assertNotNull("application should not be null", application);
                return null;
            }
        });
    }
    
    /**
     * <ul>
     * Test for:
     * <li>Application modules should be empty outside <code>runReadAction</code></li>
     * </ul>
     * <p>
     * <b>This should not be ever done!</b>
     * @throws Exception if any error occurs.
     */
    public void testApplicationOutsideOfRunReadAction() throws Exception {
        
        // test model
        Application application = getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Application>() {
            public Application run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // test application
                int size = earProject.getProjectProperties().getJarContentAdditional().size();
                assertSame("application should contains exactly " + size + " modules", size, application.sizeModule());
                return application;
            }
        });
        
        // test model
        try {
            application.sizeModule();
            fail("should not get here");
            
        } catch (IllegalStateException expected) {
        }
    }
    
    /**
     * <ul>
     * Test for:
     * <li>Application should always contain the same number of modules as EAR project</li>
     * </ul>
     * @throws Exception if any error occurs.
     */
    public void testGetApplicationModules() throws Exception {
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // test application
                int size = earProject.getProjectProperties().getJarContentAdditional().size();
                assertSame("application should contains exactly " + size + " modules", size, application.sizeModule());

                return null;
            }
        });
        
        // remove ejb module
        EarProjectProperties earProjectProperties = earProject.getProjectProperties();
        List<VisualClassPathItem> modules = new ArrayList<VisualClassPathItem>();
        modules.addAll(earProjectProperties.getJarContentAdditional());
        VisualClassPathItem ejb = getEjb(earProjectProperties.getJarContentAdditional());
        assertNotNull("ejb module should exist", ejb);
        modules.remove(ejb);
        earProjectProperties.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules);
        earProjectProperties.store();

        // just to be sure that ejb module was removed
        EditableProperties ep = TestUtil.loadProjectProperties(earProject.getProjectDirectory());
        String ejbReferenceValue = ep.getProperty(EJB_REFERENCE_EXPECTED_KEY);
        assertNull("ejb reference should not exist", ejbReferenceValue);
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // test application
                int size = earProject.getProjectProperties().getJarContentAdditional().size();
                assertSame("application should contains exactly " + size + " modules", size, application.sizeModule());

                return null;
            }
        });
        
    }
    
    /**
     * <ul>
     * Test for:
     * <li>Application should always be appropriate for EAR project</li>
     * </ul>
     * <ol>
     * This test should be as follows (but cannot be right now because of missing functionality of metadata model):
     * <li>get model</li>
     * <li>change EJB module</li>
     * <li>verify model</li>
     * </ol>
     * @throws Exception if any error occurs.
     */
    public void testChangesInEAR() throws Exception {
        
        EarProjectProperties earProjectProperties = earProject.getProjectProperties();
        VisualClassPathItem ejb = getEjb(earProjectProperties.getJarContentAdditional());
        assertEquals("ejb path should be ok", EJB_NAME + ".jar", ejb.getCompletePathInArchive());
        
        // change ejb
        final String otherPath = "otherPath";
        List<VisualClassPathItem> modules = new ArrayList<VisualClassPathItem>();
        modules.addAll(earProjectProperties.getJarContentAdditional());
        ejb = getEjb(modules);
        ejb.setPathInEAR(otherPath);
        earProjectProperties.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, modules);
        earProjectProperties.store();
        
        // test model
        getModel().runReadAction(new MetadataModelAction<ApplicationMetadata, Void>() {
            public Void run(ApplicationMetadata metadata) {
                Application application = metadata.getRoot();
                
                // verify ejb
                Module ejbModule = getEjbModule(application.getModule());
                assertEquals("ejb path should be ok", otherPath + "/" + EJB_NAME + ".jar", ejbModule.getEjb());
                return null;
            }
        });
    }
    
    private MetadataModel<ApplicationMetadata> getModel() throws IOException, InterruptedException {
        return earProject.getAppModule().getMetadataModel();
    }
    
    private VisualClassPathItem getEjb(List<VisualClassPathItem> modules) {
        for (VisualClassPathItem vcpi : modules) {
            if (vcpi.getRaw().indexOf(EJB_REFERENCE_EXPECTED_KEY) != -1
                    && EJB_REFERENCE_EXPECTED_VALUE.endsWith(vcpi.getEvaluated())) {
                return vcpi;
            }
        }
        return null;
    }
    
    private Module getEjbModule(Module[] modules) {
        for (Module m : modules) {
            if (m.getEjb() != null) {
                return m;
            }
        }
        return null;
    }
    
    private ProjectEar getProjectEar() {
        return earProject.getLookup().lookup(ProjectEar.class);
    }
}
