/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class FileObjectCrawlerTest extends NbTestCase {

    private static final CancelRequest CR = new CancelRequest() {
        public boolean isRaised() {
            return false;
        }
    };

    public FileObjectCrawlerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws IOException {
        clearWorkDir();
        File wd = getWorkDir();
        final FileObject wdFO = FileUtil.toFileObject(wd);
        final FileObject cache = FileUtil.createFolder(wdFO, "cache");

        CacheFolder.setCacheFolder(cache);
    }

    public void testIncludesExcludes() throws IOException {
        final FileObject src = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "src");
        assertNotNull(src);

        populateFolderStructure(new File(getWorkDir(), "src"),
            "p1/Included1.java",
            "p1/Included2.java",
            "p1/a/Included3.java",
            "p1/a/Included4.java",
            "p2/Excluded1.java",
            "p2/Excluded2.java",
            "p2/a/Excluded3.java",
            "p2/a/Excluded4.java"
        );

        ClassPath cp = ClassPathSupport.createClassPath(Arrays.asList(new FilteringPathResourceImplementation() {
            private final Pattern p = Pattern.compile("p1/.*");

            public boolean includes(URL root, String resource) {
                return p.matcher(resource).matches();
            }

            public URL[] getRoots() {
                try {
                    return new URL[]{src.getURL()};
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public ClassPathImplementation getContent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        }));

        FileObjectCrawler crawler = new FileObjectCrawler(src, false, cp.entries().get(0), CR);
        assertCollectedFiles("Wrong files collected", crawler.getAllResources(),
                "p1/Included1.java",
                "p1/Included2.java",
                "p1/a/Included3.java",
                "p1/a/Included4.java"
        );
    }

    public void testRelativePaths() throws IOException {
        File root = new File(getWorkDir(), "src");
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(FileUtil.toFileObject(root), false, null, CR);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);
        
        FileObject folder = FileUtil.toFileObject(new File(root, "org/pckg1/pckg2"));
        FileObjectCrawler crawler2 = new FileObjectCrawler(FileUtil.toFileObject(root), new FileObject [] { folder }, false, null, CR);
        assertCollectedFiles("Wrong files collected from " + folder, crawler2.getAllResources(),
            "org/pckg1/pckg2/file1.txt",
            "org/pckg1/pckg2/file2.txt"
        );
    }

    public void testDeletedFiles() throws IOException {
        File root = new File(getWorkDir(), "src");
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(FileUtil.toFileObject(root), false, null, CR);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);

        FileObject pckg2 = FileUtil.toFileObject(new File(root, "org/pckg1/pckg2"));
        FileObject org = FileUtil.toFileObject(new File(root, "org"));
        org.delete();

        FileObjectCrawler crawler2 = new FileObjectCrawler(FileUtil.toFileObject(root), new FileObject [] { pckg2 }, false, null, CR);
        assertCollectedFiles("There should be no files in " + root, crawler2.getAllResources());

        FileObjectCrawler crawler3 = new FileObjectCrawler(FileUtil.toFileObject(root), false, null, CR);
        assertCollectedFiles("There should be no files in " + root, crawler1.getAllResources());
        assertCollectedFiles("All files in " + root + " should be deleted", crawler1.getDeletedResources());
    }

    protected void assertCollectedFiles(String message, Collection<IndexableImpl> resources, String... expectedPaths) throws IOException {
        Set<String> collectedPaths = new HashSet<String>();
        for(IndexableImpl ii : resources) {
            collectedPaths.add(ii.getRelativePath());
        }
        Set<String> expectedPathsFiltered = new HashSet<String>();
        for(String path : expectedPaths) {
            if (!path.endsWith("/")) { // crawler only collects files
                expectedPathsFiltered.add(path);
            }
        }
        assertEquals(message, expectedPathsFiltered, collectedPaths);
    }

    private static void populateFolderStructure(File root, String... filesOrFolders) throws IOException {
        root.mkdirs();
        for(String fileOrFolder : filesOrFolders) {
            if (fileOrFolder.endsWith("/")) {
                // folder
                File folder = new File(root, fileOrFolder.substring(0, fileOrFolder.length() - 1));
                folder.mkdirs();
            } else {
                // file
                File file = new File(root, fileOrFolder);
                File folder = file.getParentFile();
                folder.mkdirs();
                FileUtil.createData(FileUtil.toFileObject(folder), file.getName());
            }
        }
    }
}