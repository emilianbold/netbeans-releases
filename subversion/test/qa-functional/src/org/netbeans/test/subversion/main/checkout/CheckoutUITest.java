/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * CheckoutUITest.java
 *
 * Created on 19 April 2006, 13:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.subversion.main.checkout;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CheckoutUITest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "SVNApplication";
    public File projectPath;
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of CheckoutUITest */
    public CheckoutUITest(String name) {
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
        suite.addTest(new CheckoutUITest("testInvokeClose"));
        suite.addTest(new CheckoutUITest("testChangeAccessTypes"));
        suite.addTest(new CheckoutUITest("testIncorrentUrl"));        
        suite.addTest(new CheckoutUITest("testAvailableFields"));
        suite.addTest(new CheckoutUITest("testRepositoryFolder"));        
        return suite;
    }
    
    public void testInvokeClose() throws Exception {
        TestKit.showStatusLabels();
        OutputOperator oo = OutputOperator.invoke();
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();   
        co.btCancel().pushNoBlock();
    }
    
    public void testChangeAccessTypes() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        Thread.sleep(1000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVN);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_SVN);
        Thread.sleep(1000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVNSSH);
        //rso.txtPassword().setText(RepositoryStepOperator.ITEM_SVNSSH);
        Thread.sleep(1000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTP);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTP);
        Thread.sleep(1000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTPS);
        Thread.sleep(1000);
        co.btCancel().pushNoBlock();
    }
    
    public void testIncorrentUrl() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //wrong file
        rso.setRepositoryURL("dfile:///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :dfile:///", rso.lblWarning().getText());
        //wrong svn
        rso.setRepositoryURL("dsvn://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :dsvn://", rso.lblWarning().getText());
        //space in file
        rso.setRepositoryURL("file :///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :file :///", rso.lblWarning().getText());
        //space in svn
        rso.setRepositoryURL("svn ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :svn ://", rso.lblWarning().getText());
        //space in http
        rso.setRepositoryURL("http ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :http ://", rso.lblWarning().getText());
        //space in https
        rso.setRepositoryURL("https ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :https ://", rso.lblWarning().getText());
        //space in svn+ssh
        rso.setRepositoryURL("svn+ssh ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :svn+ssh ://", rso.lblWarning().getText());
        
        co.btCancel().pushNoBlock();
    }
    
    public void testAvailableFields() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //file
        rso.selectRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        TimeoutExpiredException tee = null;
        try {
            JLabelOperator lbl = rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //http
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        JLabelOperator lblU = rso.lblUser();
        JLabelOperator lblP = rso.lblPassword();
        JButtonOperator btnProxy = rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //https
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        lblU = rso.lblUser();
        lblP = rso.lblPassword();
        btnProxy = rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        lblU = rso.lblUser();
        lblP = rso.lblPassword();
        btnProxy = rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn+ssh
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        lblU = rso.lblUseExternal();
        lblU = rso.lblTunnelCommand();
        JTextFieldOperator txt = rso.txtTunnelCommand();
        txt.typeText("plink");
        Thread.sleep(2000);
        //lblU = rso.lblUser();
        //lblP = rso.lblPassword();
        //btnProxy = rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        co.btCancel().pushNoBlock();
    }
    
    public void testRepositoryFolder() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);    
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
    
        //create repository... 
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        //next step
        rso.next();
        Thread.sleep(2000);
        
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.verify();
        RepositoryBrowserOperator rbo = wdso.browseRepository();
        rbo.verify();
        //Try to select folders
        rbo.selectFolder("branches");
        rbo.selectFolder("tags");
        rbo.selectFolder("trunk");
        rbo.selectFolder("trunk|JavaApp|src|javaapp");
        rbo.ok();
        
        assertEquals("Wrong folder selection!!!", "trunk/JavaApp/src/javaapp", wdso.getRepositoryFolder());
        rbo = wdso.browseRepository();
        rbo.selectFolder("trunk|JavaApp");
        rbo.ok();
        assertEquals("Wrong folder selection!!!", "trunk/JavaApp", wdso.getRepositoryFolder());
        wdso.setLocalFolder("/tmp");
        JFileChooserOperator jfc = wdso.browseLocalFolder();
        assertEquals("Directory set in wizard not propagated to file chooser:", true, jfc.getCurrentDirectory().getAbsolutePath().endsWith("tmp"));
        jfc.cancel();
        wdso.setRepositoryRevision("10");
        wdso.checkCheckoutContentOnly(true);
        co.btCancel().pushNoBlock();
    }
    
    public void testStopProcess() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);    
        
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
    
        //create repository... 
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);   
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        //next step
        rso.next();
        //Thread.sleep(2000);
        rso.btStop().push();
        assertEquals("Warning message - process was cancelled by user", "Action canceled by user", rso.lblWarning().getText());
        co.btCancel().pushNoBlock();
    }
}
