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

/**
 * Represents a generalized table specification in a SQL FROM  clause
 * Example forms:
 *      employees e
 *      INNER JOIN employees e ON e.id = e.id
 */

import java.util.ArrayList;
import java.util.Collection;

public class JoinTableNode implements JoinTable {

    // Fields

    private String      _joinType;  // INNER, OUTER, CROSS, NATURAL, ,
    private TableNode   _table;
    private Expression   _condition; // simplified WHERE clause

    // Constructors

    public JoinTableNode () {
    }

    public JoinTableNode(TableNode table, String joinType, Expression condition) {
        _table = table;
        _joinType = joinType;
        _condition = condition;
    }

    public JoinTableNode(TableNode table) {
        this(table, null, null);
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText() {
        String res =
            (((_joinType==null)||(_joinType.equals("CROSS")))
             ? ", "
             : "\n          " +_joinType + " JOIN ")  // NOI18N
            + _table.genText(true);

        if (_condition != null) {
            res += " ON " + _condition.genText();  // NOI18N
        }

        return res;
    }


    // Special processing for the first table in the list
    // Omit the join specification
    public String genText(boolean first) {
        return (first ? _table.genText(true) : this.genText());
    }


    // Methods

    // Accessors/Mutators

    public Table getTable() {
        return _table;
    }

    public String getTableName() {
        return _table.getTableName();
    }

    public String getCorrName() {
        return _table.getCorrName();
    }

    public String getTableSpec() {
        return _table.getTableSpec();
    }

    public String getFullTableName() {
        return _table.getFullTableName();
    }

    public String getJoinType () {
        return _joinType;
    }

    public void setJoinType (String joinType) {
        _joinType = joinType;
    }

    public Expression getExpression() {
        return _condition;
    }

    public void setExpression(Expression condition) {
        _condition = condition;
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns (Collection columns) {
        if (_condition != null)
            _condition.getReferencedColumns(columns);
    }

    public void getQueryItems(Collection items) {
        if (_condition != null) {
            items.add(_table);
            items.add(_condition);
        }
    }

    void renameTableSpec(String oldTableSpec, String corrName) {
        ((TableNode)this.getTable()).renameTableSpec(oldTableSpec, corrName);

        if (_condition instanceof Predicate)
            ((Predicate)_condition).renameTableSpec(oldTableSpec, corrName);
    }

    void setTableSpec(String oldTableSpec, String newTableSpec)
    {
        ((TableNode)this.getTable()).setTableSpec(oldTableSpec, newTableSpec);
    }

    public void addJoinCondition(String[] rel) {

        // Convert relationship into join
        ColumnNode col1 = new ColumnNode(rel[0], rel[1]);
        ColumnNode col2 = new ColumnNode(rel[2], rel[3]);
        Predicate pred = new Predicate(col1, col2);

        // Update the JoinTable object with join information
        setJoinType("INNER");  // NOI18N
        setExpression(pred);
    }
}
