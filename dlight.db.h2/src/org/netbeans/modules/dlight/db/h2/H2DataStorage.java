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
package org.netbeans.modules.dlight.db.h2;

import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.storage.SQLStackStorage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

public final class H2DataStorage extends SQLDataStorage implements StackDataStorage {

    private static final String SQL_QUERY_DELIMETER = ";"; // NOI18N
    private static final Logger logger = DLightLogger.getLogger(H2DataStorage.class);
    private static boolean driverLoaded = false;
    private static final AtomicInteger dbIndex = new AtomicInteger();
    private SQLStackStorage stackStorage;
    private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();
    private static final String tmpDir;
    private static final String url;
    private String dbURL;


    static {
        try {
            Class driver = Class.forName("org.h2.Driver"); // NOI18N
            logger.info("Driver for H2DB (" + driver.getName() + ") Loaded "); // NOI18N
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        String tempDir = hi.getTempDir();
        url = "jdbc:h2:" + tempDir + "/dlight"; // NOI18N
        tmpDir = hi.getOSFamily() == HostInfo.OSFamily.WINDOWS
                ? WindowsSupport.getInstance().convertoToWindowsPath(tempDir)
                : tempDir;
    }

    private void initStorageTypes() {
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(H2DataStorageFactory.H2_DATA_STORAGE_TYPE));
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
        supportedStorageTypes.addAll(super.getStorageTypes());
    }

    H2DataStorage() throws SQLException {
        this(url + dbIndex.incrementAndGet());
    }

    private H2DataStorage(String url) throws SQLException {
        super(url);
        dbURL = url;
        try {
            initStorageTypes();
            stackStorage = new SQLStackStorage(this);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    // FIXUP: deleting /tmp/dlight*


    static {
        File tmpDirFile = new File(tmpDir); // NOI18N
        if (tmpDirFile.exists()) {
            File[] files = tmpDirFile.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.startsWith("dlight"); // NOI18N
                }
            });
            int newValue = 0;
            boolean result = true;
            for (int i = 0; i < files.length; i++) {
                boolean localResult = files[i].delete();
                result = result && localResult;
                newValue++;
            }
            if (!result) {
                //should increse dbIndex??
                dbIndex.set(newValue);
            }
        }
    }

    @Override
    public boolean shutdown() {
        boolean result = stackStorage.shutdown() && super.shutdown();
        final String filesToDelete = dbURL.substring(dbURL.lastIndexOf("/") + 1);//NOI18N
        File tmpDirFile = new File(tmpDir); // NOI18N
        if (tmpDirFile.exists()) {
            File[] files = tmpDirFile.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.startsWith(filesToDelete); // NOI18N
                }
            });
            for (int i = 0; i < files.length; i++) {
                result &= files[i].delete();
            }
        }
        return result;
    }

    @Override
    public boolean createTablesImpl(List<DataTableMetadata> tableMetadatas) {
        for (DataTableMetadata tdmd : tableMetadatas) {
            if (!tdmd.getName().equals(STACK_METADATA_VIEW_NAME)) {
                if (!createTable(tdmd)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;
    }

    @Override
    protected void connect(String dburl) throws SQLException {
        connection = DriverManager.getConnection(dburl, "admin", ""); // NOI18N
    }

    public int putStack(List<CharSequence> stack, long sampleDuration) {
        return stackStorage.putStack(stack, sampleDuration);
    }

    public void flush() {
        try {
            stackStorage.flush();
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public List<FunctionMetric> getMetricsList() {
        return stackStorage.getMetricsList();
    }

    public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) {
        try {
            return stackStorage.getPeriodicStacks(startTime, endTime, interval);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) {
        try {
            return stackStorage.getCallers(path, aggregate);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ArrayList<FunctionCall>();
        }
    }

    public List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) {
        try {
            return stackStorage.getCallees(path, aggregate);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ArrayList<FunctionCall>();
        }
    }

    public List<FunctionCall> getHotSpotFunctions(FunctionMetric metric, int limit) {
        return stackStorage.getHotSpotFunctions(metric, limit);

    }

    @Override
    protected String getSQLQueriesDelimeter() {
        return SQL_QUERY_DELIMETER;
    }

    public List<FunctionCall> getFunctionsList(DataTableMetadata metadata, List<Column> metricsColumn, FunctionDatatableDescription functionDescription) {
        return stackStorage.getFunctionsList(metadata, metricsColumn, functionDescription);
    }
}
