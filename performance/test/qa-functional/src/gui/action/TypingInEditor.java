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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.modules.editor.options.BaseOptions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class TypingInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private EditorOperator editorOperator;
    
    private int fontSize, caretBlinkRate;
    private String fileName;
    private int caretPositionX, caretPositionY;
    
    private Class kitClass,optionsClass;
    
    Node fileToBeOpened;
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName) {
        super(testName);
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations
    }
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TypingInEditor("testJavaEditor", "Type a character in Java Editor"));
        suite.addTest(new TypingInEditor("testTxtEditor", "Type a character in Txt Editor"));
        suite.addTest(new TypingInEditor("testJspEditor", "Type a character in Jsp Editor"));
        return suite;
    }
    
    public void testTxtEditor() {
        fileName = "textfile.txt";
        caretPositionX = 2;
        caretPositionY = 1;
        kitClass = org.netbeans.modules.editor.plain.PlainKit.class;
        optionsClass = org.netbeans.modules.editor.plain.options.PlainOptions.class;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }
    
    public void testJavaEditor() {
        fileName = "Main.java";
        caretPositionX = 38;
        caretPositionY = 19;
        kitClass = org.netbeans.modules.editor.java.JavaKit.class;
        optionsClass = org.netbeans.modules.java.editor.options.JavaOptions.class;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }
   
    public void testJspEditor() {
        fileName = "Test.jsp";
        caretPositionX = 6;
        caretPositionY = 9;
        kitClass = org.netbeans.modules.web.core.syntax.JSPKit.class;
        optionsClass = org.netbeans.modules.web.core.syntax.settings.JSPOptions.class;
        fileToBeOpened = new Node(new WebPagesNode("PerformanceTestData"), fileName);
        doMeasurement();
    }
   
    public void initialize() {
        // open a java file in the editor
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        
        //wait painting pf folds in the editor
        waitNoEvent(2000);
        
        // go to the right place
        editorOperator.setCaretPosition(caretPositionX,caretPositionY);
        
        setEditorForMeasuringOn();
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        //new Action(null, null, new Shortcut(KeyEvent.VK_A)).perform(editorOperator);
        editorOperator.typeKey('a');
        return null;
    }
    
    public void close() {
        // do nothing
    }
    
    
    public void shutdown() {
        setEditorForMeasuringOff();
        editorOperator.closeDiscard();
        super.shutdown();
    }

    private void setEditorForMeasuringOn(){
        // measure only paint events from QuietEditorPane
        repaintManager().setOnlyEditor(true);
        
        // set large font size for Editor
        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options.getClass().isInstance(optionsClass)) {
            fontSize = options.getFontSize();
            options.setFontSize(20);
        }
        
        caretBlinkRate = options.getCaretBlinkRate();
        //disable caret blinkering
        options.setCaretBlinkRate(0);
    }
    
    private void setEditorForMeasuringOff(){
        // measure only paint events from QuietEditorPane
        repaintManager().setRegionFilter(null);
        
        // set back the original font size for Editor
        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options.getClass().isInstance(optionsClass)) {
            options.setFontSize(fontSize);
        }
        
        // set back the original blink rate
        options.setCaretBlinkRate(caretBlinkRate);
    }
    
    public static void main(java.lang.String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new TypingInEditor("testJspEditor", "Type a character in Jsp Editor"));
    }
}