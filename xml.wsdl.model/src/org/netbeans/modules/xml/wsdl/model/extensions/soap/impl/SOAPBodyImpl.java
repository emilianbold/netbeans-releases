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

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
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
        this(model, createPrefixedElement(SOAPQName.BODY.getQName(), model));
    }
    
    public void accept(SOAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public List<String> getParts() {
        String s = getAttribute(SOAPAttribute.PARTS);
        return s == null ? null : Util.parse(s);
    }
    
    public List<Reference<Part>> getPartRefs() {
        String s = getAttribute(SOAPAttribute.PARTS);
        return s == null ? null : parseParts(s);
    }
    
    public void addPart(String part) {
        String parts = getAttribute(SOAPAttribute.PARTS);
        parts = parts == null ? part : parts.trim() + " " + part; //NOI18N
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, parts);
    }
    
    public void addPartRef(Reference<Part> partRef) {
        addPart(partRef.getRefString());
    }
    
    public void addPart(int index, String part) {
        List<String> parts = getParts();
        if (parts != null) {
            parts.add(index, part);
        } else {
            parts = Collections.singletonList(part);
        }
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, Util.toString(parts));;
    }
    
    public void addPartRef(int index, Reference<Part> partRef) {
        addPart(index, partRef.getRefString());
    }
    
    public void removePart(String part) {
        Collection<String> parts = getParts();
        if (parts != null && parts.remove(part)) {
            setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, Util.toString(parts));
        }
    }

    public void removePartRef(Reference<Part> partRef) {
        removePart(partRef.getRefString());
    }

    public void setParts(List<String> parts) {
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, Util.toString(parts));
    }

    public void setPartRefs(List<Reference<Part>> parts) {
        String value = null;
        if (parts != null && ! parts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Reference<Part> ref : parts) {
                sb.append(ref.getRefString());
            }
            value = sb.toString();
        }
        setAttribute(PARTS_PROPERTY, SOAPAttribute.PARTS, value);
    }
    
    private List<Reference<Part>> parseParts(String s) {
        List<Reference<Part>> ret = new ArrayList<Reference<Part>>();
        for (String part : Util.parse(s)) {
            ret.add(new PartReference(this, part));
        }
        return ret;
    }


    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingInput || target instanceof BindingOutput) {
            return true;
        }
        return false;
    }
}
