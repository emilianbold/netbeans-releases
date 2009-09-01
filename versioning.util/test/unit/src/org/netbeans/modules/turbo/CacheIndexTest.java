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

package org.netbeans.modules.turbo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author Tomas Stupka
 */
public class CacheIndexTest extends NbTestCase {
    private File workDir;
    private File wc;

    public CacheIndexTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        workDir = new File(System.getProperty("work.dir"));
        wc = new File(workDir, getName() + "_wc");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testAddFile() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
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
        ci.add(folder11);
        checkParents(index, folder11);
        
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});

        // add file111_1 -> all versioned parents will be added
        ci.add(file111_1);
        checkParents(index, file111_1);
        
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add file111_1 one more time -> the structure won't change
        ci.add(file111_1);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add file111_2 -> the parent structure won't change as they are already there
        ci.add(file111_2);
        checkParents(index, file111_1, file111_2);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});

        // add file112_1 -> the parent structure won't change as they are already there
        ci.add(file112_1);
        checkParents(index, file111_1, file111_2, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});
        assertValueSet(index.get(folder112), new File[] {file112_1});

    }

    public void testAddFiles() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
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

        // add file111_1, file111_2, file111_3 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_2);
        s.add(file111_3);
        ci.add(folder111, s);
        checkParents(index, file111_1, file111_2, file111_3);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2, file111_3});

        // add file111_1, file111_3 -> all versioned parents will be added
        s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_3);
        ci.add(folder111, s);
        checkParents(index, file111_1, file111_3);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_3});

        // add file112_1
        s = new HashSet<File>();
        s.add(file112_1);
        ci.add(folder112, s);

        checkParents(index, file111_1, file111_3, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_3});
        assertValueSet(index.get(folder112), new File[] {file112_1});        
    }

    public void testAddRemoveLastNode() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        folder111.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        file111_1.createNewFile();

        // add file111_1
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        ci.add(folder111, s);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add empty set 
        s = new HashSet<File>();
        ci.add(folder111, s);

        assertEquals(0, index.keySet().size());
    }

    public void testAddRemovePartFromLastNodes() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        folder111.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        file111_1.createNewFile();

        // add file111_1
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_2);
        ci.add(folder111, s);

        checkParents(index, file111_1, file111_2);
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});

        // add empty set
        s = new HashSet<File>();
        s.add(file111_1); // file111_2 will be removed
        ci.add(folder111, s);

        checkParents(index, file111_1);
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1}); // only file111_1, file111_2 was removed
    }

    public void testAddRemovePartFromNode() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 =   new File(root,      "folder1");
        File folder11 =  new File(folder1,     "folder11");
        File folder111 = new File(folder11,     "folder111");
        File file111_1 = new File(folder111,      "file111_1");
        File folder112 = new File(folder11,     "folder112");
        File file112_1 = new File(folder112,      "file112_1");
        folder111.mkdirs();
        folder112.mkdirs();
        file111_1.createNewFile();
        file112_1.createNewFile();

        // add file111_1, file112_1 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        ci.add(folder111, s);
        s.clear();
        s.add(file112_1);
        ci.add(folder112, s);
        checkParents(index, file111_1, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1});
        assertValueSet(index.get(folder112), new File[] {file112_1});

        // remove folder111 -> nothing will be removed - file111_1 still there
        s = new HashSet<File>();
        s.add(folder112);
        ci.add(folder11, s);
        checkParents(index, file111_1, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1});
        assertValueSet(index.get(folder112), new File[] {file112_1});

        // remove file111_1 -> parent folder111 will be also removed
        s = new HashSet<File>();        
        ci.add(folder111, s);
        checkParents(index, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder112});
        assertValueSet(index.get(folder112), new File[] {file112_1});
    }

    private void assertValueSet(Set<File> s, File... expectedValues) {
        assertEquals(expectedValues.length, s.size());
        for (File ev : expectedValues) {
            if(!s.contains(ev)) {
                fail("expected value " + ev + " not found in value set");
            }
        }
    }

    private void checkParents(Map<File, Set<File>> index, File... files) {
        Set<File> parents = new HashSet<File>();

        for (File file : files) {
            File parent = file;
            while((parent = parent.getParentFile()) != null) {
                parents.add(parent);
                if(parent.equals(wc)) {
                    break;
                }
            }
            assert parent != null;
        }

        assertEquals(parents.size(), index.keySet().size());
        for (File p : parents) {
            assertTrue(index.containsKey(p));
        }
    }

    private Map<File, Set<File>> getIndex(CacheIndex ci) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = CacheIndex.class.getDeclaredField("index");
        f.setAccessible(true);
        return (Map<File, Set<File>>) f.get(ci);
    }

    private class CIndex extends CacheIndex {
        @Override
        protected boolean isManaged(File file) {
            boolean ancestorOrEqual = Utils.isAncestorOrEqual(wc, file);
            return ancestorOrEqual;
        }
    }

}
