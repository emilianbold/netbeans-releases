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
import java.util.Random;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.BrowseCVSModuleOperator;
import org.netbeans.jellytools.modules.javacvs.BrowseTagsOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.EditCVSRootOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
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
import org.netbeans.junit.ide.ProjectSupport;
/**
 *
 * @author peter
 */
public class CheckOutWizardTest extends JellyTestCase {
    
    String os_name;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator; 
    
    /**
     * Creates a new instance of CheckOutWizardTest
     */
    public CheckOutWizardTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CheckOutWizardTest.class).addTest(
                     "testInvokeCheckoutWizard",
                     "testCancelCheckoutWizard",
                     "testCheckoutWizardLocal",
                     "testCheckoutWizardFork",
                     "testCheckoutWizardPserver",
                     "testCheckoutWizardExt",
                     "testRandomChange",
                     "testLocalUI",
                     "testForkUI",
                     "testPserverUI",
                     "testExtUI",
                     "testEditCVSRootDialogUI",
                     "testPserverLoginSuccess",
                     "testCheckWizardSecondStepUI",
                     "testPserverLoginFailed",
                     "testRepositoryBrowsing",
                     "testAliasBrowsing",
                     "testBranchBrowsing",
                     "testTagBrowsing",
                     "testCheckWizardFinish"                     
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
    
    public void testInvokeCheckoutWizard() {
        new ProjectsTabOperator().tree().clearSelection();
        CheckoutWizardOperator.invoke();
    }
    
    public void testCancelCheckoutWizard() {
        new ProjectsTabOperator().tree().clearSelection();
        new CheckoutWizardOperator().cancel();
    }
    
    public void testCheckoutWizardLocal() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
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
    
    public void testCheckoutWizardFork() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
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
    
    public void testCheckoutWizardPserver() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
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
    }
    
    public void testCheckoutWizardExt() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator.invoke();
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
    
    public void testRandomChange() {
        new ProjectsTabOperator().tree().clearSelection();
        Random rand = new Random();
        int am;
        String[] cvsRoots = new String[] {":local:/cvs", ":fork:/cvs", ":pserver:test@localhost:2401/cvs", ":ext:test@localhost:2401/cvs"};
        String[] accessMethods = new String[] {"local", "fork", "pserver", "ext"};
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        EditCVSRootOperator ecro;
        for (int i = 0; i < 10; i++) {
            int index = rand.nextInt(4);
            switch (index) {
                case 0: //local access method
                    crso.setCVSRoot(cvsRoots[0]);
                    ecro = crso.edit();
                    am = rand.nextInt(4);
                    ecro.selectAccessMethod(accessMethods[am]);
                    assertEquals("Wrong access method", accessMethods[am], ecro.getAccessMethod());
                    ecro.cancel();
                    break;
                case 1: //fork access method
                    crso.setCVSRoot(cvsRoots[1]);
                    ecro = crso.edit();
                    am = rand.nextInt(4);
                    ecro.selectAccessMethod(accessMethods[am]);
                    assertEquals("Wrong access method", accessMethods[am], ecro.getAccessMethod());
                    ecro.cancel();
                    break;
                case 2: //pserver access method
                    crso.setCVSRoot(cvsRoots[2]);
                    ecro = crso.edit();
                    am = rand.nextInt(4);
                    ecro.selectAccessMethod(accessMethods[am]);
                    assertEquals("Wrong access method", accessMethods[am], ecro.getAccessMethod());
                    ecro.cancel();
                    break;
                case 3: //ext access method
                    crso.setCVSRoot(cvsRoots[3]);
                    ecro = crso.edit();
                    am = rand.nextInt(4);
                    ecro.selectAccessMethod(accessMethods[am]);
                    assertEquals("Wrong access method", accessMethods[am], ecro.getAccessMethod());
                    ecro.cancel();
                    break;
            }
        }
        cwo.cancel();
    }
    
    public void testPserverUI() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        //Invalid CVS Root
        crso.setCVSRoot(":pserver:test");
        try {
            new JLabelOperator(crso, "Invalid CVS Root");
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
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        //End UI test
        cwo.cancel();
    }
    
    public void testLocalUI() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        //Invalid CVS Root
        crso.setCVSRoot(":loca:");
        try {
            new JLabelOperator(crso, "Only :pserver:, :local:, :ext: and :fork: connection methods supported");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        crso.setCVSRoot(":local:/cvs");
        //start test UI
        //combobox
        try {
            new JComboBoxOperator(crso);
            //JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
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
        
        cwo.cancel();
    }
    
    
    public void testForkUI() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        //Invalid CVS Root
        crso.setCVSRoot(":for:");
        try {
            new JLabelOperator(crso, "Only :pserver:, :local:, :ext: and :fork: connection methods supported");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        crso.setCVSRoot(":fork:/cvs");
        //start test UI
        
        try {
            new JComboBoxOperator(crso);
            //JPasswordFieldOperator passwd = new JPasswordFieldOperator(crso);
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
        
        cwo.cancel();
    }
    
    public void testExtUI() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
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
        
        cwo.cancel();
    }
    
    public void testEditCVSRootDialogUI() {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        EditCVSRootOperator ecro = crso.edit();
        try {
            JComboBoxOperator combo = new JComboBoxOperator(ecro);
            combo.selectItem("local");
            combo.selectItem("fork");
            combo.selectItem("ext");
            combo.selectItem("pserver");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        
        //User texfield
        try {
            JTextFieldOperator user = new JTextFieldOperator(ecro, 0);
            user.getFocus();
            JTextFieldOperator host = new JTextFieldOperator(ecro, 1);
            host.getFocus();
            JTextFieldOperator repository = new JTextFieldOperator(ecro, 2);
            repository.getFocus();
            JTextFieldOperator port = new JTextFieldOperator(ecro, 3);
            port.getFocus();
            new JButtonOperator(ecro, "OK");
            new JButtonOperator(ecro, "Cancel");
            new JButtonOperator(ecro, "Help");
        } catch (TimeoutExpiredException e) {
            throw e;
        }

        ecro.cancel();
        cwo.cancel();
    }
    
    /** Test login for Pserver */
    public void testPserverLoginFailed() throws Exception{
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:test@localhost:/cvs");
        
        //prepare stream for successful authen//tification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "nonauthorized.in");
        if (in == null) {
            System.err.println(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
            in.markSupported();
        }
        
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        crso.setCVSRoot(cvss.getCvsRoot());
           
        crso.next();
        
        try {
            new JLabelOperator(crso, "Please check username, password and repository.");
        } catch (TimeoutExpiredException e) {
            throw e;
        }
        cvss.stop();
        in.close();
        cwo.cancel();
    }
    
    public void testPserverLoginSuccess() throws Exception{
        //invoke CVSCheckoutWizard
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        
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
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        BrowseCVSModuleOperator bcmo = moduleCheck.browseModule();
        bcmo.cancel();
        cwo.cancel();
    }
    
    
    public void testRepositoryBrowsing() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        String CVSroot = "";
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        crso.setPassword("");
        
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
              
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "repository_browsing.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        BrowseCVSModuleOperator browseCVSModule = moduleCheck.browseModule();
        browseCVSModule.selectModule("/cvs|CVSROOT");
        browseCVSModule.selectModule("/cvs|ForImport");
        browseCVSModule.ok();
        assertEquals("Folder in repository was not found", "ForImport", moduleCheck.getModule());
        
        cvss.stop();
        in.close();
        cwo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testAliasBrowsing() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        String CVSroot = "";
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        crso.setPassword("");
       
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
        
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "alias_browsing.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        BrowseCVSModuleOperator browseCVSModule = moduleCheck.browseModule();
        browseCVSModule.selectModule("Alias|ForImport");
        browseCVSModule.ok();
        assertEquals("Alias was not found", "ForImport", moduleCheck.getModule());
        
        cvss.stop();
        in.close();
        cwo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testBranchBrowsing() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        String CVSroot;
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        crso.setPassword("");
        
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
              
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        
        moduleCheck.setModule("ForImport");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "branch_check_browsing_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot() + ",";
        
        InputStream in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "branch_browsing.in");
        PseudoCvsServer cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        CVSroot = CVSroot + cvss2.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        BrowseTagsOperator browseTags = moduleCheck.browseBranch();
        browseTags.selectBranch("MyBranch"); 
        browseTags.ok();
        assertEquals("Branch was not found", "MyBranch", moduleCheck.getBranch());
        cvss.stop();
        in.close();
        cvss2.stop();
        in2.close();
        
        cwo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testTagBrowsing() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        String CVSroot;
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        crso.setPassword("");
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
        
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        
        moduleCheck.setModule("ForImport");
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "branch_check_browsing_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot() + ",";
        
        InputStream in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "branch_browsing.in");
        PseudoCvsServer cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        CVSroot = CVSroot + cvss2.getCvsRoot();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        
        BrowseTagsOperator browseTags = moduleCheck.browseBranch();
        
        browseTags.selectTag("MyBranch_root"); 
        
        try {
            new JButtonOperator(browseTags, "OK");
            new JButtonOperator(browseTags, "Help");
            new JButtonOperator(browseTags, "Cancel");
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        //
        
        browseTags.ok();
        assertEquals("Branch was not found", "MyBranch_root", moduleCheck.getBranch());
        cvss.stop();
        in.close();
        cvss2.stop();
        in2.close();
        
        cwo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCheckWizardSecondStepUI() throws Exception {
        new ProjectsTabOperator().tree().clearSelection();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        final CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        crso.setPassword("");
        
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
              
        //second step of checkoutwizard
        //2nd step of CheckOutWizard
        File file = new File("/tmp"); // NOI18N
        file.mkdir();
        file.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        in.close();
        BrowseCVSModuleOperator browseCVSModule = moduleCheck.browseModule();
        //Aliases node
        browseCVSModule.selectModule("Aliases");
        //Repository node
        browseCVSModule.selectModule("/cvs");
        //
        //buttons in browse module
        try {
            new JButtonOperator(browseCVSModule, "Cancel");
            new JButtonOperator(browseCVSModule, "Help");
            new JButtonOperator(browseCVSModule, "Ok").push();
        } catch(TimeoutExpiredException e) {
            throw e;
        }
        
        moduleCheck.setLocalFolder("/tmp"); // NOI18N
        JFileChooserOperator browseFolder = moduleCheck.browseLocalFolder();
        assertEquals("Directory set in wizard not propagated to file chooser:", true, browseFolder.getCurrentDirectory().getAbsolutePath().endsWith("tmp")); // NOI18N
        browseFolder.cancel();
        //
        //Browse 1
        try {
            new JButtonOperator(crso, 0);
            new JButtonOperator(crso, 1);
            new JButtonOperator(crso, 2);
            new JTextFieldOperator(crso, 0);
            new JTextFieldOperator(crso, 1);
            new JTextFieldOperator(crso, 2);
            new JButtonOperator(crso, "< Back");
            new JButtonOperator(crso, "Next >");
            new JButtonOperator(crso, "Finish");
            new JButtonOperator(crso, "Cancel");
            new JButtonOperator(crso, "Help");
        } catch (TimeoutExpiredException ex) {
        throw ex;
        }        
        cwo.cancel();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCheckWizardFinish() throws Exception {
        try {
            TestKit.closeProject(projectName);
            new ProjectsTabOperator().tree().clearSelection();
            String sessionCVSroot;
            OutputOperator.invoke();
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            CVSRootStepOperator crso = new CVSRootStepOperator();
        
            crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");

            //prepare stream for successful authentification and run PseudoCVSServer
            InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
            if (in == null) {
                System.err.println(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm());
                in.markSupported();
            }
        
            PseudoCvsServer cvss = new PseudoCvsServer(in);
            new Thread(cvss).start();
            cvss.ignoreProbe();
            String CVSroot = cvss.getCvsRoot();
            crso.setCVSRoot(CVSroot);
            sessionCVSroot = CVSroot;
            System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
            crso.next();
              
            //second step of checkoutwizard
            //2nd step of CheckOutWizard
            File tmp = new File("/tmp"); // NOI18N
            File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
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
            Thread.sleep(3000);
        
        
            OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("Checking out finished");
            cvss.stop();
            in.close();
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
        
            ProjectSupport.waitScanFinished();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(projectName);
            System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        }
    }
}
