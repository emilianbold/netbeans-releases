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
 * Represents a generalized table
 */
public class TableNode implements Table {

    // Fields

    private Identifier      _tableName;

    private Identifier      _corrName;

    private Identifier      _schemaName;


    // Constructors

    public TableNode(String tableName, String corrName, String schemaName) {
        _tableName = new Identifier(tableName);
        _corrName = corrName==null ? null : new Identifier(corrName);
        _schemaName = schemaName==null ? null : new Identifier(schemaName);
    }

    public TableNode(String tableName, String corrName) {
        this(tableName, corrName, null);
    }

    public TableNode(String tableName) {
        this(tableName, null, null);
    }

    public TableNode() {
    }

    // PseudoConstructor
    // Use static methods because we can't overload Identier, String
    public static TableNode make(Identifier tableName, Identifier corrName, Identifier schemaName) {
        TableNode t = new TableNode();
        t._tableName = tableName;
        t._corrName = corrName;
        t._schemaName = schemaName;
        return t;
    }


    // Methods
    public String genText() {
        return genText(false);
    }

    // Return the SQL string that corresponds to this Table
    // This was originally called only in FROM clauses, but is now used as
    // part of column specifications.
    // For now, assume no joins
    public String genText(boolean from) {
        if (from)       // Calling from within a FROM clause
            return
                ((_schemaName==null) ? "" : _schemaName.genText()+".") +  // NOI18N
                _tableName.genText() +
                // remove AS to fix CR5097412
                ((_corrName==null) ? "" : " " + _corrName.genText());  // NOI18N
        else            // Calling from within a column
            return
                ((_corrName!=null)
                 ? _corrName.genText()
                 : ((_schemaName==null) ? "" : _schemaName.genText()+".") +  // NOI18N
                 _tableName.genText());
    }


    // Accessors/Mutators

    public String getTableName() {
        return _tableName.getName();
    }

    public String getFullTableName() {
        return
            ((_schemaName==null) ? "" : _schemaName.getName()+".") +   // NOI18N
            _tableName.getName();
    }

    public String getCorrName() {
        return (_corrName==null) ? null : _corrName.getName();
    }

    public String getSchemaName() {
        return (_schemaName==null) ? null : _schemaName.getName();
    }

    public String getTableSpec() {
        return (_corrName!=null) ?
               _corrName.getName() :
               getFullTableName();
    }

    // if oldTableSpec is null, just replace the table schema and name.
    public void renameTableSpec(String oldTableSpec, String corrName) {
        // If we really have a correlation name, there cannot be any schema specified to split
        if (oldTableSpec == null) {
            String[] table = corrName.split("\\.");    // NOI18N
            if (table.length>1) {
                _schemaName= new Identifier(table[0]);
                _tableName = new Identifier(table[1]);
            } else
                _tableName= new Identifier(table[0]);
        }
        else if (getTableSpec().equals(oldTableSpec))
            _corrName=(corrName==null) ? null : new Identifier(corrName);
    }

    void setTableName (String tableName) {
        String[] table = tableName.split("\\.");    // NOI18N
        if (table.length>1) {
            _schemaName= new Identifier(table[0]);
            _tableName = new Identifier(table[1]);
        } else
            _tableName=new Identifier(table[0]);
    }

    void setCorrName (String corrName) {
        _corrName=new Identifier(corrName);
    }

    void setTableSpec(String oldTableSpec, String newTableSpec) {
        if (getTableSpec().equals(oldTableSpec)) {
            String[] table = newTableSpec.split("\\.");    // NOI18N
            if (table.length>1) {
                _schemaName= new Identifier(table[0]);
                _tableName = new Identifier(table[1]);
            } else
                _tableName=new Identifier(table[0]);
        }

    }

    public void getReferencedColumns(Collection columns) {}
    public void getQueryItems(Collection items) {}
}
