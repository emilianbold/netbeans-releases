/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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

import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;

/**
 *
 * @author peter
 */
public class CheckoutUITest extends JellyTestCase{
    String os_name;
    
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
        
        return suite;
    }
    
    public void testInvokeClose() throws Exception {
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();   
        co.btCancel().pushNoBlock();
    }
    
    public void testChangeAccessTypes() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        Thread.sleep(1000);
        //
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        co1so.txtUser().setText(RepositoryStepOperator.ITEM_SVN);
        co1so.txtPassword().setText(RepositoryStepOperator.ITEM_SVN);
        Thread.sleep(1000);
        //
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        co1so.txtUser().setText(RepositoryStepOperator.ITEM_SVNSSH);
        co1so.txtPassword().setText(RepositoryStepOperator.ITEM_SVNSSH);
        Thread.sleep(1000);
        //
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        co1so.txtUser().setText(RepositoryStepOperator.ITEM_HTTP);
        co1so.txtPassword().setText(RepositoryStepOperator.ITEM_HTTP);
        Thread.sleep(1000);
        //
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        co1so.txtUser().setText(RepositoryStepOperator.ITEM_HTTPS);
        co1so.txtPassword().setText(RepositoryStepOperator.ITEM_HTTPS);
        Thread.sleep(1000);
        co.btCancel().pushNoBlock();
    }
    
    public void testIncorrentUrl() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        //wrong file
        co1so.setRepositoryURL("dfile:///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :dfile:///", co1so.lblWarning().getText());
        //wrong svn
        co1so.setRepositoryURL("dsvn://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :dsvn://", co1so.lblWarning().getText());
        //space in file
        co1so.setRepositoryURL("file :///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :file :///", co1so.lblWarning().getText());
        //space in svn
        co1so.setRepositoryURL("svn ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :svn ://", co1so.lblWarning().getText());
        //space in http
        co1so.setRepositoryURL("http ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :http ://", co1so.lblWarning().getText());
        //space in https
        co1so.setRepositoryURL("https ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :https ://", co1so.lblWarning().getText());
        //space in svn+ssh
        co1so.setRepositoryURL("svn+ssh ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url :svn+ssh ://", co1so.lblWarning().getText());
        
        co.btCancel().pushNoBlock();
    }
    
    public void testAvailableFields() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        
        //file
        co1so.selectRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        TimeoutExpiredException tee = null;
        try {
            JLabelOperator lbl = co1so.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //http
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        JLabelOperator lblU = co1so.lblUser();
        JLabelOperator lblP = co1so.lblPassword();
        JButtonOperator btnProxy = co1so.btProxyConfiguration();
        
        //file
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //https
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        lblU = co1so.lblUser();
        lblP = co1so.lblPassword();
        btnProxy = co1so.btProxyConfiguration();
        
        //file
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        lblU = co1so.lblUser();
        lblP = co1so.lblPassword();
        btnProxy = co1so.btProxyConfiguration();
        
        //file
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn+ssh
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        lblU = co1so.lblUser();
        lblP = co1so.lblPassword();
        btnProxy = co1so.btProxyConfiguration();
        
        //file
        co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            JLabelOperator lbl = co1so.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        co.btCancel().pushNoBlock();
    }
}
