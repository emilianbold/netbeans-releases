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
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;


/**
 * Test of Open Project Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenProjectDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String MENU, TITLE;
    
    /** Creates a new instance of OpenProjectDialog */
    public OpenProjectDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of OpenProjectDialog */
    public OpenProjectDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public void initialize() {
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle","LBL_OpenProjectAction_Name");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle","LBL_PrjChooser_Title");
    }
    
    public ComponentOperator open() {
        // invoke File / Open File from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
}
