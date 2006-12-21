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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class TypingInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private EditorOperator editorOperator;
    private int fontSize;
    private int parsingErrors;
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName) {
        super(testName);
        /*
        expectedTime = UI_RESPONSE;
        expectedTime = 10;
        WAIT_AFTER_OPEN = 0;
        WAIT_AFTER_PREPARE = 0;
        WAIT_AFTER_CLOSE = 0;
        repeat = 100;
         */
    }
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        /*
        expectedTime = UI_RESPONSE;
        expectedTime = 10;
        repeat = 100;
        WAIT_AFTER_OPEN = 0;
        WAIT_AFTER_PREPARE = 0;
        WAIT_AFTER_CLOSE = 0;
         */
    }
    
    protected void turnOff() {
        // set large font size
        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options instanceof JavaOptions) {
            fontSize = ((JavaOptions)options).getFontSize();
            ((JavaOptions)options).setFontSize(20);
        }
    }
    
    protected void turnBack() {
        // set back the original font size
        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        if (options instanceof JavaOptions) {
            ((JavaOptions)options).setFontSize(fontSize);
        }
    }
    
    public void initialize() {
        /*
        javax.swing.text.Caret thecaret = editorOperator.txtEditorPane().getCaret();
        if (thecaret instanceof BaseCaret)
        {
            BaseCaret thebasecaret = (BaseCaret) thecaret;
            setAreaToFilter (thebasecaret.x-20, thebasecaret.y-20, thebasecaret.width+40, thebasecaret.height+40);
        }
         */
        repaintManager().setOnlyEditor(true);
        setJavaEditorCaretFilteringOn();
        
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
        editorOperator = EditorWindowOperator.getEditor("Main.java");
        
        //wait painting pf folds in the editor
        waitNoEvent(2000);
        
        // go to the right place
        editorOperator.setCaretPosition(38,19);
        // make the file modified
        //new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_ENTER)).perform(editorOperator);
        
        turnOff();
        
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
        turnBack();
        repaintManager().setOnlyEditor(false);
        repaintManager().setRegionFilter(null);
        editorOperator.closeDiscard();
        super.shutdown();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new TypingInEditor("measureTime", "Type a character in Editor"));
    }
}