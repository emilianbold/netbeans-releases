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

package org.netbeans.modules.php.editor.parser;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BreakStatement;
import org.netbeans.modules.php.editor.parser.astnodes.CastExpression;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.CloneExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EchoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.InLineHtml;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Quote;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Visitor;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalysis implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> semanticHighlights;
    
    public SemanticAnalysis () {
        semanticHighlights = null;
    }
    
    public Map<OffsetRange, ColoringAttributes> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo compilationInfo) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }
        
        PHPParseResult result = getParseResult(compilationInfo);
        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);
        
        result.getProgram().accept(new SemanticHighlightVisitor(highlights));
        
        if (highlights.size() > 0) {
            semanticHighlights = highlights;
        }
        else {
            semanticHighlights = null;
        }
    }
    
    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    private PHPParseResult getParseResult(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            return null;
        } else {
            return ((PHPParseResult)result);
        }
    }
    
    private class SemanticHighlightVisitor implements Visitor {
        
        Map<OffsetRange, ColoringAttributes> highlights;

        public SemanticHighlightVisitor(Map<OffsetRange, ColoringAttributes> highlights) {
            this.highlights = highlights;
        }
        
        private OffsetRange createOffsetRange (ASTNode node) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }
        
        public void visit(ArrayAccess arrayAccess) {
            
        }

        public void visit(ArrayCreation arrayCreation) {
            
        }

        public void visit(ArrayElement arrayElement) {
            
        }

        public void visit(Assignment assignment) {
            
        }

        public void visit(ASTError astError) {
            
        }

        public void visit(BackTickExpression backTickExpression) {
            
        }

        public void visit(Block block) {
            if (isCancelled())
                return;
            
            for (Statement statement : block.getStatements()) {
                statement.accept(this);
            }
        }

        public void visit(BreakStatement breakStatement) {
            
        }

        public void visit(CastExpression castExpression) {
            
        }

        public void visit(CatchClause catchClause) {
            
        }

        public void visit(ClassConstantDeclaration classConstantDeclaration) {
            
        }

        public void visit(ClassDeclaration cldec) {
            if (isCancelled())
                return;
            Identifier name = cldec.getName();
            OffsetRange or = new OffsetRange(name.getStartOffset(), name.getEndOffset());
            highlights.put(or, ColoringAttributes.CLASS);
            cldec.getBody().accept(this);
        }

        public void visit(ClassInstanceCreation classInstanceCreation) {
            
        }

        public void visit(ClassName className) {
            
        }

        public void visit(CloneExpression cloneExpression) {
            
        }

        public void visit(Comment comment) {
            
        }

        public void visit(ConditionalExpression conditionalExpression) {
            
        }

        public void visit(ContinueStatement continueStatement) {
            
        }

        public void visit(DeclareStatement declareStatement) {
            
        }

        public void visit(DoStatement doStatement) {
            
        }

        public void visit(EchoStatement echoStatement) {
            
        }

        public void visit(EmptyStatement emptyStatement) {
            
        }

        public void visit(ExpressionStatement expressionStatement) {
            
        }

        public void visit(FieldAccess fieldAccess) {
            
        }

        public void visit(FieldsDeclaration fieldsDeclaration) {
            
        }

        public void visit(ForEachStatement forEachStatement) {
            
        }

        public void visit(FormalParameter formalParameter) {
            
        }

        public void visit(ForStatement forStatement) {
            
        }

        public void visit(FunctionDeclaration functionDeclaration) {
            
        }

        public void visit(FunctionInvocation functionInvocation) {
            
        }

        public void visit(FunctionName functionName) {
            
        }

        public void visit(GlobalStatement globalStatement) {
            
        }

        public void visit(Identifier identifier) {
            
        }

        public void visit(IfStatement ifStatement) {
            
        }

        public void visit(IgnoreError ignoreError) {
            
        }

        public void visit(Include include) {
            
        }

        public void visit(InfixExpression infixExpression) {
            
        }

        public void visit(InLineHtml inLineHtml) {
            
        }

        public void visit(InstanceOfExpression instanceOfExpression) {
            
        }

        public void visit(InterfaceDeclaration interfaceDeclaration) {
            
        }

        public void visit(ListVariable listVariable) {
            
        }

        public void visit(MethodDeclaration md) {
            Identifier name = md.getFunction().getFunctionName();
            highlights.put(createOffsetRange(name), ColoringAttributes.METHOD);
        }

        public void visit(MethodInvocation methodInvocation) {
            
        }

        public void visit(ParenthesisExpression parenthesisExpression) {
            
        }

        public void visit(PostfixExpression postfixExpression) {
            
        }

        public void visit(PrefixExpression prefixExpression) {
            
        }

        public void visit(Program program) {
            if (isCancelled())
                return;
            for (Statement statement : program.getStatements()) {
                statement.accept(this);
            }
        }

        public void visit(Quote quote) {
            
        }

        public void visit(Reference reference) {
            
        }

        public void visit(ReflectionVariable reflectionVariable) {
            
        }

        public void visit(ReturnStatement returnStatement) {
            
        }

        public void visit(Scalar scalar) {
            
        }

        public void visit(SingleFieldDeclaration singleFieldDeclaration) {
            
        }

        public void visit(StaticConstantAccess classConstantAccess) {
            
        }

        public void visit(StaticFieldAccess staticFieldAccess) {
            
        }

        public void visit(StaticMethodInvocation staticMethodInvocation) {
            
        }

        public void visit(StaticStatement staticStatement) {
            
        }

        public void visit(SwitchCase switchCase) {
            
        }

        public void visit(SwitchStatement switchStatement) {
            
        }

        public void visit(ThrowStatement throwStatement) {
            
        }

        public void visit(TryStatement tryStatement) {
            
        }

        public void visit(UnaryOperation unaryOperation) {
            
        }

        public void visit(Variable variable) {
            
        }

        public void visit(WhileStatement whileStatement) {
            
        }

        public void visit(ASTNode node) {
            
        }
        
    }
}
