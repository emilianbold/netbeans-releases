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
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
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
    private BaseDocument doc;

    public IndentLevelCalculator(Document doc, Map<Position, Integer> indentLevels) {
        this.indentLevels = indentLevels;
        this.doc = (BaseDocument) doc;
        CodeStyle codeStyle = CodeStyle.get(doc);
        indentSize = codeStyle.getIndentSize();
        continuationIndentSize = codeStyle.getContinuationIndentSize();
    }

    @Override
    public void visit(Block node) {

        // do not indent virtual blocks created by namespace declarations
        if (getPath().get(0) instanceof NamespaceDeclaration){
            return;
        }

        // end of hot fix

        indentListOfStatements(node.getStatements());
        super.visit(node);
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
    public void visit(InfixExpression node) {
        indentContinuationWithinStatement(node);
        // do not call super.visit()
        // to avoid reccurency!
    }

    @Override
    public void visit(ExpressionStatement node) {
        indentContinuationWithinStatement(node);
        // do not call super.visit()
        // to avoid reccurency!
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
        // AST info does not allow to distinguish
        // between "if" and "elseif"
        if (node instanceof IfStatement) {
            String ELSE_IF = "elseif"; //NOI18N
            try {
                if (doc.getLength() > node.getStartOffset() + ELSE_IF.length()
                        && ELSE_IF.equals(doc.getText(node.getStartOffset(),
                        ELSE_IF.length()))) {
                    return;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
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
            
            if (v >= 0){
                r = v;
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

            if (v >= 0){
                r = v;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return r;
    }

    private void addIndentLevel(int offset, int indent){
        Integer existingIndent = getExistingIndentLevel(offset);

        int newIndent = existingIndent == null ? indent : indent + existingIndent;
        try {
            indentLevels.put(doc.createPosition(offset), newIndent);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //TODO optimize for performace
    private Integer getExistingIndentLevel(int offset){
        for (Position pos : indentLevels.keySet()){
            if (pos.getOffset() == offset){
                indentLevels.get(pos);
            }
        }

        return 0;
    }
}