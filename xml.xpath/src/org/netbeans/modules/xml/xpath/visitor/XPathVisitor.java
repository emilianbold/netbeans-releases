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

package org.netbeans.modules.xml.xpath.visitor;

import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.XPathVariableReference;

/**
 * Visitor interface.
 * 
 * @author sbyn
 * @version $Revision$
 */
public interface XPathVisitor {

	/**
     * Visits an location step.
     * @param locationStep to visit
     */
    void visit(LocationStep locationStep);
    
    /**
     * Visits a string literal.
     * @param stringLiteral to visit
     * @return must be false since string literals don't have children
     */
    void visit(XPathStringLiteral stringLiteral);
    
    
    /**
     * Visits a numeric literal.
     * @param numericLiteral to visit
     */
    void visit(XPathNumericLiteral numericLiteral);
    
    
    /**
     * Visits a location path.
     * @param locationPath to visit
     */
    void visit(XPathLocationPath locationPath);
    
    /**
     * Visits a expression path.
     * @param expressionPath to visit
     */
    void visit(XPathExpressionPath expressionPath);
    
    
    /**
     * Visits a core operation.
     * @param coreOperation to visit
     */
    void visit(XPathCoreOperation coreOperation);
    
    
    /**
     * Visits a core function.
     * @param coreFunction to visit
     */
    void visit(XPathCoreFunction coreFunction);
    
    
    /**
     * Visits an extension function.
     * @param extensionFunction to visit
     */
    void visit(XPathExtensionFunction extensionFunction);
    
    /**
     * Visits a Variable
     * @param vReference
     */
    void visit(XPathVariableReference vReference);
    
    /**
     * visit a predicate (predicates are inside [] in a location/expression path)
     * @param predicate
     */
    void visit(XPathPredicateExpression predicate);
    
}
