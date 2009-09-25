/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.codegen.oracle8;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.base.BaseStatements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class Oracle8Statements extends BaseStatements {
    public Oracle8Statements(AbstractDB database) {
        super(database);
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

        List whereList = (List) context.getClientProperty(StatementContext.WHERE_CONDITION_LIST);
        
        if (!"full".equalsIgnoreCase(sourceTable.getExtractionType())) { //NOI18N
            if (whereList == null) {
                whereList = new ArrayList(3);
                context.putClientProperty(StatementContext.WHERE_CONDITION_LIST, whereList);
            }

            List sourceList = new ArrayList(1);
            sourceList.add(sourceTable);
            String condition = getSourceWhereCondition(sourceList, context);
            if (condition != null && !condition.equals("")) {
            	whereList.add(condition);
                vContext.put("useWhere", Boolean.TRUE);
                vContext.put(StatementContext.WHERE_CONDITION_LIST, whereList);
            }
        }

        // Add limit constraint if a limit value is set.
        Object prop = context.getClientProperty("limit");
        if (prop != null) {
            if (whereList == null) {
                whereList = new ArrayList(3);
                context.putClientProperty(StatementContext.WHERE_CONDITION_LIST, whereList);
            }

            setSelectLimit(context, vContext);
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put(StatementContext.WHERE_CONDITION_LIST, whereList);
        }

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT); // NOI18N
    }

    public SQLPart getSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        if (context == null) {
            context = new StatementContext();
        }
        VelocityContext vContext = new VelocityContext();

        List sourceColumnIdentifiers = new ArrayList();
        Iterator it = targetTable.getColumnList().iterator();
        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            String sql = this.genFactory.generate(column, context);
            ColumnIdentifier cId = new ColumnIdentifier(column.getDisplayName(), sql);
            sourceColumnIdentifiers.add(cId);
        }
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.FALSE);
        vContext.put("distinct", super.areDistinctRowsRequired(targetTable));

        vContext.put("selectAliasName", "");
        vContext.put("fromContent", this.genFactory.generate(targetTable, context));
        vContext.put("useWhere", Boolean.FALSE);

        List whereList = (List) context.getClientProperty(StatementContext.WHERE_CONDITION_LIST);
        if (whereList == null) {
            whereList = new ArrayList(3);
            context.putClientProperty(StatementContext.WHERE_CONDITION_LIST, whereList);
        }
        setSelectLimit(context, vContext);

        String notInSql = (String) vContext.get("notInSql");
        if (!StringUtil.isNullString(notInSql)) {
            whereList.add(notInSql);
        }

        String integritySql = (String) vContext.get("integritySql");
        if (!StringUtil.isNullString(integritySql)) {
            whereList.add(integritySql);
        }

        if (whereList.size() != 0) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("isJoin", Boolean.TRUE);
            vContext.put("whereList", whereList);
        }

        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("select"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_SELECT); // NOI18N
    }

    protected void populateContextForInsertSelect(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }
        // TODO: consolidate condition in whereList at template level.
        // currently we are adding "condition" and "whereList" both
        // in context.
        List whereList = (List) context.getClientProperty(StatementContext.WHERE_CONDITION_LIST);
        if (whereList == null) {
            whereList = new ArrayList(3);
            localContext.putClientProperty(StatementContext.WHERE_CONDITION_LIST, whereList);
        }

        // SET CONTEXT TO USE TARGET TABLE ALIAS NAME IN FROM CLAUSE
        // AND ALSO ALIAS WILL BE PREPENDED IN COLUMN NAME
        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        String targetTableName = this.genFactory.generate(targetTable, localContext);
        vContext.put("targetTable", targetTableName);
        
        // Use the Table Qualification flag to suppress column prefix
        localContext.setSuppressingTablePrefixForTargetColumn(true);

        List targetColumnIdentifiers = this.createTargetIdentifierList(targetTable, localContext);
        vContext.put("targetColumnIdentifiers", targetColumnIdentifiers);

        //START SELECT
        List sourceColumnIdentifiers = this.createSourceIdentifierList(targetTable, localContext);
        vContext.put("sourceColumnIdentifiers", sourceColumnIdentifiers);

        vContext.put("aliasColumns", Boolean.FALSE);
        vContext.put("distinct", super.areDistinctRowsRequired(targetTable));

        vContext.put("selectAliasName", "");
        vContext.put("nestedIndent", "");
        // END SELECT

        // START WHERE
        vContext.put("condition", "");
        vContext.put("notInSql", "");
        vContext.put("integritySql", "");
        vContext.put("isJoin", Boolean.TRUE);

        // NOTE: to build the where clauses/join conditions, allow target columns to use
        // table aliases
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        localContext.setSuppressingTablePrefixForTargetColumn(false);

        // TODO: If filter has been applied already, don't apply again.
        String condition = getWhereCondition(targetTable, localContext);
        if (condition != null && !condition.equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("condition", condition);

            // Move various where clauses from independent VelocityContext mappings to
            // whereList.
            whereList.add(condition);
        }

        String integritySql = appendSQLForIntegrityCheck(targetTable, localContext);
        if (integritySql != null && !integritySql.trim().equals("")) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("integritySql", integritySql);
            vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.RIGHT_OUTER_JOIN, localContext));

            whereList.add(integritySql);
        } else {
            vContext.put("fromContent", getFromStatementContent(targetTable, localContext));
        }
        // END WHERE

        if (whereList.size() != 0) {
            vContext.put("useWhere", Boolean.TRUE);
            vContext.put("whereList", whereList);
        }
        
        SourceTable[] srcTables = (SourceTable[]) targetTable.
                getSourceTableList().toArray(new SourceTable[0]);
        for(SourceTable srcTable : srcTables) {
            populateContextForGroupByAndHaving(srcTable, localContext, vContext);
        }
        
        populateContextForGroupByAndHaving(targetTable, localContext, vContext);
        populateContextForGroupByAndHaving(targetTable.getJoinView(), localContext, vContext);        
    }
    
    protected void populateContextForUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }
        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        super.populateContextForUpdate(targetTable, localContext, vContext);
    }

    private void setSelectLimit(StatementContext context, VelocityContext vContext) {
        Object prop = context.getClientProperty("limit");

        List whereList = (List) context.getClientProperty(StatementContext.WHERE_CONDITION_LIST);
        if (prop != null && whereList != null) {
            vContext.put("limit", prop.toString());
            String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("limit"), vContext); // NOI18N

            // Add limit as another predicate in the where clause
            whereList.add(result);
        }
    }
}
