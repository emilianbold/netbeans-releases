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
package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import java.net.URL;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
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

    public JspCompletionProvider() {
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int type = JspSyntaxSupport.get(component.getDocument()).checkCompletion(component, typedText, false);
        if (type == ExtSyntaxSupport.COMPLETION_POPUP) {
            return COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE;
        } else {
            return 0;
        }
    }

    public CompletionTask createTask(int type, JTextComponent component) {
        if ((type & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new Query(), component);
        } else if (type == DOCUMENTATION_QUERY_TYPE) {
            return new AsyncCompletionTask(new DocQuery(null), component);
        }
        return null;
    }

    public static class Query extends AbstractQuery {

        private JTextComponent component;

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        protected void doQuery(CompletionResultSet resultSet, Document doc,
                int caretOffset) {
            JspCompletionQuery.CompletionResultSet<? extends CompletionItem> jspResultSet = new JspCompletionQuery.CompletionResultSet<CompletionItem>();
            JspCompletionQuery.instance().query(jspResultSet, component, caretOffset);

            resultSet.addAllItems(jspResultSet.getItems());
            resultSet.setAnchorOffset(jspResultSet.getAnchor());
        }
    }

    public static class DocQuery extends AbstractQuery {

        private JTextComponent component;
        private JspCompletionItem item;

        public DocQuery(JspCompletionItem item) {
            this.item = item;
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item != null) {
                //this instance created from jsp completion item
                if (item.hasHelp()) {
                    resultSet.setDocumentation(new DocItem(item));
                }
            } else {
                //called directly by infrastructure - run query
                JspCompletionQuery.CompletionResultSet<? extends CompletionItem> jspResultSet = new JspCompletionQuery.CompletionResultSet<CompletionItem>();
                JspCompletionQuery.instance().query(jspResultSet, component, caretOffset);
                if (jspResultSet.getItems().size() > 0) {
                    resultSet.setDocumentation(new DocItem((JspCompletionItem) jspResultSet.getItems().get(0)));
                    resultSet.setAnchorOffset(jspResultSet.getAnchor());
                }

            }
        }
    }

    private static class DocItem implements CompletionDocumentation {

        private JspCompletionItem ri;

        public DocItem(JspCompletionItem ri) {
            this.ri = ri;
        }

        public String getText() {
            return ri.getHelp();
        }

        public URL getURL() {
            return ri.getHelpURL();
        }

        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        public Action getGotoSourceAction() {
            return null;
        }
    }

    public static abstract class AbstractQuery extends AsyncCompletionQuery {

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            checkHideCompletion((BaseDocument) doc, caretOffset);
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            //weird, but in some situations null is passed from the framework. Seems to be a bug in active component handling
            if (doc != null) {
                checkHideCompletion((BaseDocument) doc, caretOffset);
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
        doc.readLock();
        try {
            TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
            TokenSequence<?> tokenSequence = JspSyntaxSupport.tokenSequence(
                    tokenHierarchy, HTMLTokenId.language(), adjustedOffset);
            if (tokenSequence != null) {
                tokenSequence.move(adjustedOffset);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                    return; //no token found
                }

                Token<?> tokenItem = tokenSequence.token();
                if (tokenSequence.embedded() == null &&
                        tokenItem.id() == HTMLTokenId.TEXT &&
                        !tokenItem.text().toString().startsWith("<") &&
                        !tokenItem.text().toString().startsWith("&")) {
                    hideCompletion();
                }
            }
        } finally {
            doc.readUnlock();
        }
    }

    private static void hideCompletion() {
        Completion.get().hideCompletion();
        Completion.get().hideDocumentation();
    }
}
