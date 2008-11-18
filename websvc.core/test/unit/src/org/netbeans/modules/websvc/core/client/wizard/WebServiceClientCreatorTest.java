/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.support.ClientCreator;
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
