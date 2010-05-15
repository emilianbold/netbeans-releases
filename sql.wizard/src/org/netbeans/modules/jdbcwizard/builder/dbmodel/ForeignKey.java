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
 * Interface describing foreign-key metadata for data sources providing information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface ForeignKey extends Cloneable {

    /**
     * Gets user-defined name for this Foreign Key.
     * 
     * @return FK name
     */
    public String getName();

    /**
     * Gets name, if any, of associated primary key
     * 
     * @return name of this primary key; may be null if no name exists for the PK
     */
    public String getPKName();

    /**
     * Gets read-only List of Strings (in sequential order) representing names of columns comprising
     * this ForeignKey.
     * 
     * @return List of column names
     */
    public List getColumnNames();

    /**
     * Gets read-only List of Strings (in key sequence order) representing names of PK columns
     * referenced by elements of this ForeignKey.
     * 
     * @return List of PK column names referenced by this ForeignKey
     */
    public List getPKColumnNames();

    /**
     * Gets name of PK column, if any, which the FK column (represented by the given column name)
     * references.
     * 
     * @param fkColumnName name of FK column whose referenced PK column is to be retrieved
     * @return name of matching PK column, or null if fkColumnName is not an FK column
     */
    public String getMatchingPKColumn(String fkColumnName);

    /**
     * Gets name of table containing PK columns referenced by this ForeignKey.
     * 
     * @return PK table name
     */
    public String getPKTable();

    /**
     * Gets name of schema, if any, to which PK table belongs.
     * 
     * @return schema name for PK table; null if no schema name is defined
     */
    public String getPKSchema();

    /**
     * Gets name of catalog, if any, to which PK table belongs.
     * 
     * @return catalog name for PK table; null if no catalog name is defined
     */
    public String getPKCatalog();

    /**
     * Gets reference to DBTable that owns this primary key.
     * 
     * @return parent DBTable
     */
    public DBTable getParent();

    /**
     * Gets count of columns participating in this ForeignKey.
     * 
     * @return column count
     */
    public int getColumnCount();

    /**
     * Gets name of the column positioned as the iColumn-th column, if any, participating in this
     * ForeignKey. iColumn ranges from 1 (first column) to n, where n is the total number of columns
     * in this ForeignKey.
     * 
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th DBColumn in this ForeignKey, or null if no column exists at the
     *         given position.
     */
    public String getColumnName(int iColumn);

    /**
     * Gets ordinal (base-one) sequence of the given DBColumn in this FK, provided it is part of
     * this FK. The return value ranges from 1 (first column) to n, where n is the total number of
     * columns in this ForeignKey, or -1 if col is not part of the ForeignKey.
     * 
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the first in a composite
     *         key; -1 if col does not participate in this ForeignKey
     */
    public int getSequence(DBColumn col);

    /**
     * Gets enumerated update rule associated with columns of this ForeignKey, as defined in
     * java.sql.DatabaseMetaData.
     * 
     * @return int value representing associated update rule
     * @see java.sql.DatabaseMetaData
     */
    public int getUpdateRule();

    /**
     * Gets enumerated delete rule associated with columns of this ForeignKey, as defined in
     * java.sql.DatabaseMetaData.
     * 
     * @return int value representing associated delete rule
     * @see java.sql.DatabaseMetaData
     */
    public int getDeleteRule();

    /**
     * Gets enumerated deferrability rule associated with columns of this ForeignKey, as defined in
     * java.sql.DatabaseMetaData.
     * 
     * @return int value representing associated deferrability rule
     * @see java.sql.DatabaseMetaData
     */
    public int getDeferrability();

    /**
     * Indicates whether this ForeignKey contains the column represented by the given name.
     * 
     * @param fkColumnName name of column to test
     * @return true if this ForeignKey contains the column referenced by fkColumnName, false
     *         otherwise.
     */
    public boolean contains(String fkColumnName);

    /**
     * Indicates whether this ForeignKey contains the given column.
     * 
     * @param fkCol JDBCColumn to test
     * @return true if this ForeignKey contains fkCol, false otherwise
     */
    public boolean contains(DBColumn fkCol);

    /**
     * Indicates whether this ForeignKey references columns in the DBTable represented by the given
     * tuple of (table name, schema name, catalog name).
     * 
     * @param pk PrimaryKey whose relationship to this FK is to be tested
     * @return true if this FK references columns in pk; false otherwise
     */
    public boolean references(PrimaryKey pk);
}
