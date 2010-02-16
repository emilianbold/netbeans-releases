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

import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class IndentLevelCalculator extends DefaultTreePathVisitor {

    private Map<Position, Integer> indentLevels;
    private int indentSize;
    private int continuationIndentSize;
    // contains informataion about indentation items in array deaclaration
    private int itemsInArrayDeclarationSize;

    private BaseDocument doc;
    private Collection<Class<? extends ASTNode>>PARENTS_WITHOUT_CONT_INDENT = Arrays.asList(
            Assignment.class, FieldsDeclaration.class, ReturnStatement.class,
            InfixExpression.class, ExpressionStatement.class, SingleFieldDeclaration.class,
            FunctionInvocation.class);

    public IndentLevelCalculator(Document doc, Map<Position, Integer> indentLevels) {
        this.indentLevels = indentLevels;
        this.doc = (BaseDocument) doc;
        CodeStyle codeStyle = CodeStyle.get(doc);
        indentSize = codeStyle.getIndentSize();
        continuationIndentSize = codeStyle.getContinuationIndentSize();
        itemsInArrayDeclarationSize = codeStyle.getItemsInArrayDeclarationIndentSize();
    }

    @Override
    public void visit(Block node) {
        super.visit(node);
        ASTNode parent = getPath().get(0);
        if (parent instanceof NamespaceDeclaration){
            return;
        }

	CodeStyle.BracePlacement openingBraceStyle = CodeStyle.BracePlacement.PRESERVE_EXISTING;
	if (parent instanceof ClassDeclaration) {
	    openingBraceStyle = CodeStyle.get(doc).getClassDeclBracePlacement();
	}
	else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
	    openingBraceStyle = CodeStyle.get(doc).getMethodDeclBracePlacement();
	} else if (parent instanceof IfStatement) {
	    openingBraceStyle = CodeStyle.get(doc).getIfBracePlacement();
	} else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
	    openingBraceStyle = CodeStyle.get(doc).getForBracePlacement();
	} else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
	    openingBraceStyle = CodeStyle.get(doc).getWhileBracePlacement();
	} else if (parent instanceof SwitchStatement) {
	    openingBraceStyle = CodeStyle.get(doc).getSwitchBracePlacement();
	} else if (parent instanceof CatchClause || parent instanceof TryStatement) {
	    openingBraceStyle = CodeStyle.get(doc).getCatchBracePlacement();
	} else {
	    openingBraceStyle = CodeStyle.get(doc).getOtherBracePlacement();
	}

        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, node.getStartOffset());
        ts.move(node.getStartOffset());
        if (ts.movePrevious() && ts.moveNext()) {
	    // handling alternative syntax
	    while (!(ts.token().id() == PHPTokenId.PHP_CURLY_OPEN
		    || (ts.token().id() == PHPTokenId.PHP_TOKEN && ":".equals(ts.token().text().toString())))
		    && ts.moveNext()) {
		LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_TOKEN));
	    }
	    if (openingBraceStyle != CodeStyle.BracePlacement.NEW_LINE_INDENTED) {
		ts.moveNext();
	    }
	    addIndentLevel(ts.offset(), indentSize);

	    ts.move(node.getEndOffset());
	    ts.movePrevious();
	    ts.movePrevious();
	    LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_CURLY_CLOSE, PHPTokenId.PHP_TOKEN,
		    PHPTokenId.PHP_ENDIF, PHPTokenId.PHP_ENDFOR, PHPTokenId.PHP_ENDFOREACH,
		    PHPTokenId.PHP_ENDSWITCH, PHPTokenId.PHP_ENDWHILE));
	    if (ts.token().id() == PHPTokenId.PHP_CURLY_CLOSE
		    || ts.token().id() == PHPTokenId.PHP_TOKEN
		    || ts.token().id() == PHPTokenId.PHP_ENDIF
		    || ts.token().id() == PHPTokenId.PHP_ENDFOR
		    || ts.token().id() == PHPTokenId.PHP_ENDFOREACH
		    || ts.token().id() == PHPTokenId.PHP_ENDSWITCH
		    || ts.token().id() == PHPTokenId.PHP_ENDWHILE) {
		if (ts.token().id() == PHPTokenId.PHP_TOKEN && ":".equals(ts.token().text().toString())) {
		    ts.movePrevious();
		    LexUtilities.findPrevious(ts, WSTransformer.WS_AND_COMMENT_TOKENS);
		}
		if (openingBraceStyle != CodeStyle.BracePlacement.NEW_LINE_INDENTED) {
		    ts.movePrevious();
		}
		addIndentLevel(ts.offset() + ts.token().length(), -1 * indentSize);
	    }
	    
	    
	}
    }

    @Override
    public void visit(IfStatement node) {
        indentNonBlockStatement(node.getFalseStatement());
        indentNonBlockStatement(node.getTrueStatement());

        super.visit(node);
    }

    @Override
    public void visit(DoStatement node) {
        indentNonBlockStatement(node.getBody());
        super.visit(node);
    }

    @Override
    public void visit(ForEachStatement node) {
        indentNonBlockStatement(node.getStatement());
        super.visit(node);
    }

    @Override
    public void visit(ForStatement node) {
        indentNonBlockStatement(node.getBody());
        super.visit(node);
    }

    @Override
    public void visit(WhileStatement node) {
        indentNonBlockStatement(node.getBody());
        super.visit(node);
    }

    @Override
    public void visit(ArrayCreation node) {
        Class parentClass = getPath().get(0).getClass();

        if (node.getElements().size() > 0) {
            int start = node.getElements().get(0).getStartOffset();
            ArrayElement lastElem = node.getElements().get(node.getElements().size() - 1);
            int end = lastElem.getEndOffset();

            if ((parentClass == FunctionInvocation.class
		    && ((FunctionInvocation)getPath().get(0)).getParameters().size() > 1)
		    || !PARENTS_WITHOUT_CONT_INDENT.contains(parentClass)){
//	    if (!PARENTS_WITHOUT_CONT_INDENT.contains(parentClass)){
                addIndentLevel(start, itemsInArrayDeclarationSize);
                addIndentLevel(end, -1 * itemsInArrayDeclarationSize);
            }
            else {
                addIndentLevel(start, itemsInArrayDeclarationSize - continuationIndentSize);
                addIndentLevel(end, continuationIndentSize - itemsInArrayDeclarationSize );
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(InfixExpression node) {
        Class parentClass = getPath().get(0).getClass();

        if (!PARENTS_WITHOUT_CONT_INDENT.contains(parentClass)){
            indentContinuationWithinStatement(node);
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionInvocation node) {
	Class parentClass = getPath().get(0).getClass();

        if (!PARENTS_WITHOUT_CONT_INDENT.contains(parentClass)){
            indentContinuationWithinStatement(node);
        }

        super.visit(node);
    }

    @Override
    public void visit(ExpressionStatement node) {
        Class parentClass = getPath().get(0).getClass();

        if (!PARENTS_WITHOUT_CONT_INDENT.contains(parentClass)){
            indentContinuationWithinStatement(node);
        }

        super.visit(node);
    }

    @Override
    public void visit(ReturnStatement node) {
        indentContinuationWithinStatement(node);
        super.visit(node);
    }

    @Override
    public void visit(FieldsDeclaration node) {
        indentContinuationWithinStatement(node);
         super.visit(node);
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        // TODO: this is hack for #179877
        //super.visit(node);
    }

    @Override
    public void visit(Program node) {
        for (Comment comment : node.getComments()) {
            // TODO: optimize performance by checking bounds
            if (comment instanceof PHPDocBlock) {
                try {
                    int endOfFirstLine = Utilities.getRowEnd(doc, comment.getStartOffset());

                    if (endOfFirstLine < comment.getEndOffset()) {
                        addIndentLevel(endOfFirstLine, 1);
                        addIndentLevel(comment.getEndOffset(), -1);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(SwitchCase node) {
        indentListOfStatements(node.getActions());
        super.visit(node);
    }

    private void indentContinuationWithinStatement(ASTNode node){
        try {
            int endOfFirstLine = Utilities.getRowEnd(doc, node.getStartOffset());

            if (endOfFirstLine < node.getEndOffset()){
                addIndentLevel(endOfFirstLine + 1, continuationIndentSize);

                // if the last line of the expression is only a closing brace(s)
                // do not indent it. E.g.
                // foo($a1,
                //     $a2,
                // ); // - this line should not be indented
                TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, node.getEndOffset());
                int end = node.getEndOffset();

                if (ts != null){
                    ts.move(node.getEndOffset());

                    do {
                        ts.movePrevious();
                        
                    } while (indentContinuationWithinStatement_skipToken(ts.token()));

                    end = ts.offset() + ts.token().length();
                }

                addIndentLevel(end, -1 * continuationIndentSize);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static boolean indentContinuationWithinStatement_skipToken(Token token){
        Object tokenID = token.id();

        if (tokenID == PHPTokenId.PHP_SEMICOLON){
            return true;
        }

        if (tokenID == PHPTokenId.PHP_TOKEN){
            if (")".equals(token.text().toString())){ //NOI18N
                return true;
            }
        }
        return false;
    }

    private void indentListOfStatements(List<Statement> stmts) {
        if (stmts.size() > 0){
            ASTNode firstNode = stmts.get(0);
            ASTNode lastNode = stmts.get(stmts.size() - 1);
            int start = firstNonWSBwd(doc, firstNode.getStartOffset()) + 1;
            int end = firstNonWSFwd(doc, lastNode.getEndOffset()) - 1;
            addIndentLevel(start, indentSize);
            addIndentLevel(end, -1 * indentSize);
        }
    }

    @Override
    public void visit(FunctionDeclaration node) {
        int paramCount = node.getFormalParameters().size();

        if (paramCount > 0){
            FormalParameter firstParam = node.getFormalParameters().get(0);
            FormalParameter lastParam = node.getFormalParameters().get(paramCount -1);
            addIndentLevel(firstParam.getStartOffset(), continuationIndentSize);
            addIndentLevel(lastParam.getEndOffset(), -1 * continuationIndentSize);
        }

        super.visit(node);
    }

    private void indentNonBlockStatement(ASTNode node) {
        if (node == null || node instanceof Block) {
            return;
        }

        // BEGIN AN UGLY HACK
        if (node instanceof IfStatement) {
            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, node.getStartOffset());
            ts.move(node.getStartOffset());
            ts.moveNext();

            if (ts.token().id() == PHPTokenId.PHP_ELSEIF){
                return;
            } else if (ts.token().id() == PHPTokenId.PHP_IF) {
                if (ts.movePrevious()){
                    if (ts.token().id() == PHPTokenId.WHITESPACE){
                        if (ts.movePrevious()){
                            if (ts.token().id() == PHPTokenId.PHP_ELSE){
                                return;
                            }
                        }
                    }
                }
            }
        }
        // END AN UGLY HACK

        int start = firstNonWSBwd(doc, node.getStartOffset()) + 1;
        int end = firstNonWSFwd(doc, node.getEndOffset()) - 1;
        addIndentLevel(start, indentSize);
        addIndentLevel(end, -1 * indentSize);
    }

    private static int firstNonWSBwd(BaseDocument doc, int offset){
        int r = offset;
        try {
            int v = Utilities.getFirstNonWhiteBwd(doc, offset);
            int rs = Utilities.getRowStart(doc, offset);
            
            if (v >= 0){
                r = v;
            }

            if (r < rs){
                r = rs - 1;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return r;
    }

    private static  int firstNonWSFwd(BaseDocument doc, int offset){
        int r = offset;
        try {
            int v = Utilities.getFirstNonWhiteFwd(doc, offset);
            int re = Utilities.getRowEnd(doc, offset);

            if (v >= 0){
                r = v;
            }

            if (r > re){
                r = re + 1;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return r;
    }

    private void addIndentLevel(int offset, int indent){
        //Integer existingIndent = getExistingIndentLevel(offset);

        //int newIndent = existingIndent == null ? indent : indent + existingIndent;
        try {
            indentLevels.put(doc.createPosition(offset), indent);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //TODO optimize for performace
    private Integer getExistingIndentLevel(int offset){
        for (Position pos : indentLevels.keySet()){
            if (pos.getOffset() == offset){
                return indentLevels.get(pos);
            }
        }

        return 0;
    }
}
