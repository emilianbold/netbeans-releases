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
