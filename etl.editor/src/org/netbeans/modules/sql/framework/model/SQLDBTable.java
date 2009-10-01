/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model;


/**
 * Extension of DBTable
 */
public interface SQLDBTable extends DBTable, SQLCanvasObject, SQLObject {

    /** Constant for table metadata name tag.* */
    public static final String TABLE_TAG = "dbTable"; // NOI18N

    /**
     * Adds given SQLDBColumn to this table
     * 
     * @param theColumn SQLDBColumn to be added
     * @return true if addition succeeded
     */
    public boolean addColumn(SQLDBColumn theColumn);

    /**
     * Clear catalog name override
     */
    public void clearOverride(boolean clearCatalogOverride, boolean clearSchemaOverride);

    /**
     * Copies information from given DBTable instance
     * 
     * @param source DBTable from which to copy table info
     */
    public void copyFrom(DBTable source);

    /**
     * Deletes all columns from this table
     * 
     * @return true of deletion succeeded
     */
    public boolean deleteAllColumns();

    /**
     * Deletes SQLDBColumn, if any, associated with the given column name.
     * 
     * @param columnName column name of SQLDBColumn to delete
     * @return true if deletion succeeded
     */
    public boolean deleteColumn(String columnName);

    /**
     * get the alias name for this table
     * 
     * @return alias name
     */
    public String getAliasName();

    /**
     * Gets commit batch size for this db table.
     * 
     * @return batch size for commits involving this table.
     */
    public int getBatchSize();

    /**
     * Gets the flat file location runtime input name which is generate when a flat file
     * table is added to collaboration. Use this name at runtime for file location passed
     * by eInsight.
     * 
     * @return name of runtime input argument for flat file location
     */
    public String getFlatFileLocationRuntimeInputName();

    /**
     * get table fully qualified name including schema , catalog info
     * 
     * @return fully qualified table name prefixed with alias
     */
    public String getFullyQualifiedName();

    public SQLObject getObject(String objectId);

    /**
     * get table qualified name
     * 
     * @return qualified table name prefixed with alias
     */
    public String getQualifiedName();

    public String getRuntimeArgumentName();

    /**
     * Gets the user defined table name prefix
     * 
     * @return user defined table
     */
    public String getTablePrefix();

    /**
     * Gets concats tablename alias name
     * 
     * @return String Unique name for the table
     */
    public String getUniqueTableName();

    /**
     * Gets the user defined catalog name.
     * 
     * @return user defined catalog name
     */
    public String getUserDefinedCatalogName();

    /**
     * Gets the user defined schema name.
     * 
     * @return user defined schema name
     */
    public String getUserDefinedSchemaName();

    /**
     * Gets the user defined table name.
     * 
     * @return user defined table name
     */
    public String getUserDefinedTableName();
        
    //RFE-102428
    /**
     * Gets the staging table name.
     * 
     * @return staging table name
     */
    public String getStagingTableName();
    
    /**
     * @return Returns the aliasUsed.
     */
    public boolean isAliasUsed();

    /**
     * Indicates whether table is editable.
     * 
     * @return true if table is editable, false otherwise
     */
    public boolean isEditable();

    /**
     * Indicates whether table is selected.
     * 
     * @return true if table is selected, false otherwise
     */
    public boolean isSelected();

    /**
     * Indicates whether the fully-qualified form should be used whenever one resolves
     * this table's name.
     * 
     * @return true if fully-qualified form should be used, false otherwise
     */
    public boolean isUsingFullyQualifiedName();

    /**
     * Override Catalog name
     * 
     * @param nName
     */
    public void overrideCatalogName(String nName);

    /**
     * Override Schema Name
     * 
     * @param nName
     */
    public void overrideSchemaName(String nName);

    /**
     * set the alias name for this table
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName);

    /**
     * @param aliasUsed The aliasUsed to set.
     */
    public void setAliasUsed(boolean aliasUsed);

    /**
     * Sets commit batch size for this db table.
     * 
     * @param newSize new batch size value for commits involving this table; use -1 to use
     *        default batch size.
     */
    public void setBatchSize(int newSize);

    public void setCatalog(String newCatalog);

    /**
     * Sets whether table is editable.
     * 
     * @param editable true if table is editable, false otherwise
     */
    public void setEditable(boolean editable);

    /**
     * set flat file location runtime input name which is generate when a flat file table
     * is added to collaboration
     * 
     * @param runtimeArgName name of runtime input argument for flat file location
     */
    public void setFlatFileLocationRuntimeInputName(String runtimeArgName);
   
    /**
     * Sets table name to given value.
     * 
     * @param newName new table name
     */
    public void setName(String newName);

    public void setParent(SQLDBModel newParent);

    /**
     * Sets schema name of this table.
     * 
     * @param schema new schema name
     */
    public void setSchema(String schema);

    /**
     * Sets whether table is selected.
     * 
     * @param sel true if table is selected, false otherwise
     */
    public void setSelected(boolean sel);

    /**
     * Sets the user defined table name.
     * 
     * @param tPrefix user defined table name
     */
    public void setTablePrefix(String tPrefix);

    /**
     * Sets the user defined catalog name.
     * 
     * @param newName user defined table name
     */
    public void setUserDefinedCatalogName(String newName);

    /**
     * Sets the user defined schema name.
     * 
     * @param newName user defined schema name
     */
    public void setUserDefinedSchemaName(String newName);

    /**
     * Sets the user defined table name.
     * 
     * @param newName user defined table name
     */
    public void setUserDefinedTableName(String newName);

    /**
     * Indicates whether the fully-qualified form should be used whenever one resolves
     * this table's name.
     * 
     * @param usesFullName true if fully-qualified form should be used, false otherwise
     */
    public void setUsingFullyQualifiedName(boolean usesFullName);
    
    //RFE-102428
    /**
     * Sets the staging table name.
     * 
     * @param stName staging table name
     */
    public void setStagingTableName(String stName);

}
