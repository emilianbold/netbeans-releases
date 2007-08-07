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
package org.netbeans.test.cvsmodule;

/**
 *
 * @author novakm
 */

import java.io.File;
import java.io.InputStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.BranchOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.SwitchToBranchOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;


/**
 *
 * @author novakm
 */
public class BranchCreationAndSwitchTest extends JellyTestCase {
    
    /** Creates a new instance of BranchCreationAndSwitchningTest */
    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of testIgnoreUnignoreFile */
    public BranchCreationAndSwitchTest(String name) {
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
        suite.addTest(new BranchCreationAndSwitchTest("testCheckOutProject"));
        suite.addTest(new BranchCreationAndSwitchTest("testCreateBranchForProject"));
        suite.addTest(new BranchCreationAndSwitchTest("testSwitchProjectToBranch"));
        suite.addTest(new BranchCreationAndSwitchTest("removeAllData"));
        return suite;
    }
    
    public void testCheckOutProject() throws Exception {
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
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
        
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
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = oo.getOutputTab(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        ProjectSupport.waitScanFinished();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCreateBranchForProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        PseudoCvsServer cvss;
        InputStream in;
        OutputTabOperator oto;
        String CVSroot;
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "cvs_60_create_branch.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        BranchOperator bo = BranchOperator.invoke(rootNode);
        bo.setBranchName("MyNewBranch");
        bo.checkSwitchToThisBranchAftewards(false);
        bo.checkTagBeforeBranching(false);
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        bo.branch();
        Thread.sleep(1000);
        oto.waitText("Branch");
        oto.waitText("ForImport");
        oto.waitText("finished");
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    public void testSwitchProjectToBranch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        PseudoCvsServer cvss;
        InputStream in;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String cvsRoot;
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "cvs_60_switch_branch.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvsRoot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvsRoot);
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        SwitchToBranchOperator sbo = SwitchToBranchOperator.invoke(rootNode);
        sbo.switchToBranch();
        sbo.setBranch("MyNewBranch");
        sbo.pushSwitch();
        Thread.sleep(1000);
        oto.waitText(" to Branch finished");
        cvss.stop();
        //        test branch annotation
        TestKit.showStatusLabels();
        Node nodeFile = new Node(new SourcePackagesNode(projectName), "ForImport" + "|Main.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        boolean comparison = status.equals("[; MyNewBranch]");
        assertEquals("Wrong annotation of node!!!", true, comparison);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}