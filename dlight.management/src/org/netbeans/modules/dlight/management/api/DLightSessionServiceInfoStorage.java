/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.ForeignKeyConstraint;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.SQLDataStorage;
import org.netbeans.modules.dlight.spi.support.SQLExceptions;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

/**
 *  You can use the storage to store any information which should be persistent for the session
 *  The
 */
public final class DLightSessionServiceInfoStorage extends SQLDataStorage implements ServiceInfoDataStorage {

    private static final String SQL_QUERY_DELIMETER = ";"; // NOI18N
    private static final Logger logger = DLightLogger.getLogger(DLightSessionServiceInfoStorage.class);
    static String DLIGHT_SERVICE_INFO_H2_DATABASE_URL;
    private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();
    String dbURL;
    static {
        String tempDir = null;
        try {
            HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
            tempDir = hi.getTempDir();
            if (hi.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                tempDir = WindowsSupport.getInstance().convertToWindowsPath(tempDir);
            }
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (tempDir == null || tempDir.trim().equals("")) {// NOI18N
            tempDir = System.getProperty("java.io.tmpdir"); // NOI18N
        }
        if (System.getProperty("dlight.storages.host.url") != null) {
            DLIGHT_SERVICE_INFO_H2_DATABASE_URL = System.getProperty("dlight.storages.host.url");
        } else if (PersistentDataStorageFactory.PERSISTENT_DATA_STORAGE_HOST != null) {
            String host = PersistentDataStorageFactory.PERSISTENT_DATA_STORAGE_HOST;
            String analytics_serviceinfo_folder = System.getProperty("dlight.storages.host.folder", "/export/home/analytics");
            DLIGHT_SERVICE_INFO_H2_DATABASE_URL = "jdbc:h2:tcp://" + host + analytics_serviceinfo_folder;
        } else {

            DLIGHT_SERVICE_INFO_H2_DATABASE_URL = "jdbc:h2:" + tempDir + "/service_info_h2_db_dlight"; // NOI18N
        }
    }
    public static final DataTableMetadata.Column ID_COLUMN =
            new DataTableMetadata.Column("id", Integer.class); // NOI18N
    public static final DataTableMetadata.Column SERVICE_INFO_NAME =
            new DataTableMetadata.Column("name", String.class); // NOI18N
    public static final DataTableMetadata.Column SERVICE_INFO_VALUE =
            new DataTableMetadata.Column("value", String.class); // NOI18N
    public static final DataTableMetadata SERVICE_INFO_TABLE = new DataTableMetadata(
            "ServiceInfo", // NOI18N
            Arrays.asList(ID_COLUMN, SERVICE_INFO_NAME, SERVICE_INFO_VALUE),
            null);
    private final Map<String, String> serviceInfoMap = new ConcurrentHashMap<String, String>();

    static {
        try {
            Class.forName("org.h2.Driver"); // NOI18N
            logger.fine("Driver for H2DB Loaded "); // NOI18N
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    DLightSessionServiceInfoStorage(String storageUniq) {
        super(DLIGHT_SERVICE_INFO_H2_DATABASE_URL + storageUniq + ";FILE_LOCK=NO");
        dbURL = storageUniq;
    }

    @Override
    public String getSQLQueriesDelimeter() {
        return SQL_QUERY_DELIMETER;
    }

    @Override
    public String getAutoIncrementExpresion() {
        return "AUTO_INCREMENT"; // NOI18N
    }

    @Override
    public String getPrimaryKeyExpression() {
        return "PRIMARY KEY"; // NOI18N
    }

    @Override
    public String createForeignKeyConstraint(ForeignKeyConstraint fKey) {
        return " FOREIGN KEY (" + fKey.getColumn().getColumnName() + ") REFERENCES " + // NOI18N
                fKey.getReferenceTable().getName() + "(" + fKey.getReferenceColumn().getColumnName() + ") "; // NOI18N
    }

    @Override
    public void createTables(List<DataTableMetadata> tableMetadatas) {
        for (DataTableMetadata tdmd : tableMetadatas) {
            if (tdmd == null) {
                continue;
            }
            //if (!tdmd.getName().equals(STACK_METADATA_VIEW_NAME)) {
            createTable(tdmd);
            //}
            this.tables.put(tdmd.getName(), tdmd);
        }
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;
    }

    @Override
    protected final Connection doConnect() throws SQLException {
        return DriverManager.getConnection(getDbURL(), "admin", ""); // NOI18N
    }

    @Override
    protected void postConnectInit() {
        super.postConnectInit();
        loadSchema();
        initTables();
    }

    private void initTables() {
        //all tables should be created if there are no needed tables
        //create list of names
        DataTableMetadata[] requiredTables = new DataTableMetadata[]{SERVICE_INFO_TABLE};
        List<String> existingTables = new ArrayList<String>();
        for (DataTableMetadata table : tables.values()) {
            existingTables.add(table.getName().toLowerCase());
        }
        for (DataTableMetadata table : requiredTables) {
            if (!existingTables.contains(table.getName().toLowerCase())) {
                createTable(table);
            }
        }
    }

    @Override
    public void loadSchema() {
        try {
            ResultSet rs = select("INFORMATION_SCHEMA.TABLES", null, // NOI18N
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE LIKE 'TABLE'"); // NOI18N
            if (rs == null) {
                return;
            }
            while (rs.next()) {
                String tableName = rs.getString(1);
                loadTable(tableName);
            }
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(this, ex);
        }
    }

    private void loadTable(String tableName) {
        try {
            ResultSet rs = select("INFORMATION_SCHEMA.COLUMNS", null, // NOI18N
                    "SELECT COLUMN_NAME, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE '" + tableName + "'"); // NOI18N
            List<Column> columns = new ArrayList<Column>();
            while (rs.next()) {
                Column c = new Column(rs.getString("COLUMN_NAME"), typeToClass(rs.getString("TYPE_NAME"))); // NOI18N
                columns.add(c);
            }
            DataTableMetadata result = new DataTableMetadata(tableName, columns, null);
            super.loadTable(result);
            tables.put(result.getName(), result);
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(this, ex);
        }
    }

    @Override
    public boolean hasData(DataTableMetadata data) {
        List<DataTableMetadata> toCheck = new ArrayList<DataTableMetadata>(tables.values());
        return data.isProvidedBy(toCheck);
    }

    @Override
    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    @Override
    public Map<String, String> getInfo() {
        //read the table
        if (serviceInfoMap.isEmpty()) {
            String sql = "SELECT * FROM " + SERVICE_INFO_TABLE;//NOI18N

            try {
                PreparedStatement stat = getConnection().prepareStatement(sql);
                ResultSet rs = stat.executeQuery();
                while (rs.next()) {
                    serviceInfoMap.put(rs.getString(SERVICE_INFO_NAME.getColumnName()),
                            SERVICE_INFO_VALUE.getColumnName());
                }

            } catch (SQLException ex) {
                SQLExceptions.printStackTrace(this, ex);
            }


        }
        return serviceInfoMap;
    }

    @Override
    public String getValue(String name) {
        return getInfo().get(name);
    }

    @Override
    public String put(String name, String value) {
        //throw new UnsupportedOperationException("Not supported yet.");
        String oldValue = serviceInfoMap.put(name, value);
        String sqlString = "";//NOI18N
        if (oldValue == null) {
            sqlString = "INSERT INTO " + SERVICE_INFO_TABLE.getName() + "("//NOI18N
                    + SERVICE_INFO_NAME.getColumnName() + ", " // NOI18N
                    + SERVICE_INFO_VALUE.getColumnName() + ") " + " VALUES ("//NOI18N
                    + "'" + name + "', '" + value + "'" + ")";//NOI18N
        } else {
            sqlString = " UPDATE " + SERVICE_INFO_TABLE.getName()//NOI18N
                    + " SET value='" + value + "' WHERE "//NOI18N
                    + SERVICE_INFO_NAME.getColumnName() + "='" + name + "'";//NOI18N
        }
        try {

            PreparedStatement stat = getConnection().prepareStatement(sqlString);
            stat.executeUpdate();
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(this, ex);
        }
        return oldValue;

    }
}
