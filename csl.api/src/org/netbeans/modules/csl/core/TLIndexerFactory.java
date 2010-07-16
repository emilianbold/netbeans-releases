/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;


/**
 *
 * @author hanz
 */
public final class TLIndexerFactory extends EmbeddingIndexerFactory {

    private static final Logger LOG = Logger.getLogger (TLIndexerFactory.class.getName());

    public static final String  INDEXER_NAME = "TLIndexer"; //NOI18N
    public static final int     INDEXER_VERSION = 4;

    public static final String FIELD_GROUP_NAME = "groupName"; //NOI18N
    public static final String FIELD_DESCRIPTION = "description"; //NOI18N
    public static final String FIELD_LINE_NUMBER = "lineNumber"; //NOI18N

    private static final Map<Indexable, Collection<Error>> errors = new IdentityHashMap<Indexable, Collection<Error>>();
    private static final Map<Indexable, List<Integer>> lineStartOffsetsCache = new IdentityHashMap<Indexable, List<Integer>>();
    
    @Override
    public boolean scanStarted(final Context context) {
        return true;
    }

    @Override
    public void scanFinished(Context context) {
        commitErrors(context.getRootURI(), errors, lineStartOffsetsCache);
    }

    private static void commitErrors(URL root, Map<Indexable, Collection<Error>> errors, Map<Indexable, List<Integer>> lineStartOffsetsCache) {
        for (Entry<Indexable, Collection<Error>> e : errors.entrySet()) {
            ErrorsCache.setErrors(root, e.getKey(), e.getValue(), new ErrorConvertorImpl(lineStartOffsetsCache.get(e.getKey())));
        }

        errors.clear();
        lineStartOffsetsCache.clear();
    }

    @Override
    public synchronized EmbeddingIndexer createIndexer (
        Indexable               indexable,
        Snapshot                snapshot
    ) {
        assert errors != null;
        assert lineStartOffsetsCache != null;

        return new TLIndexer(errors, lineStartOffsetsCache);
    }

    @Override
    public void filesDeleted (
        Iterable<? extends Indexable>
                                deleted,
        Context                 context
    ) {
        for (Indexable indexable : deleted) {
            ErrorsCache.setErrors(context.getRootURI(), indexable, Collections.<Error>emptyList(), DUMMY);
        }
    }

    @Override
    public void rootsRemoved (final Iterable<? extends URL> removedRoots) {
        //Not needed now
    }

    @Override
    public void filesDirty (
        Iterable<? extends Indexable>
                                dirty,
        Context                 context
    ) {
    }

    @Override
    public String getIndexerName () {
        return INDEXER_NAME;
    }

    @Override
    public int getIndexVersion () {
        return INDEXER_VERSION;
    }

    private static final class ErrorConvertorImpl implements Convertor<Error>{
        private final List<Integer> lineStartOffsets;
        public ErrorConvertorImpl(List<Integer> lineStartOffsets) {
            this.lineStartOffsets = lineStartOffsets;
        }
        public ErrorKind getKind(Error error) {
            if (error.getSeverity() == Severity.WARNING) {
                return ErrorKind.WARNING;
            } else if (error instanceof Badging && ((Badging) error).showExplorerBadge()) {
                return ErrorKind.ERROR;
            } else {
                return ErrorKind.ERROR_NO_BADGE;
            }
        }
        public int getLineNumber(Error error) {
            // #172100, ParserResult.getDiagnostics() uses document offsets rather then snapshot offsets
            int originalOffset = error.getStartPosition();
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

            return lineNumber;
        }
        public String getMessage(Error error) {
            return error.getDisplayName();
        }
    }
    
    private static final ErrorConvertorImpl DUMMY = new ErrorConvertorImpl(Collections.<Integer>emptyList());

    
    // innerclasses ............................................................

    private static final class TLIndexer extends EmbeddingIndexer {

        private final Map<Indexable, Collection<Error>> errors;
        private final Map<Indexable, List<Integer>> lineStartOffsetsCache;

        public TLIndexer(Map<Indexable, Collection<Error>> errors, Map<Indexable, List<Integer>> lineStartOffsetsCache) {
            this.errors = errors;
            this.lineStartOffsetsCache = lineStartOffsetsCache;
        }

        @Override
        protected void index (
            Indexable           indexable,
            Result              parserResult,
            Context             context
        ) {
            ParserResult gsfParserResult = (ParserResult) parserResult;

            if (!errors.containsKey(indexable)) {
                commitErrors(context.getRootURI(), errors, lineStartOffsetsCache);
            }

            Collection<Error> storedErrors = this.errors.get(indexable);

            if (storedErrors == null) {
                this.errors.put(indexable, storedErrors = new LinkedList<Error>());
            }

            if (errors == null) {
                return;
            }

            List<Integer> lineStartOffsets = lineStartOffsetsCache.get(indexable);

            if (lineStartOffsets == null) {
                lineStartOffsetsCache.put(indexable, getLineStartOffsets(gsfParserResult.getSnapshot().getSource()));
            }

            storedErrors.addAll(gsfParserResult.getDiagnostics ());
        }

        private static List<Integer> getLineStartOffsets(Source source) {
            List<Integer> lineStartOffsets = new ArrayList<Integer>();

            lineStartOffsets.add(0);

            CharSequence text = source.createSnapshot().getText();
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') { //NOI18N
                    lineStartOffsets.add(i + 1);
                }
            }

            return lineStartOffsets;
        }
    } // End of TLIndexer class

}
