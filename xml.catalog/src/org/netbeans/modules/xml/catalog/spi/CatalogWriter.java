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
