/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.TreeSet;

import org.netbeans.spi.debugger.ContextProvider;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.LookupEvent;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 *
 * @author   Jan Jancura
 */
abstract class Lookup implements ContextProvider {
    
    public static final String NOTIFY_LOAD_FIRST = "load first";
    public static final String NOTIFY_LOAD_LAST = "load last";
    public static final String NOTIFY_UNLOAD_FIRST = "unload first";
    public static final String NOTIFY_UNLOAD_LAST = "unload last";
    
    public Object lookupFirst (String folder, Class service) {
        List l = lookup (folder, service);
        if (l.isEmpty ()) return null;
        return l.get (0);
    }
    
    public abstract List lookup (String folder, Class service);
    
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.registration") != null;
    
    
    static class Instance extends Lookup {
        private Object[] services;
        
        Instance (Object[] services) {
            this.services = services;
        }
        
        public List lookup (String folder, Class service) {
            ArrayList l = new ArrayList ();
            int i, k = services.length;
            for (i = 0; i < k; i++)
                if (service.isAssignableFrom (services [i].getClass ())) {
                    l.add (services [i]);
                    if (verbose)
                        System.out.println("\nR  instance " + services [i] + 
                            " found");
                }
            return l;
        }
        
    }
    
    static class Compound extends Lookup {
        private Lookup l1;
        private Lookup l2;
        
        Compound (Lookup l1, Lookup l2) {
            this.l1 = l1;
            this.l2 = l2;
            setContext (this);
        }
        
        public List lookup (String folder, Class service) {
            return new CompoundLookupList(folder, service);
            /*List l = new LookupList(null);
            l.addAll (l1.lookup (folder, service));
            l.addAll (l2.lookup (folder, service));
            return l;*/
        }
        
        void setContext (Lookup context) {
            if (l1 instanceof Compound) ((Compound) l1).setContext (context);
            if (l1 instanceof MetaInf) ((MetaInf) l1).setContext (context);
            if (l2 instanceof Compound) ((Compound) l2).setContext (context);
            if (l2 instanceof MetaInf) ((MetaInf) l2).setContext (context);
        }
        
        private class CompoundLookupList extends LookupList implements Customizer,
                                                                       PropertyChangeListener {
            
            private String folder;
            private Class service;
            private List<PropertyChangeListener> propertyChangeListeners;
            
            public CompoundLookupList(String folder, Class service) {
                super(null);
                this.folder = folder;
                this.service = service;
                setUp();
            }
            
            private void setUp() {
                clear();
                addAll (l1.lookup (folder, service));
                addAll (l2.lookup (folder, service));
            }

            public void setObject(Object bean) {
                ((Customizer) l1).setObject(bean);
                ((Customizer) l2).setObject(bean);
            }

            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                if (propertyChangeListeners == null) {
                    propertyChangeListeners = new ArrayList<PropertyChangeListener>();
                    ((Customizer) l1).addPropertyChangeListener(this);
                    ((Customizer) l2).addPropertyChangeListener(this);
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
        private HashMap registrationCache = new HashMap ();
        private HashMap<String, Object> instanceCache = new HashMap<String, Object>();
        private Lookup context;
        private org.openide.util.Lookup.Result<ModuleInfo> moduleLookupResult;
        private ModuleChangeListener modulesChangeListener;
        private Map<ClassLoader, ModuleChangeListener> moduleChangeListeners
                = new HashMap<ClassLoader, ModuleChangeListener>();
        private Map<ModuleInfo, ModuleChangeListener> disabledModuleChangeListeners
                = new HashMap<ModuleInfo, ModuleChangeListener>();
        private Set<MetaInfLookupList> lookupLists = new WeakSet<MetaInfLookupList>();

        
        MetaInf (String rootFolder) {
            this.rootFolder = rootFolder;
            moduleLookupResult = org.openide.util.Lookup.getDefault ().lookup(
                    new org.openide.util.Lookup.Template(ModuleInfo.class));
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
        
        public List lookup (String folder, Class service) {
            MetaInfLookupList mll = new MetaInfLookupList(folder, service);
            synchronized (lookupLists) {
                lookupLists.add(mll);
            }
            return mll;
        }
        
        private List list (String folder, Class service) {
            String name = service.getName ();
            String resourceName = "META-INF/debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") + 
                ((folder == null) ? "" : folder + "/") + 
                name;
            synchronized(registrationCache) {
                if (!registrationCache.containsKey (resourceName))
                    registrationCache.put (resourceName, loadMetaInf (resourceName));
                return (List) registrationCache.get (resourceName);
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
        private ArrayList loadMetaInf (
            String resourceName
        ) {
            ArrayList l = new ArrayList ();
            try {
                ClassLoader cl = (ClassLoader) org.openide.util.Lookup.
                    getDefault ().lookup (ClassLoader.class);
                String v = "\nR lookup " + resourceName;
                Enumeration e = cl.getResources (resourceName);
                while (e.hasMoreElements ()) {
                    URL url = (URL) e.nextElement();
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
                ClassLoader cl = (ClassLoader) org.openide.util.Lookup.
                    getDefault ().lookup (ClassLoader.class);
                String method = null;
                if (service.endsWith("()")) {
                    int lastdot = service.lastIndexOf('.');
                    if (lastdot < 0) {
                        ErrorManager.getDefault().log("Bad service - dot before method name is missing: " +
                                "'" + service + "'.");
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
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(
                            e,
                            "The service "+service+" is not found.")
                        );
            } catch (InstantiationException e) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(
                            e,
                            "The service "+service+" can not be instantiated.")
                        );
            } catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(
                            e,
                            "The service "+service+" can not be accessed.")
                        );
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(
                            ex,
                            "The service "+service+" can not be created.")
                        );
            } catch (ExceptionInInitializerError ex) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(
                            ex,
                            "The service "+service+" can not be initialized.")
                        );
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

        private final class ModuleChangeListener implements PropertyChangeListener, org.openide.util.LookupListener {

            private ClassLoader cl;

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
                refreshLists(mi.isEnabled());
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
                synchronized(registrationCache) {
                    registrationCache.clear();
                }
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
         * Also it can refresh itself when the services change
         */
        private final class MetaInfLookupList extends LookupList implements Customizer {
            
            private String folder;
            private Class service;
            private List<PropertyChangeListener> propertyChangeListeners;
            public int notifyLoadOrder = 0;
            public int notifyUnloadOrder = 0;
            
            public MetaInfLookupList(String folder, Class service) {
                this(list (folder, service));
                this.folder = folder;
                this.service = service;
            }
            
            private MetaInfLookupList(List l) {
                this(l, getHiddenClassNames(l));
            }
            
            private MetaInfLookupList(List l, Set<String> s) {
                super(s);
                fillInstances(l, s);
                listenOnDisabledModules();
            }
            
            private void fillInstances(List l, Set<String> s) {
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    String className = (String) l.get (i);
                    if (className.endsWith(HIDDEN)) continue;
                    if (s != null && s.contains (className)) continue;
                    Object instance = null;
                    synchronized(instanceCache) {
                        instance = instanceCache.get (className);
                        if (instance == null) {
                            instance = createInstance (className);
                            instanceCache.put (className, instance);
                        }
                    }
                    if (instance != null) {
                        add (instance, className);
                        listenOn(instance.getClass().getClassLoader());
                    }
                }
            }
            
            private void refreshContent() {
                clear();
                List l = list (folder, service);
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
            
        }
    }
    
    /**
     * A special List implementation, which ensures that hidden elements
     * are removed when adding items into the list.
     */
    private static class LookupList extends ArrayList<Object> {

        protected Set<String> hiddenClassNames;
        private LinkedHashMap<Object, String> instanceClassNames = new LinkedHashMap<Object, String>();

        public LookupList(Set<String> hiddenClassNames) {
            this.hiddenClassNames = hiddenClassNames;
        }
        
        void add(Object instance, String className) {
            super.add(instance);
            instanceClassNames.put(instance, className);
        }
        
        @Override
        public boolean addAll(Collection c) {
            if (c instanceof LookupList) {
                LookupList ll = (LookupList) c;
                Set<String> newHiddenClassNames = ll.hiddenClassNames;
                if (newHiddenClassNames != null) {
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
                for (Iterator it = ll.iterator(); it.hasNext(); ) {
                    Object instance = it.next();
                    String className = ll.instanceClassNames.get(instance);
                    if (hiddenClassNames == null || !hiddenClassNames.contains(className)) {
                        add(instance, className);
                        addedAnything = true;
                    }
                }
                return addedAnything;
            } else {
                return super.addAll(c);
            }
        }

        @Override
        public void clear() {
            super.clear();
            instanceClassNames.clear();
        }
        
    }
    
}
