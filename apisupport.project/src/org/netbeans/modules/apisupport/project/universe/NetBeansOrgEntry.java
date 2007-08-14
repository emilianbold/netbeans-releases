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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.openide.filesystems.FileUtil;

final class NetBeansOrgEntry extends AbstractEntryWithSources {

    private final File nball;
    private final String cnb;
    private final String path;
    private final File cluster;
    private final String module;
    private final String cpext;
    private final String releaseVersion;
    private final String[] providedTokens;
    private final ManifestManager.PackageExport[] publicPackages;
    private final String[] friends;
    private final boolean deprecated;
    
    public NetBeansOrgEntry(File nball, String cnb, String path, File cluster,
            String module, String cpext, String releaseVersion,
            String[] providedTokens, ManifestManager.PackageExport[] publicPackages,
            String[] friends, boolean deprecated, String src) {
        super(src);
        this.nball = nball;
        this.cnb = cnb;
        this.path = path;
        this.cluster = cluster;
        this.module = module;
        this.cpext = cpext;
        this.releaseVersion = releaseVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.friends = friends;
        this.deprecated = deprecated;
    }
    
    public File getSourceLocation() {
        return FileUtil.normalizeFile(new File(nball, path.replace('/', File.separatorChar)));
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
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public boolean isDeclaredAsFriend(String cnb) {
        return isDeclaredAsFriend(friends, cnb);
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }

    public @Override String toString() {
        return "NetBeansOrgEntry[" + getSourceLocation() + "]"; // NOI18N
    }
    
}
