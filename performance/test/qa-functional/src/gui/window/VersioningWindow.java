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
import org.netbeans.jellytools.modules.vcscore.VersioningOperator;
import org.netbeans.jellytools.modules.vcscore.actions.VersioningExplorerAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test opening Versioning Tab.
 * @author  mmirilovic@netbeans.org
 */
public class VersioningWindow extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of VersioningWindow */
    public VersioningWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of VersioningWindow */
    public VersioningWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Versioning from the main menu
        // doesn't work now ?   new VersioningExplorerAction().performMenu();
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Window|Versioning|Versioning","|");
        return new VersioningOperator();
    }
    
    public void close() {
        // close the tab
        ((VersioningOperator)testedComponentOperator).close();
    }
    
}
