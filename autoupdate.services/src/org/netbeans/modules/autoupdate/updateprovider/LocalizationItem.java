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

package org.netbeans.modules.autoupdate.updateprovider;

import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.services.*;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalizationItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    
    private Locale locale;
    private String branding;
    private String moduleSpecificationVersion;
    private String localizedName;
    private String localizedDescription;
    private URL distribution;
    private String category;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;

    public LocalizationItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            Locale locale,
            String branding,
            String moduleSpecificationVersion,
            String localizedName,
            String localizedDescription,
            String category,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.locale = locale;
        this.branding = branding;
        this.moduleSpecificationVersion = moduleSpecificationVersion;
        this.localizedName = localizedName;
        this.localizedDescription = localizedDescription;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        this.licenseImpl = licenseImpl;
        this.category = category;
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public Locale getLocale () {
        return this.locale;
    }
    
    public String getBranding () {
        return this.branding;
    }
    
    public String getMasterModuleSpecificationVersion () {
        return this.moduleSpecificationVersion;
    }
    
    public String getLocalizedModuleName () {
        return this.localizedName;
    }
    
    public String getLocalizedModuleDescription () {
        return this.localizedDescription;
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }

    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    public String getCategory () {
        return category;
    }

}
