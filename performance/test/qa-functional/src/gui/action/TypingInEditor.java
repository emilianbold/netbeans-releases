/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import java.awt.event.KeyEvent;

import org.netbeans.editor.BaseCaret;
import org.netbeans.modules.java.settings.JavaSettings;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class TypingInEditor extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of TypingInEditor */
    public TypingInEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        /*
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
        expectedTime = UI_RESPONSE;
        /*
        expectedTime = 10;
        repeat = 100;
        WAIT_AFTER_OPEN = 0;
        WAIT_AFTER_PREPARE = 0;
        WAIT_AFTER_CLOSE = 0;
        */
    }
    
    private EditorOperator editorOperator;
    private int fontSize;
    private int parsingErrors;
    
    protected void turnOff() {
        // set large font size
        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions (kitClass);
        if (options instanceof JavaOptions) {
            fontSize = ((JavaOptions)options).getFontSize();
            ((JavaOptions)options).setFontSize(20);
        }
        // turn off the error hightlighting feature
        parsingErrors = JavaSettings.getDefault().getParsingErrors();
        JavaSettings.getDefault().setParsingErrors(0);        
    }
    
    protected void turnBack() {
        // set back the original font size
        Class kitClass = JavaKit.class;
        BaseOptions options = BaseOptions.getOptions (kitClass);
        if (options instanceof JavaOptions) {
            ((JavaOptions)options).setFontSize(fontSize);
        }
        // set the modified properties back to default
        JavaSettings.getDefault().setParsingErrors(parsingErrors);        
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
        editorOperator = new EditorWindowOperator().getEditor("Main.java");
        
        //wait painting pf folds in the editor
        waitNoEvent(2000);
        
        // go to the right place
        editorOperator.setCaretPositionToEndOfLine(32);
        // make the file modified
        //new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_ENTER)).perform(editorOperator);
        
        turnOff();
        
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_A)).perform(editorOperator);
        return null;
    }
    
    public void close() {
        // do nothing
    }
    
    public void shutdown() {
        turnBack();
        editorOperator.closeDiscard();
        repaintManager().setOnlyEditor(false);
        super.shutdown();
    }

}