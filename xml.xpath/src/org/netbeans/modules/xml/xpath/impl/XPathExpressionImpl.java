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


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitable;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;
import org.netbeans.modules.xml.xpath.visitor.impl.ExpressionWriter;


/**
 * Default implementation of the XPathExpression interface.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public abstract class XPathExpressionImpl
    implements XPathExpression {
    
    
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
        XPathVisitor visitor = new ExpressionWriter();
        accept(visitor);
        return ((ExpressionWriter) visitor).getString();
    }
    
    public String toString() {
    	return getExpressionString();
    }
}
