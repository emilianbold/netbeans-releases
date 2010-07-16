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

package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

/**
 * Implements the XPathVisitor interface to convert the XPath expression
 * to relative form. 
 * 
 * @author sbyn
 * @author nk160297
 * @version 
 */
public class ToRelativePathConverter extends XPathVisitorAdapter  {

    private XPathExpression mRootExpr;
    private XPathSchemaContext mContext;
    
    /** Constructor. */
    public ToRelativePathConverter(XPathExpression expr, XPathSchemaContext context) {
        assert expr != null && context != null;
        //
        mRootExpr = expr;
        mContext = context;
    }

    public XPathExpression convert() {
        mRootExpr.accept(this);
        return mRootExpr;
    }
    
    @Override
    public void visit(XPathCoreFunction coreFunction) {
        visitChildren( coreFunction );
    }

    @Override
    public void visit(XPathCoreOperation coreOperation) {
        visitChildren( coreOperation );
    }

    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
        visitChildren( extensionFunction );
    }
    
    /**
     * Process the special case when the absolute path is the root expression itself
     */
    @Override
    public void visit(XPathExpressionPath expressionPath) {
        XPathExpression convertedExpr = XPathSchemaContext.
                Utilities.generateRelativePath(expressionPath, mContext);
        if (convertedExpr != null && expressionPath == mRootExpr) {
            mRootExpr = convertedExpr;
        }
    }
    
    /**
     * Replace the absolute pathes to relative if possible
     * @param expr
     */
    @Override
    protected void visitChildren(XPathOperationOrFuntion expr) {
        List<XPathExpression> children = expr.getChildren();
        if (children != null) {
            ListIterator<XPathExpression> itr = children.listIterator();
            while (itr.hasNext()) {
                XPathExpression child = itr.next();
                if (child instanceof XPathExpressionPath) {
                    XPathExpression convertedExpr = XPathSchemaContext.
                            Utilities.generateRelativePath(
                            (XPathExpressionPath)child, mContext);
                    if (convertedExpr != null) {
                        itr.set(convertedExpr);
                    }
                } else {
                    child.accept(this);
                }
            }
        }
    }
    
}
