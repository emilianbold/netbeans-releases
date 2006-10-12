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
package org.netbeans.modules.xml.retriever.catalog;

import org.netbeans.modules.xml.retriever.catalog.impl.CatalogModelFactoryImpl;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nam Nguyen
 */
public abstract class CatalogWriteModelFactory extends CatalogModelFactory {
    
    /**
     * Given a FileObject that belongs to a project this method will return a
     * CatalogModel object specific to it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * If FileOwnerQuery.getOwner(anyFileObjectExistingInAProject); returns null
     * then assersion error will occur.
     * @param anyFileObjectExistingInAProject any FileObject inside a project for which CatalogModel is needed
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogWriteModel getCatalogWriteModelForProject(FileObject anyFileObjectExistingInAProject) throws CatalogModelException;
    
     /**
     * Given a FileObject this method will return a CatalogWriteModel object specific to it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * @param fileObjectOfCatalogFile any FileObject on which the catalog entries have to be created or appended.
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogWriteModel getCatalogWriteModelForCatalogFile(FileObject fileObjectOfCatalogFile) throws CatalogModelException;
    

    private static CatalogWriteModelFactory implObj = null;
    
    public static CatalogWriteModelFactory getInstance(){
        if(implObj == null) {
            implObj = new CatalogModelFactoryImpl();
        }
        return implObj;
    }
}
