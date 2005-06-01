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

import org.xml.sax.*;

import org.netbeans.modules.xml.catalog.spi.CatalogReader;

/**
 * Represents catalog entry keyed by a public ID.
 * The implementation is not cached it queries underlaying catalog.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class CatalogEntry extends Object {

    private final String publicID;
    private final CatalogReader catalog;
            
    /** Creates new CatalogEntry */
    public CatalogEntry(String publicID, CatalogReader catalog) {
        this.publicID = publicID;
        this.catalog = catalog;
    }
    
    CatalogReader getCatalog() {
        return catalog;
    }

    /**
     * Use CatalogReader or alternatively EntityResolver interface to resolve the PID.
     */
    public String getSystemID() {
        String sid = catalog.getSystemID(publicID);
        if (sid == null) {
            if (catalog instanceof EntityResolver) {
                try {
                    InputSource in = ((EntityResolver) catalog).resolveEntity(publicID, null);
                    if (in != null) {
                        sid = in.getSystemId();
                    }
                } catch (Exception ex) {
                    // return null;
                }
            }
        }

        //#53710 URL space canonization (%20 form works in most cases)
        String patchedSystemId = sid;
        if (patchedSystemId != null) {
            patchedSystemId = patchedSystemId.replaceAll("\\+", "%20"); // NOI18N
            patchedSystemId = patchedSystemId.replaceAll("\\ ", "%20"); // NOI18N
            return patchedSystemId;
        }

        return null;
    }
    
    public String getPublicID() {
        return publicID;
    }
    
    public String getName() {
        return publicID;
    }
    
    public String toString() {
        return publicID + " => " + getSystemID(); // NOI18N
    }
}
