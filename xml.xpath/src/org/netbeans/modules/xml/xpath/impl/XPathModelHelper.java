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

import java.util.HashMap;

import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;



/**
 * XPathModel helper class.
 *
 * @author Enrico Lelina
 * @version 
 * @deprecated Replaced by {@link #org.netbeans.modules.xml.xpath.AbstractXPathModelHelper}
 */
public abstract class XPathModelHelper {

    /**
     * Describe variable <code>opHash</code> here.
     *
     */
    private static HashMap opHash = new HashMap();
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


    /**
     * Instantiates a new XPathModel object.
     * @return a new XPathModel object instance
     */
    public static XPathModel newXPathModel() {
        return new XPathModelImpl();
    }
    
    
    /**
     * Instantiates a new XPathStringLiteral object.
     * @param value the value
     * @return a new XPathStringLiteral object instance
     */
    public static XPathStringLiteral newXPathStringLiteral(String value) {
        return new XPathStringLiteralImpl(value);
    }
    
    
    /**
     * Instantiates a new XPathNumericLiteral object.
     * @param value the value
     * @return a new XPathNumericLiteral object instance
     */
    public static XPathNumericLiteral newXPathNumericLiteral(Number value) {
        return new XPathNumericLiteralImpl(value);
    }
    
    
    /**
     * Instantiates a new XPathCoreFunction object.
     * @param function the function code
     * @return a new XPathCoreFunction object instance
     */
    public static XPathCoreFunction newXPathCoreFunction(int function) {
        return new XPathCoreFunctionImpl(function);
    }
    
    
    /**
     * Instantiates a new XPathExtension Function object.
     * @param name the function name
     * @return a new XPathExtensionFunction object instance
     */
    public static XPathExtensionFunction newXPathExtensionFunction(
                                                            String name) {
        return new XPathExtensionFunctionImpl(name);
    }
    
    
    /**
     * Instantiates a new XPathCoreOperation object.
     * @param code the operation code
     * @return a new XPathCoreOperatoin object instance
     */
    public static XPathCoreOperation newXPathCoreOperation(int code) {
        return new XPathCoreOperationImpl(code);
    }
    
    
    /**
     * Instantiates a new XPathLocationPath object.
     * @param steps the steps
     * @return a new XPathLocationPath object instance
     */
    public static XPathLocationPath newXPathLocationPath(LocationStep[] steps) {
        return new XPathLocationPathImpl(steps);
    }

    /**
     * gives the type of the core operation given the operator string
     * @param operator String 
     * @return int (type)
     */
    public static Integer getOperatorType(String operator) {
        
        return (Integer) opHash.get(operator);
    }

    /**
     * gives the type of the function given the function
     * @param function String 
     * @return int (type)
     */
    public static Integer getFunctionType(String function) {
        
        return (Integer) funcHash.get(function);
    }

    /**
     * Determines if a function name is valid. Assumes the function name is
     * not one of the core functions.
     * @param functionName the name of the function
     * @return true if the function name is valid, false otherwise
     */
    public static boolean isValidFunction(String functionName) {
        return XPathModelImpl.isValidFunction(functionName);
    }
}
