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
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xslt.tmap.model.api.ExNamespaceContext;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class TMapComponentAbstract 
        extends AbstractDocumentComponent<TMapComponent>
        implements TMapComponent, DocumentModelAccess.NodeUpdater 
{
    private AttributeAccess myAttributeAcces;
    
    
    public TMapComponentAbstract(TMapModelImpl model, Element e) {
        super(model, e);
        myAttributeAcces = new AttributeAccess(this);
    }

    public TMapComponentAbstract(TMapModelImpl model, TMapComponents type) {
        super(model, createNewElement(type, model));
        myAttributeAcces = new AttributeAccess(this);
    }

    @Override
    public TMapModelImpl getModel() {
        return (TMapModelImpl)super.getModel();
    }
    
    protected void populateChildren(List<TMapComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    TMapComponent comp = (TMapComponent) getModel().getFactory()
                            .create((Element) n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    public ExNamespaceContext getNamespaceContext() {
        return new ExNamespaceContextImpl(this);
    }

    protected <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        getWSDLReference( Attribute attr , Class<T> clazz )
    {
        return getAttributeAccess().getWSDLReference( attr , clazz );
    }

    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return getAttributeAccess().getAttributeValueOf(attr, stringValue);
    }

    public <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        createWSDLReference( T target, Class<T> type ) 
    {
// TODO m | r        
//////        readLock();
//////        try {
            return WSDLReferenceBuilder.getInstance().build( target , type , 
                    this );
//////        }
//////        finally {
//////            readUnlock();
//////        }   
    }

    @Override
    public void setAttribute(String eventPropertyName, Attribute attr, Object value) {
        boolean isTransact = true;
        if (!getModel().isIntransaction()) {
            getModel().startTransaction();
        } else {
            isTransact = true;
        }
        
        try {
        
            super.setAttribute(eventPropertyName, attr, value);
        
        } finally {
            if (!isTransact){
                getModel().endTransaction();
            }
        }
    }

    protected  VariableReference 
        getTMapVarReference(Attribute attr)
    {
        return getAttributeAccess().getTMapVarReference(attr);
    }

    protected void setTMapVarReference(
            Attribute attr, VariableReference ref )
    {
        getAttributeAccess().setTMapVarReference( attr , ref );
    }

    protected <T extends ReferenceableWSDLComponent> void setWSDLReference( 
            Attribute attr , WSDLReference<T> ref )
    {
        getAttributeAccess().setWSDLReference( attr , ref );
    }

    protected static Element createNewElement(TMapComponents type, TMapModelImpl model) {
        return model.getDocument().createElementNS(
                TMapComponent.TRANSFORM_MAP_NS_URI, type.getTagName());
    }

    protected final AttributeAccess getAttributeAccess() {
        return myAttributeAcces;
    }
    
    protected final String getComponentName(){
        return getPeer().getLocalName();
    }
    
    
//    
//    protected final void readLock() {
//        getModel().readLock();
//    }
//
//    protected final void readUnlock() {
//        getModel().readUnlock();
//    }
//
//    protected final  void writeLock() {
//        getModel().writeLock();
//    }
//
//    protected final void writeUnlock() {
//        getModel().writeUnlock();
//    }
//
//    
//    
}
