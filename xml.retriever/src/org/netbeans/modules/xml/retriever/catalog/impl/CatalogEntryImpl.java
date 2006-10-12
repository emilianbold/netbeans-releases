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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 *
 * @author girix
 */
public class CatalogEntryImpl implements CatalogEntry {
    private CatalogElement entryType;
    //mapping string
    private String source;
    //mapped string
    private String target;
    
    private CatalogModel thisCatModel = null;
    
    private HashMap<String,String> extraAttributeMap = null;
    
    /**
     * one example of source is: systemId attribute value for system tag of catalog
     * one example of mappingEntity is: uri attribute value for system tag of catalog
     *
     * @param entryType - Catalog entry type as in public, system, rewriteSystem, etc.
     * @param source - source URL/String
     * @param target - Target URL/String
     */
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
    }
    
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity, HashMap<String,String> extraAttribMap) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
        this.extraAttributeMap = extraAttribMap;
    }
    
    public CatalogElement getEntryType(){
        return entryType;
    }
    
    public String getSource(){
        return this.source;
    }
    
    public String getTarget(){
        return this.target;
    }
    
    public HashMap<String,String> getExtraAttributeMap(){
        return extraAttributeMap;
    }
    
    public boolean isValid() {
        if(thisCatModel == null)
            return false;
        ModelSource ms = null;
        try {
            //TODO remove null
            ms = thisCatModel.getModelSource(new URI(source), null);
        } catch (URISyntaxException ex) {
            return false;
        } catch (CatalogModelException ex) {
            return false;
        }
        if(ms != null)
            return true;
        return false;
    }
    
    public void setCatalogModel(CatalogModel thisCatModel){
        this.thisCatModel = thisCatModel;
    }
}
