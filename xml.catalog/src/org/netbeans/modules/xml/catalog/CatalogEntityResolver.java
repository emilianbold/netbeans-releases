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

import org.xml.sax.*;
import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.lib.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;

import org.netbeans.api.xml.services.*;
import org.openide.util.Lookup;

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
public class CatalogEntityResolver extends UserCatalog implements EntityResolver {

    /** Creates new CatalogEntityResolver */
    public CatalogEntityResolver() {
    }

    public EntityResolver getEntityResolver(Lookup ctx) {
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
        
        return result;
        
    }
    
    /**
     * Return all known public IDs.
     */
    public Iterator getPublicIDs(Lookup ctx) {
        
        IteratorIterator ret = new IteratorIterator();
        
        CatalogSettings mounted = CatalogSettings.getDefault();
        Iterator it = mounted.getCatalogs( new Class[] {CatalogReader.class});

        while (it.hasNext()) {
            CatalogReader next = (CatalogReader) it.next();
            ret.add(next.getPublicIDs());            
        }
        
        return ret;
    }    

}
