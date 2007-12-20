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



import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;


/**
 * Represents a core XPath operation.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class XPathCoreOperationImpl extends XPathOperatorOrFunctionImpl<String>
    implements XPathCoreOperation {
        
    /** The operator code. */
    CoreOperationType mOperationType;
    
    
    /**
     * Constructor. Instantiates a new XPathCoreOperation with the given code.
     * @param operator the operator code
     */
    public XPathCoreOperationImpl(XPathModel model, CoreOperationType opType) {
        super(model);
        setOperationType(opType);
    }
    
    
    /**
     * Gets the operator code.
     * @return the operator code
     */
    public CoreOperationType getOperationType() {
        return mOperationType;
    }
    
    
    /**
     * Sets the operator code.
     * @param operator the operator code
     */
    public void setOperationType(CoreOperationType operator) {
        mOperationType = operator;
    }
    
    
    /**
     * Gets the name of the operator.
     * @return the operator name
     */
    public String getName() {
        return getOperationType().getMetadata().getName();
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
