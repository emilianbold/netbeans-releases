/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ignoreUnignoreFileTest.java
 *
 * Created on 14. prosinec 2006, 16:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.InputStream;
import javax.swing.table.TableModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;


/**
 *
 * @author novakm
 */
public class IgnoreUnignoreTest extends JellyTestCase {
    String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of ignoreUnignoreFileTest */
    public IgnoreUnignoreTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new IgnoreUnignoreTest("testCheckOutProject"));
        suite.addTest(new IgnoreUnignoreTest("testIgnoreUnignoreFile"));
        suite.addTest(new IgnoreUnignoreTest("testIgnoreUnignoreGuiForm"));                
        suite.addTest(new IgnoreUnignoreTest("removeAllData"));
        return suite;
    }
    
    public void testCheckOutProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 18000);
        TestKit.closeProject(projectName);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
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
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = oo.getOutputTab(sessionCVSroot);
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
        oto = new OutputTabOperator(sessionCVSroot);
        oto.clear();
        nodeClass.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_file");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeClass = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
        nodeIDE = (org.openide.nodes.Node) nodeClass.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore java file
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
        oto.waitText("Refreshing CVS Status finished");
        Thread.sleep(1000);
        cvss.stop();
        TableModel model = table.getModel();
        
        expected = new String[] {"NewJFrame.form", "NewJFrame.java"};
        actual = new String[model.getRowCount()];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = model.getValueAt(i, 0).toString();
        }
        int result = TestKit.compareThem(expected, actual, false);
        assertEquals("Wrong files in view", 2, result);
        
        //ignore
        oto = new OutputTabOperator(sessionCVSroot);
        oto.clear();
        nodeFrame.performPopupAction("CVS|Ignore");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_changes_for_jframe");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        Thread.sleep(1000);
        assertEquals("File should not be listed in table", 0, table.getRowCount());
        cvss.stop();
        
        nodeFrame = new Node(new SourcePackagesNode(projectName), "xx|NewJFrame.java");
        nodeIDE = (org.openide.nodes.Node) nodeFrame.getOpenideNode();
        color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
        assertEquals("Wrong color for new file", TestKit.IGNORED_COLOR, color);
        
        //unignore
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
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
