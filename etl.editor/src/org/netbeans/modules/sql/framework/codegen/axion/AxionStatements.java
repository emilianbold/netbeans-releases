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
package org.netbeans.modules.sql.framework.codegen.axion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.MonitorUtil;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.ResolvedMapping;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.base.BaseStatements;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConnectionFactory;
import com.sun.etl.jdbc.DBConstants;
import com.sun.etl.jdbc.SQLPart;
import com.sun.etl.utils.ScEncrypt;
import com.sun.etl.utils.StringUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * @author Jonathan Giron
 * @author Ritesh Adval
 * @version $Revision$
 */
public class AxionStatements extends BaseStatements {

    protected MonitorUtil mUtil = new MonitorUtil();

    public AxionStatements(AbstractDB database) {
        super(database);
    }
    
    public SQLPart getDefragStatement(SQLDBTable table, StatementContext context) throws BaseException {
        String tableName = this.genFactory.generate(table, context);
        VelocityContext vContext = new VelocityContext();
        vContext.put("tableName", tableName); // NOI18N
        String defragStatement = TemplateBuilder.generateSql(this.db.getTemplateFileName("defrag"), vContext); // NOI18N
        return createSQLPart(defragStatement, SQLPart.STMT_DEFRAG);
    }

    protected void populateContextForInsertSelect(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        super.populateContextForInsertSelect(targetTable, context, vContext);

        StatementContext localContext = new StatementContext();
        localContext.putAll(context);

        vContext.put("nestedIndent", "");
        vContext.put("validationCondition", "");
        vContext.put("errorColumnIdentifiers", Collections.EMPTY_LIST);
        vContext.put("errorValueIdentifiers", Collections.EMPTY_LIST);
        vContext.put("errorLogTable", getDetailsTableName(targetTable));
        vContext.put("aliasErrorColumns", Boolean.FALSE);
        vContext.put("aliasErrorValues", Boolean.TRUE);
        vContext.put("aliasTargetValues", Boolean.TRUE);
        vContext.put("aliasColumns", Boolean.TRUE);

        String validationClause = createValidationConditionClause(targetTable, localContext);
        if (!StringUtil.isNullString(validationClause)) {
            vContext.put("sourceColumnIdentifiers", localContext.getClientProperty("sourceColumnIdentifiers"));
            vContext.put("validationCondition", validationClause);
            vContext.put("conditionQualifier", "FIRST");
            vContext.put("aliasColumns", Boolean.TRUE);

            localContext.setSuppressingTablePrefixForTargetColumn(true);
            localContext.putClientProperty("nestedIndent", "    ");

            vContext.put("targetValueIdentifiers", createTargetValueIdentifierList(targetTable, localContext));
            vContext.put("errorColumnIdentifiers", createErrorColumnIdentifierList(targetTable, localContext));
            vContext.put("errorValueIdentifiers", createErrorValueIdentifierList(targetTable, localContext));
        }
    }

    protected void populateContextForUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        // SELECT START
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        final boolean excludeJoinKeyColumns = false;

        //Use the Table Qualification flag to suppress column prefix
        localContext.setSuppressingTablePrefixForTargetColumn(true);
        List rMappings = createResolvedMappingsForUpdate(targetTable, excludeJoinKeyColumns, localContext);
        localContext.setSuppressingTablePrefixForTargetColumn(false);

        String targetTableSql = this.genFactory.generate(targetTable, localContext);

        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);

        vContext.put("targetTable", targetTableSql);
        // TODO: In new update syntax in axion we need to support DISTINCT
        // old way DISTINCT was part of SELECT but in new syntax we need to
        // account for it.

        vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.INNER_JOIN, localContext));
        vContext.put("nestedIndent", "    ");

        vContext.put("useUpdateWhere", Boolean.FALSE);

        String condition = getWhereCondition(targetTable, localContext);
        String updateWhereClause = getWhereClauseForUpdate(targetTable, context);
        if (condition != null && !condition.equals("")) {
            condition += " AND " + updateWhereClause;
        } else {
            condition = updateWhereClause;
        }

        if (condition != null && !condition.equals("")) {
            vContext.put("useUpdateWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }
        // SELECT END

        vContext.put("mappings", rMappings);

        // exception when
        localContext.putClientProperty("nestedIndent", "");
        localContext.putClientProperty("errorLogTable", getDetailsTableName(targetTable));
        localContext.putClientProperty("errorColumnIdentifiers", vContext.get("targetColumnIdentifiers"));
        localContext.putClientProperty("valueIdentifiers", vContext.get("sourceColumnIdentifiers"));

        vContext.put("exceptionWhen", getValidationExceptionWhenClauseForUpdate(targetTable, localContext));
    }

    public List createResolvedMappingsForUpdate(TargetTable targetTable, boolean excludeKeyColumns, StatementContext context) throws BaseException {
        ArrayList mappings = new ArrayList();
        String targetJoin = getTargetJoinClause(targetTable, SQLConstants.INNER_JOIN, context);

        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            if (column.getValue() != null) {
                String tSql = this.genFactory.generate(column, context);
                if (targetJoin.indexOf(tSql) != -1 && excludeKeyColumns) {
                    continue;
                }

                String sSql = this.genFactory.generate(column.getValue(), localContext);

                ColumnIdentifier sId = new ColumnIdentifier(null, sSql);
                ColumnIdentifier tId = new ColumnIdentifier(null, tSql);
                ResolvedMapping rm = new ResolvedMapping(sId, tId);
                mappings.add(rm);
                aliasCount++;
            }
        }

        return mappings;
    }

    private String getWhereClauseForUpdate(TargetTable targetTable, StatementContext context) throws BaseException {
        SQLCondition joinCondition = targetTable.getJoinCondition();
        SQLPredicate joinPredicate = null;
        if (joinCondition != null) {
            joinPredicate = joinCondition.getRootPredicate();
        }

        if (joinPredicate == null) {
            throw new BaseException("Missing merge condition.");
        }
        return this.genFactory.generate(joinPredicate, context); 
    }

    public SQLPart getMergeStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();
        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        localContext.putClientProperty("nestedIndent", "");

        populateAnsiMergeStatement(targetTable, localContext, vContext);

        localContext.putClientProperty("errorLogTable", getDetailsTableName(targetTable));
        localContext.setUseSourceColumnAliasName(true);
        vContext.put("nestedIndent", "");
        vContext.put("validationCondition", createValidationConditionClause(targetTable, localContext));
        vContext.put("exceptionDML", getErrorLoggingDML(targetTable, localContext));
        vContext.put("exceptionWhen", TemplateBuilder.generateSql(this.db.getTemplateFileName("exceptionWhen"), vContext));

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("merge"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_MERGE); // NOI18N
    }


    /**
     * Creates a DB link creation statement based on the connection information in the
     * given DBConnectionDefinition.
     *
     * @param def DBConnectionDefinition containing connection information to be
     *        incorporated into the new DB link statement
     * @param linkName unique name of link
     * @return SQLPart containing create DB link statement
     * @throws BaseException if error occurs during statement generation
     */
    public SQLPart getCreateDBLinkStatement(DBConnectionDefinition def, String linkName) throws BaseException {
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

    /**
     * Creates an external DB table with the given local name, based on the given
     * SQLDBTable and associated with the DB link with the given name.
     *
     * @param table SQLDBTable containing metadata for the external DB table to be created
     * @param axionTableName local name to be used in referencing the external DB table;
     *        if null, the table name embedded in <code>table</code> is used
     * @param linkName name of DB link to use in resolving connections to
     *        <code>table</code>
     * @return SQLPart containing generated create statement
     * @throws BaseException if error occurs during statement generation
     */
    public SQLPart getCreateRemoteTableStatement(SQLDBTable table, String axionTableName, String linkName) throws BaseException {
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
            vContext.put("ifNotExists", Boolean.TRUE);
            vContext.put("orderBy", "");
            vContext.put("where", "");
            vContext.put("schemaName", schemaName);
            vContext.put("catalogName", catalogName);
            

            if ((table instanceof TargetTable) 
            		&& (((TargetTable)table).isCreateTargetTable())
            		&& (dbType != DBConstants.AXION)){
            	vContext.put("createIfNotExist", Boolean.TRUE);
            }

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

    /**
     * @param table
     * @param logTableName
     * @return
     */
    public SQLPart getCreateFlatfileTableStatement(SQLDBTable table, String orgPropertiesList, boolean ifNotExists) throws BaseException {
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
        //vContext.put("orgProperties", orgPropertiesList);
        vContext.put("orgPropertiesList", orgPropertiesList);
        vContext.put("tableName", table.getName());

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createExternal"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATEFLATFILE);
    }

    /**
     * @param linkName
     * @return
     */
    public SQLPart getDropDBLinkStatement(String linkName) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        vContext.put("linkName", linkName);

        String sql = TemplateBuilder.generateSql(this.db.getTemplateFileName("dropDbLink"), vContext);
        return createSQLPart(sql, SQLPart.STMT_DROPDBLINK);
    }

    /**
     * @param table
     * @return
     */
    public SQLPart getDropExternalTableStatement(SQLDBTable table, String axionTableName, boolean ifExists, StatementContext context)
            throws BaseException {
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

    public String getValidationExceptionWhenClauseForUpdate(TargetTable target, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();

        String indent = (String) context.getClientProperty("nestedIndent");
        vContext.put("nestedIndent", (indent != null) ? indent : "    ");
        vContext.put("validationCondition", createValidationConditionClauseForUpdate(target, context));

        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.putClientProperty("errorLogTable", getDetailsTableName(target));
        localContext.putClientProperty("nestedIndent", indent + "    ");
        vContext.put("exceptionDML", getErrorLoggingDML(target, localContext));

        return TemplateBuilder.generateSql(this.db.getTemplateFileName("exceptionWhen"), vContext);
    }

    /**
     * @param targetTable
     * @param context
     * @return
     * @throws BaseException
     */
    protected String createValidationConditionClause(TargetTable targetTable, StatementContext context) throws BaseException {
        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.setUseSourceTableAliasName(true);
        localContext.setUseSourceColumnAliasName(true);
        String srcColAliasPrefix = (String)context.getClientProperty("mergeConditionSourceColumnAliasPrefix");
        List sourceColumnIdentifiers = (List) context.getClientProperty("sourceColumnIdentifiers");
        List mergeCondColumnIdentifiers = (List) context.getClientProperty("mergeConditionColumnIdentifiers");
        int lastSourceColumnAliasIndex = 0;
        
        if ((sourceColumnIdentifiers == null) || (sourceColumnIdentifiers.size() <= 0)){
            // Create mapping between source column identifiers and their aliases.        	
        	sourceColumnIdentifiers = createSourceIdentifierList(targetTable, localContext);
            context.putClientProperty("sourceColumnIdentifiers", sourceColumnIdentifiers);        	
        }

        if ((mergeCondColumnIdentifiers == null) || (mergeCondColumnIdentifiers.size() <= 0)){
        	mergeCondColumnIdentifiers = new ArrayList();
            mergeCondColumnIdentifiers.addAll(sourceColumnIdentifiers);
        }
        
        
        if (context.getClientProperty("lastSourceAliasIndex") != null){
        	lastSourceColumnAliasIndex = ((Integer) context.getClientProperty("lastSourceAliasIndex")).intValue() ;        	
        }else{
        	lastSourceColumnAliasIndex = sourceColumnIdentifiers.size();
        }
        

        List srcColumnsMappedToTarget = getSourceColsDirectlyMapped(targetTable);

        StringBuilder exceptionBuf = new StringBuilder(100);

        int i = 0;
        Iterator srcIter = targetTable.getSourceTableList().iterator();
        while (srcIter.hasNext()) {
            SourceTable srcTable = (SourceTable) srcIter.next();
            SQLCondition condition = srcTable.getDataValidationCondition();
            if (condition != null) {
                SQLPredicate predicate = condition.getRootPredicate();
                if (predicate != null) {
                    String conditionSql = this.db.createGeneratorFactory().generate(predicate, localContext);
                    if (conditionSql != null) {
                        if (i++ != 0) {
                            exceptionBuf.append(" AND ");
                        }

                        //Validation conditions operate on one Source table at a time.
                        List columnsTobeAliased = getConditionColumnsNotInList(condition, srcColumnsMappedToTarget);
                        List missingCondColIdentifiers = null;
                        List missingSrcColIdentifiers = null;                        
                        if ((srcColAliasPrefix != null) && !"".equals(srcColAliasPrefix)){
                        	missingCondColIdentifiers = createColumnIdentifiersFromSourceColumns(columnsTobeAliased, localContext,
                            		lastSourceColumnAliasIndex + 1, srcColAliasPrefix + "." + SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX);
                        	
                        }else{
                        	missingCondColIdentifiers = createColumnIdentifiersFromSourceColumns(columnsTobeAliased, localContext,
                            		lastSourceColumnAliasIndex + 1);                         	
                        }
                        missingSrcColIdentifiers = createColumnIdentifiersFromSourceColumns(columnsTobeAliased, localContext, lastSourceColumnAliasIndex + 1);
                        
                        sourceColumnIdentifiers.addAll(missingSrcColIdentifiers);
                        mergeCondColumnIdentifiers.addAll(missingCondColIdentifiers);
                        conditionSql = replaceColumnNamesWithAliases(mergeCondColumnIdentifiers, conditionSql);
                        exceptionBuf.append(conditionSql);
                    }
                }
            }
        }

        return exceptionBuf.toString();
    }

    /**
     * @param targetTable
     * @param context
     * @return
     * @throws BaseException
     */
    protected String createValidationConditionClauseForUpdate(TargetTable targetTable, StatementContext context) throws BaseException {
        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.setUseSourceTableAliasName(true);

        StringBuilder exceptionBuf = new StringBuilder(100);

        int i = 0;
        Iterator srcIter = targetTable.getSourceTableList().iterator();
        while (srcIter.hasNext()) {
            SourceTable srcTable = (SourceTable) srcIter.next();
            SQLCondition condition = srcTable.getDataValidationCondition();
            if (condition != null) {
                SQLPredicate predicate = condition.getRootPredicate();
                if (predicate != null) {
                    String conditionSql = this.db.createGeneratorFactory().generate(predicate, localContext);
                    if (conditionSql != null) {
                        if (i++ != 0) {
                            exceptionBuf.append(" AND ");
                        }
                        exceptionBuf.append(conditionSql);
                    }
                }
            }
        }

        return exceptionBuf.toString();
    }

    /**
     * @param target
     * @param context
     * @return
     */
    protected String getErrorLoggingDML(TargetTable target, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        populateContextForErrorLoggingDML(target, context, vContext);
        return TemplateBuilder.generateSql(this.db.getTemplateFileName("logError"), vContext);
    }

    protected void populateContextForErrorLoggingDML(TargetTable target, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        localContext.putAll(context);

        localContext.setSuppressingTablePrefixForTargetColumn(true);
        localContext.setUseSourceTableAliasName(true);
        localContext.setUseTargetTableAliasName(true);

        String indent = (String) localContext.getClientProperty("nestedIndent");

        vContext.put("errorLogTable", localContext.getClientProperty("errorLogTable"));
        Object obj = context.getClientProperty("errorColumnIdentifiers");
        if (obj instanceof List) {
            vContext.put("errorColumnIdentifiers", obj);
        } else {
            vContext.put("errorColumnIdentifiers", createErrorColumnIdentifierList(target, localContext));
        }

        obj = context.getClientProperty("errorValueIdentifiers");
        if (obj instanceof List) {
            vContext.put("errorValueIdentifiers", obj);
        } else {
            vContext.put("errorValueIdentifiers", createErrorValueIdentifierList(target, localContext));
        }

        vContext.put("aliasErrorValues", localContext.isUseSourceColumnAliasName() ? Boolean.TRUE : Boolean.FALSE);
        vContext.put("aliasErrorColumns", localContext.isUseTargetColumnAliasName() ? Boolean.TRUE : Boolean.FALSE);

        vContext.put("nestedIndent", (indent != null) ? indent : "");
    }

    private List createErrorColumnIdentifierList(TargetTable targetTable, StatementContext context) throws BaseException {
        List cIdentifiers = new ArrayList();

        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            String sql = this.genFactory.generate(column, context);
            ColumnIdentifier cId = new ColumnIdentifier(TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
            cIdentifiers.add(cId);
            aliasCount++;
        }

        return cIdentifiers;
    }

    protected List createErrorValueIdentifierList(TargetTable targetTable, StatementContext context) throws BaseException {
        List cIdentifiers = new ArrayList();
        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            if (column.getValue() != null) {
                String sql = this.genFactory.generate(column.getValue(), context);
                ColumnIdentifier cId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
                cIdentifiers.add(cId);
                aliasCount++;
            }
        }

        return cIdentifiers;
    }

    protected List createTargetValueIdentifierList(TargetTable targetTable, StatementContext context) throws BaseException {
        List cIdentifiers = new ArrayList();

        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            if (column.getValue() != null) {
                String sql = this.genFactory.generate(column.getValue(), context);
                ColumnIdentifier cId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
                cIdentifiers.add(cId);
                aliasCount++;
            }
        }

        return cIdentifiers;
    }

    protected String getDetailsTableName(TargetTable table) {
        return MonitorUtil.getDetailsTableName(table);
    }
}

