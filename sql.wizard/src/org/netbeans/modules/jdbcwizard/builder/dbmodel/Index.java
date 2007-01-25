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
 * Interface describing index metadata for data sources providing information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface Index extends Cloneable {

    /** RCS id */
    static final String RCS_ID = "$Id$";

    /**
     * Gets (optional) name of this index.
     * 
     * @return Index name, or null if none was defined
     */
    public String getName();

    /**
     * Gets reference to JDBCTable that owns this Index.
     * 
     * @return parent JDBCTable
     */
    public DBTable getParent();

    /**
     * Gets type of index.
     * 
     * @return index type, as enumerated in DatabaseMetaData
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public int getType();

    /**
     * Indicates whether this index enforces uniqueness
     * 
     * @return true if index enforces uniqueness, false otherwise
     */
    public boolean isUnique();

    /**
     * Gets sort order, if any, used in this index
     * 
     * @return String indicating sort order; 'A' for ascending, 'D' for descending, null if no sort
     *         order is established
     */
    public String getSortSequence();

    /**
     * Gets cardinality of this index
     * 
     * @return value representing cardinality of the index
     */
    public int getCardinality();

    /**
     * Gets read-only List of Strings (in sequential order) representing names of indexed columns.
     * 
     * @return List of names of indexed columns
     */
    public List getColumnNames();

    /**
     * Gets count of indexed columns.
     * 
     * @return column count
     */
    public int getColumnCount();

    /**
     * Gets ordinal (base-one) sequence of the given DBColumn in this Index, provided it exists. The
     * return value ranges from 1 (first column) to n, where n is the total number of columns
     * indexed, or -1 if col is not indexed by this instance.
     * 
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the first in a composite
     *         index; -1 if col is not indexed by this instance
     */
    public int getSequence(DBColumn col);

    /**
     * Gets name of the column positioned as the iColumn-th column, if any, indexed by this Index.
     * iColumn ranges from 1 (first column) to n, where n is the total number of columns indexed.
     * 
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th indexed column, or null if no column exists at the given position.
     */
    public String getColumnName(int iColumn);

    /**
     * Indicates whether the column represented by the given columnName is indexed by this instance.
     * 
     * @param columnName name of column to test
     * @return true if columnName is indexed by this instance, false otherwise.
     */
    public boolean contains(String columnName);

    /**
     * Indicates whether this column is indexed by this instance.
     * 
     * @param col DBColumn to test
     * @return true if columnName is indexed by this instance, false otherwise.
     */
    public boolean contains(DBColumn col);
}
