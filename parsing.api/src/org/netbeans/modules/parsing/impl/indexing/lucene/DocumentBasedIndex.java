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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.Query;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentBasedIndex implements IndexImpl {


    // -----------------------------------------------------------------------
    // IndexImpl implementation
    // -----------------------------------------------------------------------
    
    /**
     * Adds document
     * @param document
     */
    @Override
    public void addDocument(final IndexDocumentImpl document) {
        final boolean forceFlush;

        synchronized (this) {
            assert document instanceof LuceneDocument;
            toAdd.add((LuceneDocument) document);
            toRemove.add(document.getSourceName());
            forceFlush = lmListener.isLowMemory();
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store(false, null);
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
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }
    }
    
    @Override
    public void store(final boolean optimize, final Iterable<Indexable> indexedIndexables) throws IOException {        
        final List<LuceneDocument> toAdd;
        final List<String> toRemove;

        synchronized (this) {
            toAdd = new LinkedList<LuceneDocument>(this.toAdd);
            toRemove = new LinkedList<String>(this.toRemove);

            this.toAdd.clear();
            this.toRemove.clear();

            if (!staleFiles.isEmpty()) {
                if (indexedIndexables != null) {
                    for(Indexable i : indexedIndexables) {
                        this.staleFiles.remove(i.getRelativePath());
                    }
                } else {
                    for(LuceneDocument ldoc : toAdd) {
                        this.staleFiles.remove(ldoc.getSourceName());
                    }
                    this.staleFiles.removeAll(toRemove);
                }
            }
        }

        if (toAdd.size() > 0 || toRemove.size() > 0) {                                        
            LOGGER.log(Level.FINE, "Flushing: {0}", indexFolder); //NOI18N
            luceneIndex.store(
                    toAdd,
                    toRemove,
                    ADD_CONVERTOR,
                    REMOVE_CONVERTOR,
                    optimize);                    
        }
                
    }

    @Override
    public Collection<? extends IndexDocumentImpl> query(
            final String fieldName,
            final String value,
            final QuerySupport.Kind kind,
            final String... fieldsToLoad
    ) throws IOException, InterruptedException {
        assert fieldName != null;
        assert value != null;
        assert kind != null;
        final List<IndexDocumentImpl> result = new LinkedList<IndexDocumentImpl>();
        final Query query = Queries.createQuery(fieldName, fieldName, value, translateQueryKind(kind));
        FieldSelector selector = null;
        if (fieldsToLoad != null && fieldsToLoad.length > 0) {
            final String[] fieldsWithSource = new String[fieldsToLoad.length+1];
            System.arraycopy(fieldsToLoad, 0, fieldsWithSource, 0, fieldsToLoad.length);
            fieldsWithSource[fieldsToLoad.length] = DocumentUtil.FIELD_SOURCE_NAME;
            selector = Queries.createFieldSelector(fieldsWithSource);
        }        
        luceneIndex.query(result, QUERY_CONVERTOR, selector, null, query);
        return result;
    }

    @Override
    public void fileModified(String relativePath) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, adding stale file: {1}", new Object[]{this, relativePath}); //NOI18N
            }
            staleFiles.add(relativePath);
        }
    }

    @Override
    public Collection<? extends String> getStaleFiles() {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}, stale files: {1}", new Object[]{this, staleFiles}); //NOI18N
            }
            return new LinkedList<String>(staleFiles);
        }
    }
    
    /**
     * Checks if the lucene index is valid.
     * @return true when index is valid
     * @throws IOException when index is already closed
     */
    @Override
    public boolean isValid () throws IOException {
        return luceneIndex.isValid(true);        
    }
    
    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public DocumentBasedIndex(final URL indexFolderUrl) throws IOException {
        assert indexFolderUrl != null;
        try {
            indexFolder = new File(indexFolderUrl.toURI());
            luceneIndex = IndexManager.createIndex(indexFolder, new KeywordAnalyzer());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public void clear() throws IOException {
        luceneIndex.clear();
    }
        
    public void close() throws IOException {
        luceneIndex.close();
    }
            
    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(DocumentBasedIndex.class.getName());
    private static final Convertor<LuceneDocument,Document> ADD_CONVERTOR = new AddConvertor();
    private static final Convertor<String,Query> REMOVE_CONVERTOR = new RemoveConvertor();
    private static final Convertor<Document,IndexDocumentImpl> QUERY_CONVERTOR = new QueryConvertor();
    private static final LMListener lmListener = new LMListener();
    
    /* package */ static final int VERSION = 1;

    private final File indexFolder;
    private final Index luceneIndex;

    //@GuardedBy (this)
    private final List<LuceneDocument> toAdd = new LinkedList<LuceneDocument>();
    private final List<String> toRemove = new LinkedList<String>();
    private final Set<String> staleFiles = new HashSet<String>();

            
    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+indexFolder.getAbsolutePath()+"]";  //NOI18N
    }    

    private static Queries.QueryKind translateQueryKind(final QuerySupport.Kind kind) {
        switch (kind) {
            case EXACT: return Queries.QueryKind.EXACT;
            case PREFIX: return Queries.QueryKind.PREFIX;                
            case CASE_INSENSITIVE_PREFIX: return Queries.QueryKind.CASE_INSENSITIVE_PREFIX;
            case CAMEL_CASE: return Queries.QueryKind.CAMEL_CASE;
            case CASE_INSENSITIVE_REGEXP: return Queries.QueryKind.CASE_INSENSITIVE_PREFIX;                
            case REGEXP: return Queries.QueryKind.REGEXP;
            case CASE_INSENSITIVE_CAMEL_CASE: return Queries.QueryKind.CASE_INSENSITIVE_CAMEL_CASE;
            default: throw new UnsupportedOperationException (kind.toString());
        }
    }    
    
    private static final class AddConvertor implements Convertor<LuceneDocument, Document> {
        @Override
        public Document convert(LuceneDocument p) {
            return p.doc;
        }
    }
    
    private static final class RemoveConvertor implements Convertor<String,Query> {
        @Override
        public Query convert(String p) {
            return DocumentUtil.sourceNameQuery(p);
        }        
    }
    
    private static final class QueryConvertor implements Convertor<Document,IndexDocumentImpl> {
        @Override
        public IndexDocumentImpl convert(Document p) {
            return new LuceneDocument(p);
        }        
    }

}
