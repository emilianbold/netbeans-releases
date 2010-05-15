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
package org.netbeans.modules.sql.framework.codegen.postgreSQL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.ResolvedMapping;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.base.BaseStatements;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.SQLPart;
import com.sun.etl.jdbc.SQLUtils;
import com.sun.etl.utils.RuntimeAttribute;
import com.sun.etl.utils.StringUtil;
/**
 * For PostgreSQL Database code generations using from JDBC Code generation.
 * @author Radhika
 */
public class PostgreSQLStatements extends BaseStatements {
    private static final String RUNTIME_INPUTS_MAP = "runtimeInputsMap";
    private static final String DIRECTLY_MAPPED_SRC_COLS_EVAL = "directlyMappedSrcColsEval";
    private static final String MAPPINGS = "mappings";
    private static final String ADDITIONAL_SRC_COLS = "additionalSrcCols";

    public PostgreSQLStatements(AbstractDB database) {
        super(database);
    }

    protected void populateContextForUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        final boolean excludeJoinKeyColumns = false;
        // SELECT START
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        //Use the Table Qualification flag to suppress column prefix
        localContext.setSuppressingTablePrefixForTargetColumn(true);
        List rMappings = createResolvedMappingsForUpdate(targetTable, excludeJoinKeyColumns, localContext);
        localContext.setSuppressingTablePrefixForTargetColumn(false);

        String targetTableSql = this.genFactory.generate(targetTable, localContext);

        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);

        vContext.put("targetTable", targetTableSql);
        vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.INNER_JOIN, localContext));
        vContext.put("nestedIndent", "    ");

        vContext.put("useUpdateWhere", Boolean.FALSE);

        String condition = getWhereCondition(targetTable, localContext);

        context.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);
        context.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);

        String updateWhereClause = getWhereClauseForUpdate(targetTable, context);
        if (condition != null && !condition.equals("")) {
            condition += " AND " + updateWhereClause;
        } else {
            condition = updateWhereClause;
        }

        context.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.FALSE);
        vContext.put("tgtCondition", getWhereClauseForUpdate(targetTable, context));
        context.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);

        if (condition != null && !condition.equals("")) {
            vContext.put("useUpdateWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }
        // SELECT END

        vContext.put(PostgreSQLStatements.MAPPINGS, rMappings);

        // exception when
        localContext.putClientProperty("nestedIndent", "");
        localContext.putClientProperty("valueIdentifiers", vContext.get("sourceColumnIdentifiers"));
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

    private List evaluateSourceColumnList(List srcColListtargetTable, StatementContext context) throws BaseException {
        List srcColEvals = new ArrayList();
        Map srcExpToJdbcTypeMap = (Map) context.getClientProperty(BaseStatements.SRC_EXP_TO_JDBC_TYPE_MAP);
        if (srcExpToJdbcTypeMap == null){
            srcExpToJdbcTypeMap = new HashMap();
            context.putClientProperty(BaseStatements.SRC_EXP_TO_JDBC_TYPE_MAP, srcExpToJdbcTypeMap);
        }

        StatementContext localContext = new StatementContext();
        localContext.putAll(context);
        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        Iterator it = srcColListtargetTable.iterator();
        String sSql = null;
        SourceColumn column = null;

        while (it.hasNext()) {
            column = (SourceColumn) it.next();
            if (column != null) {
                sSql = this.genFactory.generate(column, localContext);
                srcColEvals.add(sSql);
                srcExpToJdbcTypeMap.put(sSql, "" + column.getJdbcType());
            }
        }

        return srcColEvals;
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
        	if (StringUtil.isNullString(uSchema)){
                if (!StringUtil.isNullString(table.getSchema())){
                    schemaName = table.getSchema().toUpperCase();
                }
        	}else{
        		schemaName = uSchema.toUpperCase();        		
        	}
        }
        vContext.put("schemaName", schemaName);

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("tableExists"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_CHECKTABLEEXISTS); // NOI18N;;
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

    /**
     * Returns list of integer or String. If element is integer it represent Datum posiotion in the result set else
     * it is RunTimeInput symbol name.
     *
     * @param sql
     * @param mappedList
     * @param additionalSelectCols
     * @param riMap
     * @return List of items to be populated into update statement.
     */
    private String mapDestinationCols(String sql, StatementContext context) {
        List symbolList = new ArrayList();
        List destinationsSource = new ArrayList();
        List newBindingVariables = new ArrayList();
        String symbol = null;
        int mappedCols = 0;
        RuntimeAttribute ra = null;
        List directlyMappedSrcColsEval = (List) context.getClientProperty(PostgreSQLStatements.DIRECTLY_MAPPED_SRC_COLS_EVAL) ;
        List additionalSelectColsEval = (List) context.getClientProperty(PostgreSQLStatements.ADDITIONAL_SRC_COLS) ;
        List jdbcTypeList = (List) context.getClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST) ;
        List mappings = (List) context.getClientProperty(MAPPINGS);
        Map riMap = (Map) context.getClientProperty(RUNTIME_INPUTS_MAP) ;
        Map symbol2JdbcTypeMap = (Map) context.getClientProperty(BaseStatements.SRC_EXP_TO_JDBC_TYPE_MAP);
        context.putClientProperty(SQLPart.ATTR_DESTS_SRC, destinationsSource);

        if (mappings != null){
            mappedCols = mappings.size();
        }

        if (directlyMappedSrcColsEval != null){
            symbolList.addAll(directlyMappedSrcColsEval);
        }

        if (additionalSelectColsEval != null){
            symbolList.addAll(additionalSelectColsEval);
        }

        for (int i= 1; i <= mappedCols; i++){
            destinationsSource.add(i -1 , "" + i);
        }

        // RuntimeAttributes needed in Where clause
        if (riMap != null){
            Set keys = riMap.keySet();
            Iterator itr = keys.iterator();
            while (itr.hasNext()){
                ra = (RuntimeAttribute) riMap.get(itr.next());
                symbolList.add("$" + ra.getAttributeName());
                symbol2JdbcTypeMap.put("$" + ra.getAttributeName(), "" + ra.getJdbcType());
            }
        }
        //return sourcSymbolOrder;

        // Get order of symbols in the statement and replace symbols with "?".
        sql = SQLUtils.createPreparedStatement(sql, symbolList, newBindingVariables);
        Iterator itr = newBindingVariables.iterator();

        while (itr.hasNext()){
            symbol = (String) itr.next();
            if ((symbol != null) && (symbol.startsWith("$"))){
                destinationsSource.add(symbol);
            } else {
                destinationsSource.add("" + (symbolList.indexOf(symbol) + 1) );
            }
            jdbcTypeList.add(symbol2JdbcTypeMap.get(symbol));
        }

        return sql;
    }

    // TODO Voilates Statements interface pattern, need to redesign the interfaces.
    public Map getCorrelatedUpdateStatement(TargetTable targetTable, final StatementContext sc) throws BaseException {
        StatementContext context = new StatementContext();
        context.putAll(sc);
        Map ret = null;
        Map runtimeInputsMap = (Map) context.getClientProperty(RUNTIME_INPUTS_MAP); // No I18N
        List directSourceColumns = new ArrayList();
        VelocityContext vContext = new VelocityContext();
        String templateName = "";
        String sqlSelect = null;
        String sqlUpdate = null;
        SQLPart select = null;
        SQLPart update = null;

        if (targetTable.getSourceTableList().size() != 0) {
            List srcColDirectlyMapped = getSourceColsDirectlyMapped(targetTable, context);
            List columnsTobeAliased = getConditionColumnsNotInList(targetTable.getJoinCondition(), srcColDirectlyMapped, context);
            directSourceColumns.addAll(srcColDirectlyMapped);
            directSourceColumns.addAll(columnsTobeAliased);
            List directlyMappedSrcColsEval = evaluateSourceColumnList(srcColDirectlyMapped, context);
            List additionalSrcColsEval = evaluateSourceColumnList(columnsTobeAliased, context);

            populateContextForUpdate(targetTable, context, vContext);
            vContext.put(PostgreSQLStatements.ADDITIONAL_SRC_COLS, additionalSrcColsEval);
            templateName = this.db.getTemplateFileName("correlatedSelect"); // NOI18N
            sqlSelect = TemplateBuilder.generateSql(templateName, vContext);
            templateName = this.db.getTemplateFileName("correlatedUpdate"); // NOI18N
            sqlUpdate = TemplateBuilder.generateSql(templateName, vContext);

            // Context already has JDCB type for all the selected columns
            context.putClientProperty(PostgreSQLStatements.MAPPINGS, vContext.get(PostgreSQLStatements.MAPPINGS));
            context.putClientProperty(PostgreSQLStatements.ADDITIONAL_SRC_COLS, additionalSrcColsEval);
            context.putClientProperty(PostgreSQLStatements.DIRECTLY_MAPPED_SRC_COLS_EVAL, directlyMappedSrcColsEval);
            context.putClientProperty(PostgreSQLStatements.RUNTIME_INPUTS_MAP, runtimeInputsMap);
            sqlUpdate = mapDestinationCols(sqlUpdate, context);

            select = createSQLPart(sqlSelect, SQLPart.STMT_CORRELATED_SELECT);
            update = createSQLPart(sqlUpdate, SQLPart.STMT_CORRELATED_UPDATE);
            update.setAttribute(SQLPart.ATTR_JDBC_TYPE_LIST, context.getClientProperty(SQLPart.ATTR_JDBC_TYPE_LIST));
            update.setAttribute(SQLPart.ATTR_DESTS_SRC, context.getClientProperty(SQLPart.ATTR_DESTS_SRC));

            ret = new HashMap();
            ret.put(SQLPart.STMT_CORRELATED_SELECT, select);
            ret.put(SQLPart.STMT_CORRELATED_UPDATE, update);
        } else {
            // We should not be generating correlated Update statement here...
            throw new BaseException("Illegal execution path.");
        }

        return ret;
    }

    public SQLPart getUpdateStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        String templateName = "";

        if (targetTable.getSourceTableList().size() != 0) {
            throw new IllegalStateException("Internal Error. For JDBC eWay DB single Update may not work. Use Corelated queries.") ;
        } else {
            populateContextForStaticUpdate(targetTable, context, vContext);
            templateName = this.db.getTemplateFileName("updateStatic"); // NOI18N
        }
        String result = TemplateBuilder.generateSql(templateName, vContext);

        return createSQLPart(result, SQLPart.STMT_UPDATE); // NOI18N
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
        localContext.setUseSourceColumnAliasName(true);
        vContext.put("nestedIndent", "");
        vContext.put("exceptionWhen", TemplateBuilder.generateSql(this.db.getTemplateFileName("exceptionWhen"), vContext));

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("merge"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_MERGE); // NOI18N
    }

}
