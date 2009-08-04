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
package org.netbeans.modules.dlight.impl;

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.dlight.api.storage.DataRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;

/**
 *
 */
public abstract class SQLDataStorage extends DataStorage {

    public static final String SQL_DATA_STORAGE_TYPE = "db:sql"; // NOI18N

    private interface Request {

        void execute() throws SQLException;
    }

    private static abstract class BaseRequest implements Request {

        abstract PreparedStatement getPreparedStatement() throws SQLException;

        public void execute() throws SQLException {
            PreparedStatement statement = getPreparedStatement();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SQL: dispatching insert  " + statement.toString()); //NOI18N
            }
            statement.execute();
        }
    }

    private static class CustomRequest extends BaseRequest {

        PreparedStatement preparedStatement;

        public CustomRequest(PreparedStatement preparedStatement) {
            this.preparedStatement = preparedStatement;
        }

        @Override
        PreparedStatement getPreparedStatement() throws SQLException {
            return preparedStatement;
        }
    }

    private class DataRowInsertRequest extends BaseRequest {

        final String tableName;
        final DataRow dataRow;

        public DataRowInsertRequest(String tableName, DataRow dataRow) {
            this.tableName = tableName;
            this.dataRow = dataRow;
        }

        @Override
        PreparedStatement getPreparedStatement() throws SQLException {
            DataTableMetadata tableMetadata = tables.get(tableName);
            PreparedStatement statement = getPreparedInsertStatement(tableMetadata);
            if (statement == null) {
                return createRowInsertStatement(tableName, dataRow);
            }
            List<Column> columns = tableMetadata.getColumns();
            List<String> columnNames = dataRow.getColumnNames();
            if (columnNames.size() != columns.size()) {
                // column count differs - should still create prepared statement as before
                // TODO: what if count is the same, but names differ???
                return createRowInsertStatement(tableName, dataRow);
            }

            for (int i = 0, size = columns.size(); i < size; i++) {
                Column c = columns.get(i);
                statement.setObject(i + 1, dataRow.getData(c.getColumnName()));
            }

            return statement;
        }

        @Override
        public String toString() {
            return "insert into " + tableName + ": " + dataRow.toString(); // NOI18N
        }
    }
    private LinkedBlockingQueue<Request> requestQueue;
    private final Object insertPreparedStatmentsLock = new Object();
    private final Map<String, PreparedStatement> insertPreparedStatments;
    private static final int WAIT_INTERVALS = 100;
    private static final int MAX_BULK_SIZE = 10000;
    private static final int BUFFER_COUNT = 6;
    private static final Logger logger = DLightLogger.getLogger(SQLDataStorage.class);
    protected Connection connection;
    protected HashMap<String, DataTableMetadata> tables = new HashMap<String, DataTableMetadata>();
    protected static final HashMap<Class, String> classToType = new HashMap<Class, String>();
    private boolean enabled = false;
    private AsyncThread asyncThread = null;
    private final Map<String, PreparedStatement> stmts;
    private final Map<String, String> serviceInfoMap = new ConcurrentHashMap<String, String>();


    static {
        classToType.put(Integer.class, "int"); //NOI18N
        classToType.put(Double.class, "double"); //NOI18N
        classToType.put(Float.class, "double"); //NOI18N
        classToType.put(Long.class, "bigint"); //NOI18N
        classToType.put(String.class, "varchar"); //NOI18N
    }

    protected SQLDataStorage() {
        stmts = new HashMap<String, PreparedStatement>();
        insertPreparedStatments = new HashMap<String, PreparedStatement>();
    }

    protected SQLDataStorage(String dburl) throws SQLException {
        this();
        connect(dburl);
        if (!enabled) {
            enable();
        }
    }

    public boolean shutdown() {
        disable();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Different SQL storages can have different strings as delimiter between
     * queries. As an example in MySQL ';' is using as queries delimiter and
     * in Derby you should not use any.
     * @return delimiter which will be added to all statements execution.
     */
    abstract protected String getSQLQueriesDelimeter();

    @Override
    protected void finalize() throws Throwable {
        disable();
    }

    private synchronized void disable() {
        asyncThread.shutdown();
        enabled = false;
        asyncThread = null;
    }

    protected abstract void connect(String dburl) throws SQLException;

    protected String classToType(Class clazz) {
        return classToType.get(clazz);
    }

    protected final boolean createTable(final DataTableMetadata metadata) {
        if (tables.containsKey(metadata.getName())) {
            return true;
        }
        final String tableName = metadata.getName();
        StringBuilder sb = new StringBuilder("create table " + tableName + "("); //NOI18N
        sb.append(new EnumStringConstructor<DataTableMetadata.Column>().constructEnumString(metadata.getColumns(),
                new Convertor<DataTableMetadata.Column>() {

                    public String toString(DataTableMetadata.Column item) {
                        return item.getColumnName() + " " + classToType(item.getColumnClass()); //NOI18N
                    }
                }));
        sb.append(")" + getSQLQueriesDelimeter()); //NOI18N

        logger.fine("About to execute query: " + sb.toString()); //NOI18N

        try {
            connection.prepareCall(sb.toString()).execute();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }

        logger.fine("Table " + tableName + " created"); //NOI18N

        tables.put(tableName, metadata);

        for (Column col : metadata.getIndexedColumns()) {
            try {
                connection.prepareStatement(
                        "create index " + tableName + "_" // NOI18N
                        + col.getColumnName() + "_index on " // NOI18N
                        + tableName + "(" + col.getColumnName() + ")").execute(); // NOI18N
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        DLightExecutorService.submit(new Runnable() {

            public void run() {
                getPreparedInsertStatement(metadata);
            }
        }, "SQL: Prepare Insert Statement for " + metadata.getName()); // NOI18N
        return true;
    }

    private PreparedStatement getPreparedInsertStatement(DataTableMetadata tableDescription) {
        PreparedStatement statement;
        synchronized (insertPreparedStatmentsLock) {
            statement = insertPreparedStatments.get(tableDescription.getName());
            if (statement != null) {
                return statement;
            }
            String tableName = tableDescription.getName();
            StringBuilder query = new StringBuilder("insert into " + tableName + " ("); //NOI18N
            query.append(new EnumStringConstructor<String>().constructEnumString(tableDescription.getColumnNames(),
                    new Convertor<String>() {

                        public String toString(String item) {
                            return item;
                        }
                    }));

            query.append(") values ("); //NOI18N
            int i = 0;
            int columnsCount = tableDescription.getColumnsCount();
            while (i < columnsCount - 1) {
                query.append("?, "); //NOI18N
                i++;
            }
            query.append("? ) " + getSQLQueriesDelimeter()); //NOI18N

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SQL: dispatching " + query.toString()); //NOI18N
            }

            try {
                synchronized (insertPreparedStatments) {
                    insertPreparedStatments.put(tableName, connection.prepareStatement(query.toString()));
                }
            //insert(tableName, row);
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return statement;
        }
    }

    public ResultSet select(String tableName, List<Column> columns) {
        return select(tableName, columns, null);
    }

    public Collection<DataStorageType> getStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQL_DATA_STORAGE_TYPE));
    }

    public ResultSet select(String tableName, List<Column> columns, String sqlQuery) {

        if (sqlQuery == null) {
            StringBuilder query = new StringBuilder("select "); //NOI18N

            query.append(new EnumStringConstructor<Column>().constructEnumString(columns,
                    new Convertor<Column>() {

                        public String toString(Column item) {
                            return (item.getExpression() == null) ? item.getColumnName() : item.getExpression();
                        }
                    }));

            query.append(" from ").append(tableName); //NOI18N
            sqlQuery = query.toString();
        }

        ResultSet rs = null;
        try {
            rs = connection.prepareCall(sqlQuery).executeQuery();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return rs;
    }

    protected final Connection getConnection() {
        return connection;
    }

    public final void execute(BufferedReader reader) throws SQLException, IOException {
        String line;
        StringBuilder buf = new StringBuilder();
        Statement s = connection.createStatement();
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("-- ")) { //NOI18N
                continue;
            }
            buf.append(line);
            if (line.endsWith(";")) { //NOI18N
                String sql = buf.toString();
                buf.setLength(0);
                String sqlToExecute = sql.substring(0, sql.length() - 1) + getSQLQueriesDelimeter();
                s.execute(sqlToExecute);
            }
        }
        s.close();
    }

    protected final void execute(String sql) throws SQLException {
        requestQueue.add(new CustomRequest(connection.prepareStatement(sql)));
    }

    protected void addInsertInQueue(PreparedStatement st) {
        requestQueue.add(new CustomRequest(st));
    }

    public final void addData(String tableName, List<DataRow> data) {
        for (DataRow row : data) {
            requestQueue.add(new DataRowInsertRequest(tableName, row));
        }
    }

    /**
     * Returns a prepareed statement for insertion of the row into the table
     * NB: FILLS the statement with data
     */
    private PreparedStatement createRowInsertStatement(String tableName, DataRow row) {
        if (logger.isLoggable(Level.INFO)) {
            logger.fine("Will add to the queue with using prepared statement"); //NOI18N
        }
        StringBuilder query = new StringBuilder("insert into " + tableName + " ("); //NOI18N

        query.append(new EnumStringConstructor<String>().constructEnumString(row.getColumnNames(),
                new Convertor<String>() {

                    public String toString(String item) {
                        return item;
                    }
                }));

        query.append(") values ("); //NOI18N

        query.append(new EnumStringConstructor<Object>().constructEnumString(row.getData(),
                new Convertor<Object>() {

                    public String toString(Object item) {
                        return String.valueOf(item);
                    }
                }));

        query.append(")" + getSQLQueriesDelimeter()); //NOI18N
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("----------SQL: dispatching " + query.toString()); //NOI18N
        }
        try {
            return connection.prepareStatement(query.toString());
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private void enable() {
        if (!enabled) {
            requestQueue = new LinkedBlockingQueue<Request>();
            enabled = true;
            if (asyncThread == null) {
                asyncThread = new AsyncThread();
            }
            asyncThread.start();
        }
    }

    public final PreparedStatement prepareStatement(String sql) throws SQLException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SQL: prepare statement " + sql); //NOI18N
        }
        PreparedStatement stmt = stmts.get(sql);
        if (stmt == null) {
            if (sql.startsWith("INSERT INTO ")) { //NOI18N
                stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                stmt = connection.prepareStatement(sql);
            }
            stmts.put(sql, stmt);
        }
        return stmt;
    }

    public final Map<String, String> getInfo() {
        return serviceInfoMap;
    }

    public final String getValue(String name) {
        return serviceInfoMap.get(name);
    }

    public final String put(String name, String value) {
        return serviceInfoMap.put(name, value);
    }

    public static final class EnumStringConstructor<T> {

        public String constructEnumString(Collection<? extends T> collection, Convertor<T> conv) {
            StringBuilder sb = new StringBuilder();
            Iterator<? extends T> i = collection.iterator();
            T item;

            while (i.hasNext()) {
                item = i.next();
                sb.append(conv.toString(item));
                if (i.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }

            return sb.toString();
        }
    }

    public interface Convertor<T> {

        public String toString(T item);
    }

    private class AsyncThread extends Thread {

        private boolean shutdown;
        private int emptyBufferCount;
        List<Request> requestList = new ArrayList<Request>();

        public AsyncThread() {
            setDaemon(true);
            setName("DLIGHT: SQL Storage AsyncThread"); // NOI18N
        }

        @Override
        public void run() {
            while (emptyBufferCount < BUFFER_COUNT) {
                for (int i = 0; i < MAX_BULK_SIZE; i++) {
                    try {
                        Request request = requestQueue.poll(WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (request == null) {
                            break;
                        }
                        requestList.add(request);
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    if (requestList.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        for (Request request : requestList) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("EXECUTEEEEEEEEEEEEEEE !!!SQL: dispatching request  " + request.toString()); //NOI18N
                            }

                            request.execute();
                        }

                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "SQLDataStorage.async_db_write_failed", //NOI18N
                            e);
                }
                requestList.clear();


            }
        }

        private void shutdown() {
            shutdown = true;
            while (emptyBufferCount < BUFFER_COUNT) {
                try {
                    Thread.sleep(WAIT_INTERVALS);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private class AsyncReadThread extends Thread {

        private boolean shutdown;
        private int emptyBufferCount;
        List<Request> requestList = new ArrayList<Request>();

        public AsyncReadThread() {
            setDaemon(true);
            setName("DLIGHT: SQL Storage AsyncFillModelThread"); // NOI18N
        }

        @Override
        public void run() {
            while (emptyBufferCount < BUFFER_COUNT) {
                for (int i = 0; i < MAX_BULK_SIZE; i++) {
                    try {
                        Request request = requestQueue.poll(WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (request == null) {
                            break;
                        }
                        requestList.add(request);
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    if (requestList.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        for (Request request : requestList) {
                            request.execute();
                        }

                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "SQLDataStorage_db_write_failed", //NOI18N
                            e);
                }
                requestList.clear();


            }
        }

        private void shutdown() {
            shutdown = true;
            while (emptyBufferCount < BUFFER_COUNT) {
                try {
                    Thread.sleep(WAIT_INTERVALS);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
