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
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;

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
    
    private PrimaryKey primaryKey;

    // Need a marker because there may be *no* primary key, and we don't want
    // to hit the database over and over again when there is no primary key
    private boolean primaryKeyInitialized = false;

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
            throw new MetadataException(e);
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
                    
                    IndexColumn col = createJDBCIndexColumn(index, rs).getIndexColumn();
                    index.addColumn(col);
                    LOGGER.log(Level.FINE, "Added column " + col.getName() + " to index " + indexName);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }

        indexes = Collections.unmodifiableMap(newIndexes);
    }

    protected JDBCIndex createJDBCIndex(String name, ResultSet rs) {
        try {
            IndexType type = JDBCUtils.getIndexType(rs.getShort("TYPE"));
            boolean isUnique = !rs.getBoolean("NON_UNIQUE");
            return new JDBCIndex(this.getTable(), name, type, isUnique);
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
    }

    protected JDBCIndexColumn createJDBCIndexColumn(JDBCIndex parent, ResultSet rs) {
        try {
            Column column = getColumn(rs.getString("COLUMN_NAME"));
            int position = rs.getInt("ORDINAL_POSITION");
            Ordering ordering = JDBCUtils.getOrdering(rs.getString("ASC_OR_DESC"));

            return new JDBCIndexColumn(parent.getIndex(), column.getName(), column, position, ordering);
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
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
            throw new MetadataException(e);
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
}
