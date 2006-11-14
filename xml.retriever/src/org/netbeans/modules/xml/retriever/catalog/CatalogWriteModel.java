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
 * CatalogModel.java
 *
 * Created on October 11, 2005, 1:11 AM
 */

package org.netbeans.modules.xml.retriever.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.*;
import org.openide.filesystems.FileObject;

/**
 * API interface for all the operations exposed
 * by the CatalogModel. There will be one Catalog file per Project.
 * @author girix
 */
public interface CatalogWriteModel extends CatalogModel {
    
    public static final String CATALOG_FILE_EXTENSION = ".xml";
    
    public static final String PUBLIC_CATALOG_FILE_NAME = "catalog";
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return the parget URI after looking up in the public catalog file
     * This method will just look up in the public catalog file and return result.
     * If not found in the catalog a null will be returned.
     *
     * @param locationURI
     * @return URI
     */
    public URI searchURI(URI locationURI);
    
    
    /**
     * Adds an URI to FileObject (in the same project) mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param fileObj
     */
    public void addURI(URI locationURI, FileObject fileObj) throws IOException;
    
    /**
     * Adds an URI to URI mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param alternateURI
     */
    
    public void addURI(URI locationURI, URI alternateURI) throws IOException;
    
    /**
     * Remove a URI from the catalog.
     * @param locationURI  - locationURI to be removed.
     */
    public void removeURI(URI locationURI) throws IOException;
    
    
    /**
     * Returns list of all registered catalog entries
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     */
    public Collection<CatalogEntry> getCatalogEntries();
    
    
    /**
     * This method tell if the resolver is in a sane state to retrive the correct values.
     * If false is returned means there is some problem with the resolver. For more information
     * call getState() to get the exact status message. This method should be called before calling
     * most of the resolver methods.
     */
    public boolean isWellformed();
    
    
    /**
     * Returns the current satus of the resolver.
     * Consult the return value and display appropriate messages to the user
     */
    public DocumentModel.State getState();
    
    
    /**
     * Returns the FileObject of the catalog file that this object is bound to.
     */
    public FileObject getCatalogFileObject();
    
    public void addPropertychangeListener(PropertyChangeListener pcl);
    
    public void removePropertyChangeListener(PropertyChangeListener pcl);
    
    
    /**
     * Adds nextCatalogFileURI to the catalog file as nextCatalog entry. If
     * relativize is true and nextCatalogFileURI is absolute, then nextCatalogFileURI is
     * relativized against this catalog file URI itself before writing.
     */
    public void addNextCatalog(URI nextCatalogFileURI, boolean relativize)  throws IOException;
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI)  throws IOException;
    
    
}
