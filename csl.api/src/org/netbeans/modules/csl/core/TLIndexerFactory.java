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
import java.lang.Void;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.SupportAccessor;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;


/**
 *
 * @author hanz
 */
public final class TLIndexerFactory extends EmbeddingIndexerFactory {

    private static final Logger LOG = Logger.getLogger (TLIndexerFactory.class.getName());

    public static final String  INDEXER_NAME = "TLIndexer";
    public static final int     INDEXER_VERSION = 1;

    @Override
    public EmbeddingIndexer createIndexer (
        Indexable               indexable,
        Snapshot                snapshot
    ) {
        return new TLIndexer ();
    }

    @Override
    public void filesDeleted (
        Collection<? extends Indexable>
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
        Collection<? extends Indexable>
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

    private static Set<FileObject>
                                karelPr = new HashSet<FileObject> ();

    private static class TLIndexer extends EmbeddingIndexer {

        @Override
        protected void index (
            Indexable           indexable,
            Result              parserResult,
            Context             context
        ) {
            try {
                ParserResult gsfParserResult = (ParserResult) parserResult;
                Snapshot snapshot = parserResult.getSnapshot ();
                String mimeType = parserResult.getSnapshot ().getMimeType ();
                final LanguageRegistry registry = LanguageRegistry.getInstance ();
                Language language = registry.getLanguageByMimeType (mimeType);
                IndexingSupport indexingSupport = IndexingSupport.getInstance (context);
                HintsProvider provider = language.getHintsProvider ();
                if (provider == null) {
                    saveErrors (gsfParserResult.getDiagnostics (), snapshot, indexingSupport, indexable);
                } else {
                    GsfHintsManager gsfHintsManager = language.getHintsManager ();
                    if (gsfHintsManager == null) {
                        return;
                    }
                    RuleContext ruleContext = gsfHintsManager.createRuleContext (gsfParserResult, language, -1, -1, -1);
                    if (ruleContext == null) {
                        saveErrors (gsfParserResult.getDiagnostics (), snapshot, indexingSupport, indexable);
                    } else {
                        final List<Hint> hints = new ArrayList<Hint> ();
                        List<Error> errors = new ArrayList<Error> ();
                        provider.computeErrors (gsfHintsManager, ruleContext, hints, errors);
                        provider.computeHints (gsfHintsManager, ruleContext, hints);
                        saveErrors (errors, snapshot, indexingSupport, indexable);
                        saveHints (hints, gsfHintsManager, indexingSupport, indexable, gsfParserResult, language);
                    }
                }
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
            for (Error error : errors) {
                IndexDocument indexDocument = indexingSupport.createDocument (indexable);
                indexDocument.addPair (
                    "groupName",
                    error.getSeverity () == org.netbeans.modules.csl.api.Severity.ERROR ?
                        "nb-tasklist-error" :
                        "nb-tasklist-warning",
                    true,
                    true
                );
                indexDocument.addPair (
                    "description",
                    error.getDisplayName (),
                    true,
                    true
                );
                indexDocument.addPair (
                    "lineNumber",
                    Integer.toString (getLineNumber (snapshot, error.getStartPosition ())),
                    true,
                    true
                );
                indexingSupport.addDocument (indexDocument);
            }
        }

        private static int getLineNumber (
            Snapshot            snapshot,
            int                 offset
        ) {
            int originalOffset = snapshot.getOriginalOffset (offset);
            if (originalOffset < 0) return 0;
            Snapshot originalSnapshot = snapshot.getSource ().createSnapshot ();
            String text = originalSnapshot.getText ().toString ();
            int i = text.indexOf ('\n'), o = 0;
            int lineNumber = 1;
            while (i >= 0) {
                if (originalOffset <= i)
                    return lineNumber;
                i = text.indexOf ('\n', i + 1);
                lineNumber++;
            }
            return lineNumber;
        }

        private void saveHints (
            List<Hint>          hints,
            GsfHintsManager     gsfHintsManager,
            IndexingSupport     indexingSupport,
            Indexable           indexable,
            ParserResult        gsfParserResult,
            Language            language
        ) {
            if (hints == null || hints.isEmpty ()) {
                indexingSupport.addDocument (indexingSupport.createDocument (indexable));
                return;
            }
            RuleContext ruleContext = gsfHintsManager.createRuleContext (
                gsfParserResult,
                language,
                -1, -1, -1
            );
            if (ruleContext == null) return;
            if (hints != null)
                for (Hint hint : hints) {
                    ErrorDescription errorDescription = gsfHintsManager.createDescription (
                        hint, ruleContext, false
                    );
                    if (errorDescription == null) continue;
                    try {
                        IndexDocument indexDocument = indexingSupport.createDocument (indexable);
                        indexDocument.addPair (
                            "lineNumber",
                            Integer.toString (
                                errorDescription.getRange ().getBegin ().getLine () + 1
                            ),
                            true,
                            true
                        );
                        indexDocument.addPair (
                            "groupName",
                            (errorDescription.getSeverity () == Severity.ERROR) ?
                                "nb-tasklist-errorhint" :
                                "nb-tasklist-warninghint",
                            true,
                            true
                        );
                        indexDocument.addPair (
                            "description",
                            errorDescription.getDescription (),
                            true,
                            true
                        );
                        indexingSupport.addDocument (indexDocument);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace (ex);
                    }
                } // for
        } // saveHints
    }
}
