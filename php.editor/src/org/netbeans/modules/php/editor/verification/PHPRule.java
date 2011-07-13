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

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
    
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
    }

    public void visit(BreakStatement breakStatement) {
    }

    public void visit(CastExpression castExpression) {
    }

    public void visit(CatchClause catchClause) {
    }

    public void visit(ConstantDeclaration classConstantDeclaration) {
    }

    public void visit(ClassDeclaration classDeclaration) {
    }
    
    public void leavingClassDeclaration(ClassDeclaration classDeclaration) {
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

    public void visit(NamespaceName namespaceName) {
    }

    public void visit(NamespaceDeclaration declaration) {
    }

    public void visit(GotoStatement statement) {
    }

    public void visit(GotoLabel label) {
    }

    public void visit(LambdaFunctionDeclaration declaration) {
    }

    public void visit(UseStatement statement) {
    }

    public void visit(UseStatementPart statementPart) {
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

    public void visit(MethodDeclaration methodDeclaration) {
    }

    public void visit(MethodInvocation methodInvocation) {
    }

    public void visit(ParenthesisExpression parenthesisExpression) {
    }

    public void visit(PHPDocBlock phpDocBlock) {
    }

    public void visit(PHPDocTag phpDocTag) {
    }

    public void visit(PHPDocNode phpDocNode) {
    }
    
    public void visit(PHPDocTypeNode phpDocNode) {
    }

    public void visit(PHPDocTypeTag node) {
    }

    public void visit(PHPDocVarTypeTag node) {
    }

    public void visit(PHPDocStaticAccessType node) {
    }

    public void visit(PHPVarComment node) {
    }

    @Override
    public void visit(PHPDocMethodTag node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(PostfixExpression postfixExpression) {
    }

    public void visit(PrefixExpression prefixExpression) {
    }

    public void visit(Program program) {
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
