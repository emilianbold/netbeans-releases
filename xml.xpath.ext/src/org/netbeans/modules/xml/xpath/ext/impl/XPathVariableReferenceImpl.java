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

package org.netbeans.modules.xml.xpath.ext.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.VariableResolver;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xpath.ext.spi.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * 
 * @author radval
 * @author nk160297
 *
 */
public class XPathVariableReferenceImpl extends XPathExpressionImpl 
        implements XPathVariableReference {

    // private Reference mVaribleReference;
    private QName mVariableQName;
    private VariableSchemaContext mSchemaContext;
    
    public XPathVariableReferenceImpl(XPathModel model, QName variableQName) {
        super(model);
        mVariableQName = variableQName;
    }

    public void setVariableName(QName qName) {
        mVariableQName = qName;
    }
    
    public QName getVariableName() {
        return mVariableQName;
    }

    public XPathVariable getVariable() {
        VariableResolver varResolver = myModel.getVariableResolver();
        if (varResolver != null) {
            XPathVariable var = varResolver.resolveVariable(mVariableQName);
            return var;
        }
        return null;
    }

    public ReferenceableSchemaComponent getType() {
        VariableResolver varResolver = myModel.getVariableResolver();
        if (varResolver != null) {
            ReferenceableSchemaComponent varType = 
                    varResolver.resolveVariableType(mVariableQName);
            return varType;
        }
        return null;
    }
    
    public <VAR_TYPE extends XPathVariable> VAR_TYPE 
            getVariable(Class<VAR_TYPE> varClass) {
        XPathVariable var = getVariable();
        if (var != null) {
            assert varClass.isInstance(var) : "Wrong class"; // NOI18N
            return varClass.cast(var);
        }
        return null;
    }
    
    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    @Override
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return XPathUtils.qNameObjectToString(mVariableQName);
    }

    public VariableSchemaContext getSchemaContext() {
        return mSchemaContext;
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        assert newContext instanceof VariableSchemaContext;
        mSchemaContext = (VariableSchemaContext)newContext;
    }
    
}
