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

import java.io.IOException;

/**
 * The interface is intended for NetBeans IDE platform integration.
 * <p>
 * IDE Lookup searches <tt>CatalogProvider</tt> instances registered in Lookup area.
 * It is used as Class -> instance bridge as Lookup does not support
 * direct Class registrations.
 *
 * @author  Petr Kuzel
 * @version NetBeans IDE platform integration 1.0
 */
public interface CatalogProvider {

    /**
     * @return A class with public no arg constructor loaded by a ClassLoader 
     *         allowing to load the rest of catalog implementation.
     */
    public Class provideClass() throws IOException, ClassNotFoundException;
}
