/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.spi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.perfan.storage.impl.ErprintSession;
import org.netbeans.modules.dlight.perfan.storage.impl.ExperimentStatistics;
import org.netbeans.modules.dlight.perfan.storage.impl.Metrics;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

public class MonitorsUpdateService {

    private static Pattern lineStartsWithIntegerPattern = Pattern.compile("^ *([0-9]+).*$"); // NOI18N
    private static final List<String> syncColNames = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_ulockSummary.getColumnName()));
    private static final List<String> leaksColNames = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_leakSize.getColumnName()));
    private final ErprintSession er_print;
    private final SunStudioDataCollector ssdc;
    private final boolean isSyncMonitor;
    private final boolean isMemoryMonitor;
    private final Metrics metrics;
    private Future task;

    MonitorsUpdateService(SunStudioDataCollector ssdc,
            ExecutionEnvironment execEnv,
            String sproHome, String experimentDir,
            Collection<CollectedInfo> collectedInfo) {
        this.ssdc = ssdc;
        this.er_print = new ErprintSession(execEnv, sproHome, experimentDir);
        isSyncMonitor = collectedInfo.contains(SunStudioDCConfiguration.CollectedInfo.SYNCSUMMARY);
        isMemoryMonitor = collectedInfo.contains(SunStudioDCConfiguration.CollectedInfo.MEMSUMMARY);
        metrics = isMemoryMonitor ? Metrics.constructFrom(
                Arrays.asList(SunStudioDCConfiguration.c_leakSize),
                Arrays.asList(SunStudioDCConfiguration.c_leakSize))
                : null;
    }

    public void start() {
        if (isBlank()) {
            return;
        }

        task = DLightExecutorService.scheduleAtFixedRate(new MonitorsUpdateRunnable(), 1,
                TimeUnit.SECONDS, "SunStudio monitors update task"); // NOI18N

    }

    public void stop() {
        synchronized (this) {
            if (task != null) {
                er_print.close();
                task.cancel(true);
                task = null;
            }
        }
    }

    boolean isBlank() {
        return !(isMemoryMonitor || isSyncMonitor);
    }

    private class MonitorsUpdateRunnable implements Runnable {

        public void run() {
            boolean restarted = false;

            if (isSyncMonitor) {
                try {
                    ExperimentStatistics stat = er_print.getExperimentStatistics(!restarted);
                    restarted = true;
                    if (stat != null) {
                        DataRow row = new DataRow(syncColNames, Arrays.asList(stat.getULock_p()));
                        ssdc.updateIndicators(Arrays.asList(row));
                    }
                } catch (IOException ex) {
                }
            }

            if (isMemoryMonitor) {
                try {
                    String[] result = er_print.getHotFunctions(metrics, 1, !restarted);
                    restarted = true;

                    if (result == null || result.length == 0) {
                        return;
                    }

                    Matcher m = lineStartsWithIntegerPattern.matcher(result[0]);

                    if (!m.matches()) {
                        return;
                    }

                    String value = m.group(1);

                    if (value != null) {
                        DataRow row = new DataRow(leaksColNames, Arrays.asList(Long.valueOf(value)));
                        ssdc.updateIndicators(Arrays.asList(row));
                    }
                } catch (IOException ex) {
                }
            }
        }
    }
}
