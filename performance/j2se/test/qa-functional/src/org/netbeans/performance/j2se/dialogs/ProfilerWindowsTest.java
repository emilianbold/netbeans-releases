/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 *
 * @author mkhramov@netbeans.org
 */
public class ProfilerWindowsTest extends PerformanceTestCase {
    
    private String commandName;
    private String windowName;
    
    /**
     *
     * @param testName
     */
    public ProfilerWindowsTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     *
     * @param testName
     * @param performanceDataName
     */
    public ProfilerWindowsTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(ProfilerWindowsTest.class)
             .enableModules(".*").clusters(".*")));
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
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(commandName);
        return new TopComponentOperator(windowName);
    }
    
    public void close() {
        if(testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((TopComponentOperator)this.testedComponentOperator).close();
        }
    }
     
}
