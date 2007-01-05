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

import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;

public class SyncUpdateVisitor extends CatalogVisitor.Default implements ComponentUpdater<CatalogComponent> {
    private CatalogComponent target;
    private Operation operation;
    private int index;
    
    public SyncUpdateVisitor() {
    }
    
    public void update(CatalogComponent target, CatalogComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }
    
    public void update(CatalogComponent target, CatalogComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }
    
    private void insert(String propertyName, CatalogComponent component) {
        ((CatalogComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, CatalogComponent component) {
        ((CatalogComponentImpl)target).removeChild(propertyName, component);
    }
    
    public void visit(org.netbeans.modules.xml.retriever.catalog.model.System system) {
        if (target instanceof Catalog) {
            if (operation == Operation.ADD) {
                insert(Catalog.SYSTEM_PROP, system);
            } else {
                remove(Catalog.SYSTEM_PROP, system);
            }
        }
    }
    
    public void visit(NextCatalog nextCatalog) {
        if (target instanceof Catalog) {
            if (operation == Operation.ADD) {
                insert(Catalog.NEXTCATALOG_PROP, nextCatalog);
            } else {
                remove(Catalog.NEXTCATALOG_PROP, nextCatalog);
            }
        }
    }
    
}
