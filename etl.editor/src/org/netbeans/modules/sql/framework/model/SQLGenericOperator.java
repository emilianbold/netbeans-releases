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
 * Model for operators supported by SQLBuilder
 * 
 * @author Ritesh Adval, Sudhi Seshahcala
 * @version $Revision$
 */
public interface SQLGenericOperator extends SQLOperator, SQLCanvasObject {

    // attribute name for parenthesis
    public static final String ATTR_PARENTHESIS = "parenthesis";

    /**
     * Indicates weather this operator has variable number of arguments.
     * 
     * @return true if operator has variable number of arguments; else, false.
     */
    public boolean hasVariableArgs();

    /**
     * check if operator is an aggregate function
     * 
     * @return bool
     */
    public boolean isAggregateFunction();

    /**
     * Determines if input referenced by the given argument name can received a link from
     * the given SQLObject without breaking type casting rules.
     * 
     * @param argName name of the operator input to which the source operator is being
     *        connected.
     * @param input SQLObject to which input argument is being connected.
     * @return true if 'argName' can be connected to input, false otherwise
     */
    public int isCastable(String argName, SQLObject input);

}

