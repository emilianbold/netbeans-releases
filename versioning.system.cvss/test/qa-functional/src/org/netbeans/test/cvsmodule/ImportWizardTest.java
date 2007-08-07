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

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.InputStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.BrowseRepositoryFolderOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.EditCVSRootOperator;
import org.netbeans.jellytools.modules.javacvs.FolderToImportStepOperator;
import org.netbeans.jellytools.modules.javacvs.ImportWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPasswordFieldOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
/**
 *
 * @author peter
 */
public class ImportWizardTest extends JellyTestCase {
    
    String os_name;
    File file;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ImportWizardTest("prepareProject"));
        suite.addTest(new ImportWizardTest("testImportWizardPserverUI"));
        suite.addTest(new ImportWizardTest("testImportWizardLocalUI"));
        suite.addTest(new ImportWizardTest("testImportWizardForkUI"));
        suite.addTest(new ImportWizardTest("testImportWizardExtUI"));
        suite.addTest(new ImportWizardTest("testImportWizardExt"));
        suite.addTest(new ImportWizardTest("testImportWizardLocal"));
        suite.addTest(new ImportWizardTest("testImportWizardFork"));
        suite.addTest(new ImportWizardTest("testImportWizardPserver"));
        suite.addTest(new ImportWizardTest("testImportWizardLoginSuccess"));
        suite.addTest(new ImportWizardTest("testImportWizardSecondStepUI"));
        suite.addTest(new ImportWizardTest("testImportWizardFinish"));
        suite.addTest(new ImportWizardTest("removeAllData"));
        //debug
        //suite.addTest(new ImportWizardTest("prepareProject"));
        
        //suite.addTest(new ImportWizardTest("removeAllData"));
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
    
    /** Creates a new instance of ImportWizardTest */
    public ImportWizardTest(String name) {
        super(name);
    }
    
    public void testImportWizardPserverUI() {   
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":local:/cvs");
        //Invalid CVS Root
        crso.setCVSRoot(":pserver:test");
        try {
            JLabelOperator inv = new JLabelOperator(crso, "Invalid CVS Root");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        crso.setCVSRoot(":pserver:test@localhost:2401/cvs");
        //start test UI
        
        //combobox
        try {
            JComboBoxOperator combo = new JComboBoxOperator(crso);
            JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
            JButtonOperator btnEdit = new JButtonOperator(crso, "Edit...");
            JButtonOperator btnProxy = new JButtonOperator(crso, "Proxy Configuration...");
            JButtonOperator btnBack = new JButtonOperator(crso, "< Back");
            JButtonOperator btnNext = new JButtonOperator(crso, "Next >");
            JButtonOperator btnFinish = new JButtonOperator(crso, "Finish");
            JButtonOperator btnCancel = new JButtonOperator(crso, "Cancel");
            JButtonOperator btnHelp = new JButtonOperator(crso, "Help");
        }  catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        iwo.cancel();
    }
    
    public void testImportWizardLocalUI() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":local:/cvs");
        //Invalid CVS Root
        crso.setCVSRoot(":loca:");
        try {
            JLabelOperator inv = new JLabelOperator(crso, "Only :pserver:, :local:, :ext: and :fork: connection methods supported");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        crso.setCVSRoot(":local:/cvs");
        Exception ex;
        //start test UI
        //combobox
        try {
            JComboBoxOperator combo = new JComboBoxOperator(crso);
            //JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
            JButtonOperator btnEdit = new JButtonOperator(crso, "Edit...");
            JButtonOperator btnBack = new JButtonOperator(crso, "< Back");
            JButtonOperator btnNext = new JButtonOperator(crso, "Next >");
            JButtonOperator btnFinish = new JButtonOperator(crso, "Finish");
            JButtonOperator btnCancel = new JButtonOperator(crso, "Cancel");
            JButtonOperator btnHelp = new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        
        iwo.cancel();
    }
    
    public void testImportWizardForkUI() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":fork:/cvs");
        //Invalid CVS Root
        crso.setCVSRoot(":for:");
        try {
            JLabelOperator inv = new JLabelOperator(crso, "Only :pserver:, :local:, :ext: and :fork: connection methods supported");
            assertNotNull(inv);
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        crso.setCVSRoot(":fork:/cvs");
        //start test UI
        
        try {
            JComboBoxOperator combo = new JComboBoxOperator(crso);
            //JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
            JButtonOperator btnEdit = new JButtonOperator(crso, "Edit...");
            JButtonOperator btnBack = new JButtonOperator(crso, "< Back");
            JButtonOperator btnNext = new JButtonOperator(crso, "Next >");
            JButtonOperator btnFinish = new JButtonOperator(crso, "Finish");
            JButtonOperator btnCancel = new JButtonOperator(crso, "Cancel");
            JButtonOperator btnHelp = new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        
        iwo.cancel();
    }
    
    public void testImportWizardExtUI() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":ext:test@localhost:2401/cvs");
        //Invalid CVS Root
        crso.setCVSRoot(":ext:test");
        try {
            JLabelOperator inv = new JLabelOperator(crso, "Invalid CVS Root");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        crso.setCVSRoot(":ext:test@localhost:2401/cvs");
        //start test UI
        try {
            JComboBoxOperator combo = new JComboBoxOperator(crso);
            JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
            JButtonOperator btnEdit = new JButtonOperator(crso, "Edit...");
            JButtonOperator btnProxy = new JButtonOperator(crso, "Proxy Configuration...");
            JRadioButtonOperator internal = new JRadioButtonOperator(crso, "Use Internal SSH");
            JRadioButtonOperator external = new JRadioButtonOperator(crso, "Use External Shell");
            JCheckBoxOperator remeber = new JCheckBoxOperator(crso, "Remember Password");
            JTextFieldOperator sshCommand = new JTextFieldOperator(crso);
            JButtonOperator btnBack = new JButtonOperator(crso, "< Back");
            JButtonOperator btnNext = new JButtonOperator(crso, "Next >");
            JButtonOperator btnFinish = new JButtonOperator(crso, "Finish");
            JButtonOperator btnCancel = new JButtonOperator(crso, "Cancel");
            JButtonOperator btnHelp = new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        iwo.cancel();
    }
    
    public void testImportWizardLoginSuccess() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        if (in == null) {
            System.err.println(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
            in.markSupported();
        }
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        crso.setCVSRoot(cvss.getCvsRoot());
        
        crso.next();             
        
        //Wizard proceeded to 2nd step.
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        cvss.stop();
        in.close();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        iwo.cancel();
    }
    
    public void testImportWizardExt() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":ext:test@localhost:2401/cvs");
        crso.rbUseInternalSSH().push();
        crso.setPassword("test");
        crso.cbRememberPassword().push();
        crso.cbRememberPassword().push();
        crso.rbUseExternalShell().push();
        crso.setSSHCommand("plink.exe -l user -i private_key.ppk");
        EditCVSRootOperator editOperator = crso.edit();
        assertEquals("Wrong access method in Edit CVSRoot dialog", "ext", editOperator.getAccessMethod());
        assertEquals("Wrong username Edit CVSRoot dialog", "test", editOperator.getUser());
        assertEquals("Wrong hostname in Edit CVSRoot dialog", "localhost", editOperator.getHost());
        assertEquals("Wrong port Edit CVSRoot dialog", "2401", editOperator.getPort());
        assertEquals("Wrong repository path Edit CVSRoot dialog", "/cvs", editOperator.getRepositoryPath());
        
        //change values in EditCVSRoot dialog but cancel it
        editOperator.selectAccessMethod(editOperator.ITEM_EXT);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":ext:test@localhost:2401/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.selectAccessMethod(editOperator.ITEM_EXT);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.ok();
        assertEquals("Values are not propagated correctly", ":ext:user@127.0.0.1:8080/cvs/repo", crso.getCVSRoot());
        crso.cancel();
    }
    
    public void testImportWizardLocal() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":local:/cvs");
        
        EditCVSRootOperator editOperator = crso.edit();
        assertEquals("Wrong access method in Edit CVSRoot dialog", "local", editOperator.getAccessMethod());
        assertEquals("Wrong repository path in Edit CVSRoot dialog", "/cvs", editOperator.getRepositoryPath());
        
        //change values in EditCVSRoot dialog but cancel it
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":local:/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.ok();
        assertEquals("Values are propagated, but Cancel was push", ":local:/cvs/repo", crso.getCVSRoot());
        crso.cancel();
    }
    
    public void testImportWizardFork() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":fork:/cvs");
        
        EditCVSRootOperator editOperator = crso.edit();
        assertEquals("Wrong access method in Edit CVSRoot dialog", "fork", editOperator.getAccessMethod());
        assertEquals("Wrong repository path in Edit CVSRoot dialog", "/cvs", editOperator.getRepositoryPath());
        
        //change values in EditCVSRoot dialog but cancel it
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":fork:/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.ok();
        assertEquals("Values are propagated, but Cancel was push", ":fork:/cvs/repo", crso.getCVSRoot());
        crso.cancel();
    }
    
    public void testImportWizardPserver() {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        crso.setCVSRoot(":pserver:test@localhost:2401/cvs");
        crso.setPassword("test");
        
        //crso.cbRememberPassword().setSelected(true);
        //crso.cbRememberPassword().setSelected(false);
        EditCVSRootOperator editOperator = crso.edit();
        assertEquals("Wrong access method in Edit CVSRoot dialog", "pserver", editOperator.getAccessMethod());
        assertEquals("Wrong username Edit CVSRoot dialog", "test", editOperator.getUser());
        assertEquals("Wrong hostname in Edit CVSRoot dialog", "localhost", editOperator.getHost());
        assertEquals("Wrong port Edit CVSRoot dialog", "2401", editOperator.getPort());
        assertEquals("Wrong repository path Edit CVSRoot dialog", "/cvs", editOperator.getRepositoryPath());
        
        //change values in EditCVSRoot dialog but cancel it
        editOperator.selectAccessMethod(editOperator.ITEM_PSERVER);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":pserver:test@localhost:2401/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.selectAccessMethod(editOperator.ITEM_PSERVER);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.ok();
        assertEquals("Values are not propagated correctly", ":pserver:user@127.0.0.1:8080/cvs/repo", crso.getCVSRoot());
        crso.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testImportWizardSecondStepUI() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        if (in == null) {
            System.err.println(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
            in.markSupported();
        }
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        crso.setCVSRoot(cvss.getCvsRoot());
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        crso.next();
              
        //Wizard proceeded to 2nd step.
        
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        cvss.stop();
        in.close();
        folderToImportOper.setFolderToImport(getWorkDirPath());
        JFileChooserOperator browseFolder = folderToImportOper.browseFolderToImport();
        assertEquals("Directory set in wizard not propagated to file chooser:", getWorkDir().getAbsolutePath(), browseFolder.getCurrentDirectory().getAbsolutePath()); // NOI18N
        browseFolder.cancel();
        folderToImportOper.setImportMessage("Import message"); //NOI18N
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "repository_browsing.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        String CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        folderToImportOper.setRepositoryFolder("folder");
        BrowseRepositoryFolderOperator browseRepositoryOper =  folderToImportOper.browseRepositoryFolder();
        browseRepositoryOper.selectFolder(""); // NOI18N
        browseRepositoryOper.ok();
        folderToImportOper.checkCheckoutAfterImport(false);
        iwo.cancel();
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testImportWizardFinish() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        String CVSroot;
        PseudoCvsServer cvss;
        OutputOperator oo = OutputOperator.invoke();
        TestKit.unversionProject(file, projectName);
        
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        if (in == null) {
            System.err.println(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
            in.markSupported();
        }
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        CVSroot = cvss.getCvsRoot();        
        sessionCVSroot = CVSroot;
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.setCVSRoot(CVSroot);
        crso.next();
              
        //Wizard proceeded to 2nd step.
        
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        cvss.stop();
        in.close();
        folderToImportOper.setImportMessage("initial import");
        folderToImportOper.checkCheckoutAfterImport(false);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "import_finish.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        //cvss.ignoreProbe();
        
        //crso.setCVSRoot(CVSroot);/
        folderToImportOper.finish();
        
        
        //System.out.println(CVSroot);
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Importing finished");
        cvss.stop();
        in.close();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        //TestKit.removeAllData(projectName, file);
    }
    
    public void prepareProject() throws Exception {
        TestKit.closeProject(projectName);
        file = TestKit.prepareProject("Java", "Java Application", projectName, "Main.java");
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
