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

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponentFactory;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.w3c.dom.Element;

public class CatalogModelImpl extends AbstractDocumentModel<CatalogComponent> implements CatalogModel {
    private CatalogComponentFactory factory;
    private Catalog catalog;
    
    public CatalogModelImpl(ModelSource source) {
        super(source);
        factory = new CatalogComponentFactoryImpl(this);
    }
    
    public Catalog getRootComponent() {
        return catalog;
    }

    protected ComponentUpdater<CatalogComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public CatalogComponent createComponent(CatalogComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public Catalog createRootComponent(Element root) {
        Catalog newRegistry = (Catalog) getFactory().create(root, null);
        if (newRegistry != null) {
            catalog = newRegistry;
        }
        return newRegistry;
    }

    public CatalogComponentFactory getFactory() {
        return factory;
    }
    
    public Set<QName> getQNames() {
        return CatalogQNames.getMappedQNames();
    }
        
}
