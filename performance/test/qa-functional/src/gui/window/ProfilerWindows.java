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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ProfilerWindows extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static final String menuPrefix = "Window|Profiling|"; //NOI18N
    
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
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ProfilerWindows("testProfilerControlPanel","Open Profiler Control Panel Window"));
        suite.addTest(new ProfilerWindows("testProfilerTelemetryOverview","Open Profiler VM Telemetry Overview Window"));
        suite.addTest(new ProfilerWindows("testProfilerLiveResults","Open Profiler Live Results Window"));
        suite.addTest(new ProfilerWindows("testProfilerVMTelemetry","Open Profiler Profiler VM Telemetry Window Window"));
        suite.addTest(new ProfilerWindows("testProfilerThreads","Open Profiler Threads Window"));
        suite.addTest(new ProfilerWindows("testProfilerProfilingPoints","Open Profiler Profiling Pints Window"));
        return suite;
    }
    
    public void testProfilerControlPanel() {
        commandName = "Profiler Control Panel"; //NOI18N
        windowName = "Profiler"; ////NOI18N
        doMeasurement();
    }
    
    public void testProfilerTelemetryOverview() {
        commandName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle", "HINT_TelemetryOverviewAction");
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryOverviewPanelName");
        doMeasurement();
    }
    
    public void testProfilerLiveResults() {
        commandName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle", "LBL_ShowLiveResultsWindowAction");
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_ResultsWindowName");
        doMeasurement();
    }
    
    public void testProfilerVMTelemetry() {
        commandName = "VM Telemetry"; //NOI18N
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryWindowName");
        doMeasurement();
    }
    
    public void testProfilerThreads() {
        commandName = "Threads"; //NOI18N
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "ThreadsWindow_ThreadsWindowName");
        doMeasurement();
    }
    
    public void testProfilerProfilingPoints() {
        commandName = "Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        doMeasurement();
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
        new ActionNoBlock(menuPrefix+commandName,null).performMenu(); // NOI18N
        return new TopComponentOperator(windowName);
    }
    
    public void close() {
        if(testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((TopComponentOperator)this.testedComponentOperator).close();
        }
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        repeat = 1;
        junit.textui.TestRunner.run(suite());
    }
     
}
