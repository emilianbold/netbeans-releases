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

/**
 * An interface that should be implemented by catalogs allowing modifications.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface CatalogWriter {

    /**
     * Register new mapping into catalog overwriting existing one.
     * @param publicID a key.
     * @param systemID new systemID or <tt>null</tt> to delete the entry.
     */
    public void registerCatalogEntry(String publicID, String systemID);
}
