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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.retriever.catalog.model.System;
import org.w3c.dom.Element;

public class CatalogImpl extends CatalogComponentImpl implements Catalog {
    
    public CatalogImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public CatalogImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.CATALOG));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }

    public List<System> getSystems() {
       return super.getChildren(org.netbeans.modules.xml.retriever.catalog.model.System.class);
    }

    public void addSystem(System sid) {
        appendChild(Catalog.SYSTEM_PROP, sid);
    }

    public void removeSystem(System sid) {
        removeChild(Catalog.SYSTEM_PROP, sid);
    }

    public List<NextCatalog> getNextCatalogs() {
        return super.getChildren(NextCatalog.class);
    }

    public void addNextCatalog(NextCatalog ncat) {
        appendChild(Catalog.NEXTCATALOG_PROP, ncat);
    }

    public void removeNextCatalog(NextCatalog ncat) {
        removeChild(Catalog.NEXTCATALOG_PROP, ncat);
    }
    
    
}
