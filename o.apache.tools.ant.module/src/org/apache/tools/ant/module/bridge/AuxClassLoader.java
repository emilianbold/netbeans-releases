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

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import org.openide.util.enum.*;

/**
 * Loads classes in the following order:
 * 1. JRE
 * 2. NetBeans JARs - modules etc.
 * 3. Ant JARs - whatever is in the "main" class loader.
 * 4. Contents of $nbhome/ant/nblib/*.jar, incl. bridge.jar and special tasks.
 * Lightly inspired by ProxyClassLoader, but much less complex.
 * @author Jesse Glick
 */
final class AuxClassLoader extends AntBridge.AllPermissionURLClassLoader {
    
    private final ClassLoader antLoader;
    
    public AuxClassLoader(ClassLoader nbLoader, ClassLoader antLoader, URL[] urls) {
        super(urls, nbLoader);
        this.antLoader = antLoader;
    }
    
    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return antLoader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            return super.findClass(name);
        }
    }
    
    public URL findResource(String name) {
        URL u = antLoader.getResource(name);
        if (u != null) {
            return u;
        } else {
            return super.findResource(name);
        }
    }
    
    public Enumeration findResources(String name) throws IOException {
        return new RemoveDuplicatesEnumeration(new SequenceEnumeration(antLoader.getResources(name), super.findResources(name)));
    }
    
    // XXX should maybe do something with packages... but oh well, it is rather hard.
    
}
