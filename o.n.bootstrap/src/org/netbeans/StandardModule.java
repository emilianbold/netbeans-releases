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

package org.netbeans;

// THIS CLASS OUGHT NOT USE NbBundle NOR org.openide CLASSES
// OUTSIDE OF openide-util.jar! UI AND FILESYSTEM/DATASYSTEM
// INTERACTIONS SHOULD GO ELSEWHERE.
// (NbBundle.getLocalizedValue is OK here.)

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.netbeans.Module.PackageExport;
import org.openide.modules.Dependency;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick
 */
final class StandardModule extends Module {
    
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
    private static final Map<File,Set<File>> extensionOwners = new HashMap<File,Set<File>>();
    /** Simple registry of JAR files used as modules.
     * Used only for debugging purposes, so that we can be sure
     * that no one is using Class-Path to refer to other modules.
     */
    private static final Set<File> moduleJARs = new HashSet<File>();

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
    
    /** Use ModuleManager.create as a factory. */
    public StandardModule(ModuleManager mgr, Events ev, File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, reloadable, autoload, eager);
        this.jar = jar;
        loadManifest();
        parseManifest();
        findExtensionsAndVariants(manifest);
        // Check if some other module already listed this one in Class-Path.
        // For the chronologically reverse case, see findExtensionsAndVariants().
        Set<File> bogoOwners = extensionOwners.get(jar);
        if (bogoOwners != null) {
            Util.err.warning("module " + jar + " was incorrectly placed in the Class-Path of other JARs " + bogoOwners + "; please use OpenIDE-Module-Module-Dependencies instead");
        }
        moduleJARs.add(jar);
    }

    public @Override Manifest getManifest() {
        if (manifest == null) {
            try {
                loadManifest();
            } catch (IOException x) {
                Util.err.log(Level.WARNING, "While loading manifest for " + this, x);
                manifest = new Manifest();
            }
        }
        return manifest;
    }

    public @Override void releaseManifest() {
        manifest = null;
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
                        Util.err.log(Level.WARNING, null, mre);
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
    
    public boolean owns(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof Util.ModuleProvider) {
            return ((Util.ModuleProvider) cl).getModule() == this;
        }
        return false;
        
    }
    
    public boolean isFixed() {
        return false;
    }
    
    /** Get the JAR this module is packaged in.
     * May be null for modules installed specially, e.g.
     * automatically from the classpath.
     * @see #isFixed
     */
    public File getJarFile() {
        return jar;
    }
    
    /** Create a temporary test JAR if necessary.
     * This is primarily necessary to work around a Java bug,
     * #4405789, which is marked as fixed so might be obsolete.
     */
    private void ensurePhysicalJar() throws IOException {
        if (reloadable && physicalJar == null) {
            physicalJar = Util.makeTempJar(jar);
        }
    }
    private void destroyPhysicalJar() {
        if (physicalJar != null) {
            if (physicalJar.isFile()) {
                if (! physicalJar.delete()) {
                    Util.err.warning("temporary JAR " + physicalJar + " not currently deletable.");
                } else {
                    Util.err.fine("deleted: " + physicalJar);
                }
            }
            physicalJar = null;
        } else {
            Util.err.fine("no physicalJar to delete for " + this);
        }
    }
    
    /** Open the JAR, load its manifest, and do related things. */
    private void loadManifest() throws IOException {
        Util.err.fine("loading manifest of " + jar);
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
                jarBeingOpened = jar;
                manifest = getManager().loadManifest(jar);
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
    
    /** Find any extensions loaded by the module, as well as any localized
     * variants of the module or its extensions.
     */
    private void findExtensionsAndVariants(Manifest m) {
        assert jar != null : "Cannot load extensions from classpath module " + getCodeNameBase();
        localeVariants = null;
        List<File> l = Util.findLocaleVariantsOf(jar);
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
                if (new File(ext).isAbsolute() || ext.indexOf("../") != -1) { // NOI18N
                    Util.err.warning("Class-Path value " + ext + " from " + jar + " is illegal according to the Java Extension Mechanism: must be relative and not move up directories");
                }
                File extfile = new File(jar.getParentFile(), ext.replace('/', File.separatorChar));
                if (! extfile.exists()) {
                    // Ignore unloadable extensions.
                    Util.err.warning("Class-Path value " + ext + " from " + jar + " cannot be found at " + extfile);
                    continue;
                }
                //No need to sync on extensionOwners - we are in write mutex
                    Set<File> owners = extensionOwners.get(extfile);
                    if (owners == null) {
                        owners = new HashSet<File>(2);
                        owners.add(jar);
                        extensionOwners.put(extfile, owners);
                    } else if (! owners.contains(jar)) {
                        owners.add(jar);
                        events.log(Events.EXTENSION_MULTIPLY_LOADED, extfile, owners);
                    } // else already know about it (OK or warned)
                // Also check to make sure it is not a module JAR! See constructor for the reverse case.
                if (moduleJARs.contains(extfile)) {
                    Util.err.warning("Class-Path value " + ext + " from " + jar + " illegally refers to another module; use OpenIDE-Module-Module-Dependencies instead");
                }
                if (plainExtensions == null) plainExtensions = new HashSet<File>();
                plainExtensions.add(extfile);
                l = Util.findLocaleVariantsOf(extfile);
                if (!l.isEmpty()) {
                    if (localeExtensions == null) {
                        localeExtensions = new HashSet<File>();
                    }
                    localeExtensions.addAll(l);
                }
            }
        }
        // #9273: load any modules/patches/this-code-name/*.jar files first:
        File patchdir = new File(new File(jar.getParentFile(), "patches"), // NOI18N
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
        Util.err.fine("localeVariants of " + jar + ": " + localeVariants);
        Util.err.fine("plainExtensions of " + jar + ": " + plainExtensions);
        Util.err.fine("localeExtensions of " + jar + ": " + localeExtensions);
        Util.err.fine("patches of " + jar + ": " + patches);
        if (patches != null) {
            for (File patch : patches) {
                events.log(Events.PATCH, patch);
            }
        }
    }
    
    /** Scans a directory for possible patch JARs. */
    private void scanForPatches(File patchdir) {
        if (!patchdir.isDirectory()) {
            return;
        }
        File[] jars = patchdir.listFiles(Util.jarFilter());
        if (jars != null) {
            for (File patchJar : jars) {
                if (patches == null) {
                    patches = new HashSet<File>(5);
                }
                patches.add(patchJar);
            }
        } else {
            Util.err.warning("Could not search for patches in " + patchdir);
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
                List<Util.FileWithSuffix> pairs = Util.findLocaleVariantsWithSuffixesOf(jar);
                Collections.reverse(pairs);
                for (Util.FileWithSuffix pair : pairs) {
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
        } else if (jar != null) {
            l.add(jar);
        }
        if (plainExtensions != null) l.addAll (plainExtensions);
        if (localeVariants != null) l.addAll (localeVariants);
        if (localeExtensions != null) l.addAll (localeExtensions);
        return l;
    }

    /** Set whether this module is supposed to be reloadable.
     * Has no immediate effect, only impacts what happens the
     * next time it is enabled (after having been disabled if
     * necessary).
     * Must be called from within a write mutex.
     * @param r whether the module should be considered reloadable
     */
    public void setReloadable(boolean r) {
        getManager().assertWritable();
        if (reloadable != r) {
            reloadable = r;
            getManager().fireReloadable(this);
        }
    }
    
    /** Used as a flag to tell if this module was really successfully released.
     * Currently does not work, so if it cannot be made to work, delete it.
     * (Someone seems to be holding a strong reference to the classloader--who?!)
     */
    private transient boolean released;
    /** Count which release() call is really being checked. */
    private transient int releaseCount = 0;

    /** Reload this module. Access from ModuleManager.
     * If an exception is thrown, the module is considered
     * to be in an invalid state.
     */
    public void reload() throws IOException {
        // Probably unnecessary but just in case:
        destroyPhysicalJar();
        String codeNameBase1 = getCodeNameBase();
        localizedProps = null;
        loadManifest();
        parseManifest();
        findExtensionsAndVariants(manifest);
        String codeNameBase2 = getCodeNameBase();
        if (! codeNameBase1.equals(codeNameBase2)) {
            throw new InvalidException("Code name base changed during reload: " + codeNameBase1 + " -> " + codeNameBase2); // NOI18N
        }
    }
    
    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        Util.err.fine("classLoaderUp on " + this + " with parents " + parents);
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
            ClassLoader l = parent.getClassLoader();
            if (parent.isFixed() && loaders.contains(l)) {
                Util.err.fine("#24996: skipping duplicate classloader from " + parent);
                continue;
            }
            loaders.add(l);
        }
        List<File> classp = new ArrayList<File>(3);
        if (patches != null) classp.addAll(patches);

        if (reloadable) {
            ensurePhysicalJar();
            // Using OPEN_DELETE does not work well with test modules under 1.4.
            // Random code (URL handler?) still expects the JAR to be there and
            // it is not.
            classp.add(physicalJar);
        } else {
            classp.add(jar);
        }
        // URLClassLoader would not otherwise find these, so:
        if (localeVariants != null) classp.addAll(localeVariants);

        if (localeExtensions != null) classp.addAll(localeExtensions);

        if (plainExtensions != null) classp.addAll(plainExtensions);
        
        // #27853:
        getManager().refineClassLoader(this, loaders);
        
        try {
            classloader = new OneModuleClassLoader(classp, loaders.toArray(new ClassLoader[loaders.size()]));
        } catch (IllegalArgumentException iae) {
            // Should not happen, but just in case.
            throw (IOException) new IOException(iae.toString()).initCause(iae);
        }
    }
    
    /** Turn off the classloader and release all resources. */
    protected void classLoaderDown() {
        if (classloader instanceof ProxyClassLoader) {
            ((ProxyClassLoader)classloader).destroy();
        }
        classloader = null;
        Util.err.fine("classLoaderDown on " + this + ": releaseCount=" + releaseCount + " released=" + released);
        released = false;
    }
    /** Should be called after turning off the classloader of one or more modules & GC'ing. */
    protected void cleanup() {
        if (isEnabled()) throw new IllegalStateException("cleanup on enabled module: " + this); // NOI18N
        if (classloader != null) throw new IllegalStateException("cleanup on module with classloader: " + this); // NOI18N
        if (! released) {
            Util.err.fine("Warning: not all resources associated with module " + jar + " were successfully released.");
            released = true;
        } else {
            Util.err.fine("All resources associated with module " + jar + " were successfully released.");
        }
        // XXX should this rather be done when the classloader is collected?
        destroyPhysicalJar();
    }
    
    /** Notify the module that it is being deleted. */
    public void destroy() {
        moduleJARs.remove(jar);
    }
    
    /** String representation for debugging. */
    public String toString() {
        String s = "StandardModule:" + getCodeNameBase() + " jarFile: " + jar.getAbsolutePath(); // NOI18N
        if (!isValid()) s += "[invalid]"; // NOI18N
        return s;
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

    /** Class loader to load a single module.
     * Auto-localizing, multi-parented, permission-granting, the works.
     */
    private class OneModuleClassLoader extends JarClassLoader implements Util.ModuleProvider {
        private int rc;
        /** Create a new loader for a module.
         * @param classp the List of all module jars of code directories;
         *      includes the module itself, its locale variants,
         *      variants of extensions and Class-Path items from Manifest.
         *      The items are JarFiles for jars and Files for directories
         * @param parents a set of parent classloaders (from other modules)
         */
        public OneModuleClassLoader(List<File> classp, ClassLoader[] parents) throws IllegalArgumentException {
            super(classp, parents, false, StandardModule.this);
            rc = releaseCount++;
        }
        
        public Module getModule() {
            return StandardModule.this;
        }
        
        /** Inherited.
         * @param cs is ignored
         * @return PermissionCollection with an AllPermission instance
         */
        protected PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermission();
        }
        
        /** look for JNI libraries also in modules/bin/ */
        protected String findLibrary(String libname) {
            String mapped = System.mapLibraryName(libname);
            File lib = new File(new File(jar.getParentFile(), "lib"), mapped); // NOI18N
            if (lib.isFile()) {
                return lib.getAbsolutePath();
            } else {
                return null;
            }
        }

        protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
            if (!super.shouldDelegateResource(pkg, parent)) {
                return false;
            }
            Module other;
            if (parent instanceof Util.ModuleProvider) {
                other = ((Util.ModuleProvider)parent).getModule();
            } else {
                other = null;
            }
            return getManager().shouldDelegateResource(StandardModule.this, other, pkg);
        }
        
        public String toString() {
            return super.toString() + "[" + getCodeNameBase() + "]"; // NOI18N
        }

        protected void finalize() throws Throwable {
            super.finalize();
            Util.err.fine("Finalize for " + this + ": rc=" + rc + " releaseCount=" + releaseCount + " released=" + released); // NOI18N
            if (rc == releaseCount) {
                // Hurrah! release() worked.
                released = true;
            } else {
                Util.err.fine("Now resources for " + getCodeNameBase() + " have been released."); // NOI18N
            }
        }
    }

}
