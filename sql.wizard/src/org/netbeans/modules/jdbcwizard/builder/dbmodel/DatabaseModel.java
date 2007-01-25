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
 * Root interface to be implemented by data sources that provide information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface DatabaseModel extends Cloneable {
    /** RCS id */
    static final String RCS_ID = "$Id$";

    /**
     * Gets associated DBConnectionDefinition
     * 
     * @return DBConnectionDefinition
     */
    public DBConnectionDefinition getConnectionDefinition();

    /**
     * Gets the name of this DBQueryModel.
     * 
     * @return model name
     */
    public String getModelName();

    /**
     * Gets the user-defined description string, if any, for this DBQueryModel.
     * 
     * @return String description, or null if none was defined
     */
    public String getModelDescription();

    /**
     * Adds new DBTable to the model.
     * 
     * @param table new DBTable to add
     * @return true if add succeeded, false otherwise
     */
    public boolean addTable(DBTable table);

    /**
     * Gets List of DBTables contained in this DBQueryModel
     * 
     * @return List of DBTable instances
     */
    public List getTables();

    /**
     * Gets the table, if any, associated with the given tuple (table name, schema name, catalog
     * name)
     * 
     * @param fqTableName Fully qualified table name (implementation-specific)
     * @return The table value
     */
    public DBTable getTable(String fqTableName);

    /**
     * Gets the table, if any, associated with the given tuple (table name, schema name, catalog
     * name)
     * 
     * @param tableName Table name
     * @param schemaName Schema name; may be null
     * @param catalogName Catalog name; may be null
     * @return The table value
     */
    public DBTable getTable(String tableName, String schemaName, String catalogName);

    /**
     * Constructs a fully qualified name that uniquely identifies the given DBTable instance from
     * among the available tables in this model The format of this string is implementation-specific
     * and useful only within the context of retrieving tables from this model.
     * 
     * @param tbl DBTable whose fully-qualified name is to be generated.
     * @return fully qualified table name for tbl
     */
    public String getFullyQualifiedTableName(DBTable tbl);

    /**
     * Constructs a fully qualified name that uniquely identifies a table with the given plain
     * table, schema and catalog names. The format of this string is implementation-specific and
     * useful only within the context of retrieving tables from this model.
     * 
     * @param table plain (unqualified) table name - mandatory
     * @param schema schema name - may be null
     * @param catalog catalog name - may be null
     * @return fully qualified table name based on above parameters
     */
    public String getFullyQualifiedTableName(String table, String schema, String catalog);

}
