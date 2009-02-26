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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.project;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.NbCatalogManager;
import org.apache.xml.resolver.tools.NbCatalogResolver;


/**
 * Helper class to read catalog.xml
 * @author Sreenivasan Genipudi
 */
public class ApacheResolverHelper {
    /**
     * Constructor
     */
    public ApacheResolverHelper() {
    }
    
    /**
     * Checks if the given location is  listed in catalog.xml
     * @param catalogLocation Location of Catalog.xml
     * @param locationURI The location URI to look for
     * @return true if present else returns false
     */
    public static boolean isPresent(String catalogLocation, String locationURI) {
        try {
            if (getURI(catalogLocation, locationURI) != null) {
                return true;
            }
        }catch (Exception ex) {
            
        }
        return false;
    }

    public static String getURI(String catalogLocation, String locationURI) {
        NbCatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;
    
        
        NbCatalogManager manager = new NbCatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new NbCatalogResolver(manager);
        
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        try {
         apacheCatalogResolverObj.parseCatalog(catalogLocation);
        }catch (Exception me) {
            throw new RuntimeException(me);
        }
        
        String result = null;
        try {
            result = apacheCatalogResolverObj.resolveSystem(locationURI);
        } catch (MalformedURLException ex) {
            result = null;
        } catch (IOException ex) {
            result = null;
        }     
        if (result != null && result.equals("")) {
            result = null;
        }
        return result;
    }
    

    public static boolean isPresent(ArrayList<File> catalogLocationList, String locationURI) {
        NbCatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;    

        
        NbCatalogManager manager = new NbCatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new NbCatalogResolver(manager);
        
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        for (File catalogLocation : catalogLocationList ) {
            try {
             if (catalogLocation != null && catalogLocation.exists()) {
                 apacheCatalogResolverObj.parseCatalog(catalogLocation.getAbsolutePath());
             } else {
                continue;
             }
            }catch (Exception me) {
                throw new RuntimeException(me);
            }
            
            String result = null;
            try {
                result = apacheCatalogResolverObj.resolveSystem(locationURI);
                if (result != null && (!result.equals(""))) {
                    return true;
                }

            } catch (MalformedURLException ex) {
                result = "";
            } catch (IOException ex) {
                result = "";
            }     
            return false;
        } 
        return false;
    }
    
}
