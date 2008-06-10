/*
 * DeleteUpdateTest.java
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
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class DeleteUpdateTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    
    /** Creates a new instance of DeleteUpdateTest */
    public DeleteUpdateTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
   
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(DeleteUpdateTest.class).addTest("testDeleteUpdate").enableModules(".*").clusters(".*"));
    }
    
    public void testDeleteUpdate() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        try {
//            TestKit.closeProject(PROJECT_NAME);
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
//            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Mercurial|Status");
            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Safe Delete");
            dialog.ok();
            
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
            
            //update so the deleted file appears again
            String tabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            Thread.sleep(1000);
            node.performPopupActionNoBlock("Mercurial|Update...");
            NbDialogOperator ndo = new NbDialogOperator("Update - "+PROJECT_NAME);
            JButtonOperator jbo = new JButtonOperator(ndo, "Update");
            jbo.push();
            OutputTabOperator oto = new OutputTabOperator(tabName);
//            oto.waitText("INFO: End of Update");
            Thread.sleep(1000);
            
            e=null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("TimeoutExpiredException should not have been thrown. Updating deleted file should make it visible!!!", e);
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
        }    
    }
}
