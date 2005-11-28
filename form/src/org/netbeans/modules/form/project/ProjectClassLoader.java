/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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

    private ProjectClassLoader(ClassLoader projectClassLoaderDelegate, ClassPath sources) {
        this.projectClassLoaderDelegate = projectClassLoaderDelegate;
        this.sources = sources;
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
        Class c = null;
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = projectClassLoaderDelegate.getResource(filename);
        if (url != null) {
            try {
                InputStream is = url.openStream();
                byte[] data = null;
                int first;
                while ((first = is.read()) != -1) {
                    int length = is.available();
                    byte[] b = new byte[length + 1];
                    b[0] = (byte) first;
                    int count = 1;
                    while (count < length) {
                        count += is.read(b, count, length - count);
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
                c = defineClass(name, data, 0, data.length);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        if (c == null)
            throw new ClassNotFoundException();
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
