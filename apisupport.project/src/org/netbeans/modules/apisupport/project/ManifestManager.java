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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.modules.Dependency;

/**
 * TODO - Comment whole code!
 *
 * @author Martin Krauskopf
 */
public final class ManifestManager {
    
    private String codeNameBase;
    private String releaseVersion;
    private String specificationVersion;
    private String implementationVersion;
    private String[] provTokens;
    private String provTokensString;
    private String[] requiredTokens;
    private String localizingBundle;
    private String layer;
    private String classPath;
    private PackageExport[] publicPackages;
    private boolean deprecated;
    
    public static final String OPENIDE_MODULE = "OpenIDE-Module"; // NOI18N
    public static final String OPENIDE_MODULE_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    public static final String OPENIDE_MODULE_IMPLEMENTATION_VERSION = "OpenIDE-Module-Implementation-Version"; // NOI18N
    public static final String OPENIDE_MODULE_PROVIDES = "OpenIDE-Module-Provides"; // NOI18N
    public static final String OPENIDE_MODULE_REQUIRES= "OpenIDE-Module-Requires"; // NOI18N
    public static final String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"; // NOI18N
    public static final String OPENIDE_MODULE_LOCALIZING_BUNDLE = "OpenIDE-Module-Localizing-Bundle"; // NOI18N
    public static final String OPENIDE_MODULE_PUBLIC_PACKAGES = "OpenIDE-Module-Public-Packages"; // NOI18N
    public static final String OPENIDE_MODULE_FRIENDS = "OpenIDE-Module-Friends"; // NOI18N
    public static final String CLASS_PATH = "Class-Path"; // NOI18N
    
    static final PackageExport[] EMPTY_EXPORTED_PACKAGES = new PackageExport[0];
    
    public static final ManifestManager NULL_INSTANCE = new ManifestManager();

    private ManifestManager() {
        this.provTokens = new String[0];
        this.requiredTokens = new String[0];
    }
    
    private ManifestManager(String cnb, String releaseVersion, String specVer,
            String implVer, String provTokensString, String requiredTokens,
            String locBundle, String layer, String classPath,
            PackageExport[] publicPackages, boolean deprecated) {
        this.codeNameBase = cnb;
        this.releaseVersion = releaseVersion;
        this.specificationVersion = specVer;
        this.implementationVersion = implVer;
        this.provTokensString = provTokensString;
        this.provTokens = parseTokens(provTokensString); // XXX could be lazy-loaded
        this.requiredTokens = parseTokens(requiredTokens); // XXX could be lazy-loaded
        this.localizingBundle = locBundle;
        this.layer = layer;
        this.classPath = classPath;
        this.publicPackages = publicPackages;
        this.deprecated = deprecated;
    }
    
    private String[] parseTokens(String tokens) {
        if (tokens == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(tokens, ",");
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
            result[i] = st.nextToken();
        }
        return result;
    }
    
    public static ManifestManager getInstance(File manifest, boolean loadPublicPackages) {
        ManifestManager mm = null;
        InputStream mis = null;
        try {
            if (manifest.exists()) {
                mis = new FileInputStream(manifest); // NOI18N
                Manifest mf = new Manifest(mis);
                mm = ManifestManager.getInstance(mf, loadPublicPackages);
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (mis != null) { mis.close(); }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return mm == null ? new ManifestManager() : mm;
    }
    
    public static ManifestManager getInstanceFromJAR(File jar) {
        try {
            JarFile jf = new JarFile(jar, false);
            try {
                return ManifestManager.getInstance(jf.getManifest(), true);
            } finally {
                jf.close();
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return new ManifestManager();
        }
    }
    
    public static ManifestManager getInstance(Manifest manifest, boolean loadPublicPackages) {
        Attributes attr = manifest.getMainAttributes();
        String codename = attr.getValue(OPENIDE_MODULE);
        String codenamebase = null;
        String releaseVersion = null;
        if (codename != null) {
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                codenamebase = codename;
            } else {
                codenamebase = codename.substring(0, slash);
                releaseVersion = codename.substring(slash + 1);
            }
        }
        PackageExport[] publicPackages = null;
        if (loadPublicPackages) {
            publicPackages = EMPTY_EXPORTED_PACKAGES;
            String pp = attr.getValue(OPENIDE_MODULE_PUBLIC_PACKAGES);
            if (pp != null) { // sanity check
                publicPackages = parseExportedPackages(pp);
            }
        }
        boolean deprecated = "true".equals(attr.getValue("OpenIDE-Module-Deprecated"));
        ManifestManager mm = new ManifestManager(
                codenamebase, releaseVersion,
                attr.getValue(OPENIDE_MODULE_SPECIFICATION_VERSION),
                attr.getValue(OPENIDE_MODULE_IMPLEMENTATION_VERSION),
                attr.getValue(OPENIDE_MODULE_PROVIDES),
                attr.getValue(OPENIDE_MODULE_REQUIRES),
                attr.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE),
                attr.getValue(OPENIDE_MODULE_LAYER),
                attr.getValue(CLASS_PATH),
                publicPackages,
                deprecated);
        return mm;
    }
    
    /**
     * Generates module manifest with the given values into the given
     * <code>manifest</code>.
     */
    static void createManifest(FileObject manifest, String cnb, String specVer,
            String bundlePath, String layerPath) throws IOException {
        EditableManifest em = new EditableManifest();
        em.setAttribute(OPENIDE_MODULE, cnb, null);
        em.setAttribute(OPENIDE_MODULE_SPECIFICATION_VERSION, specVer, null);
        em.setAttribute(OPENIDE_MODULE_LOCALIZING_BUNDLE, bundlePath, null);
        if (layerPath != null) {
            em.setAttribute(OPENIDE_MODULE_LAYER, layerPath, null);
        }
        Util.storeManifest(manifest, em);
    }
    
    private static PackageExport[] parseExportedPackages(String exportsS) {
        // XXX a lot of code in this method is duplicated from
        // org.netbeans.core.modules.Module class. It would be nice to maintain this
        // code only once.
        // XXX we need to parse friends list too!
        PackageExport[] exportedPackages = null;
        if (exportsS.trim().equals("-")) { // NOI18N
            exportedPackages = EMPTY_EXPORTED_PACKAGES;
        } else {
            StringTokenizer tok = new StringTokenizer(exportsS, ", "); // NOI18N
            List exports = new ArrayList(Math.max(tok.countTokens(), 1)); // List<PackageExport>
            while (tok.hasMoreTokens()) {
                String piece = tok.nextToken();
                if (piece.endsWith(".*")) { // NOI18N
                    String pkg = piece.substring(0, piece.length() - 2);
                    Dependency.create(Dependency.TYPE_MODULE, pkg);
                    if (pkg.lastIndexOf('/') != -1) {
                        throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                    }
                    exports.add(new PackageExport(pkg, false));
                } else if (piece.endsWith(".**")) { // NOI18N
                    String pkg = piece.substring(0, piece.length() - 3);
                    Dependency.create(Dependency.TYPE_MODULE, pkg);
                    if (pkg.lastIndexOf('/') != -1) {
                        throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                    }
                    exports.add(new PackageExport(pkg, true));
                } else {
                    throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                }
            }
            if (exports.isEmpty()) {
                throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
            }
            exportedPackages = (PackageExport[])exports.toArray(new PackageExport[exports.size()]);
        }
        return exportedPackages;
    }
    
    public String getCodeNameBase() {
        return codeNameBase;
    }
    
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String getSpecificationVersion() {
        return specificationVersion;
    }
    
    public String getImplementationVersion() {
        return implementationVersion;
    }
    
    public String getProvidedTokensString() {
        return provTokensString;
    }
    
    public String[] getProvidedTokens() {
        return provTokens;
    }
    
    public String[] getRequiredTokens() {
        return requiredTokens;
    }
    
    public String getLocalizingBundle() {
        return localizingBundle;
    }
    
    public String getLayer() {
        return layer;
    }
    
    public String getClassPath() {
        return classPath;
    }
    
    public PackageExport[] getPublicPackages() {
        return publicPackages;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
    
    /**
     * Struct representing a package exported from a module.
     */
    public static final class PackageExport {
        private final String pkg;
        private final boolean recursive;
        
        /** Create a package export struct with the named parameters. */
        public PackageExport(String pkg, boolean recursive) {
            this.pkg = pkg;
            this.recursive = recursive;
        }
        
        /** Package to export, in the form <samp>org.netbeans.modules.foo</samp>. */
        public String getPackage() {
            return pkg;
        }
        
        /** If true, exports subpackages also. */
        public boolean isRecursive() {
            return recursive;
        }
        
        public String toString() {
            return "PackageExport[" + pkg + (recursive ? "/**" : "") + "]"; // NOI18N
        }
    }
}
