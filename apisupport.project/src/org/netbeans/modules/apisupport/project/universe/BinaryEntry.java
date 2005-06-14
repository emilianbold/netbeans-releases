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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.ManifestManager;

final class BinaryEntry extends AbstractEntry {
    
    private final String cnb;
    private final File jar;
    private final String cpext;
    private final File nbdestdir;
    private final File clusterDir;
    private final String releaseVersion;
    private final String specVersion;
    private final String[] providedTokens;
    private LocalizedBundleInfo bundleInfo;
    private final ManifestManager.PackageExport[] publicPackages;
    private final boolean deprecated;
    
    public BinaryEntry(String cnb, File jar, File[] exts, File nbdestdir, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, boolean deprecated) {
        this.cnb = cnb;
        this.jar = jar;
        this.nbdestdir = nbdestdir;
        this.clusterDir = clusterDir;
        StringBuffer _cpext = new StringBuffer();
        for (int i = 0; i < exts.length; i++) {
            _cpext.append(':');
            _cpext.append(exts[i].getAbsolutePath());
        }
        cpext = _cpext.toString();
        this.releaseVersion = releaseVersion;
        this.specVersion = specVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.deprecated = deprecated;
    }
    
    //private boolean recurring;
    public File getSourceLocation() {
        NbPlatform platform = NbPlatform.getPlatformByDestDir(getDestDir());
            /*
            assert !recurring : jar;
            recurring = true;
            try {
             */
        return platform.getSourceLocationOfModule(getJarLocation());
            /*
            } finally {
                recurring = false;
            }
             */
    }
    
    public String getNetBeansOrgPath() {
        return null;
    }
    
    public File getJarLocation() {
        return jar;
    }
    
    public File getDestDir() {
        return nbdestdir;
    }
    
    public String getCodeNameBase() {
        return cnb;
    }
    
    public File getClusterDirectory() {
        return clusterDir;
    }
    
    public String getClassPathExtensions() {
        return cpext;
    }
    
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String getSpecificationVersion() {
        return specVersion;
    }
    
    public String[] getProvidedTokens() {
        return providedTokens;
    }
    
    protected LocalizedBundleInfo getBundleInfo() {
        if (bundleInfo == null) {
            bundleInfo = ModuleList.loadBundleInfoFromBinary(getJarLocation());
        }
        return bundleInfo;
    }
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }
    
    public String toString() {
        File source = getSourceLocation();
        return "BinaryEntry[" + getJarLocation() + (source != null ? "," + source : "") + "]"; // NOI18N
    }
    
    protected Set/*<String>*/ computePublicClassNamesInMainModule() throws IOException {
        Set/*<String>*/ result = new HashSet();
        scanJarForPublicClassNames(result, jar);
        return result;
    }
    
}
