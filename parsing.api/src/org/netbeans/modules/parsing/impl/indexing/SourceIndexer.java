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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Indexer of {@link Source} with possible embeddings
 * @author Tomas Zezula
 */
public class SourceIndexer {

    private static final Logger LOG = Logger.getLogger(SourceIndexer.class.getName());
    
    private final URL rootURL;
    private final FileObject cache;
    private final boolean followUpJob;
    private final boolean checkForEditorModifications;
    private final Map<String,EmbeddingIndexerFactory> embeddedIndexers = new HashMap<String, EmbeddingIndexerFactory>();

    public SourceIndexer(URL rootURL, FileObject cache, boolean followUpJob, boolean checkForEditorModifications) {
        assert rootURL != null;
        assert cache != null;
        this.rootURL = rootURL;
        this.cache = cache;
        this.followUpJob = followUpJob;
        this.checkForEditorModifications = checkForEditorModifications;
    }

    protected void index(Iterable<? extends Indexable> files, final List<Context> transactionContexts) throws IOException {
        // XXX: Replace with multi source when done
        for (final Indexable dirty : files) {
            try {
                final FileObject fileObject = URLMapper.findFileObject(dirty.getURL());
                if (fileObject != null) {
                    final Source src = Source.create(fileObject);
                    ParserManager.parse(Collections.singleton(src), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            final String mimeType = src.getMimeType();
                            final EmbeddingIndexerFactory indexer = findIndexer (mimeType);
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Indexing " + fileObject.getPath() + "; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                            }
                            visit(resultIterator,indexer);
                        }

                        private void visit (final ResultIterator resultIterator,
                                final EmbeddingIndexerFactory currentIndexerFactory) throws ParseException,IOException {
                            if (currentIndexerFactory != null) {
                                final Parser.Result pr = resultIterator.getParserResult();
                                if (pr != null) {
                                    final String indexerName = currentIndexerFactory.getIndexerName();
                                    final int indexerVersion = currentIndexerFactory.getIndexVersion();
                                    final Context context = SPIAccessor.getInstance().createContext(cache, rootURL, indexerName, indexerVersion, null, followUpJob, checkForEditorModifications);
                                    transactionContexts.add(context);

                                    final EmbeddingIndexer indexer = currentIndexerFactory.createIndexer(dirty, pr.getSnapshot());
                                    if (indexer != null) {
                                        try {
                                            SPIAccessor.getInstance().index(indexer, dirty, pr, context);
                                        } catch (ThreadDeath td) {
                                            throw td;
                                        } catch (Throwable t) {
                                            LOG.log(Level.WARNING, null, t);
                                        }
                                    }
                                }
                            }
                            Iterable<? extends Embedding> embeddings = resultIterator.getEmbeddings();
                            for (Embedding embedding : embeddings) {
                                final String mimeType = embedding.getMimeType();
                                final EmbeddingIndexerFactory indexerFactory = findIndexer(mimeType);
                                visit(resultIterator.getResultIterator(embedding), indexerFactory);
                            }
                        }
                    });
                }
            } catch (final ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }        
    }

    private EmbeddingIndexerFactory findIndexer (final String mimeType) {
        assert mimeType != null;
        EmbeddingIndexerFactory indexer = embeddedIndexers.get(mimeType);
        if (indexer != null) {
            return indexer;
        }
        indexer = MimeLookup.getLookup(mimeType).lookup(EmbeddingIndexerFactory.class);
        if (indexer != null) {
            embeddedIndexers.put(mimeType, indexer);
        }
        return indexer;
    }

}
