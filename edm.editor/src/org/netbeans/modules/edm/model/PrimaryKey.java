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
 * Interface describing primary-key metadata for data sources providing information 
 * in a database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface PrimaryKey extends Cloneable {

    /**
     * Gets (optional) name of this primary key.
     *
     * @return name of PK, or null if none was defined
     */
    public String getName();
    
    /**
     * Gets reference to DBTable that owns this primary key.
     *
     * @return parent DBTable
     */
    public DBTable getParent();
    
    /**
     * Gets read-only List of Strings (in key sequence order) 
     * representing names of columns referenced in this PrimaryKey.
     *
     * @return List of ColumnReference instances
     */
    public List<String> getColumnNames();
    
    /**
     * Gets count of columns participating in this PrimaryKey.
     *
     * @return column count
     */
    public int getColumnCount();
    
    /**
     * Gets ordinal (base-one) sequence of the column referenced by the given
     * columnName in this PrimaryKey, provided the column is actually part of 
     * this PK.  The return value ranges from 1 (first column) to n, where 
     * n is the total number of columns in this PrimaryKey, or -1 if the
     * column referenced by given columnName is not part of this PrimaryKey.
     *
     * @param columnName name of column whose sequence is requested
     * @return ordinal sequence of column referenced by columnName, starting 
     * with 1 if the column is the first in a composite key; -1 if the column 
     * is not part of this PrimaryKey
     */
    public int getSequence(String columnName);
    
    /**
     * Gets ordinal (base-one) sequence of the given DBColumn, provided it is
     * participating in this PK.  The return value ranges from 1 (first column)
     * to n, where n is the total number of columns in this PrimaryKey, or
     * -1 if col is not part of the PrimaryKey.
     *
     * @param col DBColumn whose sequence is requested
     * @return ordinal sequence of col, starting with 1 if the column is the 
     * first in this (composite) key; -1 if col does not participate in this 
     * PrimaryKey
     */
    public int getSequence(DBColumn col);
    
    /**
     * Gets name of the DBColumn positioned as the iColumn-th column, if any, 
     * participating in this PrimaryKey.  iColumn ranges from 1 (first column)
     * to n, where n is the total number of columns in this PrimaryKey.
     *
     * @param iColumn index of column whose name is requested
     * @return name of iColumn-th DBColumn in this PrimaryKey, or null if no 
     * column exists at the given position.
     */
    public String getDBColumnName(int iColumn);
    
    /**
     * Indicates whether this primary key contains the column represented by
     * the given name.
     *
     * @param columnName name of column to test
     * @return true if this PrimaryKey contains the column referenced by 
     * columnName, false otherwise.
     */
    public boolean contains(String columnName);
    
    /**
     * Indicates whether this primary key contains the given column.
     *
     * @param col DBColumn to test
     * @return true if this PrimaryKey contains col, false otherwise
     */
    public boolean contains(DBColumn col);
    
    /**
     * Indicates whether this PrimaryKey is referenced by the given ForeignKey.
     *
     * @param fk ForeignKey whose references are to be tested.
     * @return true if fk references this PrimaryKey, false otherwise.
     */
    public boolean isReferencedBy(ForeignKey fk);
}

