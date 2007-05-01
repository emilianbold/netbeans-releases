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
 * Represents boolean conditional expression for a source table filter.
 * 
 * @author Wei
 * @version $Revision$
 */
public interface SQLFilter extends SQLConnectableObject {
    /** Name of XML element tag for operator */
    public static final String ATTR_FILTER_TYPE = "filterType"; // NOI18N

    /** Name of XML element tag for operator */
    public static final String ATTR_OPERATOR = "operator"; // NOI18N

    /** filter type */
    public static final int EXTRACTION = 1;

    /** Constant: left */
    public static final String LEFT = "left"; // NOI18N

    /** filter type */
    public static final int NORMAL = 0;

    /** Hashtable key constant for getXXXLinearForm methods: operator */
    public static final String OPERATOR = "operator"; // NOI18N

    /** Constant: prefix */
    public static final String PREFIX = "prefix"; // NOI18N

    /** Constant: right */
    public static final String RIGHT = "right"; // NOI18N

    /** XML element tag name: next */
    public static final String TAG_NEXT = "next"; // NOI18N

    /**
     * Get filter type
     * 
     * @return type
     */
    public int getFilterType();

    /**
     * Gets chained instance of SQLFilter, if any.
     * 
     * @return next SQLFilter, or null if no chained instance exists.
     */
    public SQLFilter getNextFilter();

    /**
     * Gets current operator.
     * 
     * @return operator String
     */
    public String getOperator();

    /**
     * Gets (optional) prefix associated with this filter.
     * 
     * @return prefix string, possibly null
     */
    public String getPrefix();

    /**
     * Check if is valid
     * 
     * @return true/false
     */
    public boolean isValid();

    /**
     * Set filter type
     * 
     * @param fType
     */
    public void setFilterType(int fType);

    /**
     * Sets next filter to the given SQLFilter instance.
     * 
     * @param newNext new chained instance of SQLFilter, possibly null
     */
    public void setNextFilter(SQLFilter newNext);

    /**
     * Sets operator to given String.
     * 
     * @param newOperator new operator String
     */
    public void setOperator(String newOperator);

    /**
     * Sets (optional) prefix associated with this filter.
     * 
     * @param newPrefix new prefix string, possibly null
     */
    public void setPrefix(String newPrefix);

}

