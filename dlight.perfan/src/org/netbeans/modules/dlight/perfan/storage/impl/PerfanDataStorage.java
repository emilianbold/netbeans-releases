/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.spi.datafilter.SunStudioFiltersProvider;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.threads.api.Datarace;
import org.netbeans.modules.dlight.threads.api.Deadlock;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

public final class PerfanDataStorage implements PersistentDataStorage {

    public static final String ID = "PerfanDataStorage"; //NOI18N
    public static final DataStorageType storageType = DataStorageTypeFactory.getInstance().getDataStorageType(ID);
    private final static Logger log = DLightLogger.getLogger(PerfanDataStorage.class);
    private volatile ErprintSession er_print;
    private String experimentDirectory = null;
    private ExecutionEnvironment env;
    private final List<DataTableMetadata> tableMetadatas;
    private ServiceInfoDataStorage serviceInfoDataStorage;
    private volatile boolean isOMPExperiment = false;

    public PerfanDataStorage() {
        super();
        tableMetadatas = new ArrayList<DataTableMetadata>();
    }

    public final void attachTo(ServiceInfoDataStorage serviceInfoStorage) {
        this.serviceInfoDataStorage = serviceInfoStorage;
    }

    @Override
    public boolean shutdown() {
        if (er_print != null) {
            er_print.close();
        }
        if (experimentDirectory != null) {
            StringWriter writer = new StringWriter();
            CommonTasksSupport.rmDir(env, experimentDirectory, true, writer);
            return writer.toString().trim().equals("");
        }
        return true;
    }

    public void setFilter(String filter) {
        er_print.setFilter(filter);
    }

    public boolean hasOMPCollected() {
        //TODO: re-write, should ask one time per experiment only
        try {
            isOMPExperiment = false;
            Metrics metrics = er_print.getMetrics(true);
            if (metrics.mspec.indexOf("omp") != -1) {//NOI18N
                isOMPExperiment = true;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return isOMPExperiment;
    }

    public void init(ExecutionEnvironment execEnv, String sproHome,
            String experimentDirectory, SunStudioFiltersProvider dataFiltersProvider) {
        synchronized (this) {
            if (er_print != null) {
                er_print.close();
            }
            er_print = ErprintSession.createNew(execEnv, sproHome, experimentDirectory, dataFiltersProvider);
        }
    }

    // TODO: implement!
    public Object[] getCallees(long ref) {
        return null;
    }

    // TODO: implement!
    public Object[] getCallers(long ref) {
        return null;
    }

    /**
     * For now assume that getTopFunctions is a method that forces er_print restart...
     * TODO: change the behavior later...
     */
    public String[] getTopFunctions(Metrics metrics, int limit) {
        String[] result = null;

        try {
            result = er_print.getHotFunctions(metrics, limit, 0, true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting functions list...
        } catch (IOException ex) {
            log.log(Level.FINEST, "getTopFunctions: " + ex.toString()); // NOI18N
        }

        return result == null ? new String[0] : result;
    }

    public Metrics getCollectedMetrics() {
        try {
            return er_print.getMetrics(true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting metrics...
        } catch (IOException ex) {
            log.log(Level.FINEST, "getMetrics: " + ex.toString()); // NOI18N
        }

        return Metrics.constructFrom(
                Arrays.asList(SunStudioDCConfiguration.c_name),
                Arrays.asList(SunStudioDCConfiguration.c_name));
    }

    public String[] getTopFunctions(ErprintCommand command, Metrics metrics, int limit) throws InterruptedException {
        String[] result = null;

        try {
            result = er_print.getHotFunctions(command, metrics, limit, true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting functions list...
            throw new InterruptedException();
        } catch (IOException ex) {
            log.log(Level.FINEST, "getTopFunctions: " + ex.toString()); // NOI18N
        }

        return result;
    }

    public FunctionStatistic getFunctionStatistic(String function) {
        FunctionStatistic result = null;

        try {
            result = er_print.getFunctionStatistic(function, false);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting functions list...
        } catch (IOException ex) {
            log.log(Level.WARNING, null, ex);
        }

        return result;
    }

    public FunctionStatistic getFunctionStatistic(FunctionCallImpl functionCall) {
        FunctionStatistic result = null;

        try {
            result = er_print.getFunctionStatistic(functionCall, false);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting functions list...
        } catch (IOException ex) {
            log.log(Level.WARNING, null, ex);
        }

        return result;
    }

    public List<? extends Datarace> getDataraces() {
        List<DataraceImpl> result = null;

        try {
            result = er_print.getDataRaces(true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting deadlocks list...
        } catch (IOException ex) {
            log.log(Level.INFO, null, ex);
        }

        return result == null ? Collections.<DataraceImpl>emptyList() : result;
    }

    public List<? extends Deadlock> getDeadlocks() {
        List<DeadlockImpl> result = null;

        try {
            result = er_print.getDeadlocks(true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting deadlocks list...
        } catch (IOException ex) {
            log.log(Level.INFO, null, ex);
        }

        return result == null ? Collections.<DeadlockImpl>emptyList() : result;
    }

    public ExperimentStatistics fetchSummaryData() {
        ExperimentStatistics result = null;

        try {
            result = er_print.getExperimentStatistics(0, true);
        } catch (InterruptedIOException ex) {
            // it was terminated while getting functions list...
        } catch (IOException ex) {
            log.log(Level.WARNING, null, ex);
        }

        return result;
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return PerfanDataStorageFactory.supportedTypes;
    }

    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    public boolean hasData(DataTableMetadata data) {
        return data.isProvidedBy(tableMetadatas);
    }

    @Override
    public void createTables(List<DataTableMetadata> tableMetadatas) {
        this.tableMetadatas.addAll(tableMetadatas);
    }

    @Override
    public void addData(String tableName, List<DataRow> data) {
        // Adding data is not supported for er_print
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void syncAddData(String tableName, List<DataRow> data) {
        throw new UnsupportedOperationException("Not supported yet.");// NOI18N
    }
    

    @Override
    public void loadSchema() {
        
    }


}
