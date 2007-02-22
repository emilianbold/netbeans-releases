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

package org.netbeans.modules.xml.xpath.impl;


import java.util.Iterator;

import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitable;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;


/**
 * Represents a core XPath operation.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public class XPathCoreOperationImpl
    extends XPathOperatorOrFunctionImpl
    implements XPathCoreOperation {
        
    /** The operator code. */
    int mOperator;
    
    
    /**
     * Constructor. Instantiates a new XPathCoreOperation with the given code.
     * @param operator the operator code
     */
    public XPathCoreOperationImpl(int operator) {
        super();
        setOperator(operator);
    }
    
    
    /**
     * Gets the operator code.
     * @return the operator code
     */
    public int getOperator() {
        return mOperator;
    }
    
    
    /**
     * Sets the operator code.
     * @param operator the operator code
     */
    public void setOperator(int operator) {
        mOperator = operator;
    }
    
    
    /**
     * Gets the name of the operator.
     * @return the operator name
     */
    public String getName() {
        int code = getOperator();

        switch (code) {
        case XPathCoreOperation.OP_SUM:
            return "addition";
        case XPathCoreOperation.OP_MINUS:
            return "subtraction";
        case XPathCoreOperation.OP_MULT:
            return "multiplication";
        case XPathCoreOperation.OP_DIV:
            return "division";
        case XPathCoreOperation.OP_MOD:
            return "remainder";
        case XPathCoreOperation.OP_NEGATIVE:
            return "negative";
        case XPathCoreOperation.OP_AND:
            return "and";
        case XPathCoreOperation.OP_OR:
            return "or";
        case XPathCoreOperation.OP_EQ:
            return "equal";
        case XPathCoreOperation.OP_NE:
            return "not_equal";
        case XPathCoreOperation.OP_LT:
            return "lesser_than";
        case XPathCoreOperation.OP_LE:
            return "lesser_or_equal";
        case XPathCoreOperation.OP_GT:
            return "greater_than";
        case XPathCoreOperation.OP_GE:
            return "greater_or_equal";
        }

        return null;
    }
    
    
    /**
     * Gets the operator sign.
     * @return the operator sign
     */
    public String getSign() {
        int code = getOperator();

        switch (code) {
        case XPathCoreOperation.OP_SUM:
            return "+";
        case XPathCoreOperation.OP_MINUS:
            return "-";
        case XPathCoreOperation.OP_MULT:
            return "*";
        case XPathCoreOperation.OP_DIV:
            return "div";
        case XPathCoreOperation.OP_MOD:
            return "mod";
        case XPathCoreOperation.OP_NEGATIVE:
            return "-";
        case XPathCoreOperation.OP_AND:
            return "and";
        case XPathCoreOperation.OP_OR:
            return "or";
        case XPathCoreOperation.OP_EQ:
            return "=";
        case XPathCoreOperation.OP_NE:
            return "!=";
        case XPathCoreOperation.OP_LT:
            return "<";
        case XPathCoreOperation.OP_LE:
            return "<=";
        case XPathCoreOperation.OP_GT:
            return ">";
        case XPathCoreOperation.OP_GE:
            return ">=";
        }

        return null;
    }

    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
}
