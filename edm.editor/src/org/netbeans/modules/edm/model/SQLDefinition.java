/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.model;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.edm.model.visitors.SQLVisitedObject;
import org.netbeans.modules.edm.model.EDMException;

/**
 * Root container interface for holding SQL model objects.
 *
 * @author Sudhi Seshachala
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface SQLDefinition extends SQLContainerObject, SQLVisitedObject {

    public static final String ATTR_DISPLAYNAME = "displayName";
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_RESPONSE_TYPE = "responseType";
    
    /** XML formatting constant: indent prefix */
    public static final String INDENT = "    ";
    public static final String AXION_DB_WORKING_DIR = "AxiondbWorkingDirectory";
    public static final String AXION_DB_DATA_DIR = "AxiondbDataDirectory";
    public static final String DYNAMIC_FLAT_FILE = "DynamicFlatFile";

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
     * @throws EDMException if error occurs while resolving root join
     */
    public SQLObject getRootJoin(List<DBTable> sourceTables) throws EDMException;

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

    public EDMParentObject getEDMParentObject();

    /**
     * get the tag name for this SQLDefinition override at subclass level to return a
     * different tag name
     *
     * @return tag name to be used in xml representation of this object
     */
    public String getTagName();

    /**
     * Check if a table already exists in this definition
     *
     * @param table - table
     * @return Object - the existing table
     * @throws EDMException - exception
     */
    public Object isTableExists(DBTable table) throws EDMException;

    /**
     * Applies whatever rules are appropriate to migrate the current object model to the
     * current version of SQLDefinition as implemented by the concrete class.
     *
     * @throws EDMException if error occurs during migration
     */
    public void migrateFromOlderVersions() throws EDMException;

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

    public void setAttribute(String attrName, Object val);

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets the response type.
     * 
     * @param code
     */
    public void setResponseType(String code);

    public void setEDMParentObject(EDMParentObject newParent);

    public void setVersion(String newVersion);

    /**
     * validate the definition starting from the target tables.
     *
     * @return Map of invalid input object as keys and reason as value
     */
    public List<ValidationInfo> validate();

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

    public String getResponseType();

    /**
     * Removes the join operator only. Source Tables used in the join are retained.
     * @param joinView
     */
    public void removeJoinViewOnly(SQLJoinView joinView);
}