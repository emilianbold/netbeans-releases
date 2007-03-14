/*
 * CopyTest.java
 *
 * Created on June 6, 2006, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SwitchOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class CopyTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator; 
    
    String os_name;
    
    /** Creates a new instance of CopyTest */
    public CopyTest(String name) {
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
        suite.addTest(new CopyTest("testCreateNewCopySwitch"));
        suite.addTest(new CopyTest("testCreateNewCopy"));
        return suite;
    }
    
    public void testCreateNewCopySwitch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        try {
            TestKit.closeProject(PROJECT_NAME);

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();       

            //create repository... 
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            //RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
//            oto.clear();            
            //open project
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();

            ProjectSupport.waitScanFinished();
            new QueueTool().waitEmpty(1000);
            ProjectSupport.waitScanFinished();

            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            CopyToOperator cto = CopyToOperator.invoke(projNode);
            cto.setRepositoryFolder("branches/release01/" + PROJECT_NAME);
            cto.setCopyPurpose("New branch for project.");
            cto.checkSwitchToCopy(true);
            cto.copy();
            Thread.sleep(2000);
            oto.waitText("Copy");
            oto.waitText("finished.");

            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", "[ release01]", status);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", "[ release01]", status);
            //to do 
            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
    
    public void testCreateNewCopy() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        try {
            TestKit.closeProject(PROJECT_NAME);

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();       

            //create repository... 
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            //RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            //open project
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
//            oto.clear();            
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();

            ProjectSupport.waitScanFinished();
            new QueueTool().waitEmpty(1000);
            ProjectSupport.waitScanFinished();

            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            CopyToOperator cto = CopyToOperator.invoke(projNode);
            cto.setRepositoryFolder("branches/release01");
            cto.setCopyPurpose("New branch for project.");
            cto.checkSwitchToCopy(false);
            cto.copy();
            oto.waitText("Copy");
            oto.waitText("finished.");

            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);
            //to do 

            //switch to branch
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            SwitchOperator so = SwitchOperator.invoke(projNode);
            so.setRepositoryFolder("branches/release01/" + PROJECT_NAME);
            so.switchBt();
            oto.waitText("Switch");
            oto.waitText("finished.");

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", "[ release01]", status);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", "[ release01]", status);

            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            so = SwitchOperator.invoke(projNode);
            so.setRepositoryFolder("trunk/" + PROJECT_NAME);
            so.switchBt();
            oto.waitText("Switch");
            oto.waitText("finished.");
            Thread.sleep(2000);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|Main.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong annotation of node!!!", TestKit.UPTODATE_STATUS, status);

            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
