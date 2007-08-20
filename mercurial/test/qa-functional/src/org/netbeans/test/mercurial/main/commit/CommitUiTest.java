/*
 * CommitUiTest.java
 *
 * Created on 15 May 2006, 16:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.utils.RepositoryMaintenance;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitUiTest extends JellyTestCase{
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    
    String os_name;
    
    /** Creates a new instance of CheckoutUITest */
    public CommitUiTest(String name) {
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
        suite.addTest(new CommitUiTest("testInvokeCloseCommit"));
        return suite;
    }
    
    public void testInvokeCloseCommit() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }
        
        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        } finally {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
        }
        
        try {
            TestKit.closeProject(PROJECT_NAME);
            
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            TestKit.createNewElements(PROJECT_NAME, "xx", "NewClass");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass2");
            TestKit.createNewElement(PROJECT_NAME, "xx", "NewClass3");
            Node packNode = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            CommitOperator co = CommitOperator.invoke(packNode);
            
            co.selectCommitAction("NewClass.java", "Commit");
            co.selectCommitAction("NewClass.java", "Commit");
            co.selectCommitAction("NewClass.java", "Exclude from Commit");
            co.selectCommitAction(2, "Commit");
            co.selectCommitAction(2, "Commit");
            co.selectCommitAction(2, "Exclude from Commit");
            
            JTableOperator table = co.tabFiles();
            TableModel model = table.getModel();
            String[] expected = {"NewClass.java", "NewClass2.java",  "NewClass3.java"};
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Commit table doesn't contain all files!!!", expected.length, result);
            
            co.verify();
            co.cancel();
            //TestKit.removeAllData(PROJECT_NAME);
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);    
        }
    }
}
