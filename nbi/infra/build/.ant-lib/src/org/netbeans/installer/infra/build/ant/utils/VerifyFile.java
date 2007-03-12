/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.infra.build.ant.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class verifies the given jar file (first command-line argument) wrt its
 * validitity, i.e. checks that all classes are loadable. It browses the jar entries
 * and if it's a class, tries to load it. Mostly it watches for
 * <code>ClassFormatError</code>s and ignores several expected exceptions.
 *
 * It does not provide any means to validate the input data, since it's expected to
 * be called exclusively from from <code>Package</code>.
 *
 * The success/failure is reported via exitcode, 0 means success, 1 - failure.
 *
 * @see org.netbeans.installer.infra.build.ant.Package
 *
 * @author Kirill Sorokin
 */
public class VerifyFile {
    /**
     * The main method.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        File file = new File(args[0]);
        
        try {
            JarFile jar = new JarFile(file);
            URLClassLoader loader = 
                    new URLClassLoader(new URL[]{file.toURI().toURL()});
            
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                if (getClassName(entry) != null) {
                    try {
                        System.out.println(
                                "loading class " + getClassName(entry));    // NOI18N
                        loader.loadClass(getClassName(entry));
                    } catch (NoClassDefFoundError e) {
                        // do nothing; this is OK - classpath issues
                    } catch (IllegalAccessError e) {
                        // do nothing; this is also somewhat OK, since we do not 
                        // define any security policies
                    }
                }
            }
            
            jar.close();
            
            System.exit(0);
        } catch (Throwable e) { 
            // we need to catch everything here in order to not 
            // allow unexpected exceptions to pass through
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Converts a jar entry to a class name.
     * 
     * @param entry <code>JarEntry</code> to process.
     * @return The classname of the jar entry or <code>null</code> if it cannot be 
     *      devised.
     */
    private static String getClassName(JarEntry entry) {
        final String name = entry.getName();
        
        if (name.endsWith(".class")) {                                      // NOI18N
            final String className = 
                    name.substring(0, name.length() - 6).replace('/', '.'); // NOMAGI
            if (className.matches(
                    "([a-zA-Z][a-zA-Z0-9_]+\\.)+[a-zA-Z][a-zA-Z0-9_]+")) {  // NOI18N
                return className;
            } else {
                return null;
            }
        }
        
        return null;
    }
}
