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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */

public class PasteCSSText  extends  org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected Node fileToBeOpened;
    protected Node textFileToOpen;
    
    protected String fileName; 
    protected String textFile;
    
    private EditorOperator editorOperator;
    private int caretBlinkRate;    
    
    private static final String CONFIG_NODE = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Config");

    /** Creates a new instance of PasteCSSText */
    public PasteCSSText(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;       
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations         
    }
    
    /** Creates a new instance of PasteCSSText */    
    public PasteCSSText(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = UI_RESPONSE;        
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations         
    }
    
    @Override
    public void initialize() {
        log("::initialize");
        openAndCopy();
        
        fileName = "stylesheet.css";
        fileToBeOpened = new Node(new WebPagesNode("VisualWebProject"), "resources|" + fileName);          
    }
    
    @Override
    public void prepare() {
        log("::prepare");
        openCSSFile();
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
        
        editorOperator.makeComponentVisible();
        editorOperator.setCaretPosition(8, 1);
        
               
        //repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        waitNoEvent(2000);
    }

    @Override
    public ComponentOperator open() {
        log("::open");
        pasteText();
        return null;
    }
    
    public void close() {
        log("::close");
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        //repaintManager().resetRegionFilters();        
        EditorOperator.closeDiscardAll();        
    }
    
    public void shutdown() {
        
    }
    private void openCSSFile() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);        
    }
    
    private void pasteText() {
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator);
    }
    
    private void openAndCopy() {
        textFile = "MANIFEST.MF";
        Node projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
        textFileToOpen = new Node(projectRoot,CONFIG_NODE +"|"+textFile);
        
        new OpenAction().performAPI(textFileToOpen);        
        EditorOperator textFileEditorOperator = EditorWindowOperator.getEditor(textFile);
        textFileEditorOperator.makeComponentVisible();
        textFileEditorOperator.setCaretPosition(1,1);
        textFileEditorOperator.select(1,1,16);
        
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).perform(textFileEditorOperator);
        waitNoEvent(1000);
        
        textFileEditorOperator.closeDiscard();
    }

}
