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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeCompletionProvider implements CompletionProvider  {
    private final static String QUOTE = "\""; // NOI18N
    private final static String SYS_OPEN = "<"; // NOI18N
    private final static String SYS_CLOSE = ">"; // NOI18N
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
        if (typedText.equals(QUOTE) || typedText.equals(SYS_OPEN)) {
            if (!sup.isIncludeCompletionDisabled(component.getCaret().getDot())) {
                System.err.println("include completion will be shown");
                return COMPLETION_QUERY_TYPE;
            }
        }
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE) {
            CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
            if (!sup.isIncludeCompletionDisabled(component.getCaret().getDot())) {
                System.err.println("include completion task is created");
                return new AsyncCompletionTask(new Query(component.getCaret().getDot()), component);
            }
        }
        return null;
    }

    public static final CsmIncludeCompletionQuery getCompletionQuery() {
        return new CsmIncludeCompletionQuery(null);
    }
    
    public static final CsmIncludeCompletionQuery getCompletionQuery(CsmFile csmFile) {
        return new CsmIncludeCompletionQuery(csmFile);
    }
    
    static final class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        private Collection<? extends CompletionItem> results;
        private NbCsmCompletionQuery.CsmCompletionResult queryResult;
        
        private int creationCaretOffset;
        private int queryCaretOffset;
        
        private int queryAnchorOffset;
        
        private String filterPrefix;
        private Boolean usrInclude;
        
        Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }
        
        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            if (queryAnchorOffset > 0 && caretOffset >= queryAnchorOffset) {
                try {
                    if (isValidIncludeNamePart(doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }        
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                initAnchorAndFilter((BaseDocument) doc, caretOffset);
                queryCaretOffset = caretOffset;
                if (this.queryAnchorOffset > 0) {
                    CsmIncludeCompletionQuery query = getCompletionQuery();
                    Collection<? extends CompletionItem> items = query.query(component, (BaseDocument)doc, usrInclude);
                    if (items != null && items.size() > 0) {
                            this.results = items;
                            items = getFilteredData(items, filterPrefix);
                            resultSet.estimateItems(items.size(), -1);
                            resultSet.addAllItems(items);
                    }
                }
            } catch (BadLocationException ex) {
            }
//            if (syntSupp != null){
//                CsmSyntaxSupport sup = (CsmSyntaxSupport)syntSupp.get(CsmSyntaxSupport.class);
//                NbCsmCompletionQuery query = (NbCsmCompletionQuery) getCompletionQuery();
//                NbCsmCompletionQuery.CsmCompletionResult res = (NbCsmCompletionQuery.CsmCompletionResult)query.query(component, caretOffset, sup);
//                if (res != null) {
//                    queryCaretOffset = caretOffset;
//                    queryAnchorOffset = res.getSubstituteOffset();
//                    Collection items = res.getData();
//                    resultSet.estimateItems(items.size(), -1);
//                    // no more title in NB 6 in completion window
//                    //resultSet.setTitle(res.getTitle());
//                    resultSet.setAnchorOffset(queryAnchorOffset);
//                    resultSet.addAllItems(items);
//                    queryResult = res;
//                }
//            }
            resultSet.finish();
        }
        
        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset > -1) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isValidIncludeNamePart(filterPrefix)) {
                            filterPrefix = null;
                        }
                    } catch (BadLocationException e) {
                        // filterPrefix stays null -> no filtering
                    }
                }
            }
            return (filterPrefix != null);
        }        
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
	    resultSet.finish();
        }

        private void initAnchorAndFilter(BaseDocument doc, int caretOffset) throws BadLocationException {
            filterPrefix = null;
            queryAnchorOffset = -1;
            usrInclude = null;
            if (doc != null) {
                try {
                    doc.atomicLock();
                    SyntaxSupport syntSupp = doc.getSyntaxSupport();
                    if (syntSupp != null){
                        CsmSyntaxSupport sup = (CsmSyntaxSupport)syntSupp.get(CsmSyntaxSupport.class);
                        TokenItem tok = sup.getTokenChain(caretOffset, caretOffset+1);
                        tok = toPrevNonWhiteAndNotCommentOnSameLine(tok);
                        if (tok != null) {
                            TokenID id = tok.getTokenID();
                            switch (tok.getTokenID().getNumericID()) {
                            case CCTokenContext.SYS_INCLUDE_ID:
                            case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                                usrInclude = Boolean.TRUE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset);
                                break;
                            case CCTokenContext.USR_INCLUDE_ID:
                            case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                                usrInclude = Boolean.FALSE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset);
                                break;
                            case CCTokenContext.CPPINCLUDE_ID:
                            case CCTokenContext.CPPINCLUDE_NEXT_ID:
                                usrInclude = null;
                                queryAnchorOffset = caretOffset;
                                filterPrefix = null;
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                } finally {
                    doc.atomicUnlock();
                }
            }
        }

        private String trimIncludeSigns(String str) {
            if (str.startsWith(QUOTE) || str.startsWith(SYS_OPEN)) {
                str = str.substring(1);
            }
            if (str.endsWith(QUOTE) || str.endsWith(SYS_CLOSE)) {
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }
        
        private boolean matchPrefix(CompletionItem itm, String prefix) {
            return itm.getInsertPrefix().toString().startsWith(prefix);
        }

        private TokenItem toPrevNonWhiteAndNotCommentOnSameLine(TokenItem token) {
            boolean checkedFirst = false;
            TokenItem prev = token;
            while (token != null) {
                if (!checkedFirst) {
                    if (token.getTokenID() != CCTokenContext.WHITESPACE) {
                        return prev;
                    }
                    checkedFirst = true;
                }
                if ((token.getTokenID() == CCTokenContext.WHITESPACE) || 
                    (token.getTokenID() == CCTokenContext.BLOCK_COMMENT)) {
                    if (token.getImage().contains("\n")) {
                        return prev;
                    }
                } else {
                    return prev;
                }
                prev = token;
                token = token.getPrevious();
            }
            return prev;
        }
        
        private boolean isValidIncludeNamePart(String text) {
            if (true) return text.length() < 2 || !text.endsWith(QUOTE);
//            if (text.startsWith(QUOTE)) {
//                text = text.substring(QUOTE.length());
//            } 
//            if (text.endsWith(QUOTE)) {
//                text = text.substring(text.length() - QUOTE.length());
//            }
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection<? extends CompletionItem> getFilteredData(Collection<? extends CompletionItem> data, String prefix) {
            Collection<? extends CompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CompletionItem> ret = new ArrayList<CompletionItem>(data.size());
                for (CompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }
        
        private String getFilteredTitle(String title, String prefix) {
            int lastIdx = title.lastIndexOf('.');
            String ret = lastIdx == -1 ? prefix : title.substring(0, lastIdx + 1) + prefix;
            if (title.endsWith("*")) // NOI18N
                ret += "*"; // NOI18N
            return ret;
        }
    }    
}
