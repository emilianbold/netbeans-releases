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

package org.netbeans.modules.xml.xpath.ext;

import javax.xml.namespace.QName;


/**
 * XPathModel helper class.
 *
 * @author Enrico Lelina
 * @version
 */
public interface XPathModelFactory {

    /**
     * Instantiates a new XPathStringLiteral object.
     * @param value the value
     * @return a new XPathStringLiteral object instance
     */
    XPathStringLiteral newXPathStringLiteral(String value);

    /**
     * Instantiates a new XPathNumericLiteral object.
     * @param value the value
     * @return a new XPathNumericLiteral object instance
     */
    XPathNumericLiteral newXPathNumericLiteral(Number value);

    /**
     * Instantiates a new XPathCoreFunction object.
     * @param function the function code
     * @return a new XPathCoreFunction object instance
     */
    XPathCoreFunction newXPathCoreFunction(CoreFunctionType functionType);

    /**
     * Instantiates a new XPathExtension Function object.
     * @param name the function name. It can contain a prefix.
     * @return a new XPathExtensionFunction object instance
     */
    XPathExtensionFunction newXPathExtensionFunction(QName name);

    /**
     * Instantiates a new XPathCoreOperation object.
     * @param code the operation code
     * @return a new XPathCoreOperatoin object instance
     */
    XPathCoreOperation newXPathCoreOperation(CoreOperationType code);

    /**
     * Instantiates a new XPathLocationPath object.
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    XPathLocationPath newXPathLocationPath(LocationStep[] steps);

    /**
     * Instantiates a new XPathExpressionPath object.
     * @param rootExpression root expression if any
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    XPathExpressionPath newXPathExpressionPath(
            XPathExpression rootExpression, LocationStep[] steps);

    /**
     * Instantiates a new XPathStringLiteral object of the variable type.
     * @param steps the steps
     * @return a new XPathStringLiteral object instance
     */
    XPathVariableReference newXPathVariableReference(QName varQName);

    /**
     * Instantiates a new XPathPredicateExpression object for given expression.
     * @param expression which is a predicate expression
     * @return a new XPathPredicateExpression object instance
     */
    XPathPredicateExpression newXPathPredicateExpression(XPathExpression expression);

    LocationStep newLocationStep(XPathAxis axis, StepNodeTest nodeTest, 
            XPathPredicateExpression[] predicates);
    
}
