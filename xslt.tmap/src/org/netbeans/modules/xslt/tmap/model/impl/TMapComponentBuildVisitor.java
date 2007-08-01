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

import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapComponentBuildVisitor implements TMapVisitor {

    private TMapModelImpl myModel;
    private Element myElement;
    private TMapComponent myResult;
    
    public TMapComponentBuildVisitor(TMapModelImpl model) {
        assert model != null;
        myModel = model;
    }

    public TMapComponent createSubComponent( TMapComponent parent , Element element ) 
    {
        myElement = element;
        String namespace = element.getNamespaceURI();
        if ( namespace == null && parent instanceof TMapComponentAbstract ) {
            namespace = ((TMapComponentAbstract)parent).
                lookupNamespaceURI(element.getPrefix());
        }
        
        if (TMapComponent.TRANSFORM_MAP_NS_URI.equals(namespace)) {
            if (parent == null) {
                if (TMapComponents.TRANSFORM_MAP.getTagName().equals(
                        getElement().getLocalName()))
                {
                    setResult(new TransformMapImpl(getModel(), element));
                }
            } else {
                parent.accept(this);
            }
        }

        return myResult;
    }

    public void visit(TransformMap transformMap) {
        if (isAcceptable(TMapComponents.SERVICE)) {
            setResult(new ServiceImpl(getModel(), getElement()));
        }
    }

    public void visit(Service service) {
        if (isAcceptable(TMapComponents.OPERATION)) {
            setResult(new OperationImpl(getModel(), getElement()));
        }
    }

    public void visit(Operation operation) {
        if (isAcceptable(TMapComponents.INVOKE)) {
            setResult(new InvokeImpl(getModel(), getElement()));
        } else if (isAcceptable(TMapComponents.TRANSFORM)) {
            setResult(new TransformImpl(getModel(), getElement()));
        }
    }

    public void visit(Invoke invoke) {
        
    }

    public void visit(Transform transform) {
        if (isAcceptable(TMapComponents.PARAM)) {
            setResult(new ParamImpl(getModel(), getElement()));
        }
    }

    public void visit(Param param) {
    }

    void init(){
        myResult = null;
        myElement = null;
    }
    
    public Element getElement() {
        return myElement;
    }
    
    private boolean isAcceptable( TMapComponents acceptedComponents ) {
        return acceptedComponents.getTagName().equals( getLocalName() );
    }
    
    private String getLocalName() {
        return getElement().getLocalName();
    }
    
    private TMapModelImpl getModel() {
        return myModel;
    }
    
    private void setResult( TMapComponent component ) {
        myResult = component;
    }
}
