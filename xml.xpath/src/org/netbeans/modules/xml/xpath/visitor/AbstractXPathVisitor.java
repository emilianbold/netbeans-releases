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

package org.netbeans.modules.xml.xpath.visitor;

import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.XPathVariableReference;


/**
 * 
 * @author radval
 *
 */
public abstract class AbstractXPathVisitor implements XPathVisitor {

	public void visit(LocationStep locationStep) {
	}

	public void visit(XPathCoreFunction coreFunction) {
	}

	public void visit(XPathCoreOperation coreOperation) {
	}

	public void visit(XPathExpressionPath expressionPath) {
	}

	public void visit(XPathExtensionFunction extensionFunction) {
	}

	public void visit(XPathLocationPath locationPath) {
	}

	public void visit(XPathNumericLiteral numericLiteral) {
	}

	public void visit(XPathStringLiteral stringLiteral) {
	}

	public void visit(XPathVariableReference vReference) {
	}
	
	
	public void visit(XPathPredicateExpression predicate) {
		XPathExpression predicateExpression = predicate.getPredicate();
		if(predicateExpression != null) {
			predicateExpression.accept(this);
		}
	}

	protected void visitChildren(XPathOperationOrFuntion expr) {
		 Collection children = expr.getChildren();
		 if(children != null) {
			 Iterator it = children.iterator();
			 while(it.hasNext()) {
				 XPathExpression child = (XPathExpression) it.next();
				 child.accept(this);
				 
			 }
		 }
	}
	
}
