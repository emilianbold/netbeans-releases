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

import java.awt.Font;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class TypingInCSSEditor extends  org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String fileName;    
    private EditorOperator editorOperator;
    private int caretBlinkRate;
    private Font font;
    
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;        
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;        
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
    
    @Override
    protected void initialize() {
        fileName = "stylesheet.css";
        fileToBeOpened = new Node(new WebPagesNode("VisualWebProject"), "resources|" + fileName);                
    }
    
    public void testCSSEditor() {
        doMeasurement();
    }
    
    public void prepare() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();

        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);

        editorOperator.setCaretPosition(8, 1);        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        waitNoEvent(2000);
        
    }

    public ComponentOperator open() {        
        editorOperator.typeKey('z');
        return null;
        
    }
    @Override
    public void close() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();        
        EditorOperator.closeDiscardAll();
        
    }
}
