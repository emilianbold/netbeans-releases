/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.test.TestFileUtils;
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
    
    /** Tests normalizeFile() method. */
    public void testNormalizeFile() throws IOException {
        System.out.println("java.version="+System.getProperty("java.version"));
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
        assertTrue("Resolver should be queried if array of desired MIME types is empty.", MyResolver.QUERIED.equals(FileUtil.getMIMEType(fo, withinMIMETypes)));
        
        fo = FileUtil.createData(testFolder, "fo3.mime1");
        withinMIMETypes = new String[]{"mime3", "mime4"};
        assertFalse("Resolver should not be queried if array of desired MIME types doesn't match MIMEResolver.getMIMETypes.", MyResolver.QUERIED.equals(FileUtil.getMIMEType(fo, withinMIMETypes)));

        fo = FileUtil.createData(testFolder, "fo4.mime1");
        withinMIMETypes = new String[]{"mime1", "mime4"};
        assertTrue("Resolver should be queried if one item in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.QUERIED.equals(FileUtil.getMIMEType(fo, withinMIMETypes)));

        fo = FileUtil.createData(testFolder, "fo5.mime1");
        withinMIMETypes = new String[]{"mime1", "mime2"};
        assertTrue("Resolver should be queried if both items in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.QUERIED.equals(FileUtil.getMIMEType(fo, withinMIMETypes)));
    }

    /** MIMEResolver used in testGetMIMETypeConstrained. */
    public static final class MyResolver extends MIMEResolver {

        public static final String QUERIED = "QUERIED";
        
        public MyResolver() {
            super("mime1", "mime2");
        }
        
        /** Always returns the same just to signal it's been queried. */
        public String findMIMEType(FileObject fo) {
            return QUERIED;
        }
    }
}
