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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.Query;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;

/**
 *
 * @author Tomas Zezula
 */
public final class DocumentIndexImpl implements DocumentIndex, Runnable {
    
    private final Index luceneIndex;
    
    /**
     * Transactional extension to the index
     */
    private final Index.Transactional txLuceneIndex;
    
    /**
     * This flag is used in tests, in particular in java.source IndexerTranscationTest. System property must be set before
     * the indexing starts and will disable caching of document changes, all changes will be flushed (but not committed) immediately.
     */
    private boolean disableCache = Boolean.getBoolean("test." + DocumentIndexImpl.class.getName() + ".cacheDisable");
    
    private static final Convertor<IndexDocumentImpl,Document> ADD_CONVERTOR = Convertors.newIndexDocumentToDocumentConvertor();
    private static final Convertor<String,Query> REMOVE_CONVERTOR = Convertors.newSourceNameToQueryConvertor();
    private static final Convertor<Document,IndexDocumentImpl> QUERY_CONVERTOR = Convertors.newDocumentToIndexDocumentConvertor();
    private static final Logger LOGGER = Logger.getLogger(DocumentIndexImpl.class.getName());
    
    //@GuardedBy (this)
    private final List<IndexDocumentImpl> toAdd = new LinkedList<IndexDocumentImpl>();
    private final List<String> toRemove = new LinkedList<String>();
    private final Set<String> dirtyKeys = new HashSet<String>();
    private Reference<List[]> dataRef;

    public DocumentIndexImpl (final Index index) {
        assert index != null;
        this.luceneIndex = index;
        if (index instanceof Index.Transactional) {
            this.txLuceneIndex = (Index.Transactional)index;
        } else {
            this.txLuceneIndex = null;
        }
    }

    /**
     * Use in tests only ! Clears data ref, causing the next addDocument
     * or removeDocument to flush the buffered contents
     */
    void testClarDataRef() {
        dataRef.clear();
    }

    /**
     * Adds document
     * @param document
     */
    @Override
    public void addDocument(IndexDocument document) {
        boolean forceFlush;

        synchronized (this) {
            assert document instanceof IndexDocumentImpl;
            final Reference<List[]> ref = getDataRef();
            assert ref != null;
            forceFlush = disableCache || ref.get() == null;
            toAdd.add((IndexDocumentImpl)document);
            toRemove.add(document.getPrimaryKey());
        }
        
        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, true);
                System.gc();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }
    }

    /**
     * Removes all documents for given path
     * @param relativePath
     */
    @Override
    public void removeDocument(String primaryKey) {
        final boolean forceFlush;

        synchronized (this) {
            final Reference<List[]> ref = getDataRef();
            assert ref != null;
            forceFlush = ref.get() == null;
            toRemove.add(primaryKey);
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, true);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }
    }

    
    /**
     * Checks if the Lucene index is valid.
     * @return {@link Status#INVALID} when the index is broken, {@link Status#EMPTY}
     * when the index does not exist or {@link  Status#VALID} if the index is valid
     * @throws IOException when index is already closed
     */
    @Override
    public Index.Status getStatus() throws IOException {
        return luceneIndex.getStatus(true);
    }
    
    @Override
    public void close() throws IOException {
        luceneIndex.close();
    }
    
    @Override
    public void store(boolean optimize) throws IOException {
        store(optimize, false);
    }

    @Override
    public void run() {
        if (luceneIndex instanceof Runnable) {
            ((Runnable)luceneIndex).run();
        }
    }
    
    private void store(boolean optimize, boolean flushOnly) throws IOException {
        final List<IndexDocumentImpl> _toAdd;
        final List<String> _toRemove;

        synchronized (this) {
            _toAdd = new ArrayList<IndexDocumentImpl>(this.toAdd);
            _toRemove = new ArrayList<String>(this.toRemove);

            this.toAdd.clear();
            this.toRemove.clear();
            this.dataRef = null;

            if (!dirtyKeys.isEmpty()) {                
                for(IndexDocument ldoc : _toAdd) {
                    this.dirtyKeys.remove(ldoc.getPrimaryKey());
                }
                this.dirtyKeys.removeAll(_toRemove);                
            }
        }

        if (_toAdd.size() > 0 || _toRemove.size() > 0) {                                        
            LOGGER.log(Level.FINE, "Flushing: {0}", luceneIndex.toString()); //NOI18N
            if (flushOnly && txLuceneIndex != null) {
                txLuceneIndex.txStore(
                        _toAdd, 
                        _toRemove, 
                        ADD_CONVERTOR, 
                        REMOVE_CONVERTOR
                );
            } else {
                luceneIndex.store(
                        _toAdd,
                        _toRemove,
                        ADD_CONVERTOR,
                        REMOVE_CONVERTOR,
                        optimize);                    
            }
        } else if (!flushOnly && txLuceneIndex != null) {
            txLuceneIndex.commit();
        }
    }

    @Override
    public Collection<? extends IndexDocument> query(String fieldName, String value, QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
        assert fieldName != null;
        assert value != null;
        assert kind != null;
        final List<IndexDocumentImpl> result = new LinkedList<IndexDocumentImpl>();
        final Query query = Queries.createQuery(fieldName, fieldName, value, kind);
        FieldSelector selector = null;
        if (fieldsToLoad != null && fieldsToLoad.length > 0) {
            final String[] fieldsWithSource = new String[fieldsToLoad.length+1];
            System.arraycopy(fieldsToLoad, 0, fieldsWithSource, 0, fieldsToLoad.length);
            fieldsWithSource[fieldsToLoad.length] = IndexDocumentImpl.FIELD_PRIMARY_KEY;
            selector = Queries.createFieldSelector(fieldsWithSource);
        }        
        luceneIndex.query(result, QUERY_CONVERTOR, selector, null, query);
        return result;
    }
    
    @Override
    public Collection<? extends IndexDocument> findByPrimaryKey (
            final String primaryKeyValue,
            final Queries.QueryKind kind,
            final String... fieldsToLoad) throws IOException, InterruptedException {
                return query(IndexDocumentImpl.FIELD_PRIMARY_KEY, primaryKeyValue, kind, fieldsToLoad);
    }

    @Override
    public void markKeyDirty(final String primaryKey) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, adding dirty key: {1}", new Object[]{this, primaryKey}); //NOI18N
            }
            dirtyKeys.add(primaryKey);
        }
    }

    @Override
    public void removeDirtyKeys(final Collection<? extends String> keysToRemove) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, Removing dirty keys: {1}", new Object[]{this, keysToRemove}); //NOI18N
            }
            dirtyKeys.removeAll(keysToRemove);
        }
    }

    @Override
    public Collection<? extends String> getDirtyKeys() {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, dirty keys: {1}", new Object[]{this, dirtyKeys}); //NOI18N
            }
            return new ArrayList<String>(dirtyKeys);
        }
    }
    
    
    @Override
    public String toString () {
        return "DocumentIndex["+luceneIndex.toString()+"]";  //NOI18N
    }    
    
    
    
    private Reference<List[]> getDataRef() {
        assert Thread.holdsLock(this);
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            assert dataRef == null;
            dataRef = new SoftReference<List[]>(new List[] {toAdd, toRemove});
        }
        return dataRef;
    }
                    
}
