/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import org.openide.modules.ModuleInfo;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class providing entry points to the bridging functionality.
 * @author Jesse Glick
 */
public final class AntBridge {
    
    private AntBridge() {}
    
    private static final String KEY_MAIN_CLASS_LOADER = "mainClassLoader"; // NOI18N
    private static final String KEY_BRIDGE_CLASS_LOADER = "bridgeClassLoader"; // NOI18N
    private static final String KEY_BRIDGE = "bridge"; // NOI18N
    private static final String KEY_CUSTOM_DEFS = "customDefs"; // NOI18N
    private static final String KEY_CUSTOM_DEF_CLASS_LOADERS = "customDefClassLoaders"; // NOI18N
    private static Reference stuff = null; // Reference<Map>
    
    private static List listeners = new ArrayList(); // List<ChangeListener>
    
    private static final class MiscListener implements PropertyChangeListener, LookupListener {
        MiscListener() {}
        private ModuleInfo[] modules = null;
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (AntSettings.PROP_ANT_HOME.equals(prop) ||
                    AntSettings.PROP_EXTRA_CLASSPATH.equals(prop)) {
                AntModule.err.log("AntBridge got settings change in " + prop);
                fireChange();
            } else if (ModuleInfo.PROP_ENABLED.equals(prop)) {
                AntModule.err.log("AntBridge got module enablement change on " + ev.getSource());
                fireChange();
            }
        }
        public void resultChanged(LookupEvent ev) {
            AntModule.err.log("AntModule got ModuleInfo change");
            synchronized (this) {
                if (modules != null) {
                    for (int i = 0; i < modules.length; i++) {
                        modules[i].removePropertyChangeListener(this);
                    }
                    modules = null;
                }
            }
            fireChange();
        }
        public synchronized ModuleInfo[] getEnabledModules() {
            if (modules == null) {
                Collection c = modulesResult.allInstances();
                modules = (ModuleInfo[])c.toArray(new ModuleInfo[c.size()]);
                for (int i = 0; i < modules.length; i++) {
                    modules[i].addPropertyChangeListener(this);
                }
            }
            List/*<ModuleInfo>*/ enabledModules = new ArrayList(modules.length);
            for (int i = 0; i < modules.length; i++) {
                if (modules[i].isEnabled()) {
                    enabledModules.add(modules[i]);
                }
            }
            return (ModuleInfo[])enabledModules.toArray(new ModuleInfo[enabledModules.size()]);
        }
    }
    private static MiscListener miscListener = new MiscListener();
    private static Lookup.Result modulesResult = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class));
    static {
        AntSettings.getDefault().addPropertyChangeListener(miscListener);
        modulesResult.addLookupListener(miscListener);
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
     * Get the loader which contains the bridge code.
     */
    private static ClassLoader getBridgeClassLoader() {
        return (ClassLoader)getStuff().get(KEY_BRIDGE_CLASS_LOADER);
    }
    
    /**
     * Get any custom task/type definitions stored in $nbhome/ant/nblib/*.jar.
     * Some of the classes might not be fully resolvable, so beware.
     * The names will include namespace prefixes.
     * <p>
     * Only minimal antlib syntax is currently interpreted here:
     * only <code>&lt;taskdef&gt;</code> and <code>&lt;typedef&gt;</code>,
     * and only the <code>name</code> and <code>classname</code> attributes.
     */
    public static Map/*<String,Map<String,Class>>*/ getCustomDefsWithNamespace() {
        return (Map)getStuff().get(KEY_CUSTOM_DEFS);
    }
    
    /**
     * Same as {@link #getCustomDefsWithNamespace} but without any namespace prefixes.
     */
    public static Map/*<String,Map<String,Class>>*/ getCustomDefsNoNamespace() {
        Map/*<String,Map<String,Class>>*/ m = new HashMap();
        Iterator it = getCustomDefsWithNamespace().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String type = (String)entry.getKey();
            Map defs = (Map)entry.getValue();
            Map/*Map<String,Class>*/ m2 = new HashMap();
            Iterator it2 = defs.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry entry2 = (Map.Entry)it2.next();
                String fqn = (String)entry2.getKey();
                Class clazz = (Class)entry2.getValue();
                String name;
                int idx = fqn.lastIndexOf(':');
                if (idx != -1) {
                    name = fqn.substring(idx + 1);
                } else {
                    name = fqn;
                }
                m2.put(name, clazz);
            }
            m.put(type, m2);
        }
        return m;
    }
    
    /**
     * Get a map from enabled module code name bases to class loaders containing
     * JARs from ant/nblib/*.jar.
     */
    public static Map/*<String,ClassLoader>*/ getCustomDefClassLoaders() throws IOException {
        return (Map)getStuff().get(KEY_CUSTOM_DEF_CLASS_LOADERS);
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
                // Cf. #37437.
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
            ClassLoader bridgeLoader = createBridgeClassLoader(main);
            m.put(KEY_BRIDGE_CLASS_LOADER, bridgeLoader);
            Class impl = bridgeLoader.loadClass("org.apache.tools.ant.module.bridge.impl.BridgeImpl"); // NOI18N
            m.put(KEY_BRIDGE, (BridgeInterface)impl.newInstance());
            Map cDCLs = createCustomDefClassLoaders(main);
            m.put(KEY_CUSTOM_DEF_CLASS_LOADERS, cDCLs);
            m.put(KEY_CUSTOM_DEFS, createCustomDefs(cDCLs));
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
        m.put(KEY_BRIDGE_CLASS_LOADER, dummy);
        m.put(KEY_BRIDGE, new DummyBridgeImpl(e));
        Map defs = new HashMap();
        defs.put("task", new HashMap()); // NOI18N
        defs.put("type", new HashMap()); // NOI18N
        m.put(KEY_CUSTOM_DEFS, defs);
        m.put(KEY_CUSTOM_DEF_CLASS_LOADERS, Collections.EMPTY_MAP);
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
    
    private static ClassLoader createBridgeClassLoader(ClassLoader main) throws Exception {
        File bridgeJar = InstalledFileLocator.getDefault().locate("ant/nblib/bridge.jar", "org.apache.tools.ant.module", false); // NOI18N
        if (bridgeJar == null) {
            throw new IllegalStateException("no ant/nblib/bridge.jar found"); // NOI18N
        }
        return createAuxClassLoader(bridgeJar, main, AntBridge.class.getClassLoader());
    }
    
    private static ClassLoader createAuxClassLoader(File lib, ClassLoader main, ClassLoader moduleLoader) throws IOException {
        return new AuxClassLoader(moduleLoader, main, lib.toURI().toURL());
    }
    
    /**
     * Get a map from enabled module code name bases to class loaders containing
     * JARs from ant/nblib/*.jar.
     */
    private static Map/*<String,ClassLoader>*/ createCustomDefClassLoaders(ClassLoader main) throws IOException {
        Map/*<String,ClassLoader>*/ m = new HashMap();
        ModuleInfo[] modules = miscListener.getEnabledModules();
        InstalledFileLocator ifl = InstalledFileLocator.getDefault();
        for (int i = 0; i < modules.length; i++) {
            String cnb = modules[i].getCodeNameBase();
            String cnbDashes = cnb.replace('.', '-');
            File lib = ifl.locate("ant/nblib/" + cnbDashes + ".jar", cnb, false); // NOI18N
            if (lib == null) {
                continue;
            }
            ClassLoader l = createAuxClassLoader(lib, main, modules[i].getClassLoader());
            m.put(cnb, l);
        }
        return m;
    }
    
    private static Map/*<String,Map<String,Class>>*/ createCustomDefs(Map cDCLs) throws IOException {
        Map m = new HashMap();
        Map tasks = new HashMap();
        Map types = new HashMap();
        // XXX #36776: should eventually support <macrodef>s here
        m.put("task", tasks); // NOI18N
        m.put("type", types); // NOI18N
        Iterator it = cDCLs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String cnb = (String)entry.getKey();
            ClassLoader l = (ClassLoader)entry.getValue();
            String resource = cnb.replace('.', '/') + "/antlib.xml"; // NOI18N
            URL antlib = l.getResource(resource);
            if (antlib == null) {
                throw new IOException("Could not find " + antlib + " in ant/nblib/" + cnb.replace('.', '-') + ".jar"); // NOI18N
            }
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(antlib.toExternalForm()), false, true, /*XXX needed?*/null, null);
            } catch (SAXException e) {
                throw (IOException)new IOException(e.toString()).initCause(e);
            }
            Element docEl = doc.getDocumentElement();
            if (!docEl.getLocalName().equals("antlib")) { // NOI18N
                throw new IOException("Bad root element for " + antlib + ": " + docEl); // NOI18N
            }
            NodeList nl = docEl.getChildNodes();
            Properties newTaskDefs = new Properties();
            Properties newTypeDefs = new Properties();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element def = (Element)n;
                boolean type;
                if (def.getNodeName().equals("taskdef")) { // NOI18N
                    type = false;
                } else if (def.getNodeName().equals("typedef")) { // NOI18N
                    type = true;
                } else {
                    AntModule.err.log(ErrorManager.WARNING, "Warning: unrecognized definition " + def + " in " + antlib);
                    continue;
                }
                String name = def.getAttribute("name"); // NOI18N
                if (name == null) {
                    // Not a hard error since there might be e.g. <taskdef resource="..."/> here
                    // which we do not parse but which is permitted in antlib by Ant.
                    AntModule.err.log(ErrorManager.WARNING, "Warning: skipping definition " + def + " with no 'name' in " + antlib);
                    continue;
                }
                String classname = def.getAttribute("classname"); // NOI18N
                if (classname == null) {
                    // But this is a hard error.
                    throw new IOException("No 'classname' attr on def of " + name + " in " + antlib); // NOI18N
                }
                // XXX would be good to handle at least onerror attr too
                (type ? newTypeDefs : newTaskDefs).setProperty(name, classname);
            }
            loadDefs(newTaskDefs, tasks, l);
            loadDefs(newTypeDefs, types, l);
        }
        return m;
    }
    
    private static void loadDefs(Properties p, Map defs, ClassLoader l) throws IOException {
        // Similar to IntrospectedInfo.load, after having parsed the properties.
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
                // Cf. #36702 for possible better solution.
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
    
    // XXX better would be to multiplex to the currently running Ant process, as
    // determined by the current thread group
    // Better still would be to let the execution engine handle it entirely,
    // by creating a custom InputOutput: #1961
    
    private static PrintStream origOut, origErr;
    
    /**
     * Handle I/O scoping for overlapping project runs.
     * You must call {@link #restoreSystemOutErr} in a finally block.
     * @param out new temporary output stream for the VM
     * @param err new temporary error stream for the VM
     * @see "#36396"
     */
    public static synchronized void pushSystemOutErr(PrintStream out, PrintStream err) {
        if (origOut == null) {
            origOut = System.out;
            origErr = System.err;
        } else {
            // Oh well, old output may be sent to the wrong window...
        }
        System.setOut(out);
        System.setErr(err);
    }
    
    /**
     * Restore original I/O streams after a call to {@link #pushSystemOutErr}.
     */
    public static synchronized void restoreSystemOutErr() {
        if (origOut != null) {
            System.setErr(origErr);
            System.setOut(origOut);
            origOut = null;
            origErr = null;
        } else {
            // Again, never mind.
        }
    }
    
}
