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

import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.OptionsViewAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Options.
 *
 * @author  mmirilovic@netbeans.org
 */
public class Options extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    OptionsOperator options;
    
    /** Creates a new instance of Options */
    public Options(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of Options */
    public Options(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare(){
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open(){
        // push menu Tools|Options
        new OptionsViewAction().performMenu();
        options = new OptionsOperator();
        return options;
    }
    
    public void close(){
        if(options != null)
            options.close();
    }
}
