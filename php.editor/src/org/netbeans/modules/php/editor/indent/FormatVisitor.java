/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */


package org.netbeans.modules.php.editor.indent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.editor.indent.TokenFormatter.DocumentOptions;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class FormatVisitor extends DefaultVisitor {

    private BaseDocument document;
    private final List<FormatToken> formatTokens;
    TokenSequence<PHPTokenId> ts;
    private LinkedList<ASTNode> path;
    private int indentLevel;
    private DocumentOptions options;
    private boolean includeWSBeforePHPDoc;
    private boolean isCurly; // whether the last visited block is curly or standard syntax.

    public FormatVisitor(BaseDocument document) {
	this.document = document;
	ts = LexUtilities.getPHPTokenSequence(document, 0);
	path = new LinkedList<ASTNode>();
	indentLevel = 0;
	options = new DocumentOptions(document);
	includeWSBeforePHPDoc = true;
	formatTokens = new ArrayList<FormatToken>();
	formatTokens.add(new FormatToken.InitToken());
    }

    public List<FormatToken> getFormatTokens() {
	return formatTokens;
    }

    @Override
    public void scan(ASTNode node) {
	if (node == null) {
	    return;
	}
	int indent = path.size();


	// find comment before the node.
	List<FormatToken> beforeTokens = new ArrayList<FormatToken>(30);
	int indexBeforeLastComment = -1;  // remember last comment
	while (ts.moveNext() && ts.offset() < node.getStartOffset()) {
	    addFormatToken(beforeTokens);
	    if (ts.token().id() == PHPTokenId.PHPDOC_COMMENT_START
		    || (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
		    && "//".equals(ts.token().text().toString())
		    && indexBeforeLastComment == -1)) {
		indexBeforeLastComment = beforeTokens.size() - 1;
	    }
	}
	if (indexBeforeLastComment > 0) { // if there is a comment, put the new lines befere the comment, not directly before the node.
	    for (int i = 0; i < indexBeforeLastComment; i++) {
		formatTokens.add(beforeTokens.get(i));
	    }
	    includeWSBeforePHPDoc = true;
	    if (node instanceof ClassDeclaration || node instanceof InterfaceDeclaration) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
		includeWSBeforePHPDoc = false;
	    } else if (node instanceof FunctionDeclaration || node instanceof MethodDeclaration) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
		includeWSBeforePHPDoc = false;
	    } else if (node instanceof FieldsDeclaration) {
		if (isPreviousNodeTheSameInBlock((Block) path.get(0), (Statement) node)) {
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_FIELDS, ts.offset()));
		} else {
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELD, ts.offset()));
		}
		includeWSBeforePHPDoc = false;
	    } else if (node instanceof UseStatement) {
		if (isPreviousNodeTheSameInBlock((Block) path.get(0), (Statement) node)) {
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_USE, ts.offset()));
		} else {
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE, ts.offset()));
		}
		includeWSBeforePHPDoc = false;
	    }
	    for (int i = indexBeforeLastComment; i < beforeTokens.size(); i++) {
		formatTokens.add(beforeTokens.get(i));
	    }
	} else {
	    formatTokens.addAll(beforeTokens);
	}

//	    addFormatToken(formatTokens);

	ts.movePrevious();

	path.addFirst(node);
	super.scan(node);
	path.removeFirst();

//	if (ts.offset() <= node.getEndOffset()) {
	while (ts.moveNext() && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
	    //xxSystem.out.println(indentS + ts.token().id() + "<" + ts.offset() + ", " + (ts.offset() + ts.token().length()) + ">, " + "After " + node.getClass().getSimpleName() + " [" + node.getStartOffset() + ", " + node.getEndOffset() + "]");
	    addFormatToken(formatTokens);
	}
	ts.movePrevious();
//	}
    }

    @Override
    public void scan(Iterable<? extends ASTNode> nodes) {
	super.scan(nodes);
    }

    @Override
    public void visit(ArrayCreation node) {
	int delta = options.indentArrayItems - options.continualIndentSize;
	while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_ARRAY) {
	    addFormatToken(formatTokens);
	}
	if (formatTokens.get(formatTokens.size() - 1).getId() == FormatToken.Kind.WHITESPACE_INDENT
		|| path.get(1) instanceof ArrayElement
		|| path.get(1) instanceof FormalParameter) {
	    // when the array is on the beginning of the line, indent items in normal way
	    delta = options.indentArrayItems;
	}
	addFormatToken(formatTokens); // add array keyword
	formatTokens.add(new FormatToken.IndentToken(ts.offset(), delta));
	super.visit(node);
	formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), -1 * delta));
	addAllUntilOffset(node.getEndOffset());
    }

    @Override
    public void visit(Assignment node) {
	scan(node.getLeftHandSide());
	while (ts.moveNext() && ts.offset() < node.getRightHandSide().getStartOffset()
		&& ts.token().id() != PHPTokenId.PHP_TOKEN) {
	    addFormatToken(formatTokens);
	}
	if (ts.token().id() == PHPTokenId.PHP_TOKEN) {
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_ASSIGN_OP, ts.offset()));
	    addFormatToken(formatTokens);
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_ASSIGN_OP, ts.offset() + ts.token().length()));
	} else {
	    ts.movePrevious();
	}
	scan(node.getRightHandSide());
    }

    @Override
    public void visit(Block node) {
	if (path.size() > 1 && path.get(1) instanceof NamespaceDeclaration) {
	    // dont process blok for namespace
	    super.visit(node);
	    return;
	}
	
	isCurly = node.isCurly();
	while (node.isCurly() && ts.moveNext() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) {
	    addFormatToken(formatTokens);
	}

	ASTNode parent = path.get(1);

	if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
	    if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS_LEFT_BRACE, ts.offset()));
	    } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION_LEFT_BRACE, ts.offset()));
	    } else if (parent instanceof IfStatement) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_LEFT_BRACE, ts.offset()));
	    } else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_LEFT_BRACE, ts.offset()));
	    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_LEFT_BRACE, ts.offset()));
	    } else if (parent instanceof SwitchStatement) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_LEFT_BACE, ts.offset()));
	    } else if (parent instanceof CatchClause || parent instanceof TryStatement) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_LEFT_BRACE, ts.offset()));
	    } else {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OTHER_LEFT_BRACE, ts.offset()));
	    }
	    addFormatToken(formatTokens);

	    boolean indentationIncluded = false;
	    while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) == 0)
		    || isComment(ts.token())) {
		if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT && !indentationIncluded) {
		    formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
		    indentationIncluded = true;
		}
		addFormatToken(formatTokens);
	    }
	    if (!indentationIncluded) {
		formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
	    }

	    if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLASS_LEFT_BRACE, ts.offset()));
	    }

	}

	ts.movePrevious();


	super.visit(node);

	if (node.isCurly() && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
	    while (ts.moveNext() && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
		if (ts.token().id() == PHPTokenId.PHP_CURLY_CLOSE) {
		    FormatToken lastToken = formatTokens.get(formatTokens.size() - 1);

		    if (lastToken.getId() == FormatToken.Kind.WHITESPACE
			    || lastToken.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
			formatTokens.remove(formatTokens.size() - 1);
			formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
			formatTokens.add(lastToken);
		    } else {
			formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
		    }

		    boolean includeWBC = false;  // is open after close ? {}
		    if (ts.movePrevious()
			    && (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN
			    || ts.token().id() == PHPTokenId.WHITESPACE)) {
			if (ts.token().id() == PHPTokenId.WHITESPACE) {
				if (ts.movePrevious() && ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
				    includeWBC = true;
				}
				ts.moveNext();
			}
			if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
			    includeWBC = true;
			}
		    }
		    ts.moveNext();
		    if (includeWBC) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_OPEN_CLOSE_BRACES, ts.offset()));
		    }

//		    boolean inludeSpaceBefore = (ts.movePrevious() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) ? true : false;
//		    ts.moveNext();
		    if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration) {
			if (includeWSBeforePHPDoc) {
			    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS_RIGHT_BRACE, ts.offset()));
			}
			addFormatToken(formatTokens);
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLASS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
			if (!includeWBC) {
			    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION_RIGHT_BRACE, ts.offset()));
			}
			addFormatToken(formatTokens);
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_FUNCTION, ts.offset() + ts.token().length()));
		    } else if (parent instanceof IfStatement) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_RIGHT_BRACE, ts.offset()));
			addFormatToken(formatTokens);
		    } else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_RIGHT_BRACE, ts.offset()));
			addFormatToken(formatTokens);
		    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_RIGHT_BRACE, ts.offset()));
			addFormatToken(formatTokens);
		    } else if (parent instanceof SwitchStatement) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_RIGHT_BACE, ts.offset()));
			addFormatToken(formatTokens);
		    } else if (parent instanceof CatchClause || parent instanceof TryStatement) {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_RIGHT_BRACE, ts.offset()));
			addFormatToken(formatTokens);
		    } else {
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OTHER_RIGHT_BRACE, ts.offset()));
			addFormatToken(formatTokens);
		    }
		} else {
		    addFormatToken(formatTokens);
		}
	    }
	    ts.movePrevious();
	}
    }

    @Override
    public void visit(CastExpression node) {
	super.visit(node);
    }

    @Override
    public void visit(ClassDeclaration node) {

	addAllUntilOffset(node.getStartOffset());
	if (includeWSBeforePHPDoc) {
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
	} else {
	    includeWSBeforePHPDoc = true;
	}
	while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) {
	    switch (ts.token().id()) {
		case PHP_IMPLEMENTS:
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset(), ts.token().text().toString()));
		    Expression inter = node.getInterfaes().get(0);
		    ts.movePrevious();
		    addUnbreakalbeSequence(inter, true); // adding implements keyword and first interface
		    for (int i = 1; i < node.getInterfaes().size(); i++) {
			if (ts.moveNext() && ts.token().id() == PHPTokenId.WHITESPACE) {
			    addFormatToken(formatTokens);
			}
			formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_INTERFACE_LIST, ts.offset()));
			inter = node.getInterfaes().get(i);
			addUnbreakalbeSequence(inter, false);
		    }
		    break;
		case PHP_EXTENDS:
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset(), ts.token().text().toString()));
		    addFormatToken(formatTokens);
		    break;
	    }
	    addFormatToken(formatTokens);
	}

	ts.movePrevious();
	super.visit(node);
    }

    @Override
    public void visit(ClassInstanceCreation node) {
	scan(node.getClassName());
	if (node.ctorParams() != null && node.ctorParams().size() > 0) {
	    formatTokens.add(new FormatToken.IndentToken(node.getClassName().getEndOffset(), options.continualIndentSize));
	    scan(node.ctorParams());
	    formatTokens.add(new FormatToken.IndentToken(node.ctorParams().get(node.ctorParams().size() - 1).getEndOffset(), -1 * options.continualIndentSize));
	    addAllUntilOffset(node.getEndOffset());
	} else {
	    super.visit(node);
	}
    }

    @Override
    public void visit(ConditionalExpression node) {
	scan(node.getCondition());
	boolean wrap = node.getIfTrue() != null ? true : false;
	boolean putContinualIndent = !(path.get(1) instanceof Assignment);
	if (wrap) {
	    while (ts.moveNext()
		    && !(ts.token().id() == PHPTokenId.PHP_TOKEN && "?".equals(ts.token().text().toString()))) {
		addFormatToken(formatTokens);
	    }

	    int start = ts.offset();
	    if (putContinualIndent) {
		formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
	    }
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, start));
	    ts.movePrevious();
	    addAllUntilOffset(node.getIfTrue().getStartOffset());
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));

	}
	scan(node.getIfTrue());
	if (wrap) {
	    addEndOfUnbreakableSequence(node.getIfTrue().getEndOffset());
	    if (putContinualIndent) {
		formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
	    }
	}
	wrap = node.getIfFalse() != null ? true : false;
	if (wrap) {
	    while (ts.moveNext()
		    && !(ts.token().id() == PHPTokenId.PHP_TOKEN && ":".equals(ts.token().text().toString()))) {
		addFormatToken(formatTokens);
	    }
	    int start = ts.offset();
	    if (putContinualIndent) {
		formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
	    }
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, start));
	    ts.movePrevious();
	    addAllUntilOffset(node.getIfFalse().getStartOffset());
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));

	}
	scan(node.getIfFalse());
	if (wrap) {
	    addEndOfUnbreakableSequence(node.getIfFalse().getEndOffset());
	    if (putContinualIndent) {
		formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
	    }
	}
    }

    @Override
    public void visit(DoStatement node) {
	ASTNode body = node.getBody();
	if (body != null && !(body instanceof Block)) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_DO_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(body);
	}
	scan(node.getCondition());
	addAllUntilOffset(node.getEndOffset());
    }

    @Override
    public void visit(ExpressionStatement node) {
	if (node.getExpression() instanceof FunctionInvocation) {
	    super.visit(node);
	} else {
	    while (ts.moveNext() && ts.offset() < node.getStartOffset()) {
		addFormatToken(formatTokens);
	    }
	    addFormatToken(formatTokens); // add the first token of the expression and then add the indentation
	    formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
	    super.visit(node);
	    formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), -1 * options.continualIndentSize));
	}
    }

    @Override
    public void visit(FieldsDeclaration node) {
	Block block = (Block) path.get(1);
	int index = 0;
	List<Statement> statements = block.getStatements();
	while (index < statements.size() && statements.get(index).getStartOffset() < node.getStartOffset()) {
	    index++;
	}
	addAllUntilOffset(node.getStartOffset());
	if (index < statements.size()
		&& index > 0 && statements.get(index - 1) instanceof FieldsDeclaration) {
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_FIELDS, ts.offset()));
	} else {
	    if (includeWSBeforePHPDoc) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELD, ts.offset()));
	    } else {
		includeWSBeforePHPDoc = true;
	    }
	}
	super.visit(node);
	if (index == statements.size() - 1
		|| ((index < statements.size() - 1) && !(statements.get(index + 1) instanceof FieldsDeclaration))) {
	    //addAllUntilOffset(statements.get(index).getEndOffset() + 1);
	    addRestOfLine();
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELD, ts.offset() + ts.token().length()));
	}
    }

    @Override
    public void visit(ForEachStatement node) {
	scan(node.getExpression());
	scan(node.getKey());
	scan(node.getValue());
	ASTNode body = node.getStatement();
	if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    scan(node.getStatement());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else if (body != null && !(body instanceof Block)) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(node.getStatement());
	}
    }

    @Override
    public void visit(ForStatement node) {
	scan(node.getInitializers());
	boolean wrap = node.getConditions() != null && node.getConditions().size() > 0 ? true : false;
	if (wrap) {
	    int start = node.getConditions().get(0).getStartOffset();
	    addAllUntilOffset(start);
	    formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	}
	scan(node.getConditions());
	if (wrap) {
	    addEndOfUnbreakableSequence(node.getConditions().get(node.getConditions().size() - 1).getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
	}
	wrap = node.getUpdaters() != null && node.getUpdaters().size() > 0 ? true : false;
	if (wrap) {
	    int start = node.getUpdaters().get(0).getStartOffset();
	    addAllUntilOffset(start);
	    formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	}
	scan(node.getUpdaters());
	if (wrap) {
	    addEndOfUnbreakableSequence(node.getUpdaters().get(node.getUpdaters().size() - 1).getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
	}
	ASTNode body = node.getBody();
	if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    scan(node.getBody());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else if (body != null && !(body instanceof Block)) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(node.getBody());
	}
    }

    @Override
    public void visit(FunctionDeclaration node) {
	if (!(path.size() > 1 && path.get(1) instanceof MethodDeclaration)) {
	    while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE
		    || isComment(ts.token()))) {
		addFormatToken(formatTokens);
	    }
	    if (includeWSBeforePHPDoc) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
	    } else {
		includeWSBeforePHPDoc = true;
	    }
	    ts.movePrevious();
	}
	scan(node.getFunctionName());    // scan the name
	// scan the parameters
	List<FormalParameter> parameters = node.getFormalParameters();
	if (parameters != null && parameters.size() > 0) {
	    while (ts.moveNext() && ts.offset() < parameters.get(0).getStartOffset()) {
		addFormatToken(formatTokens);
	    }
	    ts.movePrevious();
	    addUnbreakalbeSequence(parameters.get(0), true);
	    for (int i = 1; i < parameters.size(); i++) {
		if (ts.moveNext() && ts.token().id() == PHPTokenId.WHITESPACE) {
		    addFormatToken(formatTokens);
		} else {
		    ts.movePrevious();
		}
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_PARAMETER_LIST, ts.offset() + ts.token().length()));
		addUnbreakalbeSequence(parameters.get(i), false);
	    }
	}
        scan(node.getBody()); // scan the body of the function
    }

    @Override
    public void visit(FunctionInvocation node) {
	if (path.size() > 1 && path.get(1) instanceof MethodInvocation) {
	    while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_OBJECT_OPERATOR
		    && ts.offset() < node.getStartOffset()) {
		addFormatToken(formatTokens);
	    }
	    if (ts.token().id() == PHPTokenId.PHP_OBJECT_OPERATOR) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_CHAINED_METHOD_CALLS, ts.offset()));
		addFormatToken(formatTokens);
	    } else {
		ts.movePrevious();
	    }

	}
	scan(node.getFunctionName());
	if (node.getParameters() != null && node.getParameters().size() > 0) {
	    formatTokens.add(new FormatToken.IndentToken(node.getFunctionName().getEndOffset(), options.continualIndentSize));
	    scan(node.getParameters());
	    formatTokens.add(new FormatToken.IndentToken(node.getParameters().get(node.getParameters().size() - 1).getEndOffset(), -1 * options.continualIndentSize));
	    addAllUntilOffset(node.getEndOffset());
	} else {
	    super.visit(node);
	}
    }

    @Override
    public void visit(InfixExpression node) {
	scan(node.getLeft());
	FormatToken.Kind whitespace = FormatToken.Kind.WHITESPACE_AROUND_BINARY_OP;

	if (node.getOperator() == InfixExpression.OperatorType.CONCAT) {
	    whitespace = FormatToken.Kind.WHITESPACE_AROUND_CONCAT_OP;
	}

	while (ts.moveNext() && ts.offset() < node.getRight().getStartOffset()
		&& ts.token().id() != PHPTokenId.PHP_TOKEN) {
	    addFormatToken(formatTokens);
	}
	if (ts.token().id() == PHPTokenId.PHP_TOKEN) {
	    formatTokens.add(new FormatToken(whitespace, ts.offset()));
	    addFormatToken(formatTokens);
	    formatTokens.add(new FormatToken(whitespace, ts.offset() + ts.token().length()));
	} else {
	    ts.movePrevious();
	}
	scan(node.getRight());
    }

    @Override
    public void visit(IfStatement node) {
	addAllUntilOffset(node.getCondition().getStartOffset());
	formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
	scan(node.getCondition());
	ASTNode body = node.getTrueStatement();
	formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
	if (body != null && body instanceof Block && !((Block) body).isCurly()) {
	    isCurly = false;
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    scan(body);
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else if (body != null && !(body instanceof Block)) {
	    isCurly = false;
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_ELSE_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(node.getTrueStatement());
	}
	body = node.getFalseStatement();
	if (body != null && body instanceof Block && !((Block) body).isCurly()
		&& !(body instanceof IfStatement)) {
	    isCurly = false;
	    while (ts.moveNext() && ts.offset() < body.getStartOffset()) {
		if (ts.token().id() == PHPTokenId.PHP_ELSE || ts.token().id() == PHPTokenId.PHP_ELSEIF) {
		    formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		} else {
		    addFormatToken(formatTokens);
		}
	    }
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    scan(node.getFalseStatement());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else if (body != null && !(body instanceof Block) && !(body instanceof IfStatement)) {
	    isCurly = false;
	    while (ts.moveNext() && ts.offset() < body.getStartOffset()) {
		if (ts.token().id() == PHPTokenId.PHP_ELSE || ts.token().id() == PHPTokenId.PHP_ELSEIF) {
		    formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		} else {
		    addFormatToken(formatTokens);
		}
	    }
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_ELSE_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(node.getFalseStatement());
	}

    }

    @Override
    public void visit(MethodDeclaration node) {
	while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE
		|| isComment(ts.token()))) {
	    addFormatToken(formatTokens);
	}
	if (includeWSBeforePHPDoc) {
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
	} else {
	    includeWSBeforePHPDoc = true;
	}
	ts.movePrevious();
	super.visit(node);
    }

    @Override
    public void visit(NamespaceDeclaration node) {
	addAllUntilOffset(node.getStartOffset());
	formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_NAMESPACE, node.getStartOffset()));
	scan(node.getName());
	addRestOfLine();
	formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_NAMESPACE, node.getStartOffset()));
	scan(node.getBody());
    }

    @Override
    public void visit(Program program) {
	path.addFirst(program);
	ts.move(0);
	ts.moveNext();
	ts.movePrevious();
	addFormatToken(formatTokens);
	super.visit(program);
	while (ts.moveNext()) {
	    addFormatToken(formatTokens);
	}
	path.removeFirst();
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
	scan(node.getName());
	if (node.getValue() != null) {
	    while (ts.moveNext() && ts.offset() < node.getValue().getStartOffset()) {
		if (ts.token().id() == PHPTokenId.PHP_TOKEN && "=".equals(ts.token().text().toString())) {
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_ASSIGN_OP, ts.offset()));
		    addFormatToken(formatTokens);
		    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_ASSIGN_OP, ts.offset() + ts.token().length()));
		} else {
		    addFormatToken(formatTokens);
		}
	    }
	    ts.movePrevious();
	    scan(node.getValue());
	}
    }

    @Override
    public void visit(SwitchCase node) {
	if (node.getValue() == null) {
	    ts.moveNext();
	    addFormatToken(formatTokens);
	} else {
	    scan(node.getValue());
	}
	formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
	if (node.getActions() != null) {
	    scan(node.getActions());
	    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
	}

    }

    @Override
    public void visit(SwitchStatement node) {
	scan(node.getExpression());
	if (node.getBody() != null && (node.getBody() instanceof Block && !((Block) node.getBody()).isCurly())) {
	    addAllUntilOffset(node.getBody().getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(node.getBody().getStartOffset(), options.indentSize));
	    scan(node.getBody());
	    if (node.getBody().getStatements().size() > 0) {
		Statement lastOne = node.getBody().getStatements().get(node.getBody().getStatements().size() - 1);
		while (lastOne.getEndOffset() < formatTokens.get(formatTokens.size() - 1).getOffset()) {
		    formatTokens.remove(formatTokens.size() - 1);
		}
		while (lastOne.getEndOffset() < ts.offset()) {
		    ts.movePrevious();
		}
		formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
	    }
	    addAllUntilOffset(node.getEndOffset());
	} else {
	    scan(node.getBody());
	}
    }

    @Override
    public void visit(TryStatement node) {
	scan(node.getBody());
	scan(node.getCatchClauses());
    }

    @Override
    public void visit(WhileStatement node) {
	scan(node.getCondition());
	ASTNode body = node.getBody();
	if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    scan(node.getBody());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else if (body != null && !(body instanceof Block)) {
	    addAllUntilOffset(body.getStartOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_STATEMENT, ts.offset()));
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	    scan(body);
	    addEndOfUnbreakableSequence(body.getEndOffset());
	    formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
	} else {
	    scan(node.getBody());
	}
    }

    @Override
    public void visit(UseStatement node) {
	Block block = (Block) path.get(1);
	List<Statement> statements = block.getStatements();


	if (isPreviousNodeTheSameInBlock(block, node)) {
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_USE, ts.offset()));
	} else {
	    if (includeWSBeforePHPDoc) {
		formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE, ts.offset()));
	    }
	}
	includeWSBeforePHPDoc = true;


	super.visit(node);
	if (isNextNodeTheSameInBlock(block, node)) {
	    addRestOfLine();
	    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_USE, ts.offset() + ts.token().length()));
	}
    }

    private void addFormatToken(List<FormatToken> tokens) {
	switch (ts.token().id()) {
	    case WHITESPACE:
		tokens.add((countOfNewLines(ts.token().text()) > 0)
			? new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, ts.offset(), ts.token().text().toString())
			: new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset(), ts.token().text().toString()));
		break;
	    case PHP_LINE_COMMENT:
		String text = ts.token().text().toString();
		if (ts.token().text().charAt(ts.token().length() - 1) == '\n') {
		    text = text.substring(0, text.length() - 1);
		    int newOffset = ts.offset() + ts.token().length() - 1;
		    tokens.add(new FormatToken(FormatToken.Kind.LINE_COMMENT, ts.offset(), text));
		    if (ts.moveNext() && ts.token().id() == PHPTokenId.WHITESPACE) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, newOffset, "\n" + ts.token().text().toString()));
			if (ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
			    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_LINE_COMMENTS, ts.offset()));
			} else {
			    ts.movePrevious();
			}
		    } else {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, newOffset, "\n"));
			ts.movePrevious();
		    }

		} else {
		    tokens.add(new FormatToken(FormatToken.Kind.LINE_COMMENT, ts.offset(), text));
		}
		break;
	    case PHP_OPENTAG:
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OPEN_PHP_TAG, ts.offset()));
		tokens.add(new FormatToken(FormatToken.Kind.OPEN_TAG, ts.offset(), ts.token().text().toString()));
		tokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.initialIndent));
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_OPEN_PHP_TAG, ts.offset() + ts.token().length()));
		break;
	    case PHP_CLOSETAG:
		tokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.initialIndent));
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLOSE_PHP_TAG, ts.offset()));
		tokens.add(new FormatToken(FormatToken.Kind.CLOSE_TAG, ts.offset(), ts.token().text().toString()));
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLOSE_PHP_TAG, ts.offset() + ts.token().length()));

		break;
	    case PHP_COMMENT:
//	    case PHP_COMMENT_END:
//	    case PHP_COMMENT_START:
		tokens.add(new FormatToken(FormatToken.Kind.COMMENT, ts.offset(), ts.token().text().toString()));
		break;
	    case PHPDOC_COMMENT:
		tokens.add(new FormatToken(FormatToken.Kind.DOC_COMMENT, ts.offset(), ts.token().text().toString()));
		break;
	    case PHPDOC_COMMENT_END:
//	    case PHPDOC_COMMENT_START:
		tokens.add(new FormatToken(FormatToken.Kind.DOC_COMMENT_END, ts.offset(), ts.token().text().toString()));
		break;
	    case PHP_OBJECT_OPERATOR:
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_OBJECT_OP, ts.offset()));
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_OBJECT_OP, ts.offset() + ts.token().length()));
		break;
	    case PHP_CASTING:
		text = ts.token().text().toString();
		String part1 = text.substring(0, text.indexOf('(') + 1);
		String part2 = text.substring(part1.length(), text.indexOf(')'));
		String part3 = text.substring(part1.length() + part2.length());
		String ws1 = "";
		String ws2 = "";
		int index = 0;
		while (index < part2.length() && part2.charAt(index) == ' ') {
		    ws1 = ws1 + ' ';
		    index++;
		}
		index = part2.length() - 1;
		while (index > 0 && part2.charAt(index) == ' ') {
		    ws2 = ws2 + ' ';
		    index--;
		}
		part2 = part2.trim();
		int length = 0;
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), part1));
		length += part1.length();
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_TYPE_CAST_PARENS, ts.offset() + part1.length()));
		if (ws1.length() > 0) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset() + length, ws1));
		    length += ws1.length();
		}
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset() + length, part2));
		length += part2.length();
		if (ws2.length() > 0) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset() + length, ws2));
		    length += ws2.length();
		}
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_TYPE_CAST_PARENS, ts.offset() + length));
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset() + length, part3));
		length += part3.length();
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_TYPE_CAST, ts.offset() + length));
		break;
	    case PHP_TOKEN:
		text = ts.token().text().toString();
		ASTNode parent = path.get(0);
		if ("(".equals(text)) {
		    if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_METHOD_DEC_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof FunctionInvocation || parent instanceof MethodInvocation || parent instanceof ClassInstanceCreation) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_METHOD_CALL_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof IfStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_IF_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof ForEachStatement || parent instanceof ForStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_FOR_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_WHILE_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof SwitchStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_SWITCH_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof CatchClause) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_CATCH_PARENS, ts.offset() + ts.token().length()));
		    } else if (parent instanceof ArrayCreation) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_PAREN, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_DECL_PARENS, ts.offset() + ts.token().length()));
		    } else {
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    }
		} else if (")".equals(text)) {
		    if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof FunctionInvocation || parent instanceof MethodInvocation || parent instanceof ClassInstanceCreation) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof IfStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_IF_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof ForEachStatement || parent instanceof ForStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_FOR_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_WHILE_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof SwitchStatement) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_SWITCH_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof CatchClause) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_CATCH_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else if (parent instanceof ArrayCreation) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_DECL_PARENS, ts.offset()));
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    } else {
			tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    }
		} else if ("[".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS, ts.offset() + ts.token().length()));
		} else if ("]".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		} else if (parent instanceof ConditionalExpression
			&& ("?".equals(text) || ":".equals(text))) {
		    //tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset() + ts.token().length()));
		} else if (",".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_COMMA, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_COMMA, ts.offset() + ts.token().length()));
		} else if ("!".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), text));
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset() + ts.token().length()));
		} else {
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		}
		break;
	    case PHP_OPERATOR:
		text = ts.token().text().toString();
		if ("=>".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_KEY_VALUE_OP, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_KEY_VALUE_OP, ts.offset() + ts.token().length()));
		} else if ("++".equals(text) || "--".equals(text)) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset()));
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), text));
		    //if (ts.moveNext() && !(ts.token().id() == PHPTokenId.PHP_TOKEN && ")".equals(ts.token().text().toString()))) {
			tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset() + ts.token().length()));
		    //}
//		    ts.movePrevious();
		} else {
		    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		}
		break;
	    case PHP_WHILE:
		if (path.get(0) instanceof DoStatement && isCurly) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE, ts.offset()));
		}
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		break;
	    case PHP_ELSE:
	    case PHP_ELSEIF:
		if (isCurly) {
		    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ELSE, ts.offset()));
		}
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		break;
	    case PHP_SEMICOLON:
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SEMI, ts.offset()));
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		//tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_SEMI, ts.offset() + ts.token().length()));
		break;
	    case PHP_CATCH:
		tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH, ts.offset()));
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
		break;
	    case T_INLINE_HTML:
		FormatToken.InitToken token = (FormatToken.InitToken)formatTokens.get(0);
		if (!token.hasHTML() && !isWhitespace(ts.token().text())) {
		    token.setHasHTML(true);
		}
	    default:
		tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
	}
    }

    private void addAllUntilOffset(int offset) {
	while (ts.moveNext() && ts.offset() < offset) {
	    addFormatToken(formatTokens);
	}
	ts.movePrevious();
    }

    private void addRestOfLine() {
	while (ts.moveNext()
		&& !(ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) > 0)
		&& ts.token().id() != PHPTokenId.PHP_LINE_COMMENT) {
	    addFormatToken(formatTokens);
	}
	if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
		|| (ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) > 0)) {
	    addFormatToken(formatTokens);
	    if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT && ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
		addFormatToken(formatTokens);
	    } else {
		ts.movePrevious();
	    }
	} else {
	    ts.movePrevious();
	}
    }

    private int getIndentSize() {
	return options.initialIndent + indentLevel * options.indentSize;
    }

    /**
     *
     * @param chs
     * @return number of new lines in the input
     */
    private int countOfNewLines(CharSequence chs) {
	int count = 0;
	for (int i = 0; i < chs.length(); i++) {
	    if (chs.charAt(i) == '\n') { // NOI18N
		count++;
	    }
	}
	return count;
    }

    private void addEndOfUnbreakableSequence(int endOffset) {
	while (ts.moveNext()
		&& ((ts.token().id() == PHPTokenId.WHITESPACE
		&& countOfNewLines(ts.token().text()) == 0)
		|| isComment(ts.token()))) {
	    addFormatToken(formatTokens);
	    if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
		    && !"//".equals(ts.token().text().toString())) {
		break;
	    }
	}
	if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
	    FormatToken last = formatTokens.remove(formatTokens.size() - 1);
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length() - 1, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
	    formatTokens.add(last);
	} else {
	    ts.movePrevious();
	    if ((ts.token().id() == PHPTokenId.WHITESPACE
		    && countOfNewLines(ts.token().text()) == 0)) {
		formatTokens.remove(formatTokens.size() - 1);
		formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
		ts.movePrevious();
	    } else {
		formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
	    }
	}
    }

    private void addUnbreakalbeSequence(ASTNode node, boolean addAnchor) {
	formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
	addAllUntilOffset(node.getStartOffset());
//	while (ts.moveNext() && ts.offset() < endOffset) {
//	    if (positionOfAnchor > 0 && ts.offset() == positionOfAnchor) {
//		formatTokens.add(new FormatToken.AnchorToken(ts.offset()));
//	    }
//	    addFormatToken(formatTokens);  // add all until the end offset
//	}
	if (addAnchor) {
	    formatTokens.add(new FormatToken.AnchorToken(ts.offset() + ts.token().length()));
	}
	scan(node);
//	ts.movePrevious();
	// add , whitespaces and comments
	while (ts.moveNext()
		&& (ts.token().id() == PHPTokenId.WHITESPACE
		|| isComment(ts.token())
		|| (ts.token().id() == PHPTokenId.PHP_TOKEN && ",".equals(ts.token().text().toString())))) {
	    addFormatToken(formatTokens);
	}
	ts.movePrevious();

	int index = formatTokens.size() - 1;
	FormatToken lastToken = formatTokens.get(index);
	FormatToken removedWS = null;
	if (lastToken.getId() == FormatToken.Kind.WHITESPACE || lastToken.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
	    removedWS = formatTokens.remove(formatTokens.size() - 1);
	    index--;
	    lastToken = formatTokens.get(index);
	}

	if (lastToken.getId() == FormatToken.Kind.WHITESPACE_AFTER_COMMA) {
	    formatTokens.remove(index);
	    formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
	    formatTokens.add(lastToken);
	    if (removedWS != null) {
		formatTokens.add(removedWS);
	    }
	} else {
	    if (lastToken.getId() == FormatToken.Kind.LINE_COMMENT && removedWS != null) {
		formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length() - 1, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
		formatTokens.add(removedWS);
		ts.moveNext();
	    } else {
		formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
	    }

	}
    }

    private boolean isComment(Token<? extends PHPTokenId> token) {
	return token.id() == PHPTokenId.PHPDOC_COMMENT
		|| token.id() == PHPTokenId.PHPDOC_COMMENT_END
		|| token.id() == PHPTokenId.PHPDOC_COMMENT_START
		|| token.id() == PHPTokenId.PHP_COMMENT
		|| token.id() == PHPTokenId.PHP_COMMENT_END
		|| token.id() == PHPTokenId.PHP_COMMENT_START
		|| token.id() == PHPTokenId.PHP_LINE_COMMENT;
    }

    private boolean isPreviousNodeTheSameInBlock(Block block, Statement statement) {
	int index = 0;   // index of the current statement in the block
	List<Statement> statements = block.getStatements();
	while (index < statements.size() && statements.get(index).getStartOffset() < statement.getStartOffset()) {
	    index++;
	}
	return (index < statements.size()
		&& index > 0
		&& statements.get(index - 1).getClass().equals(statement.getClass()));
    }

    private boolean isNextNodeTheSameInBlock(Block block, Statement statement) {
	int index = 0;   // index of the current statement in the block
	List<Statement> statements = block.getStatements();
	while (index < statements.size() && statements.get(index).getStartOffset() < statement.getStartOffset()) {
	    index++;
	}
	return (index == statements.size() - 1
		|| !((index < statements.size() - 1) && (statements.get(index + 1).getClass().equals(statement.getClass()))));
    }

    protected static boolean isWhitespace (final CharSequence text) {
	int index = 0;
	while (index < text.length()
		&& Character.isWhitespace(text.charAt(index))) {
	    index++;
	}
	return index == text.length();
    }
}
