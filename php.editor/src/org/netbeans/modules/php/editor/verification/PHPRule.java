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
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
abstract class PHPRule implements AstRule, UserConfigurableRule, Visitor {

    protected PHPRuleContext context;
    protected List<Hint> result = new LinkedList<Hint>();

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public Set<? extends Object> getKinds() {
        return Collections.singleton(PHPHintsProvider.FIRST_PASS_HINTS);
    }

    final void setContext(PHPRuleContext context) {
        this.context = context;
    }

    public List<Hint> getResult() {
        return result;
    }

    public void resetResult() {
        result.clear();
        context = null; // just to detect errors
    }

    protected void addResult(Hint hint) {
        result.add(hint);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
    }

    @Override
    public void visit(ArrayCreation arrayCreation) {
    }

    @Override
    public void visit(ArrayElement arrayElement) {
    }

    @Override
    public void visit(Assignment assignment) {
    }

    @Override
    public void visit(ASTError astError) {
    }

    @Override
    public void visit(BackTickExpression backTickExpression) {
    }

    @Override
    public void visit(Block block) {
    }

    @Override
    public void visit(BreakStatement breakStatement) {
    }

    @Override
    public void visit(CastExpression castExpression) {
    }

    @Override
    public void visit(CatchClause catchClause) {
    }

    @Override
    public void visit(ConstantDeclaration classConstantDeclaration) {
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
    }

    public void leavingClassDeclaration(ClassDeclaration classDeclaration) {
    }

    @Override
    public void visit(ClassInstanceCreation classInstanceCreation) {
    }

    @Override
    public void visit(ClassName className) {
    }

    @Override
    public void visit(CloneExpression cloneExpression) {
    }

    @Override
    public void visit(Comment comment) {
    }

    @Override
    public void visit(ConditionalExpression conditionalExpression) {
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
    }

    @Override
    public void visit(DeclareStatement declareStatement) {
    }

    @Override
    public void visit(DoStatement doStatement) {
    }

    @Override
    public void visit(EchoStatement echoStatement) {
    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
    }

    @Override
    public void visit(FieldsDeclaration fieldsDeclaration) {
    }

    @Override
    public void visit(ForEachStatement forEachStatement) {
    }

    @Override
    public void visit(FormalParameter formalParameter) {
    }

    @Override
    public void visit(ForStatement forStatement) {
    }

    @Override
    public void visit(FunctionDeclaration functionDeclaration) {
    }

    @Override
    public void visit(FunctionInvocation functionInvocation) {
    }

    @Override
    public void visit(FunctionName functionName) {
    }

    @Override
    public void visit(GlobalStatement globalStatement) {
    }

    @Override
    public void visit(Identifier identifier) {
    }

    @Override
    public void visit(NamespaceName namespaceName) {
    }

    @Override
    public void visit(NamespaceDeclaration declaration) {
    }

    @Override
    public void visit(GotoStatement statement) {
    }

    @Override
    public void visit(GotoLabel label) {
    }

    @Override
    public void visit(LambdaFunctionDeclaration declaration) {
    }

    @Override
    public void visit(UseStatement statement) {
    }

    @Override
    public void visit(UseStatementPart statementPart) {
    }

    @Override
    public void visit(IfStatement ifStatement) {
    }

    @Override
    public void visit(IgnoreError ignoreError) {
    }

    @Override
    public void visit(Include include) {
    }

    @Override
    public void visit(InfixExpression infixExpression) {
    }

    @Override
    public void visit(InLineHtml inLineHtml) {
    }

    @Override
    public void visit(InstanceOfExpression instanceOfExpression) {
    }

    @Override
    public void visit(InterfaceDeclaration interfaceDeclaration) {
    }

    @Override
    public void visit(ListVariable listVariable) {
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
    }

    @Override
    public void visit(MethodInvocation methodInvocation) {
    }

    @Override
    public void visit(ParenthesisExpression parenthesisExpression) {
    }

    @Override
    public void visit(PHPDocBlock phpDocBlock) {
    }

    @Override
    public void visit(PHPDocTag phpDocTag) {
    }

    @Override
    public void visit(PHPDocNode phpDocNode) {
    }

    @Override
    public void visit(PHPDocTypeNode phpDocNode) {
    }

    @Override
    public void visit(PHPDocTypeTag node) {
    }

    @Override
    public void visit(PHPDocVarTypeTag node) {
    }

    @Override
    public void visit(PHPDocStaticAccessType node) {
    }

    @Override
    public void visit(PHPVarComment node) {
    }

    @Override
    public void visit(PHPDocMethodTag node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(PostfixExpression postfixExpression) {
    }

    @Override
    public void visit(PrefixExpression prefixExpression) {
    }

    @Override
    public void visit(Program program) {
    }

    @Override
    public void visit(Quote quote) {
    }

    @Override
    public void visit(Reference reference) {
    }

    @Override
    public void visit(ReflectionVariable reflectionVariable) {
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
    }

    @Override
    public void visit(Scalar scalar) {
    }

    @Override
    public void visit(SingleFieldDeclaration singleFieldDeclaration) {
    }

    @Override
    public void visit(StaticConstantAccess classConstantAccess) {
    }

    @Override
    public void visit(StaticFieldAccess staticFieldAccess) {
    }

    @Override
    public void visit(StaticMethodInvocation staticMethodInvocation) {
    }

    @Override
    public void visit(StaticStatement staticStatement) {
    }

    @Override
    public void visit(SwitchCase switchCase) {
    }

    @Override
    public void visit(SwitchStatement switchStatement) {
    }

    @Override
    public void visit(ThrowStatement throwStatement) {
    }

    @Override
    public void visit(TryStatement tryStatement) {
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {
    }

    @Override
    public void visit(Variable variable) {
    }

    @Override
    public void visit(WhileStatement whileStatement) {
    }

    @Override
    public void visit(ASTNode node) {
    }
}
