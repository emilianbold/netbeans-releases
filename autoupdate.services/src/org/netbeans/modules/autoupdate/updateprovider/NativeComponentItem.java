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

import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import java.util.Set;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public class NativeComponentItem extends UpdateItemImpl {
    
    private boolean isInstalled;
    private String codeName;
    private String specificationVersion;
    private Set<String> dependencies;
    private String displayName;
    private String description;
    private String downloadSize;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    public NativeComponentItem (
            boolean isInstalled,
            String codeName,
            String specificationVersion,
            String downloadSize,
            Set<String> dependencies,
            String displayName,
            String description,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            CustomInstaller installer,
            CustomUninstaller uninstaller,
            UpdateLicenseImpl license) {
        this.isInstalled = isInstalled;
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.dependencies = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.licenseImpl = license;
        this.downloadSize = downloadSize;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, installer, uninstaller);
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public String getDisplayName () {
        return this.displayName;
    }
    
    public String getDescription () {
        return this.description;
    }
    
    public Set<String> getDependenciesToModules () {
        return this.dependencies;
    }
    
    public int getDownloadSize () {
        return Integer.parseInt (downloadSize);
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        assert false : "Invalid call getUpdateLicenseImpl() on NativeComponentItem.";
        return this.licenseImpl;
    }
    
    public String getAgreement () {
        assert false : "Invalid call getAgreement() on NativeComponentItem.";
        return "";
        //return licenseImpl.getAgreement ();
    }

    public String getCategory () {
        throw new UnsupportedOperationException ("Not supported yet.");
    }
}
