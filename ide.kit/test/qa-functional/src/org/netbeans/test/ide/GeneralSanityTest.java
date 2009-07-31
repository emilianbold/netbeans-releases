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

package org.netbeans.test.ide;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.permanentUI.utils.Utilities;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class GeneralSanityTest extends NbTestCase {

    public GeneralSanityTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        CountingSecurityManager.initWrites();

        // disable 'slowness detection'
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000");
        NbTestSuite s = new NbTestSuite();
        s.addTest(new GeneralSanityTest("testInitBlacklistedClassesHandler"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.createConfiguration(
                GeneralSanityTest.class
            ).gui(true).clusters(".*").enableModules(".*").
            honorAutoloadEager(true).
            addTest(
                "testWaitForUIReady",
                "testNoWrites",
                "testBlacklistedClassesHandler",
                "testOrgOpenideOptionsIsDisabledAutoload",
                "testOrgNetBeansModulesLanguagesIsDisabledAutoload",
                "testOrgNetBeansModulesGsfIsDisabledAutoload",
                "testInstalledPlugins"
            )
        ));
        return s;
    }

    public void testNoWrites() throws Exception {
        String msg = "No writes during startup.\n" +
            "Writing any files to disk during start is inefficient and usualy unnecessary.\n" +
            "Consider using declarative registration in your layer.xml file, or delaying\n" +
            "the initialization of the whole subsystem till it is really used.\n" +
            "In case it is necessary to perform the write, you can modify the\n" +
            "'allowed-file-write.txt' file in ide.kit module. More details at\n" +
            "http://wiki.netbeans.org/FitnessViaWhiteAndBlackList";

        CountingSecurityManager.assertCounts(msg, 0);
        // disable further collecting of
        CountingSecurityManager.initialize("non-existent", CountingSecurityManager.Mode.CHECK_READ, null);
    }

    public void testInitBlacklistedClassesHandler() {
        String configFN = new File(getDataDir(), "BlacklistedClassesHandlerConfig.xml").getPath();
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();

        System.out.println("BlacklistedClassesHandler will be initialized with " + configFN);
        if (bcHandler.initSingleton(configFN)) {
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
        } else {
            fail("Cannot initialize blacklisted class handler");
        }
    }

    public void testWaitForUIReady() throws Exception {
        class R implements Runnable {
            int countDown = 10;

            public synchronized void run() {
                notifyAll();
                countDown--;
            }

            synchronized final void waitForAWT() throws InterruptedException {
                while (countDown > 0) {
                    WindowManager.getDefault().invokeWhenUIReady(this);
                    wait();
                }
            }
        }
        R r = new R();
        r.waitForAWT();

    }

    public void testBlacklistedClassesHandler() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        if (bcHandler.isGeneratingWhitelist()) {
            bcHandler.saveWhiteList(getLog("whitelist.txt"));
        }
        try {
            if (bcHandler.hasWhitelistStorage()) {
                bcHandler.saveWhiteList();
                bcHandler.saveWhiteList(getLog("whitelist.txt"));
                bcHandler.reportDifference(getLog("diff.txt"));
                assertTrue(bcHandler.reportViolations(getLog("violations.xml"))
                        + bcHandler.reportDifference(), bcHandler.noViolations());
            } else {
                assertTrue(bcHandler.reportViolations(getLog("violations.xml")), bcHandler.noViolations());
            }
        } finally {
            bcHandler.unregister();
        }
    }

    public void testOrgOpenideOptionsIsDisabledAutoload() {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("org.openide.options")) {
                assertFalse("org.openide.options shall not be enabled", m.isEnabled());
                return;
            }
        }
        fail("No org.openide.options module found, it should be present, but disabled");
    }

    public void testOrgNetBeansModulesLanguagesIsDisabledAutoload() {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("org.netbeans.modules.languages")) {
                assertFalse("org.netbeans.modules.languages shall not be enabled", m.isEnabled());
                return;
            }
        }
        fail("No org.netbeans.modules.languages module found, it should be present, but disabled");
    }
    
    public void testOrgNetBeansModulesGsfIsDisabledAutoload() {
        boolean gsfapi = false, gsf = false, gsfpath = false;
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("org.netbeans.modules.gsf.api")) {
                assertFalse("org.netbeans.modules.gsf.api should not be enabled", m.isEnabled());
                gsfapi = true;
            }
            if (m.getCodeNameBase().equals("org.netbeans.modules.gsf")) {
                assertFalse("org.netbeans.modules.gsf should not be enabled", m.isEnabled());
                gsf = true;
            }
            if (m.getCodeNameBase().equals("org.netbeans.modules.gsfpath.api")) {
                assertFalse("org.netbeans.modules.gsfpath.api should not be enabled", m.isEnabled());
                gsfpath = true;
            }
        }
        assertTrue("No org.netbeans.modules.gsf.api module found, it should be present, but disabled", gsfapi);
        assertTrue("No org.netbeans.modules.gsf module found, it should be present, but disabled", gsf);
        assertTrue("No org.netbeans.modules.gsfpath.api module found, it should be present, but disabled", gsfpath);
    }

    public void testInstalledPlugins() throws IOException {
        TreeSet<MyModule> idePlugins = new TreeSet<MyModule>();

        // plugins installed in the IDE
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            assertTrue(m instanceof Module);
            Module mm = (Module)m;
            if ("false".equals(m.getAttribute("AutoUpdate-Show-In-Client"))) {
                continue;
            }
            if (mm.isAutoload() || mm.isEager() || mm.isFixed()) {
                continue;
            }
            idePlugins.add(new MyModule(
                    mm.getCodeNameBase(),
                    mm.getDisplayName(),
                    "" + mm.getLocalizedAttribute("OpenIDE-Module-Display-Category"),
                    getCluster(mm)));
        }

        // add plugins that were not built during this particular build
        idePlugins.addAll(getAbsentModulesFromGoldenFile());

        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";
        final String idePluginsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";


        try {
            PrintStream ideFile = getLog(getName() + "_ide.txt");
            
            //make a diff
            printPlugins(ideFile, idePlugins);
            Manager.getSystemDiff().diff(idePluginsLogFile, getPluginsGoldenFile(), diffFile);
            //assert
            String message = 
                    "The list of visible plugins under Tools -> Plugins -> Installed has changed. \n" +
                    "Please make sure that your plugins correctly declare AutoUpdate-Show-In-Client \n" +
                    "property in their manifest file. If your change to the list of plugins is intentional, \n" +
                    "please follow the UI review process: http://wiki.netbeans.org/UIReviewProcess and change \n" +
                    "the golden file in ide.kit/test/qa-functional/data/permanentUI/plugins/installed-plugins.txt.\n" +
                    Utilities.readFileToString(diffFile);

            assertFile(message, getPluginsGoldenFile() , idePluginsLogFile, diffFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void printPlugins(PrintStream out, Set<MyModule> plugins) {

        out.println("||Codebase||Display name||Category||Cluster");

        for (MyModule m : plugins) {
            out.println(m.toString());
        }
    }

    private static String getCluster(Module m) {
        File cluster = m.getJarFile().getParentFile().getParentFile();
        return stripClusterNumber(cluster.getName());
    }

    private static String stripClusterNumber(String cluster) {
        return Pattern.compile("[0-9]+").split(cluster)[0];
    }
    /**
     * constructs the relative path to the golden file to Installed Plugins permanent UI spec
     * @return
     */
    private String getPluginsGoldenFile() {
        String dataDir = "";
        try {
            dataDir = getDataDir().getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dataDir + File.separator + "permanentUI" + File.separator + "plugins" + File.separator + "installed-plugins.txt";
    }

    private Set<MyModule> getAbsentModulesFromGoldenFile() throws IOException {
        TreeSet<MyModule> result = new TreeSet<MyModule>();
        Set<String> presentClusters = getAllClusters();
        Scanner scanner = new Scanner(new File(getPluginsGoldenFile()));
        scanner.nextLine(); // header row, ignore
        while (scanner.hasNext()) {
            String moduleRow = scanner.nextLine();
            Scanner scanner2 = new Scanner (moduleRow);
            scanner2.useDelimiter("\\x7C"); // | character
            MyModule m = new MyModule(scanner2.next(), scanner2.next(), scanner2.next(), scanner2.next());
            getAllClusters();
            if (!presentClusters.contains(m.cluster)) {
                result.add(m);
            }
        }
        return result;
    }

    private Set<String> getAllClusters() {
        String dirs = System.getProperty("netbeans.dirs");
        String[] dirsArray = Pattern.compile(File.pathSeparator).split(dirs);
        TreeSet<String> result = new TreeSet<String>();
        for (int i = 0; i < dirsArray.length; i++) {
            result.add(stripClusterNumber(new File(dirsArray[i]).getName()));
        }
        return result;
    }


    static class MyModule implements Comparable<MyModule> {
        String codeNameBase;
        String displayName;
        String displayCategory;
        String cluster;

        public MyModule(String codeNameBase, String displayName, String displayCategory, String cluster) {
            this.codeNameBase    = codeNameBase;
            this.displayName     = displayName;
            this.displayCategory = displayCategory;
            this.cluster         = cluster;
        }

        public int compareTo(MyModule m2) {
            String s = displayCategory + " " + displayName;
            return s.compareToIgnoreCase(m2.displayCategory + " " + m2.displayName);
        }

        @Override
        public String toString() {
            String output = "";
            output += "|" + codeNameBase;
            output += "|" + displayName;
            output += "|" + displayCategory;
            output += "|" + cluster;
            return output;
        }

    }

}
