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

package org.netbeans.modules.xml.xpath.ext.impl;

import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;

/**
 * Represents a core XPath function.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class XPathCoreFunctionImpl extends XPathOperatorOrFunctionImpl<String>
    implements XPathCoreFunction {
        
    /** The function type. */
    CoreFunctionType mFunctionType;
    
    /**
     * Constructor. Instantiates a new XPathCoreFunction with the given code.
     * @param function the function code
     */
    public XPathCoreFunctionImpl(XPathModel model, CoreFunctionType functionType) {
        super(model);
        setFunctionType(functionType);
    }
    
    /**
     * Gets the function code.
     * @return the function code
     */
    public CoreFunctionType getFunctionType() {
        return mFunctionType;
    }
    
    /**
     * Sets the function code.
     * @param function the function code
     */
    public void setFunctionType(CoreFunctionType type) {
        mFunctionType = type;
    }
    
    /**
     * Gets the name of the function.
     * @return the function name or null if invalid
     */
    public String getName() {
        return getFunctionType().getMetadata().getName();
    }

    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    @Override
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }

}
