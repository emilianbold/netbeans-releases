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
import java.io.PrintWriter;
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
    private String localizingBundle;
    private String layer;
    private String classPath;
    private PackageExport[] publicPackages;
    
    private static final String OPENIDE_MODULE = "OpenIDE-Module"; // NOI18N
    private static final String OPENIDE_MODULE_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    private static final String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"; // NOI18N
    private static final String OPENIDE_MODULE_LOCALIZING_BUNDLE = "OpenIDE-Module-Localizing-Bundle"; // NOI18N
    private static final String OPENIDE_MODULE_PUBLIC_PACKAGES = "OpenIDE-Module-Public-Packages"; // NOI18N
    private static final String OPENIDE_MODULE_FRIENDS = "OpenIDE-Module-Friends"; // NOI18N
    private static final String CLASS_PATH = "Class-Path"; // NOI18N
    
    static final PackageExport[] EMPTY_EXPORTED_PACKAGES = new PackageExport[0];
    
    static final ManifestManager NULL_INSTANCE = new ManifestManager();
    
    private ManifestManager() {}
    
    private ManifestManager(String cnb, String releaseVersion, String specVer,
            String locBundle, String layer, String classPath,
            PackageExport[] publicPackages) {
        this.codeNameBase = cnb;
        this.releaseVersion = releaseVersion;
        this.specificationVersion = specVer;
        this.localizingBundle = locBundle;
        this.layer = layer;
        this.classPath = classPath;
        this.publicPackages = publicPackages;
    }
    
    static ManifestManager getInstance(File manifest, boolean loadPublicPackages) {
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
        return mm;
    }
    
    static ManifestManager getInstanceFromJAR(File jar) {
        ManifestManager mm = null;
        JarFile jf = null;
        try {
            jf = new JarFile(jar);
            mm = ManifestManager.getInstance(jf.getManifest(), true);
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (jf != null) { jf.close(); }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return mm;
    }
    
    static ManifestManager getInstance(Manifest manifest, boolean loadPublicPackages) {
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
        ManifestManager mm = new ManifestManager(
                codenamebase, releaseVersion,
                attr.getValue(OPENIDE_MODULE_SPECIFICATION_VERSION),
                attr.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE),
                attr.getValue(OPENIDE_MODULE_LAYER),
                attr.getValue(CLASS_PATH),
                publicPackages);
        return mm;
    }
    
    /**
     * Generates module manifest with the given values into the given
     * <code>manifest</code>.
     */
    static void createManifest(FileObject manifest, String cnb, String specVer,
            String bundlePath, String layerPath) throws IOException {
        FileLock lock = manifest.lock();
        try {
            OutputStream os = manifest.getOutputStream(lock);
            try {
                EditableManifest em = new EditableManifest();
                em.setAttribute(OPENIDE_MODULE, cnb, null);
                em.setAttribute(OPENIDE_MODULE_SPECIFICATION_VERSION, specVer, null);
                em.setAttribute(OPENIDE_MODULE_LOCALIZING_BUNDLE, bundlePath, null);
                em.setAttribute(OPENIDE_MODULE_LAYER, layerPath, null);
                em.write(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private static PackageExport[] parseExportedPackages(String exportsS) {
        // XXX a lot of code in this method is duplicated from
        // org.netbeans.core.modules.Module class. It would be nice to maintain this
        // code only once.
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
    
    String getCodeNameBase() {
        return codeNameBase;
    }
    
    String getReleaseVersion() {
        return releaseVersion;
    }
    
    String getSpecificationVersion() {
        return specificationVersion;
    }
    
    String getLocalizingBundle() {
        return localizingBundle;
    }
    
    String getLayer() {
        return layer;
    }
    
    String getClassPath() {
        return classPath;
    }
    
    ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
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
            return "PackageExport[" + pkg + (recursive ? "**/" : "") + "]"; // NOI18N
        }
    }
}
