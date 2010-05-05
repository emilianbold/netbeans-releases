/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.junit.internal.NbModuleLogHandler;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger LOG;

    static {
        System.setProperty("org.netbeans.MainImpl.154417", "true");
        LOG = Logger.getLogger(NbModuleSuite.class.getName());
    }

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
        final List<String> clusterRegExp;
        /** each odd is cluster reg exp, each even is module reg exp */
        final List<String> moduleRegExp;
        final ClassLoader parentClassLoader;
        final boolean reuseUserDir;
        final boolean gui;
        final boolean enableClasspathModules;
        final boolean honorAutoEager;
        final Level failOnMessage;
        final Level failOnException;

        private Configuration(
            List<String> clusterRegExp,
            List<String> moduleRegExp,
            ClassLoader parent,
            List<Item> testItems,
            Class<? extends TestCase> latestTestCase,
            boolean reuseUserDir,
            boolean gui,
            boolean enableCPModules,
            boolean honorAutoEager,
            Level failOnMessage,
            Level failOnException
        ) {
            this.clusterRegExp = clusterRegExp;
            this.moduleRegExp = moduleRegExp;
            this.parentClassLoader = parent;
            this.tests = testItems;
            this.reuseUserDir = reuseUserDir;
            this.latestTestCaseClass = latestTestCase;
            this.gui = gui;
            this.enableClasspathModules = enableCPModules;
            this.honorAutoEager = honorAutoEager;
            this.failOnException = failOnException;
            this.failOnMessage = failOnMessage;
        }

        static Configuration create(Class<? extends TestCase> clazz) {            
            return new Configuration(
                null, null, ClassLoader.getSystemClassLoader().getParent(),
                Collections.<Item>emptyList(), clazz, false, true, true, false
                , null, null);
        }
        
        /** Regular expression to match clusters that shall be enabled.
         * To enable all cluster, one can use <code>".*"</code>. To enable
         * ide and java clusters, it is handy to pass in <code>"ide.*|java.*</code>.
         * There is no need to requrest presence of <code>platform.*</code> cluster,
         * as that is available all the time by default.
         * <p>
         * Since version 1.55 this method can be called multiple times.
         * 
         * @param regExp regular expression to match cluster names
         * @return clone of this configuration with cluster set to regExp value
         */
        public Configuration clusters(String regExp) {
            ArrayList<String> list = new ArrayList<String>();
            if (clusterRegExp != null) {
                list.addAll(clusterRegExp);
            }
            if (regExp != null) {
                list.add(regExp);
            }
            if (list.isEmpty()) {
                list = null;
            }
            return new Configuration(
                list, moduleRegExp, parentClassLoader, tests,
                latestTestCaseClass, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException);
        }

        /** By default only modules on classpath of the test are enabled, 
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules
         * in all clusters pass in <code>".*"</code>. Since 1.55 this method
         * is cummulative.
         * 
         * @param regExp regular expression to match code name base of modules
         * @return clone of this configuration with enable modules set to regExp value
         */
        public Configuration enableModules(String regExp) {
            if (regExp == null) {
                return this;
            }
            return enableModules(".*", regExp);
        }

        /** By default only modules on classpath of the test are enabled,
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules in
         * one cluster, use this method and pass <code>".*"</code> as list of
         * modules. This method is cumulative.
         *
         * @param clusterRegExp regular expression to match clusters
         * @param moduleRegExp regular expression to match code name base of modules
         * @return clone of this configuration with enable modules set to regExp value
         * @since 1.55
         */
        public Configuration enableModules(String clusterRegExp, String moduleRegExp) {
            List<String> arr = new ArrayList<String>();
            if (this.moduleRegExp != null) {
                arr.addAll(this.moduleRegExp);
            }
            arr.add(clusterRegExp);
            arr.add(moduleRegExp);
            return new Configuration(
                this.clusterRegExp, arr, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules, honorAutoEager, failOnMessage, failOnException);
        }

        Configuration classLoader(ClassLoader parent) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parent, tests,
                latestTestCaseClass, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException);
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
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules, honorAutoEager, failOnMessage, failOnException);
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
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                newTests, test, reuseUserDir, gui, enableClasspathModules,
                honorAutoEager
            , failOnMessage, failOnException);
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
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
            , failOnMessage, failOnException);
        }

        /** By default all modules on classpath are enabled (so you can link
         * with modules that you compile against), this method allows you to
         * disable this feature, which is useful if the test is known to not
         * link against any of classpath classes.
         *
         * @param enable pass false to ignore modules on classpath
         * @return new configuration clone
         * @since 1.56
         */
        public Configuration enableClasspathModules(boolean enable) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enable, honorAutoEager, failOnMessage, failOnException);
        }

        /** By default the {@link #enableModules(java.lang.String)} method
         * converts all autoloads into regular modules and enables them. This
         * is maybe useful in certain situations, but does not really mimic the
         * real behaviour of the system when it is executed. Those who need
         * to as closely as possible simulate the real run, can use
         * <code>honorAutoloadEager(true)</code>.
         *
         * @param honor true in case autoloads shall remain autoloads and eager modules eager
         * @return new configuration filled with this data
         * @since 1.57
         */
        public Configuration honorAutoloadEager(boolean honor) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honor
            , failOnMessage, failOnException);
        }

        /** Fails if there is a message sent to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @param level the minimal level of the message
         * @return new configuration filled with this data
         * @since 1.58
         */
        public Configuration failOnMessage(Level level) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
                , level, failOnException);
        }

        /** Fails if there is an exception reported to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @param level the minimal level of the message
         * @return new configuration filled with this data
         * @since 1.58
         */
        public Configuration failOnException(Level level) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir,
                gui, enableClasspathModules, honorAutoEager
                , failOnMessage, level);
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
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                newTests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException);
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
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader,
                tests, latestTestCaseClass, reuseUserDir, gui,
                enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException);
        }

        /**
         * Enables or disables userdir reuse. By default it is disabled.
         * @param reuse true or false
         * @return clone of this configuration with userdir reuse mode associated
         * @since 1.52
         */
        public Configuration reuseUserDir(boolean reuse) {
            return new Configuration(
                clusterRegExp, moduleRegExp, parentClassLoader, tests,
                latestTestCaseClass, reuse, gui, enableClasspathModules
            ,honorAutoEager, failOnMessage, failOnException);
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
     * @param tests names of test methods to execute from the <code>clazz</code>, if
     *    no test methods are specified, all tests in the class are executed
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
        
        private static String[] tokenizePath(String path) {
            List<String> l = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
            char dosHack = '\0';
            char lastDelim = '\0';
            int delimCount = 0;
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                if (s.length() == 0) {
                    // Strip empty components.
                    continue;
                }
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if (c == ':' || c == ';') {
                        // Just a delimiter.
                        lastDelim = c;
                        delimCount++;
                        continue;
                    }
                }
                if (dosHack != '\0') {
                    // #50679 - "C:/something" is also accepted as DOS path
                    if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                        // We had a single letter followed by ':' now followed by \something or /something
                        s = "" + dosHack + ':' + s;
                        // and use the new token with the drive prefix...
                    } else {
                        // Something else, leave alone.
                        l.add(Character.toString(dosHack));
                        // and continue with this token too...
                    }
                    dosHack = '\0';
                }
                // Reset count of # of delimiters in a row.
                delimCount = 0;
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        // Probably a DOS drive letter. Leave it with the next component.
                        dosHack = c;
                        continue;
                    }
                }
                l.add(s);
            }
            if (dosHack != '\0') {
                //the dosHack was the last letter in the input string (not followed by the ':')
                //so obviously not a drive letter.
                //Fix for issue #57304
                l.add(Character.toString(dosHack));
            }
            return l.toArray(new String[l.size()]);
        }

        static void findClusters(Collection<File> clusters, List<String> regExps) throws IOException {
            File plat = findPlatform().getCanonicalFile();
            String selectiveClusters = System.getProperty("cluster.path.final"); // NOI18N
            Set<File> path = null;
            if (selectiveClusters != null) {
                path = new HashSet<File>();
                for (String p : tokenizePath(selectiveClusters)) {
                    File f = new File(p);
                    path.add(f.getCanonicalFile());
                }
            }
            if (path == null) {
                path = new HashSet<File>(Arrays.asList(plat.getParentFile().listFiles()));
            }
            for (String c : regExps) {
                for (File f : path) {
                    if (f.equals(plat)) {
                        continue;
                    }
                    if (!f.getName().matches(c)) {
                        continue;
                    }
                    File m = new File(new File(f, "config"), "Modules");
                    if (m.exists()) {
                        clusters.add(f);
                    }
                }
            }
        }

        private void runInRuntimeContainer(TestResult result) throws Exception {
            System.getProperties().remove("netbeans.dirs");
            File platform = findPlatform();
            File[] boot = new File(platform, "lib").listFiles();
            List<URL> bootCP = new ArrayList<URL>();
            for (int i = 0; i < boot.length; i++) {
                URL u = boot[i].toURI().toURL();
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

            //in case we're running code coverage, load the coverage libraries
            if (System.getProperty("code.coverage.classpath") != null)
            {
                File coveragePath = new File(System.getProperty("code.coverage.classpath"));
                if (coveragePath.isDirectory()) {
                    for (File jar : coveragePath.listFiles()) {
                        if (jar.getName().endsWith(".jar")) {
                            bootCP.add(jar.toURI().toURL());
                        }
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
            if (System.getProperty("netbeans.logger.noSystem") == null) {
                System.setProperty("netbeans.logger.noSystem", "true");
            }
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
            if (config.enableClasspathModules) {
                modules.addAll(findEnabledModules(NbTestSuite.class.getClassLoader()));
            }
            modules.add("org.openide.filesystems");
            modules.add("org.openide.modules");
            modules.add("org.openide.util");
            modules.remove("org.netbeans.insane");
            modules.add("org.netbeans.core.startup");
            modules.add("org.netbeans.bootstrap");
            turnModules(ud, !config.honorAutoEager, modules, config.moduleRegExp, platform);
            if (config.enableClasspathModules) {
                turnClassPathModules(ud, NbTestSuite.class.getClassLoader());
            }

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (File f : findClusters()) {
                turnModules(ud, !config.honorAutoEager, modules, config.moduleRegExp, f);
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

            Test handler = NbModuleLogHandler.registerBuffer(config.failOnMessage, config.failOnException);
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
                            toRun.addTest(new NbTestSuiteLogCheck(sndClazz));
                        } else {
                            NbTestSuite t = new NbTestSuiteLogCheck();
                            t.addTests(sndClazz, item.fileNames);
                            toRun.addTest(t);
                        }
                    }else{
                        Class<? extends Test> sndClazz =
                            testLoader.loadClass(item.clazz.getName()).asSubclass(Test.class);
                        toRun.addTest(sndClazz.newInstance());
                    }
                }

                if (handler != null) {
                    toRun.addTest(handler);
                }

                testCount = toRun.countTestCases();
                toRun.run(result);
            } catch (ClassNotFoundException ex) {
                result.addError(this, ex);
            } catch (NoClassDefFoundError ex) {
                result.addError(this, ex);
            }
            if (handler != null) {
                NbModuleLogHandler.finish();
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

        static File findPlatform() {
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

        private File[] findClusters() throws IOException {
            Collection<File> clusters = new LinkedHashSet<File>();
            if (config.clusterRegExp != null) {
                findClusters(clusters, config.clusterRegExp);
            }

            if (config.enableClasspathModules) {
                // find "cluster" from
                // k/o.n.m.a.p.N/csam/testModule/build/cluster/modules/org-example-testModule.jar
                // tested in apisupport.project
                for (String s : tokenizePath(System.getProperty("java.class.path"))) {
                    File module = new File(s);
                    File cluster = module.getParentFile().getParentFile();
                    File m = new File(new File(cluster, "config"), "Modules");
                    if (m.exists() || cluster.getName().equals("cluster")) {
                        clusters.add(cluster);
                    }
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
                    if (jar == null) {
                        continue;
                    }
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
                if (!u.getProtocol().equals("file")) {
                    throw new IllegalStateException(u.toExternalForm());
                } else {
                    return null;
                }
            }
        }

        
        static void preparePatches(String path, Properties prop, Class... classes) throws URISyntaxException {
            Pattern tests = Pattern.compile(".*\\" + File.separator + "([^\\" + File.separator + "]+)\\" + File.separator + "tests\\.jar");
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String jar : tokenizePath(path)) {
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
            for (;;) {
                int index = builder.indexOf("\r\n");
                if (index == -1) {
                    break;
                }
                builder.deleteCharAt(index);
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
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResource("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
                }
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                if (isUnit(name)) {
                    return junit.getResources(name);
                }
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResources("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
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
        private static Pattern EAGER = Pattern.compile("<param name=[\"']eager[\"']>([^<]*)</param>", Pattern.MULTILINE);
        
        private static void turnModules(File ud, boolean autoloads, TreeSet<String> modules, List<String> regExp, File... clusterDirs) throws IOException {
            if (regExp == null) {
                return;
            }
            File config = new File(new File(ud, "config"), "Modules");
            config.mkdirs();

            Iterator<String> it = regExp.iterator();
            for (;;) {
                if (!it.hasNext()) {
                    break;
                }
                String clusterReg = it.next();
                String moduleReg = it.next();
                Pattern modPattern = Pattern.compile(moduleReg);
                for (File c : clusterDirs) {
                    if (!c.getName().matches(clusterReg)) {
                        continue;
                    }

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
                        enableModule(xml, autoloads, contains, new File(config, m.getName()));
                    }
                }
            }
        }
        
        private static void enableModule(String xml, boolean autoloads, boolean enable, File target) throws IOException {
            boolean toEnable = false;
            {
                  Matcher matcherEnabled = ENABLED.matcher(xml);
                if (matcherEnabled.find()) {
                    toEnable = "false".equals(matcherEnabled.group(1));
                }
                Matcher matcherEager = EAGER.matcher(xml);
                if (matcherEager.find()) {
                    if ("true".equals(matcherEager.group(1))) {
                        return;
                    }
                }
                if (!autoloads) {
                    Matcher matcherAuto = AUTO.matcher(xml);
                    if (matcherAuto.find()) {
                        if ("true".equals(matcherAuto.group(1))) {
                            return;
                        }
                    }
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
            String previous = null;
            if (file.exists()) {
                previous = asString(new FileInputStream(file), true);
                if (previous.equals(xml)) {
                    return;
                }
                LOG.fine("rewrite module file: " + file);
                charDump(previous);
                LOG.fine("new----");
                charDump(xml);
                LOG.fine("end----");
            }
            FileOutputStream os = new FileOutputStream(file);
            os.write(xml.getBytes("UTF-8"));
            os.close();
        }

        private static void charDump(String text) {
            StringBuilder sb = new StringBuilder(5 * text.length());
            for (int i = 0; i < text.length(); i++) {
                if (i % 8 == 0) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                } else {
                    sb.append(' ');
                }

                int ch = text.charAt(i);
                if (' ' <= ch && ch <= 'z') {
                    sb.append('\'').append((char)ch).append('\'');
                } else {
                    sb.append('x').append(two(Integer.toHexString(ch).toUpperCase()));
                }
            }
            sb.append('\n');
            LOG.fine(sb.toString());
        }

        private static String two(String s) {
            int len = s.length();
            switch (len) {
                case 0: return "00";
                case 1: return "0" + s;
                case 2: return s;
                default: return s.substring(len - 2);
            }
        }

    } // end of S

    private static class NbTestSuiteLogCheck extends NbTestSuite {
        public NbTestSuiteLogCheck() {
        }
        public NbTestSuiteLogCheck(Class<? extends TestCase> clazz) {
            super(clazz);
        }

        @Override
        public void runTest(Test test, TestResult result) {
            int e = result.errorCount();
            int f = result.failureCount();
            super.runTest(test, result);
            if (e == result.errorCount() && f == result.failureCount()) {
                NbModuleLogHandler.checkFailures((TestCase) test, result);
            }
        }
    }
}
