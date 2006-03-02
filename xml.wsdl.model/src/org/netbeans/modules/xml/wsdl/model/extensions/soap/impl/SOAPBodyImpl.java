/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPBodyImpl extends SOAPMessageBaseImpl implements SOAPBody {
    
    /** Creates a new instance of SOAPBodyImpl */
    public SOAPBodyImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPBodyImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.BINDING.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public Collection<String> getParts() {
        String s = getAttribute(SOAPAttribute.PARTS);
        return s == null ? null : Util.parse(s);
    }
    
    public void addPart(String part) {
        Collection<String> parts = getParts();
        parts.add(part);
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, Util.toString(parts));;
    }
    
    public void removePart(String part) {
        Collection<String> parts = getParts();
        parts.remove(part);
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, Util.toString(parts));
    }
}
