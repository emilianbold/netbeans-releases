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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;

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
    
    
    public void testSaveModifiedJavaFile(){
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
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
