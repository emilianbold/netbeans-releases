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
 * Class to hold parameter metadata.
 *
 * @author Susan Chen
 * @version 
 */
public class Parameter {
    private String name = "";           // name of parameter
    private String javaType;            // Java type - ex. java.lang.String
    private String sqlType;             // SQL type - ex. BIGINT, NUMERIC
    private String paramType;           // parameter type description: IN, INOUT, OUT, RETURN, RESULT
    private int ordinalPosition;        // ordinal position
    private int numericPrecision;       // numeric precision
    private int numericScale;           // numeric scale
    private boolean isNullable;         //specifies if the parameter is nullable

    /** 
     * Creates an instance of Paramter.
     */
    public Parameter() {
        name = "";
        javaType = "";
        sqlType = "";
        paramType = "";
        ordinalPosition = 0;
        numericPrecision = 0;
        numericScale = 0;
        isNullable = false;
    }
    
    /**
     * Creates an instance of Parameter with the given name.
     *
     * @param newName Parameter name
     */
    public Parameter(String newName) {
        name = newName;
    }
    
    /**
     * Creates an instance of Parameter with the given name and java type.
     *
     * @param newName Parameter name
     * @param newJavaType Java type
     */    
    public Parameter(String newName, String newJavaType) {
        name = newName;
        javaType = newJavaType;
    }
    
    /**
     * Creates an instance of Parameter with the given attributes.
     *
     * @param newName Parameter name
     * @param newJavaType Java type
     * @param newParamType Parameter type
     * @param newOrdinalPosition Ordinal position
     * @param newNumericPrecision Numeric precision
     * @param newNumericScale Numeric scale
     * @param newIsNullable Nullable flag
     */
    public Parameter(String newName, String newJavaType, String newParamType, int newOrdinalPosition, 
        int newNumericPrecision, int newNumericScale, boolean newIsNullable) {
        name = newName;
        javaType = newJavaType;
        paramType = newParamType;
        ordinalPosition = newOrdinalPosition;
        numericPrecision = newNumericPrecision;
        numericScale = newNumericScale;
        isNullable = newIsNullable;
    }

    public Parameter(Parameter p) {
        name = p.getName();
        javaType = p.getJavaType();
        sqlType = p.getSqlType();
        paramType = p.getParamType();
        ordinalPosition = p.getOrdinalPosition();
        numericPrecision = p.getNumericPrecision();
        numericScale = p.getNumericScale();
        isNullable = p.getIsNullable();
    }

    /**
     * Get the parameter name.
     *
     * @return parameter name
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
     * Get the parameter type.
     *
     * @return Parameter type
     */
    public String getParamType() {
        return paramType;
    }
    
    /**
     * Get the parameter ordinal position.
     *
     * @return Parameter ordinal position
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }
 
    /**
     * Get the parameter numeric precision.
     *
     * @return Parameter numeric precision.
     */
    public int getNumericPrecision() {
        return numericPrecision;
    }

    /**
     * Get the parameter numeric scale.
     *
     * @return Parameter numeric scale.
     */
    public int getNumericScale() {
        return numericScale;
    }

    /**
     * Get the parameter nullable flag.
     *
     * @return Parameter nullable flag.
     */
    public boolean getIsNullable() {
        return isNullable;
    }
    
    /**
     * Set the parameter name.
     *
     * @param newName Parameter name
     */
    public void setName(String newName) {
        name = newName;
    }
    
    /**
     * Set the parameter Java type.
     *
     * @param newJavaType Parameter Java type.
     */    
    public void setJavaType(String newJavaType) {
        javaType = newJavaType;
    }

    /**
     * Set the parameter SQL type.
     *
     * @param newSqlType Parameter SQL type.
     */
    public void setSqlType(String newSqlType) {
        sqlType = newSqlType;
    }
    
    /**
     * Set the parameter type.
     *
     * @param newParamType Parameter type.
     */
    public void setParamType(String newParamType) {
        paramType = newParamType;
    }
    
    /**
     * Set the parameter ordinal position.
     *
     * @param newOrdinalPosition Parameter ordinal Position.
     */
    public void setOrdinalPosition(int newOrdinalPosition) {
        ordinalPosition = newOrdinalPosition;
    }    

    /**
     * Set the parameter numeric position.
     *
     * @param newNumericPrecision Parameter numeric precision
     */
    public void setNumericPrecision(int newNumericPrecision) {
        numericPrecision = newNumericPrecision;
    }

    /**
     * Set the parameter numeric scale.
     *
     * @param newNumericScale Parameter numeric scale
     */
    public void setNumericScale(int newNumericScale) {
        numericScale = newNumericScale;
    } 
    
    /**
     * Set the parameter nullable flag.
     *
     * @param newIsNullable Parameter nullable flag
     */
    public void setIsNullable(boolean newIsNullable) {
        isNullable = newIsNullable;
    }
    
    /*public int getAccessType() {
        if (getParamType().equals("IN")) {
            return OtdLeaf.Access.WRITE;
        } 
        if (getParamType().equals("INOUT")) {
            return OtdLeaf.Access.MODIFY;
        } 
        return OtdLeaf.Access.READ;
    }*/    
}
