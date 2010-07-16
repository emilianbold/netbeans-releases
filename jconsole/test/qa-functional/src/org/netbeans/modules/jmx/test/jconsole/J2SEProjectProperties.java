/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Select created Anagram Game managed with JMX project.
 * Then, run/debug project with monitoring and management.
 * 
 * Same test as J2SEProject but configuring JConsole via 
 * Project -> Properties.
 */
public class J2SEProjectProperties extends JConsoleTestCase {

    /** Creates a new instance of BundleKeys */
    public J2SEProjectProperties(String name) {
        super(name);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }

    public static NbTestSuite suite() {

        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new J2SEProjectProperties("runWithJConsole"));
        //        suite.addTest(new J2SEProjectProperties("debugWithJConsole"));
        //        suite.addTest(new J2SEProjectProperties("runWithRemoteManagement"));
        //        suite.addTest(new J2SEProjectProperties("debugWithRemoteManagement"));
        return suite;
    }

    public void runWithJConsole() {

        String rmiPort = "5000";
        
        System.out.println("============  runWithJConsole  ============");

        System.out.println("Invoke Project -> Properties");
        new ProjectsTabOperator().getProjectRootNode(
                PROJECT_NAME_J2SE_PROJECT_INTEGRATION).properties();
        NbDialogOperator ndo = new NbDialogOperator(
                PROPERTIES_DIALOG_TITLE + " - " + PROJECT_NAME_J2SE_PROJECT_INTEGRATION);

        System.out.println("Select " + MONITORING_AND_MANAGEMENT + " category");
        new Node(new JTreeOperator(ndo), MONITORING_AND_MANAGEMENT).select();

        sleep(5000);

        // Enable RMI remote access
        setCheckBoxSelection(ENABLE_RMI_REMOTE_ACCESS_CHECK_BOX, ndo, true);
        
        // Set the RMI port
        setTextFieldContent(RMI_PORT_TEXT_FIELD, ndo, rmiPort);

        selectNode(PROJECT_NAME_J2SE_PROJECT_INTEGRATION);
        doItLocal("Run Main Project With Monitoring and Management", 
                "run-management", rmiPort);
    }

    public void debugWithJConsole() {

        System.out.println("============  debugWithJConsole  ============");
    }

    private void doItLocal(String action, String target, String rmiPort) {
        OutputTabOperator oto;
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        // push "Open" toolbar button in "System" toolbar
        System.out.println("Starting " + action + "...");
        mainWindow.getToolbarButton(mainWindow.getToolbar("Management"),
                action).push();
        sleep(2000);

        checkOutputTabOperator(target, "Found manageable process, connecting JConsole to process...");
        oto = checkOutputTabOperator("-connect-jconsole", "jconsole  -interval=4");
        if (oto != null) terminateProcess(oto);
    }
}
