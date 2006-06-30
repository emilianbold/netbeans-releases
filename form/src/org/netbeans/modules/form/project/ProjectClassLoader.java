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

package org.netbeans.modules.form.project;

import java.io.InputStream;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * A class loader loading user classes from given project (execution classpath
 * is used) with special care given to resources. When finding a resource, the
 * the project's sources are tried first (before execution classpath) to allow
 * components added to a form in this project to access resources without need
 * to build the project first. Even if built, the resources in sources take
 * precedence as they are likely more up-to-date.
 *
 * @author Tomas Pavek
 */

class ProjectClassLoader extends ClassLoader {

    private ClassLoader projectClassLoaderDelegate;
    private ClassPath sources;
    private ClassLoader systemClassLoader;

    private ProjectClassLoader(ClassLoader projectClassLoaderDelegate, ClassPath sources) {
        this.projectClassLoaderDelegate = projectClassLoaderDelegate;
        this.sources = sources;
        this.systemClassLoader = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
    }

    static ClassLoader getUpToDateClassLoader(FileObject fileInProject, ClassLoader clSoFar) {
        ClassLoader existingCL = clSoFar instanceof ProjectClassLoader ?
                ((ProjectClassLoader)clSoFar).projectClassLoaderDelegate : clSoFar;
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        ClassLoader actualCL = classPath != null ? classPath.getClassLoader(true) : null;
        if (actualCL == existingCL)
            return clSoFar;
        if (actualCL == null)
            return null;
        return new ProjectClassLoader(actualCL, ClassPath.getClassPath(fileInProject, ClassPath.SOURCE));
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("org.apache.commons.logging.")) { // NOI18N HACK: Issue 50642
            try {
                return systemClassLoader.loadClass(name);
            } catch (ClassNotFoundException cnfex) {
                // The logging classes are not in the IDE, we can use ProjectClassLoader
            }
        }
        Class c = null;
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = projectClassLoaderDelegate.getResource(filename);
        if (url != null) {
            try {
                InputStream is = url.openStream();
                byte[] data = null;
                int first;
                int available = is.available();
                while ((first = is.read()) != -1) {
                    int length = is.available();
                    if (length != available) { // Workaround for issue 4401122
                        length++;
                    }
                    byte[] b = new byte[length];
                    b[0] = (byte) first;
                    int count = 1;
                    while (count < length) {
                        int read = is.read(b, count, length - count);
                        assert (read != -1);
                        count += read;
                    }
                    if (data == null) {
                        data = b;
                    }
                    else {
                        byte[] temp = new byte[data.length + count];
                        System.arraycopy(data, 0, temp, 0, data.length);
                        System.arraycopy(b, 0, temp, data.length, count);
                        data = temp;
                    }
                }
                int dot = name.lastIndexOf('.');
                if (dot != -1) { // Is there anything we should do for the default package?
                    String packageName = name.substring(0, dot);
                    Package pakcage = getPackage(packageName);
                    if (pakcage == null) {
                        // PENDING are we able to determine the attributes somehow?
                        definePackage(packageName, null, null, null, null, null, null, null);
                    }
                }
                c = defineClass(name, data, 0, data.length);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        else if (ClassPathUtils.getClassLoadingType(name) == ClassPathUtils.SYSTEM_CLASS) {
            // fallback to system classloader for indirectly loaded classes
            // e.g. if a bean uses GroupLayout then supply it automatically
            c = systemClassLoader.loadClass(name);
        }
        if (c == null)
            throw new ClassNotFoundException(name);
        return c;
    }

    protected URL findResource(String name) {
        FileObject fo = sources.findResource(name);
        if (fo != null) {
            try {
                return fo.getURL();
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return projectClassLoaderDelegate.getResource(name);
    }
}
