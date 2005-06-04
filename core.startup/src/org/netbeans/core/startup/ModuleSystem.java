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

package org.netbeans.core.startup;

import org.openide.*;
import org.netbeans.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.net.URL;
import java.net.MalformedURLException;

/** Controller of the IDE's whole module system.
 * Contains higher-level convenience methods to
 * access the basic functionality and procedural
 * stages of the module system.
 * The NbTopManager should hold a reference to one instance.
 * Methods are thread-safe.
 * @author Jesse Glick
 */
public final class ModuleSystem {
    private final ModuleManager mgr;
    private final NbInstaller installer;
    private final ModuleList list;
    private final Events ev;
    
    /** Initialize module system.
     * The system file system is needed as that holds the Modules/ folder.
     * Note if the systemFileSystem is read-only, no module list will be created,
     * so it is forbidden to call readList, scanForNewAndRestore, or installNew.
     */
    public ModuleSystem(FileSystem systemFileSystem) throws IOException {
        ev = Boolean.getBoolean("netbeans.modules.quiet") ? (Events)new QuietEvents() : new NbEvents();
        installer = new NbInstaller(ev);
        mgr = new ModuleManager(installer, ev);
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ModuleManager.PROP_CLASS_LOADER.equals(ev.getPropertyName())) {
                    org.netbeans.core.startup.MainLookup.systemClassLoaderChanged(mgr.getClassLoader());
                }
            }
        };
        mgr.addPropertyChangeListener(l);
        
        // now initialize to core/* classloader, later we reassign to all modules
        org.netbeans.core.startup.MainLookup.systemClassLoaderChanged(installer.getClass ().getClassLoader ());
        // #28465: initialize module lookup early
        org.netbeans.core.startup.MainLookup.moduleLookupReady(mgr.getModuleLookup());
        if (systemFileSystem.isReadOnly()) {
            list = null;
        } else {
            FileObject root = systemFileSystem.getRoot();
            FileObject modulesFolder = root.getFileObject("Modules"); // NOI18N
            if (modulesFolder == null) {
                modulesFolder = root.createFolder("Modules"); // NOI18N
            }
            list = new ModuleList(mgr, modulesFolder, ev);
            ((NbInstaller)installer).registerList(list);
            ((NbInstaller)installer).registerManager(mgr);
        }
        ev.log(Events.CREATED_MODULE_SYSTEM);
    }
    
    /** Get the raw module manager.
     * Useful for pieces of the UI needing to directly affect the set of installed modules.
     * For example, the Modules node in the Options window may use this.
     */
    public ModuleManager getManager() {
        return mgr;
    }
    
    /** Get the event-logging handler.
     */
    public Events getEvents() {
        return ev;
    }
    
    /** Produce a list of JAR files including all installed modules,
     * their extensions, and enabled locale variants of both.
     * Will be returned in a classpath-like order.
     * Intended for use by the execution engine (though sort of deprecated).
     * @return <code>List&lt;File&gt;</code> of module-related JARs/ZIPs
     */
    public List getModuleJars () {
        mgr.mutexPrivileged().enterReadAccess();
        try {
            Iterator modules = mgr.getEnabledModules().iterator();
            List l = new ArrayList (); // List<File>
            while (modules.hasNext ()) {
                l.addAll (((Module) modules.next ()).getAllJars ());
            }
            return l;
        } finally {
            mgr.mutexPrivileged().exitReadAccess();
        }
    }

    /** We just make the modules now, restore them later
     * to optimize the layer merge.
     */
    private Set bootModules = null; // Set<Module>
    
    /** Load modules found in the classpath.
     * Note that they might not satisfy all their dependencies, in which
     * case oh well...
     */
    public void loadBootModules() {	
        // Keep a list of manifest URL prefixes which we know we do not need to
        // parse. Some of these manifests might be signed, and if so, we do not
        // want to touch them, as it slows down startup quite a bit.
        Collection ignoredPrefixes = new ArrayList(3); // List<String>
        try {
            // skip the JDK/JRE libraries
            String jdk = System.getProperty("java.home");
            if (jdk.endsWith(File.separator + "jre")) { // NOI18N
                jdk = jdk.substring(0, jdk.length() - 4);
            }
            File f = new File(jdk);
            ignoredPrefixes.add("jar:" + f.toURI().toURL()); // NOI18N
            // skip $nbhome/lib/ext/*.jar; all fixes modules should be in
            // $nbhome/lib/ (or perhaps elsewhere, with -cp:a)
            String nbhomeS = System.getProperty("netbeans.home");
            if (nbhomeS != null) {
                File nbhome = new File(nbhomeS);
                f = new File(new File(nbhome, "lib"), "ext"); // NOI18N
                ignoredPrefixes.add("jar:" + f.toURI().toURL()); // NOI18N
            }
        } catch (MalformedURLException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("ignoredPrefixes=" + ignoredPrefixes);
        }
        
        mgr.mutexPrivileged().enterWriteAccess();
        ev.log(Events.START_LOAD_BOOT_MODULES);
        try {
            bootModules = new HashSet(10);
            ClassLoader loader = ModuleSystem.class.getClassLoader();
            Enumeration e = loader.getResources("META-INF/MANIFEST.MF"); // NOI18N
            ev.log(Events.PERF_TICK, "got all manifests"); // NOI18N
            
            // There will be duplicates: cf. #32576.
            Set checkedManifests = new HashSet(); // Set<URL>
            MANIFESTS:
            while (e.hasMoreElements()) {
                URL manifestUrl = (URL)e.nextElement();
                if (!checkedManifests.add(manifestUrl)) {
                    // Already seen, ignore.
                    continue;
                }
                String manifestUrlS = manifestUrl.toExternalForm();
                Iterator it = ignoredPrefixes.iterator();
                while (it.hasNext()) {
                    if (manifestUrlS.startsWith((String)it.next())) {
                        continue MANIFESTS;
                    }
                }
                if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Util.err.log("Checking boot manifest: " + manifestUrlS);
                }
                
                InputStream is;
                try {
                    is = manifestUrl.openStream();
                } catch (IOException ioe) {
                    // Debugging for e.g. #32493 - which JAR was guilty?
                    Util.err.annotate(ioe, ErrorManager.UNKNOWN, "URL: " + manifestUrl, null, null, null); // NOI18N
                    throw ioe;
                }
                try {
                    Manifest mani = new Manifest(is);
                    Attributes attr = mani.getMainAttributes();
                    if (attr.getValue("OpenIDE-Module") == null) { // NOI18N
                        // Not a module.
                        continue;
                    }
                    bootModules.add(mgr.createFixed(mani, manifestUrl, loader));
                } finally {
                    is.close();
                }
            }
            if (list == null) {
                // Plain calling us, we have to install now.
                // Do it the simple way.
                mgr.enable(bootModules);
            }
            ev.log(Events.PERF_TICK, "added all classpath modules"); // NOI18N
	    
        } catch (IOException ioe) {
            // Note: includes also InvalidException's for malformed this and that.
            // Probably if a bootstrap module is corrupt we are in pretty bad shape
            // anyway, so don't bother trying to be fancy and install just some of
            // them etc.
            Util.err.notify(ioe);
        } catch (DuplicateException de) {
            Util.err.notify(de);
        } finally {
            // Not 100% accurate in this case:
            ev.log(Events.FINISH_LOAD_BOOT_MODULES);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Read disk settings and determine what the known modules are.
     */
    public void readList() {
        ev.log(Events.PERF_START, "ModuleSystem.readList"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            list.readInitial();
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
	ev.log(Events.PERF_END, "ModuleSystem.readList"); // NOI18N
    }
    
    /** Install read modules.
     */
    public void restore() {
	ev.log(Events.PERF_START, "ModuleSystem.restore"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Set toTrigger = new HashSet(bootModules/*Collections.EMPTY_SET*/);
            list.trigger(toTrigger);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
	ev.log(Events.PERF_END, "ModuleSystem.restore"); // NOI18N	
    }
    
    /** Shut down the system: ask modules to shut down.
     * Some of them may refuse.
     */
    public boolean shutDown(Runnable midHook) {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            return mgr.shutDown(midHook);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    /** Load a module in test (reloadable) mode.
     * If there is an existing module with a different JAR, get
     * rid of it and load this one instead.
     * If it is already installed, disable it and reenable it
     * to reload its contents.
     * If other modules depend on it, disable them first and
     * then (try to) enable them again later.
     */
    final void deployTestModule(File jar) throws IOException {
        if (! jar.isAbsolute()) throw new IOException("Absolute paths only please"); // NOI18N
        mgr.mutexPrivileged().enterWriteAccess();
        ev.log(Events.START_DEPLOY_TEST_MODULE, jar);
        // For now, just print to stderr directly; could also go thru Events.
        // No need for I18N, module developers are expected to know English
        // well enough.
        System.err.println("Deploying test module " + jar + "..."); // NOI18N
        try {
            // The test module:
            Module tm = null;
            // Anything that needs to be turned back on later:
            Set toReenable = new HashSet(); // Set<Module>
            // First see if this refers to an existing module.
            // (If so, make sure it is reloadable.)
            Iterator it = mgr.getModules().iterator();
            while (it.hasNext()) {
                Module m = (Module)it.next();
                if (jar.equals(m.getJarFile())) {
                    // Hah, found it.
                    if (! m.isReloadable()) {
                        m.setReloadable(true);
                    }
                    turnOffModule(m, toReenable);
                    mgr.reload(m);
                    tm = m;
                    break;
                }
            }
            if (tm == null) {
                // This JAR not encountered before. Try to load it. If it is
                // a duplicate of an existing module in a different location,
                // kill the existing one and replace it with this one.
                try {
                    tm = mgr.create(jar, new ModuleHistory(jar.getAbsolutePath()), true, false, false);
                } catch (DuplicateException dupe) {
                    Module old = dupe.getOldModule();
                    System.err.println("Replacing old module in " + old.getJarFile()); // NOI18N
                    turnOffModule(old, toReenable);
                    mgr.delete(old);
                    try {
                        tm = mgr.create(jar, new ModuleHistory(jar.getAbsolutePath()), true, false, false);
                    } catch (DuplicateException dupe2) {
                        // Should not happen.
                        IOException ioe = new IOException(dupe2.toString());
                        Util.err.annotate(ioe, dupe2);
                        throw ioe;
                    }
                }
            }
            // Try to turn on the test module. It might throw InvalidExc < IOExc.
            System.err.println("Enabling " + tm.getJarFile() + "..."); // NOI18N
            if (!mgr.simulateEnable(Collections.singleton(tm)).contains(tm)) {
                throw new IOException("Cannot enable " + tm.getJarFile() + "; problems: " + tm.getProblems());
            }
            mgr.enable(tm);
            // OK, so far so good; also try to turn on any other modules if
            // we can that were on before. Just try to turn them all on.
            // Don't get fancy; if some of them could not be turned on, the
            // developer will be told and can clean up the situation as needed.
            // Also any of them marked as reloadable, reload them now.
            if (! toReenable.isEmpty()) {
                System.err.println("Also re-enabling:"); // NOI18N
                it = toReenable.iterator();
                while (it.hasNext()) {
                    Module m = (Module)it.next();
                    System.err.println("\t" + m.getDisplayName()); // NOI18N
                    if (m.isReloadable()) {
                        m.reload();
                    }
                }
                try {
                    mgr.enable(toReenable);
                } catch (IllegalArgumentException iae) {
                    // Strange new dependencies, etc.
                    throw new IOException(iae.toString());
                }
            }
            System.err.println("Done."); // NOI18N
        } finally {
            ev.log(Events.FINISH_DEPLOY_TEST_MODULE, jar);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    /** Make sure some module is disabled.
     * If there were any other non-autoload modules enabled
     * which depended on it, make note of them.
     */
    private void turnOffModule(Module m, Set toReenable) {
        if (! m.isEnabled()) {
            // Already done.
            return;
        }
        Iterator it = mgr.simulateDisable(Collections.singleton(m)).iterator();
        while (it.hasNext()) {
            Module m2 = (Module)it.next();
            if (!m2.isAutoload() && !m2.isEager()) {
                toReenable.add(m2);
            }
        }
        try {
            System.err.println("Disabling " + m.getJarFile() + "..."); // NOI18N
            // Don't mention the others, they will be mentioned later anyway.
            mgr.disable(toReenable);
        } finally {
            toReenable.remove(m);
        }
    }
    
    /** Get the effective "classpath" used by a module.
     * <p>This is a somewhat stretched notion, but should give something that looks
     * as much like a classpath as possible, i.e. a list of directories or JARs
     * separated by the standard separator, which roughly represents what resources
     * are visible to the module's classloader. May use special syntax to represent
     * situations in which only certain packages are available from a particular
     * "classpath" entry.
     * <p>Disabled modules have no classpath (empty string).
     * <p>Call within a mutex.
     * @param m the module to build a classpath for
     * @return an approximation of that module's classpath
     * @see "#22466"
     * @since org.netbeans.core/1 > 1.5
     */
    public String getEffectiveClasspath(Module m) {
        return installer.getEffectiveClasspath(m);
    }
    
    /** Dummy event handler that does not print anything.
     * Useful for test scripts where you do not really want to see
     * everything going by.
     */
    private static final class QuietEvents extends Events {
        QuietEvents() {}
        protected void logged(String message, Object[] args) {}
    }
    
}
