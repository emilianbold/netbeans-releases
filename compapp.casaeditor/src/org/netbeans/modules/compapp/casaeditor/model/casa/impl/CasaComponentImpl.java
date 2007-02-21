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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.*;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public abstract class CasaComponentImpl extends AbstractDocumentComponent<CasaComponent>
        implements CasaComponent {

   public CasaComponentImpl(CasaModel model, Element e) {
        super(model, e);
    }
    
    protected String getNamespaceURI() {
        return CasaQName.CASA_NS_URI;
    }
        
    public CasaModel getModel() {
        return (CasaModel) super.getModel();
    }
    
    protected void populateChildren(List<CasaComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            CasaModel model = getModel();
            CasaComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    CasaComponent comp = componentFactory.create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    protected Object getAttributeValueOf(Attribute attribute, String stringValue) {
        return stringValue;
    }  
    
    public static Element createElementNS(CasaModel model, CasaQName rq) {
        return model.getDocument().createElementNS(
                rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
    
    protected <T extends ReferenceableCasaComponent> NamedComponentReference<T> 
            resolveGlobalReference(            
            Class<T> c, Attribute attrName) {
        
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    public <T extends ReferenceableCasaComponent> NamedComponentReference<T> 
            createReferenceTo(T target, Class<T> type) {
        
        return new GlobalReferenceImpl<T>(target, type, this);
    }
}

