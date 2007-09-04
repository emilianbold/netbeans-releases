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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.actions.FavoritesAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test opening Favorites Tab.
 * @author  mmirilovic@netbeans.org
 */
public class FavoritesWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Creates a new instance of FavoritesWindow */
    public FavoritesWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /** Creates a new instance of FavoritesWindow */
    public FavoritesWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    @Override
    protected void initialize() {
//        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Favorites from the main menu
        new FavoritesAction().performShortcut();
        return new FavoritesOperator();
    }

    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((FavoritesOperator)testedComponentOperator).close();
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new FavoritesWindow("measureTime"));
    }
    
}
