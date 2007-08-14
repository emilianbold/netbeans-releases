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
import java.awt.event.KeyEvent;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;


/**
 * Test of Paste text to opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PasteInJspEditor extends WebPerformanceTestCase {
    private String file;
    private EditorOperator editorOperator1, editorOperator2;
    
    /** Creates a new instance of PasteInEditor */
    public PasteInJspEditor(String testName) {
        super(testName);
        init();
    }
    
    /** Creates a new instance of PasteInEditor */
    public PasteInJspEditor(String file, String testName, String performanceDataName) {
        super(testName, performanceDataName);
        this.file = file;
        init();
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_PREPARE = 3000;
        // in case this time is longer than 1000ms we will catch events generated
        // by parser which starts with 1000ms delay
        WAIT_AFTER_OPEN = 750;
    }
    
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
        jspOptions().setFontSize(20);
        jspOptions().setCodeFoldingEnable(false);
        // open two java files in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|Test.jsp"));
        editorOperator1 = new EditorWindowOperator().getEditor("Test.jsp");
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator2 = new EditorWindowOperator().getEditor(file);
        // copy a part of the first file to the clipboard
        editorOperator1.makeComponentVisible();
        editorOperator1.select(12,18);
        new CopyAction().perform();
        // go to the end of the second file
        editorOperator2.makeComponentVisible();
        editorOperator2.setCaretPositionToLine(1);
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        eventTool().waitNoEvent(2000);
    }
    
    public void prepare() {
        System.out.println("=== " + this.getClass().getName() + " ===");
    }
    
    public ComponentOperator open(){
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        // paste the clipboard contents
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        return null;
    }
    
    public void close() {
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();
        
    }
    
    protected void shutdown() {
        super.shutdown();
        // close the second file without saving it
        editorOperator1.closeDiscard();
        editorOperator2.closeDiscard();
    }
}
