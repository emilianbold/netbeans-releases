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

import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;
import org.netbeans.modules.xml.xpath.ext.visitor.impl.ExpressionWriter;

/**
 * Default implementation of the XPathExpression interface.
 * 
 * @author Enrico Lelina
 * @version 
 */
public abstract class XPathExpressionImpl implements XPathExpression {

    protected XPathModel mModel;
    
    public XPathExpressionImpl(XPathModel model) {
        mModel = model;
    }
    
    public XPathModel getModel() {
        return mModel;
    }
    
    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    public void accept(XPathVisitor visitor) {
        // do nothing -- must be subclassed
    }
    
    /**
     * String representation.
     * @return the string representation
     */
    public String getExpressionString() {
        XPathVisitor visitor = new ExpressionWriter(mModel);
        accept(visitor);
        return ((ExpressionWriter) visitor).getString();
    }
    
    public String getExpressionString(NamespaceContext nc) {
        if (mModel.getNamespaceContext() == nc) {
            // optimization
            return getExpressionString();
        }
        XPathVisitor visitor = new ExpressionWriter(nc);
        accept(visitor);
        return ((ExpressionWriter) visitor).getString();
    }

    @Override
    public String toString() {
        return getExpressionString();
    }
}
