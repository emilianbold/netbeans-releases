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
package org.netbeans.jellytools.modules.javacvs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator,
 * BrowseCVSModuleOperator, BrowseTagsOperator, CVSRootStepOperator, EditCVSRootOperator, 
 * ModuleToCheckoutStepOperator, ProxyConfigurationOperator.
 * 
 * 
 * @author Jiri.Skrivanek@sun.com
 */ 
public class CheckoutWizardOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CheckoutWizardOperatorTest("testInvoke"));
        suite.addTest(new CheckoutWizardOperatorTest("testEditCVSRoot"));
        suite.addTest(new CheckoutWizardOperatorTest("testProxy"));
        suite.addTest(new CheckoutWizardOperatorTest("testModuleToCheckout"));
        suite.addTest(new CheckoutWizardOperatorTest("testFinish"));
        return suite;
    }
    
    /** Set up executed before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public CheckoutWizardOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method.*/
    public void testInvoke() {
        CheckoutWizardOperator.invoke();
    }

    /** Tests CVS root customizer. */
    public void testEditCVSRoot() {
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        cvsRootOper.setPassword("password");  // NOI18N
        cvsRootOper.setCVSRoot(":local:repository"); // NOI18N
        EditCVSRootOperator editOper = cvsRootOper.edit(); // NOI18N
        assertEquals("Wrong access method in Edit CVS Root dialog:", "local", editOper.getAccessMethod()); // NOI18N
        assertEquals("Wrong repository path in Edit CVS Root dialog:", "repository", editOper.getRepositoryPath()); // NOI18N
        
        editOper.selectAccessMethod(editOper.ITEM_PSERVER);
        editOper.setUser("user");// NOI18N
        editOper.setHost("host");// NOI18N
        editOper.setRepositoryPath("repository");// NOI18N
        editOper.setPort("8080");
        editOper.ok();
        String expected = ":pserver:user@host:8080repository";// NOI18N
        assertEquals("Values set in Edit CVS Root dialog not propagated:", expected, cvsRootOper.getCVSRoot());// NOI18N
    }

    /** Tests proxy customizer. */
    public void testProxy() {
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        OptionsOperator proxyOper = cvsRootOper.proxyConfiguration();
        proxyOper.close();
    }
    
    /** Test Module to Checkout panel of wizard.  */
    public void testModuleToCheckout() throws Exception {
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        InputStream in = new ByteArrayInputStream(
                new byte[] {'I', ' ', 'L', 'O', 'V', 'E', ' ', 'Y', 'O', 'U', '\n'});
        PseudoCvsServer cvss;
        try {
            cvss = new PseudoCvsServer(in);
        } catch (IOException ioe) {
            throw new JemmyException("Error initializing PseudoCvsServer: "+ioe); // NOI18N
        }
        cvss.ignoreProbe();
        new Thread(cvss).start();
        cvsRootOper.setCVSRoot(cvss.getCvsRoot());
        cvsRootOper.next();
        ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
        moduleOper.setModule("module"); // NOI18N
        BrowseCVSModuleOperator browseModuleOper = moduleOper.browseModule();
        browseModuleOper.selectModule("/cvs"); // NOI18N
        browseModuleOper.ok();
        moduleOper.setBranch("branch"); // NOI18N
        /* It throws connection exception. It is tested in javacvs module.
        BrowseTagsOperator browseTagsOper = moduleOper.browseBranch();
        browseTagsOper.selectPath("HEAD"); // NOI18N
        browseTagsOper.ok();
        assertEquals("Branch set in Browse Tags dialog not propagated:", "HEAD", moduleOper.getBranch()); // NOI18N
         */
        moduleOper.setLocalFolder(getWorkDirPath()); // NOI18N
        JFileChooserOperator browseFolder = moduleOper.browseLocalFolder();
        assertEquals("Directory set in wizard not propagated to file chooser:", getWorkDir().getAbsolutePath(), browseFolder.getCurrentDirectory().getAbsolutePath()); // NOI18N
        browseFolder.cancel();
        cvss.stop();
    }

    /** Test finish wizard. */
    public void testFinish() {
        // Cancel wizard (finish is possible but causes an error because 
        // of use of pseudo pserver.
        new CheckoutWizardOperator().cancel();
    }
}
