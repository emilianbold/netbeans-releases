/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.xpath.impl;


import java.util.Iterator;

import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitable;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;


/**
 * Represents a core XPath operation.
 * 
 * @author Enrico Lelina
 * @version 
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
