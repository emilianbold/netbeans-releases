/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 41)
public class NodeJsCodeCompletion implements CompletionProvider {

    private static final String REQUIRE = "require";
    private static final String TEMPLATE_REQUIRE = "('${cursor}')";
    

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.EMPTY_LIST;
        }
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();
        if (jsCompletionContext == CompletionContext.STRING || jsCompletionContext == CompletionContext.EXPRESSION
                || jsCompletionContext == CompletionContext.GLOBAL) {
            TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
            if (th == null) {
                return Collections.EMPTY_LIST;
            }
            int carretOffset = ccContext.getCaretOffset();
            int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
            if (ts == null) {
                return Collections.EMPTY_LIST;
            }
            ts.move(eOffset);

            if (!ts.movePrevious()) {
                return Collections.EMPTY_LIST;
            }

            Token<? extends JsTokenId> token = null;
            JsTokenId tokenId;
            if (jsCompletionContext == CompletionContext.STRING || jsCompletionContext == CompletionContext.EXPRESSION) {
                String wholePrefix = ts.token().id() == JsTokenId.STRING ? ts.token().text().toString().trim() : "";
                token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.STRING_BEGIN, JsTokenId.STRING));
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                    tokenId = token.id();
                    if (tokenId == JsTokenId.IDENTIFIER && REQUIRE.equals(token.text().toString())) {
                        // name of modules
                        if (wholePrefix.isEmpty() || (wholePrefix.charAt(0) != '.' && wholePrefix.charAt(0) != '/')) {
                            Collection<String> modules = NodeJsDataProvider.getDefault(fo).getRuntimeModules();
                            for(String module: modules) {
                                if (module.startsWith(prefix)) {
                                    NodeJsElement handle = new NodeJsElement.NodeJsModuleElement(fo, module);
                                    result.add(new NodeJsCompletionItem.NodeJsModuleCompletionItem(handle, eOffset - prefix.length()));
                                }
                            }
                        }
                        int prefixLength = (".".equals(wholePrefix) || "..".equals(wholePrefix) || "../".equals(wholePrefix)) ? wholePrefix.length() : prefix.length();
                        result.addAll((new NodeJsCompletionItem.FilenameSupport()).getItems(ccContext.getParserResult().getSnapshot().getSource().getFileObject(), eOffset - prefixLength, ".." + prefix));
                    }
                } else {
                    if (tokenId == JsTokenId.IDENTIFIER && ts.movePrevious()) {
                        token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                        tokenId = token.id();
                    }
                    if (tokenId == JsTokenId.OPERATOR_ASSIGNMENT) {
                        // offer require()
                        if (prefix.isEmpty() || REQUIRE.startsWith(prefix)) {
                            NodeJsElement handle = new NodeJsElement(fo, REQUIRE, NodeJsDataProvider.getDefault(fo).getDocumentationForGlobalObject(REQUIRE), TEMPLATE_REQUIRE, ElementKind.METHOD);
                            result.add(new NodeJsCompletionItem(handle, eOffset - prefix.length()));
                        }
                    }
                }
            }
        }

        return result.isEmpty() ? Collections.EMPTY_LIST : result;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element instanceof NodeJsElement) {
            return ((NodeJsElement)element).getDocumentation();
        }
        String fqn = null;
        if (element instanceof JsObject) {
            fqn = ((JsObject)element).getFullyQualifiedName();
        }
        if (element instanceof IndexedElement) {
            fqn = ((IndexedElement)element).getFQN();
        }
        FileObject fo = element.getFileObject();
        if (fo != null && fqn != null) {
            if (!fqn.startsWith(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX)) {
                if (fo != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX).append(fo.getName());
                    sb.append('.').append(fqn);
                    fqn = sb.toString();
                    return NodeJsDataProvider.getDefault(fo).getDocumentation(fqn);
                }
            } else {
                return NodeJsDataProvider.getDefault(fo).getDocumentation(fqn);
            }
        }
        return null;
    }

}
