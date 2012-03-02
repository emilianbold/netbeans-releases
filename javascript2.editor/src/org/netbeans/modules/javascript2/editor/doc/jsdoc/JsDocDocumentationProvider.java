/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.doc.jsdoc;

import com.oracle.nashorn.ir.Node;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.DeclarationElement;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.NamedParameterElement;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.UnnamedParameterElement;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.DocParameter;
import org.netbeans.modules.javascript2.editor.model.DocumentationProvider;
import org.netbeans.modules.javascript2.editor.model.JsComment;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocDocumentationProvider implements DocumentationProvider {

    JsParserResult parserResult;

    public JsDocDocumentationProvider(JsParserResult parserResult) {
        this.parserResult = parserResult;
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - process shared tags and nocode comments
    @Override
    public List<Type> getReturnType(Node node) {
        JsComment comment = getCommentForOffset(node.getStart());
        if (comment != null) {
            JsDocBlock jsDocBlock = (JsDocBlock) comment;
            if (jsDocBlock.getType() == JsDocCommentType.DOC_COMMON) {
                for (JsDocElement jsDocElement : jsDocBlock.getTags()) {
                    if (jsDocElement.getType() == JsDocElement.Type.RETURN
                            || jsDocElement.getType() == JsDocElement.Type.RETURNS) {
                        return ((UnnamedParameterElement) jsDocElement).getParamTypes();
                    } else if (jsDocElement.getType() == JsDocElement.Type.TYPE) {
                        return Arrays.asList(((DeclarationElement) jsDocElement).getDeclaredType());
                    }
                }
            }
        }
        return null;
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - process shared tags and nocode comments
    @Override
    public List<DocParameter> getParameters(Node node) {
        List<DocParameter> params = new LinkedList<DocParameter>();
        JsComment comment = getCommentForOffset(node.getStart());
        if (comment != null) {
            JsDocBlock jsDocBlock = (JsDocBlock) comment;
            if (jsDocBlock.getType() == JsDocCommentType.DOC_COMMON) {
                for (JsDocElement jsDocElement : jsDocBlock.getTags()) {
                    if (jsDocElement.getType() == JsDocElement.Type.PARAM
                            || jsDocElement.getType() == JsDocElement.Type.ARGUMENT) {
                        params.add((NamedParameterElement) jsDocElement);
                    }
                }
            }
        }
        return params;
    }

    protected JsComment getCommentForOffset(int offset) {
        int endOffset = getEndOffsetOfAssociatedComment(offset);
        if (endOffset > 0) {
            return parserResult.getComments().get(endOffset);
        }
        return null;
    }

    private int getEndOffsetOfAssociatedComment(int offset) {
        TokenHierarchy<?> tokenHierarchy = parserResult.getSnapshot().getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(tokenHierarchy, offset);
        if (ts != null) {
            ts.move(offset);

            // get to first EOL
            while (ts.movePrevious() && ts.token().id() != JsTokenId.EOL);

            // search for DOC_COMMENT
            while (ts.movePrevious()) {
                if (ts.token().id() == JsTokenId.DOC_COMMENT) {
                    return ts.token().offset(tokenHierarchy) + ts.token().length();
                } else if (isWhitespaceToken(ts.token())) {
                    continue;
                } else {
                    return -1;
                }
            }
        }

        return -1;
    }

    private boolean isWhitespaceToken(Token<? extends JsTokenId> token) {
        return token.id() == JsTokenId.EOL || token.id() == JsTokenId.WHITESPACE
                || token.id() == JsTokenId.BLOCK_COMMENT || token.id() == JsTokenId.DOC_COMMENT
                || token.id() == JsTokenId.LINE_COMMENT;
    }

}
