/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.explorer.infos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.MessageFormat;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DatabaseProductNotFoundException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.adaptors.DefaultAdaptor;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DerbyConectionEventListener;

//commented out for 3.6 release, need to solve for next Studio release
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
//import org.netbeans.modules.db.explorer.PointbasePlus;
import org.openide.util.Exceptions;
//import org.openide.nodes.Node;
//import org.netbeans.modules.db.explorer.nodes.ConnectionNode;


public class ConnectionNodeInfo extends DatabaseNodeInfo {
    
    static final long serialVersionUID =-8322295510950137669L;
    
    private static final Logger LOGGER = Logger.getLogger(
            ConnectionNodeInfo.class.getName());
    
    protected ConnectionNodeInfo() {
        addConnectionListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ( propertyName.equals(DatabaseNodeInfo.DATABASE) ||
                     propertyName.equals(DatabaseNodeInfo.SCHEMA) ||
                     propertyName.equals(DatabaseNodeInfo.USER) ) {
                    
                    try {
                        resetChildren();
                    } catch (DatabaseException dbe) {
                        Exceptions.printStackTrace(dbe);
                    }
                    
                    setDisplayName(getDatabaseConnection().getName());
                }
                else if (propertyName.equals(DatabaseNodeInfo.CONNECTION)) {
                    try {
                        update((Connection)evt.getNewValue());
                    } catch ( DatabaseException dbe ) {
                        Exceptions.printStackTrace(dbe);
                    }
                }
                notifyChange();
            }
        });

        put(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ALTER_ADD, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ALTER_DROP, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CONVERT, Boolean.FALSE);
        put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, Boolean.FALSE);
        put(DefaultAdaptor.PROP_GROUP_BY, Boolean.FALSE);
        put(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, Boolean.FALSE);
        put(DefaultAdaptor.PROP_BEYOND_GROUP_BY, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ESCAPE_LIKE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_MULTIPLE_RS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, Boolean.FALSE);
        put(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, Boolean.FALSE);
        put(DefaultAdaptor.PROP_OUTER_JOINS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_FULL_OUTER_JOINS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SCHEMAS_IN_DML, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOGS_IN_DML, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, Boolean.FALSE);
        put(DefaultAdaptor.PROP_POSITIONED_DELETE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_POSITIONED_UPDATE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_STORED_PROCEDURES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SUBQUERY_IN_INS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_UNION, Boolean.FALSE);
        put(DefaultAdaptor.PROP_UNION_ALL, Boolean.FALSE);
        put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, Boolean.FALSE);
        put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, Boolean.FALSE);
        put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, Boolean.FALSE);
        put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, Boolean.FALSE);
        put(DefaultAdaptor.PROP_TRANSACTIONS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, Boolean.FALSE);
        put(DefaultAdaptor.PROP_BATCH_UPDATES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_CATALOG_AT_START, Boolean.FALSE);
        put(DefaultAdaptor.PROP_COLUMN_ALIASING, Boolean.FALSE);
        put(DefaultAdaptor.PROP_DDL_CAUSES_COMMIT, Boolean.FALSE);
        put(DefaultAdaptor.PROP_DDL_IGNORED_IN_TRANSACTIONS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_DIFF_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_LOCAL_FILES, Boolean.FALSE);
        put(DefaultAdaptor.PROP_FILE_PER_TABLE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_ROWSIZE_INCLUDING_BLOBS, Boolean.FALSE);
        put(DefaultAdaptor.PROP_NULL_PLUS_NULL_IS_NULL, Boolean.FALSE);
        put(DefaultAdaptor.PROP_PROCEDURES_ARE_CALLABLE, Boolean.FALSE);
        put(DefaultAdaptor.PROP_TABLES_ARE_SELECTABLE, Boolean.FALSE);
        
        put(DefaultAdaptor.PROP_READONLY, isReadOnly() ? Boolean.TRUE : Boolean.FALSE);
        
        try {
            update(null);
        } catch ( DatabaseException dbe ) {
            Exceptions.printStackTrace(dbe);
        }
    }    

    @Override
    protected void initChildren(Vector children) throws DatabaseException {
        if ( isConnected() ) {
            children.add(createNodeInfo(this, DatabaseNode.TABLELIST));
            children.add(createNodeInfo(this, DatabaseNode.VIEWLIST));
            children.add(createNodeInfo(this, DatabaseNode.PROCEDURELIST));
        }
    }
    
    private void update(Connection conn) throws DatabaseException {
        if ( conn == null ) {
            // Not connected, so no children
            // I don't believe this is necessary.  refreshChildren()
            // should already be taking care of this
            //setChildren(new Vector());
        } else {
            setProperties();
        }
    }
    
    private void setProperties() {
        try {
            DatabaseMetaData dmd = getSpecification().getMetaData();
            
            put(DefaultAdaptor.PROP_PRODUCTNAME, dmd.getDatabaseProductName());

            put(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, dmd.supportsMixedCaseIdentifiers() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, dmd.supportsMixedCaseQuotedIdentifiers() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ALTER_ADD, dmd.supportsAlterTableWithAddColumn() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ALTER_DROP, dmd.supportsAlterTableWithDropColumn() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CONVERT, dmd.supportsConvert() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, dmd.supportsTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, dmd.supportsDifferentTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, dmd.supportsExpressionsInOrderBy() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, dmd.supportsOrderByUnrelated() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_GROUP_BY, dmd.supportsGroupBy() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, dmd.supportsGroupByUnrelated() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_BEYOND_GROUP_BY, dmd.supportsGroupByBeyondSelect() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ESCAPE_LIKE, dmd.supportsLikeEscapeClause() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_MULTIPLE_RS, dmd.supportsMultipleResultSets() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, dmd.supportsMultipleTransactions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, dmd.supportsNonNullableColumns() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, dmd.supportsMinimumSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, dmd.supportsCoreSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, dmd.supportsExtendedSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, dmd.supportsANSI92EntryLevelSQL() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, dmd.supportsANSI92IntermediateSQL() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, dmd.supportsANSI92FullSQL() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, dmd.supportsIntegrityEnhancementFacility() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_OUTER_JOINS, dmd.supportsOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_FULL_OUTER_JOINS, dmd.supportsFullOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, dmd.supportsLimitedOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SCHEMAS_IN_DML, dmd.supportsSchemasInDataManipulation() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, dmd.supportsSchemasInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, dmd.supportsSchemasInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, dmd.supportsSchemasInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, dmd.supportsSchemasInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOGS_IN_DML, dmd.supportsCatalogsInDataManipulation() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, dmd.supportsCatalogsInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, dmd.supportsCatalogsInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, dmd.supportsCatalogsInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, dmd.supportsCatalogsInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_POSITIONED_DELETE, dmd.supportsPositionedDelete() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_POSITIONED_UPDATE, dmd.supportsPositionedUpdate() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, dmd.supportsSelectForUpdate() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_STORED_PROCEDURES, dmd.supportsStoredProcedures() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, dmd.supportsSubqueriesInComparisons() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, dmd.supportsSubqueriesInExists() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SUBQUERY_IN_INS, dmd.supportsSubqueriesInIns() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, dmd.supportsSubqueriesInQuantifieds() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, dmd.supportsCorrelatedSubqueries() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_UNION, dmd.supportsUnion() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_UNION_ALL, dmd.supportsUnionAll() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, dmd.supportsOpenCursorsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, dmd.supportsOpenCursorsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, dmd.supportsOpenStatementsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, dmd.supportsOpenStatementsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_TRANSACTIONS, dmd.supportsTransactions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, dmd.supportsDataDefinitionAndDataManipulationTransactions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, dmd.supportsDataManipulationTransactionsOnly() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_BATCH_UPDATES, dmd.supportsBatchUpdates() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_CATALOG_AT_START, dmd.isCatalogAtStart() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_COLUMN_ALIASING, dmd.supportsColumnAliasing() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_DDL_CAUSES_COMMIT, dmd.dataDefinitionCausesTransactionCommit() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_DDL_IGNORED_IN_TRANSACTIONS, dmd.dataDefinitionIgnoredInTransactions() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_DIFF_TABLE_CORRELATION_NAMES, dmd.supportsDifferentTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_LOCAL_FILES, dmd.usesLocalFiles() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_FILE_PER_TABLE, dmd.usesLocalFilePerTable() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_ROWSIZE_INCLUDING_BLOBS, dmd.doesMaxRowSizeIncludeBlobs() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_NULL_PLUS_NULL_IS_NULL, dmd.nullPlusNonNullIsNull() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_PROCEDURES_ARE_CALLABLE, dmd.allProceduresAreCallable() ? Boolean.TRUE : Boolean.FALSE);
            put(DefaultAdaptor.PROP_TABLES_ARE_SELECTABLE, dmd.allTablesAreSelectable() ? Boolean.TRUE : Boolean.FALSE);

            put(DefaultAdaptor.PROP_MAX_BINARY_LITERAL_LENGTH, new Integer(dmd.getMaxBinaryLiteralLength()));
            put(DefaultAdaptor.PROP_MAX_CHAR_LITERAL_LENGTH, new Integer(dmd.getMaxCharLiteralLength()));
            put(DefaultAdaptor.PROP_MAX_COLUMN_NAME_LENGTH, new Integer(dmd.getMaxColumnNameLength()));
            put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_GROUPBY, new Integer(dmd.getMaxColumnsInGroupBy()));
            put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_INDEX, new Integer(dmd.getMaxColumnsInIndex()));
            put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_ORDERBY, new Integer(dmd.getMaxColumnsInOrderBy()));
            put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_SELECT, new Integer(dmd.getMaxColumnsInSelect()));
            put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_TABLE, new Integer(dmd.getMaxColumnsInTable()));
            put(DefaultAdaptor.PROP_MAX_CONNECTIONS, new Integer(dmd.getMaxConnections()));
            put(DefaultAdaptor.PROP_MAX_CURSORNAME_LENGTH, new Integer(dmd.getMaxCursorNameLength()));
            put(DefaultAdaptor.PROP_MAX_INDEX_LENGTH, new Integer(dmd.getMaxIndexLength()));
            put(DefaultAdaptor.PROP_MAX_SCHEMA_NAME, new Integer(dmd.getMaxSchemaNameLength()));
            put(DefaultAdaptor.PROP_MAX_PROCEDURE_NAME, new Integer(dmd.getMaxProcedureNameLength()));
            put(DefaultAdaptor.PROP_MAX_CATALOG_NAME, new Integer(dmd.getMaxCatalogNameLength()));
            put(DefaultAdaptor.PROP_MAX_ROW_SIZE, new Integer(dmd.getMaxRowSize()));
            put(DefaultAdaptor.PROP_MAX_STATEMENT_LENGTH, new Integer(dmd.getMaxStatementLength()));
            put(DefaultAdaptor.PROP_MAX_STATEMENTS, new Integer(dmd.getMaxStatements()));
            put(DefaultAdaptor.PROP_MAX_TABLENAME_LENGTH, new Integer(dmd.getMaxTableNameLength()));
            put(DefaultAdaptor.PROP_MAX_TABLES_IN_SELECT, new Integer(dmd.getMaxTablesInSelect()));
            put(DefaultAdaptor.PROP_MAX_USERNAME, new Integer(dmd.getMaxUserNameLength()));
            put(DefaultAdaptor.PROP_DEFAULT_ISOLATION, new Integer(dmd.getDefaultTransactionIsolation()));

            put(DefaultAdaptor.PROP_URL, dmd.getURL());
            put(DefaultAdaptor.PROP_USERNAME, dmd.getUserName());
            put(DefaultAdaptor.PROP_PRODUCTVERSION, dmd.getDatabaseProductVersion());
            put(DefaultAdaptor.PROP_DRIVERNAME, dmd.getDriverName());
            put(DefaultAdaptor.PROP_DRIVER_VERSION, dmd.getDriverVersion());
            put(DefaultAdaptor.PROP_DRIVER_MAJOR_VERSION, new Integer(dmd.getDriverMajorVersion()));
            put(DefaultAdaptor.PROP_DRIVER_MINOR_VERSION, new Integer(dmd.getDriverMinorVersion()));
            put(DefaultAdaptor.PROP_IDENTIFIER_QUOTE, dmd.getIdentifierQuoteString());
            put(DefaultAdaptor.PROP_SQL_KEYWORDS, dmd.getSQLKeywords());

            put(DefaultAdaptor.PROP_NUMERIC_FUNCTIONS, dmd.getNumericFunctions());
            put(DefaultAdaptor.PROP_STRING_FUNCTIONS, dmd.getStringFunctions());
            put(DefaultAdaptor.PROP_SYSTEM_FUNCTIONS, dmd.getSystemFunctions());
            put(DefaultAdaptor.PROP_TIME_FUNCTIONS, dmd.getTimeDateFunctions());
            put(DefaultAdaptor.PROP_STRING_ESCAPE, dmd.getSearchStringEscape());
            put(DefaultAdaptor.PROP_EXTRA_CHARACTERS, dmd.getExtraNameCharacters());
            put(DefaultAdaptor.PROP_SCHEMA_TERM, dmd.getSchemaTerm());
            put(DefaultAdaptor.PROP_PROCEDURE_TERM, dmd.getProcedureTerm());
            put(DefaultAdaptor.PROP_CATALOG_TERM, dmd.getCatalogTerm());
            put(DefaultAdaptor.PROP_CATALOGS_SEPARATOR, dmd.getCatalogSeparator());
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private void connect(String dbsys) throws DatabaseException {
        String drvurl = getDriver();
        String dburl = getDatabase();
        
        try {
//commented out for 3.6 release, need to solve for next Studio release
            // check if there is connected connection by Pointbase driver
            // Pointbase driver doesn't permit the concurrently connection
//            if (drvurl.startsWith(PointbasePlus.DRIVER)) {
//                Node n[] = getParent().getNode().getChildren().getNodes();
//                for (int i = 0; i < n.length; i++)
//                    if (n[i] instanceof ConnectionNode) {
//                        ConnectionNodeInfo cinfo = (ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo();
//                        if (cinfo.getDriver().startsWith(PointbasePlus.DRIVER))
//                            if (!(cinfo.getDatabase().equals(dburl)&&cinfo.getUser().equals(getUser())))
//                                if ((cinfo.getConnection()!=null))
//                                    throw new Exception(bundle.getString("EXC_PBConcurrentConn")); // NOI18N
//                    }
//            }

            DatabaseConnection con = new DatabaseConnection(drvurl, dburl, getUser(), getPassword());
            Connection connection = con.createJDBCConnection();
            
            finishConnect(dbsys, con, connection);
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }

    /*
     * Connect to this node a DBConnection which is already connected to the
     * database. Used when adding a new connection: the newly added DBConnection is already
     * connected to the database, so this methods helps avoiding connecting to the
     * database once more.
     */
    public void connect(DBConnection conn) throws DatabaseException {
        try {
            DatabaseConnection con = (DatabaseConnection) conn;
            
            Connection connection = con.getConnection();
            
            SpecificationFactory factory = (SpecificationFactory) getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            setReadOnly(false);
            spec = (Specification) factory.createSpecification(con, connection);
            put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            setSpecification(spec);

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(connection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(getSchema());
            setDriverSpecification(drvSpec);
            setConnection(connection); // fires change
        } catch (DatabaseProductNotFoundException e) {
            setReadOnly(false);
            connect("GenericDatabaseSystem"); //NOI18N
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }
    
    public void finishConnect(String dbsys, DatabaseConnection con, Connection connection) throws DatabaseException {
        try {
            SpecificationFactory factory = (SpecificationFactory) getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            if (dbsys != null) {
                spec = (Specification) factory.createSpecification(con, dbsys, connection);
            } else {
                setReadOnly(false);
                spec = (Specification) factory.createSpecification(con, connection);
            }
            put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            setSpecification(spec);

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(connection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(getSchema());
            setDriverSpecification(drvSpec);
            setConnection(connection); // fires change
        } catch (DatabaseProductNotFoundException e) {
            setReadOnly(false);
            connect("GenericDatabaseSystem"); //NOI18N
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    
    public void disconnect() throws DatabaseException {
        Connection connection = getConnection();
        if (connection != null) {
            String message = null;
            try {
                setConnection(null); // fires change
                connection.close();
            } catch (Exception exc) {
                // connection is broken, connection state has been changed
                setConnection(null); // fires change
                
                message = MessageFormat.format(bundle().getString("EXC_ConnectionError"), exc.getMessage()); // NOI18N
            }

            // XXX hack for Derby
            DerbyConectionEventListener.getDefault().afterDisconnect(getDatabaseConnection(), connection);
            
            if (message != null) {
                throw new DatabaseException(message);
            }
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            DatabaseConnection cinfo = (DatabaseConnection) getDatabaseConnection();
            ConnectionList.getDefault().remove(cinfo);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public Object put(String key, Object obj) {
        if (key.equals(USER) || key.equals(DRIVER) || key.equals(DATABASE)
                || key.equals(SCHEMA) || key.equals(REMEMBER_PWD)) {
            updateConnection((String)key, obj);
        }
        return super.put(key, obj);
    }
    
    private void updateConnection(String key, Object newVal) {
        DatabaseConnection infoConn = getDatabaseConnection();
        DatabaseConnection connFromList = ConnectionList.getDefault().getConnection(infoConn);
        if (connFromList != null) {
            if (key.equals(SCHEMA))
                connFromList.setSchema((String)newVal);
            else if (key.equals(USER))
                connFromList.setUser((String)newVal);
            else if (key.equals(DRIVER)) {
                connFromList.setDriver((String)newVal);
            } else if (key.equals(DATABASE)) {
                connFromList.setDatabase((String)newVal);
            } else if ( key.equals(REMEMBER_PWD)) {
                connFromList.setRememberPassword(((Boolean)newVal).booleanValue());
            }
        }
        setName(infoConn.getName());
    }

    @Override
    public Vector<DatabaseNodeInfo> getChildren() throws DatabaseException{
        if ( ! isConnected() ) {
            return new Vector<DatabaseNodeInfo>();
        } else {
            return super.getChildren();
        }
    }

    @Override
    public void refreshChildren() throws DatabaseException {
        Vector<DatabaseNodeInfo> children = getChildren();
        for ( DatabaseNodeInfo info : children ) {
            info.refreshChildren();
        }
    }
}
