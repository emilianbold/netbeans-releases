/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Reports location of unit tests.
 * XXX For promo-D, there is no project.xml information about this, so just guess:
 * if there is exactly one Java source root in the project whose folder name
 * begins with "test" (case-insensitively), that is considered the test root
 * (for any other source root). If there is exactly one other package root,
 * that is also considered the source root for that test root. Otherwise nothing
 * is reported.
 * @see "#47835"
 * @author Jesse Glick
 */
final class TestQuery implements UnitTestForSourceQueryImplementation {
    
    private static final String TEST_PREFIX = "test"; // NOI18N
    
    private final Project project;
    
    public TestQuery(Project project) {
        this.project = project;
    }

    public URL findUnitTest(FileObject source) {
        Set/*<FileObject>*/ roots = findJavaRoots();
        if (roots.contains(source)) {
            FileObject testRoot = findTestRoot(roots);
            if (testRoot != null && testRoot != source) {
                // Distinct test root.
                try {
                    return testRoot.getURL();
                } catch (FileStateInvalidException x) {
                    assert false : x;
                    return null;
                }
            } else {
                // No test root, or this is the test root.
                return null;
            }
        } else {
            // What is it? not mine
            return null;
        }
    }

    public URL findSource(FileObject unitTest) {
        Set/*<FileObject>*/ roots = findJavaRoots();
        if (roots.contains(unitTest)) {
            FileObject testRoot = findTestRoot(roots);
            if (testRoot == unitTest) {
                // OK, this is really the test root; see if there is one other root.
                if (roots.size() == 2) {
                    roots.remove(unitTest);
                    assert roots.size() == 1 : roots;
                    FileObject src = (FileObject) roots.iterator().next();
                    try {
                        return src.getURL();
                    } catch (FileStateInvalidException x) {
                        assert false : x;
                        return null;
                    }
                } else {
                    // Nope, forget it.
                    return null;
                }
            } else {
                // That wasn't the known test root, so skip it.
                return null;
            }
        } else {
            // Unknown.
            return null;
        }
    }
    
    /**
     * Find all registered Java package roots.
     */
    private Set/*<FileObject>*/ findJavaRoots() {
        Sources s = ProjectUtils.getSources(project);
        SourceGroup[] groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set/*<FileObject>*/ roots = new HashSet();
        for (int i = 0; i < groups.length; i++) {
            roots.add(groups[i].getRootFolder());
        }
        return roots;
    }
    
    /**
     * Find a "test" root, if there is exactly one.
     */
    private FileObject findTestRoot(Set/*<FileObject>*/ javaRoots) {
        FileObject testRoot = null;
        Iterator it = javaRoots.iterator();
        while (it.hasNext()) {
            FileObject root = (FileObject) it.next();
            if (root.getNameExt().toLowerCase(Locale.US).startsWith(TEST_PREFIX)) {
                if (testRoot == null) {
                    testRoot = root;
                } else {
                    // More than one.
                    return null;
                }
            }
        }
        return testRoot;
    }
    
}
