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

package org.netbeans.modules.xml.xpath.ext.visitor.impl;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.OperationMetadata;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

/**
 * Implements the XPathVisitor interface to generate a string representation
 * of an expression.
 * 
 * @author sbyn
 * @author nk160297
 * @version 
 */
public class ExpressionWriter extends XPathVisitorAdapter  {
    
    /** The string buffer. */
    protected StringBuffer mBuffer;
    
    protected XPathExpression mParentExpr;
    protected XPathModel mXPathModel;
    protected NamespaceContext mNC;
    
    /** Constructor. */
    public ExpressionWriter(XPathModel xPathModel) {
        this((NamespaceContext)null);
        mXPathModel = xPathModel;
    }
    
    /** Constructor. */
    public ExpressionWriter(NamespaceContext nc) {
        mBuffer = new StringBuffer();
        mNC = nc;
    }
    
    /**
     * Gets the string representation of the expression.
     * @return the string representation
     */
    public String getString() {
        return mBuffer.toString();
    }

    /**
     * Visits an location step.
     * @param locationStep to visit
     */
    @Override
    public void visit(LocationStep locationStep) {
        if (mNC == null) {
            mBuffer.append(locationStep.getString());
        } else {
            mBuffer.append(locationStep.getString(mNC));
        }
        XPathPredicateExpression[] predicates = locationStep.getPredicates();
        if (predicates != null) {
            for (int j = 0, length = predicates.length; j < length; j++) {
                predicates[j].accept(this);
            }
        }
    }

    @Override
    public void visit(XPathPredicateExpression predicate) {
        XPathExpression predicateExpression = predicate.getPredicate();
        if(predicateExpression != null) {
            mBuffer.append('[');
            mParentExpr = predicate;
            predicateExpression.accept(this);
            mBuffer.append(']');
        }
    }

    @Override
    public void visit(XPathVariableReference vReference) {
        mBuffer.append("$" + vReference.getVariableName());
    }
    
    /**
     * Visits a string literal.
     * @param stringLiteral to visit
     */
    @Override
    public void visit(XPathStringLiteral stringLiteral) {
        
        // quotes in literal strings for xpath 1.0 basically work like this:
        // 1. we can either quote strings with single or double quotes
        // 2. quote the string with single quotes if the string contains double quotes
        //    i.e.   'the "correct" way',   'the 'incorrect' way'
        // 3. quote the string with double quotes if the string contains single quotes
        //    i.e.   "the 'correct' way",   "the "incorrect" way"
        // - josh
        
        String literal = stringLiteral.getValue();
        boolean isStringQuoted = false;
        if (literal.length() >= 2) {
            if        (literal.startsWith("'")  && literal.endsWith("'")) {
                isStringQuoted = true;
            } else if (literal.startsWith("\"") && literal.endsWith("\"")) {
                isStringQuoted = true;
            }
        }
        
        if (isStringQuoted) {
            // if literal is already quoted, do not quote the literal
            mBuffer.append(literal);
        } else {
            if (literal.indexOf("'") >= 0) {
                // string contains a single-quote, 
                // it must be quoted with double-quotes
                mBuffer.append("\"");
                mBuffer.append(literal);
                mBuffer.append("\"");
            } else {
                // quote the string with single-quotes by default
                mBuffer.append("'");
                mBuffer.append(literal);
                mBuffer.append("'");
            }
        }
        
    }
    
    /**
     * Visits a numeric literal.
     * @param numericLiteral to visit
     */
    @Override
    public void visit(XPathNumericLiteral numericLiteral) {
        mBuffer.append(numericLiteral.getValue().toString());
    }
    
    /**
     * Visits a location path.
     * @param locationPath to visit
     */
    @Override
    public void visit(XPathLocationPath locationPath) {
        LocationStep[] steps = locationPath.getSteps();
        if (locationPath.getAbsolute()) {
            mBuffer.append(LocationStep.STEP_SEPARATOR);
        }
        for (int i = 0; i < steps.length; i++) {
            if (i != 0) {
                mBuffer.append(LocationStep.STEP_SEPARATOR);
            }
            mParentExpr = locationPath;
            steps[i].accept(this);
        }
    }

    /**
     * Visits a expression path.
     * @param locationPath to visit
     */
    @Override
    public void visit(XPathExpressionPath expressionPath) {
        XPathExpression rootExpression = expressionPath.getRootExpression();
        if(rootExpression != null) {
            mParentExpr = expressionPath;
            rootExpression.accept(this);
        }
        //
        LocationStep[] steps = expressionPath.getSteps();
        for (int i = 0; i < steps.length; i++) {
            mBuffer.append(LocationStep.STEP_SEPARATOR);
            mParentExpr = expressionPath;
            steps[i].accept(this);
        }
    }
    
    /**
     * Visits a core operation.
     * @param coreOperation to visit
     */
    @Override
    public void visit(XPathCoreOperation coreOperation) {
        if (CoreOperationType.OP_NEGATIVE == coreOperation.getOperationType()) {
            mBuffer.append(coreOperation.getOperationType().getMetadata().getName());
            if(coreOperation.getChildCount() > 0) {
                XPathExpression firstArg = coreOperation.getChild(0);
                mParentExpr = coreOperation;
                firstArg.accept(this);
            }
        } else {
            boolean isBracketsRequired = false;
            OperationMetadata metadata = 
                    coreOperation.getOperationType().getMetadata();
            if (mParentExpr != null && mParentExpr instanceof XPathCoreOperation) {
                int parentPrecedence = ((XPathCoreOperation)mParentExpr).
                        getOperationType().getMetadata().getPrecedenceLevel();
                int myPrecedence = metadata.getPrecedenceLevel();
                isBracketsRequired = parentPrecedence > myPrecedence;
            }
            //
            if (isBracketsRequired) {
                mBuffer.append("(");
            }
            //
            boolean isFirst = true;
            for (XPathExpression childExpr : coreOperation.getChildren()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    mBuffer.append(' ');
                    mBuffer.append(metadata.getName());
                    mBuffer.append(' ');
                }
                //
                mParentExpr = coreOperation;
                childExpr.accept(this);
            }
            //
            if (isBracketsRequired) {
                mBuffer.append(")");
            }
        }
    }
    
    /**
     * Visits a core function.
     * @param coreFunction to visit
     */
    @Override
    public void visit(XPathCoreFunction coreFunction) {
        mBuffer.append(coreFunction.getName());
        mBuffer.append('(');
        boolean isFirst = true;
        for (XPathExpression expr : coreFunction.getChildren()) {
            if (isFirst) {
                isFirst = false;
            } else {
                mBuffer.append(", ");
            }
            //
            mParentExpr = coreFunction;
            expr.accept(this);
        }
        mBuffer.append(')');
    }
    
    /**
     * Visits an extension function.
     * @param extensionFunction to visit
     */
    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
        QName extFuncQName = extensionFunction.getName();
        String prefix = extFuncQName.getPrefix();
        String localPart = extFuncQName.getLocalPart();
        //
        if (prefix == null || prefix.length() == 0) {
            prefix = calculateFuncPrefix(extFuncQName);
        }
        //
        if (prefix == null || prefix.length() == 0) {
            mBuffer.append(localPart);
        } else {
            mBuffer.append(prefix + ":" + localPart);
        }
        //
        mBuffer.append('(');
        boolean isFirst = true;
        for (XPathExpression expr : extensionFunction.getChildren()) {
            if (isFirst) {
                isFirst = false;
            } else {
                mBuffer.append(", ");
            }
            //
            mParentExpr = extensionFunction;
            expr.accept(this);
        }
        mBuffer.append(')');
    }

    public String calculateFuncPrefix(QName funcQName) {
        if (mNC != null) {
            String nsUri = funcQName.getNamespaceURI();
            return mNC.getPrefix(nsUri);
        } else if (mXPathModel != null) {
            NamespaceContext nsContext = mXPathModel.getNamespaceContext();
            if (nsContext != null) {
                String nsUri = funcQName.getNamespaceURI();
                return nsContext.getPrefix(nsUri);
            }
        }
        return null;
    }
}
