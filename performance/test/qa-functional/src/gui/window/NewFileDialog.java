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
import org.netbeans.jellytools.actions.ActionNoBlock;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.DialogOperator;

/**
 * Test of New File Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class NewFileDialog extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of NewFileDialog */
    public NewFileDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of NewFileDialog */
    public NewFileDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
        // work around issue 35962 (Main menu popup accidentally rolled up)
        new ActionNoBlock("Help|About", null).perform();
        new org.netbeans.jellytools.NbDialogOperator("About").close();
    }
    
    public ComponentOperator open() {
        // invoke File / Open File from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("File|New File...","|");
        return new DialogOperator("New File - Choose File Type");
    }
    
}
