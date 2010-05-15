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
import java.util.Iterator;
import java.util.List;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLGroupBy;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.SQLPart;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.model.PrimaryKey;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class BaseStatements implements Statements {

    protected static final String SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX = "s_column";
    protected static final String SRC_EXP_TO_JDBC_TYPE_MAP = "srcExpToJdbcTypeMap";
    protected AbstractDB db;
    protected AbstractGeneratorFactory genFactory;

    public BaseStatements(AbstractDB database) {
        this.db = database;
        this.genFactory = database.getGeneratorFactory();
    }

    public SQLPart getCreateStatement(SQLDBTable table, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }

        StringBuilder resultBuf = new StringBuilder(50);
        VelocityContext vContext = new VelocityContext();

        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));

        String tableName = this.genFactory.generate(table, context);
        vContext.put("tableName", tableName);
        vContext.put("tempTableName", SQLObjectUtil.generateTemporaryTableName(table.getName()));

        List<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();
        List<Boolean> nullableIdentifiers = new ArrayList<Boolean>();

        List columns = table.getColumnList();
        for (int i = 0; i < columns.size(); i++) {
            // Should be part of expression/type generator.
            SQLDBColumn column = (SQLDBColumn) columns.get(i);
            String name = db.getEscapedName(column.getName());

            int jdbcTypeInt = column.getJdbcType();
            int precision = column.getPrecision();
            int scale = column.getScale();

            resultBuf.setLength(0);
            resultBuf.append(name).append(" ").append(this.db.getTypeGenerator().generate(jdbcTypeInt, precision, scale));

            cIdentifiers.add(new ColumnIdentifier(null, resultBuf.toString()));
            nullableIdentifiers.add(Boolean.valueOf(column.isNullable()));
        }

        vContext.put("sourceColumnIdentifiers", cIdentifiers);
        vContext.put("nullables", nullableIdentifiers);

        List<String> pkIdentifiers = new ArrayList<String>();
        PrimaryKey pk = table.getPrimaryKey();
        if (pk != null && pk.getColumnCount() != 0) {
            Iterator pkIter = pk.getColumnNames().iterator();
            while (pkIter.hasNext()) {
                pkIdentifiers.add(db.getEscapedName(pkIter.next().toString()));
            }
        }

        vContext.put("pkIdentifiers", pkIdentifiers);

        String createStatement = TemplateBuilder.generateSql(this.db.getTemplateFileName("create"), vContext); // NOI18N
        return createSQLPart(createStatement, SQLPart.STMT_CREATE); // NOI18N
    }

    public SQLPart getDefragStatement(SQLDBTable table, StatementContext context) throws EDMException {
        // Null implementation: do nothing. Allow subclasses to override if necessary.
        return null;
    }

    public SQLPart getDeleteStatement(SQLDBTable table, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();
        StringBuilder resultBuf = new StringBuilder();

        //DELETE STATEMENT
        String tableName = this.genFactory.generate(table, context);
        vContext.put("table", tableName);

        String deleteStatement = TemplateBuilder.generateSql(this.db.getTemplateFileName("delete"), vContext); // NOI18N
        resultBuf.append(deleteStatement);

        return createSQLPart(resultBuf.toString(), SQLPart.STMT_DELETE); // NOI18N
    }

    public SQLPart getDropStatement(SQLDBTable table, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();

        String tableName = this.genFactory.generate(table, context);
        vContext.put("tableName", tableName);

        Object prop = context.getClientProperty(StatementContext.IF_EXISTS);
        vContext.put(StatementContext.IF_EXISTS, (prop instanceof Boolean) ? (Boolean) prop : Boolean.FALSE);

        String dropStatement = TemplateBuilder.generateSql(this.db.getTemplateFileName("drop"), vContext); // NOI18N
        return createSQLPart(dropStatement, SQLPart.STMT_DROP);
    }

    public SQLPart getInitializationStatements(StatementContext context) throws EDMException {
        return null;
    }

    protected void populateContextForGroupByAndHaving(SQLObject object, StatementContext context, VelocityContext vContext) throws EDMException {
        SQLGroupBy groupBy = null;
        if (object instanceof SourceTable) {
            groupBy = ((SourceTable) object).getSQLGroupBy();
        } else if (object instanceof SQLJoinView) {
            groupBy = ((SQLJoinView) object).getSQLGroupBy();
        }
        if (groupBy != null && groupBy.getColumns().size() > 0) {
            vContext.put("useGroupBy", Boolean.TRUE);
            List groupByList = createGroupByIdentifierList(object, context);
            vContext.put("groupByIdentifiers", groupByList);

            String havingCondition = getHavingCondition(groupBy, context);
            if (havingCondition != null && !havingCondition.equals("")) {
                vContext.put("havingCondition", havingCondition);
            }
        }
    }

    public SQLPart getRowCountStatement(SQLDBTable table, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();
        boolean isSource = table.getObjectType() == SQLConstants.SOURCE_TABLE;

        vContext.put("tableName", this.genFactory.generate(table, context));
        vContext.put("useWhere", Boolean.FALSE);

        SQLCondition cond = ((SourceTable) table).getFilterCondition();
        SQLObject predicate = (cond != null) ? cond.getRootPredicate() : null;

        if (predicate != null) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("whereCondition", this.genFactory.generate(predicate, context));
        }
        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("rowCount"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_ROWCOUNT);
    }

    public SQLPart getSelectStatement(SourceTable sourceTable, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();

        List sourceColumnIdentifiers = this.createSourceIdentifierList(sourceTable, context);
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.valueOf(context.isUseSourceColumnAliasName()));
        vContext.put("distinct", Boolean.valueOf(sourceTable.isSelectDistinct()));

        vContext.put("selectAliasName", "");
        String tableName = this.genFactory.generate(sourceTable, context);
        vContext.put("fromContent", tableName);

        // Add extraction conditions only if user did not set extraction flag to full.
        vContext.put("condition", "");
        vContext.put("useWhere", Boolean.FALSE);
        vContext.put("nestedIndent", "");
        //NOI18N
        List<SourceTable> sourceList = new ArrayList<SourceTable>(1);
        sourceList.add(sourceTable);
        String condition = getSourceWhereCondition(sourceList, context);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
            vContext.put("notInSql", "");
            vContext.put("integritySql", "");
        }

        Object prop = context.getClientProperty("limit");
        String limit = (prop == null) ? "" : prop.toString();
        vContext.put("limit", limit);

        populateContextForGroupByAndHaving(sourceTable, context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT);
    }

    public SQLPart getSelectStatement(SQLJoinView joinView, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();

        List<ColumnIdentifier> sourceColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        Iterator it = joinView.getSourceTables().iterator();
        while (it.hasNext()) {
            SourceTable table = (SourceTable) it.next();
            sourceColumnIdentifiers.addAll(this.createSourceIdentifierList(table, context));
        }


        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.valueOf(context.isUseSourceColumnAliasName()));

        vContext.put("selectAliasName", "");
        String fromContent = this.genFactory.generate(joinView.getRootJoin(), context);
        vContext.put("fromContent", fromContent);

        // Add extraction conditions only if user did not set extraction flag to full.
        vContext.put("condition", "");
        vContext.put("useWhere", Boolean.FALSE);
        vContext.put("nestedIndent", "");
        String condition = getSourceWhereCondition(joinView.getSourceTables(), context);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
            vContext.put("notInSql", "");
            vContext.put("integritySql", "");
        }


        Object prop = context.getClientProperty("limit");
        String limit = (prop == null) ? "" : prop.toString();
        vContext.put("limit", limit);

        SourceTable[] srcTables = joinView.getSourceTables().toArray(new SourceTable[0]);
        for (SourceTable srcTable : srcTables) {
            populateContextForGroupByAndHaving(srcTable, context, vContext);
        }
        populateContextForGroupByAndHaving(joinView, context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT);
    }

    public SQLPart getSelectStatement(SQLJoinOperator joinOp, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();

        List<ColumnIdentifier> sourceColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        for (SourceTable table : joinOp.getAllSourceTables()) {
            sourceColumnIdentifiers.addAll(this.createSourceIdentifierList(table, context));
        }

        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.valueOf(context.isUseSourceColumnAliasName()));

        vContext.put("selectAliasName", "");
        String fromContent = this.genFactory.generate(joinOp, context);
        vContext.put("fromContent", fromContent);

        // Add extraction conditions only if user did not set extraction flag to full.
        vContext.put("condition", "");
        vContext.put("useWhere", Boolean.FALSE);
        vContext.put("nestedIndent", "");
        String condition = getSourceWhereCondition(joinOp.getAllSourceTables(), context);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
            vContext.put("notInSql", "");
            vContext.put("integritySql", "");
        }


        Object prop = context.getClientProperty("limit");
        String limit = (prop == null) ? "" : prop.toString();
        vContext.put("limit", limit);

        SourceTable[] srcTables = joinOp.getAllSourceTables().toArray(new SourceTable[0]);
        for (SourceTable srcTable : srcTables) {
            populateContextForGroupByAndHaving(srcTable, context, vContext);
        }

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT);
    }

    public SQLPart getTableExistsStatement(SQLDBTable table, StatementContext context) throws EDMException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();

        // WT 63392: Need to replace characters normally used to escape table
        // names with single-quotes in context of using the table name as a String.
        vContext.put("tableName", getUnqualifiedTableName(table, context));

        // If schemaName is supplied in the context, use that value rather than the name
        // associated
        // with the target table - table may be a SourceTable but the appropriate schema
        // to use may
        // not be the value obtained from table.getSchema().
        String schemaName = (String) context.getClientProperty("targetSchema");
        if (StringUtil.isNullString(schemaName)) {
            String uSchema = table.getUserDefinedSchemaName();
            if (StringUtil.isNullString(uSchema)) {
                if (!StringUtil.isNullString(table.getSchema())) {
                    schemaName = table.getSchema().toUpperCase();
                }
            } else {
                schemaName = uSchema.toUpperCase();
            }
        }
        vContext.put("schemaName", schemaName);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("tableExists"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_CHECKTABLEEXISTS); // NOI18N;;
    }

    public SQLPart getTruncateStatement(SQLDBTable targetTable, StatementContext context) throws EDMException {
        VelocityContext vContext = new VelocityContext();

        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));
        vContext.put("tableName", this.genFactory.generate(targetTable, context));

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("truncate"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_TRUNCATE); // NOI18N;;
    }


    public SQLPart normalizeSQLForExecution(SQLPart rawSQLPart) {
        return rawSQLPart;
    }

    protected List<ColumnIdentifier> createGroupByIdentifierList(SQLObject object, StatementContext context) throws EDMException {
        List<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();

        SQLGroupBy rGroupBy = null;
        if (object instanceof SourceTable) {
            rGroupBy = ((SourceTable) object).getSQLGroupBy();
        } else if (object instanceof SQLJoinView) {
            rGroupBy = ((SQLJoinView) object).getSQLGroupBy();
        }
        if (rGroupBy != null && rGroupBy.getColumns().size() > 0) {
            Iterator it = rGroupBy.getColumns().iterator();
            while (it.hasNext()) {
                SQLObject expr = (SQLObject) it.next();
                if (expr instanceof SourceColumn) {
                    String sql = this.genFactory.generate(expr, context);
                    ColumnIdentifier cId = new ColumnIdentifier(null, sql);
                    cIdentifiers.add(cId);
                }
            }
        }

        return cIdentifiers;
    }

    protected List<ColumnIdentifier> createSourceIdentifierList(SourceTable sourceTable, StatementContext context) throws EDMException {
        ArrayList<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();

        Iterator it = sourceTable.getColumnList().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            SourceColumn column = (SourceColumn) it.next();
            if (column.isVisible()) {
                String sql = this.genFactory.generate(column, context);
                ColumnIdentifier cId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
                cIdentifiers.add(cId);
                aliasCount++;
            }
        }

        return cIdentifiers;
    }

    protected SQLPart createSQLPart(String sqlString, String key) {
        SQLPart sqlPart = new SQLPart(sqlString, key, "");
        return sqlPart;
    }

    protected String getSourceWhereCondition(List sTables, StatementContext context) throws EDMException {
        StringBuilder sourceCondition = new StringBuilder(50);
        Iterator it = sTables.iterator();
        int cnt = 0;

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            SQLCondition condition = sTable.getFilterCondition();
            SQLPredicate predicate = condition.getRootPredicate();

            if (predicate != null) {
                if (cnt != 0) {
                    sourceCondition.append(" AND ");
                }
                sourceCondition.append(this.genFactory.generate(predicate, context));
                cnt++;
            }
        }

        return sourceCondition.toString();
    }

    protected String getHavingCondition(SQLGroupBy groupBy, StatementContext context) throws EDMException {
        String havingCondition = null;
        SQLCondition condition = groupBy.getHavingCondition();
        SQLPredicate predicate = condition.getRootPredicate();
        if (predicate != null) {
            havingCondition = this.genFactory.generate(predicate, context);
        }
        return havingCondition;
    }

    protected String getUnqualifiedTableName(SQLDBTable dbTable, StatementContext context) {
        String tableName = "";

        String userDefined = dbTable.getUserDefinedTableName();
        if (StringUtil.isNullString(userDefined)) {
            if (context.isUsingUniqueTableName() || context.isUsingUniqueTableName(dbTable)) {
                tableName = dbTable.getTablePrefix() + dbTable.getUniqueTableName();
            } else {
                tableName = dbTable.getTablePrefix() + dbTable.getName();
            }
        } else {
            tableName = dbTable.getTablePrefix() + userDefined;
        }

        return tableName;
    }

    @SuppressWarnings(value = "unchecked")
    protected List getConditionColumnsNotInList(SQLCondition condition, List origList, StatementContext context) {
        int jdbcType = -1;
        Object obj = null;

        if (context == null) {
            context = new StatementContext();
        }
        List jdbcTypeList = (List) context.getClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST);
        List columnsInCondition = condition.getParticipatingColumns();
        List columnsNotInList = new ArrayList();

        if (jdbcTypeList == null) {
            jdbcTypeList = new ArrayList();
        }

        if (columnsInCondition != null) {
            Iterator itr = columnsInCondition.iterator();
            while (itr.hasNext()) {
                obj = itr.next();
                if (obj instanceof SourceColumn) {
                    if (!origList.contains(obj)) {
                        columnsNotInList.add(obj);
                        jdbcType = ((SourceColumn) obj).getJdbcType();
                        jdbcTypeList.add("" + jdbcType);
                    }
                }
            }
        }

        context.putClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST, jdbcTypeList);
        return columnsNotInList;
    }

    protected List getConditionColumnsNotInList(SQLCondition condition, List origList) {
        return getConditionColumnsNotInList(condition, origList, (StatementContext) null);
    }

    protected List<ColumnIdentifier> createColumnIdentifiersFromSourceColumns(List columnTobeAliased, StatementContext context, int startIndex) throws EDMException {
        return createColumnIdentifiersFromSourceColumns(columnTobeAliased, context, startIndex, SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX);
    }

    protected List<ColumnIdentifier> createColumnIdentifiersFromSourceColumns(List columnTobeAliased, StatementContext context, int startIndex, String aliasPrefix) throws EDMException {
        List<ColumnIdentifier> colIdentifier = new ArrayList<ColumnIdentifier>();
        if ((columnTobeAliased != null) && (columnTobeAliased.size() > 0)) {

            Iterator itr = columnTobeAliased.iterator();
            while (itr.hasNext()) {
                SourceColumn sc = (SourceColumn) itr.next();
                String sql = this.genFactory.generate(sc, context);
                ColumnIdentifier cId = new ColumnIdentifier(aliasPrefix + startIndex, sql);
                colIdentifier.add(cId);
                startIndex++;
            }
        }
        return colIdentifier;
    }

    protected List<ColumnIdentifier> getColIdsFromRM(List<ResolvedMapping> rmList) throws EDMException {
        Iterator it1 = rmList.iterator();
        List<ColumnIdentifier> colIds = new ArrayList<ColumnIdentifier>();
        while (it1.hasNext()) {
            ColumnIdentifier cId = ((ResolvedMapping) it1.next()).getSource();
            colIds.add(cId);
        }
        return colIds;
    }

    protected String replaceColumnNamesWithAliases(List colIdentifier, String sql) throws EDMException {
        if ((colIdentifier != null) && (colIdentifier.size() > 0)) {
            List<String> columnSqlList = new ArrayList<String>();
            List<String> aliasList = new ArrayList<String>();

            Iterator iter = colIdentifier.iterator();
            while (iter.hasNext()) {
                ColumnIdentifier cId = (ColumnIdentifier) iter.next();
                if (!cId.isExpression()) {
                    columnSqlList.add(cId.getSql());
                    aliasList.add(cId.getAliasName());
                }
            }

            String[] columnSqls = new String[0];
            columnSqls = columnSqlList.toArray(columnSqls);
            String[] aliases = new String[0];
            aliases = aliasList.toArray(aliases);
            sql = StringUtil.replaceInString(sql, columnSqls, aliases);
        }

        return sql;
    }

}