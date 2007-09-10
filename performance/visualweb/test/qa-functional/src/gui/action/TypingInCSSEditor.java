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

import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
/**
 *
 * @author mkhramov@netbeans.org
 */
public class TypingInCSSEditor extends TypingInEditor {
    
    
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName) {
        super(testName);
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
    /** Creates a new instance of TypingInCSSEditor */
    public TypingInCSSEditor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations        
    }
    
    public void testCSSEditor() {
        fileName = "stylesheet.css";
        caretPositionX = 1;
        caretPositionY = 8;
        kitClass = org.netbeans.modules.css.editor.CssEditorKit.class;
        optionsClass = org.netbeans.modules.css.options.CssOptions.class;
        fileToBeOpened = new Node(new WebPagesNode("VisualWebProject"),"resources|"+fileName);
        doMeasurement();
    }
}
