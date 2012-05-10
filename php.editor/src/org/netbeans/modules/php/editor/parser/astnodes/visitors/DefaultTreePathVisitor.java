/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser.astnodes.visitors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class DefaultTreePathVisitor extends DefaultVisitor{
    private final List<ASTNode> path = Collections.synchronizedList(new LinkedList<ASTNode>());
    private final List<ASTNode> unmodifiablePath;

    public DefaultTreePathVisitor() {
        unmodifiablePath = Collections.unmodifiableList(path);
    }

    /**
     * ... reversed order ....
     *
     *
     * @return
     */
    public List<ASTNode> getPath() {
        return unmodifiablePath;
    }

    @Override
    public void visit(NamespaceDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(UseStatement statement) {
        path.add(0, statement);super.visit(statement);path.remove(0);
    }

    @Override
    public void visit(UseStatementPart statementPart) {
        path.add(0, statementPart);super.visit(statementPart);path.remove(0);
    }


    @Override
    public void visit(ArrayAccess node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ArrayCreation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ArrayElement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Assignment node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    protected void addToPath(ASTNode node) {
        path.add(0, node);
    }
    protected void removeFromPath() {
        path.remove(0);
    }

    @Override
    public void visit(ASTError astError) {
        super.visit(astError);
    }

    @Override
    public void visit(BackTickExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Block node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(BreakStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(CastExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(CatchClause node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ConstantDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ClassDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ClassName node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(CloneExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Comment node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ConditionalExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ContinueStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(DeclareStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(DoStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(EchoStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(EmptyStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ExpressionStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FieldAccess node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FieldsDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ForEachStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FormalParameter node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ForStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FunctionInvocation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(FunctionName node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(GlobalStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Identifier node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(IfStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(IgnoreError node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Include node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(InfixExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(InLineHtml node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(InstanceOfExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ListVariable node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(MethodDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(MethodInvocation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ParenthesisExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PostfixExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PrefixExpression node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Program node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Quote node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Reference node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ReflectionVariable node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ReturnStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Scalar node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(StaticConstantAccess node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(StaticStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(SwitchCase node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(SwitchStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ThrowStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(TryStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(UnaryOperation node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(Variable node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(WhileStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(ASTNode node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocBlock node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocTypeTag node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocTag node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocVarTypeTag node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocMethodTag node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(PHPDocNode node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(TraitDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(TraitMethodAliasDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(TraitConflictResolutionDeclaration node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(UseTraitStatement node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(UseTraitStatementPart node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

    @Override
    public void visit(AnonymousObjectVariable node) {
        path.add(0, node);super.visit(node);path.remove(0);
    }

}
