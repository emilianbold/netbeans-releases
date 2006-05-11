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

package org.openide.filesystems;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.*;

/**
 * @author Radek Matous
 */
public class FileObject75826Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private LocalFileSystem lfs;
    private FileObject testFo;

    public FileObject75826Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(FileObject75826Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = (LocalFileSystem)TestUtilHid.createLocalFileSystem(getName(), new String[]{getName() });
        lfs = new TestFileSystem(lfs, getName());
        testFo = lfs.findResource(getName());
        assertNotNull(testFo);
    }

    public void testOutputStreamFiresIOException() throws IOException {
        OutputStream os = null;
        FileLock lock = null;
        try {
            os = testFo.getOutputStream();
            fail();
        } catch (IOException ex) {}
        try {
            lock = testFo.lock();
            assertNotNull(lock);
            assertTrue(lock.isValid());
        } finally {
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
        }
    }

    public void testCloseStreamFiresIOException() throws IOException {
        FileLock lock = null;
        OutputStream os = testFo.getOutputStream();
        try {
            os.close();
            fail();
        } catch (IOException ex) {}
        try {
            lock = testFo.lock();
            assertNotNull(lock);
            assertTrue(lock.isValid());
        } finally {
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
        }
    }

    private static final class TestFileSystem extends LocalFileSystem {
        TestFileSystem(LocalFileSystem lfs, String testName) throws Exception {
            super();
            if ("testOutputStreamFiresIOException".equals(testName)) {
                this.info = new LocalFileSystem.Impl(this) {
                    public OutputStream outputStream(String name) throws java.io.IOException {
                        throw new IOException();
                    }
                };
            } else if ("testCloseStreamFiresIOException".equals(testName)) {
                this.info = new LocalFileSystem.Impl(this) {
                    public OutputStream outputStream(String name) throws java.io.IOException {
                        return new FilterOutputStream(super.outputStream(name)) {
                            public void close() throws IOException {
                                throw new IOException();
                            }
                        };
                    }
                };
            }
            setRootDirectory(lfs.getRootDirectory());
        }
    }
}