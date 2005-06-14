/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import org.netbeans.modules.apisupport.project.ManifestManager;

final class NetBeansOrgEntry extends AbstractEntryWithSources {
    
    private final File nball;
    private final String cnb;
    private final String path;
    private final File cluster;
    private final String module;
    private final String cpext;
    private final String releaseVersion;
    private final String specVersion;
    private final String[] providedTokens;
    private final ManifestManager.PackageExport[] publicPackages;
    private final boolean deprecated;
    
    public NetBeansOrgEntry(File nball, String cnb, String path, File cluster,
            String module, String cpext, String releaseVersion, String specVersion,
            String[] providedTokens, ManifestManager.PackageExport[] publicPackages,
            boolean deprecated) {
        this.nball = nball;
        this.cnb = cnb;
        this.path = path;
        this.cluster = cluster;
        this.module = module;
        this.cpext = cpext;
        this.releaseVersion = releaseVersion;
        this.specVersion = specVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.deprecated = deprecated;
    }
    
    public File getSourceLocation() {
        return new File(nball, path.replace('/', File.separatorChar));
    }
    
    public String getNetBeansOrgPath() {
        return path;
    }
    
    public File getJarLocation() {
        return new File(getClusterDirectory(), module.replace('/', File.separatorChar));
    }
    
    public File getDestDir() {
        return new File(nball, ModuleList.DEST_DIR_IN_NETBEANS_ORG);
    }
    
    public String getCodeNameBase() {
        return cnb;
    }
    
    public File getClusterDirectory() {
        return cluster;
    }
    
    public String getClassPathExtensions() {
        return cpext;
    }
    
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String[] getProvidedTokens() {
        return providedTokens;
    }
    
    public String getSpecificationVersion() {
        return specVersion;
    }
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }
    
    public String toString() {
        return "NetBeansOrgEntry[" + getSourceLocation() + "]"; // NOI18N
    }
    
}
