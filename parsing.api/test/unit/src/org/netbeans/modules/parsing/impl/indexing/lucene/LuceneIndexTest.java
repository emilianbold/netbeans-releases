/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;
import java.util.Collection;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexableImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {

    private static File wd;
    private File indexFolder;
    private LuceneIndex index;
    
    public LuceneIndexTest(final String name) {
        super(name);
    }

    @Before
    public void setUp() throws IOException {
        clearWorkDir();
        wd = getWorkDir();
        indexFolder = new File (wd, "index");   //NOI18N
        indexFolder.mkdirs();
        index = new LuceneIndex(indexFolder.toURI().toURL());
    }

    @After
    public void tearDown() throws IOException {
        index.close();
    }

    @Test
    public void testIndexAddDelete() throws Exception {
        for (int i=0; i< 1000; i++) {
            LuceneDocument docwrap = new LuceneDocument(SPIAccessor.getInstance().create(new FakeIndexableImpl(i)));
            docwrap.addPair("bin", Integer.toBinaryString(i), true, true);
            docwrap.addPair("oct", Integer.toOctalString(i), true, true);
            index.addDocument(docwrap);
        }
        index.store();
        BitSet expected = new BitSet(1000);
        expected.set(0, 1000);
        assertIndex(expected);
        for (int i = 100; i<200; i++) {
            index.removeDocument(Integer.toString(i));
            expected.clear(i);
        }
        index.store();
        assertIndex(expected);
    }

    @Test
    public void testIsValid() throws Exception {
        //Empty index => valid
        assertTrue(index.isValid());

        clearValidityCache();
        LuceneDocument docwrap = new LuceneDocument(SPIAccessor.getInstance().create(new FakeIndexableImpl(1)));
        docwrap.addPair("bin", Integer.toBinaryString(1), true, true);
        docwrap.addPair("oct", Integer.toOctalString(1), true, true);
        index.addDocument(docwrap);
        index.store();
        //Existing index => valid
        assertTrue(index.isValid());
        assertTrue(indexFolder.listFiles().length>0);

        clearValidityCache();
        createLock();
        //Index with orphan lock => invalid
        assertFalse(index.isValid());
        assertTrue(indexFolder.listFiles().length==0);

        clearValidityCache();
        docwrap = new LuceneDocument(SPIAccessor.getInstance().create(new FakeIndexableImpl(1)));
        docwrap.addPair("bin", Integer.toBinaryString(1), true, true);
        docwrap.addPair("oct", Integer.toOctalString(1), true, true);
        index.addDocument(docwrap);
        index.store();
        assertTrue(index.isValid());
        assertTrue(indexFolder.listFiles().length>0);

        //Broken index => invalid
        clearValidityCache();
        File[] files = indexFolder.listFiles();
        FileOutputStream out = new FileOutputStream(files[0]);
        try {
            out.write(new byte[] {0,0,0,0,0,0,0,0,0,0}, 0, 10);
        } finally {
            out.close();
        }
        assertFalse(index.isValid());
        assertTrue(indexFolder.listFiles().length==0);
        
    }

    private void clearValidityCache() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final Field valid = li.getDeclaredField("valid");   //NOI18N
        valid.setAccessible(true);
        valid.set(index, false);
        final Field reader = li.getDeclaredField("reader");   //NOI18N
        reader.setAccessible(true);
        IndexReader r = (IndexReader) reader.get(index);
        if (r != null) {
            r.close();
        }
        reader.set(index,null);
    }

    private void createLock() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final Field directory = li.getDeclaredField("directory");   //NOI18N
        directory.setAccessible(true);
        Directory dir = (Directory) directory.get(index);
        dir.makeLock("test").obtain();   //NOI18N
    }


    private void assertIndex(final BitSet expected) throws IOException {
        for (int i=0; i < expected.length(); i++) {
            final Collection<? extends IndexDocumentImpl> res = index.query("bin", Integer.toBinaryString(i), Kind.EXACT, "bin","oct");
            boolean should = expected.get(i);
            assertEquals(should, res.size()==1);
            if (should) {
                assertEquals(res.iterator().next().getValue("bin"), Integer.toBinaryString(i));
                assertEquals(res.iterator().next().getValue("oct"), Integer.toOctalString(i));
            }
        }
    }


    private static class FakeIndexableImpl implements IndexableImpl {

        private final int id;

        public FakeIndexableImpl (final int id) {
            this.id = id;
        }

        public String getRelativePath() {
            return Integer.toString(id);
        }

        public URL getURL() {
            try {
                return new File(wd, getRelativePath()).toURI().toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        public String getMimeType() {
            return "text/test";
        }

        public boolean isTypeOf(String mimeType) {
            return true;
        }

    }

}
