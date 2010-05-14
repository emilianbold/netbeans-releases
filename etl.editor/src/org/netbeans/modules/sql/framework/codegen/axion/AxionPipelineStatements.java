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
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ColumnIdentifier;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.SQLPart;

/**
 * @author Ritesh Adval
 */
public class AxionPipelineStatements extends AxionStatements {
    public AxionPipelineStatements(AbstractDB database) {
        super(database);
    }
    
    /**
     * Creates SQL statement to generate log details table for the given TargetTable.
     * 
     * @param table TargetTable whose log details table is to be created
     * @param useMemoryTable true if statement should use syntax for Axion memory table,
     *        false if a delimited flatfile table is required.
     * @return SQLPart containing SQL statement that generates the desired log details
     *         table
     * @exception if error occurs during statement generation
     */
    public SQLPart getCreateLogDetailsTableStatement(TargetTable table, boolean useMemoryTable) throws BaseException {
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = createLogDetailsVelocityContext(table);

        vContext.put("useMemoryTable", Boolean.valueOf(useMemoryTable));
        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createLogDetailsTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATELOGDETAILSTABLE);
    }

    public SQLPart getCreateRemoteLogDetailsTableStatement(TargetTable table, String linkName) throws BaseException {
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = createLogDetailsVelocityContext(table);
        vContext.put("linkName", linkName);
        vContext.put("vendor", "AXION");

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("createDesignTimeLogDetailsRemoteTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_CREATEREMOTELOGDETAILSTABLE);
    }

    public SQLPart getRemountRemoteLogDetailsStatement(TargetTable table) throws BaseException {
        StringBuilder sqlBuf = new StringBuilder(50);

        VelocityContext vContext = new VelocityContext();
        vContext.put("tableName", getDetailsTableName(table));
        vContext.put("isExternal", Boolean.TRUE);

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("remountTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_REMOUNTREMOTETABLE);
    }

    /**
     * @param table
     * @param logTableName
     * @return
     */
    public SQLPart getSelectRejectedRowsCountFromDetailsTableStatement(TargetTable table) throws BaseException {
        StringBuilder sqlBuf = new StringBuilder(100);
        VelocityContext vContext = new VelocityContext();

        String detailsTableName = getDetailsTableName(table);
        vContext.put("targetTable", detailsTableName);

        sqlBuf.append(TemplateBuilder.generateSql(this.db.getTemplateFileName("selectRejectedRowsCountFromDetailsTable"), vContext));

        return createSQLPart(sqlBuf.toString(), SQLPart.STMT_SELECTREJECTEDROWCTFROMDETAILS);
    }

    /**
     * @param context
     * @param context2
     */
    private VelocityContext createLogDetailsVelocityContext(TargetTable table) {
        StatementContext context = new StatementContext();
        VelocityContext vContext = new VelocityContext();

        context.setUsingFullyQualifiedTablePrefix(false);

        List cIdentifiers = new ArrayList();
        List nullableIdentifiers = new ArrayList();
        StringBuilder columnBuf = new StringBuilder(50);

        List columns = (ArrayList) table.getColumnList();
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

        String tableName = getDetailsTableName(table);

        vContext.put("recordDelimiter", "\\r\\n");
        vContext.put("fieldDelimiter", ",");
        vContext.put("textQualifier", "\"");
        vContext.put("isFirstLineHeader", "true");
        vContext.put("ifNotExists", Boolean.TRUE);

        vContext.put("tableName", tableName);
        vContext.put("fileName", tableName + ".bad");

        return vContext;
    }
}

