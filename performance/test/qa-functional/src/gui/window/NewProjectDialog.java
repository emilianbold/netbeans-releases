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

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.NewProjectAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of New Project Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class NewProjectDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of NewProjectDialog */
    public NewProjectDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of NewProjectDialog */
    public NewProjectDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open() {
        // invoke File / Open File from the main menu
        new NewProjectAction().performMenu();
        return new NewProjectWizardOperator();
    }
    
}
