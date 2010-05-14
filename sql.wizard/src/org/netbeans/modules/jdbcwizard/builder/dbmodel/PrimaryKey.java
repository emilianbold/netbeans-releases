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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel;

import java.util.List;

/**
 * Interface describing primary-key metadata for data sources providing information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface PrimaryKey extends Cloneable {

    /**
     * Gets (optional) name of this primary key.
     * 
     * @return name of PK, or null if none was defined
     */
    public String getName();

    /**
     * Gets reference to DBTable that owns this primary key.
     * 
     * @return parent DBTable
     */
    public DBTable getParent();

    /**
     * Gets read-only List of Strings (in key sequence order) representing names of columns
     * referenced in this PrimaryKey.
     * 
     * @return List of ColumnReference instances
     */
    public List getColumnNames();

    /**
     * Gets count of columns participating in this PrimaryKey.
     * 
     * @return column count
     */
    public int getColumnCount();

    /**
     * Gets ordinal (base-one) sequence of the column referenced by the given columnName in this
     * PrimaryKey, provided the column is actually part of this PK. The return value ranges from 1
     * (first column) to n, where n is the total number of columns in this PrimaryKey, or -1 if the
     * column referenced by given columnName is not part of this PrimaryKey.
     * 
     * @param columnName name of column whose sequence is requested
     * @return ordinal sequence of column referenced by columnName, starting with 1 if the column is
     *         the first in a composite key; -1 if the column is not part of this PrimaryKey
     */
    public int getSequence(String columnName);

    /**
     * Gets ordinal (base-one) sequence of the given DBColumn, provided it is participating in this
     * PK. The return value ranges from 1 (first column) to n, where n is the total number of
     * columns in this PrimaryKey, or -1 if col is not part of the PrimaryKey.
     * 
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the first in this
     *         (composite) key; -1 if col does not participate in this PrimaryKey
     */
    public int getSequence(DBColumn col);

    /**
     * Gets name of the DBColumn positioned as the iColumn-th column, if any, participating in this
     * PrimaryKey. iColumn ranges from 1 (first column) to n, where n is the total number of columns
     * in this PrimaryKey.
     * 
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th DBColumn in this PrimaryKey, or null if no column exists at the
     *         given position.
     */
    public String getDBColumnName(int iColumn);

    /**
     * Indicates whether this primary key contains the column represented by the given name.
     * 
     * @param columnName name of column to test
     * @return true if this PrimaryKey contains the column referenced by columnName, false
     *         otherwise.
     */
    public boolean contains(String columnName);

    /**
     * Indicates whether this primary key contains the given column.
     * 
     * @param col DBColumn to test
     * @return true if this PrimaryKey contains col, false otherwise
     */
    public boolean contains(DBColumn col);

    /**
     * Indicates whether this PrimaryKey is referenced by the given ForeignKey.
     * 
     * @param fk ForeignKey whose references are to be tested.
     * @return true if fk references this PrimaryKey, false otherwise.
     */
    public boolean isReferencedBy(ForeignKey fk);
}
