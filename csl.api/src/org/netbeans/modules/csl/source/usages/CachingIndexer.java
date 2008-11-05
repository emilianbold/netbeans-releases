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

package org.netbeans.modules.csl.source.usages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Indexer;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.IndexDocument;
import org.netbeans.modules.csl.api.IndexDocumentFactory;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * This class takes parse results, indexes them and stashes the resulting
 * objects until flush is called - or until the number of documents stashed
 * reaches a given limit. When that happens, the lucene index is updated.
 */
public class CachingIndexer {
    /** Number of files whose index results we cache per file system
     *  and language type until we flush the cache.
     */
    private static final int CACHE_INDEX_SIZE = 300;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());

    private URL root;
    private int fileCountGuess;
    private Map<Language,LanguageIndex> indices = new HashMap<Language,LanguageIndex>();

    public static CachingIndexer get(URL root, int size) {
        return new CachingIndexer(size, root);
    }

    public CachingIndexer(int fileCountGuess, URL root) {
        this.fileCountGuess = Math.min(2000, fileCountGuess);
        this.root = root;

    }

    private LanguageIndex getLanguageIndex(Language language) throws IOException {
        LanguageIndex li = indices.get(language);
        if (li == null) {
            ClassIndexImpl uqImpl = ClassIndexManager.get(language).createUsagesQuery(root, true);
            assert uqImpl != null;
            SourceAnalyser analyzer = uqImpl.getSourceAnalyser();
            assert analyzer != null;

            List<IndexBatchEntry> batchList = new ArrayList<IndexBatchEntry>(fileCountGuess);
            boolean create = analyzer.hasData();

            li = new LanguageIndex(analyzer, batchList, create, language);
            indices.put(language, li);
        }

        return li;
    }

    public void flush() throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "FLUSHING INDEX for root " + root);
        }

        for (LanguageIndex li : indices.values()) {
            li.flush();
        }
    }

    public void index(Language language, File file, Iterable<ParserResult> trees) throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Stashing index documents for file " + file);
        }
        LanguageIndex li = getLanguageIndex(language);
        li.index(file, trees);
    }
    
    public void remove(Language language, String url) throws IOException {
        LanguageIndex li = getLanguageIndex(language);
        li.remove(url);
    }

    /**
     * Each LanguageIndex object represents cached index data for a specific language.
     * It stores information relevant to caching and flushing index data for the
     * given analyzer.
     */
    private static final class LanguageIndex implements IndexDocumentFactory {
        final SourceAnalyser analyzer;
        final List<IndexBatchEntry> entries;
        private int size;
        private boolean create;
        private final Language language;

        public LanguageIndex(SourceAnalyser analyzer, List<IndexBatchEntry> entries, boolean create, Language language) {
            this.analyzer = analyzer;
            this.entries = entries;
            this.create = create;
            this.language = language;
        }

        public SourceAnalyser getAnalyzer() {
            return analyzer;
        }

        public void flush() throws IOException {
            try {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Flushing index for " + language.getDisplayName() + "; the number of stashed documents is " + entries.size());
                }

                if (entries.size() > 0) {
                    analyzer.batchStore(entries, create);
                    entries.clear();
                }
            } finally {
                create = false;
            }
        }

        public void index(File file, Iterable<ParserResult> trees) throws IOException {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Stashing index documents for file " + file);
            }
            Indexer indexer = language.getIndexer();
            assert indexer != null : language;
            if (indexer != null) {
                String fileUrl = indexer.getPersistentUrl(file);
                for (ParserResult result : trees) {
                    @SuppressWarnings("unchecked") // I control the factory so this is safe
                    List<IndexDocumentImpl> documents = (List)indexer.index(result, this);
                    // Null means delete this document from the index which is different than
                    // a document with no indexable information
                    if (documents == null) {
                        documents = Collections.emptyList();
                    }
                    IndexBatchEntry entry = new IndexBatchEntry(fileUrl, language, documents, analyzer);
                    entries.add(entry);
                }
                size++;

                if (size == CACHE_INDEX_SIZE) {
                    flush();
                    size = 0;
                }
            }
        }

        private void remove(String fileUrl) {
            Indexer indexer = language.getIndexer();
            assert indexer != null : language;
            if (indexer != null) {
                IndexBatchEntry entry = new IndexBatchEntry(fileUrl, language, null, analyzer);
                entries.add(entry);
                size++;
            }            
        }
        
        public IndexDocument createDocument(int initialPairs) {
            return new IndexDocumentImpl(initialPairs);
        }

        public IndexDocument createDocument(int initialPairs, String overrideUrl) {
            return new IndexDocumentImpl(initialPairs, overrideUrl);
        }
    }
}
