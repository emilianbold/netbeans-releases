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
    
    /** Are the default definitions loaded yet? */
    static boolean isDefaultsPrepared () {
        return defaults != null;
    }
    
    /** Get default definitions specified by Ant's defaults.properties.
     * @return the singleton defaults
     */
    public static IntrospectedInfo getDefaults () {
        if (defaults != null) return defaults;
        synchronized (IntrospectedInfo.class) {
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
            for (int i = 0; i < 2; i++) {
                boolean task = (i == 0);
                Map m = DefinitionRegistry.getDefs (task);
                if (m.size () > 0) {
                    AntModule.err.log ("Introspecting " + m.size () + " ad-hoc defs (task=" + task + ")...");
                    Iterator it = m.entrySet ().iterator ();
                    while (it.hasNext ()) {
                        Map.Entry entry = (Map.Entry) it.next ();
                        String name = (String) entry.getKey ();
                        Class def = (Class) entry.getValue ();
                        defaults.register (name, def, task);
                    }
                }
            }
            // [PENDING] ErrorManager.isLoggable > NB3.1
            AntModule.err.log ("IntrospectedInfo.defaults=" + defaults);
            return defaults;
        }
    }
    
    private static final long serialVersionUID = -2290064038236292995L;
    
    private Map clazzes = Collections.synchronizedMap (new HashMap ()); // Map<String,IntrospectedClass>
    private Map tasks = new HashMap (); // Map<String,String>
    private Map types = new HashMap (); // Map<String,String>
    
    /** Make new empty set of info.
     */
    public IntrospectedInfo () {
    }
    
    /** Get task definitions.
     * @return an immutable map from task names to class names
     */
    public Map getTaskdefs () {
        return Collections.unmodifiableMap (tasks);
    }
    
    /** Get type definitions.
     * @return an immutable map from type names to class names
     */
    public Map getTypedefs () {
        return Collections.unmodifiableMap (types);
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
     * May change the defined task/type for a given name, but currently
     * will not redefine structure if classes are modified.
     * Also any class definitions contained in the default map (if not this one)
     * are just ignored; you should refer to the default map for info on them.
     * @param name name of the task or type as it appears in scripts
     * @param clazz the implementing class
     * @param task true for a task, false for a data type
     */
    public synchronized void register (String name, Class clazz, boolean task) {
        (task ? tasks : types).put (name, clazz.getName ());
        analyze (clazz);
    }
    
    private void analyze (Class clazz) {
        if (isKnown (clazz.getName ()) || getDefaults ().isKnown (clazz.getName ())) {
            // Currently will not try to redefine anything.
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
                } catch (BuildException be) {
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, be);
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
                } catch (BuildException be) {
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, be);
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
        scanMap (p.getTaskDefinitions (), true);
        scanMap (p.getDataTypeDefinitions (), false);
        AntModule.err.log ("IntrospectedInfo.scanProject: p=" + p.getName () + "; this=" + this);
    }
    
    private void scanMap (Map/*<String,Class>*/ m, boolean task) {
        Iterator it = m.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry) it.next ();
            String name = (String) entry.getKey ();
            Class clazz = (Class) entry.getValue ();
            Map registry = task ? tasks : types; // Map<String,String>
            synchronized (this) {
                Map defaults = task ? getDefaults ().getTaskdefs () : getDefaults ().getTypedefs (); // Map<String,String>
                if (registry.get (name) == null && defaults.get (name) == null) {
                    registry.put (name, clazz.getName ());
                }
                if (! getDefaults ().isKnown (clazz.getName ())) {
                    analyze (clazz);
                }
            }
        }
    }
    
    public String toString () {
        return "IntrospectedInfo[tasks=" + tasks + ",types=" + types + ",clazzes=" + clazzes + "]"; // NOI18N
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
