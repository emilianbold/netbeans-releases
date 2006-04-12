/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;

/**
 * Basic impl in case no other providers can be found.
 * @author Jesse Glick
 */
public class FallbackDefaultJavaPlatform extends JavaPlatform {

    public FallbackDefaultJavaPlatform() {
        setSystemProperties(System.getProperties());
    }

    public String getDisplayName() {
        return System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.version"); // NOI18N
    }

    public Map getProperties() {
        return Collections.emptyMap();
    }

    private static ClassPath sysProp2CP(String propname) {
        String sbcp = System.getProperty(propname);
        if (sbcp == null) {
            return null;
        }
        List/*<URL>*/ roots = new ArrayList();
        StringTokenizer tok = new StringTokenizer(sbcp, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            File f = new File(tok.nextToken());
            if (!f.exists()) {
                continue;
            }
            URL u;
            try {
                u = f.toURI().toURL();
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
            if (FileUtil.isArchiveFile(u)) {
                u = FileUtil.getArchiveRoot(u);
            }
            roots.add(u);
        }
        return ClassPathSupport.createClassPath((URL[]) roots.toArray(new URL[roots.size()]));
    }

    private static ClassPath sampleClass2CP(Class prototype) {
        CodeSource cs = prototype.getProtectionDomain().getCodeSource();
        return ClassPathSupport.createClassPath(cs != null ? new URL[] {cs.getLocation()} : new URL[0]);
    }

    public ClassPath getBootstrapLibraries() {
        // XXX ignore standard extensions etc.
        ClassPath cp = sysProp2CP("sun.boot.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(Object.class);
    }

    public ClassPath getStandardLibraries() {
        ClassPath cp = sysProp2CP("java.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(/* likely in startup CP */ Dependency.class);
    }

    public String getVendor() {
        return System.getProperty("java.vm.vendor");
    }

    public Specification getSpecification() {
        return new Specification("J2SE", Dependency.JAVA_SPEC); // NOI18N
    }

    public Collection getInstallFolders() {
        return Collections.singleton(FileUtil.toFileObject(new File(System.getProperty("java.home")))); // NOI18N
    }

    public FileObject findTool(String toolName) {
        return null; // XXX too complicated, probably unnecessary for this purpose
    }

    public ClassPath getSourceFolders() {
        return ClassPathSupport.createClassPath(new URL[0]);
    }

    public List getJavadocFolders() {
        return Collections.emptyList();
    }

}
