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
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author   Jan Jancura
 */
abstract class Lookup {
    
    abstract List lookup (String folder, Class service);
    abstract Object lookupFirst (String folder, Class service);
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.registration") != null;
    
    
    static class Instance extends Lookup {
        private Object[] services;
        
        Instance (Object[] services) {
            this.services = services;
        }
        
        List lookup (String folder, Class service) {
//            if (verbose)
//                System.out.println("\nR lookup instance " + service);
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
        
        Object lookupFirst (String folder, Class service) {
//            if (verbose)
//                System.out.println("\nR lookup instance " + service);
            int i, k = services.length;
            for (i = 0; i < k; i++)
                if (service.isAssignableFrom (services [i].getClass ())) {
                    if (verbose)
                        System.out.println("\nR  instance " + services [i] + 
                            " found");
                    return services [i];
                }
            return null;
        }
    }
    
    static class Compound extends Lookup {
        private Lookup l1;
        private Lookup l2;
        
        Compound (Lookup l1, Lookup l2) {
            this.l1 = l1;
            this.l2 = l2;
        }
        
        List lookup (String folder, Class service) {
            ArrayList l = new ArrayList ();
            l.addAll (l1.lookup (folder, service));
            l.addAll (l2.lookup (folder, service));
            return l;
        }
        
        Object lookupFirst (String folder, Class service) {
            Object o = l1.lookupFirst (folder, service);
            if (o != null) return o;
            return l2.lookupFirst (folder, service);
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
        
        List lookup (String folder, Class service) {
            String name = service.getName ();
            String resourceName = "META-INF/debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") + 
                ((folder == null) ? "" : folder + "/") + 
                name;
            if (!registrationCache.containsKey (resourceName))
                registrationCache.put (resourceName, loadMetaInf (resourceName));
            List l = (List) registrationCache.get (resourceName);
            
            ArrayList ll = new ArrayList ();
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                String className = (String) l.get (i);
                Object instance = null;
                instance = instanceCache.get (className);
//                if (instance != null)
//                    instance = ((WeakReference) instance).get ();
                if (instance == null) {
                    instance = createInstance (className);
                    instanceCache.put (className, instance);//new WeakReference (instance));
                }
                if (instance != null)
                    ll.add (instance);
            }
            return ll;
        }
        
        Object lookupFirst (String folder, Class service) {
            List l = lookup (folder, service);
            if (l.size () < 1) return null;
            return l.get (0);
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
                String v = "\nR lookup " + resourceName;
                ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
                Enumeration e = cl.getResources (resourceName);
                while (e.hasMoreElements ()) {
                    URL url = (URL) e.nextElement();
                    //S ystem.out.println("  url: " + url);
                    InputStream is = url.openStream ();
                    //S ystem.out.println("  is: " + is);
                    if (is == null) continue;
                    BufferedReader br = new BufferedReader (
                        new InputStreamReader (is)
                    );
                    String s = br.readLine ().trim ();
                    while (s != null) {
                        if (s.startsWith ("#")) continue;
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
