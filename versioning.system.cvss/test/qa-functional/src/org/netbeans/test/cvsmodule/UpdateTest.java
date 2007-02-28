/*
 * UpdateTest.java
 *
 * Created on July 12, 2006, 12:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CommitOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author pvcs
 */
public class UpdateTest extends JellyTestCase {
    
    static final String PROJECT1 = "Project1";
    static final String PROJECT2 = "Project2";
    static final String cvsRoot1 = ":pserver:test@qa-linux-s6:/usr/local/CVSrepo";
    static final String cvsRoot2 = ":pserver:pvcs@peterp.czech.sun.com:/usr/cvsrepo";
    //static final String[] nodes1 = new String[] {"aa|NewClass1.java", "aa|NewClass2.java", "aa|NewClass3.java", "aa|NewClass4.java", "aa|NewClass5.java",
    //        "bb|NewClass1.java", "bb|NewClass2.java", "bb|NewClass3.java", "bb|NewClass4.java", "bb|NewClass5.java",
    //        "cc|NewClass1.java", "cc|NewClass2.java", "cc|NewClass3.java", "cc|NewClass4.java", "cc|NewClass5.java"};
    
    static final String[] nodes1 = new String[] {"aa|NewClass1.java", "aa|NewClass2.java"};
            
    String os_name;
    static String sessionCVSroot;
    boolean unix = false;
    final String projectName = "CVS Client Library";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of UpdateTest */
    public UpdateTest(String name) {
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
        //suite.addTest(new UpdateTest("testOpen"));
        suite.addTest(new UpdateTest("testBrokenUpdateModMer"));
        suite.addTest(new UpdateTest("testBrokenUpdateModConf"));
        suite.addTest(new UpdateTest("testBrokenUpdateModMod"));
        suite.addTest(new UpdateTest("testBrokenUpdateMerMer"));
        suite.addTest(new UpdateTest("testBrokenUpdateMerConf"));
        suite.addTest(new UpdateTest("testBrokenUpdateConfMer"));
        suite.addTest(new UpdateTest("testBrokenUpdateConfMod"));
        suite.addTest(new UpdateTest("testBrokenUpdateModMerMer"));
        suite.addTest(new UpdateTest("testBrokenUpdateModMerConf"));
        suite.addTest(new UpdateTest("testBrokenUpdateModConfConf"));
        suite.addTest(new UpdateTest("testBrokenUpdateMerModMer"));
        suite.addTest(new UpdateTest("testBrokenUpdateConfModConf"));
        suite.addTest(new UpdateTest("testBrokenUpdateConfConfMod"));
        suite.addTest(new UpdateTest("testBrokenUpdateMerMerMod"));
        return suite;
    }
    
    public void testOpen() throws Exception {
        File loc = new File("/tmp/work/w1153322002833");
        //closeProject(PROJECT1);
        //closeProject(PROJECT2);
        openProject(loc, PROJECT1);
    }
    
    public void testUpdate() throws Exception {
        String cvsRoot = ":pserver:anoncvs@cvsnetbeansorg.sfbay.sun.com:/cvs";
        Node node;
        org.openide.nodes.Node nodeIDE;
        String color;
                
        String[] nodes = new String[] {
            "org.netbeans.lib.cvsclient|Bundle.properties",
            "org.netbeans.lib.cvsclient|CVSRoot.java",
            "org.netbeans.lib.cvsclient|Client.java",
            "org.netbeans.lib.cvsclient|ClientServices.java",
            "org.netbeans.lib.cvsclient.admin|AdminHandler.java",
            "org.netbeans.lib.cvsclient.admin|DateComparator.java",
            "org.netbeans.lib.cvsclient.admin|Entry.java",
            "org.netbeans.lib.cvsclient.admin|StandardAdminHandler.java",
            "org.netbeans.lib.cvsclient.command|BasicCommand.java",
            "org.netbeans.lib.cvsclient.command|BinaryBuilder.java",
            "org.netbeans.lib.cvsclient.command|BuildableCommand.java",
            "org.netbeans.lib.cvsclient.command|Builder.java",
            "org.netbeans.lib.cvsclient.command|Bundle.properties",
            "org.netbeans.lib.cvsclient.command|Command.java",
            "org.netbeans.lib.cvsclient.command|CommandAbortedException.java",
            "org.netbeans.lib.cvsclient.command|CommandException.java",
            "org.netbeans.lib.cvsclient.command|CommandUtils.java",
            "org.netbeans.lib.cvsclient.command|DefaultFileInfoContainer.java",
            "org.netbeans.lib.cvsclient.command|FileInfoContainer.java",
            "org.netbeans.lib.cvsclient.command|GlobalOptions.java",
            "org.netbeans.lib.cvsclient.command|KeywordSubstitutionOptions.java",
            "org.netbeans.lib.cvsclient.command|PipedFileInformation.java",
            "org.netbeans.lib.cvsclient.command|PipedFilesBuilder.java",
            "org.netbeans.lib.cvsclient.command|RepositoryCommand.java",
            "org.netbeans.lib.cvsclient.command|TemporaryFileCreator.java",
            "org.netbeans.lib.cvsclient.command|Watch.java",
            "org.netbeans.lib.cvsclient.command|WrapperUtils.java"
        };
        VersioningOperator vo = VersioningOperator.invoke();
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.clear();
        node = new Node(new ProjectsTabOperator().tree(), projectName);
        node.performPopupAction("CVS|Show Changes");
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        
        assertEquals("Wrong files counts in Versioning view", nodes.length, vo.tabFiles().getRowCount());
        String[] actual = new String[vo.tabFiles().getRowCount()];
        String[] expected = new String[nodes.length];
        for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
            actual[i] = vo.tabFiles().getModel().getValueAt(i, 0).toString();
        }
        for (int i = 0; i < nodes.length; i++) {
            expected[i] = getObjectName(nodes[i]);
        }
        
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Some files disappear!!!", expected.length, result);
        
        for (int j = 0; j < 10; j ++) {
            oto = new OutputTabOperator(cvsRoot);
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.clear();
            node = new Node(new ProjectsTabOperator().tree(), projectName);
            node.performPopupAction("CVS|Update");
            oto.waitText("Updating \"CVS Client Library\" finished");
            Thread.sleep(1000);
            for (int i = 0; i < nodes.length; i++) {
                node = new Node(new SourcePackagesNode(projectName), nodes[i]);
                nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong color of <" + nodes[i] + ">", TestKit.MODIFIED_COLOR, color);
            }
            vo = VersioningOperator.invoke();
            actual = new String[vo.tabFiles().getRowCount()];
            for (int i = 0; i < vo.tabFiles().getRowCount(); i++) {
                actual[i] = vo.tabFiles().getModel().getValueAt(i, 0).toString();
            }
            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Some files disappear!!!", expected.length, result);
        }    
    }
    
    String getObjectName(String value) {
        int pos = value.lastIndexOf('|');
        return value.substring(pos + 1);
    }
    
    
    /* test invokes issue #71488
     * if 1st file is "M" and
     * 2nd is merged then first one changed to up-to-date
     */
    
    public void testBrokenUpdateModMer() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        //closeProject(PROJECT2);
        //TestKit.deleteRecursively(work);
        
        Node node1;
        Node node2;
        org.openide.nodes.Node nodeIDE1;
        org.openide.nodes.Node nodeIDE2;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        //location2 = checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
        
        for (int i = 0; i < 1; i++) {
            editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
            //editFilesForMerge(PROJECT2, iter);
            
            closeProject(PROJECT1);
            //closeProject(PROJECT2);
            
            checkOutProject(cvsRoot1, "test", PROJECT1);
            //checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
        
            editChosenFile(PROJECT1, "NewClass2.java", 7, iter1);
            //editFiles(PROJECT2, iter);
            
            node1 = new Node(new SourcePackagesNode(PROJECT1), "");
            //node2 = new Node(new SourcePackagesNode(PROJECT2), "");
            CommitOperator co = CommitOperator.invoke(new Node[] {node1});
            assertEquals("Wrong count of files to commit", 1, co.tabFiles().getRowCount());

            OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
            //OutputTabOperator oto2 = new OutputTabOperator(cvsRoot2);
            oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto1.clear();
            //oto2.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            //oto2.clear();
            co.commit();
            oto1.waitText("Committing");
            oto1.waitText("finished");
            //oto2.waitText("Committing");
            //oto2.waitText("finished");
            //delete all
            closeProject(PROJECT1);
            //closeProject(PROJECT2);
            //TestKit.deleteRecursively(work);
            
            openProject(location1, PROJECT1);
            //openProject(location2, PROJECT2);
            
            editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
            //editFilesOthers(PROJECT2, iter);
            
            updateProject(PROJECT1, cvsRoot1);
            //updateProject(PROJECT2, cvsRoot2);
            
            Thread.sleep(1000);
            oto1 = new OutputTabOperator(cvsRoot1);
            oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto1.clear();
            Node node = new Node(new SourcePackagesNode(PROJECT1), "");
            node.performPopupAction("CVS|Show Changes");
            oto1.waitText("Refreshing CVS Status finished");
            
            VersioningOperator vo = VersioningOperator.invoke();
            String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
            String[] actual = new String[vo.tabFiles().getRowCount()];
            for (int k = 0; k < actual.length; k++) {
                actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
                System.out.println(actual[k]);
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Wrong records displayed in dialog", 2, result);
            
            //Commit
            node1 = new Node(new SourcePackagesNode(PROJECT1), "");
            //node2 = new Node(new SourcePackagesNode(PROJECT2), "");
            co = CommitOperator.invoke(new Node[] {node1});
            assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
            co.cancel();
            /*
            oto1 = new OutputTabOperator(cvsRoot1);
            //oto2 = new OutputTabOperator(cvsRoot2);
            oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto1.clear();
            //oto2.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            //oto2.clear();
            co.commit();
            oto1.waitText("Committing");
            oto1.waitText("finished");
            //oto2.waitText("Committing");
            //oto2.waitText("finished");
            //delete all
            closeProject(PROJECT1);
            //closeProject(PROJECT2);
            //TestKit.deleteRecursively(work);
            
            //check out again
            location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
            //location2 = checkOutProject(cvsRoot2, "pvcspvcs", PROJECT2);
            
            //validate data
            validateCheckout(PROJECT1, iter, new int[] {1, 6});
            //validateCheckout(PROJECT2, iter, new int[] {1, 6});
           */
        }
    }
    
    public void testBrokenUpdateModConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
               
        iter1 = System.currentTimeMillis();
        //change last file from last package
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 1, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        //OutputTabOperator oto2 = new OutputTabOperator(cvsRoot2);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Locally Modified", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();        
    }        
    
    public void testBrokenUpdateModMod() throws Exception {
        int j = 0;
        long iter;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        //closeProject(PROJECT2);
        //TestKit.deleteRecursively(work);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
                
        iter = System.currentTimeMillis();
        
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter);
        
        updateProject(PROJECT1, cvsRoot1);
        
        Thread.sleep(1000);
        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Locally Modified", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);


        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();
    }
    
    public void testBrokenUpdateMerMer() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
               
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter1);
        closeProject(PROJECT1);
                    
        checkOutProject(cvsRoot1, "test", PROJECT1);

        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter2);

        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
            System.out.println(actual[k]);
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Locally Modified", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();
    }
    
    public void testBrokenUpdateConfConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        //change last file from last package
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
            System.out.println(actual[k]);
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Local Conflict", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();     
    }

    public void testBrokenUpdateMerConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);

        closeProject(PROJECT1);

        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 2, iter2);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter2);
        //editFiles(PROJECT2, iter);

        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        updateProject(PROJECT1, cvsRoot1);

        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Locally Modified", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();           
    }

    public void testBrokenUpdateConfMer() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        iter1 = System.currentTimeMillis();
        //change last file from last package
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass2.java", 2, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Local Conflict", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateConfMod() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File location2;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
               
        iter1 = System.currentTimeMillis();
        
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 1, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        expected = new String[] {"Local Conflict", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 2, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 2, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateModMerMer() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        iter1 = System.currentTimeMillis();
        
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass1.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Locally Modified", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateModMerConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
                
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter1);
        closeProject(PROJECT1);
                    
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass1.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Locally Modified", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateModConfConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        Node node2;
        org.openide.nodes.Node nodeIDE1;
        org.openide.nodes.Node nodeIDE2;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass1.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Local Conflict", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();          
    }
    
    public void testBrokenUpdateMerModMer() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass2.java", 7, iter1);
        updateProject(PROJECT1, cvsRoot1);
        
        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Locally Modified", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }

    public void testBrokenUpdateMerModConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass2.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Locally Modified", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateConfModConf() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter2);
        editChosenFile(PROJECT1, "NewClass3.java", 3, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass2.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Local Conflict", "Locally Modified", "Local Conflict"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateConfConfMod() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        Node node2;
        org.openide.nodes.Node nodeIDE1;
        org.openide.nodes.Node nodeIDE2;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter2);
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass3.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        NbDialogOperator nbDialog = new NbDialogOperator("Warning");
        JButtonOperator btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");
        nbDialog = new NbDialogOperator("Command");
        btnOk = new JButtonOperator(nbDialog);
        btnOk.push();

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Local Conflict", "Local Conflict", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public void testBrokenUpdateMerMerMod() throws Exception {
        int j = 0;
        long iter1 = 1;
        long iter2 = 2;
        File location1;
        File work = new File("/tmp/work");
        work.mkdirs();
        closeProject(PROJECT1);
        
        Node node1;
        org.openide.nodes.Node nodeIDE1;
        
        location1 = checkOutProject(cvsRoot1, "test", PROJECT1);
        
        iter1 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 3, iter1);
        editChosenFile(PROJECT1, "NewClass2.java", 3, iter1);
        
        closeProject(PROJECT1);
        
        checkOutProject(cvsRoot1, "test", PROJECT1);
        iter2 = System.currentTimeMillis();
        editChosenFile(PROJECT1, "NewClass1.java", 5, iter2);
        editChosenFile(PROJECT1, "NewClass2.java", 5, iter2);
        
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        CommitOperator co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit", 2, co.tabFiles().getRowCount());

        OutputTabOperator oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();
        co.commit();
        oto1.waitText("Committing");
        oto1.waitText("finished");
        closeProject(PROJECT1);
        
        openProject(location1, PROJECT1);
        
        editChosenFile(PROJECT1, "NewClass3.java", 7, iter1);
        
        updateProject(PROJECT1, cvsRoot1);
        
        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        Node node = new Node(new SourcePackagesNode(PROJECT1), "");
        node.performPopupAction("CVS|Show Changes");
        oto1.waitText("Refreshing CVS Status finished");

        Thread.sleep(1000);
        oto1 = new OutputTabOperator(cvsRoot1);
        oto1.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto1.clear();

        VersioningOperator vo = VersioningOperator.invoke();
        String[] expected = new String[] {"NewClass1.java", "NewClass2.java", "NewClass3.java"};
        String[] actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        expected = new String[] {"Locally Modified", "Locally Modified", "Locally Modified"};
        actual = new String[vo.tabFiles().getRowCount()];
        for (int k = 0; k < actual.length; k++) {
            actual[k] = vo.tabFiles().getValueAt(k, 1).toString();
        }
        result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong records displayed in dialog", 3, result);

        //Commit
        node1 = new Node(new SourcePackagesNode(PROJECT1), "");
        co = CommitOperator.invoke(new Node[] {node1});
        assertEquals("Wrong count of files to commit - issue #71488", 3, co.tabFiles().getRowCount());
        co.cancel();           
    }
    
    public static void closeProject(String project_name) {
        try {
            Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
            rootNode.performPopupAction("Close Project");
        }    
        catch (Exception e) {
            
        }    
    }
    
    public void updateProject(String project, String cvsRoot) throws Exception {
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        Node node = new Node(new ProjectsTabOperator().tree(), project);
        node.performPopupAction("CVS|Update");
        oto.waitText("Updating");
        oto.waitText("finished");        
    }
    
    public File checkOutProject(String cvsRoot, String passwd, String project) throws Exception {
        File work = new File("/tmp/work/w" + System.currentTimeMillis());
        work.mkdir();
        OutputOperator oo = OutputOperator.invoke();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(cvsRoot);
        //crso.setPassword("");
        crso.setPassword(passwd);
        crso.next();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        moduleCheck.setModule(project);
        moduleCheck.setLocalFolder(work.getCanonicalPath());
        moduleCheck.finish();
        OutputTabOperator oto = new OutputTabOperator(cvsRoot);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        ProjectSupport.waitScanFinished();
        
        return work;
    }
    
    public void editChosenFile(String project, String name, int line, long iter) {
        Node node = new Node(new ProjectsTabOperator().tree(), project);
        //node.performPopupAction("CVS|Show Changes");
        node = new Node(new SourcePackagesNode(project), "aa|" + name);
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator(name);
        eo.insert("//" + name + " >iter< " + iter + "\n", line, 1);
        eo.save();
    }
    
    public void validateCheckout(String project, long iter, int[] indexes) throws Exception {
        Node node;
        EditorOperator eo;
        for (int i = nodes1.length - 1; i < nodes1.length; i++) {
            node = new Node(new SourcePackagesNode(project), nodes1[i]);
            node.performPopupAction("Open");
            eo = new EditorOperator(getObjectName(nodes1[i]));
            for (int j = 0; j < indexes.length; j++) {
                String line = eo.getText(indexes[j]);
                System.out.println("line: " + line);
                assertEquals("Data was not committed!!!", "//" + nodes1[i] + " >iter< " + iter + "\n", line);
                
            }
            if (i == nodes1.length - 1) 
                eo.closeDiscardAll();
        }
        
    }
    
    public void openProject(File location, String project) throws Exception {
        new ActionNoBlock("File|Open Project", null).perform();
        NbDialogOperator nb = new NbDialogOperator("Open Project");
        JFileChooserOperator fco = new JFileChooserOperator(nb);
        System.out.println(location.getCanonicalPath());
        fco.setCurrentDirectory(new File(location, project));
        fco.approve();
        ProjectSupport.waitScanFinished();
    }
}
