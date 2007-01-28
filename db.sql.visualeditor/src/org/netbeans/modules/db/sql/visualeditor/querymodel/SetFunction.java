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

import java.util.Collection;

/**
 * Represents a SQL Set function (AVG, COUNT, MAX, MIN, SUM)
 * Example Form: SUM(Orders.Quantity), MAX(Employee.Salary), COUNT(Employee.Name)
 */

public class SetFunction extends ColumnItem implements UnaryExpression {

    public static final int NONE = 0;
    public static final int AVG = 1;
    public static final int COUNT = 2;
    public static final int MAX = 3;
    public static final int MIN = 4;
    public static final int SUM = 5;

    private int _type;
    private ColumnNode _argument;
    private Identifier _alias;

    private SetFunction() { }

    public SetFunction(int type, ColumnNode argument, Identifier alias) {
        _type = type;
        _argument = argument;
        _alias = alias;
    }

    Column getReferencedColumn() {
        return _argument;
    }

    public void getReferencedColumns(Collection columns) {
        columns.add(_argument);
    }
    public void getQueryItems(Collection items) {
        items.add(_argument);
    }

    public String genText() {
        String funcType = null;
        switch (_type) {
            case AVG:
                funcType = "AVG(";
                break;
            case COUNT:
                funcType = "COUNT(";
                break;
            case MAX:
                funcType = "MAX(";
                break;
            case MIN:
                funcType = "MIN(";
                break;
            case SUM:
                funcType = "SUM(";
                break;
            default:
                break;
        }
        funcType += _argument.genText();
        funcType += ")";
        if (_alias != null) {
            funcType += " AS " + _alias.genText();
        }
        return funcType;
    }

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    /**
     * Rename the table part of the column spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _argument.renameTableSpec(oldTableSpec, corrName);
    }

    public boolean isParameterized() {
        return false;
    }

    public Expression getOperand() {
        return _argument;
    }

}
