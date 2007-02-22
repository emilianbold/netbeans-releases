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

import org.apache.commons.jxpath.ri.compiler.VariableReference;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathPredicateNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.function.extension.GetContainerDataFunction;
import org.netbeans.modules.xml.xpath.function.extension.impl.GetContainerDataFunctionImpl;



/**
 * XPathModel helper class.
 *
 * @author Enrico Lelina
 * @version $Revision$
 */
public class XPathModelHelperImpl extends AbstractXPathModelHelper {

    /**
     * Instantiates a new XPathModel object.
     * @return a new XPathModel object instance
     */
    public XPathModel newXPathModel() {
        return new XPathModelImpl();
    }
    
    
    /**
     * Instantiates a new XPathStringLiteral object.
     * @param value the value
     * @return a new XPathStringLiteral object instance
     */
    public XPathStringLiteral newXPathStringLiteral(String value) {
        return new XPathStringLiteralImpl(value);
    }
   
    /**
     * Instantiates a new XPathVariableReference object of the type variable.
     * @param value the value
     * @return a new XPathVariableReference object instance
     */    
    public XPathVariableReference newXPathVariableReference(VariableReference vReference) {

        return new XPathVariableReferenceImpl(vReference);
    }

    
    /**
     * Instantiates a new XPathPredicateExpression object for given expression.
     * @param expression which is a predicate expression
     * @return a new XPathPredicateExpression object instance
     */
    public XPathPredicateExpression newXPathPredicateExpression(XPathExpression expression) {
		return new XPathPredicateExpressionImpl(expression);
	}


	/**
     * Instantiates a new XPathNumericLiteral object.
     * @param value the value
     * @return a new XPathNumericLiteral object instance
     */
    public XPathNumericLiteral newXPathNumericLiteral(Number value) {
        return new XPathNumericLiteralImpl(value);
    }
    
   /**
     * Instantiates a new XPathPredicateNumericLiteral object.
     * @param value the value
     * @return a new XPathPredicateNumericLiteral object instance
     */
    public XPathPredicateNumericLiteral newXPathPredicateNumericLiteral(Long value) {
        return new XPathPredicateNumericLiteralImpl(value);
    }
        
    /**
     * Instantiates a new XPathCoreFunction object.
     * @param function the function code
     * @return a new XPathCoreFunction object instance
     */
    public XPathCoreFunction newXPathCoreFunction(int function) {
        return new XPathCoreFunctionImpl(function);
    }
    
    
    /**
     * Instantiates a new XPathExtension Function object.
     * @param name the function name
     * @return a new XPathExtensionFunction object instance
     */
    public XPathExtensionFunction newXPathExtensionFunction(
                                                            String name) {
    	if(name != null && name.equals(GetContainerDataFunction.NAME)) {
    		return new GetContainerDataFunctionImpl(name);
    	} else {
    		return new XPathExtensionFunctionImpl(name);
    	}
    }
    
    
    /**
     * Instantiates a new XPathCoreOperation object.
     * @param code the operation code
     * @return a new XPathCoreOperatoin object instance
     */
    public XPathCoreOperation newXPathCoreOperation(int code) {
        return new XPathCoreOperationImpl(code);
    }
    
    
    /**
     * Instantiates a new XPathLocationPath object.
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    public XPathLocationPath newXPathLocationPath(LocationStep[] steps) {
        return new XPathLocationPathImpl(steps);
    }

    /**
     * Instantiates a new XPathExpressionPath object.
     * @param rootExpression root expression if any
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    public XPathExpressionPath newXPathExpressionPath(XPathExpression rootExpression, 
    												 	   LocationStep[] steps) {
    	
    	return new XPathExpressionPathImpl(rootExpression, steps, false);
    }
    
    /**
     * Determines if a function name is valid. Assumes the function name is
     * not one of the core functions.
     * @param functionName the name of the function
     * @return true if the function name is valid, false otherwise
     */
    public boolean isValidFunction(String functionName) {
        return XPathModelImpl.isValidFunction(functionName);
    }
    
    /**
     * Determines if a operator name is valid. Assumes the operatior name is
     * built in operator.
     * @param operatorName the name of the function
     * @return true if the operatorName name is valid, false otherwise
     */
    public boolean isValidOperator(String operatorName) {
        if(opHash != null && opHash.keySet().contains(operatorName)) {
            return true;
        }
        
        return false;
    }
}
