/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import org.openide.cookies.InstanceCookie;
import org.openide.util.MapFormat;
import org.openide.nodes.*;
import org.openide.util.datatransfer.*;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.dlg.ConnectDialog;

/**
* Node representing open or closed connection to database.
*/

public class ConnectionNode extends DatabaseNode implements InstanceCookie {
    public void setInfo(DatabaseNodeInfo nodeinfo) {
        super.setInfo(nodeinfo);
        DatabaseNodeInfo info = getInfo();

        //String url = info.getDatabase();
        DatabaseOption option = RootNode.getOption();
        Vector cons = option.getConnections();
        Enumeration enu = cons.elements();
        setName(info.getName());

        info.addConnectionListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DatabaseNodeInfo.DATABASE))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.SCHEMA))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.USER))
                    setConnectionName();
                if (evt.getPropertyName().equals(DatabaseNodeInfo.CONNECTION))
                    update((Connection)evt.getNewValue());
            }
        });

        getCookieSet().add(this);
    }

    private void setConnectionName() {
        String displayName = getInfo().getDatabaseConnection().getName();
        setDisplayName(displayName);
    }

    public String instanceName() {
        return "org.netbeans.lib.sql.ConnectionSource"; //NOI18N
    }

    public Class instanceClass() throws IOException, ClassNotFoundException {
        return Class.forName("org.netbeans.lib.sql.ConnectionSource", true, org.openide.TopManager.getDefault ().currentClassLoader ()); //NOI18N
    }

    public Object instanceCreate() {
        DatabaseNodeInfo info = getInfo();
        try {
            Method met;
            Class objclass = instanceClass();
            String drv = info.getDriver();
            String db = info.getDatabase();
            String usr = info.getUser();
            String pwd = info.getPassword();
            String schema = info.getSchema();
            Object obj =  objclass.newInstance();

            met = objclass.getMethod("setDriver", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {drv});
            met = objclass.getMethod("setDatabase", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {db});
            met = objclass.getMethod("setUsername", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {usr});
            met = objclass.getMethod("setPassword", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {pwd});
            met = objclass.getMethod("setSchema", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {schema});

            return obj;

        } catch (Exception ex) {
            ex.printStackTrace ();
            return null;
        }
    }

    
    private void update(Connection connection) {
        boolean connecting = (connection != null);
        DatabaseNodeChildren children = (DatabaseNodeChildren)getChildren();
        DatabaseNodeInfo info = getInfo();
        setIconBase((String)info.get(connecting ? "activeiconbase" : "iconbase")); //NOI18N
        setConnectionName();
        Sheet.Set set = getSheet().get(Sheet.PROPERTIES);

        try {

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

            Node.Property dbproductprop = set.get(DatabaseNodeInfo.DBPRODUCT);
            PropertySupport newdbproductprop = createPropertySupport(dbproductprop.getName(), dbproductprop.getValueType(), dbproductprop.getDisplayName(), dbproductprop.getShortDescription(), info, false);
            set.put(newdbproductprop);
            firePropertyChange("dbproduct",dbproductprop,newdbproductprop); //NOI18N

            Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
            PropertySupport newusrprop = createPropertySupport(usrprop.getName(), usrprop.getValueType(), usrprop.getDisplayName(), usrprop.getShortDescription(), info, !connecting);
            set.put(newusrprop);
            firePropertyChange("user",usrprop,newusrprop); //NOI18N

            Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
            PropertySupport newrememberprop = createPropertySupport(rememberprop.getName(), rememberprop.getValueType(), rememberprop.getDisplayName(), rememberprop.getShortDescription(), info, connecting);
            set.put(newrememberprop);
            firePropertyChange("rememberpassword",rememberprop,newrememberprop); //NOI18N

            if (!connecting)
                children.remove(children.getNodes());
            else {
                DatabaseMetaData dmd = info.getSpecification().getMetaData();

                try {
                    info.put(DefaultAdaptor.PROP_PRODUCTNAME, dmd.getDatabaseProductName());

                    info.put(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, new Boolean(dmd.supportsMixedCaseIdentifiers()));
                    info.put(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, new Boolean(dmd.supportsMixedCaseQuotedIdentifiers()));
                    info.put(DefaultAdaptor.PROP_ALTER_ADD, new Boolean(dmd.supportsAlterTableWithAddColumn()));
                    info.put(DefaultAdaptor.PROP_ALTER_DROP, new Boolean(dmd.supportsAlterTableWithDropColumn()));
                    info.put(DefaultAdaptor.PROP_CONVERT, new Boolean(dmd.supportsConvert()));
                    info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, new Boolean(dmd.supportsTableCorrelationNames()));
                    info.put(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, new Boolean(dmd.supportsDifferentTableCorrelationNames()));
                    info.put(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, new Boolean(dmd.supportsExpressionsInOrderBy()));
                    info.put(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, new Boolean(dmd.supportsOrderByUnrelated()));
                    info.put(DefaultAdaptor.PROP_GROUP_BY, new Boolean(dmd.supportsGroupBy()));
                    info.put(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, new Boolean(dmd.supportsGroupByUnrelated()));
                    info.put(DefaultAdaptor.PROP_BEYOND_GROUP_BY, new Boolean(dmd.supportsGroupByBeyondSelect()));
                    info.put(DefaultAdaptor.PROP_ESCAPE_LIKE, new Boolean(dmd.supportsLikeEscapeClause()));
                    info.put(DefaultAdaptor.PROP_MULTIPLE_RS, new Boolean(dmd.supportsMultipleResultSets()));
                    info.put(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, new Boolean(dmd.supportsMultipleTransactions()));
                    info.put(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, new Boolean(dmd.supportsNonNullableColumns()));
                    info.put(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, new Boolean(dmd.supportsMinimumSQLGrammar()));
                    info.put(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, new Boolean(dmd.supportsCoreSQLGrammar()));
                    info.put(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, new Boolean(dmd.supportsExtendedSQLGrammar()));
                    info.put(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, new Boolean(dmd.supportsANSI92EntryLevelSQL()));
                    info.put(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, new Boolean(dmd.supportsANSI92IntermediateSQL()));
                    info.put(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, new Boolean(dmd.supportsANSI92FullSQL()));
                    info.put(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, new Boolean(dmd.supportsIntegrityEnhancementFacility()));
                    info.put(DefaultAdaptor.PROP_OUTER_JOINS, new Boolean(dmd.supportsOuterJoins()));
                    info.put(DefaultAdaptor.PROP_FULL_OUTER_JOINS, new Boolean(dmd.supportsFullOuterJoins()));
                    info.put(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, new Boolean(dmd.supportsLimitedOuterJoins()));
                    info.put(DefaultAdaptor.PROP_SCHEMAS_IN_DML, new Boolean(dmd.supportsSchemasInDataManipulation()));
                    info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, new Boolean(dmd.supportsSchemasInProcedureCalls()));
                    info.put(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, new Boolean(dmd.supportsSchemasInTableDefinitions()));
                    info.put(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, new Boolean(dmd.supportsSchemasInIndexDefinitions()));
                    info.put(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, new Boolean(dmd.supportsSchemasInPrivilegeDefinitions()));
                    info.put(DefaultAdaptor.PROP_CATALOGS_IN_DML, new Boolean(dmd.supportsCatalogsInDataManipulation()));
                    info.put(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, new Boolean(dmd.supportsCatalogsInProcedureCalls()));
                    info.put(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, new Boolean(dmd.supportsCatalogsInTableDefinitions()));
                    info.put(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, new Boolean(dmd.supportsCatalogsInIndexDefinitions()));
                    info.put(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, new Boolean(dmd.supportsCatalogsInPrivilegeDefinitions()));
                    info.put(DefaultAdaptor.PROP_POSITIONED_DELETE, new Boolean(dmd.supportsPositionedDelete()));
                    info.put(DefaultAdaptor.PROP_POSITIONED_UPDATE, new Boolean(dmd.supportsPositionedUpdate()));
                    info.put(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, new Boolean(dmd.supportsSelectForUpdate()));
                    info.put(DefaultAdaptor.PROP_STORED_PROCEDURES, new Boolean(dmd.supportsStoredProcedures()));
                    info.put(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, new Boolean(dmd.supportsSubqueriesInComparisons()));
                    info.put(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, new Boolean(dmd.supportsSubqueriesInExists()));
                    info.put(DefaultAdaptor.PROP_SUBQUERY_IN_INS, new Boolean(dmd.supportsSubqueriesInIns()));
                    info.put(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, new Boolean(dmd.supportsSubqueriesInQuantifieds()));
                    info.put(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, new Boolean(dmd.supportsCorrelatedSubqueries()));
                    info.put(DefaultAdaptor.PROP_UNION, new Boolean(dmd.supportsUnion()));
                    info.put(DefaultAdaptor.PROP_UNION_ALL, new Boolean(dmd.supportsUnionAll()));
                    info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, new Boolean(dmd.supportsOpenCursorsAcrossCommit()));
                    info.put(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, new Boolean(dmd.supportsOpenCursorsAcrossRollback()));
                    info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, new Boolean(dmd.supportsOpenStatementsAcrossCommit()));
                    info.put(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, new Boolean(dmd.supportsOpenStatementsAcrossRollback()));
                    info.put(DefaultAdaptor.PROP_TRANSACTIONS, new Boolean(dmd.supportsTransactions()));
                    info.put(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, new Boolean(dmd.supportsDataDefinitionAndDataManipulationTransactions()));
                    info.put(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, new Boolean(dmd.supportsDataManipulationTransactionsOnly()));

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
}
