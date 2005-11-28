/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Override and Implement Methods dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class OverrideMethods extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private String MENU, TITLE;
    
    /** Creates a new instance of OverrideMethods */
    public OverrideMethods(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of OverrideMethods */
    public OverrideMethods(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Source") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.java.tools.Bundle","LAB_OverrideTool");
        TITLE = Bundle.getStringTrimmed("org.netbeans.modules.java.tools.Bundle","LBL_OverridePanel2_Title");
        
        // open a java file in the editor
        editor = gui.Utilities.openJavaFile();
        waitNoEvent(5000);  // annotations, folds, toolbars, ...
    }
    
    public void prepare() {
        editor.makeComponentVisible();
        editor.setCaretPositionToLine(31);
   }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }

    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new OverrideMethods("measureTime"));
    }
}
