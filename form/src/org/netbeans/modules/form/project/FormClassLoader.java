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
import org.openide.ErrorManager;

/**
 * A special classloader capable to combine system classpath (IDE modules) and
 * user project classpath into one. Classes loaded by this classloader can link
 * with module classes running in the IDE and access resources on project
 * classpath at the same time.
 *
 * @author Tomas Pavek
 */

final class FormClassLoader extends ClassLoader {

    private ClassLoader systemClassLoader;
    private ClassLoader projectClassLoader;

    FormClassLoader(ClassLoader projectClassLoader) {
        this.systemClassLoader = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
        this.projectClassLoader = projectClassLoader;
    }

    ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        int type = ClassPathUtils.getClassLoadingType(name);
        if (type == ClassPathUtils.UNSPECIFIED_CLASS) {
            if (projectClassLoader == null)
                throw new ClassNotFoundException(ClassPathUtils.getBundleString("MSG_NullClassPath")); // NOI18N
            return projectClassLoader.loadClass(name);
        }
        if (type == ClassPathUtils.SYSTEM_CLASS)
            return systemClassLoader.loadClass(name);
        // otherwise type == ClassPathUtils.SYSTEM_CLASS_WITH_PROJECT

        Class c = null;
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = systemClassLoader.getResource(filename);
        if (url == null && projectClassLoader != null)
            url = projectClassLoader.getResource(filename);
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
        if (c == null)
            throw new ClassNotFoundException(name);

        return c;
    }

    public URL getResource(String name) {
        URL url = projectClassLoader != null ? projectClassLoader.getResource(name) : null;
        if (url == null)
            url = systemClassLoader.getResource(name);
        return url;
    }
}
