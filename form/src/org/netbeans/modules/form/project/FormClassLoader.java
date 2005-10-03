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

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        Class c = loadClassImpl(name);

        if (resolve)
            resolveClass(c);

        return c;
    }

    private Class loadClassImpl(String name) throws ClassNotFoundException {
        try {
            return findSystemClass(name);
        }
        catch (ClassNotFoundException ex) {}

        int type = ClassPathUtils.getClassLoadingType(name);
        if (type == ClassPathUtils.UNSPECIFIED_CLASS)
            return projectClassLoader.loadClass(name);
        if (type == ClassPathUtils.SYSTEM_CLASS)
            return systemClassLoader.loadClass(name);
        // otherwise type == ClassPathUtils.SYSTEM_CLASS_WITH_PROJECT

        Class c = findLoadedClass(name);
        if (c != null)
            return c;

        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = systemClassLoader.getResource(filename);
        if (url == null)
            url = projectClassLoader.getResource(filename);
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
                System.out.println("define: "+name);
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

    public URL getResource(String name) {
        URL url = projectClassLoader.getResource(name);
        if (url == null)
            url = systemClassLoader.getResource(name);
        return url;
    }

//    protected URL findResource(String name) {
//        System.out.println("findResource: "+name);
//        return super.findResource(name);
//    }
//
//    protected Enumeration findResources(String name) throws IOException {
//        System.out.println("findResources: "+name);
//        return super.findResources(name);
//    }
//
//    protected Package getPackage(String name) {
//        System.out.println("getPackage: "+name);
//        return super.getPackage(name);
//    }
//
//    protected Package[] getPackages() {
//        System.out.println("getPackages");
//        return super.getPackages();
//    }
}
