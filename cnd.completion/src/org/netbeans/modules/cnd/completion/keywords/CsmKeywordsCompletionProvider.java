/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.keywords;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author alsimon
 */
public class CsmKeywordsCompletionProvider implements CompletionProvider {

    public CsmKeywordsCompletionProvider() {
        // default constructor to be created as lookup service
    }

    private final static boolean TRACE = Boolean.getBoolean("cnd.completion.keywords.trace");

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (TRACE) {
            System.err.println("typed text " + typedText);
        }
        return 0;
//        CompletionSupport sup = CompletionSupport.get(component);
//        if (sup == null) {
//            return 0;
//        }
//        int dot = component.getCaretPosition();
//        if (TRACE) {
//            System.err.println("keywords completion will be shown on " + dot); // NOI18N
//        }
//        return COMPLETION_QUERY_TYPE;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (TRACE) {
            System.err.println("queryType = " + queryType); // NOI18N
        }
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            int dot = component.getCaret().getDot();
            if (TRACE) {
                System.err.println("keywords completion task is created with offset " + dot); // NOI18N
            }
            return new AsyncCompletionTask(new Query(dot), component);
        }
        return null;
    }

    // method for tests
    /*package*/ static Collection<CsmKeywordCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset);
        Collection<CsmKeywordCompletionItem> items = query.getItems(doc, caretOffset);
        if (TRACE) {
            System.err.println("Completion Items " + items.size());
            for (CsmKeywordCompletionItem completionItem : items) {
                System.err.println(completionItem.toString());
            }
        }
        return items;
    }

    private static final String[] keywords;
    static {
        List<String> list = new ArrayList<String>();
        for(CppTokenId token : CppTokenId.values()) {
            if (CppTokenId.KEYWORD_CATEGORY.equals(token.primaryCategory()) ||
                CppTokenId.KEYWORD_DIRECTIVE_CATEGORY.equals(token.primaryCategory())) {
                final String text = token.fixedText();
                if (text != null && text.length() > 2) {
                    list.add(text);
                }
            }
        }
        keywords = list.toArray(new String[list.size()]);
    }
    
    private static final class Query extends AsyncCompletionQuery {

        private Collection<CsmKeywordCompletionItem> results;
        private int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;

        /*package*/ Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (TRACE) {
                System.err.println("query on " + caretOffset + " anchor " + queryAnchorOffset); // NOI18N
            }
            Collection<CsmKeywordCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset > 0) {
                if (items != null && items.size() > 0) {
                    this.results = items;
                    items = getFilteredData(items, this.filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(queryAnchorOffset);
                }
                resultSet.setHasAdditionalItems(false);
            }
            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            if (TRACE) {
                System.err.println("canFilter on " + caretOffset + " anchor " + queryAnchorOffset); // NOI18N
            }
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    Completion.get().hideCompletion();
                }
            } else {
                Completion.get().hideCompletion();
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CsmKeywordCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private Filter<CppTokenId> getLanguageFilter(BaseDocument doc) {
            Language<?> language = (Language<?>) doc.getProperty(Language.class);
            if (language != null) {
                InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
                if (lexerAttrs != null) {
                    return (Filter<CppTokenId>) lexerAttrs.getValue(LanguagePath.get(language), CndLexerUtilities.LEXER_FILTER);
                }
            }
            return null;
        }
        
        
        private Collection<CsmKeywordCompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            Collection<CsmKeywordCompletionItem> items = new ArrayList<CsmKeywordCompletionItem>();
            try {
                if (init(doc, caretOffset)) {
                    Filter<CppTokenId> languageFilter = getLanguageFilter(doc);
                    for (String string : keywords) {
                        if (languageFilter != null) {
                            if (languageFilter.check(string) == null) {
                                continue;
                            }
                        }
                        items.add(CsmKeywordCompletionItem.createItem(queryAnchorOffset, caretOffset, string));
                    }
                }
            } catch (BadLocationException ex) {
                // no completion
            }
            return items;
        }
        
        private boolean init(final BaseDocument doc, final int caretOffset) throws BadLocationException {
            filterPrefix = "";
            queryAnchorOffset = -1;
            doc.readLock();
            try {
                TokenSequence<TokenId> ppTs = CndLexerUtilities.getCppTokenSequence(doc, caretOffset, true, true);
                if (ppTs == null || ppTs.token() == null) {
                    return false;
                }
                final TokenId id = ppTs.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case WHITESPACE:
                            // use caret offset
                            queryAnchorOffset = caretOffset;
                            break;
                        default:
                            // use start of token
                            queryAnchorOffset = ppTs.offset();
                            break;
                    }
                }
                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
            } catch (BadLocationException ex) {
                // skip
            } finally {
                doc.readUnlock();
            }
            if (TRACE) {
                System.err.println(" anchorOffset=" + queryAnchorOffset + // NOI18N
                        " filterPrefix=" + filterPrefix); // NOI18N
            }
            return this.queryAnchorOffset >= 0;
        }

        private Collection<CsmKeywordCompletionItem> getFilteredData(Collection<CsmKeywordCompletionItem> data, String prefix) {
            Collection<CsmKeywordCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmKeywordCompletionItem> ret = new ArrayList<CsmKeywordCompletionItem>(data.size());
                for (CsmKeywordCompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }

        private boolean matchPrefix(CsmKeywordCompletionItem itm, String prefix) {
            return itm.getItemText().startsWith(prefix);
        }
    }
}
