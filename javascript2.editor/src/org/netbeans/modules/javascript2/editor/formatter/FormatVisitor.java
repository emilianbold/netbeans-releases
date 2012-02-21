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
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.nashorn.ir.*;
import com.oracle.nashorn.parser.TokenType;
import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public class FormatVisitor extends NodeVisitor {

    private final TokenSequence<? extends JsTokenId> ts;

    private final FormatTokenStream tokenStream;

    private final Set<Block> caseNodes = new HashSet<Block>();

    public FormatVisitor(FormatTokenStream tokenStream, TokenSequence<? extends JsTokenId> ts) {
        this.ts = ts;
        this.tokenStream = tokenStream;
    }

    @Override
    public Node visit(Block block, boolean onset) {
        if (onset && (block instanceof FunctionNode || isScript(block)
                || block.getStart() < block.getFinish())) {

            if (caseNodes.contains(block)) {
                handleCaseBlock(block);
            } else {
                handleStandardBlock(block);
            }
        }

        if (block instanceof FunctionNode || isScript(block)
                || block.getStart() != block.getFinish()) {
            return null;
        } else {
            return super.visit(block, onset);
        }
    }

    @Override
    public Node visit(CaseNode caseNode, boolean onset) {
        // we need to mark if block is case body as block itself has
        // no reference to case node
        if (onset) {
            caseNodes.add(caseNode.getBody());
        } else {
            caseNodes.remove(caseNode.getBody());
        }
        return super.visit(caseNode, onset);
    }

    @Override
    public Node visit(WhileNode whileNode, boolean onset) {
        if (onset) {
            if (handleWhile(whileNode)) {
                return null;
            }
        }

        return super.visit(whileNode, onset);
    }

    @Override
    public Node visit(DoWhileNode doWhileNode, boolean onset) {
        if (onset) {
            if (handleWhile(doWhileNode)) {
                return null;
            }
        }

        return super.visit(doWhileNode, onset);
    }

    @Override
    public Node visit(ForNode forNode, boolean onset) {
        if (onset) {
            if (handleWhile(forNode)) {
                return null;
            }
        }

        return super.visit(forNode, onset);
    }

    @Override
    public Node visit(IfNode ifNode, boolean onset) {
        if (onset) {
            // pass block
            Block body = ifNode.getPass();
            if (body.getStart() == body.getFinish()) {
                handleVirtualBlock(body);
            } else {
                visit(body, onset);
            }

            // fail block
            body = ifNode.getFail();
            if (body != null) {
                if (body.getStart() == body.getFinish()) {
                    handleVirtualBlock(body);
                } else {
                    visit(body, onset);
                }
            }
        }

        return null;
    }

    @Override
    public Node visit(WithNode withNode, boolean onset) {
        if (onset) {
            Block body = withNode.getBody();
            if (body.getStart() == body.getFinish()) {
                handleVirtualBlock(body);
                return null;
            }
        }

        return super.visit(withNode, onset);
    }


    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        visit((Block) functionNode, onset);
        return null;
    }

    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            // indentation mark
            FormatToken formatToken = getPreviousToken(getStart(objectNode), JsTokenId.BRACKET_LEFT_CURLY);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }

            for (Node property : objectNode.getElements()) {
                int finish = getFinish(property);

                formatToken = getPreviousToken(finish, null);
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY));
                }
            }

            // put indentation mark after non white token
            formatToken = getPreviousToken(getFinish(objectNode), JsTokenId.BRACKET_RIGHT_CURLY);
            if (formatToken != null) {
                formatToken = previousNonWhiteToken(getStart(objectNode));
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }

        return super.visit(objectNode, onset);
    }

    @Override
    public Node visit(SwitchNode switchNode, boolean onset) {
        if (onset) {
            FormatToken formatToken = getNextToken(getStart(switchNode), JsTokenId.BRACKET_LEFT_CURLY);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }

            List<CaseNode> nodes = new ArrayList<CaseNode>(switchNode.getCases());
            if (switchNode.getDefaultCase() != null) {
                nodes.add(switchNode.getDefaultCase());
            }

            for (CaseNode caseNode : nodes) {
                int finish = getFinish(caseNode);

                formatToken = getPreviousToken(finish, JsTokenId.OPERATOR_COLON);
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CASE));
                }
            }

            // put indentation mark after non white token
            formatToken = getPreviousToken(getFinish(switchNode), JsTokenId.BRACKET_RIGHT_CURLY);
            if (formatToken != null) {
                formatToken = previousNonWhiteToken(getStart(switchNode));
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }
        return super.visit(switchNode, onset);
    }

    private boolean handleWhile(WhileNode whileNode) {
        Block body = whileNode.getBody();
        if (body.getStart() == body.getFinish()) {
            handleVirtualBlock(body);
            return true;
        }
        return false;
    }

    private void handleStandardBlock(Block block) {
        handleBlockContent(block);

        // indentation mark
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.BRACKET_LEFT_CURLY);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
        }

        // put indentation mark after non white token
        formatToken = getPreviousToken(getFinish(block), JsTokenId.BRACKET_RIGHT_CURLY);
        if (formatToken != null && !isScript(block)) {
            formatToken = previousNonWhiteToken(getStart(block));
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
    }

    private void handleVirtualBlock(Block block) {
        assert block.getStart() == block.getFinish() && block.getStatements().size() <= 1;

        if (block.getStatements().isEmpty()) {
            return;
        }

        handleBlockContent(block);

        Node statement = block.getStatements().get(0);
        
        // indentation mark
        Token token = getPreviousNonEmptyToken(getStart(statement));
        if (token != null) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (formatToken != null && !isScript(block)) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }
        }

        // put indentation mark after non white token
        FormatToken formatToken = getPreviousToken(getFinish(statement), null);
        if (formatToken != null && !isScript(block)) {
            formatToken = previousNonWhiteToken(getStart(block));
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
    }

    private void handleCaseBlock(Block block) {
        handleBlockContent(block);

        // indentation mark
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.OPERATOR_COLON);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
        }

        // put indentation mark after non white token
        Token token = getNextNonEmptyToken(getFinish(block));
        if (token != null) {
            formatToken = previousNonWhiteToken(getStart(block));
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
    }

    private void handleBlockContent(Block block) {
        // functions
        if (block instanceof FunctionNode) {
            for (FunctionNode function : ((FunctionNode) block).getFunctions()) {
                function.accept(this);
            }
        }

        // statements
        for (Node statement : block.getStatements()) {
            int finish = getFinish(statement);
            statement.accept(this);

            FormatToken formatToken = getPreviousToken(finish, null);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
            }
        }
    }

    private FormatToken getNextToken(int offset, JsTokenId expected) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<?extends JsTokenId> token = ts.token();
        if (expected != null) {
            while (expected != token.id() && ts.moveNext()) {
                token = ts.token();
            }
            if (expected != token.id()) {
                return null;
            }
        }
        if (token != null) {
            return tokenStream.getToken(ts.offset());
        }
        return null;
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<?extends JsTokenId> token = ts.token();
        if (expected != null) {
            while (expected != token.id() && ts.movePrevious()) {
                token = ts.token();
            }
            if (expected != token.id()) {
                return null;
            }
        }
        if (token != null) {
            return tokenStream.getToken(ts.offset());
        }
        return null;
    }

    private Token getNextNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.moveNext()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    private Token getPreviousNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.movePrevious()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    private FormatToken previousNonWhiteToken(int backstop) {
        Token ret = null;
        while (ts.movePrevious() && ts.offset() >= backstop) {
            Token current = ts.token();
            if (current.id() != JsTokenId.WHITESPACE) {
                ret = current;
                break;
            }
        }

        if (ret != null) {
            return tokenStream.getToken(ts.offset());
        }
        return null;
    }

    private static int getStart(Node node) {
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to start
        // in case it is string literal.
        int start = node.getStart();
        long firstToken = node.getToken();
        if (com.oracle.nashorn.parser.Token.descType(firstToken).equals(TokenType.STRING)) {
            start--;
        }

        return start;
    }

    private int getFinish(Node node) {
        // we are fixing the wrong finish offset here
        // only function node has last token
        if (node instanceof FunctionNode) {
            FunctionNode function = (FunctionNode) node;
            if (node.getStart() == node.getFinish()) {
                long lastToken = function.getLastToken();
                int finish = node.getStart() + com.oracle.nashorn.parser.Token.descPosition(lastToken)
                        + com.oracle.nashorn.parser.Token.descLength(lastToken);
                // check if it is a string
                if (com.oracle.nashorn.parser.Token.descType(lastToken).equals(TokenType.STRING)) {
                    finish++;
                }
                return finish;
            } else {
                return node.getFinish();
            }
        }

        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to finish
        // in case it is string literal.
        int finish = node.getFinish();
        ts.move(finish);
        if(!ts.moveNext()) {
            return finish;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() == JsTokenId.STRING_END) {
            return finish + 1;
        }

        return finish;
    }

    private boolean isScript(Node node) {
        return (node instanceof FunctionNode)
                && ((FunctionNode) node).getKind() == FunctionNode.Kind.SCRIPT;
    }

    private static void appendTokenAfterLastVirtual(FormatToken previous, FormatToken token) {
        FormatToken current = previous;
        while (current.next() != null && current.next().isVirtual()) {
            current = current.next();
        }
        appendToken(current, token);
    }

    private static void appendToken(FormatToken previous, FormatToken token) {
        FormatToken original = previous.next();
        previous.setNext(token);
        token.setNext(original);
    }
}
