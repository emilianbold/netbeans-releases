/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.xam.Named;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public abstract class NamedImpl extends WSDLComponentBase implements Named<WSDLComponent> {
    
    /** Creates a new instance of NamedImpl */
    public NamedImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }

}
