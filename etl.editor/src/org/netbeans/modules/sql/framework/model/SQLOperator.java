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

import java.util.List;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;

import com.sun.sql.framework.exception.BaseException;

/**
 * Common interface for generic operator and predicate.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface SQLOperator extends SQLConnectableObject {
    public static final String ATTR_CUSTOM_OPERATOR = "customOperator";

    public static final String ATTR_CUSTOM_OPERATOR_NAME = "customOperatorName";
    /* XML attribute: script ref */
    public static final String ATTR_SCRIPTREF = "scriptRef";

    public Object getArgumentValue(String argName) throws BaseException;

    /**
     * Returns name of the User specific operator.
     * 
     * @return
     */
    public String getCustomOperatorName();

    /**
     * Get the script of this operator.
     * 
     * @return Return script of this operator.
     */
    public SQLOperatorDefinition getOperatorDefinition();

    /**
     * Gets canonical operator type, e.g., "concat", "tolowercase", etc..
     * 
     * @return canonical operator name
     */
    public String getOperatorType();

    public IOperatorXmlInfo getOperatorXmlInfo();

    /**
     * Returns True if operator represents user specific operator else false.
     * 
     * @return
     */
    public boolean isCustomOperator();

    /**
     * Indicates whether open and close parentheses should be appended upon evaluation of
     * this operator.
     * 
     * @return true if parentheses are to be appended, false otherwise
     */
    public boolean isShowParenthesis();

    public void setArgument(String argName, Object val) throws BaseException;

    public void setArguments(List args) throws BaseException;

    /**
     * Sets whether this object represents user specific operator.
     * @param customOperator 
     */
    public void setCustomOperator(boolean customOperator);

    /**
     * Sets the name of this user specific operator. Which is also used to evaluate.
     * @param customOperatorName 
     */
    public void setCustomOperatorName(String customOperatorName);

    public void setDbSpecificOperator(String dbName) throws BaseException;

    /**
     * sets canonical operator type, e.g., "concat", "tolowercase", etc..
     * 
     * @param opName canonical operator name
     * @throws com.sun.sql.framework.exception.BaseException 
     */
    public void setOperatorType(String opName) throws BaseException;

    public void setOperatorXmlInfo(IOperatorXmlInfo opInfo) throws BaseException;

    /**
     * Sets whether parentheses needs to be appended upon evaluation of this operator.
     * 
     * @param show true if parentheses are to be appended, false otherwise
     */
    public void setShowParenthesis(boolean show);
}
