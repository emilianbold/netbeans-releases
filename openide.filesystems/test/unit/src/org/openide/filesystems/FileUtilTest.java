/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * @author Jesse Glick, Jiri Skrivanek
 */
public class FileUtilTest extends NbTestCase {

    public FileUtilTest(String n) {
        super(n);
    }

    public static Test suite() {
        Test suite = null;
            //suite = new FileUtilTest("testNormalizeFile");
        if (suite == null) {
            suite = new NbTestSuite(FileUtilTest.class);
        }
        return suite;
    }

    @Override
    public void setUp() throws IOException {
        // folder of declarative resolvers must exist before MIME resolvers tests
        FileUtil.createFolder(FileUtil.getConfigRoot(), "Services/MIMEResolver");
    }

    public void testToFileObjectSlash() throws Exception { // #98388
        if (!Utilities.isUnix()) {
            return;
        }
        File root = new File("/");
        assertTrue(root.isDirectory());
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(root);
        final FileObject LFS_ROOT = lfs.getRoot();
        MockLookup.setInstances(new URLMapper() {
            public URL getURL(FileObject fo, int type) {
                return null;
            }
            public FileObject[] getFileObjects(URL url) {
                if (url.toExternalForm().equals("file:/")) {
                    return new FileObject[] {LFS_ROOT};
                } else {
                    return null;
                }
            }
        });
        URLMapper.reset();
        assertEquals(LFS_ROOT, FileUtil.toFileObject(root));
    }

    public void testArchiveConversion() throws Exception {
        final LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(getWorkDir());
        MockLookup.setInstances(new URLMapper() {
            String rootURL = lfs.getRoot().getURL().toString();
            @Override
            public FileObject[] getFileObjects(URL url) {
                String u = url.toString();
                FileObject f = null;
                if (u.startsWith(rootURL)) {
                    f = lfs.findResource(u.substring(rootURL.length()));
                }
                return f != null ? new FileObject[] {f} : null;
            }
            @Override
            public URL getURL(FileObject fo, int type) {
                return null;
            }
        });
        URLMapper.reset();

        TestFileUtils.writeFile(lfs.getRoot(), "README", "A random file with some stuff in it.");
        assertCorrectURL("README", null, null); // not an archive
        TestFileUtils.writeFile(lfs.getRoot(), "README.txt", "A random file with some stuff in it.");
        assertCorrectURL("README.txt", null, null); // not an archive either
        TestFileUtils.writeFile(lfs.getRoot(), "empty.zip", "");
        assertCorrectURL("empty.zip", "jar:", "empty.zip!/");
        TestFileUtils.writeZipFile(lfs.getRoot(), "normal.zip", "something:text inside a ZIP entry");
        assertCorrectURL("normal.zip", "jar:", "normal.zip!/");
        assertCorrectURL("nonexistent.zip", "jar:", "nonexistent.zip!/");
        lfs.getRoot().createFolder("folder");
        assertCorrectURL("folder", "", "folder/");
        lfs.getRoot().createFolder("some.folder");
        assertCorrectURL("some.folder", "", "some.folder/");
        assertCorrectURL("nonexistent", "", "nonexistent/");
        assertCorrectURL("non existent.zip", "jar:", "non%20existent.zip!/");
        assertCorrectURL("non existent", "", "non%20existent/");

        assertCorrectFile("folder", "", "folder/");
        assertCorrectFile("stuff.zip", "jar:", "stuff.zip!/");
        assertCorrectFile(null, "jar:", "stuff.zip!/subentry/");
        assertCorrectFile(null, "http:", "");
        // Impossible to even construct such a URL: assertCorrectFolder("stuff.zip", "jar:", "stuff.zip");
        assertCorrectFile("stuff.zip", "", "stuff.zip");
        assertCorrectFile("folder", "", "folder");
        assertCorrectFile("fol der", "", "fol%20der/");
        assertCorrectFile("stu ff.zip", "jar:", "stu%20ff.zip!/");
        assertCorrectFile("stu ff.zip", "", "stu%20ff.zip");
        assertCorrectFile("fol der", "", "fol%20der");
    }
    private void assertCorrectURL(String filename, String expectedURLPrefix, String expectedURLSuffix) throws Exception {
        File d = getWorkDir();
        assertEquals(expectedURLSuffix == null ? null : new URL(expectedURLPrefix + d.toURI() + expectedURLSuffix),
                FileUtil.urlForArchiveOrDir(new File(d, filename)));
    }
    private void assertCorrectFile(String expectedFilename, String urlPrefix, String urlSuffix) throws Exception {
        assertEquals(expectedFilename == null ? null : new File(getWorkDir(), expectedFilename),
                FileUtil.archiveOrDirForURL(new URL(urlPrefix + getWorkDir().toURI() + urlSuffix)));
    }

    /** Tests translation from jar resource url to jar archive url. */
    public void testGetArchiveFile() throws Exception {
        String urls[][] = {
            // resource url, expected jar url
            {"jar:file:/a.jar!/META-INF/MANIFEST.MF", "file:/a.jar"}, // unix root
            {"jar:file:/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:/a/b/c/a.jar"}, // unix
            {"jar:file:/C:/a.jar!/META-INF/MANIFEST.MF", "file:/C:/a.jar"}, // windows root
            {"jar:file:/C:/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:/C:/a/b/c/a.jar"}, // windows
            {"jar:file://computerName/sharedFolder/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a.jar"}, // windows UNC root malformed
            {"jar:file://computerName/sharedFolder/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a/b/c/a.jar"}, // windows UNC malformed
            {"jar:file:////computerName/sharedFolder/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a.jar"}, // windows UNC root
            {"jar:file:////computerName/sharedFolder/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a/b/c/a.jar"} // windows UNC
        };
        for (int i = 0; i < urls.length; i++) {
            assertEquals("FileUtil.getArchiveFile failed.", new URL(urls[i][1]), FileUtil.getArchiveFile(new URL(urls[i][0])));
        }
    }

    public void testIsArchiveFileRace() throws Exception {
        final LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        final File wd = getWorkDir();
        lfs.setRootDirectory(wd);
        MockLookup.setInstances(new URLMapper() {
            String rootURL = lfs.getRoot().getURL().toString();
            @Override
            public FileObject[] getFileObjects(URL url) {
                String u = url.toString();
                FileObject f = null;
                if (u.startsWith(rootURL)) {
                    f = lfs.findResource(u.substring(rootURL.length()));
                }
                return f != null ? new FileObject[] {f} : null;
            }
            @Override
            public URL getURL(FileObject fo, int type) {
                return null;
            }
        });
        URLMapper.reset();
        final File testFile = new File (wd,"test.jar"); //NOI18N
        FileUtil.createData(testFile);

        final Logger log = Logger.getLogger(FileUtil.class.getName());
        log.setLevel(Level.FINEST);
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("isArchiveFile_FILE_RESOLVED".equals(record.getMessage())) {  //NOI18N
                    try {
                        final FileObject fo = (FileObject) record.getParameters()[0];
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        log.addHandler(handler);
        try {
            final boolean result = FileUtil.isArchiveFile(testFile.toURI().toURL());
            assertTrue("The test.jar should be archive.",result);   //NOI18N
        } finally {
            log.removeHandler(handler);
        }
    }

    /** Tests normalizeFile() method. */
    public void testNormalizeFile() throws IOException {
        // pairs of path before and after normalization
        Map<String, String> paths = new HashMap<String, String>();
        if (Utilities.isWindows()) {
            paths.put("A:\\", "A:\\");
            paths.put("A:\\dummy", "A:\\dummy");
            paths.put("a:\\", "A:\\");
            try {
                new File("a:\\dummy").getCanonicalPath();
                paths.put("a:\\dummy", "A:\\dummy");
            } catch (IOException e) {
                // if getCanonicalPath fails, normalization returns File.getAbsolutePath
                paths.put("a:\\dummy", "a:\\dummy");
            }
            paths.put("C:\\", "C:\\");
            paths.put("C:\\dummy", "C:\\dummy");
            paths.put("c:\\", "C:\\");
            paths.put("c:\\dummy", "C:\\dummy");
            paths.put("c:\\.", "C:\\");
            paths.put("c:\\..", "C:\\");
            paths.put("c:\\dummy\\.", "C:\\dummy");
            paths.put("c:\\dummy\\..", "C:\\");
            paths.put("c:\\dummy\\.\\foo", "C:\\dummy\\foo");
            paths.put("c:\\dummy\\..\\foo", "C:\\foo");
            paths.put("\\\\", "\\\\");
            paths.put("\\\\computerName\\sharedFolder", "\\\\computerName\\sharedFolder");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\.", "\\\\computerName\\sharedFolder\\dummy");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\..", "\\\\computerName\\sharedFolder");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\.\\foo", "\\\\computerName\\sharedFolder\\dummy\\foo");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\..\\foo", "\\\\computerName\\sharedFolder\\foo");
        } else {
            paths.put("/", "/");
            paths.put("/dummy/.", "/dummy");
            paths.put("/dummy/..", "/");
            paths.put("/dummy/./foo", "/dummy/foo");
            paths.put("/dummy/../foo", "/foo");
        }
        // #137407 - java.io.File(".") should be normalized
        paths.put(".", new File(".").getCanonicalPath());
        paths.put("..", new File("..").getCanonicalPath());

        for (String path : paths.keySet()) {
            File file = new File(path);
            assertTrue("Idempotency violated for path: " + path, FileUtil.normalizeFile(FileUtil.normalizeFile(file)).equals(FileUtil.normalizeFile(file)));
            assertEquals("File not normalized: " + path, paths.get(path), FileUtil.normalizeFile(file).getPath());
        }
    }

    /** Tests that only resolvers are queried which supply at least one of
     * MIME types given in array in FileUtil.getMIMEType(fo, String[]).
     * See issue 137734.
     */
    public void testGetMIMETypeConstrained() throws IOException {
        MyResolver resolver = new MyResolver();
        MockLookup.setInstances(resolver);
        assertNotNull(Lookup.getDefault().lookup(MyResolver.class));
        FileObject testFolder = FileUtil.createMemoryFileSystem().getRoot();

        FileObject fo = FileUtil.createData(testFolder, "fo1.mime1");
        String[] withinMIMETypes = null;
        try {
            FileUtil.getMIMEType(fo, withinMIMETypes);
            fail("FileUtil.getMIMEType(fo, null) should throw IllegalArgumentException.");
        } catch (NullPointerException npe) {
            // exception correctly thrown
        }
        
        fo = FileUtil.createData(testFolder, "fo2.mime1");
        withinMIMETypes = new String[0];
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertTrue("Resolver should be queried if array of desired MIME types is empty.", MyResolver.wasQueried());
        
        fo = FileUtil.createData(testFolder, "fo3.mime1");
        withinMIMETypes = new String[]{"mime3", "mime4"};
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertFalse("Resolver should not be queried if array of desired MIME types doesn't match MIMEResolver.getMIMETypes.", MyResolver.wasQueried());

        fo = FileUtil.createData(testFolder, "fo4.mime1");
        withinMIMETypes = new String[]{"mime1", "mime4"};
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertTrue("Resolver should be queried if one item in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.wasQueried());

        fo = FileUtil.createData(testFolder, "fo5.mime1");
        withinMIMETypes = new String[]{"mime1", "mime2"};
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertTrue("Resolver should be queried if both items in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.wasQueried());
    }

    /** MIMEResolver used in testGetMIMETypeConstrained. */
    public static final class MyResolver extends MIMEResolver {

        public MyResolver() {
            super("mime1", "mime2");
        }

        /** Always returns null and change value to signal it's been queried. */
        public String findMIMEType(FileObject fo) {
            queried = true;
            return null;
        }
        private static boolean queried = false;

        public static boolean wasQueried() {
            boolean wasQueried = queried;
            queried = false;
            return wasQueried;
        }
    }

    /** Test recovery of FileUtil.createFolder(FileObject, String) method when
     * other thread created folder in the middle of processing (see #152219).
     */
    public void testFolderAlreadyExists152219() {
        final FileObject folder = FileUtil.createMemoryFileSystem().getRoot();
        final String name = "subfolder";
        Handler handler = new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("createFolder - before create folder if not exists.")) {
                    try {
                        folder.createFolder(name);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        Logger logger = Logger.getLogger(FileUtil.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINEST);
        try {
            FileUtil.createFolder(folder, name);
        } catch (IOException ioe) {
            fail("FileUtil.createFolder(FileObject, String) should try to refresh folder because other thread can create folder before.");
        } finally {
            logger.removeHandler(handler);
        }
    }

    /** Tests FileUtil.setMIMEType method (see #153202). */
    public void testSetMIMEType() throws IOException {
        FileObject testRoot = FileUtil.createMemoryFileSystem().getRoot();
        FileObject g1FO = testRoot.createData("a", "g1");
        FileObject g2FO = testRoot.createData("a", "g2");
        FileObject xmlFO = testRoot.createData("a", "xml");
        String gifMIMEType = "image/gif";
        String bmpMIMEType = "image/bmp";
        String xmlMIMEType = "text/xml";
        String unknownMIMEType = "content/unknown";

        assertEquals("Wrong MIME type.", unknownMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(bmpMIMEType));
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(gifMIMEType));
        // xml registered to text/xml as fallback
        assertEquals("Wrong MIME type.", xmlMIMEType, xmlFO.getMIMEType());

         // {image/bmp=[g1]}
        FileUtil.setMIMEType("g1", bmpMIMEType);
        assertEquals("Wrong list of extensions.", Collections.singletonList("g1"), FileUtil.getMIMETypeExtensions(bmpMIMEType));
         // {image/bmp=[g1, g2]}
        FileUtil.setMIMEType("g2", bmpMIMEType);
        assertEquals("Wrong MIME type.", bmpMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong MIME type.", bmpMIMEType, g2FO.getMIMEType());
        assertTrue("Wrong list of extensions.", Arrays.asList("g1", "g2").containsAll(FileUtil.getMIMETypeExtensions(bmpMIMEType)));
         // {image/bmp=[g2], image/gif=[g1]}
        FileUtil.setMIMEType("g1", gifMIMEType);
        assertEquals("Wrong MIME type.", gifMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Arrays.asList("g1"), FileUtil.getMIMETypeExtensions(gifMIMEType));
        assertEquals("Wrong list of extensions.", Arrays.asList("g2"), FileUtil.getMIMETypeExtensions(bmpMIMEType));
         // {image/gif=[g1]}
        FileUtil.setMIMEType("g2", null);
        assertEquals("Wrong MIME type.", unknownMIMEType, g2FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Arrays.asList("g1"), FileUtil.getMIMETypeExtensions(gifMIMEType));
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(bmpMIMEType));
    }

    /** Tests getConfigFile method (see #91534). */
    public void testGetConfigFile() throws IOException {
        @SuppressWarnings("deprecation")
        FileObject rootDFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertNotNull("Sample FileObject not created.", rootDFS.createFolder("folder1").createFolder("folder2").createData("file.ext"));
        assertNotNull("Existing FileObject not found.", FileUtil.getConfigFile("folder1/folder2/file.ext"));
        assertNull("Path with backslashes is not valid.", FileUtil.getConfigFile("folder1\\folder2\\file.ext"));
        assertEquals("Root should be returned for empty path.", rootDFS, FileUtil.getConfigFile(""));
        assertEquals("Root should be returned from getConfigRoot", rootDFS, FileUtil.getConfigRoot());
        try {
            FileUtil.getConfigFile(null);
            fail("NullPointerException should be thrown for null path.");
        } catch (NullPointerException npe) {
            // OK
        }
    }

    /** Tests that refreshAll runs just once in time (see #170556). */
    @RandomlyFails // NB-Core-Build #4062: FileUtil.refreshAll not called. expected:<2> but was:<1>
    public void testRefreshConcurrency() throws Exception {
        Logger logger = Logger.getLogger(FileUtil.class.getName());
        logger.setLevel(Level.FINE);
        final AtomicInteger concurrencyCounter = new AtomicInteger(0);
        final AtomicInteger maxConcurrency = new AtomicInteger(0);
        final AtomicInteger calledCounter = new AtomicInteger(0);
        logger.addHandler(new Handler() {

            private boolean concurrentStarted = false;

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("refreshAll - started")) {
                    calledCounter.incrementAndGet();
                    concurrencyCounter.incrementAndGet();
                    if (!concurrentStarted) {
                        concurrentStarted = true;
                        new Thread("Concurrent refresh") {

                            @Override
                            public void run() {
                                FileUtil.refreshAll();
                            }
                        }.start();
                        synchronized (this) {
                            try {
                                wait(500);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                } else if (record.getMessage().equals("refreshAll - scheduled")) {
                    if (concurrentStarted) {
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                } else if (record.getMessage().equals("refreshAll - finished")) {
                    concurrencyCounter.decrementAndGet();
                    if (concurrencyCounter.get() > maxConcurrency.get()) {
                        maxConcurrency.set(concurrencyCounter.get());
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        FileUtil.refreshAll();
        assertEquals("FileUtil.refreshAll should not be called concurrently.", 0, maxConcurrency.get());
        assertEquals("FileUtil.refreshAll not called.", 2, calledCounter.get());
    }
}
