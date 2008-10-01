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

package org.netbeans.modules.gsfret.source.usages;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.Language;
import org.netbeans.napi.gsfret.source.ClassIndex;
import org.netbeans.modules.gsfret.source.util.LowMemoryEvent;
import org.netbeans.modules.gsfret.source.util.LowMemoryListener;
import org.netbeans.modules.gsfret.source.util.LowMemoryNotifier;
import org.openide.util.Exceptions;

/**
 * Lucene interface - Responsible for storing and and querying at the lowest level.
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * 
 * Rip out the old query stuff.
 * 
 * @todo Find a faster or more efficient "batch" operation for storing tons of documents
 *   at startup (When scanning the boot path).
 * @todo Can deletion be better?
 * 
 * @author Tomas Zezula
 * @author Tor Norbye
 */
class LuceneIndex extends Index {
    
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N
    static final String REFERENCES = "gsf";    // NOI18N
    private static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    
    private final File refCacheRoot;
    //@GuardedBy(this)
    private Directory directory;
    private Long rootTimeStamp;
    
    //@GuardedBy (this)
    private IndexReader reader; //Cache, do not use this directly, use getReader
    private volatile boolean closed;
    
    // For debugging purposes only
    private ClassIndexImpl classIndex;
    private File cacheRoot;
        
    public static Index create (final Language language, final File cacheRoot, ClassIndexImpl classIndex) throws IOException { 

        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        LuceneIndex index = new LuceneIndex (language, getReferencesCacheFolder(cacheRoot));
        
        // For debugging (lucene browser) only
        index.classIndex = classIndex;
        index.cacheRoot = cacheRoot;
        
        return index;
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final Language language, final File refCacheRoot) throws IOException {
        super(language);
        assert refCacheRoot != null;
        this.refCacheRoot = refCacheRoot;
        this.directory = FSDirectory.getDirectory(refCacheRoot, NoLockFactory.getNoLockFactory());      //Locking controlled by rwlock
    }

    private void regExpSearch (final Pattern pattern, Term startTerm, final IndexReader in, final Set<Term> toSearch/*, final AtomicBoolean cancel*/, boolean caseSensitive) throws IOException/*, InterruptedException*/ {        
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
            // TODO - if startText==startPrefix keep startTerm alone
            startPrefix = startBuilder.toString();
            // TODO: The java version of lucene index reassigned the start term here
            // startTerm = caseSensitive ? DocumentUtil.simpleNameTerm(startPrefix) : DocumentUtil.caseInsensitiveNameTerm(startPrefix);
        }
        else {
            startPrefix=startText;
        }
        final String camelField = startTerm.field();
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
    
    private void prefixSearch(Term nameTerm, final IndexReader in, final Set<Term> toSearch/*, final AtomicBoolean cancel*/) throws IOException/*, InterruptedException*/ {
        final String prefixField = nameTerm.field();
        final String name = nameTerm.text();
        final TermEnum en = in.terms(nameTerm);
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

    public boolean isUpToDate(String resourceName, long timeStamp) throws IOException {
        checkPreconditions();

        // Don't do anything for preindexed filesystems
        if (Index.isPreindexed(cacheRoot)) {
            return true;
        }
        
        if (!isValid(false)) {
            return false;
        }
        try {
            Searcher searcher = new IndexSearcher (this.getReader());
            try {
                Hits hits;
                if (resourceName == null) {
                    synchronized (this) {
                        if (this.rootTimeStamp != null) {
                            return rootTimeStamp.longValue() >= timeStamp;
                        }
                    }
                    hits = searcher.search(new TermQuery(DocumentUtil.rootDocumentTerm()));
                }
                else {
                    hits = searcher.search(DocumentUtil.binaryNameQuery(resourceName));
                }

                if (hits.length() != 1) {   //0 = not present, 1 = present and has timestamp, >1 means broken index, probably killed IDE, treat it as not up to date and store will fix it.
                    return false;
                }
                else {                    
                    try { 
                        Hit hit = (Hit) hits.iterator().next();
                        long cacheTime = DocumentUtil.getTimeStamp(hit.getDocument());
                        if (resourceName == null) {
                            synchronized (this) {
                                this.rootTimeStamp = new Long (cacheTime);
                            }
                        }
                        return cacheTime >= timeStamp;
                    } catch (ParseException pe) {
                        throw new IOException ();
                    }
                }
            } finally {
                searcher.close();
            }
        } catch (java.io.FileNotFoundException fnf) {
            this.clear();
            return false;
        }
    }
    
    
    private static final FieldSelector FILE_AND_TIMESTAMP = new FieldSelector() {
        public FieldSelectorResult accept(String key) {
            return DocumentUtil.FIELD_FILENAME.equals(key) || DocumentUtil.FIELD_TIME_STAMP.equals(key) ?
                FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
        }
    };
    
    private static class CustomFieldSelector implements FieldSelector {
        private Set<String> includeFields;
        
        CustomFieldSelector(Set<String> includeFields) {
            this.includeFields = includeFields;
        }
        public FieldSelectorResult accept(String key) {
            // Always load the filename since clients don't know about it but it's needed
            // to call getPersistentUrl()
            boolean include = includeFields.contains(key) || key.equals(DocumentUtil.FIELD_FILENAME);
            return include ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
        }
    }
    
    public Map<String,String> getTimeStamps() throws IOException {
        checkPreconditions();
        if (!isValid(false)) {
            return null;
        }
        final IndexReader in = getReader();
        Map<String,String> result = new HashMap<String,String>(2*in.numDocs());
        for (int i = 0, n = in.numDocs(); i < n; i++) {
            if (in.isDeleted(i)) {
                continue;
            }
            Document document = in.document(i, FILE_AND_TIMESTAMP);
            // TODO - use a query instead! Faster iteration!
            String timestamp = document.get(DocumentUtil.FIELD_TIME_STAMP);
            String filename = document.get(DocumentUtil.FIELD_FILENAME);
            if (timestamp != null && filename != null) {
                // Ugh - what if I already have another document for the same file
                // in here - shouldn't I pick the oldest timestamp? Or is it 
                // the responsibility of the clients to always update all documents 
                // at the same time for a given file?
                result.put(filename, timestamp);
            }
        }
        
        return result;
    }

    public boolean isValid (boolean tryOpen) throws IOException {
        checkPreconditions();
        boolean res = IndexReader.indexExists(this.directory);
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
                                final java.lang.reflect.Field field = c.getDeclaredField("refCount");
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
            this.directory = FSDirectory.getDirectory(refCacheRoot, NoLockFactory.getNoLockFactory());      //Locking controlled by rwlock
            closed = false;
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
           if (PREINDEXING) {
               return;
           }
           this.closed = true;
        }
    }

    public @Override String toString () {
        return getClass().getSimpleName()+"["+this.refCacheRoot.getAbsolutePath()+"]";  //NOI18N
    }

    private synchronized IndexReader getReader () throws IOException {
        if (this.reader == null) {            
            this.reader = IndexReader.open(this.directory);
        }        
        return this.reader;
    }
    
    private synchronized IndexWriter getWriter (final boolean create) throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        IndexWriter writer = new IndexWriter (this.directory,new KeywordAnalyzer(), create);
        return writer;
    }
    
    private static File getReferencesCacheFolder (final File cacheRoot) throws IOException {
        File refRoot = new File (cacheRoot,REFERENCES);
        if (!refRoot.exists()) {
            refRoot.mkdir();
        }
        return refRoot;
    }

    private void checkPreconditions () {
        if (closed) {
            throw new IllegalStateException ("Index already closed");   //NOI18N
        }
    }

    private static class LMListener implements LowMemoryListener {        
        
        private AtomicBoolean lowMemory = new AtomicBoolean (false);
        
        public void lowMemory(LowMemoryEvent event) {
            lowMemory.set(true);
        }        
    }
    

    // BEGIN TOR MODIFICATIONS
    public void batchStore(List<IndexBatchEntry> list, boolean create) throws IOException {
        checkPreconditions();

        // First, delete previous documents referring to any of these files
        // (but we don't have to do that for new filesystems being scanned,
        // which will be invalid)
        if (!create) {
            assert ClassIndexManager.holdsWriteLock();
            IndexReader in = getReader();

            String prevUrl = "";
            for (IndexBatchEntry entry : list) {
                String fileUrl = entry.getFilename();
                if (fileUrl == null) {
                    continue;
                }
                if (fileUrl.equals(prevUrl)) {
                    continue;
                }
                prevUrl = fileUrl;

                final Searcher searcher = new IndexSearcher(in);
                try {
                    if (fileUrl != null) {
                        BooleanQuery query = new BooleanQuery ();
                        query.add (new TermQuery (new Term (DocumentUtil.FIELD_FILENAME, fileUrl)),BooleanClause.Occur.MUST);

                        Hits hits = searcher.search(query);
                        for (int i=0; i<hits.length(); i++) {
                            in.deleteDocument (hits.id(i));
                        }
                    }
                    in.deleteDocuments (DocumentUtil.rootDocumentTerm());
                } finally {
                    searcher.close();
                }
            }
        }
        long timeStamp = System.currentTimeMillis();
        
        final IndexWriter out = getWriter(create);
        try {
            if (debugIndexMerging) {
                out.setInfoStream (System.err);
            }
            final LuceneIndexMBean indexSettings = LuceneIndexMBeanImpl.getDefault();
            if (indexSettings != null) {
                out.setMergeFactor(indexSettings.getMergeFactor());
                out.setMaxMergeDocs(indexSettings.getMaxMergeDocs());
                out.setMaxBufferedDocs(indexSettings.getMaxBufferedDocs());
            }        
            LowMemoryNotifier lm = LowMemoryNotifier.getDefault();
            LMListener lmListener = new LMListener ();
            lm.addLowMemoryListener (lmListener);        
            Directory memDir = null;
            IndexWriter activeOut = null;        
            if (lmListener.lowMemory.getAndSet(false)) {
                activeOut = out;
            }
            else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
            }        
            try {
                activeOut.addDocument (DocumentUtil.createRootTimeStampDocument (timeStamp));
                for (IndexBatchEntry entry : list) {
                    String filename = entry.getFilename();
                    List<IndexDocumentImpl> documents = entry.getDocuments();
                    if (documents != null && documents.size() > 0) {
                        String createEmptyUrl = null;
                        for (IndexDocumentImpl document : documents) {
                            Document newDoc = new Document();
                            newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
                            if (document.overrideUrl != null) {
                                newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, document.overrideUrl, Field.Store.YES, Field.Index.UN_TOKENIZED));
                                createEmptyUrl = filename;
                            } else if (filename != null) {
                                newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, filename, Field.Store.YES, Field.Index.UN_TOKENIZED));
                            }

                            for (int i = 0, n = document.indexedKeys.size(); i < n; i++) {
                                String key = document.indexedKeys.get(i);
                                String value = document.indexedValues.get(i);
                                assert key != null && value != null : "key=" + key + ", value=" + value;
                                Field field = new Field(key, value, Field.Store.YES, Field.Index.UN_TOKENIZED);
                                newDoc.add(field);
                            }

                            for (int i = 0, n = document.unindexedKeys.size(); i < n; i++) {
                                String key = document.unindexedKeys.get(i);
                                String value = document.unindexedValues.get(i);
                                assert key != null && value != null : "key=" + key + ", value=" + value;
                                Field field = new Field(key, value, Field.Store.YES, Field.Index.NO);
                                newDoc.add(field);
                            }

                            activeOut.addDocument(newDoc);
                        }

                        if (createEmptyUrl != null) {
                            // For timestamp analysis

                            Document newDoc = new Document();
                            newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
                            newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, createEmptyUrl, Field.Store.YES, Field.Index.UN_TOKENIZED));
                            activeOut.addDocument(newDoc);
                        }
                    } else if (filename != null && documents != null) { // documents == null: delete
                        Document newDoc = new Document();
                        newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
                        newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, filename, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        activeOut.addDocument(newDoc);
                    }
                }

                if (memDir != null && lmListener.lowMemory.getAndSet(false)) {                       
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});                        
                    memDir = new RAMDirectory ();        
                    activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});   
                    activeOut = null;
                    memDir = null;
                }
                synchronized (this) {
                    this.rootTimeStamp = new Long (timeStamp);
                }
            } finally {
                lm.removeLowMemoryListener (lmListener);  
            }
        } finally {
            out.close();
        }
    }
    
    public void store(String fileUrl, List<IndexDocument> documents) throws IOException {
        checkPreconditions();
        assert ClassIndexManager.holdsWriteLock();

        boolean create = !isValid(false);
        if (!create) {
            IndexReader in = getReader();
            
            final Searcher searcher = new IndexSearcher (in);
            try {
                if (fileUrl != null) {
                    BooleanQuery query = new BooleanQuery ();
                    query.add (new TermQuery (new Term (DocumentUtil.FIELD_FILENAME, fileUrl)),BooleanClause.Occur.MUST);

                    Hits hits = searcher.search(query);
                    for (int i=0; i<hits.length(); i++) {
                        in.deleteDocument (hits.id(i));
                    }
                }
                in.deleteDocuments (DocumentUtil.rootDocumentTerm());
            } finally {
                searcher.close();
            }
        }
        long timeStamp = System.currentTimeMillis();
        if (documents != null) {
            store(documents, create, timeStamp, fileUrl);
        }
    }    
    
    private void store (List<IndexDocument> d, final boolean create, final long timeStamp, final String filename) throws IOException {        
        @SuppressWarnings("unchecked")
        List<IndexDocumentImpl> documents = (List<IndexDocumentImpl>)(List)d;
        final IndexWriter out = getWriter(create);
        try {
            if (debugIndexMerging) {
                out.setInfoStream (System.err);
            }
            final LuceneIndexMBean indexSettings = LuceneIndexMBeanImpl.getDefault();
            if (indexSettings != null) {
                out.setMergeFactor(indexSettings.getMergeFactor());
                out.setMaxMergeDocs(indexSettings.getMaxMergeDocs());
                out.setMaxBufferedDocs(indexSettings.getMaxBufferedDocs());
            }        
            LowMemoryNotifier lm = LowMemoryNotifier.getDefault();
            LMListener lmListener = new LMListener ();
            lm.addLowMemoryListener (lmListener);        
            Directory memDir = null;
            IndexWriter activeOut = null;        
            if (lmListener.lowMemory.getAndSet(false)) {
                activeOut = out;
            }
            else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
            }        
            try {
                activeOut.addDocument (DocumentUtil.createRootTimeStampDocument (timeStamp));
                if (documents != null && documents.size() > 0) {
                    for (IndexDocumentImpl document : documents) {
                        Document newDoc = new Document();
                        newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
                        if (document.overrideUrl != null) {
                            newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, document.overrideUrl, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        } else if (filename != null) {
                            newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, filename, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }

                        for (int i = 0, n = document.indexedKeys.size(); i < n; i++) {
                            String key = document.indexedKeys.get(i);
                            String value = document.indexedValues.get(i);
                            assert key != null && value != null : "key=" + key + ", value=" + value;
                            Field field = new Field(key, value, Field.Store.YES, Field.Index.UN_TOKENIZED);
                            newDoc.add(field);
                        }

                        for (int i = 0, n = document.unindexedKeys.size(); i < n; i++) {
                            String key = document.unindexedKeys.get(i);
                            String value = document.unindexedValues.get(i);
                            assert key != null && value != null : "key=" + key + ", value=" + value;
                            Field field = new Field(key, value, Field.Store.YES, Field.Index.NO);
                            newDoc.add(field);
                        }

                        activeOut.addDocument(newDoc);
                    }
                } else if (filename != null) {
                    Document newDoc = new Document();
                    newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
                    newDoc.add(new Field (DocumentUtil.FIELD_FILENAME, filename, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    activeOut.addDocument(newDoc);
                }

                if (memDir != null && lmListener.lowMemory.getAndSet(false)) {                       
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});                        
                    memDir = new RAMDirectory ();        
                    activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});   
                    activeOut = null;
                    memDir = null;
                }
                synchronized (this) {
                    this.rootTimeStamp = new Long (timeStamp);
                }
            } finally {
                lm.removeLowMemoryListener (lmListener);  
            }
        } finally {
            out.close();
        }
    }
    
    @SuppressWarnings ("unchecked") // NOI18N, unchecked - lucene has source 1.4
    public void search(final String primaryField, final String name, final NameKind kind, final Set<ClassIndex.SearchScope> scope, 
            final Set<SearchResult> result, final Set<String> terms) throws IOException {
        checkPreconditions();
        if (!isValid(false)) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
            return;
        }

        assert name != null;
        final Set<Term> toSearch = new TreeSet<Term> (new TermComparator());
                
        final IndexReader in = getReader();
        switch (kind) {
            case EXACT_NAME:
                {
                    toSearch.add(new Term (primaryField, name));
                    break;
                }
            case PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    if (terms != null && terms.size() > 0) {
                        gsfEmptyPrefixSearch(in, result, terms);
                    }
                    else {
                        gsfEmptyPrefixSearch(in, result, Collections.singleton(primaryField));
                    }
                    
                    return;
                }
                else {
                    final Term nameTerm = new Term (primaryField, name);
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CASE_INSENSITIVE_PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    if (terms != null && terms.size() > 0) {
                        gsfEmptyPrefixSearch(in, result, terms);
                    }
                    else {
                        gsfEmptyPrefixSearch(in, result, Collections.singleton(primaryField));
                    }
                    return;
                }
                else {                    
                    final Term nameTerm = new Term (primaryField, name.toLowerCase());
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CAMEL_CASE:
                if (name.length() == 0) {
                    search(primaryField, name, NameKind.CASE_INSENSITIVE_PREFIX, scope, result, terms);
                    return;
                }
                {
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(name, lastIndex + 1);
                        String token = name.substring(lastIndex, index == -1 ? name.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token); 
                        // TODO - add in Ruby chars here?
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                        lastIndex = index;
                    }
                    while(index != -1);

                    final Pattern pattern = Pattern.compile(sb.toString());
                    final Term nameTerm = new Term(primaryField, prefix);
                    regExpSearch(pattern,nameTerm,in,toSearch/*,cancel*/, true);
                }
                break;
            case CASE_INSENSITIVE_REGEXP:
                if (name.length() == 0) {
                    search(primaryField, name, NameKind.CASE_INSENSITIVE_PREFIX, scope, result, terms);
                    return;
                }
                else {   
                    final Pattern pattern = Pattern.compile(name,Pattern.CASE_INSENSITIVE);
                    if (Character.isJavaIdentifierStart(name.charAt(0))) {
                        regExpSearch(pattern, new Term (primaryField, name.toLowerCase()), in, toSearch/*,cancel*/, false);      //XXX: Locale
                    }
                    else {
                        regExpSearch(pattern, new Term (primaryField, ""), in, toSearch/*, cancel*/, false);      //NOI18N
                    }
                    break;
                }
            case REGEXP:
                if (name.length() == 0) {
                    search(primaryField, name, NameKind.PREFIX, scope, result, terms);
                    return;
                } else {
                    final Pattern pattern = Pattern.compile(name);
                    if (Character.isJavaIdentifierStart(name.charAt(0))) {
                        regExpSearch(pattern, new Term (primaryField, name), in, toSearch/*, cancel*/, true);
                    }
                    else {
                        regExpSearch(pattern, new Term (primaryField, ""), in, toSearch/*, cancel*/, true);             //NOI18N
                    }
                    break;
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }           
        TermDocs tds = in.termDocs();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("LuceneIndex.getDeclaredTypes[%s] returned %d elements\n",this.toString(), toSearch.size()));
        }
        final Iterator<Term> it = toSearch.iterator();        
        Set<Integer> docNums = new TreeSet<Integer>();
        Map<Integer,List<String>> matches = new HashMap<Integer,List<String>>();
        while (it.hasNext()) {
            Term next = it.next();
            tds.seek(next);
            while (tds.next()) {
                Integer docNum = Integer.valueOf(tds.doc());
                List<String> matchTerms = matches.get(docNum);
                if (matchTerms == null) {
                    matchTerms = new ArrayList<String>();
                    matches.put(docNum, matchTerms);
                }
                matchTerms.add(next.text());
                docNums.add(docNum);
            }
        }
        for (Integer docNum : docNums) {
            final Document doc = in.document(docNum);
            
            List<String> matchList = matches.get(docNum);
            FilteredDocumentSearchResult map = new FilteredDocumentSearchResult(doc, primaryField, matchList, docNum);
            result.add(map);
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
    
    
    // TODO: Create a filtered DocumentSearchResult here which
    // contains matches for a given document.
    
    private class DocumentSearchResult implements SearchResult {
        private Document doc;
        private int docId;
        
        private DocumentSearchResult(Document doc, int docId) {
            this.doc = doc;
            this.docId = docId;
        }

        public String getValue(String key) {
            return doc.get(key);
        }

        public String[] getValues(String key) {
            return doc.getValues(key);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Enumeration en = doc.fields();
            while (en.hasMoreElements()) {
                Field f = (Field)en.nextElement();
                sb.append(f.name());
                sb.append(":");
                sb.append(f.stringValue());
                sb.append("\n");
            }
            
            return sb.toString();
        }
    
        public int getDocumentNumber() {
            return docId;
        }

        public Object getDocument() {
            return doc;
        }

        public Object getIndexReader() {
            try {
                return getReader();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }

        public Object getIndex() {
            return LuceneIndex.this.classIndex;
        }

        public File getSegment() {
            return LuceneIndex.this.cacheRoot;
        }

        public String getPersistentUrl() {
            return getValue(DocumentUtil.FIELD_FILENAME);
        }
    }
    
    private class FilteredDocumentSearchResult implements SearchResult {
        private Document doc;
        private int docId;
        private String primaryKey;
        private List<String> primaryValues;
        
        private FilteredDocumentSearchResult(Document doc, String primaryKey, List<String> primaryValues, int docId) {
            this.doc = doc;
            this.primaryKey = primaryKey;
            this.primaryValues = primaryValues;
            this.docId = docId;
        }

        public String getValue(String key) {
            if (key.equals(primaryKey)) {
                if (primaryValues.size() > 0) {
                    return primaryValues.get(0);
                } else {
                    return null;
                }
            }
            return doc.get(key);
        }

        public String[] getValues(String key) {
            if (key.equals(primaryKey)) {
                return primaryValues.toArray(new String[primaryValues.size()]);
            }
            return doc.getValues(key);
        }
        
        public String getPersistentUrl() {
            return getValue(DocumentUtil.FIELD_FILENAME);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Enumeration en = doc.fields();
            while (en.hasMoreElements()) {
                Field f = (Field)en.nextElement();
                if (f.name().equals(primaryKey)) {
                    sb.append(primaryKey);
                    sb.append(":");
                    sb.append(primaryValues.toString());
                } else {
                    sb.append(f.name());
                    sb.append(":");
                    sb.append(f.stringValue());
                }
                sb.append("\n");
            }
            
            return sb.toString();
        }
    
        public int getDocumentNumber() {
            return docId;
        }

        public Object getDocument() {
            return doc;
        }
        
        public Object getIndexReader() {
            try {
                return getReader();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }

        public Object getIndex() {
            return LuceneIndex.this.classIndex;
        }

        public File getSegment() {
            return LuceneIndex.this.cacheRoot;
        }
    }
    
    private <T> void gsfEmptyPrefixSearch (final IndexReader in, final Set<SearchResult> result, 
                                        final Set<String> terms) throws IOException {
        final int bound = in.maxDoc();
        final FieldSelector fieldSelector = new CustomFieldSelector(terms);
        for (int i=0; i<bound; i++) {
            if (!in.isDeleted(i)) {
                final Document doc = in.document(i, fieldSelector);
                if (doc != null) {
                    SearchResult map = new DocumentSearchResult(doc, i);
                    result.add(map);
                }
            }
        }
    }


    private static class TermComparator implements Comparator<Term> {
        public int compare (Term t1, Term t2) {
            int ret = t1.field().compareTo(t2.field());
            if (ret == 0) {
                ret = t1.text().compareTo(t2.text());
            }
            return ret;
        }
    }
    
    // For symbol dumper only
    public IndexReader getDumpIndexReader() throws IOException {
        return getReader();
    }
}
