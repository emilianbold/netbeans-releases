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
        WAIT_AFTER_OPEN = 10000;
    }
    
    /** Creates a new instance of Options */
    public Options(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 10000;
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
