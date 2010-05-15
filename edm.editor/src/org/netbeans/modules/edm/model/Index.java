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
package org.netbeans.modules.edm.model;

import org.netbeans.modules.edm.model.DBColumn;
import java.util.List;

/**
 * Interface describing index metadata for data sources providing information 
 * in a database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author Jonathan Giron
 */
public interface Index extends Cloneable {
    
    /**
     * Gets (optional) name of this index.
     *
     * @return Index name, or null if none was defined
     */
    public String getName();
    
    /**
     * Gets reference to ETLTable that owns this Index.
     *
     * @return parent ETLTable
     */
    public DBTable getParent();
    
    /**
     * Gets type of index.
     *
     * @return index type, as enumerated in DatabaseMetaData
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public int getType();
        
    /**
     * Indicates whether this index enforces uniqueness
     * 
     * @return true if index enforces uniqueness, false otherwise
     */
    public boolean isUnique();
    
    /**
     * Gets sort order, if any, used in this index
     *
     * @return String indicating sort order; 'A' for ascending, 'D' for 
     * descending, null if no sort order is established
     */
    public String getSortSequence();

    /**
     * Gets cardinality of this index
     *
     * @return value representing cardinality of the index
     */
    public int getCardinality();
    
    /**
     * Gets read-only List of Strings (in sequential order) representing names 
     * of indexed columns.
     *
     * @return List of names of indexed columns
     */
    public List<String> getColumnNames();
    
    /**
     * Gets count of indexed columns.
     *
     * @return column count
     */
    public int getColumnCount();
    
    /**
     * Gets ordinal (base-one) sequence of the given DBColumn in this Index, 
     * provided it exists.  The return value ranges from 1 (first column) to n, 
     * where n is the total number of columns indexed, or -1 if col 
     * is not indexed by this instance.
     *
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the 
     * first in a composite index; -1 if col is not indexed by this instance
     */
    public int getSequence(DBColumn col);
    
    /**
     * Gets name of the column positioned as the iColumn-th column, if any, 
     * indexed by this Index.  iColumn ranges from 1 (first column)
     * to n, where n is the total number of columns indexed.
     *
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th indexed column, or null if no column exists 
     * at the given position.
     */
    public String getColumnName(int iColumn);
    
    /**
     * Indicates whether the column represented by the given columnName 
     * is indexed by this instance.
     *
     * @param columnName name of column to test
     * @return true if columnName is indexed by this instance, false otherwise.
     */
    public boolean contains(String columnName);

    /**
     * Indicates whether this column is indexed by this instance.
     *
     * @param col DBColumn to test
     * @return true if columnName is indexed by this instance, false otherwise.
     */
    public boolean contains(DBColumn col);
}

