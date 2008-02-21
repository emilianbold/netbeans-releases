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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.util;

import java.io.IOException;
import java.net.URL;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author nam
 */
public class WsdlUtil {

    public static WsdlData findWsdlData(String url, String serviceName) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            return manager.findWsdlData(url, serviceName);
        }
        return null;
    }
    
    public static WsdlData addWsdlData(String url, String packageName) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            return manager.addWsdlData(url, packageName);
        }
        return null;
    }
    
    public static WsdlData getWsdlData(String url, String serviceName, boolean synchronous) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            return manager.getWsdlData(url, serviceName, synchronous);
        } 
        return null;
    }
    
    public static void removeWsdlData(WsdlData data) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            manager.removeWsdlData(data.getOriginalWsdlUrl(), data.getName());
        }
    }    

    public static void saveWsdlData(WsdlData data) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            manager.save(data);
        }
    }

    public static void refreshWsdlData(WsdlData data) {
        WsdlDataManager manager = Lookup.getDefault().lookup(WsdlDataManager.class);
        if (manager != null) {
            manager.refresh(data);
        }
    }

    public static boolean isJAXRPCAvailable() {
        return getWebServiceSupportLibDef(false) != null;
    }

    /**
     * @return The library definition containing the web service support jar files, null if it does not exist
     */
    public static Library getWebServiceSupportLibDef(boolean isJ2EE_15) {
        String libraryName = (isJ2EE_15) ? "jaxws21" : "jaxrpc16";
        Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
        return libDef;
    }

    public static String getCatalogForWsdl(String wsdlUrl) {
        String serviceDir = getServiceDirName(wsdlUrl);
        return "/" + serviceDir + "/catalog/catalog.xml";
    }
    
    public static String getServiceDirName(String wsdlUrl) {
        try {
            URL url;
            url = new URL(wsdlUrl);

            String urlPath = url.getPath();
            int start;
            if (url.getProtocol().toLowerCase().startsWith("file")) { // NOI18N
                start = urlPath.lastIndexOf(System.getProperty("path.separator")); // NOI18N
                start = (start < 0) ? urlPath.lastIndexOf("/") : start; // NOI18N
            } else {
                start = urlPath.lastIndexOf("/");
            }
            start++;

            return urlPath.substring(start).replace('.', '-'); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
}
