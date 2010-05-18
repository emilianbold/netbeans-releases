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

package org.netbeans.modules.xml.xpath;

import java.util.HashMap;

import org.apache.commons.jxpath.ri.compiler.VariableReference;


/**
 * XPathModel helper class.
 *
 * @author Enrico Lelina
 * @version 
 */
public abstract class AbstractXPathModelHelper {

    /**
     * Describe variable <code>opHash</code> here.
     *
     */
    protected static HashMap opHash = new HashMap();
    /**
     * Describe variable <code>funcHash</code> here.
     *
     */
    private static HashMap funcHash = new HashMap();

    static {
        opHash.put("+", new Integer(XPathCoreOperation.OP_SUM));
        opHash.put("-", new Integer(XPathCoreOperation.OP_MINUS));
        opHash.put("*", new Integer(XPathCoreOperation.OP_MULT));
        opHash.put("div", new Integer(XPathCoreOperation.OP_DIV));
        opHash.put("mod", new Integer(XPathCoreOperation.OP_MOD));
        // **FIX*        opHash.put("", new Integer(XPathCoreOperation.OP_NEGATIVE));
        opHash.put("negative", new Integer(XPathCoreOperation.OP_NEGATIVE));
        opHash.put("and", new Integer(XPathCoreOperation.OP_AND));
        opHash.put("or", new Integer(XPathCoreOperation.OP_OR));
        opHash.put("==", new Integer(XPathCoreOperation.OP_EQ));
        opHash.put("!=", new Integer(XPathCoreOperation.OP_NE));
        opHash.put("&lt;", new Integer(XPathCoreOperation.OP_LT));
        opHash.put("<", new Integer(XPathCoreOperation.OP_LT));
        opHash.put("&lt;=", new Integer(XPathCoreOperation.OP_LE));
        opHash.put("<=", new Integer(XPathCoreOperation.OP_LE));
        opHash.put("&gt;", new Integer(XPathCoreOperation.OP_GT));
        opHash.put(">", new Integer(XPathCoreOperation.OP_GT));
        opHash.put("&gt;=", new Integer(XPathCoreOperation.OP_GE));
        opHash.put(">=", new Integer(XPathCoreOperation.OP_GE));
    }
    

    static {

        funcHash.put("concat", new Integer(XPathCoreFunction.FUNC_CONCAT));
        funcHash.put("last", new Integer(XPathCoreFunction.FUNC_LAST));
        funcHash.put("position", new Integer(XPathCoreFunction.FUNC_POSITION));
        funcHash.put("count", new Integer(XPathCoreFunction.FUNC_COUNT));
        funcHash.put("id", new Integer(XPathCoreFunction.FUNC_ID));
        funcHash.put("local-name", new Integer(XPathCoreFunction.FUNC_LOCAL_NAME));
        funcHash.put("namespace-uri", new Integer(XPathCoreFunction.FUNC_NAMESPACE_URI));
        funcHash.put("name", new Integer(XPathCoreFunction.FUNC_NAME));
        funcHash.put("string", new Integer(XPathCoreFunction.FUNC_STRING));
        funcHash.put("starts-with", new Integer(XPathCoreFunction.FUNC_STARTS_WITH));
        funcHash.put("contains", new Integer(XPathCoreFunction.FUNC_CONTAINS));
        funcHash.put("substring-before", new Integer(XPathCoreFunction.FUNC_SUBSTRING_BEFORE));
        funcHash.put("substring-after", new Integer(XPathCoreFunction.FUNC_SUBSTRING_AFTER));
        funcHash.put("substring", new Integer(XPathCoreFunction.FUNC_SUBSTRING));
        funcHash.put("string-length", new Integer(XPathCoreFunction.FUNC_STRING_LENGTH));
        funcHash.put("normalize-space", new Integer(XPathCoreFunction.FUNC_NORMALIZE_SPACE));
        funcHash.put("translate", new Integer(XPathCoreFunction.FUNC_TRANSLATE));
        funcHash.put("boolean", new Integer(XPathCoreFunction.FUNC_BOOLEAN));
        funcHash.put("not", new Integer(XPathCoreFunction.FUNC_NOT));
        funcHash.put("true", new Integer(XPathCoreFunction.FUNC_TRUE));
        funcHash.put("false", new Integer(XPathCoreFunction.FUNC_FALSE));
        funcHash.put("lang", new Integer(XPathCoreFunction.FUNC_LANG));
        funcHash.put("number", new Integer(XPathCoreFunction.FUNC_NUMBER));
        funcHash.put("sum", new Integer(XPathCoreFunction.FUNC_SUM));
        funcHash.put("floor", new Integer(XPathCoreFunction.FUNC_FLOOR));
        funcHash.put("ceiling", new Integer(XPathCoreFunction.FUNC_CEILING));
        funcHash.put("round", new Integer(XPathCoreFunction.FUNC_ROUND));
        funcHash.put("null", new Integer(XPathCoreFunction.FUNC_NULL));
        funcHash.put("key", new Integer(XPathCoreFunction.FUNC_KEY));
    }

    /** Singleton Castor support */
    private static AbstractXPathModelHelper mXPathModelHelper = null; 

    /**
     * Get Castor support from Enterprise Designer context.
     * @return AbstractXPathModelHelper    The castor support object.
     */
    public static synchronized AbstractXPathModelHelper getInstance() {
        if (mXPathModelHelper == null) {
            mXPathModelHelper = loadImpl(null);
        }

        return mXPathModelHelper;
    }
    
    /**
     * Get the Castor support using the given Class Loader.
     * @param loader Class Loader that can find the XPathModelHelper 
     * implementation class.
     * @return AbstractCastorSuppport The castor support object.
     */
    public static synchronized AbstractXPathModelHelper getInstance(ClassLoader loader) {
        if (null == mXPathModelHelper) {
            mXPathModelHelper = loadImpl(loader);
        }
        return mXPathModelHelper;
    }

    /** Loads the XPathModelHelper implementation class.
     * @param   loader  ClassLoader to use.
     * @return  XPathModelHelper implementing class.
     */
    private static AbstractXPathModelHelper loadImpl(ClassLoader loader) {
        String implClassName = null;
        AbstractXPathModelHelper axmh = null;
        try {
            implClassName = 
                System.getProperty
                ("org.netbeans.modules.xml.xpath.AbstractXPathModelHelper",
                 "org.netbeans.modules.xml.xpath.impl.XPathModelHelperImpl");
            Class implClass = null;
            if (loader != null) {
                implClass = Class.forName(implClassName, true, loader);
            } else {
                implClass = Class.forName(implClassName);
            }
            axmh = (AbstractXPathModelHelper) implClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find/load " + implClassName, e);
        }
        return axmh;
    }
    
    /**
     * Instantiates a new XPathModel object.
     * @return a new XPathModel object instance
     */
    public abstract XPathModel newXPathModel();
    
    /**
     * Instantiates a new XPathStringLiteral object.
     * @param value the value
     * @return a new XPathStringLiteral object instance
     */
    public abstract XPathStringLiteral newXPathStringLiteral(String value);
    
    /**
     * Instantiates a new XPathNumericLiteral object.
     * @param value the value
     * @return a new XPathNumericLiteral object instance
     */
    public abstract XPathNumericLiteral newXPathNumericLiteral(Number value);

    /**
     * Instantiates a new XPathPredicateNumericLiteral object.
     * @param value the value
     * @return a new XPathPredicateNumericLiteral object instance
     */
    public abstract XPathPredicateNumericLiteral newXPathPredicateNumericLiteral(Long value);
    
    /**
     * Instantiates a new XPathCoreFunction object.
     * @param function the function code
     * @return a new XPathCoreFunction object instance
     */
    public abstract XPathCoreFunction newXPathCoreFunction(int function);
    
    /**
     * Instantiates a new XPathExtension Function object.
     * @param name the function name
     * @return a new XPathExtensionFunction object instance
     */
    public abstract XPathExtensionFunction newXPathExtensionFunction(String name);
    
    /**
     * Instantiates a new XPathCoreOperation object.
     * @param code the operation code
     * @return a new XPathCoreOperatoin object instance
     */
    public abstract XPathCoreOperation newXPathCoreOperation(int code);
    
    /**
     * Instantiates a new XPathLocationPath object.
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    public abstract XPathLocationPath newXPathLocationPath(LocationStep[] steps);

    /**
     * Instantiates a new XPathExpressionPath object.
     * @param rootExpression root expression if any
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    public abstract XPathExpressionPath newXPathExpressionPath(XPathExpression rootExpression, 
    												 	   LocationStep[] steps);

    /**
     * Instantiates a new XPathStringLiteral object of the variable type.
     * @param steps the steps
     * @return a new XPathStringLiteral object instance
     */
    public abstract XPathVariableReference newXPathVariableReference(VariableReference vReference);

    /**
     * Instantiates a new XPathPredicateExpression object for given expression.
     * @param expression which is a predicate expression
     * @return a new XPathPredicateExpression object instance
     */
    public abstract XPathPredicateExpression newXPathPredicateExpression(XPathExpression expression);

    /**
     * gives the type of the core operation given the operator string
     * @param operator String 
     * @return int (type)
     */
    public Integer getOperatorType(String operator) {
        
        return (Integer) opHash.get(operator);
    }

    /**
     * gives the type of the function given the function
     * @param function String 
     * @return int (type)
     */
    public Integer getFunctionType(String function) {
        
        return (Integer) funcHash.get(function);
    }

    /**
     * Determines if a function name is valid. Assumes the function name is
     * not one of the core functions.
     * @param functionName the name of the function
     * @return true if the function name is valid, false otherwise
     */
    public abstract boolean isValidFunction(String functionName);
    
    /**
     * Determines if a operator name is valid. Assumes the operatior name is
     * built in operator.
     * @param operatorName the name of the function
     * @return true if the operatorName name is valid, false otherwise
     */
    public abstract boolean isValidOperator(String operatorName);
}
