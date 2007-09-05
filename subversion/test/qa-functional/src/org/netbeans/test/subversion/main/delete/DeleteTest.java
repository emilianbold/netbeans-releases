/*
 * DeleteTest.java
 *
 * Created on August 17, 2006, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.delete;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.RevertModificationsOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class DeleteTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;     
    
    /** Creates a new instance of DeleteTest */
    public DeleteTest(String name) {
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
        suite.addTest(new DeleteTest("testDeleteRevert"));
        suite.addTest(new DeleteTest("testDeleteCommit"));
        return suite;
    }
    
    public void testDeleteRevert() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        try {
            OutputOperator.invoke();
            TestKit.closeProject(PROJECT_NAME);
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
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
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
//            oto.clear();            
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            
            TestKit.waitForScanFinishedAndQueueEmpty();
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.performPopupAction("Subversion|Show Changes");
            oto.waitText("Refreshing... finished.");
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
            JButtonOperator btn = new JButtonOperator(dialog, "Yes");
            btn.push();
            
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1).toString());
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - file should appear in Versioning view!!!", e);
            
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("TimeoutExpiredException should have been thrown. Deleted file can't be visible!!!", e);
            
            //revert local changes
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            RevertModificationsOperator rmo = RevertModificationsOperator.invoke(node);
            rmo.rbLocalChanges().push();
            rmo.revert();
            oto.waitText("Modifications finished.");
            
            e = null;
            try {
                vo = VersioningOperator.invoke();
                table = vo.tabFiles();
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - file should disappear in Versioning view!!!", e);
            
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Reverted file should be visible!!!", e);
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
    public void testDeleteCommit() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        try {
            TestKit.closeProject(PROJECT_NAME);
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator vo = VersioningOperator.invoke();
            CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
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
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
//            oto.clear();            
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            
            TestKit.waitForScanFinishedAndQueueEmpty();
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.performPopupAction("Subversion|Show Changes");
            oto.waitText("Refreshing... finished.");
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
            dialog.yes();
            
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1).toString());
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - file should appear in Versioning view!!!", e);
            
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("TimeoutExpiredException should have been thrown. Deleted file can't be visible!!!", e);
            
            //commit deleted file
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            CommitOperator cmo = CommitOperator.invoke(node);
            assertEquals("There should be \"Main.java\" file in Commit dialog!!!", cmo.tabFiles().getValueAt(0, 0), "Main.java");
            cmo.commit();
            oto.waitText("finished.");
            
            e = null;
            try {
                vo = VersioningOperator.invoke();
                table = vo.tabFiles();
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Unexpected behavior - file should disappear in Versioning view!!!", e);
            
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("Deleteted file should be visible!!!", e);
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
}
