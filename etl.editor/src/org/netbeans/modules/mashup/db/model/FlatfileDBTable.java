/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mashup.db.model;

import java.io.File;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLDBTable;


/**
 * Extends DBTable to support metadata and behavior of a flatfile as an analogue for a
 * database table.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDBTable extends SQLDBTable {

    public static final String PROP_CREATE_IF_NOT_EXIST = "CREATE_IF_NOT_EXIST";

    /* Constant: property name for file name */
    public static final String PROP_FILENAME = "FILENAME"; // NOI18N

    /* Constant: prefix of names for wizard-only properties */
    public static final String PROP_WIZARD = "WIZARD"; // NOI18N

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
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this ETLTable instance; false
     *         otherwise
     */
    @Override
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
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    String getCreateStatementSQL(String directory, String theTableName, String runtimeName, boolean isDynamicFilePath,
            boolean createDataFileIfNotExist);

    String getDropStatementSQL();

    /**
     * Gets the encoding scheme.
     * 
     * @return encoding scheme
     */
    String getEncodingScheme();

    String getFlatfilePropertiesSQL();
    
    String getFileName();

    /**
     * Gets local path to sample file.
     * 
     * @return path (in local workstation file system) to file, excluding the filename.
     */
    String getLocalFilePath();

    String getParserType();
    
    Map getProperties();

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
    @Override
    int hashCode();

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

    void setParseType(String type);

    void setProperties(Map newProps);

    boolean setProperty(String key, Object value);

    /**
     * Overrides default implementation to return fully-qualified name of this DBTable
     * (including name of parent DatabaseModel).
     * 
     * @return table name.
     */
    @Override
    String toString();

    void updateProperties(Map newProps);
}

