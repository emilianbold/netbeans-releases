package org.netbeans.qa.form.undoredo;
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

import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.junit.NbTestCase;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.form.*;
import org.netbeans.jellytools.modules.form.properties.editors.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jellytools.actions.*;

import org.netbeans.jemmy.operators.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
//import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.qa.form.*;
import java.io.*;

public class BaseTest extends JellyTestCase {
    
    public String FILE_NAME = "clear_JFrame";
    public String PACKAGE_NAME = "data";
    public String DATA_PROJECT_NAME = "SampleProject";
    public String FRAME_ROOT = "[JFrame]";
    
    public MainWindowOperator mainWindow;
    public ProjectsTabOperator pto;
    public Node formnode;
    
    
    
    ComponentInspectorOperator inspector;
    FormDesignerOperator formDesigner;
    ComponentPaletteOperator palette;
    
    EditorOperator editor;
    EditorWindowOperator ewo;
    String fileName;
    
    /** */
    public BaseTest(String testName) {
        super(testName);
    }
    /*
     * select tab in PropertySheet
     */
    public void selectPropertiesTab(PropertySheetOperator pso){
        selectTab(pso, 0);
    }
    public void selectEventsTab(PropertySheetOperator pso){
        selectTab(pso, 1);
    }
    public void selectCodeTab(PropertySheetOperator pso){
        selectTab(pso, 2);
    }
    //select tab in PropertySheet
    public void selectTab(PropertySheetOperator pso, int index){
        sleep(1000);
        JToggleButtonOperator tbo=null;
        if (tbo==null) {
            tbo = new JToggleButtonOperator(pso, " ", index);
        }
        tbo.push();
    }
    
    
    public void testScenario() {
        
        mainWindow = MainWindowOperator.getDefault();
        pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();
        log("Form node selected.");
        
        EditAction editAction = new EditAction();
        editAction.perform(formnode);
        log("Source Editor window opened.");
        
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        log("Form Editor window opened.");
        
        ComponentInspectorOperator cio = new ComponentInspectorOperator();
        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();
        
        palette = new ComponentPaletteOperator();
        inspector = new ComponentInspectorOperator();
        
        //init property sheet and select the proper "tab"
        PropertySheetOperator pso = cio.properties();
        //        selectPropertiesTab(pso);
        new Action(null,"Add From Palette|Swing Containers|Panel").performPopup(new Node(inspector.treeComponents(),"[JFrame]"));
        //        selectPropertiesTab(pso);
        new Action(null,"Add From Palette|Swing Containers|Panel").performPopup(new Node(inspector.treeComponents(),"[JFrame]"));
        
        
        
        
        //change properties (color)
        inspector.selectComponent("[JFrame]|JPanel1 [JPanel]");
        selectPropertiesTab(pso);
        
        //        new ColorProperty(pso, "background").setRGBValue(202,234,223);
        
        new ColorProperty(new PropertySheetOperator("jPanel1 [JPanel] - Properties"), "background").setColorValue(new Color(202,234,223));
        
        inspector.selectComponent("[JFrame]|JPanel2 [JPanel]");
        selectPropertiesTab(pso);
        new ColorProperty(new PropertySheetOperator("jPanel2 [JPanel] - Properties"), "background").setRGBValue(252,34,3);
        
        // add JButton1 to JPanel1
        new Action(null,"Add From Palette|Swing Controls|Button").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel1 [JPanel]"));
        
        // add JButton2 to JPanel2
        new Action(null,"Add From Palette|Swing Controls|Button").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel2 [JPanel]"));
        
        // cut-paste JButton1 from JPanel1 to JPanel2
        
        new Action(null,"Cut").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel1 [JPanel]|jButton1 [JButton]"));
        new Action(null,"Paste").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel2 [JPanel]"));
        
        
        
        // change properties
        inspector.selectComponent("[JFrame]|JPanel2 [JPanel]|jButton1 [JButton]");
        
        new Property(pso, "text").setValue("<html><font color='red' size='+3'>QA</font> test");
        
        // change order
        new ActionNoBlock(null,"Change Order...").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel2 [JPanel]"));
        NbDialogOperator changeOrder = new NbDialogOperator("Change Order");
        new JListOperator(changeOrder).selectItem(1);
        new JButtonOperator(changeOrder,"Move up").doClick();
        changeOrder.btOK().doClick();
        
        // change generated code
        inspector.selectComponent("[JFrame]|JPanel2 [JPanel]|jButton1 [JButton]");
        
        new Property(pso, "text").openEditor();
        FormCustomEditorOperator fceo = new FormCustomEditorOperator("text");
        fceo.advanced();
        FormCustomEditorAdvancedOperator fceao = new FormCustomEditorAdvancedOperator();
        fceao.setGeneratePreInitializationCode(true);
        fceao.setPreInitializationCode("aaa");
        fceao.setGeneratePostInitializationCode(true);
        fceao.setPostInitializationCode("bbb");
        fceao.ok();
        fceo.ok();
        
        // event
        selectEventsTab(pso);
        
        Property prop = new Property(pso, "actionPerformed");
        prop.setValue("myAction");
        
        
        //   selectPropertiesTab(pso);
        
        
        for (int i=0;i<2;i++) {
            // section undo testing
            
            openAction.perform(formnode);
            sleep(2000) ;
            
            assertTrue("check in Editor 11b",checkEditor("private void myAction"));
            
            undo(1);
            
            // check if aaa, bbb are generated
            
            assertTrue("check in Editor 10b",checkEditor("aaa,bbb"));
            
            undo(1);
            
            // check if aaa, bbb are not in editor
            assertTrue("check i// assertTrun Editor 10a",!checkEditor("aaa,bbb"));
            
            
            //now it's not possible to check, in editore, there is different code (no on the same row)
            assertTrue("check in Editor 9b",checkEditor("jPanel2.add(jButton1),jPanel2.add(jButton2)"));
            
            undo(1);
            //check if panel order was changed
            assertTrue("check in Editor 9b",checkEditor("jPanel2.add(jButton2),jPanel2.add(jButton1)"));
            
            
            assertTrue("check in Editor 8b",checkEditor("<html>"));
            
            undo(1);
            
            assertTrue("check in Editor 8a",!checkEditor("<html>"));
            
            
            //check if both buttons are in panel2
            assertTrue("check in Editor 7a",checkEditor("jPanel2.add(jButton1"));
            assertTrue("check in Editor 7b",checkEditor("jPanel2.add(jButton2"));
            
            undo(1);
            //check if panel2 has only button2 and panel1 has only button1 as well
            assertTrue("check in Editor 7c",!checkEditor("jPanel2.add(jButton1"));
            assertTrue("check in Editor 7d",checkEditor("jPanel1.add(jButton1"));
            assertTrue("check in Editor 7e",checkEditor("jPanel2.add(jButton2"));
            
            undo(1);
            //check if button2 is not in jframe and background of panel2 is still set
            assertTrue("check in Editor 4a",!checkEditor("jButton2"));
            assertTrue("check in Editor 4b",checkEditor("jPanel2.setBackground"));
            
            undo(1);
            //check if button1 is not in jframe and background of panel1 is still set
            assertTrue("check in Editor 3a",!checkEditor("jButton1"));
            assertTrue("check in Editor 3b",checkEditor("jPanel1.setBackground"));
            
            undo(1);
            //check background if is not set on panel2
            assertTrue("check in Editor 2a",!checkEditor("jPanel2.setBackground"));
            
            undo(1);
            //check background if is not set on panel1
            assertTrue("check in Editor 1a",!checkEditor("jPanel1.setBackground"));
            
            
            undo(1);
            
            //check if panel2 disappeared from jframe
            assertTrue("check in Editor 0a",!checkEditor("jPanel2"));
            
            undo(1);
            //check if panel1 disappeared from jframe
            assertTrue("check in Editor 0b",!checkEditor("jPanel1"));
            
            
            //redo
            
            redo(1);
            //check if panel1 is in jframe
            assertTrue("check in Editor R01a",checkEditor("jPanel1"));
            
            redo(1);
            //check if panel1 is in jframe
            assertTrue("check in Editor R02a",checkEditor("jPanel2"));
            
            redo(1);
            //check if background was set for panel1
            assertTrue("check in Editor R03a",checkEditor("jPanel1.setBackground"));
            
            redo(1);
            //check if background was set for panel2
            assertTrue("check in Editor R04a",checkEditor("jPanel2.setBackground"));
            
            redo(1);
            //check if button1 was added in panel1
            assertTrue("check in Editor R05a",checkEditor("jPanel1.add(jButton1"));
            
            redo(1);
            //check if button2 was added in panel2
            assertTrue("check in Editor R06a",checkEditor("jPanel2.add(jButton2"));
            
            redo(1);
            //check if panel2 contains both buttons and panel1 is empty
            assertTrue("check in Editor R07a",checkEditor("jPanel2.add(jButton1"));
            assertTrue("check in Editor R07b",checkEditor("jPanel2.add(jButton2"));
            assertTrue("check in Editor R07c",!checkEditor("jPanel1.add(jButton2"));
            
            redo(1);
            //check if text in html was added
            assertTrue("check in Editor R08a",checkEditor("<html>"));
            assertTrue("check in Editor R08b",checkEditor("jPanel2.add(jButton2),jPanel2.add(jButton1)"));
            
            redo(1);
            //check if buttons order was changed
            assertTrue("check in Editor 9b",checkEditor("jPanel2.add(jButton1),jPanel2.add(jButton2)"));
            
            redo(1);
            //check if string aaa, bbb was added in editor
            assertTrue("check in Editor 10b",checkEditor("aaa,bbb"));
            
            redo(1);
            //check if action "myAction" was added in editor
            assertTrue("check in Editor 11b",checkEditor("private void myAction"));
            
            
            
        
        }
        
          undo(11);
        
        
        
        
                Action saveAction;
                saveAction = new Action("File|Save", null);
                saveAction.perform();
        
    }
    
    
    /** Run test.
     */
    public void testFormFile() {
        try {
            getRef().print(
                    VisualDevelopmentUtil.readFromFile(
                    getDataDir().getAbsolutePath() + File.separatorChar + DATA_PROJECT_NAME +  File.separatorChar + "src" + File.separatorChar + PACKAGE_NAME + File.separatorChar + FILE_NAME + ".form")
                    );
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        System.out.println("reffile: " + this.getName()+".ref");
        try {
            System.out.println("workdir: " + getWorkDir());
        } catch (Exception e) {
            System.out.println("e:" + e.getMessage() );
        }
        compareReferenceFiles();
    }
    
    /** Run test.
     */
    public void testJavaFile() {
        try {
            getRef().print(
                    VisualDevelopmentUtil.readFromFile(
                    getDataDir().getAbsolutePath() + File.separatorChar + DATA_PROJECT_NAME +  File.separatorChar + "src" + File.separatorChar + PACKAGE_NAME + File.separatorChar + FILE_NAME + ".java")
                    );
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        compareReferenceFiles();
        
        ewo = new EditorWindowOperator();
        ewo.closeDiscard();
        
        
        log("Test finished");
        
        
    }
    /** Run test.
     */
    
    
    
    void undo(int n) {
        //first switch to FormEditor tab
/*OpenAction openAction = new OpenAction();
openAction.perform(formnode);
 */
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        
        sleep(500) ;
        //inspector.selectComponent("[JFrame]");
        for (int i=0;i<n;i++) {
            sleep(500);
            mainWindow.getToolbarButton(mainWindow.getToolbar("Edit"), "Undo").push();
            sleep(500);
        }
    }
    
    void redo(int n) {
        //first switch to FormEditor tab
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        inspector.selectComponent("[JFrame]");
        for (int i=0;i<n;i++) {
            sleep(1000);
            mainWindow.getToolbarButton(mainWindow.getToolbar("Edit"), "Redo").push();
            sleep(1000);
        }
    }
    
    void sleep(int ms) {
        try {Thread.sleep(ms);} catch (Exception e) {}
    }
    
    
    boolean checkEditor(String regexp) {
        /*editor = ewo.getEditor("clear_JFrame");
        editor = new EditorOperator("clear_JFrame");
         */
        EditAction editAction = new EditAction();
        editAction.perform(formnode);
        log("Source Editor window opened.");
        
        
        editor = ewo.getEditor();
        sleep(300);
        String editortext = editor.getText();
        // text without escape characters
        /*
        StringBuffer newtext = new StringBuffer();
        for (int i=0;i<editortext.length();i++) {
            char ch = editortext.charAt(i);
            if (ch >= 32)
                newtext.append(ch);
        }
         */
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(regexp,",");
        int pos = -1;
        boolean result = true;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            pos = editortext.indexOf(token,pos);
            if (pos == -1) {
                result = false;
                break;
            }
            pos += token.length();
        }
        System.out.println("Result: " + result);
        return result;
    }
    /** Test could be executed internaly in Forte without Tonga
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        System.setProperty("nbjunit.workdir","c:/z");
        junit.textui.TestRunner.run(suite());
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BaseTest("testScenario"));
        //suite.addTest(new BaseTest("testFormFile"));
        //suite.addTest(new BaseTest("testJavaFile"));
        return suite;
    }
   /* public static junit.framework.Test suite() {
        return new NbTestSuite(BaseTest.class);
    }
    */
    
}

