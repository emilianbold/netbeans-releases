/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.util.enum.*;

/**
 * Loads classes in the following order:
 * 1. JRE
 * 2. Ant JARs - whatever is in the "main" class loader.
 * 3. Some NetBeans module class loader.
 * 4. Some other JAR from $nbhome/ant/nblib/*.jar.
 * Lightly inspired by ProxyClassLoader, but much less complex.
 * @author Jesse Glick
 */
final class AuxClassLoader extends AntBridge.AllPermissionURLClassLoader {
    
    private final ClassLoader nbLoader;
    
    public AuxClassLoader(ClassLoader nbLoader, ClassLoader antLoader, URL extraJar) {
        super(new URL[] {extraJar}, antLoader);
        this.nbLoader = nbLoader;
    }
    
    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return nbLoader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            return super.findClass(name);
        }
    }
    
    public URL findResource(String name) {
        URL u = nbLoader.getResource(name);
        if (u != null) {
            return u;
        } else {
            return super.findResource(name);
        }
    }
    
    public Enumeration findResources(String name) throws IOException {
        return new RemoveDuplicatesEnumeration(new SequenceEnumeration(nbLoader.getResources(name), super.findResources(name)));
    }
    
    // XXX should maybe do something with packages... but oh well, it is rather hard.
    
}
