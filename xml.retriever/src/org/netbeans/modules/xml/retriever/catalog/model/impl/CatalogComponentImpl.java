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
package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class CatalogComponentImpl extends AbstractDocumentComponent<CatalogComponent> 
            implements CatalogComponent {
    
    public CatalogComponentImpl(CatalogModelImpl model, Element element) {
        super(model, element);
    }
    
    public CatalogModelImpl getModel() {
        return (CatalogModelImpl) super.getModel();
    }

    static public Element createElementNS(CatalogModel model, CatalogQNames rq) {
        return model.getDocument().createElementNS(rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    protected void populateChildren(List<CatalogComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    CatalogModel model = getModel();
                    CatalogComponent comp = (CatalogComponent) model.getFactory().create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    
}
