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

import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;

/**
 *
 * @author radval
 *
 */
public class XPathPredicateExpressionImpl extends XPathExpressionImpl 
        implements XPathPredicateExpression {

    private XPathExpression mExpression;

    private XPathSchemaContext mSchemaContext;

    public XPathPredicateExpressionImpl(XPathModel model, 
            XPathExpression expression) {
        super(model);
        this.mExpression = expression;
    }

    public XPathExpression getPredicate() {
        return this.mExpression;
    }

    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    @Override
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
    
    public XPathSchemaContext getSchemaContext() {
        if (mSchemaContext == null) {
            boolean success = false;
            if (mModel.getRootExpression() != null) {
                success = mModel.resolveExtReferences(false);
            } else {
                success = mModel.resolveExpressionExtReferences(this);
            }
            //
            // TODO: Nikita. Uncomment for Debugging
            //
//            if (success && mSchemaContext == null) {
//                // assert false : "Wrong behavior!"; // NOI18N
//                //
//                // Try again for debugging purposes
//                if (myModel.getRootExpression() != null) {
//                    success = myModel.resolveExtReferences(true);
//                } else {
//                    success = myModel.resolveExpressionExtReferences(this);
//                }
//            }
        }
        return mSchemaContext;
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        mSchemaContext = newContext;
    }
    
}
