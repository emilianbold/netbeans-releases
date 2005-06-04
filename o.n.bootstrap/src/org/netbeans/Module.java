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

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import java.util.zip.ZipEntry;
import org.netbeans.JarClassLoader;
import org.netbeans.ProxyClassLoader;
import org.openide.ErrorManager;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick
 */
public final class Module extends ModuleInfo {
    
    public static final String PROP_RELOADABLE = "reloadable"; // NOI18N
    public static final String PROP_CLASS_LOADER = "classLoader"; // NOI18N
    public static final String PROP_MANIFEST = "manifest"; // NOI18N
    public static final String PROP_VALID = "valid"; // NOI18N
    public static final String PROP_PROBLEMS = "problems"; // NOI18N
    
    /** manager which owns this module */
    private final ModuleManager mgr;
    /** event logging (should not be much here) */
    private final Events ev;
    /** associated history object
     * @see ModuleHistory
     */
    private final Object history;
    /** JAR file holding the module */
    private final File jar;
    /** if reloadable, temporary JAR file actually loaded from */
    private File physicalJar = null;
    /** true if currently enabled; manipulated by ModuleManager */
    private boolean enabled;
    /** whether it is supposed to be easily reloadable */
    private boolean reloadable;
    /** whether it is supposed to be automatically loaded when required */
    private final boolean autoload;
    /** if true, this module is eagerly turned on whenever it can be */
    private final boolean eager;
    /** module manifest */
    private Manifest manifest;
    /** code name base (no slash) */
    private String codeNameBase;
    /** code name release, or -1 if undefined */
    private int codeNameRelease;
    /** full code name */
    private String codeName;
    /** provided tokens */
    private String[] provides;
    /** set of dependencies parsed from manifest */
    private Dependency[] dependenciesA;
    /** specification version parsed from manifest, or null */
    private SpecificationVersion specVers;
    /** currently active module classloader */
    private ClassLoader classloader = null;
    /** module classloaders that might have been created before */
    private final Set oldClassLoaders = new WeakSet(3); // Set<OneModuleClassLoader>
    /** localized properties, only non-null if requested from disabled module */
    private Properties localizedProps;
    /** public packages, may be null */
    private PackageExport[] publicPackages;
    /** Set<String> of CNBs of friend modules or null */
    private Set/*<String>*/ friendNames;
    
    /** Map from extension JARs to sets of JAR that load them via Class-Path.
     * Used only for debugging purposes, so that a warning is printed if two
     * different modules try to load the same extension (which would cause them
     * to both load their own private copy, which may not be intended).
     */
    private static final Map extensionOwners = new HashMap(); // Map<File,Set<File>>
    /** Simple registry of JAR files used as modules.
     * Used only for debugging purposes, so that we can be sure
     * that no one is using Class-Path to refer to other modules.
     */
    private static final Set moduleJARs = new HashSet(); // Set<File>

    /** Set of locale-variants JARs for this module (or null).
     * Added explicitly to classloader, and can be used by execution engine.
     */
    private Set localeVariants = null; // Set<File>
    /** Set of extension JARs that this module loads via Class-Path (or null).
     * Can be used e.g. by execution engine. (#9617)
     */
    private Set plainExtensions = null; // Set<File>
    /** Set of localized extension JARs derived from plainExtensions (or null).
     * Used to add these to the classloader. (#9348)
     * Can be used e.g. by execution engine.
     */
    private Set localeExtensions = null; // Set<File>
    /** Patches added at the front of the classloader (or null).
     * Files are assumed to be JARs; directories are themselves.
     */
    private Set patches = null; // Set<File>

    /** Use ModuleManager.create as a factory. */
    Module(ModuleManager mgr, Events ev, File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        if (autoload && eager) throw new IllegalArgumentException("A module may not be both autoload and eager"); // NOI18N
        this.mgr = mgr;
        this.ev = ev;
        this.jar = jar;
        this.history = history;
        this.reloadable = reloadable;
        this.autoload = autoload;
        this.eager = eager;
        enabled = false;
        loadManifest();
        parseManifest();
        findExtensionsAndVariants(manifest);
        // Check if some other module already listed this one in Class-Path.
        // For the chronologically reverse case, see findExtensionsAndVariants().
        Set bogoOwners = (Set)extensionOwners.get(jar);
        if (bogoOwners != null) {
            Util.err.log(ErrorManager.WARNING, "WARNING - module " + jar + " was incorrectly placed in the Class-Path of other JARs " + bogoOwners + "; please use OpenIDE-Module-Module-Dependencies instead");
        }
        moduleJARs.add(jar);
    }
    
    /** Create a special-purpose "fixed" JAR. */
    Module(ModuleManager mgr, Events ev, Manifest manifest, Object history, ClassLoader classloader) throws InvalidException {
        this.mgr = mgr;
        this.ev = ev;
        this.manifest = manifest;
        this.history = history;
        this.classloader = classloader;
        jar = null;
        reloadable = false;
        autoload = false;
        eager = false;
        enabled = false;
        //loadLocalizedPropsClasspath();
        parseManifest();
    }
    
    /** Get the associated module manager. */
    public ModuleManager getManager() {
        return mgr;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    // Access from ModuleManager:
    void setEnabled(boolean enabled) {
        /* #13647: actually can happen if loading of bootstrap modules is rolled back:
        if (isFixed() && ! enabled) throw new IllegalStateException("Cannot disable a fixed module: " + this); // NOI18N
        */
        this.enabled = enabled;
    }
    
    /** Normally a module once created and managed is valid
     * (that is, either installed or not, but at least managed).
     * If it is deleted any remaining references to it become
     * invalid.
     */
    public boolean isValid() {
        return mgr.get(getCodeNameBase()) == this;
    }
    
    /** Is this module automatically loaded?
     * If so, no information about its state is kept
     * permanently beyond the existence of its JAR file;
     * it is enabled when some real module needs it to be,
     * and disabled when this is no longer the case.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=9779">#9779</a>
     */
    public boolean isAutoload() {
        return autoload;
    }
    
    /** Is this module eagerly enabled?
     * If so, no information about its state is kept permanently.
     * It is turned on whenever it can be, i.e. whenever it meets all of
     * its dependencies. This may be used to implement "bridge" modules with
     * simple functionality that just depend on two normal modules.
     * A module may not be simultaneously eager and autoload.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=17501">#17501</a>
     * @since org.netbeans.core/1 1.3
     */
    public boolean isEager() {
        return eager;
    }
    
    /** Get an associated arbitrary attribute.
     * Right now, simply provides the main attributes of the manifest.
     * In the future some of these could be suppressed (if only of dangerous
     * interest, e.g. Class-Path) or enhanced with other information available
     * from the core (if needed).
     */
    public Object getAttribute(String attr) {
        return getManifest().getMainAttributes().getValue(attr);
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
        String locb = manifest.getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
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
                        Util.err.notify(mre);
                    }
                } else {
                    Util.err.log(ErrorManager.WARNING, "WARNING - cannot efficiently load non-*.properties OpenIDE-Module-Localizing-Bundle: " + locb);
                }
            }
            if (!usingLoader) {
                if (localizedProps == null) {
                    Util.err.log("Trying to get localized attr " + attr + " from disabled module " + getCodeNameBase());
                    try {
                        if (jar != null) {
                            JarFile jarFile = new JarFile(jar, false);
                            try {
                                loadLocalizedProps(jarFile, manifest);
                            } finally {
                                jarFile.close();
                            }
                        } else if (classloader != null) {
                            loadLocalizedPropsClasspath();
                        } else {
                            throw new IllegalStateException();
                        }
                    } catch (IOException ioe) {
                        Util.err.annotate(ioe, ErrorManager.INFORMATIONAL, jar.getAbsolutePath(), null, null, null);
                        Util.err.notify(ErrorManager.INFORMATIONAL, ioe);
                        if (localizedProps == null) {
                            localizedProps = new Properties();
                        }
                    }
                }
                String val = localizedProps.getProperty(attr);
                if (val != null) {
                    return val;
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
    
    public String getCodeName() {
        return codeName;
    }
    
    public String getCodeNameBase() {
        return codeNameBase;
    }
    
    public int getCodeNameRelease() {
        return codeNameRelease;
    }
    
    public String[] getProvides() {
        return provides;
    }
    /** Test whether the module provides a given token or not. 
     * @since JST-PENDING again used from NbProblemDisplayer
     */
    public final boolean provides(String token) {
        for (int i = 0; i < provides.length; i++) {
            if (provides[i].equals(token)) {
                return true;
            }
        }
        return false;
    }
    
    public Set getDependencies() {
        return new HashSet(Arrays.asList(dependenciesA));
    }
    // Faster to loop over:
    // @since JST-PENDING called from NbInstaller
    public final Dependency[]  getDependenciesArray() {
        return dependenciesA;
    }
    
    public SpecificationVersion getSpecificationVersion() {
        return specVers;
    }
    
    public boolean owns(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof Util.ModuleProvider) {
            return ((Util.ModuleProvider) cl).getModule() == this;
        }
        return false;
        
    }
    
    /** Get all packages exported by this module to other modules.
     * @return a list (possibly empty) of exported packages, or null to export everything
     * @since org.netbeans.core/1 > 1.4
     * @see "#19621"
     */
    public PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    /** Checks whether we use friends attribute and if so, then
     * whether the name of module is listed there.
     */
    public boolean isDeclaredAsFriend (Module module) {
        if (friendNames == null) {
            return true;
        }
        return friendNames.contains(module.getCodeNameBase());
    }
    
    /** Parse information from the current manifest.
     * Includes code name, specification version, and dependencies.
     * If anything is in an invalid format, throws an exception with
     * some kind of description of the problem.
     */
    private void parseManifest() throws InvalidException {
        Attributes attr = manifest.getMainAttributes();
        // Code name
        codeName = attr.getValue("OpenIDE-Module"); // NOI18N
        if (codeName == null) {
            InvalidException e = new InvalidException("Not a module: no OpenIDE-Module tag in manifest of " + /* #17629: important! */jar); // NOI18N
            // #29393: plausible user mistake, deal with it politely.
            Util.err.annotate(e, NbBundle.getMessage(Module.class, "EXC_not_a_module", jar.getAbsolutePath()));
            throw e;
        }
        try {
            // This has the side effect of checking syntax:
            if (codeName.indexOf(',') != -1) {
                throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module: " + codeName); // NOI18N
            }
            Dependency.create(Dependency.TYPE_MODULE, codeName);
            Object[] cnParse = Util.parseCodeName(codeName);
            codeNameBase = (String)cnParse[0];
            codeNameRelease = (cnParse[1] != null) ? ((Integer)cnParse[1]).intValue() : -1;
            if (cnParse[2] != null) throw new NumberFormatException(codeName);
            // Spec vers
            String specVersS = attr.getValue("OpenIDE-Module-Specification-Version"); // NOI18N
            if (specVersS != null) {
                try {
                    specVers = new SpecificationVersion(specVersS);
                } catch (NumberFormatException nfe) {
                    InvalidException ie = new InvalidException("While parsing OpenIDE-Module-Specification-Version: " + nfe.toString()); // NOI18N
                    Util.err.annotate(ie, nfe);
                    throw ie;
                }
            } else {
                specVers = null;
            }
            // Token provides
            String providesS = attr.getValue("OpenIDE-Module-Provides"); // NOI18N
            if (providesS == null) {
                provides = new String[] {};
            } else {
                StringTokenizer tok = new StringTokenizer(providesS, ", "); // NOI18N
                provides = new String[tok.countTokens()];
                for (int i = 0; i < provides.length; i++) {
                    String provide = tok.nextToken();
                    if (provide.indexOf(',') != -1) {
                        throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module-Provides: " + provide); // NOI18N
                    }
                    Dependency.create(Dependency.TYPE_MODULE, provide);
                    if (provide.lastIndexOf('/') != -1) throw new IllegalArgumentException("Illegal OpenIDE-Module-Provides: " + provide); // NOI18N
                    provides[i] = provide;
                }
                if (new HashSet(Arrays.asList(provides)).size() < provides.length) {
                    throw new IllegalArgumentException("Duplicate entries in OpenIDE-Module-Provides: " + providesS); // NOI18N
                }
            }
            String[] additionalProvides = mgr.refineProvides (this);
            if (additionalProvides != null) {
                if (provides == null) {
                    provides = additionalProvides;
                } else {
                    ArrayList l = new ArrayList ();
                    l.addAll (Arrays.asList (provides));
                    l.addAll (Arrays.asList (additionalProvides));
                    provides = (String[])l.toArray (provides);
                }
            }
            
            // Exports
            String exportsS = attr.getValue("OpenIDE-Module-Public-Packages"); // NOI18N
            if (exportsS != null) {
                if (exportsS.trim().equals("-")) { // NOI18N
                    publicPackages = new PackageExport[0];
                } else {
                    StringTokenizer tok = new StringTokenizer(exportsS, ", "); // NOI18N
                    List exports = new ArrayList(Math.max(tok.countTokens(), 1)); // List<PackageExport>
                    while (tok.hasMoreTokens()) {
                        String piece = tok.nextToken();
                        if (piece.endsWith(".*")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 2);
                            Dependency.create(Dependency.TYPE_MODULE, pkg);
                            if (pkg.lastIndexOf('/') != -1) throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            exports.add(new PackageExport(pkg.replace('.', '/') + '/', false));
                        } else if (piece.endsWith(".**")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 3);
                            Dependency.create(Dependency.TYPE_MODULE, pkg);
                            if (pkg.lastIndexOf('/') != -1) throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            exports.add(new PackageExport(pkg.replace('.', '/') + '/', true));
                        } else {
                            throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                        }
                    }
                    if (exports.isEmpty()) throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                    publicPackages = (PackageExport[])exports.toArray(new PackageExport[exports.size()]);
                }
            } else {
                Util.err.log(ErrorManager.WARNING, "Warning: module " + codeNameBase + " does not declare OpenIDE-Module-Public-Packages in its manifest, so all packages are considered public by default: http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.4-public-packages");
                publicPackages = null;
            }
            
            {
                // friends 
                String friends = attr.getValue("OpenIDE-Module-Friends"); // NOI18N
                if (friends != null) {
                    StringTokenizer tok = new StringTokenizer(friends, ", "); // NOI18N
                    HashSet set = new HashSet ();
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
                    if (publicPackages == null || publicPackages.length == 0) {
                        throw new IllegalArgumentException("No use specifying OpenIDE-Module-Friends without any public packages: " + friends); // NOI18N
                    }
                    this.friendNames = set;
                }
            }
            
            
            // Dependencies
            Set dependencies = new HashSet(20); // Set<Dependency>
            // First convert IDE/1 -> org.openide/1, so we never have to deal with
            // "IDE deps" internally:
            Set openideDeps = Dependency.create(Dependency.TYPE_IDE, attr.getValue("OpenIDE-Module-IDE-Dependencies")); // NOI18N
            if (!openideDeps.isEmpty()) {
                // If empty, leave it that way; NbInstaller will add it anyway.
                Dependency d = (Dependency)openideDeps.iterator().next();
                String name = d.getName();
                if (!name.startsWith("IDE/")) throw new IllegalStateException("Weird IDE dep: " + name); // NOI18N
                dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/" + name.substring(4) + " > " + d.getVersion())); // NOI18N
                if (dependencies.size() != 1) throw new IllegalStateException("Should be singleton: " + dependencies); // NOI18N
                
                Util.err.log(ErrorManager.WARNING, "Warning: the module " + codeNameBase + " uses OpenIDE-Module-IDE-Dependencies which is deprecated. See http://openide.netbeans.org/proposals/arch/modularize.html"); // NOI18N
            }
            dependencies.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
            dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
            String pkgdeps = attr.getValue("OpenIDE-Module-Package-Dependencies"); // NOI18N
            if (pkgdeps != null) {
                // XXX: Util.err.log(ErrorManager.WARNING, "Warning: module " + codeNameBase + " uses the OpenIDE-Module-Package-Dependencies manifest attribute, which is now deprecated: XXX URL TBD");
                dependencies.addAll(Dependency.create(Dependency.TYPE_PACKAGE, pkgdeps)); // NOI18N
            }
            dependencies.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
            // Permit the concrete installer to make some changes:
            mgr.refineDependencies(this, dependencies);
            dependenciesA = (Dependency[])dependencies.toArray(new Dependency[dependencies.size()]);
        } catch (IllegalArgumentException iae) {
            InvalidException ie = new InvalidException("While parsing a dependency attribute: " + iae.toString()); // NOI18N
            Util.err.annotate(ie, iae);
            throw ie;
        }
    }

    /** Get the JAR this module is packaged in.
     * May be null for modules installed specially, e.g.
     * automatically from the classpath.
     * @see #isFixed
     */
    public File getJarFile() {
        return jar;
    }
    
    /** Check if this is a "fixed" module.
     * Fixed modules are installed automatically (e.g. based on classpath)
     * and cannot be uninstalled or manipulated in any way.
     */
    public boolean isFixed() {
        return jar == null;
    }

    /** Create a temporary test JAR if necessary.
     * This is primarily necessary to work around a Java bug,
     * #4405789, which might be fixed in 1.4--check up on this.
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
                    Util.err.log(ErrorManager.WARNING, "Warning: temporary JAR " + physicalJar + " not currently deletable.");
                } else {
                    Util.err.log("deleted: " + physicalJar);
                }
            }
            physicalJar = null;
        } else {
            Util.err.log("no physicalJar to delete for " + this);
        }
    }
    
    /** Open the JAR, load its manifest, and do related things. */
    private void loadManifest() throws IOException {
        Util.err.log("loading manifest of " + jar);
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
                manifest = mgr.loadManifest(jar);
            }
        } catch (IOException e) {
            if (jarBeingOpened != null) {
                Util.err.annotate(e, ErrorManager.UNKNOWN, "While loading manifest from: " + jarBeingOpened, null, null, null); // NOI18N
            }
            throw e;
        }
    }
    
    /** Find any extensions loaded by the module, as well as any localized
     * variants of the module or its extensions.
     */
    private void findExtensionsAndVariants(Manifest m) {
        assert jar != null : "Cannot load extensions from classpath module " + codeNameBase;
        localeVariants = null;
        List l = Util.findLocaleVariantsOf(jar, false);
        if (!l.isEmpty()) localeVariants = new HashSet(l);
        plainExtensions = null;
        localeExtensions = null;
        String classPath = m.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if (classPath != null) {
            StringTokenizer tok = new StringTokenizer(classPath);
            while (tok.hasMoreTokens()) {
                String ext = tok.nextToken();
                if (new File(ext).isAbsolute() || ext.indexOf("../") != -1) { // NOI18N
                    if (ext.equals("../lib/updater.jar")) { // NOI18N
                        // Special case, see #24703.
                        // JAR is special to the launcher, so it makes sense in lib/ rather
                        // than modules/ext/. However updater.jar is not in startup classpath,
                        // so autoupdate module explicitly declares it this way.
                    } else {
                        Util.err.log(ErrorManager.WARNING, "WARNING: Class-Path value " + ext + " from " + jar + " is illegal according to the Java Extension Mechanism: must be relative and not move up directories");
                    }
                }
                File extfile = new File(jar.getParentFile(), ext.replace('/', File.separatorChar));
                if (! extfile.exists()) {
                    // Ignore unloadable extensions.
                    Util.err.log(ErrorManager.WARNING, "Warning: Class-Path value " + ext + " from " + jar + " cannot be found at " + extfile);
                    continue;
                }
                //No need to sync on extensionOwners - we are in write mutex
                    Set owners = (Set)extensionOwners.get(extfile);
                    if (owners == null) {
                        owners = new HashSet(2);
                        owners.add(jar);
                        extensionOwners.put(extfile, owners);
                    } else if (! owners.contains(jar)) {
                        owners.add(jar);
                        ev.log(Events.EXTENSION_MULTIPLY_LOADED, extfile, owners);
                    } // else already know about it (OK or warned)
                // Also check to make sure it is not a module JAR! See constructor for the reverse case.
                if (moduleJARs.contains(extfile)) {
                    Util.err.log(ErrorManager.WARNING, "WARNING: Class-Path value " + ext + " from " + jar + " illegally refers to another module; use OpenIDE-Module-Module-Dependencies instead");
                }
                if (plainExtensions == null) plainExtensions = new HashSet();
                plainExtensions.add(extfile);
                l = Util.findLocaleVariantsOf(extfile, false);
                if (!l.isEmpty()) {
                    if (localeExtensions == null) localeExtensions = new HashSet();
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
            while (tokenizer.hasMoreElements()) {
                String element = (String) tokenizer.nextElement();
                File fileElement = new File(element);
                if (fileElement.exists()) {
                    if (patches == null) {
                        patches = new HashSet(15);
                    }
                    patches.add(fileElement);
                }
            }
        }
        Util.err.log("localeVariants of " + jar + ": " + localeVariants);
        Util.err.log("plainExtensions of " + jar + ": " + plainExtensions);
        Util.err.log("localeExtensions of " + jar + ": " + localeExtensions);
        Util.err.log("patches of " + jar + ": " + patches);
        if (patches != null) {
            Iterator it = patches.iterator();
            while (it.hasNext()) {
                ev.log(Events.PATCH, it.next());
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
            for (int j = 0; j < jars.length; j++) {
                if (patches == null) {
                    patches = new HashSet(5);
                }
                patches.add(jars[j]);
            }
        } else {
            Util.err.log(ErrorManager.WARNING, "Could not search for patches in " + patchdir);
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
                List pairs = Util.findLocaleVariantsOf(jar, true);
                Collections.reverse(pairs);
                Iterator it = pairs.iterator();
                while (it.hasNext()) {
                    Object[] pair = (Object[])it.next();
                    File localeJar = (File)pair[0];
                    String suffix = (String)pair[1];
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
                Util.err.log("localizedProps=" + localizedProps);
            }
            */
        }
    }
    
    /** Similar, but for fixed modules only.
     * Should be very rarely used: only for classpath modules with a strangely
     * named OpenIDE-Module-Localizing-Bundle (not *.properties).
     */
    private void loadLocalizedPropsClasspath() throws InvalidException {
        Attributes attr = manifest.getMainAttributes();
        String locbundle = attr.getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        if (locbundle != null) {
            Util.err.log("Localized props in " + locbundle + " for " + attr.getValue("OpenIDE-Module"));
            try {
                int idx = locbundle.lastIndexOf('.'); // NOI18N
                String name, ext;
                if (idx == -1) {
                    name = locbundle;
                    ext = ""; // NOI18N
                } else {
                    name = locbundle.substring(0, idx);
                    ext = locbundle.substring(idx);
                }
                List suffixes = new ArrayList(10);
                Iterator it = NbBundle.getLocalizingSuffixes();
                while (it.hasNext()) {
                    suffixes.add(it.next());
                }
                Collections.reverse(suffixes);
                it = suffixes.iterator();
                while (it.hasNext()) {
                    String suffix = (String)it.next();
                    String resource = name + suffix + ext;
                    InputStream is = classloader.getResourceAsStream(resource);
                    if (is != null) {
                        Util.err.log("Found " + resource);
                        if (localizedProps == null) {
                            localizedProps = new Properties();
                        }
                        localizedProps.load(is);
                    }
                }
                if (localizedProps == null) {
                    throw new IOException("Could not find localizing bundle: " + locbundle); // NOI18N
                }
            } catch (IOException ioe) {
                InvalidException e = new InvalidException(ioe.toString());
                Util.err.annotate(e, ioe);
                throw e;
            }
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
    public List getAllJars() {
        if (jar == null) {
            // Classpath module.
            return Collections.EMPTY_LIST;
        }
        List l = new ArrayList (); // List<File>
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

    /** Is this module supposed to be easily reloadable?
     * If so, it is suitable for testing inside the IDE.
     * Controls whether a copy of the JAR file is made before
     * passing it to the classloader, which can affect locking
     * and refreshing of the JAR.
     */
    public boolean isReloadable() {
        return reloadable;
    }
    
    /** Set whether this module is supposed to be reloadable.
     * Has no immediate effect, only impacts what happens the
     * next time it is enabled (after having been disabled if
     * necessary).
     * Must be called from within a write mutex.
     * @param r whether the module should be considered reloadable
     */
    public void setReloadable(boolean r) {
        mgr.assertWritable();
        if (isFixed()) throw new IllegalStateException();
        if (reloadable != r) {
            reloadable = r;
            mgr.fireReloadable(this);
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
     * @since JST-PENDING: needed from ModuleSystem
     */
    public final void reload() throws IOException {
        if (isFixed()) throw new IllegalStateException();
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
    
    // impl of ModuleInfo method
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        if (!enabled) {
            throw new IllegalArgumentException("Not enabled: " + codeNameBase); // NOI18N
        }
        assert classloader != null : "Should have had a non-null loader for " + this;
        return classloader;
    }

    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    void classLoaderUp(Set parents) throws IOException {
        if (isFixed()) return; // no need
        Util.err.log("classLoaderUp on " + this + " with parents " + parents);
        // Find classloaders for dependent modules and parent to them.
        List loaders = new ArrayList(parents.size() + 1); // List<ClassLoader>
        // This should really be the base loader created by org.nb.Main for loading openide etc.:
        loaders.add(Module.class.getClassLoader());
        Iterator it = parents.iterator();
        while (it.hasNext()) {
            Module parent = (Module)it.next();
            PackageExport[] exports = parent.getPublicPackages();
            if (exports != null && exports.length == 0) {
                // Check if there is an impl dep here.
                Dependency[] deps = getDependenciesArray();
                boolean implDep = false;
                for (int i = 0; i < deps.length; i++) {
                    if (deps[i].getType() == Dependency.TYPE_MODULE &&
                            deps[i].getComparison() == Dependency.COMPARE_IMPL &&
                            deps[i].getName().equals(parent.getCodeName())) {
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
                Util.err.log("#24996: skipping duplicate classloader from " + parent);
                continue;
            }
            loaders.add(l);
        }
        List classp = new ArrayList(3); // List<File|JarFile>
        if (patches != null) {
            for (it = patches.iterator(); it.hasNext(); ) {
                File f = (File)it.next();
                if (f.isDirectory()) {
                    classp.add(f);
                } else {
                    classp.add(new JarFile(f, false));
                }
            }
        }
        if (reloadable) {
            ensurePhysicalJar();
            // Using OPEN_DELETE does not work well with test modules under 1.4.
            // Random code (URL handler?) still expects the JAR to be there and
            // it is not.
            classp.add(new JarFile(physicalJar, false));
        } else {
            classp.add(new JarFile(jar, false));
        }
        // URLClassLoader would not otherwise find these, so:
        if (localeVariants != null) {
        for (it = localeVariants.iterator(); it.hasNext(); ) {
            classp.add(new JarFile((File)it.next(), false));
        }
        }
        if (localeExtensions != null) {
        for (it = localeExtensions.iterator(); it.hasNext(); ) {
            File act = (File)it.next();
            classp.add(act.isDirectory() ? (Object)act : new JarFile(act, false));
        }
        }
        if (plainExtensions != null) {
        for( it = plainExtensions.iterator(); it.hasNext(); ) {
            File act = (File)it.next();
            classp.add(act.isDirectory() ? (Object)act : new JarFile(act, false));
        }
        }
        
        // #27853:
        mgr.refineClassLoader(this, loaders);
        
        try {
            classloader = new OneModuleClassLoader(classp, (ClassLoader[])loaders.toArray(new ClassLoader[loaders.size()]));
        } catch (IllegalArgumentException iae) {
            // Should not happen, but just in case.
            IOException ioe = new IOException(iae.toString());
            Util.err.annotate(ioe, iae);
            throw ioe;
        }
        oldClassLoaders.add(classloader);
    }
    
    /** Turn off the classloader and release all resources. */
    void classLoaderDown() {
        if (isFixed()) return; // don't touch it
        if (classloader instanceof ProxyClassLoader) {
            ((ProxyClassLoader)classloader).destroy();
        }
        classloader = null;
        Util.err.log("classLoaderDown on " + this + ": releaseCount=" + releaseCount + " released=" + released);
        released = false;
    }
    /** Should be called after turning off the classloader of one or more modules & GC'ing. */
    void cleanup() {
        if (isFixed()) return; // don't touch it
        if (isEnabled()) throw new IllegalStateException("cleanup on enabled module: " + this); // NOI18N
        if (classloader != null) throw new IllegalStateException("cleanup on module with classloader: " + this); // NOI18N
        if (! released) {
            Util.err.log("Warning: not all resources associated with module " + jar + " were successfully released.");
            released = true;
        } else {
            Util.err.log ("All resources associated with module " + jar + " were successfully released.");
        }
        // XXX should this rather be done when the classloader is collected?
        destroyPhysicalJar();
    }
    
    /** Notify the module that it is being deleted. */
    void destroy() {
        // #21114: try to release all JAR locks
        Iterator it = oldClassLoaders.iterator();
        while (it.hasNext()) {
            OneModuleClassLoader l = (OneModuleClassLoader)it.next();
            l.releaseLocks();
        }
        moduleJARs.remove(jar);
    }
    
    /** Get the JAR manifest.
     * Should never be null, even if disabled.
     * Might change if a module is reloaded.
     * It is not guaranteed that change events will be fired
     * for changes in this property.
     */
    public Manifest getManifest() {
        return manifest;
    }
    
    /** Get a set of {@link org.openide.modules.Dependency} objects representing missed dependencies.
     * This module is examined to see
     * why it would not be installable.
     * If it is enabled, there are no problems.
     * If it is in fact installable (possibly only
     * by also enabling some other managed modules which are currently disabled), and
     * all of its non-module dependencies are met, the returned set will be empty.
     * Otherwise it will contain a list of reasons why this module cannot be installed:
     * non-module dependencies which are not met; and module dependencies on modules
     * which either do not exist in the managed set, or are the wrong version,
     * or themselves cannot be installed
     * for some reason or another (which may be separately examined).
     * Note that in the (illegal) situation of two or more modules forming a cyclic
     * dependency cycle, none of them will be installable, and the missing dependencies
     * for each will be stated as the dependencies on the others. Again other modules
     * dependent on modules in the cycle will list failed dependencies on the cyclic modules.
     * Missing package dependencies are not guaranteed to be reported unless an install
     * of the module has already been attempted, and failed due to them.
     * The set may also contain {@link InvalidException}s representing known failures
     * of the module to be installed, e.g. due to classloader problems, missing runtime
     * resources, or failed ad-hoc dependencies. Again these are not guaranteed to be
     * reported unless an install has already been attempted and failed due to them.
     */
    public Set getProblems() {
        if (! isValid()) throw new IllegalStateException("Not valid: " + this); // NOI18N
        if (isEnabled()) return Collections.EMPTY_SET;
        return Collections.unmodifiableSet(mgr.missingDependencies(this));
    }
    
    // Access from ChangeFirer:
    final void firePropertyChange0(String prop, Object old, Object nue) {
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("Module.propertyChange: " + this + " " + prop + ": " + old + " -> " + nue);
        }
        firePropertyChange(prop, old, nue);
    }
    
    /** Get the history object representing what has happened to this module before.
     * @see ModuleHistory
     */
    public final Object getHistory() {
        return history;
    }
    
    /** String representation for debugging. */
    public String toString() {
        String s = "Module:" + getCodeNameBase(); // NOI18N
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
    private class OneModuleClassLoader extends JarClassLoader implements Util.ModuleProvider, Util.PackageAccessibleClassLoader {
        private int rc;
        /** Create a new loader for a module.
         * @param classp the List of all module jars of code directories;
         *      includes the module itself, its locale variants,
         *      variants of extensions and Class-Path items from Manifest.
         *      The items are JarFiles for jars and Files for directories
         * @param parents a set of parent classloaders (from other modules)
         */
        public OneModuleClassLoader(List classp, ClassLoader[] parents) throws IllegalArgumentException {
            super(classp, parents, false);
            rc = releaseCount++;
        }
        
        public Module getModule() {
            return Module.this;
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

        protected boolean isSpecialResource(String pkg) {
            if (mgr.isSpecialResource(pkg)) {
                return true;
            }
            return super.isSpecialResource(pkg);
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
            return mgr.shouldDelegateResource(Module.this, other, pkg);
        }
        
        public String toString() {
            return super.toString() + "[" + getCodeNameBase() + "]"; // NOI18N
        }

        protected void finalize() throws Throwable {
            super.finalize();
            Util.err.log("Finalize for " + this + ": rc=" + rc + " releaseCount=" + releaseCount + " released=" + released); // NOI18N
            if (rc == releaseCount) {
                // Hurrah! release() worked.
                released = true;
            } else {
                Util.err.log("Now resources for " + getCodeNameBase() + " have been released."); // NOI18N
            }
        }
    }
    
    /** Struct representing a package exported from a module.
     * @since org.netbeans.core/1 > 1.4
     * @see Module#getPublicPackages
     */
    public static final class PackageExport {
        /** Package to export, in the form <samp>org/netbeans/modules/foo/</samp>. */
        public final String pkg;
        /** If true, export subpackages also. */
        public final boolean recursive;
        /** Create a package export struct with the named parameters. */
        public PackageExport(String pkg, boolean recursive) {
            this.pkg = pkg;
            this.recursive = recursive;
        }
        public String toString() {
            return "PackageExport[" + pkg + (recursive ? "**/" : "") + "]"; // NOI18N
        }
    }

}
