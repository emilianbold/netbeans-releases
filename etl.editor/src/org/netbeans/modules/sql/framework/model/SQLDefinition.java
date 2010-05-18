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
package org.netbeans.modules.sql.framework.model;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;
import com.sun.etl.exception.BaseException;

/**
 * Root container interface for holding SQL model objects.
 *
 * @author Sudhi Seshachala
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface SQLDefinition extends SQLContainerObject, SQLVisitedObject {

    public static final String ATTR_CONTAINS_JAVA_OPERATORS = "containsJavaOperators";
    public static final String ATTR_DISPLAYNAME = "displayName";
    public static final String ATTR_EXECUTION_STRATEGY_CODE = "executionStrategyCode";
    public static final String ATTR_VERSION = "version";
    // Execution Strategy codes
    public static final int EXECUTION_STRATEGY_BEST_FIT = 0;
    public static final int EXECUTION_STRATEGY_DEFAULT = EXECUTION_STRATEGY_BEST_FIT;
    public static final int EXECUTION_STRATEGY_PIPELINE = 1;
    public static final int EXECUTION_STRATEGY_STAGING = 2;
    //Response TYPES
    public static final int EXECUTION_STRATEGY_WEBBROWSET = 1;
    public static final int EXECUTION_STRATEGY_RELATIONALMAP = 2;
    /** XML formatting constant: indent prefix */
    public static final String INDENT = "    ";

    public static final String AXION_DB_WORKING_DIR="AxiondbWorkingDirectory";
    public static final String AXION_DB_DATA_DIR="AxiondbDataDirectory";
    public static final String DYNAMIC_FLAT_FILE="DynamicFlatFile";

    /**
     * add an sql object listener
     *
     * @param listener sql object listener
     */
    public void addSQLObjectListener(SQLObjectListener listener);

    /**
     * Clear Catalog and Schema names overrides from all DatabaseModel
     */
    public void clearOverride(boolean clearCatalog, boolean clearSchema);

    /**
     * generate unique id for objects in this sqldefinition
     */
    public String generateId();

    /**
     * Gets the List of Databases
     *
     * @return java.util.List for this
     */
    public List<SQLDBModel> getAllDatabases();

    /**
     * set the condition text
     *
     * @param text condition text
     */
    public void setExecutionStrategyStr(String text);

    
    /**
     * sets the working folder where axion instance will 
     * run this colloboration
     * @param appDataRoot
     */
    public void setAxiondbWorkingDirectory(String appDataRoot);
    
    /**
     * sets the name of the axion instance where this etl 
     * colloboration is run
     * @param dbInstanceName
     */
    public void setAxiondbDataDirectory(String dbInstanceName);
    
    /**
     * getter for axion db working folder
     * @return
     */
    public String getAxiondbWorkingDirectory();
    
    /**
     * getter for the axion database instance name
     * @return
     */
    public String getAxiondbDataDirectory();
    
    
    public boolean isDynamicFlatFile();
    
    public void setDynamicFlatFile(boolean flag);
    
    
    public Object getAttributeValue(String attrName);

    /**
     * Gets display name.
     *
     * @return current display name
     */
    public String getDisplayName();

    /**
     * Gets execution strategy code set.
     *
     * @return
     */
    public Integer getExecutionStrategyCode();

    /**
     * Gets execution strategy string value .
     *
     * @return
     */
    public String getExecutionStrategyStr();

    /**
     * get all join sources. This includes tables which are not part of any join view and
     * joinviews.
     *
     * @return list of join sources
     */
    public List<DBTable> getJoinSources();

    /**
     * Gets the Root SQLJoinOperator object, if any, from the given List
     *
     * @param sourceTables List of source table SQLObjects
     * @return SQLObject root join
     * @throws BaseException if error occurs while resolving root join
     */
    public SQLObject getRootJoin(List<DBTable> sourceTables) throws BaseException;

    /**
     * get runtime db model
     *
     * @return runtime dbmodel
     */
    public RuntimeDatabaseModel getRuntimeDbModel();

    /**
     * Gets the List of SourceColumns
     *
     * @return List, possibly empty, of SourceColumns
     */
    public List<DBColumn> getSourceColumns();

    /**
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of source DatabaseModels
     */
    public List<SQLDBModel> getSourceDatabaseModels();

    /**
     * Gets the List of SourceTables
     *
     * @return List, possibly empty, of SourceTables
     */
    public List<DBTable> getSourceTables();

    public SQLFrameworkParentObject getSQLFrameworkParentObject();

    /**
     * get the tag name for this SQLDefinition override at subclass level to return a
     * different tag name
     *
     * @return tag name to be used in xml representation of this object
     */
    public String getTagName();

    /**
     * Gets the List of TargetColumns
     *
     * @return List, possibly empty, of TargetColumns
     */
    public List<DBColumn> getTargetColumns();

    /**
     * Gets a List of target DatabaseModels
     *
     * @return List, possibly empty, of target DatabaseModels
     */
    public List<SQLDBModel> getTargetDatabaseModels();

    /**
     * Gets the List of TargetTables
     *
     * @return List, possibly empty, of TargetTables
     */
    public List<DBTable> getTargetTables();

    /**
     * Indicates whether this model has data validation conditions.
     *
     * @return true if data validation conditions exist; false otherwise
     */
    public boolean hasValidationConditions();

    /**
     * Check if a java operator is used in the model.
     *
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators();

    /**
     * Check if a table already exists in this definition
     *
     * @param table - table
     * @return Object - the existing table
     * @throws BaseException - exception
     */
    public Object isTableExists(DBTable table) throws BaseException;

    /**
     * Applies whatever rules are appropriate to migrate the current object model to the
     * current version of SQLDefinition as implemented by the concrete class.
     *
     * @throws BaseException if error occurs during migration
     */
    public void migrateFromOlderVersions() throws BaseException;

    /**
     * Override Catalog names in proper DatabaseModel
     * @param overrideMapMap
     */
    public void overrideCatalogNamesForDb(Map overrideMapMap);

    /**
     * Override Schema names in proper DatabaseModel
     * @param overrideMapMap
     */
    public void overrideSchemaNamesForDb(Map overrideMapMap);

    /**
     * remove sql object listener
     *
     * @param listener sql object listener
     */
    public void removeSQLObjectListener(SQLObjectListener listener);

    /**
     * check if we have to use axion database if definition contains a java operator or
     * there is a validation condition on one of source tables.
     *
     * @return
     */
    public boolean requiresPipelineProcess();

    public void setAttribute(String attrName, Object val);

    /**
     * set it to true if a java operator is used in the model
     *
     * @param javaOp true if there is a java operator
     */
    public void setContainsJavaOperators(boolean javaOp);

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets the execution strategy.
     *
     * @param code
     */
    public void setExecutionStrategyCode(Integer code);

    public void setSQLFrameworkParentObject(SQLFrameworkParentObject newParent);

    public void setVersion(String newVersion);

    /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> validate();
    
    /**
     * validate the definition model alone (not for drivers)
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> validateModel();

   /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> badgeValidate();
    
    /**
     * Validate Database synchronization. Identify any eTL Collaboration element which has been
     * deleted or modified in Database.
     *
     * @return Map of invalid object as keys and reason as value
     */
    public List<ValidationInfo> validateDbSynchronization();
}