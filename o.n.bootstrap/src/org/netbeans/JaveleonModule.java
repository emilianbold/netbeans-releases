/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Allan Gregersen
 */
public final class JaveleonModule extends Module {

    public static boolean isJaveleonPresent;

    static {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.javeleon.reload.ReloadFacade");
            isJaveleonPresent = true;
        } catch (ClassNotFoundException ex) {
            // Javeleon was not present... nothing to do then!
        }
    }

        /*
     * Method used by OneModuleClassLoader to register a class loader a
     * to the Javeleon runtime. The associated module will be update-enabled
     */
    static Method javeleonFacadeMethod;

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

    public static Method javeleonReloadMethod;

    static {
        if(JaveleonModule.isJaveleonPresent) {
            try {
                Class javeleonReloadClass = Thread.currentThread().getContextClassLoader().loadClass("org.javeleon.reload.ReloadModule");
                javeleonReloadMethod = javeleonReloadClass.getDeclaredMethod("incrementGlobalId", new Class[]{});
            } catch (Exception ex) {
               // No worries, javeleon is just not enabled
            }
        }
    }

    private String codeNameBase;
    private Dependency[] dependenciesA;
    private final File jar;
    private final File physicalJar;
    private Manifest manifest;
    private static HashMap<String,ClassLoader> currentClassLoaders = new HashMap<String, ClassLoader>();

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


    JaveleonModule(ModuleManager mgr, File jar, Object history, Events ev) throws IOException {
        super(mgr, ev, history, true, false, false);
        this.jar = jar;
        physicalJar = Util.makeTempJar(jar);
    }

    @Override
    public File getJarFile() {
        return jar;
    }
    
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
            ClassLoader l = getCurrentModuleClassLoader(parent);
            if (parent.isFixed() && loaders.contains(l)) {
                Util.err.log(Level.FINE, "#24996: skipping duplicate classloader from {0}", parent);
                continue;
            }
            loaders.add(l);
        }
        List<File> classp = new ArrayList<File>(3);
        findExtensionsAndVariants(manifest);
        if (patches != null) classp.addAll(patches);

        // Using OPEN_DELETE does not work well with test modules under 1.4.
        // Random code (URL handler?) still expects the JAR to be there and
        // it is not.
        classp.add(physicalJar);

        // URLClassLoader would not otherwise find these, so:
        if (localeVariants != null) classp.addAll(localeVariants);

        if (localeExtensions != null) classp.addAll(localeExtensions);

        if (plainExtensions != null) classp.addAll(plainExtensions);
        
        try {
            classloader = new JaveleonModuleClassLoader(classp, loaders.toArray(new ClassLoader[loaders.size()]));
            currentClassLoaders.put(getCodeNameBase(), classloader);
        } catch (IllegalArgumentException iae) {
            // Should not happen, but just in case.
            throw (IOException) new IOException(iae.toString()).initCause(iae);
        }
    }

    private ClassLoader getCurrentModuleClassLoader(Module m) {
        if(currentClassLoaders.containsKey(m.getCodeNameBase()))
            return currentClassLoaders.get(m.getCodeNameBase());
        else
            return m.getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        return classloader;
    }
        /** Find any extensions loaded by the module, as well as any localized
     * variants of the module or its extensions.
     */
    private void findExtensionsAndVariants(Manifest m) {
        assert jar != null : "Cannot load extensions from classpath module " + getCodeNameBase();
        localeVariants = null;
        List<File> l = LocaleVariants.findLocaleVariantsOf(jar, getCodeNameBase());
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
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("localeVariants of " + jar + ": " + localeVariants);
            Util.err.fine("plainExtensions of " + jar + ": " + plainExtensions);
            Util.err.fine("localeExtensions of " + jar + ": " + localeExtensions);
            Util.err.fine("patches of " + jar + ": " + patches);
        }
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

    @Override
    public String toString() {
        return "Javeleon module version " + jar.toString();
    }

    @Override
    public List<File> getAllJars() {
        return Collections.singletonList(jar);
    }

    @Override
    public void setReloadable(boolean r) {
        //
    }

    @Override
    public void reload() throws IOException {
        // Javeleon will do this
    }

    @Override
    protected void classLoaderDown() {
        // do not touch the class loader... Javeleon system will handle it
    }

    @Override
    protected void cleanup() {
        
    }

    @Override
    protected void destroy() {
        // nothing to do
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public Manifest getManifest() {
        if(manifest == null) {
            try {
                loadManifest();
            } catch (IOException ex) {
                manifest = new Manifest();
            }
        }
        return manifest;
    }

    /** Open the JAR, load its manifest, and do related things. */
    private void loadManifest() throws IOException {
       manifest = getManager().loadManifest(physicalJar);
       parseManifest();
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        return null;
    }

    public Dependency[] getJaveleonDependencies() {
        if(dependenciesA == null)
            initDeps(); // TODO . find attributes

        return dependenciesA;
    }

     /** Initializes dependencies of this module
     *
     * @param knownDeps Set<Dependency> of this module known from different source,
     *    can be null
     * @param attr attributes in manifest to parse if knownDeps is null
     */
    private void initDeps()
    throws IllegalStateException, IllegalArgumentException {

        Attributes attr = getManifest().getMainAttributes();
        // Dependencies
        Set<Dependency> dependencies = new HashSet<Dependency>(20);
        // First convert IDE/1 -> org.openide/1, so we never have to deal with
        // "IDE deps" internally:
        @SuppressWarnings(value = "deprecation")
        Set<Dependency> openideDeps = Dependency.create(Dependency.TYPE_IDE, attr.getValue("OpenIDE-Module-IDE-Dependencies")); // NOI18N
        if (!openideDeps.isEmpty()) {
            // If empty, leave it that way; NbInstaller will add it anyway.
            Dependency d = openideDeps.iterator().next();
            String name = d.getName();
            if (!name.startsWith("IDE/")) {
                throw new IllegalStateException("Weird IDE dep: " + name); // NOI18N
            }
            dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/" + name.substring(4) + " > " + d.getVersion())); // NOI18N
            if (dependencies.size() != 1) {
                throw new IllegalStateException("Should be singleton: " + dependencies); // NOI18N
            }
            Util.err.log(Level.WARNING, "the module {0} uses OpenIDE-Module-IDE-Dependencies which is deprecated. See http://openide.netbeans.org/proposals/arch/modularize.html", codeNameBase); // NOI18N
        }
        dependencies.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
        dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
        String pkgdeps = attr.getValue("OpenIDE-Module-Package-Dependencies"); // NOI18N
        if (pkgdeps != null) {
            // XXX: Util.err.log(ErrorManager.WARNING, "Warning: module " + codeNameBase + " uses the OpenIDE-Module-Package-Dependencies manifest attribute, which is now deprecated: XXX URL TBD");
            dependencies.addAll(Dependency.create(Dependency.TYPE_PACKAGE, pkgdeps)); // NOI18N
        }
        dependencies.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
        dependencies.addAll(Dependency.create(Dependency.TYPE_NEEDS, attr.getValue("OpenIDE-Module-Needs"))); // NOI18N
        dependencies.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, attr.getValue("OpenIDE-Module-Recommends"))); // NOI18N
        // Permit the concrete installer to make some changes:
        dependenciesA = dependencies.toArray(new Dependency[dependencies.size()]);
    }

    class JaveleonModuleClassLoader extends JarClassLoader {

        public JaveleonModuleClassLoader(List<File> allJars, ClassLoader[] parents) {
            super(allJars, parents);
            try {
                javeleonFacadeMethod.invoke(null, this, getCodeNameBase());
            } catch (Exception ex) {
                // OK, give up. Javeleon is not enabled!
            }
        }

        /**
         * Look up a native library as described in modules documentation.
         * @see http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/api.html#jni
         */
        protected @Override String findLibrary(String libname) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            String arch = System.getProperty("os.arch"); // NOI18N
            String system = System.getProperty("os.name").toLowerCase(); // NOI18N
            String mapped = System.mapLibraryName(libname);
            File lib;

            lib = ifl.locate("modules/lib/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) return lib.getAbsolutePath();

            lib = ifl.locate("modules/lib/" + arch + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) return lib.getAbsolutePath();

            lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) return lib.getAbsolutePath();

            return null;
        }
    }

}
