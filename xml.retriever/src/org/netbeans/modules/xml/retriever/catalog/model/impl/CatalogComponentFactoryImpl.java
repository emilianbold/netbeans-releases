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
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponentFactory;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.w3c.dom.Element;

public class CatalogComponentFactoryImpl implements CatalogComponentFactory {
    private CatalogModelImpl model;
    
    public CatalogComponentFactoryImpl(CatalogModelImpl model) {
        this.model = model;
    }
    
    public CatalogComponent create(Element element, CatalogComponent context) {
        if (context == null) {
            if (areSameQName(CatalogQNames.CATALOG, element)) {
                return new CatalogImpl(model, element);
            } else {
                return null;
            }
        } else {
            return new CreateVisitor().create(element, context);
        }
    }
    
    
    public NextCatalog createNextCatalog() {
        return new NextCatalogImpl(model);
    }
    
    public org.netbeans.modules.xml.retriever.catalog.model.System createSystem() {
        return new SystemImpl(model);
    }
    
    public Catalog createCatalog() {
        return new CatalogImpl(model);
    }
    
    public static boolean areSameQName(CatalogQNames q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
    
    public static class CreateVisitor extends CatalogVisitor.Default {
        Element element;
        CatalogComponent created;
        
        CatalogComponent create(Element element, CatalogComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(CatalogQNames q) {
            return areSameQName(q, element);
        }
        
        public void visit(Catalog context) {
            if (isElementQName(CatalogQNames.SYSTEM)) {
                created = new SystemImpl((CatalogModelImpl)context.getModel(), element);
            }
            if (isElementQName(CatalogQNames.NEXTCATALOG)) {
                created = new NextCatalogImpl((CatalogModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(org.netbeans.modules.xml.retriever.catalog.model.System context) {
            
        }
        
        public void visit(NextCatalog context) {
            
        }
        
    }
}
