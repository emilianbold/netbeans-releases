/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.api;

import java.io.*;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map

import org.apache.tools.ant.*;

import org.openide.*;

import org.apache.tools.ant.module.AntModule;

/** Represents Ant-style introspection info for a set of classes.
 * There should be one instance which is loaded automatically
 * from defaults.properties files, i.e. standard tasks/datatypes.
 * A second is loaded from settings and represents custom tasks/datatypes.
 * Uses Ant's IntrospectionHelper for the actual work, but manages the results
 * and makes them safely serializable (stores only classnames, etc.).
 */
public final class IntrospectedInfo implements Serializable {
    
    private static IntrospectedInfo defaults = null;
    private static boolean loadedDefaults = false;
    
    /** Are the default definitions loaded yet? */
    static synchronized boolean isDefaultsPrepared () {
        return loadedDefaults;
    }
    
    /** Get default definitions specified by Ant's defaults.properties.
     * @return the singleton defaults
     */
    public static synchronized IntrospectedInfo getDefaults () {
            if (defaults != null) return defaults;
            AntModule.err.log ("IntrospectedInfo.getDefaults: loading...");
            defaults = new IntrospectedInfo ();
            // Not necessary and causes problems during test mode:
            //ClassLoader cl = TopManager.getDefault ().currentClassLoader ();
            ClassLoader cl = IntrospectedInfo.class.getClassLoader ();
            InputStream taskDefaults = cl.getResourceAsStream ("org/apache/tools/ant/taskdefs/defaults.properties");
            if (taskDefaults != null) {
                try {
                    defaults.load (taskDefaults, true, cl);
                } catch (IOException ioe) {
                    AntModule.err.log ("Could not load default taskdefs");
                    AntModule.err.notify (ioe);
                }
            } else {
                AntModule.err.log ("Could not open default taskdefs");
            }
            InputStream typeDefaults = cl.getResourceAsStream ("org/apache/tools/ant/types/defaults.properties");
            if (typeDefaults != null) {
                try {
                    defaults.load (typeDefaults, false, cl);
                } catch (IOException ioe) {
                    AntModule.err.log ("Could not load default typedefs");
                    AntModule.err.notify (ioe);
                }
            } else {
                AntModule.err.log ("Could not open default typedefs");
            }
            Iterator kit = DefinitionRegistry.getKinds().iterator();
            while (kit.hasNext()) {
                String kind = (String)kit.next();
                Map m = DefinitionRegistry.getDefs (kind);
                if (m.size () > 0) {
                    AntModule.err.log ("Introspecting " + m.size () + " ad-hoc defs (kind=" + kind + ")...");
                    Iterator it = m.entrySet ().iterator ();
                    while (it.hasNext ()) {
                        Map.Entry entry = (Map.Entry) it.next ();
                        String name = (String) entry.getKey ();
                        Class def = (Class) entry.getValue ();
                        defaults.register (name, def, kind);
                    }
                }
            }
            if (AntModule.err.isLoggable(ErrorManager.UNKNOWN)) {
                AntModule.err.log ("IntrospectedInfo.defaults=" + defaults);
            }
            loadedDefaults = true;
            return defaults;
    }
    
    private static final long serialVersionUID = -2290064038236292995L;
    
    private Map clazzes = Collections.synchronizedMap (new HashMap ()); // Map<String,IntrospectedClass>
    /** definitions first by kind then by name to class name */
    private Map namedefs = new HashMap(); // Map<String,Map<String,String>>
    
    /** Make new empty set of info.
     */
    public IntrospectedInfo () {
    }
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        if (namedefs == null) {
            // Compatibility with older versions of this class.
            namedefs = new HashMap();
            ObjectInputStream.GetField fields = is.readFields();
            namedefs.put("task", fields.get("tasks", new HashMap())); // NOI18N
            namedefs.put("type", fields.get("types", new HashMap())); // NOI18N
        }
    }
    
    /** Get task definitions.
     * @return an immutable map from task names to class names
     * @deprecated since 2.4, look up by kind instead
     */
    public Map getTaskdefs () {
        return getDefs("task"); // NOI18N
    }
    
    /** Get type definitions.
     * @return an immutable map from type names to class names
     * @deprecated since 2.4, look up by kind instead
     */
    public Map getTypedefs () {
        return getDefs("type"); // NOI18N
    }
    
    /** Get definitions.
     * @param the kind of definition, e.g. <code>task</code>
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
            return /*1.3: Collections.EMPTY_MAP*/ new HashMap (1);
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
            return /*1.3: Collections.EMPTY_MAP*/ new HashMap (1);
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /** Load defs from a properties file. */
    private void load (InputStream is, boolean tasks, ClassLoader cl) throws IOException {
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
            String clazzname = (String) entry.getValue ();
            try {
                Class clazz = cl.loadClass (clazzname);
                register (name, clazz, tasks);
            } catch (ClassNotFoundException cnfe) {
                // This is normal, e.g. Ant's taskdefs include optional tasks we don't have.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. optional tasks which we cannot resolve against.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + ncdfe);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // Not normal; if it is there it ought to be resolvable etc.
                IOException ioe = new IOException ("Could not load class " + clazzname); // NOI18N
                AntModule.err.annotate (ioe, t);
                throw ioe;
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
     * @param task true for a task, false for a data type
     * @throws various errors if the class could not be resolved, e.g. NoClassDefFoundError
     * @deprecated since 2.4, should register by kind instead
     */
    public synchronized void register (String name, Class clazz, boolean task) {
        register(name, clazz, task ? "task" : "type"); // NOI18N
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
        if (isKnown (clazz.getName ()) || getDefaults ().isKnown (clazz.getName ())) {
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
        IntrospectionHelper helper = IntrospectionHelper.getHelper (clazz);
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
                    if (Task.class.isAssignableFrom (clazz) &&
                        ((name.equals ("location") && type.equals ("org.apache.tools.ant.Location")) || // NOI18N
                         (name.equals ("taskname") && type.equals ("java.lang.String")) || // NOI18N
                         (name.equals ("description") && type.equals ("java.lang.String")))) { // NOI18N
                        // IntrospectionHelper is supposed to exclude such things, but I guess not.
                        // "description" may be OK to actually show on nodes, but since it is common // NOI18N
                        // to all tasks it should not be stored as such.
                        continue;
                    }
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
    }
    
    /* Scan an existing (already-run) project to see if it has any new tasks/types.
     * Any new definitions found will automatically be added to the known list.
     * Currently this will <em>not</em> try to change existing definitions, i.e.
     * if a task is defined to be implemented with a different class, or if a
     * class changes structure.
     * Will not try to define anything contained in the defaults list.
     * @param p project to scan
     */
    public void scanProject (Project p) {
        scanMap (p.getTaskDefinitions (), "task"); // NOI18N
        scanMap (p.getDataTypeDefinitions (), "type"); // NOI18N
        AntModule.err.log ("IntrospectedInfo.scanProject: p=" + p.getName () + "; this=" + this);
    }
    
    private void scanMap (Map/*<String,Class>*/ m, String kind) {
        if (kind == null) throw new IllegalArgumentException();
        Iterator it = m.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry) it.next ();
            String name = (String) entry.getKey ();
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
                    } catch (Throwable t) {
                        // Not so normal.
                        AntModule.err.annotate (t, ErrorManager.INFORMATIONAL, "Cannot scan class " + clazz.getName (), null, null, null); // NOI18N
                        AntModule.err.notify (ErrorManager.INFORMATIONAL, t);
                    }
                }
            }
        }
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
    
}
