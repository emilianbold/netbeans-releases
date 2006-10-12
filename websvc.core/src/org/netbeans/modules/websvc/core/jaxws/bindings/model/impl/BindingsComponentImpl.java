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
package org.netbeans.modules.websvc.core.jaxws.bindings.model.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponent;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Roderico Cruz
 */
public abstract class BindingsComponentImpl extends AbstractDocumentComponent<BindingsComponent>
        implements BindingsComponent{
    
    /** Creates a new instance of BindingsComponentImpl */
    public BindingsComponentImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    protected void populateChildren(List<BindingsComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    BindingsModel bindingsModel = getModel();
                    BindingsComponentImpl comp = (BindingsComponentImpl) bindingsModel.getFactory().create((Element)n,this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    protected abstract String getNamespaceURI(); 

    public BindingsModelImpl getModel() {
        return (BindingsModelImpl) super.getModel();
    }
    
    protected static org.w3c.dom.Element createNewElement(QName qName, BindingsModel model){
        return model.getDocument().createElementNS(
                qName.getNamespaceURI(),
                qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, BindingsModel model){
        org.w3c.dom.Element e = createNewElement(qName, model);
        e.setPrefix(qName.getPrefix());
        return e;
    }
    
}
