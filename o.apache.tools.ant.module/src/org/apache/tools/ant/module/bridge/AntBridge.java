/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.bridge;

import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.event.*;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;

/**
 * Utility class providing entry points to the bridging functionality.
 * @author Jesse Glick
 */
public final class AntBridge {
    
    private AntBridge() {}
    
    private static final String KEY_MAIN_CLASS_LOADER = "mainClassLoader"; // NOI18N
    private static final String KEY_AUX_CLASS_LOADER = "auxClassLoader"; // NOI18N
    private static final String KEY_BRIDGE = "bridge"; // NOI18N
    private static final String KEY_CUSTOM_DEFS = "customDefs";
    private static Reference stuff = null; // Reference<Map>
    
    private static List listeners = new ArrayList(); // List<ChangeListener>
    
    private static final class MiscListener implements PropertyChangeListener, LookupListener {
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (prop == null ||
                    AntSettings.PROP_ANT_HOME.equals(prop) ||
                    AntSettings.PROP_EXTRA_CLASSPATH.equals(prop)) {
                AntModule.err.log("AntBridge got settings change in " + prop);
                fireChange();
            }
        }
        public void resultChanged(LookupEvent ev) {
            AntModule.err.log("AntModule got ClassLoader change");
            fireChange();
        }
    }
    private static MiscListener miscListener = new MiscListener();
    private static Lookup.Result classpathResult = Lookup.getDefault().lookup(new Lookup.Template(ClassLoader.class));
    static {
        AntSettings.getDefault().addPropertyChangeListener(miscListener);
        classpathResult.addLookupListener(miscListener);
    }
    
    /**
     * Listen for changes in the contents of the bridge, as e.g. after changing the
     * location of the installed copy of Ant.
     */
    public static synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * Stop listening for changes in the contents of the bridge.
     */
    public static synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private static void fireChange() {
        stuff = null;
        ChangeEvent ev = new ChangeEvent(AntBridge.class);
        ChangeListener[] ls;
        synchronized (AntBridge.class) {
            ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (int i = 0; i < ls.length; i++) {
            ls[i].stateChanged(ev);
        }
    }
    
    /**
     * Get the loader responsible for loading Ant together with any
     * user-defined classpath.
     */
    public static ClassLoader getMainClassLoader() {
        return (ClassLoader)getStuff().get(KEY_MAIN_CLASS_LOADER);
    }
    
    /**
     * Get the loader which contains the bridge code as well as any
     * custom-defined tasks.
     */
    private static ClassLoader getAuxClassLoader() {
        return (ClassLoader)getStuff().get(KEY_AUX_CLASS_LOADER);
    }
    
    /**
     * Get any custom task/type definitions stored in $nbhome/ant/nblib/*.jar.
     * Some of the classes might not be fully resolvable, so beware.
     */
    public static Map/*<String,Map<String,Class>>*/ getCustomDefs() {
        return (Map)getStuff().get(KEY_CUSTOM_DEFS);
    }
    
    /**
     * Return a class loader which can load from Ant JARs as well the user
     * development class path. It is not cached, since user classes can
     * change quickly. Similar to NbClassLoader.
     * @param reference a file to refer to in order to determine the proper class path; may be null
     */
    public static ClassLoader createUserClassLoader(FileObject reference) {
        ClassLoader main = getMainClassLoader();
        ClassPath cp = ClassPath.getClassPath(reference, ClassPath.EXECUTE);
        if (cp != null) {
            try {
                // XXX use ClassPath.getClassLoader when that method accepts a parent loader!
                FileObject[] roots = cp.getRoots();
                URL[] urls = new URL[roots.length];
                for (int i = 0; i < roots.length; i++) {
                    urls[i] = roots[i].getURL();
                }
                return new AllPermissionURLClassLoader(urls, main);
            } catch (FileStateInvalidException ie) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, ie);
            }
        }
        return main;
    }
    
    /**
     * Get the bridge interface.
     */
    public static BridgeInterface getInterface() {
        return (BridgeInterface)getStuff().get(KEY_BRIDGE);
    }
    
    private synchronized static Map getStuff() {
        Map m;
        if (stuff != null) {
            m = (Map)stuff.get();
        } else {
            m = null;
        }
        if (m == null) {
            m = createStuff();
            stuff = new SoftReference(m);
        }
        return m;
    }
    
    private static Map createStuff() {
        AntModule.err.log("AntBridge.createStuff - loading Ant installation...");
        Map m = new HashMap();
        try {
            ClassLoader main = createMainClassLoader();
            m.put(KEY_MAIN_CLASS_LOADER, main);
            // Ensures that the loader is functional, and that it is at least 1.5.x
            // so that our classes can link against it successfully:
            main.loadClass("org.apache.tools.ant.input.InputHandler"); // NOI18N
            File[] nblibs = getNblibs();
            ClassLoader aux = createAuxClassLoader(nblibs, main);
            m.put(KEY_AUX_CLASS_LOADER, aux);
            Class impl = aux.loadClass("org.apache.tools.ant.module.bridge.impl.BridgeImpl"); // NOI18N
            m.put(KEY_BRIDGE, (BridgeInterface)impl.newInstance());
            m.put(KEY_CUSTOM_DEFS, createCustomDefs(nblibs, aux));
        } catch (Exception e) {
            fallback(m, e);
        } catch (LinkageError e) {
            fallback(m, e);
        }
        return m;
    }
    
    private static void fallback(Map m, Throwable e) {
        m.clear();
        ClassLoader dummy = ClassLoader.getSystemClassLoader();
        m.put(KEY_MAIN_CLASS_LOADER, dummy);
        m.put(KEY_AUX_CLASS_LOADER, dummy);
        m.put(KEY_BRIDGE, new DummyBridgeImpl(e));
        Map defs = new HashMap();
        defs.put("task", new HashMap()); // NOI18N
        defs.put("type", new HashMap()); // NOI18N
        m.put(KEY_CUSTOM_DEFS, defs);
    }
    
    private static final class JarFilter implements FilenameFilter {
        JarFilter() {}
        public boolean accept(File dir, String name) {
            return name.toLowerCase(Locale.US).endsWith(".jar"); // NOI18N
        }
    }
    
    private static ClassLoader createMainClassLoader() throws Exception {
        List cp = new ArrayList(); // List<URL>
        File libdir = new File(AntSettings.getDefault().getAntHome(), "lib"); // NOI18N
        if (!libdir.isDirectory()) throw new IOException("No such Ant library dir: " + libdir); // NOI18N
        AntModule.err.log("Creating main class loader from " + libdir);
        File[] libs = libdir.listFiles(new JarFilter());
        if (libs == null) throw new IOException("Listing: " + libdir); // NOI18N
        for (int i = 0; i < libs.length; i++) {
            cp.add(libs[i].toURI().toURL());
        }
        NbClassPath extra = AntSettings.getDefault().getExtraClasspath();
        String extrapath = extra.getClassPath();
        if (extrapath.startsWith("\"") && extrapath.endsWith("\"")) { // NOI18N
            // *@%!* NbClassPath.getClassPath semantics.
            extrapath = extrapath.substring(1, extrapath.length() - 1);
        }
        StringTokenizer tok = new StringTokenizer(extrapath, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            cp.add(new File(tok.nextToken()).toURI().toURL());
        }
        // XXX note that systemClassLoader will include boot.jar, and perhaps anything else
        // in lib/ext/*.jar, like rmi-ext.jar. Would be nicer to exclude everything NB-specific.
        // However the simplest way - to use the parent loader (JRE ext loader) - does not work
        // well because Ant assumes that tools.jar is in its classpath (for <javac> etc.).
        // Manually readding the JDK JARs would be possible, but then they would not be shared
        // with the versions used inside NB, which may cause inefficiencies or more memory usage.
        return new AllPermissionURLClassLoader((URL[])cp.toArray(new URL[cp.size()]), ClassLoader.getSystemClassLoader());
    }
    
    private static File[] getNblibs() throws IOException {
        // XXX this will not work for modules installed in the user dir...
        // pending stronger semantics from IFL re. directories
        // -> when this is fixed, remove ant/nblib check from org.netbeans.modules.autoupdate.ModuleUpdate
        File nblibdir = InstalledFileLocator.getDefault().locate("ant/nblib", "org.apache.tools.ant.module", false); // NOI18N
        File bridgeJar = new File(nblibdir, "bridge.jar");
        if (!bridgeJar.isFile()) throw new IOException("No such Ant bridge JAR: " + bridgeJar); // NOI18N
        File[] libs = nblibdir.listFiles(new JarFilter());
        if (libs == null) throw new IOException("Listing: " + nblibdir); // NOI18N
        return libs;
    }
    
    private static ClassLoader createAuxClassLoader(File[] libs, ClassLoader main) throws Exception {
        List cp = new ArrayList(); // List<URL>
        for (int i = 0; i < libs.length; i++) {
            cp.add(libs[i].toURI().toURL());
        }
        ClassLoader nbLoader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        return new AuxClassLoader(nbLoader, main, (URL[])cp.toArray(new URL[cp.size()]));
    }
    
    private static Map/*<String,Map<String,Class>>*/ createCustomDefs(File[] libs, ClassLoader aux) throws IOException {
        Map m = new HashMap();
        Map tasks = new HashMap();
        Map types = new HashMap();
        m.put("task", tasks); // NOI18N
        m.put("type", types); // NOI18N
        for (int i = 0; i < libs.length; i++) {
            JarFile j = new JarFile(libs[i]);
            try {
                JarEntry e = j.getJarEntry("META-INF/taskdefs.properties"); // NOI18N
                if (e != null) {
                    AntModule.err.log("Loading custom taskdefs from " + libs[i]);
                    loadDefs(j.getInputStream(e), tasks, aux);
                }
                e = j.getJarEntry("META-INF/typedefs.properties"); // NOI18N
                if (e != null) {
                    AntModule.err.log("Loading custom typedefs from " + libs[i]);
                    loadDefs(j.getInputStream(e), tasks, aux);
                }
            } finally {
                j.close();
            }
        }
        return m;
    }
    
    private static void loadDefs(InputStream is, Map defs, ClassLoader l) throws IOException {
        // Similar to IntrospectedInfo.load, but just picks up the classes.
        Properties p = new Properties();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        Iterator it = p.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            String clazzname = (String)entry.getValue();
            try {
                Class clazz = l.loadClass(clazzname);
                defs.put(name, clazz);
            } catch (ClassNotFoundException cnfe) {
                // This is not normal. If the class is mentioned, it should be there.
                IOException ioe = new IOException("Could not load class " + clazzname + ": " + cnfe); // NOI18N
                AntModule.err.annotate(ioe, cnfe);
                throw ioe;
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. tasks dumped there by disabled modules.
                AntModule.err.log("AntBridge.loadDefs: skipping " + clazzname + ": " + ncdfe);
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                IOException ioe = new IOException("Could not load class " + clazzname + ": " + e); // NOI18N
                AntModule.err.annotate(ioe, e);
                throw ioe;
            }
        }
    }
    
    static class AllPermissionURLClassLoader extends URLClassLoader {
        
        private static PermissionCollection allPermission;
        private static synchronized PermissionCollection getAllPermissions() {
            if (allPermission == null) {
                allPermission = new Permissions();
                allPermission.add(new AllPermission());
            }
            return allPermission;
        }
        
        public AllPermissionURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        
        protected final PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermissions();
        }

        /* Debugging:
        public URL getResource(String name) {
            URL u = super.getResource(name);
            System.err.println("ACL.gR: " + name + " -> " + u + " [" + this + "]");
            return u;
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            try {
                Class c = super.loadClass(name);
                java.security.CodeSource s = c.getProtectionDomain().getCodeSource();
                System.err.println("ACL.lC: " + name + " from " + (s != null ? s.getLocation() : null) + " [" + this + "]");
                return c;
            } catch (ClassNotFoundException e) {
                System.err.println("ACL.lC: CNFE on " + name + " [" + this + "]");
                throw e;
            }
        }
         */
        
    }
    
}
