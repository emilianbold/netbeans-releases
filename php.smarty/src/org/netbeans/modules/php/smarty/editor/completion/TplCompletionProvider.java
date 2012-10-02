/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.smarty.editor.completion;

import java.lang.String;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 * Implementation of {@link CompletionProvider} for Tpl documents.
 *
 * @author Martin Fousek
 */
public class TplCompletionProvider implements CompletionProvider {

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
//        Document doc = component.getDocument();
//        int dotPos = component.getCaret().getDot();
//        boolean openCC = checkOpenCompletion(doc, dotPos, typedText);
//        return openCC ? COMPLETION_QUERY_TYPE + DOCUMENTATION_QUERY_TYPE : 0;
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        AsyncCompletionTask task = null;
        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            task = new AsyncCompletionTask(new Query(), component);
        }
        return task;
    }

    private static class Query extends AbstractQuery {

        private volatile Set<TplCompletionItem> items = new HashSet<TplCompletionItem>();
        private JTextComponent component;

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                TplCompletionQuery.CompletionResult result = new TplCompletionQuery(doc).query();
                if (result != null) {
                    items = getItems(result, doc, caretOffset);
                } else {
                    items = Collections.<TplCompletionItem>emptySet();
                }
                resultSet.addAllItems(items);

            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private Set<TplCompletionItem> getItems(TplCompletionQuery.CompletionResult result, Document doc, int offset) {
            Set<TplCompletionItem> entries = new HashSet<TplCompletionItem>();
            ArrayList<String> commands; boolean inSmarty = false;

            if (CodeCompletionUtils.insideSmartyCode(doc, offset)) {
                if (CodeCompletionUtils.inVariableModifiers(doc, offset)) {
                    entries.addAll(result.getVariableModifiers());
                    inSmarty = true;
                }
                commands = CodeCompletionUtils.afterSmartyCommand(doc, offset);
                if (!commands.isEmpty()) {
                    entries.addAll(result.getParamsForCommand(commands));
                    inSmarty = true;
                }
                if (!inSmarty) {
                    if (result != null) {
                        entries.addAll(result.getFunctions());
                    }
                }
            }

            return entries;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            try {
                if (component.getText(component.getCaretPosition() - 1, 1).toString().equals("|")) {
                    return false;
                }
                
            } catch (BadLocationException ex) {
                return false;
            }

            String prefix = CodeCompletionUtils.getTextPrefix(component.getDocument(), component.getCaretPosition());

            //check the items
            for (CompletionItem item : items) {
                if (CodeCompletionUtils.startsWithIgnoreCase(((TplCompletionItem) item).getItemText(), prefix)) {
                    return true; //at least one item will remain
                }
            }

            return false;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            String prefix = CodeCompletionUtils.getTextPrefix(component.getDocument(), component.getCaretPosition());

            //check the items
            for (CompletionItem item : items) {
                if (CodeCompletionUtils.startsWithIgnoreCase(((TplCompletionItem) item).getItemText(), prefix)) {
                    resultSet.addItem(item);
                }
            }
            resultSet.finish();
        }
    }

    public static class DocQuery extends AbstractQuery {

        private CompletionItem item;

        public DocQuery(TplCompletionItem item) {
            this.item = item;
        }

        @Override
        protected void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item == null) {
                try {
                    //item == null means that the DocQuery is invoked
                    //based on the explicit documentation opening request
                    //(not ivoked by selecting a completion item in the list)
                    TplCompletionQuery.CompletionResult result = new TplCompletionQuery(doc).query();
                    if (result != null && result.getFunctions().size() > 0) {
                        item = result.getFunctions().iterator().next();
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            TplCompletionItem tplItem = (TplCompletionItem) item;
            if (tplItem != null && tplItem.getHelp() != null) {
                resultSet.setDocumentation(new DocItem(tplItem));
            }
        }
    }
    private static abstract class AbstractQuery extends AsyncCompletionQuery {

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            checkHideCompletion((BaseDocument) component.getDocument(), component.getCaretPosition());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                doQuery(resultSet, doc, caretOffset);
            } finally {
                resultSet.finish();
            }
        }

        abstract void doQuery(CompletionResultSet resultSet, Document doc, int caretOffset);
    }

    private static void checkHideCompletion(final BaseDocument doc, final int caretOffset) {
        //test whether we are just in text and eventually close the opened completion
        //this is handy after end tag autocompletion when user doesn't complete the
        //end tag and just types a text
        //test whether the user typed an ending quotation in the attribute value
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

                tokenSequence.move(caretOffset == 0 ? 0 : caretOffset - 1);
                if (!tokenSequence.moveNext()) {
                    return;
                }

//                Token tokenItem = tokenSequence.token();
//                if (tokenItem.id() == HTMLTokenId.TEXT && !tokenItem.text().toString().startsWith("<") && !tokenItem.text().toString().startsWith("&")) {
//                    hideCompletion();
//                }
            }
        });
    }

//    static boolean checkOpenCompletion(Document document, final int dotPos, String typedText) {
//        final BaseDocument doc = (BaseDocument) document;
//        switch (typedText.charAt(typedText.length() - 1)) {
//            case '/':
//                if (dotPos >= 2) { // last char before inserted slash
//                    try {
//                        String txtBeforeSpace = doc.getText(dotPos - 2, 2);
//                        if (txtBeforeSpace.equals("</")) // NOI18N
//                        {
//                            return true;
//                        }
//                    } catch (BadLocationException e) {
//                        //no action
//                    }
//                }
//                break;
//            case ' ':
//                doc.readLock();
//                try {
//                    TokenSequence ts = Utils.getJoinedHtmlSequence(doc, dotPos);
//                    if (ts == null) {
//                        //no suitable token sequence found
//                        return false;
//                    }
//
//                    int diff = ts.move(dotPos);
//                    if (ts.moveNext() &&
//                            ts.token().id() == HTMLTokenId.WS && //if current token is whitespace
//                            diff == 1 && //and the caret is just after one char of the token
//                            ts.movePrevious() && //then go back and check if the token before is one of following types
//                            (ts.token().id() == HTMLTokenId.TAG_OPEN ||
//                            ts.token().id() == HTMLTokenId.VALUE ||
//                            ts.token().id() == HTMLTokenId.VALUE_CSS ||
//                            ts.token().id() == HTMLTokenId.VALUE_JAVASCRIPT)) {
//                        return true;
//                    }
//
//                } finally {
//                    doc.readUnlock();
//                }
//                break;
//            case '<':
//            case '&':
//                return true;
//            case '>':
//                //handle tag autocomplete
//                final boolean[] ret = new boolean[1];
//                doc.runAtomic(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        TokenSequence ts = Utils.getJoinedHtmlSequence(doc, dotPos);
//                        if (ts == null) {
//                            //no suitable token sequence found
//                            ret[0] = false;
//                        } else {
//                            ts.move(dotPos - 1);
//                            if (ts.moveNext() || ts.movePrevious()) {
//                                if (ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL && !CharSequenceUtilities.equals("/>", ts.token().text())) {
//                                    ret[0] = true;
//                                }
//                            }
//                        }
//                    }
//                });
//                return ret[0];
//
//        }
//        return false;
//
//    }
//
//    private static void hideCompletion() {
//        Completion.get().hideCompletion();
//        Completion.get().hideDocumentation();
//    }
//
//    private static class LinkDocItem implements CompletionDocumentation {
//
//        private URL url;
//
//        public LinkDocItem(URL url) {
//            this.url = url;
//        }
//
//        @Override
//        public String getText() {
//            return null;
//            /*
//            String anchor = HelpManager.getDefault().getAnchorText(url);
//            if(anchor != null)
//            return HelpManager.getDefault().getHelpText(url, anchor);
//            else
//            return HelpManager.getDefault().getHelpText(url);
//             */
//        }
//
//        @Override
//        public URL getURL() {
//            return url;
//        }
//
//        @Override
//        public CompletionDocumentation resolveLink(String link) {
//            return new LinkDocItem(HelpManager.getDefault().getRelativeURL(url, link));
//        }
//
//        @Override
//        public Action getGotoSourceAction() {
//            return null;
//        }
//    }
//
//    private static class NoDocItem implements CompletionDocumentation {
//
//        @Override
//        public String getText() {
//            return NbBundle.getMessage(TplCompletionProvider.class, "MSG_No_Doc_For_Target"); //NOI18N
//        }
//
//        @Override
//        public URL getURL() {
//            return null;
//        }
//
//        @Override
//        public CompletionDocumentation resolveLink(String link) {
//            return null;
//        }
//
//        @Override
//        public Action getGotoSourceAction() {
//            return null;
//        }
//    }
//
    private static class DocItem implements CompletionDocumentation {

        TplCompletionItem item;

        public DocItem(TplCompletionItem tci) {
            this.item = tci;
        }

        @Override
        public String getText() {
            return item.getHelp();
        }

        @Override
        public URL getURL() {
            return item.getHelpURL();
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }
}
