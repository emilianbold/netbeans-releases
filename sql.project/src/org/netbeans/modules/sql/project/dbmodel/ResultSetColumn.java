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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;

/**
 * Class to hold resultset column metadata.
 *
 * @author Susan Chen
 * @version 
 */
public class ResultSetColumn {
    private String name = "";           // name of parameter
    private String javaType;            // Java type - ex. java.lang.String
    private String sqlType;             // SQL type - ex. BIGINT, NUMERIC
    private int ordinalPosition;        // ordinal position
    private int numericPrecision;       // numeric precision
    private int numericScale;           // numeric scale
    private boolean isNullable;         //specifies if the parameter is nullable
    private String label = "";          // title of column in resultset

    /** 
     * Creates a new instance of ResultSetColumn.
     */
    public ResultSetColumn() {
        name = "";
        javaType = "";
        sqlType = "";
        ordinalPosition = 0;
        numericPrecision = 0;
        numericScale = 0;
        isNullable = false;
    }
    
    public ResultSetColumn(ResultSetColumn rs) {
        name = rs.getName();
        javaType = rs.getJavaType();
        sqlType = rs.getSqlType();
        ordinalPosition = rs.getOrdinalPosition();
        numericPrecision = rs.getNumericPrecision();
        numericScale = rs.getNumericScale();
        isNullable = rs.getIsNullable();
        label = rs.getLabel();
    }

    /**
     * Creates a new instance of ResultSetColumn with the given name.
     *
     * @param newName ResultSetColumn name
     */
    public ResultSetColumn(String newName) {
        name = newName;
    }
    
    /**
     * Creates a new instance of ResultSetColum with the given attributes.
     *
     * @param newName ResultSetColumn name
     * @param newJavaType Java type
     */
    public ResultSetColumn(String newName, String newJavaType) {
        name = newName;
        javaType = newJavaType;
    }
    
    /**
     * Creates a new instance of ResultSetColum with the given attributes.
     *
     * @param newName ResultSetColumn name
     * @param newJavaType Java type
     * @param newOrdinalPosition Ordinal position
     * @param newNumericPrecision Numeric precision
     * @param newNumericScale Numeric scale
     * @param newIsNullable Nullable flag
     */    
    public ResultSetColumn(String newName, String newJavaType, int newOrdinalPosition, 
        int newNumericPrecision, int newNumericScale, boolean newIsNullable) {
        name = newName;
        javaType = newJavaType;
        ordinalPosition = newOrdinalPosition;
        numericPrecision = newNumericPrecision;
        numericScale = newNumericScale;
        isNullable = newIsNullable;
    }
    
    /**
     * Get the ResultSet column name.
     *
     * @return ResultSet column name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the Java type.
     *
     * @return Java type
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * Get the SQL type.
     *
     * @return SQL type
     */
    public String getSqlType() {
        return sqlType;
    }

    /**
     * Get the ResultSet column ordinal position.
     *
     * @return ResultSet column ordinal position
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }
    
    /**
     * Get the ResultSet column numeric precision.
     *
     * @return ResultSet column numeric precision
     */
    public int getNumericPrecision() {
        return numericPrecision;
    }

    /**
     * Get the ResultSet column numeric scale.
     *
     * @return ResultSet column numeric scale
     */
    public int getNumericScale() {
        return numericScale;
    }

    /**
     * Get the ResultSet column nullable flag.
     *
     * @return ResultSet column nullable flag.
     */
    public boolean getIsNullable() {
        return isNullable;
    }
    
    /**
     * Set the ResultSet column name.
     *
     * @param newName ResultSet column name
     */
    public void setName(String newName) {
        name = newName;
    }
   
    /**
     * Set the ResultSet column Java type.
     *
     * @param newJavaType ResultSet column Java type.
     */
    public void setJavaType(String newJavaType) {
        javaType = newJavaType;
    }

    /**
     * Set the ResultSet column SQL type.
     *
     * @param newSqlType ResultSet column SQL type.
     */
    public void setSqlType(String newSqlType) {
        sqlType = newSqlType;
    }
    
    /**
     * Set the ResultSet column ordinal position.
     *
     * @param newOrdinalPosition ResultSet column ordinal position.
     */    
    public void setOrdinalPosition(int newOrdinalPosition) {
        ordinalPosition = newOrdinalPosition;
    }    
    
    /**
     * Set the ResultSet column numeric precision.
     *
     * @param newNumericPrecision ResultSet column numeric precision.
     */     
    public void setNumericPrecision(int newNumericPrecision) {
        numericPrecision = newNumericPrecision;
    }
    
    /**
     * Set the ResultSet column numeric scale.
     *
     * @param newNumericScale ResultSet column numeric scale.
     */ 
    public void setNumericScale(int newNumericScale) {
        numericScale = newNumericScale;
    } 
    
    /**
     * Set the ResultSet column nullable flag.
     *
     * @param newIsNullable ResultSet column nullable flag
     */    
    public void setIsNullable(boolean newIsNullable) {
        isNullable = newIsNullable;
    }
    /**
     * Get the ResultSet column label.
     *
     * @return ResultSet column label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * Set the ResultSet column label.
     *
     * @param newName ResultSet column label
     */
    public void setLabel(String newName) {
        label = newName;
    }
}

