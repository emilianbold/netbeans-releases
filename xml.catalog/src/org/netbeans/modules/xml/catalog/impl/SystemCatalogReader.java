/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.impl;

import java.awt.Image;
import java.lang.reflect.*;
import java.util.*;
import java.io.Serializable;

import org.xml.sax.*;

import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;

import org.netbeans.modules.xml.catalog.spi.*;

/**
 * Read mapping redistered in IDE system resolver/catalog.
 * It uses knowledge of IDE catalog implementation.
 *
 * @author  Petr Kuzel
 * @version 1.0
 *
 */
public class SystemCatalogReader implements CatalogReader, Serializable {
    /** */
    private static final boolean DEBUG = true;

    /** Serial Version UID */
    private static final long serialVersionUID = -6353123780493006631L;
    
    /** Creates new SystemCatalogReader */
    public SystemCatalogReader() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     */
    public Iterator getPublicIDs() {

        if ( DEBUG ) {
            Util.debug ("--> SystemCatalogReader.getPublicIDs");
        }

            // get instance of system resolver that contains the catalog
            
            Lookup.Template templ = new Lookup.Template(EntityCatalog.class);            
            Lookup.Result res = Lookup.getDefault().lookup(templ);

            if ( DEBUG ) {
                Util.debug ("-*- SystemCatalogReader.getPublicIDs: lookup.result = " + res);
            }

            HashSet set = new HashSet();
            boolean found = false;

            Iterator it = res.allInstances().iterator();
            while (it.hasNext()) {                
                EntityCatalog next = (EntityCatalog) it.next();

                if ( DEBUG ) {
                    Util.debug ("-*- SystemCatalogReader.getPublicIDs: next = " + next);
                }

        try {
                Field uriMapF = next.getClass().getDeclaredField("id2uri");  // NOI18N
                if (uriMapF == null) continue;

                uriMapF.setAccessible(true);
                found = true;

                Map uris = (Map) uriMapF.get(next);
                if (uris != null) {
                   set.addAll(uris.keySet());               
                }
        } catch (NoSuchFieldException ex) {
            if ( DEBUG ) {
                Util.debug ("<-! SystemCatalogReader.getPublicIDs: ex = " + ex);
                ex.printStackTrace();
            }
            return null;
        } catch (IllegalAccessException ex) {
            if ( DEBUG ) {
                Util.debug ("<-! SystemCatalogReader.getPublicIDs: ex = " + ex);
                ex.printStackTrace();
            }
            return null;
        } catch (IllegalArgumentException ex) {
            if ( DEBUG ) {
                Util.debug ("<-! SystemCatalogReader.getPublicIDs: ex = " + ex);
                ex.printStackTrace();
            }
            return null;
        }

            }
            if ( DEBUG ) {
                Util.debug ("-*- SystemCatalogReader.getPublicIDs: found = " + found);
            }
            if (found = false)
                return null;

            if ( DEBUG ) {
                Util.debug ("<-- SystemCatalogReader.getPublicIDs: set = " + set);
            }
            return set.iterator();
            
            
    }
    
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     */
    public String getSystemID(String publicId) {
        
        try {
            EntityResolver sysResolver = EntityCatalog.getDefault();
            
            if (sysResolver == null) return null;

            InputSource in = sysResolver.resolveEntity(publicId, null);
            if (in == null) return null;
            
            return in.getSystemId();
            
        } catch (java.io.IOException ex) {            
            return null;
        } catch (SAXException ex) {
            return null;
        }
    }

    /**
     * No refresh is necessary, it is always fresh in RAM.
     */
    public void refresh() {
    }
    
   
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @see addCatalogListener
     */
    public void removeCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }

    /*
     * System catalog is singleton.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return getClass().equals(obj.getClass());
    }
    
    public int hashCode() {
        return getClass().hashCode();
    }
}
