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

// A QUERY clause
// This is the main interface representing the query statement
public interface Query extends QueryItem {
    public Select getSelect();
    public void setSelect(Select select);
    public From getFrom();
    public void setFrom(From from);
    public Where getWhere();
    public void setWhere(Where where);
    public GroupBy getGroupBy();
    public void setGroupBy(GroupBy groupBy);
    public OrderBy getOrderBy();
    public void setOrderBy(OrderBy orderBy);
    public Having getHaving();
    public void setHaving(Having having);

    public void removeTable(String tableSpec);
    public void renameTableSpec(String oldTableSpec, String corrName);

    public void replaceStar(ColumnProvider tableReader);

    public void addColumn(String tableSpec, String columnName);
    public void removeColumn(String tableSpec, String columnName);
}
