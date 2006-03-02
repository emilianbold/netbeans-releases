/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
