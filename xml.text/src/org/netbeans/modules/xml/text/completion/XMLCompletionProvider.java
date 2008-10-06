/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.text.completion;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
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
        SyntaxSupport support = Utilities.getDocument(component).getSyntaxSupport();
        if( (support == null) || !(support instanceof XMLSyntaxSupport))
            return 0;
        
        int type = ((XMLSyntaxSupport)support).checkCompletion(component, typedText, false);
        if(type == ExtSyntaxSupport.COMPLETION_POPUP)
            return COMPLETION_QUERY_TYPE;
        
        return 0;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE || queryType == COMPLETION_ALL_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        
        return null;
    }
    
    static class Query extends AsyncCompletionQuery {
        
        private static final XMLCompletionQuery QUERY = new XMLCompletionQuery();
        private JTextComponent component;
        
        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected boolean doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (ENABLED) {
                XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.getSyntaxSupport(component);
                if (support != null) {
                    XMLCompletionQuery.XMLCompletionResult res = 
                        (XMLCompletionQuery.XMLCompletionResult) QUERY.query(component, caretOffset, support);
                    
                    if(res != null) {
                        List/*<CompletionItem>*/ results = res.getData();
                        resultSet.addAllItems(results);
                        resultSet.setTitle(res.getTitle());
                        resultSet.setAnchorOffset(res.getSubstituteOffset());
                        return results.size() == 0;
                    }
                }
            }
            
            return true;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            boolean noResults = doQuery(resultSet, doc, caretOffset);
//            //issue 128275: I'm not sure about this condition here
//            if(doc != null && noResults) {
//                checkHideCompletion((BaseDocument)doc, caretOffset);
//            }
            resultSet.finish();
        }
        
    }
    
// XXX: remove dependency on the old org.netbeans.editor.ext.Completion & co.
//    private static XMLCompletionQuery.XMLCompletionResult queryImpl(JTextComponent component, int offset) {
//        if (!ENABLED) return null;
//        
//        Class kitClass = Utilities.getKitClass(component);
//        if (kitClass != null) {
//            ExtEditorUI eeui = (ExtEditorUI)Utilities.getEditorUI(component);
//            org.netbeans.editor.ext.Completion compl = ((XMLKit)Utilities.getKit(component)).createCompletionForProvider(eeui);
//            XMLSyntaxSupport support = (XMLSyntaxSupport)Utilities.getSyntaxSupport(component);
//            return (XMLCompletionQuery.XMLCompletionResult)compl.getQuery().query(component, offset, support);
//        }
//        
//        return null;
//    }
    
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
