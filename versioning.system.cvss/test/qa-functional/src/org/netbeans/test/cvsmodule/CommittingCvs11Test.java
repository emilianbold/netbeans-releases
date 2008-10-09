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

/*
 * CommittingCvs11Test.java
 *
 * Created on 08 March 2006, 11:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CommitOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
/**
 *
 * @author peter
 */
public class CommittingCvs11Test extends JellyTestCase {

    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    static File cacheFolder;
    String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;

    /**
     * Creates a new instance of CommittingCvs11Test
     */
    public CommittingCvs11Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
        try {
            TestKit.extractProtocol(getDataDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CommittingCvs11Test.class).addTest(
                     "testCheckOutProject",
                     "testCommitModified",
                     "removeAllData"
                )
                .enableModules(".*")
                .clusters(".*")
        );
     }

    public void testCheckOutProject() throws Exception {
        PROTOCOL_FOLDER = "protocol";
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        TestKit.closeProject(projectName);
        OutputOperator.invoke();
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //crso.setPassword("");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");   
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        //System.out.println(sessionCVSroot);
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
              
        //second step of checkoutwizard
        //2nd step of CheckOutWizard
        
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        cacheFolder = new File(work, projectName + File.separator + "src" + File.separator + "forimport" + File.separator + "CVS" + File.separator + "RevisionCache");
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        try {
            Thread.sleep(1000);
            in.close();
        } catch (IOException e) {
            //
        }
        moduleCheck.setModule("ForImport");        
        moduleCheck.setLocalFolder(work.getAbsolutePath()); // NOI18N
        
        //Pseudo CVS server for finishing check out wizard
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_finish_2.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        //cvss.ignoreProbe();
        
        //crso.setCVSRoot(CVSroot);
        //combo.setSelectedItem(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        
        //OutputOperator oo = OutputOperator.invoke();
        //System.out.println(CVSroot);
        
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot);
        oto.waitText("Checking out finished");
        cvss.stop();
        try {
            Thread.sleep(1000);
            in.close();
        } catch (IOException e) {
            //
        }
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
//        TestKit.waitForQueueEmpty();
//        ProjectSupport.waitScanFinished();
        
        //create new elements for testing
        TestKit.createNewElementsCommitCvs11(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }

    public void testCommitModified() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3, cvss4;
        InputStream in, in2, in3, in4;
        CommitOperator co;
        String CVSroot, color;
        JTableOperator table;
        OutputOperator oo;
        OutputTabOperator oto;
        VersioningOperator vo;
        String[] expected;
        String[] actual;
        String allCVSRoots;
        org.openide.nodes.Node nodeIDE;
        PROTOCOL_FOLDER = "protocol" + File.separator + "cvs_committing_cvs11";
        
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        
        Node packNode = new Node(new SourcePackagesNode("ForImport"), "xx");
        co = CommitOperator.invoke(packNode);
        Thread.sleep(1000);
        table = co.tabFiles();
        TableModel model = table.getModel();
        
        expected = new String[] {"NewClass.java", "NewClass2.java", "NewClass3.java", "NewClass4.java"};   
        actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 4, result);
        co.setCommitMessage("Initial commit message");
        
        //oo = OutputOperator.invoke();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        allCVSRoots = cvss.getCvsRoot() + ",";

        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_add_4.in");     
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        allCVSRoots = allCVSRoots + cvss2.getCvsRoot() + ",";
        
        in3 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_commit_4.in");
        cvss3 = new PseudoCvsServer(in3);
        new Thread(cvss3).start();
        allCVSRoots = allCVSRoots + cvss3.getCvsRoot()+ ",";
        
        in4 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_final.in");
        cvss4 = new PseudoCvsServer(in4);
        new Thread(cvss4).start();
        allCVSRoots = allCVSRoots + cvss4.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", allCVSRoots);
        co.commit();
        oto.waitText("Committing");
        oto.waitText("finished");
               
        cvss.stop();
        cvss2.stop();
        cvss3.stop();
        cvss4.stop();
        //modify files
        Node node = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("NewClass.java");
        eo.deleteLine(2);
        eo.insert(" a", 3, 4);
        eo.save();
        node = new Node(new SourcePackagesNode(projectName), "xx|NewClass2.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass2.java");
        eo.deleteLine(2);
        eo.insert(" a", 3, 4);
        eo.save();
        node = new Node(new SourcePackagesNode(projectName), "xx|NewClass3.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass3.java");
        eo.deleteLine(2);
        eo.insert(" a", 3, 4);
        eo.save();
        node = new Node(new SourcePackagesNode(projectName), "xx|NewClass4.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass4.java");
        eo.deleteLine(2);
        eo.insert(" a", 3, 4);
        eo.save();
        //commit changes
        
        //
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_commit_4_modified_show_changes.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        packNode.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        oto.waitText("Refreshing");
        oto.waitText("finished");
        cvss.stop();
        
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        co = CommitOperator.invoke(packNode);
        //
        Thread.sleep(1000);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_commit_4_modified_wrong.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();       
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        co.commit();
               
        oto.waitText("Committing");
        oto.waitText("finished");
        cvss.stop();
        cvss2.stop();

        //
        Node nodeClass = new Node(new SourcePackagesNode("ForImport"), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());

        nodeClass = new Node(new SourcePackagesNode("ForImport"), "xx|NewClass2.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());

        nodeClass = new Node(new SourcePackagesNode("ForImport"), "xx|NewClass3.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node!!!", TestKit.MODIFIED_COLOR, color);

        nodeClass = new Node(new SourcePackagesNode("ForImport"), "xx|NewClass4.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());

        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }

    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
