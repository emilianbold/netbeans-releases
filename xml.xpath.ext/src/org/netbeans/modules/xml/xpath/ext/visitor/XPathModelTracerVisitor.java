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

package org.netbeans.modules.xml.xpath.ext.visitor;

import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;

/**
 *
 * @author nk160297
 */
public abstract class XPathModelTracerVisitor extends XPathVisitorAdapter {
    
    @Override
    public void visit(LocationStep locationStep) {
        XPathPredicateExpression[] expressions = locationStep.getPredicates();
        if ( expressions!= null ){
            for (XPathPredicateExpression expression : expressions) {
                expression.accept( this );
            }
        }
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

    @Override
    public void visit(XPathLocationPath locationPath) {
        LocationStep[] steps = locationPath.getSteps();
        if ( steps != null ){
            for (LocationStep step : steps) {
                step.accept( this );
            }
        }
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
        XPathExpression expression = expressionPath.getRootExpression();
        if ( !expressionPath.equals( expression ) ) {
            expression.accept( this );
        }
        LocationStep[] steps = expressionPath.getSteps();
        if ( steps != null ){
            for (LocationStep step : steps) {
                step.accept( this );
            }
        }
    }

}

