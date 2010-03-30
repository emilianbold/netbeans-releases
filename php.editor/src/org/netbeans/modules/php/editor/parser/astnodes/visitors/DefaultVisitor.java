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

import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Petr Pisl
 */
public class DefaultVisitor implements Visitor {

    public void scan(ASTNode node) {
        if (node != null) {
            node.accept(this);
        }
    }

    public void scan(Iterable<? extends ASTNode> nodes) {
        if (nodes != null) {
            for (ASTNode n : nodes) {
                scan(n);
            }
        }
    }

    public void visit(ArrayAccess node) {
        scan(node.getName());
        scan(node.getIndex());
    }

    public void visit(ArrayCreation node) {
        scan(node.getElements());
    }

    public void visit(ArrayElement node) {
        scan(node.getKey());
        scan(node.getValue());
    }

    public void visit(Assignment node) {
        scan(node.getLeftHandSide());
        scan(node.getRightHandSide());
    }

    public void visit(ASTError astError) {
    }

    public void visit(BackTickExpression node) {
        scan(node.getExpressions());
    }

    public void visit(Block node) {
        scan(node.getStatements());
    }

    public void visit(BreakStatement node) {
        scan(node.getExpression());
    }

    public void visit(CastExpression node) {
        scan(node.getExpression());
    }

    public void visit(CatchClause node) {
        scan(node.getClassName());
        scan(node.getVariable());
        scan(node.getBody());
    }

    public void visit(ConstantDeclaration node) {
        scan(node.getNames());
        scan(node.getInitializers());
    }

    public void visit(ClassDeclaration node) {
        scan(node.getName());
        scan(node.getSuperClass());
        scan(node.getInterfaes());
        scan(node.getBody());
    }

    public void visit(ClassInstanceCreation node) {
        scan(node.getClassName());
        scan(node.ctorParams());
    }

    public void visit(ClassName node) {
        scan(node.getName());
    }

    public void visit(CloneExpression node) {
        scan(node.getExpression());
    }

    public void visit(Comment comment) {
    }

    public void visit(ConditionalExpression node) {
        scan(node.getCondition());
        scan(node.getIfTrue());
        scan(node.getIfFalse());
    }

    public void visit(ContinueStatement node) {
        scan(node.getExpression());
    }

    public void visit(DeclareStatement node) {
        scan(node.getDirectiveNames());
        scan(node.getDirectiveValues());
        scan(node.getBody());
    }

    public void visit(DoStatement node) {
        scan(node.getCondition());
        scan(node.getBody());
    }

    public void visit(EchoStatement node) {
        scan(node.getExpressions());
    }

    public void visit(EmptyStatement emptyStatement) {
    }

    public void visit(ExpressionStatement node) {
        scan(node.getExpression());
    }

    public void visit(FieldAccess node) {
        scan(node.getDispatcher());
        scan(node.getField());
    }

    public void visit(FieldsDeclaration node) {
        scan(node.getFields());
    }

    public void visit(ForEachStatement node) {
        scan(node.getExpression());
        scan(node.getKey());
        scan(node.getValue());
        scan(node.getStatement());
    }

    public void visit(FormalParameter node) {
        scan(node.getParameterName());
        scan(node.getParameterType());
        scan(node.getDefaultValue());
    }

    public void visit(ForStatement node) {
        scan(node.getInitializers());
        scan(node.getConditions());
        scan(node.getUpdaters());
        scan(node.getBody());
    }

    public void visit(FunctionDeclaration node) {
        scan(node.getFunctionName());
        scan(node.getFormalParameters());
        scan(node.getBody());
    }

    public void visit(FunctionInvocation node) {
        scan(node.getFunctionName());
        scan(node.getParameters());
    }

    public void visit(FunctionName node) {
        scan(node.getName());
    }

    public void visit(GlobalStatement node) {
        scan(node.getVariables());
    }

    public void visit(Identifier identifier) {
    }

    public void visit(IfStatement node) {
        scan(node.getCondition());
        scan(node.getTrueStatement());
        scan(node.getFalseStatement());
    }

    public void visit(IgnoreError node) {
        scan(node.getExpression());
    }

    public void visit(Include node) {
        scan(node.getExpression());
    }

    public void visit(InfixExpression node) {
        scan(node.getLeft());
        scan(node.getRight());
    }

    public void visit(InLineHtml inLineHtml) {
    }

    public void visit(InstanceOfExpression node) {
        scan(node.getExpression());
    }

    public void visit(InterfaceDeclaration node) {
        scan(node.getName());
        scan(node.getInterfaes());
        scan(node.getBody());
    }

    public void visit(ListVariable node) {
        scan(node.getVariables());
    }

    public void visit(MethodDeclaration node) {
        scan(node.getFunction());
    }

    public void visit(MethodInvocation node) {
        scan(node.getDispatcher());
        scan(node.getMethod());
    }

    public void visit(ParenthesisExpression node) {
        scan(node.getExpression());
    }

    public void visit(PostfixExpression node) {
        scan(node.getVariable());
    }

    public void visit(PrefixExpression node) {
        scan(node.getVariable());
    }

    public void visit(Program program) {
        scan(program.getStatements());
    }

    public void visit(Quote node) {
        scan(node.getExpressions());
    }

    public void visit(Reference node) {
        scan(node.getExpression());
    }

    public void visit(ReflectionVariable node) {
        scan(node.getName());
    }

    public void visit(ReturnStatement node) {
        scan(node.getExpression());
    }

    public void visit(Scalar scalar) {
    }

    public void visit(SingleFieldDeclaration node) {
        scan(node.getName());
        scan(node.getValue());
    }

    public void visit(StaticConstantAccess node) {
        scan(node.getClassName());
        scan(node.getConstant());
    }

    public void visit(StaticFieldAccess node) {
        scan(node.getClassName());
        scan(node.getField());
    }

    public void visit(StaticMethodInvocation node) {
        scan(node.getClassName());
        scan(node.getMethod());
    }

    public void visit(StaticStatement node) {
        scan(node.getExpressions());
    }

    public void visit(SwitchCase node) {
        scan(node.getValue());
        scan(node.getActions());
    }

    public void visit(SwitchStatement node) {
        scan(node.getExpression());
        scan(node.getBody());
    }

    public void visit(ThrowStatement node) {
        scan(node.getExpression());
    }

    public void visit(TryStatement node) {
        scan(node.getCatchClauses());
        scan(node.getBody());
    }

    public void visit(UnaryOperation node) {
        scan(node.getExpression());
    }

    public void visit(Variable node) {
        scan(node.getName());
    }

    public void visit(WhileStatement node) {
        scan(node.getCondition());
        scan(node.getBody());
    }

    public void visit(ASTNode node) {
    }

    public void visit(PHPDocBlock node) {
        scan(node.getTags());
    }

    public void visit(PHPDocNode node) {
    }

    public void visit(PHPDocTag node) {
    }

    public void visit(PHPDocTypeTag node) {
        scan(node.getTypes());
    }

    public void visit(PHPDocVarTypeTag node) {
        scan(node.getVariable());
        scan(node.getTypes());
    }

    public void visit(PHPDocStaticAccessType node) {
        scan(node.getClassName());
        scan(node.getConstant());
    }

    public void visit(PHPVarComment node) {
        scan(node.getVariable());
    }

    public void visit(NamespaceName namespaceName) {
        scan(namespaceName.getSegments());
    }

    public void visit(NamespaceDeclaration declaration) {
        scan(declaration.getName());
        scan(declaration.getBody());
    }

    public void visit(GotoLabel label) {
        scan(label.getName());
    }

    public void visit(GotoStatement statement) {
        scan(statement.getLabel());
    }

    public void visit(LambdaFunctionDeclaration declaration) {
        scan(declaration.getFormalParameters());
        scan(declaration.getLexicalVariables());
        scan(declaration.getBody());
    }

    public void visit(UseStatement statement) {
        scan(statement.getParts());
    }

    public void visit(UseStatementPart statementPart) {
        scan(statementPart.getName());
        scan(statementPart.getAlias());
    }
}
