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

package org.netbeans.modules.xml.xpath;

/**
 * Represents a core XPath operation.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public interface XPathCoreOperation extends XPathOperationOrFuntion {
    
    /** Operator code: sum */
    public static final int OP_SUM = 1;
    
    /** Operator code: minus */
    public static final int OP_MINUS = 2;
    
    /** Operator code: multiplication */
    public static final int OP_MULT = 3;
    
    /** Operator code: division */
    public static final int OP_DIV = 4;
    
    /** Operator code: mod */
    public static final int OP_MOD = 5;
    
    /** Operator code: negative/unary minus */
    public static final int OP_NEGATIVE = 6;
    
    /** Operator code: and */
    public static final int OP_AND = 7;
    
    /** Operator code: or */
    public static final int OP_OR = 8;
    
    /** Operator code: equal */
    public static final int OP_EQ = 9;
    
    /** Operator code: not equal */
    public static final int OP_NE = 10;
    
    /** Operator code: less than */
    public static final int OP_LT = 11;
    
    /** Operator code: less than or equal */
    public static final int OP_LE = 12;
    
    /** Operator code: greater than */
    public static final int OP_GT = 13;
    
    /** Operator code: greater than or equal */
    public static final int OP_GE = 14;
    
    
    /**
     * Gets the operator code.
     * @return the operator code
     */
    int getOperator();
    
    
    /**
     * Sets the operator code.
     * @param operator the operator code
     */
    void setOperator(int operator);
    
    
    
    /**
     * Gets the operator sign.
     * @return the operator sign
     */
    String getSign();
}
