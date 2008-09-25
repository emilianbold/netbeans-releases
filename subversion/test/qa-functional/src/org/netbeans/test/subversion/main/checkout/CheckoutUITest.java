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
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
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
    
    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(CheckoutUITest.class).addTest(
//                    "testInvokeClose",
                    "testChangeAccessTypes"//,
//                    "testIncorrentUrl",
//                    "testAvailableFields",
//                    "testRepositoryFolder"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeClose() throws Exception {
        TestKit.showStatusLabels();
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        co.btCancel().pushNoBlock();
    }
    
    public void testChangeAccessTypes() throws Exception {
        TestKit.showStatusLabels();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        Thread.sleep(100);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVN);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_SVN);
        Thread.sleep(100);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVNSSH);
        Thread.sleep(100);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTP);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTP);
        Thread.sleep(100);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTPS);
        Thread.sleep(100);
        co.btCancel().pushNoBlock();
    }
    
    public void testIncorrentUrl() throws Exception {
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //wrong file
        rso.setRepositoryURL("dfile:///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: dfile:///", rso.lblWarning().getText());
        //wrong svn
        rso.setRepositoryURL("dsvn://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: dsvn://", rso.lblWarning().getText());
        //space in file
        rso.setRepositoryURL("file :///");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: file :///", rso.lblWarning().getText());
        //space in svn
        rso.setRepositoryURL("svn ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: svn ://", rso.lblWarning().getText());
        //space in http
        rso.setRepositoryURL("http ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: http ://", rso.lblWarning().getText());
        //space in https
        rso.setRepositoryURL("https ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: https ://", rso.lblWarning().getText());
        //space in svn+ssh
        rso.setRepositoryURL("svn+ssh ://");
        assertEquals("This should be wrong url string!!!", "Invalid svn url: svn+ssh ://", rso.lblWarning().getText());
        
        co.btCancel().pushNoBlock();
    }
    
    public void testAvailableFields() throws Exception {
        long timeoutCO = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeoutCO);
        }

        long timeoutDW = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", timeoutDW);
        }

        long timeoutRSO = JemmyProperties.getCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout", timeoutRSO);
        }

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
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //http
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //https
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn+ssh
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        rso.lblUseExternal();
        rso.lblTunnelCommand();
        JTextFieldOperator txt = rso.txtTunnelCommand();
        txt.typeText("plink");
        Thread.sleep(2000);

        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", timeoutDW);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeoutCO);
        JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitDialogTimeout", timeoutRSO);
        co.btCancel().pushNoBlock();
    }
    
    public void testRepositoryFolder() throws Exception {
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
        rso.btStop().push();
        assertEquals("Warning message - process was cancelled by user", "Action canceled by user", rso.lblWarning().getText());
        co.btCancel().pushNoBlock();
    }
}
