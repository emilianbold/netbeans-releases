/*
 * DeleteTest.java
 *
 * Created on August 17, 2006, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.delete;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.mercurial.operators.RevertModificationsOperator;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.RepositoryMaintenance;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class DeleteTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    
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
            TestKit.closeProject(PROJECT_NAME);
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
            JButtonOperator btn = new JButtonOperator(dialog, "Yes");
            btn.push();
            
            Thread.sleep(1000);
            node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            node.performPopupAction("Mercurial|Status");

            VersioningOperator vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Locally Removed]", "Locally Removed", table.getValueAt(0, 1).toString());
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
            
            //revert changes
            
            node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            RevertModificationsOperator rmo = RevertModificationsOperator.invoke(node);
            rmo.revert();
            Thread.sleep(5000);
          
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
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.performPopupAction("Mercurial|Status");
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
            dialog.yes();
            
            Thread.sleep(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Locally Removed]", "Locally Removed", table.getValueAt(0, 1).toString());
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
            node = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
            CommitOperator cmo = CommitOperator.invoke(node);
            assertEquals("There should be \"Main.java\" file in Commit dialog!!!", cmo.tabFiles().getValueAt(0, 0), "Main.java");
            cmo.commit();
            OutputTabOperator oto = new OutputTabOperator("Mercurial");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("INFO: End of Commit");
            Thread.sleep(1000);
            
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
            assertNotNull("Deleted file should be visible!!!", e);
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
}
