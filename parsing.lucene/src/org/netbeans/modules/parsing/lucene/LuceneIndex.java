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

import org.apache.lucene.store.NoLockFactory;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
//@NotTreadSafe
public class LuceneIndex implements Index {

    private static final String PROP_INDEX_POLICY = "java.index.useMemCache";   //NOI18N
    private static final String PROP_CACHE_SIZE = "java.index.size";    //NOI18N
    private static final boolean debugIndexMerging = Boolean.getBoolean("java.index.debugMerge");     // NOI18N
    private static final CachePolicy DEFAULT_CACHE_POLICY = CachePolicy.DYNAMIC;
    private static final float DEFAULT_CACHE_SIZE = 0.05f;
    private static final CachePolicy cachePolicy = getCachePolicy();
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    private static final FieldSelector ALL_FIELDS = new AllFieldsSelector();
    
    private static LockFactory lockFactory;
    
    private final DirCache dirCache;       
    
    /** unit tests */
    public static void setDisabledLocks(final boolean disabled) {
        lockFactory = disabled ? NoLockFactory.getNoLockFactory() : null;
    }

    public static LuceneIndex create (final File cacheRoot, final Analyzer analyzer) throws IOException {
        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        return new LuceneIndex (cacheRoot, analyzer);
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot, final Analyzer analyzer) throws IOException {
        assert refCacheRoot != null;
        assert analyzer != null;
        this.dirCache = new DirCache(refCacheRoot,cachePolicy, analyzer);
    }
    
    @Override
    public <T> void query (
            final @NonNull Collection<? super T> result,
            final @NonNull Convertor<? super Document, T> convertor,
            @NullAllowed FieldSelector selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries
            ) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);   //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        Parameters.notNull("result", result);       //NOI18N        
        final IndexReader in = dirCache.getReader();
        if (in == null) {
            LOGGER.log(Level.FINE, "{0} is invalid!", this);
            return;
        }
        if (selector == null) {
            selector = ALL_FIELDS;
        }
        final BitSet bs = new BitSet(in.maxDoc());
        final Collector c = new BitSetCollector(bs);
        final Searcher searcher = new IndexSearcher(in);
        try {
            for (Query q : queries) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                searcher.search(q, c);
            }
        } finally {
            searcher.close();
        }        
        for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
            if (cancel != null && cancel.get()) {
                throw new InterruptedException ();
            }
            final Document doc = in.document(docNum, selector);
            final T value = convertor.convert(doc);
            if (value != null) {
                result.add (value);
            }
        }
    }
    
    @Override
    public <T> void queryTerms(
            final @NonNull Collection<? super T> result,
            final @NullAllowed Term seekTo,
            final @NonNull StoppableConvertor<Term,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        
        final IndexReader in = dirCache.getReader();
        if (in == null) {
            return;
        }

        final TermEnum terms = seekTo == null ? in.terms () : in.terms (seekTo);        
        try {
            do {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                final Term currentTerm = terms.term();
                if (currentTerm != null) {                    
                    final T vote = filter.convert(currentTerm);
                    if (vote != null) {
                        result.add(vote);
                    }
                }
            } while (terms.next());
        } catch (StoppableConvertor.Stop stop) {
            //Stop iteration of TermEnum
        } finally {
            terms.close();
        }
    }
    
    @Override
    public <S, T> void queryDocTerms(
            final @NonNull Map<? super T, Set<S>> result,
            final @NonNull Convertor<? super Document, T> convertor,
            final @NonNull Convertor<? super Term, S> termConvertor,
            @NullAllowed FieldSelector selector,
            final @NullAllowed AtomicBoolean cancel,
            final @NonNull Query... queries) throws IOException, InterruptedException {
        Parameters.notNull("queries", queries);             //NOI18N
        Parameters.notNull("slector", selector);            //NOI18N
        Parameters.notNull("convertor", convertor);         //NOI18N
        Parameters.notNull("termConvertor", termConvertor); //NOI18N
        Parameters.notNull("result", result);               //NOI18N
        final IndexReader in = dirCache.getReader();
        if (in == null) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));   //NOI18N
            return;
        }
        if (selector == null) {
            selector = ALL_FIELDS;
        }
        final BitSet bs = new BitSet(in.maxDoc());
        final Collector c = new BitSetCollector(bs);
        final Searcher searcher = new IndexSearcher(in);
        final TermCollector termCollector = new TermCollector();
        try {
            for (Query q : queries) {
                if (cancel != null && cancel.get()) {
                    throw new InterruptedException ();
                }
                if (q instanceof TermCollector.TermCollecting) {
                    ((TermCollector.TermCollecting)q).attach(termCollector);
                } else {
                    throw new IllegalArgumentException (
                            String.format("Query: %s does not implement TermCollecting",    //NOI18N
                            q.getClass().getName()));
                }
                searcher.search(q, c);
            }
        } finally {
            searcher.close();
        }
        
        boolean logged = false;
        for (int docNum = bs.nextSetBit(0); docNum >= 0; docNum = bs.nextSetBit(docNum+1)) {
            if (cancel != null && cancel.get()) {
                throw new InterruptedException ();
            }
            final Document doc = in.document(docNum, selector);
            final T value = convertor.convert(doc);
            if (value != null) {
                final Set<Term> terms = termCollector.get(docNum);
                if (terms != null) {
                    result.put (value, convertTerms(termConvertor, terms));
                } else {
                    if (!logged) {
                        LOGGER.log(Level.WARNING, "Index info [maxDoc: {0} numDoc: {1} docs: {2}]",
                                new Object[] {
                                    in.maxDoc(),
                                    in.numDocs(),
                                    termCollector.docs()
                                });
                        logged = true;
                    }
                    LOGGER.log(Level.WARNING, "No terms found for doc: {0}", docNum);
                }
            }
        }
    }
    
    private static <T> Set<T> convertTerms(final Convertor<? super Term, T> convertor, final Set<? extends Term> terms) {
        final Set<T> result = new HashSet<T>(terms.size());
        for (Term term : terms) {
            result.add(convertor.convert(term));
        }
        return result;
    }

    @Override
    public <S, T> void store (
            final @NonNull Collection<T> toAdd,
            final @NonNull Collection<S> toDelete,
            final @NonNull Convertor<? super T, ? extends Document> docConvertor,
            final @NonNull Convertor<? super S, ? extends Query> queryConvertor,
            final boolean optimize) throws IOException{
        try {
            IndexManager.writeAccess(new IndexManager.Action<Void>() {
                @Override
                public Void run() throws IOException, InterruptedException {
                    _store(toAdd, toDelete, docConvertor, queryConvertor, optimize);
                    return null;
                }
            });
        } catch (InterruptedException ie) {
            throw new IOException("Interrupted");   //NOI18N
        }
    }

    private <S, T> void _store (
            final @NonNull Collection<T> data,
            final @NonNull Collection<S> toDelete,
            final @NonNull Convertor<? super T, ? extends Document> docConvertor,
            final @NonNull Convertor<? super S, ? extends Query> queryConvertor,
            final boolean optimize) throws IOException {
        assert IndexManager.holdsWriteLock();
        boolean create = !exists();
        final IndexWriter out = dirCache.getWriter(create);
        try {
            if (!create) {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
            }            
            if (debugIndexMerging) {
                out.setInfoStream (System.err);
            }                
            final LowMemoryWatcher lmListener = LowMemoryWatcher.getInstance();
            Directory memDir = null;
            IndexWriter activeOut = null;
            if (lmListener.isLowMemory()) {
                activeOut = out;
            }
            else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir, dirCache.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
            }
            for (Iterator<T> it = data.iterator(); it.hasNext();) {
                T entry = it.next();
                it.remove();
                final Document doc = docConvertor.convert(entry);
                activeOut.addDocument(doc);
                if (memDir != null && lmListener.isLowMemory()) {
                    activeOut.close();
                    out.addIndexesNoOptimize(new Directory[] {memDir});
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (memDir, dirCache.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
                }
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
        } catch (RuntimeException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } catch (IOException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } finally {
            try {
                out.close();
            } finally {
                dirCache.refreshReader();
            }
        }
    }
        
    @Override
    public boolean isValid (boolean force) throws IOException {
        return dirCache.isValid(force);
    }

    @Override
    public void clear () throws IOException {
        try {
            IndexManager.writeAccess(new IndexManager.Action<Void>() {
                @Override
                public Void run() throws IOException, InterruptedException {
                    dirCache.clear();
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public boolean exists () {
        return this.dirCache.exists();
    }

    @Override
    public void close () throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Closing index: {0} {1}",  //NOI18N
                    new Object[]{
                        this.dirCache.toString(),
                        Thread.currentThread().getStackTrace()});
        }
        dirCache.close(true);
    }


    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+this.dirCache.toString()+"]";  //NOI18N
    }    
           
    private static CachePolicy getCachePolicy() {
        final String value = System.getProperty(PROP_INDEX_POLICY);   //NOI18N
        if (Boolean.TRUE.toString().equals(value) ||
            CachePolicy.ALL.getSystemName().equals(value)) {
            return CachePolicy.ALL;
        }
        if (Boolean.FALSE.toString().equals(value) ||
            CachePolicy.NONE.getSystemName().equals(value)) {
            return CachePolicy.NONE;
        }
        if (CachePolicy.DYNAMIC.getSystemName().equals(value)) {
            return CachePolicy.DYNAMIC;
        }
        return DEFAULT_CACHE_POLICY;
    }
    

    //<editor-fold defaultstate="collapsed" desc="Private classes (NoNormsReader, TermComparator, CachePolicy)">
    
    private static class AllFieldsSelector implements FieldSelector {
        @Override
        public FieldSelectorResult accept(final String fieldName) {
            return FieldSelectorResult.LOAD;
        }
    }
    
    private static class NoNormsReader extends FilterIndexReader {

        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] _norms = fakeNorms ();
            return _norms;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] _norms = fakeNorms ();
            System.arraycopy(_norms, 0, norm, offset, _norms.length);
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
    }
        
    private enum CachePolicy {
        
        NONE("none", false),          //NOI18N
        DYNAMIC("dynamic", true),     //NOI18N
        ALL("all", true);             //NOI18N
        
        private final String sysName;
        private final boolean hasMemCache;
        
        CachePolicy(final String sysName, final boolean hasMemCache) {
            assert sysName != null;
            this.sysName = sysName;
            this.hasMemCache = hasMemCache;
        }
        
        String getSystemName() {
            return sysName;
        }
        
        boolean hasMemCache() {
            return hasMemCache;
        }
    }    
    
    private static final class DirCache implements Evictable {
        
        private static final String CACHE_LOCK_PREFIX = "nb-lock";  //NOI18N
        private static final RequestProcessor RP = new RequestProcessor(LuceneIndex.class.getName(), 1);
        private static final long maxCacheSize = getCacheSize();
        private static volatile long currentCacheSize;
        
        private final File folder;
        private final CachePolicy cachePolicy;
        private final Analyzer analyzer;
        private FSDirectory fsDir;
        private RAMDirectory memDir;
        private CleanReference ref;
        private IndexReader reader;
        private volatile boolean closed;
        private volatile Boolean validCache;
        
        private DirCache(
                final @NonNull File folder,
                final @NonNull CachePolicy cachePolicy,
                final @NonNull Analyzer analyzer) throws IOException {
            assert folder != null;
            assert cachePolicy != null;
            assert analyzer != null;
            this.folder = folder;
            this.fsDir = createFSDirectory(folder);
            this.cachePolicy = cachePolicy;                        
            this.analyzer = analyzer;
        }
        
        Analyzer getAnalyzer() {
            return this.analyzer;
        }
                                
        synchronized void clear() throws IOException {
            checkPreconditions();
            close (false);
            try {
                final String[] content = fsDir.listAll();
                boolean dirty = false;
                if (content != null) {
                    for (String file : content) {
                        try {
                            fsDir.deleteFile(file);
                        } catch (IOException e) {
                            //Some temporary files
                            if (fsDir.fileExists(file)) {
                                dirty = true;
                            }
                        }
                    }
                }
                if (dirty) {
                    //Try to delete dirty files and log what's wrong
                    final File cacheDir = fsDir.getFile();
                    final File[] children = cacheDir.listFiles();
                    if (children != null) {
                        for (final File child : children) {                                                
                            if (!child.delete()) {
                                final Class c = fsDir.getClass();
                                int refCount = -1;
                                try {
                                    final Field field = c.getDeclaredField("refCount"); //NOI18N
                                    field.setAccessible(true);
                                    refCount = field.getInt(fsDir);
                                } catch (NoSuchFieldException e) {/*Not important*/}
                                  catch (IllegalAccessException e) {/*Not important*/}
                                final Map<Thread,StackTraceElement[]> sts = Thread.getAllStackTraces();
                                throw new IOException("Cannot delete: " + child.getAbsolutePath() + "(" +   //NOI18N
                                        child.exists()  +","+                                               //NOI18N
                                        child.canRead() +","+                                               //NOI18N
                                        child.canWrite() +","+                                              //NOI18N
                                        cacheDir.canRead() +","+                                            //NOI18N
                                        cacheDir.canWrite() +","+                                           //NOI18N
                                        refCount +","+                                                      //NOI18N
                                        sts +")");                                                          //NOI18N
                            }
                        }
                    }
                }
            } finally {
                //Need to recreate directory, see issue: #148374
                this.close(true);
                this.fsDir = createFSDirectory(this.folder);
                closed = false;
            }
        }
        
        synchronized void close (final boolean closeFSDir) throws IOException {
            try {
                try {
                    if (this.reader != null) {
                        this.reader.close();
                        this.reader = null;
                    }
                } finally {                        
                    if (memDir != null) {
                        assert cachePolicy.hasMemCache();
                        if (this.ref != null) {
                            this.ref.clear();
                        }
                        final Directory tmpDir = this.memDir;
                        memDir = null;
                        tmpDir.close();
                    }
                }
            } finally {
                if (closeFSDir) {
                    this.closed = true;
                    this.fsDir.close();
                }
            }
        }
        
        boolean exists() {
            try {
                return IndexReader.indexExists(this.fsDir);
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                LOGGER.log(Level.INFO, "Broken index: " + folder.getAbsolutePath(), e);
                return false;
            }
        }
        
        boolean isValid(boolean force) throws IOException {
            checkPreconditions();
            Boolean valid = validCache;
            if (force ||  valid == null) {
                final Collection<? extends String> locks = getOrphanLock();
                boolean res = false;
                if (!locks.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Broken (locked) index folder: {0}", folder.getAbsolutePath());   //NOI18N
                    for (String lockName : locks) {
                        fsDir.deleteFile(lockName);
                    }
                    if (force) {
                        clear();
                    }
                } else {
                    res = exists();
                    if (res && force) {
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
                valid = res;
                validCache = valid;
            }
            return valid;
        }
        
        IndexWriter getWriter (final boolean create) throws IOException {
            checkPreconditions();
            hit();
            //Issue #149757 - logging
            try {
                return new IndexWriter (this.fsDir, analyzer, create, IndexWriter.MaxFieldLength.LIMITED);
            } catch (IOException ioe) {
                throw annotateException (ioe);
            }
        }
        
        synchronized IndexReader getReader () throws IOException {
            checkPreconditions();
            hit();
            if (this.reader == null) {
                if (validCache == Boolean.FALSE) {
                    return null;
                }
                //Issue #149757 - logging
                try {
                    Directory source;
                    if (cachePolicy.hasMemCache()) {                        
                        memDir = new RAMDirectory(fsDir);
                        if (cachePolicy == CachePolicy.DYNAMIC) {
                            ref = new CleanReference (new RAMDirectory[] {this.memDir});
                        }
                        source = memDir;
                    } else {
                        source = fsDir;
                    }
                    assert source != null;
                    this.reader = new NoNormsReader(IndexReader.open(source,true));
                } catch (final FileNotFoundException fnf) {
                    //pass - returns null
                } catch (IOException ioe) {
                    throw annotateException (ioe);
                }
            }
            return this.reader;
        }


        synchronized void refreshReader() throws IOException {
            try {
                if (cachePolicy.hasMemCache()) {
                    close(false);
                } else {
                    if (reader != null) {
                        final IndexReader newReader = reader.reopen();
                        if (newReader != reader) {
                            reader.close();
                            reader = newReader;
                        }
                    }
                }
            } finally {
                 validCache = true;
            }
        }
        
        @Override
        public String toString() {
            return this.folder.getAbsolutePath();
        }
        
        @Override
        public void evicted() {
            //When running from memory cache no need to close the reader, it does not own file handler.
            if (!cachePolicy.hasMemCache()) {
                //Threading: The called may own the CIM.readAccess, perform by dedicated worker to prevent deadlock
                RP.post(new Runnable() {
                    @Override
                    public void run () {
                        try {
                            IndexManager.writeAccess(new IndexManager.Action<Void>() {
                                @Override
                                public Void run() throws IOException, InterruptedException {
                                    close(false);
                                    LOGGER.log(Level.FINE, "Evicted index: {0}", folder.getAbsolutePath()); //NOI18N
                                    return null;
                                }
                            });
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InterruptedException ie) {
                            Exceptions.printStackTrace(ie);
                        }
                    }
                });
            } else if ((ref != null && currentCacheSize > maxCacheSize)) {
                ref.clearHRef();
            }
        }
        
        private synchronized void hit() {
            if (!cachePolicy.hasMemCache()) {
                try {
                    final URL url = folder.toURI().toURL();
                    IndexCacheFactory.getDefault().getCache().put(url, this);
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                }
            } else if (ref != null) {
                ref.get();
            }
        }
        
        private Collection<? extends String> getOrphanLock () {
            final List<String> locks = new LinkedList<String>();
            final String[] content = folder.list();
            if (content != null) {
                for (String name : content) {
                    if (name.startsWith(CACHE_LOCK_PREFIX)) {
                        locks.add(name);
                    }
                }
            }
            return locks;
        }
        
        private void checkPreconditions () throws IndexClosedException {
            if (closed) {
                throw new IndexClosedException();
            }
        }
        
        private IOException annotateException (final IOException ioe) {
            final StringBuilder message = new StringBuilder();            
            File[] children = folder.listFiles();
            if (children == null) {
                message.append("Non existing index folder");    //NOI18N
            }
            else {
                message.append("Current Lucene version: ").     //NOI18N
                        append(LucenePackage.get().getSpecificationVersion()).
                        append('(').    //NOI18N
                        append(LucenePackage.get().getImplementationVersion()).
                        append(")\n");    //NOI18N
                for (File c : children) {
                    message.append(c.getName()).append(" f: ").append(c.isFile()).
                    append(" r: ").append(c.canRead()).
                    append(" w: ").append(c.canWrite()).append("\n");  //NOI18N
                }
            }
            return Exceptions.attachMessage(ioe, message.toString());
        }
        
        private static FSDirectory createFSDirectory (final File indexFolder) throws IOException {
            assert indexFolder != null;
            final FSDirectory directory  = FSDirectory.open(indexFolder, lockFactory);
            directory.getLockFactory().setLockPrefix(CACHE_LOCK_PREFIX);
            return directory;
        } 
        
        private static long getCacheSize() {
            float per = -1.0f;
            final String propVal = System.getProperty(PROP_CACHE_SIZE); 
            if (propVal != null) {
                try {
                    per = Float.parseFloat(propVal);
                } catch (NumberFormatException nfe) {
                    //Handled below
                }
            }
            if (per<0) {
                per = DEFAULT_CACHE_SIZE;
            }
            return (long) (per * Runtime.getRuntime().maxMemory());
        }
        
        private final class CleanReference extends SoftReference<RAMDirectory[]> implements Runnable {
            
            @SuppressWarnings("VolatileArrayField")
            private volatile Directory[] hardRef; //clearHRef may be called by more concurrently (read lock).
            private final AtomicLong size = new AtomicLong();  //clearHRef may be called by more concurrently (read lock).

            private CleanReference(final RAMDirectory[] dir) {
                super (dir, Utilities.activeReferenceQueue());
                boolean doHardRef = currentCacheSize < maxCacheSize;
                if (doHardRef) {
                    this.hardRef = dir;
                    long _size = dir[0].sizeInBytes();
                    size.set(_size);
                    currentCacheSize+=_size;
                }
                LOGGER.log(Level.FINEST, "Caching index: {0} cache policy: {1}",    //NOI18N
                new Object[]{
                    folder.getAbsolutePath(),
                    cachePolicy.getSystemName()
                });
            }
            
            @Override
            public void run() {
                try {
                    LOGGER.log(Level.FINEST, "Dropping cache index: {0} cache policy: {1}", //NOI18N
                    new Object[] {
                        folder.getAbsolutePath(),
                        cachePolicy.getSystemName()
                    });
                    close(false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            @Override
            public void clear() {
                clearHRef();
                super.clear();
            }
            
            void clearHRef() {
                this.hardRef = null;
                long mySize = size.getAndSet(0);
                currentCacheSize-=mySize;
            }
        }        
    }
    //</editor-fold>

}
