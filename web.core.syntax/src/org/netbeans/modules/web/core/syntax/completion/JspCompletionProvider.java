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
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;


/**A testing Completion Provider that provides abbreviations as result.
 *
 * @author Jan Lahoda
 */
public class JspCompletionProvider implements CompletionProvider {
    
    /**
     * Enable the AbbreviationsCompletionProvider
     */
    //private static final boolean ENABLED = Boolean.getBoolean("nebeans.editor.completion.abbreviations.enable");
    private static final boolean ENABLED = true;
    
    /** Creates a new instance of JavaDocCompletionProvider */
    public JspCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = ((JspSyntaxSupport)Utilities.getDocument(component).getSyntaxSupport()).checkCompletion(component, typedText, false);
        if(type == ExtSyntaxSupport.COMPLETION_POPUP) return COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE;
        else return 0;
    }
    
    public CompletionTask createTask(int type, JTextComponent component) {
        if (type == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(), component);
        else if (type == DOCUMENTATION_QUERY_TYPE)
            return new AsyncCompletionTask(new DocQuery(null), component);
        return null;
    }
    
    static final class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            List/*<CompletionItem>*/ results = queryImpl(component, caretOffset);
            assert (results != null);
            resultSet.addAllItems(results);
            resultSet.finish();
        }
    }
    
    static class DocQuery extends AsyncCompletionQuery {
        
        private JTextComponent component;
        private ResultItem item;
        
        public  DocQuery(ResultItem item) {
            this.item = item;
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if(item == null) {
                List result = queryImpl(component, caretOffset);
                if(result != null && result.size() > 0) {
                    Object resultObj = result.get(0);
                    if(resultObj instanceof ResultItem)
                        item = (ResultItem)resultObj;
                }
            }
            if(item != null) resultSet.setDocumentation(new DocItem(item));
            resultSet.finish();
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
    
    static List/*<CompletionItem>*/ queryImpl(JTextComponent component, int offset) {
        if (!ENABLED) return Collections.EMPTY_LIST;
        
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            ExtEditorUI eeui = (ExtEditorUI)Utilities.getEditorUI(component);
            Completion compl = JspCompletionSupport.createCompletion(eeui);
            JspSyntaxSupport support = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            
            CompletionQuery.Result res = compl.getQuery().query(component, offset, support);
            if(res  == null) return Collections.EMPTY_LIST;
            else return res.getData();
        }
        
        return Collections.EMPTY_LIST;
    }
    
}
