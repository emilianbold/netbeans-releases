/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 - 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class JDBCTable extends TableImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCTable.class.getName());

    private final JDBCSchema jdbcSchema;
    private final String name;

    private Map<String, Column> columns;
    private Map<String, Index> indexes;
    private Map<String, ForeignKey> foreignKeys;
    
    private PrimaryKey primaryKey;

    // Need a marker because there may be *no* primary key, and we don't want
    // to hit the database over and over again when there is no primary key
    private boolean primaryKeyInitialized = false;
    private static final String SQL_EXCEPTION_NOT_YET_IMPLEMENTED = "not yet implemented";

    public JDBCTable(JDBCSchema jdbcSchema, String name) {
        this.jdbcSchema = jdbcSchema;
        this.name = name;
    }

    public final Schema getParent() {
        return jdbcSchema.getSchema();
    }

    public final String getName() {
        return name;
    }

    public final Collection<Column> getColumns() {
        return initColumns().values();
    }

    public final Column getColumn(String name) {
        return MetadataUtilities.find(name, initColumns());
    }

    @Override
    public PrimaryKey getPrimaryKey() {
        return initPrimaryKey();
    }

    @Override
    public Index getIndex(String indexName) {
        return MetadataUtilities.find(indexName, initIndexes());
    }

    @Override
    public Collection<Index> getIndexes() {
        return initIndexes().values();
    }

    @Override
    public Collection<ForeignKey> getForeignKeys() {
        return initForeignKeys().values();
    }

    @Override
    public ForeignKey getForeignKeyByInternalName(String name) {
         return MetadataUtilities.find(name, initForeignKeys());
    }

    @Override
    public final void refresh() {
        columns = null;
        primaryKey = null;
        primaryKeyInitialized = false;
    }

    @Override
    public String toString() {
        return "JDBCTable[name='" + name + "']"; // NOI18N
    }

    protected JDBCColumn createJDBCColumn(ResultSet rs) throws SQLException {
        int position = rs.getInt("ORDINAL_POSITION");
        return new JDBCColumn(this.getTable(), position, JDBCValue.createTableColumnValue(rs));
    }

    protected JDBCPrimaryKey createJDBCPrimaryKey(String pkName, Collection<Column> pkcols) {
        return new JDBCPrimaryKey(this.getTable(), pkName, pkcols);
    }

    protected void createColumns() {
        Map<String, Column> newColumns = new LinkedHashMap<String, Column>();
        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getColumns(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name, "%"); // NOI18N
            try {
                while (rs.next()) {
                    Column column = createJDBCColumn(rs).getColumn();
                    newColumns.put(column.getName(), column);
                    LOGGER.log(Level.FINE, "Created column {0}", column);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }
        columns = Collections.unmodifiableMap(newColumns);
    }

    protected void createIndexes() {
        Map<String, Index> newIndexes = new LinkedHashMap<String, Index>();
        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getIndexInfo(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name, false, true);
            try {
                JDBCIndex index = null;
                String currentIndexName = null;
                while (rs.next()) {
                    if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                        continue;
                    }

                    String indexName = rs.getString("INDEX_NAME");
                    if (index == null || !(currentIndexName.equals(indexName))) {
                        index = createJDBCIndex(indexName, rs);
                        LOGGER.log(Level.FINE, "Created index " + index);

                        newIndexes.put(index.getName(), index.getIndex());
                        currentIndexName = indexName;
                    }

                    JDBCIndexColumn idx = createJDBCIndexColumn(index, rs);
                    if (idx == null) {
                        LOGGER.log(Level.INFO, "Cannot create index column for " + indexName + " from " + rs);
                    } else {
                        IndexColumn col = idx.getIndexColumn();
                        index.addColumn(col);
                        LOGGER.log(Level.FINE, "Added column " + col.getName() + " to index " + indexName);
                    }
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        indexes = Collections.unmodifiableMap(newIndexes);
    }

    protected JDBCIndex createJDBCIndex(String name, ResultSet rs) {
        IndexType type = IndexType.OTHER;
        boolean isUnique = false;
        try {
            type = JDBCUtils.getIndexType(rs.getShort("TYPE"));
            isUnique = !rs.getBoolean("NON_UNIQUE");
        } catch (SQLException e) {
            filterSQLException(e);
        }
        return new JDBCIndex(this.getTable(), name, type, isUnique);
    }

    protected JDBCIndexColumn createJDBCIndexColumn(JDBCIndex parent, ResultSet rs) {
        Column column = null;
        int position = 0;
        Ordering ordering = Ordering.NOT_SUPPORTED;
        try {
            column = getColumn(rs.getString("COLUMN_NAME"));
            position = rs.getInt("ORDINAL_POSITION");
            ordering = JDBCUtils.getOrdering(rs.getString("ASC_OR_DESC"));
        } catch (SQLException e) {
            filterSQLException(e);
        }
        if (column == null) {
            LOGGER.log(Level.INFO, "Cannot get column for index " + parent + " from " + rs);
            return null;
        }
        return new JDBCIndexColumn(parent.getIndex(), column.getName(), column, position, ordering);
    }

        protected void createForeignKeys() {
        Map<String,ForeignKey> newKeys = new LinkedHashMap<String,ForeignKey>();
        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getImportedKeys(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name);
            try {
                JDBCForeignKey fkey = null;
                String currentKeyName = null;
                while (rs.next()) {
                    String keyName = rs.getString("FK_NAME");
                    // We have to assume that if the foreign key name is null, then this is a *new*
                    // foreign key, even if the last foreign key name was also null.
                    if (fkey == null || keyName == null || !(currentKeyName.equals(keyName))) {
                        fkey = createJDBCForeignKey(keyName, rs);
                        LOGGER.log(Level.FINE, "Created foreign key " + keyName);

                        newKeys.put(fkey.getInternalName(), fkey.getForeignKey());
                        currentKeyName = keyName;
                    }

                    ForeignKeyColumn col = createJDBCForeignKeyColumn(fkey, rs).getForeignKeyColumn();
                    fkey.addColumn(col);
                    LOGGER.log(Level.FINE, "Added foreign key column " + col.getName() + " to foreign key " + keyName);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        foreignKeys = Collections.unmodifiableMap(newKeys);
    }

    protected JDBCForeignKey createJDBCForeignKey(String name, ResultSet rs) {
        return new JDBCForeignKey(this.getTable(), name);
    }

    protected JDBCForeignKeyColumn createJDBCForeignKeyColumn(JDBCForeignKey parent, ResultSet rs) {
        Table table = findReferredTable(rs);
        String colname = null;
        Column referredColumn = null;
        colname = null;
        Column referringColumn = null;
        int position = 0;

        try {
            table = findReferredTable(rs);
            colname = rs.getString("PKCOLUMN_NAME"); // NOI18N
            referredColumn = table.getColumn(colname);
            if (referredColumn == null) {
                throw new MetadataException(getMessage("ERR_COL_NOT_FOUND", table.getParent().getParent().getName(), table.getParent().getName(), table.getName(), colname)); // NOI18N
            }

            colname = rs.getString("FKCOLUMN_NAME");
            referringColumn = getColumn(colname);

            position = rs.getInt("KEY_SEQ");
        } catch (SQLException e) {
            filterSQLException(e);
        }
        return new JDBCForeignKeyColumn(parent.getForeignKey(), referringColumn.getName(), referringColumn, referredColumn, position);
    }
    
    private String getMessage(String key, String ... args) {
        return NbBundle.getMessage(JDBCTable.class, key, args);
    }

    private Table findReferredTable(ResultSet rs) {
        JDBCMetadata metadata = jdbcSchema.getJDBCCatalog().getJDBCMetadata();
        Catalog catalog;
        Schema schema;
        Table table = null;

        try {
            String catalogName = rs.getString("PKTABLE_CAT"); // NOI18N
            if (catalogName == null || catalogName.length() == 0) {
                catalog = jdbcSchema.getParent();
            } else {
                catalog = metadata.getCatalog(catalogName);
                if (catalog == null) {
                    throw new MetadataException(getMessage("ERR_CATALOG_NOT_FOUND", catalogName)); // NOI18N
                }
            }

            String schemaName = rs.getString("PKTABLE_SCHEM"); // NOI18N

            if (schemaName == null || schemaName.length() == 0) {
                schema = catalog.getSyntheticSchema();
            } else {
                schema = catalog.getSchema(schemaName);
                if (schema == null) {
                    throw new MetadataException(getMessage("ERR_SCHEMA_NOT_FOUND", schemaName, catalog.getName()));
                }
            }

            String tableName = rs.getString("PKTABLE_NAME");
            table = schema.getTable(tableName);

            if (table == null) {
                throw new MetadataException(getMessage("ERR_TABLE_NOT_FOUND", catalogName, schemaName, tableName));
            }

        } catch (SQLException e) {
            filterSQLException(e);
        }

        return table;
    }


    protected void createPrimaryKey() {
        String pkname = null;
        Collection<Column> pkcols = new ArrayList<Column>();
        try {
            ResultSet rs = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getPrimaryKeys(jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(), name); // NOI18N
            try {
                while (rs.next()) {
                    if (pkname == null) {
                        pkname = rs.getString("PK_NAME");
                    }
                    String colName = rs.getString("COLUMN_NAME");
                    pkcols.add(getColumn(colName));
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        primaryKey = createJDBCPrimaryKey(pkname, Collections.unmodifiableCollection(pkcols)).getPrimaryKey();
    }

    private Map<String, Column> initColumns() {
        if (columns != null) {
            return columns;
        }
        LOGGER.log(Level.FINE, "Initializing columns in {0}", this);
        createColumns();
        return columns;
    }

    private Map<String, Index> initIndexes() {
        if (indexes != null) {
            return indexes;
        }
        LOGGER.log(Level.FINE, "Initializing indexes in {0}", this);

        createIndexes();
        return indexes;
    }

    private Map<String,ForeignKey> initForeignKeys() {
        if (foreignKeys != null) {
            return foreignKeys;
        }
        LOGGER.log(Level.FINE, "Initializing foreign keys in {0}", this);

        createForeignKeys();
        return foreignKeys;
    }

    private PrimaryKey initPrimaryKey() {
        if (primaryKeyInitialized) {
            return primaryKey;
        }
        LOGGER.log(Level.FINE, "Initializing columns in {0}", this);
        // These need to be initialized first.
        getColumns();
        createPrimaryKey();
        primaryKeyInitialized = true;
        return primaryKey;
    }

    private void filterSQLException(SQLException x) throws MetadataException {
        if (SQL_EXCEPTION_NOT_YET_IMPLEMENTED.equalsIgnoreCase(x.getMessage())) {
            Logger.getLogger(JDBCTable.class.getName()).log(Level.FINE, x.getLocalizedMessage(), x);
        } else {
            throw new MetadataException(x);
        }
    }
}
