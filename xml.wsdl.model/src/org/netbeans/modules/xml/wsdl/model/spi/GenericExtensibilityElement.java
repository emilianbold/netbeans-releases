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
package org.netbeans.modules.xml.wsdl.model.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class GenericExtensibilityElement extends WSDLComponentBase implements ExtensibilityElement.ParentSelector {
    
    /** Creates a new instance of GenericExtensibilityElement */
    public GenericExtensibilityElement(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public GenericExtensibilityElement(WSDLModel model, QName qname){
        this(model, createNewElement(qname, model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        public Class getType() { return String.class; }
        public String getName() { return name; }
        public Class getMemberType() { return null; }
    }
    
    public String getAttribute(String attribute) {
        return getAttribute(new StringAttribute(attribute));
    }
    public void setAttribute(String attribute, String value) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_FRAGMENT_PROPERTY, text);
    }

    public void addAnyElement(ExtensibilityElement anyElement, int index) {
        List<WSDLComponent> all = getChildren();
        if (index > all.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        insertAtIndex(EXTENSIBILITY_ELEMENT_PROPERTY, anyElement, index);
    }

    public void removeAnyElement(ExtensibilityElement any) {
        super.removeExtensibilityElement(any);
    }

    public List<ExtensibilityElement> getAnyElements() {
        List<ExtensibilityElement> result = new ArrayList<ExtensibilityElement>();
        List<ExtensibilityElement> allEEs = super.getExtensibilityElements();
        for (ExtensibilityElement ee : allEEs) {
            if (! ee.getModel().getQNames().contains(ee.getQName())) {
                result.add(ee);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Generic extensibility by default can be added to any WSDL component.
     */
    public boolean canBeAddedTo(Component target) {
        if (target instanceof WSDLComponent) {
            return true;
        }
        return false;
    }
}
