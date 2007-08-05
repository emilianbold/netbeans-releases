/*
 * ExportDiffPatchTest.java
 *
 * Created on Piatok, 2006, september 15, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class ExportDiffPatchTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of ExportDiffPatchTest */
    public ExportDiffPatchTest(String name) {
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
        suite.addTest(new ExportDiffPatchTest("invokeExportDiffPatch"));
        return suite;
    }
    
    public void invokeExportDiffPatch() throws Exception {
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
            
            //modify, save file and invoke Diff
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Open");
            EditorOperator eo = new EditorOperator("Main.java");
            eo.deleteLine(2);
            eo.insert(" insert", 5, 1);
            eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
            eo.save();
            node.performPopupAction("Subversion|Show Changes");
            Thread.sleep(1000);
            vo = VersioningOperator.invoke();
            //Save action should change the file annotations
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.MODIFIED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.MODIFIED_STATUS, status);
            assertEquals("Wrong number of records in Versioning view!!!", 1, vo.tabFiles().getRowCount());
            
            new ProjectsTabOperator().tree().clearSelection();
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            node.performMenuActionNoBlock("Versioning|Subversion|Export Diff Patch...");
            Operator.setDefaultStringComparator(oldOperator);
            
            //node.select();
            
            nbdialog = new NbDialogOperator("Export");
            JButtonOperator btn = new JButtonOperator(nbdialog, "Export");
            JTextFieldOperator tf = new JTextFieldOperator(nbdialog, 0);
            String patchFile = "/tmp/patch" + System.currentTimeMillis() + ".patch";
            File file = new File(patchFile);
            tf.setText(file.getCanonicalFile().toString());
            oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            btn.push();
            oto.waitText("Diff Patch finished");
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            boolean generated = false;
            if (line != null) {
                generated = line.indexOf("# This patch file was generated by NetBeans IDE") != -1 ? true : false;
            }
            
            br.close();
            assertTrue("Diff Patch file is empty!", generated);
            System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
            stream.flush();
            stream.close();
            
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }    
    }
    
}
