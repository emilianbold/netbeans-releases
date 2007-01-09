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

import java.io.File;
import java.io.InputStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.modules.javacvs.BranchOperator;
import org.netbeans.jellytools.modules.javacvs.BrowseTagsOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.MergeChangesFromBranchOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.ProxyConfigurationOperator;
import org.netbeans.jellytools.modules.javacvs.SwitchToBranchOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
/**
 *
 * @author peter
 */
public class BranchTest extends JellyTestCase {
    
    String os_name;
    static String sessionCVSroot;
    boolean unix = false;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of BranchTest */
    public BranchTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BranchTest("testCheckOutProject"));
        suite.addTest(new BranchTest("testBranchDialogUI"));
        suite.addTest(new BranchTest("testSwitchToBranchDialogUI"));    
        suite.addTest(new BranchTest("testMergeChangesFromBranchDialogUI"));
        suite.addTest(new BranchTest("testOnNonVersioned"));
        suite.addTest(new BranchTest("removeAllData"));
        //debug
        //suite.addTest(new BranchTest("testOnNonVersioned"));
        return suite;
    }
    
    public void testCheckOutProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        TestKit.closeProject(projectName);
        String CVSroot;
        OutputOperator oo = OutputOperator.invoke();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //crso.setPassword("");
        ProxyConfigurationOperator pco = crso.proxyConfiguration();
        pco.noProxyDirectConnection();
        pco.ok();
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");   
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
              
        try {
           JProgressBarOperator progress = new JProgressBarOperator(crso);
           JButtonOperator btnStop = new JButtonOperator(crso);
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        //second step of checkoutwizard
        //2nd step of CheckOutWizard
        
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        moduleCheck.setModule(projectName);        
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
        
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        Thread.sleep(1000);
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        //create new elements for testing
        TestKit.createNewElements(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testBranchDialogUI() throws Exception {
        
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        PseudoCvsServer cvss;
        InputStream in;
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "browse_tags_branches.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        //invoke Branch dialog on file node
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        
        BranchOperator bo = BranchOperator.invoke(node);
        BrowseTagsOperator browseTags = bo.browse();
        
        //Head node
        browseTags.selectPath("Head");
        //Tags node
        browseTags.selectPath("Tags");
        //Branches node
        browseTags.selectPath("Branches");
        //
        browseTags.selectPath("Branches|MyBranch");
        browseTags.selectPath("Tags|MyBranch_root");
        cvss.stop();
        //
        //Ok button
        try {
            JButtonOperator btnBranch = new JButtonOperator(browseTags, "OK");
            JButtonOperator btnHelp = new JButtonOperator(browseTags, "Help");
            JButtonOperator btnCancel = new JButtonOperator(browseTags, "Cancel");
            btnCancel.push();
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        try {
            JTextFieldOperator tf1 = new JTextFieldOperator(bo, 0);
            JTextFieldOperator tf2 = new JTextFieldOperator(bo, 1);
            JCheckBoxOperator cb1 = new JCheckBoxOperator(bo, "Tag Before Branching");
            JCheckBoxOperator cb2 = new JCheckBoxOperator(bo, "Switch to This Branch Afterwards");
            JButtonOperator btnBranch = new JButtonOperator(bo, "Branch");
            JButtonOperator btnHelp = new JButtonOperator(bo, "Help");
            JButtonOperator btnCancel = new JButtonOperator(bo, "Cancel");
            
        } catch (TimeoutExpiredException ex) {
            throw ex;
        } 
        //
        bo.checkTagBeforeBranching(false);
        assertFalse(bo.txtTagName().isEnabled());
        //
        bo.checkTagBeforeBranching(true);
        assertTrue(bo.txtTagName().isEnabled());
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        bo.cancel();
    }
    
    public void testSwitchToBranchDialogUI() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        SwitchToBranchOperator sbo = SwitchToBranchOperator.invoke(node);
        JRadioButtonOperator trunkRb = new JRadioButtonOperator(sbo, "Switch to Trunk");
        JRadioButtonOperator branchRb = new JRadioButtonOperator(sbo, "Switch to Branch");
        sbo.switchToBranch();
        
        PseudoCvsServer cvss;
        InputStream in;
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "browse_tags_branches.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        //invoke Branch dialog on file node
        
        BrowseTagsOperator browseTags = sbo.browse();
        //Head node
        browseTags.selectPath("Head");
        //Tags node
        browseTags.selectPath("Tags");
        //Branches node
        browseTags.selectPath("Branches");
        //
        browseTags.selectPath("Branches|MyBranch");
        browseTags.selectPath("Tags|MyBranch_root");
        cvss.stop();
        //
        //Ok button
        TimeoutExpiredException tee = null;
        try {
            JButtonOperator btnOk = new JButtonOperator(browseTags, "OK");
            JButtonOperator btnHelp = new JButtonOperator(browseTags, "Help");
            JButtonOperator btnCancel = new JButtonOperator(browseTags, "Cancel");
            btnCancel.push();
        } catch(Exception e) {
            if (e instanceof TimeoutExpiredException) {
                tee = (TimeoutExpiredException) e;
            } else {
                throw e;
            }
        }
        assertNull("All components should be available, but some of them were not!", tee);
        
        tee = null;
        try {
            JTextFieldOperator tf1 = new JTextFieldOperator(sbo, 0);
            //tf1.getFocus();
            
            JButtonOperator btnBranch = new JButtonOperator(sbo, "Switch");
            JButtonOperator btnHelp = new JButtonOperator(sbo, "Help");
            JButtonOperator btnCancel = new JButtonOperator(sbo, "Cancel");
        } catch (Exception e) {
            if (e instanceof TimeoutExpiredException) {
                tee = (TimeoutExpiredException) e;
            } else {
                throw e;
            }
        }        
        assertNull("All components should be available, but some of them were not!", tee);
        
        //check functionality of radiobutton selection 
        sbo.switchToTrunk();
        assertEquals(false, sbo.btBrowse().isEnabled());
        sbo.switchToBranch();
        assertEquals(true, sbo.btBrowse().isEnabled());
        sbo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testMergeChangesFromBranchDialogUI() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        MergeChangesFromBranchOperator mcbo = MergeChangesFromBranchOperator.invoke(node);
        mcbo.mergeFromBranch();
        
        //browse 1.
        PseudoCvsServer cvss;
        InputStream in;
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "browse_tags_branches.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        
        BrowseTagsOperator browseTags = mcbo.browseMergeFromBranch();
        //Head node
        browseTags.selectPath("Head");
        //Tags node
        browseTags.selectPath("Tags");
        //Branches node
        browseTags.selectPath("Branches");
        browseTags.selectPath("Branches|MyBranch");
        browseTags.selectPath("Tags|MyBranch_root");
        cvss.stop();
        try {
            JButtonOperator btnOk = new JButtonOperator(browseTags, "OK");
            JButtonOperator btnHelp = new JButtonOperator(browseTags, "Help");
            JButtonOperator btnCancel = new JButtonOperator(browseTags, "Cancel");
            btnCancel.push();
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        
        //browse 2.
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "browse_tags_branches.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        
        mcbo.checkMergeAfterTag(true);
        browseTags = mcbo.browseMergeAfterTag();
        //Head node
        browseTags.selectPath("Head");
        //Tags node
        browseTags.selectPath("Tags");
        //Branches node
        browseTags.selectPath("Branches");
        browseTags.selectPath("Branches|MyBranch");
        browseTags.selectPath("Tags|MyBranch_root");
        cvss.stop();
        try {
            JButtonOperator btnOk = new JButtonOperator(browseTags, "OK");
            JButtonOperator btnHelp = new JButtonOperator(browseTags, "Help");
            JButtonOperator btnCancel = new JButtonOperator(browseTags, "Cancel");
            btnCancel.push();
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        
        //browse 3.
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "browse_tags_branches.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        
        mcbo.checkTagAfterMerge(true);
        browseTags = mcbo.browseTagAfterMerge();
        //Head node
        browseTags.selectPath("Head");
        //Tags node
        browseTags.selectPath("Tags");
        //Branches node
        browseTags.selectPath("Branches");
        browseTags.selectPath("Branches|MyBranch");
        browseTags.selectPath("Tags|MyBranch_root");
        cvss.stop();
        try {
            JButtonOperator btnOk = new JButtonOperator(browseTags, "OK");
            JButtonOperator btnHelp = new JButtonOperator(browseTags, "Help");
            JButtonOperator btnCancel = new JButtonOperator(browseTags, "Cancel");
            btnCancel.push();
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        try {
            JTextFieldOperator tf1 = new JTextFieldOperator(mcbo, 0);
            JTextFieldOperator tf2 = new JTextFieldOperator(mcbo, 1);
            JTextFieldOperator tf3 = new JTextFieldOperator(mcbo, 2);
            JButtonOperator btnMerge = new JButtonOperator(mcbo, "Merge");
            JButtonOperator btnHelp = new JButtonOperator(mcbo, "Help");
            JButtonOperator btnCancel = new JButtonOperator(mcbo, "Cancel");
        } catch (TimeoutExpiredException ex) {
            throw ex;
        }  
        
        //functionality of button
        //for radiobutton
        mcbo.mergeFromTrunk();
        assertFalse(mcbo.txtMergeFromBranch().isEnabled());
        assertFalse(mcbo.btBrowseMergeFromBranch().isEnabled());
        //
        mcbo.mergeFromBranch();
        assertTrue(mcbo.txtMergeFromBranch().isEnabled());
        assertTrue(mcbo.btBrowseMergeFromBranch().isEnabled());
        
        //
        mcbo.checkMergeAfterTag(false);
        assertFalse(mcbo.txtMergeAfterTag().isEnabled());
        assertFalse(mcbo.btBrowseMergeAfterTag().isEnabled());
        //
        mcbo.checkMergeAfterTag(true);
        assertTrue(mcbo.txtMergeAfterTag().isEnabled());
        assertTrue(mcbo.btBrowseMergeAfterTag().isEnabled());
        
        //
        mcbo.checkTagAfterMerge(false);
        assertFalse(mcbo.txtTagAfterMerge().isEnabled());
        assertFalse(mcbo.btBrowseTagAfterMerge().isEnabled());
        //
        mcbo.checkTagAfterMerge(true);
        assertTrue(mcbo.txtTagAfterMerge().isEnabled());
        assertTrue(mcbo.btBrowseTagAfterMerge().isEnabled());
        mcbo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testOnNonVersioned() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        //delete fake versioning of file
        //TestKit.unversionProject(file, projNonName);
        
        TimeoutExpiredException tee = null;
        try {
            Node node = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
            BranchOperator bo = BranchOperator.invoke(node);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        //
        tee = null;
        try {
            Node node = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
            SwitchToBranchOperator sbo = SwitchToBranchOperator.invoke(node);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e; 
        }    
        assertNotNull(tee);
        //
        tee = null; 
        try {
            Node node = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
            MergeChangesFromBranchOperator mcbo = MergeChangesFromBranchOperator.invoke(node);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }    
        assertNotNull(tee);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}


