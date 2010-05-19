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
package org.netbeans.modules.sql.framework.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface describing table metadata for data sources providing information in a 
 * database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author Sudhendra Seshachala, Jonathan Giron
 */
public interface DBTable extends Cloneable {

    /**
     * Gets the user-defined name of this DBTable object.
     *
     * @return table name
     */
    public String getName();
    
    /**
     * Gets the user-defined description String, if any, defined for this 
     * instance.
     *
     * @return description String, for this DBTable or null if none was defined.
     */
    public String getDescription();

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
    public Map<String, DBColumn> getColumns();

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
    public List<DBColumn> getColumnList();
    
    /**
     * Get the DatabaseModel that contains this table.
     *
     * @return the instance of data source
     */
    public DatabaseModel getParent();

    /**
     * Gets PrimaryKey, if any, defined on this table.
     *
     * @return PrimaryKey instance containing metadata for this table's PK,
     * or null if no PK is defined
     */
    public PrimaryKey getPrimaryKey();

    /**
     * Gets a List of ForeignKeys defined on columns in this DBTable.
     *
     * @return List of ForeignKeys defined on columns of this table; returns
     * empty List if no ForeignKeys exist
     */
    public List<ForeignKey> getForeignKeys();
    
    /**
     * Gets the ForeignKey instance, if any, associated with the given FK name.
     *
     * @param fkName name of FK to locate
     * @return ForeignKey associated with fkName, or null if not found.
     */
    public ForeignKey getForeignKey(String fkName);

    /**
     * Gets a read-only Set of DBTables, if any, whose primary keys are 
     * referenced by foreign key columns in this table.
     *
     * @return read-only List of names of tables referenced by columns in this 
     * table; returns empty List if this DBTable has no FK columns.
     */
    public Set getReferencedTables();
    
    /**
     * Indicates whether the given table is referenced by one or more foreign
     * key in this table.
     * 
     * @param pkTarget table whose relationship with this table are to be checked
     * @return true if this table has one or more FKs that reference pkTarget,
     * false otherwise
     */
    public boolean references(DBTable pkTarget);
    
    /**
     * Gets ForeignKey, if any, that references a corresponding PrimaryKey in
     * the given DBTable.
     *
     * @param target DBTable whose relationship to this table is to be tested
     * @return ForeignKey instance representing reference to target, or null
     * if no such reference exists.
     */
    public ForeignKey getReferenceFor(DBTable target);
    
    /**
     * Gets List of Index objects representing indices defined on columns of
     * this table.
     *
     * @return List of Indexes defined on this table; returns empty List if no
     * indexes are defined.
     */
    public List<Index> getIndexes();
    
    /**
     * Gets Index, if any, associated with the given name.
     *
     * @param indexName name of index, if any, to be retrieved
     * @return Index instance associated with indexName, or null if none was
     * found.
     */
    public Index getIndex(String indexName);
}
