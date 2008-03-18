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
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * @author Jesse Glick
 */
public class FileUtilTest extends NbTestCase {

    public FileUtilTest(String n) {
        super(n);
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

}
