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
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
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
    private final static boolean TRACE = Boolean.getBoolean("cnd.completion.includes.trace");
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
        if (typedText.equals(CsmIncludeCompletionItem.QUOTE) || 
                typedText.equals(CsmIncludeCompletionItem.SYS_OPEN) || 
                typedText.equals(" ") ||
                typedText.equals(CsmIncludeCompletionItem.SLASH)) {
            int dot = component.getCaret().getDot();
            if (TRACE) System.out.println("offset " + dot);
            if (!sup.isIncludeCompletionDisabled(dot)) {
                if (TRACE) System.out.println("include completion will be shown on " + dot);
                return COMPLETION_QUERY_TYPE;
            } else {
                if (TRACE) System.out.println("include completion will NOT be shown on " + dot);
            }
        }
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (TRACE) System.out.println("queryType = " + queryType);
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            boolean all = (queryType == COMPLETION_ALL_QUERY_TYPE);
            int dot = component.getCaret().getDot();
            CsmSyntaxSupport sup = (CsmSyntaxSupport)Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
            if (!sup.isIncludeCompletionDisabled(dot)) {
                if (TRACE) System.out.println("include completion task is created with offset " + dot);
                return new AsyncCompletionTask(new Query(dot, all), component);
            } else {
                if (TRACE) System.out.println("include completion task is NOT created on " + dot);
            }
        }
        return null;
    }
    
    // method for tests
    /*package*/ static Collection<CsmIncludeCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset, true);
        Collection<CsmIncludeCompletionItem> items = query.getItems(doc, caretOffset);
        items = query.getFilteredData(items, query.filterPrefix);
        if (TRACE) {
            System.err.println("Completion Items " + items.size());
            for (CsmIncludeCompletionItem completionItem : items) {
                System.err.println(completionItem.toString());
            }              
        }
        return items;
    }
        
            
    private static final class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        private Collection<CsmIncludeCompletionItem> results;
        
        private int creationCaretOffset;
        private int resultSetAnchorOffset;
        
        private int queryAnchorOffset;
        
        private String dirPrefix;
        private String filterPrefix;
        private Boolean usrInclude;
        private boolean showAll;
        
        /*package*/ Query(int caretOffset, boolean showAll) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            this.resultSetAnchorOffset = creationCaretOffset;
            this.showAll = showAll;
        }
        
        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            if (TRACE) System.out.println("preQueryUpdate on " + caretOffset + " created on " + creationCaretOffset);
            Document doc = component.getDocument();
            if (creationCaretOffset > 0 && caretOffset >= creationCaretOffset) {
                try {
                    if (isValidIncludeNamePart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }        
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (TRACE) System.out.println("query on " + caretOffset + " anchor " + queryAnchorOffset);
            Collection<CsmIncludeCompletionItem> items = getItems((BaseDocument)doc, caretOffset);
            if (this.queryAnchorOffset > 0) {
                if (items != null && items.size() > 0) {
                        this.results = items;
                        items = getFilteredData(items, filterPrefix);
                        resultSet.estimateItems(items.size(), -1);
                        resultSet.addAllItems(items);
                        resultSet.setAnchorOffset(resultSetAnchorOffset);
                }
                resultSet.setHasAdditionalItems(true);
            }
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
            String oldDir = dirPrefix;
            dirPrefix = "";
            if (queryAnchorOffset > -1 && caretOffset > queryAnchorOffset) {
                try {
                    String typedText = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                    if (isValidIncludeNamePart(typedText)) {
                        filterPrefix = typedText;
                    }
                } catch (BadLocationException e) {
                    // filterPrefix stays null -> no filtering
                }
            }
            fixFilter();
            if (TRACE) {
                System.out.println("canFilter INCINFO: usrInclude=" + usrInclude +            
                    " anchorOffset=" + queryAnchorOffset + " oldDir=" + oldDir +
                    " dirPrefix="+dirPrefix + " filterPrefix=" + filterPrefix);
            }
            return (filterPrefix != null) && oldDir.equals(dirPrefix);
        }        
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(resultSetAnchorOffset);
                Collection<? extends CsmIncludeCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(true);
	    resultSet.finish();
        }

        private void fixFilter() {
            if (filterPrefix != null) {
                int origSlash = filterPrefix.lastIndexOf(CsmIncludeCompletionItem.SLASH);
                resultSetAnchorOffset = queryAnchorOffset;
                if (origSlash != -1) {
                    resultSetAnchorOffset += origSlash + 1;
                }
                filterPrefix = trimIncludeSigns(filterPrefix);
                int slash = filterPrefix.lastIndexOf(CsmIncludeCompletionItem.SLASH);
                if (slash != -1) {
                    dirPrefix = filterPrefix.substring(0, slash + 1);
                    filterPrefix = filterPrefix.substring(slash + 1);
                }
            }
        }

        private Collection<CsmIncludeCompletionItem> getItems(BaseDocument doc, int caretOffset) {
            Collection<CsmIncludeCompletionItem> items = Collections.<CsmIncludeCompletionItem>emptyList();
            try {
                if (init(doc, caretOffset)) {
                    CsmIncludeCompletionQuery query = new CsmIncludeCompletionQuery(null);
                    items = query.query(doc, dirPrefix, queryAnchorOffset, usrInclude, showAll);
                }
            } catch (BadLocationException ex) {
            }
            return items;
        }
        
        private boolean init(BaseDocument doc, int caretOffset) throws BadLocationException {
            resultSetAnchorOffset = caretOffset;
            filterPrefix = null;
            queryAnchorOffset = -1;
            usrInclude = null;
            dirPrefix = "";
            if (doc != null) {
                CsmSyntaxSupport sup = (CsmSyntaxSupport) doc.getSyntaxSupport().get(CsmSyntaxSupport.class);
                if (sup != null) {
                    TokenItem tok = sup.getTokenItem(caretOffset);
                    if (tok != null) {
                        doc.atomicLock();
                        try {
                            switch (tok.getTokenID().getNumericID()) {
                                case CCTokenContext.SYS_INCLUDE_ID:
                                case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                                    usrInclude = Boolean.FALSE;
                                    queryAnchorOffset = tok.getOffset();
                                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                    break;
                                case CCTokenContext.USR_INCLUDE_ID:
                                case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                                    usrInclude = Boolean.TRUE;
                                    queryAnchorOffset = tok.getOffset();
                                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                    break;
                            }
                        } catch (BadLocationException ex) {
                        } finally {
                            doc.atomicUnlock();
                        }                        
                    }
                    if (queryAnchorOffset < 0) { // no inside "" or <>
                        tok = sup.shiftToNonWhiteBwd(tok);
                        if (tok != null) {
                            switch (tok.getTokenID().getNumericID()) {
                            case CCTokenContext.CPPINCLUDE_ID:
                            case CCTokenContext.CPPINCLUDE_NEXT_ID:
                                // after #include or #include_next => init query offset
                                usrInclude = null;
                                queryAnchorOffset = caretOffset;
                                filterPrefix = null;
                                break;
                            case CCTokenContext.INCOMPLETE_SYS_INCLUDE_ID:
                                usrInclude = Boolean.FALSE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                break;
                            case CCTokenContext.INCOMPLETE_USR_INCLUDE_ID:
                                usrInclude = Boolean.TRUE;
                                queryAnchorOffset = tok.getOffset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                                break;
                            }
                        }
                    }
                }
            }
            fixFilter();
            if (TRACE) {
                System.out.println("INCINFO: usrInclude=" + usrInclude +
                    " anchorOffset=" + queryAnchorOffset + 
                    " dirPrefix="+dirPrefix + " filterPrefix=" + filterPrefix);
            }
            return this.queryAnchorOffset > 0;
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

        private boolean isValidIncludeNamePart(String text) {
            if (text == null || text.length() == 0) {
                // nothing
                return true;
            }
            if (text.length() == 1) {
               if (text.startsWith(CsmIncludeCompletionItem.QUOTE)) {
                   // opening "
                   return true;
               } else if (text.startsWith(CsmIncludeCompletionItem.SYS_OPEN)) {
                   // opening <, need to recalc data if not system was calculated before
                   return usrInclude == Boolean.FALSE;
               }
            }
            if (text.endsWith(CsmIncludeCompletionItem.QUOTE) ||
                        text.endsWith(CsmIncludeCompletionItem.SYS_CLOSE)) {
                // after include string
                return false;
            } else {            
                return true;
            }
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
        
        private boolean matchPrefix(CsmIncludeCompletionItem itm, String prefix) {
            return itm.getItemText().startsWith(prefix);
        }        
    }    
}
