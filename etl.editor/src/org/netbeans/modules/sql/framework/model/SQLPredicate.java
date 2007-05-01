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
 * Represents boolean conditional expressions for join, case, etc.,
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface SQLPredicate extends SQLOperator {

    /** Name of XML element tag for operator */
    public static final String ATTR_OPERATOR = "operator";

    // attribute name for parenthesis
    public static final String ATTR_PARENTHESIS = "parenthesis";

    /** Constant: left */
    public static final String LEFT = "left";

    /** Hashtable key constant for getXXXLinearForm methods: operator */
    public static final String OPERATOR = "operator";

    /** Constant: prefix */
    public static final String PREFIX = "prefix";

    /** Constant: right */
    public static final String RIGHT = "right";

    /**
     * method getRoot gets the root SQLPredicate.
     * 
     * @return SQLPredicate of the root.
     */
    public SQLPredicate getRoot();

    /**
     * method setRoot sets the root SQLPredicate.
     * 
     * @param pred is the SQLPredicate of the new root.
     */
    public void setRoot(SQLPredicate pred);

}

