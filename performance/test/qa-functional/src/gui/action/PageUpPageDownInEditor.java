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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of Page Up and Page Down in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class PageUpPageDownInEditor extends testUtilities.PerformanceTestCase {
    
    private boolean pgup;
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public PageUpPageDownInEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
        pgup = true;
    }
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public PageUpPageDownInEditor(String testName, String performanceDataName, boolean up) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
        pgup = up;
    }
    
    private EditorOperator editorOperator;
    
    public void initialize() {
        EditorOperator.closeDiscardAll();
        
        repaintManager().setOnlyEditor(true);
        setJavaEditorCaretFilteringOn();
        
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java"));
        editorOperator = new EditorWindowOperator().getEditor("Main20kB.java");
    }
    
    public void prepare() {
        // scroll to the place where we start
        if (pgup)
            // press CTRL+END
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        else
            // go to the first line
            editorOperator.setCaretPositionToLine(1);
   }
    
    public ComponentOperator open(){
        if (pgup)
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_UP)).perform(editorOperator);
        else
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_DOWN)).perform(editorOperator);
        return null;
    }

    protected void shutdown() {
        super.shutdown();
        repaintManager().setOnlyEditor(false);
    }
    
}
