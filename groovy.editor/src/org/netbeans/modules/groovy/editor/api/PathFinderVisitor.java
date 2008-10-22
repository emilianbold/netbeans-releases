/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.editor.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Visitor that builds path to element identified at given position
 * 
 * @todo skipping irrelevant subtrees, see IsInside(...) method
 * 
 * @author Martin Adamek
 */
public class PathFinderVisitor extends ClassCodeVisitorSupport {

    private static final Logger LOG = Logger.getLogger(PathFinderVisitor.class.getName());

    private final SourceUnit sourceUnit;
    private final int line;
    private final int column;
    private final List<ASTNode> path;

    public PathFinderVisitor(SourceUnit sourceUnit, int line, int column) {
        this.sourceUnit = sourceUnit;
        this.line = line;
        this.column = column;
        this.path = new  ArrayList<ASTNode>();
    }
    
    public List<ASTNode> getPath() {
        return path;
    }
    
    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    // super visitor doesn't visit parameters
    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);
        for (Parameter parameter : node.getParameters()) {
            isInside(parameter, line, column);
        }
    }

    @Override
    protected void visitStatement(Statement statement) {
    }

    public void visitBlockStatement(BlockStatement node) {
        if (isInside(node, line, column, false)) {
            path.add(node);
        } else {
            for (Object object : node.getStatements()) {
                if (isInside((ASTNode) object, line, column, false)) {
                    path.add(node);
                    break;
                }
            }
        }

        for (Object object : node.getStatements()) {
            Statement statement = (Statement) object;
            statement.visit(this);
        }
    }

    public void visitForLoop(ForStatement node) {
        if (isInside(node, line, column)) {
            super.visitForLoop(node);
        }
    }

    public void visitWhileLoop(WhileStatement node) {
        if (isInside(node, line, column)) {
            super.visitWhileLoop(node);
        }
    }

    public void visitDoWhileLoop(DoWhileStatement node) {
        if (isInside(node, line, column)) {
            super.visitDoWhileLoop(node);
        }
    }

    public void visitIfElse(IfStatement node) {
        if (isInside(node, line, column)) {
            super.visitIfElse(node);
        }
    }

    public void visitExpressionStatement(ExpressionStatement node) {
        if (isInside(node, line, column)) {
            super.visitExpressionStatement(node);
        }
    }

    public void visitReturnStatement(ReturnStatement node) {
        if (isInside(node, line, column)) {
            super.visitReturnStatement(node);
        }
    }

    public void visitAssertStatement(AssertStatement node) {
        if (isInside(node, line, column)) {
            super.visitAssertStatement(node);
        }
    }

    public void visitTryCatchFinally(TryCatchStatement node) {
        if (isInside(node, line, column)) {
            super.visitTryCatchFinally(node);
        }
    }

    public void visitSwitch(SwitchStatement node) {
        if (isInside(node, line, column)) {
            super.visitSwitch(node);
        }
    }

    public void visitCaseStatement(CaseStatement node) {
        if (isInside(node, line, column)) {
            super.visitCaseStatement(node);
        }
    }

    public void visitBreakStatement(BreakStatement node) {
        if (isInside(node, line, column)) {
            super.visitBreakStatement(node);
        }
    }

    public void visitContinueStatement(ContinueStatement node) {
        if (isInside(node, line, column)) {
            super.visitContinueStatement(node);
        }
    }

    public void visitThrowStatement(ThrowStatement node) {
        if (isInside(node, line, column)) {
            super.visitThrowStatement(node);
        }
    }

    public void visitSynchronizedStatement(SynchronizedStatement node) {
        if (isInside(node, line, column)) {
            super.visitSynchronizedStatement(node);
        }
    }

    public void visitCatchStatement(CatchStatement node) {
        if (isInside(node, line, column)) {
            super.visitCatchStatement(node);
        }
    }

    public void visitMethodCallExpression(MethodCallExpression node) {
        if (isInside(node, line, column)) {
            super.visitMethodCallExpression(node);
        }
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        if (isInside(node, line, column)) {
            super.visitStaticMethodCallExpression(node);
        }
    }

    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        if (isInside(node, line, column)) {
            super.visitConstructorCallExpression(node);
        }
    }

    public void visitTernaryExpression(TernaryExpression node) {
        if (isInside(node, line, column)) {
            super.visitTernaryExpression(node);
        }
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression node) {
        if (isInside(node, line, column)) {
            super.visitShortTernaryExpression(node);
        }
    }

    public void visitBinaryExpression(BinaryExpression node) {
        if (isInside(node, line, column)) {
            super.visitBinaryExpression(node);
        }
    }

    public void visitPrefixExpression(PrefixExpression node) {
        if (isInside(node, line, column)) {
            super.visitPrefixExpression(node);
        }
    }

    public void visitPostfixExpression(PostfixExpression node) {
        if (isInside(node, line, column)) {
            super.visitPostfixExpression(node);
        }
    }

    public void visitBooleanExpression(BooleanExpression node) {
        if (isInside(node, line, column)) {
            super.visitBooleanExpression(node);
        }
    }

    public void visitClosureExpression(ClosureExpression node) {
        if (isInside(node, line, column)) {
            super.visitClosureExpression(node);
        }
    }

    public void visitTupleExpression(TupleExpression node) {
        if (isInside(node, line, column)) {
            super.visitTupleExpression(node);
        }
    }

    public void visitMapExpression(MapExpression node) {
        if (isInside(node, line, column)) {
            super.visitMapExpression(node);
        }
    }

    public void visitMapEntryExpression(MapEntryExpression node) {
        if (isInside(node, line, column)) {
            super.visitMapEntryExpression(node);
        }
    }

    public void visitListExpression(ListExpression node) {
        if (isInside(node, line, column)) {
            super.visitListExpression(node);
        }
    }

    public void visitRangeExpression(RangeExpression node) {
        if (isInside(node, line, column)) {
            super.visitRangeExpression(node);
        }
    }

    public void visitPropertyExpression(PropertyExpression node) {

        // XXX PropertyExpression has wrong offsets, e.g. 4-4 for 'this.field1 = 77'
        // and was never added to path,
        // therefore let's check if its children are wraping given position
        // and add it then

        Expression objectExpression = node.getObjectExpression();
        Expression property = node.getProperty();
        
        if (isInside(node, line, column, false)) {
            path.add(node);
        } else {
            boolean nodeAdded = false;
            if (isInside(objectExpression, line, column, false)) {
                path.add(node);
                nodeAdded = true;
            }
            if (isInside(property, line, column, false)) {
                if (!nodeAdded) {
                    path.add(node);
                }
            }
        }

        objectExpression.visit(this);
        property.visit(this);
    }

    public void visitAttributeExpression(AttributeExpression node) {
        if (isInside(node, line, column)) {
            super.visitAttributeExpression(node);
        }
    }

    public void visitFieldExpression(FieldExpression node) {
        if (isInside(node, line, column)) {
            super.visitFieldExpression(node);
        }
    }

    public void visitMethodPointerExpression(MethodPointerExpression node) {
        if (isInside(node, line, column)) {
            super.visitMethodPointerExpression(node);
        }
    }

    public void visitConstantExpression(ConstantExpression node) {
        if (isInside(node, line, column)) {
            super.visitConstantExpression(node);
        }
    }

    public void visitClassExpression(ClassExpression node) {
        if (isInside(node, line, column)) {
            super.visitClassExpression(node);
        }
    }

    public void visitVariableExpression(VariableExpression node) {
        if (isInside(node, line, column)) {
//            System.out.println("### VariableExpression " + node.getLineNumber() + ", " + node.getColumnNumber() + ", " + node.getLastLineNumber() + ", " + node.getLastColumnNumber());
            super.visitVariableExpression(node);
        }
    }

    public void visitDeclarationExpression(DeclarationExpression node) {
        if (isInside(node, line, column)) {
            super.visitDeclarationExpression(node);
        }
    }

    public void visitRegexExpression(RegexExpression node) {
        if (isInside(node, line, column)) {
            super.visitRegexExpression(node);
        }
    }

    public void visitGStringExpression(GStringExpression node) {
        if (isInside(node, line, column)) {
            super.visitGStringExpression(node);
        }
    }

    public void visitArrayExpression(ArrayExpression node) {
        if (isInside(node, line, column)) {
            super.visitArrayExpression(node);
        }
    }

    public void visitSpreadExpression(SpreadExpression node) {
        if (isInside(node, line, column)) {
            super.visitSpreadExpression(node);
        }
    }

    public void visitSpreadMapExpression(SpreadMapExpression node) {
        if (isInside(node, line, column)) {
            super.visitSpreadMapExpression(node);
        }
    }

    public void visitNotExpression(NotExpression node) {
        if (isInside(node, line, column)) {
            super.visitNotExpression(node);
        }
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        if (isInside(node, line, column)) {
            super.visitUnaryMinusExpression(node);
        }
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        if (isInside(node, line, column)) {
            super.visitUnaryPlusExpression(node);
        }
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        if (isInside(node, line, column)) {
            super.visitBitwiseNegationExpression(node);
        }
    }

    public void visitCastExpression(CastExpression node) {
        if (isInside(node, line, column)) {
            super.visitCastExpression(node);
        }
    }

    public void visitArgumentlistExpression(ArgumentListExpression node) {
        if (isInside(node, line, column)) {
            super.visitArgumentlistExpression(node);
        }
    }

    public void visitClosureListExpression(ClosureListExpression node) {
        if (isInside(node, line, column)) {
            super.visitClosureListExpression(node);
        }
    }

    public void visitClass(ClassNode node) {
        if (isInside(node, line, column)) {
            super.visitClass(node);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        if (isInside(node, line, column)) {
            super.visitConstructor(node);
        }
    }

    public void visitMethod(MethodNode node) {
        if (isInside(node, line, column)) {
            super.visitMethod(node);
        }
    }

    public void visitField(FieldNode node) {
        if (isInside(node, line, column)) {
            super.visitField(node);
        }
    }

    public void visitProperty(PropertyNode node) {
        if (isInside(node, line, column)) {
            super.visitProperty(node);
        }
    }

    private boolean isInside(ASTNode node, int line, int column) {
        return isInside(node, line, column, true);
    }
    
    private boolean isInside(ASTNode node, int line, int column, boolean addToPath) {

        fixCoordinates(node);
        int beginLine = node.getLineNumber();
        int beginColumn = node.getColumnNumber();
        int endLine = node.getLastLineNumber();
        int endColumn = node.getLastColumnNumber();
        
        LOG.finest("isInside: " + node + " - " + beginLine + ", " + beginColumn + ", " + endLine + ", " + endColumn);
        
        if (beginLine == -1 || beginColumn == -1 || endLine == -1 || endColumn == -1) {
            // this node doesn't provide its coordinates, some wrappers do that
            // let's say yes and visit its children
            return addToPath ? true : false;
        }
        
        boolean result = false;

        if (beginLine == endLine) {
            if (line == beginLine && column >= beginColumn && column < endColumn) {
                result = true;
            }
        } else if (line == beginLine) {
            if (column >= beginColumn) {
                result = true;
            }
        } else if (line == endLine) {
            if (column < endColumn) {
                result = true;
            }
        } else if (beginLine < line && line < endLine) {
            result = true;
        } else {
            result = false;
        }
        
        if (result && addToPath) {
            path.add(node);
            LOG.finest("Path:" + path);
        }

        // if addToPath is false, return result, we want to know real state of affairs
        // and not to continue traversing
        return addToPath ? true : result;
    }

    private void fixCoordinates(ASTNode node) {
        // noop for Groovy 1.5.7
    }

}
