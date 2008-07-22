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

package org.netbeans.junit;


import java.net.URISyntaxException;
import test.pkg.not.in.junit.NbModuleSuiteIns;
import test.pkg.not.in.junit.NbModuleSuiteT;
import test.pkg.not.in.junit.NbModuleSuiteS;
import java.io.File;
import org.netbeans.testjunit.AskForOrgOpenideUtilEnumClass;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import test.pkg.not.in.junit.NbModuleSuiteTUserDir;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteTest extends TestCase {

    public NbModuleSuiteTest(String testName) {
        super(testName);
    }

    public void testUserDir() {
        Test instance = NbModuleSuite.create(NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false));
        junit.textui.TestRunner.run(instance);

        assertEquals("Doesn't exist", System.getProperty("t.userdir"));

        instance = NbModuleSuite.create(NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false).reuseUserDir(true));
        junit.textui.TestRunner.run(instance);

        assertEquals("Exists", System.getProperty("t.userdir"));

        instance = NbModuleSuite.create(NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false).reuseUserDir(false));
        junit.textui.TestRunner.run(instance);

        assertEquals("Doesn't exist", System.getProperty("t.userdir"));
        assertProperty("netbeans.full.hack", "true");
    }
    
    public void testPreparePathes() throws URISyntaxException {
        Properties p = new Properties();

        String prop = File.separator + "x" + File.separator + "c:org-openide-util.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-nodes.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-util" + File.separator  + "tests.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-filesystems.jar";

        NbModuleSuite.S.preparePatches(prop, p);


        assertNull(
            p.getProperty("netbeans.patches.org.openide.util")
        );
        assertEquals(
            File.separator + "x" + File.separator + "org-openide-util" + File.separator  + "tests.jar",
            p.getProperty("netbeans.systemclassloader.patches")
        );
    }

    public void testAccessToInsaneAndFS() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.create(NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).gui(false).enableModules(".*"));
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.fs", "OK");
    }

    public void testAccessToInsaneAndFSWithAllModules() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.allModules(NbModuleSuiteIns.class);
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.fs", "OK");
    }

    public void testAccessToInsaneAndFSWithAllModulesEnumerated() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.allModules(NbModuleSuiteIns.class, "testFS");
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "no");
        assertProperty("ins.fs", "OK");
    }

    public void testOneCanEnumerateMethodsFromTheSuite() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");




        Test instance = NbModuleSuite.create(
            NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).addTest("testOne").
            addTest("testThree").gui(false)

        );
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "No");
        assertProperty("ins.three", "OK");
    }

    public void testOneCanEnumerateMethodsFromTheSuiteWithANewMethod() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");




        Test instance = NbModuleSuite.create(NbModuleSuiteIns.class, null, null, "testOne", "testThree");
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "No");
        assertProperty("ins.three", "OK");
    }

    public void testEmptyArrayMeansAll() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");

        Test instance = NbModuleSuite.create(NbModuleSuiteIns.class, null, null, new String[0]);
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "OK");
        assertProperty("ins.three", "OK");
    }

    static void assertProperty(String name, String value) {
        String v = System.getProperty(name);
        assertEquals("Property " + name, value, v);
    }

    public void testTwoClassesAtOnce() throws Exception {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");
        System.setProperty("en.one", "No");

        NbModuleSuite.Configuration config = NbModuleSuite.Configuration.create(
            AskForOrgOpenideUtilEnumClass.class
        ).enableModules("org.openide.util.enumerations").gui(false)
        .addTest(NbModuleSuiteIns.class, "testSecond");
        Test instance = NbModuleSuite.create(config);
        junit.textui.TestRunner.run(instance);

        assertProperty("en.one", "OK");
        assertProperty("ins.one", "No");
        assertProperty("ins.two", "OK");
        assertProperty("ins.three", "No");
    }

    public void testAccessExtraDefinedAutoload() {
        System.setProperty("en.one", "No");

        NbModuleSuite.Configuration config = NbModuleSuite.Configuration.create(AskForOrgOpenideUtilEnumClass.class);
        NbModuleSuite.Configuration addEnum = config.enableModules("org.openide.util.enumerations");
        Test instance = NbModuleSuite.create(addEnum.gui(false));
        junit.textui.TestRunner.run(instance);

        assertEquals("OK", System.getProperty("en.one"));
    }
    /*
    public void testAccessClassPathDefinedAutoload() {

        NbModuleSuite.Configuration config = NbModuleSuite.Configuration.create(En.class);
        String manifest =
"Manifest-Version: 1.0\n" +
"OpenIDE-Module-Module-Dependencies: org.openide.util.enumerations>1.5\n" +
"OpenIDE-Module: org.netbeans.modules.test.nbjunit\n" +
"OpenIDE-Module-Specification-Version: 1.0\n";

        ClassLoader loader = new ManifestClassLoader(config.parentClassLoader, manifest);
        NbModuleSuite.Configuration load = config.classLoader(loader);
        Test instance = NbModuleSuite.create(load);
        junit.textui.TestRunner.run(instance);

        assertEquals("OK", System.getProperty("en.one"));
    }
     */

    public void testModulesForCL() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(ClassLoader.getSystemClassLoader());
        s.remove("org.netbeans.modules.nbjunit");
        assertEquals("Two modules left: " + s, 3, s.size());

        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("junit: " + s, s.contains("org.netbeans.libs.junit4"));
        assertTrue("insane: " + s, s.contains("org.netbeans.insane"));
    }

    public void testIfOneCanLoadFromToolsJarOneShallDoThatInTheFrameworkAsWell() throws Exception {

        Class<?> vmm;
        try {
            vmm = ClassLoader.getSystemClassLoader().loadClass("com.sun.jdi.VirtualMachineManager");
        } catch (ClassNotFoundException ex) {
            vmm = null;
            //throw ex;
        }
        Class<?> own;
        try {
            own = Thread.currentThread().getContextClassLoader().loadClass("com.sun.jdi.VirtualMachineManager");
        } catch (ClassNotFoundException ex) {
            //own = null;
            throw ex;
        }

        //assertEquals(vmm, own);

    }

    public void testModulesForMe() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(getClass().getClassLoader());
        s.remove("org.netbeans.modules.nbjunit");
        assertEquals("Two modules left: " + s, 3, s.size());

        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("JUnit: " + s, s.contains("org.netbeans.libs.junit4"));
        assertTrue("insane: " + s, s.contains("org.netbeans.insane"));
    }

    public void testAddSuite() throws Exception{
        System.setProperty("t.one", "No");
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        conf = conf.addTest(TS.class).gui(false);
        junit.textui.TestRunner.run(NbModuleSuite.create(conf));
        assertProperty("t.one", "OK");
    }

    public static class TS extends NbTestSuite{

        public TS() {
            super(NbModuleSuiteT.class);
        }
    }

    public void testRunSuiteNoSimpleTests() throws Exception{
        System.setProperty("s.one", "No");
        System.setProperty("s.two", "No");
        System.setProperty("nosuit", "OK");
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration().gui(false);
        junit.textui.TestRunner.run(NbModuleSuite.create(conf.addTest(NbModuleSuiteS.class)));
        assertProperty("s.one", "OK");
        assertProperty("s.two", "OK");
        assertProperty("nosuit", "OK");
    }

    public void testRunEmptyConfiguration() throws Exception{
        junit.textui.TestRunner.run(NbModuleSuite.create(NbModuleSuite.emptyConfiguration().gui(false)));
    }

    public void testAddTestCase()throws Exception{
        System.setProperty("t.one", "No");
        Test instance = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(NbModuleSuiteT.class).gui(false)
        );
        junit.textui.TestRunner.run(instance);

        assertProperty("t.one", "OK");
    }
}
