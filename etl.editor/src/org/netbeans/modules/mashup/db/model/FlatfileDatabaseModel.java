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

import java.util.Map;

import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.model.database.JDBCConnectionProvider;
import org.w3c.dom.Element;


/**
 * Root interface to be implemented by ETL-compatible flatfile data sources that provide
 * information in a row-and-column addressable format. Extends DatabaseModel to support
 * collection of flatfiles as analogues for a database instance of tables. This model
 * represents a collection of one or more flatfiles in a single directory.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDatabaseModel extends DatabaseModel, JDBCConnectionProvider {

    /**
     * Adds new SourceTable to the model.
     * 
     * @param table new DBTable to add
     */
    void addTable(FlatfileDBTable table);
    
    /**
     * Clones this object.
     * 
     * @return shallow copy of this ETLDataSource
     */
    Object clone();

    /**
     * Copies member values from those contained in the given DatabaseModel instance.
     */
    void copyFrom(DatabaseModel src);

    /**
     * Copies member values from those contained in the given FlatfileDatabaseModel
     * instance.
     * 
     * @param src DatabaseModel whose contents are to be copied into this instance
     */
    void copyFrom(FlatfileDatabaseModel src);

    /**
     * Create DBTable instance with the given table, schema, and catalog names.
     * 
     * @param tableName table name of new table
     * @param schemaName schema name of new table
     * @param catalogName catalog name of new table
     * @return an instance of ETLTable if successful, null if failed.
     */
    DBTable createTable(String tableName, String schemaName, String catalogName);

    /**
     * Deletes all tables associated with this data source.
     * 
     * @return true if all tables were deleted successfully, false otherwise.
     */
    boolean deleteAllTables();

    /**
     * Delete table from the ETLDataSource
     * 
     * @param fqTableName fully qualified name of table to be deleted.
     * @return true if successful. false if failed.
     */
    boolean deleteTable(String fqTableName);

    /**
     * @see java.lang.Object#equals
     */
    boolean equals(Object refObj);

    /**
     * Gets name of DBConnectionDefinition associated with this database model.
     * 
     * @return name of associated DBConnectionDefinition instance
     */
    String getConnectionName();
    
    /**
     * Gets Flatfile instance, if any, whose file name matches the given String
     * 
     * @param aName file name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    FlatfileDBTable getFileMatchingFileName(String aName);

    /**
     * Gets Flatfile instance, if any, whose table name matches the given String.
     * 
     * @param tableName table name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    FlatfileDBTable getFileMatchingTableName(String tableName);

    FlatfileDBConnectionDefinition getFlatfileDBConnectionDefinition(boolean download);

    Map getFlatfileTablePropertyMap(String flatfileName);

    Map getFlatfileTablePropertyMaps();

    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.repository.FlatfileDatabaseModel#getMajorVersion
     */
    int getMajorVersion();

    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.repository.FlatfileDatabaseModel#getMicroVersion
     */
    int getMicroVersion();

    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.repository.FlatfileDatabaseModel#getMinorVersion
     */
    int getMinorVersion();

    /**
     * Gets a read-only Map of table names to available DBTable instances in this model.
     * 
     * @return readonly Map of table names to DBTable instances
     */
    Map getTableMap();

    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.repository.FlatfileDatabaseModel#getVersionString
     */
    String getVersionString();

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    int hashCode();

    /**
     * Unmarshal this object from XML element.
     * 
     * @param xmlElement
     */
    void parseXML(Element xmlElement);

    /**
     * Setter for FlatfileDBConnectionDefinition
     * 
     * @param theConnectionDefinition to be set
     */
    void setConnectionDefinition(FlatfileDBConnectionDefinition theConnectionDefinition);

    /**
     * Sets the Connection Name associated with connection name
     * 
     * @param theConName associated with this DataSource
     */
    void setConnectionName(String theConName);

    /**
     * Sets the description string of this DatabaseModel
     * 
     * @param newDesc new description string
     */
    void setDescription(String newDesc);

    /**
     * Setter for Model Name
     * 
     * @param theModelName
     */
    void setModelName(String theModelName);

    /**
     * Sets repository object, if any, providing underlying data for this DatabaseModel
     * implementation.
     * 
     * @param obj StcdbObjectTypeDefinition hosting this object's metadata, or null if data are not
     *        held by a StcdbObjectTypeDefinition.
     */
    void setSource(FlatfileDefinition obj);


    /**
     * Setter for tables
     * 
     * @param theTables to be part of Model
     */
    void setTables(Map theTables);

    /**
     * Overrides default implementation to return name of this DatabaseModel.
     * 
     * @return model name.
     */
    String toString();

    /**
     * Marshall this object to XML string.
     * 
     * @param prefix
     * @return XML string
     */
    String toXMLString(String prefix);

}

