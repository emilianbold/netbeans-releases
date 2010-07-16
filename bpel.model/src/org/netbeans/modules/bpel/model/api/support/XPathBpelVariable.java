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
package org.netbeans.modules.bpel.model.api.support;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The special BPEL variable container is intended to be used by XPath model. 
 * 
 * @author nk160297
 */
public final class XPathBpelVariable implements XPathVariable {

    private VariableDeclaration myVarDecl;
    private Part myPart;

    /**
     * The part can be null.
     */ 
    public XPathBpelVariable(VariableDeclaration var, Part part) {
        assert var != null;
        myVarDecl = var;
        myPart = part;
    }
    
    public VariableDeclaration getVarDecl() {
        return myVarDecl;
    }
    
    public Part getPart() {
        return myPart;
    }

    public QName getName() {
        return constructXPathName();
    }
    
    public ReferenceableSchemaComponent getType() {
        //
        NamedComponentReference<GlobalElement> gElementRef = null;
        NamedComponentReference<GlobalType> gTypeRef = null;
        Part part = getPart();
        if (part != null) {
            gElementRef = part.getElement();
            if (gElementRef == null) {
                gTypeRef = part.getType();
            }
        } else {
            AbstractVariableDeclaration varDecl = getVarDecl();
            gElementRef = varDecl.getElement();
            if (gElementRef == null) {
                gTypeRef = varDecl.getType();
            }
        }
        //
        if (gElementRef != null) { 
            GlobalElement gElement = gElementRef.get();
            return gElement;
        } 
        //
        if (gTypeRef != null) {
            GlobalType gType = gTypeRef.get();
            return gType;
        }
        //
        return null;
    }
    
    public QName constructXPathName() {
        String partName = null;
        if (myPart != null) {
            partName = myPart.getName();
        }
        //
        String totalVarName;
        if (partName != null && partName.length() != 0) {
            totalVarName = myVarDecl.getVariableName() + "." + partName;
        } else {
            totalVarName = myVarDecl.getVariableName();
        }
        //
        // It looks like a prefix is not required for BPEL variables
        return new QName(totalVarName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XPathBpelVariable)) {
            return false;
        }
        //
        XPathBpelVariable other = (XPathBpelVariable)obj;
        //
        if (!(myVarDecl.equals(other.myVarDecl))) {
            return false;
        }
        //
        if (myPart != null && other.myPart == null) {
            return false;
        }
        if (myPart == null && other.myPart != null) {
            return false;
        }
        if (myPart != null && other.myPart != null) {
            if (!(myPart.equals(other.myPart))) {
                return false;
            }
        }
        //
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.myVarDecl != null ? this.myVarDecl.getVariableName().hashCode() : 0);
        hash = 67 * hash + (this.myPart != null ? this.myPart.getName().hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return getExpressionString();
    }

    public String getExpressionString() {
        String varName = myVarDecl.getVariableName();
        if (myPart == null) {
            return "$" + varName;
        } else {
            return "$" + varName + "." + myPart.getName();
        }
    }
}
