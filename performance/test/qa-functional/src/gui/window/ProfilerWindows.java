/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

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
        commandName = "Window|Profiling|Profiler Control Panel"; //NOI18N
        windowName = "Profiler"; ////NOI18N
        doMeasurement();
    }
    
    public void testProfilerTelemetryOverview() {
        commandName = "Window|Profiling|"+Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle", "HINT_TelemetryOverviewAction");
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryOverviewPanelName");
        doMeasurement();
    }
    
    public void testProfilerLiveResults() {
        commandName = "Window|Profiling|"+Bundle.getStringTrimmed("org.netbeans.modules.profiler.actions.Bundle", "LBL_ShowLiveResultsWindowAction");
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_ResultsWindowName");
        doMeasurement();
    }
    
    public void testProfilerVMTelemetry() {
        commandName = "Window|Profiling|VM Telemetry"; //NOI18N
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "LAB_TelemetryWindowName");
        doMeasurement();
    }
    
    public void testProfilerThreads() {
        commandName = "Window|Profiling|Threads"; //NOI18N
        windowName = Bundle.getStringTrimmed("org.netbeans.modules.profiler.Bundle", "ThreadsWindow_ThreadsWindowName");
        doMeasurement();
    }
    
    public void testProfilerProfilingPoints() {
        commandName = "Window|Profiling|Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        doMeasurement();
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
//        new Action(menuPrefix+commandName,null).performMenu(); // NOI18N
//	waitNoEvent(1000);
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(commandName);
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
