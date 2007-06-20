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

import java.util.List;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
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

    public List<Invokes> getInvokess() {
        return getChildren(Invokes.class);
    }

    public void removeInvokes(Invokes invokes) {
        removeChild(TYPE.getTagName(), invokes);
    }

    public void addInvokes(Invokes invokes) {
        addAfter(TYPE.getTagName(), invokes, TYPE.getChildTypes());
    }

    public int getSizeOfInvokes() {
        List<Invokes> invokes = getInvokess();
        return invokes == null ? 0 : invokes.size();
    }

    public List<Transform> getTransforms() {
        return getChildren(Transform.class);
    }

    public void removeTransforms(Transform transform) {
        removeChild(TYPE.getTagName(), transform);
    }

    public void addTransform(Transform transform) {
        addAfter(TYPE.getTagName(), transform, TYPE.getChildTypes());
    }

    public int getSizeOfTransform() {
        List<Transform> transforms = getTransforms();
        return transforms == null ? 0 : transforms.size();
    }

}
