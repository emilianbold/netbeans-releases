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
 * accompanied this code. If applicable, append the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by appending
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you append GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author petr
 */
public class PrintASTVisitor implements Visitor {

    private StringBuffer buffer;
    private final static String NEW_LINE = "\n";
    private final static String TAB = "    ";
    private int indent;

    public String printTree(ASTNode node) {
        buffer = new StringBuffer();
        indent = 0;
        node.accept(this);
        return buffer.toString();
    }

    private void addOffsets(ASTNode node) {
        buffer.append(" start='").append(node.getStartOffset()).append("' end='").append(node.getEndOffset()).append("'");
    }

    private void addIndentation() {
        for (int i = 0; i < indent; i++) {
            buffer.append(TAB);
        }
    }

    private void addSimpleElement(String name, ASTNode node) {
        addIndentation();
        buffer.append("<").append(name);
        addOffsets(node);
        buffer.append("/>").append(NEW_LINE);
    }

    private void openElement(String name, ASTNode node, boolean close) {
        addIndentation();
        buffer.append("<").append(name);
        addOffsets(node);
        if (close) {
            buffer.append(">").append(NEW_LINE);
        }
    }

    /**
     * Formats a given string to an XML file
     * @param input 
     * @return String the formatted string
     */
    protected static String getXmlStringValue(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
        return escapedString;
    }

    private void closeElement(String name) {
        addIndentation();
        buffer.append("</").append(name).append(">").append(NEW_LINE);
    }

    private void addNodeDescription(String name, ASTNode node, boolean newline) {
        addIndentation();
        buffer.append(name);
        addOffsets(node);
        if (newline) {
            buffer.append(NEW_LINE);
        }
    }

    public void visit(ArrayAccess arrayAccess) {
        addIndentation();
        buffer.append("### Not supported yet arrayAccess.\n");
    }

    public void visit(ArrayCreation arrayCreation) {
        addIndentation();
        buffer.append("### Not supported yet ArrayCreation.\n");
    }

    public void visit(ArrayElement arrayElement) {

        addIndentation();
        buffer.append("### Not supported yet ArrayElement.\n");
    }

    public void visit(Assignment assignment) {
        addNodeDescription("Assignment", assignment, false);
        buffer.append(" operator: '").append(assignment.getOperator().name()).append("'").append(NEW_LINE);
        indent++;
        assignment.getLeftHandSide().accept(this);
        assignment.getRightHandSide().accept(this);
        indent--;
    }

    public void visit(ASTError astError) {
        addNodeDescription("ASTError", astError, true);
    }

    public void visit(BackTickExpression backTickExpression) {
        addIndentation();
        buffer.append("### Not supported yet BackTickExpression.\n");
    }

    public void visit(Block block) {
        openElement("Block", block, false);
        buffer.append(" isCurly='").append(block.isCurly()).append("'>").append(NEW_LINE);
        indent++;
        for (ASTNode statement : block.getStatements()) {
            statement.accept(this);
        }
        indent--;
        closeElement("Block");
    }

    public void visit(BreakStatement breakStatement) {
        addIndentation();
        buffer.append("### Not supported yet BreakStatement.\n");
    }

    public void visit(CastExpression castExpression) {
        addIndentation();
        buffer.append("### Not supported yet CastExpression.\n");
    }

    public void visit(CatchClause catchClause) {
        addIndentation();
        buffer.append("### Not supported yet CatchClause.\n");
    }

    public void visit(ClassConstantDeclaration classConstantDeclaration) {
        addIndentation();
        buffer.append("### Not supported yet ClassConstantDeclaration.\n");
    }

    public void visit(ClassDeclaration classDeclaration) {
        addIndentation();
        buffer.append("<ClassDeclaration");
        addOffsets(classDeclaration);
        buffer.append(" modifier='").append(classDeclaration.getModifier().name()).append("'>").append(NEW_LINE);
        indent++;
        addIndentation();
        buffer.append("<ClassName>").append(NEW_LINE);
        indent++;
        classDeclaration.getName().accept(this);
        indent--;
        addIndentation();
        buffer.append("</ClassName>").append(NEW_LINE);

        addIndentation();
        buffer.append("<SuperClassName>").append(NEW_LINE);
        if (classDeclaration.getSuperClass() != null) {
            indent++;
            classDeclaration.getSuperClass().accept(this);
            indent--;
        }
        addIndentation();
        buffer.append("</SuperClassName>").append(NEW_LINE);

        addIndentation();
        buffer.append("<Interfaces>").append(NEW_LINE);
        indent++;
        for (Identifier identifier : classDeclaration.getInterfaes()) {
            identifier.accept(this);
        }
        indent--;
        addIndentation();
        buffer.append("</Interfaces>").append(NEW_LINE);
        classDeclaration.getBody().accept(this);

        indent--;
        addIndentation();
        buffer.append("</ClassDeclaration>").append(NEW_LINE);
    }

    public void visit(ClassInstanceCreation classInstanceCreation) {
        addIndentation();
        buffer.append("### Not supported yet ClassInstanceCreation.\n");
    }

    public void visit(ClassName className) {
        addIndentation();
        buffer.append("<ClassName");
        addOffsets(className);
        buffer.append(">").append(NEW_LINE);
        indent++;
        className.getName().accept(this);
        indent--;
        addIndentation();
        buffer.append("</ClassName>").append(NEW_LINE);
    }

    public void visit(CloneExpression cloneExpression) {
        addIndentation();
        buffer.append("### Not supported yet CloneExpression.\n");
    }

    public void visit(Comment comment) {
        addNodeDescription("Comment", comment, false);
	buffer.append(" commentType='").append(comment.getCommentType()).append("'/>").append(NEW_LINE); 
    }

    public void visit(ConditionalExpression conditionalExpression) {
        addIndentation();
        buffer.append("### Not supported yet ConditionalExpression.\n");
    }

    public void visit(ContinueStatement continueStatement) {
        addIndentation();
        buffer.append("### Not supported yet ContinueStatement.\n");
    }

    public void visit(DeclareStatement declareStatement) {
        addIndentation();
        buffer.append("### Not supported yet DeclareStatement.\n");
    }

    public void visit(DoStatement doStatement) {
        addIndentation();
        buffer.append("### Not supported yet DoStatement.\n");
    }

    public void visit(EchoStatement echoStatement) {
        openElement("EchoStatement", echoStatement, true);
        indent++;
        for (ASTNode node : echoStatement.getExpressions()) {
            node.accept(this);
        }
        indent--;
        closeElement("EchoStatement");
    }

    public void visit(EmptyStatement emptyStatement) {
        addSimpleElement("EmptyStatement", emptyStatement);
    }

    public void visit(ExpressionStatement expressionStatement) {
        addNodeDescription("ExpressionStatement", expressionStatement, true);
        indent++;
        expressionStatement.getExpression().accept(this);
        indent--;
    }

    public void visit(FieldAccess fieldAccess) {
        addNodeDescription("FieldAccess", fieldAccess, true);
        indent++;
        fieldAccess.getDispatcher().accept(this);
        fieldAccess.getField().accept(this);
        indent--;
    }

    public void visit(FieldsDeclaration fieldsDeclaration) {
        addNodeDescription("FieldsDeclaration", fieldsDeclaration, false);
        buffer.append(" modifier='").append(fieldsDeclaration.getModifierString()).append("'>").append(NEW_LINE);
        indent++;
        for (SingleFieldDeclaration node : fieldsDeclaration.getFields()) {
            addIndentation();
            buffer.append("<VariableName>\n");
            indent++;
            node.getName().accept(this);
            indent--;
            addIndentation();
            buffer.append("</VariableName>\n");
            addIndentation();
            buffer.append("<InitialValue>\n");
            Expression expr = node.getValue();
            if (expr != null) {
                indent++;
                expr.accept(this);
                indent--;
            }
            addIndentation();
            buffer.append("</InitialValue>\n");
        }
        indent--;
        addIndentation();
        buffer.append("</FieldsDeclaration>").append(NEW_LINE);

    }

    public void visit(ForEachStatement forEachStatement) {
        addIndentation();
        buffer.append("### Not supported yet ForEachStatement.\n");
    }

    public void visit(FormalParameter formalParameter) {
        addNodeDescription("FormalParameter", formalParameter, false);
        buffer.append(" isMandatory: '").append(formalParameter.isMandatory()).append("'").append(NEW_LINE);
        indent++;
        if (formalParameter.getParameterType() != null) {
            formalParameter.getParameterType().accept(this);
        } else {
            addIndentation();
            buffer.append("ParameterType null").append(NEW_LINE);
        }
        formalParameter.getParameterName().accept(this);
        if (formalParameter.getDefaultValue() != null) {
            formalParameter.getDefaultValue().accept(this);
        } else {
            addIndentation();
            buffer.append("DefaultValue null").append(NEW_LINE);
        }
        indent--;
    }

    public void visit(ForStatement forStatement) {
        addIndentation();
        buffer.append("### Not supported yet ForStatement.\n");
    }

    public void visit(FunctionDeclaration functionDeclaration) {
        addNodeDescription("FunctionDeclaration", functionDeclaration, false);
        buffer.append(" isReference: '").append(functionDeclaration.isReference()).append("'").append(NEW_LINE);
        indent++;
        functionDeclaration.getFunctionName().accept(this);
        for (FormalParameter node : functionDeclaration.getFormalParameters()) {
            node.accept(this);
        }
        functionDeclaration.getBody().accept(this);
        indent--;
    }

    public void visit(FunctionInvocation functionInvocation) {
        addIndentation();
        buffer.append("### Not supported yet FunctionInvocation.\n");
    }

    public void visit(FunctionName functionName) {
        addIndentation();
        buffer.append("### Not supported yet FunctionName.\n");
    }

    public void visit(GlobalStatement globalStatement) {
        addNodeDescription("GlobalStatement", globalStatement, true);
        indent++;
        for (Variable node : globalStatement.getVariables()) {
            node.accept(this);
        }
        indent--;
    }

    public void visit(Identifier identifier) {
        addIndentation();
        buffer.append("<Identifier");
        addOffsets(identifier);
        buffer.append(" name='").append(identifier.getName()).append("'/>").append(NEW_LINE);
    }

    public void visit(IfStatement ifStatement) {
        addIndentation();
        buffer.append("### Not supported yet IfStatement.\n");
    }

    public void visit(IgnoreError ignoreError) {
        addIndentation();
        buffer.append("### Not supported yet IgnoreError.\n");
    }

    public void visit(Include include) {
        addIndentation();
        buffer.append("### Not supported yet Include.\n");
    }

    public void visit(InfixExpression infixExpression) {
        addNodeDescription("InfixExpression", infixExpression, false);
        buffer.append(" operator: '").append(infixExpression.getOperator().name()).append("'").append(NEW_LINE);
        indent++;
        infixExpression.getLeft().accept(this);
        infixExpression.getRight().accept(this);
        indent--;
    }

    public void visit(InLineHtml inLineHtml) {
        addSimpleElement("InLineHtml", inLineHtml);
    }

    public void visit(InstanceOfExpression instanceOfExpression) {
        addIndentation();
        buffer.append("### Not supported yet InstanceOfExpression.\n");
    }

    public void visit(InterfaceDeclaration interfaceDeclaration) {
        addIndentation();
        buffer.append("### Not supported yet InterfaceDeclaration.\n");
    }

    public void visit(ListVariable listVariable) {
        addIndentation();
        buffer.append("### Not supported yet ListVariable.\n");
    }

    public void visit(MethodDeclaration methodDeclaration) {
        addIndentation();
        buffer.append("### Not supported yet MethodDeclaration.\n");
    }

    public void visit(MethodInvocation methodInvocation) {
        addIndentation();
        buffer.append("### Not supported yet MethodInvocation.\n");
    }

    public void visit(ParenthesisExpression parenthesisExpression) {
        addIndentation();
        buffer.append("### Not supported yet ParenthesisExpression.\n");
    }

    public void visit(PostfixExpression postfixExpression) {
        addIndentation();
        buffer.append("### Not supported yet PostfixExpression.\n");
    }

    public void visit(PrefixExpression prefixExpression) {
        addIndentation();
        buffer.append("### Not supported yet PrefixExpression.\n");
    }

    public void visit(Program program) {
        addIndentation();
        buffer.append("<Program");
        addOffsets(program);
        buffer.append(">").append(NEW_LINE);
        indent++;
        addIndentation();
        buffer.append("<Statements>").append(NEW_LINE);
        indent++;
        for (Statement node : program.getStatements()) {
            node.accept(this);
        }
        indent--;
        addIndentation();
        buffer.append("</Statements>").append(NEW_LINE);
        indent--;
        addIndentation();
        buffer.append("</Program>"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void visit(Quote quote) {
        addNodeDescription("Quote", quote, false);

        buffer.append(" type: '").append(quote.getQuoteType().name()).append("'");
        buffer.append(NEW_LINE);
        indent++;
        for (Expression node : quote.getExpressions()) {
            node.accept(this);
        }
        indent--;
    }

    public void visit(Reference reference) {
        addIndentation();
        buffer.append("### Not supported yet Reference.\n");
    }

    public void visit(ReflectionVariable reflectionVariable) {
        addIndentation();
        buffer.append("### Not supported yet ReflectionVariable.\n");
    }

    public void visit(ReturnStatement returnStatement) {
        addIndentation();
        buffer.append("### Not supported yet ReturnStatement.\n");
    }

    public void visit(Scalar scalar) {
        openElement("Scalar", scalar, false);
        buffer.append(" type='").append(scalar.getScalarType().name()).append("'");
        if (scalar.getStringValue() != null) {
            buffer.append(" value='").append(getXmlStringValue(scalar.getStringValue())).append("'");
        }
        buffer.append("/>").append(NEW_LINE);
    }

    public void visit(SingleFieldDeclaration singleFieldDeclaration) {
        addIndentation();
        buffer.append("### Not supported yet SingleFieldDeclaration.\n");
    }

    public void visit(StaticConstantAccess classConstantAccess) {
        addIndentation();
        buffer.append("### Not supported yet StaticConstantAccess.\n");
    }

    public void visit(StaticFieldAccess staticFieldAccess) {
        addIndentation();
        buffer.append("### Not supported yet StaticFieldAccess.\n");
    }

    public void visit(StaticMethodInvocation staticMethodInvocation) {
        openElement("StaticMethodInvocation", staticMethodInvocation, true);
        indent++;
        addIndentation();
        buffer.append("<ClassName>").append(NEW_LINE);
        indent++;
        staticMethodInvocation.getClassName().accept(this);
        indent--;
        addIndentation();
        buffer.append("</ClassName>").append(NEW_LINE);
        staticMethodInvocation.getMethod().accept(this);
        indent--;
        closeElement("StaticMethodInvocation");
    }

    public void visit(StaticStatement staticStatement) {
        addIndentation();
        buffer.append("### Not supported yet StaticStatement.\n");
    }

    public void visit(SwitchCase switchCase) {
        addIndentation();
        buffer.append("### Not supported yet SwitchCase.\n");
    }

    public void visit(SwitchStatement switchStatement) {
        addIndentation();
        buffer.append("### Not supported yet SwitchStatement.\n");
    }

    public void visit(ThrowStatement throwStatement) {
        addIndentation();
        buffer.append("### Not supported yet ThrowStatement.\n");
    }

    public void visit(TryStatement tryStatement) {
        addIndentation();
        buffer.append("### Not supported yet TryStatement.\n");
    }

    public void visit(UnaryOperation unaryOperation) {
        addIndentation();
        buffer.append("### Not supported yet UnaryOperation.\n");
    }

    public void visit(Variable variable) {
        addNodeDescription("Variable", variable, false);
        buffer.append(" isDollared: '").append(variable.isDollared()).append("'").append(NEW_LINE);
        indent++;
        variable.getName().accept(this);
        indent--;
    }

    public void visit(WhileStatement whileStatement) {
        addIndentation();
        buffer.append("### Not supported yet WhileStatement.\n");
    }

    public void visit(ASTNode node) {
        addIndentation();
        buffer.append("### Not supported yet ASTNode.\n");
    }
}
