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

import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.actions.FilesViewAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test opening Files Tab.
 * @author  mmirilovic@netbeans.org
 */
public class FilesWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of FilesWindow */
    public FilesWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of FilesWindow*/
    public FilesWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Files from the main menu
        new FilesViewAction().performMenu();
        return new FilesTabOperator();
    }
    
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((FilesTabOperator)testedComponentOperator).close();
    }
    
    public void shutdown() {
        new FilesViewAction().perform();
    }
}
