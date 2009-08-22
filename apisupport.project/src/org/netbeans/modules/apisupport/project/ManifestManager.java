/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.modules.Dependency;
import org.openide.util.Exceptions;

// XXX a lot of code in this method is more or less duplicated from
// org.netbeans.core.modules.Module class. Do not forgot to refactor this as
// soon as there is some kind of API (public packages, friends, ...)

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
    private String[] neededTokens;
    private String localizingBundle;
    private String layer;
    private String generatedLayer;
    private String classPath;
    private PackageExport[] publicPackages;
    private String[] friendNames;
    private String moduleDependencies;
    private boolean deprecated;
    private Boolean autoUpdateShowInClient;
    
    public static final String OPENIDE_MODULE = "OpenIDE-Module"; // NOI18N
    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName"; // NOI18N
    public static final String OPENIDE_MODULE_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    public static final String BUNDLE_VERSION = "Bundle-Version"; // NOI18N
    public static final String OPENIDE_MODULE_IMPLEMENTATION_VERSION = "OpenIDE-Module-Implementation-Version"; // NOI18N
    public static final String OPENIDE_MODULE_PROVIDES = "OpenIDE-Module-Provides"; // NOI18N
    public static final String OPENIDE_MODULE_REQUIRES = "OpenIDE-Module-Requires"; // NOI18N
    public static final String OPENIDE_MODULE_NEEDS = "OpenIDE-Module-Needs"; // NOI18N
    public static final String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"; // NOI18N
    public static final String OPENIDE_MODULE_LOCALIZING_BUNDLE = "OpenIDE-Module-Localizing-Bundle"; // NOI18N
    public static final String OPENIDE_MODULE_PUBLIC_PACKAGES = "OpenIDE-Module-Public-Packages"; // NOI18N
    public static final String BUNDLE_EXPORT_PACKAGE = "Export-Package"; // NOI18N
    public static final String BUNDLE_IMPORT_PACKAGE = "Import-Package"; // NOI18N
    public static final String OPENIDE_MODULE_FRIENDS = "OpenIDE-Module-Friends"; // NOI18N
    public static final String OPENIDE_MODULE_MODULE_DEPENDENCIES = "OpenIDE-Module-Module-Dependencies"; // NOI18N
    public static final String CLASS_PATH = "Class-Path"; // NOI18N
    public static final String AUTO_UPDATE_SHOW_IN_CLIENT = "AutoUpdate-Show-In-Client"; // NOI18N

    private static final String GENERATED_LAYER_PATH = "META-INF/generated-layer.xml";    // NOI18N

    static final PackageExport[] EMPTY_EXPORTED_PACKAGES = new PackageExport[0];
    
    public static final ManifestManager NULL_INSTANCE = new ManifestManager();
    
    private ManifestManager() {
        this.provTokens = new String[0];
        this.requiredTokens = new String[0];
        this.neededTokens = new String[0];
    }
    
    private ManifestManager(String cnb, String releaseVersion, String specVer,
            String implVer, String provTokensString, String requiredTokens, String neededTokens,
            String locBundle, String layer, boolean withGeneratedLayer, String classPath,
            PackageExport[] publicPackages, String[] friendNames,
            boolean deprecated, Boolean autoUpdateShowInClient, String moduleDependencies) {
        this.codeNameBase = cnb;
        this.releaseVersion = releaseVersion;
        this.specificationVersion = specVer;
        this.implementationVersion = implVer;
        this.provTokensString = provTokensString;
        this.provTokens = parseTokens(provTokensString); // XXX could be lazy-loaded
        this.requiredTokens = parseTokens(requiredTokens); // XXX could be lazy-loaded
        this.neededTokens = parseTokens(neededTokens); // XXX could be lazy-loaded
        this.localizingBundle = locBundle;
        this.layer = layer;
        if (withGeneratedLayer)
            this.generatedLayer = GENERATED_LAYER_PATH;
        this.classPath = classPath;
        this.publicPackages = (publicPackages == null)
                ? EMPTY_EXPORTED_PACKAGES : publicPackages;
        this.friendNames = friendNames;
        this.deprecated = deprecated;
        this.autoUpdateShowInClient = autoUpdateShowInClient;
        this.moduleDependencies = moduleDependencies;
    }
    
    private String[] parseTokens(String tokens) {
        if (tokens == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(tokens, ","); // NOI18N
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
            result[i] = st.nextToken().trim();
        }
        return result;
    }
    
    public static ManifestManager getInstance(File manifest, boolean loadPublicPackages) {
        if (manifest.exists()) {
            try {
                InputStream mis = new FileInputStream(manifest); // NOI18N
                try {
                    Manifest mf = new Manifest(mis);
                    return ManifestManager.getInstance(mf, loadPublicPackages);
                } finally {
                    mis.close();
                }
            } catch (IOException x) {
                Exceptions.attachMessage(x, "While opening: " + manifest);
                Exceptions.printStackTrace(x);
            }
        }
        return NULL_INSTANCE;
    }
    
    public static ManifestManager getInstanceFromJAR(File jar) {
        return getInstanceFromJAR(jar, false);
    }

    public static ManifestManager getInstanceFromJAR(File jar, boolean withGeneratedLayer) {
        try {
            if (!jar.isFile()) {
                throw new IOException("No such JAR: " + jar); // NOI18N
            }
            JarFile jf = new JarFile(jar, false);
            try {
                Manifest m = jf.getManifest();
                if (m == null) { // #87064
                    throw new IOException("No manifest in " + jar); // NOI18N
                }
                withGeneratedLayer = withGeneratedLayer && (jf.getJarEntry(GENERATED_LAYER_PATH) != null);
                return ManifestManager.getInstance(m, true, withGeneratedLayer);
            } finally {
                jf.close();
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return NULL_INSTANCE;
        }
    }
    
    public static ManifestManager getInstance(Manifest manifest, boolean loadPublicPackages) {
        return getInstance(manifest, loadPublicPackages, false);
    }

    public static ManifestManager getInstance(Manifest manifest, boolean loadPublicPackages, boolean withGeneratedLayer) {
        Attributes attr = manifest.getMainAttributes();
        String codename = attr.getValue(OPENIDE_MODULE);
        if (codename == null) {
            if (attr.getValue(BUNDLE_SYMBOLIC_NAME) != null) {
                return getOSGiInstance(manifest, loadPublicPackages, withGeneratedLayer);
            }
        }

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
        String[] friendNames = null;
        if (loadPublicPackages) {
            publicPackages = EMPTY_EXPORTED_PACKAGES;
            String pp = attr.getValue(OPENIDE_MODULE_PUBLIC_PACKAGES);
            if (pp != null) {
                publicPackages = parseExportedPackages(pp);
            }
            String friends = attr.getValue(OPENIDE_MODULE_FRIENDS);
            if (friends != null) {
                friendNames = parseFriends(friends);
                if (friendNames.length > 0 && publicPackages.length == 0) {
                    throw new IllegalArgumentException("No use specifying OpenIDE-Module-Friends without any public packages: " + friends); // NOI18N
                }
            }
        }
        boolean deprecated = "true".equals(attr.getValue("OpenIDE-Module-Deprecated")); // NOI18N
        String autoUpdateShowInClient = attr.getValue(AUTO_UPDATE_SHOW_IN_CLIENT);
        return new ManifestManager(
                codenamebase, releaseVersion,
                attr.getValue(OPENIDE_MODULE_SPECIFICATION_VERSION),
                attr.getValue(OPENIDE_MODULE_IMPLEMENTATION_VERSION),
                attr.getValue(OPENIDE_MODULE_PROVIDES),
                attr.getValue(OPENIDE_MODULE_REQUIRES),
                attr.getValue(OPENIDE_MODULE_NEEDS),
                attr.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE),
                attr.getValue(OPENIDE_MODULE_LAYER),
                withGeneratedLayer,
                attr.getValue(CLASS_PATH),
                publicPackages,
                friendNames,
                deprecated,
                autoUpdateShowInClient != null ? Boolean.valueOf(autoUpdateShowInClient) : null,
                attr.getValue(OPENIDE_MODULE_MODULE_DEPENDENCIES));
    }

    private static ManifestManager getOSGiInstance(Manifest manifest, boolean loadPublicPackages, boolean withGeneratedLayer) {
        Attributes attr = manifest.getMainAttributes();
        String codenamebase = attr.getValue(BUNDLE_SYMBOLIC_NAME);
        PackageExport[] publicPackages = null;
        String requires = null;
        String provides = null;
        publicPackages = EMPTY_EXPORTED_PACKAGES;
        {
            String pp = attr.getValue(BUNDLE_EXPORT_PACKAGE);
            if (pp != null) {
                List<PackageExport> arr = new ArrayList<PackageExport>();
                StringBuffer sb = new StringBuffer();
                String sep = "";
                for (String p : pp.replaceAll("\"[^\"]*\"", "").split(",")) {
                    final PackageExport pe = new PackageExport(p.replaceAll(";.*$", "").trim(), false);
                    arr.add(pe);
                    sb.append(sep).append(pe.getPackage());
                    sep = ",";
                }
                publicPackages = arr.toArray(new PackageExport[0]);
                provides = sb.toString();
            }
        }
        {
            String pp = attr.getValue(BUNDLE_IMPORT_PACKAGE);
            if (pp != null) {
                StringBuffer sb = new StringBuffer();
                String sep = "";
                for (String p : pp.replaceAll("\"[^\"]*\"", "").split(",")) {
                    sb.append(sep).append(p.replaceAll(";.*$", "").trim());
                    sep = ",";
                }
                requires = sb.toString();
            }
        }

        if (!loadPublicPackages) {
            publicPackages = EMPTY_EXPORTED_PACKAGES;
        }
        return new ManifestManager(
                codenamebase, null,
                just3dots(attr.getValue(BUNDLE_VERSION)),
                attr.getValue(BUNDLE_VERSION),
                provides, // provides
                requires, // requires
                null, // needs
                attr.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE),
                attr.getValue(OPENIDE_MODULE_LAYER),
                withGeneratedLayer,
                attr.getValue(CLASS_PATH),
                publicPackages, //publicPackages,
                null, //friendNames,
                false, // deprecated,
                null, // autoUpdateShowInClient != null ? Boolean.valueOf(autoUpdateShowInClient) : null,
                attr.getValue(OPENIDE_MODULE_MODULE_DEPENDENCIES));
    }

    private static String just3dots(String v) {
        if (v == null) {
            return null;
        }
        String[] segments = v.split("\\.");
        final int max = 3;
        int[] version = new int[segments.length > max ? max : segments.length];
        for (int i = 0; i < version.length; i++) {
            version[i] = Integer.parseInt(segments[i]);
        }
        StringBuilder sb = new StringBuilder();
        String conditionalDot = "";
        for (int i = 0; i < version.length; i++) {
            sb.append(conditionalDot);
            sb.append(version[i]);
            conditionalDot = ".";
        }
        return sb.toString();
    }
    
    /**
     * Generates module manifest with the given values into the given
     * <code>manifest</code>.
     */
    static void createManifest(FileObject manifest, String cnb, String specVer,
            String bundlePath, String layerPath, boolean osgi) throws IOException {
        EditableManifest em = new EditableManifest();
        if (osgi) {
            em.setAttribute(BUNDLE_SYMBOLIC_NAME, cnb, null);
            em.setAttribute(BUNDLE_VERSION, specVer, null);
        } else {
            em.setAttribute(OPENIDE_MODULE, cnb, null);
            em.setAttribute(OPENIDE_MODULE_SPECIFICATION_VERSION, specVer, null);
        }
        em.setAttribute(OPENIDE_MODULE_LOCALIZING_BUNDLE, bundlePath, null);
        if (layerPath != null) {
            em.setAttribute(OPENIDE_MODULE_LAYER, layerPath, null);
        }
        Util.storeManifest(manifest, em);
    }
    
    private static PackageExport[] parseExportedPackages(final String exportsS) {
        PackageExport[] exportedPackages = null;
        if (exportsS.trim().equals("-")) { // NOI18N
            exportedPackages = EMPTY_EXPORTED_PACKAGES;
        } else {
            StringTokenizer tok = new StringTokenizer(exportsS, ", "); // NOI18N
            List<PackageExport> exports = new ArrayList<PackageExport>(Math.max(tok.countTokens(), 1));
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
            exportedPackages = exports.toArray(new PackageExport[exports.size()]);
        }
        return exportedPackages;
    }
    
    private static String[] parseFriends(final String friends) {
        Set<String> set = new HashSet<String>();
        StringTokenizer tok = new StringTokenizer(friends, ", "); // NOI18N
        while (tok.hasMoreTokens()) {
            String piece = tok.nextToken();
            if (piece.indexOf('/') != -1) {
                throw new IllegalArgumentException("May specify only module code name bases in OpenIDE-Module-Friends, not major release versions: " + piece); // NOI18N
            }
            // Indirect way of checking syntax:
            Dependency.create(Dependency.TYPE_MODULE, piece);
            // OK, add it.
            set.add(piece);
        }
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Empty OpenIDE-Module-Friends: " + friends); // NOI18N
        }
        return set.toArray(new String[set.size()]);
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
    
    public String[] getNeededTokens() {
        return neededTokens;
    }
    
    public String getLocalizingBundle() {
        return localizingBundle;
    }
    
    public String getLayer() {
        return layer;
    }

    public String getGeneratedLayer() {
        return generatedLayer;
    }
    
    public String getClassPath() {
        return classPath;
    }
    
    /**
     * @return an array of public packages. May be empty but not <code>null</code>.
     */
    public PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public String[] getFriends() {
        return friendNames;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }

    public Boolean getAutoUpdateShowInClient() {
        return autoUpdateShowInClient;
    }
    
    public Set<Dependency> getModuleDependencies() {
        if (moduleDependencies != null) {
            return Dependency.create(Dependency.TYPE_MODULE, moduleDependencies);
        } else {
            return Collections.emptySet();
        }
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
        
        public @Override String toString() {
            return "PackageExport[" + pkg + (recursive ? "/**" : "") + "]"; // NOI18N
        }
    }
    
}
