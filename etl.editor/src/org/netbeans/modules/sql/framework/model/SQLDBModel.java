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

import java.util.List;
import java.util.Map;

import org.netbeans.modules.etl.model.ETLObject;
import com.sun.sql.framework.exception.BaseException;

/**
 * Extension of DBModel
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */

public interface SQLDBModel extends DatabaseModel, SQLObject {

    /** Constant for DatabaseModel metadata name tag. */
    public static final String MODEL_TAG = "dbModel";
    /** String constant for model name tag. */
    public static final String NAME = "name";
    /** String constant for absolute path to owning project */
    public static final String PROJECTPATH = "projectPath";
    /** String constant for reference ID. */
    public static final String REFID = "refId";
    /** String constant for Database reference key ID. */
    public static final String REFKEY = "refKey";
    /** String constant indicating source type */
    public static final String STRTYPE_SOURCE = "source";
    /** String constant indicating source type */
    public static final String STRTYPE_TARGET = "target";

    /**
     * Adds table to this instance.
     * 
     * @param table new table to add
     * @throws IllegalStateException if unable to add table
     */
    public void addTable(SQLDBTable table) throws IllegalStateException;

    public void clearOverride(boolean clearCatalogOverride, boolean clearSchemaOverride);

    /**
     * Clones this object.
     * 
     * @return shallow copy of this SQLDataSource
     */
    public Object clone();

    /**
     * check if a table exists This will check if a table is in database model,
     */
    public boolean containsTable(SQLDBTable table);

    /**
     * Copies member values to those contained in the given DatabaseModel instance.
     * 
     * @param src DatabaseModel whose contents are to be copied into this instance
     */
    public void copyFrom(DatabaseModel src);

    /**
     * Copies member values to those contained in the given DatabaseModel instance, using
     * the given value for object type.
     * 
     * @param src DatabaseModel whose contents are to be copied into this instance
     * @param objType type of object (SOURCE_DBMODEL or TARGET_DBMODEL)
     */
    public void copyFrom(DatabaseModel src, int objType);

    /**
     * Create DBTable instance with the given table, schema, and catalog names.
     * 
     * @param tableName table name of new table
     * @param schemaName schema name of new table
     * @param catalogName catalog name of new table
     * @return an instance of SQLTable if successful, null if failed.
     */
    public DBTable createTable(String tableName, String schemaName, String catalogName);

    /**
     * Deletes all tables associated with this data source.
     * 
     * @return true if all tables were deleted successfully, false otherwise.
     */
    public boolean deleteAllTables();

    /**
     * Delete table from the SQLDataSource
     * 
     * @param fqTableName fully qualified name of table to be deleted.
     * @return true if successful. false if failed.
     */
    public boolean deleteTable(String fqTableName);

    /**
     * @see java.lang.Object#equals
     */
    public boolean equals(Object refObj);

    /**
     * Gets the allTables attribute of the SQLDataSource object
     * 
     * @return The allTables value
     */
    public Map getAllSQLTables();

    /**
     * get a list of tables based on table name, schema name and catalog name since we
     * allow duplicate tables this will return a list of tables
     */
    public List getAllTables(String tableName, String schemaName, String catalogName);

    /**
     * Gets SQLDBConnectionDefinition of the SQLDataSource object
     * 
     * @return ConnectionDefinition of the SQLDataSource object
     */
    public DBConnectionDefinition getETLDBConnectionDefinition() throws BaseException;

    /**
     * Gets SQLObject, if any, having the given object ID.
     * 
     * @param objectId ID of SQLObject being sought
     * @return SQLObject associated with objectID, or null if no such object exists.
     */
    public SQLObject getObject(String objectId);

    public String getRefKey();

    /**
     * Gets a read-only Map of table names to available DBTable instances in this model.
     * 
     * @return readonly Map of table names to DBTable instances
     */
    public Map getTableMap();

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode();



    public void overrideCatalogNames(Map catalogOverride);

    public void overrideSchemaNames(Map schemaOverride);

    public void setConnectionDefinition(DBConnectionDefinition dbConnectionDef);

    /**
     * Sets the description string of this DatabaseModel
     * 
     * @param newDesc new description string
     */
    public void setDescription(String newDesc);

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelName
     */
    public void setModelName(String theName);

    public void setRefKey(String aKey);

    /**
     * Sets repository object, if any, providing underlying data for this DatabaseModel
     * implementation.
     * 
     * @param obj Object hosting this object's metadata, or null if data are not
     *        held by a ETLObject.
     */
    public void setSource(ETLObject obj);

    public void setSQLFrameworkParentObject(SQLFrameworkParentObject aParent);

}
