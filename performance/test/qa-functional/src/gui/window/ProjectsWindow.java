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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ProjectViewAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test opening Projects Tab.
 * @author  mmirilovic@netbeans.org
 */
public class ProjectsWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of ProjectsWindow */
    public ProjectsWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ProjectsWindow*/
    public ProjectsWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Projects from the main menu
        new ProjectViewAction().performMenu();
        return new ProjectsTabOperator();
    }
    
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((ProjectsTabOperator)testedComponentOperator).close();
    }
    
    public void shutdown() {
        new ProjectViewAction().performMenu();
    }
    
}
