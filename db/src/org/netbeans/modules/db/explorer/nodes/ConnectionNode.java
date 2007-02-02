/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbMetaDataTransferProvider;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.openide.util.Lookup;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.RequestProcessor;

import org.netbeans.lib.ddl.adaptors.DefaultAdaptor;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.util.datatransfer.ExTransferable;

/**
* Node representing open or closed connection to database.
*/

public class ConnectionNode extends DatabaseNode {
    
    private boolean createPropSupport = true;
    
    public void setInfo(DatabaseNodeInfo nodeinfo) {
        super.setInfo(nodeinfo);
        DatabaseNodeInfo info = getInfo();

        setName(info.getName());

        info.put(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ALTER_ADD, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ALTER_DROP, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CONVERT, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_GROUP_BY, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_BEYOND_GROUP_BY, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ESCAPE_LIKE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_MULTIPLE_RS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_OUTER_JOINS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_FULL_OUTER_JOINS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SCHEMAS_IN_DML, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOGS_IN_DML, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_POSITIONED_DELETE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_POSITIONED_UPDATE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_STORED_PROCEDURES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SUBQUERY_IN_INS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_UNION, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_UNION_ALL, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_TRANSACTIONS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_BATCH_UPDATES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_CATALOG_AT_START, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_COLUMN_ALIASING, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_DDL_CAUSES_COMMIT, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_DDL_IGNORED_IN_TRANSACTIONS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_DIFF_TABLE_CORRELATION_NAMES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_LOCAL_FILES, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_FILE_PER_TABLE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_ROWSIZE_INCLUDING_BLOBS, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_NULL_PLUS_NULL_IS_NULL, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_PROCEDURES_ARE_CALLABLE, Boolean.FALSE);
        info.put(DefaultAdaptor.PROP_TABLES_ARE_SELECTABLE, Boolean.FALSE);
        
        info.put(DefaultAdaptor.PROP_READONLY, info.isReadOnly() ? Boolean.TRUE : Boolean.FALSE);

        info.addConnectionListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DatabaseNodeInfo.DATABASE))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.SCHEMA))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.USER))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.CONNECTION)) {
                    update((Connection)evt.getNewValue());
                    firePropertyChange(null, null, null);
                }
            }
        });

        getCookieSet().add(this);
    }

    private void setConnectionName() {
        String displayName = getInfo().getDatabaseConnection().getName();
        setDisplayName(displayName);
    }
        
    private boolean createPropSupport() {
        return createPropSupport;
    }
    
    private void setPropSupport(boolean value) {
        createPropSupport = value;
    }
    
    private void update(Connection connection) {        
        final boolean connecting = (connection != null);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                DatabaseNodeChildren children = (DatabaseNodeChildren)getChildren();
                DatabaseNodeInfo info = getInfo();
                
                setIconBase((String)info.get(connecting ? "activeiconbase" : "iconbase")); //NOI18N
                setConnectionName();
                Sheet.Set set = getSheet().get(Sheet.PROPERTIES);
                
                try {
                    if (createPropSupport()) {
                        Node.Property dbprop = set.get(DatabaseNodeInfo.DATABASE);
                        PropertySupport newdbprop = createPropertySupport(dbprop.getName(), dbprop.getValueType(), dbprop.getDisplayName(), dbprop.getShortDescription(), info, !connecting);
                        set.put(newdbprop);
                        firePropertyChange("db",dbprop,newdbprop); //NOI18N

                        Node.Property drvprop = set.get(DatabaseNodeInfo.DRIVER);
                        PropertySupport newdrvprop = createPropertySupport(drvprop.getName(), drvprop.getValueType(), drvprop.getDisplayName(), drvprop.getShortDescription(), info, !connecting);
                        set.put(newdrvprop);
                        firePropertyChange("driver",drvprop,newdrvprop); //NOI18N

                        Node.Property schemaprop = set.get(DatabaseNodeInfo.SCHEMA);
                        PropertySupport newschemaprop = createPropertySupport(schemaprop.getName(), schemaprop.getValueType(), schemaprop.getDisplayName(), schemaprop.getShortDescription(), info, !connecting);
                        set.put(newschemaprop);
                        firePropertyChange("schema",schemaprop,newschemaprop); //NOI18N

                        Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
                        PropertySupport newusrprop = createPropertySupport(usrprop.getName(), usrprop.getValueType(), usrprop.getDisplayName(), usrprop.getShortDescription(), info, !connecting);
                        set.put(newusrprop);
                        firePropertyChange("user",usrprop,newusrprop); //NOI18N

                        Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
                        PropertySupport newrememberprop = createPropertySupport(rememberprop.getName(), rememberprop.getValueType(), rememberprop.getDisplayName(), rememberprop.getShortDescription(), info, connecting);
                        set.put(newrememberprop);
                        firePropertyChange("rememberpwd",rememberprop,newrememberprop); //NOI18N
                        
                        setPropSupport(false);
                    } else {
                        Node.Property dbprop = set.get(DatabaseNodeInfo.DATABASE);
                        set.put(dbprop);
                        firePropertyChange("db",null,dbprop); //NOI18N

                        Node.Property drvprop = set.get(DatabaseNodeInfo.DRIVER);
                        firePropertyChange("driver",null,drvprop); //NOI18N

                        Node.Property schemaprop = set.get(DatabaseNodeInfo.SCHEMA);
                        firePropertyChange("schema",null,schemaprop); //NOI18N

                        Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
                        firePropertyChange("user",null,usrprop); //NOI18N

                        Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
                        firePropertyChange("rememberpwd",null,rememberprop); //NOI18N
                    }
                    
                    if (!connecting) {
                        children.remove(children.getNodes());
                        getInfo().getChildren().clear();
                    } else {
                        DatabaseMetaData dmd = info.getSpecification().getMetaData();

                        try {
                            info.put(DefaultAdaptor.PROP_PRODUCTNAME, dmd.getDatabaseProductName());

                            info.put(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, dmd.supportsMixedCaseIdentifiers() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, dmd.supportsMixedCaseQuotedIdentifiers() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ALTER_ADD, dmd.supportsAlterTableWithAddColumn() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ALTER_DROP, dmd.supportsAlterTableWithDropColumn() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CONVERT, dmd.supportsConvert() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, dmd.supportsTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, dmd.supportsDifferentTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, dmd.supportsExpressionsInOrderBy() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, dmd.supportsOrderByUnrelated() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_GROUP_BY, dmd.supportsGroupBy() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, dmd.supportsGroupByUnrelated() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_BEYOND_GROUP_BY, dmd.supportsGroupByBeyondSelect() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ESCAPE_LIKE, dmd.supportsLikeEscapeClause() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_MULTIPLE_RS, dmd.supportsMultipleResultSets() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, dmd.supportsMultipleTransactions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, dmd.supportsNonNullableColumns() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, dmd.supportsMinimumSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, dmd.supportsCoreSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, dmd.supportsExtendedSQLGrammar() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, dmd.supportsANSI92EntryLevelSQL() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, dmd.supportsANSI92IntermediateSQL() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, dmd.supportsANSI92FullSQL() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, dmd.supportsIntegrityEnhancementFacility() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_OUTER_JOINS, dmd.supportsOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_FULL_OUTER_JOINS, dmd.supportsFullOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, dmd.supportsLimitedOuterJoins() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SCHEMAS_IN_DML, dmd.supportsSchemasInDataManipulation() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, dmd.supportsSchemasInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, dmd.supportsSchemasInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, dmd.supportsSchemasInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, dmd.supportsSchemasInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOGS_IN_DML, dmd.supportsCatalogsInDataManipulation() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, dmd.supportsCatalogsInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, dmd.supportsCatalogsInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, dmd.supportsCatalogsInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, dmd.supportsCatalogsInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_POSITIONED_DELETE, dmd.supportsPositionedDelete() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_POSITIONED_UPDATE, dmd.supportsPositionedUpdate() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, dmd.supportsSelectForUpdate() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_STORED_PROCEDURES, dmd.supportsStoredProcedures() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, dmd.supportsSubqueriesInComparisons() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, dmd.supportsSubqueriesInExists() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SUBQUERY_IN_INS, dmd.supportsSubqueriesInIns() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, dmd.supportsSubqueriesInQuantifieds() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, dmd.supportsCorrelatedSubqueries() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_UNION, dmd.supportsUnion() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_UNION_ALL, dmd.supportsUnionAll() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, dmd.supportsOpenCursorsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, dmd.supportsOpenCursorsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, dmd.supportsOpenStatementsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, dmd.supportsOpenStatementsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_TRANSACTIONS, dmd.supportsTransactions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, dmd.supportsDataDefinitionAndDataManipulationTransactions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, dmd.supportsDataManipulationTransactionsOnly() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_BATCH_UPDATES, dmd.supportsBatchUpdates() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_CATALOG_AT_START, dmd.isCatalogAtStart() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_COLUMN_ALIASING, dmd.supportsColumnAliasing() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_DDL_CAUSES_COMMIT, dmd.dataDefinitionCausesTransactionCommit() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_DDL_IGNORED_IN_TRANSACTIONS, dmd.dataDefinitionIgnoredInTransactions() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_DIFF_TABLE_CORRELATION_NAMES, dmd.supportsDifferentTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_LOCAL_FILES, dmd.usesLocalFiles() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_FILE_PER_TABLE, dmd.usesLocalFilePerTable() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_ROWSIZE_INCLUDING_BLOBS, dmd.doesMaxRowSizeIncludeBlobs() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_NULL_PLUS_NULL_IS_NULL, dmd.nullPlusNonNullIsNull() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_PROCEDURES_ARE_CALLABLE, dmd.allProceduresAreCallable() ? Boolean.TRUE : Boolean.FALSE);
                            info.put(DefaultAdaptor.PROP_TABLES_ARE_SELECTABLE, dmd.allTablesAreSelectable() ? Boolean.TRUE : Boolean.FALSE);

                            info.put(DefaultAdaptor.PROP_MAX_BINARY_LITERAL_LENGTH, new Integer(dmd.getMaxBinaryLiteralLength()));
                            info.put(DefaultAdaptor.PROP_MAX_CHAR_LITERAL_LENGTH, new Integer(dmd.getMaxCharLiteralLength()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMN_NAME_LENGTH, new Integer(dmd.getMaxColumnNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_GROUPBY, new Integer(dmd.getMaxColumnsInGroupBy()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_INDEX, new Integer(dmd.getMaxColumnsInIndex()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_ORDERBY, new Integer(dmd.getMaxColumnsInOrderBy()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_SELECT, new Integer(dmd.getMaxColumnsInSelect()));
                            info.put(DefaultAdaptor.PROP_MAX_COLUMNS_IN_TABLE, new Integer(dmd.getMaxColumnsInTable()));
                            info.put(DefaultAdaptor.PROP_MAX_CONNECTIONS, new Integer(dmd.getMaxConnections()));
                            info.put(DefaultAdaptor.PROP_MAX_CURSORNAME_LENGTH, new Integer(dmd.getMaxCursorNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_INDEX_LENGTH, new Integer(dmd.getMaxIndexLength()));
                            info.put(DefaultAdaptor.PROP_MAX_SCHEMA_NAME, new Integer(dmd.getMaxSchemaNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_PROCEDURE_NAME, new Integer(dmd.getMaxProcedureNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_CATALOG_NAME, new Integer(dmd.getMaxCatalogNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_ROW_SIZE, new Integer(dmd.getMaxRowSize()));
                            info.put(DefaultAdaptor.PROP_MAX_STATEMENT_LENGTH, new Integer(dmd.getMaxStatementLength()));
                            info.put(DefaultAdaptor.PROP_MAX_STATEMENTS, new Integer(dmd.getMaxStatements()));
                            info.put(DefaultAdaptor.PROP_MAX_TABLENAME_LENGTH, new Integer(dmd.getMaxTableNameLength()));
                            info.put(DefaultAdaptor.PROP_MAX_TABLES_IN_SELECT, new Integer(dmd.getMaxTablesInSelect()));
                            info.put(DefaultAdaptor.PROP_MAX_USERNAME, new Integer(dmd.getMaxUserNameLength()));
                            info.put(DefaultAdaptor.PROP_DEFAULT_ISOLATION, new Integer(dmd.getDefaultTransactionIsolation()));

                            info.put(DefaultAdaptor.PROP_URL, dmd.getURL());
                            info.put(DefaultAdaptor.PROP_USERNAME, dmd.getUserName());
                            info.put(DefaultAdaptor.PROP_PRODUCTVERSION, dmd.getDatabaseProductVersion());
                            info.put(DefaultAdaptor.PROP_DRIVERNAME, dmd.getDriverName());
                            info.put(DefaultAdaptor.PROP_DRIVER_VERSION, dmd.getDriverVersion());
                            info.put(DefaultAdaptor.PROP_DRIVER_MAJOR_VERSION, new Integer(dmd.getDriverMajorVersion()));
                            info.put(DefaultAdaptor.PROP_DRIVER_MINOR_VERSION, new Integer(dmd.getDriverMinorVersion()));
                            info.put(DefaultAdaptor.PROP_IDENTIFIER_QUOTE, dmd.getIdentifierQuoteString());
                            info.put(DefaultAdaptor.PROP_SQL_KEYWORDS, dmd.getSQLKeywords());

                            info.put(DefaultAdaptor.PROP_NUMERIC_FUNCTIONS, dmd.getNumericFunctions());
                            info.put(DefaultAdaptor.PROP_STRING_FUNCTIONS, dmd.getStringFunctions());
                            info.put(DefaultAdaptor.PROP_SYSTEM_FUNCTIONS, dmd.getSystemFunctions());
                            info.put(DefaultAdaptor.PROP_TIME_FUNCTIONS, dmd.getTimeDateFunctions());
                            info.put(DefaultAdaptor.PROP_STRING_ESCAPE, dmd.getSearchStringEscape());
                            info.put(DefaultAdaptor.PROP_EXTRA_CHARACTERS, dmd.getExtraNameCharacters());
                            info.put(DefaultAdaptor.PROP_SCHEMA_TERM, dmd.getSchemaTerm());
                            info.put(DefaultAdaptor.PROP_PROCEDURE_TERM, dmd.getProcedureTerm());
                            info.put(DefaultAdaptor.PROP_CATALOG_TERM, dmd.getCatalogTerm());
                            info.put(DefaultAdaptor.PROP_CATALOGS_SEPARATOR, dmd.getCatalogSeparator());
                        } catch (Exception ex) {
                            //ex.printStackTrace();
                        }
                        
                        // Create subnodes

                        DatabaseNodeInfo innernfo;
                        innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.TABLELIST);
                        children.createSubnode(innernfo, true);
                        innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.VIEWLIST);
                        children.createSubnode(innernfo, true);
                        innernfo = DatabaseNodeInfo.createNodeInfo(info, DatabaseNode.PROCEDURELIST);
                        children.createSubnode(innernfo, true);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
        }, 0);
    }
    
    /**
    * Can be destroyed only if connection is closed.
    */
    public boolean canDestroy() {
        return !getInfo().isConnected();
    }

    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Connection"); //NOI18N
    }

    public Transferable clipboardCopy() throws IOException {
        Transferable result;
        final DbMetaDataTransferProvider dbTansferProvider = (DbMetaDataTransferProvider)Lookup.getDefault().lookup(DbMetaDataTransferProvider.class);
        if (dbTansferProvider != null) {
            ExTransferable exTransferable = ExTransferable.create(super.clipboardCopy());
            ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
            final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
            exTransferable.put(new ExTransferable.Single(dbTansferProvider.getConnectionDataFlavor()) {
                protected Object getData() {
                    return dbTansferProvider.createConnectionData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver());
                }
            });
            result = exTransferable;
        } else {
            result = super.clipboardCopy();
        }
        return result;
    }

}
