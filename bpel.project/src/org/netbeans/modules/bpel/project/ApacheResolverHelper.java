/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.project;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;

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
        CatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;    
    
        
        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new CatalogResolver(manager);
        
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
        CatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;    

        
        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new CatalogResolver(manager);
        
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
