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

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.actions.Action;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ProfilerWindows extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static final String menuPrefix = "Profile|View|";
    
    private String commandName;
    private String windowName;
    
    /**
     * 
     * @param testName 
     */
    public ProfilerWindows(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;        
    }
    /**
     * 
     * @param testName
     * @param performanceDataName 
     */
    public ProfilerWindows(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;        
    }
    /**
     *  initialize 
     */
    public void initialize(){
        log("::initialize::");
        new TopComponentOperator(windowName).closeWindow();
    }
    
    public void testProfilerControlPanel() {
        commandName = "Profiler Control Panel"; //NOI18N
        windowName = "Profiler"; ////NOI18N
        doMeasurement();
    }
    
    /**
     *  test invocation time for Telemetry Overview window
     */
    public void testProfilerTelemetryOverview() {
        commandName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.actions.Bundle", "HINT_TelemetryOverviewAction");
        windowName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryOverviewPanelName");     
        doMeasurement();
    }
    
    public void testProfilerLiveResults() {
        commandName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.actions.Bundle", "LBL_ShowLiveResultsWindowAction");
        windowName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.Bundle", "LiveResultsWindow_LiveResultsTabName");     
        doMeasurement();
    }
    
    public void testProfilerVMTelemetry() {
        commandName = "VM Telemetry"; //NOI18N
        windowName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryWindowName");    
        doMeasurement();
    }
    
    public void testProfilerThreads() {
        commandName = "Threads"; //NOI18N
        windowName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.profiler.Bundle", "ThreadsWindow_ThreadsWindowName");      
        doMeasurement();
    }
    
    public void testProfilerProfilingPoints() {
        commandName = "Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        doMeasurement();
    }
    /**
     * 
     */
    public void prepare() {
        
    }

    public ComponentOperator open() {
        new Action(menuPrefix+commandName,null).performMenu(); // NOI18N
        return new TopComponentOperator(windowName);
    }
    
    public void close() {
        if(testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((TopComponentOperator)this.testedComponentOperator).close();
        }
        
    }
}
