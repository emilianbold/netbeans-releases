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

package org.netbeans.test.editor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.permanentUI.utils.Utilities;
import org.netbeans.test.ide.BlacklistedClassesHandler;
import org.netbeans.test.ide.BlacklistedClassesHandlerSingleton;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class GeneralSanityTest extends NbTestCase {

    public GeneralSanityTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTest(new GeneralSanityTest("testInitBlacklistedClassesHandler"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.createConfiguration(
                GeneralSanityTest.class
            ).gui(true).clusters(".*").enableModules(".*").
            honorAutoloadEager(true).
            addTest(
                "testBlacklistedClassesHandler",
                "testOrgOpenideOptionsIsDisabledAutoload",
                "testInstalledPlugins"
            )
        ));
        return s;
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
    
    public void testInstalledPlugins() {
        TreeSet<ModuleInfo> idePlugins = new TreeSet<ModuleInfo>(new Comparator<ModuleInfo>() {

            public int compare(ModuleInfo m1, ModuleInfo m2) {
                String s = m1.getLocalizedAttribute("OpenIDE-Module-Display-Category") + " " + m1.getDisplayName();
                return s.compareToIgnoreCase(m2.getLocalizedAttribute("OpenIDE-Module-Display-Category") + " " + m2.getDisplayName());
            }
            
        });
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            assertTrue(m instanceof Module);
            Module mm = (Module)m;
            if ("false".equals(m.getAttribute("AutoUpdate-Show-In-Client"))) {
                continue;
            }
            if (mm.isAutoload() || mm.isEager() || mm.isFixed()) {
                continue;
            }
            idePlugins.add(m);
        }

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
                    "the golden file in ide.kit/qa-functional/data/permanentUI/plugins/installed-plugins.txt.\n" +
                    Utilities.readFileToString(diffFile);

            assertFile(message, getPluginsGoldenFile() , idePluginsLogFile, diffFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void printPlugins(PrintStream out, Set<ModuleInfo> plugins) {

        String output = "||Codebase||Display name||Category";
        out.println(output);

        for (ModuleInfo m : plugins) {
            output = "";
            output += "|" + m.getCodeNameBase();
            output += "|" + m.getDisplayName();
            output += "|" + m.getLocalizedAttribute("OpenIDE-Module-Display-Category");
            out.println(output);

        }
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

}
