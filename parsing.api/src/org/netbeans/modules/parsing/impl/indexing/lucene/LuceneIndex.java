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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndex implements IndexImpl {

    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N

    static final int VERSION = 1;

    private File indexFolder;

    private Directory directory;
    //@GuardedBy (this)
    private IndexReader reader; //Cache, do not use this dirrectly, use getReader
    private volatile boolean closed;

    private final List<LuceneDocument> toAdd = new LinkedList<LuceneDocument>();
    private final List<String> toRemove = new LinkedList<String>();

    public LuceneIndex (final URL root) throws IOException {
        assert root != null;
        try {
            indexFolder = new File (root.toURI());
            directory = FSDirectory.getDirectory(indexFolder,NoLockFactory.getNoLockFactory());
        } catch (URISyntaxException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void addDocument(final IndexDocumentImpl document) {
        assert document instanceof LuceneDocument;       
        toAdd.add((LuceneDocument)document);
    }

    public void removeDocument(final String relativePath) {
        toRemove.add(relativePath);
    }

    public void store() throws IOException {
        try {
            checkPreconditions();
            //assert ClassIndexManager.getDefault().holdsWriteLock();
            //1) delete all documents from to delete and toAdd
            final boolean create = !isValid (false);
            if (!create) {
                IndexReader in = getReader();
                final Searcher searcher = new IndexSearcher (in);
                try {
                    for (Iterator<String> it = toRemove.iterator(); it.hasNext();) {
                        String toRemoveItem = it.next();
                        it.remove();
                        deleteFile (in, searcher, toRemoveItem);
                    }
                    for (LuceneDocument toRemoveItem : toAdd) {
                        deleteFile(in, searcher, toRemoveItem.getSourceName());
                    }
                } finally {
                    searcher.close();
                }
            }
            //2) add all documents form to add
            final IndexWriter out = getWriter(create);
            try {
                if (debugIndexMerging) {
                    out.setInfoStream (System.err);
                }

                LMListener lmListener = new LMListener ();
                Directory memDir = null;
                IndexWriter activeOut = null;
                if (lmListener.isLowMemory()) {
                    activeOut = out;
                }
                else {
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true);
                }                
                for (Iterator<LuceneDocument> it = toAdd.iterator(); it.hasNext();) {
                    final LuceneDocument doc = it.next();
                    it.remove();
                    activeOut.addDocument(doc.doc);
                    if (memDir != null && lmListener.isLowMemory()) {
                        activeOut.close();
                        out.addIndexes(new Directory[] {memDir});
                        memDir = new RAMDirectory ();
                        activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true);
                    }
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});
                    activeOut = null;
                    memDir = null;
                }
                
            } finally {
                out.close();
            }
        } finally {
            toRemove.clear();
            toAdd.clear();
        }
    }


    private static void deleteFile (final IndexReader in,
            final Searcher searcher,
            final String toRemoveItem) throws IOException {
        Hits hits = searcher.search(DocumentUtil.sourceNameQuery(toRemoveItem));
        //Create copy of hists
        int[] dindx = new int[hits.length()];
        int dindxLength = 0;
        for (int i=0; i<dindx.length; i++) {
            dindx[dindxLength++] = hits.id(i);
        }
        for (int i=0; i<dindxLength; i++) {
            in.deleteDocument (dindx[i]);
        }
    }


    public synchronized void close () throws IOException {
        try {
            if (this.reader != null) {
                this.reader.close();
                this.reader = null;
            }
        } finally {
           this.directory.close();
           this.closed = true;
        }
    }
    
    private void checkPreconditions () {
        if (closed) {
            //throw new ClassIndexImpl.IndexAlreadyClosedException();
        }
    }

    private boolean isValid (boolean tryOpen) throws IOException {
        checkPreconditions();
        boolean res = false;
        try {
            res = IndexReader.indexExists(this.directory);
        } catch (IOException e) {
            return res;
        }
        if (res && tryOpen) {
            try {
                getReader();
            } catch (java.io.IOException e) {
                res = false;
                clear();
            }
        }
        return res;
    }

    public synchronized void clear () throws IOException {
        checkPreconditions();
        this.close ();
        try {
            final String[] content = this.directory.list();
            boolean dirty = false;
            for (String file : content) {
                try {
                    directory.deleteFile(file);
                } catch (IOException e) {
                    //Some temporary files
                    if (directory.fileExists(file)) {
                        dirty = true;
                    }
                }
            }
            if (dirty) {
                //Try to delete dirty files and log what's wrong
                final File cacheDir = ((FSDirectory)this.directory).getFile();
                final File[] children = cacheDir.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        if (!child.delete()) {
                            final Class c = this.directory.getClass();
                            int refCount = -1;
                            try {
                                final Field field = c.getDeclaredField("refCount");
                                field.setAccessible(true);
                                refCount = field.getInt(this.directory);
                            } catch (NoSuchFieldException e) {/*Not important*/}
                              catch (IllegalAccessException e) {/*Not important*/}

                            throw new IOException("Cannot delete: " + child.getAbsolutePath() + "(" +   //NOI18N
                                    child.exists()  +","+                                               //NOI18N
                                    child.canRead() +","+                                               //NOI18N
                                    child.canWrite() +","+                                              //NOI18N
                                    cacheDir.canRead() +","+                                            //NOI18N
                                    cacheDir.canWrite() +","+                                           //NOI18N
                                    refCount+")");                                                      //NOI18N
                        }
                    }
                }
            }
        } finally {
            //Need to recreate directory, see issue: #148374
            this.directory = FSDirectory.getDirectory(indexFolder, NoLockFactory.getNoLockFactory());      //Locking controlled by rwlock
            closed = false;
        }
    }

    private synchronized IndexReader getReader () throws IOException {
        if (this.reader == null) {
            //Issue #149757 - logging
            try {
                //It's important that no Query will get access to original IndexReader
                //any norms call to it will initialize the HashTable of norms: sizeof (byte) * maxDoc() * max(number of unique fields in document)
                this.reader = new NoNormsReader(IndexReader.open(this.directory));
            } catch (IOException ioe) {
                throw annotateException (ioe);
            }
        }
        return this.reader;
    }

    private synchronized IndexWriter getWriter (final boolean create) throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        //Issue #149757 - logging
        try {
            IndexWriter writer = new IndexWriter (this.directory, new KeywordAnalyzer(), create);
            return writer;
        } catch (IOException ioe) {
            throw annotateException (ioe);
        }
    }

    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+indexFolder.getAbsolutePath()+"]";  //NOI18N
    }


    private IOException annotateException (final IOException ioe) {
        String message;
        File[] children = indexFolder.listFiles();
        if (children == null) {
            message = "Non existing index folder";
        }
        else {
            StringBuilder b = new StringBuilder();
            for (File c : children) {
                b.append(c.getName() +" f: " + c.isFile() + " r: " + c.canRead() + " w: " + c.canWrite()+"\n");  //NOI18N
            }
            message = b.toString();
        }
        return Exceptions.attachMessage(ioe, message);
    }

    /**
     * Expert: Bypass read of norms
     */
    private static class NoNormsReader extends FilterIndexReader {


        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] norms = fakeNorms ();
            return norms;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] norms = fakeNorms ();
            System.arraycopy(norms, 0, norm, offset, norms.length);
        }

        @Override
        public boolean hasNorms(String field) throws IOException {
            return false;
        }

        @Override
        protected void doSetNorm(int doc, String field, byte norm) throws CorruptIndexException, IOException {
            //Ignore
        }

        @Override
        protected void doClose() throws IOException {
            synchronized (this)  {
                this.norms = null;
            }
            super.doClose();
        }

        /**
         * Expert: Fakes norms, norms are not needed for Netbeans index.
         */
        private synchronized byte[] fakeNorms() {
            if (this.norms == null) {
                this.norms = new byte[maxDoc()];
                Arrays.fill(this.norms, DefaultSimilarity.encodeNorm(1.0f));
            }
            return this.norms;
        }
    }

}
