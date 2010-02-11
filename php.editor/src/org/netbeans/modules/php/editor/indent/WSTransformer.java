/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import sun.security.jca.GetInstance;

/**
 * This class calculates all white-space tranformations other than
 * line indentation, e.g. breaking or merging lines,
 * removing reduntant spaces
 *
 * @author Tomasz.Slota@Sun.COM
 */
class WSTransformer extends DefaultTreePathVisitor {
    private String newLineReplacement = "\n"; //NOI18N

    private Context context;
    private List<Replacement> replacements = new LinkedList<WSTransformer.Replacement>();
    private Collection<CodeRange> unbreakableRanges = new TreeSet<CodeRange>();
    private Collection<Integer> breakPins = new LinkedList<Integer>();

    private final List<PHPTokenId> WS_AND_COMMENT_TOKENS = Arrays.asList(PHPTokenId.PHPDOC_COMMENT_START,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.WHITESPACE,
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT,
            PHPTokenId.PHP_LINE_COMMENT);

    private final List<PHPTokenId> COMMENT_TOKENS = Arrays.asList(PHPTokenId.PHPDOC_COMMENT_START,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT,
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT,
            PHPTokenId.PHP_LINE_COMMENT);
    
    private final Collection<PHPTokenId> NO_BREAK_B4_TKNS = Arrays.asList(PHPTokenId.PHP_CLOSETAG,
            PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_CATCH,
            PHPTokenId.PHP_WHILE);

    private final List<String> ASSIGN_OPERATORS =  Arrays.asList(
	    "=", ".=", "=.", "+=", "=+", "=-", "-=" //NOI18N
	    );
    private final List<String> BINARY_OPERATORS =  Arrays.asList(
	    "+", "-", "*", "/", "<", ">", "<>", "<=", ">=", "==", "===", //NOI18N
	    "%", "&", "|", "^", "~", "<<", ">>", "!=", "!==", ".", "&&", "||"
	    );
    private final List<String> UNARY_OPERATOS = Arrays.asList(
	    "++", "--", "!" //NOI8N
	    );
    // keep information, whether the function declaration is visited directly or from method declaration
    private boolean isMethod;

    public WSTransformer(Context context) {
        this.context = context;
        isMethod = false;
    }

    @Override
    public void visit(Program node) {
	super.visit(node);
	TokenSequence<PHPTokenId> ts = tokenSequence(node.getStartOffset());
        Token<? extends PHPTokenId> token;

	while (ts.moveNext()) {
	    token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_OPERATOR, 
		    PHPTokenId.PHP_OBJECT_OPERATOR));
	    if (token.id() == PHPTokenId.PHP_OBJECT_OPERATOR) {
		checkSpaceAroundToken(ts, CodeStyle.get(context.document()).spaceAroundObjectOps());
	    }
	    else {
		String text = token.text().toString();
		if (".".equals(text)) {
		    checkSpaceAroundToken(ts, CodeStyle.get(context.document()).spaceAroundStringConcatOps());
		}
		else if (ASSIGN_OPERATORS.contains(text)) {
		    checkSpaceAroundToken(ts, CodeStyle.get(context.document()).spaceAroundAssignOps());
		}
		else if (BINARY_OPERATORS.contains(text)) {
		    checkSpaceAroundToken(ts, CodeStyle.get(context.document()).spaceAroundBinaryOps());
		}
		else if (UNARY_OPERATOS.contains(text)) {
		    boolean check = false;
		    if (ts.moveNext()) {
			LexUtilities.findNext(ts, WS_AND_COMMENT_TOKENS);
			if (ts.token().id() != PHPTokenId.PHP_TOKEN) {
			    check = true;
			}
		    }
		    else {
			check = true;
		    }
		    if (check) {
			checkSpaceAroundToken(ts, CodeStyle.get(context.document()).spaceAroundUnaryOps());
		    }
		}
	    }
	}
    }


    @Override
    public void visit(Block node) {
        // TODO: check formatting boundaries here

	ASTNode parent = getPath().get(0);

        if (parent instanceof NamespaceDeclaration){
            super.visit(node);
            return;
        }
        
        if (node.isCurly()){
	    CodeStyle.BracePlacement openingBraceStyle;
	    if (parent instanceof ClassDeclaration) {
		openingBraceStyle = CodeStyle.get(context.document()).getClassDeclBracePlacement();
	    }
	    else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
		openingBraceStyle = CodeStyle.get(context.document()).getMethodDeclBracePlacement();
	    }
	    else {
		openingBraceStyle = CodeStyle.get(context.document()).getOtherBracePlacement();
	    }

            newLineReplacement = CodeStyle.BracePlacement.NEW_LINE == openingBraceStyle ? "\n" : " "; //NOI18N
            if (CodeStyle.BracePlacement.NEW_LINE != openingBraceStyle && getPath().size() > 0) {
                if (parent instanceof ClassDeclaration) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeClassDeclLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof FunctionDeclaration) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeMethodDeclLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof IfStatement) {
                    IfStatement ifStatement = (IfStatement) parent;
                    if (node.getStartOffset() == ifStatement.getTrueStatement().getStartOffset()) {
                        newLineReplacement = CodeStyle.get(context.document()).spaceBeforeIfLeftBrace() ? " " : ""; //NOI18N
                    }
                    else {
                        newLineReplacement = CodeStyle.get(context.document()).spaceBeforeElseLeftBrace() ? " " : ""; //NOI18N
                    }
                }
                else if (parent instanceof WhileStatement) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeWhileLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof DoStatement) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeDoLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof SwitchStatement) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeSwitchLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeForLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof TryStatement) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeTryLeftBrace() ? " " : ""; //NOI18N
                }
                else if (parent instanceof CatchClause) {
                    newLineReplacement = CodeStyle.get(context.document()).spaceBeforeCatchLeftBrace() ? " " : ""; //NOI18N
                }
            }
            
            TokenSequence<PHPTokenId> tokenSequence = tokenSequence(node.getStartOffset());
            tokenSequence.move(node.getStartOffset());

            if (tokenSequence.moveNext()
                    && tokenSequence.token().id() == PHPTokenId.PHP_CURLY_OPEN){
                int start = tokenSequence.offset();
                int length = 0;
                if (tokenSequence.movePrevious()
                        && tokenSequence.token().id() == PHPTokenId.WHITESPACE){
                    length = tokenSequence.token().length();
		    if (CodeStyle.BracePlacement.PRESERVE_EXISTING == openingBraceStyle
			    && countOfNewLines(tokenSequence.token().text()) > 0) {
			    newLineReplacement = "\n";
		    }
                }

                boolean precededByOpenTag = tokenSequence.token().id() == PHPTokenId.PHP_OPENTAG;

                if (!precededByOpenTag && length > 0){
                    if (tokenSequence.movePrevious()){
                        precededByOpenTag = tokenSequence.token().id() == PHPTokenId.PHP_OPENTAG;
                    }
                }

                if (!precededByOpenTag){
                    Replacement preOpenBracket = new Replacement(start, length, newLineReplacement);
                    replacements.add(preOpenBracket);
                }
            }

            tokenSequence.move(node.getStartOffset());

            if (tokenSequence.moveNext() && !doNotSplitLine(tokenSequence, true)){
                Replacement postOpen = new Replacement(tokenSequence.offset() +
                        tokenSequence.token().length(), 0, "\n"); //NOI18N
                replacements.add(postOpen);
            }

            tokenSequence.move(node.getEndOffset());

            if (tokenSequence.movePrevious()){
                int closPos = tokenSequence.offset();
                if (!doNotSplitLine(tokenSequence, false)){
                    // avoid adding double line break in case that } is preceded with ;
                    tokenSequence.movePrevious();
                    if (tokenSequence.token().id() != PHPTokenId.PHP_SEMICOLON
                            && tokenSequence.token().id() != PHPTokenId.PHP_OPENTAG){
                        Replacement preClose = new Replacement(closPos, 0, "\n"); //NOI18N
                        replacements.add(preClose);
                    }
                    tokenSequence.moveNext();
                }

                tokenSequence.move(node.getEndOffset());
                if (tokenSequence.movePrevious() && !doNotSplitLine(tokenSequence, true)){

                    tokenSequence.move(node.getEndOffset());
                    if (tokenSequence.moveNext()){

                        if (tokenSequence.token().id() == PHPTokenId.WHITESPACE){
                            tokenSequence.moveNext();
                        }

                        PHPTokenId id = tokenSequence.token().id();

                        if (id != PHPTokenId.PHP_SEMICOLON && !(id == PHPTokenId.PHP_TOKEN
                                && TokenUtilities.equals(tokenSequence.token().text(), ","))){
                            Replacement postClose = new Replacement(tokenSequence.offset() +
                                    tokenSequence.token().length(), 0, "\n"); //NOI18N
                            replacements.add(postClose);
                        }
                    }
                }
            }
            
        }
        super.visit(node);
    }

    @Override
    public void visit(ClassDeclaration node) {
        // Blank lines
        int insertLines = 0;
        ASTNode previousNode = previousNode(node);

        insertLines = (previousNode == null)
                ? CodeStyle.get(context.document()).getBlankLinesBeforeClass()
                : insertLineBeforeAfter(astNodeToType(previousNode), astNodeToType(node));
        checkEmptyLinesBefore(node.getStartOffset(), insertLines, true);

        // lines after header of class (after {)
        List<Statement> statements = node.getBody().getStatements();
        if (statements.size() == 0 || !isBlankLinesInteresting(statements.get(0))) {
            insertLines = insertLineBeforeAfter(ElemType.CLASS, ElemType.CLASS_HEADER);
            TokenSequence<PHPTokenId> ts = tokenSequence(node.getStartOffset());
            ts.move(node.getStartOffset());
            if (ts.moveNext() && ts.moveNext()) {
                LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_CURLY_OPEN));
                checkEmptyLinesBefore(ts.offset() + 1, insertLines, true);
            }
        }

        // line before end of class (before })
        insertLines = (statements.size() == 0)
                ? insertLineBeforeAfter(ElemType.CLASS_HEADER, ElemType.CLASS_BEFORE_END)
                : insertLineBeforeAfter(astNodeToType(statements.get(statements.size() - 1)), ElemType.CLASS_BEFORE_END);
        checkEmptyLinesBefore(node.getEndOffset() - 1, insertLines, true);

        // lines after class declaration (after })
        ASTNode nextNode = nextNode(node);
        if (nextNode == null || !isBlankLinesInteresting(nextNode)) {
             checkEmptyLinesBefore(node.getEndOffset() + 1,
                     CodeStyle.get(context.document()).getBlankLinesAfterClass(), false);
        }
        super.visit(node);
    }

    @Override
    public void visit(MethodDeclaration node) {
        visitFunctionMethod(node);
        isMethod = true;
        super.visit(node);
        isMethod = false;
    }


    @Override
    public void visit(FunctionDeclaration node) {
        if (!isMethod) {  // add blank lines, only if it is not a method
            visitFunctionMethod(node);
        }
        super.visit(node);
    }

    private void visitFunctionMethod(ASTNode node) {
        int insertLines = 0;
        ASTNode previousNode = previousNode(node);

        insertLines = (previousNode == null)
                ? CodeStyle.get(context.document()).getBlankLinesBeforeFunction()
                : insertLineBeforeAfter(astNodeToType(previousNode), astNodeToType(node));
        checkEmptyLinesBefore(node.getStartOffset(), insertLines, true);

        // line before end of function (before })
        List<Statement> statements = null;
        if (node instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration)node;
            if (md.getFunction().getBody() != null) {
                statements = md.getFunction().getBody().getStatements();
            }
            else {
                // probably abstract method
                return;
            }
        }
        else if (node instanceof FunctionDeclaration) {
            statements = ((FunctionDeclaration)node).getBody().getStatements();
        }


        insertLines = (statements == null || statements.size() == 0)
                ? insertLineBeforeAfter(ElemType.FUNCTION, ElemType.FUNCTION_BEFORE_END)
                : insertLineBeforeAfter(astNodeToType(statements.get(statements.size() - 1)), ElemType.FUNCTION_BEFORE_END);
        checkEmptyLinesBefore(node.getEndOffset() - 1, insertLines, true);

        // format the end of the function method after }
        ASTNode nextNode = nextNode(node);
        if (nextNode == null || !isBlankLinesInteresting(nextNode)) {
             checkEmptyLinesBefore(node.getEndOffset(),
                     CodeStyle.get(context.document()).getBlankLinesAfterFunction(), false);
        }

        // space between method/function name and (
        Identifier name = null;
        if (node instanceof FunctionDeclaration) name = ((FunctionDeclaration)node).getFunctionName();
        if (node instanceof MethodDeclaration) name = ((MethodDeclaration)node).getFunction().getFunctionName();
        if (name != null) {
            checkSpaceBetweenTokenAndOpenParen(name.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeMethodDeclParen(),
                     Arrays.asList(PHPTokenId.PHP_STRING));
        }
    }

    @Override
    public void visit(FunctionName node) {
        super.visit(node);
        // space between method/function call and (
        checkSpaceBetweenTokenAndOpenParen(node.getName().getEndOffset(), CodeStyle.get(context.document()).spaceBeforeMethodCallParen(),
                     Arrays.asList(PHPTokenId.PHP_STRING));
    }


    @Override
    public void visit(NamespaceDeclaration node) {
        int insertLines = 0;
        ASTNode pnode = previousNode(node);

        insertLines = (pnode == null)
                ? CodeStyle.get(context.document()).getBlankLinesBeforeNamespace()
                : insertLineBeforeAfter(astNodeToType(pnode), astNodeToType(node));
        checkEmptyLinesBefore(node.getStartOffset(), insertLines, true);

        pnode = (node.getBody().getStatements().size() > 0) ?  node.getBody().getStatements().get(0) : null;
        if (pnode == null || !isBlankLinesInteresting(pnode)) {
            insertLines = CodeStyle.get(context.document()).getBlankLinesAfterNamespace();
            TokenSequence<PHPTokenId> ts = tokenSequence(node.getStartOffset());
            ts.move(node.getStartOffset());
            if (ts.moveNext() && ts.moveNext()) {
                LexUtilities.findEndOfLine(ts);
                checkEmptyLinesBefore(ts.offset(), insertLines, true);
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(UseStatement node) {
        int insertLines = 0;
        ASTNode previousNode = previousNode(node);

        insertLines = (previousNode == null)
                ? CodeStyle.get(context.document()).getBlankLinesBeforeUse()
                : insertLineBeforeAfter(astNodeToType(previousNode), astNodeToType(node));
        checkEmptyLinesBefore(node.getStartOffset(), insertLines, true);

        ASTNode nextNode = nextNode(node);
        if (nextNode == null || !isBlankLinesInteresting(nextNode)) {
             insertLines = CodeStyle.get(context.document()).getBlankLinesAfterUse();
             checkEmptyLinesBefore(node.getEndOffset(), insertLines, false);
        }

        super.visit(node);
    }

    @Override
    public void visit(FieldsDeclaration node) {
        int insertLines = 0;
        ASTNode previousNode = previousNode(node);

        insertLines =  (previousNode == null)
                ? CodeStyle.get(context.document()).getBlankLinesBeforeField()
                : insertLineBeforeAfter(astNodeToType(previousNode), astNodeToType(node));
        checkEmptyLinesBefore(node.getStartOffset(), insertLines, true);

        ASTNode nextNode = nextNode(node);
        if (nextNode == null || !isBlankLinesInteresting(nextNode)) {
            insertLines = CodeStyle.get(context.document()).getBlankLinesAfterField();
            checkEmptyLinesBefore(node.getEndOffset(), insertLines, false);
        }
        super.visit(node);
    }

    @Override
    public void visit(DoStatement node) {
        super.visit(node);
        int offset = node.getEndOffset();
        TokenSequence<PHPTokenId> ts = tokenSequence(offset);
        ts.move(offset);
        if (ts.moveNext() && ts.movePrevious()) {
            LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_WHILE));
            offset = ts.offset();
            // space between } and WHILE
            checkSpaceBetweenCurlyCloseAndToken(offset, CodeStyle.get(context.document()).spaceBeforeWhile(),
                    Arrays.asList(PHPTokenId.PHP_WHILE));
            // space between WHILE and (
            checkSpaceBetweenTokenAndOpenParen(offset, CodeStyle.get(context.document()).spaceBeforeWhileParen(),
                     Arrays.asList(PHPTokenId.PHP_WHILE));
        }
	// spaces within
	checkSpacesWithinParents(
		node.getCondition().getStartOffset(),
		node.getCondition().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinWhileParens());
    }

    @Override
    public void visit(IfStatement node) {
        super.visit(node);
        // space between IF or ELSEIF and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeIfParen(),
                 Arrays.asList(PHPTokenId.PHP_IF));
        if (node.getFalseStatement() != null) {
            // space between } and ELSE or ELSEIF
            checkSpaceBetweenCurlyCloseAndToken(node.getFalseStatement().getStartOffset(),
                    CodeStyle.get(context.document()).spaceBeforeElse(),
                    Arrays.asList(PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF));
        }
	// spaces within
	checkSpacesWithinParents(
		node.getCondition().getStartOffset(),
		node.getCondition().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinIfParens());
    }

    @Override
    public void visit(WhileStatement node) {
        super.visit(node);
        // space between WHILE and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeWhileParen(),
                 Arrays.asList(PHPTokenId.PHP_WHILE));
	// spaces within
	checkSpacesWithinParents(
		node.getCondition().getStartOffset(),
		node.getCondition().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinWhileParens());
    }

    @Override
    public void visit(SwitchStatement node) {
        super.visit(node);
        // space between SWITCH and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeSwitchParen(),
                 Arrays.asList(PHPTokenId.PHP_SWITCH));
	// spaces within
	checkSpacesWithinParents(
		node.getExpression().getStartOffset(),
		node.getExpression().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinSwitchParens());
    }

    @Override
    public void visit(CatchClause node) {
        super.visit(node);
        // space between } and CATCH
        checkSpaceBetweenCurlyCloseAndToken(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeCatch(),
                 Arrays.asList(PHPTokenId.PHP_CATCH));
        // space between CATCH and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeCatchParen(),
                 Arrays.asList(PHPTokenId.PHP_CATCH));
	// spaces within
	checkSpacesWithinParents(
		node.getClassName().getStartOffset(),
		node.getVariable().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinCatchParens());
    }

    @Override
    public void visit(ForStatement node) {
        int start = node.getStartOffset();
        int end = node.getBody().getStartOffset();
        unbreakableRanges.add(new CodeRange(start, end));
        // space between FOR and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeForParen(),
                 Arrays.asList(PHPTokenId.PHP_FOR));
	// spaces within
	if (node.getInitializers().size() > 0 && node.getUpdaters().size() > 0) {
	    checkSpacesWithinParents(
		    node.getInitializers().get(0).getStartOffset(),
		    node.getUpdaters().get(node.getUpdaters().size()-1).getEndOffset(),
		    CodeStyle.get(context.document()).spaceWithinForParens());
	}
        super.visit(node);

    }

    @Override
    public void visit(ForEachStatement node) {
        // space between FOREACH and (
        checkSpaceBetweenTokenAndOpenParen(node.getStartOffset(), CodeStyle.get(context.document()).spaceBeforeForParen(),
                 Arrays.asList(PHPTokenId.PHP_FOREACH));
	// spaces within
	checkSpacesWithinParents(
		node.getExpression().getStartOffset(),
		node.getValue().getEndOffset(),
		CodeStyle.get(context.document()).spaceWithinForParens());
        super.visit(node);
    }

    private void checkSpaceBetweenCurlyCloseAndToken(int offset, boolean insertSpace,
             final List<PHPTokenId> beforeTokens) {
        TokenSequence<PHPTokenId> ts = tokenSequence(offset);
        ts.move(offset);
        if (ts.moveNext() && ts.movePrevious()) {
            LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_SEMICOLON ));
            LexUtilities.findPreviousToken(ts, beforeTokens);
            if (beforeTokens.contains(ts.token().id())) {
                replaceSpaceBeforeToken(ts, insertSpace, Arrays.asList(PHPTokenId.PHP_CURLY_CLOSE));
            }
        }
    }

    private void checkSpaceBetweenTokenAndOpenParen(int offset, boolean insertSpace,
             final List<PHPTokenId> afterTokens) {
        TokenSequence<PHPTokenId> ts = tokenSequence(offset);
        ts.move(offset);
        if (ts.moveNext() && ts.movePrevious()) {
            Token<? extends PHPTokenId> token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
            if (token.text().charAt(0) == '(') {
                replaceSpaceBeforeToken(ts, insertSpace, afterTokens);
            }
        }
    }

    private void checkSpaceAroundToken(TokenSequence<PHPTokenId>ts, boolean insertSpace) {
	int offset = ts.offset();
	replaceSpaceBeforeToken(ts, insertSpace, null);
	ts.move(offset);
	ts.moveNext();

	if (ts.moveNext()) {
	    LexUtilities.findNext(ts, WS_AND_COMMENT_TOKENS);
	    replaceSpaceBeforeToken(ts, insertSpace, null);
	    ts.move(offset);
	    ts.moveNext();
	}
    }

    private void checkSpacesWithinParents(int start, int end, boolean space) {
	TokenSequence<PHPTokenId> ts = tokenSequence(start);
	ts.move(start);
	if (ts.moveNext() && ts.movePrevious()) {
	    if (ts.token().id() == PHPTokenId.PHP_TOKEN) {
		ts.moveNext();
	    }
	    LexUtilities.findNext(ts, WS_AND_COMMENT_TOKENS);
	    replaceSpaceBeforeToken(ts, space, Arrays.asList(PHPTokenId.PHP_TOKEN));
	}
	ts.move(end);
	if (ts.moveNext() && ts.movePrevious()) {
	    PHPTokenId tokenid = ts.token().id();
	    ts.moveNext();
	    LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
	    replaceSpaceBeforeToken(ts, space, Arrays.asList(tokenid));
	}
    }

    private void replaceSpaceBeforeToken(TokenSequence<PHPTokenId> ts, boolean space,
            final List<PHPTokenId> afterTokens) {
        if (ts.movePrevious()) {
            Token<? extends PHPTokenId> token = ts.token();
            if (((afterTokens == null && token.id() != PHPTokenId.WHITESPACE)
		    || (afterTokens != null && afterTokens.contains(token.id()))
                    || token.id() == PHPTokenId.PHP_COMMENT_END) && space) {
                replacements.add(new Replacement(ts.offset() + token.length(), 0, " ")); //NOI18N
            } else if (token.id() == PHPTokenId.WHITESPACE && countOfNewLines(token.text()) == 0) {
                if (space) {
                    if (token.text().length() > 1) {
                        replacements.add(new Replacement(ts.offset() + token.length(), token.length(), " ")); //NOI18N
                    }
                } else {
                    replacements.add(new Replacement(ts.offset() + token.length(), token.length(), "")); //NOI18N
                }
                ts.movePrevious();
                token = ts.token();
            }
            if (token.id() == PHPTokenId.PHP_COMMENT_END) {
                token = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.PHP_COMMENT_END,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_START));
                ts.moveNext();
                replaceSpaceBeforeToken(ts, space, afterTokens);
            }
        }
    }

    public void tokenScan(){
        // TODO: check formatting boundaries here
        TokenSequence<PHPTokenId> tokenSequence = tokenSequence(0);
        tokenSequence.moveStart();

        while (tokenSequence.moveNext()){
            if (!isWithinUnbreakableRange(tokenSequence.offset())
                    && splitTrigger(tokenSequence)){
                int splitPos = tokenSequence.offset() + 1;
                if (doNotSplitLine(tokenSequence, true)){
                    continue;
                }

                Replacement replacement = new Replacement(splitPos, 0, "\n");
                replacements.add(replacement);
            }
        }
    }

    private boolean splitTrigger(TokenSequence<PHPTokenId> tokenSequence){

        PHPTokenId tokenId = tokenSequence.token().id();

        if (tokenId == PHPTokenId.PHP_SEMICOLON){
            return true;
        }

        //TODO: handle 'case:'

        return false;
    }

    /**
     *
     * @param offset
     * @param countOfLines  How many lines should be plased on the offset
     * @param checkCommentsBefore Should be the comments before the offset pressed?
     */
    private void checkEmptyLinesBefore(int offset, int countOfLines, boolean checkCommentsBefore) {
        TokenSequence<PHPTokenId> ts = tokenSequence(offset);
        ts.move(offset);
        if (ts.moveNext() && ts.movePrevious()) {
            int currentNewLines = 0;
            Token<? extends PHPTokenId> token = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.WHITESPACE,
                    PHPTokenId.PHP_PRIVATE, PHPTokenId.PHP_PUBLIC, PHPTokenId.PHP_PROTECTED));
            boolean lineCommentBefore = false;

            while((isComment(token) && checkCommentsBefore)
                    || token.id() == PHPTokenId.PHP_PRIVATE
                    || token.id() == PHPTokenId.PHP_PUBLIC
                    || token.id() == PHPTokenId.PHP_PROTECTED) {

                    currentNewLines = 0;
                    lineCommentBefore = (token.id() == PHPTokenId.PHP_LINE_COMMENT);
                    ts.moveNext();
                    token = ts.token();
                    if (token.id() == PHPTokenId.WHITESPACE) {
                        for (int i = 0; i < token.text().length(); i++) {
                            if (token.text().charAt(i) == '\n') {
                                currentNewLines++;
                            }
                        }

                        if (lineCommentBefore && currentNewLines > 0) {
                            // remove all lines, one line is in the line comment
                            replacements.add(new Replacement(ts.offset() + token.length(),
                                token.length(), "")); //NOI18N
                        }
                        else if (currentNewLines > 1) {
                            // keep one new line
                            replacements.add(new Replacement(ts.offset() + token.length(),
                                token.length(), "\n")); //NOI18N
                        }
                    }
                    
                    if (lineCommentBefore) {
                        lineCommentBefore = false;
                    }
                    
                    

                if (ts.movePrevious() && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.WHITESPACE,
                        PHPTokenId.PHP_PRIVATE, PHPTokenId.PHP_PUBLIC, PHPTokenId.PHP_PROTECTED));
                }
                else {
                    break;
                }
            }

            currentNewLines = 0;
            if (ts.moveNext()) {
                int insertPosition = ts.offset();
                token = ts.token();
                if (token.id() == PHPTokenId.WHITESPACE) {
                    for (int i = 0; i < token.text().length(); i++) {
                        if (token.text().charAt(i) == '\n') {
                            currentNewLines++;
                        }
                    }
                }
                String insertText = "";
                int delta = lineCommentBefore ? 0 : 1;
                if (currentNewLines > countOfLines) {
                    for (int i = 0; i < countOfLines + delta; i++) {
                        insertText = insertText + "\n";
                    }
                    replacements.add(new Replacement(ts.offset() + token.length(),
                        token.length(), insertText));
                }
                if (currentNewLines <= countOfLines){
                    for (int i = currentNewLines; i < countOfLines + delta; i++) {
                        insertText = insertText + "\n";
                    }
                    replacements.add(new Replacement(insertPosition, 0, insertText));
                }
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

    private ElemType astNodeToType (ASTNode node) {
        if (node instanceof FieldsDeclaration) return ElemType.FIELD;
        if (node instanceof ClassDeclaration) return ElemType.CLASS;
        if (node instanceof FunctionDeclaration) return ElemType.FUNCTION;
        if (node instanceof MethodDeclaration) return ElemType.FUNCTION;
        if (node instanceof UseStatement) return ElemType.USE;
        if (node instanceof NamespaceDeclaration) return ElemType.NAMESPACE;
        return ElemType.UNKNOWN;
    }



    private int insertLineBeforeAfter(ElemType afterElem, ElemType beforeElem) {

        if (afterElem == beforeElem && (afterElem == ElemType.FIELD || afterElem == ElemType.USE)) {
            return 0;
        }

        if (afterElem == ElemType.CLASS && beforeElem != ElemType.CLASS) {
            afterElem = ElemType.CLASS_HEADER;
        }
        int after = 0;
        int before = 0;

        switch (afterElem) {
            case CLASS : after =  CodeStyle.get(context.document()).getBlankLinesAfterClass(); break;
            case CLASS_HEADER : after =  CodeStyle.get(context.document()).getBlankLinesAfterClassHeader(); break;
            case NAMESPACE : after =  CodeStyle.get(context.document()).getBlankLinesAfterNamespace(); break;
            case USE : after =  CodeStyle.get(context.document()).getBlankLinesAfterUse(); break;
            case FIELD : after =  CodeStyle.get(context.document()).getBlankLinesAfterField(); break;
            case FUNCTION : after =  CodeStyle.get(context.document()).getBlankLinesAfterFunction(); break;

        }

        switch (beforeElem) {
            case CLASS : before =  CodeStyle.get(context.document()).getBlankLinesBeforeClass(); break;
            // small workaround
            case CLASS_HEADER : before =  CodeStyle.get(context.document()).getBlankLinesAfterClassHeader(); break;
            case NAMESPACE : before =  CodeStyle.get(context.document()).getBlankLinesBeforeNamespace(); break;
            case USE : before =  CodeStyle.get(context.document()).getBlankLinesBeforeUse(); break;
            case FIELD : before =  CodeStyle.get(context.document()).getBlankLinesBeforeField(); break;
            case FUNCTION : before =  CodeStyle.get(context.document()).getBlankLinesBeforeFunction(); break;
            case FUNCTION_BEFORE_END: before =  CodeStyle.get(context.document()).getBlankLinesBeforeFunctionEnd(); break;
            case CLASS_BEFORE_END : before =  CodeStyle.get(context.document()).getBlankLinesBeforeClassEnd(); break;
        }

        if (beforeElem == ElemType.CLASS_BEFORE_END) {
            if (afterElem == ElemType.CLASS_HEADER) {
                return Math.max(Math.max(after, before), 1);
            }
            else if (afterElem == ElemType.FUNCTION) {
                return before;
            }
        }

        if (beforeElem == ElemType.FUNCTION_BEFORE_END) {
            if (afterElem == ElemType.FUNCTION) {
                return Math.max(before, 1);
            }
            else {
                return before;
            }
        }

        return Math.max(after, before);
    }

    /**
     *
     * @param node
     * @return True if the next node also influence the number of empty lines
     */
    private boolean isBlankLinesInteresting(ASTNode node) {
        return node != null && (node instanceof ClassDeclaration
                || node instanceof FunctionDeclaration
                || node instanceof NamespaceDeclaration
                || node instanceof UseStatement
                || node instanceof FieldsDeclaration
                || node instanceof MethodDeclaration);
    }

    /**
     * Find previous sibling node
     * @param node
     * @return
     */
    private ASTNode previousNode(ASTNode node) {
        ASTNode previous = null;
        List<ASTNode> path = getPath();
        if (path.get(0) instanceof Block) {
            Block block = (Block) path.get(0);
            List<Statement> statements = block.getStatements();
            int index = 0;

            while (index < statements.size()
                    && statements.get(index).getEndOffset() < node.getStartOffset()) {
                previous = statements.get(index);
                index ++;
            }
            if (previous == null) {
                index = 1;
                while (index < path.size()
                        && !(path.get(index) instanceof ClassDeclaration)) {
                    index++;
                }

                if (index < path.size() && path.get(index) instanceof ClassDeclaration) {
                    previous = path.get(index);
                }
            }
        }
        return previous;
    }

    /**
     * Find next sibling node
     * @param node
     * @return The next sibling node or class declaration if it's the input node
     * the last one in the class
     */
    private ASTNode nextNode(ASTNode node) {
        ASTNode next = null;
        List<ASTNode> path = getPath();
        List<Statement> statements = null;

        if (path.size() == 1 && (path.get(0) instanceof Program)) {
            statements = ((Program)path.get(0)).getStatements();
        }
        else {
            for (ASTNode astNode : path) {
                if (astNode instanceof Block) {
                    statements = ((Block)astNode).getStatements();
                    break;
                }
            }
        }
        if (statements != null) {
            int index = statements.size() - 1;

            while (index > 0
                    && statements.get(index).getStartOffset() > node.getEndOffset()) {
                next = statements.get(index);
                index --;
            }
            if (next == null) {
                index = 1;
                while (index < path.size()
                        && !(path.get(index) instanceof ClassDeclaration)) {
                    index++;
                }

                if (index < path.size() && path.get(index) instanceof ClassDeclaration) {
                    next = path.get(index);
                }
            }
        }
        return next;
    }

    private boolean doNotSplitLine(TokenSequence<PHPTokenId> tokenSequence, boolean fwd) {
        //int orgOffset = tokenSequence.offset();
        boolean retVal = false;
        while (fwd && tokenSequence.moveNext() || !fwd && tokenSequence.movePrevious()) {
            if (WS_AND_COMMENT_TOKENS.contains(tokenSequence.token().id())) {
                if (textContainsBreak(tokenSequence.token().text())) {
                    // the split trigger is already followed by a break
                    retVal = true;
                    break;
                }
            } else {
                // do not break lines in expressions like <?php foo(); ?>, see issue #174595
                retVal = NO_BREAK_B4_TKNS.contains(tokenSequence.token().id());
                if (fwd){
                    tokenSequence.movePrevious();
                } else {
                    tokenSequence.moveNext();
                }
                break; // return false
            }
        }
        
        return retVal;
    }

    private static final boolean textContainsBreak(CharSequence charSequence){

        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) == '\n'){
                return true;
            }
        }

        return false;
    }

    private boolean isWithinUnbreakableRange(int offset){
        // TODO: optimize for n*log(n) complexity
        for (CodeRange codeRange : unbreakableRanges){
            if (codeRange.contains(offset)){
                return true;
            }
        }

        return false;
    }

    List<Replacement> getReplacements() {
        return replacements;
    }

    private TokenSequence<PHPTokenId> tokenSequence(int offset){
        return LexUtilities.getPHPTokenSequence(context.document(), offset);
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
                count ++;
            }
        }
        return count;
    }

    static class Replacement implements Comparable<Replacement>{
        private Integer offset;
        private int length;
        private String newString;

        public Replacement(int offset, int length, String newString) {
            this.offset = offset;
            this.length = length;
            this.newString = newString;
        }

        public int length() {
            return length;
        }

        public String newString() {
            return newString;
        }

        public int offset() {
            return offset;
        }

        @Override
        public int compareTo(Replacement r) {
            return offset.compareTo(r.offset);
        }
    }

    static class CodeRange implements Comparable<CodeRange>{
        private Integer start;
        private Integer end;

        public CodeRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(int offset){
            return offset >= start && offset <= end;
        }

        @Override
        public int compareTo(CodeRange o) {
            int r = start.compareTo(o.start);

            if (r == 0){
                return end.compareTo(o.end);
            }

            return r;
        }
    }

    /**
     * For better work with the nodes, which influence the blank lines algorithm
     */
    private enum ElemType {
        CLASS,
        CLASS_HEADER,
        CLASS_BEFORE_END,
        NAMESPACE,
        USE,
        FUNCTION,
        FUNCTION_BEFORE_END,
        FIELD,
        UNKNOWN;
    }
    
}
