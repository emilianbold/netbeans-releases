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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Tomas Stupka
 */
public class CacheIndexTest extends NbTestCase {
    private File workDir;
    private File dataRootDir;
    private File repoDir;
    private File wc;

    public CacheIndexTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        workDir = FileUtils.createTmpFolder("cacheindex");

        dataRootDir = new File(System.getProperty("data.root.dir"));
        repoDir = new File(dataRootDir, "repo");
        FileUtils.deleteRecursively(repoDir);
        TestKit.initRepo(repoDir, workDir);
        wc = new File(dataRootDir, getName() + "_wc");

        initRepo();
        wc.mkdirs();
        svnimport();

        System.setProperty("svnClientAdapterFactory", "commandline");
        System.setProperty("netbeans.user", dataRootDir.getAbsolutePath());
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpWC(wc);
        FileUtils.deleteRecursively(wc);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testAddToIndex() throws MalformedURLException, SVNClientException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        TestKit.commit(wc);

        Map<String, Set<String>> index = getIndex();
        index.clear();

        File root = new File(wc, "root");
        
        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        File folder112 = new File(folder11, "folder112");
        folder111.mkdirs();
        folder112.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        File file111_3 = new File(folder111, "file111_3");
        File file112_1 = new File(folder112, "file112_1");
        file111_1.createNewFile();
        file111_2.createNewFile();
        file111_3.createNewFile();
        file112_1.createNewFile();

        // add folder11 -> all versioned parents will be added
        CacheIndex.getInstance().addToIndex(folder11);
        assertEquals(3, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});

        // add file111_1 -> all versioned parents will be added
        CacheIndex.getInstance().addToIndex(file111_1);
        assertEquals(5, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));
        
        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath()});

        // add file111_1 one more time -> the structure won't change
        CacheIndex.getInstance().addToIndex(file111_1);
        assertEquals(5, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath()});

        // add file111_2 -> the parent structure won't change as they are already there
        CacheIndex.getInstance().addToIndex(file111_2);
        assertEquals(5, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath(), file111_2.getAbsolutePath()});

        // add file112_1 -> the parent structure won't change as they are already there
        CacheIndex.getInstance().addToIndex(file112_1);
        assertEquals(6, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));
        assertTrue(index.containsKey(folder112.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath(), folder112.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath(), file111_2.getAbsolutePath()});
        assertValueSet(index.get(folder112.getAbsolutePath()), new String[] {file112_1.getAbsolutePath()});

    }

    public void testAdjustIndex() throws MalformedURLException, SVNClientException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        TestKit.commit(wc);

        Map<String, Set<String>> index = getIndex();
        index.clear();
        
        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        folder111.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        File file111_3 = new File(folder111, "file111_3");
        file111_1.createNewFile();
        file111_2.createNewFile();
        file111_3.createNewFile();

        // add file111_1, file111_2, file111_3 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_2);
        s.add(file111_3);
        CacheIndex.getInstance().adjustIndex(folder111.getAbsolutePath(), s);

        assertEquals(5, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath(), file111_2.getAbsolutePath(), file111_3.getAbsolutePath()});

        // add file111_1, file111_3 -> all versioned parents will be added
        s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_3);
        CacheIndex.getInstance().adjustIndex(folder111.getAbsolutePath(), s);

        assertEquals(5, index.keySet().size());

        assertTrue(index.containsKey(wc.getAbsolutePath()));
        assertTrue(index.containsKey(root.getAbsolutePath()));
        assertTrue(index.containsKey(folder1.getAbsolutePath()));
        assertTrue(index.containsKey(folder11.getAbsolutePath()));
        assertTrue(index.containsKey(folder111.getAbsolutePath()));

        assertValueSet(index.get(wc.getAbsolutePath()), new String[] {root.getAbsolutePath()});
        assertValueSet(index.get(root.getAbsolutePath()), new String[] {folder1.getAbsolutePath()});
        assertValueSet(index.get(folder1.getAbsolutePath()), new String[] {folder11.getAbsolutePath()});
        assertValueSet(index.get(folder11.getAbsolutePath()), new String[] {folder111.getAbsolutePath()});
        assertValueSet(index.get(folder111.getAbsolutePath()), new String[] {file111_1.getAbsolutePath(), file111_3.getAbsolutePath()});

        // add empty set -
        s = new HashSet<File>();
        CacheIndex.getInstance().adjustIndex(folder111.getAbsolutePath(), s);
        assertEquals(0, index.keySet().size());

    }

    private void assertValueSet(Set<String> s, String... expectedValues) {
        assertEquals(expectedValues.length, s.size());
        for (String ev : expectedValues) {
            assertTrue(s.contains(ev));
        }
    }

    private Map<String, Set<String>> getIndex() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = CacheIndex.class.getDeclaredField("index");
        f.setAccessible(true);
        return (Map<String, Set<String>>) f.get(CacheIndex.getInstance());
    }

    private void cleanUpWC(File wc) throws IOException {
        if(wc.exists()) {
            File[] files = wc.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(!file.getName().equals("cache")) { // do not delete the cache
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.delete();
                        }
                    }
                }
            }
        }
    }

    private void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {
        TestKit.initRepo(repoDir, wc);
    }

    private void svnimport() throws SVNClientException, MalformedURLException {
        TestKit.svnimport(repoDir, wc);
    }

}
