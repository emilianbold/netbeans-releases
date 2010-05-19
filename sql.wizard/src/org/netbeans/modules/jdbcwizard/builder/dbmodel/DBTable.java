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
import java.util.Map;
import java.util.Set;

/**
 * Interface describing table metadata for data sources providing information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface DBTable extends Cloneable {

    
    /**
     * Gets the user-defined name of this DBTable object.
     * 
     * @return table name
     */
    public String getName();

    /**
     * Gets the user-defined description String, if any, defined for this instance.
     * 
     * @return description String, for this DBTable or null if none was defined.
     */
    public String getDescription();

    /**
     * @param theColumn
     * @return
     */
    public boolean addColumn(DBColumn theColumn);

    /**
     * Gets name of the schema, if any, to which this DBTable belongs.
     * 
     * @return schema name, or null if it doesn't belong to a schema
     */
    public String getSchema();

    /**
     * Gets name of the catalog, if any, to which this DBTable belongs.
     * 
     * @return catalog name, or null if it doesn't belong to a catalog
     */
    public String getCatalog();

    /**
     * Get the column map for this table.
     * 
     * @return Column metadata for this table.
     */
    public Map getColumns();

    /**
     * Gets the DBColumn associated with the given name
     * 
     * @param columnName column name
     * @return The column value
     */
    public DBColumn getColumn(String columnName);

    /**
     * Gets a read-only List of DBColumn instances contained in this table.
     * 
     * @return read-only List of DBColumns
     */
    public List getColumnList();

    /**
     * Get the DatabaseModel that contains this table.
     * 
     * @return the instance of data source
     */
    public DatabaseModel getParent();

    /**
     * Gets PrimaryKey, if any, defined on this table.
     * 
     * @return PrimaryKey instance containing metadata for this table's PK, or null if no PK is
     *         defined
     */
    public PrimaryKey getPrimaryKey();

    /**
     * Gets a List of ForeignKeys defined on columns in this DBTable.
     * 
     * @return List of ForeignKeys defined on columns of this table; returns empty List if no
     *         ForeignKeys exist
     */
    public List getForeignKeys();

    /**
     * Gets the ForeignKey instance, if any, associated with the given FK name.
     * 
     * @param fkName name of FK to locate
     * @return ForeignKey associated with fkName, or null if not found.
     */
    public ForeignKey getForeignKey(String fkName);

    /**
     * Gets a read-only Set of DBTables, if any, whose primary keys are referenced by foreign key
     * columns in this table.
     * 
     * @return read-only List of names of tables referenced by columns in this table; returns empty
     *         List if this DBTable has no FK columns.
     */
    public Set getReferencedTables();

    /**
     * Indicates whether the given table is referenced by one or more foreign key in this table.
     * 
     * @param pkTarget table whose relationship with this table are to be checked
     * @return true if this table has one or more FKs that reference pkTarget, false otherwise
     */
    public boolean references(DBTable pkTarget);

    /**
     * Gets ForeignKey, if any, that references a corresponding PrimaryKey in the given DBTable.
     * 
     * @param target DBTable whose relationship to this table is to be tested
     * @return ForeignKey instance representing reference to target, or null if no such reference
     *         exists.
     */
    public ForeignKey getReferenceFor(DBTable target);

    /**
     * Gets List of Index objects representing indices defined on columns of this table.
     * 
     * @return List of Indexes defined on this table; returns empty List if no indexes are defined.
     */
    public List getIndexes();

    /**
     * Gets Index, if any, associated with the given name.
     * 
     * @param indexName name of index, if any, to be retrieved
     * @return Index instance associated with indexName, or null if none was found.
     */
    public Index getIndex(String indexName);

    /**
     * Indicates whether table is editable.
     * 
     * @return true if table is editable, false otherwise
     */
    public boolean isEditable();

    public boolean isSelected();

    /**
     * . set table editable
     */
    public void setEditable(boolean isEditable);

    public void setSelected(boolean select);

    /**
     * @return
     */
    public boolean isSelectedforAnOperation();

    public void setSelectedforAllOperations(boolean setAll);

}
