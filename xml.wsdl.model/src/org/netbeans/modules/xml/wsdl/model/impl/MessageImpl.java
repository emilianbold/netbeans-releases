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
