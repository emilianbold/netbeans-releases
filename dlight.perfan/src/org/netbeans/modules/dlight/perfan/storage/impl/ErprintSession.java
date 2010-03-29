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
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.dlight.perfan.spi.datafilter.CollectedObjectsFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.perfan.spi.datafilter.SunStudioFiltersProvider;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;

public class ErprintSession {

    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    private final NativeProcessBuilder npb;
    private volatile Erprint er_print;
    private final SunStudioFiltersProvider dataFiltersProvider;
    private String filterString;
    private volatile boolean ready;
    private volatile boolean error;
    private volatile boolean stopped;

    private ErprintSession(ExecutionEnvironment execEnv, String sproHome, String experimentDirectory, SunStudioFiltersProvider dataFiltersProvider) {
        id = idCounter.incrementAndGet();
        String er_printCmd = sproHome + "/bin/er_print"; // NOI18N
        NativeProcessBuilder erProcessBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
        erProcessBuilder.setExecutable(er_printCmd);
        erProcessBuilder.setWorkingDirectory(experimentDirectory);
        erProcessBuilder.setArguments(experimentDirectory).unbufferOutput(true);
        this.dataFiltersProvider = dataFiltersProvider;

        npb = erProcessBuilder;
        npb.getEnvironment().put("LC_ALL", "C");//NOI18N
    }

    /**
     * Creates new ErprintSession for specified experiment on specified
     * host. Metod returns Future&lt;ErprintSession&gt; that either return
     * session (it is guarantied
     * @param execEnv
     * @param sproHome
     * @param experimentDirectory
     * @param dataFiltersProvider
     * @return
     */
    public static final ErprintSession createNew(final ExecutionEnvironment execEnv, final String sproHome, final String experimentDirectory, final SunStudioFiltersProvider dataFiltersProvider) {
        final ErprintSession session = new ErprintSession(execEnv, sproHome, experimentDirectory, dataFiltersProvider);
        session.ready = false;
        session.error = false;
        session.stopped = false;

        Runnable r = new Runnable() {

            public void run() {
                while (true) {
                    if (session.stopped || Thread.interrupted()) {
                        return;
                    }
                    try {
                        if (!HostInfoUtils.fileExists(execEnv, experimentDirectory + "/overview")) { // NOI18N
                            Thread.sleep(200);
                        } else {
                            session.ready = true;
                            return;
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ConnectException ex) {
                        //Exceptions.printStackTrace(ex);
                        session.error = true;
                        return;
                    } catch (IOException ex) {
                        if( ex.getCause() instanceof InterruptedException) { // should be catch (InterruptedException) instead
                            Thread.currentThread().interrupt();
                        } else {
                            //Exceptions.printStackTrace(ex);
                            session.error = true;
                            return;
                        }
                    }
                }
            }
        };

        // IZ165095: RUN FAILED on remote host (problem description is in the IZ)
        // Submit a task that will set readyness flag when experiment directory is ready

        DLightExecutorService.submit(r, "ErprintSession: session warmup"); // NOI18N

        return session;
    }

    private void applyFilters() {
        if (dataFiltersProvider != null) {
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
        try {
            er_print.setFilter(filterString);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private synchronized Erprint restartAndLock(boolean restart) throws IOException {
        // Never return null, do throw an exception instead. See IZ 173754.
        while (!ready) {
            if (Thread.interrupted()) {
                throw new IOException("Interrupted during warmup"); // NOI18N
            }
            if (error) {
                throw new IOException("Error during warmup"); // NOI18N
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        try {
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
        } catch (Throwable ex) {
            // failed to start er_print ?
        }

        if (er_print == null) {
            throw new IOException("Failed to start er_print"); // NOI18N
        } else {
            return er_print;
        }
    }

    private synchronized void stop_er_print() {
        if (er_print != null) {
            er_print.stop();
            er_print = null;
        }
    }

    public void close() {
        stopped = true;
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

    public Metrics getMetrics(boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getExperimentMetrics();
        } finally {
            erp.releaseLock();
        }
    }

    public List<DataraceImpl> getDataRaces(boolean restart) throws IOException {
        final Erprint erp = restartAndLock(restart);
        try {
            return erp.getDataRaces();
        } finally {
            erp.releaseLock();
        }
    }

    public void setFilter(String filterString) {
        this.filterString = filterString;
    }

    public List<DeadlockImpl> getDeadlocks(boolean restart) throws IOException {
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

    public FunctionStatistic getFunctionStatistic(FunctionCallImpl functionCall, boolean restart) throws IOException {
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

    public String[] getHotFunctions(ErprintCommand command, Metrics metrics, int limit, boolean restart) throws IOException {
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
