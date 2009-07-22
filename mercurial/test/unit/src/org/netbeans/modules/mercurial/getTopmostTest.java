/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class getTopmostTest extends NbTestCase {

    MercurialVCS mvcs;
    public getTopmostTest(String arg0) {
        super(arg0);
        mvcs = Lookup.getDefault().lookup(MercurialVCS.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearCachedValues();
    }

    public void testGetTopmostManagedParentR1R2() throws HgException, IOException {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".hg");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".hg");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);
        File tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);

        // test again - to get cached
        tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);
        tm1 = mvcs.getTopmostManagedAncestor(r1);
        assertEquals(r1, tm1);
        tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r2);
        assertEquals(r1, tm2);
    }

    public void testGetTopmostManagedParentR2R1() throws HgException, IOException {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".hg");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".hg");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        File tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);

        // test again - to get cached
        tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r2);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r1);
        assertEquals(r1, tm2);
    }

    public void testGetRepositoryRoot() throws HgException, IOException {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".hg");
        File r1f1 = createFile(r1,    "f1");
        File r2   = createFolder(r1,  "r2");
        File hgr2 = createFolder(r2,     ".hg");
        File r2f1 = createFile(r2,       "f1");
        File r2f2 = createFile(r2,       "f2");

        // test
        File tm1 = Mercurial.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        tm1 = Mercurial.getInstance().getRepositoryRoot(r1);
        assertEquals(r1, tm1);
        File tm2 = Mercurial.getInstance().getRepositoryRoot(r2f1);
        assertEquals(r2, tm2);
        tm2 = Mercurial.getInstance().getRepositoryRoot(r2f2);
        assertEquals(r2, tm2);
        tm2 = Mercurial.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, tm2);

        // test again - to get cached
        tm1 = Mercurial.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        tm1 = Mercurial.getInstance().getRepositoryRoot(r1);
        assertEquals(r1, tm1);
        tm2 = Mercurial.getInstance().getRepositoryRoot(r2f1);
        assertEquals(r2, tm2);
        tm2 = Mercurial.getInstance().getRepositoryRoot(r2f2);
        assertEquals(r2, tm2);
        tm2 = Mercurial.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, tm2);
    }

    public void testGetRepositoryRootDeeperFile() throws HgException, IOException {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".hg");
        File r1f1 = createFile(r1,    "f1");
        File r2   = createFolder(r1,  "r2");
        File hgr2 = createFolder(r2,     ".hg");
        File r2fo1 = createFolder(r2,       "f01");
        File r2fo1fo2 = createFolder(r2,       "f02");
        File r2fo1fo2f1 = createFile(r2fo1,       "f1");

        // test
        File rr = Mercurial.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1fo2);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1fo2f1);
        assertEquals(r2, rr);

        rr = Mercurial.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1fo2);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1);
        assertEquals(r2, rr);
        rr = Mercurial.getInstance().getRepositoryRoot(r2fo1fo2f1);
        assertEquals(r2, rr);                      
    }

    public void testFoldersToRootOverflow() throws Exception {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".hg");
        
        Map<File, File> m = getFoldersToRoots();

        // test
        File r1f1 = getFile(r1, "file", "s1", 100);
        File tm1 = Mercurial.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(101, m.size());

        r1f1 = getFile(r1, "file", "s2", 1500);
        tm1 = Mercurial.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(1601, m.size());

        r1f1 = getFile(r1, "file", "s3", 10);
        tm1 = Mercurial.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(11, m.size());
    }

    public void testGetRepositoryRootUnversioned() throws HgException, IOException {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".hg");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".hg");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = Mercurial.getInstance().getRepositoryRoot(getTempDir());
        assertNull(tm1);
    }

    public void testGetTompomostUnversioned() throws HgException, IOException {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".hg");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".hg");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = mvcs.getTopmostManagedAncestor(getTempDir());
        assertNull(tm1);
    }

    private void clearCachedValues() throws Exception {
        getFoldersToRoots().clear();
        getKnownRoots().clear();
    }

    private File createFile(File folder, String f) throws IOException {
        File file = new File(folder, f);
        file.createNewFile();
        file.deleteOnExit();
        return file;
    }

    private File createFolder(String f) throws IOException {
        return createFolder(getTempDir(), f);
    }

    private File createFolder(File parentfolder, String f) {
        File folder = new File(parentfolder, f);
        folder.mkdirs();
        folder.deleteOnExit();
        return folder;
    }

    private File getFile(File parentfolder, String f, String segment, int segments) {
        File file = new File(parentfolder, segment);
        for (int i = 2; i < segments; i++) {
            file = new File(file, segment);
        }
        return new File(file, f);
    }

    private static File tmp = null;

    private Map<File, File> getFoldersToRoots() throws SecurityException, IllegalArgumentException, Exception, IllegalAccessException {
        Field f = null;
        try {
            f = Mercurial.class.getDeclaredField("foldersToRoot");
        } catch (Exception ex) {
            throw ex;
        }
        f.setAccessible(true);
        Map<File, File> m = (Map<File, File>) f.get(Mercurial.getInstance());
        return m;
    }

    private Set<File> getKnownRoots() throws SecurityException, IllegalArgumentException, Exception, IllegalAccessException {
        Field f = null;
        try {
            f = MercurialVCS.class.getDeclaredField("knownRoots");
        } catch (Exception ex) {
            throw ex;
        }
        f.setAccessible(true);
        Set<File> m = (Set<File>) f.get(mvcs);
        return m;
    }

    private File getTempDir() {
        if(tmp == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            tmp = new File(tmpDir, "gtmt-" + Long.toString(System.currentTimeMillis()));
            tmp.deleteOnExit();
        }
        return tmp;
    }
}
