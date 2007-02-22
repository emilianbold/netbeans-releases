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

package org.netbeans.modules.xml.xpath.impl;

import org.netbeans.modules.xml.xpath.XPathPredicateNumericLiteral;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitable;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;



/**
 * Represents a numeric literal.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public class XPathPredicateNumericLiteralImpl
    extends XPathNumericLiteralImpl
    implements XPathPredicateNumericLiteral {
    
    /**
     * Constructor. Instantiates with the given value.
     * @param value the numeric literal value
     */
    public XPathPredicateNumericLiteralImpl(Long value) {
        super(value);
        setValue(value);
    }

    /**
     * Sets the value.
     * @param value the numeric literal value
     */
    public void setValue(Long value) {
        super.setValue(value);
    }

    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
}
