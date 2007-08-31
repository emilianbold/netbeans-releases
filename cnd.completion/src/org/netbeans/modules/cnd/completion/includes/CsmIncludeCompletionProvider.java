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
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmIncludeCompletionProvider implements CompletionProvider  {
    static int index = 0;
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
        if (typedText.equals(CsmIncludeCompletionItem.QUOTE) || 
                typedText.equals(CsmIncludeCompletionItem.SYS_OPEN) || 
                typedText.equals(" ")) {
            int dot = component.getCaret().getDot();
            System.out.println("offset " + dot);
            if (dot == 11) {
                dot=11;
            }
            if (!sup.isIncludeCompletionDisabled(dot)) {
                System.out.println("include completion will be shown");
                return COMPLETION_QUERY_TYPE;
            } else {
                System.out.println("include completion will NOT be shown");
            }
        }
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        System.out.println("queryType = " + queryType);
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            boolean all = (queryType == COMPLETION_ALL_QUERY_TYPE);
            int dot = component.getCaret().getDot();
            CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
            if (!sup.isIncludeCompletionDisabled(dot)) {
                System.out.println("include completion task is created with offset " + dot);
                return new AsyncCompletionTask(new Query(dot, all), component);
            } else {
                System.out.println("include completion task is NOT created");
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
        
        private Collection<CsmIncludeCompletionItem> results;
        
        private int creationCaretOffset;
        private int queryCaretOffset;
        
        private int queryAnchorOffset;
        
        private String filterPrefix;
        private Boolean usrInclude;
        private boolean showAll;
        
        Query(int caretOffset, boolean showAll) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            this.showAll = showAll;
        }
        
        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            System.out.println("preQueryUpdate on " + caretOffset + " anchor " + queryAnchorOffset);
            Document doc = component.getDocument();
            if (creationCaretOffset > 0 && caretOffset >= creationCaretOffset) {
                try {
                    if (isValidIncludeNamePart(doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            System.out.println("hiding completion");
            //Completion.get().hideCompletion();
        }        
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                System.out.println("query on " + caretOffset + " anchor " + queryAnchorOffset);
                initAnchorAndFilter((BaseDocument) doc, caretOffset);
                queryCaretOffset = caretOffset;
                if (this.queryAnchorOffset > 0) {
                    CsmIncludeCompletionQuery query = getCompletionQuery();
                    Collection<CsmIncludeCompletionItem> items = query.query( (BaseDocument)doc, queryAnchorOffset, usrInclude,showAll);
                    if (items != null && items.size() > 0) {
                            this.results = items;
                            items = getFilteredData(items, filterPrefix);
                            resultSet.estimateItems(items.size(), -1);
                            resultSet.addAllItems(items);
                    }
                    resultSet.setHasAdditionalItems(usrInclude != null ? usrInclude : true);
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
                Collection<? extends CsmIncludeCompletionItem> items = getFilteredData(results, filterPrefix);
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
                            switch (tok.getTokenID().getNumericID()) {
                            case CCTokenContext.SYS_INCLUDE_ID:
                            case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                                usrInclude = Boolean.FALSE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset-queryAnchorOffset);
                                break;
                            case CCTokenContext.USR_INCLUDE_ID:
                            case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                                usrInclude = Boolean.TRUE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset-queryAnchorOffset);
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
            if (filterPrefix != null) {
                filterPrefix = trimIncludeSigns(filterPrefix);
            }
            System.out.println("INCINFO: usrInclude " + usrInclude + " anchorOffset = " + queryAnchorOffset + " filterPrefix = " + filterPrefix);
        }

        private String trimIncludeSigns(String str) {
            if (str.startsWith(CsmIncludeCompletionItem.QUOTE) || 
                    str.startsWith(CsmIncludeCompletionItem.SYS_OPEN)) {
                str = str.substring(1);
            }
            if (str.endsWith(CsmIncludeCompletionItem.QUOTE) || 
                    str.endsWith(CsmIncludeCompletionItem.SYS_CLOSE)) {
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }
        
        private boolean matchPrefix(CsmIncludeCompletionItem itm, String prefix) {
            return itm.getItemText().startsWith(prefix);
        }

        private TokenItem toPrevNonWhiteAndNotCommentOnSameLine(TokenItem token) {
            boolean checkedFirst = false;
            TokenItem prev = token;
            while (token != null) {
                if (!checkedFirst) {
                    if (token.getTokenID() != CCTokenContext.WHITESPACE) {
                        return prev;
                    } else {
                        prev = token;
                        token = token.getPrevious();
                        if (token == null) {
                            return prev;
                        }
                    }
                    checkedFirst = true;
                }
                if ((token.getTokenID() == CCTokenContext.WHITESPACE) || 
                    (token.getTokenID() == CCTokenContext.BLOCK_COMMENT)) {
                    if (token.getImage().contains("\n")) {
                        return prev;
                    }
                } else {
                    return token;
                }
                prev = token;
                token = token.getPrevious();
            }
            return prev;
        }
        
        private boolean isValidIncludeNamePart(String text) {
            if (true) return text.length() < 2 || 
                    !text.endsWith(CsmIncludeCompletionItem.QUOTE);
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
        
        private Collection<CsmIncludeCompletionItem> getFilteredData(Collection<CsmIncludeCompletionItem> data, String prefix) {
            Collection<CsmIncludeCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmIncludeCompletionItem> ret = new ArrayList<CsmIncludeCompletionItem>(data.size());
                for (CsmIncludeCompletionItem itm : data) {
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
