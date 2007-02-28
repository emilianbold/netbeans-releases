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
import java.io.PrintStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.CommitOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
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
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    
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
        suite.addTest(new CommitDataTest("testRecognizeMimeType"));
        return suite;
    }
    
    public void testCommitFile() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.closeProject(PROJECT_NAME);
            org.openide.nodes.Node nodeIDE;
            long start;
            long end;
            String color;
            String status;
            JTableOperator table;
            TableModel model;
            VersioningOperator vo;
            
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
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.clear();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            //open project
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            
            ProjectSupport.waitScanFinished();
            new QueueTool().waitEmpty(1000);
            ProjectSupport.waitScanFinished();
            //Node projNode = new Node(new ProjectsTabOperator().tree(), "JavaApp");
            
            TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");
            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeFile.performPopupAction("Subversion|Show Changes");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            vo = VersioningOperator.invoke();
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
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.clear();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            cmo = CommitOperator.invoke(nodeFile);
            cmo.selectCommitAction("NewClass.java", "Add as Text");
            start = System.currentTimeMillis();
            cmo.commit();
            oto.waitText("Committing... finished.");
            end = System.currentTimeMillis();
            
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            //System.out.println("Duration of invoking Commit dialog: " + (end - start));
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
        } finally {        
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
    public void testCommitPackage() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.closeProject(PROJECT_NAME);
            org.openide.nodes.Node nodeIDE;
            JTableOperator table;
            long start;
            long end;
            String color;
            String status;
            VersioningOperator vo = VersioningOperator.invoke();
            
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
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.clear();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            //open project
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            
            ProjectSupport.waitScanFinished();
            new QueueTool().waitEmpty(1000);
            ProjectSupport.waitScanFinished();
            
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            
            TestKit.createNewPackage(PROJECT_NAME, "xx");
            Node nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            nodePack.performPopupAction("Subversion|Show Changes");
            nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            //System.out.println("status" + status);
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            table = vo.tabFiles();
            assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Expected folder is missing.", "xx", table.getModel().getValueAt(0, 0).toString());
            
            //invoke commit action but exlude the file from commit
            start = System.currentTimeMillis();
            nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            CommitOperator cmo = CommitOperator.invoke(nodePack);
            end = System.currentTimeMillis();
            //System.out.println("Duration of invoking Commit dialog: " + (end - start));
            //print log message
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            cmo.selectCommitAction("xx", "Exclude from Commit");
            assertFalse(cmo.btCommit().isEnabled());
            cmo.cancel();
            nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.clear();
            nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            cmo = CommitOperator.invoke(nodePack);
            cmo.selectCommitAction("xx", "Add Directory");
            start = System.currentTimeMillis();
            cmo.commit();
            oto.waitText("Committing... finished.");
            end = System.currentTimeMillis();
            
            nodePack = new Node(new SourcePackagesNode(PROJECT_NAME), "xx");
            nodeIDE = (org.openide.nodes.Node) nodePack.getOpenideNode();
            //System.out.println("Duration of committing file: " + (end - start));
            TestKit.printLogStream(stream, "Duration of committing folder: " + (end - start));
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong status of node!!!", TestKit.UPTODATE_STATUS, status);
            Thread.sleep(1000);
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
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
    public void testRecognizeMimeType() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.closeProject(PROJECT_NAME);
            org.openide.nodes.Node nodeIDE;
            JTableOperator table;
            long start;
            long end;
            String color;
            String status;
            String[] expected = {"pp.bmp", "pp.dib", "pp.GIF", "pp.JFIF", "pp.JPE", "pp.JPEG", "pp.JPG", "pp.PNG", "pp.TIF", "pp.TIFF", "pp.zip", "text.txt", "test.jar"};
            
            VersioningOperator vo = VersioningOperator.invoke();
            
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
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.clear();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();
            //open project
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            
            ProjectSupport.waitScanFinished();
            new QueueTool().waitEmpty(1000);
            ProjectSupport.waitScanFinished();
            
            //create various types of files
            String src = getDataDir().getCanonicalPath() + File.separator + "files" + File.separator;
            String dest = work.getCanonicalPath() + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "javaapp" + File.separator;
            
            for (int i = 0; i < expected.length; i++) {
                TestKit.copyTo(src + expected[i], dest + expected[i]);
            }
            
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            
            Node nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeSrc.performPopupAction("Subversion|Show Changes");
            oto.waitText("Refreshing... finished.");
            
            Node nodeTest;
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
                assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            }
            
            vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            String[] actual = new String[model.getRowCount()];;
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            
            oto = new OutputTabOperator("file:///tmp/repo");
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
                    assertEquals("Expected text file.", "Add as Text", model.getValueAt(i, 2).toString());
                    //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77046!!!");
                } else {
                    assertEquals("Expected text file.", "Add as Binary", model.getValueAt(i, 2).toString());
                }
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            cmo.commit();
            for (int i = 0; i < expected.length; i++) {
                oto.waitText("add -N");
                oto.waitText(expected[i]);
            }
            oto.waitText("Committing... finished.");
            //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77060!!!");
            
            //files have been committed,
            //verify explorer node
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
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
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
