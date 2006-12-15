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
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import static org.netbeans.modules.web.core.syntax.completion.VirtualJavaFromJSPCreator.FakedJavaClass;

/**
 * Code completion functionality for embedded java code (scriptlets, JSP declarations)
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JavaJSPCompletionProvider implements CompletionProvider {
    private final JavaCompletionProvider javaCompletionProvider = new JavaCompletionProvider();
    
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) == 0){
            return null;
        }
        
        //delegate to java cc provider if the context is really java code
        int offset = component.getCaret().getDot();
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(Utilities.getDocument(component));
        TokenSequence tokenSequence = JspSyntaxSupport.tokenSequence(tokenHierarchy, JavaTokenId.language(), offset);
        if(tokenSequence != null) {
            return new AsyncCompletionTask(new EmbeddedJavaCompletionQuery(component, queryType), component);
        } else {
            //not java context =>
            return null;
        }
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return javaCompletionProvider.getAutoQueryTypes(component, typedText);
    }
    
    
    static class EmbeddedJavaCompletionQuery extends AsyncCompletionQuery {
        protected int queryType;
        protected JTextComponent component;
        
        public EmbeddedJavaCompletionQuery(JTextComponent component, int queryType){
            this.queryType = queryType;
            this.component = component;
        }
        
        protected FakedJavaClass getFakedJavaClass(Document doc, int caretOffset){
            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            VirtualJavaFromJSPCreator javaCreator = new VirtualJavaFromJSPCreator(sup, doc);
            
            try{
                javaCreator.process(caretOffset);
                String dummyJavaClass = javaCreator.getDummyJavaClassBody();
                
                FakedJavaClass fakedJavaClass = new FakedJavaClass(dummyJavaClass, javaCreator.getShiftedOffset());
                
                return fakedJavaClass;
            } catch (BadLocationException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            
            return null;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc,
                int caretOffset) {
            FakedJavaClass fakedJavaClass = getFakedJavaClass(doc, caretOffset);
            List<? extends CompletionItem> items = VirtualJavaFromJSPCreator.delegatedQuery(fakedJavaClass,
                    doc, caretOffset, queryType);
            
            resultSet.addAllItems(items);
            resultSet.finish();
        }
        
        
    }
    
}

