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

package gui.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction;


/**
 * Test of Paste text to opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class ToggleBreakpoint extends WebPerformanceTestCase {
    private String file;
    private List bpList = new ArrayList();
    /** Creates a new instance of ToggleBreakpoint */
    public ToggleBreakpoint(String testName) {
        super(testName);
        init();
    }
    
    /** Creates a new instance of ToggleBreakpoint */
    public ToggleBreakpoint(String file, String testName, String performanceDataName) {
        super(testName, performanceDataName);
        this.file = file;
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
    }
    private EditorOperator editorOperator1;
    
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
        // open file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator1 = new EditorWindowOperator().getEditor(file);
        eventTool().waitNoEvent(500);
        waitNoEvent(1000);
    }
    
    public void prepare() {
        System.out.println("=== " + this.getClass().getName() + " ===");
        editorOperator1.makeComponentVisible();
        editorOperator1.setCaretPosition(7,1);
        eventTool().waitNoEvent(100);
    }
    
    public ComponentOperator open(){
        // Toggle Breakpoint
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_F8, KeyEvent.CTRL_MASK)).perform(editorOperator1);
        return null;
    }
    
    public void close() {
        deleteAllBreakpoints();
    }
    
    protected void shutdown() {
        editorOperator1.closeDiscard();
        super.shutdown();
    }
    
    private void deleteAllBreakpoints() {
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_5, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK)).perform();
        //new BreakpointsWindowAction().perform();
        //new Action("Window|Debugging|Breakpoints",null).perform();
        TopComponentOperator tco = new TopComponentOperator("Breakpoints");
        new Action(null,"Delete All").perform(tco);
        tco.close();
    }
}
