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
 */
public abstract class UserCatalog {
    
    // abstract to avoid instantionalization by API clients
    
    /**
     * Utility method looking up for an instance in default Lookup.
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
