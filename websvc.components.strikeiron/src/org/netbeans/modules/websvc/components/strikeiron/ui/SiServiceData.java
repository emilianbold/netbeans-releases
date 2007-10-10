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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components.strikeiron.ui;

import com.strikeiron.search.MarketPlaceService;
import org.netbeans.modules.websvc.components.ServiceData;

/**
 *
 * @author nam
 */
public class SiServiceData extends ServiceData {
    public static final String DEFAUL_PACKAGE_NAME = "com.strikeiron";
    
    private final MarketPlaceService service;
    private String packageName;
    
    public SiServiceData(MarketPlaceService service) {
        this.service = service;
    }
    
    public String getWsdlURL() {
        return service.getWSDL();
    }
    
    public MarketPlaceService getRawService() {
        return service;
    }

    public String getServiceName() {
        return service.getServiceName();
    }
    
    public String getProviderName() {
        return service.getProviderName();
    }

    public String getDescription() {
        return service.getDescription();
    }

    public String getInfoPage() {
        return service.getInfoPage();
    }
    
    public String getPackageName() {
        if (packageName == null) {
            return DEFAUL_PACKAGE_NAME;
        }
        return packageName;
    }

    public void setPackageName(String packageName) {
        if (DEFAUL_PACKAGE_NAME.equals(packageName)) {
            this.packageName = null;
        }
        this.packageName = packageName;
    }
    
}
