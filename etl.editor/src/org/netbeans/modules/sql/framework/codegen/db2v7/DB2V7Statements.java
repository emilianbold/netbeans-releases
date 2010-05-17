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
package org.netbeans.modules.sql.framework.codegen.db2v7;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.ResolvedMapping;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.base.BaseStatements;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.SQLPart;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DB2V7Statements extends BaseStatements {

    public DB2V7Statements(AbstractDB database) {
        super(database);
    }

    @Override
    @SuppressWarnings(value = "fallthrough")
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
            if (!rm.isKeyColumn()) {
                selectIdentifiers.add(rm.getSource());
            }
        }

        String targetTableName = this.genFactory.generate(targetTable, localContext);
        vContext.put("targetTable", targetTableName);

        vContext.put("sourceColumnIdentifiers", selectIdentifiers);
        vContext.put("aliasColumns", Boolean.TRUE);
        //vContext.put("distinct", Boolean.FALSE);
        vContext.put("distinct", areDistinctRowsRequired(targetTable));

        vContext.put("selectAliasName", "");

        List sourceTables = targetTable.getSourceTableList();
        switch (sourceTables.size()) {
            case 0:
                break;
            case 1:
                vContext.put("fromContent", genFactory.generate((SQLObject) sourceTables.get(0), localContext));
            default:
                vContext.put("fromContent", getFromStatementContent(targetTable, localContext));
        }

        vContext.put("nestedIndent", "    ");

        vContext.put("useUpdateWhere", Boolean.FALSE);

        String condition = getTargetWhereCondition(targetTable, localContext);
        if (condition != null && !condition.equals("")) {
            vContext.put("useUpdateWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }
        //SELECT END
        //SET START
        vContext.put("mappings", rMappings);
        //SET END
        // UPDATE statement WHERE clause.
        SQLCondition tgtWhere = targetTable.getJoinCondition();
        populateUpdateWhereVariables(tgtWhere, localContext, vContext);
    }

    private boolean hasOnlyColType(SQLObject sqlObj, int colType) {
        boolean ret = true;
        SQLConnectableObject exp = null;
        List list = null;
        if (sqlObj.getObjectType() == SQLConstants.COLUMN_REF) {
            sqlObj = ((ColumnRef) sqlObj).getColumn();
            if (sqlObj.getObjectType() != colType) {
                ret = false;
            }
        } else {
            if (sqlObj instanceof SQLConnectableObject) {
                exp = (SQLConnectableObject) sqlObj;
                if (colType == SQLConstants.SOURCE_COLUMN) {
                    list = exp.getTargetColumnsUsed();
                    if (list.size() > 0) {
                        ret = false;
                    } else {
                        list = exp.getSourceColumnsUsed();
                        if (list.size() <= 0) {
                            ret = false;
                        }
                    }
                } else {
                    list = exp.getSourceColumnsUsed();
                    if (list.size() > 0) {
                        ret = false;
                    } else {
                        list = exp.getTargetColumnsUsed();
                        if (list.size() <= 0) {
                            ret = false;
                        }
                    }
                }
            } else {
                ret = false;
            }
        }

        return ret;
    }

    @SuppressWarnings(value = "unchecked")
    private void findEqualsOperator(SQLPredicate predicate, StatementContext sc, List srcExpList, List tgtExpList) throws BaseException {
        SQLObject srcColExpression = null;
        SQLObject tgtColExpression = null;
        Map inputMap = null;
        boolean foundTarget = false;
        boolean foundSource = false;
        Iterator itr = null;
        String argName = null;
        SQLInputObject input = null;
        SQLObject sqlObj = null;

        if (predicate != null) {
            inputMap = predicate.getInputObjectMap();
            if ("=".equals(predicate.getOperatorType())) {
                itr = inputMap.keySet().iterator();
                while (itr.hasNext()) {
                    argName = (String) itr.next();
                    input = (SQLInputObject) inputMap.get(argName);
                    sqlObj = input.getSQLObject();
                    if (sqlObj != null) {
                        // Get the first Expression available.
                        if ((!foundSource) && (hasOnlyColType(sqlObj, SQLConstants.SOURCE_COLUMN))) {
                            foundSource = true;
                            srcColExpression = sqlObj;
                        }

                        if ((!foundTarget) && (hasOnlyColType(sqlObj, SQLConstants.TARGET_COLUMN))) {
                            foundTarget = true;
                            tgtColExpression = sqlObj;
                        }
                    }
                }

                if (foundSource && foundTarget) {
                    String srcColExp = this.genFactory.generate(srcColExpression, sc);
                    String tgtColExp = this.genFactory.generate(tgtColExpression, sc);
                    srcExpList.add(srcColExp);
                    tgtExpList.add(tgtColExp);
                }
            } else {
                itr = inputMap.keySet().iterator();
                while (itr.hasNext()) {
                    argName = (String) itr.next();
                    input = (SQLInputObject) inputMap.get(argName);
                    sqlObj = input.getSQLObject();
                    if (sqlObj != null) {
                        if (sqlObj instanceof SQLPredicate) {
                            findEqualsOperator((SQLPredicate) sqlObj, sc, srcExpList, tgtExpList);
                        }
                    }
                }
            }
        }
    }

    private void populateUpdateWhereVariables(SQLCondition tgtCondition, StatementContext sc, VelocityContext vc) throws BaseException {
        //  (fn1(tgtCol1) = func2(srcCOl)) AND (exp1 OR exp2)
        SQLPredicate rootPred = null;
        List srcExp = new ArrayList();
        List tgtExp = new ArrayList();
        if (tgtCondition != null) {
            rootPred = tgtCondition.getRootPredicate();
            findEqualsOperator(rootPred, sc, srcExp, tgtExp);
            if (tgtExp.size() > 0) {
                vc.put("updateOnlyMatchedRows", Boolean.TRUE);
                vc.put("tgtCondCols", tgtExp);
                vc.put("srcCondCols", srcExp);
            }
        }
    }

    @Override
    public List<ResolvedMapping> createResolvedMappings(TargetTable targetTable, boolean excludeKeyColumns, StatementContext context) throws BaseException {
        List<ResolvedMapping> mappings = new ArrayList<ResolvedMapping>();
        String targetJoin = getTargetJoinClause(targetTable, SQLConstants.INNER_JOIN, context);

        Iterator it = targetTable.getMappedColumns().iterator();
        int aliasCount = 1;

        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        // Use the Table Qualification flag to suppress column prefix
        localContext.setSuppressingTablePrefixForTargetColumn(true);

        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            SQLObject obj = column.getValue();
            if (obj != null) {
                String tSql = this.genFactory.generate(column, localContext);
                if (targetJoin.indexOf(tSql) != -1 && excludeKeyColumns) {
                    continue;
                }

                String sSql = this.genFactory.generate(column.getValue(), localContext);
                ColumnIdentifier sId = new ColumnIdentifier(SOURCE_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, sSql);
                ColumnIdentifier tId = new ColumnIdentifier(TARGET_COLUMN_IDENTIFIER_ALIAS_PREFIX + aliasCount, tSql);
                ResolvedMapping rm = new ResolvedMapping(sId, tId);
                rm.setKeyColumn(column.isPrimaryKey());
                mappings.add(rm);
                aliasCount++;
            }
        }

        return mappings;
    }

    private String normalizeSQLForExecution(String sql) {
        if (sql != null) {
            sql = sql.replaceAll("\r", " ");
            sql = sql.replaceAll("\n", " ");
        }
        return sql;
    }

    @Override
    public SQLPart normalizeSQLForExecution(SQLPart rawSQLPart) {
        if ((rawSQLPart != null) && (rawSQLPart.getSQL() != null)) {
            String sql = rawSQLPart.getSQL();
            sql = normalizeSQLForExecution(sql);
            rawSQLPart.setSQL(sql);
        }
        return rawSQLPart;
    }

    @Override
    protected SQLPart createSQLPart(String sqlString, String key) {
        // Normalization is only required when DB2 Connect drivers are used.
        // Since generator framework is currently specific to DB, we just
        // normalize always till make it more flexible.
        SQLPart sqlPart = new SQLPart(normalizeSQLForExecution(sqlString), key, "");
        return sqlPart;
    }
}