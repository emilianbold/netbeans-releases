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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE;
import static org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN;
import static org.netbeans.api.html.lexer.HTMLTokenId.VALUE;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
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

    private static final String WITH_BIND = "with";
    private static final String FOREACH_BIND = "foreach";

    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private final Language JS_LANGUAGE;
    private final LinkedList<StackItem> stack;
    private String lastTagOpen = null;

    private final List<ParentContext> parents = new ArrayList<>();

    private String data;

    private boolean inForEach;

    public KOJsEmbeddingProviderPlugin() {
        JS_LANGUAGE = Language.find(KOUtils.JAVASCRIPT_MIMETYPE); //NOI18N
        this.stack = new LinkedList();
    }

    @Override
    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;

        if(!KOModel.getModel(parserResult).containsKnockout()) {
            return false;
        }
        return true;
    }

    @Override
    public void endProcessing() {
        data = null;
        parents.clear();
        stack.clear();
        lastTagOpen = null;
    }

    @Override
    public boolean processToken() {
        boolean processed = false;
        String tokenText = tokenSequence.token().text().toString();

        switch (tokenSequence.token().id()) {
            case TAG_OPEN:
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
                        endKnockoutSnippet(true);
                    }
                }
                break;
            case VALUE:
                TokenSequence<KODataBindTokenId> embedded = tokenSequence.embedded(KODataBindTokenId.language());
                boolean setData = false;
                if (embedded != null) {
                    embedded.moveStart();
                    Token<KODataBindTokenId> dataValue = null;
                    boolean foreach = false;
                    while (embedded.moveNext()) {
                        if (embedded.token().id() == KODataBindTokenId.KEY) {
                            if (WITH_BIND.equals(embedded.token().text().toString()) // NOI18N
                                    || FOREACH_BIND.equals(embedded.token().text().toString())) { // NOI18N
                                stack.push(new StackItem(lastTagOpen));
                                setData = true;
                                foreach = FOREACH_BIND.equals(embedded.token().text().toString()); // NOI18N
                            }
                        }
                        if (setData && embedded.token().id() == KODataBindTokenId.VALUE && dataValue == null) {
                            dataValue = embedded.token();
                        }
                        if (embedded.embedded(JS_LANGUAGE) != null) {
                            processed = true;

                            startKnockoutSnippet(null, false);

                            boolean putParenthesis =
                                    !embedded.token().text().toString().trim().endsWith(";");

                            if (putParenthesis) {
                                embeddings.add(snapshot.create("(", KOUtils.JAVASCRIPT_MIMETYPE));
                            }
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
                            if (putParenthesis) {
                                embeddings.add(snapshot.create(")", KOUtils.JAVASCRIPT_MIMETYPE));
                            }
                            if (putParenthesis || !embedded.token().text().toString().trim().endsWith(";")) {
                                embeddings.add(snapshot.create(";", KOUtils.JAVASCRIPT_MIMETYPE));
                            }

                            endKnockoutSnippet(false);
                        }
                    }
                    if (setData) {
                        if (dataValue != null) {
                            startKnockoutSnippet(dataValue.text().toString().trim(), foreach);
                        }
                        setData = false;
                    }
                    break;
                }
            default:
                break;
        }
        return processed;
    }

    private void startKnockoutSnippet(String newData, boolean foreach) {
        assert !foreach || newData != null;
        if (newData != null) {
            String replacement = (data == null || data.equals("$root")) ? "ko.$bindings" : data;
            String toAdd = newData.replaceAll("$data", replacement);

            if (foreach) {
                toAdd = toAdd + "[0]";
            }
            if (data == null || "$root".equals(data)) {
                parents.add(new ParentContext("ko.$bindings", false));
            } else {
                parents.add(new ParentContext(data, foreach));
            }
            data = toAdd;
            inForEach = foreach;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("(function(){\n"); // NOI18N

            // for now this is actually just a placeholder
            sb.append("var $element;\n");

            // define root as reference
            sb.append("var $root = ko.$bindings;\n"); // NOI18N

            if (inForEach) {
                sb.append("var $index = 0;\n");
            }

            // define data object
            if (data == null) {
                data = "$root"; // NOI18N
            }

            sb.append("var $parentContext = ");
            generateContext(sb, parents);
            sb.append(";\n");

            sb.append("var $context = ");
            List<ParentContext> current = new ArrayList<>(parents);
            current.add(new ParentContext(data, inForEach));
            generateContext(sb, current);
            sb.append(";\n");
            generateParentAndContextData("$context.", sb, parents);

            generateParents(sb, parents);

            generateWithHierarchyStart(sb, parents);

            String dataValue = data;
            if (data == null || "$root".equals(data)) {
                dataValue = "ko.$bindings";
            }
            // may happen if enclosing with/foreach is empty - user is
            // going to fill it
            if (dataValue.trim().isEmpty()) {
                dataValue = "undefined";
            }
            sb.append("var $data = ").append(dataValue).append(";\n");
            generateWithHierarchyEnd(sb, parents);

            sb.append("with ($data) {\n");

            embeddings.add(snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        }
    }

    private void endKnockoutSnippet(boolean up) {
        if (up) {
            inForEach = false;
            if (parents.isEmpty()) {
                throw new IllegalStateException();
            }
            ParentContext context = parents.remove(parents.size() - 1);
            data = context.getValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("}\n");
            sb.append("});\n");
            embeddings.add(snapshot.create(sb.toString(), KOUtils.JAVASCRIPT_MIMETYPE));
        }
    }

    private static void generateContext(StringBuilder sb, List<ParentContext> parents) {
        if (parents.isEmpty()) {
            sb.append("undefined");
        } else {
            sb.append("{\n");
            sb.append("$parentContext :");
            generateContext(sb, parents.subList(0, parents.size() - 1));
            ParentContext parent = parents.get(parents.size() - 1);
            sb.append(",\n");
            sb.append("$root : ko.$bindings,\n");
                        if (parent.hasIndex()) {
                sb.append("$index : 0,\n");
            }
            sb.append("}");
        }
    }

    private static void generateParentAndContextData(String additionalPrefix,
            StringBuilder sb, List<ParentContext> parents) {

        if (parents.isEmpty()) {
            if (additionalPrefix != null) {
                sb.append(additionalPrefix).append("$parentContext.$data = undefined;\n");
            }
            sb.append("$parentContext.$data = undefined;\n");
            sb.append("var $parent = undefined;\n");
            return;
        }
        StringBuilder prefix = new StringBuilder("$parentContext.");
        for (int i = 0; i < parents.size() - 1; i++) {
            sb.append("with (").append(parents.get(i).getValue()).append(") {\n");
        }
        sb.append("var $parent = ").append(parents.get(parents.size() - 1).getValue()).append(";\n");
        for (int i = parents.size() - 2; i >= 0; i--) {
            if (additionalPrefix != null) {
                sb.append(additionalPrefix).append(prefix).append("$data = ").append(parents.get(i + 1).getValue()).append(";\n");
            }
            sb.append(prefix).append("$data = ").append(parents.get(i + 1).getValue()).append(";\n");
            prefix.append("$parentContext.");
            sb.append("}\n");
        }
        if (additionalPrefix != null) {
            sb.append(additionalPrefix).append(prefix).append("$data = ko.$bindings;\n");
        }
        sb.append(prefix).append("$data = ko.$bindings;\n");
    }

    private static void generateParents(StringBuilder sb, List<ParentContext> parents) {
        sb.append("var $parents = ["); // NOI18N
        int pos = sb.length();
        StringBuilder prefix = new StringBuilder("$parentContext.");
        for (int i = 0; i < parents.size(); i++) {
            sb.insert(pos, ",");
            sb.insert(pos, "$data");
            sb.insert(pos, prefix);
            prefix.append("$parentContext.");
        }
        if (!parents.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("];\n"); // NOI18N
    }

    private static void generateWithHierarchyStart(StringBuilder sb, List<ParentContext> parents) {
        for (ParentContext context : parents) {
            sb.append("with (").append(context.getValue()).append(") {\n");
        }
    }

    private static void generateWithHierarchyEnd(StringBuilder sb, List<ParentContext> parents) {
        for (int i = 0; i < parents.size(); i++) {
            sb.append("}\n");
        }
    }

    private static class StackItem {

        final String tag;
        int balance;

        public StackItem(String tag) {
            this.tag = tag;
            this.balance = 1;
        }
    }

    private static class ParentContext {

        private final String value;

        private final boolean index;

        public ParentContext(String value, boolean index) {
            this.value = value;
            this.index = index;
        }

        public String getValue() {
            return value;
        }

        public boolean hasIndex() {
            return index;
        }
    }
}
