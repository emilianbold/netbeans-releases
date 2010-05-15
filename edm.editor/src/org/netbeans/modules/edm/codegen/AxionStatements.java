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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.edm.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.DBConstants;
import org.netbeans.modules.edm.editor.utils.SQLPart;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.netbeans.modules.edm.editor.utils.ScEncrypt;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.model.DBConnectionDefinition;

/**
 * @author Jonathan Giron
 * @author Ritesh Adval
 */
public class AxionStatements extends BaseStatements {


    public AxionStatements(AbstractDB database) {
        super(database);
    }
    
    @Override
    public SQLPart getDefragStatement(SQLDBTable table, StatementContext context) throws EDMException {
        String tableName = this.genFactory.generate(table, context);
        VelocityContext vContext = new VelocityContext();
        vContext.put("tableName", tableName); // NOI18N
        String defragStatement = TemplateBuilder.generateSql(this.db.getTemplateFileName("defrag"), vContext); // NOI18N
        return createSQLPart(defragStatement, SQLPart.STMT_DEFRAG);
    }

    public SQLPart getCreateDBLinkStatement(DBConnectionDefinition def, String linkName) throws EDMException {
        VelocityContext vContext = new VelocityContext();
        vContext.put("linkName", this.db.getEscapedName(linkName));
        vContext.put("jdbcDriver", def.getDriverClass());
        vContext.put("jdbcUrl", def.getConnectionURL());

        final String userName = def.getUserName();
        String encryptedPassword = ScEncrypt.encrypt(userName, def.getPassword());
        vContext.put("userName", userName);
        vContext.put("password", encryptedPassword);
        vContext.put("otherProperties","");

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("createDbLink"), vContext);
        return createSQLPart(result, SQLPart.STMT_CREATEDBLINK);
    }

    public SQLPart getCreateRemoteTableStatement(SQLDBTable table, String axionTableName, String linkName) throws EDMException {
        StringBuilder sqlBuf = new StringBuilder(100);
        StatementContext context = new StatementContext();
        VelocityContext vContext = new VelocityContext();

        context.setUsingFullyQualifiedTablePrefix(false);

        final String userDefinedTable = table.getUserDefinedTableName();
        try {
            DBConnectionDefinition connDef = table.getParent().getConnectionDefinition();
            int dbType = SQLUtils.getSupportedDBType(connDef.getDBType());

            String catalogName = "";
            String schemaName = "";
            if (table.isUsingFullyQualifiedName()) {
                // Ensure order of precedence for catalog name is followed.
                catalogName = table.getUserDefinedCatalogName();
                if (StringUtil.isNullString(catalogName)) {
                    catalogName = table.getCatalog();
                }

                // Ensure order of precedence for schema name is followed.
                schemaName = table.getUserDefinedSchemaName();
                if (StringUtil.isNullString(schemaName)) {
                    schemaName = table.getSchema();
                }
            }
            
            String tableName = table.getUserDefinedTableName();
            if (StringUtil.isNullString(tableName)) {
                tableName = table.getName() ;
            }
            
            String tablePrefix = table.getTablePrefix();
            if (StringUtil.isNullString(tablePrefix)) {
                tableName = tablePrefix + tableName ;
            }
            
            vContext.put("linkName", this.db.getUnescapedName(linkName));
            vContext.put("remoteName", tableName);
            vContext.put("ifNotExists", Boolean.FALSE);
            vContext.put("orderBy", "");
            vContext.put("where", "");
            vContext.put("schemaName", schemaName);
            vContext.put("catalogName", catalogName);
            
            if(dbType == DBConstants.AXION) {
                vContext.put("vendor", "AXION");
            }

            boolean columnsAreCaseSensitive = (dbType == DBConstants.SYBASE);
            vContext.put("columnsAreCaseSensitive", new Boolean(columnsAreCaseSensitive));
            ((AxionDB)db).setColumnsAreCaseSensitive(columnsAreCaseSensitive);

            table.setUserDefinedTableName(axionTableName);
            String rTableName = this.genFactory.generate(table, context);
            vContext.put("tableName", rTableName);

            List cIdentifiers = new ArrayList();
            List nullableIdentifiers = new ArrayList();
            StringBuilder columnBuf = new StringBuilder(50);

            List columns = (ArrayList) table.getColumnList();
            for (int i = 0; i < columns.size(); i++) {
                // Should be part of expression/type Generator.
                SQLDBColumn column = (SQLDBColumn) columns.get(i);
                String name = db.getEscapedName(column.getName());

                int jdbcTypeInt = column.getJdbcType();
                int precision = column.getPrecision();
                int scale = column.getScale();

                columnBuf.setLength(0);
                columnBuf.append(name).append(" ").append(this.db.getTypeGenerator().generate(jdbcTypeInt, precision, scale));

                cIdentifiers.add(new ColumnIdentifier(null, columnBuf.toString()));
                nullableIdentifiers.add(Boolean.valueOf(column.isNullable()));
            }

            vContext.put("sourceColumnIdentifiers", cIdentifiers);
            vContext.put("nullables", nullableIdentifiers);

            // Don't declare primary key columns for external tables.
            vContext.put("pkIdentifiers", Collections.EMPTY_LIST);

            sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createRemote"), vContext));
        } finally {
            table.setUserDefinedTableName(userDefinedTable);
        }

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATEEXTERNAL);
    }

    public SQLPart getCreateFlatfileTableStatement(SQLDBTable table, String orgPropertiesList, boolean ifNotExists) throws EDMException {
        StringBuilder sqlBuf = new StringBuilder(100);
        StatementContext context = new StatementContext();
        VelocityContext vContext = new VelocityContext();

        context.setUsingFullyQualifiedTablePrefix(false);
        List cIdentifiers = new ArrayList();
        List nullableIdentifiers = new ArrayList();
        StringBuilder columnBuf = new StringBuilder(50);

        List columns = table.getColumnList();
        for (int i = 0; i < columns.size(); i++) {
            // should be part of expression/type Generator.
            SQLDBColumn column = (SQLDBColumn) columns.get(i);
            String name = db.getEscapedName(column.getName());

            int jdbcTypeInt = column.getJdbcType();
            int precision = column.getPrecision();
            int scale = column.getScale();

            columnBuf.setLength(0);
            columnBuf.append(name).append(" ").append(this.db.getTypeGenerator().generate(jdbcTypeInt, precision, scale));

            cIdentifiers.add(new ColumnIdentifier(null, columnBuf.toString()));
            nullableIdentifiers.add(Boolean.valueOf(column.isNullable()));
        }

        vContext.put("sourceColumnIdentifiers", cIdentifiers);
        vContext.put("nullables", nullableIdentifiers);
        vContext.put("pkIdentifiers", Collections.EMPTY_LIST);
        vContext.put("ifNotExists", Boolean.valueOf(ifNotExists));

        if (StringUtil.isNullString(orgPropertiesList)) {
            orgPropertiesList = "";
        }
        vContext.put("orgProperties", orgPropertiesList);

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createExternal"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATEFLATFILE);
    }

    public SQLPart getDropDBLinkStatement(String linkName) throws EDMException {
        VelocityContext vContext = new VelocityContext();
        vContext.put("linkName", linkName);

        String sql = TemplateBuilder.generateSql(this.db.getTemplateFileName("dropDbLink"), vContext);
        return createSQLPart(sql, SQLPart.STMT_DROPDBLINK);
    }

    public SQLPart getDropExternalTableStatement(SQLDBTable table, String axionTableName, boolean ifExists, StatementContext context)
            throws EDMException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }
        context.setUsingFullyQualifiedTablePrefix(false);
        context.putClientProperty("ifExists", Boolean.valueOf(ifExists));

        final String userDefinedTable = table.getUserDefinedTableName();
        try {
            table.setUserDefinedTableName(axionTableName);
            return getDropStatement(table, context);
        } finally {
            table.setUserDefinedTableName(userDefinedTable);
        }
    }


}

