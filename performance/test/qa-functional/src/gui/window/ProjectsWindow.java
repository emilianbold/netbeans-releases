/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
