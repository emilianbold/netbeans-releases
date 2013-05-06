/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN;
import static org.netbeans.api.html.lexer.HTMLTokenId.VALUE;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.html.knockout.model.KOModel;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Knockout javascript virtual source extension
 *
 * @author mfukala@netbeans.org, phejl@netbeans.org
 */
@MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
public class KOJsEmbeddingProviderPlugin extends JsEmbeddingProviderPlugin {

    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private final Language JS_LANGUAGE;
    private final LinkedList<StackItem> stack;
    private String lastTagOpen = null;
//    private final Map<TokenSequence<HTMLTokenId>, Boolean> initialized;

    public KOJsEmbeddingProviderPlugin() {
        JS_LANGUAGE = Language.find(KOUtils.JAVASCRIPT_MIMETYPE); //NOI18N
        this.stack = new LinkedList();
//        this.initialized = new WeakHashMap<>();
    }

    @Override
//    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
    public boolean startProcessing(Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;

//        KOModel model = KOModel.getModel(parserResult);
//        if(!model.containsKnockout()) {
//            return false;
//        }
        
        embeddings.add(snapshot.create("var $root = ko.$bindings;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
        embeddings.add(snapshot.create("var $data = $root;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
        embeddings.add(snapshot.create("var $parent = undefined;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N

        return true;
    }

    @Override
    public void endProcessing() {
        stack.clear();
//        initialized.clear();
        lastTagOpen = null;
    }

    @Override
    public boolean processToken() {
        boolean processed = false;
        String tokenText = tokenSequence.token().text().toString();

        switch (tokenSequence.token().id()) {
            case TAG_OPEN:
//                Boolean initializedVal = initialized.get(tokenSequence);
//                if (initializedVal == null || !initializedVal) {
//                    embeddings.add(snapshot.create("var $root = ko.$bindings;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
//                    embeddings.add(snapshot.create("var $data = $root;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
//                    embeddings.add(snapshot.create("var $parent = undefined;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
//                    initialized.put(tokenSequence, Boolean.TRUE);
//                    processed = true;
//                }
                lastTagOpen = tokenText;
                StackItem top = stack.peek();
                if (top != null && top.tag.equals(lastTagOpen)) {
                    top.balance++;
                }
                break;
            case TAG_CLOSE:
                top = stack.peek();
                if (top != null && top.tag.equals(tokenText)) {
                    top.balance--;
                    if (top.balance == 0) {
                        processed = true;
                        stack.pop();
                        embeddings.add(snapshot.create("});\n", KOUtils.JAVASCRIPT_MIMETYPE));  //NOI18N
                    }
                }
                break;
            case VALUE:
                TokenSequence<KODataBindTokenId> embedded = tokenSequence.embedded(KODataBindTokenId.language());
                boolean setData = false;
                if (embedded != null) {
                    embedded.moveStart();
                    while (embedded.moveNext()) {
                        if (embedded.token().id() == KODataBindTokenId.KEY) {
                            if ("with".equals(embedded.token().text().toString()) // NOI18N
                                    || "foreach".equals(embedded.token().text().toString())) { // NOI18N
                                embeddings.add(snapshot.create("(function(){\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                                embeddings.add(snapshot.create("var $parent = $data;\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                                stack.push(new StackItem(lastTagOpen));
                                setData = true;
                            }
                        }
                        if (embedded.token().id() == KODataBindTokenId.VALUE) {
                            if (setData) {
                                embeddings.add(snapshot.create("var $data = (", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                                embeddings.add(snapshot.create(embedded.offset(), embedded.token().length(), KOUtils.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(");\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                                setData = false;
                            }
                        }
                        if (embedded.embedded(JS_LANGUAGE) != null) {
                            processed = true;
                            //has javascript embedding
                            embeddings.add(snapshot.create("(function(){\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N

                            CharSequence seq = embedded.token().text();
                            int emptyLength = 0;
                            for (int i = 0; i < seq.length(); i++) {
                                if (Character.isWhitespace(seq.charAt(i))) {
                                    emptyLength++;
                                } else {
                                    break;
                                }
                            }
                            if (emptyLength < seq.length()) {
                                embeddings.add(snapshot.create(embedded.offset() + emptyLength, embedded.token().length() - emptyLength, KOUtils.JAVASCRIPT_MIMETYPE));
                            } else {
                                embeddings.add(snapshot.create(embedded.offset(), embedded.token().length(), KOUtils.JAVASCRIPT_MIMETYPE));
                            }

                            embeddings.add(snapshot.create(";\n});\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                        }
                    }
                    break;
                }
        }
        return processed;
    }

    private static class StackItem {

        final String tag;
        int balance;

        public StackItem(String tag) {
            this.tag = tag;
            this.balance = 1;
        }
    }
}
