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
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;

/**
 * Implements the XPathVisitor interface to generate a string representation
 * of an AbstractLocationPath.
 * 
 * @author sbyn
 * @author nk160297
 * @version 
 */
public class PathExpressionWriter extends ExpressionWriter  {
    
    private AbstractLocationPath mPath;
    private int mLastStepIndex;
    
    /** Constructor. */
    public PathExpressionWriter(XPathModel xPathModel) {
        super(xPathModel);
    }
    
    /** Constructor. */
    public PathExpressionWriter(NamespaceContext nc) {
        super(nc);
    }

    public void processPath(AbstractLocationPath path, int lastStepIndex) {
        mPath = path;
        mLastStepIndex = lastStepIndex;
        //
        ((XPathExpression)mPath).accept(this);
    }
    
    /**
     * Visits a location path.
     * @param locationPath to visit
     */
    @Override
    public void visit(XPathLocationPath locationPath) {
        if (locationPath != mPath || mLastStepIndex < 0) {
            super.visit(locationPath);
        } else {
            LocationStep[] steps = locationPath.getSteps();
            if (locationPath.getAbsolute()) {
                mBuffer.append(LocationStep.STEP_SEPARATOR);
            }
            //
            int minIndex = Math.min(mLastStepIndex + 1, steps.length);
            for (int i = 0; i < minIndex; i++) {
                if (i != 0) {
                    mBuffer.append(LocationStep.STEP_SEPARATOR);
                }
                mParentExpr = locationPath;
                steps[i].accept(this);
            }
        }
    }

    /**
     * Visits a expression path.
     * @param locationPath to visit
     */
    @Override
    public void visit(XPathExpressionPath expressionPath) {
        if (expressionPath != mPath || mLastStepIndex < 0) {
            super.visit(expressionPath);
        } else {
            XPathExpression rootExpression = expressionPath.getRootExpression();
            if(rootExpression != null) {
                mParentExpr = expressionPath;
                rootExpression.accept(this);
            }
            //
            LocationStep[] steps = expressionPath.getSteps();
            int minIndex = Math.min(mLastStepIndex + 1, steps.length);
            for (int i = 0; i < minIndex; i++) {
                mBuffer.append(LocationStep.STEP_SEPARATOR);
                mParentExpr = expressionPath;
                steps[i].accept(this);
            }
        }
    }
    
}
