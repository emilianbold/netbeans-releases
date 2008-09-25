/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.debugger;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 * Lookup implementation, which provides services in a list.
 * The list can refresh itself when the services change.
 * The refreshing is performed under a lock on the list object so that
 * clients have consistent data under synchronization on the list instance.
 *
 * @author   Jan Jancura, Martin Entlicher
 */
abstract class Lookup implements ContextProvider {
    
    public static final String NOTIFY_LOAD_FIRST = "load first";
    public static final String NOTIFY_LOAD_LAST = "load last";
    public static final String NOTIFY_UNLOAD_FIRST = "unload first";
    public static final String NOTIFY_UNLOAD_LAST = "unload last";
    
    public <T> T lookupFirst(String folder, Class<T> service) {
        List<? extends T> l = lookup(folder, service);
        synchronized (l) {
            if (l.isEmpty ()) return null;
            return l.get (0);
        }
    }
    
    public abstract <T> List<? extends T> lookup(String folder, Class<T> service);
    
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.registration") != null;
    
    
    static class Instance extends Lookup {
        private Object[] services;
        
        Instance (Object[] services) {
            this.services = services;
        }
        
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            List<T> l = new ArrayList<T>();
            for (Object s : services) {
                if (service.isInstance(s)) {
                    l.add(service.cast(s));
                    if (verbose)
                        System.out.println("\nR  instance " + s + " found");
                }
            }
            return l;
        }
    }
    
    static class Compound extends Lookup {
        private ContextProvider l1;
        private ContextProvider l2;
        
        Compound(ContextProvider l1, ContextProvider l2) {
            this.l1 = l1;
            this.l2 = l2;
            setContext (this);
        }
        
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            return new CompoundLookupList<T>(folder, service);
        }
        
        void setContext (Lookup context) {
            if (l1 instanceof Compound) ((Compound) l1).setContext (context);
            if (l1 instanceof MetaInf) ((MetaInf) l1).setContext (context);
            if (l2 instanceof Compound) ((Compound) l2).setContext (context);
            if (l2 instanceof MetaInf) ((MetaInf) l2).setContext (context);
        }
        
        private class CompoundLookupList<T> extends LookupList<T> implements Customizer,
                                                                       PropertyChangeListener {
            
            private String folder;
            private Class<T> service;
            private List<PropertyChangeListener> propertyChangeListeners;
            private Customizer sublist1, sublist2;
            
            public CompoundLookupList(String folder, Class<T> service) {
                super(null);
                this.folder = folder;
                this.service = service;
                setUp();
            }
            
            private synchronized void setUp() {
                clear();
                List<? extends T> list1 = l1.lookup(folder, service);
                List<? extends T> list2 = l2.lookup(folder, service);
                addAll (list1);
                addAll (list2);
                sublist1 = (list1 instanceof Customizer) ? (Customizer) list1 : null;
                sublist2 = (list2 instanceof Customizer) ? (Customizer) list2 : null;
            }

            public synchronized void setObject(Object bean) {
                if (sublist1 != null) sublist1.setObject(bean);
                if (sublist2 != null) sublist2.setObject(bean);
            }

            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                if (propertyChangeListeners == null) {
                    propertyChangeListeners = new ArrayList<PropertyChangeListener>();
                    if (sublist1 != null) sublist1.addPropertyChangeListener(this);
                    if (sublist2 != null) sublist2.addPropertyChangeListener(this);
                }
                propertyChangeListeners.add(listener);
            }

            public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeListeners.remove(listener);
            }

            public void propertyChange(PropertyChangeEvent e) {
                setUp();
                List<PropertyChangeListener> listeners;
                synchronized (this) {
                    if (propertyChangeListeners == null) {
                        return ;
                    }
                    listeners = new ArrayList<PropertyChangeListener>(propertyChangeListeners);
                }
                PropertyChangeEvent evt = new PropertyChangeEvent(this, "content", null, null);
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }
        }
    }
    
    static class MetaInf extends Lookup {
        
        private static final String HIDDEN = "-hidden"; // NOI18N
        
        private String rootFolder;
        private final Map<String,List<String>> registrationCache = new HashMap<String,List<String>>();
        private final HashMap<String, Object> instanceCache = new HashMap<String, Object>();
        private Lookup context;
        private org.openide.util.Lookup.Result<ModuleInfo> moduleLookupResult;
        private ModuleChangeListener modulesChangeListener;
        private final Map<ClassLoader, ModuleChangeListener> moduleChangeListeners
                = new HashMap<ClassLoader, ModuleChangeListener>();
        private final Map<ModuleInfo, ModuleChangeListener> disabledModuleChangeListeners
                = new HashMap<ModuleInfo, ModuleChangeListener>();
        private final Set<MetaInfLookupList> lookupLists = new WeakSet<MetaInfLookupList>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        
        MetaInf (String rootFolder) {
            this.rootFolder = rootFolder;
            moduleLookupResult = org.openide.util.Lookup.getDefault().lookupResult(ModuleInfo.class);
            //System.err.println("\nModules = "+moduleLookupResult.allInstances().size()+"\n");
            modulesChangeListener = new ModuleChangeListener(null);
            moduleLookupResult.addLookupListener(
                    WeakListeners.create(org.openide.util.LookupListener.class,
                                         modulesChangeListener,
                                         moduleLookupResult));
        }
        
        void setContext (Lookup context) {
            this.context = context;
        }
        
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            MetaInfLookupList<T> mll = new MetaInfLookupList<T>(folder, service);
            synchronized (lookupLists) {
                lookupLists.add(mll);
            }
            return mll;
        }
        
        private List<String> list(String folder, Class<?> service) {
            String name = service.getName ();
            String resourceName = "META-INF/debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") + 
                ((folder == null) ? "" : folder + "/") + 
                name;
            synchronized(registrationCache) {
                List<String> l = registrationCache.get(resourceName);
                if (l == null) {
                    l = loadMetaInf(resourceName);
                    registrationCache.put(resourceName, l);
                }
                return l;
            }
        }
    
        private static Set<String> getHiddenClassNames(List l) {
            Set<String> s = null;
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                String className = (String) l.get (i);
                if (className.endsWith(HIDDEN)) {
                    if (s == null) {
                        s = new HashSet<String>();
                    }
                    s.add(className.substring(0, className.length() - HIDDEN.length()));
                }
            }
            return s;
        }
            
        /**
         * Loads instances of given class from META-INF/debugger from given
         * folder. Given context isused as the parameter to constructor.
         */
        private List<String> loadMetaInf(String resourceName) {
            List<String> l = new ArrayList<String>();
            try {
                ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                String v = "\nR lookup " + resourceName;
                Enumeration<URL> e = cl.getResources(resourceName);
                Set<URL> urls = new HashSet<URL>();
                while (e.hasMoreElements ()) {
                    URL url = e.nextElement();
                    // Ignore duplicated URLs, necessary because of tests
                    if (urls.contains(url)) continue;
                    urls.add(url);
                    InputStream is = url.openStream ();
                    if (is == null) continue;
                    BufferedReader br = new BufferedReader (
                        new InputStreamReader (is)
                    );
                    for (String s = br.readLine(); s != null; s = br.readLine()) {
                        if (s.startsWith ("#")) continue;
                        if (s.length () == 0) continue;
                        if (verbose)
                            v += "\nR  service " + s + " found";

                        l.add (s);
                    }
                }
                if (verbose)
                    System.out.println (v);
                return l; 
            } catch (IOException e) {
                e.printStackTrace ();
            }
            throw new InternalError ("Can not read from Meta-inf!");
        }
        
        private Object createInstance (String service) {
            try {
                ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                String method = null;
                if (service.endsWith("()")) {
                    int lastdot = service.lastIndexOf('.');
                    if (lastdot < 0) {
                        Exceptions.printStackTrace(
                                new IllegalStateException("Bad service - dot before method name is missing: " +
                                "'" + service + "'."));
                        return null;
                    }
                    method = service.substring(lastdot + 1, service.length() - 2).trim();
                    service = service.substring(0, lastdot);
                }
                Class cls = cl.loadClass (service);

                Object o = null;
                if (method != null) {
                    Method m = null;
                    if (context != null) {
                        try {
                            m = cls.getDeclaredMethod(method, new Class[] { Lookup.class });
                        } catch (NoSuchMethodException nsmex) {}
                    }
                    if (m == null) {
                        try {
                            m = cls.getDeclaredMethod(method, new Class[] { });
                        } catch (NoSuchMethodException nsmex) {}
                    }
                    if (m != null) {
                        o = m.invoke(null, (m.getParameterTypes().length == 0)
                                     ? new Object[] {} : new Object[] { context });
                    }
                }
                if (o == null && context != null) {
                    Constructor[] cs = cls.getConstructors ();
                    int i, k = cs.length;
                    for (i = 0; i < k; i++) {
                        Constructor c = cs [i];
                        if (c.getParameterTypes ().length != 1) continue;
                        try {
                            o = c.newInstance (new Object[] {context});
                        } catch (IllegalAccessException e) {
                            if (verbose) {
                                System.out.println("\nservice: " + service);
                                e.printStackTrace ();
                            }
                        } catch (IllegalArgumentException e) {
                            if (verbose) {
                                System.out.println("\nservice: " + service);
                                e.printStackTrace ();
                            }
                        }
                    }
                }
                if (o == null)
                    o = cls.newInstance ();
                if (verbose)
                    System.out.println("\nR  instance " + o + 
                        " created");
                return o;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(
                        Exceptions.attachMessage(
                            e,
                            "The service "+service+" not found."));
            } catch (InstantiationException e) {
                Exceptions.printStackTrace(
                        Exceptions.attachMessage(
                        e,
                        "The service "+service+" can not be instantiated."));
            } catch (IllegalAccessException e) {
                Exceptions.printStackTrace(
                        Exceptions.attachMessage(
                        e,
                        "The service "+service+" can not be accessed."));
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(
                        Exceptions.attachMessage(
                        ex,
                        "The service "+service+" can not be created."));
            } catch (ExceptionInInitializerError ex) {
                Exceptions.printStackTrace(
                        Exceptions.attachMessage(
                        ex,
                        "The service "+service+" can not be initialized."));
            }
            return null;
        }

        private void listenOn(ClassLoader cl) {
            synchronized(moduleChangeListeners) {
                if (!moduleChangeListeners.containsKey(cl)) {
                    for (ModuleInfo mi : moduleLookupResult.allInstances()) {
                        if (mi.isEnabled() && mi.getClassLoader() == cl) {
                            ModuleChangeListener l = new ModuleChangeListener(cl);
                            mi.addPropertyChangeListener(WeakListeners.propertyChange(l, mi));
                            moduleChangeListeners.put(cl, l);
                        }
                    }
                }
            }
        }
        
        private void listenOnDisabledModules() {
            synchronized (moduleChangeListeners) {
                for (ModuleInfo mi : moduleLookupResult.allInstances()) {
                    if (!mi.isEnabled() && !disabledModuleChangeListeners.containsKey(mi)) {
                        ModuleChangeListener l = new ModuleChangeListener(null);
                        mi.addPropertyChangeListener(WeakListeners.propertyChange(l, mi));
                        disabledModuleChangeListeners.put(mi, l);
                    }
                }
            }
        }

        // used by ActionsManager to listen on clearCache events
        void addClearCacheListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        private void clearCaches() {
            synchronized (registrationCache) {
                registrationCache.clear();
            }
            pcs.firePropertyChange("PROP_CACHE", null, null); // NOI18N
        }
        
        private final class ModuleChangeListener implements PropertyChangeListener, org.openide.util.LookupListener {

            private ClassLoader cl;
            private RequestProcessor.Task refreshListEnabled;
            private RequestProcessor.Task refreshListDisabled;
            private RequestProcessor rp;

            public ModuleChangeListener(ClassLoader cl) {
                this.cl = cl;
            }

            // Some module enabled or disabled
            public void propertyChange(PropertyChangeEvent evt) {
                //System.err.println("ModuleChangeListener.propertyChange("+evt+")");
                //System.err.println("  getPropertyName = "+evt.getPropertyName()+", source = "+evt.getSource());
                if (!ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                    return ;
                }
                clearCaches(cl);
                ModuleInfo mi = (ModuleInfo) evt.getSource();
                if (!mi.isEnabled() && cl != null) {
                    synchronized (moduleChangeListeners) {
                        moduleChangeListeners.remove(cl);
                        disabledModuleChangeListeners.put(mi, this);
                    }
                    cl = null;
                } else if (mi.isEnabled()) {
                    cl = mi.getClassLoader();
                    synchronized (moduleChangeListeners) {
                        disabledModuleChangeListeners.remove(mi);
                        moduleChangeListeners.put(cl, this);
                    }
                }
                synchronized (this) {
                    if (mi.isEnabled()) {
                        if (refreshListEnabled == null) {
                            if (rp == null) {
                                rp = new RequestProcessor("Debugger Services Refresh", 1);
                            }
                            refreshListEnabled = rp.create(new Runnable() {
                                public void run() { refreshLists(true); }
                            });
                        }
                        refreshListEnabled.schedule(100);
                    } else {
                        if (refreshListDisabled == null) {
                            if (rp == null) {
                                rp = new RequestProcessor("Debugger Services Refresh", 1);
                            }
                            refreshListDisabled = rp.create(new Runnable() {
                                public void run() { refreshLists(false); }
                            });
                        }
                        refreshListDisabled.schedule(100);
                    }
                }
                //refreshLists(mi.isEnabled());
            }

            // Some new modules installed or old uninstalled
            public void resultChanged(LookupEvent ev) {
                clearCaches(null);
                synchronized (moduleChangeListeners) {
                    moduleChangeListeners.clear();
                    disabledModuleChangeListeners.clear();
                }
                refreshLists(true);
                listenOnDisabledModules();
            }

            private void clearCaches(ClassLoader cl) {
                MetaInf.this.clearCaches();
                if (cl != null) {
                    // Release the appropriate instances from the instance cache
                    synchronized(instanceCache) {
                        List<String> classes = new ArrayList<String>(instanceCache.size());
                        classes.addAll(instanceCache.keySet());
                        for (String clazz : classes) {
                            Object instance = instanceCache.get(clazz);
                            if (instance.getClass().getClassLoader() == cl) {
                                instanceCache.remove(clazz);
                            }
                        }
                    }
                }
            }
            
            private void refreshLists(boolean load) {
                List<MetaInfLookupList> ll;
                synchronized (lookupLists) {
                    ll = new ArrayList<MetaInfLookupList>(lookupLists.size());
                    //System.err.println("\nRefreshing lookup lists ("+load+"):\n");
                    //System.err.println("  unsorted: "+lookupLists+"\n");
                    ll.addAll(lookupLists);
                }
                Collections.sort(ll, getMetaInfLookupListComparator(load));
                //System.err.println("    sorted: "+ll+"\n");
                for (MetaInfLookupList mll : ll) {
                    mll.refreshContent();
                }
            }

        }
            
        public static Comparator<MetaInfLookupList> getMetaInfLookupListComparator(final boolean load) {
            return new Comparator<MetaInfLookupList>() {
                public int compare(MetaInfLookupList l1, MetaInfLookupList l2) {
                    if (load) {
                        return l1.notifyLoadOrder - l2.notifyLoadOrder;
                    } else {
                        return l1.notifyUnloadOrder - l2.notifyUnloadOrder;
                    }
                }
            };
        }
        
        /**
         * A special List implementation, which ensures that hidden elements
         * are removed when adding items into the list.
         * Also it can refresh itself when the services change.
         * The refreshing is performed under a lock on this list object so that
         * clients have consistent data under synchronization on this.
         */
        private final class MetaInfLookupList<T> extends LookupList<T> implements Customizer {
            
            private String folder;
            private final Class<T> service;
            private List<PropertyChangeListener> propertyChangeListeners;
            public int notifyLoadOrder = 0;
            public int notifyUnloadOrder = 0;
            
            public MetaInfLookupList(String folder, Class<T> service) {
                this(list(folder, service), service);
                this.folder = folder;
            }
            
            private MetaInfLookupList(List<String> l, Class<T> service) {
                this(l, getHiddenClassNames(l), service);
            }
            
            private MetaInfLookupList(List<String> l, Set<String> s, Class<T> service) {
                super(s);
                assert service != null;
                this.service = service;
                fillInstances(l, s);
                listenOnDisabledModules();
            }
            
            private void fillInstances(List<String> l, Set<String> s) {
                for (String className : l) {
                    if (className.endsWith(HIDDEN)) continue;
                    if (s != null && s.contains (className)) continue;
                    Object instance = null;
                    synchronized(instanceCache) {
                        instance = instanceCache.get (className);
                    }
                    if (instance != null) {
                        try {
                            add(service.cast(instance), className);
                        } catch (ClassCastException cce) {
                            Logger.getLogger(Lookup.class.getName()).log(Level.WARNING, null, cce);
                        }
                        listenOn(instance.getClass().getClassLoader());
                    } else if (checkClassName(className)) {
                        add(new LazyInstance<T>(service, className), className);
                    }
                }
            }

            private boolean checkClassName(String service) {
                String method = null;
                if (service.endsWith("()")) {
                    int lastdot = service.lastIndexOf('.');
                    if (lastdot < 0) {
                        Exceptions.printStackTrace(
                                new IllegalStateException("Bad service - dot before method name is missing: " +
                                "'" + service + "'."));
                        return false;
                    }
                    method = service.substring(lastdot + 1, service.length() - 2).trim();
                    service = service.substring(0, lastdot);
                }
                ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                URL resource = cl.getResource(service.replace('.', '/')+".class");
                if (resource == null) {
                    Exceptions.printStackTrace(
                            new IllegalStateException("The service "+service+" not found."));
                    return false;
                }
                return true;
            }
            
            private synchronized void refreshContent() {
                // Perform changes under a lock so that iterators reading this list
                // can sync on it
                clear();
                List<String> l = list(folder, service);
                Set<String> s = getHiddenClassNames(l);
                hiddenClassNames = s;
                fillInstances(l, s);
                firePropertyChange();
            }
            
            /* Grrrr can not be static here! :-(((
            public static Comparator<MetaInfLookupList> getComparator(final boolean load) {
                return new Comparator<MetaInfLookupList>() {
                    public int compare(MetaInfLookupList l1, MetaInfLookupList l2) {
                        if (load) {
                            return l1.notifyLoadOrder - l2.notifyLoadOrder;
                        } else {
                            return l1.notifyUnloadOrder - l2.notifyUnloadOrder;
                        }
                    }
                };
            }*/
            
            public void setObject(Object bean) {
                if (NOTIFY_LOAD_FIRST == bean) {
                    notifyLoadOrder = -1;
                } else if (NOTIFY_LOAD_LAST == bean) {
                    notifyLoadOrder = +1;
                } else if (NOTIFY_UNLOAD_FIRST == bean) {
                    notifyUnloadOrder = -1;
                } else if (NOTIFY_UNLOAD_LAST == bean) {
                    notifyUnloadOrder = +1;
                } else {
                    throw new IllegalArgumentException(bean.toString());
                }
            }

            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                if (propertyChangeListeners == null) {
                    propertyChangeListeners = new ArrayList<PropertyChangeListener>();
                }
                propertyChangeListeners.add(listener);
            }

            public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeListeners.remove(listener);
            }
            
            private void firePropertyChange() {
                List<PropertyChangeListener> listeners;
                synchronized (this) {
                    if (propertyChangeListeners == null) {
                        return ;
                    }
                    listeners = new ArrayList<PropertyChangeListener>(propertyChangeListeners);
                }
                PropertyChangeEvent evt = new PropertyChangeEvent(this, "content", null, null);
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }

            private class LazyInstance<T> extends LookupLazyEntry<T> {
                private String className;
                private Class<T> service;
                public LazyInstance(Class<T> service, String className) {
                    this.service = service;
                    this.className = className;
                }

                protected T getEntry() {
                    Object instance = null;
                    synchronized(instanceCache) {
                        instance = instanceCache.get (className);
                        if (instance == null) {
                            instance = createInstance (className);
                            instanceCache.put (className, instance);
                        }
                    }
                    if (instance != null) {
                        try {
                            return service.cast(instance);
                        } catch (ClassCastException cce) {
                            Logger.getLogger(Lookup.class.getName()).log(Level.WARNING, null, cce);
                            return null;
                        } finally {
                            listenOn(instance.getClass().getClassLoader());
                        }
                    } else {
                        return null;
                    }
                }
            }
            
        }
    }
    
    /**
     * A special List implementation, which ensures that hidden elements
     * are removed when adding items into the list.
     */
    private static class LookupList<T> extends LazyArrayList<T> {

        protected Set<String> hiddenClassNames;
        private LinkedHashMap<Object, String> instanceClassNames = new LinkedHashMap<Object, String>();

        public LookupList(Set<String> hiddenClassNames) {
            this.hiddenClassNames = hiddenClassNames;
        }
        
        void add(T instance, String className) {
            super.add(instance);
            instanceClassNames.put(instance, className);
        }
        
        void add(LazyArrayList.LazyEntry instance, String className) {
            super.add(instance);
            instanceClassNames.put(instance, className);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            if (c instanceof LookupList) {
                @SuppressWarnings("unchecked") // XXX possible to remove using more clever pattern with Class.cast
                LookupList<? extends T> ll = (LookupList<? extends T>) c;
                synchronized (ll) {
                synchronized (this) {
                    Set<String> newHiddenClassNames = ll.hiddenClassNames;
                    if (newHiddenClassNames != null) {
                        //System.err.println("\nLookupList.addAll("+c+"), hiddenClassNames = "+hiddenClassNames+" + "+newHiddenClassNames);
                        // Check the instances we have and remove the newly hidden ones:
                        for (Iterator it = newHiddenClassNames.iterator(); it.hasNext(); ) {
                            String className = (String) it.next();
                            if (instanceClassNames.containsValue(className)) {
                                for (Iterator ii = instanceClassNames.keySet().iterator(); it.hasNext(); ) {
                                    Object instance = ii.next();
                                    if (className.equals(instanceClassNames.get(instance))) {
                                        remove(instance);
                                        instanceClassNames.remove(instance);
                                        break;
                                    }
                                }
                            }
                        }
                        if (hiddenClassNames != null) {
                            hiddenClassNames.addAll(newHiddenClassNames);
                        } else {
                            hiddenClassNames = newHiddenClassNames;
                        }
                    }
                    ensureCapacity(size() + ll.size());
                    boolean addedAnything = false;
                    for (int i = 0; i < ll.size(); i++) {
                        Object entry = ll.getEntry(i);
                        String className = ll.instanceClassNames.get(entry);
                        if (hiddenClassNames == null || !hiddenClassNames.contains(className)) {
                            if (entry instanceof LazyEntry) {
                                add((LazyEntry) entry, className);
                            } else {
                                add((T) entry, className);
                            }
                            addedAnything = true;
                        }
                    }
                    return addedAnything;
                }
                }
            } else {
                return super.addAll(c);
            }
        }

        @Override
        public void clear() {
            super.clear();
            instanceClassNames.clear();
        }

        protected abstract class LookupLazyEntry<T> extends LazyEntry<T> {
            protected final T get() {
                T e = getEntry();
                synchronized (LookupList.this) {
                    String className = instanceClassNames.remove(this);
                    if (className != null) {
                        instanceClassNames.put(e, className);
                    }
                }
                return e;
            }

            protected abstract T getEntry();
        }
        
    }
    
}
