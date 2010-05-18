/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.sql.DataSource;

/**
 * just in time database meta information
 * data cached after retrieval
 * used by severnavigator and other clients
 *
 * @author John Kline
 */
public class DatabaseMetaDataHelper {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    private DataSource          dataSource;
    private Connection          connection;
    private DatabaseMetaData    metaData;
    private String[]            schemaNames;
    private String[]            tableNames;
    private TableMetaData[]     tableMetaData;
    private String[]            viewNames;
    private TableMetaData[]     viewMetaData;
    private String[]            procedureNames;
    private ProcedureMetaData[] procedureMetaData;
    private Hashtable           columnNames;
    private boolean             instantiatedWithMetaData = false ;

    public DatabaseMetaDataHelper(DataSource dataSource) throws SQLException {
        init();
        this.dataSource = dataSource;
    }

    public DatabaseMetaDataHelper(Connection connection) throws SQLException {
        init();
        this.connection = connection;
    }

    public DatabaseMetaDataHelper(DatabaseMetaData metaData) throws SQLException {
        init();
        this.metaData = metaData;
        instantiatedWithMetaData = true ;
    }

    private void init() {
        dataSource           = null;
        connection           = null;
        metaData             = null;
        schemaNames          = null;
        tableNames           = null;
        tableMetaData        = null;
        viewNames            = null;
        viewMetaData         = null;
        procedureNames       = null;
        columnNames          = new Hashtable();
    }

    public TableMetaData[] getTableMetaData() throws SQLException {
        if (tableMetaData == null) {
            tableMetaData = getTablesInternal(new String[] {"TABLE"}); // NOI18N
        }
        return tableMetaData;
    }

    public ProcedureMetaData[] getProcedureMetaData() throws SQLException {
        if (procedureMetaData == null) {
            procedureMetaData = getProceduresInternal();
        }
        return procedureMetaData;
    }

    public TableMetaData getTableMetaData(String tableName) throws SQLException {
        getTableMetaData();
        for (int i = 0; i < tableMetaData.length; i++) {
            if (getFullTableName(tableMetaData[i]).equals(tableName)) {
                return tableMetaData[i];
            }
        }
        throw new SQLException(rb.getString("TABLE_NOT_FOUND") + ": " + tableName); // NOI18N
    }

    public ProcedureMetaData getProcedureMetaData(String procedureName) throws SQLException {
        getProcedureMetaData();
        for (int i = 0; i < procedureMetaData.length; i++) {
            if (getFullProcedureName(procedureMetaData[i]).equals(procedureName)) {
                return procedureMetaData[i];
            }
        }
        throw new SQLException(rb.getString("PROCEDURE_NOT_FOUND") + ": " + procedureName); // NOI18N
    }

    public TableMetaData getViewMetaData(String viewName) throws SQLException {
        getViewMetaData();
        for (int i = 0; i < viewMetaData.length; i++) {
            if (getFullTableName(viewMetaData[i]).equals(viewName)) {
                return viewMetaData[i];
            }
        }
        throw new SQLException(rb.getString("VIEW_NOT_FOUND") + ": " + viewName); // NOI18N
    }

    public TableMetaData getTableOrViewMetaData(String name) throws SQLException {
        getTableMetaData();
        for (int i = 0; i < tableMetaData.length; i++) {
            if (getFullTableName(tableMetaData[i]).equals(name)) {
                return tableMetaData[i];
            }
        }
        getViewMetaData();
        for (int i = 0; i < viewMetaData.length; i++) {
            if (getFullTableName(viewMetaData[i]).equals(name)) {
                return viewMetaData[i];
            }
        }
        throw new SQLException(rb.getString("TABLE_OR_VIEW_NOT_FOUND") + ": " + name); // NOI18N
    }

    private String getFullTableName(TableMetaData tmd) throws SQLException {
        String schema = tmd.getMetaInfo(TableMetaData.TABLE_SCHEM);
        if (schema == null || schema.trim().equals("")) {
            schema = "";
        } else {
            schema += "."; // NOI18N
        }
        return schema + tmd.getMetaInfo(TableMetaData.TABLE_NAME);
    }

    private String getFullProcedureName(ProcedureMetaData pmd) throws SQLException {
        String schema = pmd.getMetaInfoAsString(ProcedureMetaData.PROCEDURE_SCHEM);
        if (schema == null || schema.trim().equals("")) {
            schema = "";
        } else {
            schema += "."; // NOI18N
        }
        return schema + pmd.getMetaInfo(ProcedureMetaData.PROCEDURE_NAME);
    }

    public String getNameForSelect(String tableName) throws SQLException {
        TableMetaData tmd = null;
        tmd = getTableOrViewMetaData(tableName);
        String schema = tmd.getMetaInfo(TableMetaData.TABLE_SCHEM);
        if (schema == null || schema.trim().equals("")) {
            schema = "";
        } else {
            schema += "."; // NOI18N
        }
        return schema + tmd.getMetaInfo(TableMetaData.TABLE_NAME);
    }

    public String getProcedureNameForExec(String procedureName) throws SQLException {
        ProcedureMetaData pmd = null;
        pmd = getProcedureMetaData(procedureName);
        String schema = pmd.getMetaInfoAsString(ProcedureMetaData.PROCEDURE_SCHEM);
        if (schema == null || schema.trim().equals("")) {
            schema = "";
        } else {
            schema += "."; // NOI18N
        }
        return schema + pmd.getMetaInfo(ProcedureMetaData.PROCEDURE_NAME);
    }

    public String getNoSchemaName(String tableName) throws SQLException {
        TableMetaData tmd = null;
        tmd = getTableOrViewMetaData(tableName);
        return tmd.getMetaInfo(TableMetaData.TABLE_NAME);
    }

    public String getDisplayName(String tableName) throws SQLException {
        TableMetaData tmd = null;
        tmd = getTableOrViewMetaData(tableName);
        return tmd.getMetaInfo(TableMetaData.TABLE_NAME);
    }

    /** gets all schemas in this database, a 0 length array returned is possible */
    public String[] getSchemas() throws SQLException {
        if (schemaNames == null) {
            ResultSet rs = getMetaData().getSchemas();
            ArrayList list = new ArrayList();
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM"); // NOI18N
                if (schema != null) {
                    list.add(schema);
                }
            }
            rs.close();
            schemaNames = (String[])list.toArray(new String[0]);
        }
        return schemaNames;
    }

    public String[] getTables(String schema) throws SQLException {
        return getTablesInternal(getTableMetaData(), schema);
    }

    public String[] getViews(String schema) throws SQLException {
        return getTablesInternal(getViewMetaData(), schema);
    }

    public String[] getProcedures(String schema) throws SQLException {
        return getProceduresInternal(getProcedureMetaData(), schema);
    }

    /**
     * return true is schema has no tables and no views in it
     */
    boolean isEmpty(String schema) throws SQLException {
        return isEmpty(schema, new String[] {"TABLE"}) && isEmpty(schema, new String[] {"VIEW"});
    }

    boolean isEmpty(String schema, String[] tableTypes) throws SQLException {
        ResultSet rs = getMetaData().getTables(null, schema, "%", tableTypes); // NOI18N
        boolean result = !rs.next();
        rs.close();
        return result;
    }

    /**
     * an alternative to isEmpty
     * this will return the first table or view found in a schema (formatted as a SELECT)
     * if null is returned, the schema is empty
     * in this way, we can set a validation query
     *
     * Note: if schema is null, then the query will be formed from the first table or view
     *       found in any schema
     */
    String getValidationQuery(String schema) throws SQLException {
        ResultSet rs = getMetaData().getTables(null, schema, "%", new String[] {"TABLE", "VIEW"}); // NOI18N
        String validationQuery = null;
        if (rs.next()) {
            String schema_name = rs.getString("TABLE_SCHEM");
            String table_name = rs.getString("TABLE_NAME");

            if (schema_name != null) {
                table_name = schema_name + "." + table_name;
            }
            validationQuery = DesignTimeDataSource.composeSelect(table_name, getMetaData());
        }
        rs.close();
        return validationQuery;
    }

    private String[] getTablesInternal(TableMetaData[] tmd, String schema) throws SQLException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < tmd.length; i++) {
            String tableSchema = tmd[i].getMetaInfo(TableMetaData.TABLE_SCHEM);
            if (schema == null && tableSchema == null
                || (schema != null && tableSchema != null && tableSchema.equals(schema))) {

                list.add(getFullTableName(tmd[i]));
            }
        }
        return (String[])list.toArray(new String[0]);
    }

    private String[] getProceduresInternal(ProcedureMetaData[] pmd, String schema) throws SQLException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < pmd.length; i++) {
            String procedureSchema = pmd[i].getMetaInfoAsString(ProcedureMetaData.PROCEDURE_SCHEM);
            if (schema == null && procedureSchema == null
                || (schema != null && procedureSchema != null && procedureSchema.equals(schema))) {

                list.add(getFullProcedureName(pmd[i]));
            }
        }
        return (String[])list.toArray(new String[0]);
    }

    public String[] getTables() throws SQLException {
        if (tableNames == null) {
            tableNames = new String[getTableMetaData().length];
            for (int i = 0; i < getTableMetaData().length; i++) {
                tableNames[i] = getFullTableName(getTableMetaData()[i]);
            }
        }
        return tableNames;
    }

    public String[] getProcedures() throws SQLException {
        if (procedureNames == null) {
            procedureNames = new String[getProcedureMetaData().length];
            for (int i = 0; i < getProcedureMetaData().length; i++) {
                procedureNames[i] = getFullProcedureName(getProcedureMetaData()[i]);
            }
        }
        return procedureNames;
    }

    public TableMetaData[] getViewMetaData() throws SQLException {
        if (viewMetaData == null) {
            viewMetaData = getTablesInternal(new String[] {"VIEW"}); // NOI18N
        }
        return viewMetaData;
    }

    public String[] getViews() throws SQLException {
        if (viewNames == null) {
            viewNames = new String[getViewMetaData().length];
            for (int i = 0; i < getViewMetaData().length; i++) {
                viewNames[i] = getFullTableName(getViewMetaData()[i]);
            }
        }
        return viewNames;
    }

    public String[] getColumns(String tableName) throws SQLException {
        return getTableOrViewMetaData(tableName).getColumns();
    }

    public String[] getProcedureColumns(String procedureName) throws SQLException {
        return getProcedureMetaData(procedureName).getColumns();
    }

    public ColumnMetaData[] getColumnMetaData(String tableName) throws SQLException {

        return getTableOrViewMetaData(tableName).getColumnMetaData();
    }

    public ColumnMetaData getColumnMetaData(String tableName, String columnName)
        throws SQLException {

        return getTableOrViewMetaData(tableName).getColumnMetaData(columnName);
    }

    public ProcedureColumnMetaData getProcedureColumnMetaData(String procedureName,
        String columnName) throws SQLException {

        return getProcedureMetaData(procedureName).getProcedureColumnMetaData(columnName);
    }

    public TableMetaData[] getTablesInternal(String[] tableTypes) throws SQLException {
        ResultSet rs = getMetaData().getTables(null, null, "%", tableTypes); // NOI18N
        ArrayList list = new ArrayList();
        while (rs.next()) {
            TableMetaData tmd = new TableMetaData(rs, getMetaData()) ;
            // HACK bugfix for 5095727, where mysql's getTables() ignores
            // the "tableTypes" parameter.  I.e., we ask for "VIEW" and get "TABLE" too.
            if (tableTypes == null ){
                list.add(tmd) ;
            } else {
                for ( int icnt = 0 ; icnt < tableTypes.length ; icnt++ ) {
                    // check if the returned tabletype is in the list originally requested.
                    if ( tableTypes[icnt].equals(tmd.getMetaInfo(TableMetaData.TABLE_TYPE) ) ) {
                        list.add(tmd);
                        break ;
                    }
                }
            }
            // -- end HACK bugfix for 5095727
        }
        rs.close();
        return (TableMetaData[])list.toArray(new TableMetaData[0]);
    }

    public ProcedureMetaData[] getProceduresInternal() throws SQLException {
        ResultSet rs = getMetaData().getProcedures(null, null, "%"); // NOI18N
        ArrayList list = new ArrayList();
        while (rs.next()) {
            list.add(new ProcedureMetaData(rs, getMetaData()));
        }
        rs.close();
        return (ProcedureMetaData[])list.toArray(new ProcedureMetaData[0]);
    }
/*
/////////zzz  the old String[] getProcedures() follows:
        if (procedureNames == null) {
            ResultSet rs = getMetaData().getProcedures(null, null, "%"); // NOI18N
            ArrayList list = new ArrayList();
            while (rs.next()) {
                String schema = rs.getString("PROCEDURE_SCHEM"); // NOI18N
                if (schema == null || schema.trim().equals("")) {
                    schema = "";
                } else {
                    schema += "."; // NOI18N
                }
                list.add(schema + rs.getString("PROCEDURE_NAME")); // NOI18N
            }
            rs.close();
            procedureNames = (String[])list.toArray(new String[0]);
        }
        return procedureNames;
    }
//////////zzzzzzzzzzz
 */

    public void refresh() {
        if (dataSource != null) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
        if ( ! instantiatedWithMetaData) {
            metaData = null ;
        }
        schemaNames    = null;
        tableNames     = null;
        tableMetaData  = null;
        viewMetaData   = null;
        viewNames      = null;
        procedureNames = null;
        columnNames.clear();
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
        }
        return connection;
    }

    public /*private*/ DatabaseMetaData getMetaData() throws SQLException {
        if (metaData == null) {
            metaData = getConnection().getMetaData();
        }
        return metaData;
    }

    public boolean isConnected() {
        return ( connection != null ) ;
    }
}
