/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2002.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.api;

import java.io.*;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.*;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.bridge.*;

/** Represents Ant-style introspection info for a set of classes.
 * There should be one instance which is loaded automatically
 * from defaults.properties files, i.e. standard tasks/datatypes.
 * A second is loaded from settings and represents custom tasks/datatypes.
 * Uses Ant's IntrospectionHelper for the actual work, but manages the results
 * and makes them safely serializable (stores only classnames, etc.).
 */
public final class IntrospectedInfo implements Serializable {
    
    private static IntrospectedInfo defaults = null;
    
    /** Get default definitions specified by Ant's defaults.properties.
     * @return the singleton defaults
     */
    public static synchronized IntrospectedInfo getDefaults() {
        if (defaults != null) return defaults;
        AntModule.err.log("IntrospectedInfo.getDefaults: loading...");
        defaults = new IntrospectedInfo();
        defaults.loadDefaults(true);
        return defaults;
    }
    
    private static final long serialVersionUID = -2290064038236292995L;
    
    private Map clazzes = Collections.synchronizedMap (new HashMap ()); // Map<String,IntrospectedClass>
    /** definitions first by kind then by name to class name */
    private Map namedefs = new HashMap(); // Map<String,Map<String,String>>
    
    private transient Set listeners = new HashSet(5); // Set<ChangeListener>
    private transient Set tonotify = new HashSet(5); // Set<ChangeListener>
    
    private transient ChangeListener antBridgeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
            clearDefs();
            loadDefaults(false);
            fireStateChanged();
        }
    };
    
    /** Make new empty set of info.
     */
    public IntrospectedInfo () {
    }
    
    private void clearDefs() {
        clazzes.clear();
        namedefs.clear();
    }
    
    private void loadDefaults(boolean listen) {
        ClassLoader cl = AntBridge.getMainClassLoader();
        InputStream taskDefaults = cl.getResourceAsStream("org/apache/tools/ant/taskdefs/defaults.properties");
        if (taskDefaults != null) {
            try {
                defaults.load(taskDefaults, "task", cl); // NOI18N
            } catch (IOException ioe) {
                AntModule.err.log("Could not load default taskdefs");
                AntModule.err.notify(ioe);
            }
        } else {
            AntModule.err.log("Could not open default taskdefs");
        }
        InputStream typeDefaults = cl.getResourceAsStream("org/apache/tools/ant/types/defaults.properties");
        if (typeDefaults != null) {
            try {
                defaults.load(typeDefaults, "type", cl); // NOI18N
            } catch (IOException ioe) {
                AntModule.err.log("Could not load default typedefs");
                AntModule.err.notify(ioe);
            }
        } else {
            AntModule.err.log("Could not open default typedefs");
        }
        defaults.loadNetBeansSpecificDefinitions();
        if (listen) {
            AntBridge.addChangeListener(WeakListener.change(antBridgeListener, AntBridge.class));
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.defaults=" + defaults);
        }
    }
    
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        listeners = new HashSet(5);
        tonotify = new HashSet(5);
        //is.defaultReadObject();
        ObjectInputStream.GetField fields = is.readFields();
        clazzes = (Map)fields.get("clazzes", null); // NOI18N
        namedefs = (Map)fields.get("namedefs", null); // NOI18n
        if (namedefs == null) {
            // Compatibility with older versions of this class.
            AntModule.err.log("#15739: reading old version of IntrospectedInfo");
            namedefs = new HashMap();
            Object tasks_ = fields.get("tasks", null); // NOI18N
            if (tasks_ == null) throw new NullPointerException();
            if (! (tasks_ instanceof Map)) throw new ClassCastException(tasks_.toString());
            namedefs.put("task", tasks_); // NOI18N
            Map types = (Map)fields.get("types", null); // NOI18N
            if (types == null) throw new NullPointerException();
            namedefs.put("type", types); // NOI18N
        }
        // #15739 sanity check:
        Iterator it = namedefs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            Map value = (Map)entry.getValue();
            Iterator it2 = value.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry entry2 = (Map.Entry)it2.next();
                String key2 = (String)entry2.getKey();
                String value2 = (String)entry2.getValue();
                // that's all, just checking for ClassCastException's
            }
        }
        Iterator it2 = clazzes.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry entry2 = (Map.Entry)it2.next();
            String key2 = (String)entry2.getKey();
            IntrospectedClass value2 = (IntrospectedClass)entry2.getValue();
            // again
        }
    }
    
    /** Add a listener to changes in the definition set.
     * @param l the listener to add
     * @since 2.6
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /** Remove a listener to changes in the definition set.
     * @param l the listener to remove
     * @since 2.6
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private class ChangeTask implements Runnable {
        public void run() {
            ChangeListener[] listeners2;
            synchronized (listeners) {
                if (tonotify.isEmpty()) return;
                listeners2 = (ChangeListener[])tonotify.toArray(new ChangeListener[tonotify.size()]);
                tonotify.clear();
            }
            ChangeEvent ev = new ChangeEvent(IntrospectedInfo.this);
            for (int i = 0; i < listeners2.length; i++) {
                listeners2[i].stateChanged(ev);
            }
        }
    }
    private void fireStateChanged() {
        synchronized (listeners) {
            if (listeners.isEmpty()) return;
            if (tonotify.isEmpty()) {
                RequestProcessor.getDefault().post(new ChangeTask());
            }
            tonotify.addAll(listeners);
        }
    }
    
    /** Get definitions.
     * @param kind the kind of definition, e.g. <code>task</code>
     * @return an immutable map from definition names to class names
     */
    public Map getDefs(String kind) {
        synchronized (namedefs) {
            Map m = (Map)namedefs.get(kind);
            if (m != null) {
                return Collections.unmodifiableMap(m);
            } else {
                return Collections.EMPTY_MAP;
            }
        }
    }
    
    private IntrospectedClass getData (String clazz) throws IllegalArgumentException {
        IntrospectedClass data = (IntrospectedClass) clazzes.get (clazz);
        if (data == null) throw new IllegalArgumentException ();
        return data;
    }
    
    /** Is anything known about this class?
     * @param clazz the class name
     * @return true if it is known, false if never encountered
     */
    public boolean isKnown (String clazz) {
        return clazzes.get (clazz) != null;
    }
    
    /** Does this class support inserting text data?
     * @param clazz the class name
     * @return true if so
     * @throws IllegalArgumentException if the class is unknown
     */
    public boolean supportsText (String clazz) throws IllegalArgumentException {
        return getData (clazz).supportsText;
    }
    
    /** Get all attributes supported by this class.
     * @param clazz the class name
     * @return an immutable map from attribute name to type (class name)
     * @throws IllegalArgumentException if the class is unknown
     */
    public Map getAttributes (String clazz) throws IllegalArgumentException {
        Map map = getData (clazz).attrs;
        if (map == null) {
            return Collections.EMPTY_MAP;
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /** Get all subelements supported by this class.
     * @param clazz the class name
     * @return an immutable map from element name to type (class name)
     * @throws IllegalArgumentException if the class is unknown
     */
    public Map getElements (String clazz) throws IllegalArgumentException {
        Map map = getData (clazz).subs;
        if (map == null) {
            return Collections.EMPTY_MAP;
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /** Load defs from a properties file. */
    private void load (InputStream is, String kind, ClassLoader cl) throws IOException {
        Properties p = new Properties ();
        try {
            p.load (is);
        } finally {
            is.close ();
        }
        Iterator it = p.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry) it.next ();
            String name = (String) entry.getKey ();
            if (kind.equals("type") && name.equals("description")) { // NOI18N
                // Not a real data type; handled specially.
                AntModule.err.log("Skipping pseudodef of <description>");
                continue;
            }
            String clazzname = (String) entry.getValue ();
            try {
                Class clazz = cl.loadClass (clazzname);
                register(name, clazz, kind);
            } catch (ClassNotFoundException cnfe) {
                // This is normal, e.g. Ant's taskdefs include optional tasks we don't have.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. optional tasks which we cannot resolve against.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + ncdfe);
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                IOException ioe = new IOException ("Could not load class " + clazzname + ": " + e); // NOI18N
                AntModule.err.annotate (ioe, e);
                throw ioe;
            } catch (RuntimeException e) {
                // SecurityException etc. Not normal.
                IOException ioe = new IOException ("Could not load class " + clazzname + ": " + e); // NOI18N
                AntModule.err.annotate (ioe, e);
                throw ioe;
            }
        }
    }
    
    private void loadNetBeansSpecificDefinitions() {
        Map defsByKind = AntBridge.getCustomDefs();
        Iterator kindIt = defsByKind.entrySet().iterator();
        while (kindIt.hasNext()) {
            Map.Entry kindE = (Map.Entry)kindIt.next();
            String kind = (String)kindE.getKey();
            Map defs = (Map)kindE.getValue();
            Iterator defsIt = defs.entrySet().iterator();
            while (defsIt.hasNext()) {
                Map.Entry defsE = (Map.Entry)defsIt.next();
                String name = (String)defsE.getKey();
                Class clazz = (Class)defsE.getValue();
                register(name, clazz, kind);
            }
        }
    }
    
    /** Register a new definition.
     * May change the defined task/type for a given name, but
     * will not redefine structure if classes are modified.
     * Also any class definitions contained in the default map (if not this one)
     * are just ignored; you should refer to the default map for info on them.
     * @param name name of the task or type as it appears in scripts
     * @param clazz the implementing class
     * @param kind the kind of definition to register (<code>task</code> or <code>type</code> currently)
     * @throws various errors if the class could not be resolved, e.g. NoClassDefFoundError
     * @since 2.4
     */
    public synchronized void register(String name, Class clazz, String kind) {
        synchronized (namedefs) {
            Map m = (Map)namedefs.get(kind);
            if (m == null) {
                m = new HashMap(); // Map<String,String>
                namedefs.put(kind, m);
            }
            m.put(name, clazz.getName());
        }
        fireStateChanged();
        analyze (clazz);
    }
    /** Unregister a definition.
     * Removes it from the definition mapping, though structural
     * information about the implementing class (and classes referenced
     * by that class) will not be removed.
     * If the definition was not registered before, does nothing.
     * @param name the definition name
     * @param kind the kind of definition (<code>task</code> etc.)
     * @since 2.4
     */
    public synchronized void unregister(String name, String kind) {
        synchronized (namedefs) {
            Map m = (Map)namedefs.get(kind);
            if (m != null) {
                m.remove(name);
            }
        }
        fireStateChanged();
    }
    
    /** Reanalyze a class' structure.
     * Information about this class' structure (nested elements etc.) will
     * be reexamined, which may produce different results if this is a different
     * version of the same class than was last introspected. All referenced classes
     * which are defined in this <code>IntrospectedInfo</code> (but not traversing
     * into the default one if different from this one) will also be reanalyzed.
     * @param clazz the root class to analyze again
     * @throws various errors if the class could not be resolved, e.g. NoClassDefFoundError
     * @since ???
     * /
    public synchronized void reanalyze(Class clazz) {
        // XXX
    }*/
    
    private void analyze (Class clazz) {
        String n = clazz.getName();
        if (getDefaults().isKnown(n) || /* #23630 */isKnown(n)) {
            // Will not try to redefine anything.
            return;
        }
        //AntModule.err.log ("IntrospectedInfo.analyze: clazz=" + clazz.getName ());
        //boolean dbg = (clazz == org.apache.tools.ant.taskdefs.Taskdef.class);
        //if (! dbg && clazz.getName ().equals ("org.apache.tools.ant.taskdefs.Taskdef")) { // NOI18N
        //    AntModule.err.log ("Classloader mismatch: cl1=" + clazz.getClassLoader () + " cl2=" + org.apache.tools.ant.taskdefs.Taskdef.class.getClassLoader ());
        //}
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        IntrospectedClass info = new IntrospectedClass ();
        IntrospectionHelperProxy helper = AntBridge.getInterface().getIntrospectionHelper(clazz);
        info.supportsText = helper.supportsCharacters ();
        Enumeration e = helper.getAttributes ();
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        if (e.hasMoreElements ()) {
            info.attrs = new HashMap ();
            while (e.hasMoreElements ()) {
                String name = (String) e.nextElement ();
                //if (dbg) AntModule.err.log ("\tname=" + name);
                try {
                    String type = helper.getAttributeType (name).getName ();
                    //if (dbg) AntModule.err.log ("\ttype=" + type);
                    if (hasSuperclass(clazz, "org.apache.tools.ant.Task") && // NOI18N
                        ((name.equals ("location") && type.equals ("org.apache.tools.ant.Location")) || // NOI18N
                         (name.equals ("taskname") && type.equals ("java.lang.String")) || // NOI18N
                         (name.equals ("description") && type.equals ("java.lang.String")))) { // NOI18N
                        // IntrospectionHelper is supposed to exclude such things, but I guess not.
                        // Or it excludes location & taskType.
                        // description may be OK to actually show on nodes, but since it is common
                        // to all tasks it should not be stored as such. Ditto taskname.
                        continue;
                    }
                    // XXX also handle subclasses of DataType and its standard attrs
                    // incl. creating nicely-named node props for description, refid, etc.
                    info.attrs.put (name, type);
                } catch (RuntimeException re) { // i.e. BuildException; but avoid loading this class
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, re);
                }
            }
        } else {
            info.attrs = null;
        }
        Set nueClazzes = new HashSet (); // Set<Class>
        e = helper.getNestedElements ();
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> subels...");
        if (e.hasMoreElements ()) {
            info.subs = new HashMap ();
            while (e.hasMoreElements ()) {
                String name = (String) e.nextElement ();
                //if (dbg) AntModule.err.log ("\tname=" + name);
                try {
                    Class subclazz = helper.getElementType (name);
                    //if (dbg) AntModule.err.log ("\ttype=" + subclazz.getName ());
                    info.subs.put (name, subclazz.getName ());
                    nueClazzes.add (subclazz);
                } catch (RuntimeException re) { // i.e. BuildException; but avoid loading this class
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, re);
                }
            }
        } else {
            info.subs = null;
        }
        clazzes.put (clazz.getName (), info);
        // And recursively analyze reachable classes for subelements...
        // (usually these will already be known, and analyze will return at once)
        Iterator it = nueClazzes.iterator ();
        while (it.hasNext ()) {
            analyze ((Class) it.next ());
        }
        fireStateChanged();
    }
    
    private static boolean hasSuperclass(Class subclass, String superclass) {
        for (Class c = subclass; c != null; c = c.getSuperclass()) {
            if (c.getName().equals(superclass)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Scan an existing (already-run) project to see if it has any new tasks/types.
     * Any new definitions found will automatically be added to the known list.
     * Currently this will <em>not</em> try to change existing definitions, i.e.
     * if a task is defined to be implemented with a different class, or if a
     * class changes structure.
     * Will not try to define anything contained in the defaults list.
     * @param defs map from kinds to maps from names to classes
     */
    public void scanProject (Map defs) {
        Iterator it = defs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            scanMap((Map)e.getValue(), (String)e.getKey());
        }
        AntModule.err.log ("IntrospectedInfo.scanProject: " + this);
    }
    
    private void scanMap (Map/*<String,Class>*/ m, String kind) {
        if (kind == null) throw new IllegalArgumentException();
        Iterator it = m.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry) it.next ();
            String name = (String) entry.getKey ();
            if (kind.equals("type") && name.equals("description")) { // NOI18N
                // Not a real data type; handled specially.
                AntModule.err.log("Skipping pseudodef of <description>");
                continue;
            }
            Class clazz = (Class) entry.getValue ();
            Map registry = (Map)namedefs.get(kind); // Map<String,String>
            if (registry == null) {
                registry = new HashMap();
                namedefs.put(kind, registry);
            }
            synchronized (this) {
                Map defaults = getDefaults ().getDefs (kind); // Map<String,String>
                if (registry.get (name) == null && defaults.get (name) == null) {
                    registry.put (name, clazz.getName ());
                }
                if (! getDefaults ().isKnown (clazz.getName ())) {
                    try {
                        analyze (clazz);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (NoClassDefFoundError ncdfe) {
                        // Reasonably normal.
                        AntModule.err.log ("Skipping " + clazz.getName () + ": " + ncdfe);
                    } catch (LinkageError e) {
                        // Not so normal.
                        AntModule.err.annotate (e, ErrorManager.INFORMATIONAL, "Cannot scan class " + clazz.getName (), null, null, null); // NOI18N
                        AntModule.err.notify (ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        fireStateChanged();
    }
    
    public String toString () {
        return "IntrospectedInfo[namedefs=" + namedefs + ",clazzes=" + clazzes + "]"; // NOI18N
    }
    
    private static final class IntrospectedClass implements Serializable {
        
        private static final long serialVersionUID = 4039297397834774403L;
        
        //public String clazz;
        public boolean supportsText;
        public Map attrs; // null or name -> class; Map<String,String>
        public Map subs; // null or name -> class; Map<String,String>
        
        public String toString () {
            return "IntrospectedClass[text=" + supportsText + ",attrs=" + attrs + ",subs=" + subs + "]"; // NOI18N
        }
        
    }
    
    // merging and including custom defs:
    
    /** only used to permit use of WeakListener */
    private transient ChangeListener holder;
    
    /**
     * Merge several IntrospectedInfo instances together.
     * Responds live to updates.
     */
    private static IntrospectedInfo merge(IntrospectedInfo[] proxied) {
        final IntrospectedInfo ii = new IntrospectedInfo();
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                IntrospectedInfo ii2 = (IntrospectedInfo)ev.getSource();
                ii.clazzes.putAll(ii2.clazzes);
                Iterator it = ii2.namedefs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    String kind = (String)e.getKey();
                    Map entries = (Map)e.getValue();
                    if (ii.namedefs.containsKey(kind)) {
                        ((Map)ii.namedefs.get(kind)).putAll(entries);
                    } else {
                        ii.namedefs.put(kind, new HashMap(entries));
                    }
                }
                ii.fireStateChanged();
            }
        };
        ii.holder = l;
        for (int i = 0; i < proxied.length; i++) {
            proxied[i].addChangeListener(WeakListener.change(l, proxied[i]));
            l.stateChanged(new ChangeEvent(proxied[i]));
        }
        return ii;
    }
    
    /** defaults + custom defs */
    private static IntrospectedInfo merged;
    
    /**
     * Get all known introspected definitions.
     * Includes all those in {@link #getDefaults} plus custom definitions
     * encountered in actual build scripts (details unspecified).
     * @return a set of all known definitions, e.g. of tasks and types
     * @since 2.14
     */
    public static synchronized IntrospectedInfo getKnownInfo() {
        if (merged == null) {
            merged = merge(new IntrospectedInfo[] {
                getDefaults(),
                AntSettings.getDefault().getCustomDefs(),
            });
        }
        return merged;
    }
    
}
