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

package org.netbeans.api.project;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test {@link ProjectUtils}.
 * @author Jesse Glick
 */
public class ProjectUtilsTest extends NbTestCase {
    
    static {
        ProjectUtilsTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public ProjectUtilsTest(String name) {
        super(name);
    }
    
    public void testHasSubprojectCycles() throws Exception {
        // Check static cycle detection.
        TestProj a = new TestProj("a");
        assertFalse("no cycles in a project with no declared subprojects", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[0];
        assertFalse("no cycles in a standalone project", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj b = new TestProj("b");
        a.subprojs = new Project[] {b};
        b.subprojs = new Project[0];
        assertFalse("no cycles in a -> b", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj c = new TestProj("c");
        c.subprojs = new Project[0];
        b.subprojs = new Project[] {c};
        assertFalse("no cycles in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, null));
        TestProj d = new TestProj("d");
        d.subprojs = new Project[0];
        b.subprojs = new Project[] {d};
        c.subprojs = new Project[] {d};
        assertFalse("no cycles in a -> {b, c} -> d (DAG)", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[] {a};
        assertTrue("self-loop cycle in a -> a", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[] {b};
        b.subprojs = new Project[] {a};
        assertTrue("simple cycle in a -> b -> a", ProjectUtils.hasSubprojectCycles(a, null));
        b.subprojs = new Project[] {c};
        c.subprojs = new Project[] {b};
        assertTrue("simple cycle not involing master in a -> b -> c -> b", ProjectUtils.hasSubprojectCycles(a, null));
        c.subprojs = new Project[] {a};
        a.subprojs = new Project[] {b, d};
        d.subprojs = new Project[] {a};
        assertTrue("multiple cycles in a -> b -> c -> a, a -> d -> a", ProjectUtils.hasSubprojectCycles(a, null));
        a.subprojs = new Project[0];
        b.subprojs = new Project[0];
        assertFalse("no cycle introduced by a -> b in a, b", ProjectUtils.hasSubprojectCycles(a, b));
        c.subprojs = new Project[0];
        b.subprojs = new Project[] {c};
        assertFalse("no cycle introduced by a -> b in a, b -> c", ProjectUtils.hasSubprojectCycles(a, b));
        a.subprojs = new Project[] {b};
        assertFalse("no cycle introduced by no-op a -> b in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, b));
        assertFalse("no cycle introduced by direct a -> c in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, c));
        assertTrue("cycle introduced by a -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(a, a));
        assertTrue("cycle introduced by b -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(b, a));
        assertTrue("cycle introduced by c -> a in a -> b -> c", ProjectUtils.hasSubprojectCycles(c, a));
        c.subprojs = null;
        assertTrue("cycle introduced by c -> a in a -> b -> c (no explicit subprojects in c)", ProjectUtils.hasSubprojectCycles(c, a));
    }
    
    /**
     * Fake project with subprojects.
     */
    private static final class TestProj implements Project, SubprojectProvider {
        
        private final String name;
        /**
         * Subproject list.
         * Use null to not have a SubprojectProvider at all.
         */
        public Project[] subprojs = null;

        /**
         * Create a fake project.
         * @param name a name for debugging purposes
         */
        public TestProj(String name) {
            this.name = name;
        }
        
        public Lookup getLookup() {
            if (subprojs == null) {
                return Lookup.EMPTY;
            } else {
                return Lookups.singleton(this);
            }
        }
        
        public Set getSubprojects() {
            assert subprojs != null;
            return new HashSet(Arrays.asList(subprojs));
        }
        
        public FileObject getProjectDirectory() {
            // irrelevant
            return null;
        }
        
        public void addChangeListener(ChangeListener l) {}
        
        public void removeChangeListener(ChangeListener l) {}
        
        public String toString() {
            return name;
        }

    }
    
}
