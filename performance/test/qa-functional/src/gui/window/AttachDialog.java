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
 * Test of View Attach dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class AttachDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String MENU, TITLE;
    
    /** Creates a new instance of AttachDialog */
    public AttachDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of AttachDialog */
    public AttachDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle","Menu/RunProject") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle","CTL_Connect");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle","CTL_Connect_to_running_process");
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open() {
        // invoke Debug / Start Session / Attach from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
}
