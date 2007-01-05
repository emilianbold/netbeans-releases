/*
 * SystemImpl.java
 *
 * Created on December 6, 2006, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogAttributes;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.System;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class SystemImpl extends CatalogComponentImpl implements
        org.netbeans.modules.xml.retriever.catalog.model.System{
    
    public SystemImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public SystemImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.SYSTEM));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getSystemIDAttr() {
        return getAttribute(CatalogAttributes.systemId);
    }
    
    public String getURIAttr() {
        return getAttribute(CatalogAttributes.uri);
    }
    
    public String getXprojectCatalogFileLocationAttr() {
        return getAttribute(CatalogAttributes.xprojectCatalogFileLocation);
    }
    
    public String getReferencingFileAttr() {
        return getAttribute(CatalogAttributes.referencingFile);
    }
    
    public void setSystemIDAttr(URI uri) {
        super.setAttribute(SYSTEMID_ATTR_PROP, CatalogAttributes.systemId,
                uri.toString());
    }
    
    public void setURIAttr(URI uri) {
        super.setAttribute(URI_ATTR_PROP, CatalogAttributes.uri,
                uri.toString());
    }
    
    public void setXprojectCatalogFileLocationAttr(URI uri) {
        super.setAttribute(XPROJECTREF_ATTR_PROP, 
                CatalogAttributes.xprojectCatalogFileLocation, uri.toString());
    }
    
    public void setReferencingFileAttr(URI uri) {
        super.setAttribute(REFFILE_ATTR_PROP, CatalogAttributes.referencingFile,
                uri.toString());
    }

}
