/*
 * WizardsTest.java
 *
 * Created on July 19, 2002, 8:40 AM
 */

package org.netbeans.modules.testtools.wizards;

import java.io.PrintStream;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.testtools.*;
import org.netbeans.jellytools.nodes.*;

/** JUnit test suite with Jemmy support
 *
 * @author as103278
 * @version 1.0
 */
public class WizardsTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public WizardsTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new WizardsTest("testSimplePass"));
        return suite;
    }
    
    
    /** method called before each testcase
     */
    protected void setUp() {
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** simple test case
     *
     */
    public void testSimplePass() {
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "src").newFromTemplate("Test Tools|Test Workspace");
        NewWizardOperator wizard = new NewWizardOperator();
        wizard.next();
        new TestWorkspaceSettingsStepOperator().verify();
        wizard.next();
        new TestTypeTemplateStepOperator().verify();
        wizard.next();
        new TestTypeSettingsStepOperator().verify();
        wizard.next();
        new TestTypeAdvancedSettingsStepOperator().verify();
        wizard.next();
        new TestBagSettingsStepOperator().verify();
        wizard.next();
        new TestSuiteTemplateStepOperator().verify();
        wizard.next();
        new TestCasesStepOperator().verify();
        wizard.cancel();
    }
    
}
