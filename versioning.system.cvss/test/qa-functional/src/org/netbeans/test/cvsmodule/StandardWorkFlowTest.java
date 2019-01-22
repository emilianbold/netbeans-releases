/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.test.cvsmodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
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
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * 
 */
public class StandardWorkFlowTest extends JellyTestCase {

    static String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    static Logger log;
    
    /**
     * Creates a new instance of CheckOutWizardTest
     */
    public StandardWorkFlowTest(String name) {
        super(name);
        if (os_name == null) {
            os_name = System.getProperty("os.name");
        }
        try {
            TestKit.extractProtocol(getDataDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
          
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(StandardWorkFlowTest.class).addTest(
                        "testCheckOutProject",
                        "testIgnoreUnignoreFile",
                        "testIgnoreUnignoreGuiForm",
                        "testCommit",
                        "testCreateBranchForProject",
                        "testSwitchProjectToBranch",
                        "testDiffFile",
                        "testExportDiffPatch",
                        "testResolveConflicts",
                        "testRevertModifications",
                        "testShowAnnotations",
                        "testSearchHistory",
                        "testVersioningButtons",
                        "testRemoveFileGetBack",
                        "testRemoveFileCommit",
                        "removeAllData"
                )
                .enableModules(".*")
                .clusters(".*")
        );
     }
   
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
        if (log == null) {
            log = Logger.getLogger("org.netbeans.modules.versioning.system.cvss.t9y");
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
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
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);

        TestKit.closeProject(projectName);
        TestKit.showStatusLabels();
//
        if ((os_name !=null) && (os_name.indexOf("Mac") > -1))
            NewProjectWizardOperator.invoke().close();

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
        
        TestKit.waitText(mh);
        cvss.stop();
        in.close();
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
        
        //create new elements for testing
        TestKit.createNewElements(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testIgnoreUnignoreFile() throws Exception{
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        VersioningOperator vo;
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

        MessageHandler mh = new MessageHandler("Refreshing");
        log.addHandler(mh);

        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeClass.performPopupAction("CVS|Show Changes");
        new EventTool().waitNoEvent(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        assertEquals("Wrong row count of table.", 1, table.getRowCount());
        assertEquals("Wrong file listed in table.", "NewClass.java", table.getValueAt(0, 0).toString());
        cvss.stop();
        
        //ignore java file
        nodeClass.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //nodeClass.performPopupAction("CVS|Show Changes");
        
        new EventTool().waitNoEvent(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeClass = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore java file
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        nodeClass.performPopupAction("CVS|Unignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeClass.performPopupAction("CVS|Show Changes");
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
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
        MessageHandler mh = new MessageHandler("Refreshing");
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeFrame.performPopupAction("CVS|Show Changes");
        new EventTool().waitNoEvent(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
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
        nodeFrame.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //nodeFrame.performPopupAction("CVS|Show Changes");
        
        new EventTool().waitNoEvent(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeFrame = new Node(new SourcePackagesNode(projectName), "xx|NewJFrame.java");
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore java file
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        nodeFrame.performPopupAction("CVS|Unignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeFrame.performPopupAction("CVS|Show Changes");
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
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
        VersioningOperator vo;
        String[] expected;
        String[] actual;
        String allCVSRoots;
        org.openide.nodes.Node nodeIDE;
        
        MessageHandler mh = new MessageHandler("Committing");
        log.addHandler(mh);
        
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
        new EventTool().waitNoEvent(1000);
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
        TestKit.waitText(mh);
        
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
        org.openide.nodes.Node nodeIDE;
        String color;
        
        MessageHandler mh = new MessageHandler("Diffing");
        log.addHandler(mh);

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
       
        //TestKit.waitText(mh);
        new EventTool().waitNoEvent(2000);
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
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.refresh();
        TestKit.removeHandlers(log);
        cvss.stop();
        new EventTool().waitNoEvent(1000);
        
        //update button
        mh = new MessageHandler("Updating");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/refresh.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        diffOp.update();
        TestKit.waitText(mh);
        cvss.stop();
        new EventTool().waitNoEvent(1000);
        
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
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        
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
        
        MessageHandler mh = new MessageHandler("Diff Patch");
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/export_diff.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        btnExport.push();
        TestKit.waitText(mh);
        cvss.stop();
        new EventTool().waitNoEvent(1000);
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
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;

        MessageHandler mh = new MessageHandler("Updating");
        log.addHandler(mh);
        
        Node nodeClass = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "diff/create_conflict.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        nodeClass.performPopupActionNoBlock("CVS|Update");
        NbDialogOperator dialog = new NbDialogOperator("Warning");
        dialog.ok();
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
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
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;        
        
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

        MessageHandler mh = new MessageHandler("Branching");
        log.addHandler(mh);

        BranchOperator bo = BranchOperator.invoke(rootNode);
        bo.setBranchName("MyNewBranch");
        bo.checkSwitchToThisBranchAftewards(false);
        bo.checkTagBeforeBranching(false);
        
        bo.branch();
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testSwitchProjectToBranch() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss;
        InputStream in;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        MessageHandler mh = new MessageHandler("Switching");
        log.addHandler(mh);
        
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_branch_switch_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvsRoot = cvss.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvsRoot);
        SwitchToBranchOperator sbo = SwitchToBranchOperator.invoke(rootNode);
        sbo.switchToBranch();
        sbo.setBranch("MyNewBranch");
        sbo.pushSwitch();
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
               
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        
    }
    
    public void testRevertModifications() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss;
        InputStream in;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        MessageHandler mh = new MessageHandler("Reverting");
        log.addHandler(mh);
      
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
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
        
        nodeIDE = (org.openide.nodes.Node) nodeMain.getOpenideNode();
        assertNull("No color for node expected", nodeIDE.getHtmlDisplayName());
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testShowAnnotations() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        TestKit.deleteRecursively(cacheFolder);
        
        MessageHandler mh = new MessageHandler("Loading Annotations");
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        
        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation_1.in");
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot() + "," + cvss2.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        nodeMain.performPopupAction("CVS|Show Annotations");
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        cvss.stop(); 
        cvss2.stop();
    }
    
    public void testSearchHistory() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        
        TestKit.deleteRecursively(cacheFolder);
        
        MessageHandler mh = new MessageHandler("Searching History");
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        //nodeMain.performPopupAction("CVS|Search History...");
        SearchHistoryOperator sho = SearchHistoryOperator.invoke(nodeMain);
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        cvss.stop();
        
        mh = new MessageHandler("Searching History");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());   
        sho.setUsername("test");
        sho.setFrom("1.1");
        sho.setTo("1.1");
        sho.btSearch().push();
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
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
        InputStream in;
        PseudoCvsServer cvss;
        String CVSroot;
        JTableOperator table;       
        org.openide.nodes.Node nodeIDE;
        String color;
       
        MessageHandler mh = new MessageHandler("Refreshing");
        log.addHandler(mh);
        
        //perform Show Changes action on Main.java file
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/show_changes_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        new EventTool().waitNoEvent(1000);
        vo = VersioningOperator.invoke();
        table = vo.tabFiles();
        //System.out.println(""+table);
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        assertEquals("Table should be empty", 1, table.getRowCount());
        assertEquals("File should be [Remotely Modified]", "Remotely Modified", table.getValueAt(0, 1).toString());
        cvss.stop();
        //System.out.println("Show Changes is ok");
        
        //push refresh button
        mh = new MessageHandler("Refreshing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //vo = VersioningOperator.invoke();
        vo.refresh();
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        table = vo.tabFiles();
        assertEquals("Table should be empty", 0, table.getRowCount());
        cvss.stop();
        
        //push Update all
        mh = new MessageHandler("Updating");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //vo = VersioningOperator.invoke();
        vo.update();
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        cvss.stop();
        
        //push commit button
        vo.commit();
        new EventTool().waitNoEvent(1000);
        
        NbDialogOperator dialog = new NbDialogOperator("Comm");
        JButtonOperator btnOk = new JButtonOperator(dialog, "OK");
        btnOk.push();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testRemoveFileGetBack() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        VersioningOperator vo;
        JTableOperator table;
        
        MessageHandler mh = new MessageHandler("Reverting");
        log.addHandler(mh);
        
        TestKit.deleteRecursively(cacheFolder);
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        new EventTool().waitNoEvent(1000);
        vo = VersioningOperator.invoke();
        cvss.stop();
        
        nodeMain.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        new EventTool().waitNoEvent(1000);
        table = vo.tabFiles();
        assertEquals("Files should have been [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1).toString());       
        //node should disappear
        TimeoutExpiredException tee = null;
        long timeOut = TestKit.changeTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        try {
            nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        TestKit.changeTimeout("ComponentOperator.WaitComponentTimeout", timeOut);
        
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
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        cvss.stop();
        //node should be back
        nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testRemoveFileCommit() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);   
        PseudoCvsServer cvss, cvss2, cvss3;
        InputStream in, in2, in3;
        org.openide.nodes.Node nodeIDE;
        String color, CVSroot;
        VersioningOperator vo;
        JTableOperator table;

        MessageHandler mh = new MessageHandler("Refreshing");
        log.addHandler(mh);
        
        TestKit.deleteRecursively(cacheFolder);
        Node nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "versioning/refresh_main.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        nodeMain.performPopupAction("CVS|Show Changes");
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
        vo = VersioningOperator.invoke();
        cvss.stop();
        
        //delete file again and commit deletion
        nodeMain = new Node(new SourcePackagesNode(projectName), pathToMain);
        nodeMain.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        new EventTool().waitNoEvent(1000);
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

        mh = new MessageHandler("Committing");
        TestKit.removeHandlers(log);
        log.addHandler(mh);
        //revert delete - get back the file.
        CommitOperator co = vo.commit();
        table = co.tabFiles();
        assertEquals("There should be one file only", 1, table.getRowCount());
        assertEquals("There should Main.java file", "Main.java", table.getValueAt(0, 0));
        assertEquals("File Main.java should be [Locally Deleted]", "Locally Deleted", table.getValueAt(0, 1));
        co.commit();
 
        new EventTool().waitNoEvent(1000);
        cvss.stop();
        TestKit.waitText(mh);
        
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
