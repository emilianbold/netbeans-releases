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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.jconsole;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.JButton;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Select created Anagram Game managed with JMX project.
 * Then, run/debug project with monitoring and management.
 */
public class J2SEProject extends JConsoleTestCase {

    private static String ORIGINAL_TMP_FILE;
    static {
        //We need it to help tools.jar API to findout the local connector
        //This is an horrible hack! But no way to make it work without /var/tmp/ tmp file
        //Flushing env
        java.util.Enumeration e = System.getProperties().keys();
        java.util.Enumeration e2 = System.getProperties().elements();
        while (e.hasMoreElements()) {
            System.out.println(e.nextElement() + "=" + e2.nextElement());
        }
        String tmpFile = System.getProperty("java.io.tmpdir");

        if (tmpFile == null) {
            //This is for Windows platform, hoping it is set.
            tmpFile = System.getProperty("Env-TMP");
        }

        if (tmpFile == null) {
            if (!System.getProperty("os.name").startsWith("Win")) {
                tmpFile = "/var/tmp";
                //else
                //We can't find the tmp dir. The test must fail
            }
        }

        ORIGINAL_TMP_FILE = tmpFile == null ? null : tmpFile + File.separator;

        System.out.println("TMP FILE : " + ORIGINAL_TMP_FILE);
    }

    /** Creates a new instance of BundleKeys */
    public J2SEProject(String name) {
        super(name);
    }

    public void testRunWithJConsole() {
        
        System.out.println("============  runWithJConsole  ============");
        
        assertTmpDir();
        String ctxt = preLocalConnection();
        selectNode(PROJECT_NAME_J2SE_PROJECT_INTEGRATION);
        doItLocal("Run Main Project With Monitoring and Management", "anagrams (run-management)");
        postLocalConnection(ctxt);
    }

    public void testDebugWithJConsole() {
        
        System.out.println("============  debugWithJConsole  ============");
        
        assertTmpDir();
        String ctxt = preLocalConnection();
        selectNode(PROJECT_NAME_J2SE_PROJECT_INTEGRATION);
        doItLocal("Debug Main Project With Monitoring and Management", "anagrams (debug-management)");
        postLocalConnection(ctxt);
    }

//    runWithRemoteManagement was disabled in NB 6.0, rev. a9229cc3351b
//    public void testRunWithRemoteManagement() {
//
//        System.out.println("============  runWithRemoteManagement  ============");
//
//        selectNode(PROJECT_NAME_J2SE_PROJECT_INTEGRATION);
//        doItRemote("Run Main Project with Remote Management...", "anagrams (run-management)");
//    }
//
//    debugWithRemoteManagement was disabled in NB 6.0, rev. a9229cc3351b
//    public void testDebugWithRemoteManagement() {
//
//        System.out.println("============  debugWithRemoteManagement  ============");
//
//        selectNode(PROJECT_NAME_J2SE_PROJECT_INTEGRATION);
//        doItRemote("Debug Main Project with Remote Management...", "anagrams (debug-management)");
//    }


    private void doItLocal(String action, String target) {
        OutputTabOperator oto;
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        // push "Open" toolbar button in "System" toolbar
        System.out.println("Starting " + action + "...");        
        mainWindow.getToolbarButton(mainWindow.getToolbar("Management"), 
                action).push();
        sleep(2000);
        
        checkOutputTabOperator(target, "Found manageable process, connecting JConsole to process...");
        oto = checkOutputTabOperator("anagrams (-connect-jconsole)", "jconsole  -interval=4");
        if (oto != null) terminateProcess(oto);
    }

    private void doItRemote(final String action, String target) {
        OutputTabOperator oto;
        //We must thread the call in order not to be locked by dialog
        Runnable r = new Runnable() {
            public void run() {
                JMenuBarOperator op = MainWindowOperator.getDefault().menuBar();
                op.pushMenu("Run|Remote Management|" + action);
            }
        };
        new Thread(r).start();

        //Control dialog
        DialogOperator dop = new DialogOperator();
        String title = "Remote Management Configuration";
        int maxToWait = 10;
        while (maxToWait > 0) {
            try {
                System.out.println("Waiting for DialogOperator " + title);
                dop.waitTitle(title);
                System.out.println("(OK) Displayed DialogOperator " + title);
                break;
            } catch (Exception e) {
                System.out.println("Remote DialogOperator " + title + " not yet displayed");
                System.out.println(e.toString());
                maxToWait--;
            }
        }

        Component[] comp = dop.getComponents();
        JButton b = findOkButton(comp[0]);
        JButtonOperator op = new JButtonOperator(b);
        op.clickMouse();
        //for(int i = 0; i < comp.length; i++) {
        //  System.out.println("Component name " + comp[i].getName());
        //}
        sleep(2000);
        
        checkOutputTabOperator(target, "Found manageable process, connecting JConsole to process...");
        oto = checkOutputTabOperator("anagrams (-connect-jconsole)", "jconsole  -interval=4");
        if (oto != null) terminateProcess(oto);
    }

    private static JButton findOkButton(Component root) {
        if (root instanceof Container) {
            Component[] components = ((Container) root).getComponents();
            for (int i = 0; i < components.length; i++) {
                JButton b = findOkButton(components[i]);
                if (b != null) {
                    return b;
                }
            }
        }
        if (root instanceof JButton) {
            JButton b = (JButton) root;
            String label = b.getText();
            System.out.println("Found button label : [" + b.getText() + "]");
            if (label.equals("OK")) {
                return b;
            }
        }

        return null;
    }

    private void assertTmpDir() {
        if (ORIGINAL_TMP_FILE == null) {
            throw new IllegalArgumentException("TMP DIR is not set, check env");
        }
    }

    private String preLocalConnection() {
        //Just in case we are not testing using XTest harness
        if (ORIGINAL_TMP_FILE == null) {
            return null;
        }
        String current = System.getProperty("createSampleProject");
        System.setProperty("runWithJConsole", ORIGINAL_TMP_FILE);
        return current;
    }

    private void postLocalConnection(String ctxt) {
        if (ctxt == null) {
            return;
        }
        System.setProperty("java.io.tmpdir", ctxt);
    }
}
