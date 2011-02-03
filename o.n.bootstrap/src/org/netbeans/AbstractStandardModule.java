/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import org.netbeans.LocaleVariants.FileWithSuffix;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jesse Glick, Allan Gregersen
 */
abstract class AbstractStandardModule extends Module {

    /** JAR file holding the module */
    private final File jar;
    /** if reloadable, temporary JAR file actually loaded from */
    private File physicalJar = null;

    private Manifest manifest;
    /** Map from extension JARs to sets of JAR that load them via Class-Path.
     * Used only for debugging purposes, so that a warning is printed if two
     * different modules try to load the same extension (which would cause them
     * to both load their own private copy, which may not be intended).
     */
    protected static final Map<File, Set<File>> extensionOwners = new HashMap<File, Set<File>>();
    /** Simple registry of JAR files used as modules.
     * Used only for debugging purposes, so that we can be sure
     * that no one is using Class-Path to refer to other modules.
     */
    protected static final Set<File> moduleJARs = new HashSet<File>();
    /** Set of locale-variants JARs for this module (or null).
     * Added explicitly to classloader, and can be used by execution engine.
     */
    private Set<File> localeVariants = null;
    /** Set of extension JARs that this module loads via Class-Path (or null).
     * Can be used e.g. by execution engine. (#9617)
     */
    private Set<File> plainExtensions = null;
    /** Set of localized extension JARs derived from plainExtensions (or null).
     * Used to add these to the classloader. (#9348)
     * Can be used e.g. by execution engine.
     */
    private Set<File> localeExtensions = null;
    /** Patches added at the front of the classloader (or null).
     * Files are assumed to be JARs; directories are themselves.
     */
    private Set<File> patches = null;
    /** localized properties, only non-null if requested from disabled module */
    private Properties localizedProps;

    /** Used as a flag to tell if this module was really successfully released.
     * Currently does not work, so if it cannot be made to work, delete it.
     * (Someone seems to be holding a strong reference to the classloader--who?!)
     */
    protected transient boolean released;

    /** Count which release() call is really being checked. */
    protected transient int releaseCount = 0;

    static Method javeleonFacadeMethod;

    /*
     * Method used by OneModuleClassLoader to register a class loader a
     * to the Javeleon runtime. The associated module will be update-enabled
     */
    static {
        if(JaveleonModule.isJaveleonPresent) {
            try {
                Class javeleonFacadeClass = Thread.currentThread().getContextClassLoader().loadClass("org.javeleon.reload.ReloadFacade");
                javeleonFacadeMethod = javeleonFacadeClass.getDeclaredMethod("registerClassLoader", new Class[]{ClassLoader.class, String.class});
            } catch (Exception ex) {
                // Javeleon was not present... nothing to do then!
            }
        }
    }

    protected AbstractStandardModule(ModuleManager mgr, Events ev, File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, reloadable, autoload, eager);
        this.jar = jar;

        if (reloadable) {
            physicalJar = Util.makeTempJar(jar);
        }
        loadManifest();
        parseManifest();
        findExtensionsAndVariants(getManifest());
        // Check if some other module already listed this one in Class-Path.
        // For the chronologically reverse case, see findExtensionsAndVariants().
        Set<File> bogoOwners = extensionOwners.get(jar);
        if (bogoOwners != null) {
            Util.err.warning("module " + jar + " was incorrectly placed in the Class-Path of other JARs " + bogoOwners + "; please use OpenIDE-Module-Module-Dependencies instead");
        }
        moduleJARs.add(jar);
    }

    @Override
    public Manifest getManifest() {
        if (manifest == null) {
            try {
                loadManifest();
            } catch (IOException ex) {
                manifest = new Manifest();
            }
        }
        return manifest;
    }

    protected void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    /** Open the JAR, load its manifest, and do related things. */
    private void loadManifest() throws IOException {
        Util.err.fine("loading manifest of " + getJarFile());
        File jarBeingOpened = null; // for annotation purposes
        try {
            if (reloadable) {
                // Never try to cache reloadable JARs.
                jarBeingOpened = physicalJar; // might be null
                ensurePhysicalJar();
                jarBeingOpened = physicalJar; // might have changed
                JarFile jarFile = new JarFile(physicalJar, false);
                try {
                    Manifest m = jarFile.getManifest();
                    if (m == null) throw new IOException("No manifest found in " + physicalJar); // NOI18N
                    manifest = m;
                } finally {
                    jarFile.close();
                }
            } else {
                jarBeingOpened = getJarFile();
                manifest = getManager().loadManifest(getJarFile());
            }
        } catch (IOException e) {
            if (jarBeingOpened != null) {
                Exceptions.attachMessage(e,
                                         "While loading manifest from: " +
                                         jarBeingOpened); // NOI18N
            }
            throw e;
        }
    }

    public @Override void releaseManifest() {
        manifest = null;
    }

    protected void scanForPatches(File patchdir) {
        if (!patchdir.isDirectory()) {
            return;
        }
        File[] jars = patchdir.listFiles(Util.jarFilter());
        if (jars != null) {
            for (File patchJar : jars) {
                if (getPatches() == null) {
                    setPatches(new HashSet<File>(5));
                }
                getPatches().add(patchJar);
            }
        } else {
            Util.err.warning("Could not search for patches in " + patchdir);
        }
    }

     /** Find any extensions loaded by the module, as well as any localized
     * variants of the module or its extensions.
     */
    private void findExtensionsAndVariants(Manifest m) {
        assert getJarFile() != null : "Cannot load extensions from classpath module " + getCodeNameBase();
        localeVariants = null;
        List<File> l = LocaleVariants.findLocaleVariantsOf(getJarFile(), getCodeNameBase());
        if (!l.isEmpty()) {
            localeVariants = new HashSet<File>(l);
        }
        plainExtensions = null;
        localeExtensions = null;
        String classPath = m.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if (classPath != null) {
            StringTokenizer tok = new StringTokenizer(classPath);
            while (tok.hasMoreTokens()) {
                String ext = tok.nextToken();
                if (new File(ext).isAbsolute()) { // NOI18N
                    Util.err.log(Level.WARNING, "Class-Path value {0} from {1} is illegal according to the Java Extension Mechanism: must be relative", new Object[] {ext, getJarFile()});
                }
                File base = getJarFile().getParentFile();
                while (ext.startsWith("../")) {
                    // cannot access FileUtil.normalizeFile from here, and URI.normalize might be unsafe for UNC paths
                    ext = ext.substring(3);
                    base = base.getParentFile();
                }
                File extfile = new File(base, ext.replace('/', File.separatorChar));
                //No need to sync on extensionOwners - we are in write mutex
                Set<File> owners = extensionOwners.get(extfile);
                if (owners == null) {
                    owners = new HashSet<File>(2);
                    owners.add(getJarFile());
                    extensionOwners.put(extfile, owners);
                } else if (! owners.contains(getJarFile())) {
                    owners.add(getJarFile());
                    events.log(Events.EXTENSION_MULTIPLY_LOADED, extfile, owners);
                } // else already know about it (OK or warned)
                // Also check to make sure it is not a module JAR! See constructor for the reverse case.
                if (moduleJARs.contains(extfile)) {
                    Util.err.warning("Class-Path value " + ext + " from " + getJarFile() + " illegally refers to another module; use OpenIDE-Module-Module-Dependencies instead");
                }
                if (plainExtensions == null) plainExtensions = new HashSet<File>();
                plainExtensions.add(extfile);
                l = LocaleVariants.findLocaleVariantsOf(extfile, getCodeNameBase());
                if (!l.isEmpty()) {
                    if (localeExtensions == null) {
                        localeExtensions = new HashSet<File>();
                    }
                    localeExtensions.addAll(l);
                }
            }
        }
        // #9273: load any modules/patches/this-code-name/*.jar files first:
        File patchdir = new File(new File(getJarFile().getParentFile(), "patches"), // NOI18N
                                 getCodeNameBase().replace('.', '-')); // NOI18N
        scanForPatches(patchdir);
        // Use of the following system property is not supported, but is used
        // by e.g. XTest to influence installed modules without changing the build.
        // Format is -Dnetbeans.patches.org.nb.mods.foo=/path/to.file.jar:/path/to/dir
        String patchesClassPath = System.getProperty("netbeans.patches." + getCodeNameBase()); // NOI18N
        if (patchesClassPath != null) {
            StringTokenizer tokenizer = new StringTokenizer(patchesClassPath, File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                String element = tokenizer.nextToken();
                File fileElement = new File(element);
                if (fileElement.exists()) {
                    if (patches == null) {
                        patches = new HashSet<File>(15);
                    }
                    patches.add(fileElement);
                }
            }
        }
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("localeVariants of " + getJarFile() + ": " + localeVariants);
            Util.err.fine("plainExtensions of " + getJarFile() + ": " + plainExtensions);
            Util.err.fine("localeExtensions of " + getJarFile() + ": " + localeExtensions);
            Util.err.fine("patches of " + getJarFile() + ": " + patches);
        }
        if (patches != null) {
            for (File patch : patches) {
                events.log(Events.PATCH, patch);
            }
        }
    }

     /** Get a localized attribute.
     * First, if OpenIDE-Module-Localizing-Bundle was given, the specified
     * bundle file (in all locale JARs as well as base JAR) is searched for
     * a key of the specified name.
     * Otherwise, the manifest's main attributes are searched for an attribute
     * with the specified name, possibly with a locale suffix.
     * If the attribute name contains a slash, and there is a manifest section
     * named according to the part before the last slash, then this section's attributes
     * are searched instead of the main attributes, and for the attribute listed
     * after the slash. Currently this would only be useful for localized filesystem
     * names. E.g. you may request the attribute org/foo/MyFileSystem.class/Display-Name.
     * In the future certain attributes known to be dangerous could be
     * explicitly suppressed from this list; should only be used for
     * documented localizable attributes such as OpenIDE-Module-Name etc.
     */
    @Override
    public Object getLocalizedAttribute(String attr) {
        String locb = getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        boolean usingLoader = false;
        if (locb != null) {
            if (classloader != null) {
                if (locb.endsWith(".properties")) { // NOI18N
                    usingLoader = true;
                    String basename = locb.substring(0, locb.length() - 11).replace('/', '.');
                    try {
                        ResourceBundle bundle = NbBundle.getBundle(basename, Locale.getDefault(), classloader);
                        try {
                            return bundle.getString(attr);
                        } catch (MissingResourceException mre) {
                            // Fine, ignore.
                        }
                    } catch (MissingResourceException mre) {
                        String resource = basename.replace('.', '/') + ".properties";
                        Exceptions.attachMessage(mre, "#149833: failed to find " + basename +
                                " in locale " + Locale.getDefault() + " in " + classloader + " for " + jar +
                                "; resource lookup of " + resource + " -> " + classloader.getResource(resource));
                        Exceptions.printStackTrace(mre);
                    }
                } else {
                    Util.err.warning("cannot efficiently load non-*.properties OpenIDE-Module-Localizing-Bundle: " + locb);
                }
            }
            if (!usingLoader) {
                if (localizedProps == null) {
                    Util.err.log(Level.FINE, "Trying to get localized attr {0} from disabled module {1}", new Object[] {attr, getCodeNameBase()});
                    try {
                        // check if the jar file still exists (see issue 82480)
                        if (jar != null && jar.isFile ()) {
                            JarFile jarFile = new JarFile(jar, false);
                            try {
                                loadLocalizedProps(jarFile, getManifest());
                            } finally {
                                jarFile.close();
                            }
                        } else {
                            Util.err.log(Level.FINE, "Cannot get localized attr {0} from module {1} (missing or deleted JAR file: {2})", new Object[] {attr, getCodeNameBase(), jar});
                        }
                    } catch (IOException ioe) {
                        Util.err.log(Level.WARNING, jar.getAbsolutePath(), ioe);
                    }
                }
                if (localizedProps != null) {
                    String val = localizedProps.getProperty(attr);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }
        // Try in the manifest now.
        int idx = attr.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            // Simple main attribute.
            return NbBundle.getLocalizedValue(getManifest().getMainAttributes(), new Attributes.Name(attr));
        } else {
            // Attribute of a manifest section.
            String section = attr.substring(0, idx);
            String realAttr = attr.substring(idx + 1);
            Attributes attrs = getManifest().getAttributes(section);
            if (attrs != null) {
                return NbBundle.getLocalizedValue(attrs, new Attributes.Name(realAttr));
            } else {
                return null;
            }
        }
    }

    /** Check if there is any need to load localized properties.
     * If so, try to load them. Throw an exception if they cannot
     * be loaded for some reason. Uses an open JAR file for the
     * base module at least, though may also open locale variants
     * as needed.
     * Note: due to #19698, this cache is not usually used; only if you
     * specifically go to look at the display properties of a disabled module.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=12549">#12549</a>
     */
    private void loadLocalizedProps(JarFile jarFile, Manifest m) throws IOException {
        String locbundle = m.getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        if (locbundle != null) {
            // Something requested, read it in.
            // locbundle is a resource path.
            {
                ZipEntry bundleFile = jarFile.getEntry(locbundle);
                // May not be present in base JAR: might only be in e.g. default locale variant.
                if (bundleFile != null) {
                    localizedProps = new Properties();
                    InputStream is = jarFile.getInputStream(bundleFile);
                    try {
                        localizedProps.load(is);
                    } finally {
                        is.close();
                    }
                }
            }
            {
                // Check also for localized variant JARs and load in anything from them as needed.
                // Note we need to go in the reverse of the usual search order, so as to
                // overwrite less specific bundles with more specific.
                int idx = locbundle.lastIndexOf('.'); // NOI18N
                String name, ext;
                if (idx == -1) {
                    name = locbundle;
                    ext = ""; // NOI18N
                } else {
                    name = locbundle.substring(0, idx);
                    ext = locbundle.substring(idx);
                }
                List<FileWithSuffix> pairs = LocaleVariants.findLocaleVariantsWithSuffixesOf(jar, getCodeNameBase());
                Collections.reverse(pairs);
                for (FileWithSuffix pair : pairs) {
                    File localeJar = pair.file;
                    String suffix = pair.suffix;
                    String rsrc = name + suffix + ext;
                    JarFile localeJarFile = new JarFile(localeJar, false);
                    try {
                        ZipEntry bundleFile = localeJarFile.getEntry(rsrc);
                        // Need not exist in all locale variants.
                        if (bundleFile != null) {
                            if (localizedProps == null) {
                                localizedProps = new Properties();
                            } // else append and overwrite base-locale values
                            InputStream is = localeJarFile.getInputStream(bundleFile);
                            try {
                                localizedProps.load(is);
                            } finally {
                                is.close();
                            }
                        }
                    } finally {
                        localeJarFile.close();
                    }
                }
            }
            if (localizedProps == null) {
                // We should have loaded from at least some bundle in there...
                throw new IOException("Could not find localizing bundle: " + locbundle); // NOI18N
            }
            /* Don't log; too large and annoying:
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.fine("localizedProps=" + localizedProps);
            }
            */
        }
    }

    public boolean isFixed() {
        return false;
    }

    /** Get the JAR this module is packaged in.
     * May be null for modules installed specially, e.g.
     * automatically from the classpath.
     * @see #isFixed
     */
    public @Override File getJarFile() {
        return jar;
    }

    /** Create a temporary test JAR if necessary.
     * This is primarily necessary to work around a Java bug,
     * #4405789, which is marked as fixed so might be obsolete.
     */
    protected void ensurePhysicalJar() throws IOException {
        if (reloadable && physicalJar == null) {
            physicalJar = Util.makeTempJar(jar);
        }
    }

 /** Get all JARs loaded by this module.
     * Includes the module itself, any locale variants of the module,
     * any extensions specified with Class-Path, any locale variants
     * of those extensions.
     * The list will be in classpath order (patches first).
     * Currently the temp JAR is provided in the case of test modules, to prevent
     * sporadic ZIP file exceptions when background threads (like Java parsing) tries
     * to open libraries found in the library path.
     * JARs already present in the classpath are <em>not</em> listed.
     * @return a <code>List&lt;File&gt;</code> of JARs
     */
    public List<File> getAllJars() {
        List<File> l = new ArrayList<File>();
        if (patches != null) l.addAll(patches);
        if (physicalJar != null) {
            l.add(physicalJar);
        } else if (getJarFile() != null) {
            l.add(getJarFile());
        }
        if (plainExtensions != null) l.addAll (plainExtensions);
        if (localeVariants != null) l.addAll (localeVariants);
        if (localeExtensions != null) l.addAll (localeExtensions);
        return l;
    }

    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    @Override
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("classLoaderUp on " + this + " with parents " + parents);
        }
        // Find classloaders for dependent modules and parent to them.
        List<ClassLoader> loaders = new ArrayList<ClassLoader>(parents.size() + 1);
        // This should really be the base loader created by org.nb.Main for loading openide etc.:
        loaders.add(Module.class.getClassLoader());
        for (Module parent: parents) {
            PackageExport[] exports = parent.getPublicPackages();
            if (exports != null && exports.length == 0) {
                // Check if there is an impl dep here.
                boolean implDep = false;
                for (Dependency dep : getDependenciesArray()) {
                    if (dep.getType() == Dependency.TYPE_MODULE &&
                            dep.getComparison() == Dependency.COMPARE_IMPL &&
                            dep.getName().equals(parent.getCodeName())) {
                        implDep = true;
                        break;
                    }
                }
                if (!implDep) {
                    // Nothing exported from here at all, no sense even adding it.
                    // Shortcut; would not harm anything to add it now, but we would
                    // never use it anyway.
                    // Cf. #27853.
                    continue;
                }
            }
            ClassLoader l = getParentLoader(parent);
            if (parent.isFixed() && loaders.contains(l)) {
                Util.err.log(Level.FINE, "#24996: skipping duplicate classloader from {0}", parent);
                continue;
            }
            loaders.add(l);
        }
        List<File> classp = new ArrayList<File>(3);
        if (getPatches() != null) classp.addAll(getPatches());

        if (reloadable) {
            ensurePhysicalJar();
            // Using OPEN_DELETE does not work well with test modules under 1.4.
            // Random code (URL handler?) still expects the JAR to be there and
            // it is not.
            classp.add(getPhysicalJar());
        } else {
            classp.add(getJarFile());
        }
        // URLClassLoader would not otherwise find these, so:
        if (getLocaleVariants() != null) classp.addAll(getLocaleVariants());

        if (getLocaleExtensions() != null) classp.addAll(getLocaleExtensions());

        if (getPlainExtensions() != null) classp.addAll(getPlainExtensions());

        // #27853:
        getManager().refineClassLoader(this, loaders);

        try {
            classloader = createNewClassLoader(classp, loaders);
        } catch (IllegalArgumentException iae) {
            // Should not happen, but just in case.
            throw (IOException) new IOException(iae.toString()).initCause(iae);
        }
    }

    protected abstract ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents);

    protected ClassLoader getParentLoader(Module parent) {
        return parent.getClassLoader();
    }

    /** Turn off the classloader and release all resources. */
    @Override
    protected void classLoaderDown() {
        if (classloader instanceof ProxyClassLoader) {
            ((ProxyClassLoader)classloader).destroy();
        }
        classloader = null;
        Util.err.fine("classLoaderDown on " + this + ": releaseCount=" + releaseCount + " released=" + released);
        released = false;
    }

    /** Set whether this module is supposed to be reloadable.
     * Has no immediate effect, only impacts what happens the
     * next time it is enabled (after having been disabled if
     * necessary).
     * Must be called from within a write mutex.
     * @param r whether the module should be considered reloadable
     */
    @Override
    public void setReloadable(boolean r) {
        getManager().assertWritable();
        if (reloadable != r) {
            reloadable = r;
            getManager().fireReloadable(this);
        }
    }

    /** Reload this module. Access from ModuleManager.
     * If an exception is thrown, the module is considered
     * to be in an invalid state.
     */
    @Override
    public void reload() throws IOException {
        // Probably unnecessary but just in case:
        destroyPhysicalJar();
        String codeNameBase1 = getCodeNameBase();
        setLocalizedProps(null);
        loadManifest();
        parseManifest();
        findExtensionsAndVariants(getManifest());
        String codeNameBase2 = getCodeNameBase();
        if (! codeNameBase1.equals(codeNameBase2)) {
            throw new InvalidException("Code name base changed during reload: " + codeNameBase1 + " -> " + codeNameBase2); // NOI18N
        }
    }

    /** Should be called after turning off the classloader of one or more modules & GC'ing. */
    @Override
    protected void cleanup() {
        if (isEnabled()) throw new IllegalStateException("cleanup on enabled module: " + this); // NOI18N
        if (classloader != null) throw new IllegalStateException("cleanup on module with classloader: " + this); // NOI18N
        if (! released) {
            Util.err.fine("Warning: not all resources associated with module " + getJarFile() + " were successfully released.");
            released = true;
        } else {
            Util.err.fine("All resources associated with module " + getJarFile() + " were successfully released.");
        }
        // XXX should this rather be done when the classloader is collected?
        destroyPhysicalJar();
    }

        private void destroyPhysicalJar() {
        if (getPhysicalJar() != null) {
            if (getPhysicalJar().isFile()) {
                if (! getPhysicalJar().delete()) {
                    Util.err.warning("temporary JAR " + getPhysicalJar() + " not currently deletable.");
                } else {
                    Util.err.fine("deleted: " + getPhysicalJar());
                }
            }
           setPhysicalJar(null);
        } else {
            Util.err.fine("no physicalJar to delete for " + this);
        }
    }

    /** Notify the module that it is being deleted. */
    @Override
    public void destroy() {
        moduleJARs.remove(getJarFile());
    }

    protected Set<File> getLocaleExtensions() {
        return localeExtensions;
    }

    protected void setLocaleExtensions(Set<File> localeExtensions) {
        this.localeExtensions = localeExtensions;
    }

    protected Set<File> getLocaleVariants() {
        return localeVariants;
    }

    protected void setLocaleVariants(Set<File> localeVariants) {
        this.localeVariants = localeVariants;
    }

    protected Properties getLocalizedProps() {
        return localizedProps;
    }

    protected void setLocalizedProps(Properties localizedProps) {
        this.localizedProps = localizedProps;
    }

    protected Set<File> getPatches() {
        return patches;
    }

    protected void setPatches(Set<File> patches) {
        this.patches = patches;
    }

    protected File getPhysicalJar() {
        return physicalJar;
    }

    protected void setPhysicalJar(File physicalJar) {
        this.physicalJar = physicalJar;
    }

    protected Set<File> getPlainExtensions() {
        return plainExtensions;
    }

    protected void setPlainExtensions(Set<File> plainExtensions) {
        this.plainExtensions = plainExtensions;
    }

    /** PermissionCollection with an instance of AllPermission. */
    private static PermissionCollection modulePermissions;
    /** @return initialized @see #modulePermission */
    private static synchronized PermissionCollection getAllPermission() {
        if (modulePermissions == null) {
            modulePermissions = new Permissions();
            modulePermissions.add(new AllPermission());
            modulePermissions.setReadOnly();
        }
        return modulePermissions;
    }

    protected class AbstractOneModuleClassLoader extends JarClassLoader implements Util.ModuleProvider {

        public AbstractOneModuleClassLoader(List<File> classp, ClassLoader[] parents) throws IllegalArgumentException {
            super(classp, parents, false, AbstractStandardModule.this);
             try {
                javeleonFacadeMethod.invoke(null, this, getCodeNameBase());
            } catch (Exception ex) {
                // OK, give up. Javeleon is not enabled!
            }
        }

        @Override
        public Module getModule() {
            return AbstractStandardModule.this;
        }

        protected @Override PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermission();
        }

        /**
         * Look up a native library as described in modules documentation.
         * @see http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/api.html#jni
         */
        protected
        @Override
        String findLibrary(String libname) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            String arch = System.getProperty("os.arch"); // NOI18N
            String system = System.getProperty("os.name").toLowerCase(); // NOI18N
            String mapped = System.mapLibraryName(libname);
            File lib;

            lib = ifl.locate("modules/lib/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                return lib.getAbsolutePath();
            }

            lib = ifl.locate("modules/lib/" + arch + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                return lib.getAbsolutePath();
            }

            lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                return lib.getAbsolutePath();
            }

            return null;
        }

        protected @Override boolean shouldDelegateResource(String pkg, ClassLoader parent) {
            if (!super.shouldDelegateResource(pkg, parent)) {
                return false;
            }
            Module other;
            if (parent instanceof Util.ModuleProvider) {
                other = ((Util.ModuleProvider)parent).getModule();
            } else {
                other = null;
            }
            return getManager().shouldDelegateResource(AbstractStandardModule.this, other, pkg);
        }

        public @Override String toString() {
            return "ModuleCL@" + Integer.toHexString(System.identityHashCode(this)) + "[" + getCodeNameBase() + "]"; // NOI18N
        }
    }
}
