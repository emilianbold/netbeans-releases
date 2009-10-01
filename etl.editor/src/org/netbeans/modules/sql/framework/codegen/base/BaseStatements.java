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
package org.netbeans.modules.sql.framework.codegen.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.ResolvedMapping;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.SubSelectIdentifier;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.sql.framework.model.PrimaryKey;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseStatements implements Statements {

    public static final String LOG_SUMMARY_TABLE_NAME = "SUMMARY";
    protected static final String SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX = "s_column";
    protected static final String TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX = "d_column";
    protected static final String SRC_EXP_TO_JDBC_TYPE_MAP = "srcExpToJdbcTypeMap";
    protected AbstractDB db;
    protected AbstractGeneratorFactory genFactory;

    public BaseStatements(AbstractDB database) {
        this.db = database;
        this.genFactory = database.getGeneratorFactory();
    }

    public List<ResolvedMapping> createResolvedMappings(TargetTable targetTable, boolean excludeKeyColumns, StatementContext context) throws BaseException {
        List<ResolvedMapping> mappings = new ArrayList<ResolvedMapping>();
        String targetJoin = getTargetJoinClause(targetTable, SQLConstants.INNER_JOIN, context);

        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            if (column.getValue() != null) {
                String tSql = this.genFactory.generate(column, context);
                if (targetJoin.indexOf(tSql) != -1 && excludeKeyColumns) {
                    continue;
                }

                String sSql = this.genFactory.generate(column.getValue(), context);
                ColumnIdentifier sId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sSql);
                ColumnIdentifier tId = new ColumnIdentifier(TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, tSql);
                ResolvedMapping rm = new ResolvedMapping(sId, tId);
                mappings.add(rm);
                aliasCount++;
            }
        }

        return mappings;
    }

    /**
     * Creates SQL statement to generate log summary table.
     *
     * @param useMemoryTable true if statement should use syntax for Axion memory table,
     *        false if a delimited flatfile table is required.
     * @return SQLPart containing appropriate create statement for summary table
     */
    public SQLPart getCreateLogSummaryTableStatement(boolean useMemoryTable) throws BaseException {
        String tableName = LOG_SUMMARY_TABLE_NAME;
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = new VelocityContext();
        vContext.put("indent", "        ");

        vContext.put("tableName", tableName);
        vContext.put("fileName", tableName + ".bad");
        vContext.put("recordDelimiter", "\\r\\n");
        vContext.put("fieldDelimiter", ",");
        vContext.put("textQualifier", "\"");
        vContext.put("isFirstLineHeader", "true");

        vContext.put("ifNotExists", Boolean.TRUE);
        vContext.put("useMemoryTable", Boolean.valueOf(useMemoryTable));

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createLogSummaryTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATELOGSUMMARYTABLE);
    }

    public SQLPart getCreateStatement(SQLDBTable table, StatementContext context) throws BaseException {
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

    public SQLPart getDefragStatement(SQLDBTable table, StatementContext context) throws BaseException {
        // Null implementation: do nothing. Allow subclasses to override if necessary.
        return null;
    }

    public SQLPart getDeleteInvalidRowFromSummaryTableStatement(TargetTable table) throws BaseException {
        String summaryTable = LOG_SUMMARY_TABLE_NAME;
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = new VelocityContext();

        vContext.put("tableName", summaryTable);

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("deleteInvalidRowFromSummaryTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_DELETEINVALIDROWFROMSUMMARY);
    }

    public SQLPart getDeleteStatement(SQLDBTable table, StatementContext context) throws BaseException {
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

        //WHERE condition
        Object prop = context.getClientProperty("useWhere");
        boolean useWhere = (prop instanceof Boolean) ? ((Boolean) prop).booleanValue() : false;
        if (table instanceof TargetTable && useWhere) {
            String condition = getTargetWhereCondition((TargetTable) table, context);
            if (condition != null && !condition.trim().equals("")) {
                vContext = new VelocityContext();
                vContext.put("condition", condition);
                vContext.put("nestedIndent", "");
                String whereClause = TemplateBuilder.generateSql(this.db.getTemplateFileName("where"), vContext); // NOI18N
                resultBuf.append(whereClause);
            }
        }

        return createSQLPart(resultBuf.toString(), SQLPart.STMT_DELETE); // NOI18N
    }

    public SQLPart getDropStatement(SQLDBTable table, StatementContext context) throws BaseException {
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

    public SQLPart getInitializationStatements(StatementContext context) throws BaseException {
        return null;
    }

    public SQLPart getInsertSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        this.populateContextForInsertSelect(targetTable, context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("insertSelect"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_INSERTSELECT); // NOI18N
    }

    /**
     * @param table
     * @param logTableName
     * @return
     */
    public SQLPart getInsertStartDateIntoSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException {
        String summaryTable = LOG_SUMMARY_TABLE_NAME;
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = new VelocityContext();

        vContext.put("tableName", summaryTable);
        vContext.put("targetTable", getTableNameForStatisticsMetadata(table, context));

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("insertStartDateIntoSummaryTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_INSERTEXECUTIONRECORD);
    }

    public SQLPart getMergeStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }

        StringBuilder mergeStatement = new StringBuilder(100);
        mergeStatement.append(this.getUpdateStatement(targetTable, context).getSQL().toString());
        mergeStatement.append(Character.toString(SQLPart.STATEMENT_SEPARATOR));
        mergeStatement.append(this.getInsertSelectStatement(targetTable, context).getSQL().toString());

        return createSQLPart(mergeStatement.toString(), SQLPart.STMT_MERGE); // NOI18N;
    }

    /**
     * Returns Source Table columns directly mapped to target table.
     * Also updates JDBC types of all the mapped target columns whether directly or thru expression.
     * @param tt
     * @param context
     * @return
     */
    protected List getSourceColsDirectlyMapped(TargetTable tt, StatementContext context) {
        if (context == null) {
            context = new StatementContext();
        }

        List<SourceColumn> srcColsMappedToTgt = new ArrayList<SourceColumn>();
        Iterator it = tt.getMappedColumns().iterator();
        List<String> jdbcTypeList = new ArrayList<String>();
        String val = null;

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            SQLObject exp = column.getValue();
            val = "" + column.getJdbcType();
            jdbcTypeList.add(val);
            if (exp instanceof SourceColumn) {
                SourceColumn srcColumn = (SourceColumn) exp;
                srcColsMappedToTgt.add(srcColumn);
            }
        }
        context.putClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST, jdbcTypeList);
        return srcColsMappedToTgt;
    }

    protected List getSourceColsDirectlyMapped(TargetTable tt) {
        return getSourceColsDirectlyMapped(tt, (StatementContext) null);
    }

    protected void populateAnsiMergeStatement(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        List<ColumnIdentifier> sourceColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        List<ColumnIdentifier> targetColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        List<ColumnIdentifier> subSelectIdentifiers = new ArrayList<ColumnIdentifier>();
        List<ResolvedMapping> joinAliases = new ArrayList<ResolvedMapping>();
        List srcColDirectlyMapped = getSourceColsDirectlyMapped(targetTable);
        int lastAliasIndex = 0;

        final boolean excludeJoinKeyColumns = false;

        if (context == null) {
            context = new StatementContext();
        }

        SubSelectIdentifier subIdentifier = new SubSelectIdentifier("J1");

        //SELECT START
        context.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);
        //context.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        String targetJoin = getTargetJoinClause(targetTable, SQLConstants.INNER_JOIN, context);
        List rMappings = createResolvedMappings(targetTable, excludeJoinKeyColumns, context);
        if (rMappings != null) {
            lastAliasIndex = rMappings.size();
        }
        ListIterator it = rMappings.listIterator();
        while (it.hasNext()) {
            ResolvedMapping rm = (ResolvedMapping) it.next();

            ColumnIdentifier oldSrc = rm.getSource();
            sourceColumnIdentifiers.add(oldSrc);

            ColumnIdentifier cId = new ColumnIdentifier(subIdentifier.getAliasName() + "." + rm.getSource().getAliasName(), oldSrc.getSql());
            subSelectIdentifiers.add(cId);
            rm.setSource(cId);

            targetColumnIdentifiers.add(rm.getTarget());

            // If the target column is involved in the join condition, remove its ResolvedMapping
            // from the update statement list and remember it so that we can substitute the
            // appropraite aliases for its source column in the join condition.
            if (targetJoin.indexOf(rm.getTarget().getSql()) != -1) {
                it.remove();
                joinAliases.add(rm);
            }
        }

        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));

        // Merge condition
        String mergeCondition = "";
        SQLCondition cond = targetTable.getJoinCondition();
        List<ColumnIdentifier> colIds = null;
        if (cond != null) {
            SQLPredicate predicate = cond.getRootPredicate();
            if (predicate != null) {
                mergeCondition = this.genFactory.generate(predicate, context);
                List columnsTobeAliased = getConditionColumnsNotInList(cond, srcColDirectlyMapped);
                List<ColumnIdentifier> missingSourceColIds = this.createColumnIdentifiersFromSourceColumns(columnsTobeAliased, context, lastAliasIndex + 1);
                List<ColumnIdentifier> missingMergeColIds = this.createColumnIdentifiersFromSourceColumns(columnsTobeAliased, context, lastAliasIndex + 1, subIdentifier.getAliasName() + "." + SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX);
                lastAliasIndex = lastAliasIndex + missingSourceColIds.size();
                colIds = getColIdsFromRM(joinAliases);
                sourceColumnIdentifiers.addAll(missingSourceColIds);
                colIds.addAll(missingMergeColIds);
                // now replace the column name for source with alias name
                mergeCondition = replaceColumnNamesWithAliases(colIds, mergeCondition);
            }
        }

        if (!StringUtil.isNullString(mergeCondition)) {
            vContext.put("mergeCondition", mergeCondition);
        }

        //context.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        String targetTableName = this.genFactory.generate(targetTable, context);

        vContext.put("targetTable", targetTableName);
        vContext.put("subSelectAliasName", subIdentifier.getAliasName());
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);
        vContext.put("targetColumnIdentifiers", targetColumnIdentifiers);
        vContext.put("valueIdentifiers", subSelectIdentifiers);

        vContext.put("aliasColumns", Boolean.TRUE);
        //vContext.put("distinct", Boolean.FALSE);
        vContext.put("distinct", areDistinctRowsRequired(targetTable));
        vContext.put("selectAliasName", "");
        vContext.put("fromContent", getFromStatementContent(targetTable, context));
        vContext.put("lastSourceAliasIndex", new Integer(lastAliasIndex));

        vContext.put("useWhere", Boolean.FALSE);

        String condition = getWhereCondition(targetTable, context);

        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }
        //SELECT END
        // errorValueIdentifiers is require only for validation
        context.putClientProperty("errorValueIdentifiers", subSelectIdentifiers);
        context.putClientProperty("sourceColumnIdentifiers", sourceColumnIdentifiers);
        context.putClientProperty("mergeConditionSourceColumnAliasPrefix", subIdentifier.getAliasName());
        if ((colIds != null) && (colIds.size() > 0)) {
            context.putClientProperty("mergeConditionColumnIdentifiers", colIds);
        }

        vContext.put("mappings", rMappings);
    }

    public SQLPart getOnePassSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();

        context.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        List sourceColumnIdentifiers = this.createSourceIdentifierList(targetTable, context);
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.FALSE);
        vContext.put("distinct", areDistinctRowsRequired(targetTable));

        vContext.put("selectAliasName", "");
        vContext.put("fromContent", getFromStatementContent(targetTable, context));
        vContext.put("nestedIndent", "");

        String condition = getWhereCondition(targetTable, context);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }

        SourceTable[] srcTables = targetTable.getSourceTableList().toArray(new SourceTable[0]);
        for (SourceTable srcTable : srcTables) {
            populateContextForGroupByAndHaving(srcTable, context, vContext);
        }

        populateContextForGroupByAndHaving(targetTable, context, vContext);
        populateContextForGroupByAndHaving(targetTable.getJoinView(), context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT);
    }

    protected void populateContextForGroupByAndHaving(SQLObject object, StatementContext context, VelocityContext vContext) throws BaseException {
        SQLGroupBy groupBy = null;
        if (object instanceof SourceTable) {
            groupBy = ((SourceTable) object).getSQLGroupBy();
        } else if (object instanceof TargetTable) {
            groupBy = ((TargetTable) object).getSQLGroupBy();
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

    public SQLPart getPreparedInsertStatement(SQLDBTable table, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        this.populateContextForPrepStmtInsert(table, context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("insertValues"), vContext); // NOI18N
        SQLPart sqlPart = createSQLPart(result, SQLPart.STMT_INSERT);
        sqlPart.setAttribute(SQLPart.ATTR_JDBC_TYPE_LIST, context.getClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST));

        return sqlPart;
    }

    public SQLPart getRowCountStatement(SQLDBTable table, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();
        boolean isSource = table.getObjectType() == SQLConstants.SOURCE_TABLE;
        boolean fullExtraction = isSource && "full".equalsIgnoreCase(((SourceTable) table).getExtractionType());

        vContext.put("tableName", this.genFactory.generate(table, context));
        vContext.put("useWhere", Boolean.FALSE);

        if (isSource && !fullExtraction) {
            SQLCondition cond = ((SourceTable) table).getExtractionCondition();
            SQLObject predicate = (cond != null) ? cond.getRootPredicate() : null;

            if (predicate != null) {
                vContext.put("useWhere", Boolean.TRUE);
                vContext.put("whereCondition", this.genFactory.generate(predicate, context));
            }
        }
        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("rowCount"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_ROWCOUNT);
    }

    public SQLPart getSelectExecutionIdFromSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException {
        String summaryTable = LOG_SUMMARY_TABLE_NAME;
        StringBuilder sqlBuf = new StringBuilder(100);

        VelocityContext vContext = new VelocityContext();
        vContext.put("summaryTable", summaryTable);
        vContext.put("targetTable", getTableNameForStatisticsMetadata(table, context));

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("selectExecutionIdFromSummaryTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_SELECTEXECUTIONIDFROMSUMMARY);
    }

    public SQLPart getSelectStatement(SourceTable sourceTable, StatementContext context) throws BaseException {
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
        if (!"full".equalsIgnoreCase(sourceTable.getExtractionType())) {
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
        }

        Object prop = context.getClientProperty("limit");
        String limit = (prop == null) ? "" : prop.toString();
        vContext.put("limit", limit);

        populateContextForGroupByAndHaving(sourceTable, context, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT);
    }

    public SQLPart getSelectStatement(SQLJoinView joinView, StatementContext context) throws BaseException {
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

    public SQLPart getSelectStatement(SQLJoinOperator joinOp, StatementContext context) throws BaseException {
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

    public SQLPart getSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }

        VelocityContext vContext = new VelocityContext();

        List<ColumnIdentifier> sourceColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        Iterator it = targetTable.getColumnList().iterator();
        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            String sql = this.genFactory.generate(column, context);
            ColumnIdentifier cId = new ColumnIdentifier(column.getDisplayName(), sql);
            sourceColumnIdentifiers.add(cId);
        }
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.FALSE);
        vContext.put("distinct", Boolean.FALSE);

        vContext.put("selectAliasName", "");
        vContext.put("fromContent", this.genFactory.generate(targetTable, context));
        vContext.put("useWhere", Boolean.FALSE);
        vContext.put("nestedIndent", "");

        Object prop = context.getClientProperty("limit");
        String limit = (prop == null) ? "" : prop.toString();
        vContext.put("limit", limit);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT); // NOI18N
    }

    public SQLPart getStaticInsertStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }
        localContext.setSuppressingTablePrefixForTargetColumn(true);

        VelocityContext vContext = new VelocityContext();
        this.populateContextForStaticInsert(targetTable, localContext, vContext);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("insertValues"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_STATICINSERT); // NOI18N
    }

    public String getSummaryTableName() {
        return LOG_SUMMARY_TABLE_NAME;
    }

    public SQLPart getTableExistsStatement(SQLDBTable table, StatementContext context) throws BaseException {
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

    public SQLPart getTruncateStatement(SQLDBTable targetTable, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();

        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));
        vContext.put("tableName", this.genFactory.generate(targetTable, context));

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("truncate"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_TRUNCATE); // NOI18N;;
    }

    public SQLPart getUpdateEndDateInSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException {
        String summaryTable = LOG_SUMMARY_TABLE_NAME;
        StringBuilder sqlBuf = new StringBuilder(100);

        VelocityContext vContext = new VelocityContext();
        vContext.put("tableName", summaryTable);
        vContext.put("targetTable", getTableNameForStatisticsMetadata(table, context));

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("updateEndDateInSummaryTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_UPDATEEXECUTIONRECORD);
    }

    public SQLPart getUpdateStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        String templateName = "";

        if (targetTable.getSourceTableList().size() != 0) {
            populateContextForUpdate(targetTable, context, vContext);
            templateName = this.db.getTemplateFileName("update"); // NOI18N
        } else {
            populateContextForStaticUpdate(targetTable, context, vContext);
            templateName = this.db.getTemplateFileName("updateStatic"); // NOI18N
        }
        String result = TemplateBuilder.generateSql(templateName, vContext);

        return createSQLPart(result, SQLPart.STMT_UPDATE); // NOI18N
    }

    // TODO Voilates Statements interface pattern, need to redesign the interfaces.
    public Map getCorrelatedUpdateStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        throw new UnsupportedOperationException("Not supported for this DB yet.");
    }

    /**
     * Implements no-op version of method signature - concrete subclasses should override to
     * perform any formatting of the SQL statement in <code>rawSQLPart</code> that is required
     * for a particular JDBC driver to accept and execute it.
     *
     * @param rawSQLPart SQLPart containing SQL statement to be normalized
     * @return SQLPart containing normalized SQL statement
     */
    public SQLPart normalizeSQLForExecution(SQLPart rawSQLPart) {
        return rawSQLPart;
    }

    /**
     * Indicates whether one or more of the source tables, if any, associated with the given
     * TargetTable require the use of the DISTINCT keyword when selecting rows from them.
     *
     * @param targetTable TargetTable whose associated SourceTables are to be interrogated
     * @return Boolean.TRUE if at least one table requires the DISTINCT keyword; Boolean.FALSE
     * otherwise
     */
    protected final Boolean areDistinctRowsRequired(TargetTable targetTable) throws BaseException {
        Boolean useDistinct = Boolean.FALSE;
        Iterator srcIter = targetTable.getSourceTableList().iterator();
        while (srcIter.hasNext()) {
            SourceTable sourceTable = (SourceTable) srcIter.next();
            if (sourceTable.isSelectDistinct()) {
                useDistinct = Boolean.TRUE;
                break;
            }
        }
        return useDistinct;
    }

    private List<ColumnIdentifier> getIntegrityCheckCols(List tgtCols, StatementContext context) throws BaseException {
        List<ColumnIdentifier> colIds = new ArrayList<ColumnIdentifier>();
        Iterator itr = tgtCols.iterator();
        TargetColumn tgtCol = null;
        ColumnIdentifier colId = null;
        String sql = null;

        while (itr.hasNext()) {
            tgtCol = (TargetColumn) itr.next();
            sql = this.genFactory.generate(tgtCol, context);
            colId = new ColumnIdentifier(null, sql);
            colIds.add(colId);
        }

        return colIds;
    }

    protected String appendSQLForIntegrityCheck(TargetTable targetTable, StatementContext context) throws BaseException {
        //SQL code to check key integrity
        SQLCondition cond = targetTable.getJoinCondition();
        List colIds = null;

        if (cond != null) {
            SQLPredicate predicate = cond.getRootPredicate();
            if (predicate != null) {
                List tgtCols = predicate.getTargetColumnsUsed();
                colIds = getIntegrityCheckCols(tgtCols, context);
            }
        }

        VelocityContext vContext = new VelocityContext();
        String result = "";

        if ((colIds != null) && (colIds.size() > 0)) {
            vContext.put("targetColumnIdentifiers", colIds);
            result = TemplateBuilder.generateSql(this.db.getTemplateFileName("selectIntegrityCheck"), vContext); // NOI18N
        }

        return result;
    }

    /**
     * Gets List of ColumnIdentifiers representing all columns in a source table.
     *
     * @param sourceTable
     * @param context
     * @return
     * @throws BaseException
     */
    protected List<ColumnIdentifier> createGroupByIdentifierList(SQLObject object, StatementContext context) throws BaseException {
        List<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();

        SQLGroupBy rGroupBy = null;
        if (object instanceof SourceTable) {
            rGroupBy = ((SourceTable) object).getSQLGroupBy();
        } else if (object instanceof TargetTable) {
            rGroupBy = ((TargetTable) object).getSQLGroupBy();
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
                } else if (expr instanceof TargetColumn) {
                    TargetColumn col = (TargetColumn) expr;
                    String sql = this.genFactory.generate(col.getValue(), context);
                    ColumnIdentifier cId = new ColumnIdentifier(null, sql);
                    cIdentifiers.add(cId);
                }
            }
        }

        return cIdentifiers;
    }

    /**
     * Gets List of ColumnIdentifiers representing all columns in a source table.
     *
     * @param sourceTable
     * @param context
     * @return
     * @throws BaseException
     */
    protected List<ColumnIdentifier> createSourceIdentifierList(SourceTable sourceTable, StatementContext context) throws BaseException {
        ArrayList<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();

        Iterator it = sourceTable.getColumnList().iterator();
        int aliasCount = 1;

        while (it.hasNext()) {
            SourceColumn column = (SourceColumn) it.next();
            if (column.isVisible() || !column.isNullable()) {
                String sql = this.genFactory.generate(column, context);
                ColumnIdentifier cId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
                cIdentifiers.add(cId);
                aliasCount++;
            }
        }

        return cIdentifiers;
    }

    protected List<ColumnIdentifier> createSourceIdentifierList(TargetTable targetTable, StatementContext context) throws BaseException {
        List<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();
        int aliasCount = 1;

        for (TargetColumn column : targetTable.getMappedColumns()) {
            SQLObject mapExpression = column.getValue();
            if (mapExpression != null) {

                String sql = this.genFactory.generate(column.getValue(), context);
                ColumnIdentifier cId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
                cIdentifiers.add(cId);
                aliasCount++;

                if (mapExpression instanceof SourceColumn) {
                    cId.setExpression(false);
                } else {
                    cId.setExpression(true);
                }
            }
        }
        return cIdentifiers;
    }

    /**
     * Creates SQLPart Object
     *
     * @param sqlString the generated SQL statement string
     * @param key key used in the statement map
     * @return SQLPart
     */
    protected SQLPart createSQLPart(String sqlString, String key) {
        SQLPart sqlPart = new SQLPart(sqlString, key, "");
        return sqlPart;
    }

    protected List<ColumnIdentifier> createTargetIdentifierList(TargetTable targetTable, StatementContext context) throws BaseException {
        List<ColumnIdentifier> cIdentifiers = new ArrayList<ColumnIdentifier>();
        int aliasCount = 1;

        for (TargetColumn column : targetTable.getMappedColumns()) {
            String sql = this.genFactory.generate(column, context);
            ColumnIdentifier cId = new ColumnIdentifier(TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
            cIdentifiers.add(cId);
            aliasCount++;
        }
        return cIdentifiers;
    }

    protected String getFromStatementContent(TargetTable targetTable, StatementContext context) throws BaseException {
        SQLJoinView joinView = targetTable.getJoinView();

        if (joinView != null) {
            SQLJoinOperator join = joinView.getRootJoin();
            if (join != null) {
                return this.genFactory.generate(join, context);
            }
            throw new BaseException("Cannot create FROM statement: join is null");
        }

        List sTables = targetTable.getSourceTableList();
        if (sTables.size() != 1) {
            throw new BaseException("Cannot create FROM statement: expected 1 source table, found " + sTables.size());
        }

        return this.genFactory.generate((SQLObject) sTables.get(0), context);
    }

    protected String getFromStatementContentForTarget(TargetTable targetTable, int joinType, StatementContext context) throws BaseException {
        return getTargetJoinClause(targetTable, joinType, context);
    }

    /**
     * @param sTables
     * @param context
     * @return
     * @throws BaseException
     */
    protected String getSourceWhereCondition(List sTables, StatementContext context) throws BaseException {
        StringBuilder sourceCondition = new StringBuilder(50);
        Iterator it = sTables.iterator();
        int cnt = 0;

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            SQLCondition condition = sTable.getExtractionCondition();
            SQLPredicate predicate = condition.getRootPredicate();

            if (predicate != null && !"full".equalsIgnoreCase(sTable.getExtractionType())) {
                if (cnt != 0) {
                    sourceCondition.append(" AND ");
                }
                sourceCondition.append(this.genFactory.generate(predicate, context));
                cnt++;
            }
        }

        return sourceCondition.toString();
    }

    protected String getWhereCondition(TargetTable targetTable, StatementContext context) throws BaseException {
        List sTables = targetTable.getSourceTableList();
        String conditionText = getSourceWhereCondition(sTables, context);

        SQLCondition filterCondition = targetTable.getFilterCondition();
        SQLPredicate filterPredicate = null;
        if (filterCondition != null) {
            filterPredicate = filterCondition.getRootPredicate();
        }

        if (filterPredicate != null) {
            String filterConditionText = this.genFactory.generate(filterPredicate, context);
            if (filterConditionText != null && !filterConditionText.equals("")) {
                if (conditionText != null && !conditionText.equals("")) {
                    conditionText += " AND " + filterConditionText;
                } else {
                    conditionText = filterConditionText;
                }
            }
        }
        return conditionText;
    }

    protected String getHavingCondition(SQLGroupBy groupBy, StatementContext context) throws BaseException {
        String havingCondition = null;
        SQLCondition condition = groupBy.getHavingCondition();
        SQLPredicate predicate = condition.getRootPredicate();
        if (predicate != null) {
            havingCondition = this.genFactory.generate(predicate, context);
        }
        return havingCondition;
    }

    protected String getTargetJoinClause(TargetTable targetTable, int joinType, StatementContext context) throws BaseException {
        String joinResult = "";
        SQLCondition condition = targetTable.getJoinCondition();
        SQLPredicate predicate = null;
        SQLJoinOperator join = null;
        if (condition != null) {
            predicate = condition.getRootPredicate();
        }

        if (predicate == null) {
            throw new BaseException("Missing merge condition.");
        }

        if (targetTable.getJoinView() != null) {
            join = targetTable.getJoinView().getRootJoin();
        }

        try {
            SQLJoinOperator operator = SQLModelObjectFactory.getInstance().createSQLJoinOperator();
            // This new join opearator parent object needs to be set.
            operator.setParentObject(SQLObjectUtil.getAncestralSQLDefinition(targetTable));
            operator.setJoinType(joinType);
            SQLCondition clonedCond = (SQLCondition) condition.cloneSQLObject();

            if (joinType == SQLConstants.RIGHT_OUTER_JOIN) {
                clonedCond.replaceTargetColumnIsNullPredicate();
            }

            operator.setJoinCondition(clonedCond);

            if (join != null) {
                // add target table condition to the join object
                SQLJoinOperator joinClone = (SQLJoinOperator) join.cloneSQLObject();
                operator.addInput(SQLJoinOperator.LEFT, targetTable);
                operator.addInput(SQLJoinOperator.RIGHT, joinClone);

                joinResult += this.genFactory.generate(operator, context);
                operator.removeInputByArgName(SQLJoinOperator.RIGHT, joinClone);
            } else {
                List sTables = targetTable.getSourceTableList();
                if (sTables.size() != 1) {
                    throw new BaseException("Cannot create target join statement:  Expected 1 source table, found " + sTables.size());
                }

                operator.addInput(SQLJoinOperator.LEFT, targetTable);
                operator.addInput(SQLJoinOperator.RIGHT, (SQLObject) sTables.get(0));
                joinResult += this.genFactory.generate(operator, context);
            }
        } catch (CloneNotSupportedException ex) {
            throw new BaseException(ex);
        }

        return joinResult;
    }

    protected String getTargetWhereCondition(TargetTable targetTable, StatementContext context) throws BaseException {
        SQLCondition joinCondition = targetTable.getJoinCondition();
        String joinConditionText = "";
        if (joinCondition != null) {
            SQLPredicate joinPredicate = joinCondition.getRootPredicate();

            if (joinPredicate != null) {
                joinConditionText = this.genFactory.generate(joinPredicate, context);
                //    throw new BaseException("Missing merge condition.");
            }
        }

        SQLCondition filterCondition = targetTable.getFilterCondition();
        if (filterCondition != null) {
            SQLPredicate filterPredicate = filterCondition.getRootPredicate();

            if (filterPredicate != null) {
                String filterConditionText = this.genFactory.generate(filterPredicate, context);
                if (filterConditionText != null && !filterConditionText.equals("")) {
                    joinConditionText += " AND " + filterConditionText;
                }
            }
        }
        return joinConditionText;
    }

    /**
     * Gets table name associated with the given SQLDBTable, without qualifying
     * delimiters.
     *
     * @param dbTable SQLDBTable whose name is to be returned
     * @param context StatementContext
     * @return
     */
    protected String getUnqualifiedTableName(SQLDBTable dbTable, StatementContext context) {
        String tableName = "";

        if (dbTable.getObjectType() == SQLConstants.SOURCE_TABLE && context.isUsingTempTableName((SourceTable) dbTable)) {
            tableName = ((SourceTable) dbTable).getTemporaryTableName();
        } else {
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
        }

        return tableName;
    }

    protected void populateContextForInsertSelect(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        // SET CONTEXT TO USE TARGET TABLE ALIAS NAME IN FROM CLAUSE
        // AND ALSO ALIAS THIS ALIAS WILL BE PREPENDED IN COLUMN NAME
        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));
        String targetTableName = this.genFactory.generate(targetTable, localContext);
        vContext.put("targetTable", targetTableName);

        //Use the Table Qualification flag to suppress column prefix
        localContext.setSuppressingTablePrefixForTargetColumn(true);

        List targetColumnIdentifiers = this.createTargetIdentifierList(targetTable, localContext);
        vContext.put("targetColumnIdentifiers", targetColumnIdentifiers);

        //START SELECT
        List sourceColumnIdentifiers = this.createSourceIdentifierList(targetTable, localContext);
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);
        context.putClientProperty("sourceColumnIdentifiers", sourceColumnIdentifiers);
        vContext.put("aliasColumns", Boolean.FALSE);
        vContext.put("distinct", areDistinctRowsRequired(targetTable));

        vContext.put("selectAliasName", "");
        vContext.put("nestedIndent", "");
        //END SELECT
        //START WHERE
        vContext.put("condition", "");
        vContext.put("notInSql", "");
        vContext.put("integritySql", "");

        // NOTE: to build the where clauses/join conditions, allow target columns to use
        // table aliases
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        localContext.setSuppressingTablePrefixForTargetColumn(false);

        // TODO: If filter has been applied already, don't apply again.
        String condition = getWhereCondition(targetTable, localContext);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }

        String integritySql = appendSQLForIntegrityCheck(targetTable, localContext);
        if (integritySql != null && !integritySql.trim().equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("integritySql", integritySql);
            vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.RIGHT_OUTER_JOIN, localContext));
        } else {
            vContext.put("fromContent", getFromStatementContent(targetTable, localContext));
        }
        //END WHERE
        populateContextForGroupByAndHaving(targetTable, localContext, vContext);
    }

    @SuppressWarnings(value = "unchecked")
    protected void populateContextForUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        // Inherit settings from incoming context to allow for localized customization
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);
        //localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));

        // SELECT START
        final boolean excludeJoinKeyColumns = false;
        List rMappings = createResolvedMappings(targetTable, excludeJoinKeyColumns, localContext);
        List<ColumnIdentifier> selectIdentifiers = new ArrayList<ColumnIdentifier>();

        Iterator it = rMappings.iterator();
        while (it.hasNext()) {
            ResolvedMapping rm = (ResolvedMapping) it.next();
            selectIdentifiers.add(rm.getSource());
            selectIdentifiers.add(rm.getTarget());
        }

        List whereConditionList = (List) localContext.getClientProperty("whereList");

        if (whereConditionList != null) {
            // Will reset the conditions. Avoid duplication and prefer conditions from
            // DB specific JOIN generator.
            whereConditionList.clear();
        }

        vContext.put("sourceColumnIdentifiers", selectIdentifiers);
        vContext.put("aliasColumns", Boolean.TRUE);
        vContext.put("distinct", areDistinctRowsRequired(targetTable));
        vContext.put("selectAliasName", "");
        vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.INNER_JOIN, localContext));
        vContext.put("nestedIndent", "    ");

        vContext.put("useUpdateWhere", Boolean.FALSE);

        String condition = getWhereCondition(targetTable, localContext);
        if ((condition != null && !condition.equals("")) || (whereConditionList != null && !whereConditionList.isEmpty())) {

            if (whereConditionList == null || whereConditionList.isEmpty()) {
                whereConditionList = new ArrayList();
            }

            if ((condition != null) && (!condition.equals(""))) {
                whereConditionList.add(condition);
            }

            vContext.put("useUpdateWhere", Boolean.TRUE);
            vContext.put("conditions", whereConditionList);
        }
        //SELECT END
        //SET START
        vContext.put("mappings", rMappings);
        //SET END
    }

    /**
     * Creates appropriate table name from the given TargetTable and StatementContext
     * state to use in updating statistics metadata tables.
     *
     * @param table TargetTable whose reference name is to be created
     * @param context StatementContext to use in determing appropriate reference name
     * @return appropriate table name to use as a reference in statistics metadata tables.
     */
    protected String getTableNameForStatisticsMetadata(TargetTable table, StatementContext context) {
        String targetTableName = context.isUsingUniqueTableName() ? table.getUniqueTableName() : table.getName();
        return targetTableName.toUpperCase();
    }

    private void populateContextForPrepStmtInsert(SQLDBTable table, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        // Use the Table Qualification flag to suppress column prefix
        switch (table.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
                localContext.setSuppressingTablePrefixForSourceColumn(true);
                break;
            case SQLConstants.TARGET_TABLE:
                localContext.setSuppressingTablePrefixForTargetColumn(true);
                break;
        }

        String targetTableName = this.genFactory.generate(table, localContext);
        vContext.put("targetTable", targetTableName);

        List<ColumnIdentifier> targetColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        List<ColumnIdentifier> prepStmtPlaceholders = new ArrayList<ColumnIdentifier>();
        List<String> types = new ArrayList<String>();

        List columns = (table instanceof TargetTable) ? ((TargetTable) table).getMappedColumns() : table.getColumnList();

        Iterator it = columns.iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            if (column.isVisible()) {
                String sql = this.genFactory.generate(column, localContext);
                ColumnIdentifier cId = new ColumnIdentifier(null, sql);
                targetColumnIdentifiers.add(cId);
                prepStmtPlaceholders.add(new ColumnIdentifier(null, "?"));
                types.add(String.valueOf(column.getJdbcType()));
            }
        }

        // Put List of JDBC types for each column in given statement context.
        context.putClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST, types);

        vContext.put("targetColumnIdentifiers", targetColumnIdentifiers);

        // VALUES - prepared statement placeholders, i.e., '?'.
        vContext.put("valueIdentifiers", prepStmtPlaceholders);
    }

    private void populateContextForStaticInsert(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }

        String targetTableName = this.genFactory.generate(targetTable, context);
        vContext.put("targetTable", targetTableName);

        List targetColumnIdentifiers = this.createTargetIdentifierList(targetTable, context);
        vContext.put("targetColumnIdentifiers", targetColumnIdentifiers);

        // VALUES
        List valueIdentifiers = this.createSourceIdentifierList(targetTable, context);
        vContext.put("valueIdentifiers", valueIdentifiers);
    }

    /**
     * Populates given VelocityContext with sufficient information to generate a static
     * update statement for the given TargetTable, using hints from the given
     * StatementContext.
     *
     * @param targetTable
     * @param context
     * @param context2
     */
    protected void populateContextForStaticUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }
        localContext.setSuppressingTablePrefixForTargetColumn(true);

        String targetTableName = this.genFactory.generate(targetTable, localContext);
        vContext.put("targetTable", targetTableName);

        List setMappings = this.createStaticUpdateSetList(targetTable, localContext);
        vContext.put("setMappings", setMappings);

        String condition = getTargetWhereCondition(targetTable, localContext);
        if (condition != null && !condition.trim().equals("")) {
            vContext.put("condition", condition);
            vContext.put("nestedIndent", "");
            vContext.put("whereClause", TemplateBuilder.generateSql(this.db.getTemplateFileName("where"), vContext)); // NOI18N
            vContext.put("useWhere", Boolean.TRUE);
        }
    }

    /**
     * Creates List of UpdateSetMapping instances, each of which represents a mapping of a
     * target column to an expression (SQL function, or pseudo-column.)
     *
     * @param targetTable
     * @param context
     * @return
     */
    private List<UpdateSetMapping> createStaticUpdateSetList(TargetTable targetTable, StatementContext context) throws BaseException {
        List mappedList = targetTable.getMappedColumns();
        if (mappedList.size() == 0) {
            throw new BaseException("Must have at least one column mapped to a literal, source column, or operator.");
        }

        List<UpdateSetMapping> mappings = new ArrayList<UpdateSetMapping>(mappedList.size());
        int aliasCount = 1;

        Iterator it = mappedList.iterator();
        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            String sql = this.genFactory.generate(column, context);
            ColumnIdentifier cId = new ColumnIdentifier(TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sql);
            String expression = this.genFactory.generate(column.getValue(), context);

            mappings.add(new UpdateSetMapping(cId, expression));
            aliasCount++;
        }

        return mappings;
    }

    /**
     * Apart from returning Source columns in the condition but not in the list., will also set context
     * with JDBC type for these missing columns.
     * @param condition
     * @param origList
     * @param context
     * @return
     */
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

    protected List<ColumnIdentifier> createColumnIdentifiersFromSourceColumns(List columnTobeAliased, StatementContext context, int startIndex) throws BaseException {
        return createColumnIdentifiersFromSourceColumns(columnTobeAliased, context, startIndex, SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX);
    }

    protected List<ColumnIdentifier> createColumnIdentifiersFromSourceColumns(List columnTobeAliased, StatementContext context, int startIndex, String aliasPrefix) throws BaseException {
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

    protected List<ColumnIdentifier> getColIdsFromRM(List<ResolvedMapping> rmList) throws BaseException {
        Iterator it1 = rmList.iterator();
        List<ColumnIdentifier> colIds = new ArrayList<ColumnIdentifier>();
        while (it1.hasNext()) {
            ColumnIdentifier cId = ((ResolvedMapping) it1.next()).getSource();
            colIds.add(cId);
        }
        return colIds;
    }

    protected String replaceColumnNamesWithAliases(List colIdentifier, String sql) throws BaseException {
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

/**
     * Helper class to associate a ColumnIdentifier with a String expression, for use in
     * generating column-value mappings for the set clause of an update statement.
     *
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class UpdateSetMapping {

        private ColumnIdentifier cId;
        private String exp;

        public UpdateSetMapping(ColumnIdentifier columnId, String expression) {
            cId = columnId;
            exp = expression;
        }

        public String getAlias() {
            return cId.getAliasName();
        }

        public String getColumnName() {
            return cId.getSql();
        }

        public String getExpression() {
            return exp;
        }
    }
}
