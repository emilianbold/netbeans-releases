/*
 * WizardsTest.java
 *
 * Created on July 19, 2002, 8:40 AM
 */

package org.netbeans.modules.testtools.wizards;

import java.io.*;

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
        suite.addTest(new WizardsTest("tstPrepareFS"));
        suite.addTest(new WizardsTest("tstSimplePass"));
        suite.addTest(new WizardsTest("tstVerifySimplePass"));
        suite.addTest(new WizardsTest("tstAddTestType"));
        suite.addTest(new WizardsTest("tstVerifyNewTestType"));
        suite.addTest(new WizardsTest("tstNewTestSuite"));
        suite.addTest(new WizardsTest("tstRemoveFS"));
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
     */
    public void tstPrepareFS() throws Exception {
        ExplorerOperator.invoke().repositoryTab().mountLocalDirectoryAPI(getWorkDir().getParentFile().getAbsolutePath());
    }

    private boolean fail=false;
    
    private void delTree(File f) {
        File sub[] = f.listFiles();
        if (sub!=null) 
            for (int i=0; i<sub.length; delTree(sub[i++]));
        if (f.exists() && !f.delete()) {
            log("Cannot delete "+f.getAbsolutePath());
            fail=true;
        }
    }
    
    /** simple test case
     */
    public void tstRemoveFS() throws Exception {
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest").unmount();
        delTree(new File(getWorkDir().getParentFile(), "test"));
        if (fail) {
            fail=false;
            delTree(new File(getWorkDir().getParentFile(), "test"));
            if (fail) {
                fail=false;
                delTree(new File(getWorkDir().getParentFile(), "test"));
                assertTrue("Cannot delete some files, see log for details", !fail);
            }
        }
    }
                    
    /** simple test case
     *
     */
    public void tstSimplePass() {
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest").newFromTemplate("Test Tools|Test Workspace");
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
        TestSuiteTemplateStepOperator suite = new TestSuiteTemplateStepOperator();
        suite.setPackage("mypackage1/mypackage2.mypackage3");
        suite.verify();
        wizard.next();
        new TestCasesStepOperator().verify();
        wizard.finish();
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest/test/qa-functional").unmount();
    }
                    
    private void assertFile(String filePath) throws IOException {
        assertFile(filePath, false);
    }
        
    private void assertFile(String filePath, boolean filter) throws IOException {
        File f = new File(filePath);
        assertTrue(f.getName()+" not found", f.exists());
        if (filter) {
            BufferedReader in = new BufferedReader(new FileReader(f));
            f = new File(getWorkDir(), f.getName());
            PrintWriter out = new PrintWriter(new FileWriter(f));
            String line;
            while (in.ready()) {
                line = in.readLine();
                if (!line.startsWith(" * Created on") && !line.startsWith(" * @author"))
                    out.println(line);
            }
            in.close();
            out.close();
        }
        assertFile(f.getName()+" is different", f, getGoldenFile(getName()+"/"+f.getName()+".pass"), getWorkDir());
    }
    
    /** simple test case
     *
     */
    public void tstVerifySimplePass() throws IOException {
        String dir = getWorkDir().getParentFile().getAbsolutePath()+"/test";
        assertFile(dir+"/build.xml");
        assertFile(dir+"/build-qa-functional.xml");
        assertFile(dir+"/cfg-qa-functional.xml");
        assertFile(dir+"/qa-functional/src/mypackage1/mypackage2/mypackage3/simpleTest.java", true);
    }
                    
    /** simple test case
     *
     */
    public void tstAddTestType() {
        new FolderNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest|test").newFromTemplate("Test Tools|Unit Test Type");
        NewObjectNameStepOperator nameStep = new NewObjectNameStepOperator();
        nameStep.setName("mytype");
        nameStep.next();
        TestTypeSettingsStepOperator ttSet = new TestTypeSettingsStepOperator();
        ttSet.verify();
        ttSet.checkUseJemmy(true);
        assertTrue(ttSet.rbSDI().isSelected());
        ttSet.setMDI();
        ttSet.next();
        TestTypeAdvancedSettingsStepOperator ttAdv = new TestTypeAdvancedSettingsStepOperator();
        ttAdv.verify();
        ttAdv.setCMDSuffix("mysuffix");
        ttAdv.setCompClassPath("myclasspath");
        ttAdv.setCompExclude("myexclude");
        ttAdv.setExecExtraJARs("myjars");
        ttAdv.setJellyHome("myjellyhome");
        ttAdv.setJemmyHome("myjemmyhome");
        ttAdv.next();
        TestBagSettingsStepOperator tbSet = new TestBagSettingsStepOperator();
        tbSet.verify();
        tbSet.setAttributes("myattributes");
        tbSet.setExecExcludePattern("myexecexclude");
        tbSet.setExecIncludePattern("myexecinclude");
        tbSet.setName("myname");
        assertTrue(tbSet.rbCode().isSelected());
        tbSet.setIDE();
        tbSet.finish();
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest/test/mytype").unmount();
    }
    
    /** simple test case
     *
     */
    public void tstVerifyNewTestType() throws IOException {
        String dir = getWorkDir().getParentFile().getAbsolutePath()+"/test";
        assertFile(dir+"/build.xml");
        assertFile(dir+"/build-mytype.xml");
        assertFile(dir+"/cfg-mytype.xml");
    }
                    
    /** simple test case
     *
     */
    public void tstNewTestSuite() throws IOException {
        new FolderNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest|test").newFromTemplate("Test Tools|Jemmy&Jelly Test Suite");
        NewObjectNameStepOperator nameStep = new NewObjectNameStepOperator();
        nameStep.setName("myTestSuite");
        nameStep.next();
        TestCasesStepOperator step = new TestCasesStepOperator();
        step.verify();
        step.setName("myMethod1");
        step.selectTemplate("testSimpleTestCase");
        step.add();
        assertTrue(!step.btAdd().isEnabled());
        step.setName("myMethod2");
        step.selectTemplate("testGoldenTestCase");
        step.add();
        step.setName("myMethod3");
        step.add();
        step.lstTestCasesList().selectItem("myMethod3");
        assertEquals(2, step.lstTestCasesList().findItemIndex("myMethod3"));
        assertTrue(step.btUp().isEnabled());
        assertTrue(!step.btDown().isEnabled());
        assertTrue(step.btRemove().isEnabled());
        step.up();
        assertEquals(1, step.lstTestCasesList().findItemIndex("myMethod3"));
        assertTrue(step.btUp().isEnabled());
        assertTrue(step.btDown().isEnabled());
        step.down();
        assertEquals(2, step.lstTestCasesList().findItemIndex("myMethod3"));
        assertTrue(!step.btDown().isEnabled());
        assertTrue(step.btUp().isEnabled());
        step.remove();
        assertEquals(2, step.lstTestCasesList().getModel().getSize());
        step.lstTestCasesList().clearSelection();
        assertTrue(!step.btUp().isEnabled());
        assertTrue(!step.btDown().isEnabled());
        assertTrue(!step.btRemove().isEnabled());
        step.finish();
        EditorOperator editor = new EditorOperator("myTestSuite");
        editor.close(true);
        assertFile(getWorkDir().getParentFile().getAbsolutePath()+"/test/myTestSuite.java", true);
    }
    
}
