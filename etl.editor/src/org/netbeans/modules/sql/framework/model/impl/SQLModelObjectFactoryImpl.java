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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.List;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLFrameworkParentObject;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;

/**
 * Object factory
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */

public class SQLModelObjectFactoryImpl extends SQLModelObjectFactory {
    public SQLModelObjectFactoryImpl() {
    }

    public ColumnRef createColumnRef() {
        return new ColumnRefImpl();
    }

    public ColumnRef createColumnRef(SQLDBColumn column) {
        return new ColumnRefImpl(column);
    }

    public SQLDBConnectionDefinition createDBConnectionDefinition() {
        return new SQLDBConnectionDefinitionImpl();
    }
    
    public SQLDBConnectionDefinition createDBConnectionDefinition(DBConnectionDefinition conf) {
        return new SQLDBConnectionDefinitionImpl(conf);
    }

    public SQLDBConnectionDefinition createDBConnectionDefinition(Element element) throws BaseException {
        return new SQLDBConnectionDefinitionImpl(element);
    }

    public SQLDBConnectionDefinition createDBConnectionDefinition( String name,
                                                                   String dbType,
                                                                   String driverClass, 
                                                                   String url,
                                                                   String user,
                                                                   String password,
                                                                   String description) {
        
          return new SQLDBConnectionDefinitionImpl(name, 
                                                   dbType, 
                                                   driverClass, 
                                                   url, 
                                                   user, 
                                                   password, 
                                                   description);
    }

    public SQLDBModel createDBModel(int type) {
        return new SQLDBModelImpl(type);
    }

    public SQLDBModel createDBModel(DatabaseModel src, int modelType, SQLFrameworkParentObject sqlParent) {
        return new SQLDBModelImpl(src, modelType, sqlParent);
    }

    public SQLGroupBy createGroupBy() {
        return new SQLGroupByImpl();
    }

    public SQLGroupBy createGroupBy(List columns, Object parent) {
        return new SQLGroupByImpl(columns, parent);
    }

    public SQLFilter createLeftUnarySQLFilter() {
        return new SQLFilterImpl.LeftUnary();
    }

    public SQLFilter createRightUnarySQLFilter() {
        return new SQLFilterImpl.RightUnary();
    }

    public RuntimeDatabaseModel createRuntimeDatabaseModel() {
        return new RuntimeDatabaseModelImpl();
    }

    public RuntimeInput createRuntimeInput() {
        return new RuntimeInputImpl();
    }

    public RuntimeInput createRuntimeInput(RuntimeInput rinput) {
        return new RuntimeInputImpl(rinput);
    }

    public RuntimeOutput createRuntimeOutput() {
        return new RuntimeOutputImpl();
    }

    public RuntimeOutput createRuntimeOutput(RuntimeOutput routput) {
        return new RuntimeOutputImpl(routput);
    }

    public SourceColumn createSourceColumn(DBColumn src) {
        return new SourceColumnImpl(src);
    }

    public SourceColumn createSourceColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        return new SourceColumnImpl(colName, sqlJdbcType,colScale, colPrecision, isNullable);
    }

    public SourceColumn createSourceColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey, boolean isIndexed, boolean isNullable) {
        return new SourceColumnImpl(colName, sqlJdbcType,colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
    }

    public SourceTable createSourceTable(DBTable source) {
        return new SourceTableImpl(source);
    }

    public SourceTable createSourceTable(String name, String schema, String catalog) {
        return new SourceTableImpl(name, schema, catalog);
    }

    public SQLCondition createSQLCondition(String conditionName) {
        return new SQLConditionImpl(conditionName);
    }

    public SQLDefinition createSQLDefinition() {
        return new SQLDefinitionImpl();
    }

    public SQLDefinition createSQLDefinition(Element element) throws BaseException {
        return new SQLDefinitionImpl(element);
    }

    public SQLDefinition createSQLDefinition(Element element, SQLFrameworkParentObject parent) throws BaseException {
        return new SQLDefinitionImpl(element, parent);
    }

    public SQLDefinition createSQLDefinition(String defName) {
        return new SQLDefinitionImpl(defName);
    }

    public SQLFilter createSQLFilter() {
        return new SQLFilterImpl();
    }

    public SQLFilter createSQLFilter(Element element) throws BaseException {
        return new SQLFilterImpl(element);
    }

    public SQLJoinOperator createSQLJoinOperator() {
        return new SQLJoinOperatorImpl();
    }

    public SQLJoinTable createSQLJoinTable(SourceTable source) {
        return new SQLJoinTableImpl(source);
    }

    public SQLJoinView createSQLJoinView() {
        return new SQLJoinViewImpl();
    }
    
    public SQLLiteral createSQLLiteral(String name, String value, int jdbcType) throws BaseException {
        return new SQLLiteralImpl(name, value, jdbcType);
    }

    public SQLPredicate createSQLPredicate() {
        return new SQLPredicateImpl();
    }

    public TargetColumn createTargetColumn(DBColumn src) {
        return new TargetColumnImpl(src);
    }

    public TargetColumn createTargetColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        return new TargetColumnImpl(colName, sqlJdbcType, colScale, colPrecision, isNullable);
    }

    public TargetColumn createTargetColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey,
            boolean isForeignKey, boolean isIndexed, boolean isNullable) {
        return new TargetColumnImpl(colName, sqlJdbcType, colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
    }

    public TargetTable createTargetTable(DBTable source) {
        return new TargetTableImpl(source);
    }

    public TargetTable createTargetTable(String name, String schema, String catalog) {
        return new TargetTableImpl(name, schema, catalog);
    }

    public ValidationInfo createValidationInfo(Object obj, String description, int vType) {
        return new ValidationInfoImpl(obj, description, vType);
    }

    public VisibleSQLLiteral createVisibleSQLLiteral(String name, String value, int jdbcType) throws BaseException {
        return new VisibleSQLLiteralImpl(name, value, jdbcType);
    }

    public VisibleSQLPredicate createVisibleSQLPredicate() {
        return new VisibleSQLPredicateImpl();
    }
}
