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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;


/**
 *
 * @author hanz
 */
public final class TLIndexerFactory extends EmbeddingIndexerFactory {

    private static final Logger LOG = Logger.getLogger (TLIndexerFactory.class.getName());

    public static final String  INDEXER_NAME = "TLIndexer"; //NOI18N
    public static final int     INDEXER_VERSION = 2;

    public static final String FIELD_GROUP_NAME = "groupName"; //NOI18N
    public static final String FIELD_DESCRIPTION = "description"; //NOI18N
    public static final String FIELD_LINE_NUMBER = "lineNumber"; //NOI18N

    @Override
    public EmbeddingIndexer createIndexer (
        Indexable               indexable,
        Snapshot                snapshot
    ) {
        return new TLIndexer ();
    }

    @Override
    public void filesDeleted (
        Iterable<? extends Indexable>
                                deleted,
        Context                 context
    ) {
        try {
            IndexingSupport indexingSupport = IndexingSupport.getInstance (context);
            for (Indexable indexable : deleted)
                indexingSupport.removeDocuments (indexable);
        } catch (IOException ex) {
            LOG.log (Level.WARNING, null, ex);
        }
    }

    @Override
    public void filesDirty (
        Iterable<? extends Indexable>
                                dirty,
        Context                 context
    ) {
        try {
            IndexingSupport indexingSupport = IndexingSupport.getInstance (context);
            for (Indexable indexable : dirty)
                indexingSupport.markDirtyDocuments (indexable);
        } catch (IOException ex) {
            LOG.log (Level.WARNING, null, ex);
        }
    }

    @Override
    public String getIndexerName () {
        return INDEXER_NAME;
    }

    @Override
    public int getIndexVersion () {
        return INDEXER_VERSION;
    }


    // innerclasses ............................................................

    private static Set<FileObject> karelPr = new HashSet<FileObject> ();

    private static final class TLIndexer extends EmbeddingIndexer {

        @Override
        protected void index (
            Indexable           indexable,
            Result              parserResult,
            Context             context
        ) {
            try {
                IndexingSupport indexingSupport = IndexingSupport.getInstance (context);
                ParserResult gsfParserResult = (ParserResult) parserResult;
                saveErrors (gsfParserResult.getDiagnostics (), gsfParserResult.getSnapshot(), indexingSupport, indexable);

                FileObject fileObject = parserResult.getSnapshot ().getSource ().getFileObject ();
                if (!karelPr.contains (fileObject)) {
                    karelPr.add (fileObject);
                    if (karelPr.size () == 1)
                        try {
                            TaskProcessor.runWhenScanFinished (
                                new ExceptionAction<Void> () {
                                    public Void run () throws Exception {
                                        for (FileObject fileObject : karelPr)
                                            GsfTaskProvider.refresh (fileObject);
                                        karelPr = new HashSet<FileObject> ();
                                        return null;
                                    }
                                },
                                Collections.<Source> emptyList ()
                            );
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace (ex);
                        }
                }
            } catch (IOException ex) {
                LOG.log (Level.WARNING, null, ex);
            }
        }
        
        private void saveErrors (
            List<? extends Error>
                                errors,
            Snapshot            snapshot,
            IndexingSupport     indexingSupport,
            Indexable           indexable
        ) {
            if (errors == null || errors.isEmpty ()) {
                indexingSupport.addDocument (indexingSupport.createDocument (indexable));
                return;
            }

            List<Integer> lineStartOffsets = getLineStartOffsets(snapshot);

            for (Error error : errors) {
                IndexDocument indexDocument = indexingSupport.createDocument (indexable);
                indexDocument.addPair (
                    FIELD_GROUP_NAME,
                    error.getSeverity () == org.netbeans.modules.csl.api.Severity.ERROR ?
                        "nb-tasklist-error" : //NOI18N
                        "nb-tasklist-warning", //NOI18N
                    false,
                    true
                );
                indexDocument.addPair (
                    FIELD_DESCRIPTION,
                    error.getDisplayName (),
                    false,
                    true
                );

                int originalOffset = snapshot.getOriginalOffset(error.getStartPosition());
                int lineNumber = 1;
                if (originalOffset >= 0) {
                    int idx = Collections.binarySearch(lineStartOffsets, originalOffset);
                    if (idx < 0) {
                        // idx == (-(insertion point) - 1) -> (insertion point) == -idx - 1
                        int ln = -idx - 1;
                        assert ln >= 1 && ln <= lineStartOffsets.size() :
                            "idx=" + idx + ", lineNumber=" + ln + ", lineStartOffsets.size()=" + lineStartOffsets.size(); //NOI18N
                        if (ln >= 1 && ln <= lineStartOffsets.size()) {
                            lineNumber = ln;
                        }
                    } else {
                        lineNumber = idx + 1;
                    }
                }
                
                indexDocument.addPair (
                    FIELD_LINE_NUMBER,
                    Integer.toString (lineNumber),
                    false,
                    true
                );
                indexingSupport.addDocument (indexDocument);
            }
        }

// XXX: ideally we should cache the lineStartOffsets, but the cache has to be dropped
// at the end of the indexing session and I don't know how to do that
//        private static final Map<Source, List<Integer>> lineStartOffsetsCache = new WeakHashMap<Source, List<Integer>>();
        private static List<Integer> getLineStartOffsets(Snapshot snapshot) {
            Source source = snapshot.getSource();
//            List<Integer> lineStartOffsets = lineStartOffsetsCache.get(source);
//
//            if (lineStartOffsets == null) {
                List<Integer> lineStartOffsets = new ArrayList<Integer>();

                lineStartOffsets.add(0);

                CharSequence text = source.createSnapshot().getText();
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '\n') { //NOI18N
                        lineStartOffsets.add(i + 1);
                    }
                }

//                lineStartOffsetsCache.put(source, lineStartOffsets);
//            }

            return lineStartOffsets;
        }
    } // End of TLIndexer class
}
