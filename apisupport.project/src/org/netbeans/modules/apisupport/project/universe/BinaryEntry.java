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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.modules.Dependency;

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
    private final String[] friends;
    private final boolean deprecated;
    private final String[] runDependencies;
    private Set<String> allPackageNames;
    
    public BinaryEntry(String cnb, File jar, File[] exts, File nbdestdir, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, String[] friends,
            boolean deprecated, Set<Dependency> moduleDependencies) {
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
        this.friends = friends;
        this.deprecated = deprecated;
        Set<String> deps = new TreeSet<String>();
        for (Dependency d : moduleDependencies) {
            String codename = d.getName();
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                deps.add(codename);
            } else {
                deps.add(codename.substring(0, slash));
            }
        }
        runDependencies = deps.toArray(new String[deps.size()]);
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
            bundleInfo = Util.findLocalizedBundleInfoFromJAR(getJarLocation());
            if (bundleInfo == null) {
                bundleInfo = LocalizedBundleInfo.EMPTY;
            }
        }
        return bundleInfo;
    }
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public synchronized Set<String> getAllPackageNames() {
        if (allPackageNames == null) {
            allPackageNames = new TreeSet<String>();
            Util.scanJarForPackageNames(allPackageNames, getJarLocation());
        }
        return allPackageNames;
    }
    
    public boolean isDeclaredAsFriend(String cnb) {
        return isDeclaredAsFriend(friends, cnb);
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }
    
    public String toString() {
        File source = getSourceLocation();
        return "BinaryEntry[" + getJarLocation() + (source != null ? "," + source : "") + "]"; // NOI18N
    }
    
    protected Set<String> computePublicClassNamesInMainModule() throws IOException {
        Set<String> result = new HashSet<String>();
        scanJarForPublicClassNames(result, jar);
        return result;
    }

    public String[] getRunDependencies() {
        return runDependencies;
    }
    
}
