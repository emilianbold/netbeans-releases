/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 *
 * @author   Jan Jancura
 */
abstract class Lookup {
    
    abstract List lookup (String folder, Class service, Set hidden);
    abstract Set getHiddenItems (String folder, Class service);
    Object lookupFirst (String folder, Class service) {
        List l = lookup (folder, service);
        if (l.isEmpty ()) return null;
        return l.get (0);
    }
    List lookup (String folder, Class service) {
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
    }
    
    static class MetaInf extends Lookup {
        
        private Object context;
        private String rootFolder;
        private HashMap registrationCache = new HashMap ();
        private HashMap instanceCache = new HashMap ();

        
        MetaInf (String rootFolder, Object context) {
            this.context = context;
            this.rootFolder = rootFolder;
        }
        
        List lookup (String folder, Class service, Set hidden) {
            List l = list (folder, service);
            
            Set s = new HashSet (l);
            
            ArrayList ll = new ArrayList ();
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                String className = (String) l.get (i);
                if (s.contains (className + "-hidden")) continue;
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
                if (!s.endsWith ("-hidden")) continue;
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
                ClassLoader cl = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                String v = "\nR lookup " + resourceName;
                Enumeration e = cl.getResources (resourceName);
                while (e.hasMoreElements ()) {
                    URL url = (URL) e.nextElement();
                    InputStream is = url.openStream ();
                    if (is == null) continue;
                    BufferedReader br = new BufferedReader (
                        new InputStreamReader (is)
                    );
                    String s = br.readLine ();
                    while (s != null) {
                        if (s.startsWith ("#")) continue;
                        if (s.length () == 0) continue;
                        if (verbose)
                            v += "\nR  service " + s + " found";

                        l.add (s);
                        s = br.readLine ();
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
                ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
                Class cls = cl.loadClass (service);

                Object o = null;
                if (context != null) {
                    Constructor[] cs = cls.getConstructors ();
                    int i, k = cs.length;
                    for (i = 0; i < k; i++) {
                        Constructor c = cs [i];
                        if (c.getParameterTypes ().length != 1) continue;
                        try {
                            o = c.newInstance (new Object[] {context});
                        } catch (IllegalAccessException e) {
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
                System.out.println("\nservice: " + service);
                System.out.println("context: " + context);
                e.printStackTrace ();
            } catch (InstantiationException e) {
                System.out.println("\nservice: " + service);
                System.out.println("context: " + context);
                e.printStackTrace ();
            } catch (IllegalAccessException e) {
                System.out.println("\nservice: " + service);
                System.out.println("context: " + context);
                e.printStackTrace ();
            } catch (InvocationTargetException ex) {
                System.out.println("\nservice: " + service);
                System.out.println("context: " + context);
                ex.getCause ().printStackTrace ();
            }
            return null;
        }
    }
}
