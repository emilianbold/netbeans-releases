/*
 * NextCatalogImpl.java
 *
 * Created on December 6, 2006, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.net.URI;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogAttributes;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class NextCatalogImpl extends CatalogComponentImpl implements
        org.netbeans.modules.xml.retriever.catalog.model.NextCatalog{
    
    public NextCatalogImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public NextCatalogImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.NEXTCATALOG));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getCatalogAttr() {
        return getAttribute(CatalogAttributes.catalog);
    }

    public void setCatalogAttr(URI uri) {
        super.setAttribute(CATALOG_ATTR_PROP, CatalogAttributes.catalog, uri.toString());
    }
    
}
