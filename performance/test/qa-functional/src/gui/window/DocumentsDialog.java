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

import org.netbeans.jellytools.DocumentsDialogOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.DocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Documents dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class DocumentsDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorWindowOperator editor;
    
    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize(){
        gui.Utilities.open10FilesFromJEdit();
        editor = new EditorWindowOperator();
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open() {
        // invoke Window / Documents from the main menu
        new DocumentsAction().performMenu();
        return new DocumentsDialogOperator();
    }

    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }
    
}
