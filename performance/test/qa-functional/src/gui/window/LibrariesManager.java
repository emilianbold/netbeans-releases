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
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Libraries Manager
 *
 * @author  mmirilovic@netbeans.org
 */
public class LibrariesManager extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of LibrariesManager */
    public LibrariesManager(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of LibrariesManager */
    public LibrariesManager(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
        // work around issue 35962 (Main menu popup accidentally rolled up)
        new ActionNoBlock("Help|About", null).perform();
        new NbDialogOperator("About").close();
   }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Tools|Libraries Manager...","|");
        return new NbDialogOperator("Libraries Manager");
    }
    
}
