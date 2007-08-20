/*
 * CommitDataTest.java
 *
 * Created on 27 May 2006, 19:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.RepositoryMaintenance;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitDataTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
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
        suite.addTest(new CommitDataTest("testRecognizeMimeType"));
        return suite;
    }
    
    public void testCommitFile() throws Exception {
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
            TestKit.showStatusLabels();
            TestKit.closeProject(PROJECT_NAME);
            OutputOperator oo = OutputOperator.invoke();
            
            org.openide.nodes.Node nodeIDE;
            long start;
            long end;
            String color;
            String status;
            JTableOperator table;
            TableModel model;
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");
            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeFile.performPopupAction("Mercurial|Status");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            VersioningOperator vo = VersioningOperator.invoke();
            table = vo.tabFiles();
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            
            //invoke commit action but exlude the file from commit
            start = System.currentTimeMillis();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            CommitOperator cmo = CommitOperator.invoke(nodeFile);
            end = System.currentTimeMillis();
            //System.out.println("Duration of invoking Commit dialog: " + (end - start));
            //print message to log file.
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            cmo.selectCommitAction("NewClass.java", "Exclude from Commit");
            TimeoutExpiredException tee = null;
            assertFalse(cmo.btCommit().isEnabled());
            cmo.cancel();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            table = vo.tabFiles();
            Thread.sleep(1000);
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Expected file is missing.", "NewClass.java", table.getModel().getValueAt(0, 0).toString());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            cmo = CommitOperator.invoke(nodeFile);
            cmo.selectCommitAction("NewClass.java", "Commit");
            start = System.currentTimeMillis();
            cmo.commit();
            OutputTabOperator oto = new OutputTabOperator("Mercurial");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("INFO: End of Commit");
            end = System.currentTimeMillis();
            
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            //        TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {        
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
    public void testRecognizeMimeType() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.showStatusLabels();
            TestKit.closeProject(PROJECT_NAME);
            org.openide.nodes.Node nodeIDE;
            JTableOperator table;
            long start;
            long end;
            String color;
            String status;
            String[] expected = {"pp.bmp", "pp.dib", "pp.GIF", "pp.JFIF", "pp.JPE", "pp.JPEG", "pp.JPG", "pp.PNG", "pp.TIF", "pp.TIFF", "pp.zip", "text.txt", "test.jar"};
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            File work = TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            //create various types of files
            String src = getDataDir().getCanonicalPath() + File.separator + "files" + File.separator;
            String dest = work.getCanonicalPath() + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "javaapp" + File.separator;
            
            for (int i = 0; i < expected.length; i++) {
                TestKit.copyTo(src + expected[i], dest + expected[i]);
            }
            
            Node nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeSrc.performPopupAction("Mercurial|Status");
            new EventTool().waitNoEvent(10000);
            
            Node nodeTest;
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
                assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            }
            
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            String[] actual = new String[model.getRowCount()];;
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            
            OutputTabOperator oto = new OutputTabOperator("Mercurial");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            CommitOperator cmo = CommitOperator.invoke(nodeSrc);
            table = cmo.tabFiles();
            model = table.getModel();
            actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
                if (actual[i].endsWith(".txt")) {
                    assertEquals("Expected text file.", "Commit", model.getValueAt(i, 2).toString());
                    //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77046!!!");
                } else {
                    assertEquals("Expected text file.", "Commit", model.getValueAt(i, 2).toString());
                }
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            cmo.commit();
            for (int i = 0; i < expected.length; i++) {
                oto.waitText("hg add " + expected[i]);
                //oto.waitText(expected[i]);
            }
            oto.waitText("INFO: End of Commit");
            //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77060!!!");
            
            //files have been committed,
            //verify explorer node
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                stream.print(expected[i] + ": " + nodeIDE.getHtmlDisplayName());
                assertNull("Wrong status or color of node!!!", nodeIDE.getHtmlDisplayName());
            }
            //verify versioning view
            vo = VersioningOperator.invoke();
            TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            //TestKit.removeAllData(PROJECT_NAME);
            stream.flush();
            stream.close();
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
