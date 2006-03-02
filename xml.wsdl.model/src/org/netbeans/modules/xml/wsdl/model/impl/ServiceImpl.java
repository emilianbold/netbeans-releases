/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class ServiceImpl extends NamedImpl implements Service {
    
    /** Creates a new instance of ServiceImple */
    public ServiceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ServiceImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.SERVICE.getQName(), model));
    }

    public void removePort(Port port) {
        removeChild(PORT_PROPERTY, port);
    }

    public void addPort(Port port) {
        super.addAfter(PORT_PROPERTY, port, TypeCollection.DOCUMENTATION.types());
    }

    public Collection<Port> getPorts() {
        return getChildren(Port.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
