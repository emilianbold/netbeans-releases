/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import java.awt.event.KeyEvent;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Go To Line dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class GotoLineDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private String TITLE;
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "goto-title");
        // open a java file in the editor
        editor = gui.Utilities.openJavaFile();
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open(){
        // press CTRL+G
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_G, KeyEvent.CTRL_MASK)).perform(editor);
        return new NbDialogOperator("Go to Line");
    }
    
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }

}
