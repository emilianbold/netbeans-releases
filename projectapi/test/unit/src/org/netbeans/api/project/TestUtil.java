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

package org.netbeans.api.project;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Help set up org.netbeans.api.project.*Test.
 * @author Jesse Glick
 */
public final class TestUtil extends ProxyLookup {
    
    static {
        TestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtil.class.getName());
        Assert.assertEquals(TestUtil.class, Lookup.getDefault().getClass());
    }
    
    private static TestUtil DEFAULT;
    /** Do not call directly */
    public TestUtil() {
        Assert.assertNull(DEFAULT);
        DEFAULT = this;
        setLookup(new Object[0]);
    }
    
    /**
     * Set the global default lookup.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     */
    public static void setLookup(Lookup l) {
        DEFAULT.setLookups(new Lookup[] {l});
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances) {
        ClassLoader l = TestUtil.class.getClassLoader();
        DEFAULT.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(l),
            Lookups.singleton(l),
        });
    }
    
    private static boolean warned = false;
    /**
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            return fo;
        } else {
            if (!warned) {
                warned = true;
                System.err.println("No FileObject for " + root + " found.\n" +
                                    "Maybe you need ${openide/masterfs.dir}/modules/org-netbeans-modules-masterfs.jar\n" +
                                    "in test.unit.run.cp.extra, or make sure Lookups.metaInfServices is included in Lookup.default, so that\n" +
                                    "Lookup.default<URLMapper>=" + Lookup.getDefault().lookup(new Lookup.Template(URLMapper.class)).allInstances() + " includes MasterURLMapper\n" +
                                    "e.g. by using TestUtil.setLookup(Object[]) rather than TestUtil.setLookup(Lookup).");
            }
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    /**
     * Delete a file and all subfiles.
     */
    public static void deleteRec(File f) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) {
                throw new IOException("List " + f);
            }
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (!f.delete()) {
            throw new IOException("Delete " + f);
        }
    }
    
    /**
     * Create a testing project factory which recognizes directories containing
     * a subdirectory called "testproject".
     * If that subdirectory contains a file named "broken" then loading the project
     * will fail with an IOException.
     */
    public static ProjectFactory testProjectFactory() {
        return new TestProjectFactory();
    }
    
    /**
     * Try to force a GC.
     */
    public static void gc() {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    private static final Map/*<FileObject,int>*/ loadCount = new WeakHashMap();
    
    /**
     * Check how many times {@link ProjectFactory#loadProject} has been called
     * (with any outcome) on a given project directory.
     */
    public static int projectLoadCount(FileObject dir) {
        Integer i = (Integer)loadCount.get(dir);
        if (i != null) {
            return i.intValue();
        } else {
            return 0;
        }
    }
    
    /**
     * Mark a test project to fail with a given error when it is next saved.
     * The error only applies to the next save, not subsequent ones.
     * @param p a test project
     * @param error an error to throw (IOException or Error or RuntimeException),
     *              or null if it should succeed
     */
    public static void setProjectSaveWillFail(Project p, Throwable error) {
        ((TestProject)p).error = error;
    }
    
    /**
     * Get the number of times a test project was successfully saved with no error.
     * @param p a test project
     * @return the save count
     */
    public static int projectSaveCount(Project p) {
        return ((TestProject)p).saveCount;
    }
    
    /**
     * Mark a test project as modified.
     * @param p a test project
     */
    public static void modify(Project p) {
        ((TestProject)p).state.markModified();
    }
    
    /**
     * Mark a test project as modified.
     * @param p a test project
     */
    public static void notifyDeleted(Project p) {
        ((TestProject)p).state.notifyDeleted();
    }
    
    /**
     * If set to something non-null, loading a broken project will wait for
     * notification on this monitor before throwing an exception.
     * @see ProjectManagerTest#testLoadExceptionWithConcurrentLoad
     */
    public static Object BROKEN_PROJECT_LOAD_LOCK = null;
    
    private static final class TestProjectFactory implements ProjectFactory {
        
        TestProjectFactory() {}
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            Integer i = (Integer)loadCount.get(projectDirectory);
            if (i == null) {
                i = new Integer(1);
            } else {
                i = new Integer(i.intValue() + 1);
            }
            loadCount.put(projectDirectory, i);
            FileObject testproject = projectDirectory.getFileObject("testproject");
            if (testproject != null && testproject.isFolder()) {
                if (testproject.getFileObject("broken") != null) {
                    if (BROKEN_PROJECT_LOAD_LOCK != null) {
                        synchronized (BROKEN_PROJECT_LOAD_LOCK) {
                            try {
                                BROKEN_PROJECT_LOAD_LOCK.wait();
                            } catch (InterruptedException e) {
                                assert false : e;
                            }
                        }
                    }
                    throw new IOException("Load failed of " + projectDirectory);
                } else {
                    return new TestProject(projectDirectory, state);
                }
            } else {
                return null;
            }
        }
        
        public void saveProject(Project project) throws IOException, ClassCastException {
            TestProject p = (TestProject)project;
            Throwable t = p.error;
            if (t != null) {
                p.error = null;
                if (t instanceof IOException) {
                    throw (IOException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw (RuntimeException)t;
                }
            }
            p.saveCount++;
        }
        
        public boolean isProject(FileObject dir) {
            FileObject testproject = dir.getFileObject("testproject");
            return testproject != null && testproject.isFolder();
        }
        
    }
    
    private static final class TestProject implements Project {
        
        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        
        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getProjectDirectory() {
            return dir;
        }
        
        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }

        /* Probably unnecessary to have a ProjectInformation here:
        public String getName() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
        
        public String getDisplayName() {
            return "Test Project in " + getProjectDirectory().getNameExt();
        }
        
        public Image getIcon() {
            return null;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
         */
        
    }
    
    /**
     * Open a URL of content (for example from {@link Class#getResource}) and copy it to a named file.
     * The new file can be given as a parent directory plus a relative (slash-separated) path.
     * The file may not already exist, but intermediate directories may or may not.
     * If the content URL is null, the file is just created, no more; if it already existed
     * it is touched (timestamp updated) and its contents are cleared.
     * @return the file object
     */
    public static FileObject createFileFromContent(URL content, FileObject parent, String path) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("null parent");
        }
        Assert.assertTrue("folder", parent.isFolder());
        FileObject fo = parent;
        StringTokenizer tok = new StringTokenizer(path, "/");
        boolean touch = false;
        while (tok.hasMoreTokens()) {
            Assert.assertNotNull("fo is null (parent=" + parent + " path=" + path + ")", fo);
            String name = tok.nextToken();
            if (tok.hasMoreTokens()) {
                FileObject sub = fo.getFileObject(name);
                if (sub == null) {
                    FileObject fo2 = fo.createFolder(name);
                    Assert.assertNotNull("createFolder(" + fo + ", " + name + ") -> null", fo2);
                    fo = fo2;
                } else {
                    Assert.assertTrue("folder", sub.isFolder());
                    fo = sub;
                }
            } else {
                FileObject sub = fo.getFileObject(name);
                if (sub == null) {
                    FileObject fo2 = fo.createData(name);
                    Assert.assertNotNull("createData(" + fo + ", " + name + ") -> null", fo2);
                    fo = fo2;
                } else {
                    fo = sub;
                    touch = true;
                }
            }
        }
        assert fo.isData();
        if (content != null || touch) {
            FileLock lock = fo.lock();
            try {
                OutputStream os = fo.getOutputStream(lock);
                try {
                    if (content != null) {
                        InputStream is = content.openStream();
                        try {
                            FileUtil.copy(is, os);
                        } finally {
                            is.close();
                        }
                    }
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
        return fo;
    }
    
}
