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
package org.netbeans.modules.bpel.model.impl;

import java.util.Collection;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class BpelChildEntitiesBuilder {
    
    BpelChildEntitiesBuilder( BpelModelImpl model ){
        myModel = model; 
    }
    

    BpelEntity create( Element element, BpelContainer parent ) {
        assert parent != null;
        
        String namespace = element.getNamespaceURI();
        if ( namespace == null && parent instanceof BpelContainerImpl ) {
            namespace = ((BpelContainerImpl) parent).lookupNamespaceURI(element
                    .getPrefix());
        }
        
        if ( BpelEntity.BUSINESS_PROCESS_NS_URI.equals(namespace) 
                && (parent instanceof BpelContainerImpl ))
        {
            // we create via "create" method only BPEL spec elements.
            return ((BpelContainerImpl)parent).create(element);
        }
        else if ( ServiceRef.SERVICE_REF_NS.equals(namespace)
                && BpelElements.SERVICE_REF.getName().
                                    equals( element.getLocalName() )
                && (parent instanceof BpelContainerImpl ))
        {    
            /*
             *  This is the case "service-ref" element.
             *  In the case of big quantity of such elements ( in external 
             *  namespace but not realy extension one need to implement
             *  this with visitor idiom
             */
            if ( parent instanceof From || parent instanceof Literal 
                    || parent instanceof Query ) 
            {
                    return new ServiceRefImpl( getModel() , element ); 
            }
        }
        else {
            QName elementQName = new QName(namespace, element.getLocalName());
            Collection<EntityFactory> factories = 
                getModel().getEntityRegistry().getFactories();
            for (EntityFactory factory : factories) {
                if ( factory.isApplicable( element.getNamespaceURI()) ){
                    if ( factory.getElementQNames().contains(elementQName)) {
                        BpelEntity entity = factory.create( parent , element );
                        if ( entity!= null ){
                            return entity;
                        }
                    }
                }
            }
            return null;
        }
        return null;
    }
    
    private BpelModelImpl getModel() {
        return myModel;
    }
    
    private BpelModelImpl myModel;
}
