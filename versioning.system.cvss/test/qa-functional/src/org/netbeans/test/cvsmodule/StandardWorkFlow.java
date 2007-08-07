/*
 * StandardWorkFlow.java
 * 
 * Created on 26 October 2005, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.cvsmodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.BranchOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CommitOperator;
import org.netbeans.jellytools.modules.javacvs.DiffOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.SearchHistoryOperator;
import org.netbeans.jellytools.modules.javacvs.SwitchToBranchOperator;
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author peter
 */
public class StandardWorkFlow extends JellyTestCase {
    
    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /**
     * Creates a new instance of CheckOutWizardTest
     */
    public StandardWorkFlow(String name) {
        super(name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new StandardWorkFlow("testCheckOutProject"));
        suite.addTest(new StandardWorkFlow("testIgnoreUnignoreFile"));
        suite.addTest(new StandardWorkFlow("testIgnoreUnignoreGuiForm"));
        suite.addTest(new StandardWorkFlow("testCommit"));
        suite.addTest(new StandardWorkFlow("testCreateBranchForProject"));
        suite.addTest(new StandardWorkFlow("testSwitchProjectToBranch"));
        suite.addTest(new StandardWorkFlow("testDiffFile"));
        suite.addTest(new StandardWorkFlow("testExportDiffPatch"));
        suite.addTest(new StandardWorkFlow("testResolveConflicts"));
        suite.addTest(new StandardWorkFlow("testRevertModifications"));
        suite.addTest(new StandardWorkFlow("testShowAnnotations"));
        suite.addTest(new StandardWorkFlow("testSearchHistory"));
        suite.addTest(new StandardWorkFlow("testVersioningButtons"));
        suite.addTest(new StandardWorkFlow("testRemoveFileGetBack"));
        suite.addTest(new StandardWorkFlow("testRemoveFileCommit"));
        suite.addTest(new StandardWorkFlow("removeAllData"));
        return suite;
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
    
    public void testCheckOutProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        OutputOperator oo = OutputOperator.invoke();
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //crso.setPassword("");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");   
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        //System.out.println(sessionCVSroot);
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
              
        //second step of checkoutwizard
        //2nd step of CheckOutWizard
        
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        cacheFolder = new File(work, projectName + File.separator + "src" + File.separator + "forimport" + File.separator + "CVS" + File.separator + "RevisionCache");
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        moduleCheck.setModule("ForImport");        
        moduleCheck.setLocalFolder(work.getAbsolutePath()); // NOI18N
        
        //Pseudo CVS server for finishing check out wizard
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_finish_2.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        //cvss.ignoreProbe();
        
        //crso.setCVSRoot(CVSroot);
        //combo.setSelectedItem(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        
        //System.out.println(CVSroot);
        
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        ProjectSupport.waitScanFinished();
        
        //create new elements for testing
        TestKit.createNewElements(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testIgnoreUnignoreFile() throws Exception{
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        VersioningOperator vo;
        //OutputOperator oo;
        OutputTabOperator oto;
        InputStream in;
        PseudoCvsServer cvss;
        String CVSroot;
        JTableOperator table;       
        org.openide.nodes.Node nodeIDE;
        String color;
       
        Node nodeClass = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        
        //show changes stream for pseudocvsserver
        //oo = OutputOperator.invoke();
        //System.out.println(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeClass.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        assertEquals("Wrong row count of table.", 1, table.getRowCount());
        assertEquals("Wrong file listed in table.", "NewClass.java", table.getValueAt(0, 0).toString());
        cvss.stop();
        
        //ignore java file
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        nodeClass.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //nodeClass.performPopupAction("CVS|Show Changes");
        
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeClass = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore java file
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto.clear();
        nodeClass.performPopupAction("CVS|Unignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeClass.performPopupAction("CVS|Show Changes");
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 1, table.getRowCount());
        cvss.stop();   
       
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testIgnoreUnignoreGuiForm() throws Exception{
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        VersioningOperator vo;
        //OutputOperator oo;
        OutputTabOperator oto;
        InputStream in;
        PseudoCvsServer cvss;
        String CVSroot;
        JTableOperator table;       
        org.openide.nodes.Node nodeIDE;
        String color;
        Object[] expected;
        Object[] actual;
       
        Node nodeFrame = new Node(new SourcePackagesNode(projectName), "xx|NewJFrame.java");
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        
        //show changes stream for pseudocvsserver
        //oo = OutputOperator.invoke();
        //System.out.println(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeFrame.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        //assertEquals("Wrong row count of table.", 1, table.getRowCount());
        //assertEquals("Wrong file listed in table.", "NewClass.java", table.getValueAt(0, 0).toString());
        cvss.stop();
        TableModel model = table.getModel();
  
        expected = new String[] {"NewJFrame.form", "NewJFrame.java"};
        actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong files in view", 2, result);
        
        //ignore java file
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        nodeFrame.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //nodeFrame.performPopupAction("CVS|Show Changes");
        
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeFrame = new Node(new SourcePackagesNode(projectName), "xx|NewJFrame.java");
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore java file
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        nodeFrame.performPopupAction("CVS|Unignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeFrame.performPopupAction("CVS|Show Changes");
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 2, table.getRowCount());
        cvss.stop();   
       
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCommit() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        PseudoCvsServer cvss, cvss2, cvss3, cvss4;
        InputStream in, in2, in3, in4;
        CommitOperator co;
        String CVSroot, color;
        JTableOperator table;
        //OutputOperator oo;
        OutputTabOperator oto;
        VersioningOperator vo;
        String[] expected;
        String[] actual;
        String allCVSRoots;
        org.openide.nodes.Node nodeIDE;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        Node packNode = new Node(new SourcePackagesNode("ForImport"), "xx");
        //
        Node nodeClass = new Node(new SourcePackagesNode("ForImport"), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        //
        
        Node nodeFrame = new Node(new SourcePackagesNode("ForImport"), "xx|NewJFrame.java");
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.NEW_COLOR, color);
        //
        
        co = CommitOperator.invoke(packNode);
        Thread.sleep(1000);
        table = co.tabFiles();
        TableModel model = table.getModel();
        
        expected = new String[] {"NewClass.java", "NewJFrame.form", "NewJFrame.java"};   
        actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);
        co.setCommitMessage("Initial commit message");
        
        //oo = OutputOperator.invoke();
        
        //oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        allCVSRoots = cvss.getCvsRoot() + ",";
     
        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_add.in");
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        allCVSRoots = allCVSRoots + cvss2.getCvsRoot() + ",";
        
        in3 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_invoke_commit.in");
        cvss3 = new PseudoCvsServer(in3);
        new Thread(cvss3).start();
        allCVSRoots = allCVSRoots + cvss3.getCvsRoot() + ",";
        
        in4 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_final.in");
        cvss4 = new PseudoCvsServer(in4);
        new Thread(cvss4).start();
        allCVSRoots = allCVSRoots + cvss4.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", allCVSRoots);
        co.commit();
        oto.waitText("Committing \"xx\" finished");
        
        cvss.stop();
        cvss2.stop();
        cvss3.stop();
        cvss4.stop();
        
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());
        
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        assertNull("Wrong color of node!!!", nodeIDE.getHtmlDisplayName());
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testDiffFile() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss;
        InputStream in;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.insert(" insert", 5, 1);
        eo.insert("\tSystem.out.println(\"\");\n", 19, 1);
        eo.save();
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff_class.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        node.performPopupAction("CVS|Diff...");
        //oo = OutputOperator.invoke();
       
        oto.waitText("Diffing \"Main.java\" finished");
        Thread.sleep(1000);
        cvss.stop();
        
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for modified file", TestKit.MODIFIED_COLOR, color);
        
        //verify next button
        DiffOperator diffOp = new DiffOperator("Main.java");
        //
        try {
            TimeoutExpiredException afee = null;
            diffOp.next();
            diffOp.next();
            try {
                diffOp.next();
            } catch (TimeoutExpiredException e) {
                afee = e;
            }
            assertNotNull("TimeoutExpiredException was expected.", afee);

            //verify previous button
            afee = null;
            diffOp.previous();
            diffOp.previous();
            try {
                diffOp.previous();
            } catch (TimeoutExpiredException e) {
                afee = e;
            }
            assertNotNull("TimeoutExpiredException was expected.", afee);
        } catch (Exception e) {
            System.out.println("Problem with buttons of differences");
        }    
 
        //refresh button
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.refresh();
        oto.waitText("Refreshing CVS Status finished");
        cvss.stop();
        Thread.sleep(1000);
        
        //update button
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.update();
        oto.waitText("Updating Sources finished");
        cvss.stop();
        Thread.sleep(1000);
        
        //commit button
        CommitOperator co = diffOp.commit();
        JTableOperator table = co.tabFiles();
        assertEquals("There should be only one file!", 1, table.getRowCount());
        assertEquals("There should be Main.java file only!", "Main.java", table.getValueAt(0, 0));
        co.cancel();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        eo.closeAllDocuments();
        TestKit.deleteRecursively(cacheFolder);
    }
    
    public void testExportDiffPatch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        //nodeClass.select();
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        new ProjectsTabOperator().tree().clearSelection();
        nodeClass.performMenuActionNoBlock("Versioning|CVS|Export");
        Operator.setDefaultStringComparator(oldOperator);
        NbDialogOperator dialog = new NbDialogOperator("Export");
        JTextFieldOperator tf = new JTextFieldOperator(dialog, 0);
        String patchFile = "/tmp/patch" + System.currentTimeMillis() + ".patch"; 
        File file = new File(patchFile);
        //file.createNewFile();
        tf.setText(file.getCanonicalFile().toString());
        JButtonOperator btnExport = new JButtonOperator(dialog, "export");
        //oo = OutputOperator.invoke();
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/export_diff.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        btnExport.push();
        oto.waitText("Diff Patch finished");
        cvss.stop();
        Thread.sleep(1000);
        //test file existence
        assertTrue("Diff Patch file wasn't created!", file.isFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        boolean generated = false;
        if (line != null) {
            generated = line.indexOf("# This patch file was generated by NetBeans IDE") != -1 ? true : false;
        }
            
        br.close();
        assertTrue("Diff Patch file is empty!", generated);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testResolveConflicts() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/create_conflict.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        nodeClass.performPopupActionNoBlock("CVS|Update");
        NbDialogOperator dialog = new NbDialogOperator("Warning");
        dialog.ok();
        oto.waitText("cvs server: conflicts found in Main.java");
        oto.waitText("Updating \"Main.java\" finished");
        Thread.sleep(1000);
        cvss.stop();
        
        nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for file in conflict", TestKit.CONFLICT_COLOR, color);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCreateBranchForProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);        
        
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_branch_switch.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        
        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_branch_switch_1.in");
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        //cvsRoot = cvsRoot + cvss2.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        BranchOperator bo = BranchOperator.invoke(rootNode);
        bo.setBranchName("MyNewBranch");
        bo.checkSwitchToThisBranchAftewards(false);
        bo.checkTagBeforeBranching(false);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);        
        bo.branch();
        Thread.sleep(1000);
        //oo = OutputOperator.invoke();
        //oto.waitText("Branching \"ForImport [Main]\" finished");
        oto.waitText("Branch");
        oto.waitText("ForImport");
        oto.waitText("finished");
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testSwitchProjectToBranch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss;
        InputStream in;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_branch_switch_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvsRoot = cvss.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvsRoot);
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        SwitchToBranchOperator sbo = SwitchToBranchOperator.invoke(rootNode);
        sbo.switchToBranch();
        sbo.setBranch("MyNewBranch");
        sbo.pushSwitch();
        Thread.sleep(1000);
        //oo = OutputOperator.invoke();
        oto.waitText(" to Branch finished");
               
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        
    }
    
    public void testRevertModifications() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss;
        InputStream in;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
      
        //delete RevsionCache folder. It can contain checked out revisions
        TestKit.deleteRecursively(cacheFolder);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "revert_modifications.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        nodeMain.performPopupActionNoBlock("CVS|Revert Modifications");
        NbDialogOperator nbDialog = new NbDialogOperator("Confirm overwrite");
        JButtonOperator btnYes = new JButtonOperator(nbDialog, "Yes");
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        btnYes.push();
        Thread.sleep(1000);
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Reverting finished");
        cvss.stop();
        
        
        nodeIDE = (org.openide.nodes.Node) nodeMain.getOpenideNode();
        assertNull("No color for node expected", nodeIDE.getHtmlDisplayName());
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testShowAnnotations() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        TestKit.deleteRecursively(cacheFolder);
        
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        
        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation_1.in");
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot() + "," + cvss2.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        oto = new OutputTabOperator(sessionCVSroot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        nodeMain.performPopupAction("CVS|Show Annotations");
        Thread.sleep(1000);
        oto.waitText("Loading Annotations finished");
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        cvss.stop(); 
        cvss2.stop();
    }
    
    public void testSearchHistory() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        TestKit.deleteRecursively(cacheFolder);
        
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        //nodeMain.performPopupAction("CVS|Search History...");
        SearchHistoryOperator sho = SearchHistoryOperator.invoke(nodeMain);
        oto.waitText("Searching History finished");
        Thread.sleep(1000);
        cvss.stop();
        
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());   
        sho.setUsername("test");
        sho.setFrom("1.1");
        sho.setTo("1.1");
        sho.btSearch().push();
        oto.waitText("Searching History started");
        Thread.sleep(1000);
        cvss.stop();
        JListOperator list = sho.lstHistory(); 
        
        ListModel model = list.getModel();
        assertEquals("Wrong result count", 2, model.getSize());
        assertTrue("Revision \"1.1\" is missing", model.getElementAt(1).toString().indexOf("1.1") > 0);
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");   
    }
    
    public void testVersioningButtons() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        VersioningOperator vo;
        //OutputOperator oo;
        OutputTabOperator oto;
        InputStream in;
        PseudoCvsServer cvss;
        String CVSroot;
        JTableOperator table;       
        org.openide.nodes.Node nodeIDE;
        String color;
       
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        //perform Show Changes action on Main.java file
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        //oo = OutputOperator.invoke();
        //System.out.println(sessionCVSroot);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/show_changes_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        assertEquals("Table should be empty", 1, table.getRowCount());
        assertEquals("File should be [Remotely Modified]", "Remotely Modified", table.getValueAt(0, 1).toString());
        cvss.stop();
        //System.out.println("Show Changes is ok");
        
        //push refresh button
        //oo = OutputOperator.invoke();
        //oto = oo.getOutputTab(sessionCVSroot);
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //vo = VersioningOperator.invoke();
        vo.refresh();
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        table = vo.tabFiles();
        assertEquals("Table should be empty", 0, table.getRowCount());
        cvss.stop();
        
        //push Update all
        //oo = OutputOperator.invoke();
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.clear();
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //vo = VersioningOperator.invoke();
        vo.update();
        oto.waitText("Updating Sources finished");
        Thread.sleep(1000);
        cvss.stop();
        
        //push commit button
        vo.commit();
        Thread.sleep(1000);
        
        NbDialogOperator dialog = new NbDialogOperator("Comm");
        JButtonOperator btnOk = new JButtonOperator(dialog, "OK");
        btnOk.push();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testRemoveFileGetBack() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        VersioningOperator vo;
        JTableOperator table;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        TestKit.deleteRecursively(cacheFolder);
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        vo = VersioningOperator.invoke();
        cvss.stop();
        
        nodeMain.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        Thread.sleep(1000);
        table = vo.tabFiles();
        assertEquals("Files should have been [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1).toString());       
        //node should disappear
        TimeoutExpiredException tee = null;
        try {
            nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "revert_modifications.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        
        //revert delete - get back the file.
        new Thread(new Runnable() {
            public void run() {
                VersioningOperator vo = VersioningOperator.invoke();
                vo.performPopup("Main.java", "Revert Delete");
            }    
        }).start();
        dialog = new NbDialogOperator("Confirm overwrite");
        dialog.yes();
        oto.waitText("Reverting finished");
        Thread.sleep(1000);
        cvss.stop();
        //node should be back
        nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testRemoveFileCommit() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        //OutputOperator oo;
        OutputTabOperator oto;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        VersioningOperator vo;
        JTableOperator table;
        
        oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        
        TestKit.deleteRecursively(cacheFolder);
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        Thread.sleep(1000);
        vo = VersioningOperator.invoke();
        cvss.stop();
        
        //delete file again and commit deletion
        nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        nodeMain.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        Thread.sleep(1000);
        table = vo.tabFiles();
        assertEquals("Files should have been [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1).toString());       
        
        TimeoutExpiredException tee = null;
        try {
            nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "commit_locally_deleted_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        
        //revert delete - get back the file.
        CommitOperator co = vo.commit();
        table = co.tabFiles();
        assertEquals("There should be one file only", 1, table.getRowCount());
        assertEquals("There should Main.java file", "Main.java", table.getValueAt(0, 0));
        assertEquals("File Main.java should be [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1));
        co.commit();
 
        Thread.sleep(1000);
        cvss.stop();
        oto.waitText("Committing finished");
        
        tee = null;
        try {
            nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void removeAllData() {
        TestKit.removeAllData(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
