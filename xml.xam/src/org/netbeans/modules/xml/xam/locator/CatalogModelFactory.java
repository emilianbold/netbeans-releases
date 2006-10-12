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

package org.netbeans.modules.xml.xam.locator;

import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Returns a CatalogModel for a project
 * @author girix
 */
public abstract class CatalogModelFactory {
    
    /**
     * Given a ModelSource this method will return a Locator object specific to the it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * @param modelSource a not null model source for which catalog model is requested.
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException;
    
    public abstract LSResourceResolver getLSResourceResolver();
    
    private static CatalogModelFactory implObj = null;
    
    public static CatalogModelFactory getDefault(){
        if(implObj == null) {
            implObj = (CatalogModelFactory) Lookup.getDefault().lookup(CatalogModelFactory.class);
        }
        if (implObj == null) {
            implObj = new Default();
        }
        return implObj;
    }
    
    public static class Default extends CatalogModelFactory {
        public CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException {
            return (CatalogModel) modelSource.getLookup().lookup(CatalogModel.class);
        }

        public LSResourceResolver getLSResourceResolver() {
            return null;
        }
    }
}
