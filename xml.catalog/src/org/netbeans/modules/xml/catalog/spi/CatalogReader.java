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
package org.netbeans.modules.xml.catalog.spi;

import java.util.*;

/**
 * An interface to catalog content reader.
 * An implementation is responsible for reading catalog content (mappings).
 * <p>
 * Implementation class should provide its BeanInfo describing it.
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
