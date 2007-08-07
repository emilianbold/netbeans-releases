/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.io.InputStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
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
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
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

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CommittingCvs11Test("testCheckOutProject"));
        suite.addTest(new CommittingCvs11Test("testCommitModified"));
        suite.addTest(new CommittingCvs11Test("removeAllData"));
        return suite;
    }

    public void testCheckOutProject() throws Exception {
        PROTOCOL_FOLDER = "protocol";
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        TestKit.closeProject(projectName);
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
        in.close();
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
        
        OutputOperator oo = OutputOperator.invoke();
        //System.out.println(CVSroot);
        
        OutputTabOperator oto = oo.getOutputTab(sessionCVSroot);
        oto.waitText("Checking out finished");
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        ProjectSupport.waitScanFinished();
        
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
        
        oo = OutputOperator.invoke();
        oto = oo.getOutputTab(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
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
