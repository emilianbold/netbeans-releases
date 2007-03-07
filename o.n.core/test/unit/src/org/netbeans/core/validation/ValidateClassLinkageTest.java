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

package org.netbeans.core.validation;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

/**
 * Test that all classes in the system can load and link.
 * Since the default 64m is not enough to load all IDE classes, run with:
 * ant -f core/test/build.xml -Dxtest.attribs=emptyide -Dxtest.includes=org/netbeans/core/ValidateClassLinkageTest.class -Dxtest.ide.jvmargs=-XX:MaxPermSize=128m
 * @author Jesse Glick
 */
public class ValidateClassLinkageTest extends NbTestCase {
    
    public ValidateClassLinkageTest(String name) {
        super(name);
    }
    
    /**
     * Try to load every class we can find.
     * @see org.netbeans.core.modules.NbInstaller#preresolveClasses
     */
    public void testClassLinkage() throws Exception {
        if (ValidateClassLinkageTest.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
            // do not check anything as this probably means we are running
            // plain Unit test and not inside the IDE mode
            return;
        }
        
        
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        assertNotNull("Context CL has some autoloads in it", l.getResource("org/openide/windows/InputOutput.class"));
        Enumeration e = l.getResources("META-INF/MANIFEST.MF");
        Set/*<File>*/ jars = new TreeSet();
        while (e.hasMoreElements()) {
            URL manifest = (URL)e.nextElement();
            String murl = manifest.toExternalForm();
            assertTrue(murl.endsWith("/META-INF/MANIFEST.MF"));
            if (murl.startsWith("jar:")) {
                assertTrue(murl.endsWith("!/META-INF/MANIFEST.MF"));
                String jarfileurl = murl.substring(4, murl.length() - "!/META-INF/MANIFEST.MF".length());
                assertTrue(jarfileurl.startsWith("file:/"));
                assertTrue(jarfileurl.endsWith(".jar"));
                if (jarfileurl.indexOf("/jre/lib/") != -1) {
                    System.err.println("Skipping " + jarfileurl);
                    continue;
                }
                File f = new File(new URI(jarfileurl));
                jars.add(f);
            }
        }
        Map/*<String,Throwable>*/ errorsByClazz = new TreeMap();
        Map/*<String,File>*/ locationsByClass = new HashMap();
        Iterator it = jars.iterator();
        while (it.hasNext()) {
            File jar = (File)it.next();
            System.err.println("Checking JAR: " + jar);
            JarFile jarfile = new JarFile(jar);
            try {
                e = jarfile.entries();
                while (e.hasMoreElements()) {
                    JarEntry entry = (JarEntry)e.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        String clazz = name.substring(0, name.length() - 6).replace('/', '.');
                        if (clazz.startsWith("org.netbeans.xtest.")) {
                            // Skip these; a lot seem to want to link against Ant. Test-time only anyway.
                            continue;
                        }
                        if (clazz.startsWith("javax.help.tagext.")) {
                            // Servlet part of JavaHelp, which we don't use. Ignore.
                            continue;
                        }
                        //System.err.println("class: " + clazz);
                        Throwable t = null;
                        try {
                            Class.forName(clazz, false, l);
                        } catch (ClassNotFoundException cnfe) {
                            t = cnfe;
                        } catch (LinkageError le) {
                            t = le;
                        } catch (RuntimeException re) { // e.g. IllegalArgumentException from package defs
                            t = re;
                        }
                        if (t != null) {
                            errorsByClazz.put(clazz, t);
                            locationsByClass.put(clazz, jar);
                        }
                    }
                }
            } finally {
                jarfile.close();
            }
        }
        if (!errorsByClazz.isEmpty()) {
            it = errorsByClazz.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String clazz = (String)entry.getKey();
                Throwable t = (Throwable)entry.getValue();
                // Will go the logs:
                System.err.println("From " + clazz + " in " + locationsByClass.get(clazz) + ":");
                t.printStackTrace();
            }
            fail("Linkage or class loading errors encountered in " + errorsByClazz.keySet() + " (see logs for details)");
        }
    }
    
}
