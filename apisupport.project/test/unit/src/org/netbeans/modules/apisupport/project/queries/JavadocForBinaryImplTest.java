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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.filesystems.FileUtil;

/**
 * Test {@link JavadocForBinaryImpl}.
 *
 * @author Jesse Glick
 */
public class JavadocForBinaryImplTest extends TestBase {

    static {
        JavadocForBinaryImpl.ignoreNonexistentRoots = false;
    }
    
    private File suite2, suite3;
    
    public JavadocForBinaryImplTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        suite2 = file(extexamplesF, "suite2");
        suite3 = file(extexamplesF, "suite3");
    }
    
    public void testJavadocForNetBeansOrgModules() throws Exception {
        // Have to load at least one module to get the scan going.
        ClassPath.getClassPath(nbroot.getFileObject("ant/src"), ClassPath.COMPILE);
        File beansJar = file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-beans.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(Util.urlForJar(beansJar)).getRoots();
        URL[] expectedRoots = new URL[] {
            Util.urlForDir(file("nbbuild/build/javadoc/org-netbeans-modules-beans")),
            urlForJar(apisZip, "org-netbeans-modules-beans/"),
        };
        assertEquals("correct Javadoc roots for beans", urlSet(expectedRoots), urlSet(roots));
    }
    
    public void testJavadocForExternalModules() throws Exception {
        ClassPath.getClassPath(FileUtil.toFileObject(file(EEP + "/suite2/misc-project/src")), ClassPath.COMPILE);
        File miscJar = file(EEP + "/suite2/build/cluster/modules/org-netbeans-examples-modules-misc.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(Util.urlForJar(miscJar)).getRoots();
        URL[] expectedRoots = new URL[] {
            Util.urlForDir(file(suite2, "misc-project/build/javadoc/org-netbeans-examples-modules-misc")),
            // It is inside ${netbeans.home}/.. so read this.
            urlForJar(apisZip, "org-netbeans-examples-modules-misc/"),
        };
        assertEquals("correct Javadoc roots for misc", urlSet(expectedRoots), urlSet(roots));
        ClassPath.getClassPath(FileUtil.toFileObject(file(EEP + "/suite3/dummy-project/src")), ClassPath.COMPILE);
        File dummyJar = file(suite3, "dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar");
        roots = JavadocForBinaryQuery.findJavadoc(Util.urlForJar(dummyJar)).getRoots();
        expectedRoots = new URL[] {
            Util.urlForDir(file(suite3, "dummy-project/build/javadoc/org-netbeans-examples-modules-dummy")),
        };
        assertEquals("correct Javadoc roots for dummy", urlSet(expectedRoots), urlSet(roots));
    }
    
    private static URL urlForJar(File jar, String path) throws Exception {
        return new URL(Util.urlForJar(jar), path);
    }
    
    private static SortedSet/*<String>*/ urlSet(URL[] urls) {
        SortedSet/*<String>*/ set = new TreeSet();
        for (int i = 0; i < urls.length; i++) {
            set.add(urls[i].toExternalForm());
        }
        return set;
    }
    
}
