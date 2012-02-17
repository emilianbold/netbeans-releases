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
    public Node visit(AccessNode accessNode, boolean onset) {
        return super.visit(accessNode, onset);
    }

    @Override
    public Node visit(Block block, boolean onset) {
        if (onset && (block instanceof FunctionNode || isScript(block)
                || block.getStart() < block.getFinish())) {

            // indentation mark
            if (caseNodes.contains(block)) {
                handleCaseBlock(block);
            } else {


                Token token = getToken(block.position(), JsTokenId.BRACKET_LEFT_CURLY);
                if (token != null && !isScript(block)) {
                    FormatToken formatToken = tokenStream.getToken(ts.offset());
                    if (formatToken != null) {
                        FormatToken next = formatToken.next();
                        if (next != null && (next.getKind() == FormatToken.Kind.AFTER_CASE)) {
                            formatToken = next;
                        }
                        appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                    }
                }

                if (block instanceof FunctionNode) {
                    for (FunctionNode function : ((FunctionNode) block).getFunctions()) {
                        function.accept(this);
                    }
                }

                for (Node statement : block.getStatements()) {
                    int finish = getFinish(statement);
                    statement.accept(this);

                    token = getToken(finish, null);
                    if (token != null) {
                        FormatToken formatToken = tokenStream.getToken(ts.offset());
                        if (formatToken != null) {
                            appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
                        }
                    }
                }

                // put indentation mark after non white token
                token = getToken(getFinish(block), JsTokenId.BRACKET_RIGHT_CURLY);
                if (token != null && !isScript(block)) {
                    FormatToken formatToken = previousNonWhiteToken(block.getStart());
                    if (formatToken != null) {
                        FormatToken next = formatToken.next();
                        if (next != null && (next.getKind() == FormatToken.Kind.AFTER_STATEMENT)) {
                            formatToken = next;
                        }
                        appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                    }
                }
            }
        }

        if (block instanceof FunctionNode || isScript(block)
                || block.getStart() != block.getFinish()) {
            return null;
        } else {
            return super.visit(block, onset);
        }
    }

    public void handleCaseBlock(Block block) {
        // indentation mark
        Token token = getToken(block.position(), JsTokenId.OPERATOR_COLON);

        if (token != null) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (formatToken != null) {
                FormatToken next = formatToken.next();
                if (next != null && (next.getKind() == FormatToken.Kind.AFTER_CASE)) {
                    formatToken = next;
                }
                appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }
        }

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

            token = getToken(finish, null);
            if (token != null) {
                FormatToken formatToken = tokenStream.getToken(ts.offset());
                if (formatToken != null) {
                    appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
                }
            }
        }

        // put indentation mark after non white token
        token = getNextNonEmptyToken(getFinish(block));
        if (token != null) {
            FormatToken formatToken = previousNonWhiteToken(block.getStart());
            if (formatToken != null) {
                FormatToken next = formatToken.next();
                if (next != null && (next.getKind() == FormatToken.Kind.AFTER_STATEMENT)) {
                    formatToken = next;
                }
                appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
    }

    @Override
    public Node visit(BinaryNode binaryNode, boolean onset) {
        return super.visit(binaryNode, onset);
    }

    @Override
    public Node visit(BreakNode breakNode, boolean onset) {
        return super.visit(breakNode, onset);
    }

    @Override
    public Node visit(CallNode callNode, boolean onset) {
        return super.visit(callNode, onset);
    }

    @Override
    public Node visit(CaseNode caseNode, boolean onset) {
        // we need to mark if block is case body
        if (onset) {
            caseNodes.add(caseNode.getBody());
        } else {
            caseNodes.remove(caseNode.getBody());
        }
        return super.visit(caseNode, onset);
    }

    @Override
    public Node visit(CatchNode catchNode, boolean onset) {
        return super.visit(catchNode, onset);
    }

    @Override
    public Node visit(ContinueNode continueNode, boolean onset) {
        return super.visit(continueNode, onset);
    }

    @Override
    public Node visit(DoWhileNode doWhileNode, boolean onset) {
        return super.visit(doWhileNode, onset);
    }

    @Override
    public Node visit(ExecuteNode executeNode, boolean onset) {
        return super.visit(executeNode, onset);
    }

    @Override
    public Node visit(ForNode forNode, boolean onset) {
        return super.visit(forNode, onset);
    }

    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        visit((Block) functionNode, onset);
        return null;
    }

    @Override
    public Node visit(IdentNode identNode, boolean onset) {
        return super.visit(identNode, onset);
    }

    @Override
    public Node visit(IfNode ifNode, boolean onset) {
        return super.visit(ifNode, onset);
    }

    @Override
    public Node visit(IndexNode indexNode, boolean onset) {
        return super.visit(indexNode, onset);
    }

    @Override
    public Node visit(LabelNode labeledNode, boolean onset) {
        return super.visit(labeledNode, onset);
    }

    @Override
    public Node visit(LineNumberNode lineNumberNode, boolean onset) {
        return super.visit(lineNumberNode, onset);
    }

    @Override
    public Node visit(LiteralNode literalNode, boolean onset) {
        return super.visit(literalNode, onset);
    }

    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            // indentation mark
            Token token = getToken(objectNode.position(), JsTokenId.BRACKET_LEFT_CURLY);
            if (token != null) {
                FormatToken formatToken = tokenStream.getToken(ts.offset());
                if (formatToken != null) {
                    appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                }
            }

            for (Node property : objectNode.getElements()) {
                int finish = getFinish(property);

                token = getToken(finish, null);
                if (token != null) {
                    FormatToken formatToken = tokenStream.getToken(ts.offset());
                    if (formatToken != null) {
                        FormatToken next = formatToken.next();
                        if (next != null && next.getKind() == FormatToken.Kind.AFTER_COMMA) {
                            formatToken = next;
                        }
                        appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY));
                    }
                }
            }

            // put indentation mark after non white token
            token = getToken(getFinish(objectNode), JsTokenId.BRACKET_RIGHT_CURLY);
            if (token != null) {
                FormatToken formatToken = previousNonWhiteToken(objectNode.getStart());
                if (formatToken != null) {
                    FormatToken next = formatToken.next();
                    if (next != null && next.getKind() == FormatToken.Kind.AFTER_PROPERTY) {
                        formatToken = next;
                    }
                    appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }

        return super.visit(objectNode, onset);
    }

    @Override
    public Node visit(PhiNode phiNode, boolean onset) {
        return super.visit(phiNode, onset);
    }

    @Override
    public Node visit(PropertyNode propertyNode, boolean onset) {
        return super.visit(propertyNode, onset);
    }

    @Override
    public Node visit(ReferenceNode referenceNode, boolean onset) {
        return super.visit(referenceNode, onset);
    }

    @Override
    public Node visit(ReturnNode returnNode, boolean onset) {
        return super.visit(returnNode, onset);
    }

    @Override
    public Node visit(RuntimeNode runtimeNode, boolean onset) {
        return super.visit(runtimeNode, onset);
    }

    @Override
    public Node visit(SwitchNode switchNode, boolean onset) {
        if (onset) {
            Token token = getNextToken(switchNode.position(), JsTokenId.BRACKET_LEFT_CURLY);

            if (token != null) {
                FormatToken formatToken = tokenStream.getToken(ts.offset());
                if (formatToken != null) {
                    appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                }
            }

            List<CaseNode> nodes = new ArrayList<CaseNode>(switchNode.getCases());
            if (switchNode.getDefaultCase() != null) {
                nodes.add(switchNode.getDefaultCase());
            }

            for (CaseNode caseNode : nodes) {
                int finish = getFinish(caseNode);

                token = getToken(finish, JsTokenId.OPERATOR_COLON);
                if (token != null) {
                    FormatToken formatToken = tokenStream.getToken(ts.offset());
                    if (formatToken != null) {
                        appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CASE));
                    }
                }
            }

            // put indentation mark after non white token
            token = getToken(getFinish(switchNode), JsTokenId.BRACKET_RIGHT_CURLY);
            if (token != null) {
                FormatToken formatToken = previousNonWhiteToken(switchNode.getStart());
                if (formatToken != null) {
                    FormatToken next = formatToken.next();
                    if (next != null && (next.getKind() == FormatToken.Kind.AFTER_STATEMENT
                            || next.getKind() == FormatToken.Kind.AFTER_CASE)) {
                        formatToken = next;
                    }
                    appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }
        return super.visit(switchNode, onset);
    }

    @Override
    public Node visit(TernaryNode ternaryNode, boolean onset) {
        return super.visit(ternaryNode, onset);
    }

    @Override
    public Node visit(ThrowNode throwNode, boolean onset) {
        return super.visit(throwNode, onset);
    }

    @Override
    public Node visit(TryNode tryNode, boolean onset) {
        return super.visit(tryNode, onset);
    }

    @Override
    public Node visit(UnaryNode unaryNode, boolean onset) {
        return super.visit(unaryNode, onset);
    }

    @Override
    public Node visit(VarNode varNode, boolean onset) {
        return super.visit(varNode, onset);
    }

    @Override
    public Node visit(WhileNode whileNode, boolean onset) {
        return super.visit(whileNode, onset);
    }

    @Override
    public Node visit(WithNode withNode, boolean onset) {
        return super.visit(withNode, onset);
    }

    private Token getToken(int offset, JsTokenId expected) {
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
        return token;
    }

    private Token getNextToken(int offset, JsTokenId expected) {
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
        return token;
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

    private int getFinish(Node node) {
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

    private static void appendToken(FormatToken previous, FormatToken token) {
        FormatToken original = previous.next();
        previous.setNext(token);
        token.setNext(original);
    }
}
