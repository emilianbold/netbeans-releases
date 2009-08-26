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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.java.source.usages.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {
    
    //Copied from DocumentUtil, nneds to be synchronized when changed
    private static final String FIELD_RESOURCE_NAME = "resourceName";   //NOI18N
    private static final String FIELD_REFERENCES = "references";        //NOI18N
    
    private static final int REF_SIZE = UsageType.values().length;
    
    private File indexFolder1;
    private File indexFolder2;
    
    public LuceneIndexTest (String testName) {
        super (testName);                
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        //Prepare indeces
    }
    
    public void testIndeces () throws Exception {
        if (indexFolder1 != null && indexFolder2 != null) {
            assertTrue (indexFolder1.isDirectory());
            assertTrue (indexFolder1.canRead());
            assertTrue (IndexReader.indexExists(indexFolder1));
            assertTrue (indexFolder2.isDirectory());
            assertTrue (indexFolder2.canRead());
            assertTrue (IndexReader.indexExists(indexFolder2));
            compareIndeces (indexFolder1, indexFolder2);
        }
    }

    public void testHitsBug () throws Exception {
        File root = getWorkDir();
        root.mkdirs();
        Directory dir = FSDirectory.getDirectory(root);
        final IndexWriter w = new IndexWriter(dir, new KeywordAnalyzer(),true);
        try {
           for (int i=0; i<200; i++) {
               Field field = new Field("KEY","value"+i,Field.Store.YES,Field.Index.NO_NORMS);
               Document doc = new Document();
               doc.add(field);
               w.addDocument(doc);
           }
        } finally {
            w.close();
        }
        final IndexReader in = IndexReader.open(dir);
        try {
            final Searcher searcher = new IndexSearcher (in);
            try {
                Hits hits = searcher.search(new  PrefixQuery((new Term("KEY", "value1"))));
                for (int j=0; j<hits.length(); j++) {
                    in.deleteDocument (hits.id(j));
                }
            } finally {
                searcher.close();
            }
        } finally {
            in.close();
        }
    }
    
    
    private static void compareIndeces (final File file1, final File file2) throws IOException {
        Directory dir1 = FSDirectory.getDirectory(file1);
        Directory dir2 = FSDirectory.getDirectory(file2);
        IndexReader in1 = IndexReader.open(dir1);
        try {
            IndexReader in2 = IndexReader.open(dir2);
            try {
                int len1 = in1.numDocs();
                int len2 = in2.numDocs();
                assertEquals("Indeces have different size",len1, len2);
                Searcher search = new IndexSearcher(in2);
                try {
                    for (int i=0; i< len1; i++) {
                        Document doc1 = in1.document(i);
                        String name1 = doc1.getField(FIELD_RESOURCE_NAME).stringValue();
                        if (name1.equals("/")) {
                            continue;
                        }
                        Hits hits = search.search(new TermQuery( new Term(FIELD_RESOURCE_NAME,name1)));
                        assertEquals("No document for " + name1, 1, len2);                        
                        Document doc2 = ((Hit)hits.iterator().next()).getDocument();
                        compare(doc1,doc2);
                    }
                } finally {
                    search.close();
                }
            } finally {
                in2.close();
            }
        } finally {
            in1.close();
        }
    }
    
    private static void compare (Document doc1, Document doc2) throws IOException {                        
        Field[] f1 = doc1.getFields(FIELD_REFERENCES);
        Field[] f2 = doc2.getFields(FIELD_REFERENCES);
        assertEquals("Reference size not equal for:" + doc1 + " and: " + doc2, f1.length,f2.length);
        Map<String,String> m1 = fill (f1);
        Map<String,String> m2 = fill (f2);
        for (Map.Entry<String,String> e : m1.entrySet()) {
            String key = e.getKey();
            String value1 = e.getValue();
            String value2= m2.get(key);
            assertNotNull("Unknown reference: " + key,value2);            
            assertEquals("Different usage types",value1,value2);            
        }
    }
    
    private static Map<String,String> fill (Field[] fs) {
        Map<String,String> m1 = new HashMap<String, String> ();
        for (Field f : fs) {
            String ru = f.stringValue();
            int index = ru.length() - REF_SIZE;
            String key = ru.substring(0,index);
            String value = ru.substring(index);
            m1.put (key, value);
        }
        return m1;
    }

    public void testIsValid() throws Exception {
        final File wd = getWorkDir();
        final File cache = new File(wd,"cache");
        final File indexFolder = new File (cache,"refs");
        cache.mkdirs();
        final LuceneIndex index = (LuceneIndex) LuceneIndex.create(cache);
        //Empty index => invalid
        assertFalse(index.isValid(true));

        clearValidityCache(index);
        Map<Pair<String,String>,Object[]> refs = new HashMap<Pair<String,String>,Object[]>();
        List<String> xref = new LinkedList<String>();
        String sym = "";
        String ident = "";
        refs.put(Pair.<String,String>of("A", null), new Object[]{xref,sym,ident});
        Set<Pair<String,String>> toDel = new HashSet<Pair<String,String>>();
        index.store(refs, toDel);       
        //Existing index => valid
        assertTrue(index.isValid(true));
        assertTrue(indexFolder.listFiles().length>0);

        clearValidityCache(index);
        createLock(index);
        //Index with orphan lock => invalid
        assertFalse(index.isValid(true));
        assertTrue(indexFolder.listFiles().length==0);

        clearValidityCache(index);
        index.store(refs, toDel);
        assertTrue(index.isValid(true));
        assertTrue(indexFolder.listFiles().length>0);

        //Broken index => invalid
        clearValidityCache(index);
        File bt = null;;
        for (File file : indexFolder.listFiles()) {
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
        assertFalse(index.isValid(true));
        assertTrue(indexFolder.listFiles().length==0);

    }


    private void createLock(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field directory = li.getDeclaredField("directory");   //NOI18N
        directory.setAccessible(true);
        Directory dir = (Directory) directory.get(index);
        dir.makeLock("test").obtain();   //NOI18N
    }


    private void clearValidityCache(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field reader = li.getDeclaredField("reader");   //NOI18N
        reader.setAccessible(true);
        IndexReader r = (IndexReader) reader.get(index);
        if (r != null) {
            r.close();
        }
        reader.set(index,null);
    }
    
}
