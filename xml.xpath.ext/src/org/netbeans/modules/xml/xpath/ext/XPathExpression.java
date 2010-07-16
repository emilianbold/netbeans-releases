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

package org.netbeans.modules.xml.xpath.ext;

import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitable;

/**
 * <p>
 * Represents an XPath expression.
 * <p>
 * An expression may have children representing steps in a location path or
 * arguments to operations and functions. The children expressions are
 * represented as an ordered collection.
 *
 * @author Enrico Lelina
 * @version 
 */
public interface XPathExpression extends XPathVisitable {
    
    /**
     * Returns the XPath model to which the expression is owned. 
     */ 
    XPathModel getModel();
    
    /**
     * Gets the string representation of the expression.
     * @return the string representation
     */
    String getExpressionString();

    /**
     * Does almost the same as the previous method but use the specified
     * namespace context while building locaiton steps' prefixes.
     *
     * @return the string representation
     */
    String getExpressionString(NamespaceContext nc);

}
