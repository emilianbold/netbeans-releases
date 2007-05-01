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
package org.netbeans.modules.mashup.db.model;

import java.io.File;
import java.util.Map;

import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.common.utils.FlatfileDBTableMarker;
import org.w3c.dom.Element;


/**
 * Extends DBTable to support metadata and behavior of a flatfile as an analogue for a
 * database table.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDBTable extends DBTable, FlatfileDBTableMarker {

    public static final String PROP_CREATE_IF_NOT_EXIST = "CREATE_IF_NOT_EXIST";

    /* Constant: property name for file name */
    public static final String PROP_FILENAME = "FILENAME"; // NOI18N

    /* Constant: prefix of names for wizard-only properties */
    public static final String PROP_WIZARD = "WIZARD"; // NOI18N

    /**
     * Adds a DBColumn instance to this table.
     * 
     * @param theColumn column to be added.
     * @return true if successful. false if failed.
     */
    boolean addColumn(FlatfileDBColumn theColumn);

    /**
     * Clone a deep copy of DBTable.
     * 
     * @return a copy of DBTable.
     */
    Object clone();

    /**
     * Compares DBTable with another object for lexicographical ordering. Null objects and
     * those DBTables with null names are placed at the end of any ordered collection
     * using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name
     *         is the same. 1 if the column name is greater than obj to be compared.
     */
    int compareTo(Object refObj);

    /**
     * Performs deep copy of contents of given DBTable. We deep copy (that is, the method
     * clones all child objects such as columns) because columns have a parent-child
     * relationship that must be preserved internally.
     * 
     * @param source DBTable providing contents to be copied.
     */
    void copyFrom(DBTable source);

    /**
     * Performs deep copy of contents of given FlatfileDBTable. We deep copy (that is, the
     * method clones all child objects such as columns) because columns have a
     * parent-child relationship that must be preserved internally.
     * 
     * @param source FlatfileDBTable providing contents to be copied.
     */
    void copyFrom(FlatfileDBTable source);

    /**
     * Convenience class to create FlatfileDBColumnImpl instance (with the given column
     * name, data source name, JDBC type, scale, precision, and nullable), and add it to
     * this FlatfileDBTableImpl instance.
     * 
     * @param columnName Column name
     * @param jdbcType JDBC type defined in SQL.Types
     * @param scale Scale
     * @param precision Precision
     * @param isPK true if part of primary key, false otherwise
     * @param isFK true if part of foreign key, false otherwise
     * @param isIndexed true if indexed, false otherwise
     * @param nullable Nullable
     * @return new FlatfileDBColumnImpl instance
     */
    FlatfileDBColumn createColumn(String columnName, int jdbcType, int scale, int precision, boolean isPK, boolean isFK, boolean isIndexed,
            boolean nullable);

    /**
     * Deletes all columns associated with this table.
     * 
     * @return true if all columns were deleted successfully, false otherwise.
     */
    boolean deleteAllColumns();

    /**
     * Deletes DBColumn, if any, associated with the given name from this table.
     * 
     * @param columnName column name to be removed.
     * @return true if successful. false if failed.
     */
    boolean deleteColumn(String columnName);

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this ETLTable instance; false
     *         otherwise
     */
    boolean equals(Object obj);

    /**
     * Gets the Create Statement SQL for creating table for a flat file
     * 
     * @return SQL for this Flatfile with getTableName()
     */
    String getCreateStatementSQL();

    /**
     * Gets the SQL create statement to create a text table representing this flatfile.
     * 
     * @param tableName table name to use in synthesizing the create statement; if null,
     *        the current table name yielded by getName() will be used
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    String getCreateStatementSQL(String tableName);

    /**
     * Gets the SQL create statement to create a text table representing this flatfile.
     * 
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    String getCreateStatementSQL(String directory, String theTableName, String runtimeName, boolean isDynamicFilePath,
            boolean createDataFileIfNotExist);

    String getDropStatementSQL();

    /**
     * Gets the SQL Drop statement to drop the text table representing this flatfile.
     * 
     * @param tableName name of table to use in synthesizing the drop statement; if null,
     *        uses the value yielded by getName()
     * @return SQLstatement to drop a text table representing the contents of this
     *         flatfile
     */
    String getDropStatementSQL(String tableName);

    /**
     * Gets the encoding scheme.
     * 
     * @return encoding scheme
     */
    String getEncodingScheme();

    String getFlatfilePropertiesSQL();

    /**
     * Gets local path to sample file.
     * 
     * @return path (in local workstation file system) to file, excluding the filename.
     */
    String getLocalFilePath();

    String getParserType();

    /**
     * Gets property string associated with the given name.
     * 
     * @param key property key
     * @return property associated with propName, or null if no such property exists.
     */
    String getProperty(String key);

    String getSelectStatementSQL(int rows);

    /**
     * Gets the table name.
     * 
     * @return Table name
     */
    String getTableName();

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    int hashCode();

    void parseXML(Element xmlElement);
    /**
     * Sets description text for this instance.
     * 
     * @param newDesc new descriptive text
     */
    void setDescription(String newDesc);

    /**
     * Sets the encoding scheme.
     * 
     * @param newEncoding encoding scheme
     */
    void setEncodingScheme(String newEncoding);

    /**
     * Sets the file name.
     * 
     * @param newName new file name
     */
    void setFileName(String newName);

    /**
     * Sets local path to sample file.
     * 
     * @param localFile File representing path to sample file. If localFile represents the
     *        file itself, only the directory path will be stored.
     */
    void setLocalFilePath(File localFile);

    /*
     * Setters and non-API helper methods for this implementation.
     */
    /**
     * Sets table name to new value.
     * 
     * @param newName new value for table name
     */
    void setName(String newName);

    /**
     * Sets parent DatabaseModel to the given reference.
     * 
     * @param newParent new DatabaseModel parent
     */
    void setParent(FlatfileDatabaseModel newParent);

    void setParseType(String type);

    void setProperties(Map newProps);

    boolean setProperty(String key, Object value);

    /**
     * Overrides default implementation to return fully-qualified name of this DBTable
     * (including name of parent DatabaseModel).
     * 
     * @return table name.
     */
    String toString();

    /**
     * Marshall this object to XML string.
     * 
     * @param prefix
     * @return XML string
     */
    String toXMLString(String prefix);

    void updateProperties(Map newProps);
}

