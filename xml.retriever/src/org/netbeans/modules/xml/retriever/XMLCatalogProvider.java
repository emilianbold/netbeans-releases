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

package org.netbeans.modules.xml.retriever;

import java.net.URI;
import org.openide.filesystems.FileObject;

/**
 * The XMLCatalogProvider allows a {@link org.netbeans.api.project.Project} to
 * convey information about the XML catalog file usage within the project.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Chris Webster
 * @author Girish
 */
public interface XMLCatalogProvider {
    /**
     * This constant identifies source roots which can be used to store
     * retrieved XML artifacts.
     */
    public static final String TYPE_RETRIEVED = "retrieved"; //NOI18N
    
    /**
     * Provide the project root relative reference to the catalog file
     * for the specified XML artifact.
     * @param targetFile represents the XML artifact which may require use of
     * a catalog for resolution. The common case, a single project wide catalog
     * file, would delegate to #getProjectWideCatalog().
     * @return a URI representing the relative path from the project root to the
     * catalog file (i.e. ./catalog.xml which is the default)
     */
    URI getCatalog(FileObject targetFile);
    
    /**
     * Provide the project root relative reference to the catalog file
     * @return a URI representing the relative path from the project root to the
     * catalog file (i.e. ./catalog.xml which is the default)
     */
    URI getProjectWideCatalog();
    
}
