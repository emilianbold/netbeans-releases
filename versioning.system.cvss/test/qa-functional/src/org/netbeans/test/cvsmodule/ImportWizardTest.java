/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.InputStream;
import junit.framework.Test;
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
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
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
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ImportWizardTest.class).addTest(
                    "prepareProject",
                    "testImportWizardPserverUI",
                    "testImportWizardLocalUI",
                    "testImportWizardForkUI",
                    "testImportWizardExtUI",
                    "testImportWizardExt",
                    "testImportWizardLocal",
                    "testImportWizardFork",
                    "testImportWizardPserver",
                    "testImportWizardLoginSuccess",
                    "testImportWizardSecondStepUI",
                    "testImportWizardFinish",
                    "removeAllData"
                )
                .enableModules(".*")
                .clusters(".*")
        );
     }
    
    @Override
    protected void setUp() throws Exception {
        
        os_name = System.getProperty("os.name");
        System.out.println("### "+getName()+" ###");
        try {
            TestKit.extractProtocol(getDataDir());
        } catch (Exception e ) {
            e.printStackTrace();
        }
        
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
            new JComboBoxOperator(crso);
            new JPasswordFieldOperator(crso);
            new JButtonOperator(crso, "Edit...");
            new JButtonOperator(crso, "Proxy Configuration...");
            new JButtonOperator(crso, "< Back");
            new JButtonOperator(crso, "Next >");
            new JButtonOperator(crso, "Finish");
            new JButtonOperator(crso, "Cancel");
            new JButtonOperator(crso, "Help");
        }  catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        iwo.cancel();
    }
    
    public void testImportWizardLocalUI() {
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
            new JButtonOperator(crso, "Edit...");
            new JButtonOperator(crso, "< Back");
            new JButtonOperator(crso, "Next >");
            new JButtonOperator(crso, "Finish");
            new JButtonOperator(crso, "Cancel");
            new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        
        iwo.cancel();
    }
    
    public void testImportWizardForkUI() {
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
            new JComboBoxOperator(crso);
            new JButtonOperator(crso, "Edit...");
            new JButtonOperator(crso, "< Back");
            new JButtonOperator(crso, "Next >");
            new JButtonOperator(crso, "Finish");
            new JButtonOperator(crso, "Cancel");
            new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        
        iwo.cancel();
    }
    
    public void testImportWizardExtUI() {
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
            new JLabelOperator(crso, "Invalid CVS Root");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        crso.setCVSRoot(":ext:test@localhost:2401/cvs");
        //start test UI
        try {
            new JComboBoxOperator(crso);
            new JPasswordFieldOperator(crso);
            new JButtonOperator(crso, "Edit...");
            new JButtonOperator(crso, "Proxy Configuration...");
            new JRadioButtonOperator(crso, "Use Internal SSH");
            new JRadioButtonOperator(crso, "Use External Shell");
            new JCheckBoxOperator(crso, "Remember Password");
            new JTextFieldOperator(crso);
            new JButtonOperator(crso, "< Back");
            new JButtonOperator(crso, "Next >");
            new JButtonOperator(crso, "Finish");
            new JButtonOperator(crso, "Cancel");
            new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //end test UI
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        iwo.cancel();
    }
    
    public void testImportWizardLoginSuccess() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        
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
        editOperator.selectAccessMethod(EditCVSRootOperator.ITEM_EXT);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":ext:test@localhost:2401/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.selectAccessMethod(EditCVSRootOperator.ITEM_EXT);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.ok();
        assertEquals("Values are not propagated correctly", ":ext:user@127.0.0.1:8080/cvs/repo", crso.getCVSRoot());
        crso.cancel();
    }
    
    public void testImportWizardLocal() {
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
        
        EditCVSRootOperator editOperator = crso.edit();
        assertEquals("Wrong access method in Edit CVSRoot dialog", "pserver", editOperator.getAccessMethod());
        assertEquals("Wrong username Edit CVSRoot dialog", "test", editOperator.getUser());
        assertEquals("Wrong hostname in Edit CVSRoot dialog", "localhost", editOperator.getHost());
        assertEquals("Wrong port Edit CVSRoot dialog", "2401", editOperator.getPort());
        assertEquals("Wrong repository path Edit CVSRoot dialog", "/cvs", editOperator.getRepositoryPath());
        
        //change values in EditCVSRoot dialog but cancel it
        editOperator.selectAccessMethod(EditCVSRootOperator.ITEM_PSERVER);
        editOperator.setRepositoryPath("/cvs/repo");
        editOperator.setHost("127.0.0.1");
        editOperator.setUser("user");
        editOperator.setPort("8080");
        editOperator.cancel();
        assertEquals("Values are propagated, but Cancel was push", ":pserver:test@localhost:2401/cvs", crso.getCVSRoot());
        
        //change values in EditCVSRoot dialog
        editOperator = crso.edit();
        editOperator.selectAccessMethod(EditCVSRootOperator.ITEM_PSERVER);
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
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator iwo = ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        
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
        assertEquals("Directory set in wizard not propagated to file chooser:", getWorkDir().getAbsolutePath().toLowerCase(), browseFolder.getCurrentDirectory().getAbsolutePath().toLowerCase()); // NOI18N
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
        String CVSroot;
        PseudoCvsServer cvss;
        OutputOperator.invoke();
        TestKit.unversionProject(file, projectName);
        
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        Operator.setDefaultStringComparator(comOperator);
        ImportWizardOperator.invoke(node);
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
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
        folderToImportOper.finish();
        
        
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Importing finished");
        cvss.stop();
        in.close();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
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
