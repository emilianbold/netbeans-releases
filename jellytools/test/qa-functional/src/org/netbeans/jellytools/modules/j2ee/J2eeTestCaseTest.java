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
package org.netbeans.jellytools.modules.j2ee;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;

/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCaseTest extends JellyTestCase {

    public J2eeTestCaseTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty("tomcat.home");
        System.clearProperty("jboss.home");
        System.clearProperty("glassfish.home");
        System.clearProperty("j2ee.appserver.path");
        System.clearProperty("com.sun.aas.installRoot");
        System.clearProperty("org.netbeans.modules.tomcat.autoregister.catalinaHome");
        System.clearProperty("org.netbeans.modules.j2ee.jboss4.installRoot");
        System.clearProperty("testA");
    }

    public void testSetUp() throws Exception{
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);
        new J2eeTestCase("test").setUp();
        String[] files = tmpDir.list();
        int count = 0;
        for (String file : files) {
            if (file.startsWith("J2EE_TEST_CASE_PID_FILE")){
                count++;
            }
        }
        assertEquals("just one file", 1, count);
    }

    public void testNoServer() {
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("just empty test - no server is registered", 1, t.countTestCases());
    }

    public void testAnyServer() {
        System.setProperty("tomcat.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 3, t.countTestCases());
    }

    public void testAnyServerByNames() {
        System.setProperty("jboss.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf, "testA", "testB").gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 2, t.countTestCases());
    }

    public void testTomcatSByNames() {
        System.setProperty("tomcat.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(TOMCAT, conf, "testA", "testB").gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 2, t.countTestCases());
    }

    public void testGlassfishPreferedFromTomcat() throws IOException {
        System.setProperty("glassfish.home", getWorkDirPath());
        new File(getWorkDir(), "domains/domain1").mkdirs();
        System.setProperty("tomcat.home", getDataDir().getPath());

        Configuration conf = NbModuleSuite.createConfiguration(VerifyGlassfish.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("glassfish registered", GLASSFISH.name(), System.getProperty("registered"));
    }


    public void testCreateAllModulesServerSuite() {
        System.setProperty("tomcat.home", getWorkDirPath());
        Test t = J2eeTestCase.createAllModulesServerSuite(ANY, TD.class, "testA", "testB");
        t.run(new TestResult());
        assertEquals("both tests", 2, t.countTestCases());
    }

    public void testAlreadyRegistered(){
        System.setProperty("org.netbeans.modules.tomcat.autoregister.catalinaHome", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(TOMCAT, conf, "testA", "testB").gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("test A, test B", 2, t.countTestCases());

        conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(GLASSFISH, conf, "testA").gui(false);
        t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("empty test", 1, t.countTestCases());
        
    }
    
    public void testIsRegistered() {
        testServer(GLASSFISH, "com.sun.aas.installRoot");
        testServer(TOMCAT, "org.netbeans.modules.tomcat.autoregister.catalinaHome");
        testServer(JBOSS, "org.netbeans.modules.j2ee.jboss4.installRoot");
    }

    private void testServer(Server server, String property) {
        assertFalse(J2eeTestCase.isRegistered(server));
        assertFalse(J2eeTestCase.isRegistered(ANY));

        System.setProperty(property, getDataDir().getPath());
        assertTrue(J2eeTestCase.isRegistered(server));
        assertTrue(J2eeTestCase.isRegistered(ANY));
        System.clearProperty(property);
    }

    public static class TD extends J2eeTestCase {

        public TD(String str) {
            super(str);
        }

        public void testA() {
            System.setProperty("testA", "AAA");
        }

        public void testB() {
        }
    }

    public static class VerifyGlassfish extends J2eeTestCase {

        public VerifyGlassfish(String str) {
            super(str);
        }

        public void testA() {
            if (isRegistered(GLASSFISH) && !isRegistered(TOMCAT)){
                System.setProperty("registered", GLASSFISH.name());
            }
        }

    }

}
