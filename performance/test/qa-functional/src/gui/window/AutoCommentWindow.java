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

package gui.window;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Auto Comment Window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org 
 */
public class AutoCommentWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private String MENU, TITLE;
    
    /** Creates a new instance of AutoCommentWindow */
    public AutoCommentWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of AutoCommentWindow */
    public AutoCommentWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.javadoc.comments.Bundle","CTL_AUTOCOMMENT_MenuItem");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.javadoc.comments.Bundle","CTL_AUTOCOMMENT_WindowTitle");
    
        // open a java file in the editor
        editor = gui.Utilities.openJavaFile();
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open() {
        // invoke Tools / Auto Comment from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new TopComponentOperator(TITLE);
    }
    
    public void close() {
        // close the tab
        new TopComponentOperator(TITLE).close();
    }
    
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }

}
