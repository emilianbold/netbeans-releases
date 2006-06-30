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
package org.netbeans.modules.xml.catalog.spi;

import java.util.*;

/**
 * An interface to catalog content reader.
 * An implementation is responsible for reading catalog content (mappings).
 * <p>
 * Implementation class should provide its BeanInfo describing it.
 * <p>Should also implement {@link CatalogDescriptor} and may also implement
 * {@link CatalogWriter} and/or {@link org.xml.sax.EntityResolver} as needed.</p>
 *
 * @author Petr Kuzel
 */
public interface CatalogReader {

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator getPublicIDs();
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh();
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId);
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name);
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    public String resolvePublic(String publicId);
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l);
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l);
    
}
