package org.netbeans.qa.form.undoredo;
/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

import java.io.PrintStream;
import java.io.PrintWriter;

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

public class BaseTest extends JellyTestCase {
    
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
    protected void setUp() {
        if (MainFrame.getMainFrame().isMDI())
            org.netbeans.test.oo.gui.jelly.Options.switchToSDI() ;
    }
     */
    
   /*
    public void tearDown() {
        MainFrame mf = MainFrame.getMainFrame();
            mf.pushMenuNoBlock("File|Save All");
            try {Thread.sleep(15000);} catch (Exception e) {}
            System.out.println("save..");
    }
    */
    
    //select tab in PropertySheet
    public void selectTab(PropertySheetOperator pso, String name){
        sleep(1000);
        ComponentSearcher searcher = new ComponentSearcher((Container)pso.getSource());
        javax.swing.JTabbedPane tPane= JTabbedPaneOperator.findJTabbedPane((Container)pso.getSource(), searcher.getTrueChooser(""));
        JTabbedPaneOperator tPaneOperator = new JTabbedPaneOperator(tPane);
        if(tPaneOperator.getTabCount() == -1)return;
        for(int i = 0 ; i<tPaneOperator.getTabCount(); i++){
            if(tPaneOperator.getTitleAt(i).equals(name)) {
                tPaneOperator.setSelectedIndex(i);
                return;
            }
        }
    }
      
    public void testScenario() {
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        fileName = "clear_JFrame";        

        log("Open clear form");
        String packageName = "data";
        FormNode node = new FormNode("src|data|clear_JFrame"); 
        node.open();
        ewo = new EditorWindowOperator();
        editor = ewo.getEditor("clear_JFrame");

        formDesigner = new FormDesignerOperator(fileName);
        palette = new ComponentPaletteOperator();
        inspector = new ComponentInspectorOperator();
        
        PropertySheetOperator pso = inspector.properties();
        
        new Action(null,"Add From Palette|Swing|JPanel").performPopup(new Node(inspector.treeComponents(),"[JFrame]"));
        new Action(null,"Add From Palette|Swing|JPanel").performPopup(new Node(inspector.treeComponents(),"[JFrame]"));
        
        //change properties (color)
        inspector.selectComponent("[JFrame]|JPanel1 [JPanel]");
        selectTab(pso, "Properties");
        
        new ColorProperty(pso, "background").setRGBValue(202,234,223);
        inspector.selectComponent("[JFrame]|JPanel2 [JPanel]");
        new ColorProperty(pso, "background").setRGBValue(252,34,3);
        
        // add JButton1 to JPanel1
        new Action(null,"Add From Palette|Swing|JButton").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel1 [JPanel]"));
        
        // add JButton2 to JPanel2
        new Action(null,"Add From Palette|Swing|JButton").performPopup(new Node(inspector.treeComponents(),"[JFrame]|JPanel2 [JPanel]"));
        
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
        //inspector.selectNode("[JFrame]" + inspector.delim + "JPanel2 [JPanel]" + inspector.delim + "jButton1 [JButton]");
        //inspector.switchToTab("Properties");
        //inspector.openEditDialog("text");
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
        selectTab(pso, "Events");
        Property prop = new Property(pso, "actionPerformed");
        prop.setValue("myAction");
        formDesigner = new FormDesignerOperator(fileName);
        selectTab(pso, "Properties");
        
       
        
        //2x
        for (int i=0;i<2;i++) {
            // undo
            assertTrue("check in Editor 11b",checkEditor("private void myAction"));
            undo(1);
            assertTrue("check in Editor 11a",!checkEditor("private void myAction"));
            
            assertTrue("check in Editor 10b",checkEditor("aaa,bbb"));
            undo(1);
            assertTrue("check in Editor 10a",!checkEditor("aaa,bbb"));
            
            assertTrue("check in Editor 9b",checkEditor("jPanel2.add(jButton1,jPanel2.add(jButton2"));
            undo(1);
            assertTrue("check in Editor 9a",checkEditor("jPanel2.add(jButton2,jPanel2.add(jButton1"));
            
            assertTrue("check in Editor 8b",checkEditor("html"));
            undo(1);
            assertTrue("check in Editor 8a",!checkEditor("html"));
            
            assertTrue("check in Editor 7b",checkEditor("jPanel2.add(jButton1"));
            undo(1);
            assertTrue("check in Editor 7a",!checkEditor("jPanel2.add(jButton1"));
            
            assertTrue("check in Editor 6b",checkEditor("jPanel2.add(jButton2"));
            undo(1);
            assertTrue("check in Editor 6a",!checkEditor("jPanel2.add(jButton2"));
            
            assertTrue("check in Editor 5b",checkEditor("jPanel1.add(jButton1"));
            undo(1);
            assertTrue("check in Editor 5a",!checkEditor("jPanel1.add(jButton1"));
            
            assertTrue("check in Editor 4b",checkEditor("jPanel2.setBackground"));
            undo(1);
            assertTrue("check in Editor 4a",!checkEditor("jPanel2.setBackground"));
            
            assertTrue("check in Editor 3b",checkEditor("jPanel1.setBackground"));
            undo(1);
            assertTrue("check in Editor 3a",!checkEditor("jPanel1.setBackground"));
            
            assertTrue("check in Editor 2b",checkEditor("jPanel2"));
            undo(1);
            assertTrue("check in Editor 2a",!checkEditor("jPanel2"));
            
            assertTrue("check in Editor 1b",checkEditor("jPanel1"));
            undo(1);
            assertTrue("check in Editor 1a",!checkEditor("jPanel1"));
            // redo
            
            assertTrue("check in Editor 101a",!checkEditor("jPanel1"));
            redo(1);
            assertTrue("check in Editor 101b",checkEditor("jPanel1"));
            
            assertTrue("check in Editor 102a",!checkEditor("jPanel2"));
            redo(1);
            assertTrue("check in Editor 102b",checkEditor("jPanel2"));
            
            assertTrue("check in Editor 103a",!checkEditor("jPanel1.setBackground"));
            redo(1);
            assertTrue("check in Editor 103b",checkEditor("jPanel1.setBackground"));
            
            assertTrue("check in Editor 104a",!checkEditor("jPanel2.setBackground"));
            redo(1);
            assertTrue("check in Editor 104b",checkEditor("jPanel2.setBackground"));
            
            assertTrue("check in Editor 105a",!checkEditor("jPanel1.add(jButton1"));
            redo(1);
            assertTrue("check in Editor 105b",checkEditor("jPanel1.add(jButton1"));
            
            assertTrue("check in Editor 106a",!checkEditor("jPanel2.add(jButton2"));
            redo(1);
            assertTrue("check in Editor 106b",checkEditor("jPanel2.add(jButton2"));
            
            assertTrue("check in Editor 107a",!checkEditor("jPanel2.add(jButton1"));
            redo(1);
            assertTrue("check in Editor 107b",checkEditor("jPanel2.add(jButton1"));
            
            assertTrue("check in Editor 108a",!checkEditor("html"));
            redo(1);
            assertTrue("check in Editor 108b",checkEditor("html"));
            
            assertTrue("check in Editor 109a",checkEditor("jPanel2.add(jButton2,jPanel2.add(jButton1"));
            redo(1);
            assertTrue("check in Editor 109b",checkEditor("jPanel2.add(jButton1,jPanel2.add(jButton2"));
            
            assertTrue("check in Editor 110a",!checkEditor("aaa,bbb"));
            redo(1);
            assertTrue("check in Editor 110b",checkEditor("aaa,bbb"));
            
            assertTrue("check in Editor 111a",!checkEditor("private void myAction"));
            redo(1);
            assertTrue("check in Editor 111b",checkEditor("private void myAction"));
            
        }
        
        undo(11);
        
        editor.closeDiscard();
        
        log("Test finished");
        
    }
    
    void undo(int n) {
        //first switch to FormEditor tab
        formDesigner = new FormDesignerOperator(fileName);
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        inspector.selectComponent("[JFrame]");
        for (int i=0;i<n;i++) {
            sleep(1000);
            mainWindow.getToolbarButton(mainWindow.getToolbar("Edit"), "Undo").push();
            sleep(1000);
        }
    }
    
    void redo(int n) {
        //first switch to FormEditor tab
        formDesigner = new FormDesignerOperator(fileName);
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
        editor = ewo.getEditor("clear_JFrame");
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
    
    public static junit.framework.Test suite() {
        return new NbTestSuite(BaseTest.class);
    }

    
}

