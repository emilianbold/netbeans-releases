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

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of About dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class About extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of ValidateAbout */
    public About(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidateAbout */
    public About(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        // invoke Help / About from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Help|About","|");
        return new org.netbeans.jellytools.NbDialogOperator("About");
    }
    
}
