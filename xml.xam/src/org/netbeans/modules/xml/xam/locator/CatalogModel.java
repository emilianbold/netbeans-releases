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

/*
 * Locator.java
 *
 * Created on March 29, 2006, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xam.locator;

import java.net.URI;
import org.netbeans.modules.xml.xam.ModelSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 *
 * @author girix
 */
public interface CatalogModel extends EntityResolver, LSResourceResolver{
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return ModelSource object containing the concrete FileObject
     * of the locally stored file. This method will just look up in the public catalog file
     * and return result. It Will NOT do relative path resolution.
     *
     * @param locationURI
     * @return ModelSource
     * @throws CatalogModelException
     * CatalogModelException will be throw for the following:<B></B>
     * 1. If the file that was supposed to be there but not found. This case a
     * FileNotFoundException is wrapped inside<B></B>
     * 2. If a (java)File object could not be created from the retrived catalog
     * entry.URISyntaxException will be wrapped inside DepResolverException.<B></B>
     * 3. IOException will be wrapped around if a (NB)FileObject could not be
     * created from the File object for various reasons by NB module
     */
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException;
    
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return ModelSource object containing the concrete FileObject
     * of the locally stored file. This method will just look up in the public catalog file
     * and return result. If not found in the catalog will then do relative path resolution
     * against modelSourceOfSourceDocument's FileObject. Relative locations should be resolved using this method
     *
     * @param locationURI
     * @param modelSourceOfSourceDocument
     * @return ModelSource
     * @throws CatalogModelException
     * CatalogModelException will be throw for the following:<B></B>
     * 1. If the file that was supposed to be there but not found. This case a
     * FileNotFoundException is wrapped inside<B></B>
     * 2. If a (java)File object could not be created from the retrived catalog
     * entry.URISyntaxException will be wrapped inside DepResolverException.<B></B>
     * 3. IOException will be wrapped around if a (NB)FileObject could not be
     * created from the File object for various reasons by NB module
     */
    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException;
    
}
