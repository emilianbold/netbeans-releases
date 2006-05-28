/*
 * CommitDataTest.java
 *
 * Created on 27 May 2006, 19:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.commit;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitDataTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    String os_name;
    
    /** Creates a new instance of CommitDataTest */
    public CommitDataTest(String name) {
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
        suite.addTest(new CommitDataTest("testCommitFile"));      
        suite.addTest(new CommitDataTest("testCommitPackage"));      
        return suite;
    }
    
    public void testCommitFile() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        org.openide.nodes.Node nodeIDE;
        long start;
        long end;
        String color;
        String status;
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();       
        OutputTabOperator oto = new OutputTabOperator("SVN Output");
        oto.clear();
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        
        TestKit.createNewElement("JavaApp", "javaapp", "NewClass");
        Node nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
        
        //invoke commit action but exlude the file from commit
        start = System.currentTimeMillis();
        CommitOperator cmo = CommitOperator.invoke(nodeFile);
        end = System.currentTimeMillis();
        System.out.println("Duration of invoking Commit dialog: " + (end - start));
        cmo.selectCommitAction("NewClass.java", "Exclude from Commit");
        cmo.commit();
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
        
        oto.clear();
        cmo = CommitOperator.invoke(nodeFile);
        cmo.selectCommitAction("NewClass.java", "Add As Text");
        start = System.currentTimeMillis();
        cmo.commit();
        oto.waitText("Comitting... finished.");
        end = System.currentTimeMillis();
        
        nodeFile = new Node(new SourcePackagesNode("JavaApp"), "javaapp" + "|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
        System.out.println("Duration of committing file: " + (end - start));
        //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());
        
        TestKit.removeAllData("JavaApp");
    }
 
    public void testCommitPackage() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        org.openide.nodes.Node nodeIDE;
        long start;
        long end;
        String color;
        String status;
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();       
        OutputTabOperator oto = new OutputTabOperator("SVN Output");
        oto.clear();
        
        //create repository... 
        new File(TMP_PATH).mkdirs();
        new File(TMP_PATH + File.separator + WORK_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + WORK_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        rso.next();
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.setRepositoryFolder("trunk/JavaApp");
        wdso.setLocalFolder(TMP_PATH + File.separator + WORK_PATH);
        wdso.checkCheckoutContentOnly(false);
        wdso.finish();
        //open project
        oto.waitText("Checking out... finished.");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        ProjectSupport.waitScanFinished();
        Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
        
        TestKit.createNewPackage("JavaApp", "xx");
        Node nodePack = new Node(new SourcePackagesNode("JavaApp"), "xx");
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        //System.out.println("status" + status);
        assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
        
        //invoke commit action but exlude the file from commit
        start = System.currentTimeMillis();
        CommitOperator cmo = CommitOperator.invoke(nodePack);
        end = System.currentTimeMillis();
        System.out.println("Duration of invoking Commit dialog: " + (end - start));
        cmo.selectCommitAction("xx", "Exclude from Commit");
        cmo.commit();
        nodePack = new Node(new SourcePackagesNode("JavaApp"), "xx");
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
        
        oto.clear();
        cmo = CommitOperator.invoke(nodePack);
        cmo.selectCommitAction("xx", "Add Directory");
        start = System.currentTimeMillis();
        cmo.commit();
        oto.waitText("Comitting... finished.");
        end = System.currentTimeMillis();
        
        nodePack = new Node(new SourcePackagesNode("JavaApp"), "xx");
        nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
        System.out.println("Duration of committing file: " + (end - start));
        status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong status of node!!!", TestKit.UPTODATE_STATUS, status);
        
        TestKit.removeAllData("JavaApp");
    }
}
