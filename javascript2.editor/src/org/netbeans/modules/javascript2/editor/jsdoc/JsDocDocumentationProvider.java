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
package org.netbeans.modules.javascript2.editor.jsdoc;

import com.oracle.nashorn.ir.Node;
import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DeclarationElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.NamedParameterElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.UnnamedParameterElement;
import org.netbeans.modules.javascript2.editor.lexer.CommonTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.DocParameter;
import org.netbeans.modules.javascript2.editor.model.DocumentationProvider;
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
    // TODO - try to move that directly into JsDocBlock
    @Override
    public List<Type> getReturnType(Node node) {
        JsDocBlock block = getCommentForOffset(node.getStart());
        if (block != null && block.getType() == JsDocCommentType.DOC_COMMON) {
            return getReturnType(block);
        }
        return Collections.<Type>emptyList();
    }

    private List<Type> getReturnType(JsDocBlock block) {
        for (JsDocElement jsDocElement : block.getTagsForTypes(
                new JsDocElement.Type[]{JsDocElement.Type.RETURN, JsDocElement.Type.RETURNS})) {
            return ((UnnamedParameterElement) jsDocElement).getParamTypes();
        }
        for (JsDocElement jsDocElement : block.getTagsForType(JsDocElement.Type.TYPE)) {
            return Arrays.asList(((DeclarationElement) jsDocElement).getDeclaredType());
        }
        return Collections.<Type>emptyList();
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - try to move that directly into JsDocBlock
    @Override
    public List<DocParameter> getParameters(Node node) {
        JsDocBlock block = getCommentForOffset(node.getStart());
        if (block != null && block.getType() == JsDocCommentType.DOC_COMMON) {
            List<DocParameter> params = new LinkedList<DocParameter>();
            for (JsDocElement jsDocElement : block.getTagsForTypes(
                    new JsDocElement.Type[]{JsDocElement.Type.PARAM, JsDocElement.Type.ARGUMENT})) {
                params.add((NamedParameterElement) jsDocElement);
            }
            return params;
        }
        return Collections.<DocParameter>emptyList();
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - try to move that directly into JsDocBlock
    @Override
    public String getDocumentation(Node node) {
        JsDocBlock block = getCommentForOffset(node.getStart());
        if (block != null && block.getType() == JsDocCommentType.DOC_COMMON) {
            return buildDocumentation(block);
        }
        return null;
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - try to move that directly into JsDocBlock
    @Override
    public boolean isDeprecated(Node node) {
        JsDocBlock block = getCommentForOffset(node.getStart());
        if (block != null && block.getType() == JsDocCommentType.DOC_COMMON) {
            for (JsDocElement jsDocElement : block.getTagsForType(JsDocElement.Type.DEPRECATED)) {
                return true;
            }
        }
        return false;
    }

    // TODO - rewrite for getting all associated comments and call getter for all and merge results
    // TODO - try to move that directly into JsDocBlock
    @Override
    public Set<Modifier> getModifiers(Node node) {
        JsDocBlock block = getCommentForOffset(node.getStart());
        if (block != null && block.getType() == JsDocCommentType.DOC_COMMON) {
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            for (JsDocElement jsDocElement : block.getTagsForTypes(new JsDocElement.Type[]{
                    JsDocElement.Type.PRIVATE, JsDocElement.Type.PUBLIC, JsDocElement.Type.STATIC})) {
                modifiers.add(Modifier.fromString(jsDocElement.getType().toString().substring(1)));
            }
            return modifiers;
        }
        return Collections.<Modifier>emptySet();
    }

    protected JsDocBlock getCommentForOffset(int offset) {
        int endOffset = getEndOffsetOfAssociatedComment(offset);
        if (endOffset > 0) {
            return (JsDocBlock) parserResult.getComments().get(endOffset);
        }
        return null;
    }

    private String buildDocumentation(JsDocBlock docBlock) {
        return JsDocPrinter.printDocumentation(docBlock);
    }

    private int getEndOffsetOfAssociatedComment(int offset) {
        TokenHierarchy<?> tokenHierarchy = parserResult.getSnapshot().getTokenHierarchy();
        TokenSequence<? extends CommonTokenId> ts = LexUtilities.getJsTokenSequence(tokenHierarchy, offset);
        if (ts != null) {
            ts.move(offset);

            // get to first EOL
            while (ts.movePrevious() 
                    && ts.token().id() != CommonTokenId.EOL
                    && ts.token().id() != CommonTokenId.OPERATOR_SEMICOLON);

            // search for DOC_COMMENT
            while (ts.movePrevious()) {
                if (ts.token().id() == CommonTokenId.DOC_COMMENT) {
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

    private boolean isWhitespaceToken(Token<? extends CommonTokenId> token) {
        return token.id() == CommonTokenId.EOL || token.id() == CommonTokenId.WHITESPACE
                || token.id() == CommonTokenId.BLOCK_COMMENT || token.id() == CommonTokenId.DOC_COMMENT
                || token.id() == CommonTokenId.LINE_COMMENT;
    }

}
