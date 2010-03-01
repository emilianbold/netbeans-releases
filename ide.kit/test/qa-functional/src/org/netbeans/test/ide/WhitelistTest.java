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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.Test;
//import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 * Whitelist test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * To run this test do the following:
 * 1. execute test/whitelist/prepare.bat to prepare LimeWare project
 * 2. execute test/whitelist/test.bat to do the measurement
 * 3. execute test/whitelist/unprepare.bat to restore the environment
 *
 * @author mrkam@netbeans.org
 */
public class WhitelistTest extends JellyTestCase {

    private static int stage;

    private static boolean initBlacklistedClassesHandler() {        
        String whitelistFN = new WhitelistTest("Dummy").getDataDir()
                + File.separator + "whitelist_" + stage + ".txt";
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();
        
        System.out.println("BlacklistedClassesHandler will be initialized with " + whitelistFN);
        if (bcHandler.initSingleton(null, whitelistFN, false)) {
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
            System.setProperty("netbeans.warmup.skip", "true");
            System.out.println("Warmup disabled");
            return true;
        } else {
            return false;
        }
    }
    
    public WhitelistTest(String name) {
        super(name);
    }
    
    public static Test suite() throws URISyntaxException {
       URL u = WhitelistTest.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(u.toURI());
        while (f != null) {
            File hg = new File(f, ".hg");
            if (hg.isDirectory()) {
                System.setProperty("versioning.unversionedFolders", f.getPath());
                System.err.println("ignoring Hg folder: " + f);
                break;
            }
            f = f.getParentFile();
        }
        stage = Integer.getInteger("test.whitelist.stage", 1);
        
        initBlacklistedClassesHandler();
        
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            WhitelistTest.class
        ).clusters(".*").honorAutoloadEager(true).
        enableModules(".*").reuseUserDir(stage > 1);
        
        conf = conf.addTest("testWhitelist" + stage);
        
        return NbModuleSuite.create(conf);
    }

    public void testWhitelist1() throws Exception {
        stage = 1;
        Thread.sleep(3000);
        testWhitelist();
    }

    public void testWhitelist2() throws Exception {
        stage = 2;
        try {
            Thread.sleep(3000);
            testWhitelist();
        } finally {
            openLime6Project();
        }
    }

    public void testWhitelist3() throws Exception {
        stage = 3;
        long start = System.currentTimeMillis();
        System.out.println("TRACE 0 0");

        OpenProjects.getDefault().openProjects().get();

        System.out.println("TRACE 1 " + (System.currentTimeMillis() - start));

        Thread.sleep(1000);

        waitParsingFinished();

        System.out.println("TRACE 2 " + (System.currentTimeMillis() - start));

        testWhitelist();
    }

    public void testWhitelist() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        bcHandler.saveWhiteList(getLog("loadedClasses_" + stage + ".txt"));
        try {
            bcHandler.listViolations(getLog("whitelist_violators_" + stage + ".txt"), false);
            bcHandler.listViolations(getLog("report_" + stage + ".txt"), false, true);

            int allowed = Integer.getInteger("allowed.violations", 0);
            int number = bcHandler.getNumberOfViolations();
            String txt = null;
            if (number > 0) {
                txt = bcHandler.reportViolations(getLog("violations_" + stage + ".xml"));
            }
            if (number > allowed) {
                fail(
                    "Too many violations. Allowed only " + allowed + " but was: " + number + ":\n" +
                    txt
                );
            }
        } finally {
            bcHandler.unregister();
        }
    }

    public void openProject(String projectPath) throws Exception {
        File path=new File(getDataDir()+"/../../../../test/qa-functional/data").getCanonicalFile();
        File projectsDir = new File(path, projectPath);
        Object prj = ProjectSupport.openProject(projectsDir);
        assertNotNull(prj);
        waitParsingFinished();
    }

    public void openLime6Project() throws Exception {
        openProject("lime6");
    }

    private static void waitParsingFinished() throws Exception {
        Project[] arr = OpenProjects.getDefault().openProjects().get();
        assertEquals("One project is open", 1, arr.length);
        assertEquals("project dir is OK", "lime6", arr[0].getProjectDirectory().getNameExt());

        class R implements Runnable {
            boolean done;
            public void run() {
                if (done) {
                    return;
                }
                if (EventQueue.isDispatchThread()) {
                    done = true;
                 //   ScanDialog.runWhenScanFinished(this, "Test waits scanning finished");
                } else {
                    try {
                        EventQueue.invokeAndWait(this);
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }
        R run = new R();

        run.run();


        Class<?> taskClass = Class.forName("org.netbeans.modules.tasklist.impl.TaskManagerImpl");
        Method getter = taskClass.getDeclaredMethod("getInstance");
        Object taskManager = getter.invoke(null);
        Method working = taskClass.getDeclaredMethod("isWorking");
        working.setAccessible(true);
        if (Boolean.TRUE.equals(working.invoke(taskManager))) {
            Method waiter = taskClass.getDeclaredMethod("waitFinished");
            waiter.setAccessible(true);
            waiter.invoke(taskManager);
        }
    }

}
