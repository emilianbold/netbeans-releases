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

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.ri.compiler.VariableReference;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;


/**
 * 
 * @author radval
 *
 */
public class XPathVariableReferenceImpl extends XPathExpressionImpl implements XPathVariableReference {

    private VariableReference mVaribleReference;
    
    public XPathVariableReferenceImpl(VariableReference variableReference) {
        setVariableReference(variableReference);
    }

    public void setVariableName(String name) {
        setVariableReference(new VariableReference(new org.apache.commons.jxpath.ri.QName(name)));
    }
    
    private void setVariableReference(VariableReference variableReference) {
        this.mVaribleReference = variableReference;
    }

    public QName getVariableName() {
        org.apache.commons.jxpath.ri.QName jxpathQName = this.mVaribleReference.getVariableName();
        String prefix = jxpathQName.getPrefix();
        String localName = jxpathQName.getName();
        if(prefix == null) {
            prefix = "";
        }
        
        if(localName == null) {
            localName = "";
        }
        return new QName(null, localName, prefix);
        
    }

    public VariableReference getVariableReference() {
        return this.mVaribleReference;
    }
    
    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
    
}
