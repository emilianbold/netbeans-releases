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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.j2ee14classloaderprovider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.netbeans.modules.visualweb.classloaderprovider.CommonClassloaderProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * This Common ClassLaoder provider simply return its ClassLoader which happens
 * to be the module ClassLoader. This module declares dependencies on the
 * modules that wrap the J2EE 1.4 platform libraries that are shared between the
 * IDE implementation and the user project. This provider is available through
 * the lookup.
 * <p>
 * The user project's meta data is be used to determine which J2EE platform it is using.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class J2EE14CommonClassloaderProvider implements CommonClassloaderProvider {
    private String[] designtimeJars = {
        // XXX Need to add these jars here so that META-INF/faces-config.xml files can be
        // discovered and loaded by the JSF1.2 RI. Ideally thin jars containing
        // the META-INF/faces-config.xml should be used. The classes should be loded
        // from the modules themselves.
        "jar:nbinst:///modules/org-netbeans-modules-visualweb-jsfsupport-designtime.jar!/", // NOI18N
        "jar:nbinst:///modules/org-netbeans-modules-visualweb-jsfsupport-designtime_1_1.jar!/", // NOI18N
        "jar:nbinst:///modules/org-netbeans-modules-visualweb-webui-designtime.jar!/", // NOI18N
        // The following two jars are here for special handling of JSF1.1 Standard components
        // and renderkits
        "jar:nbinst:///modules/ext/jsf-1_2/jsf-api.jar!/", // NOI18N
        "jar:nbinst:///modules/ext/jsf-1_2/jsf-impl.jar!/", // NOI18N
    };

    private URLClassLoader urlClassLoader;

    public J2EE14CommonClassloaderProvider() {
    }

    public ClassLoader getClassLoader() {
        synchronized  (this) {
            if (urlClassLoader == null) {
                List normalizedUrls = new ArrayList();

                for (int i = 0; i < designtimeJars.length; i++) {
                    try {
                        URL url = new URL(designtimeJars[i]);
                        FileObject fileObject = URLMapper.findFileObject(url);

                        //file inside library is broken
                        if (fileObject == null)
                            continue;

                        if ("jar".equals(url.getProtocol())) {  //NOI18N
                            fileObject = FileUtil.getArchiveFile(fileObject);
                        }
                        File f = FileUtil.toFile(fileObject);
                        if (f != null) {

                            URL entry = f.toURI().toURL();
                            if (FileUtil.isArchiveFile(entry)) {
                                entry = FileUtil.getArchiveRoot(entry);
                            } else if (!f.exists()) {
                                // if file does not exist (e.g. build/classes folder
                                // was not created yet) then corresponding File will
                                // not be ended with slash. Fix that.
                                assert !entry.toExternalForm().endsWith("/") : f; // NOI18N
                                entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                            }
                            normalizedUrls.add(entry);

                        }
                    } catch (MalformedURLException mue) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                    }
                }
                urlClassLoader = new JSF11SupportClassLoader((URL[]) normalizedUrls.toArray(new URL[0]), getClass().getClassLoader());
            }
        }
        return urlClassLoader;
    }
    
    public boolean isCapableOf(Properties capabilities) {
        if (J2EE_1_3.equals(capabilities.getProperty(J2EE_PLATFORM)) ||
            J2EE_1_4.equals(capabilities.getProperty(J2EE_PLATFORM))) {
            return true;
        }
        return false;
    }
    
    // XXX To be able to distinguish our specific project classloader during debugging.
    private static class JSF11SupportClassLoader extends URLClassLoader {
        private final URL[] urls;
        
        public JSF11SupportClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.urls = urls;
        }
        
        public String toString() {
            return super.toString() + "[urls=" + (urls == null ? null : Arrays.asList(urls)) + "]"; // NOI18N
        }
        
        // XXX HACK Support JSF 1.1 components and bean infos
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // First, check if the class has already been loaded
            Class c = findLoadedClass(name);
            if (c == null) {
                // Now check if this request is for special packages or name
                String packageName = getPackageName(name);
                if (packageName != null) {
                    if (packageName.equals("javax.faces.component.html") || // NOI18N
                        packageName.equals("com.sun.faces.renderkit.html_basic") || // NOI18N
                        packageName.equals("org.netbeans.modules.visualweb.faces.dt.component") || // NOI18N
                        packageName.equals("org.netbeans.modules.visualweb.faces.dt.component.html") || // NOI18N
                        packageName.equals("org.netbeans.modules.visualweb.faces.dt_1_1.component") || // NOI18N
                        packageName.equals("org.netbeans.modules.visualweb.faces.dt_1_1.component.html") || // NOI18N
                        name.equals("com.sun.faces.util.Util")) { // NOI18N
                        // find the class locally
                        c = findClass(name);
                        if (c != null) {
                            if (resolve) {
                                resolveClass(c);
                            }
                            return c;
                        }
                    }
                }
            }
           
            return super.loadClass(name, resolve);
        }
        
        private static String getPackageName(String name) {
            if (name == null) {
                return null;
            }
            
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex != -1) {
                return name.substring(0, lastDotIndex);
            }
            return "";
        }
    }
}
