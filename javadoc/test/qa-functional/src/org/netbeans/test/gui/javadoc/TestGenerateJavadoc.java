/*
 * TestGenerateJavadoc.java
 *
 * Created on February 3, 2003, 3:56 PM
 */

package org.netbeans.test.gui.javadoc;

import java.io.PrintStream;
import java.io.File;

import junit.framework.TestSuite;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RepositoryTabOperator;

import org.netbeans.jellytools.actions.Action;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.junit.NbTestSuite;

/** JUnit test suite with Jemmy support
 *
 * @author mk97936
 * @version 1.0
 */
public class TestGenerateJavadoc extends JellyTestCase {
    
    public static final String sep = File.separator;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public TestGenerateJavadoc(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite(TestGenerateJavadoc.class);
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
    
    public void testGenerate() {
        
        String toolsPopupMenuItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
        String toolsMainMenuItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools");
        String generateMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.javadoc.Bundle", "CTL_ActionGenerate");
        String questionWinTitle = Bundle.getStringTrimmed("org.openide.Bundle", "NTF_QuestionTitle");
        
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        RepositoryTabOperator repoTabOper = RepositoryTabOperator.invoke();
        Node topNode = new Node(repoTabOper.getRootNode(), 0);
        
        String path = repoTabOper.mountLocalDirectoryAPI(topNode.getPath() + sep + 
                            "org" + sep + "netbeans" + sep + "test" + sep + "gui" + sep + // NOI18N
                            "javadoc" + sep + "data" + sep + "sampledir"); // NOI18N
        
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 120000); // NOI18N
        Action generateJDoc = new Action(toolsMainMenuItem + "|" + generateMenuItem, // NOI18N
                                         toolsPopupMenuItem + "|" + generateMenuItem); // NOI18N
        
        // javadoc is generated to default location ${netbeans.user}/javadoc
        
        // generate for MemoryView.java
        Node memoryViewNode = new Node(new Node(repoTabOper.getRootNode(), topNode.getPath() + sep + 
                            "org" + sep + "netbeans" + sep + "test" + sep + "gui" + sep + // NOI18N
                            "javadoc" + sep + "data" + sep + "sampledir"), "examples|advanced|MemoryView"); // NOI18N
        generateJDoc.perform(memoryViewNode);

        NbDialogOperator nbDialogOper_1 = new NbDialogOperator(questionWinTitle);
        nbDialogOper_1.no();
        
        assertTrue("index.html doesn't exist!", new File(userHome + sep + "javadoc" + sep + "index.html").exists()); // NOI18N
        assertTrue("MemoryView doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "advanced" + sep + "MemoryView.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
        // generate for colorpicker package
        Node colorPickerNode = new Node(new Node(repoTabOper.getRootNode(), topNode.getPath() + sep + 
                            "org" + sep + "netbeans" + sep + "test" + sep + "gui" + sep + // NOI18N
                            "javadoc" + sep + "data" + sep + "sampledir"), "examples|colorpicker"); // NOI18N        
        generateJDoc.perform(colorPickerNode);
        
        NbDialogOperator nbDialogOper_2 = new NbDialogOperator(questionWinTitle);
        nbDialogOper_2.no();
        
        assertTrue("index.html doesn't exist!", new File(userHome + sep + "javadoc" + sep + "index.html").exists()); // NOI18N
        assertTrue("ColorPicker doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "colorpicker" + sep + "ColorPicker.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
        // generate for examples package
        Node examplesNode = new Node(new Node(repoTabOper.getRootNode(), topNode.getPath() + sep + 
                            "org" + sep + "netbeans" + sep + "test" + sep + "gui" + sep + // NOI18N
                            "javadoc" + sep + "data" + sep + "sampledir"), "examples"); // NOI18N        
        generateJDoc.perform(examplesNode);
        
        NbDialogOperator nbDialogOper_3 = new NbDialogOperator(questionWinTitle);
        nbDialogOper_3.no();
        
        assertTrue("index.html doesn't exist!", new File(userHome + sep + "javadoc" + sep + "index.html").exists()); // NOI18N
        assertTrue("ClockFrame doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "clock" + sep + "ClockFrame.html").exists()); // NOI18N
        assertTrue("ImageFrame doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "imageviewer" + sep + "ImageFrame.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
