/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

// XXX in order to support Ant 1.6 interface addition types, need to keep
// track of which classes implement a given interface

/** Represents Ant-style introspection info for a set of classes.
 * There should be one instance which is loaded automatically
 * from defaults.properties files, i.e. standard tasks/datatypes.
 * A second is loaded from settings and represents custom tasks/datatypes.
 * Uses Ant's IntrospectionHelper for the actual work, but manages the results
 * and makes them safely serializable (stores only classnames, etc.).
 * <p>
 * All task and type names may be namespace-qualified for use
 * in Ant 1.6: a name of the form <samp>nsuri:localname</samp> refers to
 * an XML element with namespace <samp>nsuri</samp> and local name <samp>localname</samp>.
 * Attribute names could also be similarly qualified, but in practice attributes
 * used in Ant never have a defined namespace. The prefix <samp>antlib:org.apache.tools.ant:</samp>
 * is implied, not expressed, on Ant core element names (for backwards compatibility).
 * Subelement names are *not* namespace-qualified here, even though in the script
 * they would be - because the namespace used in the script will actually vary
 * according to how an antlib is imported and used. An unqualified subelement name
 * should be understood to inherit a namespace from its parent element.
 * <em>(Namespace support since <code>org.apache.tools.ant.module/3 3.6</code>)</em>
 */
public final class IntrospectedInfo implements Serializable {
    
    private static IntrospectedInfo defaults = null;
    private static boolean defaultsInited = false;
    private static boolean defaultsEverInited = false;
    
    /** Get default definitions specified by Ant's defaults.properties.
     * @return the singleton defaults
     */
    public static synchronized IntrospectedInfo getDefaults() {
        if (defaults == null) {
            defaults = new IntrospectedInfo();
        }
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
            fireStateChanged();
        }
    };
    
    /** Make new empty set of info.
     */
    public IntrospectedInfo () {
    }
    
    private void init() {
        synchronized (IntrospectedInfo.class) {
            if (!defaultsInited && this == defaults) {
                AntModule.err.log("IntrospectedInfo.getDefaults: loading...");
                defaultsInited = true;
                loadDefaults(!defaultsEverInited);
                defaultsEverInited = true;
            }
        }
    }
    
    private void clearDefs() {
        clazzes.clear();
        namedefs.clear();
        defaultsInited = false;
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
            AntBridge.addChangeListener(WeakListeners.change(antBridgeListener, AntBridge.class));
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
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.fireStateChanged");
        }
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
        init();
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
        if (data == null) {
            throw new IllegalArgumentException("Unknown class: " + clazz); // NOI18N
        }
        return data;
    }
    
    /** Is anything known about this class?
     * @param clazz the class name
     * @return true if it is known, false if never encountered
     */
    public boolean isKnown (String clazz) {
        init();
        return clazzes.get (clazz) != null;
    }
    
    /** Does this class support inserting text data?
     * @param clazz the class name
     * @return true if so
     * @throws IllegalArgumentException if the class is unknown
     */
    public boolean supportsText (String clazz) throws IllegalArgumentException {
        init();
        return getData (clazz).supportsText;
    }
    
    /** Get all attributes supported by this class.
     * @param clazz the class name
     * @return an immutable map from attribute name to type (class name)
     * @throws IllegalArgumentException if the class is unknown
     */
    public Map getAttributes (String clazz) throws IllegalArgumentException {
        init();
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
        init();
        Map map = getData (clazz).subs;
        if (map == null) {
            return Collections.EMPTY_MAP;
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /**
     * Get tags represented by this class if it is an <code>EnumeratedAttribute</code>.
     * @param clazz the class name
     * @return a list of tag names, or null if the class is not a subclass of <code>EnumeratedAttribute</code>
     * @throws IllegalArgumentException if the class is unknown
     * @since org.apache.tools.ant.module/3 3.3
     */
    public String[] getTags(String clazz) throws IllegalArgumentException {
        init();
        return getData(clazz).enumTags;
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
                register(name, clazz, kind, false);
            } catch (ClassNotFoundException cnfe) {
                // This is normal, e.g. Ant's taskdefs include optional tasks we don't have.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. optional tasks which we cannot resolve against.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + ncdfe);
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + e).initCause(e); // NOI18N
            } catch (RuntimeException e) {
                // SecurityException etc. Not normal.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + e).initCause(e); // NOI18N
            }
        }
    }
    
    private void loadNetBeansSpecificDefinitions() {
        loadNetBeansSpecificDefinitions0(AntBridge.getCustomDefsNoNamespace());
        if (AntBridge.getInterface().isAnt16()) {
            // Define both.
            loadNetBeansSpecificDefinitions0(AntBridge.getCustomDefsWithNamespace());
        }
    }
    
    private void loadNetBeansSpecificDefinitions0(Map defsByKind) {
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
     * Throws various errors if the class could not be resolved, e.g. NoClassDefFoundError.
     * @param name name of the task or type as it appears in scripts
     * @param clazz the implementing class
     * @param kind the kind of definition to register (<code>task</code> or <code>type</code> currently)
     * @since 2.4
     */
    public synchronized void register(String name, Class clazz, String kind) {
        register(name, clazz, kind, true);
    }
    
    private void register(String name, Class clazz, String kind, boolean fire) {
        init();
        synchronized (namedefs) {
            Map m = (Map)namedefs.get(kind);
            if (m == null) {
                m = new HashMap(); // Map<String,String>
                namedefs.put(kind, m);
            }
            m.put(name, clazz.getName());
        }
        boolean changed = analyze(clazz, null, false);
        if (changed && fire) {
            fireStateChanged();
        }
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
        init();
        synchronized (namedefs) {
            Map m = (Map)namedefs.get(kind);
            if (m != null) {
                m.remove(name);
            }
        }
        fireStateChanged();
    }
    
    /**
     * Analyze a particular class and other classes recursively.
     * Will never try to redefine anything in the default IntrospectedInfo.
     * For custom IntrospectedInfo's, will never try to redefine anything
     * if skipReanalysis is null. If not null, will not redefine anything
     * in that set - so start recursion by passing an empty set, if you wish
     * to redefine anything you come across recursively that is not in the
     * default IntrospectedInfo, without causing loops.
     * Attribute classes are examined just in case they are EnumeratedAttribute
     * subclasses; they are not checked for subelements etc.
     * Does not itself fire changes - you should do this if the return value is true.
     * @param clazz the class to look at
     * @param skipReanalysis null to do not redefs, or a set of already redef'd classes
     * @param isAttrType false for an element class, true for an attribute class
     * @return true if something changed
     */
    private boolean analyze(Class clazz, Set/*<Class>*/ skipReanalysis, boolean isAttrType) {
        String n = clazz.getName();
        /*
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.analyze: " + n + " skipping=" + skipReanalysis + " attrType=" + isAttrType);
        }
         */
        if (getDefaults().isKnown(n)) {
            // Never try to redefine anything in the default IntrospectedInfo.
            return false;
        }
        if ((skipReanalysis == null || !skipReanalysis.add(clazz)) && /* #23630 */isKnown(n)) {
            // Either we are not redefining anything; or we are, but this class
            // has already been in the list. Skip it. If we are continuing, make
            // sure to add this class to the skip list so we do not loop.
            return false;
        }
        //AntModule.err.log ("IntrospectedInfo.analyze: clazz=" + clazz.getName ());
        //boolean dbg = (clazz == org.apache.tools.ant.taskdefs.Taskdef.class);
        //if (! dbg && clazz.getName ().equals ("org.apache.tools.ant.taskdefs.Taskdef")) { // NOI18N
        //    AntModule.err.log ("Classloader mismatch: cl1=" + clazz.getClassLoader () + " cl2=" + org.apache.tools.ant.taskdefs.Taskdef.class.getClassLoader ());
        //}
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        IntrospectedClass info = new IntrospectedClass ();
        if (isAttrType) {
            String[] enumTags = AntBridge.getInterface().getEnumeratedValues(clazz);
            if (enumTags != null) {
                info.enumTags = enumTags;
                return !info.equals(clazzes.put(clazz.getName(), info));
            } else {
                // Do not store attr clazzes unless they are interesting: EnumAttr.
                return clazzes.remove(clazz.getName()) != null;
            }
            // That's all we do - no subelements etc.
        }
        IntrospectionHelperProxy helper = AntBridge.getInterface().getIntrospectionHelper(clazz);
        info.supportsText = helper.supportsCharacters ();
        Enumeration e = helper.getAttributes ();
        Set/*<Class>*/ nueAttrTypeClazzes = new HashSet();
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        if (e.hasMoreElements ()) {
            info.attrs = new HashMap ();
            while (e.hasMoreElements ()) {
                String name = (String) e.nextElement ();
                //if (dbg) AntModule.err.log ("\tname=" + name);
                try {
                    Class attrType = helper.getAttributeType(name);
                    String type = attrType.getName();
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
                    nueAttrTypeClazzes.add(attrType);
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
        boolean changed = !info.equals(clazzes.put(clazz.getName(), info));
        // And recursively analyze reachable classes for subelements...
        // (usually these will already be known, and analyze will return at once)
        Iterator it = nueClazzes.iterator ();
        while (it.hasNext ()) {
            changed |= analyze((Class)it.next(), skipReanalysis, false);
        }
        it = nueAttrTypeClazzes.iterator();
        while (it.hasNext()) {
            changed |= analyze((Class)it.next(), skipReanalysis, true);
        }
        return changed;
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
     * This will try to change existing definitions in the custom set, i.e.
     * if a task is defined to be implemented with a different class, or if a
     * class changes structure.
     * Will not try to define anything contained in the defaults list.
     * @param defs map from kinds to maps from names to classes
     */
    public void scanProject (Map defs) {
        init();
        Iterator it = defs.entrySet().iterator();
        Set skipReanalysis = new HashSet();
        boolean changed = false;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            changed |= scanMap((Map)e.getValue(), (String)e.getKey(), skipReanalysis);
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.scanProject: " + this);
        }
        if (changed) {
            fireStateChanged();
        }
    }
    
    private boolean scanMap(Map/*<String,Class>*/ m, String kind, Set/*<Class>*/ skipReanalysis) {
        if (kind == null) throw new IllegalArgumentException();
        boolean changed = false;
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
                if (defaults.get(name) == null) {
                    changed |= !clazz.getName().equals(registry.put(name, clazz.getName()));
                }
                if (! getDefaults ().isKnown (clazz.getName ())) {
                    try {
                        changed |= analyze(clazz, skipReanalysis, false);
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
        return changed;
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
        public String[] enumTags; // null or list of tags
        
        public String toString () {
            String tags;
            if (enumTags != null) {
                tags = Arrays.asList((Object[])enumTags).toString();
            } else {
                tags = "null"; // NOI18N
            }
            return "IntrospectedClass[text=" + supportsText + ",attrs=" + attrs + ",subs=" + subs + ",enumTags=" + tags + "]"; // NOI18N
        }
        
        public int hashCode() {
            // XXX
            return 0;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof IntrospectedClass)) {
                return false;
            }
            IntrospectedClass other = (IntrospectedClass)o;
            return supportsText == other.supportsText &&
                Utilities.compareObjects(attrs, other.attrs) &&
                Utilities.compareObjects(subs, other.subs) &&
                Utilities.compareObjects(enumTags, other.enumTags);
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
                ii2.init();
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
            proxied[i].addChangeListener(WeakListeners.change(l, proxied[i]));
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
