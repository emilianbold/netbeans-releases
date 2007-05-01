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
package org.netbeans.modules.sql.framework.model;

/**
 * Holds name and JDBCtype of an argument in SQLOperatorDefinition.
 * 
 * @author Amrish K. Lal
 * @version $Revision$
 */
public final class SQLOperatorArg {

    /* Argument name */
    private String argName = null;

    /* Argument type */
    private int jdbcType = SQLConstants.JDBCSQL_TYPE_UNDEFINED;

    private String range;

    /**
     * Constructs a new instance of SQLOperatorArg with the given name and JDBC typecode.
     * 
     * @param newName argument name
     * @param newType JDBC sql type.
     */
    public SQLOperatorArg(String newName, int newType) {
        argName = newName;
        jdbcType = newType;
    }

    /**
     * Overrides default implementation to account for member variables.
     * 
     * @param refObj to be checked for equality
     * @return true if refObj equals this; false otherwise
     */
    public boolean equals(Object refObj) {
        // First check for reflexivity.
        if (this == refObj) {
            return true;
        }

        // Next check for class type..also checks for null refObj
        if (!(refObj instanceof SQLOperatorArg)) {
            return (false);
        }

        SQLOperatorArg arg = (SQLOperatorArg) refObj;
        if (this.argName != null) {
            if (!this.argName.equals(arg.getArgName())) {
                return false;
            }
        } else if (arg.getArgName() != null) {
            return false;
        }

        return this.jdbcType == arg.getJdbcType();
    }

    /**
     * Gets name of this argument.
     * 
     * @return argument name
     */
    public String getArgName() {
        return (this.argName);
    }

    /**
     * Gets JDBC typecode of this argument.
     * 
     * @return JDBC typecode
     */
    public int getJdbcType() {
        return (this.jdbcType);
    }

    /**
     * Setter for Condition to be evaluated for operators
     * 
     * @return condition to be evaluated
     */
    public String getRange() {
        return this.range;
    }

    /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = (argName != null) ? argName.hashCode() : 0;
        myHash += jdbcType;

        return myHash;
    }

    /**
     * Setter for Condition to be evaluated for operators
     * 
     * @param theRange to be evaluated
     */
    public void setRange(String theRange) {
        this.range = theRange;
    }

}

