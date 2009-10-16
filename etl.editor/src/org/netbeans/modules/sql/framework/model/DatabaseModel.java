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

import org.netbeans.modules.etl.model.ETLObject;


/**
 * Root interface to be implemented by data sources that provide information in 
 * a database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author  Sudhendra Seshachala, Jonathan Giron
 */
public interface DatabaseModel extends Cloneable {
    /**
     * Gets associated DBConnectionDefinition 
     *
     * @return DBConnectionDefinition
     */
    public DBConnectionDefinition getConnectionDefinition();

    /**
     * Gets the name of this DatabaseModel.
     *
     * @return model name
     */
    public String getModelName();
    
    /**
     * Gets the user-defined description string, if any, for this DatabaseModel.
     * @return String description, or null if none was defined
     */
    public String getModelDescription();

    /**
     * Gets List of DBTables contained in this DatabaseModel
     * @return List of DBTable instances
     */
    public List<DBTable> getTables();

    /**
     * Gets the table, if any, associated with the given tuple 
     * (table name, schema name, catalog name)
     *
     * @param fqTableName Fully qualified table name (implementation-specific)
     * @return The table value
     */
    public DBTable getTable(String fqTableName);
    
    /**
     * Gets the table, if any, associated with the given tuple 
     * (table name, schema name, catalog name)
     *
     * @param tableName Table name
     * @param schemaName Schema name; may be null
     * @param catalogName Catalog name; may be null
     * @return The table value
     */
    public DBTable getTable(String tableName, String schemaName, String catalogName);
        
    /**
     * Constructs a fully qualified name that uniquely identifies the given 
     * DBTable instance from among the available tables in this model  The
     * format of this string is implementation-specific and useful only within
     * the context of retrieving tables from this model.
     *
     * @param tbl DBTable whose fully-qualified name is to be generated.
     * @return fully qualified table name for tbl
     */
    public String getFullyQualifiedTableName(DBTable tbl);
    
    /**
     * Constructs a fully qualified name that uniquely identifies a table
     * with the given plain table, schema and catalog names.  The format of
     * this string is implementation-specific and useful only within the
     * context of retrieving tables from this model.
     *
     * @param table plain (unqualified) table name - mandatory
     * @param schema schema name - may be null
     * @param catalog catalog name - may be null
     * @return fully qualified table name based on above parameters
     */
    public String getFullyQualifiedTableName(String table, String schema, String catalog);
    
    /**
     * Gets repository object, if any, providing underlying data for this
     * DatabaseModel.
     *
     * @return ETLObject hosting this object's metadata, or null if
     * data are not held by a ETLObject.
     */
    public ETLObject getSource();
    
}

