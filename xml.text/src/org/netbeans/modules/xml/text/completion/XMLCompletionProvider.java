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

package org.netbeans.modules.xml.text.completion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenContext;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLCompletionProvider implements CompletionProvider {
    
    private static final boolean ENABLED = true;
    
    /**
     * Creates a new instance of XMLCompletionProvider
     */
    public XMLCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = ((XMLSyntaxSupport)Utilities.getDocument(component).
                getSyntaxSupport()).checkCompletion(component, typedText, false);
        
        if(type == ExtSyntaxSupport.COMPLETION_POPUP)
            return COMPLETION_QUERY_TYPE;
        else return 0;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        
        return null;
    }
    
    static class Query extends AbstractQuery {
        
        private JTextComponent component;
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            XMLCompletionQuery.XMLCompletionResult res = queryImpl(component, caretOffset);
            if(res != null) {
                List/*<CompletionItem>*/ results = res.getData();
                resultSet.addAllItems(results);
                resultSet.setTitle(res.getTitle());
                resultSet.setAnchorOffset(res.getSubstituteOffset());
            }
        }
    }
    
    private static XMLCompletionQuery.XMLCompletionResult queryImpl(JTextComponent component, int offset) {
        if (!ENABLED) return null;
        
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            ExtEditorUI eeui = (ExtEditorUI)Utilities.getEditorUI(component);
            org.netbeans.editor.ext.Completion compl = ((XMLKit)Utilities.getKit(component)).createCompletionForProvider(eeui);
            XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.getSyntaxSupport(component);
            return (XMLCompletionQuery.XMLCompletionResult)compl.getQuery().query(component, offset, support);
        }
        
        return null;
    }
    
    private static abstract class AbstractQuery extends AsyncCompletionQuery {
        
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            checkHideCompletion((BaseDocument)doc, caretOffset);
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
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
        XMLSyntaxSupport sup = (XMLSyntaxSupport)doc.getSyntaxSupport().get(XMLSyntaxSupport.class);
        try {
            TokenItem ti = sup.getTokenChain(caretOffset <= 0 ? 0 : caretOffset - 1, caretOffset);
            if(ti != null && ti.getTokenID() == XMLDefaultTokenContext.TEXT && !ti.getImage().startsWith("<") && !ti.getImage().startsWith("&")) {
                hideCompletion();
            }
        }catch(BadLocationException e) {
            //do nothing
        }
    }
    
    private static void hideCompletion() {
        Completion.get().hideCompletion();
    }
    
}
