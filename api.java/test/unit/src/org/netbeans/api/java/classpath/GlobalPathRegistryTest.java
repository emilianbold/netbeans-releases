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

package org.netbeans.api.java.classpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

// XXX need test for getSourceRoots and findResource

/**
 * Test functionality of GlobalPathRegistry.
 * @author Jesse Glick
 */
public class GlobalPathRegistryTest extends NbTestCase {
    
    public GlobalPathRegistryTest(String name) {
        super(name);
    }
    
    private GlobalPathRegistry r;
    private ClassPath cp1, cp2, cp3, cp4, cp5;
    protected void setUp() throws Exception {
        super.setUp();
        r = GlobalPathRegistry.getDefault();
        r.clear();
        LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(getWorkDir());
        FileObject root = lfs.getRoot();
        cp1 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("1")});
        cp2 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("2")});
        cp3 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("3")});
        cp4 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("4")});
        cp5 = ClassPathSupport.createClassPath(new FileObject[] {root.createFolder("5")});
    }
    
    public void testBasicOperation() throws Exception {
        assertEquals("initially no paths of type a", Collections.EMPTY_SET, r.getPaths("a"));
        r.register("a", new ClassPath[] {cp1, cp2});
        assertEquals("added some paths of type a", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        r.register("a", new ClassPath[0]);
        assertEquals("did not add any new paths to a", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), r.getPaths("a"));
        assertEquals("initially no paths of type b", Collections.EMPTY_SET, r.getPaths("b"));
        r.register("b", new ClassPath[] {cp3, cp4, cp5});
        assertEquals("added some paths of type b", new HashSet(Arrays.asList(new ClassPath[] {cp3, cp4, cp5})), r.getPaths("b"));
        r.unregister("a", new ClassPath[] {cp1});
        assertEquals("only one path left of type a", Collections.singleton(cp2), r.getPaths("a"));
        r.register("a", new ClassPath[] {cp2, cp3});
        assertEquals("only one new path added of type a", new HashSet(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("still have extra cp2 in a", new HashSet(Arrays.asList(new ClassPath[] {cp2, cp3})), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp2});
        assertEquals("last cp2 removed from a", Collections.singleton(cp3), r.getPaths("a"));
        r.unregister("a", new ClassPath[] {cp3});
        assertEquals("a now empty", Collections.EMPTY_SET, r.getPaths("a"));
        r.unregister("a", new ClassPath[0]);
        assertEquals("a still empty", Collections.EMPTY_SET, r.getPaths("a"));
        try {
            r.unregister("a", new ClassPath[] {cp3});
            fail("should not have been permitted to unregister a nonexistent entry");
        } catch (IllegalArgumentException x) {
            // Good.
        }
    }
    
    public void testListening() throws Exception {
        assertEquals("initially no paths of type b", Collections.EMPTY_SET, r.getPaths("b"));
        L l = new L();
        r.addPathRegistryListener(l);
        r.register("b", new ClassPath[] {cp1, cp2});
        GlobalPathRegistryEvent e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right registry", r, e.getRegistry());
        assertEquals("right ID", "b", e.getId());
        assertEquals("right changed paths", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp2})), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp2, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertTrue("was an addition", l.added());
        assertEquals("right changed paths", Collections.singleton(cp3), e.getChangedPaths());
        r.register("b", new ClassPath[] {cp3});
        e = l.event();
        assertNull("no event for adding a dupe", e);
        r.unregister("b", new ClassPath[] {cp1, cp3, cp3});
        e = l.event();
        assertNotNull("got an event", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", new HashSet(Arrays.asList(new ClassPath[] {cp1, cp3})), e.getChangedPaths());
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNull("no event for removing an extra", e);
        r.unregister("b", new ClassPath[] {cp2});
        e = l.event();
        assertNotNull("now an event for removing the last copy", e);
        assertFalse("was a removal", l.added());
        assertEquals("right changed paths", Collections.singleton(cp2), e.getChangedPaths());
    }
    
    private static final class L implements GlobalPathRegistryListener {
        
        private GlobalPathRegistryEvent e;
        private boolean added;
        
        public L() {}
        
        public synchronized GlobalPathRegistryEvent event() {
            GlobalPathRegistryEvent _e = e;
            e = null;
            return _e;
        }
        
        public boolean added() {
            return added;
        }
        
        public synchronized void pathsAdded(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = true;
        }
        
        public synchronized void pathsRemoved(GlobalPathRegistryEvent e) {
            assertNull("checked for last event", this.e);
            this.e = e;
            added = false;
        }
        
    }
    
}
