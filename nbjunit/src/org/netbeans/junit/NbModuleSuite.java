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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Wraps a test class with proper NetBeans Runtime Container environment.
 * This allows to execute tests in a very similar environment to the 
 * actual invocation in the NetBeans IDE. To use write your test as
 * you are used to and add suite static method:
 * <pre>
 * public class YourTest extends NbTestCase {
 *   public YourTest(String s) { super(s); }
 * 
 *   public static Test suite() {
 *     return NbModuleSuite.create(YourTest.class);
 *   }
 * 
 *   public void testXYZ() { ... }
 *   public void testABC() { ... }
 * }
 * </pre>
 *
 * @since 1.46
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuite {

    private NbModuleSuite() {}
    
    
    /** Settings object that allows one to configure execution of
     * whole {@link NbModuleSuite}.
     * 
     * @since 1.48
     * 
     */
    public static final class Configuration extends Object {
        final List<Item> tests;
        final Class<? extends TestCase> latestTestCaseClass;
        final String clusterRegExp;
        final String moduleRegExp;
        final ClassLoader parentClassLoader;
        final boolean reuseUserDir;
        final boolean gui;

        private Configuration(
            String clusterRegExp, 
            String moduleRegExp,
            ClassLoader parent, 
            List<Item> testItems,
            Class<? extends TestCase> latestTestCase, 
            boolean reuseUserDir,
            boolean gui
        ) {
            this.clusterRegExp = clusterRegExp;
            this.moduleRegExp = moduleRegExp;
            this.parentClassLoader = parent;
            this.tests = testItems;
            this.reuseUserDir = reuseUserDir;
            this.latestTestCaseClass = latestTestCase;
            this.gui = gui;
        }

        static Configuration create(Class<? extends TestCase> clazz) {            
            return new Configuration(null, null, ClassLoader.getSystemClassLoader().getParent(), Collections.<Item>emptyList(), clazz, false, true);
        }
        
        /** Regular expression to match clusters that shall be enabled.
         * To enable all cluster, one can use <code>".*"</code>. To enable
         * ide and java clusters, it is handy to pass in <code>"ide.*|java.*</code>.
         * There is no need to requrest presence of <code>platform.*</code> cluster,
         * as that is available all the time by default.
         * 
         * @param regExp regular expression to match cluster names
         * @return clone of this configuration with cluster set to regExp value
         */
        public Configuration clusters(String regExp) {
            return new Configuration(regExp, moduleRegExp, parentClassLoader, tests, latestTestCaseClass, reuseUserDir, gui);
        }

        /** By default only modules on classpath of the test are enabled, 
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules
         * in all clusters pass in <code>".*"</code>.
         * @param regExp regular expression to match code name base of modules
         * @return clone of this configuration with enable modules set to regExp value
         */
        public Configuration enableModules(String regExp) {
            return new Configuration(clusterRegExp, regExp, parentClassLoader, tests, latestTestCaseClass, reuseUserDir, gui);
        }

        Configuration classLoader(ClassLoader parent) {
            return new Configuration(clusterRegExp, moduleRegExp, parent, tests, latestTestCaseClass, reuseUserDir, gui);
        }

        /** Adds new test name, or array of names into the configuration. By 
         * default the suite executes all <code>testXYZ</code> 
         * methods present in the test class
         * (the one passed into {@link Configuration#create(java.lang.Class)}
         * method). However if there is a need to execute just some of them,
         * one can use this method to explicitly enumerate them by subsequent
         * calls to <code>addTest</code> method.
         * @param testNames list names to add to the test execution
         * @return clone of this configuration with testNames test added to the 
         *    list of executed tests
         */
        public Configuration addTest(String... testNames) {
            if (latestTestCaseClass == null){
                throw new IllegalStateException();
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            newTests.add(new Item(true, latestTestCaseClass, testNames));
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, newTests, latestTestCaseClass, reuseUserDir, gui);
        }
        
        /** Adds new test class to run, together with a list of its methods
         * that shall be executed. The list can be empty and if so, the 
         * the suite executes all <code>testXYZ</code> 
         * methods present in the test class.
         * 
         * @param test the class to also execute in this suite
         * @param testNames list names to add to the test execution
         * @return clone of this configuration with testNames test added to the 
         *    list of executed tests
         * @since 1.50
         */
        public Configuration addTest(Class<? extends TestCase> test, String... testNames) {
            if (test.equals(latestTestCaseClass)){
                return addTest(testNames);
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            addLatest(newTests);
            if ((testNames != null) && (testNames.length != 0)){
                newTests.add(new Item(true, test, testNames));
            }
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, newTests, test, reuseUserDir, gui);
        }
        
        /**
         * Add new {@link  junit.framework.Test} to run. The implementation must
         * have no parametter constructor. TastCase can be also passed as an argument
         * of this method than it's delegated to
         * {@link Configuration#addTest(java.lang.Class, java.lang.String[]) }
         *  
         * @param test Test implementation to add
         * @return clone of this configuration with new Test added to the list
         *  of executed tests
         * @since 1.50
         */
        public Configuration addTest(Class<? extends Test> test) {
            if (TestCase.class.isAssignableFrom(test)){
                Class<? extends TestCase> tc = test.asSubclass(TestCase.class);
                return addTest(tc, new String[0]);
            }
            List<Item> newTests = new ArrayList<Item>(tests);
            newTests.add(new Item(false, test, null));
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, newTests, latestTestCaseClass, reuseUserDir, gui);
        }

        private void addLatest(List<Item> newTests) {
            if (latestTestCaseClass == null){
                return;
            }
            for (Item item : newTests) {
                if (item.clazz.equals(latestTestCaseClass)){
                    return;
                }
            }
            newTests.add(new Item(true, latestTestCaseClass, null));
        }

        private Configuration getReady() {
            List<Item> newTests = new ArrayList<Item>(tests);
            addLatest(newTests);
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, newTests, latestTestCaseClass, reuseUserDir, gui);
        }
        
        /** Should the system run with GUI or without? The default behaviour
         * does not prevent any module to show UI. If <code>false</code> is 
         * used, then the whole system is instructed with <code>--nogui</code>
         * option that it shall run as much as possible in invisible mode. As
         * a result, the main window is not shown after the start, for example.
         * 
         * @param gui true or false
         * @return clone of this configuration with gui mode associated
         */
        public Configuration gui(boolean gui) {
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, tests, latestTestCaseClass, reuseUserDir, gui);
        }

        /**
         * Enables or disables userdir reuse. By default it is disabled.
         * @param reuse true or false
         * @return clone of this configuration with userdir reuse mode associated
         * @since 1.52
         */
        public Configuration reuseUserDir(boolean reuse) {
            return new Configuration(clusterRegExp, moduleRegExp, parentClassLoader, tests, latestTestCaseClass, reuse, gui);
        }
        }

    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. The set of enabled modules is going to be
     * determined from the actual classpath of a module, which is common
     * when in all NetBeans tests. All other modules are kept disabled.
     * In addition,it allows one limit the clusters that shall be made available.
     * For example <code>ide.*|java.*</code> will start the container just
     * with platform, ide and java clusters.
     * 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param clustersRegExp regexp to apply to name of cluster to find out if it is supposed to be included
     *    in the runtime container setup or not
     * @param moduleRegExp by default all modules on classpath are turned on,
     *    however this regular expression can specify additional ones. If not
     *    null, the specified cluster will be searched for modules with such
     *    codenamebase and those will be turned on
     * @return runtime container ready test
     */
    public static Test create(Class<? extends TestCase> clazz, String clustersRegExp, String moduleRegExp) {
        return new S(Configuration.create(clazz).clusters(clustersRegExp).enableModules(moduleRegExp));
    }
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. The set of enabled modules is going to be
     * determined from the actual classpath of a module, which is common
     * when in all NetBeans tests. All other modules are kept disabled.
     * In addition,it allows one limit the clusters that shall be made available.
     * For example <code>ide.*|java.*</code> will start the container just
     * with platform, ide and java clusters.
     * 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param clustersRegExp regexp to apply to name of cluster to find out if it is supposed to be included
     *    in the runtime container setup or not
     * @param moduleRegExp by default all modules on classpath are turned on,
     *    however this regular expression can specify additional ones. If not
     *    null, the specified cluster will be searched for modules with such
     *    codenamebase and those will be turned on
     * @param tests names of test methods to execute from the <code>clazz</code>, if
     *    no test methods are specified, all tests in the class are executed
     * @return runtime container ready test
     * @since 1.49
     */
    public static Test create(Class<? extends TestCase> clazz, String clustersRegExp, String moduleRegExp, String... tests) {
        Configuration conf = Configuration.create(clazz).clusters(clustersRegExp).enableModules(moduleRegExp);
        if (tests.length > 0) {
            conf = conf.addTest(tests);
        } 
        return new S(conf);
    }
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. 
     * Wraps the provided class into a test that set ups properly the
     * testing environment. All modules, in all clusters, 
     * in the tested applicationwill be included in the test. 
     * 
     * @param clazz the class with bunch of testXYZ methods
     * @param tests names of test methods to execute from the <code>clazz</code>
     * @return runtime container ready test
     * @since 1.49
     */
    public static Test allModules(Class<? extends TestCase> clazz, String... tests) {
        return create(clazz, ".*", ".*", tests);
    }
    
    /** Creates default configuration wrapping a class that can be executed
     * with the {@link NbModuleSuite} support.
     * 
     * @param clazz the class to test, the actual instances will be created
     *   for the class of the same name, but loaded by different classloader
     * @return config object prefilled with default values; the defaults may
     *   be altered with its addition instance methods
     * @since 1.48
     */
    public static Configuration createConfiguration(Class<? extends TestCase> clazz) {
        return Configuration.create(clazz);
    }
    
    /** Creates empty configuration without any class assiciated. You need
     * to call {@link Configuration#addTest(java.lang.Class, java.lang.String[])}
     * then to register proper test classes.
     * 
     * @return config object prefilled with default values; the defaults may
     *   be altered with its addition instance methods
     * @since 1.50
     */
    public static Configuration emptyConfiguration() {
        return Configuration.create(null);
    }
    
    
    /** Factory method to create wrapper test that knows how to setup proper
     * NetBeans Runtime Container environment. This method allows better
     * customization and control over the executed set of tests.
     * Wraps the provided class into a test that set ups properly the
     * testing environment, read more in {@link Configuration}.
     * 
     * 
     * @param config the configuration for the test
     * @return runtime container ready test
     * @since 1.48
     */
    public static Test create(Configuration config) {
        return new S(config);
    }

    private static final class Item {
        boolean isTestCase;
        Class<?> clazz;
        String[] fileNames;

        public Item(boolean isTestCase, Class<?> clazz, String[] fileNames) {
            this.isTestCase = isTestCase;
            this.clazz = clazz;
            this.fileNames = fileNames;
        }
    }

    static final class S extends NbTestSuite {
        final Configuration config;
        private static int invocations;
        private static File lastUserDir;
        private int testCount = 0; 
        
        public S(Configuration config) {
            this.config = config.getReady();
        }

        @Override
        public int countTestCases() {
            return testCount;
        }

        @Override
        public void run(TestResult result) {
            try {
                runInRuntimeContainer(result);
            } catch (Exception ex) {
                result.addError(this, ex);
            }
        }

        private void runInRuntimeContainer(TestResult result) throws Exception {
            File platform = findPlatform();
            File[] boot = new File(platform, "lib").listFiles();
            List<URL> bootCP = new ArrayList<URL>();
            for (int i = 0; i < boot.length; i++) {
                URL u = boot[i].toURL();
                if (u.toExternalForm().endsWith(".jar")) {
                    bootCP.add(u);
                }
            }
            
            File jdkHome = new File(System.getProperty("java.home"));
            if (!"Mac OS X".equals(System.getProperty("os.name"))) {
                jdkHome = jdkHome.getParentFile();
            }
            File jdkLib = new File(jdkHome, "lib");
            if (jdkLib.isDirectory()) {
                for (File jar : jdkLib.listFiles()) {
                    if (jar.getName().endsWith(".jar")) {
                        bootCP.add(jar.toURI().toURL());
                    }
                }
            }
            
            // loader that does not see our current classloader
            JUnitLoader junit = new JUnitLoader(config.parentClassLoader, NbModuleSuite.class.getClassLoader());
            URLClassLoader loader = new URLClassLoader(bootCP.toArray(new URL[0]), junit);
            Class<?> main = loader.loadClass("org.netbeans.Main"); // NOI18N
            Assert.assertEquals("Loaded by our classloader", loader, main.getClassLoader());
            Method m = main.getDeclaredMethod("main", String[].class); // NOI18N

            System.setProperty("java.util.logging.config", "-");
            System.setProperty("netbeans.logger.console", "true");
            System.setProperty("netbeans.home", platform.getPath());
            System.setProperty("netbeans.full.hack", "true");

            File ud = new File(new File(Manager.getWorkDirPath()), "userdir" + invocations++);
            if (config.reuseUserDir) {
                ud = lastUserDir != null ? lastUserDir : ud;
            } else {
                NbTestCase.deleteSubFiles(ud);
            }
            lastUserDir = ud;
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());

            TreeSet<String> modules = new TreeSet<String>();
            modules.addAll(findEnabledModules(NbTestSuite.class.getClassLoader()));
            modules.add("org.openide.filesystems");
            modules.add("org.openide.modules");
            modules.add("org.openide.util");
            modules.remove("org.netbeans.insane");
            modules.add("org.netbeans.core.startup");
            modules.add("org.netbeans.bootstrap");
            turnModules(ud, modules, config.moduleRegExp, platform);
            turnClassPathModules(ud, NbTestSuite.class.getClassLoader());

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (File f : findClusters()) {
                turnModules(ud, modules, config.moduleRegExp, f);
                sb.append(sep);
                sb.append(f.getPath());
                sep = File.pathSeparator;
            }
            System.setProperty("netbeans.dirs", sb.toString());

            System.setProperty("netbeans.security.nocheck", "true");

            List<Class<?>> allClasses = new ArrayList<Class<?>>(config.tests.size());
            for (Item item : config.tests) {
                allClasses.add(item.clazz);
            }
            preparePatches(System.getProperty("java.class.path"), System.getProperties(), allClasses.toArray(new Class[0]));
            
            List<String> args = new ArrayList<String>();
            args.add("--nosplash");
            if (!config.gui) {
                args.add("--nogui");
            }
            m.invoke(null, (Object)args.toArray(new String[0]));

            ClassLoader global = Thread.currentThread().getContextClassLoader();
            Assert.assertNotNull("Global classloader is initialized", global);
            ClassLoader testLoader = global;
            try {
                testLoader.loadClass("junit.framework.Test");
                testLoader.loadClass("org.netbeans.junit.NbTestSuite");
                NbTestSuite toRun = new NbTestSuite();
                
                for (Item item : config.tests) {
                    if (item.isTestCase){
                        Class<? extends TestCase> sndClazz =
                            testLoader.loadClass(item.clazz.getName()).asSubclass(TestCase.class);
                        if (item.fileNames == null) {
                            toRun.addTest(new NbTestSuite(sndClazz));
                        } else {
                            NbTestSuite t = new NbTestSuite();
                            t.addTests(sndClazz, item.fileNames);
                            toRun.addTest(t);
                        }
                    }else{
                        Class<? extends Test> sndClazz =
                            testLoader.loadClass(item.clazz.getName()).asSubclass(Test.class);
                        toRun.addTest(sndClazz.newInstance());
                    }
                }
                testCount = toRun.countTestCases();
                toRun.run(result);
            } catch (ClassNotFoundException ex) {
                result.addError(this, ex);
            } catch (NoClassDefFoundError ex) {
                result.addError(this, ex);
            }
            
            Class<?> lifeClazz = global.loadClass("org.openide.LifecycleManager"); // NOI18N
            Method getDefault = lifeClazz.getMethod("getDefault"); // NOI18N
            Method exit = lifeClazz.getMethod("exit");
            Object life = getDefault.invoke(null);
            if (!life.getClass().getName().startsWith("org.openide.LifecycleManager")) { // NOI18N
                System.setProperty("netbeans.close.no.exit", "true"); // NOI18N
                exit.invoke(life);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    }
                });
            }
        }

        private File findPlatform() {
            try {
                Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
                File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
                Assert.assertTrue("Util exists: " + util, util.exists());

                return util.getParentFile().getParentFile();
            } catch (Exception ex) {
                try {
                    File nbjunit = new File(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    File harness = nbjunit.getParentFile().getParentFile();
                    Assert.assertEquals("NbJUnit is in harness", "harness", harness.getName());
                    TreeSet<File> sorted = new TreeSet<File>();
                    for (File p : harness.getParentFile().listFiles()) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                    Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                    return sorted.last();
                } catch (Exception ex2) {
                    Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
                }
                return null;
            }
        }

        private File[] findClusters() {
            Collection<File> clusters = new LinkedHashSet<File>();
            if (config.clusterRegExp != null) {
                File plat = findPlatform();

                for (File f : plat.getParentFile().listFiles()) {
                    if (f.equals(plat)) {
                        continue;
                    }
                    if (!f.getName().matches(config.clusterRegExp)) {
                        continue;
                    }
                    File m = new File(new File(f, "config"), "Modules");
                    if (m.exists()) {
                        clusters.add(f);
                    }
                }
            }
            
            // find "cluster" from
            // k/o.n.m.a.p.N/csam/testModule/build/cluster/modules/org-example-testModule.jar
            // tested in apisupport.project
            for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                File module = new File(s);
                File cluster = module.getParentFile().getParentFile();
                File m = new File(new File(cluster, "config"), "Modules");
                if (m.exists() || cluster.getName().equals("cluster")) {
                    clusters.add(cluster);
                }
            }
            return clusters.toArray(new File[0]);
        }
        
        private static Pattern CODENAME = Pattern.compile("OpenIDE-Module: *([^/$ \n\r]*)[/]?[0-9]*", Pattern.MULTILINE);
        private static Pattern VERSION = Pattern.compile("OpenIDE-Module-Specification-Version: *([0-9\\.]*)", Pattern.MULTILINE);
        /** Looks for all modules on classpath of given loader and builds 
         * their list from them.
         */
        static Set<String> findEnabledModules(ClassLoader loader) throws IOException {
            Set<String> cnbs = new TreeSet<String>();

            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                String manifest = asString(url.openStream(), true);
                Matcher m = CODENAME.matcher(manifest);
                if (m.find()) {
                    cnbs.add(m.group(1));
                }
            }

            return cnbs;
        }
        private static void turnClassPathModules(File ud, ClassLoader loader) throws IOException {
            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                String manifest = asString(url.openStream(), true);
                Matcher m = CODENAME.matcher(manifest);
                if (m.find()) {
                    String cnb = m.group(1);
                    Matcher v = VERSION.matcher(manifest);
                    if (!v.find()) {
                        throw new IllegalStateException("Cannot find version:\n" + manifest);
                    }
                    File jar = jarFromURL(url);
                    if (jar.getParentFile().getName().equals("lib")) {
                        // Otherwise will get DuplicateException.
                        continue;
                    }
                    if (jar.getParentFile().getName().equals("core")) {
                        // Otherwise will get DuplicateException.
                        continue;
                    }
                    String xml =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"" + cnb + "\">\n" +
"    <param name=\"autoload\">false</param>\n" +
"    <param name=\"eager\">false</param>\n" +
"    <param name=\"enabled\">true</param>\n" +
"    <param name=\"jar\">" + jar + "</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"    <param name=\"specversion\">" + v.group(1) + "</param>\n" +
"</module>\n";
                    
                    File conf = new File(new File(ud, "config"), "Modules");
                    conf.mkdirs();
                    File f = new File(conf, cnb.replace('.', '-') + ".xml");
                    writeModule(f, xml);
                }
            }
        }
        
        private static Pattern MANIFEST = Pattern.compile("jar:(file:.*)!/META-INF/MANIFEST.MF", Pattern.MULTILINE);
        private static File jarFromURL(URL u) {
            Matcher m = MANIFEST.matcher(u.toExternalForm());
            if (m.matches()) {
                return new File(URI.create(m.group(1)));
            } else {
                throw new IllegalStateException(u.toExternalForm());
            }
        }

        
        static void preparePatches(String path, Properties prop, Class... classes) throws URISyntaxException {
            Pattern tests = Pattern.compile(".*\\" + File.separator + "([^\\" + File.separator + "]+)\\" + File.separator + "tests\\.jar");
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String jar : path.split(File.pathSeparator)) {
                Matcher m = tests.matcher(jar);
                if (m.matches()) {
                    // in case we need it one day, let's add a switch to Configuration
                    // and choose the following line instead of netbeans.systemclassloader.patches
                    // prop.setProperty("netbeans.patches." + m.group(1).replace('-', '.'), jar);
                    sb.append(sep).append(jar);
                    sep = File.pathSeparator;
                }
            }
            for (Class c : classes) {
                URL test = c.getProtectionDomain().getCodeSource().getLocation();
                Assert.assertNotNull("URL found for " + c, test);
                sb.append(sep).append(new File(test.toURI()).getPath());
                sep = File.pathSeparator;
            }
            prop.setProperty("netbeans.systemclassloader.patches", sb.toString());
        }

        private static String asString(InputStream is, boolean close) throws IOException {
            StringBuilder builder = new StringBuilder();

            byte[] bytes = new byte[4096];
            try {
                for (int i; (i = is.read(bytes)) != -1;) {
                    builder.append(new String(bytes, 0, i, "UTF-8"));
                }
            } finally {
                if (close) {
                    is.close();
                }
            }

            return builder.toString();
        }

        private static final class JUnitLoader extends ClassLoader {
            private final ClassLoader junit;

            public JUnitLoader(ClassLoader parent, ClassLoader junit) {
                super(parent);
                this.junit = junit;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (isUnit(name)) {
                    return junit.loadClass(name);
                }
                return super.findClass(name);
            }

            @Override
            public URL findResource(String name) {
                if (isUnit(name)) {
                    return junit.getResource(name);
                }
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                if (isUnit(name)) {
                    return junit.getResources(name);
                }
                return super.findResources(name);
            }

            private final boolean isUnit(String res) {
                if (res.startsWith("junit")) {
                    return true;
                }
                if (res.startsWith("org.junit") || res.startsWith("org/junit")) {
                    return true;
                }
                if (res.startsWith("org.netbeans.junit") || res.startsWith("org/netbeans/junit")) {
                    if (res.startsWith("org.netbeans.junit.ide") || res.startsWith("org/netbeans/junit/ide")) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }

        private static Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);
        private static Pattern AUTO = Pattern.compile("<param name=[\"']autoload[\"']>([^<]*)</param>", Pattern.MULTILINE);
        
        private static void turnModules(File ud, TreeSet<String> modules, String regExp, File... clusterDirs) throws IOException {
            File config = new File(new File(ud, "config"), "Modules");
            config.mkdirs();

            Pattern modPattern = regExp == null ? null : Pattern.compile(regExp);
            for (File c : clusterDirs) {
                File modulesDir = new File(new File(c, "config"), "Modules");
                File[] allModules = modulesDir.listFiles();
                if (allModules == null) {
                    continue;
                }
                for (File m : allModules) {
                    String n = m.getName();
                    if (n.endsWith(".xml")) {
                        n = n.substring(0, n.length() - 4);
                    }
                    n = n.replace('-', '.');

                    String xml = asString(new FileInputStream(m), true);
                    
                    boolean contains = modules.contains(n);
                    if (!contains && modPattern != null) {
                        contains = modPattern.matcher(n).matches();
                    }
                    if (!contains) {
                        continue;
                    }
                    enableModule(xml, contains, new File(config, m.getName()));
                }
            }
        }
        
        private static void enableModule(String xml, boolean enable, File target) throws IOException {
            boolean toEnable = false;
            {
                Matcher matcherEnabled = ENABLED.matcher(xml);
                if (matcherEnabled.find()) {
                    toEnable = "false".equals(matcherEnabled.group(1));
                }
                if (toEnable) {
                    assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;
                    try {
                        String out = xml.substring(0, matcherEnabled.start(1)) + (enable ? "true" : "false") + xml.substring(matcherEnabled.end(1));
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw (IOException) new IOException("Unparsable:\n" + xml).initCause(ex);
                    }
                }
            }
            {
                Matcher matcherEager = AUTO.matcher(xml);
                if (matcherEager.find()) {
                    int begin = xml.indexOf("<param name=\"autoload");
                    int end = xml.indexOf("<param name=\"jar");
                    String middle = "<param name=\"autoload\">false</param>\n" + "    <param name=\"eager\">false</param>\n" + "    <param name=\"enabled\">true</param>\n" + "    ";
                    String out = xml.substring(0, begin) + middle + xml.substring(end);
                    try {
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw (IOException) new IOException("Unparsable:\n" + xml).initCause(ex);
                    }
                }
            }
        }

        private static void writeModule(File file, String xml) throws IOException {
            FileOutputStream os = new FileOutputStream(file);
            os.write(xml.getBytes("UTF-8"));
            os.close();
        }
    } // end of S
}
