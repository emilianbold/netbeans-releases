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

package org.apache.tools.ant.module.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;

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
        try {
            return super.findClass(name);
        } catch (UnsupportedClassVersionError e) {
            // May be thrown during unit tests in case there is a JDK mixup.
            Exceptions.attachMessage(e, "loading: " + name);
            throw e;
        }
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
