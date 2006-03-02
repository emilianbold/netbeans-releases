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
