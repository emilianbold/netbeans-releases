/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.sun.jersey.api.client.filter.ClientFilter;


/**
 *
 * @author jqian
 */
public class BeanIntrospector {

    public static List<HandlerParameter> getParameters(List<String> jarPaths,
            String className, boolean writable)
            throws MalformedURLException, ClassNotFoundException, IntrospectionException {

        ClassLoader classLoader = getURLClassLoader(jarPaths);
        Class clazz = classLoader.loadClass(className);

        return getParameters(clazz, writable);
    }

    public static List<HandlerParameter> getParameters(Class clazz, boolean writable)
            throws MalformedURLException, ClassNotFoundException,
            IntrospectionException {

        List<HandlerParameter> ret = new ArrayList<HandlerParameter>();

        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            String name = pd.getName();

            // Skip class property defined in Object.classs
            if (name.equals("class")) { // NOI18N
                continue;
            }

            if (!writable ||
                    writable && pd.getWriteMethod() != null) {
                String displayName = pd.getDisplayName();
                String shortDescription = pd.getShortDescription();

                HandlerParameter parameter = new HandlerParameter();
                parameter.setName(name);
                parameter.setDisplayName(displayName);
                parameter.setDescription(shortDescription);

                ret.add(parameter);
            }
        }

        return ret;
    }

    /**
     * Gets all the sub classes in the given jars that are extension of the
     * given base class.
     *
     * @param jarPaths          a list of jars
     * @param baseClass         base class/interface
     * @param includeBaseClass  whether to include the base class itself
     *                          if found in the given list of jars
     *
     * @return a list of classes that are extensions of the base class
     */
    public static List<Class> getSubClasses(List<String> jarPaths,
            Class baseClass, boolean includeBaseClass)
            throws IOException, ClassNotFoundException {

        List<Class> ret = new ArrayList<Class>();

        ClassLoader classLoader = getURLClassLoader(jarPaths);
        List<String> classNames = getClassNames(jarPaths);

        for (String className : classNames) {
            Class clazz = classLoader.loadClass(className);
            if (isDecendent(clazz, baseClass, includeBaseClass)) {
                ret.add(clazz);
            }
        }

        return ret;
    }

    static boolean isDecendent(Class clazz, Class baseClass, boolean includeBaseClass) {
        if (clazz == null) {
            return false;
        } else if (clazz.equals(baseClass)) {
            return includeBaseClass;
        } else {
            Class superClass = clazz.getSuperclass();
            if (isDecendent(superClass, baseClass, true)) {
                return true;
            }
            for (Class interface_ : clazz.getInterfaces()) {
                if (isDecendent(interface_, baseClass, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<String> getClassNames(List<String> jarPaths)
            throws IOException {

        List<String> ret = new ArrayList<String>();

        for (String jarPath : jarPaths) {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    if (entryName.endsWith(".class")) { // NOI18N
                        entryName = entryName.substring(0, entryName.length() - 6); // trim ".class"
                        entryName = entryName.replaceAll("/", "."); // NOI18N
                        ret.add(entryName);
                    }
                }
            }
        }

        return ret;
    }

    private static ClassLoader getURLClassLoader(List<String> jarPaths)
            throws MalformedURLException {

        List<URL> urls = new ArrayList<URL>();

        for (String jarPath : jarPaths) {
            URL url = new File(jarPath).toURL();
            urls.add(url);
        }

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[]{}),
                ClientFilter.class.getClassLoader());

        return classLoader;
    }

    public static List<Class> getSubClasses(List<String> jarPaths,
            String baseClassName, boolean includeBaseClass)
            throws IOException, ClassNotFoundException {

        List<String> baseClassNames = new ArrayList<String>();
        baseClassNames.add(baseClassName);

        return getSubClasses(jarPaths, baseClassNames, includeBaseClass);
    }

    public static List<Class> getSubClasses(List<String> jarPaths,
            List<String> baseClassNames, boolean includeBaseClass)
            throws IOException, ClassNotFoundException {

        List<Class> ret = new ArrayList<Class>();

        ClassLoader classLoader = getURLClassLoader(jarPaths);
        List<String> classNames = getClassNames(jarPaths);

        for (String className : classNames) {
            Class clazz = classLoader.loadClass(className);
            if (isDecendent(clazz, baseClassNames, includeBaseClass)) {
                ret.add(clazz);
            }
        }

        return ret;
    }

     static boolean isDecendent(Class clazz, List<String> baseClassNames,
             boolean includeBaseClass) {

        if (clazz == null) {
            return false;
        } else if (baseClassNames.contains(clazz.getName())) {
            return includeBaseClass;
        } else {
            Class superClass = clazz.getSuperclass();
            if (isDecendent(superClass, baseClassNames, true)) {
                return true;
            }
            for (Class interface_ : clazz.getInterfaces()) {
                if (isDecendent(interface_, baseClassNames, true)) {
                    return true;
                }
            }
        }

        return false;
    }
}
