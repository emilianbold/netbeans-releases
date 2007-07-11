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
import javax.swing.KeyStroke;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;

/**
 * Test of typing in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org
 */
public class TypingInJspEditor extends WebPerformanceTestCase {
    private String file;
    private int line;
    
    /** Creates a new instance of TypingInEditor */
    public TypingInJspEditor(String file, int line, String testName) {
        super(testName);
        this.file = file;
        this.line = line;
        init();
    }
    
    /** Creates a new instance of TypingInEditor */
    public TypingInJspEditor(String file, int line, String testName, String performanceDataName) {
        super(testName, performanceDataName);
        this.file = file;
        this.line = line;
        init();
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_PREPARE = 3000;
    }
    
    private EditorOperator editorOperator;
    
    protected void initialize() {
        System.out.println("=== " + this.getClass().getName() + " ===");
        jspOptions().setCaretBlinkRate(0);
        // delay between the caret stops and the update of his position in status bar
        jspOptions().setStatusBarCaretDelay(0);
        jspOptions().setFontSize(20);
        jspOptions().setCodeFoldingEnable(false);
        // open a java file in the editor
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"),"Web Pages|"+file));
        editorOperator = new EditorWindowOperator().getEditor(file);
        // go to the right place
        editorOperator.setCaretPositionToLine(line);
        // make the file modified
        //XXX new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_ENTER)).perform(editorOperator);
        //wait painting pf folds in the editor
        new EventTool().waitNoEvent(1000);
    }
    
    public void prepare() {
   }
    
    public ComponentOperator open(){
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        KeyStroke keyA = KeyStroke.getKeyStroke('a');
        new ActionNoBlock(null, null, keyA).perform(editorOperator);
        return null;
    }
    
    public void close() {
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();
       
    }
    
    protected void shutdown() {
        editorOperator.closeDiscard();
        super.shutdown();
    }
}
