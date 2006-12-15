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

package org.netbeans.modules.web.core.syntax.completion;

import java.net.URL;
import java.util.List;
import javax.swing.Action;
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
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLCompletionQuery;
import org.netbeans.editor.ext.html.HTMLCompletionQuery.HTMLResultItem;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;


/** JSP completion provider implementation
 *
 * @author Marek.Fukala@Sun.COM
 */
public class JspCompletionProvider implements CompletionProvider {
    
    /** Creates a new instance of JavaDocCompletionProvider */
    public JspCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = ((JspSyntaxSupport)Utilities.getDocument(component).getSyntaxSupport()).checkCompletion(component, typedText, false);
        if(type == ExtSyntaxSupport.COMPLETION_POPUP) {
            return COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE;
        } else return 0;
    }
    
    public CompletionTask createTask(int type, JTextComponent component) {
        if (type == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(component.getCaret().getDot()), component);
        else if (type == DOCUMENTATION_QUERY_TYPE)
            return new AsyncCompletionTask(new DocQuery(null), component);
        return null;
    }
    
    static final class Query extends AbstractQuery {
        
        private JTextComponent component;
        
        private int creationCaretOffset;
        
        Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CompletionQuery.Result res = queryImpl(component, caretOffset);
            if(res != null) {
                List/*<CompletionItem>*/ results = res.getData();
                resultSet.addAllItems(results);
                resultSet.setTitle(res.getTitle());
                resultSet.setAnchorOffset(((JspCompletionQuery.SubstituteOffsetProvider)res).getSubstituteOffset());
            }
        }
    }
    
    static class DocQuery extends AbstractQuery {
        
        private JTextComponent component;
        private ResultItem item;
        
        public  DocQuery(ResultItem item) {
            this.item = item;
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CompletionQuery.Result res = queryImpl(component, caretOffset);
            if(item == null) {
                if(res != null) {
                    List result = res.getData();
                    if(result != null && result.size() > 0) {
                        Object resultObj = result.get(0);
                        if(resultObj instanceof ResultItem) {
                            item = (ResultItem)resultObj;
                        } else if(resultObj instanceof HTMLResultItem) {
                            HTMLResultItem htmlItem = (HTMLResultItem)resultObj;
                            if(htmlItem != null && htmlItem.getHelpID() != null) {
                                resultSet.setDocumentation(new HTMLCompletionQuery.DocItem(htmlItem));
                                resultSet.setTitle(res.getTitle());
                                return ;
                            }
                        }
                    }
                }
            }
            if(item != null &&
                    !(item instanceof JspCompletionItem.ELItem) &&
                    (item.getHelp() != null || item.getHelpURL() != null)) {
                resultSet.setDocumentation(new DocItem(item));
                if(res != null) {
                    resultSet.setTitle(res.getTitle());
                }
            }
        }
    }
    
    static class DocItem implements CompletionDocumentation {
        private ResultItem ri;
        
        public DocItem(ResultItem ri) {
            this.ri = ri;
        }
        
        public String getText() {
            return ri.getHelp();
        }
        
        public URL getURL() {
            return ri.getHelpURL();
        }
        
        public CompletionDocumentation resolveLink(String link) {
            //????
            return null;
        }
        
        public Action getGotoSourceAction() {
            return null;
        }
    }
    
    static CompletionQuery.Result queryImpl(JTextComponent component, int offset) {
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            JspSyntaxSupport support = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            return new JspCompletionQuery(HTMLCompletionQuery.getDefault()).query(component, offset, support);
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
            //weird, but in some situations null is passed from the framework. Seems to be a bug in active component handling
            if(doc != null) {
                checkHideCompletion((BaseDocument)doc, caretOffset);
            }
            doQuery(resultSet, doc, caretOffset);
            resultSet.finish();
        }
        
        abstract void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset);
        
    }
    
    private static void checkHideCompletion(BaseDocument doc, int caretOffset) {
        //test whether we are just in text and eventually close the opened completion
        //this is handy after end tag autocompletion when user doesn't complete the
        //end tag and just types a text
        int adjustedOffset = caretOffset == 0 ? 0 : caretOffset - 1;
        
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = JspSyntaxSupport.tokenSequence(tokenHierarchy, HTMLTokenId.language(), adjustedOffset);
        if(tokenSequence != null) {
            int diff = tokenSequence.move(adjustedOffset);
            if(diff >= tokenSequence.token().length() || diff == Integer.MAX_VALUE) {
                return; //no token found
            }
            
            Token tokenItem = tokenSequence.token();
            if(tokenItem.id() == HTMLTokenId.TEXT && !tokenItem.text().toString().startsWith("<") && !tokenItem.text().toString().startsWith("&")) {
                hideCompletion();
            }
        }
    }
    
    private static void hideCompletion() {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
    }
    
}
