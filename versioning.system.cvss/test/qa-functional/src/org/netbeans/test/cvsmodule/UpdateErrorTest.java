/*
 * UpdateErrorTest.java
 *
 * Created on 25 August 2006, 16:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.InputStream;
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
import org.netbeans.jemmy.JemmyProperties;
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
public class UpdateErrorTest extends JellyTestCase {
    
    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    static File cacheFolder;
    String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /**
     * Creates a new instance of UpdateErrorTest
     */
    public UpdateErrorTest(String name) {
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
        suite.addTest(new UpdateErrorTest("testCheckOutProject"));
        suite.addTest(new UpdateErrorTest("testUpdate"));
        suite.addTest(new UpdateErrorTest("removeAllData"));
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
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testUpdate() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
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
        PROTOCOL_FOLDER = "protocol" + File.separator + "update_access_denied";
        
        vo = VersioningOperator.invoke();
        oo = OutputOperator.invoke();
        oto = oo.getOutputTab(sessionCVSroot);
        
        Node node = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.insert(" a", 3, 4);
        eo.save();      
        //
        oto = oo.getOutputTab(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_package.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        node = new Node(new SourcePackagesNode("ForImport"), "forimport");
        node.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        oto.waitText("Refreshing");
        oto.waitText("finished");
        cvss.stop();
        
        assertEquals("File should be listed in Versioning view", "Main.java", vo.tabFiles().getValueAt(0, 0).toString());
        
        node = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node!!!", TestKit.MODIFIED_COLOR, color);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "update_access_denied.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        node = ProjectsTabOperator.invoke().getProjectRootNode("ForImport");
        //oto = oo.getOutputTab(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        node.performPopupAction("CVS|Update");
        Thread.sleep(1000);
        cvss.stop();
        oto.waitText("Updating");
        oto.waitText("finished");
        
        NbDialogOperator dialog = new NbDialogOperator("Command");
        JButtonOperator btn = new JButtonOperator(dialog, "Ok");
        btn.push();
        
        Thread.sleep(1000);
        node = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node. Modified file can't be changed to up-to-date. See issue http://www.netbeans.org/issues/show_bug.cgi?id=83476!!!", TestKit.MODIFIED_COLOR, color);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
}
