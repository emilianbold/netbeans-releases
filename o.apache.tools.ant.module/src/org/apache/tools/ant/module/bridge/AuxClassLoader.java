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

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.util.Enumerations;

/**
 * Loads classes in the following order:
 * 1. JRE (well, actually app loader, but minus org.apache.tools.** and org.netbeans.**)
 * 2. Ant JARs - whatever is in the "main" class loader.
 * 3. Some NetBeans module class loader.
 * 4. Some other JAR from $nbhome/ant/nblib/*.jar.
 * Used for two cases:
 * A. bridge.jar for #4 and the Ant module for #3.
 * B. ant/nblib/o-n-m-foo.jar for #4 and modules/o-n-m-foo.jar for #3.
 * Lightly inspired by ProxyClassLoader, but much less complex.
 * @author Jesse Glick
 */
final class AuxClassLoader extends AntBridge.AllPermissionURLClassLoader {
    
    private static boolean masked(String name) {
        return name.startsWith("org.apache.tools.") && !name.startsWith("org.apache.tools.ant.module."); // NOI18N
    }
    
    private final ClassLoader nbLoader;
    
    public AuxClassLoader(ClassLoader nbLoader, ClassLoader antLoader, URL extraJar) {
        super(new URL[] {extraJar}, antLoader);
        this.nbLoader = nbLoader;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!masked(name)) {
            try {
                return nbLoader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // OK, didn't find it.
            }
        }
        return super.findClass(name);
    }
    
    @Override
    public URL findResource(String name) {
        if (!masked(name)) {
            URL u = nbLoader.getResource(name);
            if (u != null) {
                return u;
            }
        }
        return super.findResource(name);
    }
    
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        // XXX probably wrong now... try to fix somehow
        return Enumerations.removeDuplicates (
            Enumerations.concat (
                nbLoader.getResources(name), 
                super.findResources(name)
            )
        );
    }
    
    // XXX should maybe do something with packages... but oh well, it is rather hard.
    
}
