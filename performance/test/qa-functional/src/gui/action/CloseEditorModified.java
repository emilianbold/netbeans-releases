/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;


import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Closing Editor tabs.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseEditorModified extends testUtilities.PerformanceTestCase {
    
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
    
    
    public void testClosingModifiedJavaFile(){
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
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
