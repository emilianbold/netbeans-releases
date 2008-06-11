/*
 * IgnoreTest.java
 *
 * Created on June 8, 2006, 9:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class IgnoreTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    
    /** Creates a new instance of IgnoreTest */
    public IgnoreTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
   
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(IgnoreTest.class).addTest("testIgnoreUnignoreFile" /*, "testFinalRemove" */).enableModules(".*").clusters(".*"));
    }
        
    public void testIgnoreUnignoreFile() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);  
        System.out.println("DEBUG: testIgnoreUnignoreFile - start");
        try {
            
            OutputOperator oo = OutputOperator.invoke();
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));

            TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            node.performPopupAction("Mercurial|Toggle Ignore");
            String outputTabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);
            OutputTabOperator oto = new OutputTabOperator(outputTabName);
//            oto.waitText("INFO: End of Ignore");
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be ignored!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be ignored!!!", TestKit.IGNORED_STATUS, status);
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            TimeoutExpiredException tee = null;
            //unignore file
            oto = new OutputTabOperator(outputTabName);
            oto.clear();
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            node.performPopupAction("Mercurial|Toggle Ignore");
//            oto.waitText("INFO: End of Unignore");
            Thread.sleep(1000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status);
            
            //verify content of Versioning view
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|NewClass");
            node.performPopupAction("Mercurial|Status");
            new EventTool().waitNoEvent(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            assertEquals("Versioning view should be empty", 1, model.getRowCount());
            assertEquals("File should be listed in Versioning view", "NewClass.java", model.getValueAt(0, 0).toString());
            
            stream.flush();
            stream.close();
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
//            TestKit.closeProject(PROJECT_NAME);
        }
        System.out.println("DEBUG: testIgnoreUnignoreFile - finish");
    }
    
    public void testFinalRemove() throws Exception {
        TestKit.finalRemove();
    }
}
