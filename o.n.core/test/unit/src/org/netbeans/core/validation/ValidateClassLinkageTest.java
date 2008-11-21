/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
