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

import jdk.nashorn.internal.ir.AccessNode;
import jdk.nashorn.internal.ir.BinaryNode;
import jdk.nashorn.internal.ir.Block;
import jdk.nashorn.internal.ir.CallNode;
import jdk.nashorn.internal.ir.CaseNode;
import jdk.nashorn.internal.ir.CatchNode;
import jdk.nashorn.internal.ir.DoWhileNode;
import jdk.nashorn.internal.ir.ForNode;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.ir.LiteralNode;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.ObjectNode;
import jdk.nashorn.internal.ir.PropertyNode;
import jdk.nashorn.internal.ir.ReferenceNode;
import jdk.nashorn.internal.ir.SwitchNode;
import jdk.nashorn.internal.ir.TernaryNode;
import jdk.nashorn.internal.ir.TryNode;
import jdk.nashorn.internal.ir.UnaryNode;
import jdk.nashorn.internal.ir.VarNode;
import jdk.nashorn.internal.ir.WhileNode;
import jdk.nashorn.internal.ir.WithNode;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;
import jdk.nashorn.internal.parser.TokenType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public class FormatVisitor extends NodeVisitor {

    private static final Set<TokenType> UNARY_TYPES = EnumSet.noneOf(TokenType.class);

    static {
        Collections.addAll(UNARY_TYPES, TokenType.ADD, TokenType.SUB,
                TokenType.BIT_NOT, TokenType.NOT,
                TokenType.INCPOSTFIX, TokenType.INCPREFIX,
                TokenType.DECPOSTFIX, TokenType.DECPREFIX);
    }

    private final TokenSequence<? extends JsTokenId> ts;

    private final FormatTokenStream tokenStream;

    private final int formatFinish;

    private final Set<Block> caseNodes = new HashSet<Block>();

    public FormatVisitor(FormatTokenStream tokenStream, TokenSequence<? extends JsTokenId> ts, int formatFinish) {
        this.ts = ts;
        this.tokenStream = tokenStream;
        this.formatFinish = formatFinish;
    }

    @Override
    public Node enter(Block block) {
        boolean isCaseNode = false;
        if (block instanceof FunctionNode || isScript(block)
                || caseNodes.contains(block) || !isVirtual(block)) {

            if (caseNodes.contains(block)) {
                // if the block is real block it is reused down the ast tree
                // so we need to remove it to be handled normally later
                caseNodes.remove(block);
                isCaseNode = true;
                handleCaseBlock(block);
            } else if (isScript(block)) {
                handleBlockContent(block);
            } else {
                handleStandardBlock(block);
            }
        }

        if (block instanceof FunctionNode || isScript(block)
                || isCaseNode || !isVirtual(block)) {
            return null;
        } else {
            return super.enter(block);
        }
    }

    @Override
    public Node leave(Block block) {
        if (block instanceof FunctionNode || isScript(block)
                || !isVirtual(block)) {
            return null;
        } else {
            return super.leave(block);
        }
    }

    @Override
    public Node enter(CaseNode caseNode) {
        // we need to mark if block is case body as block itself has
        // no reference to case node
        caseNodes.add(caseNode.getBody());
        return super.enter(caseNode);
    }

    @Override
    public Node leave(CaseNode caseNode) {
        // we are removing mark
        caseNodes.remove(caseNode.getBody());
        return super.leave(caseNode);
    }

    @Override
    public Node enter(WhileNode whileNode) {
        // within parens spaces
        markSpacesWithinParentheses(whileNode, getStart(whileNode), getStart(whileNode.getBody()),
                FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(whileNode.getBody(), FormatToken.Kind.BEFORE_WHILE_BRACE);

        if (handleWhile(whileNode, FormatToken.Kind.AFTER_WHILE_START)) {
            return null;
        }

        return super.enter(whileNode);
    }

    @Override
    public Node enter(DoWhileNode doWhileNode) {
        // within parens spaces
        int leftStart;
        Block body = doWhileNode.getBody();
        if (isVirtual(body)) {
            // unfortunately due to condition at the end of do-while
            // we have to care about virtual block
            List<Node> statements = body.getStatements();
            leftStart = getFinish(statements.get(statements.size() - 1));
        } else {
            leftStart = getFinish(doWhileNode.getBody());
        }
        markSpacesWithinParentheses(doWhileNode, leftStart, getFinish(doWhileNode),
                FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(doWhileNode.getBody(), FormatToken.Kind.BEFORE_DO_BRACE);

        FormatToken whileToken = getPreviousToken(getFinish(doWhileNode), JsTokenId.KEYWORD_WHILE);
        if (whileToken != null) {
            FormatToken beforeWhile = whileToken.previous();
            if (beforeWhile != null) {
                appendToken(beforeWhile, FormatToken.forFormat(FormatToken.Kind.BEFORE_WHILE_KEYWORD));
            }
        }
        if (handleWhile(doWhileNode, FormatToken.Kind.AFTER_DO_START)) {
            return null;
        }

        return super.enter(doWhileNode);
    }

    @Override
    public Node enter(ForNode forNode) {
        // within parens spaces
        markSpacesWithinParentheses(forNode, getStart(forNode), getStart(forNode.getBody()),
                FormatToken.Kind.AFTER_FOR_PARENTHESIS, FormatToken.Kind.BEFORE_FOR_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(forNode.getBody(), FormatToken.Kind.BEFORE_FOR_BRACE);

        if (!forNode.isForEach() && !forNode.isForIn()) {
            Node init = forNode.getInit();
            Node test = forNode.getTest();

            FormatToken formatToken = null;

            // unfortunately init and test may be null
            if (init != null) {
                formatToken = getNextToken(getFinish(init), JsTokenId.OPERATOR_SEMICOLON);
            } else {
                formatToken = getNextToken(getStart(forNode), JsTokenId.OPERATOR_SEMICOLON,
                        getStart(forNode.getBody()));
            }
            if (formatToken != null && test != null) {
                appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.BEFORE_FOR_TEST));
            }

            if (test != null) {
                formatToken = getNextToken(getFinish(forNode.getTest()), JsTokenId.OPERATOR_SEMICOLON);
            } else {
                // we use the position of init semicolon
                int start = formatToken != null ? formatToken.getOffset() + 1 : getStart(forNode);
                formatToken = getNextToken(start, JsTokenId.OPERATOR_SEMICOLON,
                                            getStart(forNode.getBody()));
            }
            if (formatToken != null && forNode.getModify() != null) {
                appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.BEFORE_FOR_MODIFY));
            }
        }
        if (handleWhile(forNode, FormatToken.Kind.AFTER_FOR_START)) {
            return null;
        }

        return super.enter(forNode);
    }

    @Override
    public Node enter(IfNode ifNode) {
        ifNode.getTest().accept(this);

        // within parens spaces
        markSpacesWithinParentheses(ifNode, getStart(ifNode), getStart(ifNode.getPass()),
                FormatToken.Kind.AFTER_IF_PARENTHESIS, FormatToken.Kind.BEFORE_IF_PARENTHESIS);

        // pass block
        Block body = ifNode.getPass();
        // mark space before left brace
        markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_IF_BRACE);

        if (isVirtual(body)) {
            handleVirtualBlock(body, FormatToken.Kind.AFTER_IF_START);
        } else {
            enter(body);
        }

        // fail block
        body = ifNode.getFail();
        if (body != null) {
            if (isVirtual(body)) {
                // do the standard block related things
                List<Node> statements = body.getStatements();
                // there might be no statements when code is broken
                if (!statements.isEmpty() && (statements.get(0) instanceof IfNode)) {
                    // we mark else if statement here
                    handleVirtualBlock(body, FormatToken.Kind.ELSE_IF_INDENTATION_INC,
                            FormatToken.Kind.ELSE_IF_INDENTATION_DEC, FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START);
                } else {
                    // mark space before left brace
                    markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_ELSE_BRACE);

                    handleVirtualBlock(body, FormatToken.Kind.AFTER_ELSE_START);
                }
            } else {
                // mark space before left brace
                markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_ELSE_BRACE);

                enter(body);
            }
        }

        return null;
    }

    @Override
    public Node leave(IfNode ifNode) {
        return null;
    }

    @Override
    public Node enter(WithNode withNode) {
        // within parens spaces
        markSpacesWithinParentheses(withNode, getStart(withNode), getStart(withNode.getBody()),
                FormatToken.Kind.AFTER_WITH_PARENTHESIS, FormatToken.Kind.BEFORE_WITH_PARENTHESIS);

        Block body = withNode.getBody();

        // mark space before left brace
        markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_WITH_BRACE);

        if (isVirtual(body)) {
            handleVirtualBlock(body, FormatToken.Kind.AFTER_WITH_START);
            return null;
        }

        return super.enter(withNode);
    }

    @Override
    public Node enter(FunctionNode functionNode) {
        enter((Block) functionNode);

        if (!isScript(functionNode)) {
            int start = getFunctionStart(functionNode);

            FormatToken leftParen = getNextToken(start, JsTokenId.BRACKET_LEFT_PAREN);
            if (leftParen != null) {
                FormatToken previous = leftParen.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_DECLARATION));
                }

                // mark the within parenthesis places

                // remove original paren marks
                FormatToken mark = leftParen.next();
                assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
                tokenStream.removeToken(mark);

                // this works if the offset starts with block as it is now
                FormatToken rightParen = getPreviousToken(getStart(functionNode),
                        JsTokenId.BRACKET_RIGHT_PAREN, leftParen.getOffset());
                if (rightParen != null) {
                    previous = rightParen.previous();
                    assert previous != null && previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous;
                    tokenStream.removeToken(previous);
                }

                // mark left brace of block - this works if function node
                // start offset is offset of the left brace
                FormatToken leftBrace = getNextToken(getStart(functionNode),
                        JsTokenId.BRACKET_LEFT_CURLY, getFinish(functionNode));
                if (leftBrace != null) {
                    previous = leftBrace.previous();
                    if (previous != null) {
                        appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_BRACE));
                    }
                }

                // place the new marks
                if (!functionNode.getParameters().isEmpty()) {
                    appendToken(leftParen, FormatToken.forFormat(
                            FormatToken.Kind.AFTER_FUNCTION_DECLARATION_PARENTHESIS));

                    if (rightParen != null) {
                        previous = rightParen.previous();
                        if (previous != null) {
                            appendToken(previous, FormatToken.forFormat(
                                    FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARENTHESIS));
                        }
                    }
                }

                // place function parameters marks
                for (IdentNode param : functionNode.getParameters()) {
                    FormatToken ident = getNextToken(getStart(param), JsTokenId.IDENTIFIER);
                    if (ident != null) {
                        FormatToken beforeIdent = ident.previous();
                        if (beforeIdent != null) {
                            appendToken(beforeIdent,
                                    FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARAMETER));
                        }
                    }
                }

                if (functionNode.isStatement()) {
                    FormatToken rightBrace = getPreviousToken(getFinish(functionNode),
                            JsTokenId.BRACKET_RIGHT_CURLY,
                            leftBrace != null ? leftBrace.getOffset() : start);
                    if (rightBrace != null) {
                        appendToken(rightBrace, FormatToken.forFormat(
                                FormatToken.Kind.AFTER_STATEMENT));
                    }
                }
            }

        }
        return null;
    }

    @Override
    public Node leave(FunctionNode functionNode) {
        leave((Block) functionNode);

        return null;
    }

    @Override
    public Node enter(CallNode callNode) {
        FormatToken leftBrace = getNextToken(getFinish(callNode.getFunction()),
                JsTokenId.BRACKET_LEFT_PAREN, getFinish(callNode));
        if (leftBrace != null) {
            FormatToken previous = leftBrace.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL));
            }

            // mark the within parenthesis places

            // remove original paren marks
            FormatToken mark = leftBrace.next();
            assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
            tokenStream.removeToken(mark);

            // there is -1 as on the finish position may be some outer paren
            // so we really need the position precisely
            FormatToken rightBrace = getPreviousToken(getFinish(callNode) - 1,
                    JsTokenId.BRACKET_RIGHT_PAREN, getStart(callNode));
            if (rightBrace != null) {
                previous = findVirtualToken(rightBrace,
                        FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS, true);

                // this might happen for sanitization inserted paren
                if (previous != null) {
                    tokenStream.removeToken(previous);
                }
            }

            // place the new marks
            if (!callNode.getArgs().isEmpty()) {
                appendToken(leftBrace, FormatToken.forFormat(
                        FormatToken.Kind.AFTER_FUNCTION_CALL_PARENTHESIS));

                if (rightBrace != null) {
                    previous = rightBrace.previous();
                    if (previous != null) {
                        appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_CALL_PARENTHESIS));
                    }
                }
            }

            // place function arguments marks
            for (Node arg : callNode.getArgs()) {
                FormatToken argToken = getNextToken(getStart(arg), null);
                if (argToken != null) {
                    FormatToken beforeArg = argToken.previous();
                    if (beforeArg != null) {
                        appendToken(beforeArg,
                                FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL_ARGUMENT));
                    }
                }
            }
        }
        handleFunctionCallChain(callNode);

        return super.enter(callNode);
    }

    @Override
    public Node enter(ObjectNode objectNode) {
        // indentation mark
        FormatToken formatToken = getPreviousToken(getStart(objectNode), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_OBJECT_START));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_LEFT_BRACE));
            FormatToken previous = formatToken.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT));
            }
        }

        int objectFinish = getFinish(objectNode);
        for (Node property : objectNode.getElements()) {
            property.accept(this);

            PropertyNode propertyNode = (PropertyNode) property;
            if (propertyNode.getGetter() != null) {
                ReferenceNode getter = (ReferenceNode) propertyNode.getGetter();
                markPropertyFinish(getFinish(getter.getReference()), objectFinish, false);
            }
            if (propertyNode.getSetter() != null) {
                ReferenceNode setter = (ReferenceNode) propertyNode.getSetter();
                markPropertyFinish(getFinish(setter.getReference()), objectFinish, false);
            }

            // mark property end
            markPropertyFinish(getFinish(property), objectFinish, true);
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = getPreviousNonWhiteToken(getFinish(objectNode) - 1,
                getStart(objectNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_RIGHT_BRACE));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT_END));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }

        return null;
    }

    @Override
    public Node enter(PropertyNode propertyNode) {
        FormatToken colon = getNextToken(getFinish(propertyNode.getKey()),
                JsTokenId.OPERATOR_COLON, getFinish(propertyNode));
        if (colon != null) {
            appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY_OPERATOR));
            FormatToken before = colon.previous();
            if (before != null) {
                appendTokenAfterLastVirtual(before, FormatToken.forFormat(FormatToken.Kind.BEFORE_PROPERTY_OPERATOR));
            }
        }
        return super.enter(propertyNode);
    }

    @Override
    public Node enter(SwitchNode switchNode) {
        // within parens spaces
        markSpacesWithinParentheses(switchNode);

        // mark space before left brace
        markSpacesBeforeBrace(switchNode);

        FormatToken formatToken = getNextToken(getStart(switchNode), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
        }

        List<CaseNode> nodes = new ArrayList<CaseNode>(switchNode.getCases());
        if (switchNode.getDefaultCase() != null) {
            nodes.add(switchNode.getDefaultCase());
        }

        for (CaseNode caseNode : nodes) {
            int start = getStart(caseNode.getBody());

            formatToken = getPreviousToken(start, JsTokenId.OPERATOR_COLON);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CASE));
            }
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = getPreviousNonWhiteToken(getFinish(switchNode),
                getStart(switchNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }

        return super.enter(switchNode);
    }

    @Override
    public Node enter(UnaryNode unaryNode) {
        TokenType type = unaryNode.tokenType();
        if (UNARY_TYPES.contains(type)) {
            if (TokenType.DECPOSTFIX.equals(type) || TokenType.INCPOSTFIX.equals(type)) {
                FormatToken formatToken = getPreviousToken(getFinish(unaryNode),
                        TokenType.DECPOSTFIX.equals(type) ? JsTokenId.OPERATOR_DECREMENT : JsTokenId.OPERATOR_INCREMENT);

                if (formatToken != null) {
                    formatToken = formatToken.previous();
                    if (formatToken != null) {
                        appendToken(formatToken,
                                FormatToken.forFormat(FormatToken.Kind.BEFORE_UNARY_OPERATOR));
                    }
                }
            } else {
                FormatToken formatToken = getNextToken(getStart(unaryNode), null);

                // may be null when we are out of formatted area
                if (formatToken != null) {
                    // remove around binary operator tokens added during token
                    // stream creation
                    if (TokenType.ADD.equals(type) || TokenType.SUB.equals(type)) {
                        assert formatToken.getId() == JsTokenId.OPERATOR_PLUS
                                    || formatToken.getId() == JsTokenId.OPERATOR_MINUS : formatToken;
                        // we remove blindly inserted binary op markers
                        FormatToken toRemove = findVirtualToken(formatToken,
                                FormatToken.Kind.BEFORE_BINARY_OPERATOR, true);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);
                        toRemove = findVirtualToken(formatToken,
                                FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP, true);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP : toRemove;
                        tokenStream.removeToken(toRemove);

                        toRemove = findVirtualToken(formatToken,
                                FormatToken.Kind.AFTER_BINARY_OPERATOR, false);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);
                        toRemove = findVirtualToken(formatToken,
                                FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP, false);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP : toRemove;
                        tokenStream.removeToken(toRemove);
                    }

                    appendToken(formatToken,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_UNARY_OPERATOR));
                }
            }
        }

        return super.enter(unaryNode);
    }

    @Override
    public Node enter(TernaryNode ternaryNode) {
        int start = getStart(ternaryNode.rhs());
        FormatToken question = getPreviousToken(start, JsTokenId.OPERATOR_TERNARY);
        if (question != null) {
            FormatToken previous = question.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR_WRAP));
            }
            appendToken(question, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
            appendToken(question, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR_WRAP));
            FormatToken colon = getPreviousToken(getStart(ternaryNode.third()), JsTokenId.OPERATOR_COLON);
            if (colon != null) {
                previous = colon.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                    appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR_WRAP));
                }
                appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
                appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR_WRAP));
            }
        }

        return super.enter(ternaryNode);
    }

    @Override
    public Node enter(CatchNode catchNode) {
        // within parens spaces
        markSpacesWithinParentheses(catchNode, getStart(catchNode), getStart(catchNode.getBody()),
                FormatToken.Kind.AFTER_CATCH_PARENTHESIS, FormatToken.Kind.BEFORE_CATCH_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(catchNode.getBody(), FormatToken.Kind.BEFORE_CATCH_BRACE);

        return super.enter(catchNode);
    }

    @Override
    public Node enter(TryNode tryNode) {
        // mark space before left brace
        markSpacesBeforeBrace(tryNode.getBody(), FormatToken.Kind.BEFORE_TRY_BRACE);

        Block finallyBody = tryNode.getFinallyBody();
        if (finallyBody != null) {
            // mark space before finally left brace
            markSpacesBeforeBrace(tryNode.getFinallyBody(), FormatToken.Kind.BEFORE_FINALLY_BRACE);
        }

        return super.enter(tryNode);
    }

    @Override
    public Node enter(LiteralNode literalNode) {
        Object value = literalNode.getValue();
        if (value instanceof Node[]) {
            int start = getStart(literalNode);
            int finish = getFinish(literalNode);
            FormatToken leftBracket = getNextToken(start, JsTokenId.BRACKET_LEFT_BRACKET, finish);
            if (leftBracket != null) {
                appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_START));
                appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_BRACKET));
                appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                FormatToken rightBracket = getPreviousToken(finish - 1, JsTokenId.BRACKET_RIGHT_BRACKET, start + 1);
                if (rightBracket != null) {
                    FormatToken previous = rightBracket.previous();
                    if (previous != null) {
                        appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_END));
                        appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_BRACKET));
                        appendToken(previous, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                    }
                }
            }

            Node[] items = literalNode.getArray();
            if (items != null && items.length > 0) {
                int prevItemFinish = start;
                for (int i = 1; i < items.length; i++) {
                    Node prevItem = items[i - 1];
                    if (prevItem != null) {
                        prevItemFinish = getFinish(prevItem);
                    }
                    FormatToken comma = getNextToken(prevItemFinish, JsTokenId.OPERATOR_COMMA, finish);
                    if (comma != null) {
                        prevItemFinish = comma.getOffset();
                        appendTokenAfterLastVirtual(comma,
                                FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM));
                    }
                }
            }
        }

        return super.enter(literalNode);
    }

    @Override
    public Node enter(VarNode varNode) {
        int finish = getFinish(varNode) - 1;
        Token nextToken = getNextNonEmptyToken(finish);
        if (nextToken != null && nextToken.id() == JsTokenId.OPERATOR_COMMA) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (formatToken != null) {
                FormatToken next = formatToken.next();
                assert next != null && next.getKind() == FormatToken.Kind.AFTER_COMMA : next;
                appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_VAR_DECLARATION));
            }
        }

        return super.enter(varNode);
    }

    private void handleFunctionCallChain(CallNode callNode) {
        Node function = callNode.getFunction();
        if (function instanceof AccessNode) {
            Node base = ((AccessNode) function).getBase();
            if (base instanceof CallNode) {
                CallNode chained = (CallNode) base;
                int finish = getFinish(chained);
                FormatToken formatToken = getNextToken(finish, JsTokenId.OPERATOR_DOT);
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_CHAIN_CALL_DOT));
                    formatToken = formatToken.previous();
                    if (formatToken != null) {
                        appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_CHAIN_CALL_DOT));
                    }
                }
            }
        }
    }

    private boolean handleWhile(WhileNode whileNode, FormatToken.Kind afterStart) {
        Block body = whileNode.getBody();
        if (isVirtual(body)) {
            handleVirtualBlock(body, afterStart);
            return true;
        }
        return false;
    }

    private void handleStandardBlock(Block block) {
        handleBlockContent(block);

        // indentation mark & block start
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = getPreviousNonWhiteToken(getFinish(block) - 1,
                getStart(block), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }
    }

    private void handleCaseBlock(Block block) {
        handleBlockContent(block);

        // indentation mark & block start
        FormatToken formatToken = getPreviousToken(getStart(block), JsTokenId.OPERATOR_COLON, true);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
        }

        // put indentation mark
        formatToken = getCaseBlockEndToken(block);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind afterBlock) {
        handleVirtualBlock(block, FormatToken.Kind.INDENTATION_INC, FormatToken.Kind.INDENTATION_DEC,
                afterBlock);
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind indentationInc,
            FormatToken.Kind indentationDec, FormatToken.Kind afterBlock) {

        assert isVirtual(block) : block;

        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            if (block.getStatements().size() > 1) {
                int count = 0;
                // there may be multiple var statements due to the comma
                // separated vars translated to multiple statements in ast
                for (Node node : block.getStatements()) {
                    if (!(node instanceof VarNode)) {
                        count++;
                    }
                }
                assert count <= 1;
            }
        }

        if (block.getStatements().isEmpty()) {
            return;
        }

        handleBlockContent(block);

        Node statement = block.getStatements().get(0);

        // indentation mark & block start
        Token token = getPreviousNonEmptyToken(getStart(statement));

        /*
         * If its VarNode it does not contain var keyword so we have to search
         * for it.
         */
        if (statement instanceof VarNode && token.id() == JsTokenId.KEYWORD_VAR) {
            token = getPreviousNonEmptyToken(ts.offset());
        }

        if (token != null) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (!isScript(block)) {
                if (formatToken == null && ts.offset() <= formatFinish) {
                    formatToken = tokenStream.getTokens().get(0);
                }
                if (formatToken != null) {
                    appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationInc));
                    if (afterBlock != null) {
                        appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(afterBlock));
                    }
                }
            }
        }

        // put indentation mark after non white token
        int finish = getFinish(statement);
        // empty statement has start == finish
        FormatToken formatToken = getPreviousToken(
                statement.getStart() < finish ? finish - 1 : finish, null, true);
        if (formatToken != null && !isScript(block)) {
            appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationDec));
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
        List<Node> statements = block.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            Node statement = statements.get(i);
            statement.accept(this);

            int start = getStart(statement);
            int finish = getFinish(statement);

            /*
             * What do we solve here? Unfortunately nashorn parses single
             * var statement as (possibly) multiple VarNodes. For example:
             * var a=1,b=2; is parsed to two VarNodes. The first covering a=1,
             * the second b=2. So we iterate subsequent VarNodes searching the
             * last one and the proper finish token.
             */
            if (statement instanceof VarNode) {
                int index = i + 1;
                Node lastVarNode = statement;

                while (i + 1 < statements.size()) {
                    Node next = statements.get(++i);
                    if (!(next instanceof VarNode)) {
                        i--;
                        break;
                    } else {
                        Token token = getPreviousNonEmptyToken(getStart(next));
                        if (token != null && JsTokenId.KEYWORD_VAR == token.id()) {
                            i--;
                            break;
                        }
                    }
                    lastVarNode = next;
                }

                for (int j = index; j < i + 1; j++) {
                    Node skipped = statements.get(j);
                    skipped.accept(this);
                }

                finish = getFinish(lastVarNode);
            }

            // empty statement has start == finish
            FormatToken formatToken = getPreviousToken(start < finish ? finish - 1 : finish, null);
            if (formatToken != null) {
                appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT), true);
            }
        }
    }

    private void markSpacesWithinParentheses(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            markSpacesWithinParentheses(node, leftStart, token.getOffset(),
                    FormatToken.Kind.AFTER_SWITCH_PARENTHESIS, FormatToken.Kind.BEFORE_SWITCH_PARENTHESIS);
        }
    }

    private void markSpacesBeforeBrace(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            FormatToken previous = token.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_SWITCH_BRACE));
            }
        }
    }

    /**
     * Method putting formatting tokens for within parenthesis rule. Note
     * that this method may be more secure as it can search for the left paren
     * from start of the node and for the right from the body of the node
     * avoiding possibly wrong offset of expressions/conditions.
     *
     * @param outerNode the node we are marking, such as if, while, with
     * @param leftStart from where to start search to the right for the left paren
     * @param rightStart from where to start search to the left for the right paren
     * @param leftMark where to stop searching for the left paren
     * @param rightMark where to stop searching for the right paren
     */
    private void markSpacesWithinParentheses(Node outerNode, int leftStart,
            int rightStart, FormatToken.Kind leftMark, FormatToken.Kind rightMark) {

        FormatToken leftParen = getNextToken(leftStart,
                JsTokenId.BRACKET_LEFT_PAREN, getFinish(outerNode));
        if (leftParen != null) {
            FormatToken mark = leftParen.next();
            assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
            tokenStream.removeToken(mark);

            appendToken(leftParen, FormatToken.forFormat(leftMark));
            FormatToken rightParen = getPreviousToken(rightStart,
                    JsTokenId.BRACKET_RIGHT_PAREN, getStart(outerNode));
            if (rightParen != null) {
                FormatToken previous = rightParen.previous();
                assert previous != null && previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous;
                tokenStream.removeToken(previous);

                previous = rightParen.previous();
                if (previous != null) {
                    appendToken(previous, FormatToken.forFormat(rightMark));
                }
            }
        }
    }

    private void markSpacesBeforeBrace(Block block, FormatToken.Kind mark) {
        FormatToken brace = getPreviousToken(getStart(block), null,
                getStart(block) - 1);
        if (brace != null) {
            FormatToken previous = brace.previous();
            if (previous != null) {
                appendToken(previous, FormatToken.forFormat(mark));
            }
        }
    }

    private void markPropertyFinish(int finish, int objectFinish, boolean checkDuplicity) {
        FormatToken formatToken = getNextToken(finish, JsTokenId.OPERATOR_COMMA, objectFinish);
        if (formatToken != null) {
            appendTokenAfterLastVirtual(formatToken,
                    FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY), checkDuplicity);
        }
    }

    private FormatToken getNextToken(int offset, JsTokenId expected) {
        return getToken(offset, expected, false, false, null);
    }

    private FormatToken getNextToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, false, false, stop);
    }

    private FormatToken getNextToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, false, startFallback, null);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected) {
        return getPreviousToken(offset, expected, false);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, true, false, stop);
    }

    private FormatToken getPreviousToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, true, startFallback, null);
    }

    private FormatToken getToken(int offset, JsTokenId expected, boolean backward,
            boolean startFallback, Integer stopMark) {

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<? extends JsTokenId> token = ts.token();
        if (expected != null) {
            while (expected != token.id()
                    && (stopMark == null || ((stopMark >= ts.offset() && !backward) || (stopMark <=ts.offset() && backward)))
                    && ((backward && ts.movePrevious()) || (!backward && ts.moveNext()))) {
                token = ts.token();
            }
            if (expected != token.id()) {
                return null;
            }
        }
        if (token != null) {
            return getFallback(ts.offset(), startFallback);
        }
        return null;
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

    private FormatToken getPreviousNonWhiteToken(int offset, int stop, JsTokenId expected, boolean startFallback) {
        assert stop <= offset;
        FormatToken ret = getPreviousToken(offset, expected, startFallback);
        if (startFallback && ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START) {
            return ret;
        }

        if (ret != null) {
            if (expected == null) {
                return ret;
            }

            Token token = null;
            while (ts.movePrevious() && ts.offset() >= stop) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    token = current;
                    break;
                }
            }

            if (token != null) {
                return getFallback(ts.offset(), startFallback);
            }
        }
        return null;
    }

    /**
     * Finds the next non empty token first and then move back to non whitespace
     * token.
     *
     * @param block case block
     * @return format token
     */
    private FormatToken getCaseBlockEndToken(Block block) {
        int start = getStart(block);
        int finish = getFinish(block) - 1;
        ts.move(finish);

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

        if (ret != null) {
            while (ts.movePrevious() && ts.offset() >= start) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    ret = current;
                    break;
                }
            }

            if (ret != null) {
                return getFallback(ts.offset(), true);
            }
        }
        return null;
    }

    private FormatToken getFallback(int offset, boolean fallback) {
        FormatToken ret = tokenStream.getToken(offset);
        if (ret == null && fallback && offset < formatFinish) {
            ret = tokenStream.getTokens().get(0);
            assert ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START;
        }
        return ret;
    }

    private int getStart(Node node) {
        // unfortunately in binary node the token represents operator
        // so string fix would not work
        if (node instanceof BinaryNode) {
            return getStart((BinaryNode) node);
        }
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to start
        // in case it is string literal.
        int start = node.getStart();
        long firstToken = node.getToken();
        TokenType type = jdk.nashorn.internal.parser.Token.descType(firstToken);
        if (type.equals(TokenType.STRING) || type.equals(TokenType.ESCSTRING)) {
            ts.move(start - 1);
            if (ts.moveNext()) {
                Token<? extends JsTokenId> token = ts.token();
                if (token.id() == JsTokenId.STRING_BEGIN) {
                    start--;
                }
            }
        }

        return start;
    }

    private int getStart(BinaryNode node) {
        return getStart(node.lhs());
    }

    private static int getFunctionStart(FunctionNode node) {
        return jdk.nashorn.internal.parser.Token.descPosition(node.getFirstToken());
    }

    private int getFinish(Node node) {
        // we are fixing the wrong finish offset here
        // only function node has last token
        if (node instanceof FunctionNode) {
            FunctionNode function = (FunctionNode) node;
            if (node.getStart() == node.getFinish()) {
                long lastToken = function.getLastToken();
                int finish = node.getStart() + jdk.nashorn.internal.parser.Token.descPosition(lastToken)
                        + jdk.nashorn.internal.parser.Token.descLength(lastToken);
                // check if it is a string
                if (jdk.nashorn.internal.parser.Token.descType(lastToken).equals(TokenType.STRING)) {
                    finish++;
                }
                return finish;
            } else {
                return node.getFinish();
            }
        } else if (node instanceof VarNode) {
            Token token = getNextNonEmptyToken(getFinishFixed(node) - 1);
            if (token != null && JsTokenId.OPERATOR_SEMICOLON == token.id()) {
                return ts.offset() + 1;
            } else {
                return getFinishFixed(node);
            }
        }

        return getFinishFixed(node);
    }

    private int getFinishFixed(Node node) {
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to finish
        // in case it is string literal.
        int finish = node.getFinish();
        ts.move(finish);
        if (!ts.moveNext()) {
            return finish;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() == JsTokenId.STRING_END) {
            return finish + 1;
        }

        return finish;
    }

    private static boolean isScript(Node node) {
        return (node instanceof FunctionNode)
                && ((FunctionNode) node).getKind() == FunctionNode.Kind.SCRIPT;
    }

    private boolean isVirtual(Block block) {
        return block.getStart() == block.getFinish()
                    || jdk.nashorn.internal.parser.Token.descType(block.getToken()) != TokenType.LBRACE
                    || block.isCatchBlock();
    }

    @CheckForNull
    private static FormatToken findVirtualToken(FormatToken token, FormatToken.Kind kind,
            boolean backwards) {
        FormatToken result = backwards ? token.previous() : token.next();
        while (result != null && result.isVirtual()
                && result.getKind() != kind) {
            result = backwards ? result.previous() : result.next();;
        }
        if (result != null && result.getKind() != kind) {
            return null;
        }
        return result;
    }

    private static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token) {
        appendTokenAfterLastVirtual(previous, token, false);
    }

    private static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token, boolean checkDuplicity) {

        assert previous != null;

        @NonNull
        FormatToken current = previous;
        FormatToken next = current.next();

        while (next != null && next.isVirtual()) {
            current = next;
            next = next.next();
        }
        if (!checkDuplicity || !current.isVirtual() || !token.isVirtual()
                || current.getKind() != token.getKind()) {
            appendToken(current, token);
        }
    }

    private static void appendToken(FormatToken previous, FormatToken token) {
        FormatToken original = previous.next();
        previous.setNext(token);
        token.setPrevious(previous);
        token.setNext(original);
        if (original != null) {
            original.setPrevious(token);
        }
    }

}
