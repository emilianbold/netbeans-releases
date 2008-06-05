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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Petr Pisl
 */
public class PrintASTVisitor implements Visitor {

    private StringBuffer buffer;
    private final static String NEW_LINE = "\n";
    private final static String TAB = "    ";
    private int indent;

    
    private class XMLPrintNode {
        
        private class GroupItem {
            private final String groupName;
            private final List<ASTNode> group;

            public GroupItem(String groupName, List<ASTNode> group) {
                this.groupName = groupName;
                this.group = group;
            }

            public List<ASTNode> getGroup() {
                return group;
            }

            public String getGroupName() {
                return groupName;
            }
        }
        
        private ASTNode node;
        private String name;
        private String[] attributes;
        // <name of children group, childrens>
        private List<GroupItem> childrenGroups;
 
        public XMLPrintNode(ASTNode node, String name){
            this(node, name, new String[]{});
        }
        
        public XMLPrintNode(ASTNode node, String name, String[] attributes){
            this.node = node;
            this.name = name;
            this.attributes = attributes;
            this.childrenGroups = new ArrayList <GroupItem> ();
        }
                
        public void addChildrenGroup(String groupName, ASTNode[] groupChildren) {
            ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
            for (int i = 0; i < groupChildren.length; i++) {
               nodes.add(groupChildren[i]);
            }
            addChildrenGroup(groupName, nodes);
        }
        
        public void addChildrenGroup(String groupName, List nodes) {
            if (nodes != null) {
                if (this.childrenGroups == null) {
                    this.childrenGroups = new ArrayList<GroupItem>();
                }
                this.childrenGroups.add(new GroupItem(groupName, nodes));
            }
        }
        
        public void addChildren(List nodes) {
            if (nodes != null)
                addChildrenGroup("", nodes);
        }
        
        public void addChild(ASTNode node) {
            ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
            nodes.add(node);
            addChildrenGroup("", nodes);
        }
        
        public void print(Visitor visitor) {
            addIndentation();
            buffer.append("<").append(name);
            addOffsets(node);
            for (int i = 0; i < attributes.length; i++) {
                String attrName = attributes[i];
                String attrValue = attributes[++i];
                if (attrValue == null) {
                    attrValue = "null";
                }
                buffer.append(" ").append(attrName).append("='").append(attrValue).append("'");
            }
            if (childrenGroups.size() > 0) {
                buffer.append(">").append(NEW_LINE);
                indent++;
                for (GroupItem groupItem : childrenGroups) {
                    if (groupItem.getGroupName().length() > 0) {
                        addIndentation();
                        buffer.append("<").append(groupItem.getGroupName()).append(">").append(NEW_LINE);
                        indent++;
                    }
                    if (groupItem.getGroup() != null) {
                        for (ASTNode aSTNode : groupItem.getGroup()) {
                            if (aSTNode != null) {
                                aSTNode.accept(visitor);
                            }
                        }
                    }
                    if (groupItem.getGroupName().length() > 0) {
                        indent--;
                        addIndentation();
                        buffer.append("</").append(groupItem.getGroupName()).append(">").append(NEW_LINE);
                    }
                }
                indent--;
                addIndentation();
                buffer.append("</").append(name).append(">").append(NEW_LINE);
            }
            else {
                buffer.append("/>").append(NEW_LINE);
            }
        }
    }
 
    public String printTree(ASTNode node) {
        return printTree(node, 0);
    }
    
    public String printTree(ASTNode node, int startindent) {
        buffer = new StringBuffer();
        indent = startindent;
        node.accept(this);
        return buffer.toString();
    }

    private void addOffsets(ASTNode node) {
        buffer.append(" start='").append(node.getStartOffset()).append("' end='").append(node.getEndOffset()).append("'");
    }

    protected void addIndentation() {
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
    public static String getXmlStringValue(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("\n","\\\\n");
        escapedString = escapedString.replaceAll("\r","\\\\r");
        escapedString = escapedString.replaceAll("\t","\\\\t");
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

    public void visit(ArrayAccess node) {
        XMLPrintNode printNode = new XMLPrintNode(node, "ArrayAccess",
                new String[]{ "type", node.getArrayType().name(),
                    "isDollared", (node.isDollared()?"true":"false")});
        printNode.addChildrenGroup("Index", new ASTNode[]{node.getIndex()});
        printNode.addChildrenGroup("Name", new ASTNode[]{node.getName()});
        printNode.print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(assignment, "Assignment",
                new String[]{"operator", assignment.getOperator().name()});
        printNode.addChild(assignment.getLeftHandSide());
        printNode.addChild(assignment.getRightHandSide());
        printNode.print(this);
    }

    public void visit(ASTError astError) {
        (new XMLPrintNode(astError, "ASTError")).print(this);
    }

    public void visit(BackTickExpression backTickExpression) {
        addIndentation();
        buffer.append("### Not supported yet BackTickExpression.\n");
    }

    public void visit(Block block) {
        XMLPrintNode printNode = new XMLPrintNode(block, "Block", 
                new String[]{"isCurly", (block.isCurly()?"true":"flase")});
        printNode.addChildren(block.getStatements());
        printNode.print(this);
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

    public void visit(ClassConstantDeclaration node) {
        XMLPrintNode printNode = new XMLPrintNode(node, "ClassConstantDeclaration");
        printNode.addChildrenGroup("Names", node.getNames());
        printNode.addChildrenGroup("Initializers", node.getInitializers());
        printNode.print(this);
    }

    public void visit(ClassDeclaration classDeclaration) {
        XMLPrintNode printNode = new XMLPrintNode(classDeclaration, "ClassDeclaration",
                new String[]{"modifier", classDeclaration.getModifier().name()});
        printNode.addChildrenGroup("ClassName", new ASTNode[]{classDeclaration.getName()});
        printNode.addChildrenGroup("SuperClassName", new ASTNode[]{classDeclaration.getSuperClass()});
        printNode.addChildrenGroup("Interfaces", classDeclaration.getInterfaes());
        printNode.addChild(classDeclaration.getBody());
        printNode.print(this);
    }

    public void visit(ClassInstanceCreation classInstanceCreation) {
        addIndentation();
        buffer.append("### Not supported yet ClassInstanceCreation.\n");
    }

    public void visit(ClassName className) {
        XMLPrintNode printNode = new XMLPrintNode(className, "ClassName");
        printNode.addChild(className.getName());
        printNode.print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(echoStatement, "EchoStatement");
        printNode.addChildren(echoStatement.getExpressions());
        printNode.print(this);
    }

    public void visit(EmptyStatement emptyStatement) {
        (new XMLPrintNode(emptyStatement, "EmptyStatement")).print(this);
    }

    public void visit(ExpressionStatement expressionStatement) {
        XMLPrintNode printNode = new XMLPrintNode(expressionStatement, "ExpressionStatement");
        printNode.addChild(expressionStatement.getExpression());
        printNode.print(this);
    }

    public void visit(FieldAccess fieldAccess) {
        XMLPrintNode printNode = new XMLPrintNode(fieldAccess, "FieldAccess");
        printNode.addChild(fieldAccess.getDispatcher());
        printNode.addChild(fieldAccess.getField());
        printNode.print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(functionInvocation, "FunctionInvocation");
        printNode.addChild(functionInvocation.getFunctionName());
        printNode.addChildrenGroup("Parameters", functionInvocation.getParameters());
        printNode.print(this);
    }

    public void visit(FunctionName functionName) {
       XMLPrintNode printNode = new XMLPrintNode(functionName, "FucntionName");
       printNode.addChild(functionName.getName());
       printNode.print(this);
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
        (new XMLPrintNode(identifier, "Identifier", new String[]{"name", identifier.getName()})).print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(include, "Include", 
                new String [] {"type", include.getIncludeType().name() });
        printNode.addChild(include.getExpression());
        printNode.print(this);
    }

    public void visit(InfixExpression infixExpression) {
        XMLPrintNode printNode = new XMLPrintNode(infixExpression, "InfixExpression",
                new String[]{"operator", infixExpression.getOperator().name()});
        printNode.addChild(infixExpression.getLeft());
        printNode.addChild(infixExpression.getRight());
        printNode.print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(program, "Program");
        printNode.addChildrenGroup("Statements", program.getStatements());
        printNode.print(this);
    }

    public void visit(Quote quote) {
        XMLPrintNode xmlNode = new XMLPrintNode(quote, "Quote", new String[]{"type", quote.getQuoteType().name()});
        xmlNode.addChildrenGroup("Expressions", quote.getExpressions());
        xmlNode.print(this);
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
        (new XMLPrintNode(scalar, "Scalar", 
                new String[]{"type", scalar.getScalarType().name(),
                "value", getXmlStringValue(scalar.getStringValue())})).print(this);
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
        XMLPrintNode printNode = new XMLPrintNode(variable, "Variable", 
                new String[]{"isDollared", (variable.isDollared()?"true":"false")});
         
        printNode.addChild(variable.getName());
        printNode.print(this);
    }

    public void visit(WhileStatement whileStatement) {
        addIndentation();
        buffer.append("### Not supported yet WhileStatement.\n");
    }

    public void visit(ASTNode node) {
        // this node shouldn't appear in the result.
        (new XMLPrintNode(node, "ASTNode")).print(this);
    }

    public void visit(PHPDocBlock phpDocBlock) {
        addIndentation();
        buffer.append("### Not supported yet phpDocBlock.\n");
    }
}
