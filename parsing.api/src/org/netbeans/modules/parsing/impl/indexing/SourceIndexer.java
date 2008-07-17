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
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.MultiLanguageUserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Indexer of {@link Source} with possible embeddings
 * @author Tomas Zezula
 */
public class SourceIndexer extends CustomIndexer {
    
    private final Map<String,EmbeddingIndexer> embeddedIndexers;

    public SourceIndexer (final Map<String,EmbeddingIndexer> embeddedIndexers) {
        assert embeddedIndexers != null;
        this.embeddedIndexers = embeddedIndexers;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, final Context context) {
        final List<Indexable> dirtyFiles = new LinkedList<Indexable>();
        try {
        findDirtyFiles(files, context, dirtyFiles);
        //todo: Replace with multi source when done
        for (final Indexable dirty : dirtyFiles) {
            try {
                final FileObject fileObject = URLMapper.findFileObject(dirty.getURI().toURL());
                if (fileObject != null) {
                    final Source src = Source.create(fileObject);
                    ParserManager.parse(Collections.singleton(src), new MultiLanguageUserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            final String mimeType = src.getMimeType();
                            final EmbeddingIndexer indexer = embeddedIndexers.get(mimeType);
                            visit(resultIterator,indexer);
                        }

                        private void visit (final ResultIterator resultIterator,
                                final EmbeddingIndexer currentIndexer) throws ParseException {
                            if (currentIndexer != null) {
                                IndexingSPIAccessor.getInstance().index(currentIndexer, resultIterator.getParserResult(), context);
                            }
                            Iterable<? extends Embedding> embeddings = resultIterator.getEmbeddings();
                            for (Embedding embedding : embeddings) {
                                final String mimeType = embedding.getMimeType();
                                final EmbeddingIndexer indexer = embeddedIndexers.get(mimeType);                                
                                visit(resultIterator.getResultIterator(embedding), indexer);
                            }
                        }
                    });
                }
            } catch (final MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }
            catch (final ParseException e) {
                Exceptions.printStackTrace(e);
            }
        }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void findDirtyFiles (final Iterable<? extends Indexable> files, final Context ctx,
            Collection<? super Indexable> dirtyFiles) throws IOException {
        final Map <String,Indexable> lookup = new HashMap<String,Indexable>();
        for (Indexable indexable : files) {
            lookup.put(indexable.getName(), indexable);
        }
        final Map<String,Long> data = null; //todo: index should create this
        for (Map.Entry<String,Long> e : data.entrySet()) {
            Indexable indexable = lookup.remove(e.getKey());
            if (indexable == null) {
                //Removed file
                //todo: clean the document
            }
            else if (indexable.getLastModified() > e.getValue()) {
                //Modified file
                dirtyFiles.add(indexable);
            }
        }
        //Add new files
        dirtyFiles.addAll(lookup.values());

    }

}
