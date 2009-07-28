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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.perfan.spi.datafilter.CollectedObjectsFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.perfan.spi.datafilter.SunStudioFiltersProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;

public class ErprintSession {

    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    private final NativeProcessBuilder npb;
    private volatile Erprint er_print;
    private final SunStudioFiltersProvider dataFiltersProvider;

    public ErprintSession(ExecutionEnvironment execEnv, String sproHome, String experimentDirectory, SunStudioFiltersProvider dataFiltersProvider) {
        id = idCounter.incrementAndGet();
        String er_printCmd = sproHome + "/bin/er_print"; // NOI18N
        NativeProcessBuilder erProcessBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
        erProcessBuilder.setExecutable(er_printCmd);
        erProcessBuilder.setWorkingDirectory(experimentDirectory);
        erProcessBuilder.setArguments(experimentDirectory).unbufferOutput(true);
        this.dataFiltersProvider = dataFiltersProvider;

        npb = erProcessBuilder;
    }

    private void applyFilters() {
        if (dataFiltersProvider == null) {
            return;
        }

        final List<DataFilter> filters = dataFiltersProvider.getDataFilters();

        synchronized (filters) {
            for (DataFilter filter : filters) {
                try {
                    if (filter instanceof CollectedObjectsFilter) {
                        er_print.selectObjects((CollectedObjectsFilter) filter);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private synchronized Erprint restartAndLock(boolean restart) throws IOException {
        if (er_print == null || restart) {
            stop_er_print();
            er_print = new Erprint(npb, id);

            er_print.addLock();
            applyFilters();
        } else {
            try {
                er_print.addLock();
            } catch (IllegalStateException ex) {
                // OK... it is termineted. so don't add lock
                // TODO: review
            }
        }

        return er_print;
    }

    private synchronized void stop_er_print() {
        if (er_print != null) {
            er_print.stop();
            er_print = null;
        }
    }

    public void close() {
        stop_er_print();
    }

    public ExperimentStatistics getExperimentStatistics(int timeout, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getExperimentStatistics();
        } finally {
            erp.releaseLock();
        }
    }

    public ThreadsStatistic getThreadsStatistic(int timeout, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getThreadsStatistics();
        } finally {
            erp.releaseLock();
        }
    }

    public LeaksStatistics getExperimentLeaks(boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getExperimentLeaks();
        } finally {
            erp.releaseLock();
        }
    }

    public List<DataRace> getDataRaces(boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getDataRaces();
        } finally {
            erp.releaseLock();
        }
    }

    public List<Deadlock> getDeadlocks(boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getDeadlocks();
        } finally {
            erp.releaseLock();
        }
    }

    public String[] getCallersCallees(int limit) {
        // TODO: implemet
        return new String[0];
    }

    public FunctionStatistic getFunctionStatistic(String functionName, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getFunctionStatistic(functionName);
        } finally {
            erp.releaseLock();
        }
    }

    public FunctionStatistic getFunctionStatistic(FunctionCallWithMetric functionCall, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getFunctionStatistic(functionCall);
        } finally {
            erp.releaseLock();
        }
    }

    public String[] getHotFunctions(Metrics metrics, int limit, int timeout, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);

        synchronized (erp) {
            Metrics prev_metrics = null;
            try {
                prev_metrics = erp.setMetrics(metrics);
                String[] result = erp.getHotFunctions(limit);
                return result;
            } finally {
                if (prev_metrics != null) {
                    erp.setMetrics(prev_metrics);
                }
                erp.releaseLock();
            }
        }
    }

    public String[] getHotFunctions(String command, Metrics metrics, int limit, boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);

        synchronized (erp) {
            Metrics prev_metrics = null;
            try {
                prev_metrics = erp.setMetrics(metrics);
                String[] result = erp.getHotFunctions(command, limit);
                return result;
            } finally {
                try {
                    if (prev_metrics != null) {
                        erp.setMetrics(prev_metrics);
                    }
                } finally {
                    erp.releaseLock();
                }
            }
        }
    }
}
