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
package org.netbeans.modules.xml.catalog.impl.sun;

import java.io.IOException;

import org.netbeans.modules.xml.catalog.spi.CatalogProvider;

/**
 * Provide class representing Catalog reader class.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class SunCatalogProvider implements CatalogProvider {

    public Class provideClass() throws IOException, ClassNotFoundException {
        return Catalog.class;
    }
}
