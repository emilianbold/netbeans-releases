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

package org.netbeans.api.java.classpath;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * ClassPath tests
 *
 * @author David Konecny, Tomas Zezula
 */
public class ClassPathTest extends NbTestCase {
    
    public ClassPathTest(String testName) {
        super(testName);
    }
    
    private File getBaseDir() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir;
    }
    
    /**
     * Tests ClassPath.getResourceName ();
     */
    public void testGetResourceName() throws Exception {
        File f = getBaseDir();        
        f = new File(f.getPath()+"/w.e.i.r.d/f  o  l  d  e  r");
        f.mkdirs();
        File f2 = new File(f, "org/netbeans/test");
        f2.mkdirs();
        File f3 = new File(f2, "Main.java");
        f3.createNewFile();

        FileObject cpRoot = FileUtil.toFileObject(f);
        FileObject cpItem = FileUtil.toFileObject(f2);
        FileObject clazz = FileUtil.toFileObject(f3);
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[]{cpRoot});
        String pkg = cp.getResourceName(cpItem);
        assertEquals("org/netbeans/test", pkg);
        
        pkg = cp.getResourceName(cpItem, '.', true);
        assertEquals("org.netbeans.test", pkg);
        
        pkg = cp.getResourceName(cpItem, '.', false);
        assertEquals("org.netbeans.test", pkg);
        
        pkg = cp.getResourceName(cpItem, '#', true);
        assertEquals("org#netbeans#test", pkg);
        
        pkg = cp.getResourceName(cpItem, '#', false);
        assertEquals("org#netbeans#test", pkg);
        
        pkg = cp.getResourceName(clazz);
        assertEquals("org/netbeans/test/Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '.', true);
        assertEquals("org.netbeans.test.Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '.', false);
        assertEquals("org.netbeans.test.Main", pkg);
        
        pkg = cp.getResourceName(clazz, '@', true);
        assertEquals("org@netbeans@test@Main.java", pkg);
        
        pkg = cp.getResourceName(clazz, '@', false);
        assertEquals("org@netbeans@test@Main", pkg);
    }
    
    /**
     * Tests ClassPath.findAllResources(), ClassPath.findResoruce(), 
     * ClassPath.contains (), ClassPath.findOwnerRoot(),
     * ClassPath.isResourceVisible ()
     */
    public void testGetResource () throws Exception {
        File root_1 = new File (getBaseDir(),"root_1");
        root_1.mkdir();
        File root_2 = new File (getBaseDir(),"root_2");
        root_2.mkdir();
        FileObject[] roots = new FileObject [] {
            FileUtil.toFileObject(root_1),
            FileUtil.toFileObject(root_2),
        };
        
        FileObject tmp = roots[0].createFolder("org");
        tmp = tmp.createFolder("me");
        FileObject testFo_1 = tmp.createData("Foo","txt");
        tmp = roots[1].createFolder("org");
        tmp = tmp.createFolder("me");
        FileObject testFo_2 = tmp.createData("Foo","txt");        
        ClassPath cp = ClassPathSupport.createClassPath(roots);        
        
        //findResource
        assertTrue(cp.findResource ("org/me/Foo.txt")==testFo_1);
        assertTrue (cp.findResource("org/me/None.txt")==null);
        
        //findAllResources
        List res = cp.findAllResources ("org/me/Foo.txt");
        assertTrue (res.size() == 2);
        assertTrue (res.contains(testFo_1));
        assertTrue (res.contains(testFo_2));
        
        //contains
        assertTrue (cp.contains (testFo_1));
        assertTrue (cp.contains (testFo_2));
        assertFalse (cp.contains (roots[0].getParent()));
        
        //findOwnerRoot
        assertTrue (cp.findOwnerRoot(testFo_1)==roots[0]);
        assertTrue (cp.findOwnerRoot(testFo_2)==roots[1]);

        /*
        //isResourceVisible
        assertTrue (cp.isResourceVisible(testFo_1));
        assertFalse (cp.isResourceVisible(testFo_2));
         */
        
        cp = null;
        roots[0].delete();
        roots[1].delete();
    }
    
    /**
     * Test ClassPath.getRoots(), ClassPath.addPropertyChangeListener (),
     * ClassPath.entries () and classpath SPI.
     */
    public void testListening() throws Exception {

        File root_1 = new File (getBaseDir(),"root_1");
        root_1.mkdir();
        File root_2 = new File (getBaseDir(),"root_2");
        root_2.mkdir();
        File root_3 = new File (getBaseDir(),"root_3.jar");
        JarOutputStream out = new JarOutputStream ( new FileOutputStream (root_3));
        try {            
            out.putNextEntry(new ZipEntry("test.txt"));
            out.write ("test".getBytes());
        } finally {
            out.close ();
        }        
        assertNotNull ("Can not find file",FileUtil.toFileObject(root_1));
        assertNotNull ("Can not find file",FileUtil.toFileObject(root_2));
        assertNotNull ("Can not find file",FileUtil.toFileObject(root_3));
        TestClassPathImplementation impl = new TestClassPathImplementation();
	ClassPath cp = ClassPathFactory.createClassPath (impl);
        impl.addResource(root_1.toURI().toURL());
        cp.addPropertyChangeListener (impl);
        Set events = new HashSet ();
        events.add(ClassPath.PROP_ENTRIES);
        events.add(ClassPath.PROP_ROOTS);
        impl.expectEvents (events);
        impl.addResource (root_2.toURI().toURL());
        impl.assertEvents();
        assertTrue (cp.getRoots().length==2);
        events = new HashSet ();
        events.add (ClassPath.PROP_ENTRIES);
        events.add (ClassPath.PROP_ROOTS);
        impl.expectEvents (events);
        impl.removeResource (root_2.toURI().toURL());
        impl.assertEvents();
        assertTrue (cp.getRoots().length==1);
        events = new HashSet ();
        events.add (ClassPath.PROP_ROOTS);
        impl.expectEvents (events);
        FileObject fo = cp.getRoots()[0];
        FileObject parentFolder = fo.getParent();        
        fo.delete();
        impl.assertEvents();
        assertTrue (cp.getRoots().length==0);
        events = new HashSet ();
        events.add (ClassPath.PROP_ROOTS);
        impl.expectEvents (events);
        parentFolder.createFolder("root_1");
        assertTrue (cp.getRoots().length==1);
        impl.assertEvents ();       
        FileObject archiveFile = FileUtil.toFileObject(root_3);
        impl.addResource(FileUtil.getArchiveRoot(archiveFile.getURL()));
        assertEquals (cp.getRoots().length,2);
        events = new HashSet ();
        events.add (ClassPath.PROP_ROOTS);
        impl.expectEvents (events);
        root_3.delete();
        root_3 = new File (getBaseDir(),"root_3.jar");
        Thread.sleep(1000);
        out = new JarOutputStream ( new FileOutputStream (root_3));
        try {            
            out.putNextEntry(new ZipEntry("test2.txt"));
            out.write ("test2".getBytes());
        } finally {
            out.close ();
        }
        archiveFile.refresh();
        impl.assertEvents();
        root_1.delete();
        root_2.delete();
        root_3.delete();
        cp = null;
    }
    
    public void testChangesAcknowledgedWithoutListener() throws Exception {
        // Discovered in #72573.
        clearWorkDir();
        File root = new File(getWorkDir(), "root");
        URL rootU = root.toURI().toURL();
        if (!rootU.toExternalForm().endsWith("/")) {
            rootU = new URL(rootU.toExternalForm() + "/");
        }
        ClassPath cp = ClassPathSupport.createClassPath(new URL[] {rootU});
        assertEquals("nothing there yet", null, cp.findResource("f"));
        FileObject f = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "root/f");
        assertEquals("found new file", f, cp.findResource("f"));
        f.delete();
        assertEquals("again empty", null, cp.findResource("f"));
    }
    
    static final class TestClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

        private PropertyChangeSupport support = new PropertyChangeSupport (this);
        private List resources = new ArrayList ();
        private Set unknownEvents = new HashSet ();
        private Set events;

        public synchronized void addResource (URL resource) {
            PathResourceImplementation pr = ClassPathSupport.createResource (resource);
            this.resources.add (pr);
            this.support.firePropertyChange (ClassPathImplementation.PROP_RESOURCES,null,null);
        }

        public synchronized void removeResource (URL resource) {
            for (Iterator it = this.resources.iterator(); it.hasNext();) {
                PathResourceImplementation pr = (PathResourceImplementation) it.next ();
                if (Arrays.asList(pr.getRoots()).contains (resource)) {
                    this.resources.remove (pr);
                    this.support.firePropertyChange (ClassPathImplementation.PROP_RESOURCES,null,null);
                    break;
                }
            }
        }

        public synchronized List /*<PathResourceImplementation>*/ getResources() {
            return this.resources;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener (listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener (listener);
        }

        public void propertyChange (PropertyChangeEvent event) {
            String propName = event.getPropertyName();
            if (this.events.contains(propName)) {
                this.events.remove(propName);
            }
            else {
                unknownEvents.add (propName);
            }
        }

        void expectEvents (Set events) {
            this.unknownEvents.clear();
            this.events = events;
        }

        void assertEvents () {
            if (events.size()>0) {
                events.clear();
                assertTrue (false);
            }
            if (unknownEvents.size()>0) {
                unknownEvents.clear();
                assertTrue (false);
            }
        }
    }
    
}
