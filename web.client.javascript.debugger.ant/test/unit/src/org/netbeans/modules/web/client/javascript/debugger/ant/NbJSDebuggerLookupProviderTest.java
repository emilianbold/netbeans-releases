/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ant;

import java.io.InputStream;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.client.javascript.debugger.ant.test.SetupUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author quynguyen
 */
public class NbJSDebuggerLookupProviderTest extends NbTestCase {
    private Project project;
    
    public NbJSDebuggerLookupProviderTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        clearWorkDir();
        project = SetupUtils.unzipProject(getWorkDir());
        assertNotNull(project);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createAdditionalLookup method, of class NbJSDebuggerLookupProvider.
     */
    public void testCreateAdditionalLookup() {
        System.out.println("createAdditionalLookup");
        Lookup baseContext = Lookups.fixed(project);
        
        NbJSDebuggerLookupProvider instance = new NbJSDebuggerLookupProvider();
        
        Lookup result = instance.createAdditionalLookup(baseContext);
        assertNotNull("ProjectOpenedHook not in Lookup", result.lookup(ProjectOpenedHook.class));
    }

    /**
     * Test of setJSDebuggerProperty method, of class NbJSDebuggerLookupProvider.
     */
    public void testSetJSDebuggerProperty() {
        System.out.println("setJSDebuggerProperty");
        NbJSDebuggerLookupProvider instance = new NbJSDebuggerLookupProvider();
        instance.setJSDebuggerProperty(project);
        
        FileObject fo = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        assertNotNull("Could not find " + AntProjectHelper.PRIVATE_PROPERTIES_PATH, fo);
        
        try {
            InputStream is = fo.getInputStream();
            Properties p = new Properties();
            p.load(is);
            is.close();
            assertNotNull("Property " + NbJSDebuggerLookupProvider.JSDEBUGGER_PROP + " not set correctly", p.getProperty(NbJSDebuggerLookupProvider.JSDEBUGGER_PROP));
        }catch (Exception fnfe) {
            fnfe.printStackTrace();
            fail();
        }

    }

    /**
     * Test of unsetJSDebuggerProperty method, of class NbJSDebuggerLookupProvider.
     */
    public void testUnsetJSDebuggerProperty() {
        System.out.println("unsetJSDebuggerProperty");
        NbJSDebuggerLookupProvider instance = new NbJSDebuggerLookupProvider();
        instance.setJSDebuggerProperty(project);
        instance.unsetJSDebuggerProperty(project);

        FileObject fo = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        assertNotNull("Could not find " + AntProjectHelper.PRIVATE_PROPERTIES_PATH, fo);
        
        try {
            InputStream is = fo.getInputStream();
            Properties p = new Properties();
            p.load(is);
            is.close();
            assertNull("Property " + NbJSDebuggerLookupProvider.JSDEBUGGER_PROP + " not unset correctly", p.getProperty(NbJSDebuggerLookupProvider.JSDEBUGGER_PROP));
            
            fo.delete(fo.lock());
            instance.unsetJSDebuggerProperty(project);
            FileObject privateProps = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            assertNull("private.properties file should not be created", privateProps);
        }catch (Exception fnfe) {
            fnfe.printStackTrace();
            fail();
        }
    }

}
