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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Save modified file.
 *
 * @author  mmirilovic@netbeans.org
 */
public class SaveModifiedFile extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Editor with opened file */
    public static EditorOperator editorOperator;
    
    /**
     * Creates a new instance of SaveModifiedFile
     * @param testName the name of the test
     */
    public SaveModifiedFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=2000;
    }
    
    /**
     * Creates a new instance of SaveModifiedFile
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SaveModifiedFile(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=2000;
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        new OpenAction().performPopup(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = new EditorOperator("Main.java");
        waitNoEvent(2000);
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        editorOperator.setCaretPosition(1, 3);
        editorOperator.txtEditorPane().typeText("XXX");
    }
    
    public ComponentOperator open(){
        new SaveAction().performShortcut(editorOperator);
        editorOperator.waitModified(false);
        return null;
    }
    
}
