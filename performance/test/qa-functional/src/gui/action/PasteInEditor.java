/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import java.awt.event.KeyEvent;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of Paste text to opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PasteInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of PasteInEditor */
    public PasteInEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
    }
    
    /** Creates a new instance of PasteInEditor */
    public PasteInEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }
    
    private EditorOperator editorOperator1, editorOperator2;
    
    public void initialize() {
        EditorOperator.closeDiscardAll();
        
        repaintManager().setOnlyEditor(true);
        setJavaEditorCaretFilteringOn();
        
        // open two java files in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java"));
        editorOperator1 = new EditorWindowOperator().getEditor("Main20kB.java");
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|TestClassForCopyPaste.java"));
        editorOperator2 = new EditorWindowOperator().getEditor("TestClassForCopyPaste.java");
    }
    
    public void prepare() {
        // copy a part of the first file to the clipboard
        editorOperator1.makeComponentVisible();
        editorOperator1.select(53,443);
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).perform(editorOperator1);
        waitNoEvent(1000);
        // go to the end of the second file
        editorOperator2.makeComponentVisible();
        editorOperator2.setCaretPositionToLine(23);
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator2);
   }
    
    public ComponentOperator open(){
        // paste the clipboard contents
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        return null;
    }
    
    public void shutdown() {
        // close the second file without saving it
        editorOperator2.closeDiscard();
        editorOperator1.closeDiscard();
        repaintManager().setOnlyEditor(false);
    }
    
}
