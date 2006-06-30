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

package org.netbeans.modules.testtools.wizards;

/*
 * WizardsTest.java
 *
 * Created on July 19, 2002, 8:40 AM
 */

import java.io.*;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.modules.testtools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.diff.Diff;

/** JUnit test suite with Jemmy support
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
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
    protected void setUp() throws IOException {
        JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
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
    public void tstSimplePass() throws Exception {
        new NewTemplateAction().perform();
        NewWizardOperator wizard = new NewWizardOperator();
        new ChooseTemplateStepOperator().selectTemplate("Test Tools|Test Workspace");
        wizard.next();
        new FilesystemNode(new TargetLocationStepOperator().tree(), "WizardsTest").select();
        wizard.next();
        new TestWorkspaceSettingsStepOperator().verify();
        wizard.next();
        TestTypeTemplateStepOperator template = new TestTypeTemplateStepOperator();
        template.selectTemplate("Functional");
        template.verify();
        wizard.next();
        new TestTypeSettingsStepOperator().verify();
        wizard.next();
        new TestTypeAdvancedSettingsStepOperator().verify();
        wizard.next();
        new TestBagSettingsStepOperator().verify();
        wizard.next();
        TestSuiteTemplateStepOperator suite = new TestSuiteTemplateStepOperator();
        suite.selectTemplate("Simple");
        suite.setPackage("mypackage1/mypackage2.mypackage3");
        suite.verify();
        wizard.next();
        new TestCasesStepOperator().verify();
        wizard.finish();
        Thread.sleep(2000);
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "WizardsTest/test/qa-functional").unmount();
    }
    
    private void assertXMLFile(String filePath) throws IOException {
        File f = new File(filePath);
        assertTrue(f.getName()+" not found", f.exists());
        FileInputStream in = new FileInputStream(f);
        byte data[]=new byte[in.available()];
        in.read(data);
        in.close();
        StringTokenizer file=new StringTokenizer(new String(data), "<");
        f = new File(getWorkDir(), f.getName());
        PrintWriter out = new PrintWriter(new FileWriter(f));
        String token;
        while (file.hasMoreTokens()) {
            token=file.nextToken();
            if (token.startsWith("!--")) {
                while (token.indexOf("-->")<0 && file.hasMoreTokens()) {
                    out.print("<"+token);
                    token=file.nextToken();
                }
                out.print("<"+token);
            } else {
                out.print('<');
                int i=token.indexOf('>');
                if (i>0) {
                    StringTokenizer st=new StringTokenizer(token.substring(0,i), "\n\t\r ");
                    if (st.hasMoreTokens()) out.print(st.nextToken());
                    TreeSet ts=new TreeSet();
                    while (st.hasMoreTokens()) {
                        ts.add(st.nextToken());
                    }
                    Iterator it=ts.iterator();
                    while (it.hasNext()) {
                        out.print(" "+it.next());
                    }
                }
                out.print('>');
                out.print(token.substring(i+1)); 
            }
        }       
        out.close();
        assertFile(f.getName()+" is different", f, getGoldenFile(getName()+"/"+f.getName()+".pass"), getWorkDir());
    }
        
    private void assertFilterFile(String filePath) throws IOException {
        File f = new File(filePath);
        assertTrue(f.getName()+" not found", f.exists());
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
        assertFile(f.getName()+" is different", f, getGoldenFile(getName()+"/"+f.getName()+".pass"), getWorkDir());
    }
    
    /** simple test case
     *
     */
    public void tstVerifySimplePass() throws IOException {
        String dir = getWorkDir().getParentFile().getAbsolutePath()+"/test";
        assertXMLFile(dir+"/build.xml");
        assertXMLFile(dir+"/build-qa-functional.xml");
        assertXMLFile(dir+"/cfg-qa-functional.xml");
        assertFilterFile(dir+"/qa-functional/src/mypackage1/mypackage2/mypackage3/simpleTest.java");
    }
                    
    /** simple test case
     *
     */
    public void tstAddTestType() {
        new NewTemplateAction().perform();
        NewWizardOperator wizard = new NewWizardOperator();
        new ChooseTemplateStepOperator().selectTemplate("Test Tools|Unit Test Type");
        wizard.next();
        TargetLocationStepOperator nameStep = new TargetLocationStepOperator();
        new FolderNode(nameStep.tree(), "WizardsTest|test").select();
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
        assertXMLFile(dir+"/build.xml");
        assertXMLFile(dir+"/build-mytype.xml");
        assertXMLFile(dir+"/cfg-mytype.xml");
    }
                    
    /** simple test case
     *
     */
    public void tstNewTestSuite() throws IOException {
        new NewTemplateAction().perform();
        NewWizardOperator wizard = new NewWizardOperator();
        new ChooseTemplateStepOperator().selectTemplate("Test Tools|Jemmy&Jelly Test Suite");
        wizard.next();
        TargetLocationStepOperator nameStep = new TargetLocationStepOperator();
        new FolderNode(nameStep.tree(), "WizardsTest|test").select();
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
        assertFilterFile(getWorkDir().getParentFile().getAbsolutePath()+"/test/myTestSuite.java");
        assertTrue("golden file missing", new File(getWorkDir().getParentFile().getAbsolutePath()+"/test/data/goldenfiles/myTestSuite/myMethod2.pass").exists());
    }
    
}
