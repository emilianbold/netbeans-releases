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

import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NoLockFactory;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexReaderInjection;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Note - there can be only a single IndexWriter at a time for the dir index. For consistency, the Writer is
 * kept in a thread-local variable until it is committed. Lucene will throw an exception if another Writer creation
 * attempt is done (by another thread, presumably). 
 * <p/>
 * It should be thread-safe (according to Lucene docs) to use an IndexWriter while Readers are opened.
 * <p/>
 * As Reader and Writers can be used in parallel, all query+store operations use readLock so they can run in parallel.
 * Operations which affect the whole index (close, clear) use write lock. RefreshReader called internally from writer's commit (close)
 * is incompatible with parallel reads, as it closes the old reader - uses writeLock.
 * <p/>
 * Locks must be acquired in the order [rwLock, LuceneIndex]. The do* method synchronize on the DirCache instance and must be called 
 * if the code already holds rwLock.
 *
 * @author Tomas Zezula
 */
//@NotTreadSafe
public class LuceneIndex implements Index.Transactional, Index.WithTermFrequencies, Runnable {

    private static final String PROP_INDEX_POLICY = "java.index.useMemCache";   //NOI18N
    private static final String PROP_CACHE_SIZE = "java.index.size";    //NOI18N
    private static final String PROP_DIR_TYPE = "java.index.dir";       //NOI18N
    private static final String DIR_TYPE_MMAP = "mmap";                 //NOI18N
    private static final String DIR_TYPE_NIO = "nio";                   //NOI18N
    private static final String DIR_TYPE_IO = "io";                     //NOI18N
    private static final CachePolicy DEFAULT_CACHE_POLICY = CachePolicy.DYNAMIC;
    private static final float DEFAULT_CACHE_SIZE = 0.05f;
    private static final CachePolicy cachePolicy = getCachePolicy();
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    
    private static boolean disableLocks;
    
    private final DirCache dirCache;       
    
    /** unit tests */
    public static void setDisabledLocks(final boolean disabled) {
        disableLocks = disabled;
    }

    public static LuceneIndex create (final File cacheRoot, final Analyzer analyzer) throws IOException {
        return new LuceneIndex (cacheRoot, analyzer);
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot, final Analyzer analyzer) throws IOException {
        assert refCacheRoot != null;
        assert analyzer != null;
        this.dirCache = new DirCache(
                refCacheRoot,
                cachePolicy,
                analyzer,
                disableLocks ?
                    NoLockFactory.getNoLockFactory():
                    new RecordOwnerLockFactory());
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
        
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.log(Level.FINE, "{0} is invalid!", this);
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final IndexSearcher searcher = new IndexSearcher(in);
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
            if (convertor instanceof IndexReaderInjection) {
                ((IndexReaderInjection)convertor).setIndexReader(in);
            }
            try {
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
            } finally {
                if (convertor instanceof IndexReaderInjection) {
                    ((IndexReaderInjection)convertor).setIndexReader(null);
                }
            }
        } finally {
            dirCache.releaseReader(in);
        }
    }
    
    @Override
    public <T> void queryTerms(
            final @NonNull Collection<? super T> result,
            final @NullAllowed Term seekTo,
            final @NonNull StoppableConvertor<Term,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        queryTermsImpl(result, seekTo, Convertors.newTermEnumToTermConvertor(filter), cancel);
    }
    
    @Override
    public <T> void queryTermFrequencies(
            final @NonNull Collection<? super T> result,
            final @NullAllowed Term seekTo,
            final @NonNull StoppableConvertor<Index.WithTermFrequencies.TermFreq,T> filter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        queryTermsImpl(result, seekTo, Convertors.newTermEnumToFreqConvertor(filter), cancel);
    }
    
    //where
    private <T> void queryTermsImpl(
            final @NonNull Collection<? super T> result,
            final @NullAllowed Term seekTo,
            final @NonNull StoppableConvertor<TermEnum,T> adapter,
            final @NullAllowed AtomicBoolean cancel) throws IOException, InterruptedException {
        
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                return;
            }

            final TermEnum terms = seekTo == null ? in.terms () : in.terms (seekTo);        
            try {
                if (adapter instanceof IndexReaderInjection) {
                    ((IndexReaderInjection)adapter).setIndexReader(in);
                }
                try {
                    do {
                        if (cancel != null && cancel.get()) {
                            throw new InterruptedException ();
                        }
                        final T vote = adapter.convert(terms);
                        if (vote != null) {
                            result.add(vote);
                        }
                    } while (terms.next());
                } catch (StoppableConvertor.Stop stop) {
                    //Stop iteration of TermEnum finally {
                } finally {
                    if (adapter instanceof IndexReaderInjection) {
                        ((IndexReaderInjection)adapter).setIndexReader(null);
                    }
                }
            } finally {
                terms.close();
            }
        } finally {
            dirCache.releaseReader(in);
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
        if (selector == null) {
            selector = AllFieldsSelector.INSTANCE;
        }
        IndexReader in = null;
        try {
            in = dirCache.acquireReader();
            if (in == null) {
                LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));   //NOI18N
                return;
            }
            final BitSet bs = new BitSet(in.maxDoc());
            final Collector c = new BitSetCollector(bs);
            final IndexSearcher searcher = new IndexSearcher(in);
            final TermCollector termCollector = new TermCollector(c);
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
                    searcher.search(q, termCollector);
                }
            } finally {
                searcher.close();
            }
        
            boolean logged = false;
            if (convertor instanceof IndexReaderInjection) {
                ((IndexReaderInjection)convertor).setIndexReader(in);
            }
            try {
                if (termConvertor instanceof IndexReaderInjection) {
                    ((IndexReaderInjection)termConvertor).setIndexReader(in);
                }
                try {
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
                } finally {
                    if (termConvertor instanceof IndexReaderInjection) {
                        ((IndexReaderInjection)termConvertor).setIndexReader(null);
                    }
                }
            } finally {
                if (convertor instanceof IndexReaderInjection) {
                    ((IndexReaderInjection)convertor).setIndexReader(null);
                }
            }
        } finally {
            dirCache.releaseReader(in);
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
    public void run() {
        dirCache.beginTx();
    }

    @Override
    public void commit() throws IOException {
        dirCache.closeTxWriter();
    }

    @Override
    public void rollback() throws IOException {
        dirCache.rollbackTxWriter();
    }

    @Override
    public <S, T> void txStore(
            final Collection<T> toAdd, 
            final Collection<S> toDelete, final Convertor<? super T, ? extends Document> docConvertor, 
            final Convertor<? super S, ? extends Query> queryConvertor) throws IOException {
        
        final IndexWriter wr = dirCache.acquireWriter();
        try {
            try {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Storing in TX {0}: {1} added, {2} deleted",
                            new Object[] { this, toAdd.size(), toDelete.size() }
                            );
                }
                _doStore(toAdd, toDelete, docConvertor, queryConvertor, wr);
            } finally {
                // nothing committed upon failure - readers not affected
                boolean ok = false;
                try {
                    ((FlushIndexWriter)wr).callFlush(false, true);
                    ok = true;
                } finally {
                    if (!ok) {
                        dirCache.rollbackTxWriter();
                    }
                }
            }
        } finally {
            dirCache.releaseWriter(wr);
        }
    }
    
    private <S, T> void _doStore(
            @NonNull final Collection<T> data, 
            @NonNull final Collection<S> toDelete,
            @NonNull final Convertor<? super T, ? extends Document> docConvertor, 
            @NonNull final Convertor<? super S, ? extends Query> queryConvertor,
            @NonNull final IndexWriter out) throws IOException {
        try {
            if (dirCache.exists()) {
                for (S td : toDelete) {
                    out.deleteDocuments(queryConvertor.convert(td));
                }
            }            
            if (data.isEmpty()) {
                return;
            }
            final LowMemoryWatcher lmListener = LowMemoryWatcher.getInstance();
            Directory memDir = null;
            IndexWriter activeOut = null;
            if (lmListener.isLowMemory()) {
                activeOut = out;
            } else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (
                    memDir,
                    new IndexWriterConfig(
                        Version.LUCENE_35,
                        dirCache.getAnalyzer()));
            }
            for (Iterator<T> it = fastRemoveIterable(data).iterator(); it.hasNext();) {
                T entry = it.next();
                it.remove();
                final Document doc = docConvertor.convert(entry);
                activeOut.addDocument(doc);
                if (memDir != null && lmListener.isLowMemory()) {
                    activeOut.close();
                    out.addIndexes(memDir);
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (
                        memDir,
                        new IndexWriterConfig(
                            Version.LUCENE_35,
                            dirCache.getAnalyzer()));
                }
            }
            data.clear();
            if (memDir != null) {
                activeOut.close();
                out.addIndexes(memDir);
                activeOut = null;
                memDir = null;
            }
        } catch (RuntimeException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        } catch (IOException e) {
            throw Exceptions.attachMessage(e, "Lucene Index Folder: " + dirCache.folder.getAbsolutePath());
        }
    }

    @Override
    public <S, T> void store (
            final @NonNull Collection<T> data,
            final @NonNull Collection<S> toDelete,
            final @NonNull Convertor<? super T, ? extends Document> docConvertor,
            final @NonNull Convertor<? super S, ? extends Query> queryConvertor,
            final boolean optimize) throws IOException {
        
        final IndexWriter wr = dirCache.acquireWriter();
        dirCache.storeCloseSynchronizer.enter();
        try {
            try {
                try {
                    _doStore(data, toDelete, docConvertor, queryConvertor, wr);
                } finally {
                    LOGGER.log(Level.FINE, "Committing {0}", this);
                    dirCache.releaseWriter(wr);
                }
            } finally {
                dirCache.close(wr);
            }
        } finally {
            dirCache.storeCloseSynchronizer.exit();
        }
    }
        
    @Override
    public Status getStatus (boolean force) throws IOException {
        return dirCache.getStatus(force);
    }

    @Override
    public void clear () throws IOException {
        dirCache.clear();
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

    private static <T> Iterable<T> fastRemoveIterable(final Collection<T> c) {
        return c instanceof ArrayList ?
                new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator() {
                        return new Iterator<T>() {
                            private final ListIterator<T> delegate = ((List)c).listIterator();

                            @Override
                            public boolean hasNext() {
                                return delegate.hasNext();
                            }

                            @Override
                            public T next() {
                                return delegate.next();
                            }

                            @Override
                            public void remove() {
                                delegate.set(null);
                            }
                        };
                    }
                } :
                c;
    }
    

    //<editor-fold defaultstate="collapsed" desc="Private classes (NoNormsReader, TermComparator, CachePolicy)">
        
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
        private final LockFactory lockFactory;
        private final CachePolicy cachePolicy;
        private final Analyzer analyzer;
        private final StoreCloseSynchronizer storeCloseSynchronizer;
        private volatile FSDirectory fsDir;
        private RAMDirectory memDir;
        private CleanReference ref;
        private IndexReader reader;
        private volatile boolean closed;
        private volatile Throwable closeStackTrace;
        private volatile Status validCache;
        private final OwnerReference owner = new OwnerReference();
        private final ReadWriteLock rwLock = new java.util.concurrent.locks.ReentrantReadWriteLock();
        
        /**
         * IndexWriter with potentially uncommitted data; local to a thread.
         */
        private ThreadLocal<IndexWriter>   txWriter = new ThreadLocal<IndexWriter>();
        
        private DirCache(
                final @NonNull File folder,
                final @NonNull CachePolicy cachePolicy,
                final @NonNull Analyzer analyzer,
                final @NonNull LockFactory lockFactory) throws IOException {
            assert folder != null;
            assert cachePolicy != null;
            assert analyzer != null;
            assert lockFactory != null;
            this.folder = folder;
            this.lockFactory = lockFactory;
            this.fsDir = createFSDirectory(folder, lockFactory);
            this.cachePolicy = cachePolicy;                        
            this.analyzer = analyzer;
            this.storeCloseSynchronizer = new StoreCloseSynchronizer();
        }
        
        Analyzer getAnalyzer() {
            return this.analyzer;
        }
        
        void clear() throws IOException {
            Future<Void> sync;
            while (true) {
                rwLock.writeLock().lock();
                try {
                    sync = storeCloseSynchronizer.getSync();
                    if (sync == null) {
                        doClear();
                        break;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
                try {
                    sync.get();
                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
                                
        private synchronized void doClear() throws IOException {
            checkPreconditions();
            // already write locked
            doClose(false);
            try {
                if (lockFactory instanceof RecordOwnerLockFactory) {
                    ((RecordOwnerLockFactory)lockFactory).forceRemoveLocks();
                }
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
                    final File cacheDir = fsDir.getDirectory();
                    final File[] children = cacheDir.listFiles();
                    if (children != null) {
                        for (final File child : children) {                                                
                            if (!child.delete()) {                                
                                final Map<String,String> sts = stackTraces(Thread.getAllStackTraces());
                                throw new IOException("Cannot delete: " + child.getAbsolutePath() + "(" +   //NOI18N
                                        child.exists()  +","+                                               //NOI18N
                                        child.canRead() +","+                                               //NOI18N
                                        child.canWrite() +","+                                              //NOI18N
                                        cacheDir.canRead() +","+                                            //NOI18N
                                        cacheDir.canWrite() +","+                                           //NOI18N
                                        (lockFactory instanceof RecordOwnerLockFactory ?
                                            ((RecordOwnerLockFactory)lockFactory).getOwner():
                                            "???") +","+                                                    //NOI18N
                                        sts +")");                                                          //NOI18N
                            }
                        }
                    }
                }
            } finally {
                //Need to recreate directory, see issue: #148374
                this.fsDir.close();
                this.fsDir = createFSDirectory(this.folder, this.lockFactory);
            }
        }
        
        void close(IndexWriter writer) throws IOException {
            if (writer == null) {
                return;
            }
            boolean success = false;
            try {
                writer.close();
                success = true;
            } finally {
                if (txWriter.get() == writer) {
                    LOGGER.log(Level.FINE, "TX writer cleared for {0}", this);
                    txWriter.remove();
                    owner.clear();
                    try {
                        if (!success) {
                            if ((lockFactory instanceof RecordOwnerLockFactory) &&
                                ((RecordOwnerLockFactory)lockFactory).getOwner() == Thread.currentThread()) {
                                ((RecordOwnerLockFactory)lockFactory).forceRemoveLocks();
                            } else if (IndexWriter.isLocked(fsDir)) {
                                IndexWriter.unlock(fsDir);
                            }
                        }
                    } catch (IOException ioe) {
                        LOGGER.log(
                           Level.WARNING,
                           "Cannot unlock index {0} while recovering, {1}.",  //NOI18N
                           new Object[] {
                               folder.getAbsolutePath(),
                            ioe.getMessage()
                           });
                    } finally {
                        refreshReader();
                    }
                }
            }
        }
        
        void close (final boolean closeFSDir) throws IOException {
            Future<Void> sync;
            while (true) {
                rwLock.writeLock().lock();
                try {
                    sync = storeCloseSynchronizer.getSync();
                    if (sync == null) {
                        doClose(closeFSDir);
                        break;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
                try {
                    sync.get();
                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        synchronized void doClose (final boolean closeFSDir) throws IOException {
            try {
                rollbackTxWriter();
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
                if (closeFSDir) {
                    this.closeStackTrace = new Throwable();
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
        
        Status getStatus (boolean force) throws IOException {
            checkPreconditions();
            Status valid = validCache;
            if (force ||  valid == null) {
                rwLock.writeLock().lock();
                try {
                    final Collection<? extends String> locks = getOrphanLock();
                    Status res = Status.INVALID;
                    if (!locks.isEmpty()) {
                        if (txWriter.get() != null) {
                            res = Status.WRITING;
                        } else {
                            LOGGER.log(Level.WARNING, "Broken (locked) index folder: {0}", folder.getAbsolutePath());   //NOI18N
                            synchronized (this) {
                                for (String lockName : locks) {
                                    fsDir.deleteFile(lockName);
                                }
                            }
                            if (force) {
                                clear();
                            }
                        }
                    } else {
                        if (!exists()) {
                            res = Status.EMPTY;
                        } else if (force) {
                            try {
                                getReader();
                                res = Status.VALID;
                            } catch (java.io.IOException e) {
                                clear();
                            } catch (RuntimeException e) {
                                clear();
                            }
                        } else {
                            res = Status.VALID;
                        }
                    }
                    valid = res;
                    validCache = valid;
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
            return valid;
        }
        
        boolean closeTxWriter() throws IOException {
            IndexWriter writer = txWriter.get();
            if (writer != null) {
                LOGGER.log(Level.FINE, "Committing {0}", this);
                close(writer);
                return true;
            } else {
                return false;
            }
        }
        
        boolean rollbackTxWriter() throws IOException {
            final IndexWriter writer = txWriter.get();
            if (writer != null) {
                try {
                    writer.rollback();
                    return true;
                } finally {
                    txWriter.remove();
                    owner.clear();
                }
            } else {
                return false;
            }
        }
        
        void beginTx() {
            owner.assertNoModifiedWriter();
            owner.setOwner(Thread.currentThread());
        }
        
        /**
         * The writer operates under readLock(!) since we do not want to lock out readers,
         * but just close, clear and commit operations. 
         * 
         * @return
         * @throws IOException 
         */
        IndexWriter acquireWriter () throws IOException {
            checkPreconditions();
            hit();

            boolean ok = false;
            
            rwLock.readLock().lock();
            IndexWriter writer = txWriter.get();
            try {
                if (writer != null) {
                    owner.assertSingleThreadWriter();
                    ok = true;
                    return writer;
                }
                try {
                    final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
                    //The posix::fsync(int) is very slow on Linux ext3,
                    //minimize number of files sync is done on.
                    //http://netbeans.org/bugzilla/show_bug.cgi?id=208224
                    final boolean alwaysCFS = Utilities.getOperatingSystem() == Utilities.OS_LINUX;
                    if (alwaysCFS) {
                        //TieredMergePolicy has better performance:
                        //http://blog.mikemccandless.com/2011/02/visualizing-lucenes-segment-merges.html
                        final TieredMergePolicy mergePolicy = new TieredMergePolicy();
                        mergePolicy.setNoCFSRatio(1.0);
                        iwc.setMergePolicy(mergePolicy);
                    }
                    final IndexWriter iw = new FlushIndexWriter (this.fsDir, iwc);
                    txWriter.set(iw);
                    owner.modified();
                    ok = true;
                    return iw;
                } catch (IOException ioe) {
                    //Issue #149757 - logging
                    throw annotateException (ioe);
                }
            } finally {
                if (!ok) {
                    rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseWriter(@NonNull final IndexWriter w) {
            assert txWriter.get() == w || txWriter.get() == null;
            rwLock.readLock().unlock();
        }
        
        IndexReader acquireReader() throws IOException {
            rwLock.readLock().lock();
            IndexReader r = null;
            try {
                r = getReader();
                return r;
            } finally {
                if (r == null) {
                  rwLock.readLock().unlock();
                }
            }
        }
        
        void releaseReader(IndexReader r) {
            if (r == null) {
                return;
            }
            assert r == this.reader;
            rwLock.readLock().unlock();
        }
        
        private synchronized IndexReader getReader () throws IOException {
            checkPreconditions();
            hit();
            if (this.reader == null) {
                if (validCache != Status.VALID &&
                    validCache != Status.WRITING &&
                    validCache != null) {
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
                    this.reader = IndexReader.open(source,true);
                } catch (final FileNotFoundException fnf) {
                    //pass - returns null
                } catch (IOException ioe) {
                    if (validCache == null) {
                        return null;
                    } else {
                        throw annotateException (ioe);
                    }
                }
            }
            return this.reader;
        }


        void refreshReader() throws IOException {
            try {
                if (cachePolicy.hasMemCache()) {
                    close(false);
                } else {
                    rwLock.writeLock().lock();
                    try {
                        synchronized (this) {
                            if (reader != null) {
                                final IndexReader newReader = IndexReader.openIfChanged(reader);
                                if (newReader != null) {
                                    reader.close();
                                    reader = newReader;
                                }
                            }
                        }
                    } finally {
                        rwLock.writeLock().unlock();
                    }
                }
            } finally {
                 validCache = Status.VALID;
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
                            close(false);
                            LOGGER.log(Level.FINE, "Evicted index: {0}", folder.getAbsolutePath()); //NOI18N
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
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
                    final URL url = Utilities.toURI(folder).toURL();
                    IndexCacheFactory.getDefault().getCache().put(url, this);
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                }
            } else if (ref != null) {
                ref.get();
            }
        }
        
        private Collection<? extends String> getOrphanLock () {
            final List<String> locks = new ArrayList<String>();
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
                throw (IndexClosedException) new IndexClosedException().initCause(closeStackTrace);
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
                message.append("threads: ").append(stackTraces(Thread.getAllStackTraces())).append("\n");   //NOI18N
                if (lockFactory instanceof RecordOwnerLockFactory) {
                    final Thread ownerThread = ((RecordOwnerLockFactory)lockFactory).getOwner();
                    if (ownerThread != null) {
                        message.append("owner:").append(ownerThread).             //NOI18N
                        append("(").append(ownerThread.getId()).append(")");      //NOI18N
                    }
                    final Exception caller = ((RecordOwnerLockFactory)lockFactory).getCaller();
                    if (caller != null) {
                        message.append(" from: ").append(Arrays.asList(caller.getStackTrace())); //NOI18N
                    }
                }
            }
            return Exceptions.attachMessage(ioe, message.toString());
        }
        
        private static FSDirectory createFSDirectory (
                final File indexFolder,
                final LockFactory lockFactory) throws IOException {
            assert indexFolder != null;
            assert lockFactory != null;
            final FSDirectory directory;
            final String dirType = System.getProperty(PROP_DIR_TYPE);
            if(DIR_TYPE_MMAP.equals(dirType)) {
                directory = new MMapDirectory(indexFolder, lockFactory);
            } else if (DIR_TYPE_NIO.equals(dirType)) {
                directory = new NIOFSDirectory(indexFolder, lockFactory);
            } else if (DIR_TYPE_IO.equals(dirType)) {
                directory = new SimpleFSDirectory(indexFolder, lockFactory);
            } else {
                directory = FSDirectory.open(indexFolder, lockFactory);
            }
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
        
        private Map<String,String> stackTraces(final Map<Thread,StackTraceElement[]> traces) {
            final Map<String,String> result = new HashMap<String, String>();
            for (Map.Entry<Thread,StackTraceElement[]> entry : traces.entrySet()) {
                result.put(
                    entry.getKey().toString()+"("+entry.getKey().getId()+")",   //NOI18N
                    Arrays.toString(entry.getValue()));
            }
            return result;
        }
                
        private final class OwnerReference {
            
            //@GuardedBy("this")
            private Thread txThread;
            //@GuardedBy("this")
            private boolean modified;
            
            synchronized void setOwner (@NullAllowed final Thread thread) {
                txThread = thread;
                modified = false;
            }
            
            synchronized void clear() {
                txThread = null;
                modified = false;
            }
            
            synchronized void modified() {
                modified = true;
            }
            
            synchronized void assertNoModifiedWriter() {
                if (txThread != null && modified) {
                    final Throwable t = new Throwable(String.format(
                        "Using stale writer, possibly forgotten call to store, " +  //NOI18N
                        "old owner Thread %s, " +           //NOI18N
                        "new owner Thread %s .",            //NOI18N
                            txThread,
                            Thread.currentThread()));
                    LOGGER.log(
                        Level.WARNING,
                        "Using stale writer",   //NOI18N
                        t);
                }
            }
            
            synchronized void assertSingleThreadWriter() {
                if (txThread != null && txThread != Thread.currentThread()) {
                    final Throwable t = new Throwable(String.format(
                        "Other thread using opened writer, " +       //NOI18N
                        "old owner Thread %s , " +          //NOI18N
                        "new owner Thread %s.",             //NOI18N
                            txThread,
                            Thread.currentThread()));
                    LOGGER.log(
                        Level.WARNING,
                        "Multiple writers",   //NOI18N
                        t);
                }
            }
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

    private static class FlushIndexWriter extends IndexWriter {

        public FlushIndexWriter(
                @NonNull final Directory d,
                @NonNull final IndexWriterConfig conf) throws CorruptIndexException, LockObtainFailedException, IOException {
            super(d, conf);
        }
        
        /**
         * Accessor to index flush for this package
         * @param triggerMerges
         * @param flushDeletes
         * @throws IOException 
         */
        void callFlush(boolean triggerMerges, boolean flushDeletes) throws IOException {
            // flushStores ignored in Lucene 3.5
            super.flush(triggerMerges, true, flushDeletes);
        }
    }

    private static final class StoreCloseSynchronizer {

        private ThreadLocal<Boolean> isWriterThread = new ThreadLocal<Boolean>(){
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        //@GuardedBy("this")
        private int depth;


        StoreCloseSynchronizer() {}


        synchronized void enter() {
            depth++;
            isWriterThread.set(Boolean.TRUE);
        }

        synchronized void exit() {
            assert depth > 0;
            depth--;
            isWriterThread.remove();
            if (depth == 0) {
                notifyAll();
            }
        }

        synchronized Future<Void> getSync() {
            if (depth == 0 || isWriterThread.get() == Boolean.TRUE) {
                return null;
            } else {
                return new Future<Void>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        synchronized(StoreCloseSynchronizer.this) {
                            return depth == 0;
                        }
                    }

                    @Override
                    public Void get() throws InterruptedException, ExecutionException {
                        synchronized (StoreCloseSynchronizer.this) {
                            while (depth > 0) {
                                StoreCloseSynchronizer.this.wait();
                            }
                        }
                        return null;
                    }

                    @Override
                    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        if (unit != TimeUnit.MILLISECONDS) {
                            throw new UnsupportedOperationException();
                        }
                        synchronized (StoreCloseSynchronizer.this) {
                            while (depth > 0) {
                                StoreCloseSynchronizer.this.wait(timeout);
                            }
                        }
                        return null;
                    }
                };
            }
        }

    }
}
