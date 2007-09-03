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


import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Closing Editor tabs.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseEditorModified extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Editor with opened file */
    public static EditorOperator editorOperator;
    
    /** Dialog with asking for Save */
    private static NbDialogOperator dialog;
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     */
    public CloseEditorModified(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=1500;
    }
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditorModified(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=1500;
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        new OpenAction().performPopup(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = new EditorOperator("Main.java");
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        editorOperator.txtEditorPane().typeText("XXX");
    }
    
    public ComponentOperator open(){
        //TODO issue 44593 new CloseViewAction().performPopup(editorOperator); 
        new CloseViewAction().performMenu(editorOperator); 

        dialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.text.Bundle", "LBL_SaveFile_Title"));
        return dialog;
    }
    
    public void close(){
        dialog.cancel();
    }
    
}
