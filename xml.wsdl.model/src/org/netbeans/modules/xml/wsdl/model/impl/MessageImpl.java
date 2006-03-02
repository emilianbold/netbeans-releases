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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class MessageImpl extends NamedImpl implements Message {
    
    /** Creates a new instance of MessageImpl */
    public MessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public MessageImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.MESSAGE.getQName(), model));
    }
    
    public Collection<Part> getParts() {
        return getChildren(Part.class);
    }

    public void removePart(Part part) {
        removeChild(PART_PROPERTY, part);
    }

    public void addPart(Part part) {
        appendChild(PART_PROPERTY, part);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
