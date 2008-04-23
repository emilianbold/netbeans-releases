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

import java.io.File;
import org.netbeans.testjunit.AskForOrgOpenideUtilEnumClass;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.insane.scanner.ObjectMap;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteTest extends TestCase {
    
    public NbModuleSuiteTest(String testName) {
        super(testName);
    }            
    
    public static Test suite() {
        //return new NbModuleSuiteTest("testAccessExtraDefinedAutoload");
        return new NbTestSuite(NbModuleSuiteTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRun() {
        Test instance = NbModuleSuite.create(T.class, null, null);
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
        assertProperty("netbeans.full.hack", "true");
    }
    
    public void testPreparePathes() {
        Properties p = new Properties();
        
        NbModuleSuite.S.preparePatches(
            File.separator + "x" + File.separator + "c:org-openide-util.jar" + File.pathSeparator + 
            File.separator + "x" + File.separator + "org-openide-nodes.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-util" + File.separator  + "tests.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-filesystems.jar", p);
        
        
        assertEquals(
            File.separator + "x" + File.separator + "org-openide-util" + File.separator  + "tests.jar",
            p.getProperty("netbeans.patches.org.openide.util")
        );
    }

    public void testAccessToInsane() {
        Test instance = NbModuleSuite.create(Ins.class, null, null);
        junit.textui.TestRunner.run(instance);
        
        assertProperty("ins.one", "OK");
    }

    public void testOneCanEnumerateMethodsFromTheSuite() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");

        
        
        
        Test instance = NbModuleSuite.create(
            NbModuleSuite.createConfiguration(Ins.class).addTest("testOne").addTest("testThree")
        );
        junit.textui.TestRunner.run(instance);
        
        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "No");
        assertProperty("ins.three", "OK");
    }
    
    private static void assertProperty(String name, String value) {
        String v = System.getProperty(name);
        assertEquals("Property " + name, value, v);
    }

    public void testAccessExtraDefinedAutoload() {
        NbModuleSuite.Configuration config = NbModuleSuite.Configuration.create(AskForOrgOpenideUtilEnumClass.class);
        NbModuleSuite.Configuration addEnum = config.enableModules("org.openide.util.enumerations");
        Test instance = NbModuleSuite.create(addEnum);
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
/*    
    private static class ManifestClassLoader extends ClassLoader {
        private final String manifest;
        public ManifestClassLoader(ClassLoader parent, String manifest) {
            super(parent);
            this.manifest = manifest;
        }

        @Override
        protected URL findResource(String name) {
            try {
                class H extends URLStreamHandler {

                    @Override
                    protected URLConnection openConnection(URL u) throws IOException {
                        return new C(u);
                    }

                    class C extends URLConnection {

                        StringBufferInputStream buf;

                        public C(URL url) {
                            super(url);
                            buf = new StringBufferInputStream(url.getHost());
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return buf;
                        }

                        @Override
                        public void connect() throws IOException {
                        }
                    }
                }

                URL u = new URL("text:", manifest, 0, "", new H());
                return u;
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            return Collections.enumeration(Collections.singleton(findResource(name)));
        }
        
    }
*/    
    public static class T extends TestCase {
        public T(String t) {
            super(t);
        }

        public void testOne() {
            System.setProperty("t.one", "OK");
        }

        public void testFullhack() {
            System.setProperty("t.hack", System.getProperty("netbeans.full.hack"));
        }
    }

    public static class Ins extends TestCase 
    implements org.netbeans.insane.scanner.Visitor {
        public Ins(String t) {
            super(t);
        }

        public void testOne() {
            try {
                Class<?> access = Class.forName("org.netbeans.insane.model.Support");
                System.setProperty("ins.one", "OK");
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public void testSecond() {
            System.setProperty("ins.two", "OK");
        }

        public void testThree() {
            System.setProperty("ins.three", "OK");
        }

        public void visitClass(Class cls) {
        }

        public void visitObject(ObjectMap map, Object object) {
        }

        public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
        }

        public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
        }

        public void visitStaticReference(ObjectMap map, Object to, Field ref) {
        }
    }
    
}
