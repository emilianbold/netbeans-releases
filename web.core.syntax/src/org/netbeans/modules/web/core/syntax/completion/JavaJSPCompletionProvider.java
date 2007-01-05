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
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.web.core.syntax.SimplifiedJSPServlet;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

/**
 * Code completion functionality for Java code embedded in JSP files:
 * - scriptlets (<% ... %>)
 * - JSP declarations (<%! ... %> )
 * - expressions (<%= ... %>)
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JavaJSPCompletionProvider implements CompletionProvider {
    private final JavaCompletionProvider javaCompletionProvider = new JavaCompletionProvider();
    private static final Logger logger = Logger.getLogger(JavaJSPCompletionProvider.class.getName());
    
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0){
            Document doc = Utilities.getDocument(component);
            int caretOffset = component.getCaret().getDot();
            
            if (isWithinScriptlet(doc, caretOffset)){
                //delegate to java cc provider if the context is really java code
                return new AsyncCompletionTask(new EmbeddedJavaCompletionQuery(component, queryType), component);
            }
        }
        
        return null;
    }
    
    private boolean isWithinScriptlet(Document doc, int offset){
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        
        if(tokenSequence.move(offset) != Integer.MAX_VALUE) {
            Object tokenID = tokenSequence.token().id();
            if (tokenID == JspTokenId.SCRIPTLET){
                return true;
            } else if (tokenID == JspTokenId.SYMBOL2) {
                // maybe the caret is placed just before the ending script delimiter?
                tokenSequence.movePrevious();
                
                if (tokenSequence.token().id() == JspTokenId.SCRIPTLET){
                    return true;
                }
            }
        }
        
        return false;
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
        
        protected void query(CompletionResultSet resultSet, Document doc,
                int caretOffset) {
            
            SimplifiedJSPServlet simplifiedJSPServlet = new SimplifiedJSPServlet(doc);
            try{
                simplifiedJSPServlet.process();
                String fakedClassBody = simplifiedJSPServlet.getVirtualClassBody();
                int shiftedOffset = simplifiedJSPServlet.getShiftedOffset(caretOffset);
                
                if (shiftedOffset >= 0){
                    logger.fine("JSP CC: delegating CC query to java file:\n" //NOI18N
                            + fakedClassBody.substring(0, shiftedOffset)
                            + "|" + fakedClassBody.substring(shiftedOffset)); //NOI18N
                    
                    CompletionQueryDelegatedToJava delegate = new CompletionQueryDelegatedToJava(
                            caretOffset, shiftedOffset, queryType);
                    
                    delegate.create(doc, fakedClassBody);
                    List<? extends CompletionItem> items =  delegate.getCompletionItems();
                    resultSet.addAllItems(items);
                } else{
                    logger.severe("caretOffset outside of embedded java code"); //NOI18N
                }
            } catch (BadLocationException e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            } finally{
                resultSet.finish();
            }
        }
    }
    
    static class CompletionQueryDelegatedToJava extends SimplifiedJSPServlet.VirtualJavaClass{
        private int caretOffset;
        private int queryType;
        private int shiftedOffset;
        private List<? extends CompletionItem> completionItems;
        
        CompletionQueryDelegatedToJava(int caretOffset, int shiftedOffset, int queryType){
            this.caretOffset = caretOffset;
            this.shiftedOffset = shiftedOffset;
            this.queryType = queryType;
        }
        
        protected void process(FileObject fileObject, JavaSource javaSource){
            try{
                completionItems = JavaCompletionProvider.query(
                        javaSource, queryType, shiftedOffset, caretOffset);
            } catch (IOException e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        List<? extends CompletionItem> getCompletionItems(){
            return completionItems;
        }
    }
    
}

