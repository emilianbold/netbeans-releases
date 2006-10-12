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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLCompletionQuery.HTMLResultItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.netbeans.editor.ext.html.*;


/**A testing Completion Provider that provides abbreviations as result.
 *
 * @author Jan Lahoda
 */
public class HTMLCompletionProvider implements CompletionProvider {
    
    private static ErrorManager ERR = ErrorManager.getDefault();
    
    /**Whether only full match of the abbreviation code should be considered for the completion.
     * E.g. if NON_EXACT_MATCH == true, ser| would provide System.err.println("|"); abbreviation,
     * if NON_EXACT_MATCH == false, ser| would not provide the abbreviation, but serr| would.
     */
    //private static final boolean NON_EXACT_MATCH = Boolean.getBoolean("nebeans.editor.completion.abbreviations.nonexactmatch");
    private static final boolean NON_EXACT_MATCH = true;
    
    /**
     * Enable the AbbreviationsCompletionProvider
     */
    //private static final boolean ENABLED = Boolean.getBoolean("nebeans.editor.completion.abbreviations.enable");
    private static final boolean ENABLED = true;
    
    /** Creates a new instance of JavaDocCompletionProvider */
    public HTMLCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = ((HTMLSyntaxSupport)Utilities.getDocument(component).getSyntaxSupport()).checkCompletion(component, typedText, false);
        if(type == ExtSyntaxSupport.COMPLETION_POPUP) return COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE;
        else return 0;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(component.getCaret().getDot()), component);
        else if (queryType == DOCUMENTATION_QUERY_TYPE)
            return new AsyncCompletionTask(new DocQuery(null), component);
        return null;
    }
    
    static class Query extends AbstractQuery {
        
        private JTextComponent component;
        
        private int creationCaretOffset;
        
        Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            HTMLCompletionQuery.HTMLCompletionResult res = (HTMLCompletionQuery.HTMLCompletionResult)queryImpl(component, caretOffset);
            if(res == null) return ;
            
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
        if (!ENABLED) return null;
        
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
        HTMLSyntaxSupport sup = (HTMLSyntaxSupport)doc.getSyntaxSupport().get(HTMLSyntaxSupport.class);
        try {
            TokenItem ti = sup.getTokenChain(caretOffset <= 0 ? 0 : caretOffset - 1, caretOffset);
            if(ti != null && ti.getTokenID() == HTMLTokenContext.TEXT && !ti.getImage().startsWith("<") && !ti.getImage().startsWith("&")) {
                hideCompletion();
            }
        }catch(BadLocationException e) {
            //do nothing
        }
    }
    
    private static void hideCompletion() {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
    }
}
