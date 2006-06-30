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
     *         allowing to load the rest of catalog implementation
     *         (must implement {@link CatalogReader})
     */
    public Class provideClass() throws IOException, ClassNotFoundException;
}
