/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.testtools;

/*
 * DataObjectTests.java
 *
 * Created on July 23, 2002, 2:01 PM
 */
import java.beans.BeanInfo;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.StringTokenizer;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.testtools.nodes.XTestBuildScriptNode;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jellytools.properties.editors.ServiceTypeCustomEditorOperator;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/** JUnit test suite with Jemmy support
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class DataObjectTests extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DataObjectTests(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new DataObjectTests("testVerifyPopup"));
        suite.addTest(new DataObjectTests("testCompilerSettings"));
        suite.addTest(new DataObjectTests("testExecutorSettings"));
        suite.addTest(new DataObjectTests("testCompile"));
        suite.addTest(new DataObjectTests("testExecute"));
        suite.addTest(new DataObjectTests("testClean"));
        suite.addTest(new DataObjectTests("testCleanResults"));
        suite.addTest(new DataObjectTests("testIcon"));
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
    public void testVerifyPopup() {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").verifyPopup();
    }
    
    /** simple test case
     *
     */
    public void testCompilerSettings() {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").properties();
        PropertySheetOperator ps = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, "build");
        PropertySheetTabOperator tab = ps.getPropertySheetTabOperator("Execution");
        ServiceTypeCustomEditorOperator customizer=new ServiceTypeProperty(tab, "Compiler").invokeCustomizer();
        assertEquals("XTest Compilation", customizer.getServiceTypeValue());
        new StringProperty(customizer.propertySheet(), "Jemmy Home").setValue("/jemmyyhome");
        new StringProperty(customizer.propertySheet(), "Jelly Home").setValue("/jellyhome");
        new StringProperty(customizer.propertySheet(), "XTest Home").setValue("/xtesthome");
        new StringProperty(customizer.propertySheet(), "Netbeans Home").setValue("/jellyhome");
        new StringProperty(customizer.propertySheet(), "Test Type").setValue("testtype");
        customizer.ok();
        ps.close();
    }
    
    /** simple test case
     *
     */
    public void testExecutorSettings() {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").properties();
        PropertySheetOperator ps = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, "build");
        PropertySheetTabOperator tab = ps.getPropertySheetTabOperator("Execution");
        ServiceTypeCustomEditorOperator customizer=new ServiceTypeProperty(tab, "Executor").invokeCustomizer();
        assertEquals("XTest Execution", customizer.getServiceTypeValue());
        new StringProperty(customizer.propertySheet(), "Jemmy Home").setValue("/jemmyyhome");
        new StringProperty(customizer.propertySheet(), "Jelly Home").setValue("/jellyhome");
        new StringProperty(customizer.propertySheet(), "XTest Home").setValue("/xtesthome");
        new StringProperty(customizer.propertySheet(), "Netbeans Home").setValue("/jellyhome");
        new StringProperty(customizer.propertySheet(), "Test Type").setValue("testtype");
        new StringProperty(customizer.propertySheet(), "Attributes").setValue("attributes");
        customizer.ok();
        ps.close();
    }
    
    /** test case with golden file
     *
     */
    public void testCompile() throws Exception {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").compile();
        MainWindowOperator.getDefault().waitStatusText("Finished build.");
        Thread.sleep(2000);
        OutputWindowOperator out = new OutputWindowOperator();
        out.selectPage("XTest Test Script (buildtests)");
        StringTokenizer st=new StringTokenizer(out.getText(), "\n");
        assertTrue("Too short output", st.countTokens()>=10);
        assertEquals("echo:", st.nextToken());
        String s = st.nextToken();
        assertTrue(s, s.startsWith("netbeans.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("xtest.home="));
        assertTrue(s, s.endsWith("xtesthome"));
        assertEquals("xtest.testtype=testtype", st.nextToken());
        assertEquals("xtest.attribs=", st.nextToken());
        s = st.nextToken();
        assertTrue(s, s.startsWith("jemmy.home="));
        assertTrue(s, s.endsWith("jemmyyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("jelly.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        assertEquals("buildtests:", st.nextToken());
        assertEquals("BUILD SUCCESSFUL", st.nextToken());
    }
    
    /** test case with golden file
     *
     */
    public void testExecute() throws Exception {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").execute();
        MainWindowOperator.getDefault().waitStatusText("Finished Ant target(s).");
        Thread.sleep(2000);
        OutputWindowOperator out = new OutputWindowOperator();
        out.selectPage("XTest Test Script (all)");
        StringTokenizer st=new StringTokenizer(out.getText(), "\n");
        assertTrue("Too short output", st.countTokens()>=10);
        assertEquals("echo:", st.nextToken());
        String s = st.nextToken();
        assertTrue(s, s.startsWith("netbeans.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("xtest.home="));
        assertTrue(s, s.endsWith("xtesthome"));
        assertEquals("xtest.testtype=testtype", st.nextToken());
        assertEquals("xtest.attribs=attributes", st.nextToken());
        s = st.nextToken();
        assertTrue(s, s.startsWith("jemmy.home="));
        assertTrue(s, s.endsWith("jemmyyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("jelly.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        assertEquals("all:", st.nextToken());
        assertEquals("BUILD SUCCESSFUL", st.nextToken());
    }
    
    /** test case with golden file
     *
     */
    public void testClean() throws Exception {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").clean();
        MainWindowOperator.getDefault().waitStatusText("Finished build.");
        Thread.sleep(2000);
        OutputWindowOperator out = new OutputWindowOperator();
        out.selectPage("XTest Test Script (cleantests)");
        StringTokenizer st=new StringTokenizer(out.getText(), "\n");
        assertTrue("Too short output", st.countTokens()>=10);
        assertEquals("echo:", st.nextToken());
        String s = st.nextToken();
        assertTrue(s, s.startsWith("netbeans.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("xtest.home="));
        assertTrue(s, s.endsWith("xtesthome"));
        assertEquals("xtest.testtype=testtype", st.nextToken());
        assertEquals("xtest.attribs=", st.nextToken());
        s = st.nextToken();
        assertTrue(s, s.startsWith("jemmy.home="));
        assertTrue(s, s.endsWith("jemmyyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("jelly.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        assertEquals("cleantests:", st.nextToken());
        assertEquals("BUILD SUCCESSFUL", st.nextToken());
    }
    
    /** test case with golden file
     *
     */
    public void testCleanResults() throws Exception {
        ExplorerOperator.invoke().repositoryTab();
        new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").cleanResults();
        MainWindowOperator.getDefault().waitStatusText("Finished build.");
        Thread.sleep(2000);
        OutputWindowOperator out = new OutputWindowOperator();
        out.selectPage("XTest Test Script (cleanresults)");
        StringTokenizer st=new StringTokenizer(out.getText(), "\n");
        assertTrue("Too short output", st.countTokens()>=10);
        assertEquals("echo:", st.nextToken());
        String s = st.nextToken();
        assertTrue(s, s.startsWith("netbeans.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("xtest.home="));
        assertTrue(s, s.endsWith("xtesthome"));
        assertEquals("xtest.testtype=testtype", st.nextToken());
        assertEquals("xtest.attribs=", st.nextToken());
        s = st.nextToken();
        assertTrue(s, s.startsWith("jemmy.home="));
        assertTrue(s, s.endsWith("jemmyyhome"));
        s = st.nextToken();
        assertTrue(s, s.startsWith("jelly.home="));
        assertTrue(s, s.endsWith("jellyhome"));
        assertEquals("cleanresults:", st.nextToken());
        assertEquals("BUILD SUCCESSFUL", st.nextToken());
    }
    
    /** simple test case
     *
     */
    public void testIcon() throws Exception {
        ExplorerOperator.invoke().repositoryTab();
        Field f = Class.forName("org.openide.explorer.view.VisualizerNode").getDeclaredField("node");
        f.setAccessible(true);
        Node n = (Node)f.get(new XTestBuildScriptNode("qa-functional|org|netbeans|modules|testtools|data|build").getTreePath().getLastPathComponent());
        assertEquals(Utilities.loadImage("org/netbeans/modules/testtools/XTestIcon.gif"), n.getIcon(BeanInfo.ICON_COLOR_16x16));
    }
    
}
