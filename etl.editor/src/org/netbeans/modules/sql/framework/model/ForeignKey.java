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

import org.netbeans.modules.sql.framework.model.DBColumn;
import java.util.List;

/**
 * Interface describing foreign-key metadata for data sources providing information 
 * in a database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author Jonathan Giron
 */
public interface ForeignKey extends Cloneable {
    
    /**
     * Gets user-defined name for this Foreign Key.
     *
     * @return FK name
     */
    public String getName();
    
    /**
     * Gets name, if any, of associated primary key
     *
     * @return name of this primary key; may be null if no name exists for the PK
     */
    public String getPKName();
    
    /**
     * Gets read-only List of Strings (in sequential order) representing names 
     * of columns comprising this ForeignKey.
     *
     * @return List of column names
     */
    public List<String> getColumnNames();
    
    /**
     * Gets read-only List of Strings (in key sequence order) representing
     * names of PK columns referenced by elements of this ForeignKey.
     *
     * @return List of PK column names referenced by this ForeignKey
     */
    public List<String> getPKColumnNames();

    /**
     * Gets name of PK column, if any, which the FK column (represented by the 
     * given column name) references.
     *
     * @param fkColumnName name of FK column whose referenced PK column is to
     * be retrieved
     * @return name of matching PK column, or null if fkColumnName is not an FK
     * column
     */
    public String getMatchingPKColumn(String fkColumnName);

    /**
     * Gets name of table containing PK columns referenced by this ForeignKey.
     *
     * @return PK table name
     */
    public String getPKTable();

    /**
     * Gets name of schema, if any, to which PK table belongs.
     *
     * @return schema name for PK table; null if no schema name is defined
     */
    public String getPKSchema();
    
    /**
     * Gets name of catalog, if any, to which PK table belongs.
     *
     * @return catalog name for PK table; null if no catalog name is defined
     */
    public String getPKCatalog();
    
    /**
     * Gets reference to DBTable that owns this primary key.
     *
     * @return parent DBTable
     */
    public DBTable getParent();
    
    /**
     * Gets count of columns participating in this ForeignKey.
     *
     * @return column count
     */
    public int getColumnCount();
    
    /**
     * Gets name of the column positioned as the iColumn-th column, if any, 
     * participating in this ForeignKey.  iColumn ranges from 1 (first column)
     * to n, where n is the total number of columns in this ForeignKey.
     *
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th DBColumn in this ForeignKey, or null if no 
     * column exists at the given position.
     */
    public String getColumnName(int iColumn);
    
    /**
     * Gets ordinal (base-one) sequence of the given DBColumn in this FK, 
     * provided it is part of this FK.  The return value ranges from 1 (first 
     * column) to n, where n is the total number of columns in this ForeignKey, 
     * or -1 if col is not part of the ForeignKey.
     *
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the 
     * first in a composite key; -1 if col does not participate in this 
     * ForeignKey
     */
    public int getSequence(DBColumn col);
    
    /**
     * Gets enumerated update rule associated with columns of this ForeignKey, 
     * as defined in java.sql.DatabaseMetaData.
     *
     * @return int value representing associated update rule
     * @see java.sql.DatabaseMetaData
     */
    public int getUpdateRule();

    /**
     * Gets enumerated delete rule associated with columns of this ForeignKey, 
     * as defined in java.sql.DatabaseMetaData.
     *
     * @return int value representing associated delete rule
     * @see java.sql.DatabaseMetaData
     */
    public int getDeleteRule();
    
    /**
     * Gets enumerated deferrability rule associated with columns of this 
     * ForeignKey, as defined in java.sql.DatabaseMetaData.
     *
     * @return int value representing associated deferrability rule
     * @see java.sql.DatabaseMetaData
     */
    public int getDeferrability();
    
    
    /**
     * Indicates whether this ForeignKey contains the column represented by
     * the given name.
     *
     * @param fkColumnName name of column to test
     * @return true if this ForeignKey contains the column referenced by 
     * fkColumnName, false otherwise.
     */
    public boolean contains(String fkColumnName);

    /**
     * Indicates whether this ForeignKey contains the given column.
     *
     * @param fkCol ETLColumn to test
     * @return true if this ForeignKey contains fkCol, false otherwise
     */
    public boolean contains(DBColumn fkCol);
    
    /**
     * Indicates whether this ForeignKey references columns in the DBTable
     * represented by the given tuple of (table name, schema name, catalog name).
     *
     * @param pk PrimaryKey whose relationship to this FK is to be tested
     * @return true if this FK references columns in pk; false otherwise
     */
    public boolean references(PrimaryKey pk);
}

