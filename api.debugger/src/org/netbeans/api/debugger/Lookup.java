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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.spi.debugger.ContextProvider;
import org.openide.ErrorManager;


/**
 *
 * @author   Jan Jancura
 */
abstract class Lookup implements ContextProvider {
    
    abstract List lookup (String folder, Class service, Set hidden);
    abstract Set getHiddenItems (String folder, Class service);
    public Object lookupFirst (String folder, Class service) {
        List l = lookup (folder, service);
        if (l.isEmpty ()) return null;
        return l.get (0);
    }
    public List lookup (String folder, Class service) {
        return lookup (folder, service, Collections.EMPTY_SET);
    }
    
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.registration") != null;
    
    
    static class Instance extends Lookup {
        private Object[] services;
        
        Instance (Object[] services) {
            this.services = services;
        }
        
        List lookup (String folder, Class service, Set hidden) {
            ArrayList l = new ArrayList ();
            int i, k = services.length;
            for (i = 0; i < k; i++)
                if (service.isAssignableFrom (services [i].getClass ())) {
                    if (hidden.contains (services [i].getClass ().getName ()))
                        continue;
                    l.add (services [i]);
                    if (verbose)
                        System.out.println("\nR  instance " + services [i] + 
                            " found");
                }
            return l;
        }
        
        Set getHiddenItems (String folder, Class service) {
            return Collections.EMPTY_SET;
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
        
        List lookup (String folder, Class service, Set hidden) {
            ArrayList l = new ArrayList ();
            l.addAll (l1.lookup (folder, service, hidden));
            l.addAll (l2.lookup (folder, service, hidden));
            return l;
        }
        
        Set getHiddenItems (String folder, Class service) {
            Set s = new HashSet ();
            Iterator i = l1.getHiddenItems (folder, service).iterator ();
            while (i.hasNext ())
                s.add (i.next ());
            i = l2.getHiddenItems (folder, service).iterator ();
            while (i.hasNext ())
                s.add (i.next ());
            return s;
        }
        
        void setContext (Lookup context) {
            if (l1 instanceof Compound) ((Compound) l1).setContext (context);
            if (l1 instanceof MetaInf) ((MetaInf) l1).setContext (context);
            if (l2 instanceof Compound) ((Compound) l2).setContext (context);
            if (l2 instanceof MetaInf) ((MetaInf) l2).setContext (context);
        }
    }
    
    static class MetaInf extends Lookup {
        
        private static final String HIDDEN = "-hidden"; // NOI18N
        
        private String rootFolder;
        private HashMap registrationCache = new HashMap ();
        private HashMap instanceCache = new HashMap ();
        private Lookup context;

        
        MetaInf (String rootFolder) {
            this.rootFolder = rootFolder;
        }
        
        void setContext (Lookup context) {
            this.context = context;
        }
        
        List lookup (String folder, Class service, Set hidden) {
            List l = list (folder, service);
            
            Set s = new HashSet (l);
            
            ArrayList ll = new ArrayList ();
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                String className = (String) l.get (i);
                if (className.endsWith(HIDDEN)) continue;
                if (s.contains (className + HIDDEN)) continue;
                Object instance = null;
                instance = instanceCache.get (className);
                if (instance == null) {
                    instance = createInstance (className);
                    instanceCache.put (className, instance);
                }
                if (instance != null)
                    ll.add (instance);
            }
            return ll;
        }
        
        Set getHiddenItems (String folder, Class service) {
            Iterator i = list (folder, service).iterator ();
            Set h = new HashSet ();
            while (i.hasNext ()) {
                String s = (String) i.next ();
                if (!s.endsWith (HIDDEN)) continue;
                h.add (s.substring (0, s.length () - 7));
            }
            return h;
        }        
        
        private List list (String folder, Class service) {
            String name = service.getName ();
            String resourceName = "META-INF/debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") + 
                ((folder == null) ? "" : folder + "/") + 
                name;
            if (!registrationCache.containsKey (resourceName))
                registrationCache.put (resourceName, loadMetaInf (resourceName));
            return (List) registrationCache.get (resourceName);
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
    }
}
