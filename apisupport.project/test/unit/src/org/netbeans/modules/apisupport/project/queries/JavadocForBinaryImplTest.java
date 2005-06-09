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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.*;

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
        ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant")));
        File beansJar = file("nbbuild/netbeans/ide5/modules/org-netbeans-modules-beans.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(Util.urlForJar(beansJar)).getRoots();
        URL[] expectedRoots = new URL[] {
            Util.urlForDir(file("nbbuild/build/javadoc/org-netbeans-modules-beans")),
            urlForJar(apisZip, "org-netbeans-modules-beans/"),
        };
        assertEquals("correct Javadoc roots for beans", urlSet(expectedRoots), urlSet(roots));
    }
    
    public void testJavadocForExternalModules() throws Exception {
        ProjectManager.getDefault().findProject(FileUtil.toFileObject(file(EEP + "/suite2/misc-project")));
        File miscJar = file("nbbuild/netbeans/devel/modules/org-netbeans-examples-modules-misc.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(Util.urlForJar(miscJar)).getRoots();
        URL[] expectedRoots = new URL[] {
            Util.urlForDir(file(suite2, "misc-project/build/javadoc/org-netbeans-examples-modules-misc")),
            // It is inside ${netbeans.home}/.. so read this.
            urlForJar(apisZip, "org-netbeans-examples-modules-misc/"),
        };
        assertEquals("correct Javadoc roots for misc", urlSet(expectedRoots), urlSet(roots));
        ProjectManager.getDefault().findProject(FileUtil.toFileObject(file(EEP + "/suite3/dummy-project")));
        File dummyJar = file(suite3, "nbplatform/devel/modules/org-netbeans-examples-modules-dummy.jar");
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
