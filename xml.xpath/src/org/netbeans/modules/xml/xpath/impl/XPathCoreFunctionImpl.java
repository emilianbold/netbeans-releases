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

import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.function.core.visitor.XPathCoreFunctionVisitor;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;



/**
 * Represents a core XPath function.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public class XPathCoreFunctionImpl
    extends XPathOperatorOrFunctionImpl
    implements XPathCoreFunction {
        
    /** The function code. */
    int mFunction;
    
    
    /**
     * Constructor. Instantiates a new XPathCoreFunction with the given code.
     * @param function the function code
     */
    public XPathCoreFunctionImpl(int function) {
        super();
        setFunction(function);
    }
    
    
    /**
     * Gets the function code.
     * @return the function code
     */
    public int getFunction() {
        return mFunction;
    }
    
    
    /**
     * Sets the function code.
     * @param function the function code
     */
    public void setFunction(int function) {
        mFunction = function;
    }
    
    
    /**
     * Gets the name of the function.
     * @return the function name or null if invalid
     */
    public String getName() {
        int code = getFunction();

        switch (code) {
        case XPathCoreFunction.FUNC_LAST:
            return "last";
        case XPathCoreFunction.FUNC_POSITION:
            return "position";
        case XPathCoreFunction.FUNC_COUNT:
            return "count";
        case XPathCoreFunction.FUNC_ID:
            return "id";
        case XPathCoreFunction.FUNC_LOCAL_NAME:
            return "local-name";
        case XPathCoreFunction.FUNC_NAMESPACE_URI:
            return "namespace-uri";
        case XPathCoreFunction.FUNC_NAME:
            return "name";
        case XPathCoreFunction.FUNC_STRING:
            return "string";
        case XPathCoreFunction.FUNC_CONCAT:
            return "concat";
        case XPathCoreFunction.FUNC_STARTS_WITH:
            return "starts-with";
        case XPathCoreFunction.FUNC_CONTAINS:
            return "contains";
        case XPathCoreFunction.FUNC_SUBSTRING_BEFORE:
            return "substring-before";
        case XPathCoreFunction.FUNC_SUBSTRING_AFTER:
            return "substring-after";
        case XPathCoreFunction.FUNC_SUBSTRING:
            return "substring";
        case XPathCoreFunction.FUNC_STRING_LENGTH:
            return "string-length";
        case XPathCoreFunction.FUNC_NORMALIZE_SPACE:
            return "normalize-space";
        case XPathCoreFunction.FUNC_TRANSLATE:
            return "translate";
        case XPathCoreFunction.FUNC_BOOLEAN:
            return "boolean";
        case XPathCoreFunction.FUNC_NOT:
            return "not";
        case XPathCoreFunction.FUNC_TRUE:
            return "true";
        case XPathCoreFunction.FUNC_FALSE:
            return "false";
        case XPathCoreFunction.FUNC_LANG:
            return "lang";
        case XPathCoreFunction.FUNC_NUMBER:
            return "number";
        case XPathCoreFunction.FUNC_SUM:
            return "sum";
        case XPathCoreFunction.FUNC_FLOOR:
            return "floor";
        case XPathCoreFunction.FUNC_CEILING:
            return "ceiling";
        case XPathCoreFunction.FUNC_ROUND:
            return "round";
        case XPathCoreFunction.FUNC_NULL:
            return "null";
        case XPathCoreFunction.FUNC_KEY:
            return "key";
        case XPathCoreFunction.FUNC_FORMAT_NUMBER:
            return "format-number";
        case XPathCoreFunction.FUNC_EXISTS:
        	return "exists";
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


	public void accept(XPathCoreFunctionVisitor visitor) {
		//do nothing
		
	}
    
    
}
