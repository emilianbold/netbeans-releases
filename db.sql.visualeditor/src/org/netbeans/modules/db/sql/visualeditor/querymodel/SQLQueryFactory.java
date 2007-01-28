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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import org.netbeans.modules.db.sql.visualeditor.parser.SQLParser;
import org.netbeans.modules.db.sql.visualeditor.parser.ParseException;
import java.util.ArrayList;

public class SQLQueryFactory {

    public static Query parse(String query) throws ParseException {
        SQLParser parser = new SQLParser(new java.io.StringReader(query));
        return (Query)parser.SQLQuery();
    }

    public static Where createWhere(Expression expr) {
        return new WhereNode(expr);
    }

    public static Predicate createPredicate(Value val1, Value val2, String op) {
        return new Predicate(val1, val2, op);
    }

    public static Predicate createPredicate(Value val1, Object literal, String op) {
        Literal val2 = new Literal(literal);
        return new Predicate(val1, val2, op);
    }

    public static Predicate createPredicate(String[] rel) {
        return new Predicate(rel);
    }

    public static GroupBy createGroupBy(ArrayList columnList) {
        return new GroupByNode(columnList);
    }

    public static OrderBy createOrderBy() {
        return new OrderByNode();
    }

    public static Column createColumn(String tableSpec, String columnName) {
        return new ColumnNode(tableSpec, columnName);
    }

    public static Literal createLiteral(Object value) {
        return new Literal(value);
    }

    public static Table createTable(String tableName, String corrName, String schemaName) {
        return new TableNode(tableName, corrName, schemaName);
    }

    public static JoinTable createJoinTable(Table table) {
        return new JoinTableNode((TableNode)table);
    }

    public static And createAnd(Expression expr1, Expression expr2) {
        ArrayList items = new ArrayList();
        items.add(expr1);
        items.add(expr2);
        return new AndNode(items);
    }

    public static Or createOr(Expression expr1, Expression expr2) {
        ArrayList items = new ArrayList();
        items.add(expr1);
        items.add(expr2);
        return new OrNode(items);
    }
}
