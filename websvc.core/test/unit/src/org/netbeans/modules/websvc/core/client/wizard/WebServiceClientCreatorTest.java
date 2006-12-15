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

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.core.ClientCreator;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.testutils.RepositoryImpl;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Lukas Jungmann
 */
public class WebServiceClientCreatorTest extends NbTestCase {
    
    private WizardDescriptor wd;
    
    public WebServiceClientCreatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testJ2SEonJdk6() throws Exception {
        System.setProperty("websvc.core.test.repo.root", this.getWorkDirPath());
        MockServices.setServices(RepositoryImpl.class);
        File projectRoot = new File(getDataDir(), "projects/j2se_16");
        FileObject projectDir = FileUtil.toFileObject(FileUtil.normalizeFile(projectRoot));
        Project p = FileOwnerQuery.getOwner(projectDir);
        assert p != null : "null Project";
        wd = new WizardDescriptor(new Panel[] {new WebServiceClientWizardDescriptor()});
          
        wd.putProperty(ClientWizardProperties.JAX_VERSION, ClientWizardProperties.JAX_WS);
        wd.putProperty(ClientWizardProperties.WSDL_FILE_PATH, FileUtil.normalizeFile(new File(getDataDir(), "wsdl/MyWebService.wsdl")).getAbsolutePath());
        wd.putProperty(ClientWizardProperties.WSDL_PACKAGE_NAME, "client.jaxws");
        ClientCreator creator = CreatorProvider.getClientCreator(p, wd);
        if (creator!=null) {        
            try {
                creator.createClient();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ((JaxWsClientCreator)creator).task.waitFinished();
        }
        //some checks...

    }
    
    /**
     * Test of create method, of class org.netbeans.modules.websvc.core.client.wizard.WebServiceClientCreator.
     */
    public void testCreate() {
//        Thread.yield();
//        System.out.println("create");
        
//        org.netbeans.modules.websvc.core.client.wizard.WebServiceClientCreator instance = null;
//
//        Set expResult = null;
//        Set result = instance.create();
//        assertEquals(expResult, result);
//
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
    /**
     * Test of getJavaSourceGroups method, of class org.netbeans.modules.websvc.core.client.wizard.WebServiceClientCreator.
     */
    public void testGetJavaSourceGroups() {
//        System.out.println("getJavaSourceGroups");
        
//        Project project = null;
//
//        SourceGroup[] expResult = null;
//        SourceGroup[] result = org.netbeans.modules.websvc.core.client.wizard.WebServiceClientCreator.getJavaSourceGroups(project);
//        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
