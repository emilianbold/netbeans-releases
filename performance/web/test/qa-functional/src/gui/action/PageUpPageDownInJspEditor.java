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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.settings.JSPOptions;
import org.netbeans.test.web.performance.WebPerformanceTestCase;


/**
 * Test of Page Up and Page Down in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class PageUpPageDownInJspEditor extends WebPerformanceTestCase {
    private boolean pgup;
    private String file;
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public PageUpPageDownInJspEditor(String file, String testName) {
        super(testName);
        pgup = true;
        this.file = file;
        init();
    }
    
    /** Creates a new instance of PageUpPageDownInEditor */
    public PageUpPageDownInJspEditor(String file, String testName, String performanceDataName, boolean up) {
        super(testName, performanceDataName);
        pgup = up;
        this.file = file;
        init();
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
    }
    
    private EditorOperator editorOperator;
    private int statusBarCaretDelay;
    private boolean codeFoldindEnabled;
    
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
        jspOptions().setCodeFoldingEnable(false);
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator = new EditorWindowOperator().getEditor(file);
        // turn off the status bar delay
        JSPOptions options = (JSPOptions) BaseOptions.getOptions(JSPKit.class);
        statusBarCaretDelay = options.getStatusBarCaretDelay();
        options.setStatusBarCaretDelay(0);
        codeFoldindEnabled = options.getCodeFoldingEnable();
        options.setCodeFoldingEnable(false);
        waitNoEvent(2000);
    }
    
    public void prepare() {
        System.out.println("=== " + this.getClass().getName() + " ===");
        // scroll to the place where we start
        if (pgup)
            // press CTRL+END
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        else
            // go to the first line
            editorOperator.setCaretPositionToLine(1);
        eventTool().waitNoEvent(500);
    }
    
    public ComponentOperator open(){
       //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        if (pgup)
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_UP)).perform(editorOperator);
        else
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_DOWN)).perform(editorOperator);
        return null;
    }
    
    protected void shutdown() {
       repaintManager().resetRegionFilters(); ///added reset filters command - possibly missing previously
       editorOperator.closeDiscard();
        super.shutdown();
    }
}
