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
package org.openide.util.lookup;

import org.openide.util.*;

import java.io.*;

import java.net.URL;

import java.util.*;


/** A lookup that implements the JDK1.3 JAR services mechanism and delegates
 * to META-INF/services/name.of.class files.
 * <p>It is not dynamic - so if you need to change the classloader or JARs,
 * wrap it in a ProxyLookup and change the delegate when necessary.
 * Existing instances will be kept if the implementation classes are unchanged,
 * so there is "stability" in doing this provided some parent loaders are the same
 * as the previous ones.
 * <p>If this is to be made public, please move it to the org.openide.util.lookup
 * package; currently used by the core via reflection, until it is needed some
 * other way.
 * @author Jaroslav Tulach, Jesse Glick
 * @see "#14722"
 */
final class MetaInfServicesLookup extends AbstractLookup {
    // Better not to use ErrorManager here - EM.gD will use this class, might cause cycles etc.
    private static final boolean DEBUG = Boolean.getBoolean("org.openide.util.lookup.MetaInfServicesLookup.DEBUG"); // NOI18N
    private static final Map knownInstances = new WeakHashMap(); // Map<Class,Object>

    /** A set of all requested classes.
     * Note that classes that we actually succeeded on can never be removed
     * from here because we hold a strong reference to the loader.
     * However we also hold classes which are definitely not loadable by
     * our loader.
     */
    private final Set classes = new WeakSet(); // Set<Class>

    /** class loader to use */
    private final ClassLoader loader;

    /** Create a lookup reading from the classpath.
     * That is, the same classloader as this class itself.
     */
    public MetaInfServicesLookup() {
        this(MetaInfServicesLookup.class.getClassLoader());
    }

    /** Create a lookup reading from a specified classloader.
     */
    public MetaInfServicesLookup(ClassLoader loader) {
        this.loader = loader;

        if (DEBUG) {
            System.err.println("Created: " + this); // NOI18N
        }
    }

    public String toString() {
        return "MetaInfServicesLookup[" + loader + "]"; // NOI18N
    }

    /* Tries to load appropriate resources from manifest files.
     */
    protected final void beforeLookup(Lookup.Template t) {
        Class c = t.getType();

        Object listeners;

        synchronized (this) {
            if (classes.add(c)) {
                // Added new class, search for it.
                Collection arr = new LinkedHashSet(lookup(new Template(Object.class)).allItems());
                search(c, arr);

                // listeners are notified under while holding lock on class c, 
                // let say it is acceptable now
                listeners = setPairsAndCollectListeners(arr);
            } else {
                // ok, nothing needs to be done
                return;
            }
        }

        notifyCollectedListeners(listeners);
    }

    /** Finds all pairs and adds them to the collection.
     *
     * @param clazz class to find
     * @param result collection to add Pair to
     */
    private void search(Class clazz, Collection result) {
        if (DEBUG) {
            System.err.println("Searching for " + clazz.getName() + " in " + clazz.getClassLoader() + " from " + this); // NOI18N
        }

        String res = "META-INF/services/" + clazz.getName(); // NOI18N
        Enumeration en;

        try {
            en = loader.getResources(res);
        } catch (IOException ioe) {
            // do not use ErrorManager because we are in the startup code
            // and ErrorManager might not be ready
            ioe.printStackTrace();

            return;
        }

        // Do not create multiple instances in case more than one JAR
        // has the same entry in it (and they load to the same class).
        // Probably would not happen, assuming JARs only list classes
        // they own, but just in case...
        List /*<Item>*/ foundClasses = new ArrayList();
        Collection removeClasses = new ArrayList(); // Collection<Class>

        boolean foundOne = false;

        while (en.hasMoreElements()) {
            if (!foundOne) {
                foundOne = true;

                // Double-check that in fact we can load the *interface* class.
                // For example, say class I is defined in two JARs, J1 and J2.
                // There is also an implementation M1 defined in J1, and another
                // implementation M2 defined in J2.
                // Classloaders C1 and C2 are made from J1 and J2.
                // A MetaInfServicesLookup is made from C1. Then the user asks to
                // lookup I as loaded from C2. J1 has the services line and lists
                // M1, and we can in fact make it. However it is not of the desired
                // type to be looked up. Don't do this check, which could be expensive,
                // unless we expect to be getting some results, however.
                Class realMcCoy = null;

                try {
                    realMcCoy = loader.loadClass(clazz.getName());
                } catch (ClassNotFoundException cnfe) {
                    // our loader does not know about it, OK
                }

                if (realMcCoy != clazz) {
                    // Either the interface class is not available at all in our loader,
                    // or it is not the same version as we expected. Don't provide results.
                    if (DEBUG) {
                        if (realMcCoy != null) {
                            System.err.println(
                                clazz.getName() + " is not the real McCoy! Actually found it in " +
                                realMcCoy.getClassLoader()
                            ); // NOI18N
                        } else {
                            System.err.println(clazz.getName() + " could not be found in " + loader); // NOI18N
                        }
                    }

                    return;
                }
            }

            URL url = (URL) en.nextElement();
            Item currentItem = null;

            try {
                InputStream is = url.openStream();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N

                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        line = line.trim();

                        // is it position attribute?
                        if (line.startsWith("#position=")) {
                            if (currentItem == null) {
                                assert false : "Found line '" + line + "' but there is no item to associate it with!";
                            }

                            try {
                                currentItem.position = Integer.parseInt(line.substring(10));
                            } catch (NumberFormatException e) {
                                // do not use ErrorManager because we are in the startup code
                                // and ErrorManager might not be ready
                                e.printStackTrace();
                            }
                        }

                        if (currentItem != null) {
                            insertItem(currentItem, foundClasses);
                            currentItem = null;
                        }

                        // Ignore blank lines and comments.
                        if (line.length() == 0) {
                            continue;
                        }

                        boolean remove = false;

                        if (line.charAt(0) == '#') {
                            if ((line.length() == 1) || (line.charAt(1) != '-')) {
                                continue;
                            }

                            // line starting with #- is a sign to remove that class from lookup
                            remove = true;
                            line = line.substring(2);
                        }

                        Class inst = null;

                        try {
                            // Most lines are fully-qualified class names.
                            inst = Class.forName(line, false, loader);
                        } catch (ClassNotFoundException cnfe) {
                            if (remove) {
                                // if we are removing somthing and the something
                                // cannot be found it is ok to do nothing
                                continue;
                            } else {
                                // but if we are not removing just rethrow
                                throw cnfe;
                            }
                        }

                        if (!clazz.isAssignableFrom(inst)) {
                            if (DEBUG) {
                                System.err.println("Not a subclass"); // NOI18N
                            }

                            throw new ClassNotFoundException(inst.getName() + " not a subclass of " + clazz.getName()); // NOI18N
                        }

                        if (remove) {
                            removeClasses.add(inst);
                        } else {
                            // create new item here, but do not put it into
                            // foundClasses array yet because following line
                            // might specify its position
                            currentItem = new Item();
                            currentItem.clazz = inst;
                        }
                    }

                    if (currentItem != null) {
                        insertItem(currentItem, foundClasses);
                        currentItem = null;
                    }
                } finally {
                    is.close();
                }
            } catch (ClassNotFoundException ex) {
                // do not use ErrorManager because we are in the startup code
                // and ErrorManager might not be ready
                ex.printStackTrace();
            } catch (IOException ex) {
                // do not use ErrorManager because we are in the startup code
                // and ErrorManager might not be ready
                ex.printStackTrace();
            }
        }

        if (DEBUG) {
            System.err.println(
                "Found impls of " + clazz.getName() + ": " + foundClasses + " and removed: " + removeClasses +
                " from: " + this
            ); // NOI18N
        }

        foundClasses.removeAll(removeClasses);

        Iterator it = foundClasses.iterator();

        while (it.hasNext()) {
            Item item = (Item) it.next();

            if (removeClasses.contains(item.clazz)) {
                continue;
            }

            result.add(new P(item.clazz));
        }
    }

    /**
     * Insert item to the list according to item.position value.
     */
    private void insertItem(Item item, List list) {
        // no position? -> add it to the end
        if (item.position == -1) {
            list.add(item);

            return;
        }

        int index = -1;
        Iterator it = list.iterator();

        while (it.hasNext()) {
            index++;

            Item i = (Item) it.next();

            if (i.position == -1) {
                list.add(index, item);

                return;
            } else {
                if (i.position > item.position) {
                    list.add(index, item);

                    return;
                }
            }
        }

        list.add(item);
    }

    private static class Item {
        private Class clazz;
        private int position = -1;
    }

    /** Pair that holds name of a class and maybe the instance.
     */
    private static final class P extends Pair {
        /** May be one of three things:
         * 1. The implementation class which was named in the services file.
         * 2. An instance of it.
         * 3. Null, if creation of the instance resulted in an error.
         */
        private Object object;

        public P(Class clazz) {
            this.object = clazz;
        }

        /** Finds the class.
         */
        private Class clazz() {
            Object o = object;

            if (o instanceof Class) {
                return (Class) o;
            } else if (o != null) {
                return o.getClass();
            } else {
                // Broken.
                return Object.class;
            }
        }

        public boolean equals(Object o) {
            if (o instanceof P) {
                return ((P) o).clazz().equals(clazz());
            }

            return false;
        }

        public int hashCode() {
            return clazz().hashCode();
        }

        protected boolean instanceOf(Class c) {
            return c.isAssignableFrom(clazz());
        }

        public Class getType() {
            return clazz();
        }

        public Object getInstance() {
            Object o = object; // keeping local copy to avoid another

            // thread to modify it under my hands
            if (o instanceof Class) {
                synchronized (o) { // o is Class and we will not create 
                                   // 2 instances of the same class

                    try {
                        Class c = ((Class) o);

                        synchronized (knownInstances) { // guards only the static cache
                            o = knownInstances.get(c);
                        }

                        if (o == null) {
                            o = c.newInstance();

                            synchronized (knownInstances) { // guards only the static cache
                                knownInstances.put(c, o);
                            }
                        }

                        // Do not assign to instance var unless there is a complete synch
                        // block between the newInstance and this line. Otherwise we could
                        // be assigning a half-constructed instance that another thread
                        // could see and return immediately.
                        object = o;
                    } catch (Exception ex) {
                        // do not use ErrorManager because we are in the startup code
                        // and ErrorManager might not be ready
                        ex.printStackTrace();
                        object = null;
                    }
                }
            }

            return object;
        }

        public String getDisplayName() {
            return clazz().getName();
        }

        public String getId() {
            return clazz().getName();
        }

        protected boolean creatorOf(Object obj) {
            return obj == object;
        }
    }
}
