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
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
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
    static Logger log;
    
    /** Creates a new instance of DeleteUpdateTest */
    public DeleteUpdateTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }
    
   
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(DeleteUpdateTest.class).addTest("testDeleteUpdate").enableModules(".*").clusters(".*"));
    }
    
    public void testDeleteUpdate() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Mercurial|Status");
            TestKit.waitText(mh);

            new EventTool().waitNoEvent(10000);

            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Delete");
//            NbDialogOperator dialog = new NbDialogOperator("Safe Delete");
            JButtonOperator btn1 = new JButtonOperator(dialog, "OK");
            
            btn1.push();
            
            new EventTool().waitNoEvent(1000);
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
            
            System.out.println("DEBUG: testDeleteUpdate - 1");
            //update so the deleted file appears again
            String tabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);
            new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp").select();
            new EventTool().waitNoEvent(1000);
            System.out.println("DEBUG: testDeleteUpdate - 2");
            //node.performPopupActionNoBlock("Mercurial|Update..."); // the popup menu was removed...

            mh = new MessageHandler("Updating");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.select();
            node.performMenuActionNoBlock("Team|Mercurial|Update...");
            NbDialogOperator dlgUpdate = new NbDialogOperator("Update Repository");
            JButtonOperator btnUpdate = new JButtonOperator(dlgUpdate, "Update");
            btnUpdate.push();

            TestKit.waitText(mh);

            new EventTool().waitNoEvent(3000);
            
            e=null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("TimeoutExpiredException should not have been thrown. Updating deleted file should make it visible!!!", e);
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        } 
    }
}
