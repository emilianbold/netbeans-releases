/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.clazz;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.netbeans.modules.xml.reference.ReferenceUtil;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.07.10
 */
public final class ClazzLoader extends ClassLoader {

    List<ClazzElement> getMethods(List<File> files) {
//out();
//out("files: " + files.size());
        List<ClazzElement> methods = new ArrayList<ClazzElement>();

        for (File file : files) {
//out("  see: " + file);
            methods.addAll(getMethods(file));
        }
//out("  ALL: " + methods.size());
//out();
        return methods;
    }

    public List<Class<?>> getExceptions(List<File> files) {
//out();
//out("files: " + files.size());
        List<Class<?>> exceptions = new ArrayList<Class<?>>();

        for (File file : files) {
//out("  see: " + file);
            exceptions.addAll(getExceptions(file));
        }
//out("  ALL: " + exceptions.size());
//out();
        return exceptions;
    }

    private List<Class<?>> getExceptions(File file) {
        List<Class<?>> classes = loadClasses(file);
        List<Class<?>> exceptions = new ArrayList<Class<?>>();
//out();
//out("GET EXCEPTIONS from classes: " + classes.size());
        for (Class<?> clazz : classes) {
//out("   see class: " + clazz.getName());

            if (Exception.class.isAssignableFrom(clazz)) {
                exceptions.add(clazz);
            }
        }
        return exceptions;
    }

    private List<Class<?>> loadClasses(File file) {
//out();
//out("file: " + file);
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<String> noDefFoundClassName = new ArrayList<String>();
        List<byte[]> noDefFoundClassData = new ArrayList<byte[]>();

        try {           
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String jarEntryName = jarEntry.getName();

                if ( !jarEntryName.endsWith(DOT_CLASS)) {
                    continue;
                }
                byte[] classData = loadClassData(jarFile, jarEntry);                   

                if (classData == null) {
                    continue;
                }
//out();
                String className = normalizeClassName(jarEntryName);
                Class<?> clazz = null;
//out("name: " + className);
//out();
                try {
                    clazz = defineClass(className, classData, 0, classData.length);
//out("defined: " + clazz);
                }
                catch (ClassFormatError e) {
                    continue;
                }
                catch (NoClassDefFoundError e) {
//out("Again: " + className);
                    noDefFoundClassName.add(className);
                    noDefFoundClassData.add(classData);
                }
                catch (LinkageError e) {
//out("linkage: " + e.getMessage());
                    try {
                        clazz = loadClass(className);
                    }
                    catch (ClassNotFoundException ee) {
                        e.printStackTrace();
//out("ClassNotFoundException: " + ee.getMessage());
                    }
                }
//out("ADD: " + clazz);
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        loadClasses(classes, noDefFoundClassName, noDefFoundClassData);

        return classes;
    }

    private List<ClazzElement> getMethods(File file) {
        List<Class<?>> classes = loadClasses(file);
        return getMethods(classes.toArray(new Class[classes.size()]), createFile(file));
    }

    private void loadClasses(List<Class<?>> classes, List<String> names, List<byte[]> datas) {
        if (names.isEmpty()) {
            return;
        }
        List<String> noDefFoundClassName = new ArrayList<String>();
        List<byte[]> noDefFoundClassData = new ArrayList<byte[]>();

        for (int i=0; i < names.size(); i++) {
            String name = names.get(i);
            byte[] data = datas.get(i);

            try {
                Class<?> clazz = defineClass(name, data, 0, data.length);
                classes.add(clazz);
            }
            catch (ClassFormatError e) {
                continue;
            }
            catch (NoClassDefFoundError e) {
//out("again: " + name);
                if ( !names.contains(name)) {
                    noDefFoundClassName.add(name);
                    noDefFoundClassData.add(data);
                }
            }
        }
        loadClasses(classes, noDefFoundClassName, noDefFoundClassData);
    }

    private List<ClazzElement> getMethods(Class<?>[] classes, ClazzElement clazzFile) {
        List<ClazzElement> elements = new ArrayList<ClazzElement>();

        Arrays.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> class1, Class<?> class2) {
                return class1.getName().compareTo(class2.getName());
            }
        });
  
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isInterface()) {
                continue;
            }
//out();
//out("class: " + clazz.getName());
            Method[] methods;
            boolean hasDefaultConstructor;

            try {
                methods = clazz.getMethods();
                hasDefaultConstructor = hasDefaultConstructor(clazz);
            }
            catch (NoClassDefFoundError e) {
                continue;
            }
            Arrays.sort(methods, new Comparator<Method>() {
                public int compare(Method method1, Method method2) {
                    return method1.getName().compareTo(method2.getName());
                }
            });
            ClazzElement clazzElement = createClazz(clazz, clazzFile);

            for (Method method : methods) {
                int modifiers = method.getModifiers();

                if ( !hasDefaultConstructor && (modifiers & Modifier.STATIC) == 0) {
                    continue;
                }
                if ((modifiers & Modifier.PUBLIC) == 0) {
                    continue;
                }
                if ( !checkReturnType(method.getReturnType())) {
                    continue;
                }
                if ( !checkParameterTypes(method)) {
                    continue;
                }
                elements.add(createMethod(method, clazzElement));
            }
        }
        return elements;
    }

    private boolean hasDefaultConstructor(Class<?> clazz) {
        Constructor[] constructors = clazz.getConstructors();

        for (Constructor constructor : constructors) {
            if (isDefaultConstructor(constructor)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDefaultConstructor(Constructor constructor) {
        return constructor.getGenericParameterTypes().length == 0;
    }

    private boolean checkParameterTypes(Method method) {
        Class<?>[] clazzes = method.getParameterTypes();

        for (Class<?> clazz : clazzes) {
            if ( !checkParameterType(clazz)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkReturnType(Class<?> clazz) {
        return
            clazz.isAssignableFrom(String.class) ||
            clazz.isAssignableFrom(Boolean.class) ||
            clazz.isAssignableFrom(Number.class) ||
            clazz.isAssignableFrom(org.w3c.dom.Node.class) ||
            clazz.isAssignableFrom(javax.security.auth.Subject.class);
    }

    private boolean checkParameterType(Class<?> clazz) {
        return true;
    }

    private ClazzElement createMethod(Method method, ClazzElement clazz) {
        return new ClazzElement(method, getName(method), icon(ClazzLoader.class, "method"), clazz); // NOI18N
    }

    private ClazzElement createClazz(Class<?> clazz, ClazzElement file) {
        return new ClazzElement(clazz, getName(clazz), icon(ClazzLoader.class, "clazz"), file); // NOI18N
    }

    private ClazzElement createFile(File file) {
        return new ClazzElement(file, getName(file), icon(ClazzLoader.class, "file"), null); // NOI18N
    }

    private String getName(Method method) {
        StringBuffer buffer = new StringBuffer();
        int modifiers = method.getModifiers();
    
        if (modifiers != 0) {
            buffer.append(Modifier.toString(modifiers) + " "); // NOI18N
        }
        buffer.append(method.getName() + "("); // NOI18N
        Class<?>[] params = method.getParameterTypes();

        for (int i=0; i < params.length; i++) {
            buffer.append(getName(params[i]));

            if (i < params.length - 1) {
                buffer.append(", "); // NOI18N
            }
        }
        buffer.append(")"); // NOI18N
        buffer.append(": " + getName(method.getReturnType())); // NOI18N

        return buffer.toString();
    }

    private String getName(Class<?> clazz) {
        String name = clazz.getName();

        if ( !name.startsWith(JAVA_LANG)) {
            return name;
        }
        return name.substring(JAVA_LANG.length());
    }

    private String getName(File file) {
        return ReferenceUtil.getDisplayName(file);
    }

    private String normalizeClassName(String name) {
        return name.replace('/', '.').substring(0, name.length() - DOT_CLASS.length());
    }

    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();     

        if (size <= 0) {
            return null;
        }
        byte[] data = new byte[(int) size];
        jarFile.getInputStream(jarEntry).read(data);

        return data;
    }

    private static final String DOT_CLASS = ".class"; // NOI18N
    private static final String JAVA_LANG = "java.lang."; // NOI18N
}
