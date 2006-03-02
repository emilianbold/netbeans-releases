/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class ImportImpl extends WSDLComponentBase implements Import {
    
    /** Creates a new instance of ImportImpl */
    public ImportImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ImportImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.IMPORT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setNamespaceURI(String namespaceURI) {
        setAttribute(NAMESPACE_URI_PROPERTY, WSDLAttribute.NAMESPACE_URI, namespaceURI);
    }

    public void setLocation(String locationURI) {
        setAttribute(LOCATION_PROPERTY, WSDLAttribute.LOCATION, locationURI);
    }

    public String getNamespaceURI() {
        return getAttribute(WSDLAttribute.NAMESPACE_URI);
    }

    public String getLocation() {
        return getAttribute(WSDLAttribute.LOCATION);
    }
}
