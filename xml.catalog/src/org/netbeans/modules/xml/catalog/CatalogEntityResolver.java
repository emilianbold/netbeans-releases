/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog;

import java.util.*;
import java.io.*;
import java.net.URL;

import org.xml.sax.*;
import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.lib.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;

import org.netbeans.api.xml.services.*;
import org.openide.util.Lookup;
import javax.xml.transform.URIResolver;

/**
 * An entity resolver that can resolve all registrations
 * in catalogs mounted by a user.
 * This is not exposed catalog package API. The
 * package funtionality is exposed via registering
 * this entity resolver in XMLDataObject resolver chain.
 * <p>
 * The class is public only for internal XML module reasons.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CatalogEntityResolver extends UserCatalog implements EntityResolver, URIResolver {

    /** Creates new CatalogEntityResolver */
    public CatalogEntityResolver() {
    }

    public EntityResolver getEntityResolver() {
        return this;
    }
    
    /**
     * User's JAXP/TrAX <code>URIResolver</code>.
     * @return URIResolver or <code>null</code> if not supported.
     */
    public URIResolver getURIResolver() {
        return this;
    }
    
    // SAX interface method implementation
    public InputSource resolveEntity(String publicId,String systemId) 
        throws SAXException, IOException {
        InputSource result = null;            
        Iterator it = null;
        
        // try to use full featured entiry resolvers
        
        CatalogSettings mounted = CatalogSettings.getDefault();
        it = mounted.getCatalogs( new Class[] {EntityResolver.class});

        while (it.hasNext()) {
            EntityResolver next = (EntityResolver) it.next();
            result = next.resolveEntity(publicId, systemId);
            if (result != null) break;
        }
        
        // fallback to ordinaly readers        
        
        if (result == null && publicId != null) {
            
            it = mounted.getCatalogs(new Class[] {CatalogReader.class});

            while (it.hasNext()) {
                CatalogReader next = (CatalogReader) it.next();
                String sid = next.getSystemID(publicId);
                if (sid != null) {
                    result =  new InputSource(sid);
                    break;
                }
            }
        }
        
        // return result (null is allowed)

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("CatalogEntityResolver:PublicID: " + publicId + ", " + systemId + " => " + (result == null ? "null" : result.getSystemId())); // NOI18N

        // #56103 bootstrap XML catalog DTD
        if (result == null && "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN".equals(publicId)) {  // NOi18N
            URL url = org.apache.xml.resolver.Catalog.class.getResource("etc/catalog.dtd"); // NOI18N
            result = new InputSource(url.toExternalForm());
        }

        //#53710 URL space canonization (%20 form works in most cases)
        if (result != null) {
            String patchedSystemId = result.getSystemId();
            if (patchedSystemId != null) {
                patchedSystemId = patchedSystemId.replaceAll("\\+", "%20"); // NOI18N
                patchedSystemId = patchedSystemId.replaceAll("\\ ", "%20"); // NOI18N
                result.setSystemId(patchedSystemId);
            }
        }
        return result;
        
    }
    
    /**
     * Return all known public IDs.
     */
    public Iterator getPublicIDs() {
        
        IteratorIterator ret = new IteratorIterator();
        
        CatalogSettings mounted = CatalogSettings.getDefault();
        Iterator it = mounted.getCatalogs( new Class[] {CatalogReader.class});

        while (it.hasNext()) {
            CatalogReader next = (CatalogReader) it.next();
            Iterator ids = next.getPublicIDs();
            if (ids != null) {
                ret.add(ids);
            }
        }
        
        return ret;
    }    

    public javax.xml.transform.Source resolve(String publicId, String systemId) 
        throws javax.xml.transform.TransformerException {
            
        //throws SAXException, IOException {

        javax.xml.transform.Source result = null;            
        
        // try to use full featured entiry resolvers
        
        CatalogSettings mounted = CatalogSettings.getDefault();

        if (publicId != null) {
            
            Iterator it = mounted.getCatalogs(new Class[] {CatalogReader.class});

            while (it.hasNext()) {
                CatalogReader next = (CatalogReader) it.next();
                try {
                    String sid=null;
                    if (publicId.startsWith("urn:publicid:")) { //NOI18N
                        // resolving publicId from catalog
                        String urn = publicId.substring(13);
                        sid=next.resolvePublic(URNtoPublic(urn));
                    } else sid = next.resolveURI(publicId);
                    if (sid != null) {
                        javax.xml.transform.Source source =  new javax.xml.transform.sax.SAXSource();
                        source.setSystemId(sid);
                        result=source;
                        break;
                    }
                } catch (java.lang.Error error) {}
            }
        }
        
        // return result (null is allowed)

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("CatalogEntityResolver:PublicID: " + publicId + ", " + systemId + " => " + (result == null ? "null" : result.getSystemId())); // NOI18N
        return result;        
    }
    
    /** Conversion of URN string to public identifier
     *  see : http://www.faqs.org/rfcs/rfc3151.html
     */
    private String URNtoPublic(String urn) {
        return urn.replace('+',' ').replaceAll(":","//").replaceAll(";","::").replaceAll("%2B","+").replaceAll("%3A",":").replaceAll("%2F","/").replaceAll("%3B",";").replaceAll("%27","'").replaceAll("%3F","?").replaceAll("%23","#").replaceAll("%25","%");
    }
}
