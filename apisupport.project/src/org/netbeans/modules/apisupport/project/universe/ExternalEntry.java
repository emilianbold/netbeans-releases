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

final class ExternalEntry extends AbstractEntryWithSources {
    
    private final File basedir;
    private final String cnb;
    private final File clusterDir;
    private final File jar;
    private final String cpext;
    private final File nbdestdir;
    private final String releaseVersion;
    private final String specVersion;
    private final String providedTokens;
    private final ManifestManager.PackageExport[] publicPackages;
    private final boolean deprecated;
    
    public ExternalEntry(File basedir, String cnb, File clusterDir, File jar,
            String cpext, File nbdestdir, String releaseVersion, String specVersion,
            String providedTokens, ManifestManager.PackageExport[] publicPackages,
            boolean deprecated) {
        this.basedir = basedir;
        this.cnb = cnb;
        this.clusterDir = clusterDir;
        this.jar = jar;
        this.cpext = cpext;
        this.nbdestdir = nbdestdir;
        this.releaseVersion = releaseVersion;
        this.specVersion = specVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.deprecated = deprecated;
    }
    
    public File getSourceLocation() {
        return basedir;
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
    
    public String getProvidedTokens() {
        return providedTokens;
    }
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }
    
    public String toString() {
        return "ExternalEntry[" + getSourceLocation() + "]"; // NOI18N
    }
    
}
