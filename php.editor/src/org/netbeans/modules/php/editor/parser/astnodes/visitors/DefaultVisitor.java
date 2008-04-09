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
package org.netbeans.modules.php.editor.parser.astnodes.visitors;

import java.util.List;
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
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
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
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
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
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.Visitor;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;

/**
 *
 * @author Petr Pisl
 */
public class DefaultVisitor implements Visitor {

    public void visit(ArrayAccess node) {
        node.getName().accept(this);
        node.getIndex().accept(this);
    }

    public void visit(ArrayCreation node) {
        for (ArrayElement el : node.getElements()) {
            el.accept(this);
        }
    }

    public void visit(ArrayElement node) {
        if (node.getKey() != null) {
            node.getKey().accept(this);
        }
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
    }

    public void visit(Assignment node) {
        node.getLeftHandSide().accept(this);
        node.getRightHandSide().accept(this);
    }

    public void visit(ASTError astError) {
    }

    public void visit(BackTickExpression node) {
        if (node.getExpressions() != null) {
            for (Expression expr : node.getExpressions()) {
                expr.accept(this);
            }
        }
    }

    public void visit(Block node) {
        for (Statement statement : node.getStatements()) {
            statement.accept(this);
        }
    }

    public void visit(BreakStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(CastExpression node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(CatchClause node) {
        node.getClassName().accept(this);
        node.getVariable().accept(this);
        node.getBody().accept(this);
    }

    public void visit(ClassConstantDeclaration node) {
        if (node.getNames() != null) {
            for (Identifier iden : node.getNames()) {
                iden.accept(this);
            }
        }
        if (node.getInitializers() != null) {
            for (Expression expr : node.getInitializers()) {
                expr.accept(this);
            }
        }
    }

    public void visit(ClassDeclaration node) {
        if (node.getName() != null) {
            node.getName().accept(this);
        }
        if (node.getSuperClass() != null) {
            node.getSuperClass().accept(this);
        }
        if (node.getInterfaes() != null) {
            for (Identifier identifier : node.getInterfaes()) {
                identifier.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(ClassInstanceCreation node) {
        node.getClassName().accept(this);
        if (node.ctorParams() != null) {
            for (Expression expr : node.ctorParams()) {
                expr.accept(this);
            }
        }
    }

    public void visit(ClassName node) {
        node.getName().accept(this);
    }

    public void visit(CloneExpression node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(Comment comment) {
    }

    public void visit(ConditionalExpression node) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        if (node.getIfTrue() != null) {
            node.getIfTrue().accept(this);
        }
        if (node.getIfFalse() != null) {
            node.getIfFalse().accept(this);
        }
    }

    public void visit(ContinueStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(DeclareStatement node) {
        if (node.getDirectiveNames() != null) {
            for (Identifier identifier : node.getDirectiveNames()) {
                identifier.accept(this);
            }
        }
        if (node.getDirectiveValues() != null) {
            for (Expression val : node.getDirectiveValues()) {
                val.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(DoStatement node) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(EchoStatement node) {
        for (Expression expression : node.getExpressions()) {
            expression.accept(this);
        }
    }

    public void visit(EmptyStatement emptyStatement) {
    }

    public void visit(ExpressionStatement node) {
        node.getExpression().accept(this);
    }

    public void visit(FieldAccess node) {
        node.getDispatcher().accept(this);
        node.getField().accept(this);
    }

    public void visit(FieldsDeclaration node) {
        if (node.getFields() != null) {
            for (SingleFieldDeclaration decl : node.getFields()) {
                decl.accept(this);
            }
        }
    }

    public void visit(ForEachStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
        if (node.getKey() != null) {
            node.getKey().accept(this);
        }
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
        if (node.getStatement() != null) {
            node.getStatement().accept(this);
        }
    }

    public void visit(FormalParameter node) {
        if (node.getParameterName() != null) {
            node.getParameterName().accept(this);
        }

        if (node.getParameterType() != null) {
            node.getParameterType().accept(this);
        }

        if (node.getDefaultValue() != null) {
            node.getDefaultValue().accept(this);
        }
    }

    public void visit(ForStatement node) {
        if (node.getInitializers() != null) {
            for (Expression expr : node.getInitializers()) {
                expr.accept(this);
            }
        }
        if (node.getConditions() != null) {
            for (Expression expr : node.getConditions()) {
                expr.accept(this);
            }
        }
        if (node.getUpdaters() != null) {
            for (Expression expr : node.getUpdaters()) {
                expr.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(FunctionDeclaration node) {
        if (node.getFunctionName() != null) {
            node.getFunctionName().accept(this);
        }
        if (node.getFormalParameters() != null) {
            for (FormalParameter parameter : node.getFormalParameters()) {
                parameter.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(FunctionInvocation node) {
        node.getFunctionName().accept(this);
        if (node.getParameters() != null) {
            for (Expression expr : node.getParameters()) {
                expr.accept(this);
            }
        }
    }

    public void visit(FunctionName node) {
        node.getName().accept(this);
    }

    public void visit(GlobalStatement node) {
        if (node.getVariables() != null) {
            for (Variable var : node.getVariables()) {
                var.accept(this);
            }
        }
    }

    public void visit(Identifier identifier) {
    }

    public void visit(IfStatement node) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        if (node.getTrueStatement() != null) {
            node.getTrueStatement().accept(this);
        }
        if (node.getFalseStatement() != null) {
            node.getFalseStatement().accept(this);
        }
    }

    public void visit(IgnoreError node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(Include node) {
        node.getExpression().accept(this);
    }

    public void visit(InfixExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
    }

    public void visit(InLineHtml inLineHtml) {
    }

    public void visit(InstanceOfExpression node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(InterfaceDeclaration node) {
        if (node.getName() != null) {
            node.getName().accept(this);
        }
        if (node.getInterfaes() != null) {
            for (Identifier identifier : node.getInterfaes()) {
                identifier.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(ListVariable node) {
        if (node.getVariables() != null) {
            for (VariableBase var : node.getVariables()) {
                var.accept(this);
            }
        }
    }

    public void visit(MethodDeclaration node) {
        node.getFunction().accept(this);
    }

    public void visit(MethodInvocation node) {
        node.getDispatcher().accept(this);
        node.getMethod().accept(this);
    }

    public void visit(ParenthesisExpression node) {
        node.getExpression().accept(this);
    }

    public void visit(PostfixExpression node) {
        if (node.getVariable() != null) {
            node.getVariable().accept(this);
        }
    }

    public void visit(PrefixExpression node) {
        node.getVariable().accept(this);
    }

    public void visit(Program program) {
        List<Statement> statements = program.getStatements();
        for (Statement statement : statements) {
            statement.accept(this);
        }
    }

    public void visit(Quote node) {
        for (Expression expression : node.getExpressions()) {
            expression.accept(this);
        }
    }

    public void visit(Reference node) {
        node.getExpression().accept(this);
    }

    public void visit(ReflectionVariable node) {
        node.getName().accept(this);
    }

    public void visit(ReturnStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(Scalar scalar) {
    }

    public void visit(SingleFieldDeclaration node) {
        if (node.getName() != null) {
            node.getName().accept(this);
        }
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
    }

    public void visit(StaticConstantAccess node) {
        if (node.getClassName() != null) {
            node.getClassName().accept(this);
        }
        if (node.getConstant() != null) {
            node.getConstant().accept(this);
        }
    }

    public void visit(StaticFieldAccess node) {
        if (node.getClassName() != null) {
            node.getClassName().accept(this);
        }
        if (node.getField() != null) {
            node.getField().accept(this);
        }
    }

    public void visit(StaticMethodInvocation node) {
        node.getClassName().accept(this);
        node.getMethod().accept(this);
    }

    public void visit(StaticStatement node) {
        if (node.getExpressions() != null) {
            for (Expression ex : node.getExpressions()) {
                ex.accept(this);
            }
        }
    }

    public void visit(SwitchCase node) {
        if (node.getValue() != null) {
            node.getValue().accept(this);
        }
        if (node.getActions() != null) {
            for (Statement st : node.getActions()) {
                st.accept(this);
            }
        }
    }

    public void visit(SwitchStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(ThrowStatement node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(TryStatement node) {
        if (node.getCatchClauses() != null) {
            for (CatchClause cc : node.getCatchClauses()) {
                cc.accept(this);
            }
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(UnaryOperation node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
    }

    public void visit(Variable node) {
        node.getName().accept(this);
    }

    public void visit(WhileStatement node) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
    }

    public void visit(ASTNode node) {
    }

    public void visit(PHPDocBlock phpDocBlock) {
    }
}
