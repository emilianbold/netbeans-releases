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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.lucene;

/**
 *
 * @author Tomas Zezula
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {
        
    public LuceneIndexTest (String testName) {
        super (testName);                
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        //Prepare indeces
    }
                        
    public void testIsValid() throws Exception {
        final File wd = getWorkDir();
        final File cache = new File(wd,"cache");
        cache.mkdirs();
        final LuceneIndex index = LuceneIndex.create(cache, new KeywordAnalyzer());
        //Empty index => invalid
        assertEquals(Index.Status.EMPTY, index.getStatus(true));

        clearValidityCache(index);
        List<String> refs = new ArrayList<String>();
        refs.add("A");
        Set<String> toDel = new HashSet<String>();
        index.store(
                refs,
                toDel,
                new StrToDocConvertor("resources"),
                new StrToQueryCovertor("resource"),
                true);
        //Existing index => valid
        assertEquals(Index.Status.VALID, index.getStatus(true));
        assertTrue(cache.listFiles().length>0);

        clearValidityCache(index);
        createLock(index);
        //Index with orphan lock => invalid
        assertEquals(Index.Status.INVALID, index.getStatus(true));
        assertTrue(cache.listFiles().length==0);

        refs.add("B");
        clearValidityCache(index);
        index.store(
                refs,
                toDel,
                new StrToDocConvertor("resources"),
                new StrToQueryCovertor("resource"),
                true);
        assertEquals(Index.Status.VALID, index.getStatus(true));
        assertTrue(cache.listFiles().length>0);

        //Broken index => invalid
        clearValidityCache(index);
        File bt = null;
        for (File file : cache.listFiles()) {
            if (file.getName().endsWith(".cfs")) {
                bt = file;
                break;
            }
        }
        assertNotNull(bt);
        FileOutputStream out = new FileOutputStream(bt);
        try {
            out.write(new byte[] {0,0,0,0,0,0,0,0,0,0}, 0, 10);
        } finally {
            out.close();
        }
        assertEquals(Index.Status.INVALID, index.getStatus(true));
        assertTrue(cache.listFiles().length==0);

    }


    private void createLock(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field dirCache = li.getDeclaredField("dirCache");   //NOI18N
        dirCache.setAccessible(true);
        Object  o = dirCache.get(index);
        final java.lang.reflect.Field directory = o.getClass().getDeclaredField("fsDir");   //NOI18N
        directory.setAccessible(true);
        Directory dir = (Directory) directory.get(o);
        dir.makeLock("test").obtain();   //NOI18N
    }


    private void clearValidityCache(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field dirCache = li.getDeclaredField("dirCache");   //NOI18N
        dirCache.setAccessible(true);
        Object  o = dirCache.get(index);
        final java.lang.reflect.Field reader = o.getClass().getDeclaredField("reader");
        reader.setAccessible(true);
        IndexReader r = (IndexReader) reader.get(o);
        if (r != null) {
            r.close();
        }
        reader.set(o,null);
    }
    
    private static class StrToDocConvertor implements Convertor<String, Document>{
        
        private final String name;
        
        public StrToDocConvertor(final String name) {
            this.name = name;
        }
        
        @Override
        public Document convert(final String p) {
            final Document doc = new Document();
            doc.add(new Field(name, p, Field.Store.YES, Field.Index.ANALYZED));
            return doc;
        }        
    }
    
    private static class StrToQueryCovertor implements Convertor<String, Query> {
        
        private final String name;
        
        public StrToQueryCovertor(final String name) {
            this.name = name;
        }
        
        @Override
        public Query convert(String p) {
            return new TermQuery(new Term(name, p));
        }        
    }
    
}

