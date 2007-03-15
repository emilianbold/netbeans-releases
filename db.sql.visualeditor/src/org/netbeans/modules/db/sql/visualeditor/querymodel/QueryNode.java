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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class QueryNode implements Query {

    // Fields

    SelectNode  _select;
    FromNode    _from;
    WhereNode   _where;
    GroupByNode _groupBy;
    HavingNode  _having;
    OrderByNode _orderBy;


    // Constructors

    public QueryNode() {
    }

    public QueryNode(SelectNode select, FromNode from, WhereNode where,
                 GroupByNode groupBy, HavingNode having, OrderByNode orderBy) {
        _select = select;
        _from = from;
        _where = where;
        _groupBy = groupBy;
        _having = having;
        _orderBy = orderBy;
    }

    public QueryNode(SelectNode select, FromNode from) {
        this(select, from, null, null, null, null);
    }


    // Methods

    // Generate the SQL string corresponding to this model

    public String genText() {
        String res = _select.genText() + " " + _from.genText();    // NOI18N

        if (_where!=null)
            res += _where.genText();

        if (_groupBy!=null)
            res += _groupBy.genText();

        if (_having!=null)
            res += _having.genText();

        if (_orderBy!=null)
            res += _orderBy.genText();

        return res;
    }


    // Dump out the model, for debugging purposes

    public String toString() {
        return (_select.toString() +
                _from.toString() +
                _where.toString() );
    }


    // Accessors/Mutators

    public Select getSelect() {
        return _select;
    }

    public void setSelect(Select select) {
        _select = (SelectNode)select;
    }

    public From getFrom() {
        return _from;
    }

    public void setFrom(From from) {
        _from = (FromNode)from;
    }

    public Where getWhere() {
        return _where;
    }

    public void setWhere(Where where) {
        _where = (WhereNode)where;
    }

    public GroupBy getGroupBy() {
        return _groupBy;
    }

    public void setGroupBy(GroupBy groupBy) {
        _groupBy = (GroupByNode)groupBy;
    }

    public OrderBy getOrderBy() {
        return _orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        _orderBy = (OrderByNode)orderBy;
    }

    public Having getHaving() {
        return _having;
    }

    public void setHaving(Having having) {
        _having = (HavingNode)having;
    }

    public void removeTable (String tableSpec) {
        // Find the FROM clause for this tableName, and remove it
        _from.removeTable(tableSpec);

        // ToDo: Remove any other joins that mention this table?

        // Find any SELECT targets for this tableName, and remove them
        _select.removeTable(tableSpec);

        // Find any WHERE clauses that mention this table, and remove them
        if (_where!=null) {
            _where.removeTable(tableSpec);
            if (_where.getExpression() == null)
                _where = null;
        }

        // Find any GROUPBY clauses that mention this table, and remove them
        if (_groupBy!=null)
        {
            _groupBy.removeTable(tableSpec);
            if (_from._tableList.size() == 0)
                _groupBy = null;
        }
        removeSortSpecification(tableSpec);
    }

    public void replaceStar(ColumnProvider tableReader) {
        if (_select.hasAsteriskQualifier()) {  // NOI18N

            // Hack - if there's a star, just replace the whole list
            ArrayList columns = new ArrayList();

            // Get the list of table objects from FROM
            ArrayList tables = _from.getTables();

            // Iterate through it
            for (int i=0; i<tables.size(); i++) {
                TableNode tbl = (TableNode) tables.get(i);
                String fullTableName = tbl.getFullTableName();
		List columnNames = new ArrayList();
		tableReader.getColumnNames(fullTableName, columnNames);
                String corrName=tbl.getCorrName();
                String tableName=tbl.getTableName();
                String schemaName=tbl.getSchemaName();
                for (int j=0; j<columnNames.size(); j++) {
                    String columnName = (String) columnNames.get(j);
                    columns.add(new ColumnNode(tableName, columnName, corrName, schemaName));
                }
            }
            _select.setColumnList(columns);
        }
    }

    public void addColumn(String tableSpec, String columnName) {
        // Get the corresponding Table object from the FROM, to resolve issues
        // of corrName/tableName
        Table table = _from.findTable(tableSpec);
        ColumnNode col = new ColumnNode(table, columnName);
        
        // Note that they will share the column.  Copy if this causes problem
        _select.addColumn(col);
        if (_groupBy != null)
            _groupBy.addColumn(col);
    }

    public void removeColumn(String tableSpec, String columnName) {
        _select.removeColumn(tableSpec, columnName);
        if (_groupBy != null)
            _groupBy.removeColumn(tableSpec, columnName);
        // Remove the sort spec for this column if there was one
        removeSortSpecification(tableSpec, columnName);
    }
    
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _from.renameTableSpec(oldTableSpec, corrName);
        _select.renameTableSpec(oldTableSpec, corrName);
        if (_where!=null)
            _where.renameTableSpec(oldTableSpec, corrName);
        if (_groupBy!=null)
            _groupBy.renameTableSpec(oldTableSpec, corrName);
        if (_having!=null)
            _having.renameTableSpec(oldTableSpec, corrName);
        if (_orderBy!=null)
            _orderBy.renameTableSpec(oldTableSpec, corrName);
    }

    public void getReferencedColumns(Collection columns) {
        _from.getReferencedColumns(columns);
        _select.getReferencedColumns(columns);
        if (_where!=null)
            _where.getReferencedColumns(columns);
        if (_groupBy!=null)
            _groupBy.getReferencedColumns(columns);
        if (_having!=null)
            _having.getReferencedColumns(columns);
        if (_orderBy!=null)
            _orderBy.getReferencedColumns(columns);
    }
    
    public void getQueryItems(Collection items) {
        items.add(_from);
        items.add(_select);
        if (_where!=null)
            items.add(_where);
        if (_groupBy!=null)
            items.add(_groupBy);
        if (_having!=null)
            items.add(_having);
        if (_orderBy!=null)
            items.add(_orderBy);
    }
    //
    // private implementation
    //
    
    private void removeSortSpecification(String tableSpec) {
        if (_orderBy!=null)
            _orderBy.removeSortSpecification(tableSpec);
    }

    private void removeSortSpecification(String tableSpec, String columnName) {
        if (_orderBy!=null)
            _orderBy.removeSortSpecification(tableSpec, columnName);
    }

}


