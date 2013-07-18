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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

@NbBundle.Messages({
    "StpMonitorTooltip=If selected, threads running in profiled virtual machine are monitored.",
    "StpSamplingTooltip=If selected, threads states are updated by periodically polling the profiled application.",
    "StpLockContentionTooltip=If selected, lock contention in profiled virtual machine is monitored.",
    "StpOverrideTooltip=If selected, you can override settings used for running the application.",
    "StpWorkDirTooltip=Runtime working directory of profiled application.",
    "StpJPlatformTooltip=Java platform used for running the profiled application.",
    "StpVmArgsTooltip=Extra arguments to pass to the application virtual machine, separated by spaces.",
    "StpSampleAppTooltip=Sampling: low overhead, no short methods, no method invocation counts, no profiling points.",
    "StpProfileAppTooltip=Instrumentation: noticeable overhead, all methods, exact invocation counts, required for profiling points.",
    "StpFilterTooltip=Instrumentation filter - enables you to limit the classes that are profiled.",
    "StpShowFilterTooltip=Show details of selected instrumentation filter.",
    "StpEditFilterTooltip=Edit selected instrumentation filter.",
    "StpManageFilterSetsTooltip=Add, modify or delete the listed filter sets.",
    "StpUsePpsTooltip=If selected, defined profiling points are activated for this profiling session.",
    "StpShowPpsTooltip=View the profiling points that will be active for this profiling session.",
    "StpExactTimingTooltip=When selected, instrumentation is used for both collecting call trees and method durations.",
    "StpSampledTimingTooltip=When selected, instrumentation is used only for collecting call trees and sampling is used for measuring method durations.",
    "StpSleepWaitTooltip=If selected, method durations for Thread.sleep() and Object.wait() methods are not tracked.",
    "StpFrameworkTooltip=Profile all application's methods (typically starting from main(String[])).",
    "StpSpawnedTooltip=If selected, classes invoked from new Threads or Runnables are automatically instrumented.",
    "StpLimitThreadsTooltip=Threshold for number of profiled threads.",
    "StpCpuTimerTooltip=Use special thread CPU timer for determining method durations (Solaris only).",
    "StpInstrSchemeTooltip=Algorithm used for determining which classes are instrumented.",
    "StpMethodInvokeTooltip=If selected, simple Method.invoke() calls are profiled.",
    "StpGetterSetterTooltip=If selected, simple getter/setter calls are profiled.",
    "StpEmptyMethodsTooltip=If selected, empty method calls are profiled.",
    "StpAllocTooltip=Sampling: low overhead, live objects only, no allocation stack traces, no profiling points.",
    "StpLivenessTooltip=Instrumentation: Noticeable overhead, allocated objects, allocation stack traces, required for profiling points.",
    "StpFullLifecycleTooltip=Records the full object lifecycle of allocated objects incl. surviving generations.",
    "StpTrackEveryTooltip=Track only every n-th object to reduce profiling overhead. The total number of allocated objects is not affected.",
    "StpStackTraceTooltip=Collect information on method calls allocating the objects.",
    "StpFullDepthTooltip=Record full call stack.",
    "StpLimitDepthTooltip=Limit the depth of the recorded call stack to lower profiling overhead.",
    "StpRunGcTooltip=Invoke garbage collection when taking memory snapshot to collect only live objects.",
    "StpSamplingFrequencyLabel=Sampling frequency:",
    "StpSamplingFrequencyTooltip=Customize sampling frequency of the profiler.",
    "StpSamplingFrequencyMs=&ms"
})
package org.netbeans.modules.profiler.stp;

import org.openide.util.NbBundle;

