/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.modules.java.source.usages.LuceneIndexMBeanImpl;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;

/**
 *
 * @author Tomas Zezula
 */
class LuceneIndex extends Index {
    
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N
    private static final String REFERENCES = "refs";    // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    
    private final Directory directory;
    private Long rootTimeStamp;
    
    private IndexReader reader; //Cache, do not use this dirrectly, use getReader
    private Set<String> rootPkgCache;   //Cache, do not use this dirrectly
    
    static Index create (final File cacheRoot) throws IOException {        
        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        return new LuceneIndex (getReferencesCacheFolder(cacheRoot));
    }
    
    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot) throws IOException {
        assert refCacheRoot != null;
        this.directory = FSDirectory.getDirectory(refCacheRoot, Index.isTest() ? NoLockFactory.getNoLockFactory() : new NBLockFactory());
    }

    @SuppressWarnings("unchecked") // NOI18N, unchecked - lucene has source 1.4
    private List<String> getUsagesData(final String resourceName, Set<ClassIndexImpl.UsageType> mask, BooleanOperator operator) throws IOException {                
        if (!isValid(false)) {
            return null;
        }        
        final Searcher searcher = new IndexSearcher (this.getReader());
        try {
            final List<String> result = new LinkedList<String> ();
            Query query = null;
            if (mask == null) {
                query = new WildcardQuery(DocumentUtil.referencesTerm (resourceName,null));
            }
            else {
                assert operator != null;
                switch (operator) {
                    case AND:
                        query = new WildcardQuery(DocumentUtil.referencesTerm (resourceName, mask));
                        break;
                    case OR:
                        BooleanQuery booleanQuery = new BooleanQuery ();
                        for (ClassIndexImpl.UsageType ut : mask) {
                            final Query subQuery = new WildcardQuery(DocumentUtil.referencesTerm (resourceName, EnumSet.of(ut)));
                            booleanQuery.add(subQuery, Occur.SHOULD);                        
                        }
                        query = booleanQuery;
                        break;
                    default:
                        throw new IllegalArgumentException (operator.toString());
                }
            }
            assert query != null;
            final Hits hits = searcher.search (query);
            for (Iterator<Hit> it = (Iterator<Hit>) hits.iterator(); it.hasNext();) {
                final Hit hit = it.next ();
                final Document doc = hit.getDocument();
                final String user = DocumentUtil.getBinaryName(doc);
                final String map = DocumentUtil.getRefereneType(doc,resourceName);
                if (map != null) {
                    result.add (DocumentUtil.encodeUsage(user,map));
                }
            }
            return result;
        } finally {
            searcher.close();
        }
    }

    @SuppressWarnings ("unchecked")     // NOI18N, unchecked - lucene has source 1.4
    public List<String> getUsagesFQN(final String resourceName, final Set<ClassIndexImpl.UsageType>mask, final BooleanOperator operator) throws IOException, InterruptedException {
        if (!isValid(false)) {
            return null;
        }
        final AtomicBoolean cancel = this.cancel.get();
        assert cancel != null;
        assert resourceName != null;
        assert mask != null;
        assert operator != null;
        final Searcher searcher = new IndexSearcher (this.getReader());
        Query query;
        try {
            final List<String> result = new LinkedList<String> ();
            switch (operator) {
                case AND:
                    query = new WildcardQuery(DocumentUtil.referencesTerm (resourceName, mask));
                    break;
                case OR:
                    BooleanQuery booleanQuery = new BooleanQuery ();
                    for (ClassIndexImpl.UsageType ut : mask) {
                        final Query subQuery = new WildcardQuery(DocumentUtil.referencesTerm (resourceName, EnumSet.of(ut)));
                        booleanQuery.add(subQuery, Occur.SHOULD);                        
                    }
                    query = booleanQuery;
                    break;
                default:
                    throw new IllegalArgumentException (operator.toString());
            }
            if (cancel.get()) {
                throw new InterruptedException ();
            }
            final Hits hits = searcher.search (query);
            for (Iterator<Hit> it = (Iterator<Hit>) hits.iterator(); it.hasNext();) {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
                final Hit hit = it.next ();
                final Document doc = hit.getDocument();
                final String user = DocumentUtil.getBinaryName(doc);
                result.add (user);
            }
            return result;
        } finally {
            searcher.close();
        }
    }

    private List<String> getReferencesData(final String resourceName) throws IOException {
        if (!isValid(false)) {
            return null;
        }    
        Searcher searcher = new IndexSearcher (this.getReader());
        try {
            Hits hits = searcher.search(DocumentUtil.binaryNameQuery(resourceName));
            assert hits.length() <= 1;
            if (hits.length() == 0) {
                return null;
            }
            else {
                Hit hit = (Hit) hits.iterator().next();
                return DocumentUtil.getReferences(hit.getDocument());
            }
        }
        finally {
            searcher.close();
        }
    }
        
    public String getSourceName (final String resourceName) throws IOException {
        if (!isValid(false)) {
            return null;
        }
        Searcher searcher = new IndexSearcher (this.getReader());
        try {
            Hits hits = searcher.search(DocumentUtil.binaryNameQuery(resourceName));
            if (hits.length() == 0) {
                return null;
            }
            else {
                Hit hit = (Hit) hits.iterator().next();
                return DocumentUtil.getSourceName(hit.getDocument());
            }
        } finally {
            searcher.close();
        }
    }
        
    @SuppressWarnings ("unchecked") // NOI18N, unchecked - lucene has source 1.4
    public <T> void getDeclaredTypes (final String name, final ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Set<? super T> result) throws IOException, InterruptedException {
        if (!isValid(false)) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
            return;
        }
        final AtomicBoolean cancel = this.cancel.get();
        assert cancel != null;
        assert name != null;                
        final Set<Term> toSearch = new TreeSet<Term> (new Comparator<Term>(){
            public int compare (Term t1, Term t2) {
                int ret = t1.field().compareTo(t2.field());
                if (ret == 0) {
                    ret = t1.text().compareTo(t2.text());
                }
                return ret;
            }
        });
        final IndexReader in = getReader();
        switch (kind) {
            case SIMPLE_NAME:
                {
                    toSearch.add(DocumentUtil.simpleNameTerm(name));
                    break;
                }
            case PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    emptyPrefixSearch(in, convertor, result, cancel);
                    return;
                }
                else {
                    final Term nameTerm = DocumentUtil.simpleNameTerm(name);
                    prefixSearh(nameTerm, in, toSearch, cancel);
                    break;
                }
            case CASE_INSENSITIVE_PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    emptyPrefixSearch(in, convertor, result, cancel);
                    return;
                }
                else {                    
                    final Term nameTerm = DocumentUtil.caseInsensitiveNameTerm(name.toLowerCase());     //XXX: I18N, Locale
                    prefixSearh(nameTerm, in, toSearch, cancel);
                    break;
                }
            case CAMEL_CASE:
                if (name.length() == 0) {
                    throw new IllegalArgumentException ();
                }        
                {
                final StringBuilder patternString = new StringBuilder ();                        
                char startChar = 0;
                for (int i=0; i<name.length(); i++) {
                    char c = name.charAt(i);
                    //todo: maybe check for upper case, I18N????
                    if (i == 0) {
                        startChar = c;
                    }
                    patternString.append(c);
                    if (i == name.length()-1) {
                        patternString.append("\\w*");  // NOI18N
                    }
                    else {
                        patternString.append("[\\p{Lower}\\p{Digit}]*");  // NOI18N
                    }
                }
                final Pattern pattern = Pattern.compile(patternString.toString());
                regExpSearch(pattern,DocumentUtil.simpleNameTerm(Character.toString(startChar)),in,toSearch,cancel);
                break;
                }
            case CASE_INSENSITIVE_REGEXP:
                if (name.length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
                    throw new IllegalArgumentException ();
                }
                {   
                    final Pattern pattern = Pattern.compile(name,Pattern.CASE_INSENSITIVE);
                    regExpSearch(pattern, DocumentUtil.caseInsensitiveNameTerm(name.toLowerCase()), in, toSearch,cancel);      //XXX: Locale
                    break;
                }
            case REGEXP:
                if (name.length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
                    throw new IllegalArgumentException ();
                }                
                {   
                    final Pattern pattern = Pattern.compile(name);                    
                    regExpSearch(pattern, DocumentUtil.simpleNameTerm(name), in, toSearch, cancel);
                    break;
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }           
        TermDocs tds = in.termDocs();
        LOGGER.fine(String.format("LuceneIndex.getDeclaredTypes[%s] returned %d elements\n",this.toString(), toSearch.size()));
        final Iterator<Term> it = toSearch.iterator();        
        final ElementKind[] kindHolder = new ElementKind[1];
        Set<Integer> docNums = new TreeSet<Integer>();
        while (it.hasNext()) {
            if (cancel.get()) {
                throw new InterruptedException ();
            }
            tds.seek(it.next());
            while (tds.next()) {
                docNums.add (tds.doc());
            }
        }
        for (Integer docNum : docNums) {
            if (cancel.get()) {
                throw new InterruptedException ();
            }
            final Document doc = in.document(docNum);
            final String binaryName = DocumentUtil.getBinaryName(doc, kindHolder);
            result.add (convertor.convert(kindHolder[0],binaryName));
        }        
    }
    
    private void regExpSearch (final Pattern pattern, final Term startTerm, final IndexReader in, final Set<Term> toSearch, final AtomicBoolean cancel) throws IOException, InterruptedException {        
        final String startText = startTerm.text();
        final StringBuilder startBuilder = new StringBuilder ();
        startBuilder.append(startText.charAt(0));
        for (int i=1; i<startText.length(); i++) {
            char c = startText.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            startBuilder.append(c);
        }
        final String startPrefix = startBuilder.toString();
        final String camelField = startTerm.field();
        final TermEnum en = in.terms(startTerm);
        try {
            do {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
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
    
    private <T> void emptyPrefixSearch (final IndexReader in, final ResultConvertor<T> convertor, final Set<? super T> result, final AtomicBoolean cancel) throws IOException, InterruptedException {        
        final int bound = in.maxDoc();        
        final ElementKind[] kindHolder = new ElementKind[1];
        for (int i=0; i<bound; i++) {
            if (cancel.get()) {
                throw new InterruptedException ();
            }
            if (!in.isDeleted(i)) {
                final Document doc = in.document(i);
                if (doc != null) {
                    String binaryName = DocumentUtil.getBinaryName (doc, kindHolder);
                    if (binaryName == null) {
                        //Root timestamp document
                        continue;
                    }
                    else {
                        result.add (convertor.convert(kindHolder[0],binaryName));
                    }
                }
            }
        }
    }
    
    private void prefixSearh (Term nameTerm, final IndexReader in, final Set<Term> toSearch, final AtomicBoolean cancel) throws IOException, InterruptedException {
        final String prefixField = nameTerm.field();
        final String name = nameTerm.text();
        final TermEnum en = in.terms(nameTerm);
        try {
            do {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
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
    
    
    public void getPackageNames (final String prefix, final boolean directOnly, final Set<String> result) throws IOException, InterruptedException {        
        if (!isValid(false)) {
            return;
        }
        final AtomicBoolean cancel = this.cancel.get();
        assert cancel != null;
        final IndexReader in = getReader();
        final Term pkgTerm = DocumentUtil.packageNameTerm (prefix);
        final String prefixField = pkgTerm.field();
        if (prefix.length() == 0) {                
            if (directOnly && this.rootPkgCache != null) {
                result.addAll(this.rootPkgCache);
            }
            else {
                if (directOnly) {
                    this.rootPkgCache = new HashSet<String>();
                }
                final TermEnum terms = in.terms ();
                try {
                    do {
                        if (cancel.get()) {
                            throw new InterruptedException ();
                        }
                        final Term currentTerm = terms.term();
                        if (currentTerm != null && prefixField == currentTerm.field()) {
                            String pkgName = currentTerm.text();
                            if (directOnly) {
                                int index = pkgName.indexOf('.',prefix.length());
                                if (index>0) {
                                    pkgName = pkgName.substring(0,index);
                                }
                                this.rootPkgCache.add(pkgName);
                            }
                            result.add(pkgName);
                        }
                    } while (terms.next());
                } finally {
                    terms.close();
                }
            }
        }
        else {
            final TermEnum terms = in.terms (pkgTerm);
            try {
                do {
                    if (cancel.get()) {
                        throw new InterruptedException ();
                    }
                    final Term currentTerm = terms.term();
                    if (currentTerm != null && prefixField == currentTerm.field() && currentTerm.text().startsWith(prefix)) {
                        String pkgName = currentTerm.text();
                        if (directOnly) {
                            int index = pkgName.indexOf('.',prefix.length());
                            if (index>0) {
                                pkgName = pkgName.substring(0,index);
                            }
                        }
                        result.add(pkgName);
                    }
                    else {
                        break;
                    }
                } while (terms.next());
            } finally {
                terms.close();
            }
        }
    }

    public boolean isUpToDate(String resourceName, long timeStamp) throws IOException {        
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

                assert hits.length() <= 1;
                if (hits.length() == 0) {
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
    
    public void store (final Map<String, Pair<String,List<String>>> refs, final List<String> topLevels) throws IOException {
        this.rootPkgCache = null;
        boolean create = !isValid (false);
        long timeStamp = System.currentTimeMillis();
        if (!create) {
            IndexReader in = getReader();
            final Searcher searcher = new IndexSearcher (in);
            try {
                for (String topLevel : topLevels) {            
                    Hits hits = searcher.search(DocumentUtil.binaryContentNameQuery(topLevel));
                    for (int i=0; i<hits.length(); i++) {
                        in.deleteDocument (hits.id(i));                    
                    }
                }
                in.deleteDocuments (DocumentUtil.rootDocumentTerm());
            } finally {
                searcher.close();
            }
        }
        storeData(refs, create, timeStamp);
    }

    public void store(final Map<String, Pair<String,List<String>>> refs, final Set<String> toDelete) throws IOException {
        this.rootPkgCache = null;
        boolean create = !isValid (false);        
        long timeStamp = System.currentTimeMillis();
        if (!create) {
            IndexReader in = getReader();
            final Searcher searcher = new IndexSearcher (in);
            try {
                for (String toDeleteItem : toDelete) {
                    Hits hits = searcher.search(DocumentUtil.binaryNameQuery(toDeleteItem));
//                    Disabled assertion until the signature file links are integrated
//                    assert hits.length() <=1 : "Multiple index entries for binaryName: " + toDeleteItem;    //NOI18N
                    if (hits.length()>1) {
                        LOGGER.warning("Multiple index entries for binaryName: " + toDeleteItem); //NOI18N
                    }
                    for (int i=0; i<hits.length(); i++) {
                        in.deleteDocument (hits.id(i));
                    }
                }
                in.deleteDocuments (DocumentUtil.rootDocumentTerm());
            } finally {
                searcher.close();
            }
        }
        storeData(refs, create, timeStamp);
    }    
    
    private void storeData (final Map<String, Pair<String,List<String>>> refs, final boolean create, final long timeStamp) throws IOException {        
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
                for (Iterator<Map.Entry<String,Pair<String,List<String>>>> it = refs.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String,Pair<String,List<String>>> refsEntry = it.next();
                    it.remove();
                    String cn = refsEntry.getKey();
                    Pair<String,List<String>> ev = refsEntry.getValue();
                    List<String> cr = ev.second;
                    Document newDoc = DocumentUtil.createDocument(cn,timeStamp,cr,ev.first);
                    activeOut.addDocument(newDoc);
                    if (memDir != null && lmListener.lowMemory.getAndSet(false)) {                       
                        activeOut.close();
                        out.addIndexes(new Directory[] {memDir});                        
                        memDir = new RAMDirectory ();        
                        activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
                    }
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

    public boolean isValid (boolean tryOpen) throws IOException {  
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
        this.close ();
        final String[] content = this.directory.list();
        for (String file : content) {
            directory.deleteFile(file);
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
        }
    }
    
    public @Override String toString () {
        return this.directory.toString();
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
    
    private static class LMListener implements LowMemoryListener {        
        
        private AtomicBoolean lowMemory = new AtomicBoolean (false);
        
        public void lowMemory(LowMemoryEvent event) {
            lowMemory.set(true);
        }        
    }
    
}
