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

/**
 * Interface describing column metadata for data sources providing information in a 
 * database or database-like format.  Implementing classes must support the 
 * Cloneable interface.
 *
 * @author   Sudhendra Seshachala, Jonathan Giron
 */
public interface DBColumn extends Cloneable, Comparable {
    /**
     * Gets the column name
     *
     * @return column name
     */
    public String getName() ;
    
    /**
     * Indicates whether this column is part of a primary key.
     *
     * @return true if this column is part of a primary key; false otherwise.
     */
    public boolean isPrimaryKey() ;

    /**
     * Indicates whether this column is part of a foreign key.
     *
     * @return true if this column is part of a foreign key; false otherwise.
     */
    public boolean isForeignKey();
    
    /**
     * Indicates whether this column is indexed.
     *
     * @return true if this column is indexed; false otherwise
     */
    public boolean isIndexed();
    
    /**
     * Indicates whether this column can accept a null value.
     *
     * @return true if null is a valid value for this column, false otherwise.
     */
    public boolean isNullable();
    
    /**
     * Gets the parent/owner (DBTable) of this column
     *
     * @return DBTable containing this column
     */
    public DBTable getParent();
       
    /**
     * Gets the JDBC datatype for this column, as selected from the enumerated 
     * types in java.sql.Types.
     *
     * @return JDBC type value
     * @see java.sql.Types
     */
    public int getJdbcType();

    /**
     * Gets the JDBC datatype for this column, as a human-readable String.
     *
     * @return JDBC type value as a String
     */
    public String getJdbcTypeString();
    
    /**
     * Gets the scale attribute of this column.
     *
     * @return scale
     */
    public int getScale();
    
    /**
     * Gets the precision attribute of this column.
     *
     * @return precision
     */
    public int getPrecision();
    
    /**
     * Gets the default value
     * @return defaultValue
     */
    public String getDefaultValue();
    
    /**
     * Gets the Ordinal Position
     * @return int
     */
    public int getOrdinalPosition();
}

