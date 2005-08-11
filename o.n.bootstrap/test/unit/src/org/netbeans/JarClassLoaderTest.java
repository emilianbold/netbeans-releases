/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans;

import junit.framework.*;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;
import java.io.*;
import java.net.MalformedURLException;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;

/** 
 *
 * @author Jaroslav Tulach
 */
public class JarClassLoaderTest extends TestCase {
    
    public JarClassLoaderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JarClassLoaderTest.class);
        
        return suite;
    }

    public void testTwoClassloadersLoadTheSameSealedPackage() throws Exception {
        String c = "org.openide.util.Cancellable";
        String g = "org.openide.util.ContextGlobalProvider";
        
        OneClassLoader cancel = new OneClassLoader (
            "cancel loader", getClass().getClassLoader(), getClass().getClassLoader().getParent(), c            
        );
        
        OneClassLoader global = new OneClassLoader (
            "global that delegates to cancel", getClass().getClassLoader(), cancel, g
        );

        assertNotNull("Loads the class", cancel.loadClass(c, true));
        assertNotNull("Loads global", global.loadClass(g, true));
        assertEquals("Right CL for cancel", cancel, cancel.loadClass(c, true).getClassLoader());
        assertEquals("Right CL for global", global, global.loadClass(g, true).getClassLoader());
        
        assertEquals("Loads the same cancel", cancel.loadClass(c, true), global.loadClass(c, true));
        
        
    }
    
    public void testTwoClassloadersLoadTheSameSealedPackageInReverseOrder() throws Exception {
        String c = "org.openide.util.Cancellable";
        String g = "org.openide.util.ContextGlobalProvider";
        
        OneClassLoader cancel = new OneClassLoader (
            "cancel loader", getClass().getClassLoader(), getClass().getClassLoader().getParent(), c            
        );
        
        OneClassLoader global = new OneClassLoader (
            "global that delegates to cancel", getClass().getClassLoader(), cancel, g
        );

        assertNotNull("Loads global", global.loadClass(g, true));
        assertEquals("Right CL for global", global, global.loadClass(g, true).getClassLoader());

        assertNotNull("Loads the class", cancel.loadClass(c, true));
        assertEquals("Right CL for cancel", cancel, cancel.loadClass(c, true).getClassLoader());
        
        assertEquals("Loads the same cancel", cancel.loadClass(c, true), global.loadClass(c, true));
        
        
    }

    public void testTwoClassloadersLoadTheSamePackageForClassesThatDependOnEachOther() throws Exception {
        doDependingClasses(true);
    }
    public void testTwoClassloadersLoadTheSamePackageForClassesThatDependOnEachOtherInReverseOrder() throws Exception {
        doDependingClasses(false);
    }
    
    private void doDependingClasses(boolean smallerFirst) throws Exception {
        Set above = new java.util.HashSet();
        String c = "org.openide.util.Task";
        above.add ("org.openide.util.Cancellable");
        above.add (c);
        
        String g = "org.openide.util.RequestProcessor$Task";
        
        OneClassLoader cancel = new OneClassLoader (
            "cancel loader", getClass().getClassLoader(), getClass().getClassLoader().getParent(), above
        );
        
        OneClassLoader global = new OneClassLoader (
            "global that delegates to cancel", getClass().getClassLoader(), cancel, g
        );
        
        if (smallerFirst) {
            assertNotNull("Loads the class", cancel.loadClass(c, true));
            assertEquals("Right CL for cancel", cancel, cancel.loadClass(c, true).getClassLoader());
        }
        

        assertNotNull("Loads global", global.loadClass(g, true));
        assertEquals("Right CL for global", global, global.loadClass(g, true).getClassLoader());

        assertEquals("Loads the same cancel", cancel.loadClass(c, true), global.loadClass(c, true));
    }
    
    /** Loads one class from the parent class loader, by itself.
     */
    public static class OneClassLoader extends ProxyClassLoader {
        /** set of Strings that we accept */
        private Set classes;
        /** is sealed */
        private boolean isSealed;
        /** classloader to load class */
        private ClassLoader loadClassLoader;
        /** name */
        private String name;
        
        public OneClassLoader(String name, ClassLoader l, String classname) {
            this(name, l, new ClassLoader[] { l }, classname);
        }
        
        public OneClassLoader(String name, ClassLoader l, ClassLoader l2, String classname) {
            this(name, l, new ClassLoader[] { l2 }, classname);
        }
        
        public OneClassLoader(String name, ClassLoader l, ClassLoader l2, Set names) {
            this(name, l, new ClassLoader[] { l2 }, names);
        }
        
        public OneClassLoader(String name, ClassLoader lc, ClassLoader[] arr, String classname) {
            this(name, lc, arr, java.util.Collections.singleton(classname));
        }
        public OneClassLoader(String name, ClassLoader lc, ClassLoader[] arr, java.util.Set names) {
            super(arr);
            classes = names;
            this.loadClassLoader = lc;
            this.name = name;
        }
    
        /** For our test all packages are special.
         */
        protected boolean isSpecialResource(String pkg) {
            return true;
        }

        /** Allows to specify the right permissions, OneModuleClassLoader does it differently.
         *
        protected PermissionCollection getPermissions( CodeSource cs ) {           
            return Policy.getPolicy().getPermissions(cs);       
        } */       


        protected Class simpleFindClass(String name, String path, String pkgnameSlashes) {
            if (classes.contains (name)) {
              try {
                java.net.URL u = loadClassLoader.getResource(name.replace('.', '/') + ".class");
                assertNotNull ("URL cannot be null", u);
                java.net.URLConnection c = u.openConnection();
                int l = c.getContentLength();
                byte[] data = new byte[l];
                int cnt = c.getInputStream().read (data);
                assertEquals ("Read the same as expected", l, cnt);
                
                // do the enhancing
                byte[] d = PatchByteCode.patch (data, name);
                data = d;

                int j = name.lastIndexOf('.');
                String pkgName = name.substring(0, j);
                // Note that we assume that if we are defining a class in this package,
                // we should also define the package! Thus recurse==false.
                // However special packages might be defined in a parent and then we want
                // to have the same Package object, proper sealing check, etc.; so be safe,
                // overhead is probably small (check in parents, nope, check super which
                // delegates to system loaders).
                Package pkg = getPackageFast(pkgName, pkgnameSlashes, isSpecialResource(pkgnameSlashes));
                if (pkg != null) {
                    // XXX full sealing check, URLClassLoader does something more
                    if (pkg.isSealed() && isSealed) throw new SecurityException("sealing violation"); // NOI18N
                } else {
                    definePackage(pkgName, null, null, null, null, null, null, null);
                }

                return defineClass (name, data, 0, data.length, null); //getProtectionDomain());
              } catch (IOException ex) {
                  ex.printStackTrace();
              }
            } 
            return null;
        }
        // look up the jars and return a resource based on a content of jars
        protected URL findResource(String name) {
            return null;
        }

        protected Enumeration simpleFindResources(String name) {
            return org.openide.util.Enumerations.empty();
        }
        
        public String toString() {
            return name;
        }
    }
}
