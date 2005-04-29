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

package org.apache.tools.ant.module.bridge;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntSettings;
import org.openide.ErrorManager;
import org.openide.execution.NbClassPath;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.io.NullOutputStream;
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
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance(AntBridge.class.getName());
    
    private AntBridge() {}
    
    private static final String KEY_MAIN_CLASS_PATH = "mainClassPath"; // NOI18N
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
                err.log("AntBridge got settings change in " + prop);
                fireChange();
            } else if (ModuleInfo.PROP_ENABLED.equals(prop)) {
                err.log("AntBridge got module enablement change on " + ev.getSource());
                fireChange();
            }
        }
        public void resultChanged(LookupEvent ev) {
            err.log("AntModule got ModuleInfo change");
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
        err.log("AntBridge.createStuff - loading Ant installation...");
        Map m = new HashMap();
        try {
            List/*<File>*/ mainClassPath = createMainClassPath();
            err.log("mainClassPath=" + mainClassPath);
            m.put(KEY_MAIN_CLASS_PATH, classPathToString(mainClassPath));
            ClassLoader main = createMainClassLoader(mainClassPath);
            m.put(KEY_MAIN_CLASS_LOADER, main);
            ClassLoader bridgeLoader = createBridgeClassLoader(main);
            m.put(KEY_BRIDGE_CLASS_LOADER, bridgeLoader);
            // Ensures that the loader is functional, and that it is at least 1.5.x
            // so that our classes can link against it successfully, and that
            // we are really loading Ant from the right place:
            Class ihClazz = Class.forName("org.apache.tools.ant.input.InputHandler", false, bridgeLoader); // NOI18N
            ClassLoader loaderUsedForAnt = ihClazz.getClassLoader();
            if (loaderUsedForAnt != main) {
                throw new IllegalStateException("Wrong class loader is finding Ant: " + loaderUsedForAnt); // NOI18N
            }
            Class ihClazz2 = Class.forName("org.apache.tools.ant.input.InputHandler", false, main); // NOI18N
            if (ihClazz2 != ihClazz) {
                throw new IllegalStateException("Main and bridge class loaders do not agree on version of Ant: " + ihClazz2.getClassLoader()); // NOI18N
            }
            try {
                Class alClazz = Class.forName("org.apache.tools.ant.taskdefs.Antlib", false, bridgeLoader); // NOI18N
                if (alClazz.getClassLoader() != main) {
                    throw new IllegalStateException("Bridge loader is loading stuff from elsewhere: " + alClazz.getClassLoader()); // NOI18N
                }
                Class alClazz2 = Class.forName("org.apache.tools.ant.taskdefs.Antlib", false, main); // NOI18N
                if (alClazz2 != alClazz) {
                    throw new IllegalStateException("Main and bridge class loaders do not agree on version of Ant: " + alClazz2.getClassLoader()); // NOI18N
                }
            } catch (ClassNotFoundException cnfe) {
                // Fine, it was added in Ant 1.6.
            }
            Class impl = bridgeLoader.loadClass("org.apache.tools.ant.module.bridge.impl.BridgeImpl"); // NOI18N
            if (impl.getClassLoader() != bridgeLoader) {
                throw new IllegalStateException("Wrong class loader is finding bridge impl: " + impl.getClassLoader()); // NOI18N
            }
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
    
    private static String classPathToString(List/*<File>*/ cp) {
        StringBuffer b = new StringBuffer();
        Iterator it = cp.iterator();
        while (it.hasNext()) {
            b.append(((File) it.next()).getAbsolutePath());
            if (it.hasNext()) {
                b.append(File.pathSeparator);
            }
        }
        return b.toString();
    }
    
    private static String originalJavaClassPath = System.getProperty("java.class.path"); // NOI18N
    /**
     * Get the equivalent of java.class.path for the main Ant loader.
     * Includes everything in the main class loader,
     * plus the regular system class path (for tools.jar etc.).
     */
    public static String getMainClassPath() {
        return (String) getStuff().get(KEY_MAIN_CLASS_PATH) + File.pathSeparatorChar + originalJavaClassPath;
    }
    
    private static List/*<File>*/ createMainClassPath() throws Exception {
        // Use LinkedHashSet to automatically suppress duplicates.
        Collection/*<File>*/ cp = new LinkedHashSet();
        File libdir = new File(AntSettings.getDefault().getAntHomeWithDefault(), "lib"); // NOI18N
        if (!libdir.isDirectory()) throw new IOException("No such Ant library dir: " + libdir); // NOI18N
        err.log("Creating main class loader from " + libdir);
        // First look for ${ant.home}/patches/*.jar, to support e.g. patching #47708:
        File[] patches = new File(libdir.getParentFile(), "patches").listFiles(new JarFilter()); // NOI18N
        if (patches != null) {
            for (int i = 0; i < patches.length; i++) {
                cp.add(patches[i]);
            }
        }
        // Now continue with regular classpath.
        File[] libs = libdir.listFiles(new JarFilter());
        if (libs == null) throw new IOException("Listing: " + libdir); // NOI18N
        for (int i = 0; i < libs.length; i++) {
            cp.add(libs[i]);
        }
        // XXX consider adding ${user.home}/.ant/lib/*.jar (org.apache.tools.ant.launch.Launcher.USER_LIBDIR)
        NbClassPath extra = AntSettings.getDefault().getExtraClasspath();
        String extrapath = extra.getClassPath();
        if (extrapath.startsWith("\"") && extrapath.endsWith("\"")) { // NOI18N
            // *@%!* NbClassPath.getClassPath semantics.
            extrapath = extrapath.substring(1, extrapath.length() - 1);
        }
        StringTokenizer tok = new StringTokenizer(extrapath, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            cp.add(new File(tok.nextToken()));
        }
        extra = AntSettings.getDefault().getAutomaticExtraClasspath();
        extrapath = extra.getClassPath();
        if (extrapath.startsWith("\"") && extrapath.endsWith("\"")) { // NOI18N
            extrapath = extrapath.substring(1, extrapath.length() - 1);
        }
        tok = new StringTokenizer(extrapath, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            cp.add(new File(tok.nextToken()));
        }
        // XXX note that systemClassLoader will include boot.jar, and perhaps anything else
        // in lib/ext/*.jar, like rmi-ext.jar. Would be nicer to exclude everything NB-specific.
        // However the simplest way - to use the parent loader (JRE ext loader) - does not work
        // well because Ant assumes that tools.jar is in its classpath (for <javac> etc.).
        // Manually readding the JDK JARs would be possible, but then they would not be shared
        // with the versions used inside NB, which may cause inefficiencies or more memory usage.
        // On the other hand, if ant.jar is in ${java.class.path} (e.g. from a unit test), we
        // have to explicitly mask it out. What a mess...
        return new ArrayList(cp);
    }
    
    private static ClassLoader createMainClassLoader(List/*<File>*/ mainClassPath) throws Exception {
        URL[] cp = new URL[mainClassPath.size()];
        Iterator it = mainClassPath.iterator();
        int i = 0;
        while (it.hasNext()) {
            cp[i++] = ((File) it.next()).toURI().toURL();
        }
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            List/*<URL>*/ parentURLs;
            if (parent instanceof URLClassLoader) {
                parentURLs = Arrays.asList(((URLClassLoader) parent).getURLs());
            } else {
                parentURLs = null;
            }
            err.log("AntBridge.createMainClassLoader: cp=" + Arrays.asList(cp) + " parent.urls=" + parentURLs);
        }
        return new MaskedClassLoader(cp, parent);
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
                throw new IOException("Could not find " + resource + " in ant/nblib/" + cnb.replace('.', '-') + ".jar"); // NOI18N
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
                    err.log(ErrorManager.WARNING, "Warning: unrecognized definition " + def + " in " + antlib);
                    continue;
                }
                String name = def.getAttribute("name"); // NOI18N
                if (name == null) {
                    // Not a hard error since there might be e.g. <taskdef resource="..."/> here
                    // which we do not parse but which is permitted in antlib by Ant.
                    err.log(ErrorManager.WARNING, "Warning: skipping definition " + def + " with no 'name' in " + antlib);
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
                err.annotate(ioe, cnfe);
                throw ioe;
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. tasks dumped there by disabled modules.
                // Cf. #36702 for possible better solution.
                err.log("AntBridge.loadDefs: skipping " + clazzname + ": " + ncdfe);
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                IOException ioe = new IOException("Could not load class " + clazzname + ": " + e); // NOI18N
                err.annotate(ioe, e);
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
        
        public String toString() {
            return super.toString() + "[parent=" + getParent() + ",urls=" + Arrays.asList((Object[])getURLs()) + "]";
        }

        public URL getResource(String name) {
            URL u = super.getResource(name);
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("APURLCL.gR: " + name + " -> " + u + " [" + this + "]");
            }
            return u;
        }
        
        public Enumeration/*<URL>*/ findResources(String name) throws IOException {
            try {
                Enumeration/*<URL>*/ us = super.findResources(name);
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    // Make a copy so it can be logged:
                    List/*<URL>*/ resources = Collections.list(us);
                    us = Collections.enumeration(resources);
                    err.log("APURLCL.fRs: " + name + " -> " + resources + " [" + this + "]");
                }
                return us;
            } catch (IOException e) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
                throw e;
            }
        }

        /*
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
    
    private static boolean masked(String clazz) {
        return clazz.startsWith("org.apache.tools.") || clazz.startsWith("org.netbeans."); // NOI18N
    }

    /**
     * Special class loader that refuses to load Ant or NetBeans classes from its parent.
     * Necessary in order to be able to load the intended Ant distro from a unit test.
     */
    private static final class MaskedClassLoader extends AllPermissionURLClassLoader {
        
        public MaskedClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (masked(name)) {
                Class c = findLoadedClass(name);
                // Careful with that parent loader Eugene!
                if (c == null) {
                    c = findClass(name);
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
        
    }
    
    // I/O redirection impl. Keyed by thread group (each Ant process has its own TG).
    // Various Ant tasks (e.g. <java fork="false" output="..." ...>) need the system
    // I/O streams to be redirected to the demux streams of the project so they can
    // be handled properly. Ideally nothing would try to read directly from stdin
    // or print directly to stdout/stderr but in fact some tasks do.
    // Could also pass a custom InputOutput to ExecutionEngine, perhaps, but this
    // seems a lot simpler and probably has the same effect.

    private static int delegating = 0;
    private static InputStream origIn;
    private static PrintStream origOut, origErr;
    private static Map/*<ThreadGroup,InputStream>*/ delegateIns = new HashMap();
    private static Map/*<ThreadGroup,PrintStream>*/ delegateOuts = new HashMap();
    private static Map/*<ThreadGroup,PrintStream>*/ delegateErrs = new HashMap();
    /** list, not set, so can be reentrant - treated as a multiset */
    private static List/*<Thread>*/ suspendedDelegationTasks = new ArrayList();
    
    /**
     * Handle I/O scoping for overlapping project runs.
     * You must call {@link #restoreSystemInOutErr} in a finally block.
     * @param in new temporary input stream for this thread group
     * @param out new temporary output stream for this thread group
     * @param err new temporary error stream for this thread group
     * @see "#36396"
     */
    public static synchronized void pushSystemInOutErr(InputStream in, PrintStream out, PrintStream err) {
        if (delegating++ == 0) {
            origIn = System.in;
            origOut = System.out;
            origErr = System.err;
            System.setIn(new MultiplexInputStream());
            System.setOut(new MultiplexPrintStream(false));
            System.setErr(new MultiplexPrintStream(true));
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.put(tg, in);
        delegateOuts.put(tg, out);
        delegateErrs.put(tg, err);
    }
    
    /**
     * Restore original I/O streams after a call to {@link #pushSystemInOutErr}.
     */
    public static synchronized void restoreSystemInOutErr() {
        assert delegating > 0;
        if (--delegating == 0) {
            System.setIn(origIn);
            System.setOut(origOut);
            System.setErr(origErr);
            origIn = null;
            origOut = null;
            origErr = null;
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.remove(tg);
        delegateOuts.remove(tg);
        delegateErrs.remove(tg);
    }

    /**
     * Temporarily suspend delegation of system I/O streams for the current thread.
     * Useful when running callbacks to IDE code that might try to print to stderr etc.
     * Must be matched in a finally block by {@link #resumeDelegation}.
     * Safe to call when not actually delegating; in that case does nothing.
     * Safe to call in reentrant but not overlapping fashion.
     */
    public static synchronized void suspendDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // #58394: do *not* check that it does not yet contain t. It is OK if it does; need to
        // be able to call suspendDelegation reentrantly.
        suspendedDelegationTasks.add(t);
    }
    
    /**
     * Resume delegation of system I/O streams for the current thread group
     * after a call to {@link #suspendDelegation}.
     */
    public static synchronized void resumeDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // This is still valid: suspendedDelegationTasks must have *at least one* copy of t.
        assert suspendedDelegationTasks.contains(t) : "Have not suspended delegation in " + t;
        suspendedDelegationTasks.remove(t);
    }
    
    private static final class MultiplexInputStream extends InputStream {
        
        public MultiplexInputStream() {}
        
        private InputStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            while (tg != null && !delegateIns.containsKey(tg)) {
                tg = tg.getParent();
            }
            InputStream is = (InputStream)delegateIns.get(tg);
            if (is != null && !suspendedDelegationTasks.contains(t)) {
                return is;
            } else if (delegating > 0) {
                assert origIn != null;
                return origIn;
            } else {
                // Probably should not happen? But not sure.
                return System.in;
            }
        }
        
        public int read() throws IOException {
            return delegate().read();
        }        
        
        public int read(byte[] b) throws IOException {
            return delegate().read(b);
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate().read(b, off, len);
        }
        
        public int available() throws IOException {
            return delegate().available();
        }
        
        public boolean markSupported() {
            return delegate().markSupported();
        }        
        
        public void mark(int readlimit) {
            delegate().mark(readlimit);
        }
        
        public void close() throws IOException {
            delegate().close();
        }
        
        public long skip(long n) throws IOException {
            return delegate().skip(n);
        }
        
        public void reset() throws IOException {
            delegate().reset();
        }
        
    }
    
    private static final class MultiplexPrintStream extends PrintStream {
        
        private final boolean err;
        
        public MultiplexPrintStream(boolean err) {
            this(new NullOutputStream(), err);
        }
        
        private MultiplexPrintStream(NullOutputStream nos, boolean err) {
            super(nos);
            nos.throwException = true;
            this.err = err;
        }
        
        private PrintStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            Map/*<ThreadGroup,PrintStream>*/ delegates = err ? delegateErrs : delegateOuts;
            while (tg != null && !delegates.containsKey(tg)) {
                tg = tg.getParent();
            }
            PrintStream ps = (PrintStream)delegates.get(tg);
            if (ps != null && !suspendedDelegationTasks.contains(t)) {
                return ps;
            } else if (delegating > 0) {
                PrintStream orig = err ? origErr : origOut;
                assert orig != null;
                return orig;
            } else {
                // Probably should not happen? But not sure.
                return err ? System.err : System.out;
            }
        }
        
        public boolean checkError() {
            return delegate().checkError();
        }
        
        public void close() {
            delegate().close();
        }
        
        public void flush() {
            delegate().flush();
        }
        
        public void print(long l) {
            delegate().print(l);
        }
        
        public void print(char[] s) {
            delegate().print(s);
        }
        
        public void print(int i) {
            delegate().print(i);
        }
        
        public void print(boolean b) {
            delegate().print(b);
        }
        
        public void print(char c) {
            delegate().print(c);
        }
        
        public void print(float f) {
            delegate().print(f);
        }
        
        public void print(double d) {
            delegate().print(d);
        }
        
        public void print(Object obj) {
            delegate().print(obj);
        }
        
        public void print(String s) {
            delegate().print(s);
        }
        
        public void println(double x) {
            delegate().println(x);
        }
        
        public void println(Object x) {
            delegate().println(x);
        }
        
        public void println(float x) {
            delegate().println(x);
        }
        
        public void println(int x) {
            delegate().println(x);
        }
        
        public void println(char x) {
            delegate().println(x);
        }
        
        public void println(boolean x) {
            delegate().println(x);
        }
        
        public void println(String x) {
            delegate().println(x);
        }
        
        public void println(char[] x) {
            delegate().println(x);
        }
        
        public void println() {
            delegate().println();
        }
        
        public void println(long x) {
            delegate().println(x);
        }
        
        public void write(int b) {
            delegate().write(b);
        }
        
        public void write(byte[] b) throws IOException {
            delegate().write(b);
        }
        
        public void write(byte[] b, int off, int len) {
            delegate().write(b, off, len);
        }
        
        // XXX printf/format with varargs cannot be overridden here (JDK 1.5 specific)
        // nor can append(char,CharSequence)
        // probably does not matter however...
        
    }
    
    // Faking the system property java.class.path for the benefit of a few tasks
    // that expect it to be equal to the Ant class loader path.
    
    private static int fakingJavaClassPath = 0;
    
    /**
     * Fake the system property java.class.path temporarily.
     * Must be followed by {@link unfakeJavaClassPath} in a finally block.
     * Reentrant.
     */
    public static synchronized void fakeJavaClassPath() {
        if (fakingJavaClassPath++ == 0) {
            String cp = getMainClassPath();
            err.log("Faking java.class.path=" + cp);
            System.setProperty("java.class.path", cp); // NOI18N
        }
    }
    
    /**
     * Reverse the effect of {@link fakeJavaClassPath}.
     */
    public static synchronized void unfakeJavaClassPath() {
        if (--fakingJavaClassPath == 0) {
            err.log("Restoring java.class.path=" + originalJavaClassPath);
            System.setProperty("java.class.path", originalJavaClassPath); // NOI18N
        }
    }

}
