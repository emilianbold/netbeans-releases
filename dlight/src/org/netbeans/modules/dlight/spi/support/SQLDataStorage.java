/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.support;

import org.netbeans.modules.dlight.spi.support.impl.SQLBaseRequest;
import org.netbeans.modules.dlight.spi.support.impl.SQLRequestsProcessorImpl;
import org.netbeans.modules.dlight.api.storage.DataRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataTableMetadataFilter;
import org.netbeans.modules.dlight.api.storage.ForeignKeyConstraint;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.impl.SQLConnection;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Range;

/**
 *
 */
public abstract class SQLDataStorage implements PersistentDataStorage {

    public static final String SQL_DATA_STORAGE_TYPE = "db:sql"; // NOI18N
    private static final Logger logger = DLightLogger.getLogger(SQLDataStorage.class);
    private static final HashMap<Class<?>, String> classToType = new HashMap<Class<?>, String>();
    private static final DataStorageType storageType =
            DataStorageTypeFactory.getInstance().getDataStorageType(SQL_DATA_STORAGE_TYPE);
    protected final HashMap<String, DataTableMetadata> tables;
    private final ConcurrentHashMap<DataTableMetadata, String> tblMetadataToInsertSQL;
    private final SQLRequestsProcessorImpl requestProcessor;
    private final SQLStatementsCache statementsCache;
    private final String dburl;
    private final SQLConnection connection = new SQLConnection();
    private volatile ServiceInfoDataStorage serviceInfoDataStorage;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    static {
        classToType.put(Byte.class, "tinyint");     // NOI18N
        classToType.put(Short.class, "smallint");   // NOI18N
        classToType.put(Integer.class, "int");      // NOI18N
        classToType.put(Long.class, "bigint");      // NOI18N
        classToType.put(Double.class, "double");    // NOI18N
        classToType.put(Float.class, "real");       // NOI18N
        classToType.put(String.class, "varchar");   // NOI18N
        classToType.put(Time.class, "bigint");      // NOI18N
    }

    public static DataStorageType getStorageType() {
        return storageType;
    }

    protected SQLDataStorage(String dburl) {
        this.dburl = dburl;
        tables = new HashMap<String, DataTableMetadata>();
        tblMetadataToInsertSQL = new ConcurrentHashMap<DataTableMetadata, String>();
        requestProcessor = new SQLRequestsProcessorImpl(5000, 200, TimeUnit.MILLISECONDS);
        statementsCache = SQLStatementsCache.getFor(SQLDataStorage.this);
    }

    @Override
    public final void attachTo(ServiceInfoDataStorage serviceInfoStorage) {
        this.serviceInfoDataStorage = serviceInfoStorage;
    }

    protected final ServiceInfoDataStorage getServiceInfoDataStorage() {
        return serviceInfoDataStorage;
    }

    /**
     * Different SQL storages can have different strings as delimiter between
     * queries. As an example in MySQL ';' is using as queries delimiter and
     * in Derby you should not use any.
     * @return delimiter which will be added to all statements execution.
     */
    abstract public String getSQLQueriesDelimeter();

    /**
     * Different SQL Storages can have different expression for primary key definition
     * @return String which defines primary Key
     */
    abstract public String getPrimaryKeyExpression();

    /**
     * Different SQL storages can have different expression to define Auto Increment
     * @return auto increment expression, in not supported return <code>null</code>
     */
    abstract public String getAutoIncrementExpresion();

    /**
     * Returns String which should be executed to create Foreign Key constraint
     * @param fKey the foreign key constraint
     * @return the SQL query to execute
     */
    abstract public String createForeignKeyConstraint(ForeignKeyConstraint fKey);

    public synchronized final void connect() throws SQLException {
        connection.connect(doConnect());
        postConnectInit();
    }

    protected abstract Connection doConnect() throws SQLException;

    protected void postConnectInit() {
    }

    protected final String getDbURL() {
        return dburl;
    }

    protected String classToType(Class<?> clazz) {
        return classToType.get(clazz);
    }

    protected Class<?> typeToClass(String type) {
        Set<Class<?>> clazzes = classToType.keySet();
        for (Class<?> clazz : clazzes) {
            if (classToType.get(clazz).equalsIgnoreCase(type)) {
                return clazz;
            }
        }
        return String.class;
    }

    /**
     *
     * @param filters
     * @param tableName
     * @param columns
     * @return <code>null</code> if the view was not created
     */
    protected final String createView(Collection<DataTableMetadataFilter> filters, String tableName, List<Column> columns) {
        if (filters == null || filters.isEmpty()) {
            return tableName;
        }

        String viewName = tableName + "_DLIGHT_VIEW"; // NOI18N

        connection.execute("DROP VIEW IF EXISTS " + viewName); // NOI18N

        StringBuilder createViewQuery = new StringBuilder("CREATE  VIEW " + viewName + " AS "); // NOI18N
        createViewQuery.append("SELECT "); // NOI18N
        createViewQuery.append(new EnumStringConstructor<Column>().constructEnumString(columns,
                new Convertor<Column>() {

                    @Override
                    public String toString(Column item) {
                        return (item.getExpression() == null) ? item.getColumnName() : (item.getExpression() + " AS " + item.getColumnName()); // NOI18N
                    }
                }));
        createViewQuery.append(" FROM ").append(tableName); // NOI18N
        //check if we can create WHERE expression
        String whereClause = null;
        for (DataTableMetadataFilter filter : filters) {
            Column filterColumn = filter.getFilteredColumn();
            if (columns.contains(filterColumn)) {
                Range<?> range = filter.getNumericDataFilter().getInterval();
                if (range.getStart() != null || range.getEnd() != null) {
                    // TODO: this is a workaround ...
                    if (filterColumn.getColumnName().equals("bucket")) { // NOI18N
                        Number start = range.getStart();
                        Number end = range.getEnd();
                        range = new Range<Long>(start.longValue() / 1000000000, end.longValue() / 1000000000);
                    }
                    whereClause = range.toString(" WHERE ", "%d <= " + filterColumn.getColumnName(), " AND ", filterColumn.getColumnName() + " <= %d", null); // NOI18N
                    break;
                }
            }
        }

        if (whereClause != null) {
            createViewQuery.append(whereClause);
        } else {
            return tableName;
        }

        if (!connection.execute(createViewQuery.toString())) {
            return tableName;
        }

        return viewName;
    }

    protected final void loadTable(final DataTableMetadata metadata) {
        tables.put(metadata.getName(), metadata);
    }

    protected final boolean createTable(final DataTableMetadata metadata) {
        if (tables.containsKey(metadata.getName())) {
            return true;
        }

        final String tableName = metadata.getName();
        List<DataTableMetadata> sourceTables = metadata.getSourceTables();
        if (sourceTables!= null){
            for (final DataTableMetadata dt : sourceTables){
                createTable(dt);
            }
            if (!tables.containsKey(metadata.getName())) {
                tables.put(tableName, metadata);
            }
            return true;
        }
        StringBuilder sb = new StringBuilder("CREATE TABLE " + tableName + "("); // NOI18N
        sb.append(new EnumStringConstructor<DataTableMetadata.Column>().constructEnumString(metadata.getColumns(),
                new Convertor<DataTableMetadata.Column>() {

                    @Override
                    public String toString(DataTableMetadata.Column item) {
                        return item.getColumnName() + " " + classToType(item.getColumnClass()) // NOI18N
                                + " " + (metadata.isAutoIncrement(item) && getAutoIncrementExpresion() != null ? getAutoIncrementExpresion() : "") // NOI18N
                                + " " + (metadata.isPrimaryKey(item) && getPrimaryKeyExpression() != null ? getPrimaryKeyExpression() : ""); // NOI18N
                    }
                }));
        //now append foreign key constraints (if any)
        List<ForeignKeyConstraint> fKeys = metadata.getForeignKeyConstraints();
        if (!fKeys.isEmpty()){
            //first check if table exists if not call createTable
            for (ForeignKeyConstraint fKey : fKeys){
                DataTableMetadata referenceTable = fKey.getReferenceTable();
                if (!tables.containsKey(metadata.getName())) {
                    createTable(referenceTable);
                }
                sb.append(", ").append(createForeignKeyConstraint(fKey));//NOI18N
            }
        }
        sb.append(")").append(getSQLQueriesDelimeter()); // NOI18N

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "About to execute query: {0}", sb.toString()); // NOI18N
        }

        if (!connection.execute(sb.toString())) {
            return false;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Table {0} created", tableName); // NOI18N
        }

        tables.put(tableName, metadata);

        for (Column col : metadata.getIndexedColumns()) {
            connection.execute("create index " + tableName + "_" // NOI18N
                    + col.getColumnName() + "_index on " // NOI18N
                    + tableName + "(" + col.getColumnName() + ")"); // NOI18N
        }

        DLightExecutorService.submit(new Runnable() {

            @Override
            public void run() {
                getPreparedInsertStatement(metadata);
            }
        }, "SQL: Prepare Insert Statement for " + metadata.getName()); // NOI18N
        return true;
    }

    public PreparedStatement getPreparedInsertStatement(DataTableMetadata tableDescription, DataRow row) {
        String tableName = tableDescription.getName();
        if (tables.get(tableName) == null) {
            createTable(tableDescription);
            tableDescription = tables.get(tableName);
        }
        return createRowInsertStatement(tableDescription, row);
    }

    private PreparedStatement getPreparedInsertStatement(final DataTableMetadata tableDescription) {
        String sql = tblMetadataToInsertSQL.get(tableDescription);

        if (sql == null) {
            sql = createInsertQuery(tableDescription);
            tblMetadataToInsertSQL.putIfAbsent(tableDescription, sql);
        }

        String tableName = tableDescription.getName();

        if (!tables.containsKey(tableName)) {
            synchronized (tables) {
                if (!tables.containsKey(tableName)) {
                    if (!createTable(tableDescription)) {
                        return null;
                    }
                }
            }
        }

        PreparedStatement result = null;

        try {
            result = statementsCache.getPreparedStatement(sql);
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(this, ex);
        }

        return result;
    }

    private String createInsertQuery(DataTableMetadata tableDescription) {
        String tableName = tableDescription.getName();
        StringBuilder query = new StringBuilder("insert into " + tableName + " ("); // NOI18N
        query.append(new EnumStringConstructor<String>().constructEnumString(tableDescription.getColumnNames(),
                new Convertor<String>() {

                    @Override
                    public String toString(String item) {
                        return item;
                    }
                }));

        query.append(") values ("); // NOI18N

        int i = 0;
        int columnsCount = tableDescription.getColumnsCount();

        while (i < columnsCount - 1) {
            query.append("?, "); // NOI18N
            i++;
        }

        query.append("? ) ").append(getSQLQueriesDelimeter()); // NOI18N
        return query.toString();
    }

    public ResultSet select(String tableName, List<Column> columns) {
        return select(tableName, columns, (String)null);
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQL_DATA_STORAGE_TYPE));
    }

    public final synchronized ResultSet select(DataTableMetadata metadata, Collection<DataTableMetadataFilter> filters) {
        // synchronized -- fix for IZ 171779

        //if we have filters for the column we should create view First
        String tableName = metadata.getName();
        String sqlQuery = metadata.getViewStatement();
        List<Column> columns = metadata.getColumns();
        //apply to source tables if any
        List<DataTableMetadata> sourceTables = metadata.getSourceTables();
        Map<String, String> renamedTableNames = new HashMap<String, String>();
        if (sourceTables != null) {
            for (DataTableMetadata sourceTable : sourceTables) {
                String viewName = createView(filters, sourceTable.getName(), sourceTable.getColumns());
                if (viewName != null && !viewName.equals(sourceTable.getName())) {
                    renamedTableNames.put(sourceTable.getName(), viewName);
                }
            }
        } else {
            String viewName = createView(filters, tableName, columns);
            if (viewName != null && viewName.equals(tableName)) {
                return select(tableName, columns, sqlQuery);
            }
            if (sqlQuery == null) {
                return select(viewName, columns, (String)null);
            }
        }
        String sqlQueryNew = sqlQuery;
        if (sqlQueryNew != null){
            for (Entry<String, String> entry : renamedTableNames.entrySet()) {
                sqlQueryNew = sqlQueryNew.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        return select(tableName, columns, sqlQueryNew);
    }

    public final ResultSet select(String tableName, List<Column> columns, String sqlQuery) {
        if (sqlQuery == null) {
            StringBuilder query = new StringBuilder("select "); // NOI18N

            query.append(new EnumStringConstructor<Column>().constructEnumString(columns,
                    new Convertor<Column>() {

                        @Override
                        public String toString(Column item) {
                            return (item.getExpression() == null) ? item.getColumnName() : (item.getExpression() + " AS " + item.getColumnName()); // NOI18N
                        }
                    }));

            query.append(" from ").append(tableName); // NOI18N
            sqlQuery = query.toString();
        }
        return connection.executeQuery(sqlQuery);
    }
    

    public final ResultSet select(String tableName, List<Column> columns, Collection<DataTableMetadataFilter> filters) {
            StringBuilder query = new StringBuilder("select "); // NOI18N

            query.append(new EnumStringConstructor<Column>().constructEnumString(columns,
                    new Convertor<Column>() {

                        @Override
                        public String toString(Column item) {
                            return (item.getExpression() == null) ? item.getColumnName() : (item.getExpression() + " AS " + item.getColumnName()); // NOI18N
                        }
                    }));

            query.append(" from ").append(tableName); // NOI18N
            if (filters != null && !filters.isEmpty()){
                String whereClause = "";

                for (DataTableMetadataFilter filter : filters) {
                    Column filterColumn = filter.getFilteredColumn();
                    if (columns.contains(filterColumn)) {
                        Range<?> range = filter.getNumericDataFilter().getInterval();
                        if (range.getStart() != null || range.getEnd() != null) {
                            whereClause = range.toString(" WHERE ", "%d <= " + filterColumn.getColumnName(), " AND ", filterColumn.getColumnName() + " <= %d", null); // NOI18N
                            break;
                        }
                    }
                }
                if (!whereClause.isEmpty()){
                    query.append(whereClause);
                }
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "SQLDataStorage my method sql = {0}", query.toString()); // NOI18N
                }
            }

        return connection.executeQuery(query.toString());
    }

    public void executeUpdate(String sql) throws SQLException {
        connection.executeUpdate(sql);
    }

    public int executeUpdate(PreparedStatement statement) {
        try {
            return statement.executeUpdate();
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(this, ex);
        }
        return -1;
    }

    protected final Connection getConnection() {
        return connection.getConnection();
    }

    protected void addInsertInQueue(PreparedStatement st) {
        requestProcessor.queueRequest(new CustomRequest(st));
    }

    @Override
    public final void addData(String tableName, List<DataRow> data) {
        for (DataRow row : data) {
            requestProcessor.queueRequest(new DataRowInsertRequest(tableName, row));
        }
    }

    @Override
    public boolean shutdown() {
        isClosed.set(true);
        synchronized (this) {
            try {
                statementsCache.close();
                connection.close();
            } catch (SQLException ex) {
                SQLExceptions.printStackTrace(this, ex);
            }
        }
        return true;
    }

    @Override
    public final synchronized void syncAddData(String tableName, List<DataRow> data) {
        for (DataRow row : data) {
            DataRowInsertRequest request = new DataRowInsertRequest(tableName, row);
            try {
                request.execute();
            } catch (SQLException ex) {
                SQLExceptions.printStackTrace(this, ex);
            }
        }
    }

    /**
     * Returns a prepareed statement for insertion of the row into the table
     * NB: FILLS the statement with data
     */
    private PreparedStatement createRowInsertStatement(String tableName, DataRow row) {
        logger.fine("Will add to the queue with using prepared statement"); // NOI18N

        StringBuilder query = new StringBuilder("insert into " + tableName + " ("); // NOI18N

        query.append(new EnumStringConstructor<String>().constructEnumString(row.getColumnNames(),
                new Convertor<String>() {

                    @Override
                    public String toString(String item) {
                        return item;
                    }
                }));

        query.append(") values ("); // NOI18N

        query.append(new EnumStringConstructor<Object>().constructEnumString(row.getData(),
                new Convertor<Object>() {

                    @Override
                    public String toString(Object item) {
                        return '\'' + String.valueOf(item) + '\'';
                    }
                }));

        query.append(")").append(getSQLQueriesDelimeter()); // NOI18N

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "----------SQL: dispatching {0}", query.toString()); // NOI18N
        }

        return connection.prepareStatement(query.toString());
    }

    /**
     * Returns a prepared statement for insertion of the row into the table
     * NB: FILLS the statement with data
     */
    private PreparedStatement createRowInsertStatement(final DataTableMetadata table, DataRow row) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Will add to the queue with using prepared statement"); // NOI18N
        }

        String tableName = table.getName();
        StringBuilder query = new StringBuilder("insert into " + tableName + " ("); // NOI18N
        StringBuilder sb = new StringBuilder();
        Iterator<Column> i = table.getColumns().iterator();
        Column item;

        while (i.hasNext()) {
            item = i.next();
            if (table.isAutoIncrement(item)) {
                continue;
            }
            sb.append(item.getColumnName());
            if (i.hasNext()) {
                sb.append(", "); // NOI18N
            }
        }

        query.append(sb.toString());
        query.append(") values ("); // NOI18N

        query.append(new EnumStringConstructor<Object>().constructEnumString(row.getData(),
                new Convertor<Object>() {

                    @Override
                    public String toString(Object item) {
                        return '\'' + String.valueOf(item) + '\'';
                    }
                }));

        query.append(")").append(getSQLQueriesDelimeter()); // NOI18N
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "----------SQL: dispatching {0}", query.toString()); // NOI18N
        }

        return connection.prepareStatement(query.toString());
    }

    public final PreparedStatement prepareStatement(String sql) throws SQLException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "SQL: prepare statement {0}", sql); // NOI18N
        }
        PreparedStatement stmt;
        String sqlUpper = sql.toUpperCase();
        if (sqlUpper.startsWith("INSERT INTO ")) { // NOI18N
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else if (sqlUpper.endsWith(" FOR UPDATE")) { // NOI18N
            stmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        } else {
            stmt = connection.prepareStatement(sql);
        }
        return stmt;
    }

    public void flush() {
        requestProcessor.flush();
    }

    public final SQLRequestsProcessor getRequestsProcessor() {
        return requestProcessor;
    }

    public final boolean isClosed() {
        return isClosed.get();
    }

    private static class CustomRequest extends SQLBaseRequest {

        PreparedStatement preparedStatement;

        public CustomRequest(PreparedStatement preparedStatement) {
            this.preparedStatement = preparedStatement;
        }

        @Override
        public PreparedStatement getPreparedStatement() throws SQLException {
            return preparedStatement;
        }
    }

    private class DataRowInsertRequest extends SQLBaseRequest {

        final String tableName;
        final DataRow dataRow;

        public DataRowInsertRequest(String tableName, DataRow dataRow) {
            this.tableName = tableName;
            this.dataRow = dataRow;
        }

        @Override
        public PreparedStatement getPreparedStatement() throws SQLException {
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

    public static final class EnumStringConstructor<T> {

        public String constructEnumString(Collection<? extends T> collection, Convertor<T> conv) {
            StringBuilder sb = new StringBuilder();
            Iterator<? extends T> i = collection.iterator();
            T item;

            while (i.hasNext()) {
                item = i.next();
                sb.append(conv.toString(item));
                if (i.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }

            return sb.toString();
        }
    }

    public interface Convertor<T> {

        public String toString(T item);
    }
}
