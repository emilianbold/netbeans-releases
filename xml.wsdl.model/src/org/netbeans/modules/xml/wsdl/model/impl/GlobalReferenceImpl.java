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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.AbstractGlobalReference;
import org.netbeans.modules.xml.xam.Attribute;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Nam Nguyen
 * @author rico
 */
public class GlobalReferenceImpl<T extends ReferenceableWSDLComponent> 
        extends AbstractGlobalReference<T> implements GlobalReference<T> {
    
    /** Creates a new instance of GlobalReferenceImpl */
    //for use by factory, create from scratch
    public GlobalReferenceImpl(
            T referenced, 
            Class<T> type, 
            WSDLComponentBase parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public GlobalReferenceImpl(
            Class<T> type, 
            WSDLComponentBase parent, 
            String refString){
        super(type, parent, refString);
    }
    
    protected Definitions getDefinitions() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(parent);
        return wparent.getModel().getDefinitions();
    }
    
    public T get() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(parent);
        if (super.getReferenced() == null) {
            WSDLModel model = wparent.getWSDLModel().findWSDLModel(getEffectiveNamespace());
            String localName = getLocalName();
            T target = (model == null) ? null : new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public String getEffectiveNamespace() {
        if (getRefString() == null) {
            assert getReferenced() != null;
            return getReferenced().getWSDLModel().getDefinitions().getTargetNamespace();
        } else {
            return parent.lookupNamespaceURI(getPrefix());
        }
    }
}
