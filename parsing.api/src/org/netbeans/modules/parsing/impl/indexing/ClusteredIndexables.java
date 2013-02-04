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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author vita
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class ClusteredIndexables {

    public static final String DELETE = "ci-delete-set";    //NOI18N
    public static final String INDEX = "ci-index-set";      //NOI18N

    private static final Logger LOG = Logger.getLogger(ClusteredIndexables.class.getName());

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Creates new ClusteredIndexables
     * @param indexables, requires a list with fast {@link List#get(int)} as it heavily calls it.
     */
    public ClusteredIndexables(List<Indexable> indexables) {
        Parameters.notNull("indexables", indexables); //NOI18N  
        this.indexables = indexables;        
        this.sorted = new BitSet(indexables.size());
    }

    public Iterable<Indexable> getIndexablesFor(String mimeType) {
            if (mimeType == null) {
                mimeType = ALL_MIME_TYPES;
            }

            if (mimeType.length() == 0) {
                return new AllIndexables();
            }
            
            BitSet cluster = mimeTypeClusters.get(mimeType);
            if (cluster == null) {                
                cluster = new BitSet();
                // pick the indexables with the given mime type and add them to the cluster
                for (int i = sorted.nextClearBit(0); i < indexables.size(); i = sorted.nextClearBit(i+1)) {
                    final Indexable indexable = indexables.get(i);
                    if (SPIAccessor.getInstance().isTypeOf(indexable, mimeType)) {
                        cluster.set(i);
                        sorted.set(i);
                    }
                }
                mimeTypeClusters.put(mimeType, cluster);
            }
            
            return new BitSetIterable(cluster);
    }

    @NonNull
    public static AttachableDocumentIndexCache createDocumentIndexCache() {
        return new DocumentIndexCacheImpl();
    }

    @NonNull
    public static IndexDocument createDocument(@NonNull final String primaryKey) {
        Parameters.notNull("primaryKey", primaryKey);   //NOI18N
        return new DocumentStore.MemoryIndexDocument(primaryKey);
    }

    public static interface AttachableDocumentIndexCache extends DocumentIndexCache {
        void attach(@NonNull final String mode, @NonNull final ClusteredIndexables ci);
        void detach();
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------
    private static final String ALL_MIME_TYPES = ""; //NOI18N
    private final List<Indexable> indexables;
    private final BitSet sorted;
    private final Map<String, BitSet> mimeTypeClusters = new HashMap<String, BitSet>();
    private IndexedIterator currentIt;


    @NonNull
    private Indexable get(final int index) {
        return indexables.get(index);
    }

    private int current() {
        final IndexedIterator tmpIt = currentIt;
        return tmpIt == null ? -1 : tmpIt.index();
    }

    private static interface IndexedIterator<T> extends Iterator<T> {
        int index();
    }
    
    private static final class AllIndexablesIt implements IndexedIterator<Indexable> {

        private final Iterator<? extends Indexable> delegate;
        private int index = -1;

        AllIndexablesIt(Iterator<? extends Indexable> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Indexable next() {
            final Indexable res = delegate.next();
            index++;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable type"); //NOI18N
        }

        @Override
        public int index() {
            return index;
        }
        
    }

    private final class AllIndexables implements Iterable<Indexable> {

        @Override
        public Iterator<Indexable> iterator() {
            return ClusteredIndexables.this.currentIt = new AllIndexablesIt(indexables.iterator());
        }

    }

    private final class BitSetIterator implements IndexedIterator<Indexable> {

        private final BitSet bs;
        private int index;

        BitSetIterator(@NonNull final BitSet bs) {
            this.bs = bs;
            this.index = -1;
        }

        @Override
        public boolean hasNext() {
            return bs.nextSetBit(index + 1) >= 0;
        }

        @Override
        public Indexable next() {
            int tmp = bs.nextSetBit(index + 1);
            if (tmp < 0) {
                throw new NoSuchElementException();
            }
            index = tmp;
            return indexables.get(tmp);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable type"); //NOI18N
        }

        public int index() {
            return index;
        }

    }

    private final class BitSetIterable implements Iterable<Indexable> {

        private final BitSet bs;

        BitSetIterable(@NonNull final BitSet bs) {
            this.bs = bs;
        }

        @Override
        @NonNull
        public Iterator<Indexable> iterator() {
            return ClusteredIndexables.this.currentIt = new BitSetIterator(bs);
        }
    }

    private static final class DocumentIndexCacheImpl implements AttachableDocumentIndexCache {
      
        private ClusteredIndexables deleteIndexables;
        private ClusteredIndexables indexIndexables;
        private BitSet deleteFromDeleted;
        private BitSet deleteFromIndex;
        private DocumentStore toAdd;
        private List<String> toDeleteOutOfOrder;
        private Reference<Collection[]> dataRef;

        private volatile Pair<Long,StackTraceElement[]> attachDeleteStackTrace;
        private volatile Pair<Long,StackTraceElement[]> attachIndexStackTrace;
        private volatile Pair<Long,StackTraceElement[]> detachDeleteStackTrace;
        private volatile Pair<Long,StackTraceElement[]> detachIndexStackTrace;

        private DocumentIndexCacheImpl() {}

        @Override
        public void attach(
            @NonNull final String mode,
            @NonNull final ClusteredIndexables ci) {
            Parameters.notNull("mode", mode);   //NOI18N
            Parameters.notNull("ci", ci);       //NOI18N
            if (TransientUpdateSupport.isTransientUpdate()) {
                return;
            }
            if (DELETE.equals(mode)) {
                ensureNotReBound(this.deleteIndexables, ci);
                if (!ci.equals(this.deleteIndexables)) {
                    this.deleteIndexables = ci;
                    attachDeleteStackTrace = Pair.<Long,StackTraceElement[]>of(
                            System.nanoTime(),Thread.currentThread().getStackTrace());
                    detachDeleteStackTrace = null;
                }
            } else if (INDEX.equals(mode)) {
                ensureNotReBound(this.indexIndexables, ci);
                if (!ci.equals(this.indexIndexables)) {
                    this.indexIndexables = ci;
                    attachIndexStackTrace = Pair.<Long,StackTraceElement[]>of(
                            System.nanoTime(),Thread.currentThread().getStackTrace());
                    detachIndexStackTrace = null;
                }
            } else {
                throw new IllegalArgumentException(mode);
            }
        }

        @Override
        public void detach() {
            if (TransientUpdateSupport.isTransientUpdate()) {
                return;
            }
            detachDeleteStackTrace = detachIndexStackTrace = Pair.<Long,StackTraceElement[]>of(
                System.nanoTime(),Thread.currentThread().getStackTrace());
            clear();
            this.deleteIndexables = null;
            this.indexIndexables = null;
        }

        @Override
        public boolean addDocument(IndexDocument document) {
            if (!(document instanceof DocumentStore.MemoryIndexDocument)) {
                throw new IllegalArgumentException(document.getClass().getName());
            }
            boolean shouldFlush = init();
            handleDelete(
                indexIndexables,
                deleteFromIndex,
                toDeleteOutOfOrder,
                document.getPrimaryKey());
            shouldFlush |= toAdd.addDocument(document);
            return shouldFlush;
        }

        @Override
        public boolean removeDocument(String primaryKey) {
            final boolean shouldFlush = init();
            handleDelete(
                deleteIndexables,
                deleteFromDeleted,
                toDeleteOutOfOrder,
                primaryKey);
            return shouldFlush;
        }

        @Override
        public void clear() {
            toAdd = null;
            toDeleteOutOfOrder = null;
            deleteFromDeleted = null;
            deleteFromIndex = null;
            dataRef = null;
        }

        @Override
        public Collection<? extends String> getRemovedKeys() {
            return toDeleteOutOfOrder != null ?
                new RemovedCollection (
                    toDeleteOutOfOrder,
                    deleteIndexables,
                    deleteFromDeleted,
                    indexIndexables,
                    deleteFromIndex,
                    attachDeleteStackTrace,
                    attachIndexStackTrace,
                    detachDeleteStackTrace,
                    detachIndexStackTrace) :
                Collections.<String>emptySet();
        }

        @Override
        public Collection<? extends IndexDocument> getAddedDocuments() {
            return toAdd != null ? toAdd : Collections.<IndexDocument>emptySet();
        }

        private static void ensureNotReBound(
                @NullAllowed final ClusteredIndexables oldCi,
                @NonNull final ClusteredIndexables newCi) {
            if (oldCi != null && !oldCi.equals(newCi)) {
                throw new IllegalStateException(
                    String.format(
                        "Cannot bind to ClusteredIndexables(%d), already bound to ClusteredIndexables(%d)", //NOI18N
                        System.identityHashCode(newCi),
                        System.identityHashCode(oldCi)
                ));
            }
        }

        private static void handleDelete(
            @NullAllowed ClusteredIndexables ci,
            @NonNull BitSet bs,
            @NonNull List<? super String> toDelete,
            @NonNull String primaryKey) {
            final int index = isCurrent(ci, primaryKey);
                if (index >= 0) {
                    bs.set(index);
                } else {
                    toDelete.add(primaryKey);
                }
        }

        private static int isCurrent(
            @NullAllowed final ClusteredIndexables ci,
            @NonNull final String primaryKey) {
            if (ci == null) {
                return -1;
            }
            final int currentIndex = ci.current();
            if (currentIndex == -1) {
                return -1;
            }
            final Indexable currentIndexable = ci.get(currentIndex);
            if (primaryKey.equals(currentIndexable.getRelativePath())) {
                return currentIndex;
            }
            return -1;
        }

        private boolean init() {
            if (toAdd == null || toDeleteOutOfOrder == null) {
                assert toAdd == null &&
                    toDeleteOutOfOrder == null &&
                    deleteFromDeleted == null &&
                    deleteFromIndex == null;
                assert dataRef == null;
                toAdd = new DocumentStore();
                toDeleteOutOfOrder = new ArrayList<String>();
                deleteFromDeleted = new BitSet();
                deleteFromIndex = new BitSet();
                dataRef = new ClearReference(
                        new Collection[] {toAdd, toDeleteOutOfOrder},
                        this);
            }
            return dataRef.get() == null;
        }
    }

    private static final class ClearReference extends SoftReference<Collection[]> implements Runnable, Callable<Void> {

        private final DocumentIndexCacheImpl owner;
        private final AtomicInteger state = new AtomicInteger();

        public ClearReference(
                @NonNull final Collection[] data,
                @NonNull final DocumentIndexCacheImpl owner) {
            super(data, Utilities.activeReferenceQueue());
            Parameters.notNull("data", data);   //NOI18N
            Parameters.notNull("owner", owner); //NOI18N
            this.owner = owner;
        }

        @Override
        public void run() {
            if (!state.compareAndSet(0, 1)) {
                throw new IllegalStateException(Integer.toString(state.get()));
            }
            InjectedTasksSupport.enqueueTask(this);
            LOG.log(
                Level.FINEST,
                "Reference Task Enqueued for: {0}", //NOI18N
                owner);
        }
         

        @Override
        public Void call () throws Exception {
            if (!state.compareAndSet(1, 2)) {
                throw new IllegalStateException(Integer.toString(state.get()));
            }
            final DocumentIndex.Transactional txIndex = DocumentBasedIndexManager.getDefault().getIndex(owner);
            if (txIndex != null) {
                txIndex.txStore();
            }
            LOG.log(
                Level.FINEST,
                "Reference Task Executed for: {0}", //NOI18N
                owner);
            return null;
        }
    }
    
    private static class RemovedCollection extends AbstractCollection<String> {
        
        private final List<? extends String> outOfOrder;
        private final ClusteredIndexables deleteIndexables;
        private final BitSet deleteFromDeleted;
        private final ClusteredIndexables indexIndexables;
        private final BitSet deleteFromIndex;

        private final Pair<Long,StackTraceElement[]> attachDeleteStackTrace;
        private final Pair<Long,StackTraceElement[]> attachIndexStackTrace;
        private final Pair<Long, StackTraceElement[]> detachDeleteStackTrace;
        private final Pair<Long, StackTraceElement[]> detachIndexStackTrace;
        
        RemovedCollection(
            @NonNull final List<? extends String> outOfOrder,
            @NullAllowed final ClusteredIndexables deleteIndexables,
            @NonNull final BitSet deleteFromDeleted,
            @NullAllowed final ClusteredIndexables indexIndexables,
            @NonNull final BitSet deleteFromIndex,
            @NullAllowed final Pair<Long,StackTraceElement[]> attachDeleteStackTrace,
            @NullAllowed final Pair<Long, StackTraceElement[]> attachIndexStackTrace,
            @NullAllowed final Pair<Long, StackTraceElement[]> detachDeleteStackTrace,
            @NullAllowed final Pair<Long, StackTraceElement[]> detachIndexStackTrace) {
            assert outOfOrder != null;
            assert deleteFromDeleted != null;
            assert deleteFromIndex != null;
            this.outOfOrder = outOfOrder;
            this.deleteIndexables = deleteIndexables;
            this.deleteFromDeleted = deleteFromDeleted;
            this.indexIndexables = indexIndexables;
            this.deleteFromIndex = deleteFromIndex;
            this.attachDeleteStackTrace = attachDeleteStackTrace;
            this.attachIndexStackTrace = attachIndexStackTrace;
            this.detachDeleteStackTrace = detachDeleteStackTrace;
            this.detachIndexStackTrace = detachIndexStackTrace;
        }

        @Override
        public Iterator<String> iterator() {
            return new It(
                outOfOrder.iterator(),
                deleteIndexables,
                deleteFromDeleted,
                indexIndexables,
                deleteFromIndex,
                attachDeleteStackTrace,
                attachIndexStackTrace,
                detachDeleteStackTrace,
                detachIndexStackTrace);
        }

        @Override
        public int size() {
            return outOfOrder.size() + deleteFromDeleted.cardinality() + deleteFromIndex.cardinality();
        }
        
        @Override
        public boolean isEmpty() {
            return outOfOrder.isEmpty() && deleteFromDeleted.isEmpty() && deleteFromIndex.isEmpty();
        }
        
        
        private static class It implements Iterator<String> {

            private final Iterator<? extends String> outOfOrderIt;
            private final ClusteredIndexables deleteIndexables;
            private final BitSet deleteFromDeleted;
            private final ClusteredIndexables indexIndexables;
            private final BitSet deleteFromIndex;
            private int state;
            private int index;
            private String current;

            private final Pair<Long,StackTraceElement[]> attachDeleteStackTrace;
            private final Pair<Long,StackTraceElement[]> attachIndexStackTrace;
            private final Pair<Long, StackTraceElement[]> detachDeleteStackTrace;
            private final Pair<Long, StackTraceElement[]> detachIndexStackTrace;

            It(
                @NonNull final Iterator<? extends String> outOfOrderIt,
                @NullAllowed final ClusteredIndexables deleteIndexables,
                @NonNull final BitSet deleteFromDeleted,
                @NullAllowed final ClusteredIndexables indexIndexables,
                @NonNull final BitSet deleteFromIndex,
                @NullAllowed final Pair<Long,StackTraceElement[]> attachDeleteStackTrace,
                @NullAllowed final Pair<Long, StackTraceElement[]> attachIndexStackTrace,
                @NullAllowed final Pair<Long, StackTraceElement[]> detachDeleteStackTrace,
                @NullAllowed final Pair<Long, StackTraceElement[]> detachIndexStackTrace) {
                this.outOfOrderIt = outOfOrderIt;
                this.deleteIndexables = deleteIndexables;
                this.deleteFromDeleted = deleteFromDeleted;
                this.indexIndexables = indexIndexables;
                this.deleteFromIndex = deleteFromIndex;
                this.attachDeleteStackTrace = attachDeleteStackTrace;
                this.attachIndexStackTrace = attachIndexStackTrace;
                this.detachDeleteStackTrace = detachDeleteStackTrace;
                this.detachIndexStackTrace = detachIndexStackTrace;
            }

            @Override
            public boolean hasNext() {
                if (current != null) {
                    return true;
                }
                switch (state) {
                    case 0:
                        if (outOfOrderIt.hasNext()) {
                            current = outOfOrderIt.next();
                            return true;
                        } else {
                            index = -1;
                            state = 1;
                        }
                    case 1:
                        index = deleteFromDeleted.nextSetBit(index+1);
                        if (index >=0) {
                            if (deleteIndexables == null) {
                                throwIllegalState(
                                    "No deleteIndexables",  //NOI18N
                                    attachDeleteStackTrace,
                                    detachDeleteStackTrace);
                            }
                            try {
                                final Indexable file = deleteIndexables.get(index);
                                current = file.getRelativePath();
                                return true;
                            } catch (IndexOutOfBoundsException e) {
                                throwIllegalState(
                                    "Wrong deleteIndexables",  //NOI18N
                                    attachDeleteStackTrace,
                                    detachDeleteStackTrace);
                            }
                        } else {
                            index = -1;
                            state = 2;
                        }
                    case 2:
                        index = deleteFromIndex.nextSetBit(index+1);
                        if (index >= 0) {
                            if (indexIndexables == null) {
                                throwIllegalState(
                                    "No indexIndexables",   //NOI18N
                                    attachIndexStackTrace,
                                    detachIndexStackTrace);
                            }
                            try {
                                final Indexable file = indexIndexables.get(index);
                                current = file.getRelativePath();
                                return true;
                            } catch (IndexOutOfBoundsException e) {
                                throwIllegalState(
                                    "Wrong indexIndexables",   //NOI18N
                                    attachIndexStackTrace,
                                    detachIndexStackTrace);
                            }
                        } else {
                            index = -1;
                            state = 3;
                        }
                    default:
                        return false;
                }
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final String res = current;
                assert res != null;
                current = null;
                return res;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Immutable collection");    //NOI18N
            }

            private static void throwIllegalState(
                @NonNull final String reason,
                @NullAllowed final Pair<Long,StackTraceElement[]> attach,
                @NullAllowed final Pair<Long,StackTraceElement[]> detach) {
                throw new IllegalStateException(
                    MessageFormat.format(
                        "{0} : Attached at: {1} by: {2}, Detached at: {3} by: {4}",   //NOI18N
                        reason,
                        attach == null ? null : attach.first,
                        attach == null ? null : Arrays.asList(attach.second),
                        detach == null ? null : detach.first,
                        detach == null ? null : Arrays.asList(detach.second)));
            }
            
        }
    }

    private static final class DocumentStore implements Collection<IndexDocument>{

        private static final int INITIAL_DOC_COUNT = 100;
        private static final int INITIAL_DATA_SIZE = 1<<10;
        private static final long DATA_CACHE_SIZE = (long) (Runtime.getRuntime().maxMemory() * 0.1);

        private final Map<String,Integer> fieldNames;
        private int[] docs;
        private char[] data;
        private int nameIndex;
        private int docsPointer;
        private int dataPointer;


        DocumentStore() {
            fieldNames = new LinkedHashMap<String, Integer>();
            docs = new int[INITIAL_DOC_COUNT];
            data = new char[INITIAL_DATA_SIZE];

        }

        @Override
        public boolean add(@NonNull final IndexDocument doc) {
            addDocument(doc);
            return true;
        }
        
        boolean addDocument(@NonNull final IndexDocument doc) {
            boolean res = false;
            if (!(doc instanceof MemoryIndexDocument)) {
                throw new IllegalArgumentException();
            }
            for (Fieldable fld : ((MemoryIndexDocument)doc).getFields()) {
                final String fldName = fld.name();
                final boolean stored = fld.isStored();
                final boolean indexed = fld.isIndexed();
                final String fldValue = fld.stringValue();
                int index;
                Integer indexBoxed = fieldNames.get(fldName);
                if (indexBoxed == null) {
                    index = nameIndex++;
                    fieldNames.put(fldName, index);
                } else {
                    index = indexBoxed;
                }
                index = (index << 3) | (stored ? 4 : 0) | (indexed ? 2 : 0) | 1;

                if (docs.length < docsPointer + 2) {
                    int[] newdocs = new int[docs.length << 1];
                    System.arraycopy(docs, 0, newdocs, 0, docs.length);
                    docs = newdocs;
                }
                docs[docsPointer] = index;
                docs[docsPointer + 1] = dataPointer;
                docsPointer += 2;
                if (data.length < dataPointer + fldValue.length()) {
                    char[] newdata = new char[data.length << 1];
                    System.arraycopy(data, 0, newdata, 0, data.length);
                    data = newdata;
                    res = data.length<<1 > DATA_CACHE_SIZE;
                    LOG.log(
                        Level.FINE,
                        "New data size: {0}, flush: {1}",   //NOI18N
                        new Object[] {
                            newdata.length,
                            res
                        });
                }
                fldValue.getChars(0, fldValue.length(), data, dataPointer);
                dataPointer += fldValue.length();
            }
            if (docs.length < docsPointer + 1) {
                int[] newdocs = new int[docs.length << 1];
                System.arraycopy(docs, 0, newdocs, 0, docs.length);
                docs = newdocs;
            }
            docs[docsPointer++] = 0;
            return res;
        }

        @Override
        public Iterator<IndexDocument> iterator() {
            return new It();
        }

        @Override
        public boolean isEmpty() {
            return docsPointer == 0;
        }

        @Override
        public void clear() {
            fieldNames.clear();
            docs = new int[INITIAL_DOC_COUNT];
            data = new char[INITIAL_DATA_SIZE];
            docsPointer = 0;
            dataPointer = 0;
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }
       
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends IndexDocument> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        private class It implements Iterator<IndexDocument> {

            private int cur = 0;
            private final List<String> names;

            It() {
                names = new ArrayList<String>(fieldNames.keySet());
            }

            @Override
            public boolean hasNext() {
                return cur<docsPointer;
            }

            /**
             * Todo: Perf: non needed creation of IndexDocument, single can be used.
             */
            @Override
            public IndexDocument next() {
                IndexDocument doc = null;
                int nameIndex;
                while ((nameIndex=docs[cur++]) != 0) {
                    final boolean stored = (nameIndex & 4) == 4;
                    final boolean indexed = (nameIndex & 2) == 2;
                    nameIndex >>>= 3;
                    final int dataStart = docs[cur++];
                    final int dataEnd = docs[cur] != 0 ?
                            docs[cur+1] :
                            cur+1 == docsPointer ?
                                dataPointer :
                                docs[cur+2];
                    final String value = new String (data,dataStart, dataEnd - dataStart);
                    if (doc == null) {
                        assert "_sn".equals(names.get(nameIndex));  //NOI18N
                        doc = IndexManager.createDocument(value);
                    }
                    doc.addPair(
                            names.get(nameIndex),
                            value,
                            indexed,
                            stored);
                }
                return doc;
            }

            @Override
            public void remove() {
            }
        }

        private static final class MemoryIndexDocument implements IndexDocument {

            private static final String FIELD_PRIMARY_KEY = "_sn";  //NOI18N
            private static final String[] EMPTY = new String[0];

            private final List<Fieldable> fields = new ArrayList<Fieldable>();

            MemoryIndexDocument(@NonNull final String primaryKey) {
                Parameters.notNull("primaryKey", primaryKey);   //NOI18N
                fields.add(sourceNameField(primaryKey));
            }

            public List<Fieldable> getFields() {
                return fields;
            }

            @Override
            public String getPrimaryKey() {
                return getValue(FIELD_PRIMARY_KEY);
            }

            @Override
            public void addPair(String key, String value, boolean searchable, boolean stored) {
                final Field field = new Field (key, value,
                        stored ? Field.Store.YES : Field.Store.NO,
                        searchable ? Field.Index.NOT_ANALYZED_NO_NORMS : Field.Index.NO);
                fields.add (field);
            }

            @Override
            public String getValue(String key) {
                for (Fieldable field : fields) {
                    if (field.name().equals(key)) {
                        return field.stringValue();
                    }
                }
                return null;
            }

            @Override
            public String[] getValues(String key) {
                final List<String> result = new ArrayList<String>();
                for (Fieldable field : fields) {
                    if (field.name().equals(key)) {
                        result.add(field.stringValue());
                    }
                }
                return result.toArray(result.isEmpty() ? EMPTY : new String[result.size()]);
            }

            private Fieldable sourceNameField(@NonNull String primaryKey) {
                return new Field(FIELD_PRIMARY_KEY, primaryKey, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
            }
        }

    }

}
