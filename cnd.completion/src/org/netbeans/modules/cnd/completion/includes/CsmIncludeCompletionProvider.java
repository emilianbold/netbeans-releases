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
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
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
            System.err.println("include completion task is created");
            return new AsyncCompletionTask(new Query(component.getCaret().getDot()), component);
        }
        return null;
    }

    static final class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        private NbCsmCompletionQuery.CsmCompletionResult queryResult;
        
        private int creationCaretOffset;
        private int queryCaretOffset;
        
        private int queryAnchorOffset;
        
        private String filterPrefix;
        
        Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
        }
        
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            if (caretOffset >= creationCaretOffset) {
                try {
                    if (isJavaIdentifierPart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset)))
                        return;
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }        
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            SyntaxSupport syntSupp = Utilities.getSyntaxSupport(component);
            if (syntSupp != null){
                CsmSyntaxSupport sup = (CsmSyntaxSupport)syntSupp.get(CsmSyntaxSupport.class);
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
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset > -1) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isJavaIdentifierPart(filterPrefix)) {
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
            if (filterPrefix != null && queryResult != null) {
                // no more title in NB 6 in completion window
                //resultSet.setTitle(getFilteredTitle(queryResult.getTitle(), filterPrefix));
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CompletionItem> items = getFilteredData(queryResult.getData(), filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
	    resultSet.finish();
        }

        private boolean isJavaIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection<? extends CompletionItem> getFilteredData(Collection<? extends CompletionItem> data, String prefix) {
            List<CompletionItem> ret = new ArrayList<CompletionItem>();
//            boolean camelCase = prefix.length() > 1 && prefix.equals(prefix.toUpperCase());
            for (CompletionItem itm : data) {
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }

//            for (Iterator it = data.iterator(); it.hasNext();) {
//                CompletionItem itm = it.next();
//                // TODO: filter
//                if (itm.getItemText().startsWith(prefix) /* || prefix.length() == 0*/ ) {
////                if (JMIUtils.startsWith(itm.getItemText(), prefix)
////                        || (camelCase && (itm instanceof NbJMIResultItem.ClassResultItem) && JMIUtils.matchesCamelCase(itm.getItemText(), prefix)))
//                    ret.add(itm);
//                }
//            }
            return ret;
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
