/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.util.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.SharedClassObjectBridge;

/**
 * @author Jaroslav Tulach, Jesse Glick
 * @see Lookups#metaInfServices(ClassLoader,String)
 * @see "#14722"
 */
final class MetaInfServicesLookup extends AbstractLookup {

    private static final Logger LOGGER = Logger.getLogger(MetaInfServicesLookup.class.getName());
    static final Executor RP;
    static {
        Executor res = null;
        try {
            Class<?> seek = Class.forName("org.openide.util.RequestProcessor");
            res = (Executor)seek.newInstance();
        } catch (Throwable t) {
            res = Executors.newSingleThreadExecutor();
        }
        RP = res;
    }
    /*TBD: Inject RequestProcessor somehow
     new RequestProcessor(MetaInfServicesLookup.class.getName(), 1);
     */
    private static int knownInstancesCount;
    private static final List<Reference<Object>> knownInstances;
    static {
        knownInstances = new ArrayList<Reference<Object>>();
        for (int i = 0; i < 512; i++) {
            knownInstances.add(null);
        }
    }

    /** A set of all requested classes.
     * Note that classes that we actually succeeded on can never be removed
     * from here because we hold a strong reference to the loader.
     * However we also hold classes which are definitely not loadable by
     * our loader.
     */
    private final Map<Class<?>,Object> classes = new WeakHashMap<Class<?>,Object>();

    /** class loader to use */
    private final ClassLoader loader;
    /** prefix to prepend */
    private final String prefix;

    /** Create a lookup reading from a specified classloader.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MetaInfServicesLookup(ClassLoader loader, String prefix) {
        this.loader = loader;
        this.prefix = prefix;

        LOGGER.log(Level.FINE, "Created: {0}", this);
    }

    @Override
    public String toString() {
        return "MetaInfServicesLookup[" + loader + "]"; // NOI18N
    }

    /* Tries to load appropriate resources from manifest files.
     */
    @Override
    protected final void beforeLookup(Lookup.Template<?> t) {
        Class<?> c = t.getType();

        Collection<AbstractLookup.Pair<?>> toAdd = null;
        synchronized (this) {
            if (classes.get(c) == null) { // NOI18N
                toAdd = new ArrayList<Pair<?>>();
            } else {
                // ok, nothing needs to be done
                return;
            }
        }
        if (toAdd != null) {
            search(c, toAdd);
        }
        synchronized (this) {
            if (classes.put(c, "") == null) { // NOI18N
                // Added new class, search for it.
                LinkedHashSet<AbstractLookup.Pair<?>> arr = getPairsAsLHS();
                arr.addAll(toAdd);
                setPairs(arr, RP);
            }
        }
    }

    /** Finds all pairs and adds them to the collection.
     *
     * @param clazz class to find
     * @param result collection to add Pair to
     */
    private void search(Class<?> clazz, Collection<AbstractLookup.Pair<?>> result) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Searching for {0} in {1} from {2}", new Object[] {clazz.getName(), clazz.getClassLoader(), this});
        }

        String res = prefix + clazz.getName(); // NOI18N
        Enumeration<URL> en;

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
        List<Item> foundClasses = new ArrayList<Item>();
        Collection<Class<?>> removeClasses = new ArrayList<Class<?>>();

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
                Class<?> realMcCoy = null;

                try {
                    realMcCoy = loader.loadClass(clazz.getName());
                } catch (ClassNotFoundException cnfe) {
                    // our loader does not know about it, OK
                }

                if (realMcCoy != clazz) {
                    // Either the interface class is not available at all in our loader,
                    // or it is not the same version as we expected. Don't provide results.
                    if (realMcCoy != null) {
                        LOGGER.log(Level.WARNING, "{0} is not the real McCoy! Actually found it in {1}",
                                new Object[] {clazz.getName(), realMcCoy.getClassLoader()}); // NOI18N
                    } else {
                        LOGGER.log(Level.WARNING, "{0} could not be found in {1}", new Object[] {clazz.getName(), loader}); // NOI18N
                    }

                    return;
                }
            }

            URL url = en.nextElement();
            Item currentItem = null;

            try {
                InputStream is = url.openStream();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N

                    // XXX consider using ServiceLoaderLine instead
                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        line = line.trim();

                        // is it position attribute?
                        if (line.startsWith("#position=")) {
                            if (currentItem == null) {
                                LOGGER.log(Level.WARNING, "Found line '{0}' in {1} but there is no item to associate it with", new Object[] {line, url});
                                continue;
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

                        Class<?> inst = null;

                        try {
                            // Most lines are fully-qualified class names.
                            inst = Class.forName(line, false, loader);
                        } catch (LinkageError err) {
                            if (remove) {
                                continue;
                            }
                            throw new ClassNotFoundException(err.getMessage(), err);
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
                            throw new ClassNotFoundException(clazzToString(inst) + " not a subclass of " + clazzToString(clazz)); // NOI18N
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
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        LOGGER.log(Level.FINER, "Found impls of {0}: {1} and removed: {2} from: {3}", new Object[] {clazz.getName(), foundClasses, removeClasses, this});

        /* XXX makes no sense, wrong types:
        foundClasses.removeAll(removeClasses);
         */

        for (Item item : foundClasses) {
            if (removeClasses.contains(item.clazz)) {
                continue;
            }

            result.add(new P(item.clazz));
        }
    }
    private static String clazzToString(Class<?> clazz) {
        return clazz.getName() + "@" + clazz.getClassLoader() + ":" + clazz.getProtectionDomain().getCodeSource().getLocation(); // NOI18N
    }

    /**
     * Insert item to the list according to item.position value.
     */
    private void insertItem(Item item, List<Item> list) {
        // no position? -> add it to the end
        if (item.position == -1) {
            list.add(item);

            return;
        }

        int index = -1;
        for (Item i : list) {
            index++;

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
        private Class<?> clazz;
        private int position = -1;
        @Override
        public String toString() {
            return "MetaInfServicesLookup.Item[" + clazz.getName() + "]"; // NOI18N
        }
    }

    /** Pair that holds name of a class and maybe the instance.
     */
    private static final class P extends AbstractLookup.Pair<Object> {
        /** May be one of three things:
         * 1. The implementation class which was named in the services file.
         * 2. An instance of it.
         * 3. Null, if creation of the instance resulted in an error.
         */
        private Object object;

        public P(Class<?> clazz) {
            this.object = clazz;
        }

        /** Finds the class.
         */
        private Class<? extends Object> clazz() {
            Object o = object;

            if (o instanceof Class<?>) {
                return (Class<? extends Object>) o;
            } else if (o != null) {
                return o.getClass();
            } else {
                // Broken.
                return Object.class;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof P) {
                return ((P) o).clazz().equals(clazz());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return clazz().hashCode();
        }

        protected @Override boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(clazz());
        }

        public @Override Class<?> getType() {
            return clazz();
        }

        public @Override Object getInstance() {
            Object o = object; // keeping local copy to avoid another

            // thread to modify it under my hands
            if (o instanceof Class<?>) {
                synchronized (o) { // o is Class and we will not create 
                                   // 2 instances of the same class

                    try {
                        Class<?> c = ((Class<?>) o);
                        o = null;

                        synchronized (knownInstances) { // guards only the static cache
                            int size = knownInstances.size();
                            int index = hashForClass(c, size);
                            for (int i = 0; i < size; i++) {
                                Reference<Object> ref = knownInstances.get(index);
                                Object obj = ref == null ? null : ref.get();
                                if (obj == null) {
                                    break;
                                }
                                if (c == obj.getClass()) {
                                    o = obj;
                                    break;
                                }
                                if (++index == size) {
                                    index = 0;
                                }
                            }
                        }

                        if (o == null) {
                            o = SharedClassObjectBridge.newInstance(c);

                            synchronized (knownInstances) { // guards only the static cache
                                hashPut(o);

                                int size = knownInstances.size();
                                if (knownInstancesCount > size * 2 / 3) {
                                    LOGGER.log(Level.CONFIG, "Cache of size {0} is 2/3 full. Rehashing.", size);
                                    HashSet<Reference<Object>> all = new HashSet<Reference<Object>>();
                                    all.addAll(knownInstances);
                                    for (int i = 0; i < size; i++) {
                                        knownInstances.set(i, null);
                                    }
                                    for (int i = 0; i < size; i++) {
                                        knownInstances.add(null);
                                    }
                                    knownInstancesCount = 0;
                                    for (Reference<Object> r : all) {
                                        if (r == null) {
                                            continue;
                                        }
                                        Object instance = r.get();
                                        if (instance == null) {
                                            continue;
                                        }
                                        hashPut(instance);
                                    }
                                }

                            }
                        }

                        // Do not assign to instance var unless there is a complete synch
                        // block between the newInstance and this line. Otherwise we could
                        // be assigning a half-constructed instance that another thread
                        // could see and return immediately.
                        object = o;
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Cannot create " + object, ex);
                        object = null;
                    } catch (LinkageError x) { // #174055 + NoClassDefFoundError
                        LOGGER.log(Level.WARNING, "Cannot create " + object, x);
                        object = null;
                    }
                }
            }

            return object;
        }

        public @Override String getDisplayName() {
            return clazz().getName();
        }

        public @Override String getId() {
            return clazz().getName();
        }

        protected @Override boolean creatorOf(Object obj) {
            return obj == object;
        }
        private static int hashForClass(Class<?> c, int size) {
            return Math.abs(c.hashCode() % size);
        }

        private static void hashPut(Object o) {
            Class<?> c = o.getClass();
            int size = knownInstances.size();
            int index = hashForClass(c, size);
            for (int i = 0; i < size; i++) {
                Reference<Object> ref = knownInstances.get(index);
                Object obj = ref == null ? null : ref.get();
                if (obj == null) {
                    knownInstances.set(index, new WeakReference<Object>(o));
                    knownInstancesCount++;
                    break;
                }
                if (++index == size) {
                    index = 0;
                }
            }
        }

    }
}
