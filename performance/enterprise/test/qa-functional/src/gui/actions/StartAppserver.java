/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package gui.actions;

import gui.EPUtilities;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure application server Startup time via NetBeans TaskModel API.
 *
 * @author rashid@netbeans.org, mkhramov@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class StartAppserver extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     **/
    public StartAppserver(String testName) {
        super(testName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     *  @param performanceDataName
     **/
    public StartAppserver(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        EPUtilities.startApplicationServer();
        return null;
    }
    
    public void close(){
        log("::close");
        EPUtilities.stopApplicationServer();
    }
    
    public static void main(java.lang.String[] args) {
        repeat = 2;
        junit.textui.TestRunner.run(new StartAppserver("measureTime"));
    }
    
}