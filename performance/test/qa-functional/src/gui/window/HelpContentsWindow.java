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

import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.actions.HelpAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Help Contents window
 *
 * @author  anebuzelsky@netbeans.org
 */
public class HelpContentsWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Creates a new instance of HelpContentsWindow */
    public HelpContentsWindow(String testName) {
        super(testName);
        expectedTime = 8945; // 4.1 : 5475, N/A, 8945, 8790, 5062, 6229
    }
    
    /** Creates a new instance of HelpContentsWindow */
    public HelpContentsWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 8945; // 4.1 : 5475, N/A, 8945, 8790, 5062, 6229
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open() {
        // invoke Help / Contents from the main menu
        new HelpAction().performShortcut();
        return new HelpOperator();
    }
    
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((HelpOperator)testedComponentOperator).close();
    }
    
}
