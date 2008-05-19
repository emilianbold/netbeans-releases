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
package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class InitializeTest extends JellyTestCase {

    public File projectPath;
    public PrintStream stream;
    String os_name;
    static File f;

    public InitializeTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new InitializeTest("testInitializeAndFirstCommit"));
        return suite;
    }

    public void testInitializeAndFirstCommit() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 15000);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }

        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 15000);
        } finally {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
        }

        try {
            TestKit.closeProject(TestKit.PROJECT_NAME);
            org.openide.nodes.Node nodeIDE;
            long start;
            long end;
            JTableOperator table;
            Node nodeFile;
            OutputOperator oo = OutputOperator.invoke();
            TestKit.showStatusLabels();
            f = TestKit.prepareProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, TestKit.PROJECT_NAME);
            String s = f.getAbsolutePath() + File.separator + TestKit.PROJECT_NAME;
            Thread.sleep(1000);
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performPopupActionNoBlock("Versioning|Initialize Mercurial Project");
            NbDialogOperator ndo = new NbDialogOperator("Confirm Mercurial Version");
            ndo.yes();
//            System.out.println(ndo);
            System.out.println(s);
            OutputTabOperator oto = new OutputTabOperator(s);
            oto.waitText("INFO: End of Initialize");
            Thread.sleep(1000);
            nodeFile.performPopupAction("Mercurial|Status");
            VersioningOperator vo = VersioningOperator.invoke();
            table = vo.tabFiles();
            assertEquals("Wrong row count of table.", 8, table.getRowCount());
            start = System.currentTimeMillis();
            CommitOperator cmo = CommitOperator.invoke(nodeFile);
            end = System.currentTimeMillis();
            System.out.println("Duration of invoking Commit dialog: " + (end - start));
            //print message to log file.
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            cmo.setCommitMessage("init");
            cmo.commit();
            oto.waitText("INFO: End of Commit");
            oto.close();
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();

        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            // do not remove it as following tests will work on the project
//            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
