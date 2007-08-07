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
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private String author;
    private String downloadSize;
    private String homepage;
    private String category;
    private Date publishDate;
    private boolean isEager;
    private boolean isAutoload;
    
    private URL distribution;
    private Manifest manifest;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    protected ModuleItem () {}

    public ModuleItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            String author,
            String publishDate,
            String downloadSize,
            String homepage,
            String category,
            Manifest manifest, 
            Boolean isEager,
            Boolean isAutoload,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.manifest = manifest;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        if (publishDate != null) {
            try {
                this.publishDate = Utilities.DATE_FORMAT.parse (publishDate);
            } catch (ParseException pe) {
                Logger.getLogger (ModuleItem.class.getName ()).log (Level.INFO, pe.getMessage (), pe);
            }
        }
        this.licenseImpl = licenseImpl;
        this.author = author;
        this.downloadSize = downloadSize;
        this.homepage = homepage;
        this.category = category;
        this.isEager = isEager;
        this.isAutoload = isAutoload;
    }
    
    public String getCodeName () {
        return codeName;
    }
    
    public String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public String getAuthor () {
        return author;
    }
    
    public String getHomepage () {
        return homepage;
    }
    
    public int getDownloadSize () {
        return Integer.parseInt (downloadSize);
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }
    
    public ModuleInfo getModuleInfo () {        
        if (info == null) {
            Module m = Utilities.toModule(codeName, specificationVersion);
            info = (m != null) ? m : new DummyModuleInfo (manifest.getMainAttributes ());
        }
        return info;
    }
    
    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    public String getCategory () {
        return category;
    }
    
    public String getDate () {
        return publishDate == null ? null : Utilities.DATE_FORMAT.format (publishDate);
    }
    
    public boolean isAutoload () {
        return isAutoload;
    }

    public boolean isEager () {
        return isEager;
    }
    
}
