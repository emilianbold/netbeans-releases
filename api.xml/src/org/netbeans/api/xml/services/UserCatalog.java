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
package org.netbeans.api.xml.services;

import java.util.Iterator;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.openide.util.Lookup;

// for JavaDoc
import org.openide.xml.EntityCatalog;

/**
 * Factory of user's catalog service instaces. A client cannot instantiate and
 * use it directly. It can indirectly use instances are registeretered in a 
 * {@link Lookup} by providers.
 * <p>
 * It is user's level equivalent of system's entity resolution support 
 * {@link EntityCatalog} in OpenIDE API. Do not mix these,
 * always use <code>UserCatalog</code> while working with user's files.
 * User may, depending on provider implementation, use system catalog as
 * user's one if needed.
 *
 * @author  Libor Kramolis
 * @author  Petr Kuzel
 * @since   0.5
 * @deprecated XML tools API candidate
 */
public abstract class UserCatalog {
    
    // abstract to avoid instantionalization by API clients
    
    /**
     * Utility method looking up for the first instance in default Lookup.
     * @return UserCatalog registered in default Lookup or <code>null</code>.
     */
    public static UserCatalog getDefault() {
        return (UserCatalog) Lookup.getDefault().lookup(UserCatalog.class);
    }
    
    /**
     * User's JAXP/TrAX <code>URIResolver</code>.
     * @return URIResolver or <code>null</code> if not supported.
     */
    public URIResolver getURIResolver() {
        return null;
    }
    
    /**
     * User's SAX <code>EntityResolver</code>.
     * @return EntityResolver or <code>null</code> if not supported.
     */
    public EntityResolver getEntityResolver() {
        return null;
    }
            
    /**
     * Read-only "sampled" iterator over all registered entity public IDs.
     * @return all known public IDs or <code>null</code> if not supported. 
     */
    // Svata suggested here a live collection, but he accepts this solution if it
    // is only for informational purposes. It is.
    public Iterator getPublicIDs() {
        return null;
    }
}
