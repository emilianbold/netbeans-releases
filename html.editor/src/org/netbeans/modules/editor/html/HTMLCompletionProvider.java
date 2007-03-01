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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.html;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLCompletionQuery;
import org.netbeans.editor.ext.html.HTMLCompletionQuery.HTMLResultItem;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;


/**
 * Implementation of {@link CompletionProvider} for HTML documents.
 *
 * @author Marek Fukala
 */
public class HTMLCompletionProvider implements CompletionProvider {
    
    /** Creates a new instance of JavaDocCompletionProvider */
    public HTMLCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = ((HTMLSyntaxSupport)Utilities.getDocument(component).getSyntaxSupport()).checkCompletion(component, typedText, false);
        return type == ExtSyntaxSupport.COMPLETION_POPUP ? COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE : 0;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        AsyncCompletionTask task = null;
        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            task = new AsyncCompletionTask(new Query(), component);
        } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
            task = new AsyncCompletionTask(new DocQuery(null), component);
        } 
        return task;
    }
    
    static class Query extends AbstractQuery {
        
        private JTextComponent component;
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            HTMLCompletionQuery.HTMLCompletionResult res = (HTMLCompletionQuery.HTMLCompletionResult)queryImpl(component, caretOffset);
            if(res == null) {
                return ;
            }
            
            List/*<CompletionItem>*/ results = res.getData();
            assert (results != null);
            resultSet.addAllItems(results);
            resultSet.setTitle(res.getTitle());
            resultSet.setAnchorOffset(res.getSubstituteOffset());
        }
    }
    
    static class DocQuery extends AbstractQuery {
        
        private JTextComponent component;
        private ResultItem item;
        
        DocQuery(HTMLResultItem item) {
            this.item = item;
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CompletionQuery.Result res = null;
            if(item == null) {
                res = queryImpl(component, caretOffset);
                if(res != null) {
                    List result = res.getData();
                    if(result != null && result.size() > 0) {
                        Object resultObj = result.get(0);
                        if(resultObj instanceof ResultItem)
                            item = (ResultItem)resultObj;
                    }
                }
            }
            HTMLResultItem htmlItem = (HTMLResultItem)item;
            if(htmlItem != null && htmlItem.getHelpID() != null) {
                resultSet.setDocumentation(new HTMLCompletionQuery.DocItem(htmlItem));
                if(res != null) {
                    resultSet.setTitle(res.getTitle());
                    resultSet.setAnchorOffset(((HTMLCompletionQuery.HTMLCompletionResult)res).getSubstituteOffset());
                }
            }
        }
    }
    
    private static CompletionQuery.Result queryImpl(JTextComponent component, int offset) {
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            HTMLSyntaxSupport support = (HTMLSyntaxSupport)Utilities.getSyntaxSupport(component);
            return HTMLCompletionQuery.getDefault().query(component, offset, support);
        } else {
            return null;
        }
    }
    
    private static abstract class AbstractQuery extends AsyncCompletionQuery {
        
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            checkHideCompletion((BaseDocument)doc, caretOffset);
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            checkHideCompletion((BaseDocument)doc, caretOffset);
            doQuery(resultSet, doc, caretOffset);
            resultSet.finish();
        }
        
        abstract void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset);
        
    }
    
    private static void checkHideCompletion(BaseDocument doc, int caretOffset) {
        //test whether we are just in text and eventually close the opened completion
        //this is handy after end tag autocompletion when user doesn't complete the
        //end tag and just types a text
        //test whether the user typed an ending quotation in the attribute value
        doc.readLock();
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
            
            tokenSequence.move(caretOffset == 0 ? 0 : caretOffset - 1);
            if (!tokenSequence.moveNext())
                return;
            
            Token tokenItem = tokenSequence.token();
            if(tokenItem.id() == HTMLTokenId.TEXT && !tokenItem.text().toString().startsWith("<") && !tokenItem.text().toString().startsWith("&")) {
                hideCompletion();
            }
            
        }finally {
            doc.readUnlock();
        }
    }
    
    private static void hideCompletion() {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
    }
}
