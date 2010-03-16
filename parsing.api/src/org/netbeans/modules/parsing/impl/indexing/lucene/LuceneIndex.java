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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.impl.indexing.lucene.util.Evictable;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndex implements IndexImpl, Evictable {

    private static final RequestProcessor RP = new RequestProcessor(LuceneIndex.class.getName(),1);

    // -----------------------------------------------------------------------
    // IndexImpl implementation
    // -----------------------------------------------------------------------
    
    /**
     * Adds document
     * @param document
     */
    public void addDocument(final IndexDocumentImpl document) {
        final boolean forceFlush;

        synchronized (this) {
            assert document instanceof LuceneDocument;
            toAdd.add((LuceneDocument) document);
            forceFlush = lmListener.isLowMemory();
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, null);
                System.gc();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, annotateException(ioe, indexFolder));
            }
        }
    }

    /**
     * Removes all documents for given path
     * @param relativePath
     */
    public void removeDocument(final String relativePath) {
        final boolean forceFlush;

        synchronized (this) {
            toRemove.add(relativePath);
            forceFlush = lmListener.isLowMemory();
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, null);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, annotateException(ioe, indexFolder));
            }
        }
    }
    
    public void store(final boolean optimize, final Iterable<Indexable> indexedIndexables) throws IOException {
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                checkPreconditions();
                
                final List<LuceneDocument> toAdd;
                final List<String> toRemove;

                synchronized (LuceneIndex.this) {
                    toAdd = new LinkedList<LuceneDocument>(LuceneIndex.this.toAdd);
                    toRemove = new LinkedList<String>(LuceneIndex.this.toRemove);

                    LuceneIndex.this.toAdd.clear();
                    LuceneIndex.this.toRemove.clear();
                    if (indexedIndexables != null) {
                        for(Indexable i : indexedIndexables) {
                            LuceneIndex.this.staleFiles.remove(i.getRelativePath());
                        }
                    } else {
                        for(LuceneDocument ldoc : toAdd) {
                            LuceneIndex.this.staleFiles.remove(ldoc.getSourceName());
                        }
                        LuceneIndex.this.staleFiles.removeAll(toRemove);
                    }
                }

                if (toAdd.size() > 0 || toRemove.size() > 0) {
                    flush(indexFolder, toAdd, toRemove, LuceneIndex.this.directory, lmListener, optimize);
                }
                
                return null;
            }
        });
    }

    public Collection<? extends IndexDocumentImpl> query(
            final String fieldName,
            final String value,
            final QuerySupport.Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        assert fieldName != null;
        assert value != null;
        assert kind != null;
        
        return LuceneIndexManager.getDefault().readAccess(new LuceneIndexManager.Action<List<IndexDocumentImpl>>() {
            public List<IndexDocumentImpl> run() throws IOException {
                checkPreconditions();

                final IndexReader r = getReader();
                if (r != null) {
                    // index exists
                    return _query(r, fieldName, value, kind, fieldsToLoad);
                } else {
                    // no index
                    return Collections.<IndexDocumentImpl>emptyList();
                }
            }
        });        
    }

    public void fileModified(String relativePath) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(this + ", adding stale file: " + relativePath); //NOI18N
            }
            staleFiles.add(relativePath);
        }
    }

    public Collection<? extends String> getStaleFiles() {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(this + ", stale files: " + staleFiles); //NOI18N
            }
            return new LinkedList<String>(staleFiles);
        }
    }
    
    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public LuceneIndex(final URL indexFolderUrl) throws IOException {
        assert indexFolderUrl != null;
        try {
            this.indexFolderUrl = indexFolderUrl;
            indexFolder = new File(indexFolderUrl.toURI());
            directory = createDirectory(indexFolder);
        } catch (URISyntaxException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void clear() throws IOException {
        checkPreconditions();
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                _clear();
                return null;
            }
        });
    }

    /**
     * Checks if the lucene index is valid.
     * @return true when index is valid
     * @throws IOException when index is already closed
     */
    public boolean isValid () throws IOException {
        checkPreconditions();
        boolean res = LuceneIndexManager.getDefault().readAccess(new LuceneIndexManager.Action<Boolean>() {
            public Boolean run() throws IOException {
                return LuceneIndex.this.valid;
            }
        });
        if (res) {
            return res;
        }
        res = LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Boolean>() {
            public Boolean run() throws IOException {                
                boolean res = directory.list().length == 0;
                if (!res) {
                    final Collection<? extends String> locks = getOrphanLock();
                    res = locks.isEmpty();
                    if (res) {
                        try {
                            res = IndexReader.indexExists(directory);
                        } catch (IOException e) {
                            //Directory does not exist, no need to call clear
                            res = false;
                        } catch (RuntimeException e) {
                            LOGGER.log(Level.INFO, "Broken index: " + indexFolder.getAbsolutePath(), e);
                            res = false;
                        }
                        if (res) {
                            try {
                                getReader();
                            } catch (java.io.IOException e) {
                                res = false;
                                clear();
                            } catch (RuntimeException e) {
                                res = false;
                                clear();
                            }
                        }
                    }
                    else {
                        LOGGER.warning("Broken (locked) index folder: " + indexFolder.getAbsolutePath());   //NOI18N
                        for (String lockName : locks) {
                            directory.deleteFile(lockName);
                        }
                        clear();
                    }
                }
                LuceneIndex.this.valid = res;
                return res;
            }
        });
        return res;
    }
    
    public void close() throws IOException {
        checkPreconditions();
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                _close();
                return null;
            }
        });
    }
        
    //<editor-fold desc="Implementation of Evictable interface">
    public void evicted() {
        //Threading: The called may own the LIM.readAccess, perform by dedicated worker to prevent deadlock
        RP.post(new Runnable() {
            public void run () {
                try {
                    LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
                        public Void run() throws IOException {
                            _closeReader();
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Evicted index: " + indexFolder.getAbsolutePath()); //NOI18N
                            }
                            return null;
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    ///</editor-fold>

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N

    /* package */ static final int VERSION = 1;
    private static final String CACHE_LOCK_PREFIX = "nb-lock";  //NOI18N

    private final URL indexFolderUrl;
    private final File indexFolder;

    //@GuardedBy (LuceneIndexManager.writeAccess)
    private volatile Directory directory;
    private volatile IndexReader reader; //Cache, do not use this directly, use getReader
    private volatile boolean closed;
    private boolean valid;

    private static final LMListener lmListener = new LMListener();

    //@GuardedBy (this)
    private final List<LuceneDocument> toAdd = new LinkedList<LuceneDocument>();
    private final List<String> toRemove = new LinkedList<String>();
    private final Set<String> staleFiles = new HashSet<String>();

    private void _hit() {
        IndexCacheFactory.getDefault().getCache().put(indexFolderUrl, this);
    }

    // called under LuceneIndexManager.writeAccess
    private void _clear() throws IOException {
        _closeReader();
        try {
            boolean dirty = false;
            try {
                final String[] content = this.directory.list();                
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
            } finally {
                _closeDirectory();
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
                                final Field field = c.getDeclaredField("refCount"); //NOI18N
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
            this.directory = createDirectory(indexFolder);
            closed = false;
        }
    }

    // called under LuceneIndexManager.writeAccess
    private void _close() throws IOException {
        try {
            _closeReader();
        } finally {
           _closeDirectory();
        }
    }

    // called under LuceneIndexManager.writeAccess
    private void _closeReader() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    // called under LuceneIndexManager.writeAccess
    private void _closeDirectory() throws IOException {
        directory.close();
        closed = true;
    }



    // called under LuceneIndexManager.readAccess
    private static List<IndexDocumentImpl> _query(
            final IndexReader in,
            final String fieldName,
            final String value,
            final QuerySupport.Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        
        final List<IndexDocumentImpl> result = new LinkedList<IndexDocumentImpl>();
        final Set<Term> toSearch = new TreeSet<Term> (new TermComparator());

        switch (kind) {
            case EXACT:
                {
                    toSearch.add(new Term (fieldName,value));
                    break;
                }
            case PREFIX:
                if (value.length() == 0) {
                    if (fieldName.length() == 0) {
                        //Special case (all) handle in different way
                        emptyPrefixSearch(in, fieldsToLoad, result);
                        return result;
                    } else {
                        final Term nameTerm = new Term (fieldName, value);
                        fieldSearch(nameTerm, in, toSearch);
                        break;
                    }
                }
                else {
                    final Term nameTerm = new Term (fieldName, value);
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CASE_INSENSITIVE_PREFIX:
                if (value.length() == 0) {
                    if (fieldName.length() == 0) {
                        //Special case (all) handle in different way
                        emptyPrefixSearch(in, fieldsToLoad, result);
                        return result;
                    } else {
                        final Term nameTerm = new Term (fieldName, value);
                        fieldSearch(nameTerm, in, toSearch);
                        break;
                    }
                }
                else {
                    final Term nameTerm = new Term (fieldName,value.toLowerCase());     //XXX: I18N, Locale
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CAMEL_CASE:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                {
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);

                    final Pattern pattern = Pattern.compile(sb.toString());
                    regExpSearch(pattern, new Term (fieldName,prefix),in,toSearch);
                }
                break;
            case CASE_INSENSITIVE_REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                else {
                    final Pattern pattern = Pattern.compile(value,Pattern.CASE_INSENSITIVE);
                    if (Character.isJavaIdentifierStart(value.charAt(0))) {
                        regExpSearch(pattern, new Term (fieldName, value.toLowerCase()), in, toSearch);      //XXX: Locale
                    }
                    else {
                        regExpSearch(pattern, new Term (fieldName,""), in, toSearch);      //NOI18N
                    }
                    break;
                }
            case REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    final Pattern pattern = Pattern.compile(value);
                    if (Character.isJavaIdentifierStart(value.charAt(0))) {
                        regExpSearch(pattern, new Term (fieldName, value), in, toSearch);
                    }
                    else {
                        regExpSearch(pattern, new Term(fieldName,""), in, toSearch);             //NOI18N
                    }
                    break;
                }
            case CASE_INSENSITIVE_CAMEL_CASE:
                if (value.length() == 0) {
                    if (fieldName.length() == 0) {
                        //Special case (all) handle in different way
                        emptyPrefixSearch(in, fieldsToLoad, result);
                        return result;
                    } else {
                        final Term nameTerm = new Term (fieldName, value);
                        fieldSearch(nameTerm, in, toSearch);
                        break;
                    }
                }
                else {
                    final Term nameTerm = new Term(fieldName,value.toLowerCase());     //XXX: I18N, Locale
                    prefixSearch(nameTerm, in, toSearch);
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);
                    final Pattern pattern = Pattern.compile(sb.toString());
                    regExpSearch(pattern,new Term (fieldName, prefix),in,toSearch);
                    break;
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }
        final TermDocs tds = in.termDocs();
        final Set<Integer> docNums = new TreeSet<Integer>();
        try {
            int[] docs = new int[25];
            int[] freq = new int [25];
            int len;
            for(Term t : toSearch) {
                tds.seek(t);
                while ((len = tds.read(docs, freq))>0) {
                    for (int i = 0; i < len; i++) {
                        docNums.add (docs[i]);
                    }
                }
            }
        } finally {
            tds.close();
        }
        final FieldSelector selector = DocumentUtil.selector(fieldsToLoad);
        for (Integer docNum : docNums) {
            final Document doc = in.document(docNum, selector);
            result.add (new LuceneDocument(doc));
        }
        return result;
    }
    
    // called under LuceneIndexManager.writeAccess
    // Always has to invalidate the cached reader
    private void flush(File indexFolder, List<LuceneDocument> toAdd, List<String> toRemove, Directory directory, LMListener lmListener, final boolean optimize) throws IOException {
        LOGGER.log(Level.FINE, "Flushing: {0}", indexFolder); //NOI18N
        try {
            assert LuceneIndexManager.getDefault().holdsWriteLock();
            _hit();
            boolean exists = IndexReader.indexExists(this.directory);
            final IndexWriter out = new IndexWriter(
                directory, // index directory                
                new KeywordAnalyzer(), //analyzer to tokenize fields
                !exists, // open existing or create new index
                IndexWriter.MaxFieldLength.LIMITED
            );
            try {
                //1) delete all documents from to delete and toAdd
                if (exists) {
                    for (Iterator<String> it = toRemove.iterator(); it.hasNext();) {
                        String toRemoveItem = it.next();
                        it.remove();
                        out.deleteDocuments(DocumentUtil.sourceNameQuery(toRemoveItem));
                    }
                    for (LuceneDocument toRemoveItem : toAdd) {
                        out.deleteDocuments(DocumentUtil.sourceNameQuery(toRemoveItem.getSourceName()));
                    }
                }
            
                //2) add all documents form to add
                if (debugIndexMerging) {
                    out.setInfoStream (System.err);
                }

                Directory memDir = null;
                IndexWriter activeOut = null;
                if (lmListener.isLowMemory()) {
                    activeOut = out;
                }
                else {
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
                }
                for (Iterator<LuceneDocument> it = toAdd.iterator(); it.hasNext();) {
                    final LuceneDocument doc = it.next();
                    it.remove();
                    activeOut.addDocument(doc.doc);
                    if (memDir != null && lmListener.isLowMemory()) {
                        activeOut.close();
                        out.addIndexesNoOptimize(new Directory[] {memDir});
                        memDir = new RAMDirectory ();
                        activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
                    }
                    LOGGER.log(Level.FINEST, "LuceneDocument merged: {0}", doc); //NOI18N
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexesNoOptimize(new Directory[] {memDir});
                    activeOut = null;
                    memDir = null;
                }
                if (optimize) {
                    out.optimize(false);
                }
            } finally {
                try {
                    out.close();
                } finally {
                    refreshReader();
                }
            }
        } catch (final LockObtainFailedException e) {
            final String msg = "Valid: " + valid + " Locks: " + getOrphanLock();    //NOI18N
            throw Exceptions.attachMessage(e, msg);
        } finally {
            LOGGER.log(Level.FINE, "Index flushed: {0}", indexFolder); //NOI18N
        }
    }

    // called under LuceneIndexManager.readAccess
    private void checkPreconditions() throws IOException {
        if (closed) {
            throw new IOException("Index already closed: " + indexFolder); //NOI18N
        }
    }

    // called under LuceneIndexManager.readAccess or LuceneIndexManager.writeAccess
    private IndexReader getReader() throws IOException {
        _hit();
        synchronized (this) {
            IndexReader r = reader;
            if (r == null) {
                boolean exists = IndexReader.indexExists(this.directory);
                if (exists) {
                    //Issue #149757 - logging
                    try {
                        //It's important that no Query will get access to original IndexReader
                        //any norms call to it will initialize the HashTable of norms: sizeof (byte) * maxDoc() * max(number of unique fields in document)
                        r = reader = new NoNormsReader(IndexReader.open(this.directory));
                    } catch (IOException ioe) {
                        throw annotateException(ioe, indexFolder);
                    }
                } else {
                    LOGGER.fine(String.format("LuceneIndex[%s] does not exist.", this.toString())); //NOI18N
                }
            }
            return r;
        }
    }

    private synchronized void refreshReader() throws IOException {
        if (reader != null) {
            final IndexReader newReader = reader.reopen();
            if (newReader != reader) {
                reader.close();
                reader = newReader;
            }
        }
    }

    private static Directory createDirectory(final File indexFolder) throws IOException {
        assert indexFolder != null;
        FSDirectory directory  = FSDirectory.getDirectory(indexFolder);
        directory.getLockFactory().setLockPrefix(CACHE_LOCK_PREFIX);
        return directory;
    }

    private Collection<? extends String> getOrphanLock () {
        final String[] content = indexFolder.list();
        final List<String> locks = new LinkedList<String>();
        for (String name : content) {
            if (name.startsWith(CACHE_LOCK_PREFIX)) {
                locks.add(name);
            }
        }
        return locks;
    }


    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+indexFolder.getAbsolutePath()+"]";  //NOI18N
    }

    private static IOException annotateException (final IOException ioe, final File indexFolder) {
        String message;
        File[] children = indexFolder == null ? null : indexFolder.listFiles();
        if (children == null) {
            message = "Non existing index folder"; //NOI18N
        }
        else {
            StringBuilder b = new StringBuilder();
            b.append("Index folder: ").append(indexFolder.getAbsolutePath()).append("\n"); //NOI18N
            for (File c : children) {
                b.append(c.getName()).append(" f: ").append(c.isFile()) //NOI18N
                    .append(" r: ").append(c.canRead()) //NOI18N
                    .append(" w: ").append(c.canWrite()) //NOI18N
                    .append("\n");  //NOI18N
            }
            message = b.toString();
        }
        return Exceptions.attachMessage(ioe, message);
    }

    private static void emptyPrefixSearch (final IndexReader in, final String[] fieldsToLoad, final List<? super IndexDocumentImpl> result) throws IOException {
        final int bound = in.maxDoc();
        for (int i=0; i<bound; i++) {
            if (!in.isDeleted(i)) {
                final Document doc = in.document(i, DocumentUtil.selector(fieldsToLoad));
                if (doc != null) {
                    result.add (new LuceneDocument(doc));
                }
            }
        }
    }

    private static void fieldSearch (final Term valueTerm, final IndexReader in, final Set<? super Term> toSearch) throws IOException {
        final Object prefixField = valueTerm.field(); // It's Object only to silence the stupid hint
        final TermEnum en = in.terms(valueTerm);
        try {
            do {
                Term term = en.term();
                if (term != null && prefixField == term.field()) {
                    toSearch.add (term);
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    private static void prefixSearch (final Term valueTerm, final IndexReader in, final Set<? super Term> toSearch) throws IOException {
        final Object prefixField = valueTerm.field(); // It's Object only to silence the stupid hint
        final String name = valueTerm.text();
        final TermEnum en = in.terms(valueTerm);
        try {
            do {
                Term term = en.term();
                if (term != null && prefixField == term.field() && term.text().startsWith(name)) {
                    toSearch.add (term);
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    private static void regExpSearch (final Pattern pattern, Term startTerm, final IndexReader in, final Set< ? super Term> toSearch) throws IOException {
        final String startText = startTerm.text();
        String startPrefix;
        if (startText.length() > 0) {
            final StringBuilder startBuilder = new StringBuilder ();
            startBuilder.append(startText.charAt(0));
            for (int i=1; i<startText.length(); i++) {
                char c = startText.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                startBuilder.append(c);
            }
            startPrefix = startBuilder.toString();
            startTerm = new Term (startTerm.field(),startPrefix);
        }
        else {
            startPrefix=startText;
        }
        final Object camelField = startTerm.field(); // It's Object only to silence the stupid hint
        final TermEnum en = in.terms(startTerm);
        try {
            do {
                Term term = en.term();
                if (term != null && camelField == term.field() && term.text().startsWith(startPrefix)) {
                    final Matcher m = pattern.matcher(term.text());
                    if (m.matches()) {
                        toSearch.add (term);
                    }
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    private static int findNextUpper(String text, int offset ) {

        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Expert: Bypass read of norms
     */
    private static final class NoNormsReader extends FilterIndexReader {


        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] fakes = fakeNorms ();
            return fakes;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] fakes = fakeNorms ();
            System.arraycopy(fakes, 0, norm, offset, fakes.length);
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

        @Override
        public IndexReader reopen() throws IOException {
            final IndexReader newIn = in.reopen();
            if (newIn == in) {
                return this;
            }
            return new NoNormsReader(newIn);
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
    } // End of NoNormsReader class

}
