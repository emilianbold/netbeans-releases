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

package org.netbeans.modules.testtools.generator;

/*
 * NodeGeneratorTest.java
 *
 * Created on September 02, 2002, 2:27 PM
 */

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.*;

import junit.framework.*;
import org.netbeans.junit.*;

import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.testtools.*;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.StringProperty;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;

/** JUnit test suite with Jemmy/Jelly2 support
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class NodeGeneratorTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public NodeGeneratorTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NodeGeneratorTest("testPrepareFS"));
        suite.addTest(new NodeGeneratorTest("testGrabNode"));
        suite.addTest(new NodeGeneratorTest("testVerifyNodeCode"));
        suite.addTest(new NodeGeneratorTest("testVerifyActionCode"));
        suite.addTest(new NodeGeneratorTest("testRemoveFS"));
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
    
    private void clean(String folder) throws Exception {
        File f=new File(getWorkDir().getParentFile(), folder);
        if (f.exists()) {
            File files[] = f.listFiles();
            for (int i=0; i<files.length; i++) 
                assertTrue(files[i].delete());
            assertTrue(f.delete());
        }
    }
    
    /** simple test case
     */
    public void testPrepareFS() throws Exception {
        ExplorerOperator.invoke().repositoryTab().mountLocalDirectoryAPI(getWorkDir().getParentFile().getAbsolutePath());
        clean("nodes");
        clean("actions");
    }
    
    /** simple test case
     */
    public void testRemoveFS() throws Exception {
        clean("nodes");
        clean("actions");
        new FilesystemNode(ExplorerOperator.invoke().repositoryTab().tree(), "NodeGeneratorTest").unmount();
    }
                
    /** simple test case
     */
    public void testGrabNode() throws Exception {
        closeAllModal=true;
        NodeGeneratorOperator gen = NodeGeneratorOperator.invoke();
        gen.checkDefaultInline(true);
        gen.checkDefaultNoBlock(true);
        ExplorerOperator exp = ExplorerOperator.invoke();
        new Node(gen.treeFilesystems(), "NodeGeneratorTest").select();
        gen.start();
        gen.verifyStatus("Use CTRL-F11");
        exp.repositoryTab().getRootNode().callPopup();
        Robot r=new Robot();
        r.keyPress(KeyEvent.VK_CONTROL);
        r.keyPress(KeyEvent.VK_F11);
        r.keyRelease(KeyEvent.VK_F11);
        r.keyRelease(KeyEvent.VK_CONTROL);
        NodeEditorOperator editor = new NodeEditorOperator();
        new Node(editor.treeNodeAndActions(), "").select();
        Thread.sleep(1000);
        new StringProperty(editor.propertySheet(), "nodeName").setStringValue("TestNode");
        new Node(editor.treeNodeAndActions(), "inline ActionNoBlock(null, \"Tools").select();
        Thread.sleep(1000);
        new ComboBoxProperty(editor.propertySheet(), "inline").setValue("False");
        Thread.sleep(1000);
        new StringProperty(editor.propertySheet(), "name").setStringValue("TestAction");
        new Node(editor.treeNodeAndActions(), "new actions.TestAction").select();
        editor.verify();
        editor.ok();
        gen.verifyStatus("Finished:");
        gen.stop();
        gen.close();
        FilesystemNode fsnode=new FilesystemNode(exp.repositoryTab().tree(), "NodeGeneratorTest");
        fsnode.refreshFolder();
        new Node(fsnode, "nodes|TestNode").select();
    }
    
    /** test case with golden file
     */
    public void testVerifyNodeCode() throws Exception {
        captureScreen=false;
        dumpScreen=false;
        File f = new File(getWorkDir().getParentFile(), "nodes/TestNode.java");
        assertTrue(f.exists());
        BufferedReader in = new BufferedReader(new FileReader(f));
        PrintStream ref = getRef();
        String line;
        while (in.ready()) {
            line = in.readLine();
            if (!line.startsWith(" * Created on") && !line.startsWith(" * @author"))
                ref.println(line);
        }
        in.close();
        ref.close();
        compareReferenceFiles();
    }
    
    /** test case with golden file
     */
    public void testVerifyActionCode() throws Exception {
        captureScreen=false;
        dumpScreen=false;
        File f = new File(getWorkDir().getParentFile(), "actions/TestAction.java");
        assertTrue(f.exists());
        BufferedReader in = new BufferedReader(new FileReader(f));
        PrintStream ref = getRef();
        String line;
        while (in.ready()) {
            line = in.readLine();
            if (!line.startsWith(" * Created on") && !line.startsWith(" * @author"))
                ref.println(line);
        }
        in.close();
        ref.close();
        compareReferenceFiles();
    }
}
