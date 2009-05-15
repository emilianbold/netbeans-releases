/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import antlr.TokenStream;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
/*package*/ final class FileTokenStreamCache {
    private final List<TSData> cacheData;
    public FileTokenStreamCache() {
        this.cacheData = new CopyOnWriteArrayList<TSData>();
    }

    public final TokenStream getTokenStreamInActiveBlock(boolean filtered, int start, int end, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros) {
        for (TSData pair : cacheData) {
            if (pair.pcState != null && pair.pcState.isInActiveBlock(start, end)) {
                return pair.getTS(filtered, start, end, firstTokenIDIfExpandMacros);
            }
        }
        return null;
    }

    public final void addNewPair(FilePreprocessorConditionState.Builder pcBuilder, TokenStream ts, APTLanguageFilter lang) {
        // after the next call builder will be ready to create pc state
        List<APTToken> tokens = APTUtils.toList(ts);
        FilePreprocessorConditionState pcState = pcBuilder.build();
        TSData newData = new TSData(pcState, tokens, lang);
        cacheData.add(newData);
    }

    private static final class TSData {
        private final FilePreprocessorConditionState pcState;
        private final List<APTToken> tokens;
        private final APTLanguageFilter lang;
        private final TreeMap<Integer, Integer> knownIndices = new TreeMap<Integer, Integer>();
        private TSData(FilePreprocessorConditionState pcState, List<APTToken> tokens, APTLanguageFilter lang) {
            this.pcState = pcState;
            this.tokens = tokens;
            this.lang = lang;
            rememberIndex(0, 0);
        }

        private TokenStream getTS(boolean filtered, int start, int endOffset, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros) {
            int iteratorIndex = checkKnownIndex(start);
            // create wrapper
            ListIterator<APTToken> iterator = tokens.listIterator(iteratorIndex);
            while (iterator.hasNext()) {
                APTToken next = iterator.next();
                int currOffset = next.getOffset();
                if (currOffset >= start) {
                    if ((firstTokenIDIfExpandMacros == 0) || (next.getType() == firstTokenIDIfExpandMacros) || !APTUtils.isMacroExpandedToken(next)) {
                        iterator.previous();
                        break;
                    }
                }
            }
            TokenStream ts = new IteratorBasedTS(tokens, iterator.nextIndex(), endOffset, this);
            rememberIndex(start, iterator.nextIndex());
            return filtered ? lang.getFilteredStream(new APTCommentsFilter(ts)) : ts;
        }

        private synchronized void rememberIndex(int startOffset, int listIndex) {
            knownIndices.put(Integer.valueOf(startOffset), Integer.valueOf(listIndex));
        }

        private synchronized int checkKnownIndex(int startOffset) {
            SortedMap<Integer, Integer> tailMap = knownIndices.tailMap(startOffset);
            Integer knownOffset;
            if (tailMap.isEmpty() || (tailMap.firstKey() > startOffset)) {
                knownOffset = knownIndices.headMap(startOffset).lastKey();
            } else {
                knownOffset = tailMap.firstKey();
            }
            return knownIndices.get(knownOffset);
        }
    }

    private final static class IteratorBasedTS implements TokenStream, APTTokenStream {
        private final ListIterator<APTToken> position;
        private final List<APTToken> debugTokens; // for debug only
        private final int debugStartIndex; // for debug only
        private final int endOffset;
        private final TSData callback;
        /** Creates a new instance of ListBasedTokenStream */
        public IteratorBasedTS(List<APTToken> tokens, int startIndex, int endOffset, TSData callback) {
            this.position = tokens.listIterator(startIndex);
            this.debugTokens = tokens;
            this.debugStartIndex = startIndex;
            this.endOffset = endOffset;
            this.callback = callback;
        }

        public APTToken nextToken() {
            if (position.hasNext()) {
                APTToken out = position.next();
                assert (out != null);
                assert (!APTUtils.isEOF(out));
                int offset = out.getOffset();
                if (offset > endOffset) {
                    out = APTUtils.EOF_TOKEN;
                    if (callback != null) {
                        callback.rememberIndex(offset, position.previousIndex());
                    }
                }
                return out;
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }

        @Override
        public String toString() {
            return APTUtils.debugString(new IteratorBasedTS(debugTokens, debugStartIndex, endOffset, null));
        }
    }
}
