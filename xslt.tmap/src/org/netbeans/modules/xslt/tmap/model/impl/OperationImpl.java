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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.TransformerDescriptor;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class OperationImpl extends TMapComponentAbstract 
    implements Operation 
{

    public OperationImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.OPERATION, model));
    }

    public OperationImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public Invokes getInvokes() {
        return getChild(Invokes.class);
    }

    public void removeInvokes() {
        removeChild(TYPE.getTagName(), getInvokes());
    }

    public void setInvokes(Invokes invokes) {
        setChild(Invokes.class, TYPE.getTagName(), invokes, TYPE.getChildTypes());
    }

    public Reference<org.netbeans.modules.xml.wsdl.model.Operation> getOperation() {
        return getWSDLReference(TMapAttributes.OPERATION_NAME, 
                org.netbeans.modules.xml.wsdl.model.Operation.class);
    }

    public void setOperation(WSDLReference<org.netbeans.modules.xml.wsdl.model.Operation> opRef) {
        setWSDLReference( TMapAttributes.OPERATION_NAME, opRef);
    }

    public Reference[] getReferences() {
        return new Reference[] {getOperation()};
    }

    public String getFile() {
        return getAttribute(TMapAttributes.FILE);
    }

    public void setFile(String locationURI) {
        setAttribute(TransformerDescriptor.FILE, TMapAttributes.FILE, locationURI);
    }

    public BooleanType isTransformJbi() {
        return BooleanType.parseBooleanType(getAttribute(TMapAttributes.TRANSFORM_JBI));
    }

    public void setTransformJbi(BooleanType boolVal) {
        setAttribute(TransformerDescriptor.TRANSFORM_JBI, TMapAttributes.TRANSFORM_JBI, boolVal);
    }

    public Class<Operation> getComponentType() {
        return Operation.class;
    }
}
